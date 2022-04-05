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
import net.geocat.xml.XmlCapabilitiesAtom;
import net.geocat.xml.XmlCapabilitiesWMS;
import net.geocat.xml.XmlDocumentFactory;
import net.geocat.xml.helpers.CapabilityDeterminer;
import org.junit.Before;
import org.junit.Test;

import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TestAtomCapabilities {


    XmlDocumentFactory xmlDocumentFactory;
    WMSCapabilitiesDatasetLinkExtractor wmsCapabilitiesDatasetLinkExtractor;


    @Test
    public void test_atomFull() throws Exception {
        XmlCapabilitiesAtom xmlCapabilitiesDocument = read("atom_full.xml");
        assertNotNull(xmlCapabilitiesDocument);


        assertEquals(1, xmlCapabilitiesDocument.getDatasetLinksList().size());

        assertEquals("spatial_dataset_identifier_code1",xmlCapabilitiesDocument.getDatasetLinksList().get(0).getIdentifier());
        assertEquals("describedbyURL",xmlCapabilitiesDocument.getDatasetLinksList().get(0).getRawUrl());

        assertEquals(1, xmlCapabilitiesDocument.getEntries().size());
        assertEquals(2, xmlCapabilitiesDocument.getEntries().get(0).getLinks().size());

        assertEquals("spatial_dataset_identifier_namespace1", xmlCapabilitiesDocument.getDatasetLinksList().get(0).getAuthority());
        assertEquals("spatial_dataset_identifier_code1", xmlCapabilitiesDocument.getDatasetLinksList().get(0).getIdentifier());
        assertNull( xmlCapabilitiesDocument.getDatasetLinksList().get(0).getAuthorityName());

        assertEquals("describedby",xmlCapabilitiesDocument.getEntries().get(0).getLinks().get(0).getRel());
        assertEquals("describedbyURL",xmlCapabilitiesDocument.getEntries().get(0).getLinks().get(0).getHref());
        assertEquals("application/xml",xmlCapabilitiesDocument.getEntries().get(0).getLinks().get(0).getType());
        assertEquals("en",xmlCapabilitiesDocument.getEntries().get(0).getLinks().get(0).getHreflang());
        assertNull(xmlCapabilitiesDocument.getEntries().get(0).getLinks().get(0).getTitle());

        assertEquals("alternate",xmlCapabilitiesDocument.getEntries().get(0).getLinks().get(1).getRel());
        assertEquals("hrefALT",xmlCapabilitiesDocument.getEntries().get(0).getLinks().get(1).getHref());
        assertEquals("application/atom+xml",xmlCapabilitiesDocument.getEntries().get(0).getLinks().get(1).getType());
        assertEquals("nl",xmlCapabilitiesDocument.getEntries().get(0).getLinks().get(1).getHreflang());
        assertEquals("geology",xmlCapabilitiesDocument.getEntries().get(0).getLinks().get(1).getTitle());

    }



    public XmlCapabilitiesAtom read(String fname) throws Exception {
        String text = new Scanner(TestWMSCapabilitiesDatasetLinkExtractor.class.getClassLoader().getResourceAsStream(fname), "UTF-8")
                .useDelimiter("\\A").next();
        return (XmlCapabilitiesAtom)xmlDocumentFactory.create(text);
    }


    @Before
    public void setup(){
        xmlDocumentFactory = new XmlDocumentFactory();
        xmlDocumentFactory.capabilityDeterminer = new CapabilityDeterminer();

        wmsCapabilitiesDatasetLinkExtractor =  new WMSCapabilitiesDatasetLinkExtractor();
    }

}
