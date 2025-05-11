package org.ois.core.utils.io.data.properties;

import org.ois.core.utils.io.data.DataNode;

public class BooleanProperty extends Property<Boolean> {

    public BooleanProperty(String key) {
        super(key);
    }

    @Override
    public Boolean loadProperty(DataNode attributeValue) {
        managedData = attributeValue.getBoolean();
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
