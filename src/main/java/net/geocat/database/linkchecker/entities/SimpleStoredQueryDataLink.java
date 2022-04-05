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

import net.geocat.database.linkchecker.entities.helper.DatasetMetadataRecord;
import net.geocat.database.linkchecker.entities.helper.LinkToData;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
@DiscriminatorValue("SimpleStoredQueryDataLink")
public class SimpleStoredQueryDataLink extends LinkToData {

    @Column(columnDefinition = "text")
    private String storedProcName;

    @Column(columnDefinition = "text")
    private String code;

    @Column(columnDefinition = "text")
    private String codeSpace;

    @OneToOne(cascade = CascadeType.ALL)
    OGCRequest ogcRequest; // might be null


    public SimpleStoredQueryDataLink() {
        super();
    }

    public SimpleStoredQueryDataLink(String linkcheckjobid, String sha2, String capabilitiesdocumenttype, DatasetMetadataRecord datasetMetadataRecord) {
        super(linkcheckjobid,sha2,capabilitiesdocumenttype,datasetMetadataRecord);
    }

    //----


    public OGCRequest getOgcRequest() {
        return ogcRequest;
    }

    public void setOgcRequest(OGCRequest ogcRequest) {
        this.ogcRequest = ogcRequest;
    }

    public String getStoredProcName() {
        return storedProcName;
    }

    public void setStoredProcName(String storedProcName) {
        this.storedProcName = storedProcName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCodeSpace() {
        return codeSpace;
    }

    public void setCodeSpace(String codeSpace) {
        this.codeSpace = codeSpace;
    }

    //--

    @Override
    public String toString() {
        return "SimpleStoredQueryDataLink{" + "\n"+
                "     storedProcName: " + storedProcName + "\n" +
                "     code: " + code + "\n" +
                "     codeSpace: " + codeSpace + "\n" +
                super.toString() + "\n" +
                '}';
    }

    @Override
    public String key() {
        return super.key() +"::storedquery::"+storedProcName;
    }
}
