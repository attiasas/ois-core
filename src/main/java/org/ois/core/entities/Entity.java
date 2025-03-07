package org.ois.core.entities;

import org.ois.core.project.Entities;
import org.ois.core.utils.ID;
import org.ois.core.utils.io.data.DataNode;
import org.ois.core.utils.io.data.DataObject;
import org.ois.core.utils.log.Logger;

import java.util.Objects;

/**
 * Represents an entity in the simulation with a unique ID and type.
 * Entities can be enabled or disabled and can be serialized/deserialized using DataNode.
 */
public class Entity implements DataObject<Entity> {

    private final static Logger<Entity> log = Logger.get(Entity.class);

    /** The type of the entity. */
    public final String type;
    /** The unique identifier for this entity. */
    public final ID id;
    /** Flag indicating whether the entity is enabled. */
    private boolean enabled;

    /**
     * Constructs an Entity with the specified type.
     * The entity is assigned a unique ID and is enabled by default.
     *
     * @param type The type of the entity.
     */
    protected Entity(String type) {
        this.type = type;
        this.id = ID.generate(type);
        this.enabled = true;
    }

    /**
     * Updates the entity. If the entity is disabled, the update is skipped.
     */
    public void update() {
        if (!this.enabled) {
            return;
        }
        log.debug(Entities.LOG_TOPIC, String.format("Updating { Type: '%s', ID: '%s' }", this.type, this.id));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Entity other = (Entity) obj;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Sets the enabled state of the entity.
     *
     * @param enabled True to enable, false to disable.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Checks if the entity is enabled.
     *
     * @return True if enabled, false otherwise.
     */
    public boolean isEnabled() { return this.enabled; }

    /**
     * Loads entity data from a DataNode.
     * If the "enable" property exists, it updates the enabled state; otherwise, it remains enabled by default.
     *
     * @param data The DataNode containing entity data.
     * @return The updated entity.
     */
    @Override
    public Entity loadData(DataNode data) {
        // Default value: enable = true
        setEnabled(!data.contains(Entities.ENABLE_PROPERTY) || data.getProperty(Entities.ENABLE_PROPERTY).getBoolean());
        return this;
    }

    @Override
    public DataNode convertToDataNode() {
        DataNode root = DataNode.Object();
        // Add attributes
        root.set(Entities.TYPE_PROPERTY, type);
        root.set(Entities.ENABLE_PROPERTY, enabled);
        return root;
    }
}
