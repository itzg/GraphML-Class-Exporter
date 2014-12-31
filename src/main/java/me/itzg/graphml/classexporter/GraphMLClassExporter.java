package me.itzg.graphml.classexporter;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Geoff
 * @since 12/30/2014
 */
public class GraphMLClassExporter {
    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GraphMLClassExporter.class);

    public static final String YWORKS_ENTITY_LABEL_NAME = "com.yworks.entityRelationship.label.name";

    private final Options options;
    private final Configuration freemarkerConfig;
    private final XPathFactory xPathFactory;

    private Map<String/*graph node @id*/, ClassDefn> classDefinitions = new HashMap<>();
    private String relationshipKeyId;

    public GraphMLClassExporter(Options options) {

        this.options = options;

        freemarkerConfig = new Configuration();
        freemarkerConfig.setTemplateLoader(new MultiSourceTemplateLoader());
        xPathFactory = XPathFactory.newInstance();
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

                processDomViaTemplate(((Document) domResult.getNode()), template);
            }
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void processDomViaTemplate(Document outerGraphDoc, Template template) {

        extractClassDefinitions(outerGraphDoc);

        for (ClassDefn classDefn : classDefinitions.values()) {
            applyClassDefn(template, classDefn);
        }
    }

    private void applyClassDefn(Template template, ClassDefn classDefn) {
        try {
            String classDefnName = classDefn.getName();
            String outFileName = classDefnName + "." + options.getFileSuffix();

            try (FileWriter fileWriter = new FileWriter(outFileName)) {
                try {
                    LOG.info("Generating {} to {}", classDefnName, outFileName);
                    template.process(createTemplateMap(classDefn), fileWriter);
                } catch (TemplateException e) {
                    LOG.warn("Unable to process template against the class definition of "+ classDefnName, e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Object> createTemplateMap(ClassDefn classDefn) {
        Map<String, Object> result = new HashMap<>();
        result.put("name", classDefn.getName());
        result.put("extends", classDefn.getExtendsRef());
        result.put("package", options.getBasePackage());
        //TODO add more
        return result;
    }

    private void extractClassDefinitions(Document outerGraphDoc) {
        XPath xPath = createXPath(xPathFactory, outerGraphDoc);

        try {
            NodeList result = (NodeList) xPath.evaluate("/g:graphml/g:graph/g:node", outerGraphDoc, XPathConstants.NODESET);
            for (int i = 0; i < result.getLength(); i++) {
                Element graphNode = (Element) result.item(i);

                Node entityNameNode = (Node) xPath.evaluate("g:data/y:GenericNode/y:NodeLabel[@configuration='" +
                                YWORKS_ENTITY_LABEL_NAME + "']",
                        graphNode, XPathConstants.NODE);
                if (entityNameNode != null) {
                    String entityName = entityNameNode.getTextContent();
                    LOG.debug("Found graph node for entity named {}", entityName);
                    ClassDefn classDefn = new ClassDefn();
                    classDefn.setName(entityName);

                    classDefinitions.put(graphNode.getAttribute("id"), classDefn);
                }
            }
        } catch (XPathExpressionException e) {
            LOG.error("Looking for graph nodes that are entities", e);
            return;
        }

        try {
            Element keyNode = (Element) xPath.evaluate("/g:graphml/g:key[@attr.name='relationship']", outerGraphDoc, XPathConstants.NODE);
            if (keyNode != null) {
                relationshipKeyId = keyNode.getAttribute("id");

            }
        } catch (XPathExpressionException e) {
            LOG.warn("Trying to find relationship key", e);
        }

        if (relationshipKeyId != null) {
            try {
                String relationshipKeyDataExpr = "g:data[@key='" + relationshipKeyId + "']";
                NodeList edgeNodes = (NodeList) xPath.evaluate("/g:graphml/g:graph/g:edge[" + relationshipKeyDataExpr + "]",
                        outerGraphDoc, XPathConstants.NODESET);

                for (int i = 0; i < edgeNodes.getLength(); i++) {
                    Element edgeNode = (Element) edgeNodes.item(i);

                    ClassDefn sourceClassDefn = classDefinitions.get(edgeNode.getAttribute("source"));
                    ClassDefn targetClassDefn = classDefinitions.get(edgeNode.getAttribute("target"));

                    if (sourceClassDefn != null && targetClassDefn != null) {
                        String relationshipType = xPath.evaluate(relationshipKeyDataExpr, edgeNode);

                        switch (relationshipType) {
                            case "extends":
                                sourceClassDefn.setExtendsRef(targetClassDefn.getName());
                                break;
                        }
                    }
                }
            } catch (XPathExpressionException e) {
                LOG.warn("Trying to resolve edges");
            }
        }
    }

    private XPath createXPath(XPathFactory xPathFactory, final Document outerGraphDoc) {
        XPath xPath = xPathFactory.newXPath();
        xPath.setNamespaceContext(new GraphMLNamespaceContext(outerGraphDoc));
        return xPath;
    }

}
