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

package net.geocat.database.linkchecker.entities;

import net.geocat.database.linkchecker.entities.helper.AtomSubFeedRequest;
import net.geocat.database.linkchecker.entities.helper.DatasetMetadataRecord;
import net.geocat.database.linkchecker.entities.helper.LinkToData;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.List;

@Entity
@DiscriminatorValue("SimpleAtomLinkToData")
public class SimpleAtomLinkToData extends LinkToData {

    @Column(columnDefinition = "text")
    String layerId;

    @Column(columnDefinition = "text")
    String context;

    @OneToOne(cascade = CascadeType.ALL)
   // @OnDelete(action = OnDeleteAction.CASCADE)
    AtomSubFeedRequest atomSubFeedRequest;

    @OneToMany(
            cascade = {CascadeType.ALL}, fetch = FetchType.EAGER, mappedBy = "simpleAtomLinkToData")
    @Fetch(value = FetchMode.SUBSELECT)
  //  @OnDelete(action = OnDeleteAction.CASCADE)
    List<AtomActualDataEntry> atomActualDataEntryList;

    public SimpleAtomLinkToData() {
        super();
    }

    public SimpleAtomLinkToData(String linkcheckjobid, String sha2, String capabilitiesdocumenttype, DatasetMetadataRecord datasetMetadataRecord, String layerName) {
        super(linkcheckjobid,sha2,capabilitiesdocumenttype,datasetMetadataRecord);
        this.layerId = layerName;
    }

    //---


    public List<AtomActualDataEntry> getAtomActualDataEntryList() {
        return atomActualDataEntryList;
    }

    public void setAtomActualDataEntryList(List<AtomActualDataEntry> atomActualDataEntryList) {
        this.atomActualDataEntryList = atomActualDataEntryList;
    }

    public String getLayerId() {
        return layerId;
    }

    public void setLayerId(String layerId) {
        this.layerId = layerId;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public AtomSubFeedRequest getAtomSubFeedRequest() {
        return atomSubFeedRequest;
    }

    public void setAtomSubFeedRequest(AtomSubFeedRequest atomSubFeedRequest) {
        this.atomSubFeedRequest = atomSubFeedRequest;
    }


    //---

    @Override
    public String key() {
        return super.key() +"::"+layerId;
    }
}
