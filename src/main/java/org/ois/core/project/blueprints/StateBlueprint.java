package org.ois.core.project.blueprints;

import org.ois.core.project.Components;
import org.ois.core.project.States;
import org.ois.core.state.managed.IManagedState;
import org.ois.core.state.managed.ManagedState;
import org.ois.core.utils.ReflectionUtils;
import org.ois.core.utils.io.data.DataBlueprint;
import org.ois.core.utils.io.data.DataNode;
import org.ois.core.utils.io.data.DataObject;

import java.util.Hashtable;
import java.util.Map;

public class StateBlueprint extends DataBlueprint<IManagedState> {

    /** The fully qualified name of a custom class to instantiate. */
    protected String customClass;
    /** The registered blueprints of the entity components **/
    protected Map<String, ComponentBlueprint<IManagedState>> stateRegisteredComponents = new Hashtable<>();

    @Override
    public <S extends IManagedState> S create() {
        IManagedState state;
        if (isCustomClassBlueprint()) {
            state = createCustomState();
        } else {
            state = new ManagedState();
        }
        // Create components if registered
        for (Map.Entry<String, ComponentBlueprint<IManagedState>> componentBlueprint: stateRegisteredComponents.entrySet()) {
            createStateComponent(state, componentBlueprint.getKey(), componentBlueprint.getValue());
        }
        return (S) state;
    }

    /**
     * Checks if this blueprint defines a custom class for instantiation.
     *
     * @return {@code true} if a custom class is set, otherwise {@code false}.
     */
    private boolean isCustomClassBlueprint() {
        return customClass != null && !customClass.isBlank();
    }

    public IManagedState createCustomState() {
        try {
            return ReflectionUtils.newInstance(customClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void createStateComponent(IManagedState state, String compId, ComponentBlueprint<IManagedState> componentBlueprint) {
        state.getComponents().register(compId, componentBlueprint.setContext(state).create());
    }

    @Override
    public <B extends DataObject> B loadData(DataNode data) {
        // Load component blueprints
        registerCustomComponentsBlueprints();
        stateRegisteredComponents.putAll(Components.loadComponentsBlueprints(data, stateRegisteredComponents.keySet()));
        return (B) this;
    }

    // Implement in custom classes if you want to register custom blueprints
    public void registerCustomComponentsBlueprints() {

    }

    public void registerBlueprint(String key, ComponentBlueprint<IManagedState> blueprint) {
        if (stateRegisteredComponents.containsKey(key)) {
            throw new RuntimeException(String.format("component key '%s' already registered", key));
        }
        stateRegisteredComponents.put(key, blueprint);
    }

    @Override
    public DataNode convertToDataNode() {
        DataNode root = DataNode.Object();
        // Store custom classes if needed
        if (!getClass().equals(StateBlueprint.class)) {
            root.set(States.BLUEPRINT_CUSTOM_CLASS_PROPERTY, getClass().getName());
        }
        if (stateRegisteredComponents.isEmpty()) {
            return root;
        }
        // Store components blueprints information
        DataNode componentsNode = root.getProperty(Components.COMPONENTS_PROPERTY);
        for (Map.Entry<String, ComponentBlueprint<IManagedState>> componentBlueprint: stateRegisteredComponents.entrySet()) {
            componentsNode.set(componentBlueprint.getKey(), componentBlueprint.getValue().convertToDataNode());
        }
        return root;
    }

}
