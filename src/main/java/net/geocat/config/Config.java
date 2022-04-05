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

package net.geocat.config;

import net.geocat.dblogging.MYUnitOfWorkFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.ExtendedCamelContext;
import org.apache.camel.component.activemq.ActiveMQComponent;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jms.connection.JmsTransactionManager;

@Configuration
public class Config {


    //String brokerUrl = "tcp://localhost:61616?jms.prefetchPolicy.queuePrefetch=1";
    @Value("${activemq.url}")
    String brokerUrl;

    int maxConnections = 11;

    @Autowired
    CamelContext camelContext;

    @Bean
    public ActiveMQConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
        factory.setBrokerURL(brokerUrl);
        org.apache.activemq.RedeliveryPolicy redeliveryPolicy = factory.getRedeliveryPolicy();
        redeliveryPolicy.setMaximumRedeliveries(3);
        redeliveryPolicy.setRedeliveryDelay(3000);
        redeliveryPolicy.setInitialRedeliveryDelay(3000);
        redeliveryPolicy.setMaximumRedeliveryDelay(5000);
        return factory;
    }

    //    @Bean
//    public JmsTransactionManager jmsTransactionManager(ActiveMQConnectionFactory connectionFactory) {
//        JmsTransactionManager jmsTransactionManager = new JmsTransactionManager();
//        jmsTransactionManager.setConnectionFactory(connectionFactory);
//        return jmsTransactionManager;
//    }

    @Bean
    public CamelContextConfiguration contextConfiguration() {
        return new CamelContextConfiguration() {
            @Override
            public void beforeApplicationStart(CamelContext context) {
                context.setUseMDCLogging(true);
                context.adapt(ExtendedCamelContext.class).setUnitOfWorkFactory(new MYUnitOfWorkFactory());
            }

            @Override
            public void afterApplicationStart(CamelContext camelContext) {
            }
        };
    }


    @Bean
    //@Primary
    public ActiveMQComponent activemq(ActiveMQConnectionFactory connectionFactory) {

        PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory(connectionFactory);
        pooledConnectionFactory.setMaxConnections(50);

        JmsTransactionManager jmsTransactionManager = new JmsTransactionManager();
        jmsTransactionManager.setConnectionFactory(pooledConnectionFactory);

        ActiveMQComponent activeMQComponent = new ActiveMQComponent();
        activeMQComponent.setConnectionFactory(pooledConnectionFactory);
        activeMQComponent.setTransacted(true);
        activeMQComponent.setTransactedInOut(true);
        activeMQComponent.setTransactionManager(jmsTransactionManager);

        //  activeMQComponent.setLazyCreateTransactionManager (false);

        activeMQComponent.setCacheLevelName("CACHE_NONE");
        activeMQComponent.setAcknowledgementModeName("SESSION_TRANSACTED");


        return activeMQComponent;
    }

//    @Bean
//    public CamelContext camelContext(){
//        CamelContext ctx = new DefaultCamelContext();
//       // ctx.setMessageHistory(true);
//        return ctx;
//    }

//    @Bean(name = "transactionManager")
//    public PlatformTransactionManager dbTransactionManager() {
//        JpaTransactionManager transactionManager = new JpaTransactionManager();
//
//        return transactionManager;
//    }

    @Bean(name = "txPolicyName")
    public SpringTransactionPolicy springTransactionPolicy(ActiveMQComponent activeMQComponent) {
        SpringTransactionPolicy result = new SpringTransactionPolicy();
        result.setTransactionManager(activeMQComponent.getTransactionManager());
        return result;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        PropertySourcesPlaceholderConfigurer propsConfig
                = new PropertySourcesPlaceholderConfigurer();
        propsConfig.setLocation(new ClassPathResource("git.properties"));
        propsConfig.setIgnoreResourceNotFound(true);
        propsConfig.setIgnoreUnresolvablePlaceholders(true);
        return propsConfig;
    }
}
