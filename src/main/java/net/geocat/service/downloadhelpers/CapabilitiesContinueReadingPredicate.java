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

package net.geocat.service.downloadhelpers;

import net.geocat.http.IContinueReadingPredicate;
import net.geocat.xml.XmlStringTools;
import net.geocat.xml.helpers.CapabilitiesType;
import net.geocat.xml.helpers.CapabilityDeterminer;
import org.springframework.stereotype.Component;



import static net.geocat.xml.XmlStringTools.getNS;
import static net.geocat.xml.XmlStringTools.getPrefix;
import static net.geocat.xml.XmlStringTools.getRootTag;
import static net.geocat.xml.XmlStringTools.getTagName;
import static net.geocat.xml.XmlStringTools.replaceXMLDecl;
import static net.geocat.xml.XmlStringTools.removeComment;

@Component
public class CapabilitiesContinueReadingPredicate implements IContinueReadingPredicate {




    CapabilityDeterminer capabilityDeterminer;

    public CapabilitiesContinueReadingPredicate(CapabilityDeterminer capabilityDeterminer) {
        this.capabilityDeterminer = capabilityDeterminer;
    }

//    public static boolean isXML(String doc) {
//        try {
//            if (!doc.startsWith("<?xml")) {
//                // sometimes it doesn't start with the xml declaration
//                doc = doc.trim();
//                if (!doc.startsWith("<"))
//                    return false; //not xml
//                if (doc.length() < 4)
//                    return false;
//                //flaky, is second char a letter?
//                return Character.isLetter(doc.charAt(1));
//            }
//
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }



    @Override
    public boolean continueReading(byte[] head) {
        try {
            String doc = XmlStringTools.bytea2String(head) ;
            if (!XmlStringTools.isXML(doc))
                return false; //not XML

            doc = replaceXMLDecl(doc);
            doc = removeComment(doc);
            doc = removeDocType(doc);
            doc = getRootTag(doc).trim();

            String prefix = getPrefix(doc);
            String tag = getTagName(doc);
            String ns = getNS(prefix, doc);

            CapabilitiesType type = capabilityDeterminer.determineType(ns, tag);
            return true;
        } catch (Exception e) {
            int t = 0;
        }
        return false;
    }

    private String removeDocType(String doc) {
        return doc.replaceAll("<!DOCTYPE[\\s\\S]*?>]>","").trim();
    }


}
