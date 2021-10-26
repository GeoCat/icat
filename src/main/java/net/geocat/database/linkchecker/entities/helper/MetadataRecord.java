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

import net.geocat.xml.MetadataDocumentType;
import net.geocat.xml.helpers.CapabilitiesType;

import javax.persistence.*;

//Base class for information in a metadata record (i.e. <MD_Metadata>....)
@MappedSuperclass
public class MetadataRecord extends UpdateCreateDateTimeEntity {

    //not saved to DB - use the SHA2 link to get the data from blobstorage
    @Transient
    public String actualXML;


    @Column(columnDefinition = "text")
    private String title;

    //SHA2 of the XMNL Text
    @Column(columnDefinition = "varchar(64)")
    private String sha2;

    //File identifier (see XML XSL)
    @Column(columnDefinition = "text")
    private String fileIdentifier;

    // what type of metadata document is this (service/dataset/series/...)
    @Column(columnDefinition = "varchar(22)")
    @Enumerated(EnumType.STRING)
    private MetadataDocumentType metadataRecordType;

    // summary of this document's processing in a somewhat human-readable format
    @Column(columnDefinition = "text")
    private String humanReadable;

    //Number of documents links that resolve to a capabilities document.
    // null = not evaluated
    Integer INDICATOR_RESOLVES_TO_CAPABILITIES;

    //If >1 capabilities, then the most common type (WMS/WFS/WMTS/ATOM).  If there is an equal number, then choose any one.
    // null = not evaluated
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(5)")
    CapabilitiesType INDICATOR_CAPABILITIES_TYPE;


    public MetadataRecord()
    {
        super();
    }

    //---------------------------------------------------------------------------


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getINDICATOR_RESOLVES_TO_CAPABILITIES() {
        return INDICATOR_RESOLVES_TO_CAPABILITIES;
    }

    public void setINDICATOR_RESOLVES_TO_CAPABILITIES(Integer INDICATOR_RESOLVES_TO_CAPABILITIES) {
        this.INDICATOR_RESOLVES_TO_CAPABILITIES = INDICATOR_RESOLVES_TO_CAPABILITIES;
    }

    public CapabilitiesType getINDICATOR_CAPABILITIES_TYPE() {
        return INDICATOR_CAPABILITIES_TYPE;
    }

    public void setINDICATOR_CAPABILITIES_TYPE(CapabilitiesType INDICATOR_CAPABILITIES_TYPE) {
        this.INDICATOR_CAPABILITIES_TYPE = INDICATOR_CAPABILITIES_TYPE;
    }

    public String getHumanReadable() {
        return humanReadable;
    }

    public void setHumanReadable(String humanReadable) {
        this.humanReadable = humanReadable;
    }

    public String getSha2() {
        return sha2;
    }

    public void setSha2(String sha2) {
        this.sha2 = sha2;
    }

    public String getFileIdentifier() {
        return fileIdentifier;
    }

    public void setFileIdentifier(String recordIdentifier) {
        this.fileIdentifier = recordIdentifier;
    }

    public MetadataDocumentType getMetadataRecordType() {
        return metadataRecordType;
    }

    public void setMetadataRecordType(MetadataDocumentType metadataRecordType) {
        this.metadataRecordType = metadataRecordType;
    }

     protected void onUpdate() {
        super.onUpdate();
     }

     protected void onInsert() {
        super.onInsert();
     }

    //---------------------------------------------------------------------------

    @Override
    public String toString() {
        String result = super.toString();
        result += "     sha2: " + sha2 + "\n";
        result += "     fileIdentifier: " + fileIdentifier + "\n";
        result += "     metadataRecordType: " + metadataRecordType + "\n";
        return result;
    }
}
