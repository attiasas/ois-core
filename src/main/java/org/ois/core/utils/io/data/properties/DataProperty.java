package org.ois.core.utils.io.data.properties;

import org.ois.core.utils.io.data.DataNode;
import org.ois.core.utils.io.data.IDataObject;

public class DataProperty<T extends IDataObject<T>> extends Property<T> {

    public DataProperty(String key) {
        super(key);
    }

    @Override
    public T loadProperty(DataNode attributeValue) {
        if (managedData == null) {
            throw new RuntimeException("can't load DataObject property if managed data is null, set default value or optional");
        }
        return managedData.loadData(attributeValue);
    }

    @Override
    public DataNode appendProperty(DataNode root) {
        if (managedData == null && optional) {
            return root;
        }
        root.set(this.key, convertToDataNode());
        return root;
    }

    @Override
    public DataNode convertToDataNode() {
        return managedData == null ? DataNode.Object() : managedData.convertToDataNode();
    }
}
