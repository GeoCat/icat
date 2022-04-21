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

import net.geocat.database.linkchecker.entities.helper.CapabilitiesDatasetMetadataLinkDatasetIdentifier;
import net.geocat.database.linkchecker.entities.helper.DatasetIdentifier;
import net.geocat.database.linkchecker.entities.helper.DatasetMetadataRecordDatasetIdentifier;
import net.geocat.database.linkchecker.entities.helper.PartialDownloadHint;
import net.geocat.database.linkchecker.entities.helper.RetrievableSimpleLink;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// This models all the Dataset links from a capabilities (typically 1 per layer)
@Entity
@Table(
        indexes = {
                @Index(
                        name = "CDML_capsha2_capjobid",
                        columnList = "cap_sha2,cap_jobId",
                        unique = false
                ),
                @Index(
                        name = "CDML_jobid_fileid",
                        columnList = "linkCheckJobId,fileIdentifier",
                        unique = false
                )
        }
)
public class CapabilitiesDatasetMetadataLink extends RetrievableSimpleLink {

//    // link to the actual Dataset document (if it resolves to one)
//    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
//    @JoinColumn(name = "datasetMetadataRecordId")
//    @Fetch(value = FetchMode.JOIN)
//    CapabilitiesRemoteDatasetMetadataDocument capabilitiesRemoteDatasetMetadataDocument;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns(
            {
                    @JoinColumn(name="cap_sha2",referencedColumnName = "sha2"),
                    @JoinColumn(name="cap_jobId",referencedColumnName = "linkcheckjobid")
            }
    )
    private CapabilitiesDocument capabilitiesDocument;

     @Column(columnDefinition = "text")
    String fileIdentifier;

    @Column(columnDefinition = "text")
    String parentIdentifier;

    @OneToMany(mappedBy = "capDatasetMetadataLink",
            cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SELECT)
            @BatchSize(size=500)
    @OnDelete(action = OnDeleteAction.CASCADE)
    List<CapabilitiesDatasetMetadataLinkDatasetIdentifier> datasetIdentifiers;

    @Column(columnDefinition = "text")
    String ogcLayerName; // <Layer><Name>  or <FeatureType><Name>

//    //link back to the capabilities document this link came from
//    @ManyToOne(fetch = FetchType.EAGER,cascade = {CascadeType.ALL})
//    CapabilitiesDocument capabilitiesDocument;

    //from the Capabilities document - identity for the layer
    @Column(columnDefinition = "text")
    String identity;

    //from the Capabilities document - authority (url) for the layer
    @Column(columnDefinition = "text")
    String authority;

    //from the Capabilities document - authority (name of authority) for the layer
    // i.e. <Identity authority="XYZ" .../>
    @Column(columnDefinition = "text")
    String authorityName;

    //store summary info about this
    @Column(columnDefinition = "text")
    String summary;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long capabilitiesDatasetMetadataLinkId;


    public CapabilitiesDatasetMetadataLink() {
        this.setPartialDownloadHint(PartialDownloadHint.METADATA_ONLY);
        this.datasetIdentifiers=new ArrayList<>();
    }

    //---------------------------------------------------------------------------


    public CapabilitiesDocument getCapabilitiesDocument() {
        return capabilitiesDocument;
    }

    public void setCapabilitiesDocument(CapabilitiesDocument capabilitiesDocument) {
        this.capabilitiesDocument = capabilitiesDocument;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public String getOgcLayerName() {
        return ogcLayerName;
    }

    public void setOgcLayerName(String ogcLayerName) {
        this.ogcLayerName = ogcLayerName;
    }

    public String getFileIdentifier() {
        return fileIdentifier;
    }

    public void setFileIdentifier(String fileIdentifier) {
        this.fileIdentifier = fileIdentifier;
    }

    public List<CapabilitiesDatasetMetadataLinkDatasetIdentifier> getDatasetIdentifiers() {
        return datasetIdentifiers;
    }

    public void setDatasetIdentifiers(List<DatasetIdentifier> datasetIdentifiers) {
        this.datasetIdentifiers = datasetIdentifiers.stream().map(x->new CapabilitiesDatasetMetadataLinkDatasetIdentifier(x,this)).collect(Collectors.toList());
        // this.datasetIdentifiers = datasetIdentifiers;
    }

    public long getCapabilitiesDatasetMetadataLinkId() {
        return capabilitiesDatasetMetadataLinkId;
    }

    public void setCapabilitiesDatasetMetadataLinkId(long capabilitiesDatasetMetadataLinkId) {
        this.capabilitiesDatasetMetadataLinkId = capabilitiesDatasetMetadataLinkId;
    }

    public String getAuthorityName() {
        return authorityName;
    }

    public void setAuthorityName(String authorityName) {
        this.authorityName = authorityName;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getParentIdentifier() {
        return parentIdentifier;
    }

    public void setParentIdentifier(String parentIdentifier) {
        this.parentIdentifier = parentIdentifier;
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
        result += "      authority: " + authority + "\n";
        result += "      file Identifier: " + fileIdentifier + "\n";
      //  result += "      dataset identifier: " + datasetIdentifier + "\n";
        result += "      ogcLayerName: " + ogcLayerName + "\n";

        result += "\n";
        result += super.toString();
        result += "\n";


        result += "  }";
        return result;
    }
}
