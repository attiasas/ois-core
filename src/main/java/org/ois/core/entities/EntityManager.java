package org.ois.core.entities;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import org.ois.core.components.IComponent;
import org.ois.core.project.Entities;
import org.ois.core.utils.ID;
import org.ois.core.utils.io.data.DataBlueprint;
import org.ois.core.utils.io.data.DataNode;
import org.ois.core.utils.io.data.IDataObject;
import org.ois.core.utils.log.Logger;

import java.util.*;

/**
 * Manages the creation, storage, and lifecycle of {@link Entity} instances.
 * It supports entity retrieval, removal, and periodic updates.
 */
public class EntityManager implements IDataObject<EntityManager>, Disposable {

    private static final Logger<EntityManager> log = Logger.get(EntityManager.class);

    /** Stores entities categorized by their type and ID. */
    Map<String, Map<ID, Entity>> entities = new Hashtable<>();

    /** File handle to the manifest file. */
    FileHandle manifest;
    /** Flag indicating whether to cache the manifest data. */
    boolean saveCache;
    /** Cached data of the loaded manifest. */
    DataNode cachedManifest;

    /**
     * Sets the manifest file.
     *
     * @param manifest The manifest file handle.
     * @return The updated {@code EntityManager} instance.
     */
    public EntityManager setManifest(FileHandle manifest) {
        this.manifest = manifest;
        return this;
    }

    /**
     * Enables or disables caching of the manifest data.
     *
     * @param save {@code true} to enable caching, {@code false} otherwise.
     * @return The updated {@code EntityManager} instance.
     */
    public EntityManager setSaveCache(boolean save) {
        this.saveCache = save;
        return this;
    }

    /**
     * Creates a new entity of the specified type.
     *
     * @param type The type of the entity.
     * @param defaultIfNotFound If {@code true}, a default entity is created if no blueprint is found.
     * @return The created {@code Entity} instance.
     * @throws UnsupportedOperationException If the blueprint is missing and {@code defaultIfNotFound} is {@code false}.
     */
    public <T extends Entity> T create(String type, boolean defaultIfNotFound) {
        DataBlueprint<Entity> blueprint = Entities.getBlueprint(type);
        if (blueprint == null && !defaultIfNotFound) {
            throw new RuntimeException(String.format("can't find '%s' entity blueprint", type));
        }
        log.debug(String.format("Creating entity '%s' (with blueprint = %s)", type, blueprint == null ? "false" : blueprint.getClass().getName()));
        Entity entity = blueprint != null ? blueprint.create() : new Entity(type);
        if (!entities.containsKey(type)) {
            entities.put(type, new Hashtable<>());
        }
        entities.get(type).put(entity.id, entity);
        return (T) entity;
    }

    /**
     * Creates an entity from a {@code DataNode} containing its properties.
     *
     * @param data The data node containing entity attributes.
     * @return The created {@code Entity} instance.
     * @throws RuntimeException If the type property is missing.
     */
    public <T extends Entity> T create(DataNode data) {
        if (!data.contains(Entities.TYPE_PROPERTY)) {
            throw new RuntimeException(String.format("can't create Entity: '%s' property not provided", Entities.TYPE_PROPERTY));
        }
        return create(data.get(Entities.TYPE_PROPERTY).getString()).loadData(data);
    }

    /**
     * Creates an entity of the specified type.
     *
     * @param type The entity type.
     * @return The created {@code Entity} instance.
     */
    public <T extends Entity> T create(String type) {
        return create(type, false);
    }

    /**
     * Removes an entity from the manager.
     *
     * @param entity The entity to remove.
     * @return {@code true} if removed successfully, otherwise {@code false}.
     */
    public boolean remove(Entity entity) {
        return remove(entity.id);
    }

    /**
     * Removes an entity by its ID.
     *
     * @param id The ID of the entity to remove.
     * @return {@code true} if removed successfully, otherwise {@code false}.
     */
    public boolean remove(ID id) {
        if (!entities.containsKey(id.getTopic())) {
            return false;
        }
        if (!entities.get(id.getTopic()).containsKey(id)) {
            return false;
        }
        return entities.get(id.getTopic()).remove(id) != null;
    }

    /**
     * Retrieves an entity by its ID.
     *
     * @param id The entity ID.
     * @return The corresponding {@code Entity}, or {@code null} if not found.
     */
    public Entity get(ID id) {
        if (!entities.containsKey(id.getTopic())) {
            return null;
        }
        return entities.get(id.getTopic()).get(id);
    }

    /**
     * Retrieves all entities of the specified type.
     *
     * @param type The entity type.
     * @return A collection of entities of the given type.
     */
    public Collection<Entity> get(String type) {
        if (!entities.containsKey(type)) {
            return List.of();
        }
        return entities.get(type).values();
    }

    public <T extends IComponent> Collection<Entity> withComponent(Class<T> componentClass) {
        Collection<Entity> withComponents = new ArrayList<>();
        for (Map<ID, Entity> typeInstances : entities.values()) {
            for (Entity entity : typeInstances.values()) {
                if (entity.components.has(componentClass)) {
                    withComponents.add(entity);
                }
            }
        }
        return withComponents;
    }

    /**
     * Updates all enabled entities.
     */
    public void update() {
//        log.debug("Updating entities");
        for (Map<ID, Entity> typeInstances : entities.values()) {
            for (Entity entity : typeInstances.values()) {
                if (!entity.isEnabled()) {
//                    log.debug(String.format("entity '%s' disabled", entity.id));
                    continue;
                }
//                log.debug(String.format("Update entity '%s'", entity.id));
                entity.update();
            }
        }
    }

    /**
     * TODO: remove
     */
    public void render() {
        for (Map<ID, Entity> typeInstances : entities.values()) {
            for (Entity entity : typeInstances.values()) {
                if (!entity.isEnabled()) {
                    continue;
                }
                entity.render();
            }
        }
    }

    /**
     * TODO: remove
     */
    public void resize(int width, int height) {
        for (Map<ID, Entity> typeInstances : entities.values()) {
            for (Entity entity : typeInstances.values()) {
                if (!entity.isEnabled()) {
                    continue;
                }
                entity.resize(width, height);
            }
        }
    }

    /**
     * Clears all entities from the manager.
     */
    public void clear() { this.entities.clear(); }

    /**
     * Loads the manifest data from a file or cache.
     *
     * @param forceLoad If {@code true}, forces reloading from the file system.
     */
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
    public <M extends EntityManager> M loadData(DataNode data) {
        dispose();
        for (DataNode entityData : data.get(Entities.ENTITIES_PROPERTY)) {
            create(entityData);
        }
        return (M) this;
    }

    @Override
    public DataNode convertToDataNode() {
        DataNode root = DataNode.Object();

        DataNode entitiesProperty = root.getProperty(Entities.ENTITIES_PROPERTY);

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
