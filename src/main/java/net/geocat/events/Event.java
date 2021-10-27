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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LinkCheckAbortEvent.class, name = "Abort"),
        @JsonSubTypes.Type(value = LinkCheckRequestedEvent.class, name = "Request"),
        @JsonSubTypes.Type(value = ProcessLocalMetadataDocumentEvent.class, name = "ProMD"),
        @JsonSubTypes.Type(value = LinksFoundInAllDocuments.class, name = "AllLinksFound"),
        @JsonSubTypes.Type(value = StartProcessDocumentsEvent.class, name = "StartPro"),
        @JsonSubTypes.Type(value = AllPostProcessingCompleteEvent.class, name = "AllPostDone"),
        @JsonSubTypes.Type(value = PostProcessDatasetDocumentEvent.class, name = "PostDS"),
        @JsonSubTypes.Type(value = PostProcessServiceDocumentEvent.class, name = "PostSer"),
        @JsonSubTypes.Type(value = StartPostProcessEvent.class, name = "StartPost"),
        @JsonSubTypes.Type(value = AllLinksCheckedEvent.class, name = "AllCheck"),
        @JsonSubTypes.Type(value = ProcessDatasetDocLinksEvent.class, name = "ProsDS"),
        @JsonSubTypes.Type(value = ProcessServiceDocLinksEvent.class, name = "ProsSer"),
        @JsonSubTypes.Type(value = StartLinkProcessingEvent.class, name = "StartLink"),

})
public class Event {

}
