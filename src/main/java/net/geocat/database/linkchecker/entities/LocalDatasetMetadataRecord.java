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
import net.geocat.database.linkchecker.entities.helper.LinkToData;
import net.geocat.database.linkchecker.entities.helper.ServiceMetadataDocumentState;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//represents a harvested (local) Dataset document
 @NamedEntityGraph(
         name = "LocalDatasetMetadataRecord-lazy-graph",
         attributeNodes = {
                 //none - don't load sub-objects (operatesOn and documentLinks)
         }
 )
@Entity
@DiscriminatorValue("LocalDatasetMetadataRecord")
public class LocalDatasetMetadataRecord extends DatasetMetadataRecord {

    //processing state of the document
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20)")
    ServiceMetadataDocumentState state;



    // from the harvester - what is the harvester's record ID for this document?
    private long harvesterMetadataRecordId;


    // for display - info about this object
    @Column(columnDefinition = "text")
    private String summary;

    @OneToMany(//mappedBy = "datasetMetadataRecord",
            cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    @JoinColumn(name="linktodata_id")
    @Fetch(value = FetchMode.SUBSELECT)
   // @BatchSize(size=500)
    private Set<LinkToData> dataLinks;

    private Integer numberOfViewDataLinks;//dataLinks where WFS or Atom
    private Integer numberOfDownloadDataLinks;//dataLinks where WMTS or WMS

    //# of view links that were attempted to be downloaded.
    // typically, the same as numberOfViewDataLinks, but might be less if there's a lot of them
    //  We don't want to try 10,000 layers!
    private Integer numberOfViewLinksAttempted;

    // of the numberOfViewLinksAttempted, how many were actually successful?
    private Integer numberOfViewLinksSuccessful;

    private Integer numberOfDownloadLinksAttempted;
    private Integer numberOfDownloadLinksSuccessful;
    //--------

    public LocalDatasetMetadataRecord() {
        super();
        dataLinks = new HashSet<>();
    }

    public Integer getNumberOfViewLinksAttempted() {
        return numberOfViewLinksAttempted;
    }

    public void setNumberOfViewLinksAttempted(Integer numberOfViewLinksAttempted) {
        this.numberOfViewLinksAttempted = numberOfViewLinksAttempted;
    }

    public Integer getNumberOfViewLinksSuccessful() {
        return numberOfViewLinksSuccessful;
    }

    public void setNumberOfViewLinksSuccessful(Integer numberOfViewLinksSuccessful) {
        this.numberOfViewLinksSuccessful = numberOfViewLinksSuccessful;
    }

    public Integer getNumberOfDownloadLinksAttempted() {
        return numberOfDownloadLinksAttempted;
    }

    public void setNumberOfDownloadLinksAttempted(Integer numberOfDownloadLinksAttempted) {
        this.numberOfDownloadLinksAttempted = numberOfDownloadLinksAttempted;
    }

    public Integer getNumberOfDownloadLinksSuccessful() {
        return numberOfDownloadLinksSuccessful;
    }

    public void setNumberOfDownloadLinksSuccessful(Integer numberOfDownloadLinksSuccessful) {
        this.numberOfDownloadLinksSuccessful = numberOfDownloadLinksSuccessful;
    }

    public long getHarvesterMetadataRecordId() {
        return harvesterMetadataRecordId;
    }

    public void setHarvesterMetadataRecordId(long harvesterMetadataRecordId) {
        this.harvesterMetadataRecordId = harvesterMetadataRecordId;
    }

    public Integer getNumberOfViewDataLinks() {
        return numberOfViewDataLinks;
    }

    public void setNumberOfViewDataLinks(Integer numberOfViewDataLinks) {
        this.numberOfViewDataLinks = numberOfViewDataLinks;
    }

    public Integer getNumberOfDownloadDataLinks() {
        return numberOfDownloadDataLinks;
    }

    public void setNumberOfDownloadDataLinks(Integer numberOfDownloadDataLinks) {
        this.numberOfDownloadDataLinks = numberOfDownloadDataLinks;
    }

    public ServiceMetadataDocumentState getState() {
        return state;
    }

    public void setState(ServiceMetadataDocumentState state) {
        this.state = state;
    }

    public Set<LinkToData> getDataLinks() {
        return dataLinks;
    }

    public void setDataLinks(Set<LinkToData> dataLinks) {
        this.dataLinks = dataLinks;
    }
//---------------------------------------------------------------------------

    @PreUpdate
    protected void onUpdate() {
        summary = toString();
        super.onUpdate();
    }

    @PrePersist
    protected void onInsert() {
        summary = toString();
        super.onInsert();
    }

    //---------------------------------------------------------------------------

    @Override
    public String toString() {
        String result = "LocalDatasetMetadataRecord {\n";
        result += "      Dataset Metadata Document Id: " + getDatasetMetadataDocumentId() + "\n";
        result += "     linkCheckJobId: " + getLinkCheckJobId() + "\n";
        result += "     harvesterMetadataRecordId: " + harvesterMetadataRecordId + "\n";
        result += "     state: " + state + "\n";


        result += super.toString();

        result += "\n";

        result += " }";
        return result;
    }
}
