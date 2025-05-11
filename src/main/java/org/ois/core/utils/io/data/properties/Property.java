package org.ois.core.utils.io.data.properties;

import org.ois.core.utils.io.data.DataNode;
import org.ois.core.utils.io.data.IDataObject;

public abstract class Property<T> implements IDataObject<T> {

    protected final String key;

    protected T managedData;

    private T defaultValue;
    private boolean defaultSet;

    protected boolean optional;

    public Property(String key) {
        this.key = key;
    }

    public Property<T> set(T data) {
        this.managedData = data;
        return this;
    }

    public Property<T> setOptional(boolean optional) {
        this.optional = optional;
        return this;
    }

    public Property<T> setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
        this.defaultSet = true;
        this.managedData = defaultValue;
        return this;
    }

    public T get() {
        return managedData;
    }

    @Override
    public <D extends T> D loadData(DataNode dataNode) {
        if (!dataNode.contains(key)) {
            if (!optional) {
                throw new RuntimeException(String.format("Can't load '%s' from data node: expected '%s' attribute", managedData.getClass().getName(), this.key));
            }
            if (defaultSet) {
                managedData = defaultValue;
            }
            return (D) managedData;
        }
        return (D) loadProperty(dataNode.get(key));
    }

    public abstract T loadProperty(DataNode attributeValue);
    public abstract DataNode appendProperty(DataNode root);

    public DataNode appendPropertyToDataNode(DataNode root) {
        if (optional && defaultSet && managedData == defaultValue) {
            // No need to append optional default value
            return root;
        }
        return appendProperty(root);
    }

    @Override
    public String toString() {
        return "'" + key + '\'' + ": " + managedData;
    }
}
