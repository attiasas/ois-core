package org.ois.core.utils.io.data.properties;

import org.ois.core.utils.io.data.DataNode;

public class FloatProperty extends Property<Float> {
    public FloatProperty(String key) {
        super(key);
    }

    @Override
    public Float loadProperty(DataNode attributeValue) {
        managedData = attributeValue.getFloat();
        return managedData;
    }

    @Override
    public DataNode appendProperty(DataNode root) {
        root.set(this.key, managedData);
        return root;
    }

    @Override
    public DataNode convertToDataNode() {
        return DataNode.Primitive(managedData);
    }
}
