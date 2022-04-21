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

package net.geocat.eventprocessor.processors.datadownload.downloaders;

import net.geocat.database.linkchecker.entities.AtomActualDataEntry;
import net.geocat.database.linkchecker.entities.helper.AtomDataRequest;
import net.geocat.database.linkchecker.entities.helper.AtomSubFeedRequest;
import net.geocat.http.AlwaysAbortContinueReadingPredicate;
import net.geocat.service.downloadhelpers.PartialDownloadPredicateFactory;
import net.geocat.service.downloadhelpers.RetrievableSimpleLinkDownloader;
import net.geocat.xml.XmlCapabilitiesAtom;
import net.geocat.xml.XmlStringTools;
import net.geocat.xml.helpers.AtomEntry;
import net.geocat.xml.helpers.AtomLink;
import net.geocat.xml.helpers.XmlTagInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static net.geocat.http.HTTPRequest.ACCEPTS_HEADER_XML;
import static net.geocat.xml.XmlStringTools.determineRootTagInfo;

@Component
@Scope("prototype")
public class AtomLayerDownloader {

    private static final Logger logger = LoggerFactory.getLogger( AtomLayerDownloader.class);

    @Autowired
    PartialDownloadPredicateFactory partialDownloadPredicateFactory;

    @Autowired
    AlwaysAbortContinueReadingPredicate alwaysAbortContinueReadingPredicate;

    @Autowired
    RetrievableSimpleLinkDownloader retrievableSimpleLinkDownloader;


    public AtomDataRequest createAtomDataRequest(AtomLink link, AtomActualDataEntry atomActualDataEntry, String linkcheckjobid) {
        AtomDataRequest result = new AtomDataRequest(link.getHref());
        result.setLinkCheckJobId(linkcheckjobid);
        result.setAtomActualDataEntry(atomActualDataEntry);
        return result;
    }

    public AtomSubFeedRequest createSubFeedRequest(XmlCapabilitiesAtom atomCap, String layerId, String linkCheckJobId) throws Exception {
        AtomEntry entry = atomCap.findEntry(layerId);
        if (entry == null)
            throw new Exception("no atom entry for id="+layerId);
        AtomLink link = entry.findLink("alternate");
//        if (link == null)
//            link = entry.findLink("");

        if (link == null)
            throw new Exception("couldn't find a link to atom sub-feed!");

        if ( (link.getHref()==null) || (link.getHref().isEmpty()) )
            throw new Exception("link to atom sub-feed is null");


        AtomSubFeedRequest request = new AtomSubFeedRequest(link.getHref());
        request.setLinkCheckJobId(linkCheckJobId);
        return  request;
    }

    public AtomSubFeedRequest resolve(AtomSubFeedRequest atomSubFeedRequest) {
        retrievableSimpleLinkDownloader.process(atomSubFeedRequest, 4096,ACCEPTS_HEADER_XML);
        return atomSubFeedRequest;
    }

    public void validate(AtomSubFeedRequest atomSubFeedRequest) {
        if (!atomSubFeedRequest.getUrlFullyRead()) {
            String xml  = XmlStringTools.bytea2String(atomSubFeedRequest.getLinkContentHead());
            XmlStringTools.isXML(xml);
            atomSubFeedRequest.setUnSuccessfulAtomRequestReason("http result was not downloaded(not an xml document)");
            atomSubFeedRequest.setSuccessfulAtomRequest(false);
            return;
        }

        String partialXML = XmlStringTools.bytea2String(atomSubFeedRequest.getLinkContentHead());

        if (!XmlStringTools.isXML(partialXML)) {
            atomSubFeedRequest.setUnSuccessfulAtomRequestReason("http result is not an xml document");
            atomSubFeedRequest.setSuccessfulAtomRequest(false);
            return;
        }

        XmlTagInfo rootTagInfo = determineRootTagInfo(partialXML);
        if (!rootTagInfo.getTagName().equals("feed")) {
            atomSubFeedRequest.setUnSuccessfulAtomRequestReason("xml result is not a feed");
            atomSubFeedRequest.setSuccessfulAtomRequest(false);
            return;
        }

        atomSubFeedRequest.setSuccessfulAtomRequest(true);
    }

//    public AtomDataRequest createDataRequest(AtomLink link) {
//        AtomDataRequest result = new AtomDataRequest(link.getHref());
//        return result;
//    }

//    // outer list - one for each <Entry>
//    // inner list - multiple if there are rel="section"
//    //            - single if there are not "section" links, but a "alternate"
//    public List<List<AtomDataRequest>> createDataRequests(XmlCapabilitiesAtom atomCapDataSetFeed) {
//        if ( (atomCapDataSetFeed ==null) || (atomCapDataSetFeed.getEntries()==null) ||   (atomCapDataSetFeed.getEntries().isEmpty()))
//            return null;
//        List<List<AtomDataRequest>>  result = new ArrayList<>();
//        for(AtomEntry entry : atomCapDataSetFeed.getEntries()) {
//            List<AtomLink> sectionLinks = entry.findLinks("section");
//            if (sectionLinks != null) {
//                result.add(sectionLinks.stream().map(x->createDataRequest(x)).collect(Collectors.toList()));
//            }
//            else {
//                AtomLink linkRelative  = entry.findLink("relative");
//                if (linkRelative != null)
//                    result.add (  Arrays.asList(new AtomDataRequest[]{createDataRequest(linkRelative)} ));
//            }
//
//        }
//        if (result.isEmpty())
//            return null;
//
//        return result;
//    }


}
