package org.ois.core.entities;

import org.ois.core.project.Entities;

import java.util.ArrayList;
import java.util.List;

public class EntityManager {

    List<Entity> entities = new ArrayList<>();

    public Entity create(String type, boolean defaultIfNotFound) {
        EntityBlueprint blueprint = Entities.getBlueprint(type);
        if (blueprint == null && !defaultIfNotFound) {
            throw new UnsupportedOperationException(String.format("can't find '%s' entity blueprint", type));

        }
        Entity entity = blueprint != null ? blueprint.create() : new Entity(type);
        entities.add(entity);
        return entity;
    }

    public Entity create(String type) {
        return create(type, false);
    }

    public void clear() { this.entities.clear(); }
}
