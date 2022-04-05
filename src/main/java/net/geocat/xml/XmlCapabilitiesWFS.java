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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.geocat.service.capabilities.WMSCapabilitiesDatasetLinkExtractor.findNodes;
import static net.geocat.xml.XmlCapabilitiesAtom.attribute;
import static net.geocat.xml.XmlStringTools.getNodeTextValue;

public class XmlCapabilitiesWFS extends XmlCapabilitiesDocument {

    String getFeatureEndpoint; //not all have a getfeature endpoint
    String versionNumber;
    List<String> SRSs;


    public XmlCapabilitiesWFS(XmlDoc doc) throws Exception {
        super(doc, CapabilitiesType.WFS);
        setup_XmlCapabilitiesWFS();
        setup_getfeature20();
        if (getFeatureEndpoint == null)
            setup_getfeature11();
        setup_getversion();
        setup_srs_main();
        setup_srs_layer();
    }

    // try to find srs info inside featuretypes
    private void setup_srs_layer() throws Exception {
        List<String> allcrses = new ArrayList<>();
        Node main = getFirstNode();
        Node secondary = findNode(main,"FeatureTypeList");
        if (secondary == null)
            return; // no feature types
        List<Node> nl = findNodes(secondary,"FeatureType");

        for(Node FTnode : nl) {
            String defaultCRS = getNodeTextValue(findNode(FTnode,"DefaultCRS"));
            if (defaultCRS != null)
                allcrses.add(defaultCRS);
            List<Node> otherCRSNodes = findNodes(FTnode,"OtherCRS");
            for(Node otherCRSNode : otherCRSNodes){
                String otherCR = getNodeTextValue(otherCRSNode);
                if (otherCR != null)
                    allcrses.add(otherCR);
            }
        }
        List uniqueCRSs = allcrses.stream().distinct().collect(Collectors.toList());
        SRSs.addAll(uniqueCRSs);
        SRSs = SRSs.stream().distinct().collect(Collectors.toList());

    }

    //from the operationsmetadata/Parameter["srsName"]
    private void setup_srs_main() throws Exception {
        SRSs = new ArrayList<>();
        Node main = getFirstNode();
        Node op = findNode(main,"OperationsMetadata");
        if (op ==null)
            return;
        List<Node> ops = findNodes(op,"Parameter");
        for(Node o : ops){
            String name = attribute(o,"name");
            if ( (name == null) || (!name.equalsIgnoreCase("srsName")))
                continue;
            Node allowedValuesNode = findNode(o,"AllowedValues");
            if (allowedValuesNode!=null) {
                List<Node> allowedValuesNodes = findNodes(allowedValuesNode, "Value");
                for (Node allowedValue : allowedValuesNodes) {
                    String val = getNodeTextValue(allowedValue);
                    if (val !=null)
                        SRSs.add(val);
                }
            }
        }
    }


    private void setup_getversion() throws Exception {
        Node main = getFirstNode();
        versionNumber = attribute(main,"version");
    }

    private void setup_getfeature11() throws Exception {
        Node main = getFirstNode();
        Node op = findNode(main, Arrays.asList(new String[]{"Capability", "Request", "GetFeature", "DCPType", "HTTP", "Get"}));
        if (op == null)
            return;
        String url = attribute(op,"onlineResource");
        this.getFeatureEndpoint=url;
    }

    private void setup_getfeature20() throws Exception {
        Node main = getFirstNode();
        Node op = findNode(main,"OperationsMetadata");
        if (op ==null)
            return;
        List<Node> ops = findNodes(op,"Operation");
        for(Node o : ops){
            String name = attribute(o,"name");
            if ( (name == null) || (!name.equalsIgnoreCase("GetFeature")))
                continue;
            Node getfeatureop = findNode(o,"DCP","HTTP","Get");
            if (getfeatureop == null)
                continue;
            getFeatureEndpoint = attribute(getfeatureop,"xlink:href");
        }

    }

    private String getLayerMetadataURL_inspire(Node spatialDatasetIdentifier) throws  Exception {
        if (spatialDatasetIdentifier == null)
            return null;

        Node metadataURL = spatialDatasetIdentifier.getAttributes().getNamedItem("metadataURL");

        if ( (metadataURL ==null) || (metadataURL.getTextContent() == null ) )
            return null;

        String url = metadataURL.getTextContent().trim();
        if (url.isEmpty())
            return null;
        return url;

    }


    private void setup_XmlCapabilitiesWFS() throws Exception {
        setup_XmlCapabilitiesWFS_noinspire();
    }


    protected List<String> findMetadataURLs(Node layer) throws Exception {

        List<Node> metadataURLs = findAllNodes(layer, "MetadataURL");

        return metadataURLs.stream()
                .map(x->x.getAttributes().getNamedItem("xlink:href"))
                .filter(x->x !=null && (x.getTextContent() != null) && (!x.getTextContent().trim().isEmpty()))
                .map(x->x.getTextContent().trim())
                .collect(Collectors.toList());
    }



    //alternative way - direct links
    private void setup_XmlCapabilitiesWFS_noinspire() throws Exception {

        Node main = getFirstNode();
        Node secondary = findNode(main,"FeatureTypeList");
        if (secondary == null)
            return; // no feature types
        List<Node> nl = findNodes(secondary,"FeatureType");

        for(Node FTnode : nl) {
            String name = null;
            Node nameNode = findNode(FTnode,"Name");
            if (nameNode !=null)
                name = nameNode.getTextContent();
            if (name !=null)
                name = name.trim();

            List<String> metadataURLs = findMetadataURLs(FTnode);

            if  ( (metadataURLs == null) || (metadataURLs.isEmpty()) ) {
                DatasetLink datasetLink = new DatasetLink(null,null);
                datasetLink.setOgcLayerName(name);
                datasetLinksList.add(datasetLink);
            }
            for (String url : metadataURLs){
                DatasetLink datasetLink = new DatasetLink(null,url);
                datasetLink.setOgcLayerName(name);
                datasetLinksList.add(datasetLink);
            }

        }
        // datasetLinksList = WMSCapabilitiesDatasetLinkExtractor.unique(datasetLinksList);

    }

    //--


    public List<String> getSRSs() {
        return SRSs;
    }

    public void setSRSs(List<String> SRSs) {
        this.SRSs = SRSs;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getGetFeatureEndpoint() {
        return getFeatureEndpoint;
    }

    public void setGetFeatureEndpoint(String getFeatureEndpoint) {
        this.getFeatureEndpoint = getFeatureEndpoint;
    }


    //--



    @Override
    public String toString() {
        String result =  "XmlCapabilitiesWFS(has service reference URL="+( (getMetadataUrlRaw() !=null) && (!getMetadataUrlRaw().isEmpty())) ;
        result += ", number of Dataset links = "+getDatasetLinksList().size();
        result += ")";
        return result;
    }
}