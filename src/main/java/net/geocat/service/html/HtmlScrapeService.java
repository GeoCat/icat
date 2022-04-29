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

package net.geocat.service.html;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import net.geocat.database.linkchecker.entities.HttpResult;
import net.geocat.database.linkchecker.entities.LinkCheckJob;
import net.geocat.database.linkchecker.repos.LinkCheckJobRepo;
import net.geocat.http.HTTPRequest;
import net.geocat.http.HttpRequestFactory;
import net.geocat.http.SmartHTTPRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
public class HtmlScrapeService {

    @Autowired
    LinkCheckJobRepo linkCheckJobRepo;

    @Autowired
    @Qualifier("entityManagerFactory")
    LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean;


//    @Autowired
//    BasicHTTPRetriever basicHTTPRetriever;

    @Autowired
    SmartHTTPRetriever smartHTTPRetriever;

    @Autowired
    HttpRequestFactory httpRequestFactory;

    @Autowired
    //@Qualifier("transactionManager")
    PlatformTransactionManager transactionManager;



    public void executeSQL2(String sql) {
        EntityManager entityManager  =  localContainerEntityManagerFactoryBean.createNativeEntityManager(null);
        try {
            entityManager.getTransaction().begin();
            int n = entityManager.createNativeQuery(sql).executeUpdate();
            entityManager.getTransaction().commit();
        }
        finally {
            entityManager.close();
        }
    }

    public List executeSQL3(String sql) {
        EntityManager entityManager  =  localContainerEntityManagerFactoryBean.createNativeEntityManager(null);

       try {
           entityManager.getTransaction().begin();
           List result = entityManager.createNativeQuery(sql).getResultList();
           entityManager.getTransaction().commit();
           return result;
       }
       finally {
           entityManager.close();
       }
    }


    public String lastLinkCheckJob(String country){

        LinkCheckJob lastJob = null;
        for(LinkCheckJob job : linkCheckJobRepo.findAll()){
            if (!job.getLongTermTag().toLowerCase().startsWith(country.toLowerCase()))
                continue;
            if (lastJob == null)
                lastJob = job;
            if (lastJob.getCreateTimeUTC().compareTo(job.getCreateTimeUTC()) <1)
                lastJob = job;
        }
        return lastJob.getJobId();
    }


    /*
   delete from scrap;
   drop table scrap;
     CREATE TABLE scrap (title text, is_view boolean, is_download boolean, country_code text,
               file_id text, local_is_view boolean , local_is_download boolean);

       update scrap set file_id = (select fileidentifier from datasetmetadatarecord where datasetmetadatarecord.title = scrap.title limit 1);
       update scrap set local_is_view = (select indicator_layer_matches_view = 'PASS' from datasetmetadatarecord where datasetmetadatarecord.fileidentifier = scrap.file_id and linkcheckjobid='54a30bc1-3900-48d6-8cd3-c9d3609ee0a9');
       update scrap set local_is_download = (select indicator_layer_matches_download = 'PASS' from datasetmetadatarecord where datasetmetadatarecord.fileidentifier = scrap.file_id and linkcheckjobid='54a30bc1-3900-48d6-8cd3-c9d3609ee0a9') ;
--delete from scrap where file_id is null;




       select file_id, title, is_view, local_is_view from scrap where is_view !=  local_is_view and is_view and title is not null order by title;
       select file_id, title, is_download, local_is_download from scrap where is_download !=  local_is_download and is_download and title is not null order by title;



    */

    public void run_scrape(String countryCode, String jobid) throws  Exception {
        try{
            countryCode = countryCode.toLowerCase().substring(0,2);
            String sql =  "drop table if exists scrap;";
            executeSQL2(sql);
        }
        catch (Exception e){}

        try{
            String sql =  "CREATE TABLE scrap (geoportalid text, title text, is_view boolean, is_download boolean, country_code text,\n" +
                    "                file_id text, local_is_view boolean , local_is_download boolean, local_is_view_download boolean, local_is_download_download boolean);";
            executeSQL2(sql);
        }
        catch (Exception e){}

        String url = "https://inspire-geoportal.ec.europa.eu/solr/select?wt=json&q=*:*^1.0&sow=false&fq=sourceMetadataResourceLocator:*&fq=resourceType:(dataset%20OR%20series)&fq=memberStateCountryCode:%22MYCOUNTRYCODE%22&fl=id,resourceTitle,resourceTitle_*,providedTranslationLanguage,automatedTranslationLanguage,memberStateCountryCode,metadataLanguage,isDw:query($isDwQ),isVw:query($isVwQ),spatialScope&isDwQ=interoperabilityAspect:(DOWNLOAD_MATCHING_DATA_IS_AVAILABLE%20AND%20DATA_DOWNLOAD_LINK_IS_AVAILABLE)&isVwQ=interoperabilityAspect:(LAYER_MATCHING_DATA_IS_AVAILABLE)&isDwVwQ=interoperabilityAspect:(DOWNLOAD_MATCHING_DATA_IS_AVAILABLE%20AND%20DATA_DOWNLOAD_LINK_IS_AVAILABLE%20AND%20LAYER_MATCHING_DATA_IS_AVAILABLE)&sort=query($isDwVwQ)%20desc,%20query($isDwQ)%20desc,%20query($isVwQ)%20desc,%20resourceTitle%20asc&start=0&rows=300000&callback=?&json.wrf=processData_dtResults&_=1634538073094";
        url = url.replace("MYCOUNTRYCODE",countryCode.toLowerCase());


        HTTPRequest request = httpRequestFactory.createGET(url,null);
        request.setSaveToCache(false);
        HttpResult result = smartHTTPRetriever.retrieve(request);

       // HttpResult result = basicHTTPRetriever.retrieveJSON("GET", url, null, null, null);

        String json = new String(result.getData());
        json = json.replace("processData_dtResults(","");

        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readValue(json, JsonNode.class);

        JsonNode response =  rootNode.get("response");
        ArrayNode docs = (ArrayNode) response.get("docs");

        // executeSQL2("create table delme(i int)");
        for(JsonNode doc : docs) {
            String title = doc.get("resourceTitle").asText().replace("'","''");
            String id = doc.get("id").asText();
            boolean isView = (doc.get("isVw") != null);
            boolean isDownload = (doc.get("isDw") != null);
            String country = doc.get("memberStateCountryCode").asText();
            String sql =  String.format("INSERT INTO scrap  (title,is_view,is_download, country_code,geoportalid) VALUES ('%s',%s,%s,'%s','%s') "
                    , title, String.valueOf(isView), String.valueOf(isDownload),country, id);
            executeSQL2(sql);
            int tt=0;

        }
        int t=0;

        String sql =  "update scrap set file_id = (select fileidentifier from datasetmetadatarecord where datasetmetadatarecord.title = scrap.title limit 1);";
        executeSQL2(sql);


        sql =  " update scrap set local_is_view = (select indicator_view_link_to_data = 'PASS' from datasetmetadatarecord where datasetmetadatarecord.fileidentifier = scrap.file_id and linkcheckjobid='"+jobid+"');";
        executeSQL2(sql);

        sql =  " update scrap set local_is_view_download = (select numberofviewlinkssuccessful>0 from datasetmetadatarecord where datasetmetadatarecord.fileidentifier = scrap.file_id and linkcheckjobid='"+jobid+"');";
        executeSQL2(sql);

        sql =  " update scrap set local_is_download = (select indicator_download_link_to_data = 'PASS' from datasetmetadatarecord where datasetmetadatarecord.fileidentifier = scrap.file_id and linkcheckjobid='"+jobid+"') ";
        executeSQL2(sql);

        sql =  " update scrap set local_is_download_download = (select numberofdownloadlinkssuccessful>0 from datasetmetadatarecord where datasetmetadatarecord.fileidentifier = scrap.file_id and linkcheckjobid='"+jobid+"') ";
        executeSQL2(sql);
    }

    static Object lockObject = new Object();

    public String getHtml(String item) throws Exception {
        synchronized (lockObject) {
            if ((item == null) || (item.trim().isEmpty()))
                throw new Exception("item is empty");
            item = item.trim();

            Optional<LinkCheckJob> _job = linkCheckJobRepo.findById(item);
            if (!_job.isPresent()) {
                //not a job - must be a country
                _job =linkCheckJobRepo.findById(lastLinkCheckJob(item));
            }
            if (!_job.isPresent())
                throw new Exception("cannot find - "+item);

            LinkCheckJob  job = _job.get();

            String linkCheckJob = job.getJobId();



            run_scrape(job.getLongTermTag(), linkCheckJob);
            String result = "<head><meta charset=\"UTF-8\"></head>\n";

            result += "<h1>Differences with GeoPortal - " + linkCheckJob + " - "+job.getLongTermTag()+ "</h1><hr>";

            //       select file_id, title, is_view, local_is_view from scrap where is_view !=  local_is_view and is_view and title is not null order by title;
            //        select file_id, title, is_download, local_is_download from scrap where is_download !=  local_is_download and is_download and title is not null order by title;

            List queryResult = executeSQL3("select file_id, title, is_view, local_is_view,local_is_view_download,geoportalid from scrap where ((is_view !=  local_is_view) or (is_view !=  local_is_view_download)) and is_view and title is not null order by title");
            result += "<h1> View Differences - " + queryResult.size() + " differences (geoportal viewable)</h2>\n";
            if (!queryResult.isEmpty()) {
                result += "<b>Geoportal says these are Viewable, but we do not</b><Br><br>";
                result += results(linkCheckJob, queryResult,"viewable");
            } else {
                result += "NO DIFFERENCES<br>\n";
            }

            List queryResult2 = executeSQL3("select file_id, title, is_download, local_is_download,local_is_download_download,geoportalid from scrap where ((is_download !=  local_is_download) or (is_download !=  local_is_download_download) ) and is_download and title is not null order by title;");
            result += "<h1> Download Differences - " + queryResult2.size() + " differences (geoportal downloadable)</h2>\n";
            if (!queryResult2.isEmpty()) {
                result += "<b>Geoportal says these are downloadable, but we do not</b><Br><br>";
                result += results(linkCheckJob, queryResult2,"downloadable");
            } else {
                result += "NO DIFFERENCES<br>\n";
            }

            List queryResult3 = executeSQL3("select file_id, title, is_view, local_is_view,local_is_view_download,geoportalid from scrap where ((is_view !=  local_is_view) or (is_view !=  local_is_view_download)) and not(is_view) and title is not null order by title");
            result += "<h1> View Differences - " + queryResult3.size() + " differences (geoportal not viewable)</h2>\n";
            if (!queryResult3.isEmpty()) {
                result += "<b>Geoportal says these are NOT Viewable, but we say they are (perhaps just not accessible?)</b><Br><br>";
                result += results(linkCheckJob, queryResult3,"viewable");
            } else {
                result += "NO DIFFERENCES<br>\n";
            }

            List queryResult4 = executeSQL3("select file_id, title, is_download, local_is_download,local_is_download_download,geoportalid from scrap where((is_download !=  local_is_download) or (is_download !=  local_is_download_download) ) and not(is_download) and title is not null order by title;");
            result += "<h1> Download Differences - " + queryResult4.size() + " differences (geoportal not downloadable)</h2>\n";
            if (!queryResult4.isEmpty()) {
                result += "<b>Geoportal says these are NOT downloadable, but we say they are (perhaps just not accessible?)</b><Br><br>";
                result += results(linkCheckJob, queryResult4,"downloadable");
            } else {
                result += "NO DIFFERENCES<br>\n";
            }

            result += "<br><br><br>\n";
            return result;
        }
    }

    private String results(String linkcheckjob,List queryResult, String type) {
        String result ="";
        result +="<table border=1><tr> <td>File Identifier</td> <td>Title</td> <td>Geoportal - "+type+" </td><td>Local - "+type+" connected</td><td>Local - connected and "+type+"</td></tr>";
        for (Object o:queryResult) {
            Object[] cols = (Object[])o;
            String link = "<a href='/api/html/dataset/"+linkcheckjob+"/"+cols[0].toString()+"'>"+cols[0].toString()+"</a>";
            String link2 = "<a href='https://inspire-geoportal.ec.europa.eu/download_details.html?view=downloadDetails&resourceId="+cols[5].toString()+"&expandedSection=metadata'>"+ cols[1].toString()+"</a>";
            result +=" <tr> <td>"+link+"</td> <td>"+link2+"</td> <td>"+cols[2].toString()+"</td><td>"+cols[3].toString()+"</td><td>"+cols[4].toString()+"</td></tr>";
        }
        result += "</table>";
        return result;
    }


}
