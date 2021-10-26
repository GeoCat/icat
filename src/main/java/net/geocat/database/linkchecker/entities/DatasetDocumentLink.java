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
import net.geocat.database.linkchecker.entities.helper.DocumentLink;
import net.geocat.database.linkchecker.entities.helper.PartialDownloadHint;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;


//links from a dataset metadata document
@Entity
@Table(
        indexes = {
                @Index(
                        name = "datasetmetadatarecord_datasetmetadatadocumentid_index",
                        columnList = "datasetMetadataRecord_datasetMetadataDocumentId",
                        unique = false
                )
        }
)
public class DatasetDocumentLink extends DocumentLink {

    //which dataset metadata document did this link come from?
    @ManyToOne(fetch = FetchType.EAGER,cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    DatasetMetadataRecord datasetMetadataRecord;

    //for display - info about this link
    @Column(columnDefinition = "text")
    String summary;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long datasetMetadataLinkId;

//    //if this link resolved to a capabilities document, which one
//    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JoinColumn(name = "capabilitiesDocumentId")
//    @Fetch(value = FetchMode.SELECT)
//    private CapabilitiesDocument capabilitiesDocument;



    //---------------------------------------------------------------------------

    public DatasetDocumentLink() {
        super();
        this.setPartialDownloadHint(PartialDownloadHint.CAPABILITIES_ONLY);
    }


    //---------------------------------------------------------------------------


    public DatasetMetadataRecord getDatasetMetadataRecord() {
        return datasetMetadataRecord;
    }

    public void setDatasetMetadataRecord(DatasetMetadataRecord datasetMetadataRecord) {
        this.datasetMetadataRecord = datasetMetadataRecord;
    }

    public long getDatasetMetadataLinkId() {
        return datasetMetadataLinkId;
    }

    public void setDatasetMetadataLinkId(long datasetMetadataLinkId) {
        this.datasetMetadataLinkId = datasetMetadataLinkId;
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
        String result = "DatasetDocumentLink {\n";
        result += "      datasetMetadataLinkId: " + datasetMetadataLinkId + "\n";


//        if ( (serviceMetadataRecord != null)   )
//            result += "      serviceMetadataRecord record identifier: "+ serviceMetadataRecord.getFileIdentifier()+"\n";
//        if ( (serviceMetadataRecord != null)   )
//            result += "      serviceMetadataRecord Id: "+ serviceMetadataRecord.getServiceMetadataDocumentId()+"\n";

        result += "\n";
        result += super.toString();
        result += "\n";
        result += "     +  Link is Capabilities Document: " + (getCapabilitiesDocument() != null) + "\n";
//        if (getCapabilitiesDocument() != null) {
//            result += getCapabilitiesDocument().toString(8);
//        }

        result += "\n";

        result += "  }";
        return result;
    }
}
