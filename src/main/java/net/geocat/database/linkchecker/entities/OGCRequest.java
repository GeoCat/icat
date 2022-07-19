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

import net.geocat.database.linkchecker.entities.helper.HTTPRequestCheckerType;
import net.geocat.database.linkchecker.entities.helper.LinkToData;
import net.geocat.database.linkchecker.entities.helper.OGCLinkToData;
import net.geocat.database.linkchecker.entities.helper.RetrievableSimpleLink;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import static net.geocat.database.linkchecker.entities.helper.PartialDownloadHint.ALWAYS_PARTIAL;

@Entity
@Table(
        indexes = {
                @Index(
                        name = "OGCRequest_linkcheckjobid_idx",
                        columnList = "linkcheckjobid",
                        unique = false
                )})
public class OGCRequest extends RetrievableSimpleLink  {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long ogcRequestId;

    Boolean successfulOGCRequest;

//    @OneToOne(mappedBy = "ogcRequest")
//    OGCLinkToData linkToData;


    @Column(columnDefinition = "text")
    String summary;

    @Enumerated(EnumType.STRING)
    HTTPRequestCheckerType httpRequestCheckerType;

    @Column(columnDefinition = "text")
    String  unSuccessfulOGCRequestReason;

    public OGCRequest() {
        super();
        setPartialDownloadHint(ALWAYS_PARTIAL);
    }

    public OGCRequest(String url,HTTPRequestCheckerType httpRequestCheckerType) {
        this();
        setRawURL(url);
        setFixedURL(url);
        setHttpRequestCheckerType(httpRequestCheckerType);
    }

    public Boolean isSuccessfulOGCRequest() {
        return successfulOGCRequest;
    }

    public void setSuccessfulOGCRequest(Boolean successfulOGCRequest) {
        this.successfulOGCRequest = successfulOGCRequest;
    }

    public String getUnSuccessfulOGCRequestReason() {
        return unSuccessfulOGCRequestReason;
    }

    public void setUnSuccessfulOGCRequestReason(String unSuccessfulOGCRequestReason) {
        this.unSuccessfulOGCRequestReason = unSuccessfulOGCRequestReason;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public HTTPRequestCheckerType getHttpRequestCheckerType() {
        return httpRequestCheckerType;
    }

    public void setHttpRequestCheckerType(HTTPRequestCheckerType httpRequestCheckerType) {
        this.httpRequestCheckerType = httpRequestCheckerType;
    }

    public long getOgcRequestId() {
        return ogcRequestId;
    }

    public void setOgcRequestId(long ogcRequestId) {
        this.ogcRequestId = ogcRequestId;
    }
//
//    public OGCLinkToData getLinkToData() {
//        return linkToData;
//    }
//
//    public void setLinkToData(OGCLinkToData linkToData) {
//        this.linkToData = linkToData;
//    }

    @Override
    public String toString() {
        return "OGCRequest{\n      summary:" + summary +"\n " +
                "    successfulOGCRequest=" + successfulOGCRequest +
                "\n      unSuccessfulOGCRequestReason='" + unSuccessfulOGCRequestReason + '\'' + '\n'
                + super.toString() +
                "\n}";
    }
}
