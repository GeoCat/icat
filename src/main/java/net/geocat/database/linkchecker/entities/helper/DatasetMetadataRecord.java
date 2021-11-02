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


import net.geocat.database.linkchecker.entities.DatasetDocumentLink;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


// Represents a Dataset Metadata Record
@Entity
@Table(
        indexes = {
                @Index(
                        name = "datasetmetadatarecord_linkcheckjobid_idx",
                        columnList = "linkCheckJobId",
                        unique = false
                ),
                @Index(
                        name = "DatasetMetadataRecord_sha2_linkcheckjobid",
                        columnList = "sha2,linkCheckJobId",
                        unique = false
                )
        }
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dataset_record_type",
        discriminatorType = DiscriminatorType.STRING)
public class DatasetMetadataRecord extends MetadataRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long datasetMetadataDocumentId;

    // INSPIRE dataset identifier (from document)
    private String datasetIdentifier;

    // number of links found in the document
    //  i.e. documentLinks.size()
    private Integer numberOfLinksFound;

    //which link check job is this document apart of
    @Column(columnDefinition = "varchar(40)")
    private String linkCheckJobId;


    //list of SHA2 links to capabilities
    //   to find cap doc, use linkCheckJobId+SHA2
    @Column(columnDefinition = "text")
    private String linksToViewCapabilities;

    //list of SHA2 links to capabilities
    //   to find cap doc, use linkCheckJobId+SHA2
    @Column(columnDefinition = "text")
    private String linksToDownloadCapabilities;

    // all the outgoing links (i.e. capabilities documents) from the document
    @OneToMany(mappedBy = "datasetMetadataRecord",
            cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.JOIN)
    private List<DatasetDocumentLink> documentLinks;

    //PASS if ANY of the capabilities documents has a layer link (dataset) that matches this document (file id and dataset id).
    // null = not evaluated
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(5)")
    IndicatorStatus INDICATOR_LAYER_MATCHES;

    //PASS if ANY of the WMS/WMTS capabilities documents has a layer link (dataset) that matches this document (file id and dataset id).
    // null = not evaluated
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(5)")
    IndicatorStatus INDICATOR_LAYER_MATCHES_VIEW;

    //PASS if ANY of the WFS/ATOM capabilities documents has a layer link (dataset) that matches this document (file id and dataset id).
    // null = not evaluated
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(5)")
    IndicatorStatus INDICATOR_LAYER_MATCHES_DOWNLOAD;

    //PASS if ANY of the "download" service documents has a operatesOn (dataset) that matches this document (file id and dataset id).
    // null = not evaluated
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(5)")
    IndicatorStatus INDICATOR_SERVICE_MATCHES_DOWNLOAD;

    //PASS if ANY of the "view" service documents has a operatesOn (dataset) that matches this document (file id and dataset id).
    // null = not evaluated
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(5)")
    IndicatorStatus INDICATOR_SERVICE_MATCHES_VIEW;


    //---------------------------------------------------------------------------

    public DatasetMetadataRecord() {
        super();
        documentLinks = new ArrayList<>();
    }

    public String getLinksToViewCapabilities() {
        return linksToViewCapabilities;
    }

    public void setLinksToViewCapabilities(String linksToViewCapabilities) {
        this.linksToViewCapabilities = linksToViewCapabilities;
    }

    public String getLinksToDownloadCapabilities() {
        return linksToDownloadCapabilities;
    }

    public void setLinksToDownloadCapabilities(String linksToDownloadCapabilities) {
        this.linksToDownloadCapabilities = linksToDownloadCapabilities;
    }

    public IndicatorStatus getINDICATOR_SERVICE_MATCHES_DOWNLOAD() {
        return INDICATOR_SERVICE_MATCHES_DOWNLOAD;
    }

    public void setINDICATOR_SERVICE_MATCHES_DOWNLOAD(IndicatorStatus INDICATOR_SERVICE_MATCHES_DOWNLOAD) {
        this.INDICATOR_SERVICE_MATCHES_DOWNLOAD = INDICATOR_SERVICE_MATCHES_DOWNLOAD;
    }

    public IndicatorStatus getINDICATOR_SERVICE_MATCHES_VIEW() {
        return INDICATOR_SERVICE_MATCHES_VIEW;
    }

    public void setINDICATOR_SERVICE_MATCHES_VIEW(IndicatorStatus INDICATOR_SERVICE_MATCHES_VIEW) {
        this.INDICATOR_SERVICE_MATCHES_VIEW = INDICATOR_SERVICE_MATCHES_VIEW;
    }

    public String getLinkCheckJobId() {
        return linkCheckJobId;
    }

    public void setLinkCheckJobId(String linkCheckJobId) {
        this.linkCheckJobId = linkCheckJobId;
    }

    public IndicatorStatus getINDICATOR_LAYER_MATCHES_VIEW() {
        return INDICATOR_LAYER_MATCHES_VIEW;
    }

    public void setINDICATOR_LAYER_MATCHES_VIEW(IndicatorStatus INDICATOR_LAYER_MATCHES_VIEW) {
        this.INDICATOR_LAYER_MATCHES_VIEW = INDICATOR_LAYER_MATCHES_VIEW;
    }

    public IndicatorStatus getINDICATOR_LAYER_MATCHES_DOWNLOAD() {
        return INDICATOR_LAYER_MATCHES_DOWNLOAD;
    }

    public void setINDICATOR_LAYER_MATCHES_DOWNLOAD(IndicatorStatus INDICATOR_LAYER_MATCHES_DOWNLOAD) {
        this.INDICATOR_LAYER_MATCHES_DOWNLOAD = INDICATOR_LAYER_MATCHES_DOWNLOAD;
    }

    public IndicatorStatus getINDICATOR_LAYER_MATCHES() {
        return INDICATOR_LAYER_MATCHES;
    }

    public void setINDICATOR_LAYER_MATCHES(IndicatorStatus INDICATOR_LAYER_MATCHES) {
        this.INDICATOR_LAYER_MATCHES = INDICATOR_LAYER_MATCHES;
    }

    public List<DatasetDocumentLink> getDocumentLinks() {
        return documentLinks;
    }

    public void setDocumentLinks(List<DatasetDocumentLink> documentLinks) {
        this.documentLinks = documentLinks;
    }

    public Integer getNumberOfLinksFound() {
        return numberOfLinksFound;
    }

    public void setNumberOfLinksFound(Integer numberOfLinksFound) {
        this.numberOfLinksFound = numberOfLinksFound;
    }

    public long getDatasetMetadataDocumentId() {
        return datasetMetadataDocumentId;
    }

    public void setDatasetMetadataDocumentId(long datasetMetadataDocumentId) {
        this.datasetMetadataDocumentId = datasetMetadataDocumentId;
    }

    public String getDatasetIdentifier() {
        return datasetIdentifier;
    }

    public void setDatasetIdentifier(String datasetIdentifier) {
        this.datasetIdentifier = datasetIdentifier;
    }


    //---------------------------------------------------------------------------


    protected void onUpdate() {
        super.onUpdate();
        update();
    }


    protected void onInsert() {
        super.onInsert();
        update();
    }

    protected void update() {
        if (documentLinks != null)
            numberOfLinksFound = documentLinks.size();
    }

    //---------------------------------------------------------------------------
    @Override
    public String toString() {
        String result = super.toString();

        result += "     dataset Identifier: " + datasetIdentifier + "\n";
        if (numberOfLinksFound != null)
            result += "     number of links found: "+ numberOfLinksFound+"\n";

        return result;
    }
}
