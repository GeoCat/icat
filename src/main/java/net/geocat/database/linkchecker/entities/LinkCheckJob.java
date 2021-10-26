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

import net.geocat.database.linkchecker.entities.helper.UpdateCreateDateTimeEntity;

import javax.persistence.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;

// represents a run of the link checker
@Entity
public class LinkCheckJob extends UpdateCreateDateTimeEntity  {


    @Id
    @Column(columnDefinition = "varchar(40)")
    private String jobId;

    //GUID of the havest job this is processing
    @Column(columnDefinition = "varchar(40)")
    private String harvestJobId;

    //state of the overall process
    @Enumerated(EnumType.STRING)
    private LinkCheckJobState state;

    //important messages - usually errors during a camel route
    @Column(columnDefinition = "text")
    private String messages;

    @Column(columnDefinition = "text")
    private String longTermTag;

    // how many documents were harvested
    Long numberOfDocumentsInBatch;

    //how many of the harvested documents are service records?
    Long numberOfLocalServiceRecords;

    //how many of the harvested documents are dataset records?
    Long numberOfLocalDatasetRecords;


    //------------------------------------

    @PrePersist
    protected void onInsert() {
      super.onInsert();
    }

    @PreUpdate
    protected void onUpdate() {
        super.onUpdate();
    }

    //------------------------------------


    public String getLongTermTag() {
        return longTermTag;
    }

    public void setLongTermTag(String longTermTag) {
        this.longTermTag = longTermTag;
    }

    public Long getNumberOfLocalServiceRecords() {
        return numberOfLocalServiceRecords;
    }

    public void setNumberOfLocalServiceRecords(Long numberOfLocalServiceRecords) {
        this.numberOfLocalServiceRecords = numberOfLocalServiceRecords;
    }

    public Long getNumberOfLocalDatasetRecords() {
        return numberOfLocalDatasetRecords;
    }

    public void setNumberOfLocalDatasetRecords(Long numberOfLocalDatasetRecords) {
        this.numberOfLocalDatasetRecords = numberOfLocalDatasetRecords;
    }

    public Long getNumberOfDocumentsInBatch() {
        return numberOfDocumentsInBatch;
    }

    public void setNumberOfDocumentsInBatch(Long numberOfDocumentsInBatch) {
        this.numberOfDocumentsInBatch = numberOfDocumentsInBatch;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getHarvestJobId() {
        return harvestJobId;
    }

    public void setHarvestJobId(String harvestJobId) {
        this.harvestJobId = harvestJobId;
    }

    public LinkCheckJobState getState() {
        return state;
    }

    public void setState(LinkCheckJobState state) {
        this.state = state;
    }

    public String getMessages() {
        return messages;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }

    @Override
    public String toString() {
        String result = "LinkCheckJob {";

        result += "        jobId="+jobId+"\n";
        if (state !=null)
            result += "        state="+state+"\n";
        if (harvestJobId !=null)
            result += "        harvestJobId="+harvestJobId+"\n";

        if (numberOfDocumentsInBatch !=null)
            result += "        numberOfDocumentsInBatch="+numberOfDocumentsInBatch+"\n";
        if (numberOfLocalServiceRecords !=null)
            result += "        numberOfLocalServiceRecords="+numberOfLocalServiceRecords+"\n";
        if (numberOfLocalDatasetRecords !=null)
            result += "        numberOfLocalDatasetRecords="+numberOfLocalDatasetRecords+"\n";

        result += "}\n";

        return result;
    }

}
