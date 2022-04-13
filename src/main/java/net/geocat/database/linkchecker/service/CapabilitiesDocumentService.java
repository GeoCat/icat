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

package net.geocat.database.linkchecker.service;

import net.geocat.database.linkchecker.entities.CapabilitiesDatasetMetadataLink;
import net.geocat.database.linkchecker.entities.CapabilitiesDocument;
import net.geocat.database.linkchecker.entities.RemoteServiceMetadataRecordLink;
import net.geocat.database.linkchecker.entities.helper.DocumentLink;
import net.geocat.database.linkchecker.entities.helper.IndicatorStatus;
import net.geocat.service.BlobStorageService;
import net.geocat.service.LinkCheckBlobStorageService;
import net.geocat.xml.XmlCapabilitiesDocument;
import net.geocat.xml.XmlDoc;
import net.geocat.xml.XmlDocumentFactory;
import net.geocat.xml.XmlStringTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
public class CapabilitiesDocumentService {

    @Autowired
    BlobStorageService blobStorageService;

    @Autowired
    XmlDocumentFactory xmlDocumentFactory;

    @Autowired
    LinkCheckBlobStorageService linkCheckBlobStorageService;

    @Autowired
    RemoteServiceMetadataRecordService remoteServiceMetadataRecordService;

    @Autowired
    CapabilitiesDatasetMetadataLinkService capabilitiesDatasetMetadataLinkService;

    public CapabilitiesDocument create(DocumentLink link) throws Exception {

        String xmlStr = XmlStringTools.bytea2String(link.getFullData());

        XmlDoc _doc = xmlDocumentFactory.create(xmlStr);
        if (_doc !=null)
            link.setXmlDocInfo(_doc.toString());



        XmlCapabilitiesDocument xml = (XmlCapabilitiesDocument)_doc;


        xmlStr = XmlDoc.writeXML(xml.getParsedXml());
        String sha2 = xml.computeSHA2(xmlStr);

        linkCheckBlobStorageService.ensureBlobExists(xmlStr, sha2); //write

        CapabilitiesDocument doc = new CapabilitiesDocument();
        doc.setLinkCheckJobId(link.getLinkCheckJobId());
        doc.setSha2(sha2);


        doc.setCapabilitiesDocumentType(xml.getCapabilitiesType());

        if ((xml.getInspireDatasetLinks() !=null) && (!xml.getInspireDatasetLinks().isEmpty()) ) {
            doc.setInspireSpatialDatasetIdentifiers(xml.getInspireDatasetLinks());
            doc.getInspireSpatialDatasetIdentifiers().stream()
                    .forEach(x->x.setCapabilitiesDocument(doc));
        }

        List<CapabilitiesDatasetMetadataLink> dslinks = capabilitiesDatasetMetadataLinkService.createCapabilitiesDatasetMetadataLinks(doc, xml);
        doc.setCapabilitiesDatasetMetadataLinkList(dslinks);

        if (xml.isHasExtendedCapabilities()) {
            doc.setIndicator_HasExtendedCapabilities(IndicatorStatus.PASS);
        } else {
            doc.setIndicator_HasExtendedCapabilities(IndicatorStatus.FAIL);
            return doc;
        }

        String metadataUrl = xml.getMetadataUrlRaw();
        if ((metadataUrl == null) || (metadataUrl.isEmpty())) {
            doc.setIndicator_HasServiceMetadataLink(IndicatorStatus.FAIL);
            return doc;
        }

        doc.setIndicator_HasServiceMetadataLink(IndicatorStatus.PASS);

        RemoteServiceMetadataRecordLink remoteServiceMetadataRecordLink = remoteServiceMetadataRecordService.create(doc, metadataUrl);
        doc.setRemoteServiceMetadataRecordLink(remoteServiceMetadataRecordLink);



        return doc;
    }


}
