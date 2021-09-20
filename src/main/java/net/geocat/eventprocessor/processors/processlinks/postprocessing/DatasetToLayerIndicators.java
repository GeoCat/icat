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

import net.geocat.database.linkchecker.entities.CapabilitiesRemoteDatasetMetadataDocument;
import net.geocat.database.linkchecker.entities.LocalDatasetMetadataRecord;
import net.geocat.database.linkchecker.entities.helper.DocumentLink;
import net.geocat.database.linkchecker.entities.helper.IndicatorStatus;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class DatasetToLayerIndicators {


    public LocalDatasetMetadataRecord process (LocalDatasetMetadataRecord record) {
        if (record.getFileIdentifier() == null || record.getDatasetIdentifier() == null) {// nothing to do
            record.setINDICATOR_LAYER_MATCHES(IndicatorStatus.FAIL);
            return record;
        }

        List<DocumentLink> links = new ArrayList<DocumentLink>(record.getDocumentLinks());

        List<CapabilitiesRemoteDatasetMetadataDocument>  capDatasetDocs =  record.getDocumentLinks().stream()
                .map(x->x.getCapabilitiesDocument())
                .filter(x-> x != null)
                .map(x->x.getCapabilitiesDatasetMetadataLinkList())
                .flatMap(List::stream)
                .filter(x-> x != null)
                .map(x->x.getCapabilitiesRemoteDatasetMetadataDocument())
                .filter(x-> x != null)
                .filter(x->x.getDatasetIdentifier() !=null && x.getFileIdentifier() !=null)
                .collect(Collectors.toList());

        //check to see if there's a match
       boolean match = capDatasetDocs.stream()
                .anyMatch(x->x.getFileIdentifier().equals(record.getFileIdentifier()) && x.getDatasetIdentifier().equals(record.getDatasetIdentifier()));

       if (match) {
           record.setINDICATOR_LAYER_MATCHES(IndicatorStatus.PASS);
       }
       else {
           record.setINDICATOR_LAYER_MATCHES(IndicatorStatus.FAIL);

       }

        return record;
    }

}