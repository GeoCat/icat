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

import net.geocat.database.linkchecker.entities.CapabilitiesDocument;
import net.geocat.database.linkchecker.entities.OGCRequest;
import net.geocat.database.linkchecker.entities.helper.HTTPRequestCheckerType;
import net.geocat.database.linkchecker.entities.helper.IndicatorStatus;
import net.geocat.http.AlwaysAbortContinueReadingPredicate;
import net.geocat.http.IHTTPRetriever;
import net.geocat.service.downloadhelpers.PartialDownloadPredicateFactory;
import net.geocat.service.downloadhelpers.RetrievableSimpleLinkDownloader;
import net.geocat.xml.XmlCapabilitiesWFS;
import net.geocat.xml.XmlStringTools;
import net.geocat.xml.helpers.XmlTagInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static net.geocat.eventprocessor.processors.datadownload.downloaders.DownloaderHelper.fixBaseURL;
import static net.geocat.eventprocessor.processors.datadownload.downloaders.DownloaderHelper.setParameter;
import static net.geocat.xml.XmlStringTools.determineRootTagInfo;

@Component
@Scope("prototype")
public class WFSLayerDownloader {


    private static final Logger logger = LoggerFactory.getLogger(WFSLayerDownloader.class);


    @Autowired
    @Qualifier("cachingHttpRetriever")
    IHTTPRetriever retriever;

    @Autowired
    PartialDownloadPredicateFactory partialDownloadPredicateFactory;

    @Autowired
    AlwaysAbortContinueReadingPredicate alwaysAbortContinueReadingPredicate;

    @Autowired
    RetrievableSimpleLinkDownloader retrievableSimpleLinkDownloader;


    public static String addBasicItemsToUrl(String baseUrl,String wfsVersion) throws Exception {
        String url = baseUrl;
        url = setParameter(url, "REQUEST", "GetFeature");
        url = setParameter(url, "SERVICE", "WFS");
        url = setParameter(url, "VERSION", wfsVersion);
        return url;
    }



    public String determineTypeNameParam(XmlCapabilitiesWFS wfsCap) {
        if ( (wfsCap.getVersionNumber() == null) || (wfsCap.getVersionNumber().isEmpty()))
            return "TYPENAMES";
        if (wfsCap.getVersionNumber().startsWith("2"))
            return "TYPENAMES";
        return  "TYPENAME";
    }

    public String createURL(XmlCapabilitiesWFS wfsCap, String layerName) throws Exception {
        String url = fixBaseURL(wfsCap.getGetFeatureEndpoint());
        url = addBasicItemsToUrl(url, wfsCap.getVersionNumber());

        url = setParameter(url, determineTypeNameParam(wfsCap), layerName);

        if (wfsCap.getVersionNumber().startsWith("2"))
            url = setParameter(url, "count", "1");
        else
            url = setParameter(url, "maxFeatures", "1");

        return url;
    }

    public OGCRequest setupRequest(XmlCapabilitiesWFS wfsCap, String layerName) throws Exception {
        String url = createURL(wfsCap,layerName);

        OGCRequest ogcRequest = new OGCRequest(url, HTTPRequestCheckerType.FEATURE_COLLECTION_ONLY);
        ogcRequest.setSummary(getClass().getSimpleName()+", layer="+layerName);
        return ogcRequest;
    }



}
