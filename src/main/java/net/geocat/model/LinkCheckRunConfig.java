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

package net.geocat.model;

import net.geocat.database.harvester.entities.HarvestJob;
import net.geocat.database.harvester.entities.HarvestJobState;
import net.geocat.database.harvester.repos.HarvestJobRepo;
import net.geocat.database.linkchecker.repos.LinkCheckJobRepo;
import org.springframework.util.StringUtils;

import java.util.Optional;

public class LinkCheckRunConfig {

    String longTermTag;
    String harvestJobId;

    public static boolean useOtherJobsHTTPCache_default = true;
    public static boolean deleteHTTPCacheWhenComplete_default = false;
    public static int maxDataLinksToFollow_default = 100;
    public static int maxAtomEntriesToAttempt_default = 100;
    public static int maxAtomSectionLinksToFollow_default = 10;
    public static int storeAtMostNHistoricalRuns_default = 1000;

    boolean useOtherJobsHTTPCache = useOtherJobsHTTPCache_default;
    boolean deleteHTTPCacheWhenComplete = deleteHTTPCacheWhenComplete_default;
    
    int maxDataLinksToFollow = maxDataLinksToFollow_default;
    int maxAtomEntriesToAttempt = maxAtomEntriesToAttempt_default;
    int maxAtomSectionLinksToFollow = maxAtomSectionLinksToFollow_default;

    int storeAtMostNHistoricalRuns =storeAtMostNHistoricalRuns_default ;


    // GUID for the harvest (used as JMS Correlation ID).  Provided by server (do not specify)
    private String processID;

    public void validate(LinkCheckJobRepo linkCheckJobRepo, HarvestJobRepo harvestJobRepo) throws Exception {
        if (StringUtils.isEmpty(longTermTag) && StringUtils.isEmpty(harvestJobId)) {
            throw new Exception("LinkCheckRunConfig - harvester with name/uuid (longTermTag) or harvestJobId should be provided!");
        }

        // Filter most recent
        if (!StringUtils.isEmpty(longTermTag)) {
            Optional<HarvestJob> harvestJob = harvestJobRepo.findMostRecentHarvestJobByLongTermTag(longTermTag);
            if (!harvestJob.isPresent()) {
                throw new Exception(String.format("LinkCheckRunConfig - No harvester job related found for the harvester with name/uuid %s." , longTermTag));
            }
        } else {
            Optional<HarvestJob> harvestJob = harvestJobRepo.findById(harvestJobId);
            if (!harvestJob.isPresent()) {
                throw new Exception("LinkCheckRunConfig - cannot find previous harvest run harvestJobId: " + harvestJobId);
            }
            setLongTermTag(harvestJob.get().getLongTermTag());
            if (harvestJob.get().getState() != HarvestJobState.COMPLETE) {
                throw new Exception("Harvest run harvestJobId: " + harvestJobId + ", state '" + harvestJob.get().getState() + "' is not valid.");
            }
        }

        if (maxAtomEntriesToAttempt <=0)
            throw new Exception("maxAtomEntriesToAttempt must be >0");
        if (maxAtomSectionLinksToFollow <=0)
            throw new Exception("maxAtomSectionLinksToFollow must be >0");
        if (maxDataLinksToFollow <=0)
            throw new Exception("maxDataLinksToFollow must be >0");

    }

    //--


    public int getStoreAtMostNHistoricalRuns() {
        return storeAtMostNHistoricalRuns;
    }

    public void setStoreAtMostNHistoricalRuns(int storeAtMostNHistoricalRuns) {
        this.storeAtMostNHistoricalRuns = storeAtMostNHistoricalRuns;
    }

    public int getMaxDataLinksToFollow() {
        return maxDataLinksToFollow;
    }

    public void setMaxDataLinksToFollow(int maxDataLinksToFollow) {
        this.maxDataLinksToFollow = maxDataLinksToFollow;
    }

    public int getMaxAtomEntriesToAttempt() {
        return maxAtomEntriesToAttempt;
    }

    public void setMaxAtomEntriesToAttempt(int maxAtomEntriesToAttempt) {
        this.maxAtomEntriesToAttempt = maxAtomEntriesToAttempt;
    }

    public int getMaxAtomSectionLinksToFollow() {
        return maxAtomSectionLinksToFollow;
    }

    public void setMaxAtomSectionLinksToFollow(int maxAtomSectionLinksToFollow) {
        this.maxAtomSectionLinksToFollow = maxAtomSectionLinksToFollow;
    }

    public boolean isUseOtherJobsHTTPCache() {
        return useOtherJobsHTTPCache;
    }

    public void setUseOtherJobsHTTPCache(boolean useOtherJobsHTTPCache) {
        this.useOtherJobsHTTPCache = useOtherJobsHTTPCache;
    }

    public boolean isDeleteHTTPCacheWhenComplete() {
        return deleteHTTPCacheWhenComplete;
    }

    public void setDeleteHTTPCacheWhenComplete(boolean deleteHTTPCacheWhenComplete) {
        this.deleteHTTPCacheWhenComplete = deleteHTTPCacheWhenComplete;
    }

    public String getLongTermTag() {
        return longTermTag;
    }

    public void setLongTermTag(String longTermTag) {
        this.longTermTag = longTermTag;
    }

    public String getHarvestJobId() {
        return harvestJobId;
    }

    public void setHarvestJobId(String harvestJobId) {
        this.harvestJobId = harvestJobId;
    }

    public String getProcessID() {
        return processID;
    }

    public void setProcessID(String processID) {
        this.processID = processID;
    }
}
