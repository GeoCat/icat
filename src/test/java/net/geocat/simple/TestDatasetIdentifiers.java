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

import net.geocat.database.linkchecker.entities.helper.DatasetIdentifierNodeType;
import net.geocat.xml.XmlCapabilitiesWFS;
import net.geocat.xml.XmlDatasetMetadataDocument;
import net.geocat.xml.XmlDocumentFactory;
import net.geocat.xml.helpers.CapabilityDeterminer;
import org.junit.Before;
import org.junit.Test;

import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestDatasetIdentifiers {


    XmlDocumentFactory xmlDocumentFactory;

    @Before
    public void setup() {
        xmlDocumentFactory = new XmlDocumentFactory();
        xmlDocumentFactory.capabilityDeterminer = new CapabilityDeterminer();

    }

    public XmlDatasetMetadataDocument read(String fname) throws Exception {
        String text = new Scanner(TestWMSCapabilitiesDatasetLinkExtractor.class.getClassLoader().getResourceAsStream(fname), "UTF-8")
                .useDelimiter("\\A").next();
        return (XmlDatasetMetadataDocument) xmlDocumentFactory.create(text);
    }

    @Test
    public void test_none_totallymissing() throws Exception {
        XmlDatasetMetadataDocument datasetRecord = read("identifier/identifier_none_totallymissing1.xml");
        assertEquals(0, datasetRecord.getDatasetIdentifiers().size());

        datasetRecord = read("identifier/identifier_none_totallymissing2.xml");
        assertEquals(0, datasetRecord.getDatasetIdentifiers().size());

        datasetRecord = read("identifier/identifier_none_totallymissing3.xml");
        assertEquals(0, datasetRecord.getDatasetIdentifiers().size());

        datasetRecord = read("identifier/identifier_none_totallymissing4.xml");
        assertEquals(0, datasetRecord.getDatasetIdentifiers().size());
    }

    @Test
    public void test_none() throws Exception {
        XmlDatasetMetadataDocument datasetRecord = read("identifier/identifier_none1.xml");
        assertEquals(0, datasetRecord.getDatasetIdentifiers().size());

        datasetRecord = read("identifier/identifier_none2.xml");
        assertEquals(0, datasetRecord.getDatasetIdentifiers().size());
    }

    @Test
    public void test_simpleMD() throws Exception {
        XmlDatasetMetadataDocument datasetRecord = read("identifier/identifier_md_code.xml");
        assertEquals(1, datasetRecord.getDatasetIdentifiers().size());

        assertEquals("md_code1", datasetRecord.getDatasetIdentifiers().get(0).getCode());
        assertNull(  datasetRecord.getDatasetIdentifiers().get(0).getCodeSpace());
        assertEquals( DatasetIdentifierNodeType.MD_Identifier, datasetRecord.getDatasetIdentifiers().get(0).getIdentifierNodeType());

    }

    @Test
    public void test_multiMD() throws Exception {
        XmlDatasetMetadataDocument datasetRecord = read("identifier/identifier_md_multiple.xml");
        assertEquals(2, datasetRecord.getDatasetIdentifiers().size());

        assertEquals("md_code1", datasetRecord.getDatasetIdentifiers().get(0).getCode());
        assertNull(  datasetRecord.getDatasetIdentifiers().get(0).getCodeSpace());
        assertEquals( DatasetIdentifierNodeType.MD_Identifier, datasetRecord.getDatasetIdentifiers().get(0).getIdentifierNodeType());

        assertEquals("md_code2", datasetRecord.getDatasetIdentifiers().get(1).getCode());
        assertNull(  datasetRecord.getDatasetIdentifiers().get(1).getCodeSpace());
        assertEquals( DatasetIdentifierNodeType.MD_Identifier, datasetRecord.getDatasetIdentifiers().get(1).getIdentifierNodeType());

    }

    @Test
    public void test_simpleRS1() throws Exception {
        XmlDatasetMetadataDocument datasetRecord = read("identifier/identifier_rs_code.xml");
        assertEquals(1, datasetRecord.getDatasetIdentifiers().size());

        assertEquals("code1", datasetRecord.getDatasetIdentifiers().get(0).getCode());
        assertNull(  datasetRecord.getDatasetIdentifiers().get(0).getCodeSpace());
        assertEquals( DatasetIdentifierNodeType.RS_Identifier, datasetRecord.getDatasetIdentifiers().get(0).getIdentifierNodeType());

    }
    @Test
    public void test_simpleRS2() throws Exception {
        XmlDatasetMetadataDocument datasetRecord = read("identifier/identifier_rs_code_codespace.xml");
        assertEquals(1, datasetRecord.getDatasetIdentifiers().size());

        assertEquals("code1", datasetRecord.getDatasetIdentifiers().get(0).getCode());
        assertEquals( "codespace1", datasetRecord.getDatasetIdentifiers().get(0).getCodeSpace());
        assertEquals( DatasetIdentifierNodeType.RS_Identifier, datasetRecord.getDatasetIdentifiers().get(0).getIdentifierNodeType());
    }

    @Test
    public void test_multiRS() throws Exception {
        XmlDatasetMetadataDocument datasetRecord = read("identifier/identifier_rs_multi.xml");
        assertEquals(2, datasetRecord.getDatasetIdentifiers().size());

        assertEquals("code1", datasetRecord.getDatasetIdentifiers().get(0).getCode());
        assertEquals( "codespace1", datasetRecord.getDatasetIdentifiers().get(0).getCodeSpace());
        assertEquals( DatasetIdentifierNodeType.RS_Identifier, datasetRecord.getDatasetIdentifiers().get(0).getIdentifierNodeType());

        assertEquals("code2", datasetRecord.getDatasetIdentifiers().get(1).getCode());
        assertEquals( "codespace2", datasetRecord.getDatasetIdentifiers().get(1).getCodeSpace());
        assertEquals( DatasetIdentifierNodeType.RS_Identifier, datasetRecord.getDatasetIdentifiers().get(1).getIdentifierNodeType());
    }

    @Test
    public void test_simpleRS_authority() throws Exception {
        XmlDatasetMetadataDocument datasetRecord = read("identifier/identifier_rs_code_codespace_authority.xml");
        assertEquals(1, datasetRecord.getDatasetIdentifiers().size());

        assertEquals("code1", datasetRecord.getDatasetIdentifiers().get(0).getCode());
        assertEquals( "codespace1", datasetRecord.getDatasetIdentifiers().get(0).getCodeSpace());
        assertEquals( DatasetIdentifierNodeType.RS_Identifier, datasetRecord.getDatasetIdentifiers().get(0).getIdentifierNodeType());
    }

    @Test
    public void test_simpleRS_anchor() throws Exception {
        XmlDatasetMetadataDocument datasetRecord = read("identifier/identifier_rs_code_codespace_anchor.xml");
        assertEquals(1, datasetRecord.getDatasetIdentifiers().size());

//        assertEquals("anchortext1", datasetRecord.getDatasetIdentifiers().get(0).getCode());
//        assertEquals( "anchortext2", datasetRecord.getDatasetIdentifiers().get(0).getCodeSpace());
//        assertEquals( DatasetIdentifierNodeType.RS_Identifier, datasetRecord.getDatasetIdentifiers().get(0).getIdentifierNodeType());
//
//        assertEquals("anchortext1", datasetRecord.getDatasetIdentifiers().get(1).getCode());
//        assertEquals( "xrefcode2", datasetRecord.getDatasetIdentifiers().get(1).getCodeSpace());
//        assertEquals( DatasetIdentifierNodeType.RS_Identifier, datasetRecord.getDatasetIdentifiers().get(1).getIdentifierNodeType());
//
//        assertEquals("xrefcode1", datasetRecord.getDatasetIdentifiers().get(2).getCode());
//        assertEquals( "anchortext2", datasetRecord.getDatasetIdentifiers().get(2).getCodeSpace());
//        assertEquals( DatasetIdentifierNodeType.RS_Identifier, datasetRecord.getDatasetIdentifiers().get(2).getIdentifierNodeType());

        assertEquals("xrefcode1", datasetRecord.getDatasetIdentifiers().get(0).getCode());
        assertEquals( "xrefcode2", datasetRecord.getDatasetIdentifiers().get(0).getCodeSpace());
        assertEquals( DatasetIdentifierNodeType.RS_Identifier, datasetRecord.getDatasetIdentifiers().get(0).getIdentifierNodeType());
    }


    @Test
    public void test_simpleMD_anchor() throws Exception {
        XmlDatasetMetadataDocument datasetRecord = read("identifier/identifier_md_anchor.xml");
        assertEquals(1, datasetRecord.getDatasetIdentifiers().size());

//        assertEquals("anchortext", datasetRecord.getDatasetIdentifiers().get(0).getCode());
//        assertNull(  datasetRecord.getDatasetIdentifiers().get(0).getCodeSpace());
//        assertEquals( DatasetIdentifierNodeType.MD_Identifier, datasetRecord.getDatasetIdentifiers().get(0).getIdentifierNodeType());

        assertEquals("xrefuri", datasetRecord.getDatasetIdentifiers().get(0).getCode());
        assertNull(  datasetRecord.getDatasetIdentifiers().get(0).getCodeSpace());
        assertEquals( DatasetIdentifierNodeType.MD_Identifier, datasetRecord.getDatasetIdentifiers().get(0).getIdentifierNodeType());
    }

    @Test
    public void test_simpleMD_anchor_authority() throws Exception {
        XmlDatasetMetadataDocument datasetRecord = read("identifier/identifier_md_anchor_authority.xml");
        assertEquals(1, datasetRecord.getDatasetIdentifiers().size());
//
//        assertEquals("anchortext", datasetRecord.getDatasetIdentifiers().get(0).getCode());
//        assertNull(  datasetRecord.getDatasetIdentifiers().get(0).getCodeSpace());
//        assertEquals( DatasetIdentifierNodeType.MD_Identifier, datasetRecord.getDatasetIdentifiers().get(0).getIdentifierNodeType());

        assertEquals("xrefuri", datasetRecord.getDatasetIdentifiers().get(0).getCode());
        assertNull(  datasetRecord.getDatasetIdentifiers().get(0).getCodeSpace());
        assertEquals( DatasetIdentifierNodeType.MD_Identifier, datasetRecord.getDatasetIdentifiers().get(0).getIdentifierNodeType());
    }

}
