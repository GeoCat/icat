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

package net.geocat.xml;

import net.geocat.service.capabilities.DatasetLink;
import net.geocat.xml.helpers.AtomEntry;
import net.geocat.xml.helpers.AtomLink;
import net.geocat.xml.helpers.CapabilitiesType;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

import static net.geocat.service.capabilities.WMSCapabilitiesDatasetLinkExtractor.findNodes;

public class XmlCapabilitiesAtom extends XmlCapabilitiesDocument {

    List<AtomEntry> entries = new ArrayList<>();

    public XmlCapabilitiesAtom(XmlDoc doc) throws Exception {
        super(doc, CapabilitiesType.Atom);
        setup_XmlCapabilitiesAtom();
        setup_entries();
    }

    public AtomEntry findEntry(String id) {
        if ( (entries ==null) || (entries.isEmpty()))
            return null;

        for (AtomEntry entry: entries) {
            if ((entry.getId() !=null ) && (entry.getId().equals(id)))
                return entry;
        }

        return null;
    }

    public static String attribute(Node n, String attribute) {
        if (n==null)
            return null;
        Node att = n.getAttributes().getNamedItem(attribute);
        if (att == null)
            return null;
        String val = att.getNodeValue();
        if (val == null)
            return null;
        val = val.trim();
        if (val.isEmpty())
            return null;
        return val;
    }

    public void setup_entries() throws Exception {
        Node n = getFirstNode();
        //NodeList ns = xpath_nodeset("//atom:entry");
        List<Node> ns = XmlDoc.findAllNodes(n,"entry");
        for(int idx=0;idx<ns.size();idx++) {
            Node entryNode = ns.get(idx);
            Node idNode = findNode(entryNode,"id");
            String id = null;
            if ( (idNode!=null) && (idNode.getTextContent() !=null) && (!idNode.getTextContent().trim().isEmpty()) )
                id = idNode.getTextContent().trim();
            if ( (id ==null)||(id.isEmpty()))
                continue; // Atom spec says ID is required.  Also, we need it to identify which <entry> we are talking about

            List<AtomLink> atomLinks = new ArrayList<>();
            List<Node> links = XmlDoc.findAllNodes(entryNode,"link");
            for(int idx2=0;idx2<links.size();idx2++) {
                Node linkNode = links.get(idx2);
                AtomLink link = new AtomLink(
                        attribute(linkNode,"href"),
                        attribute(linkNode,"rel"),
                        attribute(linkNode,"type"),
                        attribute(linkNode,"hreflang"),
                        attribute(linkNode,"title")
                    );
                atomLinks.add(link);
            }
            AtomEntry entry = new AtomEntry(id,atomLinks);
            entries.add(entry);
        }
    }


    public Node findDescribedByLink(Node entryNode) {
        List<Node> nodes = findNodes(entryNode,"link");
        for (Node n : nodes) {
             Node rel = n.getAttributes().getNamedItem("rel");
             if ( (rel != null) && (rel.getNodeValue() !=null) && (!rel.getNodeValue().trim().isEmpty())) {
                 if (rel.getNodeValue().trim().equals("describedby"))
                     return n;
             }
        }
        return null;
    }

    private void setup_XmlCapabilitiesAtom() throws  Exception {
        Node n = getFirstNode();
        //NodeList ns = xpath_nodeset("//atom:entry");
        List<Node> ns = XmlDoc.findAllNodes(n,"entry");
        for(int idx=0;idx<ns.size();idx++) {
            String identity = null;
            String authority = null;
            String url = null;
            Node entryNode = ns.get(idx);
           // Node spatial_dataset_identifier_codeNode = xpath_node(entryNode,"inspire_dls:spatial_dataset_identifier_code");
            Node spatial_dataset_identifier_codeNode = findNode(entryNode,"spatial_dataset_identifier_code");
            Node spatial_dataset_identifier_namesapceNode = findNode(entryNode,"spatial_dataset_identifier_namespace");

            // Node urlNode = xpath_node(entryNode,"atom:link[@rel='describedby']");
            Node urlNode =findDescribedByLink(entryNode);
            if (findNodes(entryNode,"spatial_dataset_identifier_code").size()>1)
            {
                int t=0;
            }

            Node idNode = findNode(entryNode,"id");
            String id = null;
            if ( (idNode!=null) && (idNode.getTextContent() !=null) && (!idNode.getTextContent().trim().isEmpty()) )
                id = idNode.getTextContent().trim();

            if (spatial_dataset_identifier_codeNode != null) {
                identity = spatial_dataset_identifier_codeNode.getTextContent();
                if (identity.isEmpty())
                    identity = null;
            }
            if (spatial_dataset_identifier_namesapceNode != null) {
                authority = spatial_dataset_identifier_namesapceNode.getTextContent();
                if (authority.isEmpty())
                    authority = null;
            }
            if (urlNode != null) {
                Node hrefNode = urlNode.getAttributes().getNamedItem("href");
                if (hrefNode != null) {
                    url = hrefNode.getNodeValue();
                    if (url.isEmpty())
                        url = null;
                }
            }
            if ( (url !=null) || (identity !=null)) {
                DatasetLink dl = new DatasetLink(identity,url);
                dl.setAuthority(authority);
                dl.setOgcLayerName(id);
                this.getDatasetLinksList().add(dl);
            }

        }
    }

    //---

    public List<AtomEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<AtomEntry> entries) {
        this.entries = entries;
    }

//    Extract the dataset authority:identifier from service feed
///feed/entry/nspire_dls:spatial_dataset_identifier_code
///feed/entry/inspire_dls:spatial_dataset_identifier_namespace

    @Override
    public String toString() {
        String result =  "XmlCapabilitiesAtom(has service reference URL="+( (getMetadataUrlRaw() !=null) && (!getMetadataUrlRaw().isEmpty())) ;
        result += ", number of Dataset links = "+getDatasetLinksList().size();
        result += ")";
        return result;
    }

}