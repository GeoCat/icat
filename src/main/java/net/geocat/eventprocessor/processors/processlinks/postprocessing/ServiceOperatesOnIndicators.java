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


import net.geocat.database.harvester.repos.BlobStorageRepo;
import net.geocat.database.linkchecker.entities.*;
import net.geocat.database.linkchecker.entities.helper.DatasetIdentifier;
import net.geocat.database.linkchecker.entities.helper.IndicatorStatus;
import net.geocat.database.linkchecker.repos.LinkCheckBlobStorageRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class ServiceOperatesOnIndicators {


    @Autowired
    LinkCheckBlobStorageRepo linkCheckBlobStorageRepo;

    @Autowired
    BlobStorageRepo harvestBlogStorageRepo;


    public LocalServiceMetadataRecord process(LocalServiceMetadataRecord record, List<CapabilitiesDocument> capDocs) {
        List<OperatesOnLink> links = new ArrayList<OperatesOnLink>(record.getOperatesOnLinks());

        List<OperatesOnLink> goodLinks = links.stream()
                .filter(x->x.getSha2() != null)
                .collect(Collectors.toList());

        if ( (links.size() == goodLinks.size()) && (!links.isEmpty()) ) {
            record.setINDICATOR_ALL_OPERATES_ON_RESOLVE(IndicatorStatus.PASS);
        }
        else {
            record.setINDICATOR_ALL_OPERATES_ON_RESOLVE(IndicatorStatus.FAIL);
            record.setINDICATOR_ALL_OPERATES_ON_MATCH_CAPABILITIES(IndicatorStatus.FAIL);// cannot be true

        }

        List<CapabilitiesDatasetMetadataLink>  capDatasetDocLinks =  capDocs.stream()
                .map(x->x.getCapabilitiesDatasetMetadataLinkList())
                .flatMap(List::stream)
                .filter(x-> x != null && x.getFileIdentifier() !=null && ((x.getDatasetIdentifiers() != null) && (!x.getDatasetIdentifiers().isEmpty()) ))
                .collect(Collectors.toList());



        boolean allMatch =true;
        for (OperatesOnLink operatesOnLink : goodLinks) {
            allMatch = allMatch && matches(operatesOnLink,capDatasetDocLinks);
        }

        if (allMatch)
            record.setINDICATOR_ALL_OPERATES_ON_MATCH_CAPABILITIES(IndicatorStatus.PASS);
        else
            record.setINDICATOR_ALL_OPERATES_ON_MATCH_CAPABILITIES(IndicatorStatus.FAIL);

        return record;
    }

    public boolean matches(List  list1, List  list2) {
        if ((list1 ==null) || list1.isEmpty() || (list2 ==null) || list2.isEmpty())
                return false;
        for(Object _id1 : list1) {
            for (Object _id2:list2) {
                DatasetIdentifier id1 = (DatasetIdentifier) _id1;
                DatasetIdentifier id2 = (DatasetIdentifier) _id2;
                if ((id1==null) || (id2==null) ||(id1.getCode()==null) || (id2.getCode()==null))
                    continue;

                if (id1.getCode().equals(id2.getCode()))
                    return true;
            }
        }
        return false;
    }

    public boolean matches(OperatesOnLink operatesOnLink,  List<CapabilitiesDatasetMetadataLink>  capDatasetDocLinks) {
        CapabilitiesDatasetMetadataLink result= capDatasetDocLinks.stream()
                .filter(x-> x.getFileIdentifier().equals(operatesOnLink.getFileIdentifier()) && matches(x.getDatasetIdentifiers(),operatesOnLink.getDatasetIdentifiers())  )
                .findFirst( )
                .orElse(null);
        return result != null;
    }

//
//    private boolean process(OperatesOnRemoteDatasetMetadataRecord localOpsOnDatasetRecord) {
//        return  localOpsOnDatasetRecord.getINDICATOR_MATCHES_A_CAP_DATASET_LAYER() == IndicatorStatus.PASS;
//    }
//
//    private void preprocess(OperatesOnRemoteDatasetMetadataRecord localOpsOnDatasetRecord,List<CapabilitiesRemoteDatasetMetadataDocument> capDocs) {
//        //we are going to do a search and see if localDataset is mentioned one of the CapabilitiesRemoteDatasetMetadataDocument
//        boolean matches = capDocs.stream()
//                .anyMatch(x->x.getFileIdentifier().equals(localOpsOnDatasetRecord.getFileIdentifier()) && x.getDatasetIdentifier().equals(localOpsOnDatasetRecord.getDatasetIdentifier()));
//        if (matches)
//            localOpsOnDatasetRecord.setINDICATOR_MATCHES_A_CAP_DATASET_LAYER(IndicatorStatus.PASS);
//        else
//            localOpsOnDatasetRecord.setINDICATOR_MATCHES_A_CAP_DATASET_LAYER(IndicatorStatus.FAIL);
//    }




}
