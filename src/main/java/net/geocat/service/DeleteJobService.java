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

import net.geocat.database.linkchecker.entities.LinkCheckJob;
import net.geocat.database.linkchecker.entities.helper.UpdateCreateDateTimeEntity;
import net.geocat.database.linkchecker.repos.LinkCheckJobRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class DeleteJobService {

    @Autowired
    LinkCheckJobRepo linkCheckJobRepo;

    @Autowired
    @Qualifier("entityManagerFactory")
    LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean;

    @Autowired
    PlatformTransactionManager transactionManager;


    public void executeSql(List<String> sqls, String linkCheckJobId) {
        EntityManager entityManager = null;
        try {
            entityManager = localContainerEntityManagerFactoryBean.createNativeEntityManager(null);
            entityManager.getTransaction().begin();
            for (String sql: sqls ) {
                Query q = entityManager.createNativeQuery(sql);
                q.setParameter(1,linkCheckJobId);
                int n = q.executeUpdate();
            }
            entityManager.getTransaction().commit();
        }
        finally {
            if (entityManager != null) {
                if (entityManager.getTransaction() != null) {
                    try {
                        entityManager.getTransaction().rollback();
                    }
                    catch (Exception e) {
                        // do nothing
                    }
                }
                entityManager.close();
            }
        }
    }

    static List<String> deleteSQLS = Arrays.asList( new String[] {
            "DELETE FROM  datasetmetadatarecord WHERE linkcheckjobid=?",
            "DELETE FROM  servicemetadatarecord WHERE linkcheckjobid=?",
            "DELETE FROM  localnotprocessedmetadatarecord WHERE linkcheckjobid=?",

            "DELETE FROM  capabilitiesdatasetmetadatalink WHERE linkcheckjobid=?",
            "DELETE FROM  inspirespatialdatasetidentifier WHERE cap_jobid=?",
            "DELETE FROM  capabilitiesdocument WHERE linkcheckjobid=?",
            "DELETE FROM  remoteservicemetadatarecordlink WHERE linkcheckjobid=?",

            "DELETE FROM  atomsubfeedrequest WHERE linkcheckjobid=?",
            "DELETE FROM  ogcrequest WHERE linkcheckjobid=?",

            "DELETE FROM  logging_event WHERE jms_correlation_id = ? AND reference_flag NOT IN (2,3)",
            "DELETE FROM  logging_event_exception WHERE event_id IN (SELECT event_id FROM logging_event WHERE jms_correlation_id = ?)",
            "DELETE FROM  logging_event WHERE jms_correlation_id = ?",

            "DELETE FROM  httpresultcache WHERE linkcheckjobid=?",

            "DELETE FROM  linkcheckjob WHERE jobid=?",


    });

    public String deleteById(String linkCheckJobId) throws Exception {
        if (linkCheckJobId == null)
            throw new Exception("delete - jobid is null");
        linkCheckJobId = linkCheckJobId.trim();
        Optional<LinkCheckJob> _job = linkCheckJobRepo.findById(linkCheckJobId);
        if (!_job.isPresent())
            throw new Exception("couldnt find that job id = "+linkCheckJobId);
        LinkCheckJob job = _job.get();

        linkCheckJobId = job.getJobId(); // this prevents sql injection
        if (linkCheckJobId.contains("'") || linkCheckJobId.contains("\\"))
            throw new Exception("bad linkcheck job"); // this shouldn't be possible

        executeSql(deleteSQLS,linkCheckJobId);

        return "DELETED";
    }

    public String ensureAtMost(String longTermTag, int maxAllowed) throws Exception {
        if (longTermTag == null)
            throw new Exception("delete - countryCode is null");
        longTermTag = longTermTag.trim();

        List<LinkCheckJob> jobs = linkCheckJobRepo.findByLongTermTag(longTermTag);
        if (jobs.size() <= maxAllowed)
            return "Nothing to do - job count="+jobs.size();

        Collections.sort(jobs,
                Comparator.comparing(UpdateCreateDateTimeEntity::getCreateTimeUTC));
        Collections.reverse(jobs);

        List<LinkCheckJob> jobsToDelete = jobs.subList(maxAllowed, jobs.size());

        String  result  = jobsToDelete.size() +" jobs were deleted.";
        for (LinkCheckJob job : jobsToDelete) {
            deleteById(job.getJobId());
            result+= job.getJobId()+",";
        }
        return result;

    }


}
