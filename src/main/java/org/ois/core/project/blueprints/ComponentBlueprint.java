package org.ois.core.project.blueprints;

import org.ois.core.components.IComponent;
import org.ois.core.utils.io.data.DataBlueprint;

public abstract class ComponentBlueprint<C> extends DataBlueprint<IComponent> {
    protected C context;

    public ComponentBlueprint<C> setContext(C parent) {
        this.context = parent;
        return this;
    }
}
