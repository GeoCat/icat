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

package net.geocat.eventprocessor.processors.datadownload.downloaders;

import net.geocat.database.linkchecker.entities.CapabilitiesDocument;
import net.geocat.database.linkchecker.entities.OGCRequest;
import net.geocat.database.linkchecker.entities.SimpleLayerDatasetIdDataLink;
import net.geocat.database.linkchecker.entities.SimpleLayerMetadataUrlDataLink;
import net.geocat.database.linkchecker.entities.SimpleSpatialDSIDDataLink;
import net.geocat.database.linkchecker.entities.SimpleStoredQueryDataLink;
import net.geocat.database.linkchecker.entities.helper.LinkToData;
import net.geocat.database.linkchecker.entities.helper.SHA2JobIdCompositeKey;
import net.geocat.database.linkchecker.repos.CapabilitiesDocumentRepo;
import net.geocat.database.linkchecker.repos.LinkCheckBlobStorageRepo;
import net.geocat.xml.XmlCapabilitiesDocument;
import net.geocat.xml.XmlCapabilitiesWFS;
import net.geocat.xml.XmlCapabilitiesWMS;
import net.geocat.xml.XmlCapabilitiesWMTS;
import net.geocat.xml.XmlDocumentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class OGCRequestGenerator {

    @Autowired
    CapabilitiesDocumentRepo capabilitiesDocumentRepo;

    @Autowired
    LinkCheckBlobStorageRepo linkCheckBlobStorageRepo;

    @Autowired
    XmlDocumentFactory xmlDocumentFactory;

    @Autowired
    WFSLayerDownloader wfsLayerDownloader;

    @Autowired
    WMSLayerDownloader wmsLayerDownloader;

    @Autowired
    WMTSLayerDownloader wmtsLayerDownloader;

    @Autowired
    WFSStoredQueryDownloader wfsStoredQueryDownloader;

    public OGCInfoCacheItem prep(String linkCheckJobId, String cap_sha2) throws Exception {
        CapabilitiesDocument capabilitiesDocument = capabilitiesDocumentRepo.findById(
                new SHA2JobIdCompositeKey(cap_sha2,linkCheckJobId)
        ).get();
        String xml = linkCheckBlobStorageRepo.findById(capabilitiesDocument.getSha2()).get().getTextValue();
        XmlCapabilitiesDocument doc = (XmlCapabilitiesDocument) xmlDocumentFactory.create(xml);
        return new OGCInfoCacheItem(capabilitiesDocument,doc);
    }

    //returns a (to be resolved) resolvable OGCRequest
    public OGCRequest prepareToDownload(LinkToData link,OGCInfoCacheItem ogcInfoCacheItem) throws Exception {
        CapabilitiesDocument capabilitiesDocument = ogcInfoCacheItem.getCapabilitiesDocument();
        XmlCapabilitiesDocument doc = ogcInfoCacheItem.getXmlCapabilitiesDocument();

        if (link instanceof SimpleLayerMetadataUrlDataLink) {
            SimpleLayerMetadataUrlDataLink _link = (SimpleLayerMetadataUrlDataLink) link;
            OGCRequest result =  downloadLayer(_link,capabilitiesDocument,doc);
            result.setLinkCheckJobId(link.getLinkCheckJobId());
            return result;
        }
        if (link instanceof SimpleStoredQueryDataLink) {
            SimpleStoredQueryDataLink _link = (SimpleStoredQueryDataLink) link;
            OGCRequest result =  downloadStoredQuery( _link, capabilitiesDocument, (XmlCapabilitiesWFS)doc);
            result.setLinkCheckJobId(link.getLinkCheckJobId());
            return result;
        }
        if (link instanceof SimpleLayerDatasetIdDataLink) {
            SimpleLayerDatasetIdDataLink _link = (SimpleLayerDatasetIdDataLink) link;
            OGCRequest result =  downloadLayer(_link,capabilitiesDocument,doc);
            result.setLinkCheckJobId(link.getLinkCheckJobId());
            return result;
        }
        if (link instanceof SimpleSpatialDSIDDataLink) {
            SimpleSpatialDSIDDataLink _link = (SimpleSpatialDSIDDataLink) link;
            OGCRequest result =  downloadLayer(_link,capabilitiesDocument,doc);
            result.setLinkCheckJobId(link.getLinkCheckJobId());
            return result;
        }
        return null;
    }

    private OGCRequest downloadLayer(SimpleSpatialDSIDDataLink link, CapabilitiesDocument capabilitiesDocument, XmlCapabilitiesDocument xmlCapabilitiesDocument) throws Exception {
        if (xmlCapabilitiesDocument instanceof XmlCapabilitiesWFS){
            return wfsLayerDownloader.setupRequest( (XmlCapabilitiesWFS) xmlCapabilitiesDocument, link.getOgcLayerName());
        }
        else if (xmlCapabilitiesDocument instanceof XmlCapabilitiesWMS){
            return wmsLayerDownloader.setupRequest( (XmlCapabilitiesWMS) xmlCapabilitiesDocument, link.getOgcLayerName());

        }
        else if (xmlCapabilitiesDocument instanceof XmlCapabilitiesWMTS){
            return wmtsLayerDownloader.setupRequest( (XmlCapabilitiesWMTS) xmlCapabilitiesDocument,
                    link.getOgcLayerName());

        }
        return null;
    }

    public OGCRequest downloadLayer(SimpleLayerDatasetIdDataLink link,
                                    CapabilitiesDocument capabilitiesDocument,
                                    XmlCapabilitiesDocument xmlCapabilitiesDocument) throws Exception {
        if (xmlCapabilitiesDocument instanceof XmlCapabilitiesWFS){
            return wfsLayerDownloader.setupRequest( (XmlCapabilitiesWFS) xmlCapabilitiesDocument, link.getOgcLayerName());
        }
        else if (xmlCapabilitiesDocument instanceof XmlCapabilitiesWMS){
            return wmsLayerDownloader.setupRequest( (XmlCapabilitiesWMS) xmlCapabilitiesDocument, link.getOgcLayerName());

        }
        else if (xmlCapabilitiesDocument instanceof XmlCapabilitiesWMTS){
            return wmtsLayerDownloader.setupRequest( (XmlCapabilitiesWMTS) xmlCapabilitiesDocument,
                    link.getOgcLayerName());

        }
        return null;
    }


    public OGCRequest downloadLayer(SimpleLayerMetadataUrlDataLink link,
                                    CapabilitiesDocument capabilitiesDocument,
                                    XmlCapabilitiesDocument xmlCapabilitiesDocument) throws Exception {
        if (xmlCapabilitiesDocument instanceof XmlCapabilitiesWFS){
            return wfsLayerDownloader.setupRequest( (XmlCapabilitiesWFS) xmlCapabilitiesDocument, link.getOgcLayerName());
        }
        else if (xmlCapabilitiesDocument instanceof XmlCapabilitiesWMS){
            return wmsLayerDownloader.setupRequest( (XmlCapabilitiesWMS) xmlCapabilitiesDocument, link.getOgcLayerName());

        }
        else if (xmlCapabilitiesDocument instanceof XmlCapabilitiesWMTS){
            return wmtsLayerDownloader.setupRequest( (XmlCapabilitiesWMTS) xmlCapabilitiesDocument,
                    link.getOgcLayerName());

        }
        return null;
    }

    public OGCRequest downloadStoredQuery(SimpleStoredQueryDataLink link,
                                          CapabilitiesDocument capabilitiesDocument,
                                          XmlCapabilitiesWFS xmlCapabilitiesWFS) throws Exception {
        return wfsStoredQueryDownloader.setupRequest(xmlCapabilitiesWFS,
                capabilitiesDocument.getProcGetSpatialDataSetName(),
                link.getCode(),
                link.getCodeSpace());
    }
}
