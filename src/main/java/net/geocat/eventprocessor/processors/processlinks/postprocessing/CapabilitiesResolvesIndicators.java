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

import net.geocat.database.linkchecker.entities.*;
import net.geocat.database.linkchecker.entities.helper.DocumentLink;
import net.geocat.database.linkchecker.entities.helper.MetadataRecord;
import net.geocat.database.linkchecker.entities.helper.SHA2JobIdCompositeKey;
import net.geocat.database.linkchecker.repos.CapabilitiesDocumentRepo;
import net.geocat.service.capabilities.DatasetLink;
import net.geocat.xml.helpers.CapabilitiesType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Component
@Scope("prototype")
public class CapabilitiesResolvesIndicators {


    @Autowired
    CapabilitiesDocumentRepo capabilitiesDocumentRepo;

    public LocalServiceMetadataRecord process(LocalServiceMetadataRecord record,List<CapabilitiesDocument> capDocs) {
        List<DocumentLink> links = new ArrayList<DocumentLink>(record.getServiceDocumentLinks());
        process(record,links,capDocs);
        return record;
    }

    public LocalDatasetMetadataRecord process (LocalDatasetMetadataRecord record,List<CapabilitiesDocument> capDocs) {
        List<DocumentLink> links = new ArrayList<DocumentLink>(record.getDocumentLinks());
        process(record,links,capDocs);
        return record;
    }


    public static List<String> unique(List<String> list) {
        HashSet hs = new HashSet();
        hs.addAll(list);
        list.clear();
        list.addAll(hs);
        return list;
    }

    //populates
    //Integer INDICATOR_RESOLVES_TO_CAPABILITIES;
    //CapabilitiesType INDICATOR_CAPABILITIES_TYPE;
    public void process(MetadataRecord record, List<DocumentLink> links,List<CapabilitiesDocument> capDocs) {

        if ( (links ==null) || (links.isEmpty()) )
            return;

        int nCapDocs = (int) capDocs.size();
        record.setINDICATOR_RESOLVES_TO_CAPABILITIES(nCapDocs);

        if (nCapDocs ==0)
            return; //nothing more to do



        List<CapabilitiesType> allTypes = capDocs.stream()
                 .map(x->x.getCapabilitiesDocumentType())
                .collect(Collectors.toList());

        if (nCapDocs == 1){
            record.setINDICATOR_CAPABILITIES_TYPE(allTypes.get(0));
            return;
        }
        CapabilitiesType type =   mostCommon(allTypes);
        record.setINDICATOR_CAPABILITIES_TYPE(type);
    }

    public CapabilitiesType mostCommon(List<CapabilitiesType> list){
        Map<CapabilitiesType, Long> frequencyMap = list.stream().collect(Collectors.groupingBy(x->x, Collectors.counting()));

        long mostFrequentN = frequencyMap.entrySet().stream().max(Map.Entry.comparingByValue()).get().getValue();

        CapabilitiesType result = frequencyMap.entrySet().stream().filter(x->x.getValue()==mostFrequentN).findFirst().get().getKey();

        return result;
    }
}
