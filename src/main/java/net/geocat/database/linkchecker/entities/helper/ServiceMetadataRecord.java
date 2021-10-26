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

import net.geocat.database.linkchecker.entities.OperatesOnLink;
import net.geocat.database.linkchecker.entities.ServiceDocumentLink;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;


//base class for Service Metadata records
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "service_record_type",
        discriminatorType = DiscriminatorType.STRING)
public class ServiceMetadataRecord extends MetadataRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long serviceMetadataDocumentId;

    //type of service
    //i.e. view/download/discovery
    @Column(columnDefinition = "text")
    private String metadataServiceType;

    //number of links found in the document
    // i.e. serviceDocumentLinks.size()
    private Integer numberOfLinksFound;

    //number of operatesOn found in the document
    // i.e. operatesOnLinks.size()
    private Integer numberOfOperatesOnFound;

    @OneToMany(mappedBy = "serviceMetadataRecord",
            cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    // @JoinColumn(name="serviceMetadataRecordId")
    @Fetch(value = FetchMode.SUBSELECT)
    private Set<ServiceDocumentLink> serviceDocumentLinks;

    @OneToMany(mappedBy = "serviceMetadataRecord",
            cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    // @JoinColumn(name="serviceMetadataRecordId")
    @Fetch(value = FetchMode.SUBSELECT)
    private Set<OperatesOnLink> operatesOnLinks;


    //PASS if ANY linked capabilities document has a link to a Service Metadata Record that resolves to a Service Metadata Record.
    // null = not evaluated
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(5)")
    IndicatorStatus INDICATOR_CAPABILITIES_RESOLVES_TO_SERVICE;

//    //PASS if ANY linked capabilities document's Service Metadata Record has the same file identifier and the XML documents are the same as our starting service metadata record.
//    // null = not evaluated
//    @Enumerated(EnumType.STRING)
//    @Column(columnDefinition = "varchar(5)")
//    IndicatorStatus INDICATOR_CAPABILITIES_SERVICE_FULLY_MATCHES;

    //PASS if ANY linked capabilities document's Service Metadata Record has the same file identifier as our starting service metadata record.
    // null = not evaluated
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(5)")
    IndicatorStatus INDICATOR_CAPABILITIES_SERVICE_FILE_ID_MATCHES;

    //PASS if ALL the Capabilities Layer links resolve to a Dataset Metadata record.
    //
    //NOTE:
    //It's OK if a layer does NOT have a metadata link
    //At least one layer must have a DS link
    // null = not evaluated
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(5)")
    IndicatorStatus INDICATOR_ALL_CAPABILITIES_LAYER_RESOLVE;

    //PASS if ALL of the OperatesOnLinks resolve to a Dataset Metadata document.
    // null = not evaluated
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(5)")
    IndicatorStatus INDICATOR_ALL_OPERATES_ON_RESOLVE;

    //PASS if ALL of the OperatesOnLinks Dataset Metadata documents match a document linked from the Capabilities Layers.
    // null = not evaluated
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(5)")
    IndicatorStatus INDICATOR_ALL_OPERATES_ON_MATCH_CAPABILITIES;

    public ServiceMetadataRecord(){
        super();
        serviceDocumentLinks = new HashSet<>();
        operatesOnLinks =new HashSet<>();
    }

    //---------------------------------------------------------------------------


    public IndicatorStatus getINDICATOR_CAPABILITIES_SERVICE_FILE_ID_MATCHES() {
        return INDICATOR_CAPABILITIES_SERVICE_FILE_ID_MATCHES;
    }

    public void setINDICATOR_CAPABILITIES_SERVICE_FILE_ID_MATCHES(IndicatorStatus INDICATOR_CAPABILITIES_SERVICE_FILE_ID_MATCHES) {
        this.INDICATOR_CAPABILITIES_SERVICE_FILE_ID_MATCHES = INDICATOR_CAPABILITIES_SERVICE_FILE_ID_MATCHES;
    }

    public IndicatorStatus getINDICATOR_CAPABILITIES_RESOLVES_TO_SERVICE() {
        return INDICATOR_CAPABILITIES_RESOLVES_TO_SERVICE;
    }

    public void setINDICATOR_CAPABILITIES_RESOLVES_TO_SERVICE(IndicatorStatus INDICATOR_CAPABILITIES_RESOLVES_TO_SERVICE) {
        this.INDICATOR_CAPABILITIES_RESOLVES_TO_SERVICE = INDICATOR_CAPABILITIES_RESOLVES_TO_SERVICE;
    }

//    public IndicatorStatus getINDICATOR_CAPABILITIES_SERVICE_FULLY_MATCHES() {
//        return INDICATOR_CAPABILITIES_SERVICE_FULLY_MATCHES;
//    }
//
//    public void setINDICATOR_CAPABILITIES_SERVICE_FULLY_MATCHES(IndicatorStatus INDICATOR_CAPABILITIES_SERVICE_MATCHES) {
//        this.INDICATOR_CAPABILITIES_SERVICE_FULLY_MATCHES = INDICATOR_CAPABILITIES_SERVICE_MATCHES;
//    }

    public IndicatorStatus getINDICATOR_ALL_CAPABILITIES_LAYER_RESOLVE() {
        return INDICATOR_ALL_CAPABILITIES_LAYER_RESOLVE;
    }

    public void setINDICATOR_ALL_CAPABILITIES_LAYER_RESOLVE(IndicatorStatus INDICATOR_ALL_CAPABILITIES_LAYER_RESOLVE) {
        this.INDICATOR_ALL_CAPABILITIES_LAYER_RESOLVE = INDICATOR_ALL_CAPABILITIES_LAYER_RESOLVE;
    }

    public IndicatorStatus getINDICATOR_ALL_OPERATES_ON_RESOLVE() {
        return INDICATOR_ALL_OPERATES_ON_RESOLVE;
    }

    public void setINDICATOR_ALL_OPERATES_ON_RESOLVE(IndicatorStatus INDICATOR_ALL_OPERATES_ON_RESOLVE) {
        this.INDICATOR_ALL_OPERATES_ON_RESOLVE = INDICATOR_ALL_OPERATES_ON_RESOLVE;
    }

    public IndicatorStatus getINDICATOR_ALL_OPERATES_ON_MATCH_CAPABILITIES() {
        return INDICATOR_ALL_OPERATES_ON_MATCH_CAPABILITIES;
    }

    public void setINDICATOR_ALL_OPERATES_ON_MATCH_CAPABILITIES(IndicatorStatus INDICATOR_ALL_OPERATES_ON_MATCH_CAPABILITIES) {
        this.INDICATOR_ALL_OPERATES_ON_MATCH_CAPABILITIES = INDICATOR_ALL_OPERATES_ON_MATCH_CAPABILITIES;
    }

    public long getServiceMetadataDocumentId() {
        return serviceMetadataDocumentId;
    }

    public void setServiceMetadataDocumentId(long serviceMetadataDocumentId) {
        this.serviceMetadataDocumentId = serviceMetadataDocumentId;
    }

    public String getMetadataServiceType() {
        return metadataServiceType;
    }

    public void setMetadataServiceType(String metadataServiceType) {
        this.metadataServiceType = metadataServiceType;
    }

    public Integer getNumberOfLinksFound() {
        return numberOfLinksFound;
    }

    public void setNumberOfLinksFound(Integer numberOfLinksFound) {
        this.numberOfLinksFound = numberOfLinksFound;
    }

    public Integer getNumberOfOperatesOnFound() {
        return numberOfOperatesOnFound;
    }

    public void setNumberOfOperatesOnFound(Integer numberOfOperatesOnFound) {
        this.numberOfOperatesOnFound = numberOfOperatesOnFound;
    }

    public Set<ServiceDocumentLink> getServiceDocumentLinks() {
        return serviceDocumentLinks;
    }

    public void setServiceDocumentLinks(Set<ServiceDocumentLink> serviceDocumentLinks) {
        this.serviceDocumentLinks = serviceDocumentLinks;
    }

    public Set<OperatesOnLink> getOperatesOnLinks() {
        return operatesOnLinks;
    }

    public void setOperatesOnLinks(Set<OperatesOnLink> operatesOnLinks) {
        this.operatesOnLinks = operatesOnLinks;
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
        if (serviceDocumentLinks != null)
            numberOfLinksFound = serviceDocumentLinks.size();
        if (operatesOnLinks != null)
            numberOfOperatesOnFound = operatesOnLinks.size();
    }

    //---------------------------------------------------------------------------

    @Override
    public String toString() {
        update();
        String result = super.toString();

        result += "     metadataServiceType: " + metadataServiceType + "\n";
        if (numberOfLinksFound != null)
            result += "     numberOfLinksFound: " + numberOfLinksFound + "\n";
        if (numberOfOperatesOnFound != null)
            result += "     numberOfOperatesOnFound: " + numberOfOperatesOnFound + "\n";

        return result;
    }
}
