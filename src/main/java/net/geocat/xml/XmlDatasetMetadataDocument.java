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

import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.util.Arrays;

public class XmlDatasetMetadataDocument extends XmlMetadataDocument {

    public String datasetIdentifier;

    public XmlDatasetMetadataDocument(XmlDoc doc) throws Exception {
        super(doc);
        setup_XmlDatasetMetadataDocument();
    }

    private void setup_XmlDatasetMetadataDocument() throws XPathExpressionException {
       // Node n = xpath_node("/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString");
       // Node n = xpath_node("/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString");
        Node n = findNode(parsedXml, Arrays.asList(new String[] {"MD_Metadata","identificationInfo","MD_DataIdentification","citation","CI_Citation","identifier","MD_Identifier","code","CharacterString"}));


        if (n != null) {
            datasetIdentifier = n.getTextContent();
            return;
        }
        //n = xpath_node("/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gmx:Anchor");
        n = findNode(parsedXml, Arrays.asList(new String[] {"MD_Metadata","identificationInfo","MD_DataIdentification","citation","CI_Citation","identifier","MD_Identifier","code","Anchor"}));

        if (n != null) {
            String text = n.getTextContent();
            if ( (text != null) && (!text.trim().isEmpty()) ) {
                datasetIdentifier = text.trim();
            } else {
                n = n.getAttributes().getNamedItem("xlink:href");
                datasetIdentifier = n.getNodeValue();
            }
            return;
        }
       // n = xpath_node("/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:RS_Identifier/gmd:code/gco:CharacterString");
        n = findNode(parsedXml, Arrays.asList(new String[] {"MD_Metadata","identificationInfo","MD_DataIdentification","citation","CI_Citation","identifier","RS_Identifier","code","CharacterString"}));

        if (n != null) {
            datasetIdentifier = n.getTextContent();
            return;
        }
        //n = xpath_node("/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:RS_Identifier/gmd:code/gmx:Anchor");
        n = findNode(parsedXml, Arrays.asList(new String[] {"MD_Metadata","identificationInfo","MD_DataIdentification","citation","CI_Citation","identifier","RS_Identifier","code","Anchor"}));

        if (n != null) {
            n = n.getAttributes().getNamedItem("xlink:href");
            datasetIdentifier = n.getNodeValue();
            return;
        }
    }

    public String getDatasetIdentifier() {
        return datasetIdentifier;
    }

    public void setDatasetIdentifier(String datasetIdentifier) {
        this.datasetIdentifier = datasetIdentifier;
    }

    @Override
    public String toString() {
        String result =  "XmlDatasetMetadataDocument(fileIdentifier="+fileIdentifier;
        result += ", datasetIdentifier = "+datasetIdentifier;
        result += ")";
        return result;
    }

}
