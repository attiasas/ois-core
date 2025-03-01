package org.ois.core.entities;

import org.ois.core.utils.ID;

import java.util.Objects;

public class Entity {

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
}
