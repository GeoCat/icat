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

import net.geocat.service.downloadhelpers.CapabilitiesContinueReadingPredicate;
import net.geocat.xml.XmlStringTools;
import net.geocat.xml.helpers.CapabilityDeterminer;
import org.junit.Test;

import static junit.framework.TestCase.*;
import static net.geocat.xml.XmlStringTools.getNS;
import static net.geocat.xml.XmlStringTools.getPrefix;
import static net.geocat.xml.XmlStringTools.getRootTag;
import static net.geocat.xml.XmlStringTools.getTagName;
import static net.geocat.xml.XmlStringTools.replaceXMLDecl;

public class TestCapabilitiesContinueReadingPredicate {

    @Test
    public void test_ns(){
        CapabilitiesContinueReadingPredicate pred = new CapabilitiesContinueReadingPredicate(new CapabilityDeterminer());
        String s= "<?xml version=\"1.0\" encoding=\"UTF-8\"?><gmd:MD_Metadata xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gmx=\"http://www.isotc211.org/2005/gmx\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xsi:schemaLocation=\"http://www.isotc211.org/2005/gmd http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd\">\n" +
                "  <gmd:fileIdentifier>\n" +
                "    <gco:CharacterString>bf87f212-3ec2-4fcc-9163-1cbdf5fe334c</gco:CharacterString>\n" +
                "  </gmd:fileIdentifier>\n" +
                "  <gmd:lan";

        String s_nodecl = s.substring(38);
        String s_rootOnly = s.substring(38,409);

       assertTrue(XmlStringTools.isXML(s));

       assertEquals(s_nodecl,replaceXMLDecl(s));

       assertEquals(s_rootOnly,getRootTag(s_nodecl));

       assertEquals("MD_Metadata",getTagName(s_nodecl));
        assertEquals("gmd",getPrefix(s_nodecl));

        assertEquals("http://www.isotc211.org/2005/gmd", getNS("gmd",s_rootOnly));
    }

    @Test
    public void test_no_ns(){
        CapabilitiesContinueReadingPredicate pred = new CapabilitiesContinueReadingPredicate(new CapabilityDeterminer());
        String s= "<?xml version=\"1.0\" encoding=\"UTF-8\"?><MD_Metadata xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns=\"http://www.isotc211.org/2005/gmd\" xmlns:gmx=\"http://www.isotc211.org/2005/gmx\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xsi:schemaLocation=\"http://www.isotc211.org/2005/gmd http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd\">\n" +
                "  <fileIdentifier>\n" +
                "    <gco:CharacterString>bf87f212-3ec2-4fcc-9163-1cbdf5fe334c</gco:CharacterString>\n" +
                "  </fileIdentifier>\n" +
                "  <lan";

        String s_nodecl = s.substring(38);
        String s_rootOnly = s.substring(38,401);

        assertTrue(XmlStringTools.isXML(s));

        assertEquals(s_nodecl,replaceXMLDecl(s));

        assertEquals(s_rootOnly,getRootTag(s_nodecl));

        assertEquals("MD_Metadata",getTagName(s_nodecl));
        assertEquals(null,getPrefix(s_nodecl));

        assertEquals("http://www.isotc211.org/2005/gmd", getNS(null,s_rootOnly));

    }
}
