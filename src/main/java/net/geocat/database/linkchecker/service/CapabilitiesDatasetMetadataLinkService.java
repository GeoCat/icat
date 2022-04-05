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

package net.geocat.database.linkchecker.service;

import net.geocat.database.linkchecker.entities.CapabilitiesDatasetMetadataLink;
import net.geocat.database.linkchecker.entities.CapabilitiesDocument;
import net.geocat.database.linkchecker.entities.helper.LinkState;
import net.geocat.service.capabilities.DatasetLink;
import net.geocat.service.capabilities.DatasetLinkFixer;
import net.geocat.xml.XmlCapabilitiesDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
public class CapabilitiesDatasetMetadataLinkService {

    @Autowired
    DatasetLinkFixer datasetLinkFixer;

    public List<CapabilitiesDatasetMetadataLink> createCapabilitiesDatasetMetadataLinks(CapabilitiesDocument cap, XmlCapabilitiesDocument doc) throws Exception {
        List<CapabilitiesDatasetMetadataLink> result = new ArrayList<>();
        for (DatasetLink link : doc.getDatasetLinksList()) {

            CapabilitiesDatasetMetadataLink item = new CapabilitiesDatasetMetadataLink();
            item.setOgcLayerName(link.getOgcLayerName());
            item.setLinkState(LinkState.Created);
            item.setRawURL(link.getRawUrl());
            item.setLinkCheckJobId(cap.getLinkCheckJobId());
            item.setFixedURL(datasetLinkFixer.fix(link.getRawUrl()));
            item.setIdentity(link.getIdentifier());
            item.setAuthority(link.getAuthority());
            item.setAuthorityName(link.getAuthorityName());
            item.setCapabilitiesDocument(cap);

         //   item.setCapabilitiesDocument(cap);
            result.add(item);
        }
        return result;
    }
}
