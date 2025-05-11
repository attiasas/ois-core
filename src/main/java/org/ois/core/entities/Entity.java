package org.ois.core.entities;

import org.ois.core.components.ComponentManager;
import org.ois.core.project.Components;
import org.ois.core.project.Entities;
import org.ois.core.utils.ID;
import org.ois.core.utils.io.data.DataNode;
import org.ois.core.utils.io.data.DataObject;
import org.ois.core.utils.io.data.properties.*;
import org.ois.core.utils.log.Logger;
import org.ois.core.utils.math.Transform;

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
    private final Property<Boolean> enabled = new BooleanProperty(Entities.ENABLE_PROPERTY);
    /** The Entity registered components */
    protected final ComponentManager<Entity> components = new ComponentManager<>();

    /**
     * Constructs an Entity with the specified type.
     * The entity is assigned a unique ID and is enabled by default.
     *
     * @param type The type of the entity.
     */
    public Entity(String type) {
        this.id = ID.generate(Entities.LOG_TOPIC);
        registerProperty(this.type.set(type));
        registerProperty(this.enabled.setOptional(true).setDefaultValue(true));
    }

    /**
     * Updates the entity. If the entity is disabled, the update is skipped.
     */
    public void update() {
        if (!this.enabled.get()) {
            return;
        }
        log.debug(this.id.toString(), String.format("Updating { %s, ID: '%s' }", this.type, this.id));
        components.update();
    }

    /** TODO: remove */
    public void render() {
        if (!this.enabled.get()) {
            return;
        }
        components.render();
    }

    /** TODO: remove */
    public void resize(int width, int height) {
        if (!this.enabled.get()) {
            return;
        }
        components.resize(width, height);
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

    public ComponentManager<Entity> components() { return this.components; }

    @Override
    public <T extends DataObject> T loadData(DataNode dataNode) {
        Entity entity = super.loadData(dataNode);

        if (dataNode.contains(Components.COMPONENTS_PROPERTY)) {
            // Load components data
            components.loadData(dataNode.get(Components.COMPONENTS_PROPERTY));
        }

        return (T) entity;
    }

    @Override
    public DataNode convertToDataNode() {
        DataNode root = super.convertToDataNode();
        if (!components.isEmpty())
        {
            //Set components data
            root.set(Components.COMPONENTS_PROPERTY, components.convertToDataNode());
        }
        return root;
    }

}
