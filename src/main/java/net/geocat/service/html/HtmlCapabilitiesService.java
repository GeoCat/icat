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

import net.geocat.database.linkchecker.entities.CapabilitiesDatasetMetadataLink;
import net.geocat.database.linkchecker.entities.CapabilitiesDocument;
import net.geocat.database.linkchecker.entities.DatasetDocumentLink;
import net.geocat.database.linkchecker.entities.InspireSpatialDatasetIdentifier;
import net.geocat.database.linkchecker.entities.LinkCheckJob;
import net.geocat.database.linkchecker.entities.ServiceDocumentLink;
import net.geocat.database.linkchecker.entities.helper.DatasetIdentifier;
import net.geocat.database.linkchecker.entities.helper.LinkToData;
import net.geocat.database.linkchecker.entities.helper.SHA2JobIdCompositeKey;
import net.geocat.database.linkchecker.repos.CapabilitiesDocumentRepo;
import net.geocat.database.linkchecker.repos.DatasetDocumentLinkRepo;
import net.geocat.database.linkchecker.repos.LinkCheckBlobStorageRepo;
import net.geocat.database.linkchecker.repos.LinkCheckJobRepo;
import net.geocat.database.linkchecker.repos.LinkToDataRepo;
import net.geocat.database.linkchecker.repos.ServiceDocumentLinkRepo;
import net.geocat.xml.XmlDoc;
import net.geocat.xml.XmlDocumentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.util.List;

import static net.geocat.service.html.HtmlDatasetService.showDataLinks;
import static net.geocat.service.html.HtmlDatasetService.showDownloadableLink;

@Component
public class HtmlCapabilitiesService {

    @Autowired
    LinkToDataRepo linkToDataRepo;

    @Autowired
    LinkCheckBlobStorageRepo linkCheckBlobStorageRepo;

    @Autowired
    ServiceDocumentLinkRepo serviceDocumentLinkRepo;

    @Autowired
    DatasetDocumentLinkRepo datasetDocumentLinkRepo;

    @Autowired
    LinkCheckJobRepo linkCheckJobRepo;

    @Autowired
    CapabilitiesDocumentRepo capabilitiesDocumentRepo;

    @Autowired
    XmlDocumentFactory xmlDocumentFactory;

    public String lastLinkCheckJob(String country){
        if (country ==null)
            return lastLinkCheckJob();

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

    public String getHtml( String processID ,  String sha2) throws Exception {
        if ((processID ==null) || (processID.trim().isEmpty()))
            processID = lastLinkCheckJob();
        processID = processID.trim();
        CapabilitiesDocument capabilitiesDocument= capabilitiesDocumentRepo.findById( new SHA2JobIdCompositeKey(sha2,processID)).get();
        if (capabilitiesDocument == null)
            return "<h1> Couldn't find capabilitiesDocument record </h1>";

        String result = "<head><meta charset=\"UTF-8\"></head>\n";

          result +=  "<h1> Capabilities Document  </h1> \n";

        result += "type: "+capabilitiesDocument.getCapabilitiesDocumentType()+"<bR>\n";
        result += "procGetSpatialDataSetName: "+capabilitiesDocument.getProcGetSpatialDataSetName()+"<bR>\n";
        result += "numberOfDatasetLinks: "+capabilitiesDocument.getNumberOfDatasetLinks()+"<bR>\n";
        result += "has inspire extended capabilities: "+capabilitiesDocument.getIndicator_HasExtendedCapabilities()+"<bR>\n";
        result += "sha2: "+capabilitiesDocument.getSha2()+"<bR>\n";

        result += "<br><br>";

       // result += "<xmp>"+capabilitiesDocument.toString()  + "</xmp><br>\n<br>\n";

        result += "<h2>  Inspire Spatial Dataset Identifiers</h2>";

        if (capabilitiesDocument.getInspireSpatialDatasetIdentifiers().isEmpty())
            result += "NO Inspire Spatial Dataset Identifiers<br>";
        for (InspireSpatialDatasetIdentifier id:capabilitiesDocument.getInspireSpatialDatasetIdentifiers()) {
            String codeLink = "<a href='/api/html/identifier?code="+ URLEncoder.encode(id.getCode())+"&linkcheckjobid"+processID+"'>"+id.toString()+"</a>";

            result += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +codeLink +"<br>\n";
        }
        result+="<br>";

        result += "<h2>Back Link to Service Metadata record</h2>";

        if (capabilitiesDocument.getRemoteServiceMetadataRecordLink() == null){
            result += "NO Link to Service Metadata record<br>";
        }
        else {
            result += showDownloadableLink(capabilitiesDocument.getRemoteServiceMetadataRecordLink(),true);
           // result += "<xmp>"+ capabilitiesDocument.getRemoteServiceMetadataRecordLink().toString()+"</xmp><br>\n";
        }


        result += "<h3>Service documents that link to this capabilities</h3>\n";

        List<ServiceDocumentLink> serviceBackLinks =  serviceDocumentLinkRepo.findByLinkCheckJobIdAndSha2(capabilitiesDocument.getLinkCheckJobId(),capabilitiesDocument.getSha2());
        if (!serviceBackLinks.isEmpty()) {
            for(ServiceDocumentLink link : serviceBackLinks) {
                result += "<a href='/api/html/service/"+capabilitiesDocument.getLinkCheckJobId()+"/"+link.getLocalServiceMetadataRecord().getFileIdentifier()+"'>"+link.getLocalServiceMetadataRecord().getTitle() +"</a><br>";
            }
        }
       else {
           result += "NONE<br>\n";
        }
        result += "<h3>Dataset  documents that link to this capabilities</h3>\n";

        List<DatasetDocumentLink> datasetBackLinks =  datasetDocumentLinkRepo.findByLinkCheckJobIdAndSha2(capabilitiesDocument.getLinkCheckJobId(),capabilitiesDocument.getSha2());
        if (!datasetBackLinks.isEmpty()) {
            for(DatasetDocumentLink link : datasetBackLinks) {
                result += "<a href='/api/html/dataset/"+capabilitiesDocument.getLinkCheckJobId()+"/"+link.getDatasetMetadataRecord().getFileIdentifier()+"'>"+link.getDatasetMetadataRecord().getTitle() +"</a><br>";
            }
        } else {
            result += "NONE<br>\n";
        }

        result += "<br> <h3>Layers - "+capabilitiesDocument.getNumberOfDatasetLinks()+"</h3> ";
        int idx =0;
        for(CapabilitiesDatasetMetadataLink layer : capabilitiesDocument.getCapabilitiesDatasetMetadataLinkList()) {
            result += "<h3> layer #"+idx+"</h3><br>\n";
          //  result += "fully downloaded: "+layer.getUrlFullyRead() +"<br><br>";
            result += "Remote Dataset Identifiers:<Br>\n";
            if (layer.getDatasetIdentifiers().isEmpty())
                result += "NO Dataset Identifiers<br>\n";
            for(DatasetIdentifier identifier:layer.getDatasetIdentifiers()) {
                String codeLink = "<a href='/api/html/identifier?code="+URLEncoder.encode(identifier.getCode())+"&linkcheckjobid="+layer.getLinkCheckJobId()+"'>"+identifier.toString()+"</a>";

                result += "&nbsp;&nbsp;&nbsp;+ " +codeLink +"<br>\n";
            }
            result += "<bR>\n";
            result += showDownloadableLink(layer,true);
           // result += "<xmp>"+ layer.toString() + "</xmp><br>\n";

            idx++;
        }


        List<LinkToData> datalinks = linkToDataRepo.findByLinkCheckJobIdAndCapabilitiesSha2(capabilitiesDocument.getLinkCheckJobId(),capabilitiesDocument.getSha2());
        result += "<Br><br><h2>Datasets documents that link to data via this capabilities</h2>";
        result += showDataLinks(datalinks,true);
        if (datalinks.isEmpty())
            result += "NO DATALINKS<BR>";

        result += "<br><br><br><hr><br><br><h1>Actual Capabilities Document Text</h1><br><hr><xmp>"+text(capabilitiesDocument)+"</xmp><br><br>";
        return result;
     }




    private String text(CapabilitiesDocument cap) throws Exception {
        String xml =linkCheckBlobStorageRepo.findById(cap.getSha2()).get().getTextValue() ;
        XmlDoc x =  xmlDocumentFactory.create(xml);
        xml = XmlDoc.writeXMLPretty(xml);
        return xml;
    }
}
