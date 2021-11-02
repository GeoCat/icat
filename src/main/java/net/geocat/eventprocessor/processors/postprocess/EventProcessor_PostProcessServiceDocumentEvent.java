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

import net.geocat.database.linkchecker.entities.LocalDatasetMetadataRecord;
import net.geocat.database.linkchecker.entities.LocalServiceMetadataRecord;
import net.geocat.database.linkchecker.entities.helper.ServiceMetadataDocumentState;
import net.geocat.database.linkchecker.repos.LocalDatasetMetadataRecordRepo;
import net.geocat.database.linkchecker.repos.LocalServiceMetadataRecordRepo;
import net.geocat.eventprocessor.BaseEventProcessor;
import net.geocat.eventprocessor.processors.processlinks.EventProcessor_ProcessServiceDocLinksEvent;
import net.geocat.events.Event;
import net.geocat.events.EventFactory;
import net.geocat.events.postprocess.PostProcessServiceDocumentEvent;
import net.geocat.events.processlinks.StartLinkProcessingEvent;
import net.geocat.service.MetadataService;
import net.geocat.service.helper.ShouldTransitionOutOfPostProcessing;
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
public class EventProcessor_PostProcessServiceDocumentEvent extends BaseEventProcessor<PostProcessServiceDocumentEvent> {

    Logger logger = LoggerFactory.getLogger(EventProcessor_ProcessServiceDocLinksEvent.class);

    @Autowired
    LocalServiceMetadataRecordRepo localServiceMetadataRecordRepo;

    @Autowired
    MetadataService metadataService;

    @Autowired
    EventFactory eventFactory;

    @Autowired
    ShouldTransitionOutOfPostProcessing shouldTransitionOutOfPostProcessing;

    LocalServiceMetadataRecord localServiceMetadataRecord;


    @Override
    public EventProcessor_PostProcessServiceDocumentEvent externalProcessing() throws Exception {
//        localServiceMetadataRecord = localServiceMetadataRecordRepo.findById(getInitiatingEvent().getServiceMetadataId()).get();// make sure we re-load
//        localServiceMetadataRecord.setState(ServiceMetadataDocumentState.LINKS_POSTPROCESSED);
//        localServiceMetadataRecordRepo.save(localServiceMetadataRecord);


        localServiceMetadataRecordRepo.updateState(getInitiatingEvent().getServiceMetadataId(), ServiceMetadataDocumentState.LINKS_POSTPROCESSED);
        return this;
    }

//
//    public void save(boolean reload){
//        localServiceMetadataRecord = localServiceMetadataRecordRepo.save(localServiceMetadataRecord);
//        localServiceMetadataRecord = null;
//        if (reload)
//            localServiceMetadataRecord = localServiceMetadataRecordRepo.fullId(localServiceMetadataRecord.getServiceMetadataDocumentId());
//    }


    @Override
    public EventProcessor_PostProcessServiceDocumentEvent internalProcessing() throws Exception {
        return this;
    }


    @Override
    public List<Event> newEventProcessing() {
        List<Event> result = new ArrayList<>();

        logger.debug("finished post-processing SERVICE id="+getInitiatingEvent().getServiceMetadataId());

        String linkCheckJobId = getInitiatingEvent().getLinkCheckJobId();
//        if (metadataService.linkPostProcessingComplete(linkCheckJobId))
//        {
//            //done
//            Event e = eventFactory.createAllPostProcessingCompleteEvent(linkCheckJobId);
//            result.add(e);
//        }
        if (shouldTransitionOutOfPostProcessing.shouldSendMessage(linkCheckJobId,getInitiatingEvent().getServiceMetadataId()))
        {
            //done
            Event e = eventFactory.createAllPostProcessingCompleteEvent(linkCheckJobId);
            result.add(e);
        }
        return result;
    }
}


