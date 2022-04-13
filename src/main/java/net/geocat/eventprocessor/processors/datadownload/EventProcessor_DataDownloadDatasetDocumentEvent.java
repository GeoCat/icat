/*
 *  =============================================================================
 *  ===  Copyright (C) 2021 Food and Agriculture Organization of the
 *  ===  United Nations (FAO-UN), United Nations World Food Programme (WFP)
 *  ===  and United Nations Environment Programme (UNEP)
 *  ===
 *  ===  This program is free software; you can redistribute it and/or modify
 *  ===  it under the terms of the GNU General Public License as published by
 *  ===  the Free Software Foundation; either version 2 of the License, or (at
 *  ===  your option) any later version.
 *  ===
 *  ===  This program is distributed in the hope that it will be useful, but
 *  ===  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  ===  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  ===  General Public License for more details.
 *  ===
 *  ===  You should have received a copy of the GNU General Public License
 *  ===  along with this program; if not, write to the Free Software
 *  ===  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *  ===
 *  ===  Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 *  ===  Rome - Italy. email: geonetwork@osgeo.org
 *  ===
 *  ===  Development of this program was financed by the European Union within
 *  ===  Service Contract NUMBER – 941143 – IPR – 2021 with subject matter
 *  ===  "Facilitating a sustainable evolution and maintenance of the INSPIRE
 *  ===  Geoportal", performed in the period 2021-2023.
 *  ===
 *  ===  Contact: JRC Unit B.6 Digital Economy, Via Enrico Fermi 2749,
 *  ===  21027 Ispra, Italy. email: JRC-INSPIRE-SUPPORT@ec.europa.eu
 *  ==============================================================================
 */

package net.geocat.eventprocessor.processors.datadownload;

import net.geocat.database.linkchecker.entities.LinkCheckJob;
import net.geocat.database.linkchecker.entities.LocalDatasetMetadataRecord;
import net.geocat.database.linkchecker.entities.OGCRequest;
import net.geocat.database.linkchecker.entities.SimpleAtomLinkToData;
import net.geocat.database.linkchecker.entities.SimpleLayerDatasetIdDataLink;
import net.geocat.database.linkchecker.entities.SimpleLayerMetadataUrlDataLink;
import net.geocat.database.linkchecker.entities.SimpleSpatialDSIDDataLink;
import net.geocat.database.linkchecker.entities.SimpleStoredQueryDataLink;
import net.geocat.database.linkchecker.entities.helper.LinkToData;
import net.geocat.database.linkchecker.entities.helper.ServiceMetadataDocumentState;
import net.geocat.database.linkchecker.repos.LocalDatasetMetadataRecordRepo;
import net.geocat.database.linkchecker.service.LinkCheckJobService;
import net.geocat.eventprocessor.BaseEventProcessor;
import net.geocat.eventprocessor.processors.datadownload.downloaders.AtomDownloadProcessor;
import net.geocat.eventprocessor.processors.datadownload.downloaders.OGCInfoCacheItem;
import net.geocat.eventprocessor.processors.datadownload.downloaders.OGCRequestGenerator;
import net.geocat.eventprocessor.processors.datadownload.downloaders.OGCRequestResolver;
import net.geocat.events.Event;
import net.geocat.events.EventFactory;
import net.geocat.events.EventService;
import net.geocat.events.datadownload.DataDownloadDatasetDocumentEvent;
import net.geocat.model.LinkCheckRunConfig;
import net.geocat.service.helper.SharedForkJoinPool2;
import net.geocat.service.helper.ShouldTransitionOutOfDataDownloading;
import net.geocat.xml.helpers.CapabilitiesType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import static net.geocat.database.linkchecker.service.DatabaseUpdateService.convertToString;


@Component
@Scope("prototype")
public class EventProcessor_DataDownloadDatasetDocumentEvent extends BaseEventProcessor<DataDownloadDatasetDocumentEvent> {

    Logger logger = LoggerFactory.getLogger(EventProcessor_DataDownloadDatasetDocumentEvent.class);

    @Autowired
    LinkCheckJobService linkCheckJobService;

    @Autowired
    LocalDatasetMetadataRecordRepo localDatasetMetadataRecordRepo;

    @Autowired
    EventFactory eventFactory;

    @Autowired
    ShouldTransitionOutOfDataDownloading shouldTransitionOutOfDataDownloading;

    @Autowired
    OGCRequestGenerator ogcRequestGenerator;

    @Autowired
    OGCRequestResolver ogcRequestResolver;

    @Autowired
    SharedForkJoinPool2 sharedForkJoinPool2;

    @Autowired
    AtomDownloadProcessor atomDownloadProcessor;

    LocalDatasetMetadataRecord localDatasetMetadataRecord;

    @Override
    public EventProcessor_DataDownloadDatasetDocumentEvent externalProcessing() throws Exception {
        localDatasetMetadataRecord = localDatasetMetadataRecordRepo.findById(getInitiatingEvent().getDatasetDocumentId()).get();// make sure we re-load
        if (localDatasetMetadataRecord.getDataLinks().isEmpty()) {
            // no links to data -> nothing to download
            localDatasetMetadataRecord.setState(ServiceMetadataDocumentState.DATADOWNLOADED);

            localDatasetMetadataRecord.setNumberOfDownloadDataLinks(0);
            localDatasetMetadataRecord.setNumberOfDownloadLinksAttempted(0);
            localDatasetMetadataRecord.setNumberOfDownloadLinksSuccessful(0);

            localDatasetMetadataRecord.setNumberOfViewDataLinks(0);
            localDatasetMetadataRecord.setNumberOfViewLinksAttempted(0);
            localDatasetMetadataRecord.setNumberOfViewLinksSuccessful(0);

            save();
        }
        else
        {
            try{
                process();
                localDatasetMetadataRecord.setState(ServiceMetadataDocumentState.DATADOWNLOADED);
                save();
                logger.debug("finished DATADOWNLOADED dataset documentid="+getInitiatingEvent().getDatasetDocumentId()  );

            }
            catch(Exception e){
                logger.error("DATADOWNLOADED exception for datasetMetadataRecordId="+getInitiatingEvent().getDatasetDocumentId(),e);
                localDatasetMetadataRecord.setState(ServiceMetadataDocumentState.ERROR);
                localDatasetMetadataRecord.setErrorMessage(  convertToString(e) );
                save();
            }
        }

        return this;
    }

    private void process() throws Exception {
        //break into download and view links
        if ( (localDatasetMetadataRecord.getDataLinks() == null) || (localDatasetMetadataRecord.getDataLinks().isEmpty()))
            return;

        List<LinkToData> viewLinks = localDatasetMetadataRecord.getDataLinks().stream()
                .filter(x->x.getCapabilitiesDocumentType() == CapabilitiesType.WMS ||x.getCapabilitiesDocumentType() == CapabilitiesType.WMTS )
                .collect(Collectors.toList());
        List<LinkToData> downloadLinks = localDatasetMetadataRecord.getDataLinks().stream()
                .filter(x->x.getCapabilitiesDocumentType() == CapabilitiesType.WFS ||x.getCapabilitiesDocumentType() == CapabilitiesType.Atom)
                .collect(Collectors.toList());

        Map<String, OGCInfoCacheItem> ogcInfoCache = new HashMap<>();
        List<String> cap_sha2s = localDatasetMetadataRecord.getDataLinks(). stream()
                .map(x->x.getCapabilitiesSha2())
                .distinct()
                .collect(Collectors.toList());

        for (String capSha2 : cap_sha2s) {
            OGCInfoCacheItem ogcInfoCacheItem = ogcRequestGenerator.prep(localDatasetMetadataRecord.getLinkCheckJobId(), capSha2);
            ogcInfoCache.put(capSha2, ogcInfoCacheItem);
        }
        LinkCheckJob job = linkCheckJobService.getJobInfo(localDatasetMetadataRecord.getLinkCheckJobId(),false);
        processView(viewLinks,ogcInfoCache,job);
        processDownload(downloadLinks,ogcInfoCache,job);
     }

    private void processDownload(List<LinkToData> downloadLinks, Map<String, OGCInfoCacheItem> ogcInfoCache, LinkCheckJob job) {
        int MAX_LINKS_TO_FOLLOW = job==null ? LinkCheckRunConfig.maxDataLinksToFollow_default : job.getMaxDataLinksToFollow();

        if (downloadLinks.size() > MAX_LINKS_TO_FOLLOW) {
            // we want to always grab the same set of links...
            Collections.sort(downloadLinks,( link1,link2) ->{
                return link1.key().compareToIgnoreCase(link2.key());
            });
            downloadLinks = downloadLinks.subList(0,MAX_LINKS_TO_FOLLOW);
        }
        localDatasetMetadataRecord.setNumberOfDownloadLinksAttempted(downloadLinks.size());
        List<LinkToData> downloadLinksToProcess = downloadLinks;


        downloadLinksToProcess.stream()
                .forEach(x -> {
                    processSingleDownload(x, ogcInfoCache);
                });

        long numberSuccessful = downloadLinksToProcess.stream()
                .filter(x->x.getSuccessfullyDownloaded())
                .count();
        localDatasetMetadataRecord.setNumberOfDownloadLinksSuccessful((int)numberSuccessful);
    }



    private void processView(List<LinkToData> viewLinks, Map<String, OGCInfoCacheItem> ogcInfoCache, LinkCheckJob job) throws Exception {
        int MAX_LINKS_TO_FOLLOW = job==null ? LinkCheckRunConfig.maxDataLinksToFollow_default : job.getMaxDataLinksToFollow();

         if (viewLinks.size() > MAX_LINKS_TO_FOLLOW) {
             Collections.sort(viewLinks,( link1,link2) ->{
                 return link1.key().compareToIgnoreCase(link2.key());
             });
            viewLinks = viewLinks.subList(0,MAX_LINKS_TO_FOLLOW);
        }
        localDatasetMetadataRecord.setNumberOfViewLinksAttempted(viewLinks.size());

        ForkJoinPool pool = sharedForkJoinPool2.getPool();

        List<LinkToData> viewLinksToProcess = viewLinks;

        pool.submit(() ->
                viewLinksToProcess.stream().parallel()
                        .forEach(x -> {
                            processSingleView(x, ogcInfoCache);
                        })
        ).get();


        long numberSuccessful = viewLinksToProcess.stream()
                .filter(x->x.getSuccessfullyDownloaded())
                .count();

        localDatasetMetadataRecord.setNumberOfViewLinksSuccessful((int)numberSuccessful);
    }

    public void processSingleDownload(LinkToData link, Map<String, OGCInfoCacheItem> ogcInfoCache)  {
        try {
            if (link instanceof SimpleLayerMetadataUrlDataLink) {
                SimpleLayerMetadataUrlDataLink _link = (SimpleLayerMetadataUrlDataLink) link;
                processDownloadLink_SimpleLayerMetadataUrlDataLink(_link, ogcInfoCache);
                boolean successful =  (_link.getOgcRequest().isSuccessfulOGCRequest() == null) ? false : _link.getOgcRequest().isSuccessfulOGCRequest();
                link.setSuccessfullyDownloaded(successful);
            }
            if (link instanceof SimpleLayerDatasetIdDataLink) {
                SimpleLayerDatasetIdDataLink _link = (SimpleLayerDatasetIdDataLink) link;
                processDownloadLink_SimpleLayerDatasetIdDataLink(_link, ogcInfoCache);
                boolean successful = (_link.getOgcRequest().isSuccessfulOGCRequest() == null) ? false : _link.getOgcRequest().isSuccessfulOGCRequest();
                link.setSuccessfullyDownloaded(successful);
            }
            if (link instanceof SimpleSpatialDSIDDataLink){
                SimpleSpatialDSIDDataLink _link = (SimpleSpatialDSIDDataLink) link;
                processDownloadLink_SimpleSpatialDSIDDataLink(_link, ogcInfoCache);
                boolean successful = (_link.getOgcRequest().isSuccessfulOGCRequest() == null) ? false : _link.getOgcRequest().isSuccessfulOGCRequest();
                link.setSuccessfullyDownloaded(successful);
            }
            if (link instanceof SimpleStoredQueryDataLink){
                SimpleStoredQueryDataLink _link = (SimpleStoredQueryDataLink) link;
                processDownloadLink_SimpleStoredQueryDataLink(_link, ogcInfoCache);
                boolean successful = (_link.getOgcRequest().isSuccessfulOGCRequest() == null) ? false : _link.getOgcRequest().isSuccessfulOGCRequest();
                link.setSuccessfullyDownloaded(successful);
            }
            if (link instanceof SimpleAtomLinkToData){
                SimpleAtomLinkToData _link = (SimpleAtomLinkToData) link;
                processDownloadLink_SimpleAtomLinkToData(_link, ogcInfoCache);
              //  boolean successful = (_link.getOgcRequest().isSuccessfulOGCRequest() == null) ? false : _link.getOgcRequest().isSuccessfulOGCRequest();
            //    link.setSuccessfullyDownloaded(successful);
            }
        }
        catch (Exception e){
            link.setSuccessfullyDownloaded(false);
            link.setErrorInfo(e.getClass().getSimpleName()+" - "+e.getMessage());
            logger.debug("exception occurred while attempting to download a download", e);
        }
    }

    private void processDownloadLink_SimpleAtomLinkToData(SimpleAtomLinkToData link, Map<String, OGCInfoCacheItem> ogcInfoCache) throws Exception {
        atomDownloadProcessor.process(link, ogcInfoCache.get(link.getCapabilitiesSha2()));

    }

    private void processDownloadLink_SimpleSpatialDSIDDataLink(SimpleSpatialDSIDDataLink link, Map<String, OGCInfoCacheItem> ogcInfoCache) throws Exception {
        OGCRequest ogcRequest = ogcRequestGenerator.prepareToDownload(link,ogcInfoCache.get(link.getCapabilitiesSha2()));
        link.setOgcRequest(ogcRequest);
        ogcRequestResolver.resolve(ogcRequest);
    }

    private void processDownloadLink_SimpleStoredQueryDataLink(SimpleStoredQueryDataLink link, Map<String, OGCInfoCacheItem> ogcInfoCache) throws Exception {
        OGCRequest ogcRequest = ogcRequestGenerator.prepareToDownload(link,ogcInfoCache.get(link.getCapabilitiesSha2()));
        link.setOgcRequest(ogcRequest);
        ogcRequestResolver.resolve(ogcRequest);
    }

    private void processDownloadLink_SimpleLayerDatasetIdDataLink(SimpleLayerDatasetIdDataLink link, Map<String, OGCInfoCacheItem> ogcInfoCache) throws Exception {
        OGCRequest ogcRequest = ogcRequestGenerator.prepareToDownload(link,ogcInfoCache.get(link.getCapabilitiesSha2()));
        link.setOgcRequest(ogcRequest);
        ogcRequestResolver.resolve(ogcRequest);
    }

    private void processDownloadLink_SimpleLayerMetadataUrlDataLink(SimpleLayerMetadataUrlDataLink link, Map<String, OGCInfoCacheItem> ogcInfoCache) throws Exception {
        OGCRequest ogcRequest = ogcRequestGenerator.prepareToDownload(link,ogcInfoCache.get(link.getCapabilitiesSha2()));
        link.setOgcRequest(ogcRequest);
        ogcRequestResolver.resolve(ogcRequest);
    }


    public void processSingleView(LinkToData link, Map<String, OGCInfoCacheItem> ogcInfoCache)  {
        try {
            if (link instanceof SimpleLayerMetadataUrlDataLink) {
                SimpleLayerMetadataUrlDataLink _link = (SimpleLayerMetadataUrlDataLink) link;
                processViewLink_SimpleLayerMetadataUrlDataLink(_link, ogcInfoCache);
                boolean successful =  (_link.getOgcRequest().isSuccessfulOGCRequest() == null) ? false : _link.getOgcRequest().isSuccessfulOGCRequest();
                link.setSuccessfullyDownloaded(successful);
            }
            if (link instanceof SimpleLayerDatasetIdDataLink) {
                SimpleLayerDatasetIdDataLink _link = (SimpleLayerDatasetIdDataLink) link;
                processViewLink_SimpleLayerDatasetIdDataLink(_link, ogcInfoCache);
                boolean successful = (_link.getOgcRequest().isSuccessfulOGCRequest() == null) ? false : _link.getOgcRequest().isSuccessfulOGCRequest();
                link.setSuccessfullyDownloaded(successful);
            }
            if (link instanceof SimpleSpatialDSIDDataLink){
                SimpleSpatialDSIDDataLink _link = (SimpleSpatialDSIDDataLink) link;
                processViewLink_SimpleSpatialDSIDDataLink(_link, ogcInfoCache);
                boolean successful = (_link.getOgcRequest().isSuccessfulOGCRequest() == null) ? false : _link.getOgcRequest().isSuccessfulOGCRequest();
                link.setSuccessfullyDownloaded(successful);
            }
            //throw new Exception("don't know how to process - "+link.getClass().getCanonicalName());
        }
        catch (Exception e){
            link.setSuccessfullyDownloaded(false);
            logger.debug("exception occurred while attempting to download a view", e);
        }
     }
    private void processViewLink_SimpleSpatialDSIDDataLink(SimpleSpatialDSIDDataLink link, Map<String, OGCInfoCacheItem> ogcInfoCache) throws Exception {
        OGCRequest ogcRequest = ogcRequestGenerator.prepareToDownload(link,ogcInfoCache.get(link.getCapabilitiesSha2()));
        link.setOgcRequest(ogcRequest);
        ogcRequestResolver.resolve(ogcRequest);
    }

    private void processViewLink_SimpleLayerDatasetIdDataLink(SimpleLayerDatasetIdDataLink link, Map<String, OGCInfoCacheItem> ogcInfoCache) throws Exception {
        OGCRequest ogcRequest = ogcRequestGenerator.prepareToDownload(link,ogcInfoCache.get(link.getCapabilitiesSha2()));
        link.setOgcRequest(ogcRequest);
        ogcRequestResolver.resolve(ogcRequest);
    }

    private void processViewLink_SimpleLayerMetadataUrlDataLink(SimpleLayerMetadataUrlDataLink link, Map<String, OGCInfoCacheItem> ogcInfoCache) throws Exception {
        OGCRequest ogcRequest = ogcRequestGenerator.prepareToDownload(link,ogcInfoCache.get(link.getCapabilitiesSha2()));
        link.setOgcRequest(ogcRequest);
        ogcRequestResolver.resolve(ogcRequest);
    }


    public void save()
    {
        localDatasetMetadataRecord = localDatasetMetadataRecordRepo.save(localDatasetMetadataRecord);
    }

    @Override
    public EventProcessor_DataDownloadDatasetDocumentEvent internalProcessing() throws Exception {

        return this;
    }

    @Override
    public List<Event> newEventProcessing () {
        List<Event> result = new ArrayList<>();

        String linkCheckJobId = getInitiatingEvent().getLinkCheckJobId();

        if (shouldTransitionOutOfDataDownloading.shouldSendMessage(linkCheckJobId, getInitiatingEvent().getDatasetDocumentId())) {
            //done
            Event e = eventFactory.createAllDataDownloadedEvent(linkCheckJobId);
            result.add(e);
        }
        return result;
    }
}