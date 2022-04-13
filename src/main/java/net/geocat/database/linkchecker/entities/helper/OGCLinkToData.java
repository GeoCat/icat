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
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
@DiscriminatorValue("OGCLinkToData")
public class OGCLinkToData extends LinkToData {

    @OneToOne(cascade = CascadeType.ALL,
    fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.JOIN)
    @BatchSize(size=500)
    @JoinColumn(name="ogcrequest_ogcrequestid")
    OGCRequest ogcRequest; // might be null

    @Column(columnDefinition = "text")
    private String ogcLayerName;  //for simple (getmap/getfeature) WFS/WMS/WMTS, this is the Layer/FeatureType name

    //--


    public OGCLinkToData() {
        super();
    }

    public OGCLinkToData(String linkcheckjobid, String sha2, String capabilitiesdocumenttype, DatasetMetadataRecord datasetMetadataRecord, String ogcLayerName) {
        super(linkcheckjobid, sha2, capabilitiesdocumenttype, datasetMetadataRecord);
        this.ogcLayerName = ogcLayerName;
    }

    public OGCLinkToData(String linkcheckjobid, String sha2, String capabilitiesdocumenttype, DatasetMetadataRecord datasetMetadataRecord ) {
        super(linkcheckjobid, sha2, capabilitiesdocumenttype, datasetMetadataRecord);
     }

    @Override
    public String key() {
        return super.key() +"::"+getOgcLayerName();
    }


    public OGCRequest getOgcRequest() {
        return ogcRequest;
    }


    public void setOgcRequest(OGCRequest ogcRequest) {
        this.ogcRequest = ogcRequest;
    }

    public String getOgcLayerName() {
        return ogcLayerName;
    }

    public void setOgcLayerName(String ogcLayerName) {
        this.ogcLayerName = ogcLayerName;
    }
}
