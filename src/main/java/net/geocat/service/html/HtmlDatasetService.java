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
import net.geocat.database.linkchecker.entities.OGCRequest;
import net.geocat.database.linkchecker.entities.ServiceDocumentLink;
import net.geocat.database.linkchecker.entities.SimpleAtomLinkToData;
import net.geocat.database.linkchecker.entities.SimpleLayerDatasetIdDataLink;
import net.geocat.database.linkchecker.entities.SimpleLayerMetadataUrlDataLink;
import net.geocat.database.linkchecker.entities.SimpleSpatialDSIDDataLink;
import net.geocat.database.linkchecker.entities.SimpleStoredQueryDataLink;
import net.geocat.database.linkchecker.entities.helper.DatasetIdentifier;
import net.geocat.database.linkchecker.entities.helper.LinkToData;
import net.geocat.database.linkchecker.entities.helper.OGCLinkToData;
import net.geocat.database.linkchecker.entities.helper.RetrievableSimpleLink;
import net.geocat.database.linkchecker.repos.LinkCheckJobRepo;
import net.geocat.database.linkchecker.repos.LinkToDataRepo;
import net.geocat.database.linkchecker.repos.LocalDatasetMetadataRecordRepo;
import net.geocat.service.BlobStorageService;
import net.geocat.xml.XmlDoc;
import net.geocat.xml.helpers.CapabilitiesType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.CriteriaBuilder;
import java.net.URLEncoder;
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
        String result = "<head><meta charset=\"UTF-8\"></head>\n";

        result += "Quick link: <a href='/api/html/discoverInput/"+processID+"'>find by fileIdentifier</a><br> <br> \n";

        result += "<h1> Dataset Record</h1> \n";

        result += "<table>";

        result += "<tr><td>Title: </td><td> "+record.getTitle()+"</td></tr>\n";
        result += "<tr><td>Metadata Record Type: </td><td> "+record.getMetadataRecordType()+"</td></tr>\n";
        result += "<tr><td>Viewable: </td><td> "+(record.getNumberOfViewLinksSuccessful()>0)+"&nbsp;&nbsp;&nbsp;("+record.getNumberOfViewLinksSuccessful()+")</td></tr>\n";
        result += "<tr><td>Downloadable: </td><td> "+(record.getNumberOfDownloadLinksSuccessful()>0)+"&nbsp;&nbsp;&nbsp;("+record.getNumberOfDownloadLinksSuccessful()+")</td></tr>\n";

        result += "<tr><td>Connected to # View: </td><td> "+record.getNumberOfViewDataLinks()+" </td></tr>\n";
        result += "<tr><td>Connected to # Download: </td><td> "+record.getNumberOfDownloadDataLinks()+" </td></tr>\n";


        result += "</table>\n";

        //  result += "file identifier: "+record.getFileIdentifier()+"<br>\n";

        // result += "<xmp>" + record.toString() + "</xmp><br>\n<br>\n";


        result += "<h2> Dataset Identifiers</h1> \n";
        result += "<b>FileIdentifier: "+ record.getFileIdentifier() +"</b><br><br>\n";
        for(DatasetIdentifier identifier:record.getDatasetIdentifiers()) {
            result += "<a href='/api/html/identifier?code=" + URLEncoder.encode(identifier.getCode())+"&linkcheckjobid="+processID+"'>"+identifier.toString() +"</a><br>\n";
        }

        result +="<h2>Successful links to Capabilities Documents</h2> \n";
        int idx = 0;

        for(DatasetDocumentLink link: record.getDocumentLinks()) {
            if ( (link.getUrlFullyRead() != null) && (link.getUrlFullyRead())) {
                result += " <a href='" +"/api/html/capabilities/"+ link.getLinkCheckJobId()+"/"+link.getSha2()   + "'>"+link.getXmlDocInfo() + "</a>" + " <br>\n";
                idx++;
            }
        }
        if (idx ==0)
            result += "NO SUCCESSFUL LINKS TO CAPABILITIES DOCUMENTS<br>\n";

        result += "<h2>Found Links to Data - "+record.getDataLinks().size()+" links</h2>\n";


        if (record.getDataLinks().size() == 0) {
            result += "NO links to data<Br><br>\n";
        }
        else {
            result += showDataLinks(record);
        }
        result +="<h2>All Document Links - "+record.getDocumentLinks().size()+" links</h2>\n";
        idx = 0;
        for(DatasetDocumentLink link: record.getDocumentLinks()) {
            result += "<br> <h3>Document Link #"+idx+"</h3>";// - <a href='"+ link.getFixedURL() +"'>"+link.getFixedURL()+"</a>" +"</h3>\n";
//            result += "fully downloaed: "+link.getUrlFullyRead()+"<br>\n";
//            result += "<xmp>"+link.toString()  + "</xmp><br>\n<br>\n";
//            result +="Initial Data:<br>\n";
//            if (link.getLinkContentHead() !=null)
//                result += "<xmp>"+new String(link.getLinkContentHead())+"</xmp>";
            result +=showDownloadableLink(link,true);
            idx++;
        }

        result += "<br><br><br><hr><br><br><h1>Actual Dataset Text</h1><br><hr><xmp>"+text(record)+"</xmp><br><br>";
        return result;
    }

    public static String showDownloadableLink(RetrievableSimpleLink link, boolean startTable) {
        String result ="";
        if (startTable)
            result += "<table>";
        result += "<tr><td>link type: </td><Td>"+link.getClass().getSimpleName()+"</td></tr>";
        result += "<tr><td>raw URL: </td><Td>"+link.getRawURL()+"</td></tr>";
        String urlText = link.getFixedURL();
        if ((urlText !=null) && (!urlText.isEmpty())){
            urlText += "&nbsp;<a href='"+urlText+"'>live</a>";
        }
        result += "<tr><td>fixed URL: </td><Td>"+urlText+"</td></tr>";
        result += "<tr><td>fully downloaded: </td><Td>"+link.getUrlFullyRead()+"</td></tr>";
        if (link.getLinkHTTPStatusCode() !=null)
            result += "<tr><td>HTTP Status Code: </td><Td>"+link.getLinkHTTPStatusCode()+"</td></tr>";

        if (link.getLinkHTTPException() !=null)
            result += "<tr><td> Link HTTP Exception: </td><Td>"+link.getLinkHTTPException()+"</td></tr>";

        if (link instanceof DatasetDocumentLink) {
            result += "<tr><td>Is Inspire SimplifiedLink: </td><Td>"+((DatasetDocumentLink)link).isInspireSimplifiedLink()+"</td></tr>";
        }
        if (link.getXmlDocInfo() !=null)
            result += "<tr><td> Xml Doc Info: </td><Td>"+link.getXmlDocInfo()+"</td></tr>";

        result += "</table>";

        if (link.getLinkContentHead() !=null) {
            result +="<Br>Initial Data:<br>\n";
            result += "<xmp>" + new String(link.getLinkContentHead()) + "</xmp>";
        }
        return result ;
    }


    private String showDataLinks(LocalDatasetMetadataRecord record) {
        return showDataLinks(new ArrayList(record.getDataLinks()), false);
    }

    public static String toText(OGCRequest request) {
        String result = "<br><br><b>OGC REQUEST</b><br><Br>\n <table>";
        result += "<tr><td>summary: </td><Td>"+request.getSummary()+"</td></tr>\n";
        result += "<tr><td>successfulOGCRequest: </td><Td>"+request.isSuccessfulOGCRequest()+"</td></tr>\n";
        if (request.getUnSuccessfulOGCRequestReason() !=null)
            result += "<tr><td> UnSuccessful OGC Request Reason: </td><Td>"+request.getUnSuccessfulOGCRequestReason()+"</td></tr>\n";

//        result += "</table>";
        result += showDownloadableLink(request,false);
        return result;
    }

    public static String showDataLink(LinkToData link, boolean showDSLink, Integer _lndx, String type) {
        String result = " ";
        String indx = _lndx == null ? "" : _lndx.toString();

        result += "<h3>"+type+" link "+indx+" - <a href='/api/html/linktodata/"+link.getLinkToDataId() +"'>"+ link.getClass().getSimpleName() + "</a></h3>";

        result += "\n<table>";

        if (showDSLink) {
            String txt=  " <a href='" + "/api/html/dataset/" + link.getLinkCheckJobId() + "/" + link.getDatasetMetadataFileIdentifier() + "'>" + link.getDatasetMetadataFileIdentifier() + "  </a>"   ;
            result += "<tr><td>dataset: </td><Td>"+txt+"</td></tr>\n";
        }
        String txt =  " <a href='" +"/api/html/capabilities/"+ link.getLinkCheckJobId()+"/"+link.getCapabilitiesSha2() + "'>"+link.getCapabilitiesDocumentType()  + " Capabilities</a>"  ;

        result += "<tr><td>capabilities: </td><Td>"+txt+"</td></tr>\n";


        if (link instanceof OGCLinkToData) {
            OGCLinkToData _link = (OGCLinkToData) link;
            result += "<tr><td>ogcLayer: </td><Td>"+_link.getOgcLayerName()+"</td></tr>\n";
            if (link instanceof SimpleLayerDatasetIdDataLink) {
                SimpleLayerDatasetIdDataLink __link = (SimpleLayerDatasetIdDataLink) link;
                String codeLink = "<a href='/api/html/identifier?code="+URLEncoder.encode(__link.getCode())+"&linkcheckjobid="+__link.getLinkCheckJobId()+"'>"+__link.getCode()+"</a>";
                result += "<tr><td>code: </td><Td>"+codeLink+"</td></tr>\n";
                result += "<tr><td>codespace: </td><Td>"+__link.getCodeSpace()+"</td></tr>\n";
            }
            if (link instanceof SimpleSpatialDSIDDataLink) {
                SimpleSpatialDSIDDataLink __link = (SimpleSpatialDSIDDataLink) link;
                String codeLink = "<a href='/api/html/identifier?code="+URLEncoder.encode(__link.getCode())+"&linkcheckjobid="+__link.getLinkCheckJobId()+"'>"+__link.getCode()+"</a>";

                result += "<tr><td>code: </td><Td>"+codeLink+"</td></tr>\n";
                result += "<tr><td>codespace: </td><Td>"+__link.getCodeSpace()+"</td></tr>\n";
            }
             if (_link.getSuccessfullyDownloaded() != null) {
                result += "<tr><td>Downloaded&nbsp;success:&nbsp;</td><Td>"+_link.getSuccessfullyDownloaded()+"</td></tr>\n";
             }
             if (_link.getOgcRequest() != null) {
                 result += "</table>";
                 result+=toText(_link.getOgcRequest());
             }
        }
        if (link instanceof SimpleStoredQueryDataLink) {
            SimpleStoredQueryDataLink __link = (SimpleStoredQueryDataLink) link;
            result += "<tr><td>storedProcName: </td><Td>"+__link.getStoredProcName()+"</td></tr>\n";
            String codeLink = "<a href='/api/html/identifier?code="+URLEncoder.encode(__link.getCode())+"&linkcheckjobid="+__link.getLinkCheckJobId()+"'>"+__link.getCode()+"</a>";

            result += "<tr><td>code: </td><Td>"+codeLink+"</td></tr>\n";
            result += "<tr><td>codespace: </td><Td>"+__link.getCodeSpace()+"</td></tr>\n";
            if (__link.getSuccessfullyDownloaded() != null) {
                result += "<tr><td>Downloaded&nbsp;success:&nbsp;</td><Td>"+__link.getSuccessfullyDownloaded()+"</td></tr>\n";
            }
            if (__link.getOgcRequest() != null) {
                if (__link.getOgcRequest() != null) {
                    result += "</table>";
                    result+=toText(__link.getOgcRequest());
                }
            }
        }


        if (link instanceof SimpleAtomLinkToData) {
            SimpleAtomLinkToData _link = (SimpleAtomLinkToData) link;
            result += "<tr><td>context: </td><Td>"+_link.getContext()+"</td></tr>\n";

          //  result += "<br>context: "+_link.getContext()+"<br>\n";

            if (_link.getLayerId() != null) {
                result += "<tr><td>Layer ID: </td><Td>"+_link.getLayerId()+"</td></tr>\n";
            }
            if (_link.getSuccessfullyDownloaded() != null) {
                result += "<tr><td>Downloaded&nbsp;success:&nbsp;</td><Td>"+_link.getSuccessfullyDownloaded()+"</td></tr>\n";
            }
            result += "</table>";
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
            result += showDataLink(link,showDSLink,new Integer(indx),"view");
            indx++;
        }


        result += "<h3>Download Links - "+links_down.size()+"</h3>";
        if (links_down.isEmpty())
            result += "NO LINKS <BR>\n";

        for (LinkToData link:links_down) {
            result += showDataLink(link,showDSLink,new Integer(indx),"download");
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
