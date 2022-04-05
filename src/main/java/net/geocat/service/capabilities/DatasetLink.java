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

package net.geocat.service.capabilities;

import java.util.Objects;

public class DatasetLink {

    //for atom;
    //   ogcLayerName -> entry/id
    //   identifier -> spatial_dataset_identifier_code  code
    //   authority  -> spatial_dataset_identifier_namespace  namespace

    String identifier; // identifier (if present) - code
    String rawUrl; //metadataURL
    String authority; //for wms/wmts and atom (codespace)
    String authorityName; // alternative codespace for wmts/wms - name of the AuthorityURL
    String ogcLayerName; // name of the layer/featuretype to make ogc request

    //---------------------------------------------------------------------------

    public DatasetLink(String identifier, String rawUrl) {
        this.identifier = identifier;
        this.rawUrl = rawUrl;
    }


    //---------------------------------------------------------------------------


    public String getOgcLayerName() {
        return ogcLayerName;
    }

    public void setOgcLayerName(String ogcLayerName) {
        this.ogcLayerName = ogcLayerName;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getRawUrl() {
        return rawUrl;
    }

    public void setRawUrl(String rawUrl) {
        this.rawUrl = rawUrl;
    }

    //---------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatasetLink that = (DatasetLink) o;
        return Objects.equals(identifier, that.identifier)
                && Objects.equals(rawUrl, that.rawUrl)
                && Objects.equals(authority, that.authority) ;
    }

    public String getAuthorityName() {
        return authorityName;
    }

    public void setAuthorityName(String authorityName) {
        this.authorityName = authorityName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, rawUrl);
    }

    //---------------------------------------------------------------------------

    @Override
    public String toString() {
        String result= "DatasetLink {" +
                "identifier='" + identifier + '\'' +
                ", rawUrl='" + rawUrl + '\''  ;
        if (authority != null)
            result +=", authority="+authority;
        result += "}";
        return result;
    }
}
