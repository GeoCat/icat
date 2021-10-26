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

package net.geocat.eventprocessor.processors.processlinks.postprocessing;

import net.geocat.database.linkchecker.entities.CapabilitiesDatasetMetadataLink;
import net.geocat.database.linkchecker.entities.CapabilitiesDocument;
import net.geocat.database.linkchecker.entities.LocalServiceMetadataRecord;
import net.geocat.database.linkchecker.entities.helper.DocumentLink;
import net.geocat.database.linkchecker.entities.helper.IndicatorStatus;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class CapabilitiesDatasetLinksResolveIndicators {

    public LocalServiceMetadataRecord process(LocalServiceMetadataRecord record,List<CapabilitiesDocument> capDocs ) {
        List<DocumentLink> links = new ArrayList<DocumentLink>(record.getServiceDocumentLinks());


        boolean result = false;

        for(CapabilitiesDocument doc  : capDocs) {
            result = result || process(doc);
        }

        if (result)
            record.setINDICATOR_ALL_CAPABILITIES_LAYER_RESOLVE(IndicatorStatus.PASS);
        else
            record.setINDICATOR_ALL_CAPABILITIES_LAYER_RESOLVE(IndicatorStatus.FAIL);

        return record;
    }

    private boolean process(CapabilitiesDocument doc) {
        List<CapabilitiesDatasetMetadataLink> dsLinks = doc.getCapabilitiesDatasetMetadataLinkList();

         //get rid of null links (i.e. no url)
         dsLinks = dsLinks.stream().filter(x->x.getRawURL() != null && !x.getRawURL().isEmpty()).collect(Collectors.toList());

        int nExpect = dsLinks.size();

        dsLinks =  dsLinks.stream().filter(x-> x.getSha2() != null).collect(Collectors.toList());

        if (dsLinks.isEmpty())
            return false; // must have at least one

        if (nExpect != dsLinks.size())
            return false;

        return true;
    }

}
