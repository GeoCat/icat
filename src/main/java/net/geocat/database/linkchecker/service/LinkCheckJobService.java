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

package net.geocat.database.linkchecker.service;

import net.geocat.database.linkchecker.entities.LinkCheckJob;
import net.geocat.database.linkchecker.entities.LinkCheckJobState;
import net.geocat.database.linkchecker.repos.HttpResultRepo;
import net.geocat.database.linkchecker.repos.LinkCheckJobRepo;
import net.geocat.events.LinkCheckRequestedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Scope("prototype")
public class LinkCheckJobService {

    static Map<String, LinkCheckJob> jobs = new HashMap<>();
    static Object lockObj = new Object();

    @Autowired
    LinkCheckJobRepo linkCheckJobRepo;

    @Autowired
    HttpResultRepo httpResultRepo;


    //run this code after the job is complete
    public void finalize(String linkCheckJobId) {
        LinkCheckJob  job=getJobInfo(linkCheckJobId,true);
//        // we are using a cache - it might be out-of-date.
//        // if the job isn't marked as complete (or error/abort), we re-load it
//        if ( (job.getState() != LinkCheckJobState.ERROR) && (job.getState() != LinkCheckJobState.USERABORT) && (job.getState() != LinkCheckJobState.COMPLETE) )
//        {
//            job=getJobInfo(linkCheckJobId,true);
//        }
        if (job == null)
            return;// nothing to do - shouldnt happen

        if (job.getState() == LinkCheckJobState.ERROR) {
            _finalize(job);
        }
        else if (job.getState() == LinkCheckJobState.USERABORT) {
            _finalize(job);
        }
        else if (job.getState() == LinkCheckJobState.COMPLETE) {
            _finalize(job);
        }
        else {
            _finalize(job);
        }
    }

    private void _finalize(LinkCheckJob  job) {
        if ( (job == null) || (job.getJobId() ==null) || (job.getJobId().isEmpty()) )
            return; //shouldn't happen
        if (job.isDeleteHTTPCacheWhenComplete()) {
            Long nItemsDeleted = httpResultRepo.deleteByLinkCheckJobId(job.getJobId());
            int t=0;
        }
    }

    //access to config (when needed)
    public LinkCheckJob getJobInfo(String linkCheckJobId, boolean forceRefresh) {
        synchronized (lockObj) {
            LinkCheckJob result = jobs.get(linkCheckJobId);
            if ( (result != null) && !forceRefresh)
                return result;
            Optional<LinkCheckJob> job = linkCheckJobRepo.findById(linkCheckJobId);
            if (!job.isPresent())
                return null; //shouln't happen
            jobs.put(linkCheckJobId,job.get());
            return job.get();
        }
    }

    public LinkCheckJob updateNumberofDocumentsInBatch(String linkCheckJobId, Long number) {
        LinkCheckJob job =  linkCheckJobRepo.findById(linkCheckJobId).get();
        job.setNumberOfDocumentsInBatch( number );
        return linkCheckJobRepo.save(job);
    }

    public LinkCheckJob updateLinkCheckJobStateInDBToError(String guid) throws Exception {
        LinkCheckJob result =  updateLinkCheckJobStateInDB(guid, LinkCheckJobState.ERROR);
        finalize(guid);
        return result;
    }

    public LinkCheckJob updateLinkCheckJobStateInDB(String guid, LinkCheckJobState state) {
        LinkCheckJob job = linkCheckJobRepo.findById(guid).get();
        job.setState(state);
        return linkCheckJobRepo.save(job);
     }

    public LinkCheckJob find(String guid ) {
        LinkCheckJob job = linkCheckJobRepo.findById(guid).get();
        return job;
    }



    public LinkCheckJob createLinkCheckJobInDB(LinkCheckRequestedEvent event) {
        Optional<LinkCheckJob> job = linkCheckJobRepo.findById(event.getLinkCheckJobId());
        if (job.isPresent()) //2nd attempt
        {
            job.get().setState(LinkCheckJobState.CREATING);
            return linkCheckJobRepo.save(job.get());
        }
        LinkCheckJob newJob = new LinkCheckJob();
        newJob.setLongTermTag(event.getLongTermTag());
        newJob.setJobId(event.getLinkCheckJobId());
        newJob.setHarvestJobId(event.getHarvestJobId());

        newJob.setDeleteHTTPCacheWhenComplete(event.getLinkCheckRunConfig().isDeleteHTTPCacheWhenComplete());
        newJob.setUseOtherJobsHTTPCache(event.getLinkCheckRunConfig().isUseOtherJobsHTTPCache());

        newJob.setMaxDataLinksToFollow(event.getLinkCheckRunConfig().getMaxDataLinksToFollow());
        newJob.setMaxAtomEntriesToAttempt(event.getLinkCheckRunConfig().getMaxAtomEntriesToAttempt());
        newJob.setMaxAtomSectionLinksToFollow(event.getLinkCheckRunConfig().getMaxAtomSectionLinksToFollow());

        return linkCheckJobRepo.save(newJob);
     }

}
