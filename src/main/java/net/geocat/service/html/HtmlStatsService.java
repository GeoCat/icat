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

import net.geocat.database.linkchecker.entities.LinkCheckJob;
import net.geocat.database.linkchecker.entities.LocalDatasetMetadataRecord;
import net.geocat.database.linkchecker.repos.CapabilitiesDocumentRepo;
import net.geocat.database.linkchecker.repos.LinkCheckJobRepo;
import net.geocat.database.linkchecker.repos.LocalDatasetMetadataRecordRepo;
import net.geocat.database.linkchecker.repos.LocalServiceMetadataRecordRepo;
import net.geocat.service.BlobStorageService;
import net.geocat.xml.XmlDocumentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HtmlStatsService {

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

    public static String lastLinkCheckJob(LinkCheckJobRepo linkCheckJobRepo){
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
            linkCheckJobId = lastLinkCheckJob(linkCheckJobRepo);





        List<LocalDatasetMetadataRecord> datasets = localDatasetMetadataRecordRepo.findByLinkCheckJobId(linkCheckJobId);
        if (datasets.isEmpty()) {
            linkCheckJobId= lastLinkCheckJobByCountry(linkCheckJobRepo, linkCheckJobId);
            datasets = localDatasetMetadataRecordRepo.findByLinkCheckJobId(linkCheckJobId);
         }

        LinkCheckJob job = linkCheckJobRepo.findById(linkCheckJobId).get();

        String result = "<h1>Stats - "  + job.getLongTermTag()+" - " + linkCheckJobId  +"</h1>\n";
        result += "number of datasets - "+datasets.size()+"<br>\n";

        long nViewLinks = datasets.stream()
                .filter(x->x.getNumberOfViewDataLinks() >0)
                .count();
        long nDownloadLinks = datasets.stream()
                .filter(x->x.getNumberOfDownloadDataLinks() >0)
                .count();
        result += "number viewable - "+nViewLinks+"<br>\n";
        result += "number downloadable - "+nDownloadLinks+"<br>\n";
        result += "<br><br>\n";

        result += "<table border=1>";
        result += "<tr><td>Dataset fileid</td><td>n view links</td><td>n view attempted</td><td>n view success</td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td>n download links</td><td>n download attempted</td><td>n  download success</td>";

        for (LocalDatasetMetadataRecord record : datasets){
            String row ="";
            row += "<tr><td><a href='/api/html/dataset/"+record.getLinkCheckJobId()+"/"+record.getFileIdentifier()+"'>"+ record.getFileIdentifier() + "</a></td>";
            row += "<td style='text-align: center'>"+ record.getNumberOfViewDataLinks() + "</td>";

            String style=getStyle(record.getNumberOfViewDataLinks(),record.getNumberOfViewLinksAttempted());

            row += "<td style='text-align: center;"+style+"'>"+ record.getNumberOfViewLinksAttempted() + "</td>";

            style=getStyle(record.getNumberOfViewLinksAttempted(),record.getNumberOfViewLinksSuccessful());
            row += "<td style='text-align: center;"+style+"'>"+ record.getNumberOfViewLinksSuccessful() + "</td>";

            row += "<td> </td>";

            row += "<td style='text-align: center'>"+ record.getNumberOfDownloadDataLinks() + "</td>";

            style=getStyle(record.getNumberOfDownloadDataLinks(),record.getNumberOfDownloadLinksAttempted());

            row += "<td style='text-align: center;"+style+"'>"+ record.getNumberOfDownloadLinksAttempted() + "</td>";

            style=getStyle(record.getNumberOfDownloadLinksAttempted(),record.getNumberOfDownloadLinksSuccessful());

            row += "<td style='text-align: center;"+style+"'>"+ record.getNumberOfDownloadLinksSuccessful() + "</td>";
            row += "</tr>\n";

            row = row.replace(">null<",">-<");

            result += row;
        }

        result += "</table><Br><br>\n";
        return result;
    }

    public static String lastLinkCheckJobByCountry(LinkCheckJobRepo linkCheckJobRepo,String country){
        LinkCheckJob lastJob = null;
        for(LinkCheckJob job : linkCheckJobRepo.findAll()){
            if (!job.getLongTermTag().toLowerCase().startsWith(country.toLowerCase()))
                continue;
            if (lastJob == null)
                lastJob = job;
            if (lastJob.getCreateTimeUTC().compareTo(job.getCreateTimeUTC()) <1)
                lastJob = job;
        }
        return lastJob.getJobId();
    }



    private String getStyle(Integer a, Integer b) {
        if ((a ==null) && (b==null))
            return "";
        if ((a ==null) || (b==null))
            return "background:#FFDDDD;";
        if ((a.equals(b)))
            return "";
        return "background:#FFDDDD;";
    }
}
