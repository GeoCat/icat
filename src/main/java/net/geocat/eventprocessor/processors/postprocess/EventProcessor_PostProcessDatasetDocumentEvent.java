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

package net.geocat.eventprocessor.processors.postprocess;

import net.geocat.database.linkchecker.entities.CapabilitiesDatasetMetadataLink;
import net.geocat.database.linkchecker.entities.CapabilitiesDocument;
import net.geocat.database.linkchecker.entities.SimpleAtomLinkToData;
import net.geocat.database.linkchecker.entities.SimpleLayerDatasetIdDataLink;
import net.geocat.database.linkchecker.entities.SimpleSpatialDSIDDataLink;
import net.geocat.database.linkchecker.entities.helper.DatasetIdentifier;
import net.geocat.database.linkchecker.entities.LocalDatasetMetadataRecord;
import net.geocat.database.linkchecker.entities.SimpleLayerMetadataUrlDataLink;
import net.geocat.database.linkchecker.entities.SimpleStoredQueryDataLink;
import net.geocat.database.linkchecker.entities.helper.CapabilitiesLinkResult;
import net.geocat.database.linkchecker.entities.helper.IndicatorStatus;
import net.geocat.database.linkchecker.entities.helper.LinkToData;
import net.geocat.database.linkchecker.entities.helper.SHA2JobIdCompositeKey;
import net.geocat.database.linkchecker.entities.helper.ServiceMetadataDocumentState;
import net.geocat.database.linkchecker.entities.helper.StoreQueryCapabilitiesLinkResult;
import net.geocat.database.linkchecker.repos.CapabilitiesDatasetMetadataLinkRepo;
import net.geocat.database.linkchecker.repos.CapabilitiesDocumentRepo;
import net.geocat.database.linkchecker.repos.InspireSpatialDatasetIdentifierRepo;
import net.geocat.database.linkchecker.repos.LocalDatasetMetadataRecordRepo;
import net.geocat.database.linkchecker.repos.LocalServiceMetadataRecordRepo;
import net.geocat.database.linkchecker.repos.OperatesOnLinkRepo;
import net.geocat.eventprocessor.BaseEventProcessor;
import net.geocat.eventprocessor.processors.processlinks.EventProcessor_ProcessServiceDocLinksEvent;
import net.geocat.events.Event;
import net.geocat.events.EventFactory;
import net.geocat.events.postprocess.PostProcessDatasetDocumentEvent;
 import net.geocat.service.MetadataService;
import net.geocat.service.helper.ShouldTransitionOutOfPostProcessing;
import net.geocat.xml.helpers.CapabilitiesType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static net.geocat.database.linkchecker.service.DatabaseUpdateService.convertToString;

@Component
@Scope("prototype")
public class EventProcessor_PostProcessDatasetDocumentEvent extends BaseEventProcessor<PostProcessDatasetDocumentEvent> {

    Logger logger = LoggerFactory.getLogger(EventProcessor_ProcessServiceDocLinksEvent.class);

    @Autowired
    LocalDatasetMetadataRecordRepo localDatasetMetadataRecordRepo;

    @Autowired
    MetadataService metadataService;

    @Autowired
    EventFactory eventFactory;

    @Autowired
    OperatesOnLinkRepo operatesOnLinkRepo;

    @Autowired
    CapabilitiesDatasetMetadataLinkRepo capabilitiesDatasetMetadataLinkRepo;

    @Autowired
    LocalServiceMetadataRecordRepo localServiceMetadataRecordRepo;

    @Autowired
    ShouldTransitionOutOfPostProcessing shouldTransitionOutOfPostProcessing;

    @Autowired
    InspireSpatialDatasetIdentifierRepo inspireSpatialDatasetIdentifierRepo;

    @Autowired
    CapabilitiesDocumentRepo capabilitiesDocumentRepo;

    LocalDatasetMetadataRecord localDatasetMetadataRecord;


    @Override
    public EventProcessor_PostProcessDatasetDocumentEvent externalProcessing() throws Exception {
        return this;
    }


    public void save()
    {
        localDatasetMetadataRecord = localDatasetMetadataRecordRepo.save(localDatasetMetadataRecord);
    }


    @Override
    public EventProcessor_PostProcessDatasetDocumentEvent internalProcessing() throws Exception {
        localDatasetMetadataRecord = localDatasetMetadataRecordRepo.findById(getInitiatingEvent().getDatasetDocumentId()).get();// make sure we re-load
        if (localDatasetMetadataRecord.getState() == ServiceMetadataDocumentState.NOT_APPLICABLE)
            return this; //nothing to do
        
        try{
            process();
            localDatasetMetadataRecord.setState(ServiceMetadataDocumentState.LINKS_POSTPROCESSED);
            save();
            logger.debug("finished postprocessing dataset documentid="+getInitiatingEvent().getDatasetDocumentId()  );

        }
        catch(Exception e){
            logger.error("postprocessing exception for datasetMetadataRecordId="+getInitiatingEvent().getDatasetDocumentId(),e);
            localDatasetMetadataRecord.setState(ServiceMetadataDocumentState.ERROR);
            localDatasetMetadataRecord.setErrorMessage(  convertToString(e) );
            save();
        }
        return this;
    }

    public void findSimpleLayerMetadataURLinks(String fileId , String linkcheckjobid){
        List<CapabilitiesLinkResult> result = new ArrayList<>();
        if ( (localDatasetMetadataRecord.getDatasetIdentifiers() ==null) || localDatasetMetadataRecord.getDatasetIdentifiers().isEmpty())
            return;

        //should only be one
        for (DatasetIdentifier identifier:localDatasetMetadataRecord.getDatasetIdentifiers()) {
            if ( (identifier.getCodeSpace() != null) && (!identifier.getCodeSpace().isEmpty())) {
                List<CapabilitiesLinkResult>  links = capabilitiesDatasetMetadataLinkRepo.linkToCapabilitiesViaIdentifier_codeAndCodeSpace(localDatasetMetadataRecord.getLinkCheckJobId(),
                        identifier.getCode(),
                        identifier.getCodeSpace());
                result.addAll(links);
            }
            else {
                List<CapabilitiesLinkResult> links = capabilitiesDatasetMetadataLinkRepo.linkToCapabilitiesViaIdentifier_codeOnly(localDatasetMetadataRecord.getLinkCheckJobId(),
                        identifier.getCode());
                result.addAll(links);
            }

        }

        for(CapabilitiesLinkResult link: result){
            if (!link.getCapabilitiesdocumenttype().equals("Atom")) {
                if (link.getOgclayername() !=null) {
                    SimpleLayerMetadataUrlDataLink item = new SimpleLayerMetadataUrlDataLink(link.getLinkcheckjobid(), link.getSha2(), link.getCapabilitiesdocumenttype(), localDatasetMetadataRecord);
                    item.setOgcLayerName(link.getOgclayername());
                    this.localDatasetMetadataRecord.getDataLinks().add(item);
                }
            }
            else {
                SimpleAtomLinkToData atomItem = new SimpleAtomLinkToData(link.getLinkcheckjobid(),
                        link.getSha2(),
                        link.getCapabilitiesdocumenttype(),
                        localDatasetMetadataRecord,
                        link.getOgclayername());
                atomItem.setContext("SimpleLayerMetadataUrlDataLink");
                this.localDatasetMetadataRecord.getDataLinks().add(atomItem);
            }
        }
    }

    public void findStoredQueryLinks(List<StoreQueryCapabilitiesLinkResult> links) {
        for (StoreQueryCapabilitiesLinkResult link : links) {
            if ( (link.getProcGetSpatialDataSetName() == null) || (link.getProcGetSpatialDataSetName().isEmpty()))
                continue; //not a WFS 2.0 with the correct stored proc -- don't link
            SimpleStoredQueryDataLink item = new SimpleStoredQueryDataLink(link.getLinkcheckjobid(), link.getSha2(), link.getCapabilitiesdocumenttype(),localDatasetMetadataRecord);
            item.setCode(link.getCode());
            item.setCodeSpace(link.getCodespace());
            item.setStoredProcName(link.getProcGetSpatialDataSetName());
            this.localDatasetMetadataRecord.getDataLinks().add(item);
        }
    }



    public List<StoreQueryCapabilitiesLinkResult> findLinkedCapabilities() {
        List<StoreQueryCapabilitiesLinkResult> result = new ArrayList<>();
        if ( (localDatasetMetadataRecord.getDatasetIdentifiers() ==null) || localDatasetMetadataRecord.getDatasetIdentifiers().isEmpty())
            return result;

        //should only be one
        for (DatasetIdentifier identifier:localDatasetMetadataRecord.getDatasetIdentifiers()) {

            List<StoreQueryCapabilitiesLinkResult> links;
            if ( (identifier.getCodeSpace() != null) && (!identifier.getCodeSpace().isEmpty())) {
                links = inspireSpatialDatasetIdentifierRepo.linkToCapabilitiesViaInspire_codeAndCodespace(localDatasetMetadataRecord.getLinkCheckJobId(),
                        identifier.getCode(),
                        identifier.getCodeSpace());
                result.addAll(links);
            }
            else {
                links = inspireSpatialDatasetIdentifierRepo.linkToCapabilitiesViaInspire_codeOnly(localDatasetMetadataRecord.getLinkCheckJobId(),
                        identifier.getCode());
                result.addAll(links);
            }
        }
        return result;
    }

    public void addSingleLinkedCap(StoreQueryCapabilitiesLinkResult link) {
        CapabilitiesDocument capDoc = capabilitiesDocumentRepo.findById(new SHA2JobIdCompositeKey(link.getSha2(), localDatasetMetadataRecord.getLinkCheckJobId())).get();
        for (CapabilitiesDatasetMetadataLink layer : capDoc.getCapabilitiesDatasetMetadataLinkList()) {
            if (!link.getCapabilitiesdocumenttype().equals("Atom")) {
                SimpleSpatialDSIDDataLink item = new SimpleSpatialDSIDDataLink(link.getLinkcheckjobid(),
                        link.getSha2(),
                        link.getCapabilitiesdocumenttype(),
                        localDatasetMetadataRecord,
                        layer.getOgcLayerName());
                item.setCode(link.getCode());
                item.setCodeSpace(link.getCodespace());
                this.localDatasetMetadataRecord.getDataLinks().add(item);
            }
            else {
                SimpleAtomLinkToData atomItem = new SimpleAtomLinkToData(link.getLinkcheckjobid(),
                        link.getSha2(),
                        link.getCapabilitiesdocumenttype(),
                        localDatasetMetadataRecord,
                        layer.getOgcLayerName());
                atomItem.setContext("SimpleSpatialDSIDDataLink: code:"+link.getCode()+", codespace:"+link.getCodespace());
                this.localDatasetMetadataRecord.getDataLinks().add(atomItem);
            }
        }
    }

    public void addLinkedCapabilities( List<StoreQueryCapabilitiesLinkResult> linkedCapabilities) {
        linkedCapabilities.stream()
                .forEach(x-> addSingleLinkedCap(x));
    }

    private void findLayerIdLinks(String fileId, String linkcheckjobid) {
         if ( (localDatasetMetadataRecord.getDatasetIdentifiers() ==null) || localDatasetMetadataRecord.getDatasetIdentifiers().isEmpty())
            return;

        for (DatasetIdentifier identifier:localDatasetMetadataRecord.getDatasetIdentifiers()) {
            if ( (identifier.getCodeSpace() != null) && (!identifier.getCodeSpace().isEmpty())) {

                List<CapabilitiesLinkResult>  links = capabilitiesDatasetMetadataLinkRepo.linkToCapabilitiesLayerViaIdentifier(localDatasetMetadataRecord.getLinkCheckJobId(),
                        identifier.getCode(),
                        identifier.getCodeSpace());

                for(CapabilitiesLinkResult link: links){
                    if (!link.getCapabilitiesdocumenttype().equals("Atom")) {
                        if (link.getOgclayername() !=null) {
                            SimpleLayerDatasetIdDataLink item = new SimpleLayerDatasetIdDataLink(
                                    link.getLinkcheckjobid(),
                                    link.getSha2(),
                                    link.getCapabilitiesdocumenttype(),
                                    link.getOgclayername(),
                                    identifier.getCode(),
                                    identifier.getCodeSpace(),
                                    localDatasetMetadataRecord
                            );
                            this.localDatasetMetadataRecord.getDataLinks().add(item);
                        }
                    }
                    else {
                        SimpleAtomLinkToData atomItem = new SimpleAtomLinkToData(link.getLinkcheckjobid(),
                                link.getSha2(),
                                link.getCapabilitiesdocumenttype(),
                                localDatasetMetadataRecord,
                                link.getOgclayername());
                        atomItem.setContext("SimpleSpatialDSIDDataLink: code:"+identifier.getCode()+", codespace:"+identifier.getCodeSpace());
                        this.localDatasetMetadataRecord.getDataLinks().add(atomItem);
                    }
                }
            }
            else {
                List<CapabilitiesLinkResult> links = capabilitiesDatasetMetadataLinkRepo.linkToCapabilitiesLayerViaIdentifier(localDatasetMetadataRecord.getLinkCheckJobId(),
                        identifier.getCode());
                for(CapabilitiesLinkResult link: links){
                    if (!link.getCapabilitiesdocumenttype().equals("Atom")) {
                        if (link.getOgclayername() !=null) {
                            SimpleLayerDatasetIdDataLink item = new SimpleLayerDatasetIdDataLink(
                                    link.getLinkcheckjobid(),
                                    link.getSha2(),
                                    link.getCapabilitiesdocumenttype(),
                                    link.getOgclayername(),
                                    identifier.getCode(),
                                    identifier.getCodeSpace(),
                                    localDatasetMetadataRecord
                            );
                            this.localDatasetMetadataRecord.getDataLinks().add(item);
                        }
                    }
                    else {
                        SimpleAtomLinkToData atomItem = new SimpleAtomLinkToData(link.getLinkcheckjobid(),
                                link.getSha2(),
                                link.getCapabilitiesdocumenttype(),
                                localDatasetMetadataRecord,
                                link.getOgclayername());
                        atomItem.setContext("SimpleSpatialDSIDDataLink: code:"+identifier.getCode()+", codespace:"+identifier.getCodeSpace());
                        this.localDatasetMetadataRecord.getDataLinks().add(atomItem);
                    }
                }
            }
        }
    }


    private void process() {
        String fileId = localDatasetMetadataRecord.getFileIdentifier();
        String linkcheckjobid=localDatasetMetadataRecord.getLinkCheckJobId();

        List<StoreQueryCapabilitiesLinkResult> linkedCapabilities =  findLinkedCapabilities();// linked by inspire spatial dataset id at the document level

        addLinkedCapabilities(linkedCapabilities); // SimpleSpatialDSIDDataLink - linked at the full-document level
        findStoredQueryLinks(linkedCapabilities);// SimpleStoredQueryDataLink - linked at the full-document level (wfs 2.0 with stored query)

        findSimpleLayerMetadataURLinks(fileId,linkcheckjobid); //SimpleLayerMetadataUrlDataLink - layer based metadataURL that match

        findLayerIdLinks(fileId,linkcheckjobid); //SimpleLayerDatasetIdDataLink - layers that have inspire spatial dataset ids that match (i.e. identity/authority)

        localDatasetMetadataRecord.setDataLinks(
               new HashSet<>( LinkToData.unique(new ArrayList(localDatasetMetadataRecord.getDataLinks())))
        );

        localDatasetMetadataRecord.setINDICATOR_DOWNLOAD_LINK_TO_DATA(IndicatorStatus.FAIL);
        localDatasetMetadataRecord.setINDICATOR_VIEW_LINK_TO_DATA(IndicatorStatus.FAIL);

        List<LinkToData> viewLinks = localDatasetMetadataRecord.getDataLinks().stream()
                .filter(x->(x.getCapabilitiesDocumentType() == CapabilitiesType.WMS) || (x.getCapabilitiesDocumentType() == CapabilitiesType.WMTS))
                .collect(Collectors.toList());
        List<LinkToData> downloadLinks = localDatasetMetadataRecord.getDataLinks().stream()
                .filter(x->(x.getCapabilitiesDocumentType() == CapabilitiesType.WFS) || (x.getCapabilitiesDocumentType() == CapabilitiesType.Atom))
                .collect(Collectors.toList());

        if (!viewLinks.isEmpty())
            localDatasetMetadataRecord.setINDICATOR_VIEW_LINK_TO_DATA(IndicatorStatus.PASS);
        if (!downloadLinks.isEmpty())
            localDatasetMetadataRecord.setINDICATOR_DOWNLOAD_LINK_TO_DATA(IndicatorStatus.PASS);

        localDatasetMetadataRecord.setNumberOfViewDataLinks(viewLinks.size());
        localDatasetMetadataRecord.setNumberOfDownloadDataLinks(downloadLinks.size());

//        List<ServiceDocSearchResult> serviceLinks =  new ArrayList<>();
//        List<CapabilitiesLinkResult> capLinks =  new ArrayList<>();
//
//        if (datasetid == null) {
//            serviceLinks =  operatesOnLinkRepo.linkToService(fileId, linkcheckjobid);
//            capLinks =  capabilitiesDatasetMetadataLinkRepo.linkToCapabilities(fileId, linkcheckjobid);
//
//        } else {
//            serviceLinks =  operatesOnLinkRepo.linkToService(fileId,datasetid, linkcheckjobid);
//            capLinks =  capabilitiesDatasetMetadataLinkRepo.linkToCapabilities(fileId, datasetid,linkcheckjobid);
//        }
//
//
//
//        ServiceDocSearchResult service_view = serviceLinks.stream()
//                    .filter(x->x.getMetadataservicetype().equals("view"))
//                    .findAny()
//                    .orElse(null);
//
//        ServiceDocSearchResult service_download = serviceLinks.stream()
//                .filter(x->x.getMetadataservicetype().equals("download"))
//                .findAny()
//                .orElse(null);
//
//        CapabilitiesLinkResult cap_view = capLinks.stream()
//                .filter(x->x.getCapabilitiesdocumenttype().equals("WMS") || x.getCapabilitiesdocumenttype().equals("WMTS"))
//                .findAny()
//                .orElse(null);
//
//        CapabilitiesLinkResult cap_download = capLinks.stream()
//                .filter(x->x.getCapabilitiesdocumenttype().equals("WFS") || x.getCapabilitiesdocumenttype().toLowerCase().equals("atom"))
//                .findAny()
//                .orElse(null);
//
//        List<String> view_cap_sha2s = capLinks.stream()
//                .filter(x->x.getCapabilitiesdocumenttype().equals("WMS") || x.getCapabilitiesdocumenttype().equals("WMTS"))
//                .map(x-> x.getSha2())
//                .collect(Collectors.toList());
//
//        localDatasetMetadataRecord.setLinksToViewCapabilities( String.join(",",view_cap_sha2s));
//
//        List<String> download_cap_sha2s = capLinks.stream()
//                .filter(x->x.getCapabilitiesdocumenttype().equals("WFS") || x.getCapabilitiesdocumenttype().toLowerCase().equals("atom"))
//                .map(x-> x.getSha2())
//                .collect(Collectors.toList());
//
//        localDatasetMetadataRecord.setLinksToDownloadCapabilities( String.join(",",download_cap_sha2s));
//
//
//        localDatasetMetadataRecord.setINDICATOR_LAYER_MATCHES(IndicatorStatus.FAIL);
//        localDatasetMetadataRecord.setINDICATOR_LAYER_MATCHES_VIEW(IndicatorStatus.FAIL);
//        localDatasetMetadataRecord.setINDICATOR_LAYER_MATCHES_DOWNLOAD(IndicatorStatus.FAIL);
//        localDatasetMetadataRecord.setINDICATOR_SERVICE_MATCHES_VIEW(IndicatorStatus.FAIL);
//        localDatasetMetadataRecord.setINDICATOR_SERVICE_MATCHES_DOWNLOAD(IndicatorStatus.FAIL);
//
//
//        if (service_view != null) {
//            localDatasetMetadataRecord.setINDICATOR_SERVICE_MATCHES_VIEW(IndicatorStatus.PASS);
//        }
//        if (service_download !=null) {
//            localDatasetMetadataRecord.setINDICATOR_SERVICE_MATCHES_DOWNLOAD(IndicatorStatus.PASS);
//
//        }
//        if (cap_view != null) {
//            localDatasetMetadataRecord.setINDICATOR_LAYER_MATCHES(IndicatorStatus.PASS);
//            localDatasetMetadataRecord.setINDICATOR_LAYER_MATCHES_VIEW(IndicatorStatus.PASS);
//        }
//        if (cap_download != null){
//            localDatasetMetadataRecord.setINDICATOR_LAYER_MATCHES(IndicatorStatus.PASS);
//            localDatasetMetadataRecord.setINDICATOR_LAYER_MATCHES_DOWNLOAD(IndicatorStatus.PASS);
//        }

    }



    @Override
    public List<Event> newEventProcessing() {
        List<Event> result = new ArrayList<>();

        String linkCheckJobId = getInitiatingEvent().getLinkCheckJobId();
//        if (metadataService.linkPostProcessingComplete(linkCheckJobId))
//        {
//            //done
//            Event e = eventFactory.createAllPostProcessingCompleteEvent(linkCheckJobId);
//            result.add(e);
//        }
        if (shouldTransitionOutOfPostProcessing.shouldSendMessage(linkCheckJobId,getInitiatingEvent().getDatasetDocumentId()))
        {
            //done
            Event e = eventFactory.createAllPostProcessingCompleteEvent(linkCheckJobId);
            result.add(e);
        }
        return result;
    }
}


