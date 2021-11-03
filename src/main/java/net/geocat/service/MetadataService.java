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
import net.geocat.database.linkchecker.entities.helper.ServiceMetadataDocumentState;
import net.geocat.database.linkchecker.repos.LinkCheckJobRepo;
import net.geocat.database.linkchecker.repos.LocalDatasetMetadataRecordRepo;
import net.geocat.database.linkchecker.repos.LocalNotProcessedMetadataRecordRepo;
import net.geocat.database.linkchecker.repos.LocalServiceMetadataRecordRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Scope("prototype")
public class MetadataService {

    @Autowired
    LocalServiceMetadataRecordRepo localServiceMetadataRecordRepo;

    @Autowired
    LocalDatasetMetadataRecordRepo localDatasetMetadataRecordRepo;

    @Autowired
    LocalNotProcessedMetadataRecordRepo localNotProcessedMetadataRecordRepo;

    @Autowired
    LinkCheckJobRepo linkCheckJobRepo;

    public boolean linkProcessingComplete(String linkCheckJobId) {
        LinkCheckJob job  = linkCheckJobRepo.findById(linkCheckJobId).get();
//        long nService = localServiceMetadataRecordRepo.countByLinkCheckJobId(linkCheckJobId);
//        long nDataset = localDatasetMetadataRecordRepo.countByLinkCheckJobId(linkCheckJobId);

        Long nService =  job.getNumberOfLocalServiceRecords();
        Long nDataset = job.getNumberOfLocalDatasetRecords();
        Long nOther = job.getNumberOfNotProcessedDatasetRecords();


        long nDataset_complete = localDatasetMetadataRecordRepo.countInStates(linkCheckJobId,
                Arrays.asList(new String[] {
                        ServiceMetadataDocumentState.ERROR.toString(),
                        ServiceMetadataDocumentState.LINKS_PROCESSED.toString(),
                        ServiceMetadataDocumentState.NOT_APPLICABLE.toString(),
                })  ) ;

        if (nDataset_complete < nDataset.longValue())
            return false; //short cut - still more to do

        long nService_complete = localServiceMetadataRecordRepo.countInStates(linkCheckJobId,
                Arrays.asList(new String[] {
                        ServiceMetadataDocumentState.ERROR.toString(),
                        ServiceMetadataDocumentState.LINKS_PROCESSED.toString(),
                        ServiceMetadataDocumentState.NOT_APPLICABLE.toString(),
                })  ) ;

        if (nService_complete < nService.longValue())
            return false; //short cut - still more to do


        long nOther_complete = localNotProcessedMetadataRecordRepo.countInStates(linkCheckJobId,
                Arrays.asList(new String[] {
                        ServiceMetadataDocumentState.ERROR.toString(),
                        ServiceMetadataDocumentState.LINKS_PROCESSED.toString(),
                        ServiceMetadataDocumentState.NOT_APPLICABLE.toString(),
                })  ) ;

        if (nOther_complete < nOther.longValue())
            return false; //short cut - still more to do

//        long total_records = nDataset+ nService;
//        long total_complete = nDataset_complete+nService_complete;
//
//        return total_records == total_complete;

        return true;
    }

    public long numberRemaininglinkProcessing(String linkCheckJobId) {
        LinkCheckJob job  = linkCheckJobRepo.findById(linkCheckJobId).get();


        Long nService =  job.getNumberOfLocalServiceRecords();
        Long nDataset = job.getNumberOfLocalDatasetRecords();
        Long nOther = job.getNumberOfNotProcessedDatasetRecords();


        long nDataset_complete = localDatasetMetadataRecordRepo.countInStates(linkCheckJobId,
                Arrays.asList(new String[] {
                        ServiceMetadataDocumentState.ERROR.toString(),
                        ServiceMetadataDocumentState.LINKS_PROCESSED.toString(),
                        ServiceMetadataDocumentState.NOT_APPLICABLE.toString(),
                })  ) ;



        long nService_complete = localServiceMetadataRecordRepo.countInStates(linkCheckJobId,
                Arrays.asList(new String[] {
                        ServiceMetadataDocumentState.ERROR.toString(),
                        ServiceMetadataDocumentState.LINKS_PROCESSED.toString(),
                        ServiceMetadataDocumentState.NOT_APPLICABLE.toString(),
                })  ) ;




        long nOther_complete = localNotProcessedMetadataRecordRepo.countInStates(linkCheckJobId,
                Arrays.asList(new String[] {
                        ServiceMetadataDocumentState.ERROR.toString(),
                        ServiceMetadataDocumentState.LINKS_PROCESSED.toString(),
                        ServiceMetadataDocumentState.NOT_APPLICABLE.toString(),
                })  ) ;



        long total_records = nDataset+ nService +nOther;
        long total_complete = nDataset_complete+nService_complete +nOther_complete;
        return total_records - total_complete;

     }


    public boolean linkPostProcessingComplete(String linkCheckJobId) {
        long nService = localServiceMetadataRecordRepo.countByLinkCheckJobId(linkCheckJobId);
        long nDataset = localDatasetMetadataRecordRepo.countByLinkCheckJobId(linkCheckJobId);

        long nDataset_complete = localDatasetMetadataRecordRepo.countInStates(linkCheckJobId,
                Arrays.asList(new String[] {
                        ServiceMetadataDocumentState.ERROR.toString(),
                        ServiceMetadataDocumentState.LINKS_POSTPROCESSED.toString(),
                        ServiceMetadataDocumentState.NOT_APPLICABLE.toString(),
                })  ) ;

        long nService_complete = localServiceMetadataRecordRepo.countInStates(linkCheckJobId,
                Arrays.asList(new String[] {
                        ServiceMetadataDocumentState.ERROR.toString(),
                        ServiceMetadataDocumentState.LINKS_POSTPROCESSED.toString(),
                        ServiceMetadataDocumentState.NOT_APPLICABLE.toString(),
                })  ) ;

        long total_records = nDataset+ nService;
        long total_complete = nDataset_complete+nService_complete;

        return total_records == total_complete;
    }

    public long numberRemainingLinkPostProcessing(String linkCheckJobId) {
        long nService = localServiceMetadataRecordRepo.countByLinkCheckJobId(linkCheckJobId);
        long nDataset = localDatasetMetadataRecordRepo.countByLinkCheckJobId(linkCheckJobId);

        long nDataset_complete = localDatasetMetadataRecordRepo.countInStates(linkCheckJobId,
                Arrays.asList(new String[] {
                        ServiceMetadataDocumentState.ERROR.toString(),
                        ServiceMetadataDocumentState.LINKS_POSTPROCESSED.toString(),
                        ServiceMetadataDocumentState.NOT_APPLICABLE.toString(),
                })  ) ;

        long nService_complete = localServiceMetadataRecordRepo.countInStates(linkCheckJobId,
                Arrays.asList(new String[] {
                        ServiceMetadataDocumentState.ERROR.toString(),
                        ServiceMetadataDocumentState.LINKS_POSTPROCESSED.toString(),
                        ServiceMetadataDocumentState.NOT_APPLICABLE.toString(),
                })  ) ;

        long total_records = nDataset+ nService;
        long total_complete = nDataset_complete+nService_complete;

        return total_records - total_complete;
    }


}
