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

import net.geocat.service.capabilities.WMSCapabilitiesDatasetLinkExtractor;
import net.geocat.xml.helpers.OperatesOn;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class XmlServiceRecordDoc extends XmlMetadataDocument {
    //i.e. 'view', 'download', 'discovery' ...

    String serviceType;
    String serviceTypeVersion;
    List<OperatesOn> operatesOns = new ArrayList<>();


    public XmlServiceRecordDoc(XmlDoc doc) throws Exception {
        super(doc);
        setup_XmlServiceRecordDoc();
    }

    public String getServiceType() {
        return serviceType;
    }


    public void setup_XmlServiceRecordDoc() throws Exception {
        populateServiceType();
        populateOperatesOn();
    }

    private void populateOperatesOn() throws Exception {
      //  NodeList nl = xpath_nodeset("//srv:operatesOn");
        Node main = getFirstNode();
        Node secondary = WMSCapabilitiesDatasetLinkExtractor.findNode(main,"identificationInfo");
        if (secondary == null)
            return; //nothing to process
        secondary = WMSCapabilitiesDatasetLinkExtractor.findNode(secondary,"SV_ServiceIdentification");
        if (secondary == null)
            return; //nothing to process
        List<Node> nl = WMSCapabilitiesDatasetLinkExtractor.findNodes(secondary,"operatesOn");
        operatesOns = OperatesOn.create(nl);
    }

    public String translateServiceType(String xmlServiceType){
        if (xmlServiceType == null)
            return null;
        if (xmlServiceType.equalsIgnoreCase("ogc:wms"))
            return "view";
        else if (xmlServiceType.equalsIgnoreCase("ogc:wmts"))
            return "view";
        else if (xmlServiceType.equalsIgnoreCase("ogc:wfs"))
            return "download";


        return xmlServiceType;
    }

    public void populateServiceType() throws Exception {
       // Node n = xpath_node("//srv:serviceType/gco:LocalName");
       // Node n = xpath_node("//*[local-name()='serviceType']/*[local-name()='LocalName']");
        Node serviceId = XmlDoc.findNode(parsedXml,"MD_Metadata","identificationInfo","SV_ServiceIdentification");
        if (serviceId == null)
            return;
        Node n = XmlDoc.findNode(serviceId,"serviceType","LocalName");
        if (n != null) {
            serviceType = translateServiceType(n.getTextContent());
        }
       // n = xpath_node("//srv:serviceTypeVersion/gco:CharacterString");
    //    n = xpath_node("//*[local-name()='serviceTypeVersion']/*[local-name()='CharacterString']");
        n = XmlDoc.findNode(serviceId,"serviceTypeVersion","CharacterString");

        if (n != null)
            serviceTypeVersion = n.getTextContent();
    }

    public String getServiceTypeVersion() {
        return serviceTypeVersion;
    }

    public List<OperatesOn> getOperatesOns() {
        return operatesOns;
    }

    @Override
    public String toString() {
        String result =  "XmlServiceRecordDoc(fileIdentifier="+fileIdentifier;
        result += ", serviceType = "+serviceType;
        result += ")";
        return result;
    }

}
