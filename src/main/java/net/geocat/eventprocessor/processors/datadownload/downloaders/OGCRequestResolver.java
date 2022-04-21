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
import net.geocat.service.downloadhelpers.RetrievableSimpleLinkDownloader;
import net.geocat.xml.XmlStringTools;
import net.geocat.xml.helpers.XmlTagInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static net.geocat.eventprocessor.processors.datadownload.downloaders.DownloaderHelper.isRecognizedImage;
import static net.geocat.http.HTTPRequest.ACCEPTS_HEADER_XML;
import static net.geocat.http.HTTPRequest.ACCEPTS_HEADER_XML_IMAGE;
import static net.geocat.xml.XmlStringTools.determineRootTagInfo;

@Component
//@Scope("prototype")
public class OGCRequestResolver {
    @Autowired
    RetrievableSimpleLinkDownloader retrievableSimpleLinkDownloader;

    public void resolve(OGCRequest ogcRequest) {

        retrievableSimpleLinkDownloader.process(ogcRequest, 4096,ACCEPTS_HEADER_XML_IMAGE);

        if (ogcRequest.getIndicator_LinkResolves() == IndicatorStatus.FAIL){
            ogcRequest.setSuccessfulOGCRequest(false);
            ogcRequest.setUnSuccessfulOGCRequestReason("link did not resolve");
            return  ;
        }

        if (ogcRequest.getLinkHTTPStatusCode() != 200) {
            ogcRequest.setSuccessfulOGCRequest(false);
            ogcRequest.setUnSuccessfulOGCRequestReason("http result code is not 200");
            return  ;
        }

        validate(ogcRequest);
        return  ;
    }

    //should be done with test cases...
    private void validate(OGCRequest ogcRequest) {

        try {
            if ((ogcRequest.getLinkContentHead() == null) || (ogcRequest.getLinkContentHead().length == 0)) {
                ogcRequest.setSuccessfulOGCRequest(false);
                ogcRequest.setUnSuccessfulOGCRequestReason("no content in response");
                return;
            }

            //ANY
            if (ogcRequest.getHttpRequestCheckerType() == HTTPRequestCheckerType.ANY) {

                ogcRequest.setSuccessfulOGCRequest(true);

            }

            //IMAGE
            if (ogcRequest.getHttpRequestCheckerType() == HTTPRequestCheckerType.IMAGE_ONLY) {
                if (!isRecognizedImage(ogcRequest.getLinkContentHead())) {
                    ogcRequest.setUnSuccessfulOGCRequestReason("http result is not a recognized image type (png or jpeg)");
                    ogcRequest.setSuccessfulOGCRequest(false);
                    return;
                }
                ogcRequest.setSuccessfulOGCRequest(true);
                return;
            }

            //FEATURE COLLECTION
            if (ogcRequest.getHttpRequestCheckerType() == HTTPRequestCheckerType.FEATURE_COLLECTION_ONLY) {

                String partialXML = XmlStringTools.bytea2String(ogcRequest.getLinkContentHead());

                if (!XmlStringTools.isXML(partialXML)) {
                    ogcRequest.setUnSuccessfulOGCRequestReason("http result is not an xml document");
                    ogcRequest.setSuccessfulOGCRequest(false);
                    return;
                }

                XmlTagInfo rootTagInfo = determineRootTagInfo(partialXML);
                if (!rootTagInfo.getTagName().equals("FeatureCollection") && !rootTagInfo.getTagName().equals("SpatialDataSet")) {
                    ogcRequest.setUnSuccessfulOGCRequestReason("xml result is not a FeatureCollection");
                    ogcRequest.setSuccessfulOGCRequest(false);
                    return;
                }

                ogcRequest.setSuccessfulOGCRequest(true);
                return;
            }

            ogcRequest.setUnSuccessfulOGCRequestReason("couldnt validate - don't know validation type");
            ogcRequest.setSuccessfulOGCRequest(false);
            return;
        }
        catch (Exception e){
            ogcRequest.setUnSuccessfulOGCRequestReason("couldnt validate error: "+e.getClass().getName()+" - "+e.getMessage());
            ogcRequest.setSuccessfulOGCRequest(false);
        }
    }
}
