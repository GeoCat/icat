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

package net.geocat.eventprocessor.processors.datadownload.downloaders;

import net.geocat.database.linkchecker.entities.AtomActualDataEntry;
import net.geocat.database.linkchecker.entities.LinkCheckJob;
import net.geocat.database.linkchecker.entities.SimpleAtomLinkToData;
import net.geocat.database.linkchecker.entities.helper.AtomDataRequest;
import net.geocat.database.linkchecker.entities.helper.AtomSubFeedRequest;
import net.geocat.database.linkchecker.service.LinkCheckJobService;
import net.geocat.events.EventService;
import net.geocat.model.LinkCheckRunConfig;
import net.geocat.service.downloadhelpers.RetrievableSimpleLinkDownloader;
import net.geocat.xml.XmlCapabilitiesAtom;
import net.geocat.xml.XmlDoc;
import net.geocat.xml.XmlDocumentFactory;
import net.geocat.xml.XmlStringTools;
import net.geocat.xml.helpers.AtomEntry;
import net.geocat.xml.helpers.AtomLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class AtomDownloadProcessor {

    private static final Logger logger = LoggerFactory.getLogger( AtomDownloadProcessor.class);

    private static int HEADER_LENGTH = 4096;
    private static String ACCEPT_MIME = "*/*";

    @Autowired
    AtomLayerDownloader atomLayerDownloader;

    @Autowired
    XmlDocumentFactory xmlDocumentFactory;

    @Autowired
    RetrievableSimpleLinkDownloader retrievableSimpleLinkDownloader;

    @Autowired
    LinkCheckJobService linkCheckJobService;


    public void process(SimpleAtomLinkToData simpleAtomLinkToData,OGCInfoCacheItem ogcInfoCacheItem) throws Exception {
        if ( (simpleAtomLinkToData ==null) || (ogcInfoCacheItem ==null))
            return;

        AtomSubFeedRequest subFeedRequest= atomLayerDownloader.createSubFeedRequest(
                (XmlCapabilitiesAtom)ogcInfoCacheItem.getXmlCapabilitiesDocument(),
                simpleAtomLinkToData.getLayerId(),
                simpleAtomLinkToData.getLinkCheckJobId());
        simpleAtomLinkToData.setAtomSubFeedRequest(subFeedRequest);

        atomLayerDownloader.resolve(subFeedRequest);
        atomLayerDownloader.validate(subFeedRequest);
        if (!subFeedRequest.getSuccessfulAtomRequest()) {
            simpleAtomLinkToData.setSuccessfullyDownloaded(false);
            return;
        }
        String xmlAtomFeed2 = XmlStringTools.bytea2String(subFeedRequest.getFullData());
        XmlDoc doc =  xmlDocumentFactory.create(xmlAtomFeed2);
        if (!(doc instanceof  XmlCapabilitiesAtom)) {
            simpleAtomLinkToData.setSuccessfullyDownloaded(false);
            return;
        }
        XmlCapabilitiesAtom secondaryAtom = (XmlCapabilitiesAtom) doc;
        if (secondaryAtom.getEntries().isEmpty()) {
            simpleAtomLinkToData.setSuccessfullyDownloaded(false);
            return;
        }
        simpleAtomLinkToData.setAtomActualDataEntryList(new ArrayList<>());
        int index=0;

        //okay, 2nd phase -- try to find one dataset that fully downloads!
        List<AtomEntry> secondaryEntries = new ArrayList<>(secondaryAtom.getEntries());
        LinkCheckJob job = linkCheckJobService.getJobInfo(simpleAtomLinkToData.getLinkCheckJobId(),false);
        int nToProcess = job == null ? LinkCheckRunConfig.maxAtomEntriesToAttempt_default : job.getMaxAtomEntriesToAttempt();
        if (secondaryEntries.size()> nToProcess)
        {
            Collections.sort(secondaryEntries,(entry1, entry2) ->{
                return entry1.getId().compareToIgnoreCase(entry2.getId());
            });
            secondaryEntries = secondaryEntries.subList(0,nToProcess);
        }

        for (AtomEntry entry : secondaryEntries) {
            AtomActualDataEntry atomActualDataEntry = new AtomActualDataEntry();
            atomActualDataEntry.setLinkCheckJobId(simpleAtomLinkToData.getLinkCheckJobId());
            atomActualDataEntry.setEntryId(entry.getId());
            atomActualDataEntry.setSimpleAtomLinkToData(simpleAtomLinkToData);
            atomActualDataEntry.setIndex(index);
            index++;
            List<AtomLink> links = entry.findLinks("alternate");
            if ((links == null)||(links.isEmpty()))
                links = entry.findLinks("section");
            if ((links == null)||(links.isEmpty()))
                continue; // nothing to process

            int nToProcessDownload = job == null ? LinkCheckRunConfig.maxAtomSectionLinksToFollow_default : job.getMaxAtomSectionLinksToFollow();
            if (links.size() > nToProcessDownload) {
                Collections.sort(links,(link1, link2) ->{
                    return link1.getHref().compareToIgnoreCase(link2.getHref());
                });
                links = links.subList(0,nToProcessDownload);
            }

            List<AtomDataRequest> requests = links.stream()
                            .map(x->atomLayerDownloader.createAtomDataRequest(x,atomActualDataEntry,simpleAtomLinkToData.getLinkCheckJobId()))
                                    .collect(Collectors.toList());
            atomActualDataEntry.setAtomDataRequestList(requests);
            simpleAtomLinkToData.getAtomActualDataEntryList().add(atomActualDataEntry);
        }
        boolean success=doDownload(simpleAtomLinkToData);
        simpleAtomLinkToData.setSuccessfullyDownloaded(success);
    }

    private boolean doDownload(SimpleAtomLinkToData simpleAtomLinkToData) {
        if (simpleAtomLinkToData.getAtomActualDataEntryList().isEmpty())
            return false;
        for (AtomActualDataEntry entry: simpleAtomLinkToData.getAtomActualDataEntryList()){
            boolean entryDownloads = doDownload(entry);
            entry.setSuccessfullyDownloaded(entryDownloads);
            if (entryDownloads)
                return true;
        }
        return false;
    }

    private boolean doDownload(AtomActualDataEntry entry) {
        if (entry.getAtomDataRequestList().isEmpty())
            return false;

        boolean allGood= true;
        for(AtomDataRequest request:entry.getAtomDataRequestList()){
            doDownload(request);
            allGood = allGood && request.getSuccessfullyDownloaded();
            if (!allGood)
                break;
        }
        return allGood;
    }

    private void doDownload(AtomDataRequest request) {
        retrievableSimpleLinkDownloader.process(request,HEADER_LENGTH,ACCEPT_MIME);
        boolean someDownloaded = (request.getLinkContentHead()!= null) && (request.getLinkContentHead().length>0);
        boolean is200 = ( (request.getLinkHTTPStatusCode()!=null) && (request.getLinkHTTPStatusCode() == 200));
        request.setSuccessfullyDownloaded(someDownloaded && is200);
    }

}
