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
import net.geocat.database.linkchecker.entities.helper.ServiceMetadataDocumentState;
import net.geocat.database.linkchecker.entities.helper.StatusQueryItem;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Component
@Scope("prototype")
public interface LocalServiceMetadataRecordRepo extends CrudRepository<LocalServiceMetadataRecord, Long> {

    @Transactional
    @Modifying
    @Query(value="UPDATE LocalServiceMetadataRecord lsmr SET lsmr.state = :newState WHERE lsmr.serviceMetadataDocumentId = :id ")
    void updateState(long id, ServiceMetadataDocumentState newState);

    @Transactional
    @Modifying
    @Query(value="UPDATE LocalServiceMetadataRecord lsmr SET lsmr.state = :newState WHERE lsmr.serviceMetadataDocumentId = :id and (lsmr.state <> 'NOT_APPLICABLE')")
    void updateStateNotApplicable(long id, ServiceMetadataDocumentState newState);


    LocalServiceMetadataRecord findFirstByLinkCheckJobIdAndSha2(String linkCheckJobId, String sha2);

    List<LocalServiceMetadataRecord> findByLinkCheckJobId(String linkCheckJobId);

    LocalServiceMetadataRecord findFirstByFileIdentifierAndLinkCheckJobId(String fileID,String linkCheckJobId);

    LocalServiceMetadataRecord findFirstByFileIdentifier(String fileID);


    @Query(value = "SELECT servicemetadatadocumentid FROM servicemetadatarecord   WHERE linkcheckjobid = ?1",
            nativeQuery = true
    )
    List<Long> searchAllServiceIds(String linkCheckJobId);


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

}