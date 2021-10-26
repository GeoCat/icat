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

import net.geocat.xml.helpers.OnlineResource;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.List;

public class XmlMetadataDocument extends XmlDoc {

    //i.e. service/dataset etc...
    MetadataDocumentType metadataDocumentType;
    String fileIdentifier;
    List<OnlineResource> transferOptions = new ArrayList<>();
    List<OnlineResource> connectPoints = new ArrayList<>();
    String title;


    public XmlMetadataDocument(XmlDoc doc) throws Exception {
        super(doc);
        if (!getRootTagName().equals("MD_Metadata"))
            throw new Exception("XmlMetadataDocument -- root node should be MD_Metadata");
        setup_XmlMetadataDocument();
    }

    public void setup_XmlMetadataDocument() throws Exception {
        setupTitle();

        Node n = xpath_node("/gmd:MD_Metadata/gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue");
        String _metadataDocumentType = "";

        // gmd:hierarchyLevel is optional in xsd, although required in INSPIRE.
        // But we can rely on all metadata being valid INSPIRE metadata.
        if (n != null) {
            _metadataDocumentType = n.getTextContent();
        }

        metadataDocumentType = determineMetadataDocumentType(_metadataDocumentType);

        n = xpath_node("/gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString");
        fileIdentifier = n.getTextContent();


        NodeList nl = xpath_nodeset("//gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource");
        transferOptions = OnlineResource.create(nl);

        nl = xpath_nodeset("//srv:containsOperations/srv:SV_OperationMetadata");
        connectPoints = OnlineResource.create(nl);
    }

    private void setupTitle() throws XPathExpressionException {
           Node n =  xpath_node("/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
           if ( (n==null) || (n.getTextContent().trim().isEmpty()))
                  n= xpath_node("/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");

           if ( (n !=null) && (!n.getTextContent().trim().isEmpty()) )
               setTitle(n.getTextContent().trim());

    }

    public MetadataDocumentType determineMetadataDocumentType(String xmlText) {
        if (xmlText.equalsIgnoreCase("service"))
            return MetadataDocumentType.Service;
        if (xmlText.equalsIgnoreCase("dataset"))
            return MetadataDocumentType.Dataset;
        if (xmlText.equalsIgnoreCase("discovery"))
            return MetadataDocumentType.Discovery;
        if (xmlText.equalsIgnoreCase("series"))
            return MetadataDocumentType.Series;

        return MetadataDocumentType.UNKNOWN;
    }

    public MetadataDocumentType getMetadataDocumentType() {
        return metadataDocumentType;
    }

    public String getFileIdentifier() {
        return fileIdentifier;
    }

    public List<OnlineResource> getTransferOptions() {
        return transferOptions;
    }

    public List<OnlineResource> getConnectPoints() {
        return connectPoints;
    }

    public void setMetadataDocumentType(MetadataDocumentType metadataDocumentType) {
        this.metadataDocumentType = metadataDocumentType;
    }

    public void setFileIdentifier(String fileIdentifier) {
        this.fileIdentifier = fileIdentifier;
    }


    public void setTransferOptions(List<OnlineResource> transferOptions) {
        this.transferOptions = transferOptions;
    }

    public void setConnectPoints(List<OnlineResource> connectPoints) {
        this.connectPoints = connectPoints;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        String result =  "XmlMetadataDocument(fileIdentifier="+fileIdentifier;
        result += ", metadataDocumentType = "+metadataDocumentType;
        result += ", title="+title;
        result += ")";
        return result;
    }
}
