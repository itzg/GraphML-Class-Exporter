package me.itzg.graphml.classexporter;

import java.util.List;

/**
 * @author Geoff Bourne
 * @since 12/30/2014
 */
public class ClassDefn {
    private String name;

    private String extendsRef;

    private List<String> implementsRef;

    private List<FieldRef> fieldRefs;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtendsRef() {
        return extendsRef;
    }

    public void setExtendsRef(String extendsRef) {
        this.extendsRef = extendsRef;
    }

    public List<String> getImplementsRef() {
        return implementsRef;
    }

    public void setImplementsRef(List<String> implementsRef) {
        this.implementsRef = implementsRef;
    }

    public List<FieldRef> getFieldRefs() {
        return fieldRefs;
    }

    public void setFieldRefs(List<FieldRef> fieldRefs) {
        this.fieldRefs = fieldRefs;
    }
}
