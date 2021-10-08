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

package net.geocat.runner;

import net.geocat.database.harvester.entities.MetadataRecord;
import net.geocat.database.harvester.repos.MetadataRecordRepo;
import net.geocat.database.linkchecker.entities.*;
import net.geocat.database.linkchecker.entities.helper.PartialDownloadHint;
import net.geocat.database.linkchecker.entities.helper.ServiceMetadataRecord;
import net.geocat.database.linkchecker.entities.helper.StatusQueryItem;
import net.geocat.database.linkchecker.entities.helper.IndicatorStatus;
import net.geocat.database.linkchecker.repos.*;
import net.geocat.database.linkchecker.service.*;
import net.geocat.eventprocessor.processors.processlinks.postprocessing.*;
import net.geocat.http.BasicHTTPRetriever;
import net.geocat.http.IHTTPRetriever;
import net.geocat.service.*;

import net.geocat.service.capabilities.DatasetLink;
import net.geocat.service.capabilities.WMSCapabilitiesDatasetLinkExtractor;
import net.geocat.service.downloadhelpers.PartialDownloadPredicateFactory;
import net.geocat.xml.*;

import net.geocat.service.downloadhelpers.PartialDownloadPredicateFactory;
import net.geocat.xml.XmlCapabilitiesDocument;
import net.geocat.xml.XmlDoc;
import net.geocat.xml.XmlDocumentFactory;
import net.geocat.xml.XmlServiceRecordDoc;

import net.geocat.xml.helpers.CapabilitiesType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class MyCommandLineRunner implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(MyCommandLineRunner.class);


    @Autowired
    BlobStorageService blobStorageService;

    @Autowired
    XmlDocumentFactory xmlDocumentFactory;

    @Autowired
    LinkCheckBlobStorageRepo linkCheckBlobStorageRepo;

    @Autowired
    @Qualifier("cookieAttachingRetriever")
    IHTTPRetriever retriever;

//    @Autowired
//    LinkRepo linkRepo;

    @Autowired
    MetadataRecordRepo metadataRecordRepo;
//
//    @Autowired
//    EndpointJobRepo endpointJobRepo;

    @Autowired
    ServiceDocLinkExtractor serviceDocLinkExtractor;

//    @Autowired
//    LinkProcessor_SimpleLinkRequest linkProcessor_simpleLinkRequest;

//    @Autowired
//    LinkProcessor_ProcessCapDoc linkProcessor_processCapDoc;

//    @Autowired
//    LinkProcessor_GetCapLinkedMetadata linkProcessor_getCapLinkedMetadata;

    @Autowired
    ServiceMetadataRecordService serviceMetadataRecordService;

    @Autowired
    ServiceDocumentLinkService serviceDocumentLinkService;

    @Autowired
    LocalServiceMetadataRecordRepo localServiceMetadataRecordRepo;

    @Autowired
    OperatesOnLinkService operatesOnLinkService;

    @Autowired
    RetrieveServiceDocumentLink retrieveServiceDocumentLink;

    @Autowired
    ServiceDocumentLinkRepo serviceDocumentLinkRepo;

    @Autowired
    RemoteServiceMetadataRecordLinkRetriever remoteServiceMetadataRecordLinkRetriever;

    @Autowired
    MetadataDocumentFactory metadataDocumentFactory;

    @Autowired
    RetrieveOperatesOnLink retrieveOperatesOnLink;

    @Autowired
    RemoteServiceMetadataRecordLinkRepo remoteServiceMetadataRecordLinkRepo;

    @Autowired
    OperatesOnLinkRepo operatesOnLinkRepo;

    @Autowired
    CapabilitiesDatasetMetadataLinkRepo capabilitiesDatasetMetadataLinkRepo;

    @Autowired
    RetrieveCapabilitiesDatasetMetadataLink retrieveCapabilitiesDatasetMetadataLink;


    @Autowired
    HumanReadableServiceMetadata humanReadableServiceMetadata;

    @Autowired
    CapabilitiesDocumentRepo capabilitiesDocumentRepo;

    @Autowired
    LocalDatasetMetadataRecordRepo localDatasetMetadataRecordRepo;

    @Autowired
    CapabilitiesResolvesIndicators capabilitiesResolvesIndicators;

    @Autowired
    CapabilitiesServiceLinkIndicators capabilitiesServiceLinkIndicators;

    @Autowired
    CapabilitiesServiceMatchesLocalServiceIndicators capabilitiesServiceMatchesLocalServiceIndicators;

    @Autowired
    CapabilitiesDatasetLinksResolveIndicators capabilitiesDatasetLinksResolveIndicators;

    @Autowired
    ServiceOperatesOnIndicators serviceOperatesOnIndicators;

    @Autowired
    CapabilitiesDatasetMetadataLinkService capabilitiesDatasetMetadataLinkService;

    @Autowired
    CapabilitiesRemoteDatasetMetadataDocumentRepo capabilitiesRemoteDatasetMetadataDocumentRepo;

    @Autowired
    DatasetToLayerIndicators datasetToLayerIndicators;

    @Autowired
    PartialDownloadPredicateFactory partialDownloadPredicateFactory;

    @Autowired
    BasicHTTPRetriever basicHTTPRetriever;

    @Override
    public void run(String... args) throws Exception {
        // run3(args);
      //  LocalServiceMetadataRecord sm11 = localServiceMetadataRecordRepo.findById(12248L).get();
        try {

        // run_2(args);
         //   run12(args);

        //   run12(args);

        //    run11(args);
        }
        catch(Exception e){
            int t=0;
        }
        logger.debug("DONE!");
    }

    public void run_2(String... args) throws Exception {

        LocalServiceMetadataRecord serviceRecord = localServiceMetadataRecordRepo.findById(298852L).get();
        LocalServiceMetadataRecord serviceRecord2 = localServiceMetadataRecordRepo.fullId(298852L);
        int t=0;
    }


        private  Node  findNode (Node n,String localName) {

        NodeList nl = n.getChildNodes();
        List<Node> result = new ArrayList<>();
        for (int idx=0; idx <nl.getLength();idx++) {
            Node nn = nl.item(idx);
            if (nn.getNodeName().equals(localName)) {
               return nn;
            }
        }
        return null;
    }


    private List<Node> findNodes(Node n,String localName) {

        NodeList nl = n.getChildNodes();
        List<Node> result = new ArrayList<>();
        for (int idx=0; idx <nl.getLength();idx++) {
            Node nn = nl.item(idx);
            if (nn.getNodeName().equals(localName)) {
                result.add(nn);
                result.addAll(findNodes(nn,localName)); // recurse
            }
        }
        return result;
    }


    public void run_1(String... args) throws Exception {
        long startTime;
        long endTime;

        LocalServiceMetadataRecord serviceRecord = localServiceMetadataRecordRepo.findById(161990L).get();
        String serviceXML = blobStorageService.findXML(serviceRecord.getSha2());
        String capXML = linkCheckBlobStorageRepo.findById("D4A9F85CF61F688ABF6545C818B19A348ED382CDA9CBDDF6444B20A0965C7CCE").get().getTextValue();

            startTime = System.currentTimeMillis();

         //   XmlDoc xdoc = new XmlDoc(capXML);

//                List<DatasetLink> result = new ArrayList<>();
//                NodeList layers = xdoc.xpath_nodeset("//"+"wms"+":Layer");
//                for (int idx = 0; idx < layers.getLength(); idx++) {
//                    logger.debug("doing layer "+idx+" of "+layers.getLength());
//                    Node layer = layers.item(idx);
//                    NodeList children = layer.getChildNodes();
//                    boolean removable = true;
//                    for (int idx2 = 0; idx2<children.getLength(); idx2++) {
//                        Node child = children.item(idx2);
//                        if (child.getNodeName().equals("Layer"))
//                            removable = false;
//                    }
//                    if (removable )
//                        layer.getParentNode().removeChild(layer);
//                    //DatasetLink link = processLayer(doc, layer);
//
//                }


//        WMSCapabilitiesDatasetLinkExtractor extractor2 = new WMSCapabilitiesDatasetLinkExtractor();
//        List<DatasetLink> ds = extractor2.findLinks(xdoc);

       // List<Node> ns = findNodes(findNode(xdoc.getFirstNode(),"Capability"),"Layer");

//
//        CachedXPathAPI cachedXPathAPI = new CachedXPathAPI();
//        NodeIterator xObject = cachedXPathAPI.selectNodeIterator(xdoc.getParsedXml(), "//*[local-name()='Layer']");
//        boolean keep_going = true;
//        int idx =0;
//        while (keep_going) {
//            Node n = xObject.nextNode();
//            keep_going = n != null;
//            if (n != null) {
//                logger.debug("indx = "+idx);
//                int t=0;
//            }
//            idx++;
//        }


             XmlCapabilitiesWMS wms = (XmlCapabilitiesWMS) xmlDocumentFactory.create(capXML);

            endTime = System.currentTimeMillis();
            System.out.println("WMS parse total execution time: " + (endTime - startTime));

        startTime = System.currentTimeMillis();

          XmlServiceRecordDoc service = (XmlServiceRecordDoc) xmlDocumentFactory.create(serviceXML);
        endTime = System.currentTimeMillis();
        System.out.println("service parse total execution time: " + (endTime - startTime));

      //  serviceRecord = localServiceMetadataRecordRepo.fullId(161990L) ;

        int t=0;
    }

    public CapabilitiesDocument createCap(XmlCapabilitiesDocument xml) throws Exception {
        String sha2 ="test case";
        CapabilitiesDocument doc = new CapabilitiesDocument();
        doc.setSha2(sha2);

       // doc.setParent(link);
        doc.setCapabilitiesDocumentType(xml.getCapabilitiesType());


        if (xml.isHasExtendedCapabilities()) {
            doc.setIndicator_HasExtendedCapabilities(IndicatorStatus.PASS);
        } else {
            doc.setIndicator_HasExtendedCapabilities(IndicatorStatus.FAIL);
            return doc;
        }

        String metadataUrl = xml.getMetadataUrlRaw();
        if ((metadataUrl == null) || (metadataUrl.isEmpty())) {
            doc.setIndicator_HasServiceMetadataLink(IndicatorStatus.FAIL);
            return doc;
        }

        doc.setIndicator_HasServiceMetadataLink(IndicatorStatus.PASS);



        List<CapabilitiesDatasetMetadataLink> dslinks = capabilitiesDatasetMetadataLinkService.createCapabilitiesDatasetMetadataLinks(doc, xml);
        doc.setCapabilitiesDatasetMetadataLinkList(dslinks);

        return doc;
    }


    public void run12(String... args) throws Exception {
        long startTime;
        long endTime;
//        LocalDatasetMetadataRecord rr1 =   localDatasetMetadataRecordRepo.findById( 39316L).get();
//
//        CapabilitiesDocument cdd = rr1.getDocumentLinks().get(1).getCapabilitiesDocument();
////     Optional<LocalDatasetMetadataRecord> rr =   localDatasetMetadataRecordRepo.findById_( 39316L);
//
//
//       startTime = System.currentTimeMillis();
//       List<CapabilitiesDatasetMetadataLink> abc = rr1.getDocumentLinks().get(1).getCapabilitiesDocument().getCapabilitiesDatasetMetadataLinkList();
//        startTime = System.currentTimeMillis();
//        CapabilitiesRemoteDatasetMetadataDocument def = abc.get(10).getCapabilitiesRemoteDatasetMetadataDocument();
//        startTime = System.currentTimeMillis();
//        startTime = System.currentTimeMillis();

//
//        List<LocalDatasetMetadataRecord> records2 =   localDatasetMetadataRecordRepo.partialByLinkCheckJobId("ef19f12a-14a9-4a71-8c79-6d1d178a3768");
//
//        endTime = System.currentTimeMillis();
//        System.out.println("records2 total execution time: " + (endTime - startTime));

//        startTime = System.currentTimeMillis();
//        List<LocalDatasetMetadataRecord> records3 =   localDatasetMetadataRecordRepo.fullByLinkCheckJobId("ef19f12a-14a9-4a71-8c79-6d1d178a3768");
//        endTime = System.currentTimeMillis();
//        System.out.println("records3 total execution time: " + (endTime - startTime));

        startTime = System.currentTimeMillis();
        String jobid= "22c3d08e-0146-40cd-b2b3-a4ab87a90c3f";
        List<LocalDatasetMetadataRecord> records =   localDatasetMetadataRecordRepo.findByLinkCheckJobId(jobid);

      //  records = records.stream().filter(x->x.getFileIdentifier().equals("46035049-89f3-4723-93e9-c1af0a1274ae")).collect(Collectors.toList());

        endTime = System.currentTimeMillis();
        System.out.println("records  total execution time: " + (endTime - startTime));

        int total =records.size();
        int cap_resolves =0;
        int layer_matches = 0;

        int layer_matches_view = 0;
        int layer_matches_download = 0;

        for(LocalDatasetMetadataRecord r:records) {
//            CapabilitiesDocument cc = r.getDocumentLinks().get(1).getCapabilitiesDocument();
//            String s = cc.toString();
            if (r.getINDICATOR_RESOLVES_TO_CAPABILITIES() > 0)
                cap_resolves++;
            else {
                LocalDatasetMetadataRecord rr = localDatasetMetadataRecordRepo.fullId(r.getDatasetMetadataDocumentId());
                int tt=0;
            }
            if (r.getINDICATOR_LAYER_MATCHES() == IndicatorStatus.PASS)
                layer_matches++;
            if (r.getINDICATOR_LAYER_MATCHES_DOWNLOAD() == IndicatorStatus.PASS)
                layer_matches_download++;
            else {
                LocalDatasetMetadataRecord rr = localDatasetMetadataRecordRepo.fullId(r.getDatasetMetadataDocumentId());

                List<CapabilitiesDocument> docs = rr.getDocumentLinks().stream()
                        .filter(x->x.getCapabilitiesDocument() != null)
                        .map(x->x.getCapabilitiesDocument())
                        .collect(Collectors.toList());
                List<String> xmls = docs.stream()
                        .map(x-> linkCheckBlobStorageRepo.findById(x.getSha2()).get().getTextValue())
                        .collect(Collectors.toList());

                List<XmlCapabilitiesDocument> xmlDocs = xmls.stream()
                        .map(x-> {
                            try {
                                return (XmlCapabilitiesDocument) xmlDocumentFactory.create(x);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return null;
                        })
                        .collect(Collectors.toList());
                List<CapabilitiesDatasetMetadataLink> dsLinks_download  =  rr.getDocumentLinks().stream()
                        .map(x->x.getCapabilitiesDocument())
                        .filter(x-> x != null)
                        .filter(x-> x.getCapabilitiesDocumentType() == CapabilitiesType.WFS||x.getCapabilitiesDocumentType() == CapabilitiesType.Atom)
                        .map(x->x.getCapabilitiesDatasetMetadataLinkList())
                        .flatMap(List::stream)
                        .filter(x-> x != null)
                        .collect(Collectors.toList());
                String rr_xml = blobStorageService.findXML(rr.getSha2());

                String ds_fileId = rr.getFileIdentifier();
                String ds_dsID = rr.getDatasetIdentifier();

//                List<LocalServiceMetadataRecord> items=  localServiceMetadataRecordRepo.searchByLinkCheckJobIdAndOperatesOnFileID(jobid, ds_fileId);
//                items = items.stream().map(x-> localServiceMetadataRecordRepo.fullId(x.getServiceMetadataDocumentId())).collect(Collectors.toList());


//                LocalServiceMetadataRecord ss1 = localServiceMetadataRecordRepo.fullId(14803L);
//
//                String ss_xml1 = blobStorageService.findXML(ss1.getSha2());
//
//                LocalServiceMetadataRecord ss2 = localServiceMetadataRecordRepo.fullId(14782L);
//                String ss_xml2 = blobStorageService.findXML(ss1.getSha2());

               // String url = ((ServiceDocumentLink) ss.getServiceDocumentLinks().toArray()[0]).getFinalURL();
              //  HttpResult hr = basicHTTPRetriever.retrieveXML("GET",url,null,null,partialDownloadPredicateFactory.create(PartialDownloadHint.CAPABILITIES_ONLY) );

               // List<CapabilitiesRemoteDatasetMetadataDocument> serviceRecords = capabilitiesRemoteDatasetMetadataDocumentRepo.findBylinkCheckJobIdAndFileIdentifier(jobid,rr.getFileIdentifier());



                int t=0;
            }
            if (r.getINDICATOR_LAYER_MATCHES_VIEW() == IndicatorStatus.PASS)
                layer_matches_view++;

            else if  (r.getINDICATOR_RESOLVES_TO_CAPABILITIES()>0) { //is a cap, but no match
                LocalDatasetMetadataRecord rr = localDatasetMetadataRecordRepo.fullId(r.getDatasetMetadataDocumentId());
//
//                String rr_xml = blobStorageService.findXML(rr.getSha2());
//
//                List<CapabilitiesDocument> docs = rr.getDocumentLinks().stream()
//                        .filter(x->x.getCapabilitiesDocument() != null)
//                        .map(x->x.getCapabilitiesDocument())
//                        .collect(Collectors.toList());
//                List<String> xmls = docs.stream()
//                        .map(x-> linkCheckBlobStorageRepo.findById(x.getSha2()).get().getTextValue())
//                        .collect(Collectors.toList());
//
//                List<XmlCapabilitiesDocument> xmlDocs = xmls.stream()
//                        .map(x-> {
//                            try {
//                                return (XmlCapabilitiesDocument) xmlDocumentFactory.create(x);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                            return null;
//                        })
//                        .collect(Collectors.toList());
//
//                List<CapabilitiesDatasetMetadataLink> dsLinks  =  rr.getDocumentLinks().stream()
//                        .map(x->x.getCapabilitiesDocument())
//                        .filter(x-> x != null)
//                        .map(x->x.getCapabilitiesDatasetMetadataLinkList())
//                        .flatMap(List::stream)
//                        .filter(x-> x != null)
//                        .collect(Collectors.toList());
//
//                List<CapabilitiesRemoteDatasetMetadataDocument>  capDatasetDocs =  rr.getDocumentLinks().stream()
//                        .map(x->x.getCapabilitiesDocument())
//                        .filter(x-> x != null)
//                        .map(x->x.getCapabilitiesDatasetMetadataLinkList())
//                        .flatMap(List::stream)
//                        .filter(x-> x != null)
//                        .map(x->x.getCapabilitiesRemoteDatasetMetadataDocument())
//                        .filter(x-> x != null)
//                        .filter(x->x.getDatasetIdentifier() !=null && x.getFileIdentifier() !=null)
//                        .collect(Collectors.toList());
//                datasetToLayerIndicators.process(rr);

                Optional<DatasetDocumentLink> doc = rr.getDocumentLinks().stream()
                        .filter(x->x.getCapabilitiesDocument() != null).findFirst();
                if (doc.isPresent()){
                    CapabilitiesDocument capdoc = doc.get().getCapabilitiesDocument();
                     String xmlCap = linkCheckBlobStorageRepo.findById(capdoc.getSha2()).get().getTextValue();
                     XmlCapabilitiesDocument xmlCapDoc = (XmlCapabilitiesDocument) xmlDocumentFactory.create(xmlCap);
                    List<CapabilitiesDatasetMetadataLink> dslinks = capabilitiesDatasetMetadataLinkService.createCapabilitiesDatasetMetadataLinks(capdoc, xmlCapDoc);

                    CapabilitiesDocument cd = createCap(xmlCapDoc);
                    cd= capabilitiesDocumentRepo.save(cd);
                    CapabilitiesDocument cd2=capabilitiesDocumentRepo.findById(cd.getCapabilitiesDocumentId()).get();

                    if (!capdoc.getCapabilitiesDatasetMetadataLinkList().isEmpty()) {
                         CapabilitiesDatasetMetadataLink dd = capdoc.getCapabilitiesDatasetMetadataLinkList().get(0);
                         int uu=0;
                     }
                    int ttt=0;
                 }
            }
        }

        double percent_cap_resolves = ((double) cap_resolves)/total * 100.0;
        double percent_layer_matches = ((double) layer_matches)/total * 100.0;
        double percent_layer_matches_view = ((double) layer_matches_view)/total * 100.0;
        double percent_layer_matches_download = ((double) layer_matches_download)/total * 100.0;


        logger.debug("Dataset");
        logger.debug("-------");
        logger.debug("% cap doc resolves: "+percent_cap_resolves+"%");
        logger.debug("% layer matches the dataset: "+percent_layer_matches+"%");
        logger.debug("% layer matches the dataset (view): "+percent_layer_matches_view+"%");
        logger.debug("% layer matches the dataset (download): "+percent_layer_matches_download+"%");

        logger.debug("number of docs: "+total+"");
        logger.debug("number cap doc resolves: "+cap_resolves+" ");
        logger.debug("number layer matches the dataset (either via view or download): "+layer_matches+" ");
        logger.debug("number layer matches the dataset (view): "+layer_matches_view+" ");
        logger.debug("number layer matches the dataset (download): "+layer_matches_download+" ");

        int tt=0;
    }
    public void run11(String... args) throws Exception {
    //   List<LocalServiceMetadataRecord> records =   localServiceMetadataRecordRepo.findByLinkCheckJobId("b7ee7707-698f-41ac-8dcf-ab0c43ab297a");
        List<LocalServiceMetadataRecord> records =   localServiceMetadataRecordRepo.findByLinkCheckJobId("5b7d9b34-0b8b-4959-88e6-0a93c278a5f3");


       int total =records.size();
       int cap_resolves = 0;
       int cap_resolves_to_service = 0;
        int cap_link_to_service_fileId = 0;
        int cap_link_to_service_full_match = 0;
        int cap_ds_links_resolve = 0;
        int all_opson_match = 0;

        for(LocalServiceMetadataRecord r:records){

//            String xml = blobStorageService.findXML(r.getSha2());
//            XmlDoc xmlDoc = xmlDocumentFactory.create(xml);
//
//
//            String xmlCap = linkCheckBlobStorageRepo.findById(r.getServiceDocumentLinks().get(0).getCapabilitiesDocument().getSha2()).get().getTextValue();
//            XmlDoc xmlCapDoc = xmlDocumentFactory.create(xmlCap);

//
//            capabilitiesServiceMatchesLocalServiceIndicators.process(r);
//           capabilitiesDatasetLinksResolveIndicators.process(r);
//            serviceOperatesOnIndicators.process(r);

            if (r.getINDICATOR_ALL_OPERATES_ON_MATCH_CAPABILITIES() == IndicatorStatus.PASS)
                all_opson_match++;
            else {

                LocalServiceMetadataRecord rr = localServiceMetadataRecordRepo.fullId(r.getServiceMetadataDocumentId());
                String rr_xml = blobStorageService.findXML(rr.getSha2());

                List<OperatesOnLink> opsonlinks = new ArrayList(rr.getOperatesOnLinks());

                List<OperatesOnRemoteDatasetMetadataRecord> opsonlinks_docs =     opsonlinks.stream()
                            .filter(x-> x.getDatasetMetadataRecord() !=null)
                            .map(x->x.getDatasetMetadataRecord())
                            .collect(Collectors.toList());

                List<String> opson_docs_xml = opsonlinks_docs.stream()
                        .map(x-> linkCheckBlobStorageRepo.findById(x.getSha2()).get().getTextValue())
                        .collect(Collectors.toList());


                List<String> opson_docs_fileid = opsonlinks_docs.stream()
                        .map(x-> x.getFileIdentifier())
                        .collect(Collectors.toList());


                List<String> opson_docs_dsid = opsonlinks_docs.stream()
                        .map(x-> x.getDatasetIdentifier())
                        .collect(Collectors.toList());

                List<CapabilitiesDocument> capDocs = rr.getServiceDocumentLinks().stream()
                            .filter(x->x.getCapabilitiesDocument() != null)
                            .map(x->x.getCapabilitiesDocument())
                            .collect(Collectors.toList());
                List<String> capDocs_xmls = capDocs.stream()
                        .map(x-> linkCheckBlobStorageRepo.findById(x.getSha2()).get().getTextValue())
                        .collect(Collectors.toList());

                List<CapabilitiesRemoteDatasetMetadataDocument> cap_dsdocs = capDocs.stream()
                        .map(x-> x.getCapabilitiesDatasetMetadataLinkList())
                        .flatMap(List::stream)
                        .map(x->x.getCapabilitiesRemoteDatasetMetadataDocument())
                        .filter(x->x  != null)
                        .collect(Collectors.toList());
                List<String> cap_dsdocs_xmls = cap_dsdocs.stream()
                        .map(x-> linkCheckBlobStorageRepo.findById(x.getSha2()).get().getTextValue())
                        .collect(Collectors.toList());

                boolean no_capabilities_doc = cap_dsdocs.isEmpty();
                boolean no_operates_on = opsonlinks.isEmpty();

                int tta=1;
//
//              if (r.getServiceDocumentLinks().stream().findFirst().get().getCapabilitiesDocument() != null) {
//                  String xmlCap = linkCheckBlobStorageRepo.findById(r.getServiceDocumentLinks().stream().findFirst().get().getCapabilitiesDocument().getSha2()).get().getTextValue();
//                  XmlDoc xmlCapDoc = xmlDocumentFactory.create(xmlCap);

 //                 int tta=1;
 //             }

                int tttt=0;
            }

           if (r.getINDICATOR_RESOLVES_TO_CAPABILITIES() >0)
                cap_resolves++;
           else {
//               LocalServiceMetadataRecord rr = localServiceMetadataRecordRepo.fullId(r.getServiceMetadataDocumentId());
//                     String rr_xml = blobStorageService.findXML(rr.getSha2());
           }

           if (r.getINDICATOR_CAPABILITIES_RESOLVES_TO_SERVICE() == IndicatorStatus.PASS)
               cap_resolves_to_service++;
           else {

//               if (r.getINDICATOR_RESOLVES_TO_CAPABILITIES() >0) {
//                   LocalServiceMetadataRecord rr = localServiceMetadataRecordRepo.fullId(r.getServiceMetadataDocumentId());
//                    String rr_xml = blobStorageService.findXML(rr.getSha2());
//
//                    List<CapabilitiesDocument> docs = rr.getServiceDocumentLinks().stream()
//                            .filter(x->x.getCapabilitiesDocument() != null)
//                            .map(x->x.getCapabilitiesDocument())
//                            .collect(Collectors.toList());
//                    List<String> xmls = docs.stream()
//                            .map(x-> linkCheckBlobStorageRepo.findById(x.getSha2()).get().getTextValue())
//                            .collect(Collectors.toList());
//
//                    List<XmlCapabilitiesDocument> xmlDocs = xmls.stream()
//                            .map(x-> {
//                                try {
//                                    return (XmlCapabilitiesDocument) xmlDocumentFactory.create(x);
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                                return null;
//                            })
//                            .collect(Collectors.toList());
//                    int u=0;
//               }
//               Optional<ServiceDocumentLink> doc = r.getServiceDocumentLinks().stream()
//                       .filter(x->x.getCapabilitiesDocument() != null).findFirst();
//               if (doc.isPresent()) {
//                   CapabilitiesDocument capdoc = doc.get().getCapabilitiesDocument();
//                   String xmlCap = linkCheckBlobStorageRepo.findById(capdoc.getSha2()).get().getTextValue();
//                   XmlCapabilitiesDocument xmlCapDoc = (XmlCapabilitiesDocument) xmlDocumentFactory.create(xmlCap);
//                   List<CapabilitiesDatasetMetadataLink> dslinks = capabilitiesDatasetMetadataLinkService.createCapabilitiesDatasetMetadataLinks(capdoc, xmlCapDoc);
//
//                   CapabilitiesDocument cd = createCap(xmlCapDoc);
//                   cd = capabilitiesDocumentRepo.save(cd);
//                   CapabilitiesDocument cd2 = capabilitiesDocumentRepo.findById(cd.getCapabilitiesDocumentId()).get();
//
//                   if (!capdoc.getCapabilitiesDatasetMetadataLinkList().isEmpty()) {
//                       CapabilitiesDatasetMetadataLink dd = capdoc.getCapabilitiesDatasetMetadataLinkList().get(0);
//                       int uu = 0;
//                   }
//                   int ttt = 0;
//               }
//               int ttttt = 0;
           }

           if (r.getINDICATOR_CAPABILITIES_SERVICE_FILE_ID_MATCHES() == IndicatorStatus.PASS)
               cap_link_to_service_fileId++;
           else {
//               if (r.getINDICATOR_CAPABILITIES_RESOLVES_TO_SERVICE() == IndicatorStatus.PASS) {
//                   LocalServiceMetadataRecord rr = localServiceMetadataRecordRepo.fullId(r.getServiceMetadataDocumentId());
//                   String rr_xml = blobStorageService.findXML(rr.getSha2());
//
//                   List<CapabilitiesDocument> docs = rr.getServiceDocumentLinks().stream()
//                           .filter(x -> x.getCapabilitiesDocument() != null)
//                           .map(x -> x.getCapabilitiesDocument())
//                           .collect(Collectors.toList());
//                   List<String> xmls = docs.stream()
//                           .map(x -> linkCheckBlobStorageRepo.findById(x.getSha2()).get().getTextValue())
//                           .collect(Collectors.toList());
//
//                   List<XmlCapabilitiesDocument> xmlDocs = xmls.stream()
//                           .map(x -> {
//                               try {
//                                   return (XmlCapabilitiesDocument) xmlDocumentFactory.create(x);
//                               } catch (Exception e) {
//                                   e.printStackTrace();
//                               }
//                               return null;
//                           })
//                           .collect(Collectors.toList());
//                   List<RemoteServiceMetadataRecordLink> cap2serviceLinks = docs.stream()
//                           .map(x->x.getRemoteServiceMetadataRecordLink())
//                           .filter (x -> x !=null)
//                           .collect(Collectors.toList());
//
//                   List<String> remoteServiceXMLs = cap2serviceLinks.stream()
//                           .filter (x-> x.getRemoteServiceMetadataRecord() != null)
//                           .map (x-> x.getRemoteServiceMetadataRecord().getSha2())
//                           .map(x-> linkCheckBlobStorageRepo.findById(x).get().getTextValue())
//                           .collect(Collectors.toList());
//
//                                   List<String> ids = cap2serviceLinks.stream()
//                           .filter (x-> x.getRemoteServiceMetadataRecord() != null)
//                           .map (x-> x.getRemoteServiceMetadataRecord() )
//                           .map(x->x.getFileIdentifier())
//                           .collect(Collectors.toList());
//
//                   String originalFileID = r.getFileIdentifier();
//
//                   int u = 0;
 //              }
           }
           if (r.getINDICATOR_CAPABILITIES_SERVICE_FULLY_MATCHES() == IndicatorStatus.PASS)
               cap_link_to_service_full_match++;

           if (r.getINDICATOR_ALL_CAPABILITIES_LAYER_RESOLVE() == IndicatorStatus.PASS)
               cap_ds_links_resolve++;
           else {
//               if (r.getINDICATOR_RESOLVES_TO_CAPABILITIES() >0) {
//                   LocalServiceMetadataRecord rr = localServiceMetadataRecordRepo.fullId(r.getServiceMetadataDocumentId());
//                   String rr_xml = blobStorageService.findXML(rr.getSha2());
//
//                   List<CapabilitiesDocument> docs = rr.getServiceDocumentLinks().stream()
//                           .filter(x -> x.getCapabilitiesDocument() != null)
//                           .map(x -> x.getCapabilitiesDocument())
//                           .collect(Collectors.toList());
//                   List<String> xmls = docs.stream()
//                           .map(x -> linkCheckBlobStorageRepo.findById(x.getSha2()).get().getTextValue())
//                           .collect(Collectors.toList());
//
//                   List<XmlCapabilitiesDocument> xmlDocs = xmls.stream()
//                           .map(x -> {
//                               try {
//                                   return (XmlCapabilitiesDocument) xmlDocumentFactory.create(x);
//                               } catch (Exception e) {
//                                   e.printStackTrace();
//                               }
//                               return null;
//                           })
//                           .collect(Collectors.toList());
//
//                   List<CapabilitiesDatasetMetadataLink> ds_links_fail = docs.stream()
//                           .map(x->x.getCapabilitiesDatasetMetadataLinkList())
//                           .flatMap(List::stream)
//                           .filter (x->x != null)
//                           .filter(x-> x.getCapabilitiesRemoteDatasetMetadataDocument() == null)
//                           .collect(Collectors.toList());
//
//                   int uu=0;
//               }
           }

           int ttt=0;
       }

       double percent_cap_resolves = ((double) cap_resolves)/total * 100.0;
       double percent_cap_resolves_to_service = ((double) cap_resolves_to_service)/total * 100.0;

       double percent_cap_to_service_fileID = ((double) cap_link_to_service_fileId)/total * 100.0;
       double percent_cap_to_service_full = ((double) cap_link_to_service_full_match)/total * 100.0;

       double percent_cap_ds_links_resolve = ((double) cap_ds_links_resolve)/total * 100.0;
       double percent_all_ops_on_matches = ((double) all_opson_match)/total * 100.0;


        logger.debug("Service");
        logger.debug("-------");
        logger.debug("% cap doc resolves: "+percent_cap_resolves+"%");
        logger.debug("% cap doc resolves to service: "+percent_cap_resolves_to_service+"%");
        logger.debug("% cap doc's service matches original service record (fileid): "+percent_cap_to_service_fileID+"%");
        logger.debug("% cap doc's service matches original service record (full xml): "+percent_cap_to_service_full+"%");
        logger.debug("% cap doc's Dataset links resolve: "+percent_cap_ds_links_resolve+"%");
        logger.debug("% all operatas on matches : "+percent_all_ops_on_matches+"%");
        int t=0;
    }

        public void run10(String... args) throws Exception {
        LocalServiceMetadataRecord r =localServiceMetadataRecordRepo.findById(1L).get();
        String xml = blobStorageService.findXML(r.getSha2());
        XmlDoc xmlDoc = xmlDocumentFactory.create(xml);


         String xmlCap = linkCheckBlobStorageRepo.findById(r.getServiceDocumentLinks().stream().findFirst().get().getCapabilitiesDocument().getSha2()).get().getTextValue();
        XmlDoc xmlCapDoc = xmlDocumentFactory.create(xmlCap);

        capabilitiesServiceLinkIndicators.process(r);

    }

        public void run9(String... args) throws Exception {
        for(LocalServiceMetadataRecord localServiceMetadataRecord : localServiceMetadataRecordRepo.findAll()){
            capabilitiesServiceLinkIndicators.process(localServiceMetadataRecord);
        }
//        for(LocalDatasetMetadataRecord localDatasetMetadataRecord : localDatasetMetadataRecordRepo.findAll()){
//            capabilitiesResolvesIndicators.process(localDatasetMetadataRecord);
//        }

    }

        public void run8(String... args) throws Exception {
        Iterable<LocalDatasetMetadataRecord> records =  localDatasetMetadataRecordRepo.findAll();

        for (LocalDatasetMetadataRecord r:records){
            //String xml = linkCheckBlobStorageRepo.findById(r.getSha2()).get().getTextValue();

            String xml = blobStorageService.findXML(r.getSha2());
            XmlDoc xmlDoc = xmlDocumentFactory.create(xml);

            int t=0;
        }
    }

    public void run7(String... args) throws Exception {
      //  LinkCheckBlobStorage r1 =  linkCheckBlobStorageRepo.findById("16ADA3D6D77421D38E957705B7E7C06ABF2270242C04D669426C3814F5F6D584").get();
      //  LinkCheckBlobStorage r2 = linkCheckBlobStorageRepo.findById("988232D8B3AEDF0782B0D290C0DCBB324ACDAEFD98F606C44304B0444D7B658E").get();
        List<CapabilitiesDocument> docs = capabilitiesDocumentRepo.findByCapabilitiesDocumentType(CapabilitiesType.WFS);

        List<LinkCheckBlobStorage> xmls = docs.stream().map(x->linkCheckBlobStorageRepo.findById(x.getSha2()).get()).collect(Collectors.toList());

//        List<XmlDoc> docs2 = new ArrayList<>();
//        for (LinkCheckBlobStorage x : xmls) {
//            XmlDoc xmlDoc = xmlDocumentFactory.create(x.getTextValue());
//            docs2.add(xmlDoc);
//        }

        for(CapabilitiesDocument doc : docs) {
            ServiceMetadataRecord localService = doc.getServiceDocumentLink().getLocalServiceMetadataRecord();
            Set<OperatesOnLink> opsOns = localService.getOperatesOnLinks();
            List<CapabilitiesDatasetMetadataLink> capDSs = doc.getCapabilitiesDatasetMetadataLinkList();
//            if ( (opsOns.size() ==1) && (capDSs.size() == 1) ) {
//                OperatesOnLink opOn = opsOns.
//                OperatesOnRemoteDatasetMetadataRecord OpOnDSMD = opOn.getDatasetMetadataRecord();
//                CapabilitiesDatasetMetadataLink capDSLink = capDSs.get(0);
//                CapabilitiesRemoteDatasetMetadataDocument capDSMD = capDSLink.getCapabilitiesRemoteDatasetMetadataDocument();
//                if ( (OpOnDSMD != null) && (capDSMD !=null)){
//                    int tt=0;
//                }
//            }
            int t=0;
        }


        int t=0;
    }

    public void run6(String... args) throws Exception {
        List<StatusQueryItem> items = localServiceMetadataRecordRepo.getStatus("6da77404-7427-4c88-80a3-0e8eb83966ea");
        int t=0;
    }
        public void run5(String... args) throws Exception {
        List<LocalServiceMetadataRecord> localServiceMetadataRecords = localServiceMetadataRecordRepo.findByLinkCheckJobId("6fa41298-a2c7-44c6-b1b1-55d04064144d");
        for(LocalServiceMetadataRecord record : localServiceMetadataRecords){
            String human = record.getHumanReadable();
            String id = record.getFileIdentifier();
            try (PrintStream out = new PrintStream(new FileOutputStream(id+".txt"))) {
                out.print(human);
            }
        }
    }

        public void run4(String... args) throws Exception {
        LocalServiceMetadataRecord localServiceMetadataRecord = localServiceMetadataRecordRepo.findById(1132L).get();
        LocalServiceMetadataRecord smr = localServiceMetadataRecordRepo.save(localServiceMetadataRecord);

        List<LocalServiceMetadataRecord> list = Arrays.asList(new LocalServiceMetadataRecord[]{smr});

    }


        public void run3(String... args) throws Exception {
        String sha2_wmts = "49D36EFBA4B7A2541D31E44FC9557A8BEB0A7E275F1F22AAA00EEE953C41FF70";

        String sha2_atom = "76ABB39998E3EF07307059CC3B703A3959A48447DB98203FBF686856E0C6D4E3";

        String sha2_wms = "CB17C3851790A134F62C391DD9FFCC51052BF3451ADDE1ED07CA79430E4C1281";
        String sha2_wfs = "9ADC3010703B006F47870F7572D4C796DDF5542ADB3B4DF923015E1095FA3A89";

        String sha2_wms2 = "5E189A51157C500E3D870653B976706D7B97EF23B9220DF2DC99B8592693845E";
        String sha2_wms3 = "F42EA03157107422350861C3EECC260BF7CF06A01D206113F85D2298BC24123F";

        String sha2 = "0E75379A7CA9984B6AEFDEBCFA5AC92A0FE1D3BB47F2D1919BC318F49A7AB91C";

        MetadataRecord metadataRecord = metadataRecordRepo.findBySha2(sha2).get(0);
        String xml = blobStorageService.findXML(sha2);


        XmlServiceRecordDoc doc = (XmlServiceRecordDoc) xmlDocumentFactory.create(xml);

        LocalServiceMetadataRecord localServiceMetadataRecord =
                metadataDocumentFactory.createLocalServiceMetadataRecord(doc, metadataRecord.getMetadataRecordId(), "TESTCASE", sha2);

//        List<OnlineResource> links = serviceDocLinkExtractor.extractOnlineResource(doc);
//
//        List<ServiceDocumentLink> links2 = links.stream().map(x->serviceDocumentLinkService.create(localServiceMetadataRecord,x)).collect(Collectors.toList());
//        localServiceMetadataRecord.setServiceDocumentLinks(links2);
//
//        List<OperatesOnLink> operatesOnLinks = doc.getOperatesOns().stream()
//                                .map(x->operatesOnLinkService.create(localServiceMetadataRecord,x))
//                .collect(Collectors.toList());
//
        //    localServiceMetadataRecord.setOperatesOnLinks(operatesOnLinks);

        LocalServiceMetadataRecord sm11 = localServiceMetadataRecordRepo.findById(1L).get();
        CapabilitiesDocument cd = sm11.getServiceDocumentLinks().stream().findFirst().get().getCapabilitiesDocument();
        RemoteServiceMetadataRecordLink cd_rsmrl = cd.getRemoteServiceMetadataRecordLink();
        String ss = cd_rsmrl.toString();
        Set<OperatesOnLink> ll = sm11.getOperatesOnLinks();
        int t6t = ll.size();
        String human = humanReadableServiceMetadata.getHumanReadable(sm11);

        LocalServiceMetadataRecord smr = localServiceMetadataRecordRepo.save(localServiceMetadataRecord);

        List<LocalServiceMetadataRecord> list = Arrays.asList(new LocalServiceMetadataRecord[]{smr});
            process(list);

            int t = 0;
    }

    private void process(List<LocalServiceMetadataRecord> list) throws Exception {
        for (LocalServiceMetadataRecord record : list) {

            List<RemoteServiceMetadataRecordLink> remoteLinks = new ArrayList<>();
            List<CapabilitiesDatasetMetadataLink> capabilitiesDatasetMetadataLinkList = new ArrayList<>();

            //process servicedocumentlinks
            for (ServiceDocumentLink link : record.getServiceDocumentLinks()) {
                link = serviceDocumentLinkRepo.findById(link.getServiceMetadataLinkId()).get();// make sure we re-load
                ServiceDocumentLink link2 = (ServiceDocumentLink) retrieveServiceDocumentLink.process(link);
                link2 = serviceDocumentLinkRepo.save(link2); //re-save
                if (link2.getCapabilitiesDocument() != null) {
                    CapabilitiesDocument capabilitiesDocument = link2.getCapabilitiesDocument();
                    RemoteServiceMetadataRecordLink rsmrl = capabilitiesDocument.getRemoteServiceMetadataRecordLink();
                    if (rsmrl != null) {
                        remoteLinks.add(rsmrl);
                    }
                    for (CapabilitiesDatasetMetadataLink capabilitiesDatasetMetadataLink : capabilitiesDocument.getCapabilitiesDatasetMetadataLinkList()) {
                        capabilitiesDatasetMetadataLinkList.add(capabilitiesDatasetMetadataLink);
                    }
                }

            }

            //process resulting remoteMetadataRecordLinks
            for (RemoteServiceMetadataRecordLink link : remoteLinks) {
                RemoteServiceMetadataRecordLink link2 = remoteServiceMetadataRecordLinkRepo.findById(link.getRemoteServiceMetadataRecordLinkId()).get();
                //   CapabilitiesDocument cd = link2.getCapabilitiesDocument();
                RemoteServiceMetadataRecordLink rsmrl = remoteServiceMetadataRecordLinkRetriever.process(link2);
                RemoteServiceMetadataRecordLink rsmrl2 = remoteServiceMetadataRecordLinkRepo.save(rsmrl);
                int t = 0;
            }

            //process resulting capabilities doc dataset links
            for (CapabilitiesDatasetMetadataLink link : capabilitiesDatasetMetadataLinkList) {
                CapabilitiesDatasetMetadataLink link2 = capabilitiesDatasetMetadataLinkRepo.findById(link.getCapabilitiesDatasetMetadataLinkId()).get();
                //   CapabilitiesDocument cd = link2.getCapabilitiesDocument();
                CapabilitiesDatasetMetadataLink cdml = retrieveCapabilitiesDatasetMetadataLink.process(link2,"testcase");
                CapabilitiesDatasetMetadataLink cdml2 = capabilitiesDatasetMetadataLinkRepo.save(cdml);
                int t = 0;
            }


            for (OperatesOnLink operatesOnLink : record.getOperatesOnLinks()) {
                operatesOnLink = operatesOnLinkRepo.findById(operatesOnLink.getOperatesOnLinkId()).get(); //reload
                OperatesOnLink operatesOnLink2 = retrieveOperatesOnLink.process(operatesOnLink,"testcase");
                operatesOnLink2 = operatesOnLinkRepo.save(operatesOnLink2);
                int tt = 0;
            }
        }
    }

    public void run2(String... args) throws Exception {

        logger.debug("hi there");
//       HttpResult r= retriever.retrieveXML("GET","https://google.com/badurl", null, null,null);
//    int ut=0;

        //Optional<Link> l = linkRepo.findById(1L);
//
//        String sha2_wms = "65B3FFA90B5B277B34C7206D0283B7CD1FE89AE473998A566239992C4A59A417";
//        String sha2_wfs = "8759C3E02840BC5DE3F81B23F4AF1D124821384252AC018A0BDEB0386B76E663";
//        String sha2_atom = "546108E2F8D91EEE3E0684736F1469953EF8F71A73A63849E19B84D027704857";
//        String sha2_wmts = "49D36EFBA4B7A2541D31E44FC9557A8BEB0A7E275F1F22AAA00EEE953C41FF70";
//
//        String sha2 = sha2_wms;
//
//        MetadataRecord metadataRecord = metadataRecordRepo.findBySha2(sha2).get(0);
//        EndpointJob endpointJob = endpointJobRepo.findById(metadataRecord.getEndpointJobId()).get();
//        String xml = blobStorageService.findXML(sha2);

//        EndpointJob endpointJob = endpointJobRepo.findById(40L).get();
//        List<MetadataRecord> records = metadataRecordRepo.findByEndpointJobId(endpointJob.getEndpointJobId());

        // records = records.subList(34,40);
        //  records = Arrays.asList( new MetadataRecord[]{records.get(66)});
        //records = Arrays.asList( new MetadataRecord[]{records.get(424),records.get(501),records.get(446),records.get(448)});
        //  records = Arrays.asList( new MetadataRecord[]{records.get(446),records.get(448)});
        // records = Arrays.asList( new MetadataRecord[]{records.get(33),records.get(37)});

        // MetadataRecord record = records.get(6);

//        for(MetadataRecord record : records) {
//            try {
//                String sha2 = record.getSha2();
//                String xml = blobStorageService.findXML(sha2);
//
//                List<Link> links = serviceDocLinkExtractor.extractLinks(
//                        xml, sha2, endpointJob.getHarvestJobId(), endpointJob.getEndpointJobId(),"testcase");
//                for (Link ll : links) {
//                    try {
//                        ll = linkProcessor_simpleLinkRequest.process(ll);
//
//                        ll = linkProcessor_processCapDoc.process(ll);
//
//                        ll = linkProcessor_getCapLinkedMetadata.process(ll);
//                     //   linkRepo.save(ll);
//                        int t = 0;
//                    }
//                    catch(Exception e)
//                    {
//                        int tt=0;
//                    }
//                }
//            }
//            catch(NotServiceRecordException notServiceRecordException){
//                int utt=0;
//            }
//            catch(Exception e){
//                int t=0;
//            }
//        }
//        XmlDoc xmlDoc = xmlDocumentFactory.create(xml);
//
//        if (xmlDoc instanceof XmlServiceRecordDoc)
//        {
//            XmlServiceRecordDoc xmlServiceRecord = (XmlServiceRecordDoc) xmlDoc;
//            String capURL = xmlServiceRecord.getConnectPoints().get(0).getRawURL();
//            String capXML = new String(retriever.retrieveXML("GET",capURL,null,null,null));
//            XmlDoc capXMLDoc = xmlDocumentFactory.create(capXML);
//            int t=0;
//        }

    }
}

