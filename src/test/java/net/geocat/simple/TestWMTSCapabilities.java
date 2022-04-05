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

package net.geocat.simple;

import net.geocat.service.capabilities.WMSCapabilitiesDatasetLinkExtractor;
import net.geocat.service.capabilities.WMTSCapabilitiesDatasetLinkExtractor;
import net.geocat.xml.XmlCapabilitiesAtom;
import net.geocat.xml.XmlCapabilitiesWMTS;
import net.geocat.xml.XmlDocumentFactory;
import net.geocat.xml.helpers.CapabilityDeterminer;
import net.geocat.xml.helpers.WMTSTile;
import org.junit.Before;
import org.junit.Test;

import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TestWMTSCapabilities {

    XmlDocumentFactory xmlDocumentFactory;
    WMTSCapabilitiesDatasetLinkExtractor wmtsCapabilitiesDatasetLinkExtractor;
//
//    @Test
//    public void test_111() throws  Exception {
//        XmlCapabilitiesWMTS xmlCapabilitiesDocument = read("wmts_111.xml");
//        assertNotNull(xmlCapabilitiesDocument);
//    }

    @Test
    public void test_limitMatrix() throws  Exception {
        XmlCapabilitiesWMTS xmlCapabilitiesDocument = read("wmts_reducedMatrix.xml");
        assertNotNull(xmlCapabilitiesDocument);

        assertEquals("http://localhost:8080/geoserver", xmlCapabilitiesDocument.getGetTileEndpoint());

        assertEquals(1,xmlCapabilitiesDocument.getTileMatrixSets().size());
        assertEquals("MATRIXNAME",xmlCapabilitiesDocument.getTileMatrixSets().get(0).getIdentifier());
        assertEquals("urn:ogc:def:crs:EPSG::1234",xmlCapabilitiesDocument.getTileMatrixSets().get(0).getCRS());
        assertEquals(5,xmlCapabilitiesDocument.getTileMatrixSets().get(0).getTileMatrices().size());

        assertEquals("MATRIXNAME:0",xmlCapabilitiesDocument.getTileMatrixSets().get(0).getTileMatrices().get(0).getIdentifier());
        assertEquals(256,xmlCapabilitiesDocument.getTileMatrixSets().get(0).getTileMatrices().get(0).getHeight());
        assertEquals(256,xmlCapabilitiesDocument.getTileMatrixSets().get(0).getTileMatrices().get(0).getWidth());
        assertEquals(1,xmlCapabilitiesDocument.getTileMatrixSets().get(0).getTileMatrices().get(0).getMatrixHeight());
        assertEquals(1,xmlCapabilitiesDocument.getTileMatrixSets().get(0).getTileMatrices().get(0).getMatrixWidth());

        assertEquals("MATRIXNAME:1",xmlCapabilitiesDocument.getTileMatrixSets().get(0).getTileMatrices().get(1).getIdentifier());
        assertEquals(256,xmlCapabilitiesDocument.getTileMatrixSets().get(0).getTileMatrices().get(1).getHeight());
        assertEquals(256,xmlCapabilitiesDocument.getTileMatrixSets().get(0).getTileMatrices().get(1).getWidth());
        assertEquals(2,xmlCapabilitiesDocument.getTileMatrixSets().get(0).getTileMatrices().get(1).getMatrixHeight());
        assertEquals(2,xmlCapabilitiesDocument.getTileMatrixSets().get(0).getTileMatrices().get(1).getMatrixWidth());

                //don't test other 3 (:2 :3 :4)

        assertEquals(1,xmlCapabilitiesDocument.getWmtsLayers().size());
        assertEquals("layer1ID",xmlCapabilitiesDocument.getWmtsLayers().get(0).getIdentifier());
        assertEquals("title",xmlCapabilitiesDocument.getWmtsLayers().get(0).getTitle());
        assertEquals(1,xmlCapabilitiesDocument.getWmtsLayers().get(0).getFormats().size());
        assertEquals("image/png",xmlCapabilitiesDocument.getWmtsLayers().get(0).getFormats().get(0));

        assertEquals(1,xmlCapabilitiesDocument.getWmtsLayers().get(0).getTileMatrixSetLinks().size());
        assertEquals("MATRIXNAME",xmlCapabilitiesDocument.getWmtsLayers().get(0).getTileMatrixSetLinks().get(0).getTileMatrixSetName());
        assertEquals(4,xmlCapabilitiesDocument.getWmtsLayers().get(0).getTileMatrixSetLinks().get(0).getTileMatrixSetLimits().size());

        assertEquals("MATRIXNAME:0",xmlCapabilitiesDocument.getWmtsLayers().get(0).getTileMatrixSetLinks().get(0).getTileMatrixSetLimits().get(0).getTileMatrixName());
        assertEquals(0,xmlCapabilitiesDocument.getWmtsLayers().get(0).getTileMatrixSetLinks().get(0).getTileMatrixSetLimits().get(0).getMinTileRow());
        assertEquals(0,xmlCapabilitiesDocument.getWmtsLayers().get(0).getTileMatrixSetLinks().get(0).getTileMatrixSetLimits().get(0).getMaxTileRow());
        assertEquals(0,xmlCapabilitiesDocument.getWmtsLayers().get(0).getTileMatrixSetLinks().get(0).getTileMatrixSetLimits().get(0).getMinTileCol());
        assertEquals(0,xmlCapabilitiesDocument.getWmtsLayers().get(0).getTileMatrixSetLinks().get(0).getTileMatrixSetLimits().get(0).getMaxTileCol());

        assertEquals("MATRIXNAME:3",xmlCapabilitiesDocument.getWmtsLayers().get(0).getTileMatrixSetLinks().get(0).getTileMatrixSetLimits().get(3).getTileMatrixName());
        assertEquals(5,xmlCapabilitiesDocument.getWmtsLayers().get(0).getTileMatrixSetLinks().get(0).getTileMatrixSetLimits().get(3).getMinTileRow());
        assertEquals(6,xmlCapabilitiesDocument.getWmtsLayers().get(0).getTileMatrixSetLinks().get(0).getTileMatrixSetLimits().get(3).getMaxTileRow());
        assertEquals(3,xmlCapabilitiesDocument.getWmtsLayers().get(0).getTileMatrixSetLinks().get(0).getTileMatrixSetLimits().get(3).getMinTileCol());
        assertEquals(4,xmlCapabilitiesDocument.getWmtsLayers().get(0).getTileMatrixSetLinks().get(0).getTileMatrixSetLimits().get(3).getMaxTileCol());

        WMTSTile tile = xmlCapabilitiesDocument.sampleTile("layer1ID",null);
        assertNotNull(tile);
        assertEquals("MATRIXNAME",tile.getTileMatrixSetName());
        assertEquals("MATRIXNAME:0",tile.getTileMatrixName());
        assertEquals(0,tile.getRow());
        assertEquals(0,tile.getCol());

         tile = xmlCapabilitiesDocument.sampleTile("layer1ID","MATRIXNAME:3");
        assertNotNull(tile);
        assertEquals("MATRIXNAME",tile.getTileMatrixSetName());

        assertEquals("MATRIXNAME:3",tile.getTileMatrixName());
        assertEquals( 5,tile.getRow());
        assertEquals( 3,tile.getCol());

    }


    @Test
    public void testSimple() throws Exception {
        XmlCapabilitiesWMTS xmlCapabilitiesDocument = read("wmts_simple.xml");
        assertNotNull(xmlCapabilitiesDocument);

        assertEquals("serviceMetadataURL", xmlCapabilitiesDocument.getMetadataUrlRaw());


         assertEquals(1, xmlCapabilitiesDocument.getDatasetLinksList().size());

         assertEquals("layer1ID",xmlCapabilitiesDocument.getDatasetLinksList().get(0).getIdentifier());
         assertEquals("layerMetadataURL",xmlCapabilitiesDocument.getDatasetLinksList().get(0).getRawUrl());

    }


    public XmlCapabilitiesWMTS read(String fname) throws Exception {
        String text = new Scanner(TestWMSCapabilitiesDatasetLinkExtractor.class.getClassLoader().getResourceAsStream(fname), "UTF-8")
                .useDelimiter("\\A").next();
        return (XmlCapabilitiesWMTS)xmlDocumentFactory.create(text);
    }


    @Before
    public void setup(){
        xmlDocumentFactory = new XmlDocumentFactory();
        xmlDocumentFactory.capabilityDeterminer = new CapabilityDeterminer();
        wmtsCapabilitiesDatasetLinkExtractor = new WMTSCapabilitiesDatasetLinkExtractor();
    }
}
