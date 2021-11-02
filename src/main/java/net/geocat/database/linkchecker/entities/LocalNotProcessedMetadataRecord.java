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

import net.geocat.database.linkchecker.entities.helper.MetadataRecord;
import net.geocat.database.linkchecker.entities.helper.ServiceMetadataDocumentState;
import net.geocat.database.linkchecker.entities.helper.ServiceMetadataRecord;

import javax.persistence.*;


// represents a harvested XML metadata document that we don't process
@Entity
@DiscriminatorValue("NoProcessedMetadataRecord")
@Table(
        indexes = {
                @Index(
                        name = "LocalNotProcessedMetadataRecord_linkcheckjobid_idx",
                        columnList = "linkCheckJobId",
                        unique = false
                ),
                @Index(
                        name = "LocalNotProcessedMetadataRecord_sha2_linkcheckjobid",
                        columnList = "sha2,linkCheckJobId",
                        unique = false
                )
        }
)
public class LocalNotProcessedMetadataRecord extends MetadataRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long localNotProcessedMetadataRecordId;

    //processing state
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20)")
    ServiceMetadataDocumentState state;

    //which job this is apart of
    @Column(columnDefinition = "varchar(40)")
    private String linkCheckJobId;

    // from the harvester - what is the harvester's record ID for this document?
    private long harvesterMetadataRecordId;

    // for display - info about this object
    @Column(columnDefinition = "text")
    private String summary;


    public long getLocalNotProcessedMetadataRecordId() {
        return localNotProcessedMetadataRecordId;
    }

    public void setLocalNotProcessedMetadataRecordId(long localNotProcessedMetadataRecordId) {
        this.localNotProcessedMetadataRecordId = localNotProcessedMetadataRecordId;
    }

    public String getLinkCheckJobId() {
        return linkCheckJobId;
    }

    public void setLinkCheckJobId(String linkCheckJobId) {
        this.linkCheckJobId = linkCheckJobId;
    }


    public long getHarvesterMetadataRecordId() {
        return harvesterMetadataRecordId;
    }

    public void setHarvesterMetadataRecordId(long harvesterMetadataRecordId) {
        this.harvesterMetadataRecordId = harvesterMetadataRecordId;
    }


    public ServiceMetadataDocumentState getState() {
        return state;
    }

    public void setState(ServiceMetadataDocumentState state) {
        this.state = state;
    }


    //---------------------------------------------------------------------------

    @PreUpdate
    protected void onUpdate() {
        summary = toString();

    }

    @PrePersist
    protected void onInsert() {
        summary = toString();

    }

    //---------------------------------------------------------------------------

    @Override
    public String toString() {
        String result = "LocalNotProcessedMetadataRecord {\n";
        result += "      NOT PROCESSED Metadata Document Id: " + localNotProcessedMetadataRecordId+ "\n";
        result += "     linkCheckJobId: " + linkCheckJobId + "\n";
        result += "     harvesterMetadataRecordId: " + harvesterMetadataRecordId + "\n";
        result += "     state: " + state + "\n";


        result += super.toString();

        result += "\n";

        result += " }";
        return result;
    }
}