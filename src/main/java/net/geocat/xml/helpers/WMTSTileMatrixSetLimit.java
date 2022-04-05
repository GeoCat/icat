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

package net.geocat.xml.helpers;

public class WMTSTileMatrixSetLimit {
    String tileMatrixName;

    int minTileRow;
    int maxTileRow;
    int minTileCol;
    int maxTileCol;

    public WMTSTileMatrixSetLimit(String tileMatrixName, int minTileRow, int maxTileRow, int minTileCol, int maxTileCol) {
        this.tileMatrixName = tileMatrixName;
        this.minTileRow = minTileRow;
        this.maxTileRow = maxTileRow;
        this.minTileCol = minTileCol;
        this.maxTileCol = maxTileCol;
    }

    public String getTileMatrixName() {
        return tileMatrixName;
    }

    public void setTileMatrixName(String tileMatrixName) {
        this.tileMatrixName = tileMatrixName;
    }

    public int getMinTileRow() {
        return minTileRow;
    }

    public void setMinTileRow(int minTileRow) {
        this.minTileRow = minTileRow;
    }

    public int getMaxTileRow() {
        return maxTileRow;
    }

    public void setMaxTileRow(int maxTileRow) {
        this.maxTileRow = maxTileRow;
    }

    public int getMinTileCol() {
        return minTileCol;
    }

    public void setMinTileCol(int minTileCol) {
        this.minTileCol = minTileCol;
    }

    public int getMaxTileCol() {
        return maxTileCol;
    }

    public void setMaxTileCol(int maxTileCol) {
        this.maxTileCol = maxTileCol;
    }


    @Override
    public String toString() {
        return "WMTSTileMatrixSetLimit{" +
                "tileMatrixName='" + tileMatrixName + '\'' +
                ", minTileRow=" + minTileRow +
                ", maxTileRow=" + maxTileRow +
                ", minTileCol=" + minTileCol +
                ", maxTileCol=" + maxTileCol +
                '}';
    }
}
