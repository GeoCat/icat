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
@Table(name = "logging_event"
        ,
        indexes = {
                @Index(
                        name = "logging_correlationid_idx",
                        columnList = "jms_correlation_id",
                        unique = false
                ),
                @Index(
                        name = "logging_event_timestmp_idx",
                        columnList = "timestmp",
                        unique = false
                ),
                @Index(
                        name = "log_jms_refflag_idx",
                        columnList = "jms_correlation_id,reference_flag",
                        unique = false
                )
        }
)
public class LogbackLoggingEvent {

    @Column(columnDefinition = "bigint")
    public long timestmp;

    @Column(name = "formatted_message", columnDefinition = "text")
    public String formattedMessage;

    @Column(name = "logger_name", columnDefinition = "varchar(254)")
    public String loggerName;

    @Column(name = "level_string", columnDefinition = "varchar(254)")
    public String levelString;

    @Column(name = "thread_name", columnDefinition = "varchar(254)")
    public String threadName;

    @Column(name = "reference_flag", columnDefinition = "smallint")
    public short referenceFlag;

    @Column(columnDefinition = "varchar(254)")
    public String arg0;
    @Column(columnDefinition = "varchar(254)")
    public String arg1;
    @Column(columnDefinition = "varchar(254)")
    public String arg2;
    @Column(columnDefinition = "varchar(254)")
    public String arg3;

    @Column(name = "caller_filename", columnDefinition = "varchar(254)")
    public String callerFilename;

    @Column(name = "caller_class", columnDefinition = "varchar(254)")
    public String callerClass;

    @Column(name = "caller_method", columnDefinition = "varchar(254)")
    public String callerMethod;

    @Column(name = "caller_line", columnDefinition = "char(4)")
    public String callerLine;

    @Column(name = "jms_correlation_id", columnDefinition = "varchar(254)")
    public String jmsCorrelationId;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id", columnDefinition = "BIGINT")
    public long eventId;


}
