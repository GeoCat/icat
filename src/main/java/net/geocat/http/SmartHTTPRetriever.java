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
import net.geocat.service.helper.ProcessLockingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.locks.Lock;

@Component
@Scope("prototype")
public class SmartHTTPRetriever {

    Logger logger = LoggerFactory.getLogger(SmartHTTPRetriever.class);
    public static Object lockingObject = new Object();

    @Autowired
    HttpResultRepo httpResultRepo;

    @Autowired
   // @Qualifier("cookieAttachingRetriever")
    CookieAttachingRetriever retriever;

    @Autowired
    ProcessLockingService processLockingService;



    public HttpResult retrieve(HTTPRequest request) throws Exception  {
        Lock lock = processLockingService.getLock(request.getLocation()); // don't want to have two processes downloading at the same time...
        lock.lock();
         boolean retrieveCached = request.isUseCache() && request.getLinkCheckJobId() != null;

        try {
            if (retrieveCached) {
                HttpResult result =  retrieveCached(request);
                if ( (result.getExceptionOccurred() ==null ) || (!result.getExceptionOccurred()) )
                    return result;
                throw new HTTPCachedException("Resending previous exception - "+ result.getExceptionInfo());
            }
            else {
                return retrieveUnCached(request);
            }
        }
        finally
        {
            lock.unlock();
        }

    }

    private HttpResult retrieveUnCached(HTTPRequest request) throws Exception  {
//        return  retriever.retrieveXML(request.getVerb(),
//                request.getLocation(),
//                request.getBody(),
//                request.getCookie(),
//                request.getPredicate(),
//                request.getTimeoutSeconds(),
//                request.getTimeoutSecondsOnRetry());

        return retriever.retrieve(request);
    }


    HttpResult retrieveCached(HTTPRequest request) throws ExceptionWithCookies, IOException, RedirectException {

        HttpResult result = getCached(request);
        if (result != null) {
            logger.debug("    * CACHED - "+request.getLocation());
            return result;
        }

        try {
            result = retrieveUnCached(request);
            result.setExceptionOccurred(false);
        }
        catch (Exception e){
            result = new HttpResult();
            result.setExceptionOccurred(true);
            result.setExceptionInfo(e.getClass().getSimpleName()+" - "+e.getMessage());
        }

        result.setURL(request.getLocation()); // in a re-direct, this can get changed -- use the finalURL()
        result.setLinkCheckJobId(request.getLinkCheckJobId());

        if (request.isSaveToCache())
            result = saveResult(result,request.getLocation());

        return result;
    }


    //at this point, no other threads are downloading this URL (see lock, above)
    //So, we can save this now with out issue.
    private    HttpResult  saveResult(HttpResult result,String originalLocation) {
        synchronized (lockingObject) {
            return httpResultRepo.save(result);
        }
    }

    //at this point, no other threads are downloading this URL (see lock, above)
    private HttpResult getCached(HTTPRequest request) {
        if (!request.isCacheUseOtherJobs()) {
            HttpResult result = httpResultRepo.findByLinkCheckJobIdAndURL(request.getLinkCheckJobId(), request.getLocation());
            return result;
        }

        HttpResult result = httpResultRepo.findFirstByURL(request.getLocation());
        return result;
    }
}
