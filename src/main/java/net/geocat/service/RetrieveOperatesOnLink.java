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

import net.geocat.database.linkchecker.entities.OperatesOnLink;
 import net.geocat.database.linkchecker.service.MetadataDocumentFactory;
import net.geocat.service.downloadhelpers.RetrievableSimpleLinkDownloader;
import net.geocat.xml.XmlDatasetMetadataDocument;
import net.geocat.xml.XmlDoc;
import net.geocat.xml.XmlDocumentFactory;
import net.geocat.xml.XmlStringTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static net.geocat.service.capabilities.CapabilitiesLinkFixer.canonicalize;

@Component
@Scope("prototype")
public class RetrieveOperatesOnLink {

    @Autowired
    RetrievableSimpleLinkDownloader retrievableSimpleLinkDownloader;

    @Autowired
    MetadataDocumentFactory metadataDocumentFactory;

    @Autowired
    XmlDocumentFactory xmlDocumentFactory;

    @Autowired
    LinkCheckBlobStorageService linkCheckBlobStorageService;

    public OperatesOnLink process(OperatesOnLink link,String jobid) throws Exception {

        if ( (link.getFixedURL() == null) || link.getFixedURL().isEmpty())
            link.setFixedURL(link.getRawURL());

        if (link.getFixedURL() != null) {
            link.setFixedURL(link.getFixedURL().replace("&amp;", "&"));
            link.setFixedURL(link.getFixedURL().replace("{", "%7B"));
            link.setFixedURL(link.getFixedURL().replace("}", "%7D"));// this seems to happen a lot

        }
        link.setFixedURL( canonicalize(link.getFixedURL()));


        link = (OperatesOnLink) retrievableSimpleLinkDownloader.process(link);

        if (!link.getUrlFullyRead())
            return link;

        XmlDoc doc = xmlDocumentFactory.create(XmlStringTools.bytea2String(link.getFullData()));

        if (doc !=null)
            link.setXmlDocInfo(doc.toString());

        if (!(doc instanceof XmlDatasetMetadataDocument))
            return link;

        XmlDatasetMetadataDocument xmlDatasetMetadataDocument = (XmlDatasetMetadataDocument) doc;
        String xmlStr = XmlDoc.writeXML(doc.getParsedXml());
        String sha2 = doc.computeSHA2(xmlStr);

        link.setFileIdentifier(xmlDatasetMetadataDocument.getFileIdentifier());
        link.setDatasetIdentifier(xmlDatasetMetadataDocument.getDatasetIdentifier());

        link.setSha2(sha2);
        linkCheckBlobStorageService.ensureBlobExists(xmlStr, sha2);
      //  OperatesOnRemoteDatasetMetadataRecord operatesOnRemoteDatasetMetadataRecord = metadataDocumentFactory.createRemoteDatasetMetadataRecord(link, xmlDatasetMetadataDocument, jobid);
     //   operatesOnRemoteDatasetMetadataRecord.setSha2(sha2);

       // link.setDatasetMetadataRecord(operatesOnRemoteDatasetMetadataRecord);

        return link;
    }
}
