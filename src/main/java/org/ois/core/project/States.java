package org.ois.core.project;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import org.ois.core.debug.DevModeState;
import org.ois.core.project.blueprints.StateBlueprint;
import org.ois.core.state.IState;
import org.ois.core.state.managed.IManagedState;
import org.ois.core.utils.ReflectionUtils;
import org.ois.core.utils.io.data.DataBlueprint;
import org.ois.core.utils.io.data.DataNode;
import org.ois.core.utils.io.data.formats.JsonFormat;
import org.ois.core.utils.log.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Map;

public class States {

    private static final Logger<States> log = Logger.get(States.class);
    public static final String LOG_TOPIC = "states";

    /** The directory name inside the simulation directory that holds all the project states manifests files for the simulation **/
    public static final String STATES_DIRECTORY = "states";
    /** The property name for a custom blueprint class in the state blueprint. */
    public final static String BLUEPRINT_CUSTOM_CLASS_PROPERTY = "blueprint-class";

    /** A map that holds blueprints indexed by their state key. */
    private static final Map<String, DataBlueprint<IManagedState>> blueprints = new Hashtable<>();

    /**
     * Retrieves the blueprint for the specified state key.
     *
     * @param key The key of the state.
     * @return The {@code DataBlueprint} associated with the specified state key, or {@code null} if not found.
     */
    public static DataBlueprint<IManagedState> getBlueprint(String key) {
        return blueprints.get(key);
    }

    private static FileHandle getStateDirectory(String stateKey) {
        return Gdx.files.internal(STATES_DIRECTORY).child(stateKey);
    }

    public static Map<String, IState> loadStates(SimulationManifest manifest, boolean devMode) throws ReflectionException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Map<String ,IState> loadedStates = new Hashtable<>();
        for (Map.Entry<String, String> entry : manifest.getStates().entrySet()) {
            IState state = loadState(entry.getKey(), entry.getValue());
            if (devMode) {
                state = new DevModeState(state);
            }
            loadedStates.put(entry.getKey(), state);
            log.debug("State '" + entry.getKey() + "' loaded");
        }
        return loadedStates;
    }

    private static IState loadState(String stateKey, String stateClass) throws ReflectionException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        IState state = createState(stateClass, loadStateBlueprint(stateKey));
        if (!(state instanceof IManagedState)) {
            // Nothing more to do
            return state;
        }
        IManagedState managedState = (IManagedState) state;
        // Set state entities manifests to load when entered
        Entities.setManagerManifest(managedState.getEntityManager(), getStateDirectory(stateKey));
        return managedState;
    }

    private static IState createState(String stateClass, DataBlueprint<IManagedState> blueprint) throws ReflectionException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (blueprint != null) {
            return blueprint.create();
        }
        return ReflectionUtils.newInstance(stateClass);
    }

    /**
     * Loads and deserializes the state blueprint from the provided file.
     *
     * @return The deserialized {@code DataNode} containing the blueprint data.
     */
    private static DataBlueprint<IManagedState> loadStateBlueprint(String stateKey) throws ReflectionException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        FileHandle blueprintFile = getStateDirectory(stateKey).child("state.blueprint.ois");
        if (!blueprintFile.exists() || blueprintFile.isDirectory()) {
            // Nothing to do
            return null;
        }
        byte[] data = blueprintFile.readBytes();
        if (data == null) {
            throw new RuntimeException(String.format("Can't load state blueprint '%s'", blueprintFile));
        }
        String rawData = new String(data);
        // Create state blueprint from data
        DataNode dataNode = JsonFormat.compact().deserialize(rawData);
        DataBlueprint<IManagedState> blueprint = dataNode.contains(BLUEPRINT_CUSTOM_CLASS_PROPERTY) ? ReflectionUtils.newInstance(dataNode.get(BLUEPRINT_CUSTOM_CLASS_PROPERTY).getString()) : new StateBlueprint();
        log.debug(LOG_TOPIC, String.format("Loaded state blueprint: %s", rawData));
        blueprints.put(stateKey, blueprint.loadData(dataNode));
        return blueprint;
    }
}
