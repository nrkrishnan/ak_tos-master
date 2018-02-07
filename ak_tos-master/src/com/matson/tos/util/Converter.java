package com.matson.tos.util;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class Converter extends org.xml.sax.helpers.XMLFilterImpl {
    private String convert(String pNamespaceURI) throws SAXException {
        if (pNamespaceURI == null  ||  "".equals(pNamespaceURI)) {
            return "http://www.navis.com/argo";
        } else {
            return pNamespaceURI;
        }
    }
    public void startElement(String pNamespaceURI, String pLocalName, String pQName,
                             Attributes pAttrs) throws SAXException {
        super.startElement(convert(pNamespaceURI), pLocalName, pQName, pAttrs);
    }
    public void endElement(String pNamespaceURI, String pLocalName, String pQName) throws SAXException {
        super.endElement(convert(pNamespaceURI), pLocalName, pQName);
    }
    public void startPrefixMapping(String pPrefix, String pNamespaceURI) throws SAXException {
        super.startPrefixMapping(pPrefix, convert(pNamespaceURI));
    }
}

