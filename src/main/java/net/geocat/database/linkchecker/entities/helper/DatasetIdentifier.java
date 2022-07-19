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

package net.geocat.database.linkchecker.entities.helper;

 import net.geocat.database.linkchecker.entities.CapabilitiesDocument;
 import net.geocat.database.linkchecker.entities.helper.DatasetIdentifierNodeType;

import javax.persistence.Column;
 import javax.persistence.DiscriminatorColumn;
 import javax.persistence.DiscriminatorType;
 import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
 import javax.persistence.Index;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinColumns;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;

@Entity
@Table(
        indexes = {
                @Index(
                        name = "dsid_ds",
                        columnList = "datasetmetadatarecord_datasetmetadatadocumentid",
                        unique = false
                )
                ,
                @Index(
                        name = "dsid_cap",
                        columnList = "capdatasetmetadatalink_capabilitiesdatasetmetadatalinkid",
                        unique = false
                ),
                @Index(
                        name = "dsid_code_idx",
                        columnList = "code",
                        unique = false
                ),
                @Index(
                        name = "ops_on_idx",
                        columnList = "operatesOnLink_operatesOnLinkId",
                        unique = false
                )
        })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "datasetIdentifierParentType",
        discriminatorType = DiscriminatorType.STRING)
public class DatasetIdentifier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long datasetIdentifierId;

    @Enumerated(EnumType.STRING)
    private DatasetIdentifierNodeType identifierNodeType;

    //this should not be null
    @Column(columnDefinition = "text")
    private String code;

    // often this will be null
    @Column(columnDefinition = "text")
    private String codeSpace;




    //---
    public DatasetIdentifier() {}

    public DatasetIdentifier(DatasetIdentifierNodeType identifierNodeType, String code, String codeSpace ) {
        this.identifierNodeType = identifierNodeType;
        this.code = code;
        this.codeSpace = codeSpace;
     }

    //---



    public long getDatasetIdentifierId() {
        return datasetIdentifierId;
    }

    public void setDatasetIdentifierId(long datasetIdentifierId) {
        this.datasetIdentifierId = datasetIdentifierId;
    }

    public DatasetIdentifierNodeType getIdentifierNodeType() {
        return identifierNodeType;
    }

    public void setIdentifierNodeType(DatasetIdentifierNodeType identifierNodeType) {
        this.identifierNodeType = identifierNodeType;
    }


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


    //---


    @Override
    public String toString() {
        return " " +
                "(" + identifierNodeType +
                 ") code='" + code +
                "', codeSpace='" + codeSpace +
                "'";
    }
}
