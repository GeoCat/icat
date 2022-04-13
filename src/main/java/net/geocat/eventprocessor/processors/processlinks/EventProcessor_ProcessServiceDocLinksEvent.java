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
import net.geocat.database.linkchecker.entities.helper.SHA2JobIdCompositeKey;
import net.geocat.database.linkchecker.entities.helper.ServiceMetadataDocumentState;
import net.geocat.database.linkchecker.repos.*;

import net.geocat.eventprocessor.BaseEventProcessor;
import net.geocat.eventprocessor.processors.processlinks.postprocessing.*;
import net.geocat.events.Event;
import net.geocat.events.EventFactory;
import net.geocat.events.processlinks.ProcessServiceDocLinksEvent;
import net.geocat.service.*;
import net.geocat.service.capabilities.CapabilitiesDownloadingService;
import net.geocat.service.capabilities.CapabilitiesLinkFixer;
import net.geocat.service.helper.ShouldTransitionOutOfLinkProcessing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
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

//   @Autowired
//    RemoteServiceMetadataRecordRepo remoteServiceMetadataRecordRepo;

   @Autowired
   HumanReadableServiceMetadata humanReadableServiceMetadata;


   @Autowired
    ServiceDocOperatesOnProcessor serviceDocOperatesOnProcessor;

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
    CapabilitiesDownloadingService capabilitiesDownloadingService;

    @Autowired
    DocumentLinkToCapabilitiesProcessor documentLinkToCapabilitiesProcessor;


    @Autowired
    CapabilitiesLinkFixer capabilitiesLinkFixer;

    @Autowired
    ShouldTransitionOutOfLinkProcessing shouldTransitionOutOfLinkProcessing;

    LocalServiceMetadataRecord localServiceMetadataRecord;


    boolean throwException = true;

    @Override
    public EventProcessor_ProcessServiceDocLinksEvent externalProcessing() throws Exception {
        localServiceMetadataRecord = localServiceMetadataRecordRepo.findById(getInitiatingEvent().getServiceMetadataId()).get();// make sure we re-load
        if (localServiceMetadataRecord.getState() == ServiceMetadataDocumentState.NOT_APPLICABLE)
            return this; //nothing to do


     //   localServiceMetadataRecord = localServiceMetadataRecordRepo.fullId(getInitiatingEvent().getServiceMetadataId());// make sure we re-load

       // prune(); // remove any previous work (if this is being re-run)

        try {
            int nlinksCap = localServiceMetadataRecord.getServiceDocumentLinks().size();
            int nlinksOperates = localServiceMetadataRecord.getOperatesOnLinks().size();
            logger.debug("processing links SERVICE documentid="+getInitiatingEvent().getServiceMetadataId()+", with fileID="+ localServiceMetadataRecord.getFileIdentifier() +" that has "+nlinksCap+" document links, and "+nlinksOperates+" operates on links");

             documentLinkToCapabilitiesProcessor.processDocumentLinks(localServiceMetadataRecord);


            processOperatesOnLinks(localServiceMetadataRecord);

          //  localServiceMetadataRecord.setState(ServiceMetadataDocumentState.LINKS_PROCESSED);

            save();
            logger.trace("finished  processing links for documentid="+getInitiatingEvent().getServiceMetadataId()  );

        }
        catch(Exception e){
            logger.error("exception for serviceMetadataRecordId="+getInitiatingEvent().getServiceMetadataId(),e);
            localServiceMetadataRecord.setState(ServiceMetadataDocumentState.ERROR);
            localServiceMetadataRecord.setErrorMessage(  convertToString(e) );
            save();
        }


        return this;
    }


    public void save( ){
        localServiceMetadataRecord = localServiceMetadataRecordRepo.save(localServiceMetadataRecord);

    }

    private void processOperatesOnLinks(LocalServiceMetadataRecord localServiceMetadataRecord) throws  Exception {
        serviceDocOperatesOnProcessor.process(localServiceMetadataRecord);
    }





//    private void processDocumentLinks() throws Exception {
//        int nlinks = localServiceMetadataRecord.getServiceDocumentLinks().size();
//
//        fixURLs();
//        int linkIdx = 0;
//      //  logger.trace("processing "+nlinks+" service document links for documentid="+getInitiatingEvent().getServiceMetadataId());
//        List<String> processedURLs = new ArrayList<>();
//        for (ServiceDocumentLink link : localServiceMetadataRecord.getServiceDocumentLinks()) {
//            logger.debug("processing service document link "+linkIdx+" of "+nlinks+" links for  documentid="+getInitiatingEvent().getServiceMetadataId());
//            linkIdx++;
//            String thisURL = link.getFixedURL();
//            if (processedURLs.contains(thisURL))
//            {
//               // logger.debug("this url has already been processed - no action!");
//                link.setLinkState(LinkState.Redundant);
//            }
//            else {
//                processedURLs.add(link.getFixedURL());
//                capabilitiesDownloadingService.handleLink(link);
//            }
//        }
//        logger.trace("FINISHED processing service document links for documentid="+getInitiatingEvent().getServiceMetadataId());
//    }

//    // get a more cannonical list of URLs -- this way we can tell if they are the same easier...
//    private List<String> fixURLs() {
//        return localServiceMetadataRecord.getServiceDocumentLinks().stream()
//                .map(x-> {
//                    try {
//                          x.setFixedURL(capabilitiesLinkFixer.fix(x.getRawURL(),localServiceMetadataRecord.getMetadataServiceType()));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        x.setFixedURL(x.getRawURL());
//                    }
//                    return x;
//                })
//                .map(x->x.getFixedURL())
//                .collect(Collectors.toList());
//    }

    //optimize - don't keep loading it
    List<CapabilitiesDocument> getCapabilities(LocalServiceMetadataRecord record) {
        List<String>  cap_sha2s =     record.getServiceDocumentLinks().stream()
                .filter(x->x.getSha2() != null)
                .map(x->x.getSha2())
                .distinct()
                .collect(Collectors.toList());

        List<CapabilitiesDocument> capdocs = cap_sha2s.stream()
                .map(x-> capabilitiesDocumentRepo.findById(new SHA2JobIdCompositeKey(x ,record.getLinkCheckJobId())).get())
                .collect(Collectors.toList());

        return capdocs;
    }


    @Override
    public EventProcessor_ProcessServiceDocLinksEvent internalProcessing() throws Exception {
        //handle post-procssing
        //reload for any outstanding transactions
        localServiceMetadataRecord = localServiceMetadataRecordRepo.findById(getInitiatingEvent().getServiceMetadataId()).get();// make sure we re-load
        if (localServiceMetadataRecord.getState() == ServiceMetadataDocumentState.NOT_APPLICABLE)
            return this; //nothing to do

        try {

            List<CapabilitiesDocument> capDocs = getCapabilities(localServiceMetadataRecord);

            capabilitiesResolvesIndicators.process(localServiceMetadataRecord,capDocs); // simple record->cap indicators
            capabilitiesServiceLinkIndicators.process(localServiceMetadataRecord,capDocs); // see if a cap links back to original service records

      //    capabilitiesServiceMatchesLocalServiceIndicators.process(localServiceMetadataRecord); // see if cap links back to original service records
             capabilitiesDatasetLinksResolveIndicators.process(localServiceMetadataRecord,capDocs); // looks at the cap's DS layers
             serviceOperatesOnIndicators.process(localServiceMetadataRecord,capDocs); // check the operates on links

           localServiceMetadataRecord.setState(ServiceMetadataDocumentState.LINKS_PROCESSED);
           localServiceMetadataRecord.setHumanReadable(humanReadableServiceMetadata.getHumanReadable(localServiceMetadataRecord));
           save( );
           logger.debug("finished initial post processing  documentid="+getInitiatingEvent().getServiceMetadataId()  );
        }
        catch(Exception e){
            logger.error("post processing exception for serviceMetadataRecordId="+getInitiatingEvent().getServiceMetadataId(),e);
            localServiceMetadataRecord.setState(ServiceMetadataDocumentState.ERROR);
            localServiceMetadataRecord.setErrorMessage(  convertToString(e) );
            save( );
        }

        return this;
    }


    @Override
    public List<Event> newEventProcessing() {
        List<Event> result = new ArrayList<>();

        String linkCheckJobId = getInitiatingEvent().getLinkCheckJobId();
//        if (metadataService.linkProcessingComplete(linkCheckJobId))
//        {
//            //done
//            Event e = eventFactory.createAllLinksCheckedEvent(linkCheckJobId);
//            result.add(e);
//        }
        if (shouldTransitionOutOfLinkProcessing.shouldSendMessage(linkCheckJobId,getInitiatingEvent().getServiceMetadataId()))
        {
            //done
            Event e = eventFactory.createAllLinksCheckedEvent(linkCheckJobId);
            result.add(e);
        }
         return result;
    }


}
