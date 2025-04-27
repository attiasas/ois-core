package org.ois.core.utils.io.data.properties;

import org.ois.core.utils.io.data.DataNode;

public class StringProperty extends Property<String> {
    public StringProperty(String key) {
        super(key);
    }

    @Override
    public String loadProperty(DataNode attributeValue) {
        managedData = attributeValue.getString();
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
