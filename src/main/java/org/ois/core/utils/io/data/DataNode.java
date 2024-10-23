package org.ois.core.utils.io.data;

import java.util.*;

public class DataNode {

    /**
     * The possible data types that a node can represent
     */
    public enum Type {
        Unknown, Object, Collection, Primitive
    }

    protected String value;
    protected List<DataNode> content;
    protected Map<String, DataNode> attributes;
    protected Type nodeType;

    public DataNode(Type type) {
        this.nodeType = type;
        content = new ArrayList<>();
        attributes = new Hashtable<>();
    }

    public DataNode() {
        this(Type.Unknown);
    }

    public static DataNode Object() {
        return new DataNode(Type.Object);
    }
    public static DataNode Primitive(String value) {
        return new DataNode(Type.Primitive).setValue(value);
    }
    public static DataNode Primitive(int value) {
        return new DataNode(Type.Primitive).setValue(value);
    }
    public static DataNode Primitive(float value) {
        return new DataNode(Type.Primitive).setValue(value);
    }
    public static DataNode Primitive(boolean value) {
        return new DataNode(Type.Primitive).setValue(value);
    }
//    public static DataNode Collection(Collection<?>... collections) {
//        return new DataNode(Type.Collection)
//    }

    public Type getType() {
        if (!Type.Unknown.equals(nodeType)) {
            return nodeType;
        }
        if (!value.isEmpty()) {
            return Type.Primitive;
        }
        if (!content.isEmpty()) {
            return Type.Collection;
        }
        if (!attributes.isEmpty()) {
            return Type.Object;
        }
        return Type.Unknown;
    }


    // Node as Collection



    // Node as Primitive

    public DataNode setValue(String primitiveValue) {
        this.value = primitiveValue;
        return this;
    }

    public DataNode setValue(int primitiveValue) {
        return setValue(String.valueOf(primitiveValue));
    }

    public DataNode setValue(float primitiveValue) {
        return setValue(String.valueOf(primitiveValue));
    }

    public DataNode setValue(boolean primitiveValue) {
        return setValue(String.valueOf(primitiveValue));
    }

    public String getString() {
        return this.value;
    }

    public int getInt() {
        return Integer.parseInt(getString());
    }

    public float getFloat() {
        return Float.parseFloat(getString());
    }

    public boolean getBoolean() {
        return Boolean.parseBoolean(getString());
    }
}
