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

import net.geocat.xml.helpers.CapabilitiesType;
import net.geocat.xml.helpers.CapabilityDeterminer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpressionException;

@Component
public class XmlDocumentFactory {

//    @Autowired
//    DownloadServiceTypeProbe downloadServiceTypeProbe;

    @Autowired
    public CapabilityDeterminer capabilityDeterminer;

    public XmlDoc create(String xml) throws Exception {
//        long startTime  = System.currentTimeMillis();

        XmlDoc doc = new XmlDoc(xml);
        doc = simplify(doc);
//        System.out.println("parse time1: " + (System.currentTimeMillis() - startTime));

        if (isCSWServiceMetadataDocument(doc)) {
            XmlServiceRecordDoc xmlServiceRecordDoc = new XmlServiceRecordDoc(doc);
            return xmlServiceRecordDoc;
        }
        if (isCSWMetadataDocument(doc)) {
            doc = new XmlMetadataDocument(doc);
            XmlMetadataDocument xmlMetadataDocument = (XmlMetadataDocument) doc;
            if ( (xmlMetadataDocument.getMetadataDocumentType() == MetadataDocumentType.Dataset) || (xmlMetadataDocument.getMetadataDocumentType() == MetadataDocumentType.Series) ) {
                XmlDatasetMetadataDocument xmlDatasetMetadataDocument = new XmlDatasetMetadataDocument(xmlMetadataDocument);
                return xmlDatasetMetadataDocument;
            }
            return xmlMetadataDocument;
        }
        if (isCapabilitiesDoc(doc)) {
            CapabilitiesType type = capabilityDeterminer.determineCapabilitiesType(doc);
//            System.out.println("parse time2: " + (System.currentTimeMillis() - startTime));

            XmlDoc result =  XmlCapabilitiesDocument.create(doc, type);
//            System.out.println("parse time3: " + (System.currentTimeMillis() - startTime));

            return result;
        }
        return doc;
    }

    private XmlDoc simplify(XmlDoc doc) throws Exception {
        if (doc.getRootTagName().equals("GetRecordByIdResponse")) {
           // Node n = doc.xpath_node("//gmd:MD_Metadata");
            Node n = XmlDoc.findNode(doc.getParsedXml(),"GetRecordByIdResponse","MD_Metadata");
            if (n == null) // likely an empty response...
                return doc;
            Document d = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().newDocument();
            Node nn = d.importNode(n, true);

            d.appendChild(nn);
            return new XmlDoc(doc.getOriginalXmlString(), d);
        }
        if (doc.getRootTagName().equals("GetRecordsResponse")) {
            Node n = XmlDoc.findNode(doc.getParsedXml(),"GetRecordsResponse","SearchResults","MD_Metadata");
            if (n == null) // likely an empty response...
                return doc;
            Document d = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().newDocument();
            Node nn = d.importNode(n, true);
            d.appendChild(nn);
            return new XmlDoc(doc.getOriginalXmlString(), d);
        }

        return doc;
    }

    private boolean isCapabilitiesDoc(XmlDoc doc) {
        try {
            CapabilitiesType type = capabilityDeterminer.determineCapabilitiesType(doc);
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    public boolean isCSWMetadataDocument(XmlDoc xmlDoc) {
        return xmlDoc.getRootTagName().equals("MD_Metadata");
    }

    public boolean isCSWServiceMetadataDocument(XmlDoc xmlDoc) throws XPathExpressionException {
        if (!isCSWMetadataDocument(xmlDoc))
            return false;
        //Node n = xmlDoc.xpath_node("/gmd:MD_Metadata/gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue");
        Node n = XmlDoc.findNode(xmlDoc.parsedXml,"MD_Metadata","hierarchyLevel","MD_ScopeCode");
        if (n == null)
            return false;
        n= n.getAttributes().getNamedItem("codeListValue");
        if (n == null)
            return false;
        if (n.getNodeValue().equals("service"))
            return true;
        return false;
    }

    public void determineUnderlyingServiceType(XmlDoc doc) {

    }
}
