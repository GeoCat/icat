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

package net.geocat.database.linkchecker.entities.helper;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import static net.geocat.database.linkchecker.entities.helper.PartialDownloadHint.ALWAYS_PARTIAL;
import static net.geocat.database.linkchecker.entities.helper.PartialDownloadHint.CAPABILITIES_ONLY;

@Entity
public class AtomSubFeedRequest extends RetrievableSimpleLink  {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long atomSubFeedRequestId;

//    @OneToOne(mappedBy = "atomSubFeedRequest")
//    LinkToData linkToData;

    Boolean successfulAtomRequest;

    @Column(columnDefinition = "text")
    String unSuccessfulAtomRequestReason;


    public AtomSubFeedRequest() {
        super();
        setPartialDownloadHint(CAPABILITIES_ONLY);
    }

    public AtomSubFeedRequest(String url ) {
        this();
        setRawURL(url);
        setFixedURL(url);
     }

     //--

    public long getAtomSubFeedRequestId() {
        return atomSubFeedRequestId;
    }

    public void setAtomSubFeedRequestId(long atomSubFeedRequestId) {
        this.atomSubFeedRequestId = atomSubFeedRequestId;
    }

    public Boolean getSuccessfulAtomRequest() {
        return successfulAtomRequest;
    }

    public void setSuccessfulAtomRequest(Boolean successfulAtomRequest) {
        this.successfulAtomRequest = successfulAtomRequest;
    }

    public String getUnSuccessfulAtomRequestReason() {
        return unSuccessfulAtomRequestReason;
    }

    public void setUnSuccessfulAtomRequestReason(String unSuccessfulAtomRequestReason) {
        this.unSuccessfulAtomRequestReason = unSuccessfulAtomRequestReason;
    }
}
