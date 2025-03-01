package org.ois.core.entities;

import org.ois.core.utils.ID;
import org.ois.core.utils.io.data.DataNode;
import org.ois.core.utils.io.data.DataObject;
import org.ois.core.utils.log.Logger;

import java.util.Objects;

public class Entity {

    private final static Logger<Entity> log = Logger.get(Entity.class);

    public final String name;

    public final ID id;

    private boolean enabled;

    protected Entity() {
        this("");
    }

    protected Entity(String name) {
        this.name = name;
        this.id = ID.generate(name);
    }

    public void update() {
        if (!this.enabled) {
            return;
        }
        log.info(String.format("{ Type: '%s', ID: '%s' }", this.name, this.id));
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

    public DataObject<Entity> getBlueprint() {
        final String className = getClass().getName();

        return new DataObject<>() {
            @Override
            public Entity loadData(DataNode data) {
                if (data == null || !data.contains("type")) {
                    throw new RuntimeException("entity 'type' not provided");
                }
                Entity entity = new Entity(data.getProperty("type").getString());

                entity.setEnabled(data.getProperty("enable").getBoolean());

                return entity;
            }

            @Override
            public DataNode convertToDataNode() {
                DataNode root = DataNode.Object();
                // Add attributes
                root.set("class", className);
                root.set("type", name);
                root.set("enable", enabled);
                return root;
            }
        };
    }
}
