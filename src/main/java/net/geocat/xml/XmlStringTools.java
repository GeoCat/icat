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

package net.geocat.xml;

import net.geocat.xml.helpers.XmlTagInfo;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Scope("singleton")
public class XmlStringTools {

    public final static String UTF8_BOM = "\uFEFF";
    private final static Charset UTF8_CHARSET = Charset.forName("UTF-8");

    static Pattern tagWithNS = Pattern.compile("^<([^ :<>]+):([^ >]+)[^>]+>");
    static Pattern tagWithoutNS = Pattern.compile("^<([^ >]+)[^>]+>");


    public static String getNodeTextValue(org.w3c.dom.Node n) {
        if ( (n == null) || (n.getTextContent() == null) || (n.getTextContent().trim().isEmpty()) )
            return null;
        return n.getTextContent().trim();
    }

    public static XmlTagInfo determineRootTagInfo(String doc){
        if (!isXML(doc))
            return null; //not XML

        doc = replaceXMLDecl(doc).trim();
        doc = getRootTag(doc).trim();

        String prefix = getPrefix(doc);
        String tag = getTagName(doc);
        String ns = getNS(prefix, doc);

        return new XmlTagInfo(tag,prefix,ns);
    }

    public static String replaceXMLDecl(String doc) {
        doc = doc.replaceFirst("<\\?xml[^\\?>]+\\?>", "");
        doc = doc.replaceFirst("<\\?xml[^\\?>]+\\?>", "");
        return doc.trim();
    }


    public static String getRootTag(String doc) {
        Matcher matcher = tagWithNS.matcher(doc);
        boolean find = matcher.find();
        if (find)
            return matcher.group(0);
        matcher = tagWithoutNS.matcher(doc);
        find = matcher.find();
        if (find)
            return matcher.group(0);
        return null;
    }

    public static String getPrefix(String doc) {
        Matcher matcher = tagWithNS.matcher(doc);
        boolean find = matcher.find();
        if (!find)
            return null;
        return matcher.group(1).trim();
    }

    public static String getTagName(String doc) {
        Matcher matcher = tagWithNS.matcher(doc);
        boolean find = matcher.find();
        if (find)
            return matcher.group(2).trim();
        matcher = tagWithoutNS.matcher(doc);
        find = matcher.find();
        if (find)
            return matcher.group(1).trim();
        return null;
    }

    public static String getNS(String prefix, String tag) {
        String pattern = "xmlns=[\"']([^\"']+)[\"']";
        if (prefix != null)
            pattern = "xmlns:" + prefix + "=[\"']([^\"']+)[\"']";
        Pattern ns = Pattern.compile(pattern, Pattern.MULTILINE);
        Matcher matcher = ns.matcher(tag);
        boolean find = matcher.find();
        if (find)
            return matcher.group(1);
        return null;
    }

    public static String removeComment(String doc) {
        return doc.replaceAll("<!--[\\s\\S]*?-->","").trim();
    }



    public static String trim(String s){
        String result = s.trim();

        if (result.startsWith(UTF8_BOM)) {
            result = result.substring(1).trim();
        }

        return result;
    }

    public static String bytea2String(byte[] bytes) {
        if (bytes == null)
            return "";
        return trim(new String(bytes, UTF8_CHARSET));
    }

    public static boolean isXML(String doc) {
        try {
            if (!doc.startsWith("<?xml")) {
                // sometimes it doesn't start with the xml declaration
                doc =  trim(doc);
                if (!doc.startsWith("<"))
                    return false; //not xml
                if (doc.length() < 4)
                    return false;
                //flaky, is second char a letter?
                return Character.isLetter(doc.charAt(1));
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isXML(byte[] data) {
        return isXML(bytea2String(data));
    }
}
