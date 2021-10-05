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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Scope("prototype")
public interface LocalServiceMetadataRecordRepo extends CrudRepository<LocalServiceMetadataRecord, Long> {

    LocalServiceMetadataRecord findFirstByLinkCheckJobIdAndSha2(String linkCheckJobId, String sha2);

    List<LocalServiceMetadataRecord> findByLinkCheckJobId(String linkCheckJobId);


    //given a link check job and a Dataset file id, this will find the service records that link to that dataset
    // method - find the service records that have an operatesOnLink that resolves to a Dataset record with the same fileid
    @Query(value = "SELECT s.* FROM servicemetadatarecord s WHERE s.serviceMetadataDocumentId IN (" +
            " select servicemetadatarecord_servicemetadatadocumentid \n" +
            " from datasetmetadatarecord \n" +
            "  LEFT JOIN operatesonlink ON (operatesonlink.datasetmetadatarecordid = datasetmetadatarecord.datasetmetadatadocumentid)\n" +
            " where fileidentifier = :operatesOnFileID   \n" +
            "     and dataset_record_type='RemoteDatasetMetadataRecord'\n" +
            "     and linkcheckjobid=:linkCheckJobId"
           + ")   ",nativeQuery = true)
    List<LocalServiceMetadataRecord> searchByLinkCheckJobIdAndOperatesOnFileID(String linkCheckJobId, String operatesOnFileID);


    long countByLinkCheckJobId(String LinkCheckJobId);

    @Query(value = "Select count(*) from servicemetadatarecord   where linkcheckjobid = ?1 and service_record_type = 'LocalServiceMetadataRecord' and state != 'CREATED'",
            nativeQuery = true
    )
    long countCompletedState(String LinkCheckJobId);


    @Query(value = "Select count(*) from servicemetadatarecord   where linkcheckjobid = ?1 and service_record_type = 'LocalServiceMetadataRecord' and state  in ?2",
            nativeQuery = true
    )
    long countInStates(String LinkCheckJobId, List<String> states);

    @Query(value = "select state as state,count(*) as numberOfRecords from servicemetadatarecord where linkcheckjobid = ?1 and service_record_type = 'LocalServiceMetadataRecord'   group by state",
            nativeQuery = true)
    List<StatusQueryItem> getStatus(String LinkCheckJobId);

    @Query(value="select a from LocalServiceMetadataRecord a " +
            "LEFT JOIN FETCH a.serviceDocumentLinks b " +
            "LEFT JOIN FETCH  b.capabilitiesDocument " +
            "LEFT JOIN FETCH a.operatesOnLinks c " +
            "LEFT JOIN FETCH c.datasetMetadataRecord "+
            "where a.serviceMetadataDocumentId= ?1")
    LocalServiceMetadataRecord  fullId(long id);

    @Query(value="select a from LocalServiceMetadataRecord a " +
            "LEFT JOIN FETCH a.serviceDocumentLinks b " +
            "LEFT JOIN FETCH a.operatesOnLinks c " +
            "LEFT JOIN FETCH c.datasetMetadataRecord "+
            "where a.serviceMetadataDocumentId= ?1")
    LocalServiceMetadataRecord  fullId_operatesOn(long id);
}