package org.ois.core.utils.io.data;

import org.ois.core.utils.io.data.properties.Property;

import java.util.ArrayList;
import java.util.List;

public class DataObject implements IDataObject<DataObject> {

    List<Property> managedProperties = new ArrayList<>();

    public <T extends Property> T registerProperty(T property) {
        managedProperties.add(property);
        return property;
    }

    public <T extends DataObject> T loadObject(DataNode dataNode) {
        return (T) loadData(dataNode);
    }

    @Override
    public DataObject loadData(DataNode dataNode) {
        for (Property property : managedProperties) {
            property.loadData(dataNode);
        }
        return this;
    }

    @Override
    public DataNode convertToDataNode() {
        DataNode root = new DataNode();
        for (Property property : managedProperties) {
            property.appendPropertyToDataNode(root);
        }
        return root;
    }
}
