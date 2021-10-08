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

import com.sun.org.apache.xpath.internal.CachedXPathAPI;
import net.geocat.service.downloadhelpers.PartialDownloadPredicateFactory;
import org.springframework.util.xml.SimpleNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class XmlDoc {

    String originalXmlString;
    Document parsedXml;

    String rootTagName;


    String rootNS;


    public XmlDoc(String xml) throws Exception {
        this(xml, parseXML(xml));
    }

    public XmlDoc(String xml, Document xmlDoc) throws Exception {
        originalXmlString = xml;
        parsedXml = xmlDoc;
        setup_XmlDoc();
    }

    public XmlDoc(XmlDoc doc) throws Exception {
        originalXmlString = doc.originalXmlString;
        parsedXml = doc.parsedXml;
        setup_XmlDoc();
    }

    //again, to be consistent
    public static Document parseXML(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        // factory.setIgnoringElementContentWhitespace(true);
        factory.setFeature("http://xml.org/sax/features/namespaces", true);
        factory.setFeature("http://xml.org/sax/features/validation", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);


        DocumentBuilder builder = factory.newDocumentBuilder();

        ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        Document doc = builder.parse(input);
        return doc;
    }

    //this is here so we can be consistent with the XML writing (otherwise the SHA2 will change)
    public static String writeXML(Node doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;

        transformer = tf.newTransformer();

        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        // transformer.setOutputProperty(OutputKeys.INDENT, "no");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        StringWriter writer = new StringWriter();

        transformer.transform(new DOMSource(doc), new StreamResult(writer));

        return writer.getBuffer().toString();
    }

    public static XPath createXPath() {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();


        SimpleNamespaceContext nsCtx = new SimpleNamespaceContext();
        xpath.setNamespaceContext(nsCtx);
        nsCtx.bindNamespaceUri("gmd", "http://www.isotc211.org/2005/gmd");
        nsCtx.bindNamespaceUri("csw", "http://www.opengis.net/cat/csw/2.0.2");
        nsCtx.bindNamespaceUri("gco", "http://www.isotc211.org/2005/gco");

        nsCtx.bindNamespaceUri("gmx", "http://www.isotc211.org/2005/gmx");
        nsCtx.bindNamespaceUri("srv", "http://www.isotc211.org/2005/srv");

        nsCtx.bindNamespaceUri("inspire_common", "http://inspire.ec.europa.eu/schemas/common/1.0");
        nsCtx.bindNamespaceUri("inspire_vs", "http://inspire.ec.europa.eu/schemas/inspire_vs/1.0");
        nsCtx.bindNamespaceUri("inspire_dls", "http://inspire.ec.europa.eu/schemas/inspire_dls/1.0");

        nsCtx.bindNamespaceUri("atom", "http://www.w3.org/2005/Atom");

        nsCtx.bindNamespaceUri("wms", "http://www.opengis.net/wms");
        nsCtx.bindNamespaceUri("wfs", "http://www.opengis.net/wfs/2.0");


        nsCtx.bindNamespaceUri("wmts", "http://www.opengis.net/wmts/1.0");
        nsCtx.bindNamespaceUri("xlink", "http://www.w3.org/1999/xlink");
        nsCtx.bindNamespaceUri("ows", "http://www.opengis.net/ows/1.1");


        return xpath;
    }

    public static org.w3c.dom.NodeList xpath_nodeset(Node doc, String xpathStr) throws XPathExpressionException {
        XPath xpath = createXPath();

        return ((org.w3c.dom.NodeList) xpath.evaluate(xpathStr, doc, XPathConstants.NODESET));
    }

    public static String xpath_attribute(Node doc, String xpathStr, String attname) throws XPathExpressionException {
        XPath xpath = createXPath();

        Node n = (org.w3c.dom.Node) xpath.evaluate(xpathStr, doc, XPathConstants.NODE);
        Node att = n.getAttributes().getNamedItem(attname);
        if (att == null)
            return null;
        String value = att.getNodeValue();
        return value;
    }

    public static Node xpath_node(Node doc, String xpathStr) throws XPathExpressionException {
        XPath xpath = createXPath();

        return ((org.w3c.dom.Node) xpath.evaluate(xpathStr, doc, XPathConstants.NODE));
    }

    //abc:def --> def
    public static String getLocalName(String fullName) {
        int idx = fullName.indexOf(':');
        if (idx == -1)
            return fullName;
        return fullName.substring(idx + 1);
    }

    public static void stripEmptyElements(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                if (child.getTextContent().trim().length() == 0) {
                    child.getParentNode().removeChild(child);
                    i--;
                }
            }
            stripEmptyElements(child);
        }
    }

    public void setup_XmlDoc() throws Exception {
        rootTagName = getFirstNode().getLocalName();
        rootNS = getFirstNode().getNamespaceURI();
    }

    public Node getFirstNode() throws  Exception {
        return xpath_node("/*");
    }

    public org.w3c.dom.NodeList xpath_nodeset(String xpathStr) throws XPathExpressionException {
        XPath xpath = createXPath();


        return ((org.w3c.dom.NodeList) xpath.evaluate(xpathStr, this.parsedXml, XPathConstants.NODESET));
    }

    public String xpath_attribute(String xpathStr, String attname) throws XPathExpressionException {
        XPath xpath = createXPath();

        String value = ((org.w3c.dom.Node) xpath.evaluate(xpathStr, this.parsedXml, XPathConstants.NODE)).getAttributes().getNamedItem(attname).getNodeValue();
        return value;
    }

    public Node xpath_node(String xpathStr) throws XPathExpressionException {
        XPath xpath = createXPath();

        return ((org.w3c.dom.Node) xpath.evaluate(xpathStr, this.parsedXml, XPathConstants.NODE));
    }

    public String computeSHA2(XmlDoc doc) throws Exception {
        Document d = doc.getParsedXml();
        //stripEmptyElements(d);  //this modifies document
        String s = XmlDoc.writeXML(d);
        String sha2 = computeSHA2(s);
        return sha2;
    }

    public String computeSHA2(String xml) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(xml.getBytes(StandardCharsets.UTF_8));
        String hexHash = javax.xml.bind.DatatypeConverter.printHexBinary(hash);
        return hexHash;
    }

    public String getRootTagName() {
        return rootTagName;
    }

    public String getRootNS() {
        return rootNS;
    }

    public String getOriginalXmlString() {
        return originalXmlString;
    }

    public Document getParsedXml() {
        return parsedXml;
    }

    @Override
    public String toString() {
        return "XmlDoc(rootTag="+getRootTagName()+")";
    }
}
