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

package net.geocat.eventprocessor.processors.datadownload;

import net.geocat.database.linkchecker.entities.LocalDatasetMetadataRecord;
import net.geocat.database.linkchecker.entities.helper.ServiceMetadataDocumentState;
import net.geocat.database.linkchecker.repos.LocalDatasetMetadataRecordRepo;
import net.geocat.eventprocessor.BaseEventProcessor;
import net.geocat.eventprocessor.processors.postprocess.EventProcessor_PostProcessDatasetDocumentEvent;
import net.geocat.eventprocessor.processors.processlinks.EventProcessor_ProcessServiceDocLinksEvent;
import net.geocat.events.Event;
import net.geocat.events.EventFactory;
import net.geocat.events.datadownload.DataDownloadDatasetDocumentEvent;
import net.geocat.events.postprocess.PostProcessDatasetDocumentEvent;
import net.geocat.service.helper.ShouldTransitionOutOfDataDownloading;
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
public class EventProcessor_DataDownloadDatasetDocumentEvent extends BaseEventProcessor<DataDownloadDatasetDocumentEvent> {

    Logger logger = LoggerFactory.getLogger(EventProcessor_DataDownloadDatasetDocumentEvent.class);

    @Autowired
    LocalDatasetMetadataRecordRepo localDatasetMetadataRecordRepo;

    @Autowired
    EventFactory eventFactory;

    @Autowired
    ShouldTransitionOutOfDataDownloading shouldTransitionOutOfDataDownloading;

    LocalDatasetMetadataRecord localDatasetMetadataRecord;

    @Override
    public EventProcessor_DataDownloadDatasetDocumentEvent externalProcessing() throws Exception {
        localDatasetMetadataRecord = localDatasetMetadataRecordRepo.findById(getInitiatingEvent().getDatasetDocumentId()).get();// make sure we re-load
        if (localDatasetMetadataRecord.getDataLinks().isEmpty()) {
            // no links to data -> nothing to download
            localDatasetMetadataRecord.setState(ServiceMetadataDocumentState.DATADOWNLOADED);
            save();
        }
        else
        {
            try{
                process();
                localDatasetMetadataRecord.setState(ServiceMetadataDocumentState.DATADOWNLOADED);
                save();
                logger.debug("finished DATADOWNLOADED dataset documentid="+getInitiatingEvent().getDatasetDocumentId()  );

            }
            catch(Exception e){
                logger.error("DATADOWNLOADED exception for datasetMetadataRecordId="+getInitiatingEvent().getDatasetDocumentId(),e);
                localDatasetMetadataRecord.setState(ServiceMetadataDocumentState.ERROR);
                localDatasetMetadataRecord.setErrorMessage(  convertToString(e) );
                save();
            }
        }

        return this;
    }

    private void process() {

     }


    public void save()
    {
        localDatasetMetadataRecord = localDatasetMetadataRecordRepo.save(localDatasetMetadataRecord);
    }

    @Override
    public EventProcessor_DataDownloadDatasetDocumentEvent internalProcessing() throws Exception {

        return this;
    }

    @Override
    public List<Event> newEventProcessing () {
        List<Event> result = new ArrayList<>();

        String linkCheckJobId = getInitiatingEvent().getLinkCheckJobId();

        if (shouldTransitionOutOfDataDownloading.shouldSendMessage(linkCheckJobId, getInitiatingEvent().getDatasetDocumentId())) {
            //done
            Event e = eventFactory.createAllDataDownloadedEvent(linkCheckJobId);
            result.add(e);
        }
        return result;
    }
}