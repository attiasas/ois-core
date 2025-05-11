package org.ois.core.utils.io.data.properties;

import org.ois.core.utils.io.data.DataNode;

public class IntProperty extends Property<Integer> {
    public IntProperty(String key) {
        super(key);
    }

    @Override
    public Integer loadProperty(DataNode attributeValue) {
        managedData = attributeValue.getInt();
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
