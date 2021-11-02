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

import net.geocat.database.linkchecker.entities.helper.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;

//represents a link from a service metadata xml document
@Entity
@Table(
        indexes = {
                @Index(
                        name = "servicemetadatarecord_servicemetadatadocumentid_index",
                        columnList = "serviceMetadataRecord_serviceMetadataDocumentId",
                        unique = false
                )
        }
)
public class ServiceDocumentLink extends DocumentLink {

    //which service document did this link come from?
    @ManyToOne(fetch = FetchType.EAGER,cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    ServiceMetadataRecord serviceMetadataRecord;

    //summary info (for display/debugging)
    @Column(columnDefinition = "text")
    String summary;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long serviceMetadataLinkId;


    //---------------------------------------------------------------------------

    public ServiceDocumentLink() {
        super();
        this.setPartialDownloadHint(PartialDownloadHint.CAPABILITIES_ONLY);
    }


    //---------------------------------------------------------------------------



    public ServiceMetadataRecord getServiceMetadataRecord() {
        return serviceMetadataRecord;
    }

    public long getServiceMetadataLinkId() {
        return serviceMetadataLinkId;
    }

    public void setServiceMetadataLinkId(long serviceMetadataLinkId) {
        this.serviceMetadataLinkId = serviceMetadataLinkId;
    }


    public ServiceMetadataRecord getLocalServiceMetadataRecord() {
        return serviceMetadataRecord;
    }

    public void setServiceMetadataRecord(ServiceMetadataRecord localServiceMetadataRecord) {
        this.serviceMetadataRecord = localServiceMetadataRecord;
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
        String result = "ServiceDocumentLink {\n";
        result += "      serviceMetadataLinkId: " + serviceMetadataLinkId + "\n";
        result += "      serviceMetadataRecord Id: "+ serviceMetadataRecord.getServiceMetadataDocumentId()+"\n";

        result += "\n";
        result += super.toString();
        result += "\n";

        result += "  }";
        return result;
    }
}
