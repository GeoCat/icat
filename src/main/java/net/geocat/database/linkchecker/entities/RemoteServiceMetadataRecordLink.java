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

import javax.persistence.*;

//represents the link in the xml Capabilities document that refers to the Service metadata document
@Entity
//@Table(
//        indexes = {
//                @Index(
//                        name = "ServiceMetadataRecord_sha2_linkcheckjobid",
//                        columnList = "sha2,linkCheckJobId",
//                        unique = false
//                )
//        }
//)
public class RemoteServiceMetadataRecordLink extends RetrievableSimpleLink {

//    //which capabilities document did this link come from?
//    @OneToOne(mappedBy = "remoteServiceMetadataRecordLink")
//    CapabilitiesDocument capabilitiesDocument;

    public String getFileIdentifier() {
        return fileIdentifier;
    }

    public void setFileIdentifier(String fileIdentifier) {
        this.fileIdentifier = fileIdentifier;
    }

    //fileIdentifier
    @Column(columnDefinition = "text")
    private String fileIdentifier;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long remoteServiceMetadataRecordLinkId;

    //summary info (for display/debugging)
    @Column(columnDefinition = "text")
    private String summary;

    public RemoteServiceMetadataRecordLink() {
        this.setPartialDownloadHint(PartialDownloadHint.METADATA_ONLY);
    }

    //---------------------------------------------------------------------------

    public long getRemoteServiceMetadataRecordLinkId() {
        return remoteServiceMetadataRecordLinkId;
    }

    public void setRemoteServiceMetadataRecordLinkId(long remoteServiceMetadataRecordId) {
        this.remoteServiceMetadataRecordLinkId = remoteServiceMetadataRecordId;
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
        String result = "RemoteServiceMetadataRecordLink {\n";
        result += "      remoteServiceMetadataRecordLinkId: " + remoteServiceMetadataRecordLinkId + "\n";

        result += "\n";
        result += "      file identifier: "+fileIdentifier;

        result += "\n";
        result += super.toString();
        result += "\n";


//        result += "      has remote Service Metadata Record :" + (remoteServiceMetadataRecord != null) + "\n";
//        if (remoteServiceMetadataRecord != null) {
//            result += "      Remote Service Metadata Record file identifier:" + remoteServiceMetadataRecord.getFileIdentifier() + "\n";
//        }
        result += "  }";
        return result;
    }
}
