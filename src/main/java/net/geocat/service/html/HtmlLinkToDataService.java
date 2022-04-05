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

import net.geocat.database.linkchecker.entities.AtomActualDataEntry;
import net.geocat.database.linkchecker.entities.OGCRequest;
import net.geocat.database.linkchecker.entities.SimpleAtomLinkToData;
import net.geocat.database.linkchecker.entities.helper.AtomDataRequest;
import net.geocat.database.linkchecker.entities.helper.LinkToData;
import net.geocat.database.linkchecker.entities.helper.OGCLinkToData;
import net.geocat.database.linkchecker.entities.helper.RetrievableSimpleLink;
import net.geocat.database.linkchecker.repos.LinkToDataRepo;
import net.geocat.xml.XmlStringTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static net.geocat.service.html.HtmlDatasetService.showDataLink;

@Component
public class HtmlLinkToDataService {

    @Autowired
    LinkToDataRepo linkToDataRepo;

    public String getHtml(String _linkId) {
        long linkId = Long.parseLong(_linkId);

        LinkToData link = linkToDataRepo.findById(linkId).get();

        String result = "<h1>  "+link.getClass().getSimpleName()+" - "+linkId+"</h1>\n";

        result += "<br>";
        result += showDataLink(link,true,null);
        result += "<br>";

        if (link instanceof OGCLinkToData) {
            OGCRequest request = ((OGCLinkToData) link).getOgcRequest();
            if (request ==null)
                result += "OGCRequest: null";
            else
                result += showRequest(request);
        }
        if (link instanceof SimpleAtomLinkToData) {
            result += handleAtom((SimpleAtomLinkToData) link);
        }
        return result;
    }

    private String handleAtom(SimpleAtomLinkToData link) {
        String result = "";
        if (link.getAtomSubFeedRequest() != null) {
            result += "<br><h2>Sub Feed Request: <a href='"+link.getAtomSubFeedRequest().getFixedURL()+"'>"+link.getAtomSubFeedRequest().getFixedURL()+ "</a><br></h2>\n";
            result += "Download Successful:"+link.getAtomSubFeedRequest().getSuccessfulAtomRequest()+"<br>\n";
            if (!link.getAtomSubFeedRequest().getSuccessfulAtomRequest()) {
                result += "http code:"+link.getAtomSubFeedRequest().getLinkHTTPStatusCode()+"<br>\n";
                result += "problem: " + link.getAtomSubFeedRequest().getUnSuccessfulAtomRequestReason() + "<br>\n";
                result += "downloaded text:<br>";
                result += "<xmp>"+XmlStringTools.bytea2String(link.getAtomSubFeedRequest().getLinkContentHead())+"</xmp><br>";
            }
        }
        if (link.getAtomActualDataEntryList() !=null) {
            result += "<br><h2>SubFeed Entries</h2>\n";
            for (AtomActualDataEntry entry : link.getAtomActualDataEntryList()) {
                result += "<h3>entry: "+entry.getIndex()+" - "+entry.getEntryId()+"</h3> \n";
                result += "number of links to data: "+entry.getAtomDataRequestList().size()+"<br>\n";
                int indx = 0;
                for (AtomDataRequest dataRequest : entry.getAtomDataRequestList()) {
                    result += "<h4> entry:"+entry.getIndex()+", link to data "+indx+"</h4>\n";
                    indx++;
                    result += "url: "+dataRequest.getFixedURL()+"<br>\n";
                    result += "success:"+dataRequest.getSuccessfullyDownloaded()+"<br>\n";
                    result += "http code:"+dataRequest.getLinkHTTPStatusCode()+"<br>\n";
                    result += "downloaded text:<br>";
                    result += "<xmp>"+XmlStringTools.bytea2String(dataRequest.getLinkContentHead())+"</xmp><br>";
                }
            }
        }
        return result;
    }

    private String showRequest(RetrievableSimpleLink request) {
        String result = "";

        result += request.toString().replace("\n","<br>");
        result += "<br>downloaded text:<br>";
        result += "<xmp>"+XmlStringTools.bytea2String(request.getLinkContentHead())+"</xmp><br>";
        return result;
    }
}
