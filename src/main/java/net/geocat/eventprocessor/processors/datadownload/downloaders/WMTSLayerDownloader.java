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

import net.geocat.database.linkchecker.entities.OGCRequest;
import net.geocat.database.linkchecker.entities.helper.HTTPRequestCheckerType;
import net.geocat.database.linkchecker.entities.helper.IndicatorStatus;
import net.geocat.http.AlwaysAbortContinueReadingPredicate;
import net.geocat.http.IHTTPRetriever;
import net.geocat.http.SmartHTTPRetriever;
import net.geocat.service.downloadhelpers.PartialDownloadPredicateFactory;
import net.geocat.service.downloadhelpers.RetrievableSimpleLinkDownloader;
import net.geocat.xml.XmlCapabilitiesWMS;
import net.geocat.xml.XmlCapabilitiesWMTS;
import net.geocat.xml.helpers.WMSLayer;
import net.geocat.xml.helpers.WMSLayerBBox;
import net.geocat.xml.helpers.WMTSLayer;
import net.geocat.xml.helpers.WMTSTile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static net.geocat.eventprocessor.processors.datadownload.downloaders.DownloaderHelper.fixBaseURL;
import static net.geocat.eventprocessor.processors.datadownload.downloaders.DownloaderHelper.isRecognizedImage;
import static net.geocat.eventprocessor.processors.datadownload.downloaders.DownloaderHelper.setParameter;

@Component
@Scope("prototype")
public class WMTSLayerDownloader {

    private static final Logger logger = LoggerFactory.getLogger(net.geocat.eventprocessor.processors.datadownload.downloaders.WMSLayerDownloader.class);


    @Autowired
    SmartHTTPRetriever retriever;

    @Autowired
    PartialDownloadPredicateFactory partialDownloadPredicateFactory;

    @Autowired
    AlwaysAbortContinueReadingPredicate alwaysAbortContinueReadingPredicate;

    @Autowired
    RetrievableSimpleLinkDownloader retrievableSimpleLinkDownloader;


    public static String addBasicItemsToUrl(String baseUrl,String wmtsVersion) throws Exception {
        String url = baseUrl;
        url = setParameter(url, "REQUEST", "GetTile");
        url = setParameter(url, "SERVICE", "WMTS");
        url = setParameter(url, "VERSION", wmtsVersion);
        return url;
    }

    public String findBestImageFormat(XmlCapabilitiesWMTS wmtsCap,WMTSLayer layer) throws Exception {
         if (layer == null)
            return null;
        if (layer.supportsFormat("image/png"))
            return "image/png";
        if (layer.supportsFormat("image/jpeg"))
            return "image/jpeg";
        if (layer.supportsFormat("image/jpg"))
            return "image/jpg";
        throw new Exception("couldnt find an appropriate WMS image format");
    }

    public String createURL(XmlCapabilitiesWMTS wmtsCap, String layerName, String matrix) throws Exception {
        WMTSLayer layer = wmtsCap.findLayer(layerName);
        if (layer == null)
            throw new Exception("no such WMTS layer :"+layerName);
        String url = fixBaseURL(wmtsCap.getGetTileEndpoint());
        url = addBasicItemsToUrl(url, wmtsCap.getVersionNumber());
        url = setParameter(url, "LAYER", layerName);

        String bestFormat = findBestImageFormat(wmtsCap,layer);
        url = setParameter(url,"FORMAT",bestFormat);

        WMTSTile tile = wmtsCap.sampleTile(layerName,matrix);
        if (tile==null)
            throw new Exception("could not determine a sample tile for WMTS");

        url = setParameter(url,"TILEMATRIXSET",tile.getTileMatrixSetName());
        url = setParameter(url,"TileMatrix",tile.getTileMatrixName());
        url = setParameter(url,"TILEROW", String.valueOf(tile.getRow() ));
        url = setParameter(url,"TILECOL", String.valueOf(tile.getCol() ));

        return url;
    }


    public OGCRequest setupRequest(XmlCapabilitiesWMTS wmtsCap, String layerName) throws Exception {
        String url = createURL(wmtsCap,layerName,null);

        OGCRequest ogcRequest = new OGCRequest(url, HTTPRequestCheckerType.IMAGE_ONLY);
        ogcRequest.setSummary(getClass().getSimpleName()+", layer="+layerName);

        return ogcRequest;
     }
}
