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


import javax.persistence.*;
//taken from;
// https://raw.githubusercontent.com/qos-ch/logback/master/logback-classic/src/main/resources/ch/qos/logback/classic/db/script/postgresql.sql

@Entity
@Table(name = "logging_event_exception"
        ,
        indexes= {
                @Index(
                        name="log_ex_eventid",
                        columnList="event_id",
                        unique=false
                )
        }
)
@IdClass(LogbackLoggingEventExceptionCompositeKey.class)
public class LogbackLoggingEventException {

    @Column(name = "event_id", columnDefinition = "bigint")
    @Id
    private long eventId;

    @Column(name="i", columnDefinition = "smallint")
    @Id
    private short i;

    @Column(name = "trace_line", columnDefinition = "text")
    private String traceLine;

    @Column(name = "caused_by_depth",columnDefinition = "smallint")
    private short causedByDepth;



    //----------------------


    public short getCausedByDepth() {
        return causedByDepth;
    }

    public void setCausedByDepth(short causedByDepth) {
        this.causedByDepth = causedByDepth;
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public short getI() {
        return i;
    }

    public void setI(short i) {
        this.i = i;
    }

    public String getTraceLine() {
        return traceLine;
    }

    public void setTraceLine(String traceLine) {
        this.traceLine = traceLine;
    }
}
