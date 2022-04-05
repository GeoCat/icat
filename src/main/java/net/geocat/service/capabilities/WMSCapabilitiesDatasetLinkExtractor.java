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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.w3c.dom.Node.ELEMENT_NODE;

@Component
@Scope("prototype")
public class WMSCapabilitiesDatasetLinkExtractor implements ICapabilitiesDatasetLinkExtractor {

    Logger logger = LoggerFactory.getLogger(WMSCapabilitiesDatasetLinkExtractor.class);


//    public static List<DatasetLink> unique(List<DatasetLink> list) {
//        HashSet hs = new HashSet();
//        hs.addAll(list);
//        list.clear();
//        list.addAll(hs);
//        return list;
//    }

    public static List<Node> findNodesFullSearch(Node n, String localName) {
        NodeList nl = n.getChildNodes();
        List<Node> result = new ArrayList<>();
        for (int idx=0; idx <nl.getLength();idx++) {
            Node nn = nl.item(idx);
            String name = nn.getLocalName() == null ? nn.getNodeName() : nn.getLocalName();

            if (name.equals(localName)) {
                result.add(nn);
            }
            if (nn.getNodeType() == ELEMENT_NODE)
                result.addAll(findNodesFullSearch(nn,localName)); // recurse
        }
        return result;
    }


    public static List<Node> findNodes(Node n,String localName) {

        NodeList nl = n.getChildNodes();
        List<Node> result = new ArrayList<>();
        for (int idx=0; idx <nl.getLength();idx++) {
            Node nn = nl.item(idx);
            String name = nn.getLocalName() == null ? nn.getNodeName() : nn.getLocalName();

            if (name.equals(localName)) {
                result.add(nn);
                result.addAll(findNodes(nn,localName)); // recurse
            }
        }
        return result;
    }

    public Map<String, String> findAuthorityURLS(XmlDoc doc) throws Exception {
        Map<String, String> result = new HashMap<>();

        List<Node> nodes = findNodesFullSearch(doc.getFirstNode(),"AuthorityURL");
        for(Node node:nodes) {
            Node nameNode = node.getAttributes().getNamedItem("name");
            if (nameNode == null)
                continue;
            String name = nameNode.getTextContent();
            if (name == null)
                continue;
            name = name.trim();
            if (name.isEmpty())
                continue;
            Node onlineNode = findNode(node,"OnlineResource");
            if (onlineNode == null)
                continue;
            Node urlNode = onlineNode.getAttributes().getNamedItem("xlink:href");
            if (urlNode == null)
                continue;
            String url = urlNode.getTextContent();
            if (url == null)
                continue;
            url = url.trim();
            if (url.isEmpty())
                continue;
            result.put(name,url);

        }

        return result;
    }


    @Override
    public List<DatasetLink> findLinks(XmlDoc doc) throws Exception {
        List<DatasetLink> result = new ArrayList<>();

        Node main = doc.getFirstNode();

        Map<String,String> authorityURLs = findAuthorityURLS(doc);

        Node secondary = findNode(doc.getFirstNode(),"Capability");
        if (secondary == null)
            secondary = findNode(doc.getFirstNode(),"Contents");;

        List<Node> ns = findNodes(secondary,"Layer");

        int idx = 0;
        for(Node n : ns) {
          //  logger.debug("indx = "+idx);
            List<DatasetLink> links = processLayer(doc, n, authorityURLs);
            if (links != null)
                result.addAll(links);
            idx++;
        }

        return result;
        //return unique(result);
    }

    public List<DatasetLink> processLayer(XmlDoc doc, Node layer,Map<String,String> authorityURLs) throws Exception {
        List<DatasetLink> result = new ArrayList<>();

        String identifier = searchIdentifier(doc, layer);
        List<String> metadataUrls = searchMetadataUrls(doc, layer);
        String authority = searchAuthority(doc,layer);

        String name = null;
        Node nameNode = findNode(layer,"Name");
        if (nameNode !=null)
            name = nameNode.getTextContent();
        if (name !=null)
            name = name.trim();

//        if (name == null)
//            name = identifier;

        if ((identifier != null) || (metadataUrls != null)) {
            if (metadataUrls !=null) {
                for (String url : metadataUrls) {
                    DatasetLink item = new DatasetLink(identifier, url);
                    item.setOgcLayerName(name);
                    if ((authority != null) && (!authority.isEmpty())) {
                        authority = authority.trim();
                        String authorityurl = authorityURLs.get(authority);
                        if (authorityurl == null)
                            authorityurl = authority;
                        item.setAuthority(authorityurl);
                        item.setAuthorityName(authority);
                    }
                    result.add(item);
                }
            }
            else {
                DatasetLink item = new DatasetLink(identifier, null);
                if ((authority != null) && (!authority.isEmpty())) {
                    authority = authority.trim();
                    String authorityurl = authorityURLs.get(authority);
                    if (authorityurl == null)
                        authorityurl = authority;
                    item.setAuthority(authorityurl);
                    item.setAuthorityName(authority);
                }
                result.add(item);
            }
        }
        return result;
    }

    protected List<String> findMetadataURLs(Node layer) throws Exception {

        List<Node> metadataURLs = findAllNodes(layer, "MetadataURL");

        return metadataURLs.stream()
                .map(x-> findNode(x, "OnlineResource"))
                .filter(x->x !=null)
                .map(x->x.getAttributes().getNamedItem("xlink:href"))
                .filter(x->x !=null && (x.getTextContent() != null) && (!x.getTextContent().trim().isEmpty()))
                .map(x->x.getTextContent().trim())
                .collect(Collectors.toList());

    }


    private List<String> searchMetadataUrls(XmlDoc doc, Node layer) throws Exception {
        List<String> metadataURL = findMetadataURLs(layer);
        if ((metadataURL != null) && (!metadataURL.isEmpty()))
            return metadataURL;
        Node parentLayer = layer.getParentNode();
        if (parentLayer.getLocalName().equals("Layer"))
            return searchMetadataUrls(doc, parentLayer);
        return metadataURL;
    }

    public static Node findNode(Node n, String localName) {
        NodeList nl = n.getChildNodes();
        for (int idx=0; idx <nl.getLength();idx++) {
            Node nn = nl.item(idx);
            String name = nn.getLocalName() == null ? nn.getNodeName() : nn.getLocalName();
            if (name.equals(localName)) {
                return nn;
            }
        }
        return null;
    }


    public static List<Node> findAllNodes(Node n, String localName) {
        List<Node> result = new ArrayList<>();
        NodeList nl = n.getChildNodes();
        for (int idx=0; idx <nl.getLength();idx++) {
            Node nn = nl.item(idx);
            String name = nn.getLocalName() == null ? nn.getNodeName() : nn.getLocalName();
            if (name.equals(localName)) {
                result.add(nn);
            }
        }
        return result;
    }


    private String findIdentifier(Node layer) throws Exception {
//        Node n = XmlDoc.xpath_node(layer, namespaceIdentifier+":Identifier");
        Node n= findNode(layer, "Identifier") ;

        if (n == null)
            return null;
        return n.getTextContent().trim();
    }


    public String searchIdentifier(XmlDoc doc, Node layer) throws Exception {
        String localIdentifier = findIdentifier(layer);
        if ((localIdentifier != null) && (!localIdentifier.isEmpty()))
            return localIdentifier.trim();
        Node parentLayer = layer.getParentNode();
        if (parentLayer.getLocalName().equals("Layer"))
            return searchIdentifier(doc, parentLayer);
        return null;
    }

    private String findAuthority(Node layer) throws Exception {
      //  Node n = XmlDoc.xpath_node(layer, namespace+":Identifier");
        Node n = findNode(layer,"Identifier");
        if (n == null)
            return null;
        Node authorityNode = n.getAttributes().getNamedItem("authority");
        if (authorityNode == null)
            return null;
        String authority  = authorityNode.getNodeValue();
        if (!authority.isEmpty())
            return authority;
        return null;
    }

    public String searchAuthority(XmlDoc doc, Node layer) throws Exception {
        String localAuthority = findAuthority(layer);
        if ((localAuthority != null) && (!localAuthority.isEmpty()))
            return localAuthority;
        Node parentLayer = layer.getParentNode();
        if (parentLayer.getLocalName().equals("Layer"))
            return searchAuthority(doc, parentLayer);
        return null;
    }

}
