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
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Component
@Scope("prototype")
@Qualifier("basicHTTPRetriever")
public class BasicHTTPRetriever implements IHTTPRetriever {

    private static final HostnameVerifier TRUST_ALL_HOSTNAME_VERIFIER = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true; // always good
        }
    };
    Logger logger = LoggerFactory.getLogger(BasicHTTPRetriever.class);
    int TIMEOUT_MS = 2 * 60 * 1000;
    int initialReadSize = 4096;

    public boolean shouldReadMore(byte[] tinyBuffer, IContinueReadingPredicate predicate) {
        if (predicate == null)
            return true;

        return predicate.continueReading(tinyBuffer);
    }

    public HttpResult retrieveJSON(String verb, String location, String body, String cookie, IContinueReadingPredicate predicate)
            throws IOException, SecurityException, ExceptionWithCookies, RedirectException
    {
        return retrieve(verb,location,body, cookie,predicate, "application/json","application/json");
    }

    /**
     * @param verb     GET or POST
     * @param location url
     * @param body     for POST, body
     * @param cookie   cookies to attach -   http.setRequestProperty("Cookie", cookie);
     * @return response from server
     * @throws Exception
     */
    public HttpResult retrieveXML(String verb, String location, String body, String cookie, IContinueReadingPredicate predicate)
            throws IOException, SecurityException, ExceptionWithCookies, RedirectException
    {
        return retrieve(verb,location,body, cookie,predicate, "application/xml",  "application/xml; q=1.0,text/xml; q=1.0,application/atom+xml; q=0.9");
    }


    /**
     * @param verb     GET or POST
     * @param location url
     * @param body     for POST, body
     * @param cookie   cookies to attach -   http.setRequestProperty("Cookie", cookie);
     * @return response from server
     * @throws Exception
     */
    public HttpResult retrieve (String verb, String location, String body, String cookie, IContinueReadingPredicate predicate,String contentType,String accept) throws IOException, SecurityException, ExceptionWithCookies, RedirectException {

        if (body == null)
            body = "";
        URL url = new URL(location);
        if (!url.getProtocol().equalsIgnoreCase("http") && (!url.getProtocol().equalsIgnoreCase("https")))
            throw new SecurityException("Security violation - url should be HTTP or HTTPS");

        if (!verb.equals("POST") && !verb.equals("GET"))
            throw new SecurityException("verb should be 'POST' or 'GET'");

        logger.debug("      * " + verb + " to " + location + " with body " + body.replace("\n", ""));

        boolean isHTTPs = url.getProtocol().equalsIgnoreCase("HTTPS");

        byte[] body_bytes = body.getBytes(StandardCharsets.UTF_8);
        byte[] response_bytes;

        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;

        VerifyingTrustingX509TrustManager verifyingTrustingX509TrustManager = null;

        if (http instanceof HttpsURLConnection) {
            HttpsURLConnection https = (HttpsURLConnection) http;
            verifyingTrustingX509TrustManager = VerifyingTrustingX509TrustManagerFactory.createVerifyingTrustingX509TrustManager();
            https.setSSLSocketFactory(
                    VerifyingTrustingX509TrustManagerFactory.getIndiscriminateSSLSocketFactory(verifyingTrustingX509TrustManager));
            https.setHostnameVerifier(TRUST_ALL_HOSTNAME_VERIFIER);
        }



        http.setConnectTimeout(TIMEOUT_MS);
        http.setReadTimeout(TIMEOUT_MS);
        http.setRequestMethod(verb);
        http.setDoOutput(true);
        http.setDoInput(true);
        if (verb.equals("POST")) {
            http.setFixedLengthStreamingMode(body_bytes.length);
           // http.setRequestProperty("Content-Type", "application/xml");
        }
        if (verb.equals("POST")) {
            http.setRequestProperty("Content-Type", contentType); // some servers don't like this set if there's no body
        }
        http.setRequestProperty("Accept", accept);
        if ((cookie != null) && (!cookie.isEmpty()))
            http.setRequestProperty("Cookie", cookie);

        String response;
        try {
            http.connect();
        } catch (IOException ioException) {
            throw ioException;
        }
        boolean fullyRead = false;
        int responseCode = -1;
        String responseMIME = "";
        try {
            // send body
            if (body_bytes.length > 0) {
                try (OutputStream os = http.getOutputStream()) {
                    os.write(body_bytes);
                }
            }

            // get response
            try (InputStream is = http.getInputStream()) {
                byte[] tinyBuffer = new byte[initialReadSize];
                int ntinyRead = IOUtils.read(is, tinyBuffer);
                byte[] bigBuffer = new byte[0];
                if (shouldReadMore(tinyBuffer, predicate)) {
                    fullyRead = true;
                    bigBuffer = IOUtils.toByteArray(is);
                }
                response_bytes = new byte[ntinyRead + bigBuffer.length];
                System.arraycopy(tinyBuffer, 0, response_bytes, 0, ntinyRead);
                System.arraycopy(bigBuffer, 0, response_bytes, ntinyRead, bigBuffer.length);
                int t = 0;
            }
        } catch (IOException ioException) {
            List<String> cookies = http.getHeaderFields().get("Set-Cookie");
            InputStream errorStream = http.getErrorStream();
            byte[] errorBuffer = new byte[0];
            if (errorStream != null) {
                errorBuffer = new byte[initialReadSize];
                int nRead = IOUtils.read(errorStream, errorBuffer);
                if (nRead != initialReadSize) {
                    errorBuffer = Arrays.copyOf(errorBuffer, nRead);
                }
            }
            HttpResult errorResult = new HttpResult(errorBuffer);
            errorResult.setURL(location);
            errorResult.setHTTPS(isHTTPs);
            errorResult.setSslTrusted(true);
            if (verifyingTrustingX509TrustManager != null) {
                if (!verifyingTrustingX509TrustManager.clientTrusted || !verifyingTrustingX509TrustManager.serverTrusted) {
                    String error = "";
                    if (verifyingTrustingX509TrustManager.clientTrustedException != null) {
                        error += "CLIENT: " + verifyingTrustingX509TrustManager.clientTrustedException.getClass().getSimpleName() + " -- " + verifyingTrustingX509TrustManager.clientTrustedException.getMessage() + "\n";
                    }
                    if (verifyingTrustingX509TrustManager.serverTrustedException != null) {
                        error += "SERVER: " + verifyingTrustingX509TrustManager.serverTrustedException.getClass().getSimpleName() + " -- " + verifyingTrustingX509TrustManager.serverTrustedException.getMessage() + "\n";
                    }
                    errorResult.setSslUnTrustedReason(error);
                    errorResult.setSslTrusted(false);

                }
            }
            errorResult.setFinalURL(url.toString());
            if (cookies != null)
                errorResult.setReceivedCookie(String.join("\n",cookies));
            errorResult.setSentCookie(cookie);
            errorResult.setFullyRead(false);
            errorResult.setErrorOccurred(true);
            errorResult.setHttpCode(http.getResponseCode());
            errorResult.setContentType(http.getHeaderField("Content-Type"));
            if ((cookies != null) && (!cookies.isEmpty()))
                 errorResult.setSpecialToSendCookie(cookies.get(0));
            return errorResult;

//            if ((cookies == null) || (cookies.isEmpty()))
//                throw ioException;
//            throw new ExceptionWithCookies(ioException.getMessage(), cookies.get(0), ioException);
        } finally {
            responseCode = http.getResponseCode();
            responseMIME = http.getHeaderField("Content-Type");
            http.disconnect();
        }

        if (http.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM || http.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
            String newUrl = http.getHeaderField("Location");
            throw new RedirectException("redirect requested", newUrl);
        }
        //logger.debug("      * FINISHED " + verb + " to " + location + " with body " + body.replace("\n", ""));
        List<String> cookies = http.getHeaderFields().get("Set-Cookie");

        HttpResult result = new HttpResult(response_bytes);
        result.setURL(location);

        result.setHTTPS(isHTTPs);
        result.setSslTrusted(true);
        if (verifyingTrustingX509TrustManager != null) {
            if (!verifyingTrustingX509TrustManager.clientTrusted || !verifyingTrustingX509TrustManager.serverTrusted) {
                String error = "";
                if (verifyingTrustingX509TrustManager.clientTrustedException != null) {
                    error += "CLIENT: " + verifyingTrustingX509TrustManager.clientTrustedException.getClass().getSimpleName() + " -- " + verifyingTrustingX509TrustManager.clientTrustedException.getMessage() + "\n";
                }
                if (verifyingTrustingX509TrustManager.serverTrustedException != null) {
                    error += "SERVER: " + verifyingTrustingX509TrustManager.serverTrustedException.getClass().getSimpleName() + " -- " + verifyingTrustingX509TrustManager.serverTrustedException.getMessage() + "\n";
                }
                result.setSslUnTrustedReason(error);
                result.setSslTrusted(false);
            }
        }
        if (cookies != null)
            result.setReceivedCookie(String.join("\n",cookies));
        result.setSentCookie(cookie);
        result.setFinalURL(url.toString());
        result.setFullyRead(fullyRead);
        result.setHttpCode(responseCode);
        result.setContentType(responseMIME);
        return result;
    }

}
