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
import net.geocat.database.linkchecker.repos.LinkCheckJobRepo;
import net.geocat.database.linkchecker.repos.LocalDatasetMetadataRecordRepo;
import net.geocat.database.linkchecker.repos.LocalServiceMetadataRecordRepo;
import net.geocat.service.BlobStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    public String getHtml(String fileId) throws Exception {
        if ( (fileId == null) || (fileId.trim().isEmpty()))
            throw new Exception("empty fileid");
        fileId = fileId.trim();

        List<LocalDatasetMetadataRecord> datasets = localDatasetMetadataRecordRepo.findByFileIdentifier(fileId);
        Collections.sort(datasets, Comparator.comparing(m->m.getLastUpdateUTC()));
        Collections.reverse(datasets);

        List<LocalServiceMetadataRecord>  services= localServiceMetadataRecordRepo.findByFileIdentifier(fileId);
        Collections.sort(services, Comparator.comparing(m->m.getLastUpdateUTC()));
        Collections.reverse(services);

        String result = "";
        if (!datasets.isEmpty()) {
            result +="<h1>"+datasets.size()+" Dataset Metadata Documents with fileId="+fileId+"</h1>\n<br>\n";
            for(LocalDatasetMetadataRecord record:datasets) {
                result += "<a href='/api/html/dataset/"+record.getLinkCheckJobId()+"/"+fileId+"'>"+record.getLastUpdateUTC().toLocalDateTime().toString() +" - " + record.getLinkCheckJobId()+" - "+record.getTitle()+"</a><br>\n";
            }
        }
        if (!services.isEmpty()) {
            result +="<h1>"+services.size()+" Service Metadata Documents with fileId="+fileId+"</h1>\n";
            for(LocalServiceMetadataRecord record:services) {
                result += "<a href='/api/html/service/"+record.getLinkCheckJobId()+"/"+fileId+"'>"+record.getLastUpdateUTC().toLocalDateTime().toString() +" - " + record.getLinkCheckJobId()+" - "+record.getTitle()+"</a><br>\n";
            }
        }
        return result;
    }
}
