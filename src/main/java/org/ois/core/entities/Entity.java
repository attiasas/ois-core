package org.ois.core.entities;

import org.ois.core.project.Entities;
import org.ois.core.utils.ID;
import org.ois.core.utils.io.data.DataObject;
import org.ois.core.utils.io.data.properties.BooleanProperty;
import org.ois.core.utils.io.data.properties.StringProperty;
import org.ois.core.utils.log.Logger;

import java.util.Objects;

/**
 * Represents an entity in the simulation with a unique ID and type.
 * Entities can be enabled or disabled and can be serialized/deserialized using DataNode.
 */
public class Entity extends DataObject {

    private final static Logger<Entity> log = Logger.get(Entity.class);

    /** The type of the entity. */
    private final StringProperty type = new StringProperty(Entities.TYPE_PROPERTY);
    /** The unique identifier for this entity. */
    public final ID id;
    /** Flag indicating whether the entity is enabled. */
    private final BooleanProperty enabled = new BooleanProperty(Entities.ENABLE_PROPERTY);

    /**
     * Constructs an Entity with the specified type.
     * The entity is assigned a unique ID and is enabled by default.
     *
     * @param type The type of the entity.
     */
    protected Entity(String type) {
        this.id = ID.generate(type);
        registerProperty(this.type.set(type));
        registerProperty(this.enabled.setOptional(true).setDefaultValue(true).set(true));
    }

    /**
     * Updates the entity. If the entity is disabled, the update is skipped.
     */
    public void update() {
        if (!this.enabled.get()) {
            return;
        }
        log.debug(Entities.LOG_TOPIC, String.format("Updating { %s, ID: '%s' }", this.type, this.id));
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
        this.enabled.set(enabled);
    }

    /**
     * Checks if the entity is enabled.
     *
     * @return True if enabled, false otherwise.
     */
    public boolean isEnabled() { return this.enabled.get(); }

    public String getType() {
        return this.type.get();
    }
}
