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
import net.geocat.service.helper.ShouldTransitionOutOfLinkProcessing;
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
//
//    @Autowired
//    CapabilitiesResolvesIndicators capabilitiesResolvesIndicators;

    @Autowired
    DatasetToLayerIndicators datasetToLayerIndicators;

    @Autowired
    ShouldTransitionOutOfLinkProcessing shouldTransitionOutOfLinkProcessing;

    LocalDatasetMetadataRecord localDatasetMetadataRecord;




    @Override
    public EventProcessor_ProcessDatasetDocLinksEvent internalProcessing() throws Exception {
//        localDatasetMetadataRecord = localDatasetMetadataRecordRepo.findById(getInitiatingEvent().getDatasetDocumentId()).get();// make sure we re-load
//        localDatasetMetadataRecord.setState(ServiceMetadataDocumentState.LINKS_PROCESSED);
//        localDatasetMetadataRecordRepo.save(localDatasetMetadataRecord);
     //   metadataRecord.setState(ServiceMetadataDocumentState.LINKS_EXTRACTED);
        localDatasetMetadataRecordRepo.updateStateNotNotApplicatable(getInitiatingEvent().getDatasetDocumentId(), ServiceMetadataDocumentState.LINKS_PROCESSED);

        return this;
    }


    @Override
    public EventProcessor_ProcessDatasetDocLinksEvent externalProcessing () throws Exception {
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
        if (shouldTransitionOutOfLinkProcessing.shouldSendMessage(linkCheckJobId,getInitiatingEvent().getDatasetDocumentId()))
        {
            //done
            Event e = eventFactory.createAllLinksCheckedEvent(linkCheckJobId);
            result.add(e);
        }
        return result;
    }


}
