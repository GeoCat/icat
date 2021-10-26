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

package net.geocat.service;

import net.geocat.database.linkchecker.entities.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
public class HumanReadableServiceMetadata {


    public String getHumanReadable(LocalServiceMetadataRecord serviceMetadataRecord) {
        try {
            String result = "Summary of LocalServiceMetadataRecord\n";
            result += "----------------------------------------------\n\n";
            result += serviceMetadataRecord.toString() + "\n";
            result += "----------------------------------------------\n\n";
            result += "There are " + serviceMetadataRecord.getNumberOfLinksFound() + " links in the service document:\n\n";

            int idx = 0;
            for ( ServiceDocumentLink link : serviceMetadataRecord.getServiceDocumentLinks()) {
           // for (int idx = 0; idx < serviceMetadataRecord.getNumberOfLinksFound(); idx++) {
            //    ServiceDocumentLink link = serviceMetadataRecord.getServiceDocumentLinks().get(idx);
                result += "Link #" + idx + "\n";
                result += "--------\n\n";
                result += link.toString() + "\n\n";
                if (link.getCapabilitiesDocument() != null) {
                    CapabilitiesDocument capabilitiesDocument = link.getCapabilitiesDocument();
                    result += "\nCAPABILITIES DOCUMENT\n";
                    result += "---------------------\n\n";
                    result += capabilitiesDocument.toString() + "\n\n";
                    if (capabilitiesDocument.getRemoteServiceMetadataRecordLink() != null) {
                        RemoteServiceMetadataRecordLink remoteServiceMetadataRecordLink = capabilitiesDocument.getRemoteServiceMetadataRecordLink();
                        result += "Capabilities Document link to Service Record\n";
                        result += "--------------------------------------------\n\n";
                        result += remoteServiceMetadataRecordLink.toString() + "\n\n";
//                        if (remoteServiceMetadataRecordLink.getRemoteServiceMetadataRecord() != null) {
//                            RemoteServiceMetadataRecord remoteServiceMetadataRecord = remoteServiceMetadataRecordLink.getRemoteServiceMetadataRecord();
//                            result += "Capabilities Document Service Record\n";
//                            result += "------------------------------------\n\n";
//                            result += remoteServiceMetadataRecord.toString() + "\n\n";
//
//                        }
                    }
                    if (!capabilitiesDocument.getCapabilitiesDatasetMetadataLinkList().isEmpty()) {
                        result += "Capabilities Document links to Datasets\n";
                        result += "--------------------------------------------\n\n";
                        List<CapabilitiesDatasetMetadataLink> list = capabilitiesDocument.getCapabilitiesDatasetMetadataLinkList();
                        for (int idx2 = 0; idx2 < list.size(); idx2++) {
                            CapabilitiesDatasetMetadataLink link2 = list.get(idx2);
                            result += "Capabilities document Link #" + idx2 + " to dataset MD\n";
                            result += "------------------------------------------------\n\n";
                            result += link2.toString() + "\n\n";
//                            if (link2.getCapabilitiesRemoteDatasetMetadataDocument() != null) {
//                                CapabilitiesRemoteDatasetMetadataDocument capabilitiesRemoteDatasetMetadataDocument = link2.getCapabilitiesRemoteDatasetMetadataDocument();
//                                result += "Capabilities Document Dataset MD Record\n";
//                                result += "----------------------------------------\n\n";
//                                result += capabilitiesRemoteDatasetMetadataDocument.toString() + "\n\n";
//                            }
                        }
                    }
                }
                idx++;
            }

            result += "\n\n";
            result += "=================================================\n\n";
            result += "There are " + serviceMetadataRecord.getNumberOfOperatesOnFound() + " OperatesOn links in the service document:\n\n";

            idx=0;
            for (OperatesOnLink link:serviceMetadataRecord.getOperatesOnLinks()) {


           // for (int idx = 0; idx < serviceMetadataRecord.getNumberOfOperatesOnFound(); idx++) {
           //     OperatesOnLink link = serviceMetadataRecord.getOperatesOnLinks().get(idx);
                result += "OperatesOn Link #" + idx + "\n";
                result += "-------------------\n\n";
                result += link.toString() + "\n\n";
//                if (link.getDatasetMetadataRecord() != null) {
//                    OperatesOnRemoteDatasetMetadataRecord operatesOnRemoteDatasetMetadataRecord = link.getDatasetMetadataRecord();
//                    result += "OperatesOn Dataset MD Record\n";
//                    result += "----------------------------------------\n\n";
//                    result += operatesOnRemoteDatasetMetadataRecord.toString() + "\n\n";
//                }
                idx++;
            }
            result += "=================================================\n\n";

            return result;
        }
        catch (Exception e){
            return "error occurred - "+e.getClass().getSimpleName()+" - "+e.getMessage();
        }
    }


}
