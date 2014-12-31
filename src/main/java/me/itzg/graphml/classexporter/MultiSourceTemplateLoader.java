package me.itzg.graphml.classexporter;

import freemarker.cache.TemplateLoader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

/**
* @author Geoff Bourne
* @since 12/30/2014
*/
class MultiSourceTemplateLoader implements TemplateLoader {
    @Override
    public Object findTemplateSource(String name) throws IOException {
        URL templateUrl = this.getClass().getClassLoader().getResource("templates/" + name + ".ftl");
        if (templateUrl == null) {
            File fileSource = new File(name);
            return fileSource.exists() ? new OurTemplateSource(fileSource) : null;
        } else {
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
            } else {
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
}
