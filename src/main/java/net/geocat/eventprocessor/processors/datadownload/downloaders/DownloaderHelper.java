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

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.List;

public class DownloaderHelper {

    public static String fixBaseURL(String rawUrl){
        if (rawUrl == null)
            return  null;

        rawUrl = rawUrl.trim();
        if (!rawUrl.contains("?"))
            rawUrl+= "?";
        if (rawUrl.endsWith("&"))
            rawUrl = rawUrl.substring(0,rawUrl.length()-1);
        return rawUrl;
    }
    //put params in alphabetical order
    public static String cannonicalize(String url) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(url);
        List<NameValuePair> params =  uriBuilder.getQueryParams();
        params.sort( Comparator.comparing(x->x.getName()));
        uriBuilder.setParameters(params);

        return  uriBuilder.build().toString();

    }

    public static String findQueryParamName(String url, String name) throws Exception {
        name = name.toLowerCase();
        URIBuilder uriBuilder = new URIBuilder(url);
        for (NameValuePair param : uriBuilder.getQueryParams()) {
            if (param.getName().toLowerCase().equals(name))
                return param.getName();
        }
        return null;
    }

    public static String setParameter(String url, String paramName, String paramVal) throws Exception {
        String existingParamName = findQueryParamName(url,paramName);
        if (existingParamName == null)
            existingParamName = paramName;

        URIBuilder uriBuilder = new URIBuilder(url);
        uriBuilder.setParameter(existingParamName, paramVal);
        return  uriBuilder.build().toString();
    }

    public static boolean isSame(byte[] data, byte[] pattern) {
        if (data== null)
            return false;
        if (data.length<pattern.length)
            return false;
        int idx = 0;
        for (byte b: pattern){
            if (data[idx] != b)
                return false;
            idx++;
        }
        return true;
    }

    public static boolean isRecognizedImage(byte[] result){
        if ( (result == null) || (result.length < 8) )
            return false;

        //png
        if (isSame(result, new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}))
            return true;

        if (   (result.length < 12) )
            return false;

        //jpeg
        if (isSame(result, new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01}))
            return true;
        return false;
    }
}
