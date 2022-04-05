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

package net.geocat.service.capabilities;


import net.geocat.database.linkchecker.entities.CapabilitiesDocument;
import net.geocat.database.linkchecker.entities.HttpResult;
import net.geocat.database.linkchecker.entities.ServiceDocumentLink;
import net.geocat.database.linkchecker.entities.helper.DocumentLink;
import net.geocat.http.HTTPRequest;
import net.geocat.http.IHTTPRetriever;
import net.geocat.http.SmartHTTPRetriever;
import net.geocat.xml.XmlCapabilitiesWFS;
import net.geocat.xml.XmlDoc;
import net.geocat.xml.XmlDocumentFactory;
import net.geocat.xml.XmlStringTools;
import net.geocat.xml.helpers.CapabilitiesType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import static net.geocat.service.capabilities.CapabilitiesLinkFixer.findQueryParmName;

@Component
@Scope("prototype")
public class RetrieveStoredQueries {

    @Autowired
    XmlDocumentFactory xmlDocumentFactory;

//    @Autowired
//    @Qualifier("cachingHttpRetriever")
//    IHTTPRetriever retriever;

    @Autowired
    SmartHTTPRetriever smartHTTPRetriever;

    public String fixURL(String link) throws Exception {
        link = link.trim();
        link = link.replace(" ","%20");
        link = link.replace("&amp;","&"); // this seems to happen a lot
        link = link.replace("{","%7B");
        link = link.replace("}","%7D");

        if (link.endsWith("?"))
            link += "request=ListStoredQueries";

        String request = findQueryParmName(link,"request");
        if (request == null)
            link += "&request=ListStoredQueries";

        String version = findQueryParmName(link,"version");
        if (version == null)
            link += "&version=2.0.0";

        String service = findQueryParmName(link,"service");
        if (service == null)
            link += "&service=WFS";

        return link;
    }


    public String getSpatialDataSetStoredQuery(CapabilitiesDocument doc, DocumentLink link) {
        if (doc.getInspireSpatialDatasetIdentifiers().isEmpty())
            return null; //nothing to do
        if (doc.getCapabilitiesDocumentType() != CapabilitiesType.WFS)
            return null; // only do for WFS
        //This WFS capabilities document with a InspireSpatialDatasetIdentifier
        // we need to check to see if it has GetSpatialDataSet storedquery
        try {
            String xml = XmlStringTools.bytea2String(link.getFullData());
            XmlCapabilitiesWFS wfsCap = (XmlCapabilitiesWFS) xmlDocumentFactory.create(xml);
            Node n = wfsCap.xpath_node("//ows:Operation[@name='ListStoredQueries']/ows:DCP/ows:HTTP/ows:Get");
            if (n == null)
                return null;
            String url = n.getAttributes().getNamedItem("xlink:href").getTextContent();
            url = fixURL(url);


            HTTPRequest request = HTTPRequest.createGET(url);
            request.setLinkCheckJobId(link.getLinkCheckJobId());
            HttpResult httpResult = smartHTTPRetriever.retrieve(request);

             if (!httpResult.isFullyRead())
                return null;

            XmlDoc xmlStoreQueries = new XmlDoc(XmlStringTools.bytea2String(httpResult.getData()));
            Node query;
            query = xmlStoreQueries.xpath_node("//wfs:StoredQuery[@id='http://inspire.ec.europa.eu/operation/download/GetSpatialDataSet']");
            if (query != null)
                return "http://inspire.ec.europa.eu/operation/download/GetSpatialDataSet";
            query= xmlStoreQueries.xpath_node("//wfs:StoredQuery[@id='GetSpatialDataSet']");
            if (query != null)
                return "GetSpatialDataSet";
            return null;
        } catch (Exception e) {
            //didn't work - cannot process.  IGNORE.
            int t = 0;
        }
        return null;
    }
}
