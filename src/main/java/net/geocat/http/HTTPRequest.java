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

import org.slf4j.MDC;

public class HTTPRequest {

    public static String ACCEPTS_HEADER_XML = "application/xml,application/xml; q=1.0,text/xml; q=1.0,application/atom+xml; q=0.9";

    String verb = "GET";
    String location;
    String body = null;
    String cookie = null;
    IContinueReadingPredicate predicate = null;
    String acceptsHeader = "application/xml,application/xml; q=1.0,text/xml; q=1.0,application/atom+xml; q=0.9";

    boolean useCache = true;
    boolean saveToCache =true;
    boolean cacheUseOtherJobs = true;
    String linkCheckJobId;

    int timeoutSeconds = 20;  //vast majority will respond in this time..
    int timeoutSecondsOnRetry = 60; // some might take longer...

    public static HTTPRequest createGET(String location){
        String linkCheckJobId = MDC.get("JMSCorrelationID");
        HTTPRequest result = new HTTPRequest();
        result.setLocation(location);
        return result;
    }

    public static HTTPRequest createPOST(String location,String body){
        String linkCheckJobId = MDC.get("JMSCorrelationID");
        HTTPRequest result = new HTTPRequest();
        result.setLocation(location);
        result.setVerb("POST");
        result.setBody(body);
        return result;
    }


    public int getTimeoutSecondsOnRetry() {
        return timeoutSecondsOnRetry;
    }

    public void setTimeoutSecondsOnRetry(int timeoutSecondsOnRetry) {
        this.timeoutSecondsOnRetry = timeoutSecondsOnRetry;
    }


    public String getAcceptsHeader() {
        return acceptsHeader;
    }

    public void setAcceptsHeader(String acceptsHeader) {
        this.acceptsHeader = acceptsHeader;
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public IContinueReadingPredicate getPredicate() {
        return predicate;
    }

    public void setPredicate(IContinueReadingPredicate predicate) {
        this.predicate = predicate;
    }

    public boolean isUseCache() {
        return useCache;
    }

    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    public boolean isSaveToCache() {
        return saveToCache;
    }

    public void setSaveToCache(boolean saveToCache) {
        this.saveToCache = saveToCache;
    }

    public String getLinkCheckJobId() {
        return linkCheckJobId;
    }

    public void setLinkCheckJobId(String linkCheckJobId) {
        this.linkCheckJobId = linkCheckJobId;
    }

    public boolean isCacheUseOtherJobs() {
        return cacheUseOtherJobs;
    }

    public void setCacheUseOtherJobs(boolean cacheUseOtherJobs) {
        this.cacheUseOtherJobs = cacheUseOtherJobs;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}
