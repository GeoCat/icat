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

public class AtomEntry {

    String id;
    List<AtomLink> links;

    public AtomEntry(String id, List<AtomLink> links) {
        this.id = id;
        this.links = links;
        if (links == null)
            this.links = new ArrayList<>();
    }

    public AtomLink findLink(String rel) {
        if ( (links == null) || (links.isEmpty()))
            return null;

        for (AtomLink link:links) {
            if (link.getRel().equals(rel))
                return link;
        }
        return null;
    }

    public List<AtomLink> findLinks(String rel) {
        List<AtomLink> result = new ArrayList<>();
        if ( (links == null) || (links.isEmpty()))
            return null;

        for (AtomLink link:links) {
            if ( (link.getRel() !=null) && (link.getRel().equals(rel)))
                result.add(link);
        }
        if (!links.isEmpty())
            return result;
        return null;
    }

    public AtomEntry(String id) {
        this.id = id;
        this.links = new ArrayList<>();
    }

    //--

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<AtomLink> getLinks() {
        return links;
    }

    public void setLinks(List<AtomLink> links) {
        this.links = links;
    }

    //--

    @Override
    public String toString() {
        return "AtomEntry{" +
                "id='" + id + '\'' +
                ", links=" + links +
                '}';
    }
}
