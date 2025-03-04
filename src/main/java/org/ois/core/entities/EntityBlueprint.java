package org.ois.core.entities;

import org.ois.core.utils.io.data.Blueprint;
import org.ois.core.utils.io.data.DataNode;

public class EntityBlueprint implements Blueprint<Entity> {

    protected final String type;

    public EntityBlueprint(String type) {
        this.type = type;
    }

    @Override
    public Entity create() {
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
