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

package net.geocat.xml.helpers;

import java.util.ArrayList;
import java.util.List;

public class WMTSLayer {
    String identifier;
    String title;
    List<String> formats;

    List<WMTSTileMatrixSetLink> tileMatrixSetLinks;

    public WMTSLayer(String identifier,String title) {
        this.identifier = identifier;
        this.title = title;
        this.formats = new ArrayList<>();
        this.tileMatrixSetLinks=new ArrayList<>();
    }


    public boolean supportsFormat(String format){
        return formats.stream()
                .anyMatch(x->x.equalsIgnoreCase(format));
    }


    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public List<String> getFormats() {
        return formats;
    }

    public void setFormats(List<String> formats) {
        this.formats = formats;
    }

    public List<WMTSTileMatrixSetLink> getTileMatrixSetLinks() {
        return tileMatrixSetLinks;
    }

    public void setTileMatrixSetLinks(List<WMTSTileMatrixSetLink> tileMatrixSetLinks) {
        this.tileMatrixSetLinks = tileMatrixSetLinks;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "WMTSLayer{" +
                "identifier='" + identifier + '\'' +
                ", formats=" + formats +
                ", tileMatrixSetLinks=" + tileMatrixSetLinks +
                '}';
    }
}
