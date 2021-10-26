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

package net.geocat.xml;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

@Component
@Scope("singleton")
public class XmlStringTools {

    public final static String UTF8_BOM = "\uFEFF";
    private final static Charset UTF8_CHARSET = Charset.forName("UTF-8");


    public static String trim(String s){
        String result = s.trim();

        if (result.startsWith(UTF8_BOM)) {
            result = result.substring(1).trim();
        }

        return result;
    }

    public static String bytea2String(byte[] bytes) {
        return trim(new String(bytes, UTF8_CHARSET));
    }

    public static boolean isXML(String doc) {
        try {
            if (!doc.startsWith("<?xml")) {
                // sometimes it doesn't start with the xml declaration
                doc =  trim(doc);
                if (!doc.startsWith("<"))
                    return false; //not xml
                if (doc.length() < 4)
                    return false;
                //flaky, is second char a letter?
                return Character.isLetter(doc.charAt(1));
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isXML(byte[] data) {
        return isXML(bytea2String(data));
    }
}
