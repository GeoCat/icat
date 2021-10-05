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

package net.geocat.eventprocessor.processors.processlinks;

import net.geocat.database.linkchecker.entities.*;
import net.geocat.database.linkchecker.entities.helper.LinkState;
import net.geocat.database.linkchecker.entities.helper.ServiceMetadataDocumentState;
import net.geocat.database.linkchecker.repos.CapabilitiesDocumentRepo;
import net.geocat.database.linkchecker.repos.LocalDatasetMetadataRecordRepo;

import net.geocat.eventprocessor.BaseEventProcessor;
import net.geocat.eventprocessor.processors.processlinks.postprocessing.CapabilitiesResolvesIndicators;
import net.geocat.eventprocessor.processors.processlinks.postprocessing.DatasetToLayerIndicators;
import net.geocat.events.Event;
import net.geocat.events.EventFactory;
import net.geocat.events.processlinks.ProcessDatasetDocLinksEvent;
import net.geocat.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static net.geocat.database.linkchecker.service.DatabaseUpdateService.convertToString;

@Component
@Scope("prototype")
public class EventProcessor_ProcessDatasetDocLinksEvent extends BaseEventProcessor<ProcessDatasetDocLinksEvent> {

    Logger logger = LoggerFactory.getLogger(EventProcessor_ProcessServiceDocLinksEvent.class);



    @Autowired
    LocalDatasetMetadataRecordRepo localDatasetMetadataRecordRepo;

    @Autowired
    MetadataService metadataService;

    @Autowired
    EventFactory eventFactory;

    @Autowired
    CapabilitiesDocumentRepo capabilitiesDocumentRepo;

    @Autowired
    RetrieveCapabilitiesDatasetMetadataLink retrieveCapabilitiesDatasetMetadataLink;

    @Autowired
    RemoteServiceMetadataRecordLinkRetriever remoteServiceMetadataRecordLinkRetriever;

    @Autowired
    RetrieveServiceDocumentLink retrieveServiceDocumentLink;

    @Autowired
    CapabilitiesResolvesIndicators capabilitiesResolvesIndicators;

    @Autowired
    DatasetToLayerIndicators datasetToLayerIndicators;

    LocalDatasetMetadataRecord localDatasetMetadataRecord;




    @Override
    public EventProcessor_ProcessDatasetDocLinksEvent internalProcessing() throws Exception {
        try{

            capabilitiesResolvesIndicators.process(localDatasetMetadataRecord);
            datasetToLayerIndicators.process(localDatasetMetadataRecord);

            localDatasetMetadataRecord.setState(ServiceMetadataDocumentState.LINKS_PROCESSED);
            // localServiceMetadataRecord.setHumanReadable(humanReadableServiceMetadata.getHumanReadable(localServiceMetadataRecord));
            save();
            logger.debug("finished post processing documentid="+getInitiatingEvent().getDatasetDocumentId()  );

        }
            catch(Exception e){
            logger.error("exception for datasetMetadataRecordId="+getInitiatingEvent().getDatasetDocumentId(),e);
            localDatasetMetadataRecord.setState(ServiceMetadataDocumentState.ERROR);
            localDatasetMetadataRecord.setErrorMessage(  convertToString(e) );
            save();
        }
        return this;
    }


    @Override
    public EventProcessor_ProcessDatasetDocLinksEvent externalProcessing () throws Exception {
        localDatasetMetadataRecord = localDatasetMetadataRecordRepo.findById(getInitiatingEvent().getDatasetDocumentId()).get();// make sure we re-load

        prune();

        try {
            int nlinksCap = localDatasetMetadataRecord.getDocumentLinks().size();
             logger.debug("processing DATASET documentid="+getInitiatingEvent().getDatasetDocumentId()+" that has "+nlinksCap+" document links");

            processDocumentLinks();
            save();

            logger.debug("finished initial processing documentid="+getInitiatingEvent().getDatasetDocumentId()  );

        }
        catch(Exception e){
            logger.error("exception for datasetMetadataRecordId="+getInitiatingEvent().getDatasetDocumentId(),e);
            localDatasetMetadataRecord.setState(ServiceMetadataDocumentState.ERROR);
            localDatasetMetadataRecord.setErrorMessage(  convertToString(e) );
            save();
        }

        return this;
    }


    private void processDocumentLinks() {
        int nlinks = localDatasetMetadataRecord.getDocumentLinks().size();
        int linkIdx = 0;
        logger.debug("processing "+nlinks+" dataset document links for documentid="+getInitiatingEvent().getDatasetDocumentId());
        for (DatasetDocumentLink link : localDatasetMetadataRecord.getDocumentLinks()) {
            logger.debug("processing dataset document link "+linkIdx+" of "+nlinks+" links");
            linkIdx++;
            handleSingleDocumentLink(link);
        }
        logger.debug("FINISHED processing "+nlinks+" dataset document links for documentid="+getInitiatingEvent().getDatasetDocumentId());
    }

    // should retrieve a capabilities document
    // if so;
    //   a) resolve the service metadata link (in cap doc)
    //   b) for each of the layers in the cap doc, retrieve the dataset metadata link
    private DatasetDocumentLink handleSingleDocumentLink(DatasetDocumentLink link) {
        getCapabilitiesDoc(link);
        if (link.getCapabilitiesDocument() != null) {
            CapabilitiesDocument capabilitiesDocument = link.getCapabilitiesDocument();
            int nlinks =  capabilitiesDocument.getCapabilitiesDatasetMetadataLinkList().size();
            RemoteServiceMetadataRecordLink rsmrl = capabilitiesDocument.getRemoteServiceMetadataRecordLink();
            logger.debug("link produced a capabilities document has Service Metadata Link="+(rsmrl!=null)+", and "+nlinks+" dataset links.");

            if (rsmrl != null) {
                logger.debug("getting Capabilities Document's remote service metadata record...");
                getRemoteServiceMetadataRecordLink(rsmrl);
                if (rsmrl.getRemoteServiceMetadataRecord() !=null) {
                    RemoteServiceMetadataRecord remoteServiceMetadataRecord=  rsmrl.getRemoteServiceMetadataRecord();

                }
            }
            int linkIdx = 0;
            logger.debug("processing "+nlinks+" dataset links from the capabilities document");

            for (CapabilitiesDatasetMetadataLink capabilitiesDatasetMetadataLink : capabilitiesDocument.getCapabilitiesDatasetMetadataLinkList()) {
                logger.debug("processing link "+linkIdx+" of "+nlinks+" dataset links from the capabilities document");
                linkIdx++;
                handleLayerDatasetLink(capabilitiesDatasetMetadataLink);
                if (capabilitiesDatasetMetadataLink.getCapabilitiesRemoteDatasetMetadataDocument() !=null) {
                    logger.debug("link produced a Dataset Metadata Document");
                    CapabilitiesRemoteDatasetMetadataDocument capabilitiesRemoteDatasetMetadataDocument =capabilitiesDatasetMetadataLink.getCapabilitiesRemoteDatasetMetadataDocument();
                }
                else {
                    logger.debug("link DID NOT produce a Dataset Metadata Document");
                }
            }
        }
        else {
            logger.debug("link DID NOT produced a capabilities document");

        }
        return link;
    }

    private void handleLayerDatasetLink(CapabilitiesDatasetMetadataLink capabilitiesDatasetMetadataLink) {
        try {
            String jobid = getInitiatingEvent().getLinkCheckJobId();
            capabilitiesDatasetMetadataLink = retrieveCapabilitiesDatasetMetadataLink.process(capabilitiesDatasetMetadataLink,jobid);

            capabilitiesDatasetMetadataLink.setLinkState(LinkState.Complete);
        }
        catch(Exception e){
            logger.error("error occurred while processing Dataset MetadataDocumentId="+localDatasetMetadataRecord.getDatasetMetadataDocumentId()
                    +", CapabilitiesDatasetMetadataLink="+capabilitiesDatasetMetadataLink+", error="+e.getMessage(),e);
            capabilitiesDatasetMetadataLink.setLinkState(LinkState.ERROR);
            capabilitiesDatasetMetadataLink.setErrorMessage(  convertToString(e) );
        }
    }
    private void getRemoteServiceMetadataRecordLink(RemoteServiceMetadataRecordLink rsmrl) {
        try {

            rsmrl = remoteServiceMetadataRecordLinkRetriever.process(rsmrl);

            rsmrl.setLinkState(LinkState.Complete);
        }
        catch(Exception e){
            logger.error("error occurred while processing Dataset MetadataDocumentId="+localDatasetMetadataRecord.getDatasetMetadataDocumentId()
                    +", RemoteServiceMetadataRecordLink="+rsmrl+", error="+e.getMessage(),e);
            rsmrl.setLinkState(LinkState.ERROR);
            rsmrl.setErrorMessage(  convertToString(e) );
        }
    }

    private DatasetDocumentLink getCapabilitiesDoc(DatasetDocumentLink link) {
        try {
            link = (DatasetDocumentLink) retrieveServiceDocumentLink.process(link);

            link.setLinkState(LinkState.Complete);
        }
        catch (Exception e){
            logger.error("error occurred while processing dataset MetadataDocumentId="+localDatasetMetadataRecord.getDatasetMetadataDocumentId()
                    +", ServiceDocumentLink="+link+", error="+e.getMessage(),e);
            link.setLinkState(LinkState.ERROR);
            link.setErrorMessage(  convertToString(e) );
        }

        return link;
    }


    public void save(){
        localDatasetMetadataRecord = localDatasetMetadataRecordRepo.save(localDatasetMetadataRecord);
    }

    @Override
    public List<Event> newEventProcessing() {
        List<Event> result = new ArrayList<>();
        String linkCheckJobId = getInitiatingEvent().getLinkCheckJobId();

        if (metadataService.linkProcessingComplete(linkCheckJobId))
        {
            //done
            Event e = eventFactory.createAllLinksCheckedEvent(linkCheckJobId);
            result.add(e);
        }
        return result;
    }

    // for re-entry - need to clean up object (and database)
    public void prune() {
        List<CapabilitiesDocument> capDocuments = new ArrayList<>();

        //find objects and de-attach them
        for (DatasetDocumentLink link : localDatasetMetadataRecord.getDocumentLinks()) {
            CapabilitiesDocument capDoc = link.getCapabilitiesDocument();
            if (capDoc != null) {
                if (capDoc.getServiceDocumentLink() != null)
                    capDoc.getServiceDocumentLink().setCapabilitiesDocument(null);
                capDoc.setServiceDocumentLink(null);
                capDocuments.add(capDoc); // to be deleted
            }
        }

        if (capDocuments.isEmpty())
            return; //nothing to do

        save(); //save with objects detached

        for (CapabilitiesDocument capDoc : capDocuments) {
            capabilitiesDocumentRepo.delete(capDoc);
        }
    }

}
