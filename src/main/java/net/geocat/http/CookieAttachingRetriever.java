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

package net.geocat.http;

import net.geocat.database.linkchecker.entities.HttpResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.nio.charset.MalformedInputException;

@Component
@Scope("prototype")
@Qualifier("cookieAttachingRetriever")
public class CookieAttachingRetriever   {

    @Autowired
    @Qualifier("redirectAwareHTTPRetriever")
    public RedirectAwareHTTPRetriever retriever; // public for testing
    Logger logger = LoggerFactory.getLogger(CookieAttachingRetriever.class);

    public CookieAttachingRetriever() {

    }

    public HttpResult retrieve(HTTPRequest request) throws Exception {
        return retrieve(request.getVerb(),
                request.getLocation(),
                request.getBody(),
                request.getCookie(),
                request.getPredicate(),
                request.getTimeoutSeconds(),
                request.getTimeoutSecondsOnRetry(),
                request.getAcceptsHeader());
    }

        public HttpResult retrieve_underlying(boolean throwIfError,
                                                 boolean throwIfTimeout,
                                                 String verb,
                                                 String location,
                                                 String body,
                                                 String cookie,
                                                 IContinueReadingPredicate predicate,
                                                 int timeoutSeconds,
                                                String acceptsHeader) throws IOException, SecurityException, ExceptionWithCookies, RedirectException
        {
            try {
                HttpResult result = retriever.retrieve(verb, location, body, cookie, predicate, timeoutSeconds,acceptsHeader);
                return result;
            }
            catch(SocketTimeoutException ste) {
                logger.debug("error occurred getting - "+location+", error="+ste.getClass().getSimpleName() + " - " + ste.getMessage());
                if (throwIfTimeout)
                    throw ste;
                return null;
            }
            catch(MalformedURLException m)
            {
                throw m;//not recoverable with retry
            }
            catch (Exception e) {
                    logger.debug("error occurred getting - "+location+", error="+e.getClass().getSimpleName() + " - " + e.getMessage());
                if (throwIfError)
                    throw e;
                return null;
            }
        }



    public HttpResult retrieve(String verb, String location, String body, String cookie, IContinueReadingPredicate predicate,int timeoutSeconds, String acceptsHeader) throws IOException, SecurityException, ExceptionWithCookies, RedirectException {
        return retrieve( verb,  location,  body,  cookie,  predicate, timeoutSeconds  ,timeoutSeconds,acceptsHeader);
    }



    public HttpResult retrieve(String verb, String location, String body, String cookie, IContinueReadingPredicate predicate,int timeoutSeconds, int timeout2,String acceptsHeader) throws IOException, SecurityException, ExceptionWithCookies, RedirectException {

        HttpResult result = retrieve_underlying(false,false,verb, location, body, cookie, predicate, timeoutSeconds,acceptsHeader);
        if ( (result !=null) && (result.getHttpCode() == 404))
            return result; // short cut -- not going to change with a retry

        if (result==null || result.isErrorOccurred() || (result.getHttpCode() ==500) ) {
            try {
                Thread.sleep((long) (1 * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.debug("retrying - "+location);
            String _cookie = result !=null ? result.getSpecialToSendCookie(): null;
            result= retrieve_underlying(false,true,verb, location, body,_cookie, predicate,timeout2,acceptsHeader);
        }

        if  ( (result !=null) && (result.getHttpCode() == 403))
            return result; // short cut -- not going to change with a retry (probably shouldn't have re-tried in the first place, but...)

        //3rd try
        if (result==null || result.isErrorOccurred() || (result.getHttpCode() ==500) ) {
            try {
                Thread.sleep((long) (10 * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.debug("retrying2 - "+location);
            String _cookie = result !=null ? result.getSpecialToSendCookie(): null;
            result= retrieve_underlying(true,true,verb, location, body, _cookie, predicate,timeoutSeconds,acceptsHeader);
        }


        return result;
    }
}
