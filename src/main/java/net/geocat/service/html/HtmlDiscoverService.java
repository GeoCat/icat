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

import net.geocat.database.linkchecker.entities.LocalDatasetMetadataRecord;
import net.geocat.database.linkchecker.entities.LocalServiceMetadataRecord;
import net.geocat.database.linkchecker.entities.helper.MetadataRecord;
import net.geocat.database.linkchecker.repos.LinkCheckJobRepo;
import net.geocat.database.linkchecker.repos.LocalDatasetMetadataRecordRepo;
import net.geocat.database.linkchecker.repos.LocalServiceMetadataRecordRepo;
import net.geocat.service.BlobStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Component
public class HtmlDiscoverService {

    @Autowired
    LinkCheckJobRepo linkCheckJobRepo;

    @Autowired
    BlobStorageService blobStorageService;

    @Autowired
    LocalDatasetMetadataRecordRepo localDatasetMetadataRecordRepo;

    @Autowired
    LocalServiceMetadataRecordRepo localServiceMetadataRecordRepo;

    public String getHtmlInput(String  linkCheckJobId ) {
        if (linkCheckJobId == null)
            linkCheckJobId = "";

        String result = "";

        result += "<h1>Search for a Service or Dataset Document by File Identifier</h1><br>";

        result += "<script type='text/javascript'>";
        result += "function goToDiscover() {\n";
        result += "     var fileid = document.getElementById('fileid').value;\n";
        result += "     var linkcheckjobid = document.getElementById('linkcheckjobid').value;\n";

        result +=  "    var url = window.location.protocol + '//' +window.location.host+'/api/html/discover/' + fileid;\n";
        result +=  "    if (linkcheckjobid != '') {url += '/'+ linkcheckjobid;}";
        result +=  "    window.location = url;}\n";
        result += "</script>\n";

        result += "<table>";
        result += "<tr><td>link Check Job id:</td><td><input size='50' type='text' id='linkcheckjobid' value='"+linkCheckJobId+"' /></td><td>blank=search all jobs</td></tr>";

        result += "<tr><td>file id:</td><td><input  size='50' type='text' id='fileid' /></td></tr>";
        result += "<tr><td></td><td><input type='submit' value='submit'  onclick='goToDiscover();'  /></td></tr>";
        result += "</table>";

        return result;
    }

    public String getHtml(String fileId, String linkcheckJobId) throws Exception {
        if ( (fileId == null) || (fileId.trim().isEmpty()))
            throw new Exception("empty fileid");
        fileId = fileId.trim();

        List<LocalDatasetMetadataRecord> datasets;
        if (linkcheckJobId ==null) {
            datasets = localDatasetMetadataRecordRepo.findByFileIdentifier(fileId);
            try {
                LocalDatasetMetadataRecord r = localDatasetMetadataRecordRepo.findById(Long.parseLong(fileId)).get();
                datasets.add(r);
            }
            catch(Exception e) {}
        }
        else
            datasets= localDatasetMetadataRecordRepo.findByFileIdentifierAndLinkCheckJobId(fileId,linkcheckJobId);

        Collections.sort(datasets, Comparator.comparing(m->m.getLastUpdateUTC()));
        Collections.reverse(datasets);

        List<LocalServiceMetadataRecord>  services;
        if (linkcheckJobId ==null) {
            services = localServiceMetadataRecordRepo.findByFileIdentifier(fileId);
            try {
                LocalServiceMetadataRecord r = localServiceMetadataRecordRepo.findById(Long.parseLong(fileId)).get();
                services.add(r);
            }
            catch(Exception e) {}
        }
        else
            services= localServiceMetadataRecordRepo.findByFileIdentifierAndLinkCheckJobId(fileId,linkcheckJobId);

         Collections.sort(services, Comparator.comparing(m->m.getLastUpdateUTC()));
        Collections.reverse(services);

        String result = "<head><meta charset=\"UTF-8\"></head>\n";

        result += "<h1>Search for Documents with fileId="+fileId+"</h1>\n<br>\n";
        if (!datasets.isEmpty()) {
            result +="<h1>"+datasets.size()+" Dataset Metadata Documents </h1>\n<br>\n";
            result += "<table border=1><tr><td style='text-align: center;'><b>Run Date</b></td><td style='text-align: center;'><b>Link Check JobId</b></td><td style='text-align: center;'><b>Title</b></td></tr>\n";
            for(LocalDatasetMetadataRecord record:datasets) {
                result += result(record.getLinkCheckJobId(),record.getFileIdentifier(),record.getLastUpdateUTC(),record.getTitle(),"dataset");
              //  result += "<a href='/api/html/dataset/"+record.getLinkCheckJobId()+"/"+fileId+"'>"+record.getLastUpdateUTC().toLocalDateTime().toString() +" - " + record.getLinkCheckJobId()+" - "+record.getTitle()+"</a><br>\n";
            }
        }
        if (!services.isEmpty()) {
            result +="<h1>"+services.size()+" Service Metadata Documents  </h1>\n";
            result += "<table border=1><tr><td style='text-align: center;'><b>Run Date</b></td><td style='text-align: center;'><b>Link Check JobId</b></td><td style='text-align: center;'><b>Title</b></td></tr>\n";

            for(LocalServiceMetadataRecord record:services) {
                result += result(record.getLinkCheckJobId(),record.getFileIdentifier(),record.getLastUpdateUTC(),record.getTitle(),"service");

               // result += "<a href='/api/html/service/"+record.getLinkCheckJobId()+"/"+fileId+"'>"+record.getLastUpdateUTC().toLocalDateTime().toString() +" - " + record.getLinkCheckJobId()+" - "+record.getTitle()+"</a><br>\n";
            }
        }

        if (datasets.isEmpty() && services.isEmpty())
            result += "NO RESULTS FOR '"+fileId+"'";
        return result;
    }

    public String result(String linkCheckJobid, String fileId, ZonedDateTime date, String title, String type) {
        String url =   "<a href='/api/html/"+type+"/"+linkCheckJobid+"/"+fileId+"'>"+ title +"</a><br>\n";
        String result = "<tr>";
        result += "<td>"+date.toLocalDateTime().toString()+"</td><td>"+linkCheckJobid+"</td><td>"+url+"</td>";
        result += "</tr>";
        return result;
    }
}
