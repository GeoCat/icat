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

package net.geocat.events;

import net.geocat.database.linkchecker.entities.LinkCheckJob;
import net.geocat.database.linkchecker.repos.LinkCheckJobRepo;
import net.geocat.events.findlinks.LinksFoundInAllDocuments;
import net.geocat.events.findlinks.ProcessLocalMetadataDocumentEvent;
import net.geocat.events.findlinks.StartProcessDocumentsEvent;
import net.geocat.events.postprocess.AllPostProcessingCompleteEvent;
import net.geocat.events.postprocess.PostProcessDatasetDocumentEvent;
import net.geocat.events.postprocess.PostProcessServiceDocumentEvent;
import net.geocat.events.postprocess.StartPostProcessEvent;
import net.geocat.events.processlinks.AllLinksCheckedEvent;
import net.geocat.events.processlinks.ProcessDatasetDocLinksEvent;
import net.geocat.events.processlinks.ProcessServiceDocLinksEvent;
import net.geocat.events.processlinks.StartLinkProcessingEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Scope("prototype")
public class EventFactory {

    @Autowired
    LinkCheckJobRepo linkCheckJobRepo;


    public AllPostProcessingCompleteEvent createAllPostProcessingCompleteEvent(String linkCheckJobId) {
        AllPostProcessingCompleteEvent result = new AllPostProcessingCompleteEvent(linkCheckJobId);
        return result;

    }

    public StartPostProcessEvent createStartPostProcessEvent(String linkCheckJobId) {
        StartPostProcessEvent result = new StartPostProcessEvent(linkCheckJobId);
        return result;
    }

    public StartProcessDocumentsEvent createStartProcessDocumentsEvent(LinkCheckRequestedEvent linkCheckRequestedEvent) {
        StartProcessDocumentsEvent result = new StartProcessDocumentsEvent(
                linkCheckRequestedEvent.getLinkCheckJobId(), linkCheckRequestedEvent.getHarvestJobId()
        );
        return result;
    }

    public ProcessLocalMetadataDocumentEvent createProcessServiceMetadataDocumentEvent(String linkCheckJobId,

                                                                                       String sha2,
                                                                                       Long underlyingHarvestMetadataRecordId) {
        ProcessLocalMetadataDocumentEvent result = new ProcessLocalMetadataDocumentEvent(linkCheckJobId,  sha2,underlyingHarvestMetadataRecordId);
        return result;
    }

//    public MetadataDocumentProcessedEvent createMetadataDocumentProcessedEvent(ProcessMetadataDocumentEvent e  ) {
//        MetadataDocumentProcessedEvent result = new MetadataDocumentProcessedEvent(
//                e.getLinkCheckJobId(), e.getHarvestJobId(), e.getEndpointJobId(), e.getSha2() );
//        return result;
//    }

    public LinksFoundInAllDocuments createLinksFoundInAllDocuments(ProcessLocalMetadataDocumentEvent initiatingEvent) {
        LinksFoundInAllDocuments result = new LinksFoundInAllDocuments(initiatingEvent.getLinkCheckJobId() );
        return result;
    }


    public AllLinksCheckedEvent createAllLinksCheckedEvent(String linkcheckJobId) {
        AllLinksCheckedEvent event = new AllLinksCheckedEvent(linkcheckJobId);
        return event;
    }


    public ProcessServiceDocLinksEvent createProcessServiceDocLinkEvent(long linkId, String linkCheckJobId){
        ProcessServiceDocLinksEvent result = new ProcessServiceDocLinksEvent(linkId,linkCheckJobId);
        return result;
    }



    public PostProcessServiceDocumentEvent createPostProcessServiceDocumentEvent(long serviceMetadataId, String linkCheckJobId){
        PostProcessServiceDocumentEvent result = new PostProcessServiceDocumentEvent(serviceMetadataId,linkCheckJobId);
        return result;
    }

    public PostProcessDatasetDocumentEvent createPostProcessDatasetDocumentEvent(long serviceMetadataId, String linkCheckJobId){
        PostProcessDatasetDocumentEvent result = new PostProcessDatasetDocumentEvent(serviceMetadataId,linkCheckJobId);
        return result;
    }

    public ProcessServiceDocLinksEvent createProcessServiceDocLinksEvent(long serviceMetadataId, String linkCheckJobId){
        ProcessServiceDocLinksEvent result = new ProcessServiceDocLinksEvent(serviceMetadataId,linkCheckJobId);
        return result;
    }

    public ProcessDatasetDocLinksEvent createProcessDatasetDocLinksEvent(long serviceMetadataId, String linkCheckJobId){
        ProcessDatasetDocLinksEvent result = new ProcessDatasetDocLinksEvent(serviceMetadataId,linkCheckJobId);
        return result;
    }

    public StartLinkProcessingEvent createStartLinkProcessingEvent(String linkCheckJobId) {
        StartLinkProcessingEvent result = new StartLinkProcessingEvent(linkCheckJobId);
        return result;
    }

    public LinkCheckAbortEvent createLinkCheckAbortEvent( String processID  ) throws Exception {
        Optional<LinkCheckJob> job = linkCheckJobRepo.findById(processID);
        if (!job.isPresent())
            throw new Exception("could not find processID="+processID);
        LinkCheckAbortEvent result = new LinkCheckAbortEvent();
        result.setProcessID(processID);
        return result;
    }
}
