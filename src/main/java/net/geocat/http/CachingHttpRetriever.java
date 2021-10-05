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
import net.geocat.database.linkchecker.repos.HttpResultRepo;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Scope("prototype")
@Qualifier("cachingHttpRetriever")
 public class CachingHttpRetriever implements IHTTPRetriever {

    @Autowired
    HttpResultRepo httpResultRepo;

    @Autowired
    @Qualifier("cookieAttachingRetriever")
    public CookieAttachingRetriever retriever; // public for testing

    boolean limitByJobId = true; // false = testing (do not use in production)

    String linkCheckJobId;

    public CachingHttpRetriever() {
        linkCheckJobId = MDC.get("JMSCorrelationID");
    }

    @Override
    public HttpResult retrieveXML(String verb, String location, String body, String cookie, IContinueReadingPredicate predicate) throws IOException, SecurityException, ExceptionWithCookies, RedirectException {

        if ( (linkCheckJobId == null) || (linkCheckJobId.isEmpty()) )
            return retriever.retrieveXML( verb,  location,  body,  cookie,  predicate);

        HttpResult result = getCached(location);
        if (result != null)
            return result;

        result = retriever.retrieveXML( verb,  location,  body,  cookie,  predicate);
        result.setLinkCheckJobId(linkCheckJobId);
        result = saveResult(result);

        return result;

        }

    //chance that this throws...
    private synchronized  HttpResult saveResult(HttpResult result) {

        if (limitByJobId) {
            //could be parallel processes that did this...
            if (httpResultRepo.existsByLinkCheckJobIdAndURL(linkCheckJobId, result.getURL()))
                return result;
        }
        else {
            if (httpResultRepo.existsByURL(  result.getURL()))
                return result;
        }

        return httpResultRepo.save(result);
    }

    private HttpResult getCached(String location) {
        if (limitByJobId) {
            HttpResult result = httpResultRepo.findByLinkCheckJobIdAndURL(linkCheckJobId, location);
            return result;
        }

        HttpResult result = httpResultRepo.findFirstByURL(location);
        return result;
    }
}
