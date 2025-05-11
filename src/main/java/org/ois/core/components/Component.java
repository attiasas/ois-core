package org.ois.core.components;

import org.ois.core.project.Components;
import org.ois.core.utils.io.data.DataObject;
import org.ois.core.utils.io.data.properties.BooleanProperty;

public abstract class Component extends DataObject implements IComponent {
    protected BooleanProperty enable = new BooleanProperty(Components.ENABLE_PROPERTY);

    public Component() {
        registerProperty(enable.setOptional(true).setDefaultValue(true));
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enable.set(enabled);
    }

    @Override
    public boolean isEnable() {
        return this.enable.get();
    }

    @Override
    public void render() {}

    @Override
    public void resize(int width, int height) {}
}
