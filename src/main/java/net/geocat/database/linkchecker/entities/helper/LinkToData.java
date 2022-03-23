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

import net.geocat.database.linkchecker.entities.OGCRequest;
import net.geocat.xml.helpers.CapabilitiesType;

import javax.persistence.CascadeType;
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
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;


@Entity(name="linktodata")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "linkType",
        discriminatorType = DiscriminatorType.STRING)
public class LinkToData {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long linkToDataId;

    @OneToOne(cascade = CascadeType.ALL)
    OGCRequest ogcRequest; // might be null

    //----
    //which dataset metadata document did this link come from?
    @ManyToOne(fetch = FetchType.EAGER,cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    DatasetMetadataRecord datasetMetadataRecord;


    //link to the underlying Capabilities -----------
    //sha2 of the XML's text
    @Column(columnDefinition = "varchar(64)")
    private String capabilitiesSha2;

    //what link check job this is apart of
    @Column(columnDefinition = "varchar(40)")
    private String linkCheckJobId;

    //Type of this Capabilities Document (i.e. WFS/WMS/...)
    @Enumerated(EnumType.STRING)
    private CapabilitiesType capabilitiesDocumentType;

    public LinkToData() {
    }

    public LinkToData(String linkcheckjobid, String sha2, String capabilitiesdocumenttype,DatasetMetadataRecord datasetMetadataRecord) {
        this.linkCheckJobId = linkcheckjobid;
        this.capabilitiesSha2 = sha2;
        this.datasetMetadataRecord = datasetMetadataRecord;
        if  ( (capabilitiesdocumenttype !=null) && (!capabilitiesdocumenttype.isEmpty()))
            this.capabilitiesDocumentType = CapabilitiesType.valueOf(capabilitiesdocumenttype);
    }


    //------------------------------------------------


//    @Column(columnDefinition = "text")
//    private String storedQueryCode;
//    @Column(columnDefinition = "text")
//    private String storedQueryCodeSpace;

    //------

    public String getCapabilitiesSha2() {
        return capabilitiesSha2;
    }

    public void setCapabilitiesSha2(String capabilitiesSha2) {
        this.capabilitiesSha2 = capabilitiesSha2;
    }

    public String getLinkCheckJobId() {
        return linkCheckJobId;
    }

    public void setLinkCheckJobId(String linkCheckJobId) {
        this.linkCheckJobId = linkCheckJobId;
    }

    public CapabilitiesType getCapabilitiesDocumentType() {
        return capabilitiesDocumentType;
    }

    public void setCapabilitiesDocumentType(CapabilitiesType capabilitiesDocumentType) {
        this.capabilitiesDocumentType = capabilitiesDocumentType;
    }

    public long getLinkToDataId() {
        return linkToDataId;
    }

    public void setLinkToDataId(long linkToDataId) {
        this.linkToDataId = linkToDataId;
    }

    public DatasetMetadataRecord getDatasetMetadataRecord() {
        return datasetMetadataRecord;
    }

    public void setDatasetMetadataRecord(DatasetMetadataRecord datasetMetadataRecord) {
        this.datasetMetadataRecord = datasetMetadataRecord;
    }

    public OGCRequest getOgcRequest() {
        return ogcRequest;
    }

    public void setOgcRequest(OGCRequest ogcRequest) {
        this.ogcRequest = ogcRequest;
    }

    //------

    @Override
    public String toString() {
        return  "     capabilitiesSha2: " + capabilitiesSha2 + "\n" +
                "     linkCheckJobId: " + linkCheckJobId + "\n" +
                "     capabilitiesDocumentType: " + capabilitiesDocumentType+ "\n" ;
    }
}
