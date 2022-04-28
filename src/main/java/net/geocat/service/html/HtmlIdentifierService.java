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

package net.geocat.service.html;

import net.geocat.database.linkchecker.entities.CapabilitiesDatasetMetadataLink;
import net.geocat.database.linkchecker.entities.InspireSpatialDatasetIdentifier;
import net.geocat.database.linkchecker.entities.LocalDatasetMetadataRecord;
import net.geocat.database.linkchecker.entities.helper.DatasetIdentifier;
import net.geocat.database.linkchecker.entities.helper.DatasetMetadataRecordDatasetIdentifier;
import net.geocat.database.linkchecker.repos.CapabilitiesDatasetMetadataLinkRepo;
import net.geocat.database.linkchecker.repos.DatasetIdentifierRepo;
import net.geocat.database.linkchecker.repos.InspireSpatialDatasetIdentifierRepo;
import net.geocat.database.linkchecker.repos.LocalDatasetMetadataRecordRepo;
import net.geocat.database.linkchecker.repos.LocalServiceMetadataRecordRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class HtmlIdentifierService {

    @Autowired
    InspireSpatialDatasetIdentifierRepo inspireSpatialDatasetIdentifierRepo;

    @Autowired
    CapabilitiesDatasetMetadataLinkRepo capabilitiesDatasetMetadataLinkRepo;

//    @Autowired
//    LocalServiceMetadataRecordRepo localServiceMetadataRecordRepo;

    @Autowired
    DatasetIdentifierRepo datasetIdentifierRepo;


    public String getHtmlInput( ) {
        String result = "";

        result += "<h1>Search for a Dataset by Dataset Indentifier</h1><br>";

        result += "<script type='text/javascript'>";
        result += "function goToDiscover() {\n";
        result += "     var linkcheckjobid = document.getElementById('linkcheckjobid').value;\n";
        result += "     var code = document.getElementById('code').value;\n";
     //   result += "     var codespace = document.getElementById('codespace').value;\n";

//        result +=  "    var url = window.location.protocol + '//' +window.location.host+'/api/html/identifier/' + code+'/'+codespace;\n";
         result +=  "    var url = window.location.protocol + '//' +window.location.host+'/api/html/identifier?code=' + encodeURIComponent(code) ;\n";

        result +=  "    if (linkcheckjobid != '') {url += '&linkcheckjobid='+ linkcheckjobid;}";
        result +=  "    window.location = url;}\n";
        result += "</script>\n";

        result += "<table>";
        result += "<tr><td>link Check Job id:</td><td><input type='text' id='linkcheckjobid' /></td><td>blank=search all jobs</td></tr>";

        result += "<tr><td>code:</td><td><input type='text' id='code' /></td></tr>";
     //   result += "<tr><td>code space:</td><td><input type='text' id='codespace' /></td></tr>";

        result += "<tr><td></td><td><input type='submit' value='submit'  onclick='goToDiscover();'  /></td></tr>";
        result += "</table>";
        return result;
    }

    public String getHtml(String identifier, String codespace, String linkcheckjob) throws Exception {
        String result = "<head><meta charset=\"UTF-8\"></head>\n";
        if ((codespace !=null) && codespace.isEmpty())
            codespace = null;
        if ((linkcheckjob !=null) &&linkcheckjob.isEmpty())
            linkcheckjob = null;

        String linkCheckJobId = linkcheckjob;

        if ((identifier == null) || (identifier.isEmpty()))
            throw new Exception("identifier is null/empty");

        result += "<h1>Code: "+identifier+"</h1>\n";

        List<InspireSpatialDatasetIdentifier> inspireIds = inspireSpatialDatasetIdentifierRepo.findByCode(identifier);
        if (linkcheckjob !=null)
            inspireIds = inspireIds.stream()
                    .filter(x->x.getCapabilitiesDocument().getLinkCheckJobId().equals(linkCheckJobId))
                    .collect(Collectors.toList());
        result += "<h2>Inspire Spatial Dataset Identifier - Match to a Capabilities Document</h2>";
        if (inspireIds.isEmpty())
            result += "NO RESULTS<bR>";
        else {
            result += "<table border=1><tr><td>Linkcheck Job Id</td><td>Cap Type</td><td>Code</td><td>Namespace</td><td>SHA2</td></tr>";

            for (InspireSpatialDatasetIdentifier inspireId : inspireIds) {
                result += "<tr><td>"+inspireId.getCapabilitiesDocument().getLinkCheckJobId()+"</td>";
                result += "<td>"+inspireId.getCapabilitiesDocument().getCapabilitiesDocumentType()+"</td>";
                result += "<td>"+inspireId.getCode()+"</td><td>"+inspireId.getNamespace()+"</td>";
                String link = "<a href='/api/html/capabilities/" + inspireId.getCapabilitiesDocument().getLinkCheckJobId() + "/" + inspireId.getCapabilitiesDocument().getSha2()
                         + "'>" + inspireId.getCapabilitiesDocument().getSha2() + "</a><bR>\n";
                result += "<td>"+link+"</td></tr>";
            }
            result += "</table>";
        }




        List<CapabilitiesDatasetMetadataLink> layers = capabilitiesDatasetMetadataLinkRepo.findByIdentity(identifier);
        if (linkcheckjob !=null)
            layers = layers.stream()
                    .filter(x->x.getCapabilitiesDocument().getLinkCheckJobId().equals(linkCheckJobId))
                    .collect(Collectors.toList());
        result += "<h2>Inspire Spatial Dataset Identifier - From Layer in Capabilities Document</h2>";
        if (layers.isEmpty())
            result += "NO RESULTS<bR>";
        else {
            result += "<table border=1><tr><td>Linkcheck Job Id</td><td>Cap Type</td><td>Layer Name</td><td>Identity</td><td>Authority URL</td><td>Authority Name</td><td>SHA2</td></tr>";

            for (CapabilitiesDatasetMetadataLink layer : layers) {
                result += "<tr><td>"+layer.getCapabilitiesDocument().getLinkCheckJobId()+"</td>";
                result += "<td>"+layer.getCapabilitiesDocument().getCapabilitiesDocumentType()+"</td>";
                result += "<td>"+layer.getOgcLayerName()+"</td>";

                result += "<td>"+layer.getIdentity()+"</td><td>"+layer.getAuthority() +"</td><td>"+layer.getAuthorityName() +"</td>";
                String link = "<a href='/api/html/capabilities/" + layer.getCapabilitiesDocument().getLinkCheckJobId() + "/" + layer.getCapabilitiesDocument().getSha2()
                        + "'>" + layer.getCapabilitiesDocument().getSha2() + "</a><bR>\n";
                result += "<td>"+link+"</td></tr>";
//                result += "<a href='/api/html/capabilities/" + layer.getCapabilitiesDocument().getLinkCheckJobId() + "/" + layer.getCapabilitiesDocument().getSha2()
//                        + "'>" + layer.getCapabilitiesDocument().getLinkCheckJobId() + " :: " + layer.getCapabilitiesDocument().getSha2() + " :: " + layer.getOgcLayerName() + "</a><bR>\n";
                result += "</table>";

            }
        }

        List<DatasetIdentifier> datasetIDs = datasetIdentifierRepo.findByCode(identifier);
        if (linkcheckjob !=null)
            datasetIDs = datasetIDs.stream()
                    .filter( x-> x instanceof DatasetMetadataRecordDatasetIdentifier)
                    .filter(x->((DatasetMetadataRecordDatasetIdentifier)x).getDatasetMetadataRecord().getLinkCheckJobId().equals(linkCheckJobId))
                    .collect(Collectors.toList());
        result += "<h2>Datasets - by dataset ID</h2>";
        if (datasetIDs.isEmpty())
            result += "NO RESULTS<bR>";
        else {
            result += "<table border=1><tr><td>Linkcheck Job Id</td><td>File Identifier</td><td>Code</td><td>CodeSpace</td><td>Title</td></tr>";

            for (DatasetIdentifier datasetID : datasetIDs) {
                if (!(datasetID instanceof DatasetMetadataRecordDatasetIdentifier))
                    continue;
                DatasetMetadataRecordDatasetIdentifier _datasetID = (DatasetMetadataRecordDatasetIdentifier) datasetID;
                result += "<tr><td>"+_datasetID.getDatasetMetadataRecord().getLinkCheckJobId()+"</td>";

                String link = "<a href='/api/html/dataset/" + _datasetID.getDatasetMetadataRecord().getLinkCheckJobId() + "/" + _datasetID.getDatasetMetadataRecord().getFileIdentifier()
                        + "'>" +   _datasetID.getDatasetMetadataRecord().getFileIdentifier() + "</a> ";


                result += "<td>"+link+"</td>";
                result += "<td>"+_datasetID.getCode()+"</td>";
                result += "<td>"+_datasetID.getCodeSpace()+"</td> ";
                result += "<td>"+_datasetID.getDatasetMetadataRecord().getTitle()+"</td></tr>";

//                result += "<a href='/api/html/dataset/" + _datasetID.getDatasetMetadataRecord().getLinkCheckJobId() + "/" + _datasetID.getDatasetMetadataRecord().getFileIdentifier()
//                        + "'>" + _datasetID.getDatasetMetadataRecord().getLinkCheckJobId() + " :: " + _datasetID.getDatasetMetadataRecord().getFileIdentifier() + "</a><bR>\n";

            }
            result += "</table>";

        }

        return result;
    }
}
