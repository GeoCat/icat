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

package net.geocat.eventprocessor.processors.findlinks;

import net.geocat.database.harvester.entities.EndpointJob;
import net.geocat.database.harvester.entities.MetadataRecord;
import net.geocat.database.harvester.repos.EndpointJobRepo;
import net.geocat.database.harvester.repos.MetadataRecordRepo;
import net.geocat.database.linkchecker.entities.LinkCheckJob;
import net.geocat.database.linkchecker.entities.LocalDatasetMetadataRecord;
import net.geocat.database.linkchecker.entities.LocalServiceMetadataRecord;
import net.geocat.database.linkchecker.repos.LocalDatasetMetadataRecordRepo;
import net.geocat.database.linkchecker.repos.LocalServiceMetadataRecordRepo;
import net.geocat.database.linkchecker.service.LinkCheckJobService;
import net.geocat.database.linkchecker.service.MetadataDocumentFactory;
import net.geocat.eventprocessor.BaseEventProcessor;
import net.geocat.events.Event;
import net.geocat.events.EventFactory;
import net.geocat.events.findlinks.ProcessLocalMetadataDocumentEvent;
import net.geocat.events.findlinks.StartProcessDocumentsEvent;
import net.geocat.service.BlobStorageService;
import net.geocat.xml.XmlDocumentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class EventProcessor_StartProcessDocumentsEvent extends BaseEventProcessor<StartProcessDocumentsEvent> {

    Logger logger = LoggerFactory.getLogger(EventProcessor_StartProcessDocumentsEvent.class);

    @Autowired
    EndpointJobRepo endpointJobRepo;

    @Autowired
    MetadataRecordRepo metadataRecordRepo;

    @Autowired
    MetadataDocumentFactory metadataDocumentFactory;

    @Autowired
    EventFactory eventFactory;

    @Autowired
    XmlDocumentFactory xmlDocumentFactory;

    @Autowired
    BlobStorageService blobStorageService;

    @Autowired
    LocalDatasetMetadataRecordRepo localDatasetMetadataRecordRepo;

    @Autowired
    LocalServiceMetadataRecordRepo localServiceMetadataRecordRepo;

    @Autowired
    LinkCheckJobService linkCheckJobService;


    List<net.geocat.database.harvester.entities.MetadataRecord> metadataRecords;

    @Override
    public EventProcessor_StartProcessDocumentsEvent externalProcessing() {
        return this;
    }


    @Override
    public EventProcessor_StartProcessDocumentsEvent internalProcessing() throws Exception {

        String linkCheckJobId = getInitiatingEvent().getLinkCheckJobId();

        List<EndpointJob> endpointJobs = endpointJobRepo.findByHarvestJobId(getInitiatingEvent().getHarvestJobId());
        List<Long> endpointIds = endpointJobs.stream().map(x -> x.getEndpointJobId()).collect(Collectors.toList());

        metadataRecords = metadataRecordRepo.findByEndpointJobIdIn(endpointIds);

       // metadataRecords = metadataRecords.stream().filter(x->x.getSha2().startsWith("EE4EED5D2C77F6289DD28CFF209F4A14E7FA2B923C07FD959CFC696FFC268654")).collect(Collectors.toList());
    //    metadataRecords= metadataRecords.subList(0,10);
    //   metadataRecords = metadataRecords.stream().filter(x-> x.getMetadataRecordId() == 7661).collect(Collectors.toList());//todo - remove

    //  metadataRecords = metadataRecords.stream().filter(x-> x.getMetadataRecordId() == 2691).collect(Collectors.toList());
//           List<Long> items = Arrays.asList(new Long[] {4937L} );
//        metadataRecords = metadataRecords.stream().filter(x-> items.contains(x.getMetadataRecordId() )).collect(Collectors.toList());

        //metadataRecords = metadataRecords.stream().filter(x-> items.contains(x.getMetadataRecordId() )).collect(Collectors.toList());


//        List<String> items = Arrays.asList(new String[] {"eab36660-76ec-11e0-994d-0002a5d5c51b"});
//
        //todo - remove
//     List<String> items = Arrays.asList(new String[] { "7c2d4e17-608b-aa17-c9db-951db60ff22b",
//              "19705c73-3409-0002-c9db-951db60ff22b"
//     });
//     metadataRecords = metadataRecords.stream().filter(x-> items.contains(x.getRecordIdentifier() )).collect(Collectors.toList());
//     metadataRecords = metadataRecords.stream().filter(x-> !items.contains(x.getRecordIdentifier() )).collect(Collectors.toList());

      //  metadataRecords =    metadataRecords.subList(0,10000);





        //--------------------------------------------------------------------------------------------------------

        int nTotal= metadataRecords.size();
        //remove duplicates
        metadataRecords = metadataRecords.stream().distinct().collect(Collectors.toList());
        int nDistinct = metadataRecords.size();

        if (nTotal != nDistinct){
            logger.warn("DUPLICATE RECORDS REMOVED - linkcheckjob:"+linkCheckJobId+", nRecords="+nTotal+", nDistinct="+nDistinct);
        }


        LinkCheckJob job = linkCheckJobService.updateNumberofDocumentsInBatch(linkCheckJobId, (long) metadataRecords.size());


        return this;
    }




    @Override
    public List<Event> newEventProcessing() {
        List<Event> result = new ArrayList<>();
        String linkCheckJobId = getInitiatingEvent().getLinkCheckJobId();
        String harvestJobId = getInitiatingEvent().getHarvestJobId();

        logger.debug("There are "+metadataRecords.size()+" records to process.");

//        long nService = metadataRecords.stream().filter(x->x instanceof LocalServiceMetadataRecord).count();
//        long nDataset = metadataRecords.stream().filter(x->x instanceof LocalDatasetMetadataRecord).count();
//
//        logger.debug("There are "+nService+" service records and "+nDataset+" dataset records to process.");


        for (net.geocat.database.harvester.entities.MetadataRecord record : metadataRecords) {
            ProcessLocalMetadataDocumentEvent e =
                    eventFactory.createProcessServiceMetadataDocumentEvent(linkCheckJobId,
                            record.getSha2(),
                            record.getMetadataRecordId());
            result.add(e);

        }

     //   result = result.subList(0,180000);
        logger.debug("Finished generating to-process-document messages for "+metadataRecords.size()+" records. (camel will send all in one tx)");


        return result;
    }
}
