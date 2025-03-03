package org.ois.core.entities;

import org.ois.core.utils.io.data.Blueprint;
import org.ois.core.utils.io.data.DataNode;
import org.ois.core.utils.io.data.DataObject;

import java.util.Map;

public class EntityBlueprint implements Blueprint<Entity>, DataObject<EntityBlueprint> {

    private final String type;

    public EntityBlueprint(String type) {
        this.type = type;
    }

    @Override
    public Entity create(Map<String,Object> params) {
        if (params != null && params.containsKey("type")) {
            return new Entity((String) params.getOrDefault("type", type));
        }
        return new Entity(type);
    }

    @Override
    public EntityBlueprint loadData(DataNode data) {
        return this;
    }

    @Override
    public DataNode convertToDataNode() {
        return DataNode.Object();
    }
}
