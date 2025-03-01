package org.ois.core.entities;

import java.util.ArrayList;
import java.util.List;

public class EntityManager {

    List<Entity> entities = new ArrayList<>();

    public Entity create(String type) {
        Entity entity = new Entity(type);
        entities.add(entity);
        return entity;
    }

    public void clear() { this.entities.clear(); }
}
