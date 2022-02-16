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

package net.geocat.service;

import net.geocat.database.linkchecker.entities.LinkCheckJob;
import net.geocat.database.linkchecker.entities.helper.LogbackLoggingEvent;
import net.geocat.database.linkchecker.entities.helper.LogbackLoggingEventException;
import net.geocat.database.linkchecker.entities.helper.StatusQueryItem;
import net.geocat.database.linkchecker.repos.*;
import net.geocat.model.DocumentTypeStatus;
import net.geocat.model.LinkCheckStatus;
import net.geocat.model.StatusType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class GetStatusService {

    @Autowired
    LocalServiceMetadataRecordRepo localServiceMetadataRecordRepo;

    @Autowired
    LocalDatasetMetadataRecordRepo localDatasetMetadataRecordRepo;

    @Autowired
    LocalNotProcessedMetadataRecordRepo localNotProcessedMetadataRecordRepo;

    @Autowired
    LinkCheckJobRepo linkCheckJobRepo;

    @Autowired
    LogbackLoggingEventRepo logbackLoggingEventRepo;

    @Autowired
    LogbackLoggingEventExceptionRepo logbackLoggingEventExceptionRepo;

    public LinkCheckStatus getStatus(String processID, Boolean showErrors) {
        showErrors = showErrors == null? false : showErrors;
        LinkCheckJob job = linkCheckJobRepo.findById(processID).get();

        LinkCheckStatus result = new LinkCheckStatus(processID, job.getState());
        result.setServiceRecordStatus( computeServiceRecords(processID));
        result.setDatasetRecordStatus( computeDatasetRecords(processID));

        if (showErrors)
                setupErrorMessages(result);
        return result;
    }

    private void setupErrorMessages(LinkCheckStatus result) {
        List<LogbackLoggingEvent> exceptionLogMessages = logbackLoggingEventRepo.findExceptions(result.getProcessID());

        for(LogbackLoggingEvent exceptionLogMessage: exceptionLogMessages) {

            List<LogbackLoggingEventException> exceptionlines = logbackLoggingEventExceptionRepo.findByEventIdOrderByI(exceptionLogMessage.eventId);
            List<String> ex_messages = exceptionlines.stream()
                    .filter(x -> x.getI() == 0)
                    .sorted(Comparator.comparingInt(x -> x.getCausedByDepth()))
                    .map(x -> x.getTraceLine())
                    .collect(Collectors.toList());

            result.errorMessage.add(
                    String.join("\n  -> ", ex_messages)
            );

            Map<Short, List<LogbackLoggingEventException>> grouped = exceptionlines.stream()
                    .collect(Collectors.groupingBy(LogbackLoggingEventException::getCausedByDepth));

            List<Map.Entry<Short, List<LogbackLoggingEventException>>> exceptions = grouped.entrySet().stream()
                    .sorted(Comparator.comparingInt(x -> x.getKey()))
                    .collect(Collectors.toList());

            List<String> single_stacktrace = exceptions.stream()
                    .map(x -> makeString(x.getValue()))
                    .collect(Collectors.toList());

            result.stackTraces.add (single_stacktrace  );
        }
    }


    public String makeString(List<LogbackLoggingEventException> items) {
        List<String> strs = items.stream()
                .sorted(Comparator.comparingInt(x->x.getI()))
                .map(x->x.getTraceLine())
                .collect(Collectors.toList());
        return String.join("\n   ",strs);
    }

    public DocumentTypeStatus computeServiceRecords(String processID){
        long nrecords = localServiceMetadataRecordRepo.countByLinkCheckJobId(processID);
        List<StatusQueryItem> statusList = localServiceMetadataRecordRepo.getStatus(processID);
        List<StatusType> statusTypeList = new ArrayList<>();
        for(StatusQueryItem item: statusList){
            StatusType statusType = new StatusType(item.getState().toString(),item.getNumberOfRecords());
            statusTypeList.add(statusType);
        }

        DocumentTypeStatus result = new DocumentTypeStatus("ServiceRecord",nrecords,statusTypeList);
        return result;
    }

    public DocumentTypeStatus computeDatasetRecords(String processID){
        long nrecords = localDatasetMetadataRecordRepo.countByLinkCheckJobId(processID);
        List<StatusQueryItem> statusList = localDatasetMetadataRecordRepo.getStatus(processID);
        List<StatusType> statusTypeList = new ArrayList<>();
        for(StatusQueryItem item: statusList){
            StatusType statusType = new StatusType(item.getState().toString(),item.getNumberOfRecords());
            statusTypeList.add(statusType);
        }

        DocumentTypeStatus result = new DocumentTypeStatus("DatasetRecord",nrecords,statusTypeList);
        return result;
    }

}
