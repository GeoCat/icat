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
import net.geocat.database.linkchecker.repos.LocalDatasetMetadataRecordRepo;
import net.geocat.database.linkchecker.repos.LocalServiceMetadataRecordRepo;
import net.geocat.eventprocessor.BaseEventProcessor;
import net.geocat.eventprocessor.processors.processlinks.EventProcessor_ProcessServiceDocLinksEvent;
import net.geocat.eventprocessor.processors.processlinks.EventProcessor_StartLinkProcessingEvent;
import net.geocat.events.Event;
import net.geocat.events.EventFactory;
import net.geocat.events.postprocess.StartPostProcessEvent;
import net.geocat.events.processlinks.StartLinkProcessingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
public class EventProcessor_StartPostProcessEvent extends BaseEventProcessor<StartPostProcessEvent> {

    Logger logger = LoggerFactory.getLogger(EventProcessor_ProcessServiceDocLinksEvent.class);

    @Autowired
    EventFactory eventFactory;

    @Autowired
    LocalServiceMetadataRecordRepo localServiceMetadataRecordRepo;

    @Autowired
    LocalDatasetMetadataRecordRepo localDatasetMetadataRecordRepo;



    @Override
    public EventProcessor_StartPostProcessEvent externalProcessing() throws Exception {
        return this;
    }


    @Override
    public EventProcessor_StartPostProcessEvent internalProcessing() throws Exception {
        return this;
    }


    @Override
    public List<Event> newEventProcessing() {
        String linkCheckJobId = getInitiatingEvent().getLinkCheckJobId();

        List<Event> result = new ArrayList<>();

        List<Long> serviceIds =  localServiceMetadataRecordRepo.searchAllServiceIds(linkCheckJobId);
        for(Long id : serviceIds){
            Event e = eventFactory.createPostProcessServiceDocumentEvent(id,linkCheckJobId);
            result.add(e);
        }

        List<Long> datasetIds =  localDatasetMetadataRecordRepo.searchAllDatasetIds(linkCheckJobId);
        for(Long id : datasetIds){
            Event e = eventFactory.createPostProcessDatasetDocumentEvent(id,linkCheckJobId);
            result.add(e);
        }

//        for(LocalServiceMetadataRecord record : localServiceMetadataRecordRepo.findByLinkCheckJobId(linkCheckJobId)) {
//            long id = record.getServiceMetadataDocumentId();
//            Event e = eventFactory.createPostProcessServiceDocumentEvent(id,linkCheckJobId);
//            result.add(e);
//        }
//
//        for(LocalDatasetMetadataRecord record : localDatasetMetadataRecordRepo.findByLinkCheckJobId(linkCheckJobId)) {
//            long id = record.getDatasetMetadataDocumentId();
//            Event e = eventFactory.createPostProcessDatasetDocumentEvent(id,linkCheckJobId);
//            result.add(e);
//        }

        return result;
    }
}

