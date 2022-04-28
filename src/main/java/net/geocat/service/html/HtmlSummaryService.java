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

package net.geocat.service.html;

import net.geocat.database.linkchecker.entities.CapabilitiesDocument;
import net.geocat.database.linkchecker.entities.LinkCheckJob;
import net.geocat.database.linkchecker.entities.LocalDatasetMetadataRecord;
import net.geocat.database.linkchecker.entities.LocalServiceMetadataRecord;
import net.geocat.database.linkchecker.repos.CapabilitiesDocumentRepo;
import net.geocat.database.linkchecker.repos.LinkCheckJobRepo;
import net.geocat.database.linkchecker.repos.LocalDatasetMetadataRecordRepo;
import net.geocat.database.linkchecker.repos.LocalServiceMetadataRecordRepo;
import net.geocat.service.BlobStorageService;
import net.geocat.xml.XmlDocumentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Component
public class HtmlSummaryService {

    @Autowired
    LinkCheckJobRepo linkCheckJobRepo;

    @Autowired
    BlobStorageService blobStorageService;

    @Autowired
    LocalDatasetMetadataRecordRepo localDatasetMetadataRecordRepo;

    @Autowired
    LocalServiceMetadataRecordRepo localServiceMetadataRecordRepo;

    @Autowired
    CapabilitiesDocumentRepo capabilitiesDocumentRepo;

    @Autowired
    XmlDocumentFactory xmlDocumentFactory;


    public String lastLinkCheckJob(){
        LinkCheckJob lastJob = null;
        for(LinkCheckJob job : linkCheckJobRepo.findAll()){
            if (lastJob == null)
                lastJob = job;
            if (lastJob.getCreateTimeUTC().compareTo(job.getCreateTimeUTC()) <1)
                lastJob = job;
        }
        return lastJob.getJobId();
    }

    public String getHtml(String linkCheckJobId) {
        if ((linkCheckJobId == null) || (linkCheckJobId.trim().isEmpty()))
            linkCheckJobId = lastLinkCheckJob();

        String result = "<head><meta charset=\"UTF-8\"></head>\n";

          result += "<h1>Summary - "+linkCheckJobId+"</h1>\n";



        List<LocalDatasetMetadataRecord> datasets = localDatasetMetadataRecordRepo.findByLinkCheckJobId(linkCheckJobId);
        result += "<h2> Datasets - " + datasets.size()+"  </h2>\n";
        for (LocalDatasetMetadataRecord datasetRecord : datasets) {
            result += "<a href='/api/html/dataset/" +linkCheckJobId+"/"+datasetRecord.getFileIdentifier()+"'> "+datasetRecord.getFileIdentifier() +" - " +datasetRecord.getTitle() + "</a><br>\n";
        }



        List<LocalServiceMetadataRecord> services = localServiceMetadataRecordRepo.findByLinkCheckJobId(linkCheckJobId);
        result += "<h2> Services  - " + services.size()+"  </h2>\n";
        for (LocalServiceMetadataRecord serviceRecord : services) {
            result += "<a href='/api/html/service/" +linkCheckJobId+"/"+serviceRecord.getFileIdentifier()+"'> "+serviceRecord.getFileIdentifier() + " - "+serviceRecord.getTitle() + "</a><br>\n";
        }


        List<CapabilitiesDocument> capabilitiesDocuments = capabilitiesDocumentRepo.findByLinkCheckJobId(linkCheckJobId);
        result += "<h2> Capabilities   - " + capabilitiesDocuments.size()+"</h2>\n";
       // Collections.sort(capabilitiesDocuments, Comparator.comparing(CapabilitiesDocument::getCapabilitiesDocumentType).thenComparing(CapabilitiesDocument::getNumberOfDatasetLinks));
        Collections.sort(capabilitiesDocuments,Comparator.comparing(CapabilitiesDocument::getNumberOfDatasetLinks));
        Collections.reverse(capabilitiesDocuments);
        for (CapabilitiesDocument capabilitiesDocument : capabilitiesDocuments) {
            result += "<a href='/api/html/capabilities/" +linkCheckJobId+"/"+capabilitiesDocument.getSha2()+"'> "+capabilitiesDocument.getCapabilitiesDocumentType() + " - nlinks="+capabilitiesDocument.getNumberOfDatasetLinks() +  "</a><br>\n";
        }
        return result;
    }
}
