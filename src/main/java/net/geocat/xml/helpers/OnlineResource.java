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

import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.geocat.service.capabilities.WMSCapabilitiesDatasetLinkExtractor.findNodes;
import static net.geocat.xml.XmlDoc.findNode;

//represents a <gmd:CI_OnlineResource>
// can also have the operationName if the CI_OnlineResource comes from a containsOperation
public class OnlineResource {
    Node CI_OnlineResource;
    String operationName;

    String rawURL;
    String protocol;
    String function;

    public OnlineResource(Node node) throws Exception {
        this(node, null);
    }

    public OnlineResource(Node node, String operationName) throws Exception {
        if (!node.getLocalName().equals("CI_OnlineResource"))
            throw new Exception("OnlineResource -- root node should be CI_OnlineResource");

        this.CI_OnlineResource = node;
        this.operationName = operationName;

        parse();
    }

    public static List<OnlineResource> create(NodeList nl) throws Exception {
        List<OnlineResource> result = new ArrayList<>(nl.getLength());
        for (int idx = 0; idx < nl.getLength(); idx++) {
            Node n = nl.item(idx);
            List<OnlineResource> resources = create(n);
            result.addAll(resources);
        }
        return result;
    }

    public static List<OnlineResource> create(Node n) throws Exception {
        if (n.getLocalName().equals("CI_OnlineResource"))
            return Arrays.asList(new OnlineResource(n));
        //
        List<OnlineResource> result = new ArrayList<>();
        if (n.getLocalName().equals("SV_OperationMetadata")) {
            String opName = null;
            //Node nn = XmlDoc.xpath_node(n, "srv:operationName/gco:CharacterString");
          //  Node nn = XmlDoc.xpath_node(n, "*[local-name()='operationName']/*[local-name()='CharacterString']");
            Node nn = findNode(n, "operationName","CharacterString");
            if (nn != null)
                opName = nn.getTextContent();
           //NodeList nl = XmlDoc.xpath_nodeset(n, "srv:connectPoint/gmd:CI_OnlineResource");
           // NodeList nl = XmlDoc.xpath_nodeset(n, "*[local-name()='connectPoint']/*[local-name()='CI_OnlineResource']");
            //NodeList nl = XmlDoc.xpath_nodeset(n, "*[local-name()='connectPoint']/*[local-name()='CI_OnlineResource']");
            List<Node> nl = findNodes(n, "connectPoint");

            for (Node _n : nl) {
                Node nnn = findNode(_n,"CI_OnlineResource");
                if (nnn != null)
                    result.add(new OnlineResource(nnn, opName));
            }
            return result;
        }
        throw new Exception("dont know how to parse");
    }

    private void parse() throws XPathExpressionException {
        //URL
      //  Node urlNode = XmlDoc.xpath_node(CI_OnlineResource, "gmd:linkage/gmd:URL");
        Node urlNode = XmlDoc.findNode(CI_OnlineResource, "linkage","URL");

        if (urlNode != null)
            rawURL = urlNode.getTextContent();


        //Protocol
        //Node protocolNode = XmlDoc.xpath_node(CI_OnlineResource, "gmd:protocol/gco:CharacterString");
        Node protocolNode = XmlDoc.findNode(CI_OnlineResource, "protocol","CharacterString");
        if ((protocolNode != null)) {
            protocol = protocolNode.getTextContent();
            if (protocol.equals("null"))
                protocol = null;
        }

        //function
       // Node functionNode = XmlDoc.xpath_node(CI_OnlineResource, "gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue");
        Node functionNode = XmlDoc.findNode(CI_OnlineResource, "function","CI_OnLineFunctionCode");
        if (functionNode != null)
            functionNode = functionNode.getAttributes().getNamedItem("codeListValue");
        if (functionNode != null)
            function = functionNode.getTextContent();
    }

    public String getOperationName() {
        return operationName;
    }

    public String getRawURL() {
        return rawURL;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getFunction() {
        return function;
    }
}
