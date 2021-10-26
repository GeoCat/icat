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

package net.geocat.database.harvester.entities;


import javax.persistence.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

@Entity
@Table(name = "metadata_record"
//        ,indexes = {
//                @Index(
//                        name = "metadata_record_endpointJobId_idx",
//                        columnList = "endpointJobId",
//                        unique = false
//                ),
//                @Index(
//                        name = "metadata_record_endpointJobId_recordnumb_idx",
//                        columnList = "endpointJobId,recordNumber",
//                        unique = false
        //               )
        //       }
)
public class MetadataRecord {
    @Column(columnDefinition = "timestamp with time zone", name = "create_time_utc")
    ZonedDateTime createTimeUTC;
    @Id
    @Column(name = "metadata_record_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long metadataRecordId;
    @Column(name = "endpoint_job_id")
    private long endpointJobId;
    @Column(name = "record_number")
    private int recordNumber;
    @Column(columnDefinition = "varchar(64)")
    private String sha2;
    @Column(columnDefinition = "text", name = "record_identifier")
    private String recordIdentifier;

    @PrePersist
    private void onInsert() {
        this.createTimeUTC = ZonedDateTime.now(ZoneId.of("UTC"));
    }


    public ZonedDateTime getCreateTimeUTC() {
        return createTimeUTC;
    }


    public String getRecordIdentifier() {
        return recordIdentifier;
    }

    public void setRecordIdentifier(String recordIdentifier) {
        this.recordIdentifier = recordIdentifier;
    }


    public long getMetadataRecordId() {
        return metadataRecordId;
    }

    public void setMetadataRecordId(long metadataRecordId) {
        this.metadataRecordId = metadataRecordId;
    }

    public long getEndpointJobId() {
        return endpointJobId;
    }

    public void setEndpointJobId(long endpointJobId) {
        this.endpointJobId = endpointJobId;
    }

    public int getRecordNumber() {
        return recordNumber;
    }

    public void setRecordNumber(int recordNumber) {
        this.recordNumber = recordNumber;
    }

    public String getSha2() {
        return sha2;
    }

    public void setSha2(String sha2) {
        this.sha2 = sha2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetadataRecord that = (MetadataRecord) o;
        return Objects.equals(recordIdentifier, that.recordIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recordIdentifier);
    }
}
