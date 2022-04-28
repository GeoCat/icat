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

package net.geocat.routes.rest;


import net.geocat.service.html.HtmlCapabilitiesService;
import net.geocat.service.html.HtmlDatasetService;
import net.geocat.service.html.HtmlDiscoverService;
import net.geocat.service.html.HtmlIdentifierService;
import net.geocat.service.html.HtmlLinkToDataService;
import net.geocat.service.html.HtmlScrapeService;
import net.geocat.service.html.HtmlServiceService;
import net.geocat.service.html.HtmlStatsService;
import net.geocat.service.html.HtmlSummaryService;
import org.apache.camel.BeanScope;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Html extends RouteBuilder {

    @Value("${geocat.jettyHost}")
    public String jettyHost;

    @Value("${geocat.jettyPort}")
    public Integer jettyPort;


    @Override
    public void configure() throws Exception {
        restConfiguration().component("jetty").host(jettyHost).port(jettyPort);

        // JacksonDataFormat jsonDefHarvesterConfig = new JacksonDataFormat(HarvesterConfig.class);

        //--- incoming start process request (HTTP)
        rest("/api/html/service/")
                .get("/{processID}/{fileId}")
                .route()
                .routeId("rest.rest.html.service")
                .bean(HtmlServiceService.class, "getHtml( ${header.processID}, ${header.fileId} )", BeanScope.Request)
                .setHeader("content-type", constant("text/html"));

        rest("/api/html/capabilities/")
                .get("/{processID}/{fileId}")
                .route()
                .routeId("rest.rest.html.capabilities")
                .bean(HtmlCapabilitiesService.class, "getHtml( ${header.processID}, ${header.fileId} )", BeanScope.Request)
                .setHeader("content-type", constant("text/html")) ;

        rest("/api/html/dataset/")
                .get("/{processID}/{fileId}")
                .route()
                .routeId("rest.rest.html.dataset")
                .bean(HtmlDatasetService.class, "getHtml( ${header.processID}, ${header.fileId} )", BeanScope.Request)

                .setHeader("content-type", constant("text/html"))
        ;

        rest("/api/html/discover/")
                .get("/{fileId}")
                .route()
                .routeId("rest.rest.html.discover")
                .bean(HtmlDiscoverService.class, "getHtml(  ${header.fileId},null )", BeanScope.Request)

                .setHeader("content-type", constant("text/html"))
        ;

        rest("/api/html/discover/")
                .get("/{fileId}/{linkCheckJobId}")
                .route()
                .routeId("rest.rest.html.discover2")
                .bean(HtmlDiscoverService.class, "getHtml(  ${header.fileId},${header.linkCheckJobId}  )", BeanScope.Request)

                .setHeader("content-type", constant("text/html"))
        ;

        rest("/api/html/discoverInput/")
                .get("")
                .route()
                .routeId("rest.rest.html.discoverInput")
                .bean(HtmlDiscoverService.class, "getHtmlInput(  )", BeanScope.Request)

                .setHeader("content-type", constant("text/html"))
        ;

        rest("/api/html/compare/")
                .get("/{item}")
                .route()
                .routeId("rest.rest.html.compare")
                .bean(HtmlScrapeService.class, "getHtml( ${header.item}  )", BeanScope.Request)

                .setHeader("content-type", constant("text/html"))
        ;
        rest("/api/html/summary/")
                .get("/{processID}")
                .route()
                .routeId("rest.rest.html.summary")
                .bean(HtmlSummaryService.class, "getHtml( ${header.processID}  )", BeanScope.Request)

                .setHeader("content-type", constant("text/html"))
        ;

        rest("/api/html/stats/")
                .get("/{processID}")
                .route()
                .routeId("rest.rest.html.stats")
                .bean(HtmlStatsService.class, "getHtml( ${header.processID}  )", BeanScope.Request)

                .setHeader("content-type", constant("text/html"))
        ;

        rest("/api/html/linktodata/")
                .get("/{linkId}")
                .route()
                .routeId("rest.rest.html.linktodata")
                .bean(HtmlLinkToDataService.class, "getHtml( ${header.linkId}  )", BeanScope.Request)

                .setHeader("content-type", constant("text/html"))
        ;

//        rest("/api/html/identifier/")
//                .get("/{code}")
//                .route()
//                .routeId("rest.rest.html.identifier")
//                .bean(HtmlIdentifierService.class, "getHtml( ${header.code},null,null  )", BeanScope.Request)
//
//                .setHeader("content-type", constant("text/html"))
//        ;
//
//        rest("/api/html/identifier/")
//                .get("/{code}/{linkcheckjobid}")
//                .route()
//                .routeId("rest.rest.html.identifier3")
//                .bean(HtmlIdentifierService.class, "getHtml( ${header.code}, ${header.codespace},  ${header.linkcheckjobid}  )", BeanScope.Request)
//
//                .setHeader("content-type", constant("text/html"))
//        ;

        rest("/api/html/identifier")
                .get("?code={code}&linkcheckjobid={linkcheckjobid}" )
                .route()
                .routeId("rest.rest.html.identifier")
                .bean(HtmlIdentifierService.class, "getHtml( ${header.code},null,null  )", BeanScope.Request)

                .setHeader("content-type", constant("text/html"))
        ;

        rest("/api/html/identifierInput/")
                .get("")
                .route()
                .routeId("rest.rest.html.identifierInput")
                .bean(HtmlIdentifierService.class, "getHtmlInput(   )", BeanScope.Request)

                .setHeader("content-type", constant("text/html"))
        ;
    }
}

