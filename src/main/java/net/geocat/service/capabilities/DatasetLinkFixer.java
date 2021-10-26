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

@Component
public class DatasetLinkFixer implements ILinkFixer {


    public static String getQueryParam(String link, String name) throws Exception {
        name = name.toLowerCase();
        URIBuilder uriBuilder = new URIBuilder(link);

        for (NameValuePair param : uriBuilder.getQueryParams()) {
            if (param.getName().equals(name))
                return param.getValue();
        }
        return null;
    }

    @Override
    public String fix(String link) throws Exception {
        try {
            if (link == null)
                return link;

            link = link.replace("&amp;","&"); // this seems to happen a lot


            String requestParam = CapabilitiesLinkFixer.findQueryParmName(link, "request");
            if (requestParam == null)
                return link;

            String request = getQueryParam(link, requestParam);
            if ( (request == null) || (!request.equalsIgnoreCase("GetRecordById")) )
                return link; //only process GetRecordById requests

            String outputSchemaParam = CapabilitiesLinkFixer.findQueryParmName(link, "outputSchema");
            String elementSetNameParam = CapabilitiesLinkFixer.findQueryParmName(link, "elementSetName");


            URIBuilder uriBuilder = new URIBuilder(link);
            if (outputSchemaParam == null)
                uriBuilder.setParameter("outputSchema", "http://www.isotc211.org/2005/gmd");
            if (elementSetNameParam == null)
                uriBuilder.setParameter("elementSetName", "full");
            return uriBuilder.build().toString();
        }
        catch(Exception e){
            return link;
        }
    }

}
