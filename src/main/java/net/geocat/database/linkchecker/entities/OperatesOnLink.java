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
import net.geocat.database.linkchecker.entities.helper.ServiceMetadataRecord;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;


//represents an OperatesOn Link inside a Service Metadata Document
@Entity
@Table(
        indexes = {
                @Index(
                        name = "RSL_serviceMetadataRecord_serviceMetadataDocumentId",
                        columnList = "serviceMetadataRecord_serviceMetadataDocumentId",
                        unique = false
                ),
                @Index(
                        name = "Opon_linkcheckjob_fileid",
                        columnList = "linkCheckJobId,fileIdentifier",
                        unique = false
                )
        }
)
public class OperatesOnLink extends RetrievableSimpleLink {

    // from the Service document Operates on section
    @Column(columnDefinition = "text")
    String uuidref;

    @Column(columnDefinition = "text")
    String fileIdentifier;

    @Column(columnDefinition = "text")
    String datasetIdentifier;


    // summary of this object (for display)
    @Column(columnDefinition = "text")
    String summary;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long operatesOnLinkId;

    //which service metadata record does this link belong to?
    @ManyToOne(fetch = FetchType.EAGER)
    private ServiceMetadataRecord serviceMetadataRecord;

//    //if this link resolves to a dataset record, this represents that dataset document
//    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JoinColumn(name = "datasetMetadataRecordId")
//    @Fetch(value = FetchMode.SELECT)
//    private OperatesOnRemoteDatasetMetadataRecord datasetMetadataRecord;


    public OperatesOnLink() {
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

    public ServiceMetadataRecord getServiceMetadataRecord() {
        return serviceMetadataRecord;
    }

    public void setServiceMetadataRecord(ServiceMetadataRecord serviceMetadataRecord) {
        this.serviceMetadataRecord = serviceMetadataRecord;
    }

    public long getOperatesOnLinkId() {
        return operatesOnLinkId;
    }

    public void setOperatesOnLinkId(long operatesOnLinkId) {
        this.operatesOnLinkId = operatesOnLinkId;
    }

    public String getUuidref() {
        return uuidref;
    }

    public void setUuidref(String uuidref) {
        this.uuidref = uuidref;
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
        String result = "OperatesOnLink {\n";
        result += "      operatesOnLinkId: " + operatesOnLinkId + "\n";
        if ((uuidref != null) && (!uuidref.isEmpty()))
            result += "      uuidref: " + uuidref + "\n";

        result += "      file Identifier: " + fileIdentifier + "\n";
        result += "      dataset identifier: " + datasetIdentifier + "\n";

        result += super.toString();
       // result += "      has dataset Metadata Record :" + (datasetMetadataRecord != null) + "\n";
//        if (datasetMetadataRecord != null) {
//            result += "      dataset Metadata Record file identifier: " + datasetMetadataRecord.getFileIdentifier() + "\n";
//            result += "      dataset Metadata Record dataset identifier: " + datasetMetadataRecord.getDatasetIdentifier() + "\n";
//        }
//        if ( (serviceMetadataRecord != null)   )
//            result += "      serviceMetadataRecord record identifier: "+ serviceMetadataRecord.getFileIdentifier()+"\n";
//        if ( (localServiceMetadataRecord != null)   )
//            result += "      serviceMetadataRecord Id: "+ sServiceMetadataRecord.getServiceMetadataDocumentId()+"\n";

        result += "  }";
        return result;
    }

}
