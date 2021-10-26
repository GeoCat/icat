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

import net.geocat.database.linkchecker.entities.helper.PartialDownloadHint;
import net.geocat.database.linkchecker.entities.helper.RetrievableSimpleLink;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;

// This models all the Dataset links from a capabilities (typically 1 per layer)
@Entity
public class CapabilitiesDatasetMetadataLink extends RetrievableSimpleLink {

//    // link to the actual Dataset document (if it resolves to one)
//    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
//    @JoinColumn(name = "datasetMetadataRecordId")
//    @Fetch(value = FetchMode.JOIN)
//    CapabilitiesRemoteDatasetMetadataDocument capabilitiesRemoteDatasetMetadataDocument;

     @Column(columnDefinition = "text")
    String fileIdentifier;

     @Column(columnDefinition = "text")
    String datasetIdentifier;

//    //link back to the capabilities document this link came from
//    @ManyToOne(fetch = FetchType.EAGER,cascade = {CascadeType.ALL})
//    CapabilitiesDocument capabilitiesDocument;

    //from the Capabilities document - identity for the layer
    @Column(columnDefinition = "text")
    String identity;

    //store summary info about this
    @Column(columnDefinition = "text")
    String summary;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long capabilitiesDatasetMetadataLinkId;


    public CapabilitiesDatasetMetadataLink() {
        this.setPartialDownloadHint(PartialDownloadHint.METADATA_ONLY);
    }

    //---------------------------------------------------------------------------


    public String getFileIdentifier() {
        return fileIdentifier;
    }

    public void setFileIdentifier(String fileIdentifier) {
        this.fileIdentifier = fileIdentifier;
    }

    public String getDatasetIdentifier() {
        return datasetIdentifier;
    }

    public void setDatasetIdentifier(String datasetIdentifier) {
        this.datasetIdentifier = datasetIdentifier;
    }

    public long getCapabilitiesDatasetMetadataLinkId() {
        return capabilitiesDatasetMetadataLinkId;
    }

    public void setCapabilitiesDatasetMetadataLinkId(long capabilitiesDatasetMetadataLinkId) {
        this.capabilitiesDatasetMetadataLinkId = capabilitiesDatasetMetadataLinkId;
    }


    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    //---------------------------------------------------------------------------
    @PreUpdate
    protected void onUpdate() {
        super.onUpdate();
        this.summary = this.toString();
    }

    @PrePersist
    protected void onInsert() {
        super.onInsert();
        this.summary = this.toString();
    }

    //---------------------------------------------------------------------------
    @Override
    public String toString() {
        String result = "CapabilitiesDatasetMetadataLink {\n";
        result += "      capabilitiesDatasetMetadataLinkId: " + capabilitiesDatasetMetadataLinkId + "\n";
        result += "      identity: " + identity + "\n";
        result += "      file Identifier: " + fileIdentifier + "\n";
        result += "      dataset identifier: " + datasetIdentifier + "\n";

        result += "\n";
        result += super.toString();
        result += "\n";


        result += "  }";
        return result;
    }
}
