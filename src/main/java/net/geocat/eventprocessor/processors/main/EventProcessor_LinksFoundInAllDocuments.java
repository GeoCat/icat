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

package net.geocat.eventprocessor.processors.main;

import net.geocat.database.linkchecker.entities.LinkCheckJob;
import net.geocat.database.linkchecker.entities.LinkCheckJobState;
import net.geocat.database.linkchecker.repos.LinkCheckJobRepo;
import net.geocat.database.linkchecker.repos.LocalDatasetMetadataRecordRepo;
import net.geocat.database.linkchecker.repos.LocalNotProcessedMetadataRecordRepo;
import net.geocat.database.linkchecker.repos.LocalServiceMetadataRecordRepo;
import net.geocat.database.linkchecker.service.LinkCheckJobService;
import net.geocat.eventprocessor.BaseEventProcessor;
import net.geocat.events.Event;
import net.geocat.events.EventFactory;
import net.geocat.events.findlinks.LinksFoundInAllDocuments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
public class EventProcessor_LinksFoundInAllDocuments extends BaseEventProcessor<LinksFoundInAllDocuments> {

    Logger logger = LoggerFactory.getLogger(EventProcessor_LinksFoundInAllDocuments.class);


    @Autowired
    LinkCheckJobService linkCheckJobService;

    @Autowired
    LocalDatasetMetadataRecordRepo localDatasetMetadataRecordRepo;

    @Autowired
    LocalServiceMetadataRecordRepo localServiceMetadataRecordRepo;

    @Autowired
    LocalNotProcessedMetadataRecordRepo localNotProcessedMetadataRecordRepo;

    @Autowired
    LinkCheckJobRepo linkCheckJobRepo;

//    @Autowired
//    LinkService linkService;

    @Autowired
    EventFactory eventFactory;

    @Override
    public EventProcessor_LinksFoundInAllDocuments externalProcessing() throws Exception {
        return this;
    }


    @Override
    public EventProcessor_LinksFoundInAllDocuments internalProcessing() throws Exception {
        String linkCheckJobId = getInitiatingEvent().getLinkCheckJobId();
        linkCheckJobService.updateLinkCheckJobStateInDB(linkCheckJobId, LinkCheckJobState.LINKS_FOUND);
        LinkCheckJob job = linkCheckJobService.find(linkCheckJobId);

        long nService = localServiceMetadataRecordRepo.countByLinkCheckJobId(linkCheckJobId);
        long nData = localDatasetMetadataRecordRepo.countByLinkCheckJobId(linkCheckJobId);
        long nOther = localNotProcessedMetadataRecordRepo.countByLinkCheckJobId(linkCheckJobId);

        job.setNumberOfLocalDatasetRecords(nData);
        job.setNumberOfLocalServiceRecords(nService);
        job.setNumberOfNotProcessedDatasetRecords(nOther);

        job = linkCheckJobRepo.save(job);

        return this;
    }


    @Override
    public List<Event> newEventProcessing() {
        logger.debug("LinksFoundInAllDocuments - all documents were parsed and saved, linkcheckjobid="+ getInitiatingEvent().getLinkCheckJobId());

        List<Event> result = new ArrayList<>();
        linkCheckJobService.updateLinkCheckJobStateInDB(getInitiatingEvent().getLinkCheckJobId(), LinkCheckJobState.CHECKING_LINKS);

        Event e = eventFactory.createStartLinkProcessingEvent(getInitiatingEvent().getLinkCheckJobId());
        result.add(e);
//
//        List<Link> links = linkService.findLinks(getInitiatingEvent().getLinkCheckJobId());
//        for (Link link : links) {
//            ProcessServiceDocLinkEvent e = eventFactory.createProcessLinkEvent(link);
//            result.add(e);
//        }

        return result;
    }
}