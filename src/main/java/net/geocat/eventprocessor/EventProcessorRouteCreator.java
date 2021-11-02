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

package net.geocat.eventprocessor;


import net.geocat.events.Event;
import net.geocat.service.camelsupport.StopProcessingMessageService;
import org.apache.camel.BeanScope;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class EventProcessorRouteCreator {
    static JacksonDataFormat jsonDefHarvesterConfig = new JacksonDataFormat(Event.class);

    /**
     * eventType example - eventType=ActualHarvestEndpointStartCommand
     * Then there should be a class called
     * EventProcessor_ActualHarvestEndpointStartCommand extends BaseEventProcessor<ActualHarvestEndpointStartCommand>
     *
     * @param routeBuilder
     * @param eventType
     * @param from
     * @param to
     * @throws Exception
     */
    public void addEventProcessor(RouteBuilder routeBuilder,
                                  Class eventType,
                                  String from,
                                  String to,
                                  String tag,
                                  boolean unmarshal //will unmarshal message + transacted
    ) throws Exception {
        validate(eventType);


        //=====================================================================
        if (unmarshal)
            routeBuilder
                    .from(from)  // ${body} will be JMS message - unmarshal
                    .transacted("txPolicyName")
                    .unmarshal(jsonDefHarvesterConfig)
                    .routeId(tag + "_" + eventType.getSimpleName())
                    // .log("processing event of type " + eventType.getSimpleName() + " from " + from)
                    .log(LoggingLevel.TRACE,from + ": event = ${body}")
                    .bean(StopProcessingMessageService.class, "checkIfShouldBeProcessed", BeanScope.Request)
                    .bean(EventProcessorFactory.class, "create( ${body} )", BeanScope.Request)
                    .transform().simple("${body.externalProcessing()}")
                    .transform().simple("${body.internalProcessing()}")
                    .transform().simple("${body.newEventProcessing()}")
                    .split().simple("${body}")
                    .marshal().json()
                    .to(to)
                    ;
        else
            routeBuilder
                    .from(from)  // ${body} will be of type eventType
                    .transacted("txPolicyName")
                    .routeId(tag + "_" + eventType.getSimpleName())
                    // .log("processing event of type " + eventType.getSimpleName() + " from " + from)
                    .log(LoggingLevel.TRACE,from + ": event = ${body}")
                    .bean(StopProcessingMessageService.class, "checkIfShouldBeProcessed", BeanScope.Request)
                    .bean(EventProcessorFactory.class, "create( ${body} )", BeanScope.Request)
                    .transform().simple("${body.externalProcessing()}")
                    .transform().simple("${body.internalProcessing()}")
                    .transform().simple("${body.newEventProcessing()}")
                    .split().simple("${body}")
                    .marshal().json()
                    .to(to)
                    ;
        //=====================================================================
    }

    public void validate(Class eventType) throws Exception {
        if (!Event.class.isAssignableFrom(eventType))
            throw new Exception("RouteCreator - eventType must be Event");
        EventProcessorFactory.processorClass(eventType);//will throw if not found
    }


}
