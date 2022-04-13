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

import net.geocat.database.linkchecker.entities.LocalDatasetMetadataRecord;
import net.geocat.database.linkchecker.entities.LocalServiceMetadataRecord;
import net.geocat.database.linkchecker.entities.ServiceDocumentLink;
import net.geocat.database.linkchecker.entities.helper.DocumentLink;
import net.geocat.database.linkchecker.entities.helper.LinkState;
import net.geocat.eventprocessor.processors.processlinks.EventProcessor_ProcessServiceDocLinksEvent;
import net.geocat.service.capabilities.CapabilitiesDownloadingService;
import net.geocat.service.capabilities.CapabilitiesLinkFixer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class DocumentLinkToCapabilitiesProcessor {

    Logger logger = LoggerFactory.getLogger(DocumentLinkToCapabilitiesProcessor.class);

    @Autowired
    CapabilitiesLinkFixer capabilitiesLinkFixer;

    @Autowired
    CapabilitiesDownloadingService capabilitiesDownloadingService;

    public void processDocumentLinks( LocalServiceMetadataRecord localServiceMetadataRecord) throws Exception {
        List<? extends DocumentLink> links = new ArrayList<>(localServiceMetadataRecord.getServiceDocumentLinks());
        long docid = localServiceMetadataRecord.getServiceMetadataDocumentId();
        String serviceRecordType = localServiceMetadataRecord.getMetadataServiceType();
        processDocumentLinks(links,docid,serviceRecordType);
    }

    public void processDocumentLinks( LocalDatasetMetadataRecord localDatasetMetadataRecord) throws Exception {
        List<? extends DocumentLink> links = new ArrayList<>(localDatasetMetadataRecord.getDocumentLinks());
        links = links.stream()
                .filter(x->x.isInspireSimplifiedLink())
                .collect(Collectors.toList());
        long docid = localDatasetMetadataRecord.getDatasetMetadataDocumentId();
        String serviceRecordType = null; // no service record -> no idea what type of service this is
        processDocumentLinks(links,docid,serviceRecordType);
    }

    public void processDocumentLinks(List<? extends DocumentLink> links, long documentId, String serviceRecordType) throws Exception {

        int nlinks = links.size();

        fixURLs(links,serviceRecordType);
        int linkIdx = 0;
        //  logger.trace("processing "+nlinks+" service document links for documentid="+getInitiatingEvent().getServiceMetadataId());
        List<String> processedURLs = new ArrayList<>();
        for (DocumentLink link : links) {
            logger.debug("processing  document link "+linkIdx+" of "+nlinks+" links for  documentid="+documentId);
            linkIdx++;
            String thisURL = link.getFixedURL();
            if (processedURLs.contains(thisURL))
            {
                // logger.debug("this url has already been processed - no action!");
                link.setLinkState(LinkState.Redundant);
            }
            else {
                processedURLs.add(link.getFixedURL());
                capabilitiesDownloadingService.handleLink(link);
            }
        }
        logger.trace("FINISHED processing service document links for documentid="+documentId);
    }

    // get a more cannonical list of URLs -- this way we can tell if they are the same easier...
    private List<String> fixURLs(List<? extends DocumentLink> links, String serviceRecordType) {
        return links.stream()
                .map(x-> {
                    try {
                        x.setFixedURL(capabilitiesLinkFixer.fix(x.getRawURL(),serviceRecordType,x));
                    } catch (Exception e) {
                        e.printStackTrace();
                        x.setFixedURL(x.getRawURL());
                    }
                    return x;
                })
                .map(x->x.getFixedURL())
                .collect(Collectors.toList());
    }


}
