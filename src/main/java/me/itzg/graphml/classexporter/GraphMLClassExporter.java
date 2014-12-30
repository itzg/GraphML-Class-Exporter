package me.itzg.graphml.classexporter;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Iterator;

/**
 * @author Geoff
 * @since 12/30/2014
 */
public class GraphMLClassExporter {
    public static final String GRAPHML_NS = "http://graphml.graphdrawing.org/xmlns";
    public static final String YWORKS_GRAPHML_NS = "http://www.yworks.com/xml/graphml";

    private final Options options;
    private final Configuration freemarkerConfig;

    public GraphMLClassExporter(Options options) {

        this.options = options;

        freemarkerConfig = new Configuration();
        freemarkerConfig.setTemplateLoader(new MyTemplateLoader());
    }

    public static void main(String[] args) throws Exception {

        Options options = new Options();
        CmdLineParser cmdLineParser = new CmdLineParser(options);
        try {
            cmdLineParser.parseArgument(args);

            if (options.isHelpRequested()) {
                cmdLineParser.printUsage(System.out);
                System.exit(1);
            }
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            cmdLineParser.printUsage(System.err);
            System.exit(2);
        }

        GraphMLClassExporter exporter = new GraphMLClassExporter(options);
        exporter.start();
    }

    public void start() throws IOException, TransformerException {
        Template template = freemarkerConfig.getTemplate(options.getTemplateName());

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            Transformer transformer = transformerFactory.newTransformer();
            try (FileInputStream fin = new FileInputStream(options.getGraphMLFile())) {
                DOMResult domResult = new DOMResult();
                transformer.transform(new StreamSource(fin), domResult);

                processDomViaTemplate(domResult.getNode(), template);
            }
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void processDomViaTemplate(Node outerGraph, Template template) {

        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        xPath.setNamespaceContext(new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                switch (prefix) {
                    case "g":
                        return GRAPHML_NS;
                    case "y":
                        return YWORKS_GRAPHML_NS;
                    default:
                        return null;
                }
            }

            @Override
            public String getPrefix(String namespaceURI) {
                switch (namespaceURI) {
                    case GRAPHML_NS:
                        return "g";
                    default:
                        return null;
                }
            }

            @Override
            public Iterator getPrefixes(String namespaceURI) {
                return null;
            }
        });

        try {
            NodeList result = (NodeList) xPath.evaluate("/g:graphml/g:graph/g:node", outerGraph, XPathConstants.NODESET);
            for (int i = 0; i < result.getLength(); i++) {
                Node graphNode = result.item(i);

                String entityName = xPath.evaluate("g:data/y:GenericNode/y:NodeLabel[@configuration='com.yworks.entityRelationship.label.name']/text()", graphNode);
                System.out.println("Found "+entityName);
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }

    private static class OurTemplateSource {
        private Object source;
        private Reader reader;

        public OurTemplateSource(Object source) {
            this.source = source;
        }

        public Reader getReader() {
            return reader;
        }

        public void setReader(Reader reader) {
            this.reader = reader;
        }

        public Object getSource() {
            return source;
        }
    }

    private class MyTemplateLoader implements TemplateLoader {
        @Override
        public Object findTemplateSource(String name) throws IOException {
            URL templateUrl = this.getClass().getClassLoader().getResource("templates/" + name + ".ftl");
            if (templateUrl == null) {
                File fileSource = new File(name);
                return fileSource.exists() ? new OurTemplateSource(fileSource) : null;
            }
            else {
                return new OurTemplateSource(templateUrl);
            }
        }

        @Override
        public long getLastModified(Object templateSource) {
            OurTemplateSource ourTemplateSource = (OurTemplateSource) templateSource;
            Object rawSource = ourTemplateSource.getSource();
            return rawSource instanceof File ? ((File) rawSource).lastModified() : 0;
        }

        @Override
        public Reader getReader(Object templateSource, String encoding) throws IOException {
            OurTemplateSource ourTemplateSource = (OurTemplateSource) templateSource;
            if (ourTemplateSource.getReader() == null) {
                Object rawSource = ourTemplateSource.getSource();

                if (rawSource instanceof File) {
                    ourTemplateSource.setReader(new FileReader((File) rawSource));
                }
                else {
                    URL rawSourceUrl = (URL) rawSource;
                    ourTemplateSource.setReader(new InputStreamReader(rawSourceUrl.openStream()));
                }
            }

            return ourTemplateSource.getReader();
        }

        @Override
        public void closeTemplateSource(Object templateSource) throws IOException {
            OurTemplateSource ourTemplateSource = (OurTemplateSource) templateSource;
            ourTemplateSource.getReader().close();
            ourTemplateSource.setReader(null);
        }
    }
}
