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

package net.geocat.service;

import net.geocat.database.linkchecker.entities.CapabilitiesDocument;
import net.geocat.database.linkchecker.entities.ServiceDocumentLink;
import net.geocat.database.linkchecker.entities.helper.DocumentLink;
import net.geocat.database.linkchecker.service.CapabilitiesDocumentService;
import net.geocat.database.linkchecker.entities.HttpResult;
import net.geocat.http.IHTTPRetriever;
import net.geocat.service.capabilities.CapabilitiesLinkFixer;
import net.geocat.service.downloadhelpers.CapabilitiesContinueReadingPredicate;
import net.geocat.service.downloadhelpers.RetrievableSimpleLinkDownloader;
import net.geocat.xml.XmlDoc;
import net.geocat.xml.XmlStringTools;
import net.geocat.xml.helpers.CapabilitiesType;
import net.geocat.xml.helpers.CapabilityDeterminer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class RetrieveServiceDocumentLink {

    @Autowired
    @Qualifier("cookieAttachingRetriever")
    IHTTPRetriever retriever;

    @Autowired
    CapabilitiesContinueReadingPredicate capabilitiesContinueReadingPredicate;

    @Autowired
    CapabilityDeterminer capabilityDeterminer;

    @Autowired
    BlobStorageService blobStorageService;

    @Autowired
    CapabilitiesLinkFixer capabilitiesLinkFixer;

    @Autowired
    CapabilitiesDocumentService capabilitiesDocumentService;

    @Autowired
    RetrievableSimpleLinkDownloader retrievableSimpleLinkDownloader;


    public DocumentLink process(ServiceDocumentLink link) throws Exception {

        link.setFixedURL(capabilitiesLinkFixer.fix(link.getRawURL(), link.getLocalServiceMetadataRecord().getMetadataServiceType()));

         retrievableSimpleLinkDownloader.process(link);

        if (!link.getUrlFullyRead())
            return link;


        CapabilitiesDocument capDoc = capabilitiesDocumentService.create(link);
        link.setCapabilitiesDocument(capDoc);
        link.setSha2(capDoc.getSha2());

        return link;
    }


    public CapabilitiesType determineCapabilityType(HttpResult result) {
        try {
            String doc = XmlStringTools.bytea2String(result.getData());
            XmlDoc xmlDoc = new XmlDoc(doc);
            return capabilityDeterminer.determineCapabilitiesType(xmlDoc);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isXML(HttpResult result) {
        try {
            return XmlStringTools.isXML(result.getData());
        } catch (Exception e) {
            return false;
        }
    }
}
