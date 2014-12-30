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

    @Option(name="--template", required = true, usage="The name of a builtin template or a path to a Freemark template", metaVar = "NAME")
    private String templateName = "java";

    @Option(name="--help", help = true, aliases = {"-h","-?"}, usage="Show usage information")
    private boolean helpRequested;

    public File getGraphMLFile() {
        return graphMLFile;
    }

    public boolean isHelpRequested() {
        return helpRequested;
    }

    public String getTemplateName() {
        return templateName;
    }
}
