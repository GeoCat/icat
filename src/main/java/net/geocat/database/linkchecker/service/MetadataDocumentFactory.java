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

import net.geocat.database.linkchecker.entities.*;
import net.geocat.database.linkchecker.entities.helper.DatasetMetadataRecord;
import net.geocat.database.linkchecker.entities.helper.MetadataRecord;
import net.geocat.database.linkchecker.entities.helper.ServiceMetadataDocumentState;
import net.geocat.database.linkchecker.entities.helper.ServiceMetadataRecord;
import net.geocat.service.ServiceDocLinkExtractor;
import net.geocat.xml.XmlDatasetMetadataDocument;
import net.geocat.xml.XmlMetadataDocument;
import net.geocat.xml.XmlServiceRecordDoc;
import net.geocat.xml.helpers.OnlineResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class MetadataDocumentFactory {

    @Autowired
    ServiceDocLinkExtractor serviceDocLinkExtractor;

    @Autowired
    ServiceDocumentLinkService serviceDocumentLinkService;

    @Autowired
    OperatesOnLinkService operatesOnLinkService;

    @Autowired
    ServiceMetadataRecordService serviceMetadataRecordService;

    @Autowired
    RemoteServiceMetadataRecordService remoteServiceMetadataRecordService;

    @Autowired
    RemoteDatasetMetadataRecordService remoteDatasetMetadataRecordService;

    @Autowired
    CapabilitiesDatasetMetadataLinkService capabilitiesDatasetMetadataLinkService;


    public void augment(MetadataRecord metadataRecord, XmlMetadataDocument xml) {
        metadataRecord.setMetadataRecordType(xml.getMetadataDocumentType());
        metadataRecord.setFileIdentifier(xml.getFileIdentifier());
    }

    public void augment(DatasetMetadataRecord datasetMetadataRecord, XmlDatasetMetadataDocument xml) throws Exception {
        augment((MetadataRecord) datasetMetadataRecord, (XmlMetadataDocument) xml);
        datasetMetadataRecord.setDatasetIdentifier(xml.getDatasetIdentifier());

         List<OnlineResource> links = serviceDocLinkExtractor.extractOnlineResource(xml);

        List<DatasetDocumentLink> links2 = links.stream().map(x -> serviceDocumentLinkService.create(datasetMetadataRecord, x)).collect(Collectors.toList());
        datasetMetadataRecord.setDocumentLinks(links2);

    }

    public void augment(ServiceMetadataRecord serviceMetadataRecord, XmlServiceRecordDoc xml) throws Exception {
        augment((MetadataRecord) serviceMetadataRecord, (XmlMetadataDocument) xml);

        serviceMetadataRecord.setMetadataServiceType(xml.getServiceType());
        List<OnlineResource> links = serviceDocLinkExtractor.extractOnlineResource(xml);

        Set<ServiceDocumentLink> links2 = links.stream().map(x -> serviceDocumentLinkService.create(serviceMetadataRecord, x)).collect(Collectors.toSet());
        serviceMetadataRecord.setServiceDocumentLinks(links2);

        Set<OperatesOnLink> operatesOnLinks = xml.getOperatesOns().stream()
                .map(x -> operatesOnLinkService.create(serviceMetadataRecord, x))
                .collect(Collectors.toSet());

        serviceMetadataRecord.setOperatesOnLinks(operatesOnLinks);
    }

    public LocalServiceMetadataRecord createLocalServiceMetadataRecord(XmlServiceRecordDoc doc,
                                                                       Long underlyingHarvestMetadataRecordId,
                                                                       String linkCheckJobId,
                                                                       String sha2) throws Exception {
        LocalServiceMetadataRecord result = serviceMetadataRecordService.create(doc, underlyingHarvestMetadataRecordId, linkCheckJobId, sha2);
        augment(result, doc);
        return result;
    }

    public LocalDatasetMetadataRecord createLocalDatasetMetadataRecord(XmlDatasetMetadataDocument doc,
                                                                       Long underlyingHarvestMetadataRecordId,
                                                                       String linkCheckJobId,
                                                                       String sha2) throws Exception {
        LocalDatasetMetadataRecord result = remoteDatasetMetadataRecordService.createLocalServiceMetadataRecord(doc, underlyingHarvestMetadataRecordId, linkCheckJobId, sha2);
        augment(result, doc);
        return result;
    }

    public LocalNotProcessedMetadataRecord createLocalNotProcessedMetadataRecord(XmlMetadataDocument doc,
                                                                       Long underlyingHarvestMetadataRecordId,
                                                                       String linkCheckJobId,
                                                                       String sha2) throws Exception {
        LocalNotProcessedMetadataRecord result =  new LocalNotProcessedMetadataRecord();
        result.setHarvesterMetadataRecordId(underlyingHarvestMetadataRecordId);
        result.setLinkCheckJobId(linkCheckJobId);
        result.setSha2(sha2);


        result.setFileIdentifier(doc.getFileIdentifier());
        result.setMetadataRecordType(doc.getMetadataDocumentType()); // dataset

        result.setState(ServiceMetadataDocumentState.CREATED);

        augment(result, doc);
        return result;
    }

    public RemoteServiceMetadataRecord createRemoteServiceMetadataRecord(RemoteServiceMetadataRecordLink link, XmlServiceRecordDoc xmlServiceRecordDoc, String sha2) throws Exception {
        RemoteServiceMetadataRecord result = remoteServiceMetadataRecordService.create(link);
        augment(result, xmlServiceRecordDoc);
        link.setRemoteServiceMetadataRecord(result);
        result.setSha2(sha2);
        return result;
    }

    public OperatesOnRemoteDatasetMetadataRecord createRemoteDatasetMetadataRecord(OperatesOnLink link, XmlDatasetMetadataDocument doc,String jobid) throws Exception {
        OperatesOnRemoteDatasetMetadataRecord result = remoteDatasetMetadataRecordService.createRemoteDatasetMetadataRecord(link);
        result.setLinkCheckJobId(jobid);
        augment(result, doc);
        result.setOperatesOnLink(link);
        return result;
    }

    public CapabilitiesRemoteDatasetMetadataDocument createCapabilitiesRemoteDatasetMetadataDocument(
            CapabilitiesDatasetMetadataLink link, XmlDatasetMetadataDocument doc,String jobid) throws Exception {
        CapabilitiesRemoteDatasetMetadataDocument result = remoteDatasetMetadataRecordService.createCapabilitiesRemoteDatasetMetadataDocument(link,jobid);
        augment(result, doc);
        result.setCapabilitiesDatasetMetadataLink(link);
        return result;
    }

}
