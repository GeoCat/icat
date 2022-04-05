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
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

//simple base class that adds to entities;
// a. create/update time
// b. a place to put error messages
@MappedSuperclass
public class UpdateCreateDateTimeEntity {

    @Column(columnDefinition = "timestamp with time zone")
    ZonedDateTime createTimeUTC;
    @Column(columnDefinition = "timestamp with time zone")
    ZonedDateTime lastUpdateUTC;

    @Column(columnDefinition = "text")
    String errorMessage;

    //--------------------------------------------------------------


    protected void onInsert() {
        this.createTimeUTC = ZonedDateTime.now(ZoneId.of("UTC"));
        this.lastUpdateUTC = this.createTimeUTC;
    }


    protected void onUpdate() {
        this.lastUpdateUTC = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    //--------------------------------------------------------------

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public ZonedDateTime getCreateTimeUTC() {
        return createTimeUTC;
    }

    public void setCreateTimeUTC(ZonedDateTime createTimeUTC) {
        this.createTimeUTC = createTimeUTC;
    }

    public ZonedDateTime getLastUpdateUTC() {
        return lastUpdateUTC;
    }

    public void setLastUpdateUTC(ZonedDateTime lastUpdateUTC) {
        this.lastUpdateUTC = lastUpdateUTC;
    }


    @Override
    public String toString() {
        String result = "";
        if (createTimeUTC !=null)
            result += "     createTimeUTC="+createTimeUTC+"\n";
        if (lastUpdateUTC !=null)
            result += "     lastUpdateUTC="+lastUpdateUTC+"\n";
        if (errorMessage!=null)
            result += "     ERROR MESSAGE:"+errorMessage+"\n";
        return result;
    }
}
