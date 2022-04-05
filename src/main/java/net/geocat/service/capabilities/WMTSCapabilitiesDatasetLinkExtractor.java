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

import net.geocat.xml.XmlDoc;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

public class WMTSCapabilitiesDatasetLinkExtractor extends WMSCapabilitiesDatasetLinkExtractor {


    public WMTSCapabilitiesDatasetLinkExtractor() {

    }

    @Override
    protected List<String> findMetadataURLs(Node layer) throws Exception {
        Node nn = findNode(layer,"MetadataURL");
        if (nn != null)
            return findMetadataURLs_11(nn);
        Node n = findNode(layer, "Metadata");

        List<String> result = new ArrayList<>();

      //  Node n = XmlDoc.xpath_node(layer, "ows:Metadata");
        if (n == null)
            return null;
        Node att = n.getAttributes().getNamedItem("xlink:href");
        if (att == null)
            return null;
        result.add(att.getTextContent().trim());
        return result;
    }

    private List<String> findMetadataURLs_11(Node metadataURL) {
        Node online = findNode(metadataURL,"OnlineResource");
        if (online == null)
            return null;
        Node att = online.getAttributes().getNamedItem("xlink:href");
        if (att == null)
            return null;
        List<String> result = new ArrayList<>();

        result.add(att.getTextContent().trim());
        return result;
    }
}
