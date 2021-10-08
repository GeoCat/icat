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

package net.geocat.service.capabilities;

import net.geocat.service.ILinkFixer;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.List;

@Component
public class CapabilitiesLinkFixer implements ILinkFixer {


    public static String findQueryParmName(String link, String name) throws Exception {
        name = name.toLowerCase();
        URIBuilder uriBuilder = new URIBuilder(link);
        for (NameValuePair param : uriBuilder.getQueryParams()) {
            if (param.getName().toLowerCase().equals(name))
                return param.getName();
        }
        return null;
    }

    public static String canonicalize(String link) throws  Exception {
        if ( (link == null) || (link.isEmpty()) )
            return null;

        URIBuilder uriBuilder = new URIBuilder(link);
        List<NameValuePair> params =  uriBuilder.getQueryParams();
        params.sort( Comparator.comparing(x->x.getName()));
        uriBuilder.setParameters(params);
        return uriBuilder.build().toString();
    }

    @Override
    public String fix(String link) throws Exception {

        try {
            if (link == null)
                return link;

            link = link.replace("&amp;","&"); // this seems to happen a lot

            if (link.endsWith("?"))
                link += "request=GetCapabilities";

            String requestParam = findQueryParmName(link, "request");
            if (requestParam == null)
                return canonicalize(link);

            URIBuilder uriBuilder = new URIBuilder(link);
            uriBuilder.setParameter(requestParam, "GetCapabilities");
            return canonicalize(uriBuilder.build().toString());
        }
        catch(Exception e){
            return link;
        }
    }
}
