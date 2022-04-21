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
import net.geocat.xml.XmlCapabilitiesWMS;
import net.geocat.xml.XmlStringTools;
import net.geocat.xml.helpers.WMSLayer;
import net.geocat.xml.helpers.WMSLayerBBox;
import net.geocat.xml.helpers.XmlTagInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static net.geocat.eventprocessor.processors.datadownload.downloaders.DownloaderHelper.fixBaseURL;
import static net.geocat.eventprocessor.processors.datadownload.downloaders.DownloaderHelper.isRecognizedImage;
import static net.geocat.eventprocessor.processors.datadownload.downloaders.DownloaderHelper.isSame;
import static net.geocat.eventprocessor.processors.datadownload.downloaders.DownloaderHelper.setParameter;
import static net.geocat.xml.XmlStringTools.determineRootTagInfo;



@Component
@Scope("prototype")
public class WMSLayerDownloader {


    private static final Logger logger = LoggerFactory.getLogger(net.geocat.eventprocessor.processors.datadownload.downloaders.WMSLayerDownloader.class);


//    @Autowired
//    @Qualifier("cachingHttpRetriever")
//    IHTTPRetriever retriever;

    @Autowired
    PartialDownloadPredicateFactory partialDownloadPredicateFactory;

    @Autowired
    AlwaysAbortContinueReadingPredicate alwaysAbortContinueReadingPredicate;

    @Autowired
    RetrievableSimpleLinkDownloader retrievableSimpleLinkDownloader;


    public static String addBasicItemsToUrl(String baseUrl,String wmsVersion) throws Exception {
        String url = baseUrl;
        url = setParameter(url, "REQUEST", "GetMap");
        url = setParameter(url, "SERVICE", "WMS");
        url = setParameter(url, "VERSION", wmsVersion);
        return url;
    }



    public String determineSRSParam(XmlCapabilitiesWMS wmsCap) {
        if ( (wmsCap.getVersionNumber() == null) || (wmsCap.getVersionNumber().isEmpty()))
            return "CRS";
        if (wmsCap.getVersionNumber().startsWith("1.3"))
            return "CRS";
        return  "SRS";
    }

    public String findBestImageFormat(XmlCapabilitiesWMS wmsCap) throws Exception {
        if (wmsCap.supportsFormat("image/png"))
            return "image/png";
        if (wmsCap.supportsFormat("image/jpeg"))
            return "image/jpeg";
        if (wmsCap.supportsFormat("image/jpg"))
            return "image/jpg";
        throw new Exception("couldnt find an appropriate WMS image format");
    }

    public static double nextDouble(double value) {
        long asLong = Double.doubleToRawLongBits(value);

        if (asLong >= 0) // +ive double, so +1 to increase
            asLong = asLong + 1L;
        else if (asLong == Long.MIN_VALUE) //   0
            asLong = 1L;
        else  // -ive double, so -1 to go to increase
            asLong = asLong - 1L;

        return Double.longBitsToDouble(asLong);
    }

    // if zero sized bbox, then fix to a slightly bigger one
    // DEGREE WMS servers do NOT allow xmin==xmax or ymin==ymax
    public WMSLayerBBox fixbounds(WMSLayerBBox original) {
        if (original == null)
             return null;

        WMSLayerBBox result = original.copy();
        if (result.getXmin() == result.getXmax()) {
            result.setXmax( nextDouble(result.getXmax()));
        }
        if (result.getYmin() == result.getYmax()) {
            result.setYmax( nextDouble(result.getYmax()));
        }

        return result;
    }

    //prefer EPSG:4326  (Austria)
    public WMSLayerBBox findBestBBOX( WMSLayer layer) {
        for(WMSLayerBBox bbox : layer.getWmsLayerBBoxList()) {
            if (bbox.getCRS() == null)
                continue;
            if (bbox.getCRS().equals("EPSG:4326"))
                return bbox;
        }
        return layer.getWmsLayerBBoxList().get(0);
    }

    public String createURL(XmlCapabilitiesWMS wmsCap, String layerName) throws Exception {
        String url = fixBaseURL(wmsCap.getGetMapEndpoint());
        url = addBasicItemsToUrl(url, wmsCap.getVersionNumber());
        url = setParameter(url, "LAYERS", layerName);

        String bestFormat = findBestImageFormat(wmsCap);
        url = setParameter(url,"FORMAT",bestFormat);

        WMSLayer layer = wmsCap.findWMSLayer(layerName);
        if (layer == null)
            throw new Exception("wms doesn't know about layer:"+layerName);
        if ( (layer.getWmsLayerBBoxList() == null) || (layer.getWmsLayerBBoxList().isEmpty()))
            throw new Exception("couldnt extra bounds for layer:"+layerName);

        WMSLayerBBox wmsLayerBBox = findBestBBOX(layer);
        wmsLayerBBox = fixbounds(wmsLayerBBox);
        wmsLayerBBox = wmsLayerBBox.makeSmaller(0.25);

        url = setParameter(url, determineSRSParam(wmsCap), wmsLayerBBox.getCRS());
        url = setParameter(url,"BBOX", wmsLayerBBox.asBBOX());

        url = setParameter(url,"HEIGHT", "256");
        url = setParameter(url,"WIDTH", "256");
        url = setParameter(url,"STYLES", "");

        return url;
    }




    public OGCRequest setupRequest(XmlCapabilitiesWMS wmsCap, String layerName) throws Exception {
        String url = createURL(wmsCap,layerName);

        OGCRequest ogcRequest = new OGCRequest(url, HTTPRequestCheckerType.IMAGE_ONLY);
        ogcRequest.setSummary(getClass().getSimpleName()+", layer="+layerName);

        return ogcRequest;
    }



}


