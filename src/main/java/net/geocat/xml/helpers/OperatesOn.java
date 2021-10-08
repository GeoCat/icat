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

import net.geocat.xml.XmlDoc;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class OperatesOn {

    String uuidref;
    String rawUrl;

    public String getAttribute(Node n, String attname) {
        Node att = n.getAttributes().getNamedItem(attname);
        if (att == null)
            return null;
        String value = att.getNodeValue();
        return value;
    }

    public OperatesOn(Node node) throws Exception {
        if (!node.getLocalName().equals("operatesOn"))
            throw new Exception("OperatesOn -- root node should be operatesOn");

        uuidref = getAttribute(node, "uuidref");
        rawUrl = getAttribute(node,   "xlink:href");


//        uuidref = XmlDoc.xpath_attribute(node, ".", "uuidref");
//        rawUrl = XmlDoc.xpath_attribute(node, ".", "xlink:href");
    }

    public static List<OperatesOn> create(List<Node> nl) throws Exception {
        List<OperatesOn> result = new ArrayList<>(nl.size());
        for (Node n: nl) {
           // Node n = nl.item(idx);
            OperatesOn opOn = new OperatesOn(n);
            result.add(opOn);
        }
        return result;
    }

    public String getUuidref() {
        return uuidref;
    }

    public String getRawUrl() {
        return rawUrl;
    }
}
