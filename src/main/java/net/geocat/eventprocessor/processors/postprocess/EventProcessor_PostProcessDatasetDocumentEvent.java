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
import net.geocat.database.linkchecker.entities.OperatesOnLink;
import net.geocat.database.linkchecker.entities.helper.IndicatorStatus;
import net.geocat.database.linkchecker.entities.helper.ServiceMetadataDocumentState;
import net.geocat.database.linkchecker.repos.LocalDatasetMetadataRecordRepo;
import net.geocat.database.linkchecker.repos.LocalServiceMetadataRecordRepo;
import net.geocat.eventprocessor.BaseEventProcessor;
import net.geocat.eventprocessor.processors.processlinks.EventProcessor_ProcessServiceDocLinksEvent;
import net.geocat.events.Event;
import net.geocat.events.EventFactory;
import net.geocat.events.postprocess.PostProcessDatasetDocumentEvent;
 import net.geocat.service.MetadataService;
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
public class EventProcessor_PostProcessDatasetDocumentEvent extends BaseEventProcessor<PostProcessDatasetDocumentEvent> {

    Logger logger = LoggerFactory.getLogger(EventProcessor_ProcessServiceDocLinksEvent.class);

    @Autowired
    LocalDatasetMetadataRecordRepo localDatasetMetadataRecordRepo;

    @Autowired
    MetadataService metadataService;

    @Autowired
    EventFactory eventFactory;

    @Autowired
    LocalServiceMetadataRecordRepo localServiceMetadataRecordRepo;

    LocalDatasetMetadataRecord localDatasetMetadataRecord;


    @Override
    public EventProcessor_PostProcessDatasetDocumentEvent externalProcessing() throws Exception {
        localDatasetMetadataRecord = localDatasetMetadataRecordRepo.findById(getInitiatingEvent().getDatasetDocumentId()).get();// make sure we re-load
        return this;
    }


    public void save(boolean reload){
        localDatasetMetadataRecord = localDatasetMetadataRecordRepo.save(localDatasetMetadataRecord);
        localDatasetMetadataRecord = null;
        if (reload)
            localDatasetMetadataRecord = localDatasetMetadataRecordRepo.findById(getInitiatingEvent().getDatasetDocumentId()).get();// make sure we re-load
    }


    @Override
    public EventProcessor_PostProcessDatasetDocumentEvent internalProcessing() throws Exception {

        try{
            process();
            localDatasetMetadataRecord.setState(ServiceMetadataDocumentState.LINKS_POSTPROCESSED);
            save(false);
            logger.debug("finished postprocessing documentid="+getInitiatingEvent().getDatasetDocumentId()  );

        }
        catch(Exception e){
            logger.error("postprocessing exception for datasetMetadataRecordId="+getInitiatingEvent().getDatasetDocumentId(),e);
            localDatasetMetadataRecord.setState(ServiceMetadataDocumentState.ERROR);
            localDatasetMetadataRecord.setErrorMessage(  convertToString(e) );
            save(false);
        }
        return this;
    }

    private boolean inList(List<LocalServiceMetadataRecord> items_partially_loaded) {

        //quick and easy check - look at the uuids associated with the links
        List<String> uuids = items_partially_loaded.stream()
                .map(x->new ArrayList<OperatesOnLink>(x.getOperatesOnLinks()))
                .flatMap(List::stream)
                .map(x->x.getUuidref())
                .filter(x-> x != null)
                .collect(Collectors.toList());

        if (uuids.contains(this.localDatasetMetadataRecord.getFileIdentifier()))
            return true;

        // ok, not easy - need to actually get all the details
        //this can take some time...
        List<LocalServiceMetadataRecord> items_fully_loaded = items_partially_loaded.stream().map(x-> localServiceMetadataRecordRepo.fullId_operatesOn(x.getServiceMetadataDocumentId())).collect(Collectors.toList());

        List<String> fileIds = items_fully_loaded.stream()
                .map(x->new ArrayList<OperatesOnLink>(x.getOperatesOnLinks()))
                .flatMap(List::stream)
                .map(x->x.getDatasetMetadataRecord())
                .filter(x-> x != null)
                .map(x->x.getFileIdentifier())
                .collect(Collectors.toList());

        return (uuids.contains(this.localDatasetMetadataRecord.getFileIdentifier()));

    }

    private void process() {
        String linkCheckJobId = getInitiatingEvent().getLinkCheckJobId();
        String ds_fileId = localDatasetMetadataRecord.getFileIdentifier();

        List<LocalServiceMetadataRecord> items_partiallyLoaded=  localServiceMetadataRecordRepo.searchByLinkCheckJobIdAndOperatesOnFileID(linkCheckJobId, ds_fileId);

        List<LocalServiceMetadataRecord> items_view_partiallyLoaded = items_partiallyLoaded.stream()
                .filter(x->x.getMetadataServiceType().equalsIgnoreCase("view"))
                .collect(Collectors.toList());

        List<LocalServiceMetadataRecord> items_download_partiallyLoaded = items_partiallyLoaded.stream()
                .filter(x->x.getMetadataServiceType().equalsIgnoreCase("download"))
                .collect(Collectors.toList());

        boolean view_links = inList(items_view_partiallyLoaded);
        boolean download_links = inList(items_download_partiallyLoaded);

        this.localDatasetMetadataRecord.setINDICATOR_LAYER_MATCHES_DOWNLOAD(IndicatorStatus.FAIL);
        this.localDatasetMetadataRecord.setINDICATOR_LAYER_MATCHES_VIEW(IndicatorStatus.FAIL);
        this.localDatasetMetadataRecord.setINDICATOR_LAYER_MATCHES(IndicatorStatus.FAIL);

        if (download_links) {
            this.localDatasetMetadataRecord.setINDICATOR_LAYER_MATCHES_DOWNLOAD(IndicatorStatus.PASS);
            this.localDatasetMetadataRecord.setINDICATOR_LAYER_MATCHES(IndicatorStatus.PASS);
        }
        if (view_links) {
            this.localDatasetMetadataRecord.setINDICATOR_LAYER_MATCHES_VIEW(IndicatorStatus.PASS);
            this.localDatasetMetadataRecord.setINDICATOR_LAYER_MATCHES(IndicatorStatus.PASS);
        }

        int tt=90;
    }


    @Override
    public List<Event> newEventProcessing() {
        List<Event> result = new ArrayList<>();

        String linkCheckJobId = getInitiatingEvent().getLinkCheckJobId();
        if (metadataService.linkPostProcessingComplete(linkCheckJobId))
        {
            //done
            Event e = eventFactory.createAllPostProcessingCompleteEvent(linkCheckJobId);
            result.add(e);
        }
        return result;
    }
}


