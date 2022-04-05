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
import net.geocat.xml.helpers.CapabilitiesType;
import net.geocat.xml.helpers.WMSLayer;
import net.geocat.xml.helpers.WMSLayerBBox;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static net.geocat.service.capabilities.WMSCapabilitiesDatasetLinkExtractor.findNodes;
import static net.geocat.xml.XmlCapabilitiesAtom.attribute;

public class XmlCapabilitiesWMS extends XmlCapabilitiesDocument {

    static WMSCapabilitiesDatasetLinkExtractor wmsCapabilitiesDatasetLinkExtractor = new WMSCapabilitiesDatasetLinkExtractor();
    String getMapEndpoint;
    String versionNumber;
    List<String> supportedImageFormats;
    List<WMSLayer> wmsLayers;

    public XmlCapabilitiesWMS(XmlDoc doc) throws Exception {
        super(doc, CapabilitiesType.WMS);
        setup_XmlCapabilitiesWMS();
        setup_getversion();
        setup_getmapEndpoint();
        setup_supportedImageFormats();
        setup_layers();
    }

    public List<Node> findbboxes(Node layer){
        List<Node> bboxs = findNodes(layer,"BoundingBox");

            Node parentLayer = layer.getParentNode();
            if (parentLayer.getLocalName().equals("Layer")) {
                bboxs.addAll(findbboxes(parentLayer));
                return bboxs;
            }
            else {
                return bboxs;
            }


    }

    private void setup_layers() throws Exception {
        wmsLayers = new ArrayList<>();
        Node main = getFirstNode();
        Node cap = findNode(main, "Capability");
        List<Node> layers = findNodes_recurse(cap,"Layer");
        for(Node layer: layers) {

            Node nameNode = findNode(layer,"Name");
            if ( (nameNode == null) || (nameNode.getTextContent() ==null) || (nameNode.getTextContent().trim().isEmpty()))
                continue;
            String name = nameNode.getTextContent().trim();
            WMSLayer wmsLayer = new WMSLayer(name);
            wmsLayers.add(wmsLayer);
//            List<Node> bboxs = findNodes(layer,"BoundingBox");
//            if (bboxs.isEmpty()) {
//                Node parentLayer = layer.getParentNode();
//                if (parentLayer.getLocalName().equals("Layer")) {
//                    bboxs = findNodes(parentLayer,"BoundingBox");
//                }
//            }
            List<Node> bboxs = findbboxes(layer );
            for(Node bbox:bboxs) {
                //        <BoundingBox CRS="CRS:84" maxx="18.95663115922459" maxy="51.305916291382516" minx="12.024498725444078" miny="48.25578803534065"/>
                String crs = attribute(bbox,"CRS") ;
                if (crs == null)
                    crs = attribute(bbox,"SRS") ;
                String xmin = attribute(bbox,"minx") ;
                String ymin = attribute(bbox,"miny") ;
                String xmax = attribute(bbox,"maxx") ;
                String ymax = attribute(bbox,"maxy") ;
                WMSLayerBBox wmsLayerBBox = new WMSLayerBBox(crs, Double.valueOf(xmin),Double.valueOf(ymin),Double.valueOf(xmax),Double.valueOf(ymax));
                wmsLayer.getWmsLayerBBoxList().add(wmsLayerBBox);
            }
            int t=0;
        }
    }

    private void setup_supportedImageFormats() throws Exception {
        supportedImageFormats = new ArrayList<>();
        Node main = getFirstNode();
        Node getMap = findNode(main, Arrays.asList(new String[]{"Capability", "Request", "GetMap"}));
        if (getMap ==null)
            return;
        List<Node> formats = findNodes(getMap,"Format");
        for(Node format : formats) {
            String mime = format.getTextContent();
            if ( (mime!=null) && (!mime.trim().isEmpty()))
                supportedImageFormats.add(mime.trim());
        }
    }

    private void setup_getmapEndpoint() throws Exception {
        Node main = getFirstNode();
        Node op = findNode(main, Arrays.asList(new String[]{"Capability", "Request", "GetMap", "DCPType", "HTTP", "Get","OnlineResource"}));
        if (op == null)
            return;
        String url = attribute(op,"xlink:href");
        this.getMapEndpoint=url;
    }

    private void setup_getversion() throws Exception {
        Node main = getFirstNode();
        versionNumber = attribute(main,"version");
    }

    private void setup_XmlCapabilitiesWMS() throws Exception {
        datasetLinksList = wmsCapabilitiesDatasetLinkExtractor.findLinks(this);
    }

    //===

    public boolean supportsFormat(String format){
        return supportedImageFormats.stream()
                .anyMatch(x->x.equalsIgnoreCase(format));
    }

    public WMSLayer findWMSLayer(String layername) {
        Optional<WMSLayer> result = wmsLayers.stream()
                .filter(x->x.getName().equals(layername))
                .findFirst();
        if (result.isPresent())
            return result.get();
        return null;
    }

    //===

    public String getGetMapEndpoint() {
        return getMapEndpoint;
    }

    public void setGetMapEndpoint(String getMapEndpoint) {
        this.getMapEndpoint = getMapEndpoint;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public List<String> getSupportedImageFormats() {
        return supportedImageFormats;
    }

    public void setSupportedImageFormats(List<String> supportedImageFormats) {
        this.supportedImageFormats = supportedImageFormats;
    }

    @Override
    public String toString() {
        String result =  "XmlCapabilitiesWMS(has service reference URL="+( (getMetadataUrlRaw() !=null) && (!getMetadataUrlRaw().isEmpty())) ;
        result += ", number of Dataset links = "+getDatasetLinksList().size();
        result += ")";
        return result;
    }
}
