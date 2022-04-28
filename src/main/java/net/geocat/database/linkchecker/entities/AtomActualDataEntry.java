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

import net.geocat.database.linkchecker.entities.helper.AtomDataRequest;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class AtomActualDataEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long atomActualDataEntryId;

    Integer index;

    @ManyToOne(fetch = FetchType.EAGER,cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    SimpleAtomLinkToData simpleAtomLinkToData;

    @Column(columnDefinition = "text")
    String entryId;

    Boolean successfullyDownloaded;

    @OneToMany(
            cascade = {CascadeType.ALL}, fetch = FetchType.EAGER, mappedBy = "atomActualDataEntry")
    @Fetch(value = FetchMode.SUBSELECT)
    @OnDelete(action = OnDeleteAction.CASCADE)
  //  @JoinColumn(name = "atomDataRequestId")
    List<AtomDataRequest> atomDataRequestList;

    //--

    public AtomActualDataEntry() {

    }

    //--


    public Boolean getSuccessfullyDownloaded() {
        return successfullyDownloaded;
    }

    public void setSuccessfullyDownloaded(Boolean successfullyDownloaded) {
        this.successfullyDownloaded = successfullyDownloaded;
    }

    public int getIndex() {
        return (index == null)? 0 : index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public SimpleAtomLinkToData getSimpleAtomLinkToData() {
        return simpleAtomLinkToData;
    }

    public void setSimpleAtomLinkToData(SimpleAtomLinkToData simpleAtomLinkToData) {
        this.simpleAtomLinkToData = simpleAtomLinkToData;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public List<AtomDataRequest> getAtomDataRequestList() {
        return atomDataRequestList;
    }

    public void setAtomDataRequestList(List<AtomDataRequest> atomDataRequestList) {
        this.atomDataRequestList = atomDataRequestList;
    }

    public long getAtomActualDataEntryId() {
        return atomActualDataEntryId;
    }

    public void setAtomActualDataEntryId(long atomActualDataEntryId) {
        this.atomActualDataEntryId = atomActualDataEntryId;
    }

    //--
}
