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

package net.geocat.service.downloadhelpers;

import net.geocat.database.linkchecker.entities.helper.RetrievableSimpleLink;
import net.geocat.database.linkchecker.entities.helper.IndicatorStatus;
import net.geocat.database.linkchecker.entities.HttpResult;
import net.geocat.http.HTTPRequest;
import net.geocat.http.HttpRequestFactory;
import net.geocat.http.IContinueReadingPredicate;
import net.geocat.http.IHTTPRetriever;
import net.geocat.http.SmartHTTPRetriever;
import net.geocat.service.LoggingSupport;
import net.geocat.xml.XmlStringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;


@Component
@Scope("prototype")
public class RetrievableSimpleLinkDownloader {

    public static int headLength = 4048;

    private static final Logger logger = LoggerFactory.getLogger(RetrievableSimpleLinkDownloader.class);


    @Autowired
   // @Qualifier("cachingHttpRetriever")
   // IHTTPRetriever retriever;
    SmartHTTPRetriever smartHTTPRetriever;

    @Autowired
    PartialDownloadPredicateFactory partialDownloadPredicateFactory;

    @Autowired
    HttpRequestFactory httpRequestFactory;

    public RetrievableSimpleLink process(RetrievableSimpleLink link) {
        return process(link, headLength,null);
    }

    public RetrievableSimpleLink process(RetrievableSimpleLink link, int headLength, String acceptsHeader) {
        try {

            HttpResult data = null;

            String url = (link.getFixedURL() == null) ? link.getRawURL() : link.getFixedURL();
            if (url !=null)
                url = url.trim(); // several links have a " " at the end of them

            if ( (url == null) || (url.isEmpty()) )
            {
                link.setIndicator_LinkResolves(IndicatorStatus.FAIL);
                link.setLinkHTTPException("URL is null/empty - nothing to retrieve.");
                link.setUrlFullyRead(false);
                return link;
            }

            IContinueReadingPredicate continueReadingPredicate = partialDownloadPredicateFactory.create(link);

            try {
                HTTPRequest request = httpRequestFactory.createGET(url, link.getLinkCheckJobId());
                request.setPredicate(continueReadingPredicate);
                if ( (acceptsHeader!=null) && (!acceptsHeader.isEmpty()) )
                    request.setAcceptsHeader(acceptsHeader);
                data = smartHTTPRetriever.retrieve(request);
            } catch (Exception e) {
                link.setIndicator_LinkResolves(IndicatorStatus.FAIL);
                link.setLinkHTTPException(e.getClass().getSimpleName() + " - " + e.getMessage());
                link.setUrlFullyRead(false);
                return link;
            }
            if ((data.getHttpCode() == 200))
                link.setIndicator_LinkResolves(IndicatorStatus.PASS);
            else
                link.setIndicator_LinkResolves(IndicatorStatus.FAIL);

            link.setLinkHTTPStatusCode(data.getHttpCode());
            link.setLinkMIMEType(data.getContentType());
            link.setFinalURL(data.getFinalURL());
            link.setLinkIsHTTS(data.isHTTPS());
            if (data.isHTTPS()) {
                link.setLinkSSLTrustedByJava(data.isSslTrusted());
                link.setLinkSSLUntrustedByJavaReason(data.getSslUnTrustedReason());
            }

            byte[] headData = Arrays.copyOf(data.getData(), Math.min(headLength, data.getData().length));
            link.setLinkContentHead(headData);

            link.setLinkIsXML(isXML(data));

            link.setUrlFullyRead(data.isFullyRead());
            if (data.isFullyRead()) {
                link.setFullData(data.getData());
            }

            return link;
        } catch (Exception e) {
            Marker marker = LoggingSupport.getMarker(link.getLinkCheckJobId());
            logger.error(marker,"RetrievableSimpleLinkDownloader - error occurred processing link", e);
            return link;
        }
    }

    public boolean isXML(HttpResult result) {
        try {
            return XmlStringTools.isXML( result.getData() );
        } catch (Exception e) {
            return false;
        }
    }

}
