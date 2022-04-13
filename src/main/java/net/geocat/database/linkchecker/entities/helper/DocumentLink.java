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

import net.geocat.database.linkchecker.entities.CapabilitiesDocument;

import javax.persistence.*;
import java.util.Arrays;
import java.util.List;

//Represents a link in a document
@MappedSuperclass
public abstract class DocumentLink extends  RetrievableSimpleLink {

    //if the operation name was attached to the link, its recorded here (see XML XSL)
    @Column(columnDefinition = "text")
    String operationName;

    //if the protocol was attached to the link, its recorded here (see XML XSL)
    @Column(columnDefinition = "text")
    String protocol;

    //if the function was attached to the link, its recorded here (see XML XSL)
    @Column(columnDefinition = "text")
    String function;

    //applicationProfile from XML
    @Column(columnDefinition = "text")
    String applicationProfile;

    //not saved - if this resolved to a capabilities document, temporarily store it here.
    @Transient
    private CapabilitiesDocument capabilitiesDocument;


    //---------------------------------------------------------------


    public CapabilitiesDocument getCapabilitiesDocument() {
        return capabilitiesDocument;
    }

    public void setCapabilitiesDocument(CapabilitiesDocument capabilitiesDocument) {
        this.capabilitiesDocument = capabilitiesDocument;
    }

    public DocumentLink(){
        super();
    }

    //--

    public static List<String> validProtocols = Arrays.asList(new String[] {
            "http://www.opengis.net/def/serviceType/ogc/wms".toLowerCase(),
            "http://www.opengis.net/def/serviceType/ogc/wmts".toLowerCase(),
            "http://www.opengis.net/def/serviceType/ogc/wfs".toLowerCase(),
            "https://tools.ietf.org/html/rfc4287".toLowerCase(),
            "ATOM Syndication Format".toLowerCase(),
            "OGC Web Feature Service".toLowerCase(),
            "OGC Web Map Service".toLowerCase(),
            "OGC Web Map Tile Service".toLowerCase(),
            "wms",
            "wmts",
            "wfs",
            "atom",
            "http://www.opengeospatial.org/standards/wms",
            "http://www.opengeospatial.org/standards/wmts",
            "http://www.opengeospatial.org/standards/wfs",
            "INSPIRE Atom".toLowerCase()
    });

    public static List<String> validAppProfiles =  Arrays.asList(new String[] {
            "Download Service".toLowerCase(),
            "View Service".toLowerCase(),
            "http://inspire.ec.europa.eu/metadata-codelist/SpatialDataServiceType/download".toLowerCase(),
            "http://inspire.ec.europa.eu/metadata-codelist/SpatialDataServiceType/view".toLowerCase()
    });

    public boolean isInspireSimplifiedLink() {
        if ( (rawURL == null) || (protocol ==null) || (applicationProfile == null))
            return false;
        if ( rawURL.isEmpty() || protocol.isEmpty() || applicationProfile.isEmpty())
            return false;

        if (!validProtocols.contains(protocol.toLowerCase()))
            return false;

        if (!validAppProfiles.contains(applicationProfile.toLowerCase()))
            return false;

        return true;
    }


    //--

    public String getApplicationProfile() {
        return applicationProfile;
    }

    public void setApplicationProfile(String applicationProfile) {
        this.applicationProfile = applicationProfile;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }


    protected void onUpdate() {
        super.onUpdate();
     }


    protected void onInsert() {
        super.onInsert();
     }

    @Override
    public String toString() {
        String result = "";
        if ((operationName != null) && (!operationName.isEmpty()))
            result += "      operationName: " + operationName + "\n";

        if ((protocol != null) && (!protocol.isEmpty()))
            result += "      protocol: " + protocol + "\n";
        if ((function != null) && (!function.isEmpty()))
            result += "      function: " + function + "\n";

        result += super.toString();
        return result;
    }
}
