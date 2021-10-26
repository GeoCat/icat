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

import net.geocat.database.linkchecker.entities.LocalServiceMetadataRecord;
import net.geocat.database.linkchecker.entities.OperatesOnLink;
import net.geocat.database.linkchecker.entities.helper.LinkState;
import net.geocat.database.linkchecker.repos.LocalServiceMetadataRecordRepo;
import net.geocat.service.capabilities.CapabilitiesDownloadingService;
import net.geocat.service.helper.SharedForkJoinPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

import static net.geocat.database.linkchecker.service.DatabaseUpdateService.convertToString;

@Component
@Scope("prototype")
public class ServiceDocOperatesOnProcessor {

    Logger logger = LoggerFactory.getLogger(ServiceDocOperatesOnProcessor.class);

    @Autowired
    SharedForkJoinPool sharedForkJoinPool;

    @Autowired
    RetrieveOperatesOnLink retrieveOperatesOnLink;


    @Autowired
    LocalServiceMetadataRecordRepo localServiceMetadataRecordRepo;



    public void process(LocalServiceMetadataRecord localServiceMetadataRecord) throws  Exception {
        processOperatesOnLinks(localServiceMetadataRecord);
    }


    public void processOperatesOnLinks(LocalServiceMetadataRecord localServiceMetadataRecord)
            throws ExecutionException, InterruptedException
    {
        int nlinks = localServiceMetadataRecord.getOperatesOnLinks().size();
        logger.debug("processing  "+nlinks+ " operates on links for documentid="+localServiceMetadataRecord.getServiceMetadataDocumentId());



        ForkJoinPool pool = sharedForkJoinPool.getPool();
        int nTotal = localServiceMetadataRecord.getOperatesOnLinks().size();
        AtomicInteger counter = new AtomicInteger(0);

            pool.submit(() ->
                            localServiceMetadataRecord.getOperatesOnLinks().stream().parallel()
                                    .forEach(x -> {
                                        handleOperatesOnLink(x);
                                        int ndone = counter.incrementAndGet();
                                        String text = "processed operates on DS link " + ndone + " of " + nTotal;
                                        logger.debug(text);
                                    })
            ).get();



        logger.debug("FINISHED processing  "+nlinks+ " operates on links for documentid="+localServiceMetadataRecord.getServiceMetadataDocumentId());
    }


    private void handleOperatesOnLink(OperatesOnLink link) {
        try {
            String jobid = link.getLinkCheckJobId();
            link = retrieveOperatesOnLink.process(link,jobid);

            link.setLinkState(LinkState.Complete);
        }
        catch(Exception e){
            logger.error("error occurred while processing ServiceMetadataDocument, OperatesOnLink="+link+", error="+e.getMessage());
            link.setLinkState(LinkState.ERROR);
            link.setErrorMessage(  convertToString(e) );

        }
    }


}
