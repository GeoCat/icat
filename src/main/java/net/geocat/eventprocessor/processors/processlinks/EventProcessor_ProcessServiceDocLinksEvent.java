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
import net.geocat.database.linkchecker.repos.*;

import net.geocat.eventprocessor.BaseEventProcessor;
import net.geocat.eventprocessor.processors.processlinks.postprocessing.*;
import net.geocat.events.Event;
import net.geocat.events.EventFactory;
import net.geocat.events.processlinks.ProcessServiceDocLinksEvent;
import net.geocat.service.*;
import net.geocat.service.capabilities.CapabilitiesLinkFixer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static net.geocat.database.linkchecker.service.DatabaseUpdateService.convertToString;

@Component
@Scope("prototype")
public class EventProcessor_ProcessServiceDocLinksEvent extends BaseEventProcessor<ProcessServiceDocLinksEvent> {

    Logger logger = LoggerFactory.getLogger(EventProcessor_ProcessServiceDocLinksEvent.class);

   @Autowired
   LocalServiceMetadataRecordRepo localServiceMetadataRecordRepo;

   @Autowired
   ServiceDocumentLinkRepo serviceDocumentLinkRepo;

   @Autowired
   RetrieveServiceDocumentLink retrieveServiceDocumentLink;

   @Autowired
   RemoteServiceMetadataRecordLinkRetriever remoteServiceMetadataRecordLinkRetriever;

   @Autowired
   RemoteServiceMetadataRecordLinkRepo remoteServiceMetadataRecordLinkRepo;

   @Autowired
   CapabilitiesDatasetMetadataLinkRepo capabilitiesDatasetMetadataLinkRepo;

   @Autowired
   RetrieveCapabilitiesDatasetMetadataLink retrieveCapabilitiesDatasetMetadataLink;

   @Autowired
   RetrieveOperatesOnLink retrieveOperatesOnLink;

   @Autowired
   OperatesOnLinkRepo operatesOnLinkRepo;

   @Autowired
   CapabilitiesDocumentRepo capabilitiesDocumentRepo;

   @Autowired
    RemoteServiceMetadataRecordRepo remoteServiceMetadataRecordRepo;

   @Autowired
    CapabilitiesRemoteDatasetMetadataDocumentRepo capabilitiesRemoteDatasetMetadataDocumentRepo;

   @Autowired
   HumanReadableServiceMetadata humanReadableServiceMetadata;

   @Autowired
    OperatesOnRemoteDatasetMetadataRecordRepo operatesOnRemoteDatasetMetadataRecordRepo;

   @Autowired
   EventFactory eventFactory;

   @Autowired
   MetadataService metadataService;

   @Autowired
   CapabilitiesResolvesIndicators capabilitiesResolvesIndicators;

   @Autowired
   CapabilitiesServiceLinkIndicators capabilitiesServiceLinkIndicators;

   @Autowired
   CapabilitiesServiceMatchesLocalServiceIndicators capabilitiesServiceMatchesLocalServiceIndicators;

   @Autowired
   CapabilitiesDatasetLinksResolveIndicators capabilitiesDatasetLinksResolveIndicators;

   @Autowired
   ServiceOperatesOnIndicators serviceOperatesOnIndicators;


    @Autowired
    CapabilitiesLinkFixer capabilitiesLinkFixer;

    LocalServiceMetadataRecord localServiceMetadataRecord;


    boolean throwException = true;

    @Override
    public EventProcessor_ProcessServiceDocLinksEvent externalProcessing() throws Exception {
       // localServiceMetadataRecord = localServiceMetadataRecordRepo.findById(getInitiatingEvent().getServiceMetadataId()).get();// make sure we re-load
        localServiceMetadataRecord = localServiceMetadataRecordRepo.fullId(getInitiatingEvent().getServiceMetadataId());// make sure we re-load

        prune(); // remove any previous work (if this is being re-run)

        try {
            int nlinksCap = localServiceMetadataRecord.getServiceDocumentLinks().size();
            int nlinksOperates = localServiceMetadataRecord.getOperatesOnLinks().size();
            logger.debug("processing SERVICE documentid="+getInitiatingEvent().getServiceMetadataId()+" that has "+nlinksCap+" document links, and "+nlinksOperates+" operates on links");

            processDocumentLinks();

            processOperatesOnLinks();
             save(false);

             logger.debug("finished initial processing  documentid="+getInitiatingEvent().getServiceMetadataId()  );

        }
        catch(Exception e){
            logger.error("exception for serviceMetadataRecordId="+getInitiatingEvent().getServiceMetadataId(),e);
            localServiceMetadataRecord.setState(ServiceMetadataDocumentState.ERROR);
            localServiceMetadataRecord.setErrorMessage(  convertToString(e) );
            save(false);
        }


        return this;
    }

    // for re-entry - need to clean up object (and database)
    public void prune(){
        List<CapabilitiesDocument> capDocuments = new ArrayList<>();
        List<OperatesOnRemoteDatasetMetadataRecord> opsOnDocuments = new ArrayList<>();

        //find objects and de-attach them
        for(ServiceDocumentLink link : localServiceMetadataRecord.getServiceDocumentLinks()) {
            CapabilitiesDocument capDoc = link.getCapabilitiesDocument();
            if (capDoc != null) {
                if (capDoc.getServiceDocumentLink() != null)
                    capDoc.getServiceDocumentLink().setCapabilitiesDocument(null);
                capDoc.setServiceDocumentLink(null);
                capDocuments.add(capDoc); // to be deleted
            }
        }

        for(OperatesOnLink link : localServiceMetadataRecord.getOperatesOnLinks()) {
            OperatesOnRemoteDatasetMetadataRecord record = link.getDatasetMetadataRecord();
            if (record != null) {
                record.getOperatesOnLink().setDatasetMetadataRecord(null);
                record.setOperatesOnLink(null);
                opsOnDocuments.add(record); // to be deleted
            }
        }

        if (capDocuments.isEmpty() && opsOnDocuments.isEmpty())
            return; //nothing to do

        save(true); //save with objects detached

        for (CapabilitiesDocument capDoc: capDocuments){
            capabilitiesDocumentRepo.delete(capDoc);

        }
        for(OperatesOnRemoteDatasetMetadataRecord record : opsOnDocuments) {
            operatesOnRemoteDatasetMetadataRecordRepo.delete(record);
        }

        save(true);
    }

    public void save(boolean reload){
        localServiceMetadataRecord = localServiceMetadataRecordRepo.save(localServiceMetadataRecord);
        localServiceMetadataRecord=null;
        if (reload)
            localServiceMetadataRecord = localServiceMetadataRecordRepo.fullId(localServiceMetadataRecord.getServiceMetadataDocumentId());
    }


    private void processOperatesOnLinks() throws ExecutionException, InterruptedException {
        int nlinks = localServiceMetadataRecord.getOperatesOnLinks().size();
        int linkIdx = 0;
        logger.debug("processing  "+nlinks+ " operates on links for documentid="+getInitiatingEvent().getServiceMetadataId());



        ForkJoinPool pool = new ForkJoinPool(3);
        int nTotal = localServiceMetadataRecord.getOperatesOnLinks().size();
        AtomicInteger counter = new AtomicInteger(0);
        try {
            pool.submit(() ->
                    localServiceMetadataRecord.getOperatesOnLinks().stream().parallel()
                            .forEach(x -> {
                                handleOperatesOnLink(x);
                                int ndone = counter.incrementAndGet();
                                String text = "processed operates on DS link " + ndone + " of " + nTotal;
                                 if (x.getDatasetMetadataRecord() !=null) {
                                   text += " - returned a Dataset Metadata Record" ;
                                }
                                else {
                                     text += " -  DID NOT return a Dataset Metadata Record" ;
                                }
                                logger.debug(text);
                            })
            ).get();
        }
        finally {
            pool.shutdown();
        }



//        for (OperatesOnLink link : localServiceMetadataRecord.getOperatesOnLinks()) {
//            logger.debug("processing operates on link "+linkIdx+" of "+nlinks+" links");
//            linkIdx++;
//            handleOperatesOnLink(link);
//            if (link.getDatasetMetadataRecord() !=null) {
//                logger.debug("link returned a Dataset Metadata Record");
//            }
//            else {
//                logger.debug("link DID NOT return a Dataset Metadata Record");
//            }
//        }
        logger.debug("FINISHED processing  "+nlinks+ " operates on links for documentid="+getInitiatingEvent().getServiceMetadataId());
    }

    private void handleOperatesOnLink(OperatesOnLink link) {
        try {
            String jobid = getInitiatingEvent().getLinkCheckJobId();
            link = retrieveOperatesOnLink.process(link,jobid);

            link.setLinkState(LinkState.Complete);
        }
        catch(Exception e){
            logger.error("error occurred while processing ServiceMetadataDocumentId="+localServiceMetadataRecord.getServiceMetadataDocumentId()
                    +", OperatesOnLink="+link+", error="+e.getMessage());
            link.setLinkState(LinkState.ERROR);
            link.setErrorMessage(  convertToString(e) );

        }
    }

    private void processDocumentLinks() throws ExecutionException, InterruptedException {
        int nlinks = localServiceMetadataRecord.getServiceDocumentLinks().size();
        // get a more cannonical list of URLs -- this way we can tell if they are the same easier...
        List<String> t = localServiceMetadataRecord.getServiceDocumentLinks().stream()
                .map(x-> {
                    try {
                          x.setFixedURL(capabilitiesLinkFixer.fix(x.getRawURL()));
                    } catch (Exception e) {
                        e.printStackTrace();
                        x.setFixedURL(x.getRawURL());
                    }
                    return x;
                })
                .map(x->x.getFixedURL())
                .collect(Collectors.toList());
        int linkIdx = 0;
        logger.debug("processing "+nlinks+" service document links for documentid="+getInitiatingEvent().getServiceMetadataId());
        List<String> processedURLs = new ArrayList<>();
        for (ServiceDocumentLink link : localServiceMetadataRecord.getServiceDocumentLinks()) {
            logger.debug("processing service document link "+linkIdx+" of "+nlinks+" links");
            linkIdx++;
            String thisURL = link.getFixedURL();
            if (processedURLs.contains(thisURL))
            {
                logger.debug("this url has already been processed - no action!");
            }
            else {
                processedURLs.add(link.getFixedURL());
                handleSingleDocumentLink(link);
            }
        }
        logger.debug("FINISHED processing "+nlinks+" service document links for documentid="+getInitiatingEvent().getServiceMetadataId());
    }

    // should retrieve a capabilities document
    // if so;
    //   a) resolve the service metadata link (in cap doc)
    //   b) for each of the layers in the cap doc, retrieve the dataset metadata link
    private ServiceDocumentLink handleSingleDocumentLink(ServiceDocumentLink link) throws ExecutionException, InterruptedException {
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

            ForkJoinPool pool = new ForkJoinPool(3);
            int nTotal = capabilitiesDocument.getCapabilitiesDatasetMetadataLinkList().size();
            AtomicInteger counter = new AtomicInteger(0);
            try {
                pool.submit(() ->
                        capabilitiesDocument.getCapabilitiesDatasetMetadataLinkList().stream().parallel()
                                .forEach(x -> {
                                    handleLayerDatasetLink(x);
                                    int ndone = counter.incrementAndGet();
                                    logger.debug("processed link cap's DS link " + ndone + " of " + nTotal); // a wee bit of a lie, but will be "accurate"
                                })
                ).get();
            }
            finally {
                pool.shutdown();
            }

//            for (CapabilitiesDatasetMetadataLink capabilitiesDatasetMetadataLink : capabilitiesDocument.getCapabilitiesDatasetMetadataLinkList()) {
//                logger.debug("processing link "+linkIdx+" of "+nlinks+" dataset links from the capabilities document");
//                linkIdx++;
//                handleLayerDatasetLink(capabilitiesDatasetMetadataLink);
//                if (capabilitiesDatasetMetadataLink.getCapabilitiesRemoteDatasetMetadataDocument() !=null) {
//                    logger.debug("link produced a Dataset Metadata Document");
//                    CapabilitiesRemoteDatasetMetadataDocument capabilitiesRemoteDatasetMetadataDocument =capabilitiesDatasetMetadataLink.getCapabilitiesRemoteDatasetMetadataDocument();
//                }
//                else {
//                    logger.debug("link DID NOT produce a Dataset Metadata Document");
//                }
//            }
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
            logger.error("error occurred while processing ServiceMetadataDocumentId="+localServiceMetadataRecord.getServiceMetadataDocumentId()
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
            logger.error("error occurred while processing ServiceMetadataDocumentId="+localServiceMetadataRecord.getServiceMetadataDocumentId()
                    +", RemoteServiceMetadataRecordLink="+rsmrl+", error="+e.getMessage(),e);
            rsmrl.setLinkState(LinkState.ERROR);
            rsmrl.setErrorMessage(  convertToString(e) );
        }
    }

    private ServiceDocumentLink getCapabilitiesDoc(ServiceDocumentLink link) {
        try {
            link = (ServiceDocumentLink) retrieveServiceDocumentLink.process(link);

            link.setLinkState(LinkState.Complete);
        }
        catch (Exception e){
            logger.error("error occurred while processing ServiceMetadataDocumentId="+localServiceMetadataRecord.getServiceMetadataDocumentId()
               +", ServiceDocumentLink="+link+", error="+e.getMessage(),e);
            link.setLinkState(LinkState.ERROR);
            link.setErrorMessage(  convertToString(e) );
        }

        return link;
    }


    @Override
    public EventProcessor_ProcessServiceDocLinksEvent internalProcessing() throws Exception {
        //handle post-procssing
        //reload for any outstanding transactions
      // localServiceMetadataRecord = localServiceMetadataRecordRepo.findById(getInitiatingEvent().getServiceMetadataId()).get();// make sure we re-load
        localServiceMetadataRecord = localServiceMetadataRecordRepo.fullId(getInitiatingEvent().getServiceMetadataId());// make sure we re-load

        try {
            capabilitiesResolvesIndicators.process(localServiceMetadataRecord); // simple record->cap indicators
            capabilitiesServiceLinkIndicators.process(localServiceMetadataRecord); // see if caps' service record matches local service record
            capabilitiesServiceMatchesLocalServiceIndicators.process(localServiceMetadataRecord); // see if cap links back to original service records
            capabilitiesDatasetLinksResolveIndicators.process(localServiceMetadataRecord); // looks at the cap's DS layers
            serviceOperatesOnIndicators.process(localServiceMetadataRecord); // check the operates on links

            localServiceMetadataRecord.setState(ServiceMetadataDocumentState.LINKS_PROCESSED);
            localServiceMetadataRecord.setHumanReadable(humanReadableServiceMetadata.getHumanReadable(localServiceMetadataRecord));
            save(false);
            logger.debug("finished post processing  documentid="+getInitiatingEvent().getServiceMetadataId()  );
        }
        catch(Exception e){
            logger.error("post processing exception for serviceMetadataRecordId="+getInitiatingEvent().getServiceMetadataId(),e);
            localServiceMetadataRecord.setState(ServiceMetadataDocumentState.ERROR);
            localServiceMetadataRecord.setErrorMessage(  convertToString(e) );
            save(false);
        }

        return this;
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


}
