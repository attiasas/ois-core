package org.ois.core.entities;

import org.ois.core.project.Entities;
import org.ois.core.utils.ID;

import java.util.*;

public class EntityManager {

    Map<String, Map<ID, Entity>> entities = new Hashtable<>();

    public Entity create(String type, boolean defaultIfNotFound) {
        EntityBlueprint blueprint = Entities.getBlueprint(type);
        if (blueprint == null && !defaultIfNotFound) {
            throw new UnsupportedOperationException(String.format("can't find '%s' entity blueprint", type));
        }
        Entity entity = blueprint != null ? blueprint.create() : new Entity(type);
        if (!entities.containsKey(type)) {
            entities.put(type, new Hashtable<>());
        }
        entities.get(type).put(entity.id, entity);
        return entity;
    }

    public Entity create(String type) {
        return create(type, false);
    }

    public boolean remove(Entity entity) {
        return remove(entity.id);
    }

    public boolean remove(ID id) {
        if (!entities.containsKey(id.getTopic())) {
            return false;
        }
        if (!entities.get(id.getTopic()).containsKey(id)) {
            return false;
        }
        return entities.get(id.getTopic()).remove(id) != null;
    }

    public Entity get(ID id) {
        if (!entities.containsKey(id.getTopic())) {
            return null;
        }
        return entities.get(id.getTopic()).get(id);
    }

    public Collection<Entity> get(String type) {
        List<Entity> typeInstances = new ArrayList<>();
        if (!entities.containsKey(type)) {
            return List.of();
        }
        return entities.get(type).values();
    }

    public void update() {
        for (Map<ID, Entity> typeInstances : entities.values()) {
            for (Entity entity : typeInstances.values()) {
                if (!entity.isEnabled()) {
                    continue;
                }
                entity.update();
            }
        }
    }

    public void clear() { this.entities.clear(); }
}
