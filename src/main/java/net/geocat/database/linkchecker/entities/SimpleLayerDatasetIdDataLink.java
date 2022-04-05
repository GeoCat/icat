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

package net.geocat.database.linkchecker.entities;


import net.geocat.database.linkchecker.entities.helper.DatasetMetadataRecord;
import net.geocat.database.linkchecker.entities.helper.LinkToData;
import net.geocat.database.linkchecker.entities.helper.OGCLinkToData;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * This represents WMS, WMTS, and Atom "layers" that have an attached DatasetID
 *  WMS/WMTS: Identity and AuthorityURL
 *  Atom: InspireSpatialDatasetCode and Codespace
 *
 *  WMS/WMTS:
 *
 *   <AuthorityURL name="ABC">
 *      <OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:href="URL" xlink:type="simple"/>
 *   </AuthorityURL>
 *    <Identifier authority="ABC">DatasetCODE</Identifier>
 *
 *    Atom:
 *      <inspire_dls:spatial_dataset_identifier_code>spatial_dataset_identifier_code1</inspire_dls:spatial_dataset_identifier_code>
 *      <inspire_dls:spatial_dataset_identifier_namespace>spatial_dataset_identifier_namespace1</inspire_dls:spatial_dataset_identifier_namespace>
 *
 */
@Entity
@DiscriminatorValue("SimpleLayerDatasetIdDataLink")
public class SimpleLayerDatasetIdDataLink extends OGCLinkToData {


    @Column(columnDefinition = "text")
    private String code;

    @Column(columnDefinition = "text")
    private String codeSpace;


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCodeSpace() {
        return codeSpace;
    }

    public void setCodeSpace(String codeSpace) {
        this.codeSpace = codeSpace;
    }

    public SimpleLayerDatasetIdDataLink() {
        super();
    }

    public SimpleLayerDatasetIdDataLink(String linkcheckjobid, String sha2, String capabilitiesdocumenttype,
                                        String ogcLayerName, String code, String codeSpace, DatasetMetadataRecord datasetMetadataRecord) {
        super(linkcheckjobid,sha2,capabilitiesdocumenttype,datasetMetadataRecord,ogcLayerName);
        this.code = code;
        this.codeSpace = codeSpace;
    }

    @Override
    public String key() {
        return super.key() +"::"+getOgcLayerName();
    }

}
