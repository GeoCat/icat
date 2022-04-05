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


import net.geocat.database.linkchecker.service.DatabaseUpdateService;
import net.geocat.events.Event;
import org.apache.camel.BeanScope;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.ChoiceDefinition;
import org.apache.camel.spring.SpringRouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
public class MainLoopRouteCreator {

    Logger logger = LoggerFactory.getLogger(MainLoopRouteCreator.class);


    @Autowired
    EventProcessorRouteCreator eventProcessorRouteCreator;

    /**
     * @param routeBuilder
     * @param from
     * @param eventTypes
     * @param redirectEventList
     * @param handledElsewhereEvents - just send to "direct:" + mainRouteName + "_" + eventType.getSimpleName() but don't implement it add a .from()
     * @throws Exception
     */
    public void createEventProcessingLoop(SpringRouteBuilder routeBuilder,
                                          String from,
                                          Class[] eventTypes,
                                          List<RedirectEvent> redirectEventList,
                                          List<Class> handledElsewhereEvents,
                                          int concurrency) throws Exception {

        String mainRouteName = extractName(from);
        JacksonDataFormat jsonDefHarvesterConfig = new JacksonDataFormat(Event.class);

        routeBuilder.errorHandler(routeBuilder.transactionErrorHandler()
                .maximumRedeliveries(2)
                .redeliveryDelay(1000));

        routeBuilder.onException().onExceptionOccurred(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                Exception ex = exchange.getException();
                exchange.getMessage().setHeader("exception", ex);
                logger.error("exception occurred", ex);
            }
        })
                .bean(DatabaseUpdateService.class, "errorOccurred", BeanScope.Request).maximumRedeliveries(2)
        ;

        ChoiceDefinition choice = routeBuilder
                .from(from +"?concurrentConsumers="+concurrency)
                .routeId(mainRouteName + ".eventprocessor")
                .transacted("txPolicyName")
                .unmarshal(jsonDefHarvesterConfig)
                .setHeader("eventType", routeBuilder.simple("${body.getClass().getSimpleName()}"))
                //  .log(mainRouteName + " received event of type ${headers.eventType} and body=${body} ")
                .choice();
        // special events - re-broadcast to parent
        for (RedirectEvent redirectEvent : redirectEventList) {
            choice = choice
                    .when(routeBuilder.simple("${headers.eventType} == '" + redirectEvent.getEventType().getSimpleName() + "'"))
                    .log(LoggingLevel.TRACE,mainRouteName + " redirecting event of type ${headers.eventType} to " + redirectEvent.getEndpoint())
//                    .process(new Processor() {
//                        @Override
//                        public void process(Exchange exchange) throws Exception {
//                            logger.debug("redirect processor");
//                            logger.debug("exchange.getmessage = "+exchange.getMessage());
//                            logger.debug("exchange.getmessage.getbody = "+exchange.getMessage().getBody());
//
//                            int t=0;
//                        }
//                    })
                    .marshal().json()
                    .to(redirectEvent.getEndpoint());
        }


        // all other events, route to direct:
        for (Class eventType : eventTypes) {
            choice = choice
                    .when(routeBuilder.simple("${headers.eventType} == '" + eventType.getSimpleName() + "'"))
                   // .log(mainRouteName + " received event of type ${headers.eventType} and body=${body} ")
                    .to("direct:" + mainRouteName + "_" + eventType.getSimpleName());
        }

        //add in individual processors
        for (Class eventType : eventTypes) {
            if (!handledElsewhereEvents.contains(eventType)) {
                eventProcessorRouteCreator.addEventProcessor(routeBuilder,
                        eventType,
                        "direct:" + mainRouteName + "_" + eventType.getSimpleName(),
                        from,
                        mainRouteName,
                        false,
                        redirectEventList);
            }
        }


    }

    // activemq:abc?...  -> abc
    public String extractName(String from) {
        return from.replaceFirst("[^:]+:", "")
                .replaceFirst("\\?[.]+", "");

    }
}
