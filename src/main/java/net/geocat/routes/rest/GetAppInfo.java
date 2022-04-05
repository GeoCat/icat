package net.geocat.routes.rest;

import net.geocat.service.GetAppInfoService;
import org.apache.camel.BeanScope;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GetAppInfo extends RouteBuilder {

    @Value("${geocat.jettyHost}")
    public String jettyHost;

    @Value("${geocat.jettyPort}")
    public Integer jettyPort;

    @Override
    public void configure() throws Exception {
        restConfiguration().component("jetty").host(jettyHost).port(jettyPort);

        rest("/api/info/")
                .get("/")
                .route()
                .routeId("rest.rest.info")
                .bean(GetAppInfoService.class, "getInfo()", BeanScope.Request)

                .setHeader("content-type", constant("application/json"))
                .marshal().json()

        ;
    }
}
