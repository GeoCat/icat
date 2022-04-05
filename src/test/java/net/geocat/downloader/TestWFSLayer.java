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

package net.geocat.downloader;

import net.geocat.eventprocessor.processors.datadownload.downloaders.WFSLayerDownloader;
import net.geocat.simple.TestWMSCapabilitiesDatasetLinkExtractor;
import net.geocat.xml.XmlCapabilitiesWFS;
import net.geocat.xml.XmlDocumentFactory;
import net.geocat.xml.helpers.CapabilityDeterminer;
import org.junit.Before;
import org.junit.Test;

import java.util.Scanner;

import static net.geocat.eventprocessor.processors.datadownload.downloaders.DownloaderHelper.fixBaseURL;
import static net.geocat.eventprocessor.processors.datadownload.downloaders.DownloaderHelper.setParameter;
import static org.junit.Assert.assertEquals;

public class TestWFSLayer {

    @Test
    public void testFullUrl1() throws Exception {
        WFSLayerDownloader downloader = new WFSLayerDownloader();
        XmlCapabilitiesWFS wfsGetCap = read("wfs_inspire_and_layer_multi.xml");
        assertEquals("http://localhost:8080/geoserver/wfs?REQUEST=GetFeature&SERVICE=WFS&VERSION=2.0.0&TYPENAMES=ws%3Alayer2&count=1",
                downloader.createURL(wfsGetCap,"ws:layer2"));
    }

    //---

    @Test
    public void testUrl() throws Exception {
        XmlCapabilitiesWFS wfsGetCap = read("wfs_inspire_and_layer_multi.xml");
        assertEquals("2.0.0", wfsGetCap.getVersionNumber());
        assertEquals("http://localhost:8080/geoserver/wfs", wfsGetCap.getGetFeatureEndpoint());

    }

    @Test
    public void testFixUrl() throws Exception {
        WFSLayerDownloader downloader= new WFSLayerDownloader();

        assertEquals("http://localhost/wfs?",
                fixBaseURL("http://localhost/wfs") );

        assertEquals("http://localhost/wfs?",
                fixBaseURL("http://localhost/wfs ") );

        assertEquals("http://localhost/wfs?service=wfs",
                fixBaseURL("http://localhost/wfs?service=wfs&") );
    }

    @Test
    public void testSetURLParam() throws Exception {
        assertEquals("http://localhost/wfs?REQUEST=GetFeature",
                 setParameter("http://localhost/wfs?","REQUEST","GetFeature")
        );

        assertEquals("http://localhost/wfs?request=GetFeature",
                setParameter("http://localhost/wfs?request=xyz","REQUEST","GetFeature")
        );
    }


    @Test
    public void testSetRequiredParams() throws Exception {
        assertEquals("http://localhost/wfs?REQUEST=GetFeature&SERVICE=WFS&VERSION=2.0.0",
                WFSLayerDownloader.addBasicItemsToUrl("http://localhost/wfs?", "2.0.0")
        );
        assertEquals("http://localhost/wfs?request=GetFeature&SERVICE=WFS&VERSION=2.0.0",
                WFSLayerDownloader.addBasicItemsToUrl("http://localhost/wfs?request=GetFeature", "2.0.0")
        );

        assertEquals("http://localhost/wfs?REQUEST=GetFeature&SERVICE=WFS&VERSION=1.1",
                WFSLayerDownloader.addBasicItemsToUrl("http://localhost/wfs?", "1.1")
        );
    }
        //---

    XmlDocumentFactory xmlDocumentFactory;
    WFSLayerDownloader wfsLayerDownloader;

    public XmlCapabilitiesWFS read(String fname) throws Exception {
        String text = new Scanner(TestWMSCapabilitiesDatasetLinkExtractor.class.getClassLoader().getResourceAsStream(fname), "UTF-8")
                .useDelimiter("\\A").next();
        return (XmlCapabilitiesWFS)xmlDocumentFactory.create(text);
    }


    @Before
    public void setup(){
        xmlDocumentFactory = new XmlDocumentFactory();
        xmlDocumentFactory.capabilityDeterminer = new CapabilityDeterminer();
        wfsLayerDownloader = new WFSLayerDownloader();
    }

}
