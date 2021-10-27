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

package net.geocat.routes.queuebased;

import net.geocat.eventprocessor.MainLoopRouteCreator;
import net.geocat.eventprocessor.RedirectEvent;
import net.geocat.events.findlinks.LinksFoundInAllDocuments;
import net.geocat.events.findlinks.ProcessLocalMetadataDocumentEvent;
import net.geocat.events.findlinks.StartProcessDocumentsEvent;
import net.geocat.events.processlinks.ProcessDatasetDocLinksEvent;
import net.geocat.events.processlinks.ProcessServiceDocLinksEvent;
import net.geocat.events.processlinks.StartLinkProcessingEvent;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class FindLinksOrchestrator extends SpringRouteBuilder {

    public static String myJMSQueueName = "linkCheck.FindLinksOrchestrator";

    @Autowired
    MainLoopRouteCreator mainLoopRouteCreator;

    @Override
    public void configure() throws Exception {

        mainLoopRouteCreator.createEventProcessingLoop(this,
                "activemq:" + myJMSQueueName,
                new Class[]{StartProcessDocumentsEvent.class,ProcessLocalMetadataDocumentEvent.class},
                Arrays.asList(
                        new RedirectEvent(LinksFoundInAllDocuments.class, "activemq:" + MainOrchestrator.myJMSQueueName)
                ),
                Arrays.asList(new Class[0]),
                10
        );
    }
}
