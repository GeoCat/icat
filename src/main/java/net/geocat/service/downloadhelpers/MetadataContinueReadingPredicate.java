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

import static net.geocat.service.downloadhelpers.CapabilitiesContinueReadingPredicate.*;
import static net.geocat.xml.XmlStringTools.getNS;
import static net.geocat.xml.XmlStringTools.getPrefix;
import static net.geocat.xml.XmlStringTools.getRootTag;
import static net.geocat.xml.XmlStringTools.getTagName;
import static net.geocat.xml.XmlStringTools.removeDocType;
import static net.geocat.xml.XmlStringTools.replaceXMLDecl;

public class MetadataContinueReadingPredicate implements IContinueReadingPredicate {
    @Override
    public ContinueReading continueReading(byte[] head) {
        try {
            String doc = XmlStringTools.bytea2String(head);
            if (!XmlStringTools.isXML(doc))
                return ContinueReading.STOP_READING; //not XML

            doc = replaceXMLDecl(doc).trim();
            doc = removeDocType(doc);
            doc = getRootTag(doc).trim();

            String prefix = getPrefix(doc);
            String tag = getTagName(doc);
            String ns = getNS(prefix, doc);

            if (tag.equals("MD_Metadata") || tag.equals("GetRecordsResponse") || tag.equals("GetRecordByIdResponse")) {
                return ContinueReading.CONTINUE_READING;
            }
            return ContinueReading.STOP_READING;

        } catch (Exception e) {
            return ContinueReading.STOP_READING;
        }
    }
}
