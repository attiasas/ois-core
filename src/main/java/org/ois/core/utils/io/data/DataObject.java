package org.ois.core.utils.io.data;

import org.ois.core.utils.io.data.properties.Property;

import java.util.ArrayList;
import java.util.List;

public class DataObject implements IDataObject<DataObject> {

    List<Property> managedProperties = new ArrayList<>();

    public <P extends Property> P registerProperty(P property) {
        managedProperties.add(property);
        return property;
    }

    @Override
    public <T extends DataObject> T loadData(DataNode dataNode) {
        for (Property property : managedProperties) {
            property.loadData(dataNode);
        }
        return (T) this;
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
