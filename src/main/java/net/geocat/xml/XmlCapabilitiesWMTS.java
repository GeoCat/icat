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

import net.geocat.service.capabilities.WMTSCapabilitiesDatasetLinkExtractor;
import net.geocat.xml.helpers.CapabilitiesType;
import net.geocat.xml.helpers.TileMatrix;
import net.geocat.xml.helpers.TileMatrixSet;
import net.geocat.xml.helpers.WMTSLayer;
import net.geocat.xml.helpers.WMTSTile;
import net.geocat.xml.helpers.WMTSTileMatrixSetLimit;
import net.geocat.xml.helpers.WMTSTileMatrixSetLink;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.geocat.service.capabilities.WMSCapabilitiesDatasetLinkExtractor.findNodes;
import static net.geocat.xml.XmlCapabilitiesAtom.attribute;
import static net.geocat.xml.XmlStringTools.getNodeTextValue;

public class XmlCapabilitiesWMTS extends XmlCapabilitiesDocument {

    String getTileEndpoint;
    String versionNumber;
    List<TileMatrixSet> tileMatrixSets;
    List<WMTSLayer> wmtsLayers;


    static WMTSCapabilitiesDatasetLinkExtractor wmtsCapabilitiesDatasetLinkExtractor = new WMTSCapabilitiesDatasetLinkExtractor();


    public XmlCapabilitiesWMTS(XmlDoc doc) throws Exception {
        super(doc, CapabilitiesType.WMTS);
        setup_XmlCapabilitiesWMTS();
        setup_getversion();
        setup_getmapTilepoint();
        setup_matrixSets();
        setup_layers();
    }

    public WMTSLayer findLayer(String layerIdentity){
        Optional<WMTSLayer> layer = wmtsLayers.stream()
                .filter(x->x.getIdentifier().equals(layerIdentity))
                .findFirst();
        if (!layer.isPresent())
            return null;
        return layer.get();
    }

    public WMTSTile sampleTile(String layerIdentity,String matrix){
        WMTSLayer layer = findLayer(layerIdentity);
        if (layer ==null)
            return null;
        if (layer.getTileMatrixSetLinks().isEmpty())
            return null;
        WMTSTileMatrixSetLink link = layer.getTileMatrixSetLinks().get(0);
        TileMatrixSet tileMatrixSet = findTileMatrixSet(link.getTileMatrixSetName());
        if (tileMatrixSet == null)
            return null;

        //not limited
        if (link.getTileMatrixSetLimits().isEmpty()) {
            if (tileMatrixSet.getTileMatrices().isEmpty())
                return null;
            return new WMTSTile(link.getTileMatrixSetName(),tileMatrixSet.getTileMatrices().get(0).getIdentifier(),0,0);
        }
        //limited
        WMTSTileMatrixSetLimit limit = link.getTileMatrixSetLimits().get(0);
        if (matrix != null) {
            Optional<WMTSTileMatrixSetLimit> _limit =  link.getTileMatrixSetLimits().stream()
                    .filter(x->x.getTileMatrixName().equals(matrix))
                    .findFirst();
            if (_limit.isPresent())
                limit = _limit.get();
        }
        return new WMTSTile(link.getTileMatrixSetName(),limit.getTileMatrixName(),limit.getMinTileRow(),limit.getMinTileCol());
    }

    public TileMatrixSet findTileMatrixSet(String identity){
        Optional<TileMatrixSet> result  = this.tileMatrixSets.stream()
                .filter(x->x.getIdentifier().equals(identity)).findFirst();
        if (result.isPresent())
            return result.get();
        return null;
    }

    private void setup_layers() throws Exception {
        wmtsLayers = new ArrayList<>();
        Node main = getFirstNode();
        Node contents = findNode(main,"Contents");
        if (contents == null)
            return;
        List<Node> layers = findNodes(contents,"Layer");
        for (Node layer : layers) {
             String identifier =getNodeTextValue(findNode(layer,"Identifier"));
             String title =getNodeTextValue(findNode(layer,"Title"));
             List<String> formats = new ArrayList<>();
             List<Node> formatNodes = findNodes(layer,"Format");
             for(Node formatNode:formatNodes) {
                 String format = getNodeTextValue(formatNode);
                 if (format == null)
                     continue;
                 formats.add(format);
             }
            List<WMTSTileMatrixSetLink> links = parseMatrixLimits(layer);
            WMTSLayer wmtsLayer = new WMTSLayer(identifier,title);
            wmtsLayer.setFormats(formats);
            wmtsLayer.setTileMatrixSetLinks(links);
            wmtsLayers.add(wmtsLayer);
        }
    }

    private List<WMTSTileMatrixSetLink> parseMatrixLimits(Node layer) {
        List<WMTSTileMatrixSetLink> result = new ArrayList<>();
        List<Node> linkNodes = findNodes(layer,"TileMatrixSetLink");
        for(Node linkNode:linkNodes) {
            String matrixName = getNodeTextValue(findNode(linkNode,"TileMatrixSet"));
            if (matrixName == null)
                continue;
            Node limitsNode =  findNode(linkNode,"TileMatrixSetLimits");
            List<WMTSTileMatrixSetLimit> wmtsTileMatrixSetLimits = new ArrayList<>();
            if (limitsNode != null) {
                List<Node> limitNodes = findNodes(limitsNode,"TileMatrixLimits");
                for (Node limitNode : limitNodes) {
                    String tileMatrixName = getNodeTextValue(findNode(limitNode,"TileMatrix"));
                    String minTileRow = getNodeTextValue(findNode(limitNode,"MinTileRow"));
                    String maxTileRow = getNodeTextValue(findNode(limitNode,"MaxTileRow"));
                    String minTileCol = getNodeTextValue(findNode(limitNode,"MinTileCol"));
                    String maxTileCol = getNodeTextValue(findNode(limitNode,"MaxTileCol"));

                    WMTSTileMatrixSetLimit wmtsTileMatrixSetLimit = new WMTSTileMatrixSetLimit(tileMatrixName,
                            toInt(minTileRow), toInt(maxTileRow),toInt(minTileCol), toInt(maxTileCol) );
                    wmtsTileMatrixSetLimits.add(wmtsTileMatrixSetLimit);
                }
            }

            WMTSTileMatrixSetLink wmtsTileMatrixSetLink = new WMTSTileMatrixSetLink(matrixName);
            result.add(wmtsTileMatrixSetLink);
            wmtsTileMatrixSetLink.setTileMatrixSetLimits(wmtsTileMatrixSetLimits);
        }
        return result;
    }

    private void setup_matrixSets() throws Exception {
        tileMatrixSets = new ArrayList<>();

        Node main = getFirstNode();
        Node contents = findNode(main,"Contents");
        if (contents ==null)
            return ;
        List<Node> matrixSets = findNodes(contents,"TileMatrixSet");
        for(Node matrixSet : matrixSets) {
            Node identifierNode = findNode(matrixSet,"Identifier");

            String identifier =getNodeTextValue(identifierNode);
            if (identifier == null)
                continue;

            Node SupportedCRSNode = findNode(matrixSet,"SupportedCRS");
            String crs = getNodeTextValue(SupportedCRSNode);

            TileMatrixSet set = new TileMatrixSet(identifier,crs);
            tileMatrixSets.add(set);

            List<Node> tileMatrixs = findNodes(matrixSet,"TileMatrix");
            for (Node tileMatrix : tileMatrixs) {
                String identifier2 =getNodeTextValue(findNode(tileMatrix,"Identifier"));

                String TileWidth = getNodeTextValue(findNode(tileMatrix,"TileWidth"));
                String TileHeight = getNodeTextValue(findNode(tileMatrix,"TileHeight"));
                String MatrixWidth = getNodeTextValue(findNode(tileMatrix,"MatrixWidth"));
                String MatrixHeight = getNodeTextValue(findNode(tileMatrix,"MatrixHeight"));

                TileMatrix matrix = new TileMatrix(identifier2,toInt(TileWidth),toInt(TileHeight),toInt(MatrixWidth),toInt(MatrixHeight));
                set.getTileMatrices().add(matrix);
             }
        }
    }

    public static int toInt(String i){
        try{
            return Integer.parseInt(i);
        }
        catch (Exception e){
            return 0;
        }
    }

    private void setup_getmapTilepoint() throws Exception {
        Node main = getFirstNode();
        Node op = findNode(main,"OperationsMetadata");
        if (op ==null)
            return;
        List<Node> ops = findNodes(op,"Operation");
        for(Node o : ops){
            String name = attribute(o,"name");
            if ( (name == null) || (!name.equalsIgnoreCase("GetTile")))
                continue;
            Node getfeatureop = findNode(o,"DCP","HTTP","Get");
            if (getfeatureop == null)
                continue;
            getTileEndpoint = attribute(getfeatureop,"xlink:href");
        }
    }

    private void setup_getversion() throws Exception {
        Node main = getFirstNode();
        versionNumber = attribute(main,"version");
    }


    private void setup_XmlCapabilitiesWMTS() throws Exception {
        datasetLinksList = wmtsCapabilitiesDatasetLinkExtractor.findLinks(this);
    }

    //---


    public List<TileMatrixSet> getTileMatrixSets() {
        return tileMatrixSets;
    }

    public void setTileMatrixSets(List<TileMatrixSet> tileMatrixSets) {
        this.tileMatrixSets = tileMatrixSets;
    }

    public String getGetTileEndpoint() {
        return getTileEndpoint;
    }

    public void setGetTileEndpoint(String getTileEndpoint) {
        this.getTileEndpoint = getTileEndpoint;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }


    public List<WMTSLayer> getWmtsLayers() {
        return wmtsLayers;
    }

    public void setWmtsLayers(List<WMTSLayer> wmtsLayers) {
        this.wmtsLayers = wmtsLayers;
    }

    //--
    @Override
    public String toString() {
        String result =  "XmlCapabilitiesWMTS(has service reference URL="+( (getMetadataUrlRaw() !=null) && (!getMetadataUrlRaw().isEmpty())) ;
        result += ", number of Dataset links = "+getDatasetLinksList().size();
        result += ")";
        return result;
    }

 }
