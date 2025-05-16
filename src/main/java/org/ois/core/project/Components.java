package org.ois.core.project;

import com.badlogic.gdx.utils.reflect.ReflectionException;
import org.ois.core.project.blueprints.ComponentBlueprint;
import org.ois.core.utils.ReflectionUtils;
import org.ois.core.utils.io.data.DataNode;
import org.ois.core.utils.io.data.formats.JsonFormat;
import org.ois.core.utils.log.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class Components {
    private static final Logger<Components> log = Logger.get(Components.class);
    public static final String LOG_TOPIC = "components";

    /** The property name to determine if the component is enabled or not. */
    public final static String ENABLE_PROPERTY = "enable";
    /** The property name for a custom blueprint class in the entity blueprint. */
    public final static String BLUEPRINT_CUSTOM_CLASS_PROPERTY = "blueprint-class";
    /** The property name for components in blueprints */
    public final static String COMPONENTS_PROPERTY = "components";

    public static <C> Map<String ,ComponentBlueprint<C>> loadComponentsBlueprints(DataNode data, Set<String> registered) {
        Map<String , ComponentBlueprint<C>> loaded = new Hashtable<>();
        if (!data.contains(Components.COMPONENTS_PROPERTY)) {
            return loaded;
        }
        try {
            return loadBlueprints(data.get(Components.COMPONENTS_PROPERTY), registered);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <C> Map<String ,ComponentBlueprint<C>> loadBlueprints(DataNode componentsBlueprints, Set<String> registered) throws ReflectionException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Map<String , ComponentBlueprint<C>> loaded = new Hashtable<>();

        for (Map.Entry<String,DataNode> componentBlueprint : componentsBlueprints.properties()) {
            String componentKey = componentBlueprint.getKey();
            if (registered != null && registered.contains(componentKey)) {
                log.debug(String.format("custom component '%s' blueprint registered in code, Skip loading", componentKey));
                continue;
            }
            if (loaded.containsKey(componentKey)) {
                throw new RuntimeException(String.format("found duplicated component key '%s'", componentBlueprint.getKey()));
            }
            loaded.put(componentBlueprint.getKey(), loadComponentBlueprint(componentBlueprint.getValue()));
        }

        return loaded;
    }

    public static <C> ComponentBlueprint<C> loadComponentBlueprint(DataNode componentBlueprint) throws ReflectionException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (!componentBlueprint.contains(BLUEPRINT_CUSTOM_CLASS_PROPERTY)) {
            throw new RuntimeException(String.format("component blueprint must contain '%s' property or extended and registered in context (Entity/State blueprints) code", BLUEPRINT_CUSTOM_CLASS_PROPERTY));
        }
        ComponentBlueprint blueprint = ReflectionUtils.newInstance(componentBlueprint.get(BLUEPRINT_CUSTOM_CLASS_PROPERTY).getString());
        log.debug(LOG_TOPIC, "Component Blueprint (%s): %s", blueprint.getClass().getName(), JsonFormat.humanReadable().serialize(componentBlueprint));
        return blueprint;
    }
}
