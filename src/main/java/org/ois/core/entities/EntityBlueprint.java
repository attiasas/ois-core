package org.ois.core.entities;

import org.ois.core.utils.io.data.Blueprint;
import org.ois.core.utils.io.data.DataNode;
import org.ois.core.utils.io.data.DataObject;

public class EntityBlueprint implements Blueprint<Entity>, DataObject<EntityBlueprint> {

    private final String type;

    public EntityBlueprint(String type) {
        this.type = type;
    }

    @Override
    public Entity create(Object... ignored) {
        return new Entity(type);
    }

    @Override
    public EntityBlueprint loadData(DataNode data) {
        String dataType = this.type;
        if (data.contains("type")) {
            dataType = data.getProperty("type").getString();
        }
        return new EntityBlueprint(dataType);
    }

    @Override
    public DataNode convertToDataNode() {
        return DataNode.Object();
    }
}
