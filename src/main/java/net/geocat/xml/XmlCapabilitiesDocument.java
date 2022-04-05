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

import net.geocat.service.capabilities.DatasetLink;
import net.geocat.xml.helpers.CapabilitiesType;
import org.w3c.dom.Node;
import net.geocat.database.linkchecker.entities.InspireSpatialDatasetIdentifier;

import javax.xml.xpath.XPathExpressionException;

import static net.geocat.service.capabilities.WMSCapabilitiesDatasetLinkExtractor.findNodes;
import static net.geocat.xml.XmlStringTools.getNodeTextValue;

import java.util.ArrayList;
import java.util.List;

public class XmlCapabilitiesDocument extends XmlDoc {

    boolean hasExtendedCapabilities;
    String metadataUrlRaw;
    CapabilitiesType capabilitiesType;

    List<DatasetLink> datasetLinksList;
    List<InspireSpatialDatasetIdentifier> inspireDatasetLinks;

    String defaultLang = "eng";

    public XmlCapabilitiesDocument(XmlDoc doc, CapabilitiesType type) throws Exception {
        super(doc);
        datasetLinksList = new ArrayList<>();
        inspireDatasetLinks = new ArrayList<>();
        this.capabilitiesType = type;
        setup_XmlCapabilitiesDocument();
        if (hasExtendedCapabilities) {
            setup_spatialdatasetidentifiers();
            setup_lang();
        }
    }

    private void setup_lang() throws XPathExpressionException {
        Node extendedCap = xpath_node("//*[local-name()='ExtendedCapabilities']");
        if (extendedCap ==null)
            return;
        Node langNode = findNode(extendedCap,"ExtendedCapabilities","SupportedLanguages","DefaultLanguage","Language");
        if (langNode ==null)
           return;

        String lang = getNodeTextValue(langNode);
        if (lang != null)
            defaultLang = lang;
    }

    public static XmlCapabilitiesDocument create(XmlDoc doc, CapabilitiesType type) throws Exception {
        switch (type) {
            case WFS:
                return new XmlCapabilitiesWFS(doc);
            case WMS:
                return new XmlCapabilitiesWMS(doc);
            case WMTS:
                return new XmlCapabilitiesWMTS(doc);
            case Atom:
                return new XmlCapabilitiesAtom(doc);
            case CSW:
                return new XmlCapabilitiesCSW(doc);
        }
        throw new Exception("XmlCapabilitiesDocument - unknown type");
    }

    private void setup_XmlCapabilitiesDocument() throws Exception {
        setupExtendedCap();
    }

    private void setup_spatialdatasetidentifiers() throws Exception {
        Node extendedCap = xpath_node("//*[local-name()='ExtendedCapabilities']");
        if (extendedCap ==null)
            return;
        Node extendedCap2 = findNode(extendedCap,"ExtendedCapabilities");
        if (extendedCap2 !=null)
            extendedCap = extendedCap2;

        List<Node> sdis = findNodes(extendedCap,"SpatialDataSetIdentifier");
        for (Node sdi : sdis) {
            // metadataURL
            String metadataURL = null;
            Node metadataURLNode = sdi.getAttributes().getNamedItem("metadataURL");
            if ((metadataURLNode !=null ) && (metadataURLNode.getTextContent() !=null) && (!metadataURLNode.getTextContent().trim().isEmpty()))
                metadataURL = metadataURLNode.getTextContent().trim();

            //Code
            String code = null;
            Node codeNode = findNode(sdi,"Code");
            if ((codeNode !=null ) && (codeNode.getTextContent() !=null) && (!codeNode.getTextContent().trim().isEmpty()))
                code = codeNode.getTextContent().trim();

            //Namespace
            String namespace = null;
            Node namespaceNode = findNode(sdi,"Namespace");
            if ((namespaceNode !=null ) && (namespaceNode.getTextContent() !=null) && (!namespaceNode.getTextContent().trim().isEmpty()))
                namespace = namespaceNode.getTextContent().trim();

            if ( (code !=null)  )
                inspireDatasetLinks.add(new InspireSpatialDatasetIdentifier(metadataURL,code,namespace));
        }
    }


   public Node attemptToFindExtended(){
        Node n = findNode(parsedXml,"WFS_Capabilities","OperationsMetadata","ExtendedCapabilities");
        if (n!=null)
            return n;

       n = findNode(parsedXml,"WMS_Capabilities","Capability","ExtendedCapabilities");
       if (n!=null)
           return n;

      return null;
   }

    private void setupExtendedCap() throws Exception {
        //we use this notation because of issues with NS accross servers
        Node n=null;
        Node nn=null;
        Node nnn=null;

        n = attemptToFindExtended();
        if (n == null) {
            n = xpath_node("//*[local-name()='ExtendedCapabilities']");
            if (n == null) {
                nn = xpath_node("//*[local-name()='feed']/*[local-name()='link'][@rel=\"describedby\"]/@href");
                if (nn == n)
                    nnn = xpath_node("//wmts:ServiceMetadataURL/@xlink:href");
            }
        }

        hasExtendedCapabilities = (n != null) || (nn != null) || (nnn != null);
        if (!hasExtendedCapabilities)
            return;

        if (n != null) {
            setup_extendedcap(n);
            return;
        }

        if (nn != null) {
            setup_inspire_atom(nn);
            return;
        }

        if (nnn !=null) {
            setup_wmts_extended(nnn);
            return;
        }
    }

    private void setup_wmts_extended(Node serviceMetadataURL) {
        if (serviceMetadataURL != null) {
            this.metadataUrlRaw = serviceMetadataURL.getTextContent().trim();
        }
    }

    private void setup_extendedcap(Node n) throws Exception {
        if (n != null) {
            //Node nn = XmlDoc.xpath_node(n, "//inspire_common:MetadataUrl/inspire_common:URL");
            Node nn = XmlDoc.findNode(n, "ExtendedCapabilities","MetadataUrl","URL");
            if (nn ==null)
                nn = XmlDoc.findNode(n, "MetadataUrl","URL");
            if (nn != null)
                this.metadataUrlRaw = nn.getTextContent().trim();
        }
    }

    private void setup_inspire_atom(Node n) throws Exception {
        if (n != null) {
            this.metadataUrlRaw = n.getTextContent().trim();
        }
    }

    public boolean isHasExtendedCapabilities() {
        return hasExtendedCapabilities;
    }

    public void setHasExtendedCapabilities(boolean hasExtendedCapabilities) {
        this.hasExtendedCapabilities = hasExtendedCapabilities;
    }


    public String getMetadataUrlRaw() {
        return metadataUrlRaw;
    }

    public void setMetadataUrlRaw(String metadataUrlRaw) {
        this.metadataUrlRaw = metadataUrlRaw;
    }

    public CapabilitiesType getCapabilitiesType() {
        return capabilitiesType;
    }

    public void setCapabilitiesType(CapabilitiesType capabilitiesType) {
        this.capabilitiesType = capabilitiesType;
    }

    public List<DatasetLink> getDatasetLinksList() {
        return datasetLinksList;
    }

    public void setDatasetLinksList(List<DatasetLink> datasetLinksList) {
        this.datasetLinksList = datasetLinksList;
    }

    public List<InspireSpatialDatasetIdentifier> getInspireDatasetLinks() {
        return inspireDatasetLinks;
    }

    public void setInspireDatasetLinks(List<InspireSpatialDatasetIdentifier> inspireDatasetLinks) {
        this.inspireDatasetLinks = inspireDatasetLinks;
    }

    public String getDefaultLang() {
        return defaultLang;
    }

    public void setDefaultLang(String defaultLang) {
        this.defaultLang = defaultLang;
    }

    @Override
    public String toString() {
        String result =  "XmlCapabilities (has service reference URL="+( (getMetadataUrlRaw() !=null) && (!getMetadataUrlRaw().isEmpty())) ;
        result += ", number of Dataset links = "+getDatasetLinksList().size();
        result += ")";
        return result;
    }
}
