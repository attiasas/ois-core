package org.ois.core.entities;

import org.ois.core.utils.ID;
import org.ois.core.utils.io.data.DataNode;
import org.ois.core.utils.io.data.DataObject;
import org.ois.core.utils.log.Logger;

import java.util.Objects;

public class Entity implements DataObject<Entity> {

    private final static Logger<Entity> log = Logger.get(Entity.class);

    public final String type;

    public final ID id;

    private boolean enabled;

    protected Entity(String type) {
        this.type = type;
        this.id = ID.generate(type);
        this.enabled = true;
    }

    public void update() {
        if (!this.enabled) {
            return;
        }
        log.info(String.format("{ Type: '%s', ID: '%s' }", this.type, this.id));
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

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() { return this.enabled; }

    @Override
    public Entity loadData(DataNode data) {

        // Default value: enable = true
        setEnabled(!data.contains("enable") || data.getProperty("enable").getBoolean());

        return this;
    }

    @Override
    public DataNode convertToDataNode() {
        DataNode root = DataNode.Object();
        // Add attributes
        root.set("type", type);
        root.set("enable", enabled);
        return root;
    }
}
