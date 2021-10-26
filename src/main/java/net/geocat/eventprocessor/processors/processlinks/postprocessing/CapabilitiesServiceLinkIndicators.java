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

package net.geocat.eventprocessor.processors.processlinks.postprocessing;


import net.geocat.database.harvester.entities.BlobStorage;
import net.geocat.database.harvester.repos.BlobStorageRepo;
import net.geocat.database.linkchecker.entities.CapabilitiesDocument;
import net.geocat.database.linkchecker.entities.LinkCheckBlobStorage;
import net.geocat.database.linkchecker.entities.LocalServiceMetadataRecord;

import net.geocat.database.linkchecker.entities.RemoteServiceMetadataRecordLink;
import net.geocat.database.linkchecker.entities.helper.DocumentLink;
import net.geocat.database.linkchecker.entities.helper.IndicatorStatus;
import net.geocat.database.linkchecker.repos.LinkCheckBlobStorageRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class CapabilitiesServiceLinkIndicators {

    private static final Logger logger = LoggerFactory.getLogger(CapabilitiesServiceLinkIndicators.class);


    @Autowired
    LinkCheckBlobStorageRepo linkCheckBlobStorageRepo;

    @Autowired
    BlobStorageRepo harvestBlogStorageRepo;

    public LocalServiceMetadataRecord process(LocalServiceMetadataRecord record,List<CapabilitiesDocument> capDocs) {
        List<DocumentLink> links = new ArrayList<DocumentLink>(record.getServiceDocumentLinks());

        List<RemoteServiceMetadataRecordLink> rsmrls = capDocs.stream()
                .filter(x->x.getRemoteServiceMetadataRecordLink() !=null)
                .map(x->x.getRemoteServiceMetadataRecordLink())
                .filter(x-> x.getSha2() != null)
                .collect(Collectors.toList());


        if (rsmrls.isEmpty()) {
            record.setINDICATOR_CAPABILITIES_RESOLVES_TO_SERVICE(IndicatorStatus.FAIL);
            record.setINDICATOR_CAPABILITIES_SERVICE_FILE_ID_MATCHES(IndicatorStatus.FAIL);
            return record; //nothing to do
        }

        //at least one resolved
        record.setINDICATOR_CAPABILITIES_RESOLVES_TO_SERVICE(IndicatorStatus.PASS);

        boolean match = rsmrls.stream()
                .anyMatch(x-> x.getFileIdentifier().equals(record.getFileIdentifier()));

        if (match)
            record.setINDICATOR_CAPABILITIES_SERVICE_FILE_ID_MATCHES(IndicatorStatus.PASS);
        else
            record.setINDICATOR_CAPABILITIES_SERVICE_FILE_ID_MATCHES(IndicatorStatus.FAIL);

//        // do per-document comparision
//        for(RemoteServiceMetadataRecord remoteServiceMetadataRecord : remoteServiceMetadataRecords) {
//            try {
//             compare(record, remoteServiceMetadataRecord);
//            }
//            catch (Exception e){
//                logger.error("error while comparing two documents",e);
//                remoteServiceMetadataRecord.setIndicator_CompareServiceMetadataLink_FileIdentifier(IndicatorStatus.FAIL);
//            }
//        }

        return record;
    }

//    private void compare(LocalServiceMetadataRecord record, RemoteServiceMetadataRecord remoteServiceMetadataRecord) {
//         String localFileId = record.getFileIdentifier();
//         String remoteFileId = remoteServiceMetadataRecord.getFileIdentifier();
//         if (!compareFileIds(localFileId,remoteFileId)) {
//             remoteServiceMetadataRecord.setIndicator_CompareServiceMetadataLink_FileIdentifier(IndicatorStatus.FAIL);
//             return; //no need to go further
//         }
//         remoteServiceMetadataRecord.setIndicator_CompareServiceMetadataLink_FileIdentifier(IndicatorStatus.PASS);
//
//         String localXML = getXML(record);
//         String remoteXML = getXML(remoteServiceMetadataRecord);
//         List<Difference> diffs = areSame(localXML, remoteXML);
//
//         if (diffs.isEmpty()) {
//             remoteServiceMetadataRecord.setIndicator_CompareServiceMetadataLink_Full(IndicatorStatus.PASS);
//             return;
//         }
//
//        remoteServiceMetadataRecord.setIndicator_CompareServiceMetadataLink_Full(IndicatorStatus.FAIL);
//        String fullDiff = diffs.toString();
   //     remoteServiceMetadataRecord.setMetadataRecordDifferences(fullDiff.substring(0, Math.min(2000, fullDiff.length())));
//    }

    private List<Difference> areSame(String xml_original, String xml_remote) {
        Diff myDiff = DiffBuilder.compare(xml_original).withTest(xml_remote).ignoreComments().ignoreWhitespace().build();

        List<Difference> diffs = new ArrayList<>();
        myDiff.getDifferences().forEach(diffs::add);
        return diffs;
    }


//    private String getXML(RemoteServiceMetadataRecord record) {
//        if (record.actualXML != null)
//            return record.actualXML; //short cut
//        LinkCheckBlobStorage blob = linkCheckBlobStorageRepo.findById(record.getSha2()).get();
//        record.actualXML = blob.getTextValue();
//        return record.actualXML;
//    }

    public String getXML(LocalServiceMetadataRecord record) {
        if (record.actualXML != null)
            return record.actualXML; //short cut
        BlobStorage blob = harvestBlogStorageRepo.findById(record.getSha2()).get();
        record.actualXML = blob.getTextValue();
        return record.actualXML;
    }


    public boolean compareFileIds(String id1, String id2) {
        if ( (id1 == null) || (id2 == null))
            return false;
        return id1.equals(id2);
    }
}
