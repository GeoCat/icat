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

package net.geocat.xml;

import net.geocat.database.linkchecker.entities.helper.DatasetIdentifier;
import net.geocat.database.linkchecker.entities.helper.DatasetIdentifierNodeType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.geocat.service.capabilities.WMSCapabilitiesDatasetLinkExtractor.findNodes;
import static net.geocat.service.capabilities.WMSCapabilitiesDatasetLinkExtractor.findNodesFullSearch;

public class XmlDatasetMetadataDocument extends XmlMetadataDocument {

//    public String datasetIdentifier;
//    public String datasetIdentifierCodeSpace;
    List<DatasetIdentifier> datasetIdentifiers;
    public XmlDatasetMetadataDocument(XmlDoc doc) throws Exception {
        super(doc);
        datasetIdentifiers = new ArrayList<>();
        setup_XmlDatasetMetadataDocument();
    }

//    private void setupDatasetIdentifier() {
//        Node n = findNode(parsedXml, Arrays.asList(new String[] {"MD_Metadata","identificationInfo","MD_DataIdentification","citation","CI_Citation","identifier","MD_Identifier","code","CharacterString"}));
//        if (n != null) {
//            datasetIdentifier = n.getTextContent();
//            return;
//        }
//        //n = xpath_node("/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gmx:Anchor");
//        n = findNode(parsedXml, Arrays.asList(new String[] {"MD_Metadata","identificationInfo","MD_DataIdentification","citation","CI_Citation","identifier","MD_Identifier","code","Anchor"}));
//
//        if (n != null) {
//            String text = n.getTextContent();
//            if ( (text != null) && (!text.trim().isEmpty()) ) {
//                datasetIdentifier = text.trim();
//            } else {
//                n = n.getAttributes().getNamedItem("xlink:href");
//                datasetIdentifier = n.getNodeValue();
//            }
//            return;
//        }
//        // n = xpath_node("/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:RS_Identifier/gmd:code/gco:CharacterString");
//        n = findNode(parsedXml, Arrays.asList(new String[] {"MD_Metadata","identificationInfo","MD_DataIdentification","citation","CI_Citation","identifier","RS_Identifier","code","CharacterString"}));
//
//        if (n != null) {
//            datasetIdentifier = n.getTextContent();
//            return;
//        }
//        //n = xpath_node("/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:RS_Identifier/gmd:code/gmx:Anchor");
//        n = findNode(parsedXml, Arrays.asList(new String[] {"MD_Metadata","identificationInfo","MD_DataIdentification","citation","CI_Citation","identifier","RS_Identifier","code","Anchor"}));
//
//        if (n != null) {
//            n = n.getAttributes().getNamedItem("xlink:href");
//            datasetIdentifier = n.getNodeValue();
//            return;
//        }
//    }
//
//    private void setupDatasetIdentifierCodeSpace() {
//        Node n = findNode(parsedXml, Arrays.asList(new String[] {"MD_Metadata","identificationInfo","MD_DataIdentification","citation","CI_Citation","identifier","MD_Identifier","codeSpace","CharacterString"}));
//        if (n != null) {
//            datasetIdentifierCodeSpace = n.getTextContent();
//            if (datasetIdentifierCodeSpace !=null)
//                datasetIdentifierCodeSpace = datasetIdentifierCodeSpace.trim();
//            return;
//        }
//        n = findNode(parsedXml, Arrays.asList(new String[] {"MD_Metadata","identificationInfo","MD_DataIdentification","citation","CI_Citation","identifier","RS_Identifier","codeSpace","CharacterString"}));
//        if (n != null) {
//            datasetIdentifierCodeSpace = n.getTextContent();
//            if (datasetIdentifierCodeSpace !=null)
//                datasetIdentifierCodeSpace = datasetIdentifierCodeSpace.trim();
//            return;
//        }
//    }

//    public void check() {
//        Node n = findNode(parsedXml, Arrays.asList(new String[] {"MD_Metadata","identificationInfo","MD_DataIdentification","citation","CI_Citation"}));
//        List<Node> ids =  findNodes(n,"identifier");
//        if (ids.size()>1) {
//            int tt = 0;
//        }
//    }

    private  DatasetIdentifier  parseIdentifier(  Node node) {
         DatasetIdentifier   result= null;
        String mainNodeName = node.getLocalName() == null ? node.getNodeName() : node.getLocalName();
        DatasetIdentifierNodeType datasetIdentifierNodeType =   DatasetIdentifierNodeType.valueOf(mainNodeName);

        Node codeNode = findNode(node,"code");
        Node codespaceNode = findNode(node,"codeSpace");

        if (codeNode == null) {
           return result; // should not happen (no code node)
        }

        List<String> codeValues = new ArrayList<>();
        Node nodeCodeCharacterString = findNode(codeNode,"CharacterString");
        Node nodeCodeAnchor = findNode(codeNode, "Anchor");

        if ( (nodeCodeCharacterString == null) && (nodeCodeAnchor == null) ) {
            return result; //ie. <gmd:code/>
        }

        if (nodeCodeAnchor != null) {
            //anchors just link
//            if  ( (nodeCodeAnchor.getTextContent() != null) && (!nodeCodeAnchor.getTextContent().trim().isEmpty()) )
//                codeValues.add(nodeCodeAnchor.getTextContent().trim());
            Node link = nodeCodeAnchor.getAttributes().getNamedItem("xlink:href");
            if ( (link !=null) && (link.getNodeValue() != null) && (!link.getNodeValue().trim().isEmpty()) )
                codeValues.add(link.getNodeValue().trim());
        }
        else {
            //simple - use value in CharacterString
            if  ( (nodeCodeCharacterString.getTextContent() != null) && (!nodeCodeCharacterString.getTextContent().trim().isEmpty()) )
                codeValues.add(nodeCodeCharacterString.getTextContent().trim());
        }

        if (codeValues.isEmpty()) // they were empty
            return result;

        //make unique
        codeValues = codeValues.stream().distinct().collect( Collectors.toList());

        List<String> codespaceValues = new ArrayList<>();
        if (codespaceNode !=null) {
            Node nodeCodespaceCharacterString = findNode(codespaceNode,"CharacterString");
            Node nodeCodespaceAnchor = findNode(codespaceNode, "Anchor");
            if (nodeCodespaceAnchor != null) {
                //I didn't find any examples of this in a set of 10k documents - included for completeness
                //anchors have 2 possible results - the text and link
//                if  ( (nodeCodespaceAnchor.getTextContent() != null) && (!nodeCodespaceAnchor.getTextContent().trim().isEmpty()) )
//                    codespaceValues.add(nodeCodespaceAnchor.getTextContent().trim());
                Node link = nodeCodespaceAnchor.getAttributes().getNamedItem("xlink:href");
                if ( (link !=null) && (link.getNodeValue() != null) && (!link.getNodeValue().trim().isEmpty()) )
                    codespaceValues.add(link.getNodeValue().trim());
            }
            else {
                //simple - use value in CharacterString
                if  ( (nodeCodespaceCharacterString.getTextContent() != null) && (!nodeCodespaceCharacterString.getTextContent().trim().isEmpty()) )
                    codespaceValues.add(nodeCodespaceCharacterString.getTextContent().trim());
            }
        }

        codespaceValues = codespaceValues.stream().distinct().collect( Collectors.toList());

        for(String code:codeValues) {
            for (String codeSpace: codespaceValues) {
                DatasetIdentifier item = new DatasetIdentifier(datasetIdentifierNodeType, code, codeSpace);
                result = item;
            }
            if (codespaceValues.isEmpty()) {
                DatasetIdentifier item = new DatasetIdentifier(datasetIdentifierNodeType, code, null);
                result = item;
            }
        }



        return result;
    }

    //identifier is a <gmd:identifier>
    // find the FIRST MD_Identifier or RS_Identifier inside
    public Node firstMD_RSIdentifier(Node identifier){
        NodeList nl = identifier.getChildNodes();
        for (int idx=0; idx <nl.getLength();idx++) {
            Node nn = nl.item(idx);
            String name = nn.getLocalName() == null ? nn.getNodeName() : nn.getLocalName();
            if (name.equals("MD_Identifier") || name.equals("RS_Identifier") ) {
                return nn;
            }
        }
        return null;
    }

    public List<DatasetIdentifier> findDatasetIdentifier() {
        List<DatasetIdentifier> result = new ArrayList<>();
        //finds first one...
        Node n = findNode(parsedXml, Arrays.asList(new String[] {"MD_Metadata","identificationInfo","MD_DataIdentification","citation","CI_Citation"}));
        if (n == null)
            return new ArrayList<>();

        List<Node> identifiers  =  findNodes(n,"identifier");

        for (Node identifier :identifiers){
            Node MDRS_identifier = firstMD_RSIdentifier(identifier);
            if (MDRS_identifier != null) {
                DatasetIdentifier  id  = parseIdentifier(MDRS_identifier);
                if (id !=null)
                    result.add(id);
            }
        }
        return result;

//        List<Node> identifiersMD =  findNodesFullSearch(n,"MD_Identifier");
//        List<Node> identifiersRS =  findNodesFullSearch(n,"RS_Identifier");
//
//        if ( (identifiersMD.size() >=1) && (identifiersRS.size()>=3)) {
//            int t=0;
//        }
//
//        List<DatasetIdentifier> result = new ArrayList<>();
//        if (!identifiersMD.isEmpty()){
//            List<DatasetIdentifier> items = parseIdentifier(identifiersMD.get(0));
//            result.addAll(items);
//        }
//        else {
//            if (!identifiersRS.isEmpty()) {
//                List<DatasetIdentifier> items = parseIdentifier(identifiersRS.get(0));
//                result.addAll(items);
//            }
//        }
//        for(Node node: identifiersMD) {
//            List<DatasetIdentifier> items = parseIdentifier(node);
//            result.addAll(items);
//        }
//        for(Node node: identifiersRS) {
//            List<DatasetIdentifier> items = parseIdentifier(node);
//            result.addAll(items);
//        }

    //    return result;
    }



    private void setup_XmlDatasetMetadataDocument() throws XPathExpressionException {
     //   check();
        datasetIdentifiers = findDatasetIdentifier();

//        setupDatasetIdentifier();
//        setupDatasetIdentifierCodeSpace();
    }


    //--------------


    public List<DatasetIdentifier> getDatasetIdentifiers() {
        return datasetIdentifiers;
    }

    public void setDatasetIdentifiers(List<DatasetIdentifier> datasetIdentifiers) {
        this.datasetIdentifiers = datasetIdentifiers;
    }

    @Override
    public String toString() {
        String result =  "XmlDatasetMetadataDocument(fileIdentifier="+fileIdentifier;
        for(DatasetIdentifier id : this.datasetIdentifiers) {
            result += id.toString();
        }
        result += ")";
        return result;
    }

}
