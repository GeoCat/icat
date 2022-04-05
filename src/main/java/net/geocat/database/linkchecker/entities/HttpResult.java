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

package net.geocat.database.linkchecker.entities;

import javax.persistence.*;
import java.util.List;


//simple object for caching the results of a HTTP request
//    - many times multiple documents will link to the same url
// see RetrievableSimpleLink
@Entity
@Table(name = "HttpResultCache"
        ,indexes = {
                @Index(
                        name = "idx_httpresultcache_url",
                        columnList = "URL",
                        unique = false
                ),
        @Index(
                name = "idx_httpresultcache_job_url",
                columnList = "linkCheckJobId,URL",
                unique = true
        ),
} )
public class HttpResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long httpResultId;

    // to group them by a particular linkcheck job
    // a) we don't want to cache too long, so this gives a lifecycle
    // b) makes it easy to delete after the run
    @Column(columnDefinition = "varchar(40)")
    String linkCheckJobId;

    //actual response from server
    @Column(columnDefinition = "bytea")
    byte[] data;

    //was the data fully read (i.e. not aborted because the response is the wrong type)
    boolean fullyRead;

    // http response code from server
    Integer httpCode;

    // what was the content type response from the server (header)
    @Column(columnDefinition = "text")
    String contentType;

    // allow a cookie to be sent to the server (required by some servers for security)
    @Column(columnDefinition = "text")
    String specialToSendCookie;

    // did an error occur during the request?
    boolean errorOccurred;

    //is the end request HTTPS?
    // (i.e. after redirects)
    boolean isHTTPS;

    //original URL
    @Column(columnDefinition = "text")
    String URL;

    //actual URL this came from (after redirects)
    @Column(columnDefinition = "text")
    String finalURL;

    //cookie, if sent
    @Column(columnDefinition = "text")
    String sentCookie;

    // cookies received from request (could be security tokens)
    @Column(columnDefinition = "text")
    String  receivedCookie;

    // is the SSL cert trusted by Java?
    boolean sslTrusted;

    //reason the SSL cert was untrusted
    @Column(columnDefinition = "text")
    String sslUnTrustedReason;

    Boolean exceptionOccurred;

    @Column(columnDefinition = "text")
    String exceptionInfo;

    public HttpResult() {

    }

    public HttpResult(HttpResult other) {
        this.httpResultId = other.httpResultId;
        this.linkCheckJobId = other.linkCheckJobId;
        this.data = other.data;
        this.fullyRead = other.fullyRead;
        this.httpCode = other.httpCode;
        this.contentType = other.contentType;
        this.specialToSendCookie = other.specialToSendCookie;
        this.errorOccurred = other.errorOccurred;
        this.isHTTPS = other.isHTTPS;
        this.URL = other.URL;
        this.finalURL = other.finalURL;
        this.sentCookie = other.sentCookie;
        this.receivedCookie = other.receivedCookie;
        this.sslTrusted = other.sslTrusted;
        this.sslUnTrustedReason = other.sslUnTrustedReason;
    }




    public HttpResult(byte[] data) {
        this.data = data;
    }

    public String getExceptionInfo() {
        return exceptionInfo;
    }

    public void setExceptionInfo(String exceptionInfo) {
        this.exceptionInfo = exceptionInfo;
    }

    public Boolean getExceptionOccurred() {
        return exceptionOccurred;
    }

    public void setExceptionOccurred(Boolean exceptionOccurred) {
        this.exceptionOccurred = exceptionOccurred;
    }

    public long getHttpResultId() {
        return httpResultId;
    }

    public void setHttpResultId(long httpResultId) {
        this.httpResultId = httpResultId;
    }

    public String getLinkCheckJobId() {
        return linkCheckJobId;
    }

    public void setLinkCheckJobId(String linkCheckJobId) {
        this.linkCheckJobId = linkCheckJobId;
    }

    public Boolean getFullyRead() {
        return fullyRead;
    }

    public void setFullyRead(Boolean fullyRead) {
        this.fullyRead = fullyRead;
    }

    public void setHttpCode(Integer httpCode) {
        this.httpCode = httpCode;
    }

    public Boolean getErrorOccurred() {
        return errorOccurred;
    }

    public void setErrorOccurred(Boolean errorOccurred) {
        this.errorOccurred = errorOccurred;
    }

    public Boolean getHTTPS() {
        return isHTTPS;
    }

    public void setHTTPS(Boolean HTTPS) {
        isHTTPS = HTTPS;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public Boolean getSslTrusted() {
        return sslTrusted;
    }

    public void setSslTrusted(Boolean sslTrusted) {
        this.sslTrusted = sslTrusted;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public boolean isFullyRead() {
        return fullyRead;
    }

    public void setFullyRead(boolean fullyRead) {
        this.fullyRead = fullyRead;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getSpecialToSendCookie() {
        return specialToSendCookie;
    }

    public void setSpecialToSendCookie(String specialToSendCookie) {
        this.specialToSendCookie = specialToSendCookie;
    }

    public boolean isErrorOccurred() {
        return errorOccurred;
    }

    public void setErrorOccurred(boolean errorOccurred) {
        this.errorOccurred = errorOccurred;
    }

    public boolean isHTTPS() {
        return isHTTPS;
    }

    public void setHTTPS(boolean HTTPS) {
        isHTTPS = HTTPS;
    }

    public String getFinalURL() {
        return finalURL;
    }

    public void setFinalURL(String finalURL) {
        this.finalURL = finalURL;
    }

    public String getSentCookie() {
        return sentCookie;
    }

    public void setSentCookie(String sentCookie) {
        this.sentCookie = sentCookie;
    }

    public  String  getReceivedCookie() {
        return receivedCookie;
    }

    public void setReceivedCookie( String receivedCookie) {
        this.receivedCookie = receivedCookie;
    }

    public boolean isSslTrusted() {
        return sslTrusted;
    }

    public void setSslTrusted(boolean sslTrusted) {
        this.sslTrusted = sslTrusted;
    }

    public String getSslUnTrustedReason() {
        return sslUnTrustedReason;
    }

    public void setSslUnTrustedReason(String sslUnTrustedReason) {
        this.sslUnTrustedReason = sslUnTrustedReason;
    }
}
