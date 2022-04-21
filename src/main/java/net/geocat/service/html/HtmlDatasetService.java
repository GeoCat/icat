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

import net.geocat.database.linkchecker.entities.DatasetDocumentLink;
import net.geocat.database.linkchecker.entities.LinkCheckJob;
import net.geocat.database.linkchecker.entities.LocalDatasetMetadataRecord;
import net.geocat.database.linkchecker.entities.ServiceDocumentLink;
import net.geocat.database.linkchecker.entities.SimpleAtomLinkToData;
import net.geocat.database.linkchecker.entities.SimpleLayerDatasetIdDataLink;
import net.geocat.database.linkchecker.entities.SimpleLayerMetadataUrlDataLink;
import net.geocat.database.linkchecker.entities.SimpleSpatialDSIDDataLink;
import net.geocat.database.linkchecker.entities.SimpleStoredQueryDataLink;
import net.geocat.database.linkchecker.entities.helper.DatasetIdentifier;
import net.geocat.database.linkchecker.entities.helper.LinkToData;
import net.geocat.database.linkchecker.repos.LinkCheckJobRepo;
import net.geocat.database.linkchecker.repos.LinkToDataRepo;
import net.geocat.database.linkchecker.repos.LocalDatasetMetadataRecordRepo;
import net.geocat.service.BlobStorageService;
import net.geocat.xml.XmlDoc;
import net.geocat.xml.helpers.CapabilitiesType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class HtmlDatasetService {

    @Autowired
    LinkCheckJobRepo linkCheckJobRepo;

    @Autowired
    BlobStorageService blobStorageService;

    @Autowired
    LocalDatasetMetadataRecordRepo localDatasetMetadataRecordRepo;

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
    public String getHtml( String processID ,  String fileId) throws Exception {
        if ((processID == null) || (processID.trim().isEmpty()))
            processID = lastLinkCheckJob();
        processID = processID.trim();
        LocalDatasetMetadataRecord record = localDatasetMetadataRecordRepo.findFirstByFileIdentifierAndLinkCheckJobId(fileId, processID);
        if (record == null)
            return "<h1> Couldnt find Dataset record </h1>";

        String result = "<h1> Dataset Record</h1> \n";
        result += "<xmp>" + record.toString() + "</xmp><br>\n<br>\n";


        result += "<h1> Dataset Identifiers</h1> \n";
        result += "<b>FileIdentifier: "+ record.getFileIdentifier() +"</b><br><br>\n";
        for(DatasetIdentifier identifier:record.getDatasetIdentifiers()) {
            result += identifier.toString() +"<br>\n";
        }

        result +="<h2>Capabilities Links</h2><Br>\n";
        int idx = 0;
        for(DatasetDocumentLink link: record.getDocumentLinks()) {
            if ( (link.getUrlFullyRead() != null) && (link.getUrlFullyRead())) {
                result += "<h3>Link #" + idx + " - <a href='" +"/api/html/capabilities/"+ link.getLinkCheckJobId()+"/"+link.getSha2()   + "'>"+link.getXmlDocInfo() + "</a>" + "</h3>\n";
                idx++;
            }
        }

        result += "<h2>Links to Data</h2>\n";


        if (record.getDataLinks().size() == 0) {
            result += "NO links to data<Br><br>\n";
        }
        else {
            result += showDataLinks(record);
        }
        result +="<h2>Document Links</h2>\n";
        idx = 0;
        for(DatasetDocumentLink link: record.getDocumentLinks()) {
            result += "<br> <h3>Document Link #"+idx+" - <a href='"+ link.getFixedURL() +"'>"+link.getFixedURL()+"</a>" +"</h3>\n";
            result += "fully downloaed: "+link.getUrlFullyRead()+"<br>\n";
            result += "<xmp>"+link.toString()  + "</xmp><br>\n<br>\n";
            result +="Initial Data:<br>\n";
            if (link.getLinkContentHead() !=null)
                result += "<xmp>"+new String(link.getLinkContentHead())+"</xmp>";
            idx++;
        }

        result += "<br><br><br><hr><br><br><xmp>"+text(record)+"</xmp><br><br>";
        return result;
    }
    private String showDataLinks(LocalDatasetMetadataRecord record) {
        return showDataLinks(new ArrayList(record.getDataLinks()), false);
    }

    public static String showDataLink(LinkToData link, boolean showDSLink, Integer _lndx) {
        String result = "";
        String indx = _lndx == null ? "" : _lndx.toString();

        result += "<h3>link "+indx+" - <a href='/api/html/linktodata/"+link.getLinkToDataId() +"'>"+ link.getClass().getSimpleName() + "</a></h3>";
        if (showDSLink)
            result += "dataset: <a href='" +"/api/html/dataset/"+ link.getLinkCheckJobId()+"/"+link.getDatasetMetadataFileIdentifier()+ "'>"+link.getDatasetMetadataFileIdentifier()  + "  </a>" + "</h3><br>\n";

        result += "capabilities: <a href='" +"/api/html/capabilities/"+ link.getLinkCheckJobId()+"/"+link.getCapabilitiesSha2() + "'>"+link.getCapabilitiesDocumentType()  + " Capabilities</a>" + "</h3>\n";



        if (link instanceof SimpleLayerMetadataUrlDataLink) {
            SimpleLayerMetadataUrlDataLink _link = (SimpleLayerMetadataUrlDataLink) link;
            result += "<br>ogcLayer: "+_link.getOgcLayerName()+"<br>\n";
            if (_link.getSuccessfullyDownloaded() != null) {
                result += "Download: success->" +_link.getSuccessfullyDownloaded()+"<br>\n";
            }
            if (_link.getOgcRequest() != null) {
                result += "OGCRequest: " +_link.getOgcRequest().getFixedURL()+"<br>\n";
            }
        }
        if (link instanceof SimpleStoredQueryDataLink) {
            SimpleStoredQueryDataLink _link = (SimpleStoredQueryDataLink) link;
            result += "<br>storedProcName: "+_link.getStoredProcName()+"<br>\n";
            result += "code: "+_link.getCode()+"<br>\n";
            result += "codespace: "+_link.getCodeSpace()+"<br>\n";
            if (_link.getSuccessfullyDownloaded() != null) {
                result += "Download: success->" +_link.getSuccessfullyDownloaded()+"<br>\n";
            }
            if (_link.getOgcRequest() != null) {
                result += "OGCRequest: " +_link.getOgcRequest().getFixedURL()+"<br>\n";
            }
        }
        if (link instanceof SimpleLayerDatasetIdDataLink) {
            SimpleLayerDatasetIdDataLink _link = (SimpleLayerDatasetIdDataLink) link;
            result += "<br>code: "+_link.getCode()+"<br>\n";
            result += "codespace: "+_link.getCodeSpace()+"<br>\n";
            result += "ogcLayer: "+_link.getOgcLayerName()+"<br>\n";
            if (_link.getSuccessfullyDownloaded() != null) {
                result += "Download: success->" +_link.getSuccessfullyDownloaded()+"<br>\n";
            }
            if (_link.getOgcRequest() != null) {
                result += "OGCRequest: " +_link.getOgcRequest().getFixedURL()+"<br>\n";
            }
        }
        if (link instanceof SimpleSpatialDSIDDataLink) {
            SimpleSpatialDSIDDataLink _link = (SimpleSpatialDSIDDataLink) link;
            result += "<br>code: "+_link.getCode()+"<br>\n";
            result += "codespace: "+_link.getCodeSpace()+"<br>\n";
            if (_link.getSuccessfullyDownloaded() != null) {
                result += "Download: success->" +_link.getSuccessfullyDownloaded()+"<br>\n";
            }
            if (_link.getOgcRequest() != null) {
                result += "OGCRequest: " +_link.getOgcRequest().getFixedURL()+"<br>\n";
            }
        }
        if (link instanceof SimpleAtomLinkToData) {
            SimpleAtomLinkToData _link = (SimpleAtomLinkToData) link;
            result += "<br>context: "+_link.getContext()+"<br>\n";

            if (_link.getLayerId() != null) {
                result += "Layer ID: " +_link.getLayerId()+"<br>\n";
            }
            if (_link.getSuccessfullyDownloaded() != null) {
                result += "Download: success->" +_link.getSuccessfullyDownloaded()+"<br>\n";
            }
        }
        if (link.getErrorInfo() !=null)
            result += "error info: "+link.getErrorInfo()+"<br>\n";
        return result;
    }

    public static String showDataLinks(List<LinkToData> links, boolean showDSLink) {
        String result = "";
        int indx =0;
        List<LinkToData> links_down = links.stream()
                .filter(x->x.getCapabilitiesDocumentType() == CapabilitiesType.Atom || x.getCapabilitiesDocumentType() == CapabilitiesType.WFS)
                .collect(Collectors.toList());
        List<LinkToData> links_view = links.stream()
                .filter(x->x.getCapabilitiesDocumentType() == CapabilitiesType.WMTS || x.getCapabilitiesDocumentType() == CapabilitiesType.WMS)
                .collect(Collectors.toList());
        result += "<h3>View Links - "+links_view.size()+"</h3>";
        if (links_view.isEmpty())
            result += "NO LINKS <BR>\n";

        for (LinkToData link:links_view) {
            result += showDataLink(link,showDSLink,new Integer(indx));
            indx++;
        }


        result += "<h3>Download Links - "+links_down.size()+"</h3>";
        if (links_down.isEmpty())
            result += "NO LINKS <BR>\n";

        for (LinkToData link:links_down) {
            result += showDataLink(link,showDSLink,new Integer(indx));
            indx++;
        }
        return result;
    }

    private String text(LocalDatasetMetadataRecord record) throws Exception {
        String xml = blobStorageService.findXML(record.getSha2());
        xml = XmlDoc.writeXMLPretty(xml);
        return xml;
    }
}
