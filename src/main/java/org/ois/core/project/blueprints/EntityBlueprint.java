package org.ois.core.project.blueprints;

import org.ois.core.entities.Entity;
import org.ois.core.project.Components;
import org.ois.core.project.Entities;
import org.ois.core.utils.ReflectionUtils;
import org.ois.core.utils.io.data.DataBlueprint;
import org.ois.core.utils.io.data.DataNode;
import org.ois.core.utils.io.data.DataObject;
import org.ois.core.utils.log.Logger;

import java.util.Hashtable;
import java.util.Map;

/**
 * Represents a blueprint for creating {@link Entity} instances.
 * It allows defining a base type and an optional custom class for instantiation.
 */
public class EntityBlueprint extends DataBlueprint<Entity> {

    protected static final Logger<EntityBlueprint> log = Logger.get(EntityBlueprint.class);

    /** The base type of the entity. */
    protected final String defaultType;
    /** The fully qualified name of a custom class to instantiate. */
    protected String customClass;
    /** The registered blueprints of the entity components **/
    protected Map<String, ComponentBlueprint<Entity>> entityRegisteredComponents = new Hashtable<>();

    public EntityBlueprint() {
        this("custom");
    }

    /**
     * Constructs an {@code EntityBlueprint} with the specified type.
     *
     * @param type The type of the entity.
     */
    public EntityBlueprint(String type) {
        this.defaultType = type;
    }

    /**
     * Creates a new {@code Entity} instance based on this blueprint.
     * If a custom class is specified, it attempts to instantiate it using reflection.
     *
     * @return A new {@code Entity} instance.
     * @throws RuntimeException If the custom class instantiation fails.
     */
    @Override
    public <C extends Entity> C create() {
        Entity entity;
        boolean custom = isCustomClassBlueprint();
        if (custom) {
            entity = createCustomEntity();
        } else {
            entity = new Entity(defaultType);
        }
        // Create components if registered
        for (Map.Entry<String, ComponentBlueprint<Entity>> componentBlueprint: entityRegisteredComponents.entrySet()) {
            createEntityComponent(entity, componentBlueprint.getKey(), componentBlueprint.getValue());
        }
        log.debug("Created entity (class = %s) with %d components", entity.getClass().getName(), entity.components().size());
        return (C) entity;
    }

    public void createEntityComponent(Entity entity, String compId, ComponentBlueprint<Entity> componentBlueprint) {
        entity.components().register(compId, componentBlueprint.setContext(entity).create());
    }

    /**
     * Checks if this blueprint defines a custom class for instantiation.
     *
     * @return {@code true} if a custom class is set, otherwise {@code false}.
     */
    private boolean isCustomClassBlueprint() {
        return customClass != null && !customClass.isBlank();
    }

    public Entity createCustomEntity() {
        try {
            return ReflectionUtils.newInstance(customClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads blueprint data from a {@code DataNode}.
     */
    @Override
    public <B extends DataObject> B loadData(DataNode data) {
        // Load class
        if (data.contains(Entities.ENTITY_CUSTOM_CLASS_PROPERTY)) {
            customClass = data.get(Entities.ENTITY_CUSTOM_CLASS_PROPERTY).getString();
        }
        // Load component blueprints
        registerCustomComponentsBlueprints();
        entityRegisteredComponents.putAll(Components.loadComponentsBlueprints(data, entityRegisteredComponents.keySet()));
        return (B) this;
    }

    // Implement in custom classes if you want to register custom blueprints
    public void registerCustomComponentsBlueprints() {

    }

    public void registerBlueprint(String key, ComponentBlueprint<Entity> blueprint) {
        if (entityRegisteredComponents.containsKey(key)) {
            throw new RuntimeException(String.format("component key '%s' already registered", key));
        }
        entityRegisteredComponents.put(key, blueprint);
    }


    @Override
    public DataNode convertToDataNode() {
        DataNode root = DataNode.Object();
        // Store custom classes if needed
        if (!getClass().equals(EntityBlueprint.class)) {
            root.set(Entities.BLUEPRINT_CUSTOM_CLASS_PROPERTY, getClass().getName());
        }
        if (isCustomClassBlueprint()) {
            root.set(Entities.ENTITY_CUSTOM_CLASS_PROPERTY, customClass);
        }
        if (entityRegisteredComponents.isEmpty()) {
            return root;
        }
        // Store components blueprints information
        DataNode componentsNode = root.getProperty(Components.COMPONENTS_PROPERTY);
        for (Map.Entry<String, ComponentBlueprint<Entity>> componentBlueprint: entityRegisteredComponents.entrySet()) {
            componentsNode.set(componentBlueprint.getKey(), componentBlueprint.getValue().convertToDataNode());
        }
        return root;
    }
}
