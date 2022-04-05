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

import net.geocat.database.linkchecker.entities.InspireSpatialDatasetIdentifier;
import net.geocat.database.linkchecker.entities.helper.StoreQueryCapabilitiesLinkResult;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
public interface InspireSpatialDatasetIdentifierRepo  extends CrudRepository<InspireSpatialDatasetIdentifier, String> {

    public List<InspireSpatialDatasetIdentifier> findByCode(String code);

    @Query(value = "SELECT cap_sha2 as sha2," +
            "cap_jobid as linkcheckjobid, " +
            "capabilitiesdocument.capabilitiesdocumenttype, " +
            "procGetSpatialDataSetName, ?2 as code, ?3 as codespace " +
            "FROM inspirespatialdatasetidentifier " +
            " JOIN capabilitiesdocument ON (capabilitiesdocument.sha2=inspirespatialdatasetidentifier.cap_sha2 and capabilitiesdocument.linkcheckjobid = inspirespatialdatasetidentifier.cap_jobid) " +
            "WHERE cap_jobid = ?1 AND code =?2 AND namespace = ?3",
            nativeQuery = true
    )
    List<StoreQueryCapabilitiesLinkResult> linkToCapabilitiesViaInspire_codeAndCodespace(String linkCheckJob, String inspireCode, String inspireCodeSet);


    @Query(value = "SELECT cap_sha2 as sha2," +
            "cap_jobid as linkcheckjobid, " +
            "capabilitiesdocument.capabilitiesdocumenttype," +
            "procGetSpatialDataSetName, ?2 as code, null as codespace  " +
            "FROM inspirespatialdatasetidentifier " +
            " JOIN capabilitiesdocument ON (capabilitiesdocument.sha2=inspirespatialdatasetidentifier.cap_sha2 and capabilitiesdocument.linkcheckjobid = inspirespatialdatasetidentifier.cap_jobid) " +
            "WHERE cap_jobid = ?1 AND code =?2 AND namespace is NULL",
            nativeQuery = true
    )
    List<StoreQueryCapabilitiesLinkResult> linkToCapabilitiesViaInspire_codeOnly(String linkCheckJob, String inspireCode);

}
