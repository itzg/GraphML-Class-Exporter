package me.itzg.graphml.classexporter;

import org.w3c.dom.Document;

import javax.xml.namespace.NamespaceContext;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
* @author Geoff Bourne
* @since 12/30/2014
*/
class GraphMLNamespaceContext implements NamespaceContext {
    public static final String GRAPHML_NS = "http://graphml.graphdrawing.org/xmlns";
    public static final String YWORKS_GRAPHML_NS = "http://www.yworks.com/xml/graphml";
    private static final List<String> knownPrefixes = Arrays.asList("g", "y");
    private final Document outerGraphDoc;

    public GraphMLNamespaceContext(Document outerGraphDoc) {
        this.outerGraphDoc = outerGraphDoc;
    }

    @Override
    public String getNamespaceURI(String prefix) {
        switch (prefix) {
            case "g":
                return GRAPHML_NS;
            case "y":
                return YWORKS_GRAPHML_NS;
            default:
                return outerGraphDoc.lookupNamespaceURI(prefix);
        }
    }

    @Override
    public String getPrefix(String namespaceURI) {
        switch (namespaceURI) {
            case GRAPHML_NS:
                return "g";
            case YWORKS_GRAPHML_NS:
                return "y";
            default:
                return outerGraphDoc.lookupPrefix(namespaceURI);
        }
    }

    @Override
    public Iterator getPrefixes(String namespaceURI) {
        return knownPrefixes.iterator();
    }
}
