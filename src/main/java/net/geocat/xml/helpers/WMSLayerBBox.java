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

public class WMSLayerBBox {

    String CRS;
    double xmin;
    double ymin;
    double xmax;
    double ymax;

    public WMSLayerBBox(String CRS, double xmin, double ymin, double xmax, double ymax) {
        this.CRS = CRS;
        this.xmin = xmin;
        this.ymin = ymin;
        this.xmax = xmax;
        this.ymax = ymax;
    }

    public WMSLayerBBox copy() {
        return new WMSLayerBBox(CRS,xmin,ymin,xmax,ymax);
    }

    public WMSLayerBBox makeSmaller(double ratio) throws Exception {
        if (ratio >1)
            throw new Exception("ration <=1");
        if (ratio <=0)
            throw new Exception("ration >0");

        double width = xmax-xmin;
        double height = ymax-ymin;
        double center_x = (xmax-xmin)/2.0;
        double center_y = (ymax-ymin)/2.0;

        double _xmin = center_x - (width/2.0) * ratio;
        double _ymin = center_y - (height/2.0) * ratio;

        double _xmax = center_x + (width/2.0) * ratio;
        double _ymax = center_y + (height/2.0) * ratio;

        return new WMSLayerBBox(CRS,_xmin,_ymin,_xmax,_ymax);
    }

    public String getCRS() {
        return CRS;
    }

    public void setCRS(String CRS) {
        this.CRS = CRS;
    }

    public double getXmin() {
        return xmin;
    }

    public void setXmin(double xmin) {
        this.xmin = xmin;
    }

    public double getYmin() {
        return ymin;
    }

    public void setYmin(double ymin) {
        this.ymin = ymin;
    }

    public double getXmax() {
        return xmax;
    }

    public void setXmax(double xmax) {
        this.xmax = xmax;
    }

    public double getYmax() {
        return ymax;
    }

    public void setYmax(double ymax) {
        this.ymax = ymax;
    }


    @Override
    public String toString() {
        return "WMSLayerBBox{" +
                "CRS='" + CRS + '\'' +
                ", xmin='" + xmin + '\'' +
                ", ymin='" + ymin + '\'' +
                ", xmax='" + xmax + '\'' +
                ", ymax='" + ymax + '\'' +
                '}';
    }

    public String asBBOX() {
        return xmin+","+ymin+","+xmax+","+ymax;
    }
}
