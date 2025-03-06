package org.ois.core.entities;

import org.ois.core.project.Entities;
import org.ois.core.utils.ReflectionUtils;
import org.ois.core.utils.io.data.Blueprint;
import org.ois.core.utils.io.data.DataNode;

/**
 * Represents a blueprint for creating {@link Entity} instances.
 * It allows defining a base type and an optional custom class for instantiation.
 */
public class EntityBlueprint implements Blueprint<Entity> {

    /** The base type of the entity. */
    protected final String type;
    /** The fully qualified name of a custom class to instantiate. */
    protected String customClass;

    /**
     * Constructs an {@code EntityBlueprint} with the specified type.
     *
     * @param type The type of the entity.
     */
    public EntityBlueprint(String type) {
        this.type = type;
    }

    /**
     * Creates a new {@code Entity} instance based on this blueprint.
     * If a custom class is specified, it attempts to instantiate it using reflection.
     *
     * @return A new {@code Entity} instance.
     * @throws RuntimeException If the custom class instantiation fails.
     */
    @Override
    public Entity create() {
        if (isCustomClassBlueprint()) {
            try {
                return ReflectionUtils.newInstance(customClass);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return new Entity(type);
    }

    /**
     * Checks if this blueprint defines a custom class for instantiation.
     *
     * @return {@code true} if a custom class is set, otherwise {@code false}.
     */
    private boolean isCustomClassBlueprint() {
        return customClass != null && !customClass.isBlank();
    }

    /**
     * Loads blueprint data from a {@code DataNode}.
     */
    @Override
    public EntityBlueprint loadData(DataNode data) {

        if (data.contains(Entities.ENTITY_CUSTOM_CLASS_PROPERTY)) {
            customClass = data.get(Entities.ENTITY_CUSTOM_CLASS_PROPERTY).getString();
        }

        return this;
    }

    @Override
    public DataNode convertToDataNode() {
        DataNode root = DataNode.Object();

        if (isCustomClassBlueprint()) {
            root.set(Entities.ENTITY_CUSTOM_CLASS_PROPERTY, customClass);
        }

        return root;
    }
}
