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

package net.geocat.dblogging.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;
import org.slf4j.helpers.BasicMarker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static ch.qos.logback.core.db.DBHelper.closeStatement;

public class CustomDBAppender extends ch.qos.logback.classic.db.DBAppender {


    String SQL = "update logging_event  set jms_correlation_id=? where event_id = ?";
    String SQL_ERROR = "INSERT INTO logging_event_exception (event_id, i, caused_by_depth, trace_line) VALUES (?, ?, ?, ?)";

    Map<String, String> mergePropertyMaps(ILoggingEvent event) {
        Map<String, String> mergedMap = new HashMap<String, String>();
        // we add the context properties first, then the event properties, since
        // we consider that event-specific properties should have priority over
        // context-wide properties.
        Map<String, String> loggerContextMap = event.getLoggerContextVO().getPropertyMap();
        Map<String, String> mdcMap = event.getMDCPropertyMap();
        if (loggerContextMap != null) {
            mergedMap.putAll(loggerContextMap);
        }
        if (mdcMap != null) {
            mergedMap.putAll(mdcMap);
        }

        return mergedMap;
    }

    @Override
    protected void secondarySubAppend(ILoggingEvent event, Connection connection, long eventId) throws Throwable {
        Map<String, String> mergedMap = mergePropertyMaps(event);
        if ( (event.getMarker() != null) && (event.getMarker() instanceof BasicMarker) ){
            String name = ((BasicMarker) event.getMarker()).getName();
            mergedMap.put("markerValue",name);
        }
        insertProperties(mergedMap, connection, eventId);

        if (event.getThrowableProxy() != null) {
            insertThrowable(event.getThrowableProxy(), connection, eventId);
        }
    }
    @Override
    protected void insertProperties(Map<String, String> mergedMap, Connection connection, long eventId)
            throws SQLException {
        if (!mergedMap.containsKey("JMSCorrelationID") && !mergedMap.containsKey("markerValue"))
            return; // nothing to do

        String value = mergedMap.get("JMSCorrelationID");
        if ( (value == null) || (value.isEmpty()) ){
            value = mergedMap.get("markerValue");
        }
        if ((value == null) || (value.isEmpty()))
            return; //nothing to do

        PreparedStatement pstmt = connection.prepareStatement(SQL);
        pstmt.setString(1, value);
        pstmt.setLong(2, eventId);

        int affectedRows = pstmt.executeUpdate();
    }


    // throwable;
    // original throwable will have causedByIndex = 0
    // message is always i=0
    protected void insertThrowable(IThrowableProxy tp, Connection connection, long eventId) throws SQLException {

        PreparedStatement exceptionStatement = null;
        try {
            exceptionStatement = connection.prepareStatement(SQL_ERROR);

            short causedByIndex = 0;
            while (tp != null) {
                buildExceptionStatement(tp, exceptionStatement, eventId, causedByIndex);
                causedByIndex++;
                tp = tp.getCause();
            }

            if (cnxSupportsBatchUpdates) {
                exceptionStatement.executeBatch();
            }
        } finally {
            closeStatement(exceptionStatement);
        }

    }


    void buildExceptionStatement(IThrowableProxy tp,
                                 PreparedStatement insertExceptionStatement,
                                 long eventId,
                                 short causedByIndex) throws SQLException {

        short index=0;
        StringBuilder buf = new StringBuilder();
        ThrowableProxyUtil.subjoinFirstLine(buf, tp);
        updateExceptionStatement(insertExceptionStatement, buf.toString(),causedByIndex, index++, eventId);

        int commonFrames = tp.getCommonFrames();
        StackTraceElementProxy[] stepArray = tp.getStackTraceElementProxyArray();
        for (int i = 0; i < stepArray.length - commonFrames; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(CoreConstants.TAB);
            ThrowableProxyUtil.subjoinSTEP(sb, stepArray[i]);
            updateExceptionStatement(insertExceptionStatement, sb.toString(),causedByIndex, index++, eventId);
        }

        if (commonFrames > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(CoreConstants.TAB).append("... ").append(commonFrames).append(" common frames omitted");
            updateExceptionStatement(insertExceptionStatement, sb.toString(),causedByIndex, index++, eventId);
        }


    }


    /**
     * Add an exception statement either as a batch or execute immediately if
     * batch updates are not supported.
     */
    void updateExceptionStatement(PreparedStatement exceptionStatement, String txt, short causedByIndex, short i, long eventId) throws SQLException {
        exceptionStatement.setLong(1, eventId);
        exceptionStatement.setShort(2, i);
        exceptionStatement.setShort(3, causedByIndex);
        exceptionStatement.setString(4, txt);

        if (cnxSupportsBatchUpdates) {
            exceptionStatement.addBatch();
        } else {
            exceptionStatement.execute();
        }
    }


}
