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

import net.geocat.service.capabilities.WMTSCapabilitiesDatasetLinkExtractor;
import net.geocat.xml.XmlCapabilitiesWFS;
import net.geocat.xml.XmlCapabilitiesWMS;
import net.geocat.xml.XmlDocumentFactory;
import net.geocat.xml.helpers.CapabilityDeterminer;
import org.junit.Before;
import org.junit.Test;

import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestWFSCapabilities {

    XmlDocumentFactory xmlDocumentFactory;

    public XmlCapabilitiesWFS read(String fname) throws Exception {
        String text = new Scanner(TestWMSCapabilitiesDatasetLinkExtractor.class.getClassLoader().getResourceAsStream(fname), "UTF-8")
                .useDelimiter("\\A").next();
        return (XmlCapabilitiesWFS)xmlDocumentFactory.create(text);
    }


    @Test
    public void testSingle() throws Exception {
        XmlCapabilitiesWFS wfs = read("wfs_inspire_single.xml");


        assertEquals(1,wfs.getInspireDatasetLinks().size());

        assertEquals("ds_metadataurl1", wfs.getInspireDatasetLinks().get(0).getMetadataURL() );
        assertEquals("dsnamespace1", wfs.getInspireDatasetLinks().get(0).getNamespace() );
        assertEquals("dscode1", wfs.getInspireDatasetLinks().get(0).getCode() );


        assertTrue(wfs.getDatasetLinksList().isEmpty()); //not double counting
        int t=0;
    }

    @Test
    public void test_inspire_and_layer() throws Exception {
        XmlCapabilitiesWFS wfs = read("wfs_inspire_and_layer.xml");

        assertEquals(2,wfs.getInspireDatasetLinks().size());

        assertEquals(1, wfs.getDatasetLinksList().size()); //not double counting

        assertEquals("LANG", wfs.getDefaultLang());
        assertEquals(2,wfs.getSRSs().size());
        assertEquals("http://www.opengis.net/def/crs/EPSG/0/4258", wfs.getSRSs().get(0));
        assertEquals("http://www.opengis.net/def/crs/EPSG/0/4326", wfs.getSRSs().get(1));

        assertEquals(2,wfs.getInspireDatasetLinks().size());
        assertEquals("dscode1",wfs.getInspireDatasetLinks().get(0).getCode());
        assertEquals("dsnamespace1",wfs.getInspireDatasetLinks().get(0).getNamespace());
        assertEquals("ds_metadataurl1",wfs.getInspireDatasetLinks().get(0).getMetadataURL());

        assertEquals("ft1URL", wfs.getDatasetLinksList().get(0).getRawUrl() );
        assertEquals("ws:layer1", wfs.getDatasetLinksList().get(0).getOgcLayerName() );

        int t=0;
    }

    @Test
    public void test_inspire_and_layer_multi() throws Exception {
        XmlCapabilitiesWFS wfs = read("wfs_inspire_and_layer_multi.xml");

        assertEquals(2,wfs.getInspireDatasetLinks().size());

        assertEquals(3, wfs.getDatasetLinksList().size()); //not double counting

        assertEquals("ft1URL", wfs.getDatasetLinksList().get(0).getRawUrl() );
        assertEquals("ft2URL", wfs.getDatasetLinksList().get(1).getRawUrl() );
        assertEquals("ft3URL", wfs.getDatasetLinksList().get(2).getRawUrl() );

        assertEquals("ws:layer1", wfs.getDatasetLinksList().get(0).getOgcLayerName() );
        assertEquals("ws:layer2", wfs.getDatasetLinksList().get(1).getOgcLayerName() );
        assertEquals("ws:layer2", wfs.getDatasetLinksList().get(2).getOgcLayerName() );

        int t=0;
    }

    @Test
    public void testMulti() throws Exception {
        XmlCapabilitiesWFS wfs = read("wfs_inspire_multi.xml");


        assertEquals(2,wfs.getInspireDatasetLinks().size());

        assertEquals("ds_metadataurl1", wfs.getInspireDatasetLinks().get(0).getMetadataURL() );
        assertEquals("dsnamespace1", wfs.getInspireDatasetLinks().get(0).getNamespace() );
        assertEquals("dscode1", wfs.getInspireDatasetLinks().get(0).getCode() );


        assertEquals("ds_metadataurl2", wfs.getInspireDatasetLinks().get(1).getMetadataURL() );
        assertEquals("dsnamespace2", wfs.getInspireDatasetLinks().get(1).getNamespace() );
        assertEquals("dscode2", wfs.getInspireDatasetLinks().get(1).getCode() );

        assertTrue(wfs.getDatasetLinksList().isEmpty()); //not double counting


    }


    @Before
    public void setup(){
        xmlDocumentFactory = new XmlDocumentFactory();
        xmlDocumentFactory.capabilityDeterminer = new CapabilityDeterminer();

    }

}
