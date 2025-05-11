package org.ois.core.components;

import com.badlogic.gdx.utils.Disposable;
import org.ois.core.utils.io.data.DataNode;
import org.ois.core.utils.io.data.IDataObject;
import org.ois.core.utils.log.Logger;

import java.util.Hashtable;
import java.util.Map;

public class ComponentManager<C> implements IDataObject<ComponentManager<C>>, Disposable {

    private static final Logger<ComponentManager> log = Logger.get(ComponentManager.class);

    /** The registered components */
    private final Map<String, IComponent> components = new Hashtable<>();

    // -- Component management ---------

    /**
     * Call the update method in the registered components. If the component is disabled, the update is skipped.
     */
    public void update() {
        for (IComponent component : components.values()) {
            if (component.isEnable()) {
                component.update();
            }
        }
    }
    /**
     * Call the render method in the registered components. If the component is disabled, the render is skipped.
     */
    public void render() {
        for (IComponent component : components.values()) {
            if (component.isEnable()) {
                component.render();
            }
        }
    }
    /**
     * Call the resize method in the registered components. If the component is disabled, the resize is skipped.
     */
    public void resize(int width, int height) {
        for (IComponent component : components.values()) {
            if (component.isEnable()) {
                component.resize(width, height);
            }
        }
    }

    public <T extends IComponent> T register(String id, IComponent component) {
        return (T) this.components.put(id, component);
    }

    public <T extends IComponent> T get(String id) {
        return (T) this.components.get(id);
    }

    public boolean has(String id) {
        return this.components.containsKey(id);
    }

    public <T extends IComponent> T register(IComponent component) {
        return register(component.getClass().getName(), component);
    }

    public <T extends IComponent> T get(Class<T> componentClass) {
        for (IComponent component : components.values()) {
            if (component.getClass().isAssignableFrom(componentClass)) {
                return (T) component;
            }
        }
        return null;
    }

    public boolean has(Class componentClass) {
        return get(componentClass) != null;
    }

    public boolean isEmpty() { return this.components.isEmpty(); }

    public int size() { return this.components.size(); }

    /**
     * Clears all components from the manager.
     */
    public void clear() {
        for (IComponent componentInstance : components.values()) {
            if (componentInstance instanceof Disposable) {
                ((Disposable) componentInstance).dispose();
            }
        }
        this.components.clear();
    }

    @Override
    public void dispose() {
        clear();
    }

    // -- serialize / deserialize data

    @Override
    public <D extends ComponentManager<C>> D loadData(DataNode componentsNode) {
        for (Map.Entry<String, DataNode> componentInfo : componentsNode.properties()) {
            if (!has(componentInfo.getKey())) {
                throw new RuntimeException(String.format("'%s' component is not registered", componentInfo.getKey()));
            }
            // Get the registered component
            IComponent component = get(componentInfo.getKey());
            if (!(component instanceof IDataObject)) {
                throw new RuntimeException(String.format("component '%s' is not implementing IDataObject", component.getClass()));
            }
            // Load component data
            ((IDataObject<?>) component).loadData(componentInfo.getValue());
        }

        return (D) this;
    }


    @Override
    public DataNode convertToDataNode() {
        DataNode componentsNode = DataNode.Object();
        if (isEmpty()) {
            // Nothing to do
            return componentsNode;
        }
        for (Map.Entry<String, IComponent> componentEntry : this.components.entrySet()) {
            if (!(componentEntry.getValue() instanceof IDataObject)) {
                // Nothing to do
                continue;
            }
            // set data
            componentsNode.set(componentEntry.getKey(), ((IDataObject<?>) componentEntry.getValue()).convertToDataNode());
        }

        return componentsNode;
    }
}
