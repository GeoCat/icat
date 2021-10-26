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

import net.geocat.service.capabilities.DatasetLink;
import net.geocat.service.capabilities.WMSCapabilitiesDatasetLinkExtractor;
import net.geocat.xml.XmlCapabilitiesWMS;
import net.geocat.xml.XmlDocumentFactory;
import net.geocat.xml.helpers.CapabilityDeterminer;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;

public class TestWMSCapabilitiesDatasetLinkExtractor {

    XmlDocumentFactory xmlDocumentFactory;
    WMSCapabilitiesDatasetLinkExtractor wmsCapabilitiesDatasetLinkExtractor;

    @Before
    public void setup(){
        xmlDocumentFactory = new XmlDocumentFactory();
        xmlDocumentFactory.capabilityDeterminer = new CapabilityDeterminer();

        wmsCapabilitiesDatasetLinkExtractor =  new WMSCapabilitiesDatasetLinkExtractor();
    }


    @Test
    public void test_multiple_MetadataURLs() throws Exception {
        XmlCapabilitiesWMS xmlCapabilitiesDocument = read("wms_cap_multiple_MetadataURLS.xml");
        List<DatasetLink> links = wmsCapabilitiesDatasetLinkExtractor.findLinks(xmlCapabilitiesDocument);
        assertEquals(2,links.size());

        assertEquals("authority", links.get(0).getAuthority());
        assertEquals("layer2identifierDSURL", links.get(0).getRawUrl());
        assertEquals("layer2identifier", links.get(0).getIdentifier());


        assertEquals("authority", links.get(1).getAuthority());
        assertEquals("layer2identifierDSURL2", links.get(1).getRawUrl());
        assertEquals("layer2identifier", links.get(1).getIdentifier());

    }

    @Test
    public void info_fully_in_single_nested_layer() throws Exception {
        XmlCapabilitiesWMS xmlCapabilitiesDocument = read("wms_cap_full_nested.xml");
        List<DatasetLink> links = wmsCapabilitiesDatasetLinkExtractor.findLinks(xmlCapabilitiesDocument);
        assertEquals(1,links.size());

        assertEquals("authority", links.get(0).getAuthority());
        assertEquals("layer2identifierDSURL", links.get(0).getRawUrl());
        assertEquals("layer2identifier", links.get(0).getIdentifier());

     }

    @Test
    public void info_fully_in_single_nested_layer_parent() throws Exception {
        XmlCapabilitiesWMS xmlCapabilitiesDocument = read("wms_cap_full_nested_in_parent.xml");
        List<DatasetLink> links = wmsCapabilitiesDatasetLinkExtractor.findLinks(xmlCapabilitiesDocument);

        assertEquals(1,links.size()); // 1 because the will both be the same

        assertEquals("authority", links.get(0).getAuthority());
        assertEquals("layer2identifierDSURL", links.get(0).getRawUrl());
        assertEquals("layer2identifier", links.get(0).getIdentifier());

    }

    public XmlCapabilitiesWMS read(String fname) throws Exception {
        String text = new Scanner(TestWMSCapabilitiesDatasetLinkExtractor.class.getClassLoader().getResourceAsStream(fname), "UTF-8")
                .useDelimiter("\\A").next();
        return (XmlCapabilitiesWMS)xmlDocumentFactory.create(text);
    }
}
