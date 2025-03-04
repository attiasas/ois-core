package org.ois.core.entities;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import org.ois.core.project.Entities;
import org.ois.core.utils.ID;
import org.ois.core.utils.io.data.Blueprint;
import org.ois.core.utils.io.data.DataNode;
import org.ois.core.utils.io.data.DataObject;

import java.util.*;

public class EntityManager implements DataObject<EntityManager>, Disposable {

    Map<String, Map<ID, Entity>> entities = new Hashtable<>();

    boolean saveCache;
    FileHandle manifest;
    DataNode cachedManifest;

    public EntityManager setManifest(FileHandle manifest) {
        this.manifest = manifest;
        return this;
    }

    public EntityManager setSaveCache(boolean save) {
        this.saveCache = save;
        return this;
    }

    public Entity create(String type, boolean defaultIfNotFound) {
        Blueprint<Entity> blueprint = Entities.getBlueprint(type);
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

    public Entity create(DataNode data) {
        if (!data.contains("type")) {
            throw new RuntimeException("can't create Entity: 'type' property not provided");
        }
        return create(data.get("type").getString()).loadData(data);
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

    public void loadManifest(boolean forceLoad) {
        if (manifest == null) {
            // Nothing to do
            return;
        }
        if (cachedManifest != null && !forceLoad) {
            // Load from cache
            loadData(cachedManifest);
            return;
        }
        // Load from file system
        DataNode data = Entities.loadManifest(manifest);
        if (saveCache) {
            cachedManifest = data;
        }
        loadData(data);
    }

    @Override
    public EntityManager loadData(DataNode data) {
        clear();
        for (DataNode entityData : data.get("entities")) {
            create(entityData);
        }
        return this;
    }

    @Override
    public DataNode convertToDataNode() {
        DataNode root = DataNode.Object();

        DataNode entitiesProperty = root.getProperty("entities");

        for (Map<ID, Entity> typeInstances : entities.values()) {
            for (Entity entity : typeInstances.values()) {
                entitiesProperty.add(entity.convertToDataNode());
            }
        }

        return root;
    }

    @Override
    public void dispose() {
        for (Map<ID, Entity> typeInstances : entities.values()) {
            for (Entity entity : typeInstances.values()) {
                if (entity instanceof Disposable) {
                    ((Disposable) entity).dispose();
                }
            }
        }
        clear();
    }
}
