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

package net.geocat.database.linkchecker.repos;

import net.geocat.database.linkchecker.entities.LocalDatasetMetadataRecord;
import net.geocat.database.linkchecker.entities.LocalServiceMetadataRecord;
import net.geocat.database.linkchecker.entities.helper.StatusQueryItem;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;


@Component
@Scope("prototype")
public interface LocalDatasetMetadataRecordRepo extends CrudRepository<LocalDatasetMetadataRecord, Long> {

//    @EntityGraph(attributePaths =
//            {
//                    "documentLinks"
//                    ,"documentLinks.capabilitiesDocument"
//                      //  , "documentLinks.capabilitiesDocument.capabilitiesDatasetMetadataLinkList"
//                        ,"documentLinks.capabilitiesDocument.remoteServiceMetadataRecordLink"
//
//            })
//    Optional<LocalDatasetMetadataRecord> findById(long id);

    LocalDatasetMetadataRecord findFirstByFileIdentifierAndLinkCheckJobId(String fileID,String linkCheckJobId);

    LocalDatasetMetadataRecord findFirstByFileIdentifier(String fileID);


    LocalDatasetMetadataRecord findFirstByLinkCheckJobIdAndSha2(String linkCheckJobId, String sha2);

    List<LocalDatasetMetadataRecord> findByLinkCheckJobId(String linkCheckJobId);

    long countByLinkCheckJobId(String LinkCheckJobId);

    @Query(value = "Select count(*) from datasetmetadatarecord   where linkcheckjobid = ?1 and dataset_record_type = 'LocalDatasetMetadataRecord' and state != 'CREATED'",
            nativeQuery = true
    )
    long countCompletedState(String LinkCheckJobId);

    @Query(value = "Select count(*) from datasetmetadatarecord   where linkcheckjobid = ?1 and dataset_record_type = 'LocalDatasetMetadataRecord' and state  in ?2",
            nativeQuery = true
    )
    long countInStates(String LinkCheckJobId, List<String> states);

    @Query(value = "select state as state,count(*) as numberOfRecords from datasetmetadatarecord where linkcheckjobid = ?1 and dataset_record_type = 'LocalDatasetMetadataRecord'   group by state",
            nativeQuery = true)
    List<StatusQueryItem> getStatus(String LinkCheckJobId);



}