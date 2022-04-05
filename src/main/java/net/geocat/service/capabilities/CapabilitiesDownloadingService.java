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

package net.geocat.service.capabilities;

import net.geocat.database.linkchecker.entities.CapabilitiesDatasetMetadataLink;
import net.geocat.database.linkchecker.entities.CapabilitiesDocument;
import net.geocat.database.linkchecker.entities.RemoteServiceMetadataRecordLink;
import net.geocat.database.linkchecker.entities.ServiceDocumentLink;
import net.geocat.database.linkchecker.entities.helper.CapabilitiesDocumentState;
import net.geocat.database.linkchecker.entities.helper.DocumentLink;
import net.geocat.database.linkchecker.entities.helper.LinkState;
import net.geocat.database.linkchecker.entities.helper.SHA2JobIdCompositeKey;
import net.geocat.database.linkchecker.repos.CapabilitiesDocumentRepo;
import net.geocat.database.linkchecker.repos.ServiceDocumentLinkRepo;
import net.geocat.eventprocessor.processors.processlinks.EventProcessor_ProcessServiceDocLinksEvent;
import net.geocat.service.RemoteServiceMetadataRecordLinkRetriever;
import net.geocat.service.RetrieveCapabilitiesDatasetMetadataLink;
import net.geocat.service.RetrieveServiceDocumentLink;
import net.geocat.service.helper.ProcessLockingService;
import net.geocat.service.helper.SharedForkJoinPool;
import net.geocat.xml.XmlCapabilitiesDocument;
import net.geocat.xml.XmlCapabilitiesWFS;
import net.geocat.xml.XmlDocumentFactory;
import net.geocat.xml.XmlStringTools;
import net.geocat.xml.helpers.CapabilitiesType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

import static net.geocat.database.linkchecker.service.DatabaseUpdateService.convertToString;

@Component
@Scope("prototype")
public class CapabilitiesDownloadingService {

    Logger logger = LoggerFactory.getLogger(CapabilitiesDownloadingService.class);

    @Autowired
    SharedForkJoinPool sharedForkJoinPool;

    @Autowired
    ProcessLockingService processLockingService;

    @Autowired
    RetrieveServiceDocumentLink retrieveServiceDocumentLink;

    @Autowired
    RetrieveStoredQueries retrieveStoredQueries;

    @Autowired
    CapabilitiesDocumentRepo capabilitiesDocumentRepo;




    @Autowired
    RetrieveCapabilitiesDatasetMetadataLink retrieveCapabilitiesDatasetMetadataLink;

    @Autowired
    RemoteServiceMetadataRecordLinkRetriever remoteServiceMetadataRecordLinkRetriever;

    @Autowired
    ServiceDocumentLinkRepo serviceDocumentLinkRepo;

    public void handleLink(DocumentLink link) throws Exception {
        download(link);
        //serviceDocumentLinkRepo.save(link);
        if (link.getCapabilitiesDocument() == null) {
            //nothing to do - we don't have a capabilities document
            return;
        }
        processCapabilitiesDocument(link);

    }

    private void processCapabilitiesDocument(DocumentLink link) {
        String linkCheckId = link.getLinkCheckJobId();
        String capSha2 = link.getSha2();
        String lockKey = "cap_"+linkCheckId+"_"+capSha2;
        Lock lock = processLockingService.getLock(lockKey);
        lock.lock();
        try {
            processCapabilitiesDocument_work(link);
        }
        finally {
            lock.unlock();
        }
    }


    private void processCapabilitiesDocument_work(DocumentLink link) {
        //first lets see if the document already exists - if not, we need to make it
        CapabilitiesDocument doc  = getOrCreate(link);
        if ( (doc.getState() != CapabilitiesDocumentState.COMPLETE) && ( doc.getState() != CapabilitiesDocumentState.ERROR) ) {
            //we have the lock (no one else is processing), but this isn't complete
            //we'll try again
            //STATE ERROR: this shouldn't really happen, so should be ok to re-process it when it does
            try {
                String storedProcName = retrieveStoredQueries.getSpatialDataSetStoredQuery(doc,link);
                if (storedProcName !=null)
                    doc.setProcGetSpatialDataSetName(storedProcName);
                getServiceDocument(doc);
                getDatasetDocuments(doc);
                doc.setState(CapabilitiesDocumentState.COMPLETE);
            }
            catch(Exception e){
                doc.setState(CapabilitiesDocumentState.ERROR);
                logger.error("something happened processing capabilities document",e);
            }
            CapabilitiesDocument doc2 =  capabilitiesDocumentRepo.save(doc);
        }
    }



    private void handleLayerDatasetLink(CapabilitiesDatasetMetadataLink capabilitiesDatasetMetadataLink) {
        try {
            String jobid = capabilitiesDatasetMetadataLink.getLinkCheckJobId();
            capabilitiesDatasetMetadataLink = retrieveCapabilitiesDatasetMetadataLink.process(capabilitiesDatasetMetadataLink,jobid);

            capabilitiesDatasetMetadataLink.setLinkState(LinkState.Complete);
        }
        catch(Exception e){
            logger.error("error occurred while processing cap Dataset link, CapabilitiesDatasetMetadataLink="+capabilitiesDatasetMetadataLink+", error="+e.getMessage(),e);
            capabilitiesDatasetMetadataLink.setLinkState(LinkState.ERROR);
            capabilitiesDatasetMetadataLink.setErrorMessage(  convertToString(e) );
        }
    }

    private void getDatasetDocuments(CapabilitiesDocument capabilitiesDocument) throws ExecutionException, InterruptedException {
        if (capabilitiesDocument.getCapabilitiesDatasetMetadataLinkList() == null)
            return;

        logger.debug("processing "+capabilitiesDocument.getCapabilitiesDatasetMetadataLinkList().size()+" dataset links from the capabilities document");

        ForkJoinPool pool = sharedForkJoinPool.getPool();
        int nTotal = capabilitiesDocument.getCapabilitiesDatasetMetadataLinkList().size();
        AtomicInteger counter = new AtomicInteger(0);

            pool.submit(() ->
                    capabilitiesDocument.getCapabilitiesDatasetMetadataLinkList().stream().parallel()
                            .forEach(x -> {
                                handleLayerDatasetLink(x);
                                int ndone = counter.incrementAndGet();
                                logger.trace("processed link cap's DS link " + ndone + " of " + nTotal); // a wee bit of a lie, but will be "accurate"
                            })
            ).get();

        capabilitiesDocument.getCapabilitiesDatasetMetadataLinkList().stream()
                .forEach(x->x.setCapabilitiesDocument(capabilitiesDocument));
    }


    private void getRemoteServiceMetadataRecordLink(RemoteServiceMetadataRecordLink rsmrl) {
        try {
            rsmrl = remoteServiceMetadataRecordLinkRetriever.process(rsmrl);
            rsmrl.setLinkState(LinkState.Complete);
        }
        catch(Exception e){
            logger.error("error occurred while processing , RemoteServiceMetadataRecordLink="+rsmrl+", error="+e.getMessage(),e);
            rsmrl.setLinkState(LinkState.ERROR);
            rsmrl.setErrorMessage(  convertToString(e) );
        }
    }

    private void getServiceDocument(CapabilitiesDocument doc) {
        if (doc.getRemoteServiceMetadataRecordLink() != null) {
            getRemoteServiceMetadataRecordLink(doc.getRemoteServiceMetadataRecordLink());
        }
    }

    private Object lockObject = new Object();

    private CapabilitiesDocument getOrCreate(DocumentLink link) {
        String linkCheckId = link.getLinkCheckJobId();
        String capSha2 = link.getSha2();

        SHA2JobIdCompositeKey key = new SHA2JobIdCompositeKey(capSha2,linkCheckId);

        //make sure we dont have multiple threads (i.e. via slightly different URLs) attempt to create the doc at the same time
        synchronized (lockObject) {
            Optional<CapabilitiesDocument> capDoc = capabilitiesDocumentRepo.findById(key);
            if (capDoc.isPresent())
                return capDoc.get();

            //create
            CapabilitiesDocument doc = link.getCapabilitiesDocument();
            doc.setLinkCheckJobId(linkCheckId);
            doc.setSha2(capSha2);

            doc = capabilitiesDocumentRepo.save(doc);

            // return capabilitiesDocumentRepo.findById(key).get(); //re-load (sometimes this is better)
            return doc;
        }

    }


    private void download(DocumentLink link) {
        try {
            link =   retrieveServiceDocumentLink.process(link);
            link.setLinkState(LinkState.Complete);
        }
        catch (Exception e){
            logger.warn("error occurred while downloading capabilities document at url="+link.getFixedURL(),e);
            link.setLinkState(LinkState.ERROR);
            link.setErrorMessage(  convertToString(e) );
        }
    }

}
