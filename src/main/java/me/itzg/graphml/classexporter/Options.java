package me.itzg.graphml.classexporter;

import org.kohsuke.args4j.Option;

import java.io.File;

/**
 * @author Geoff
 * @since 12/30/2014
 */
public class Options {
    @Option(name="--graphml", required = true, usage = "The GraphML file to use as the input specification")
    private File graphMLFile;

    @Option(name="--template", usage="The name of a builtin template or a path to a Freemark template. Default is 'java'.", metaVar = "NAME")
    private String templateName = "java";

    @Option(name="--help", help = true, aliases = {"-h","-?"}, usage="Show usage information")
    private boolean helpRequested;

    @Option(name="--suffix", usage="The suffix to use on generated files. Default is 'java'")
    private String fileSuffix = "java";

    @Option(name="--base-package", usage = "The base package or namespace of the generated classes")
    private String basePackage;

    public File getGraphMLFile() {
        return graphMLFile;
    }

    public boolean isHelpRequested() {
        return helpRequested;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getFileSuffix() {
        return fileSuffix;
    }

    public void setFileSuffix(String fileSuffix) {
        this.fileSuffix = fileSuffix;
    }

    public String getBasePackage() {
        return basePackage;
    }
}
