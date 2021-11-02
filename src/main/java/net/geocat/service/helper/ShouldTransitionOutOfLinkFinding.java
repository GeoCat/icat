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

package net.geocat.service.helper;

import net.geocat.database.linkchecker.service.MetadataDocumentService;
import net.geocat.service.MetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
// makes it so only ONE event is generated
public class ShouldTransitionOutOfLinkFinding
{
    @Autowired
    MetadataDocumentService metadataDocumentService;

    static Object lockObject = new Object();

    // linkCheckJobId ->   underlyingHarvestMetadataRecordId
    protected static Map<String, Long> completedLinkCheckJobs = new HashMap<>();


    // you MUST send the complete message if this returns true.
    // All future calls will return false.
    // This prevents the message from being sent twice.
    public boolean shouldSendMessage(String linkCheckJobId, Long underlyingHarvestMetadataRecordId) {

        synchronized (lockObject) {

            if (completedLinkCheckJobs.containsKey(linkCheckJobId))
                return false; // already sent
            boolean done = metadataDocumentService.completeLinkExtract(linkCheckJobId);
            if (!done)
                return false;
            // done - we need to prevent it from happening in the future
            completedLinkCheckJobs.put(linkCheckJobId,underlyingHarvestMetadataRecordId);
            return true;

        }
    }

}
