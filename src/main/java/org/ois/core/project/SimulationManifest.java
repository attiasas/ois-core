package org.ois.core.project;

import org.ois.core.runner.RunnerConfiguration;
import org.ois.core.utils.io.data.DataNode;
import org.ois.core.utils.io.data.IDataObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the Simulation Manifest, used by the project to configure how the project simulation is set up.
 */
public class SimulationManifest implements IDataObject<SimulationManifest> {
    /** The default name of the project configuration file that will be searched at the root project **/
    public static final String DEFAULT_FILE_NAME = "simulation.ois";
    /** The title of the project **/
    private String title;
    /**
     * The initial state key used when initializing the engine.
     * This string must be equal to one of the state keys.
     */
    private String initialState;
    /**
     * A map of state keys to the class name of the corresponding `IState` implementation.
     * This map must contain at least one entry.
     */
    private Map<String, String> states = new Hashtable<>();
    /** The set of supported platforms for the simulation, based on {@link RunnerConfiguration.RunnerType}. */
    private Set<RunnerConfiguration.RunnerType> platforms = new HashSet<>();
    /** The width of the screen the simulation needs, in pixels. */
    private int screenWidth;
    /** The height of the screen the simulation needs, in pixels. */
    private int screenHeight;

    /**
     * Loads the data from a {@link DataNode} object and populates the fields of this manifest.
     *
     * @param data the {@link DataNode} containing the manifest configuration data.
     * @return the updated {@link SimulationManifest} object.
     */
    @Override
    public <M extends SimulationManifest> M loadData(DataNode data) {
        // Required
        initialState = data.get("initialState").getString();
        states = data.get("states").toStringMap();
        // Optional
        title = data.getProperty("title").getString();
        platforms = data.getProperty("runner","platforms").toStringCollection(new ArrayList<>()).stream().map(RunnerConfiguration::toPlatform).collect(Collectors.toSet());
        screenWidth = data.getProperty("runner","screenWidth").getInt();
        screenHeight = data.getProperty("runner", "screenHeight").getInt();
        return (M) this;
    }

    /**
     * Converts this manifest into a {@link DataNode} object, which can be serialized or saved.
     *
     * @return a {@link DataNode} representing the manifest's configuration.
     */
    @Override
    public DataNode convertToDataNode() {
        DataNode root = DataNode.Object();
        if (title != null && !title.isBlank()) {
            root.set("title", title);
        }
        root.set("initialState", initialState);
        root.set("states", DataNode.Map(states));
        if (screenHeight != 0) {
            root.getProperty("runner","screenWidth").setValue(screenWidth);
        }
        if (screenWidth != 0) {
            root.getProperty("runner","screenHeight").setValue(screenHeight);
        }
        if (platforms != null && !platforms.isEmpty()) {
            root.getProperty("runner").set("platforms", DataNode.Collection(platforms));
        }

        return root;
    }

    // Setters and Getters

    /**
     * Sets the initial state of the project.
     *
     * @param initialState the initial state of the project.
     * @return this {@link SimulationManifest} object.
     */
    public SimulationManifest setInitialState(String initialState) {
        this.initialState = initialState;
        return this;
    }

    /**
     * Sets the states of the simulation.
     *
     * @param states A map of state keys to the class name of the corresponding `IState` implementation.
     * @return this {@link SimulationManifest} object.
     */
    public SimulationManifest setStates(Map<String, String> states) {
        this.states = states;
        return this;
    }

    /**
     * Sets the project title.
     *
     * @param title the project title.
     * @return this {@link SimulationManifest} object.
     */
    public SimulationManifest setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Sets the supported platforms for the simulation.
     *
     * @param platforms a set of {@link RunnerConfiguration.RunnerType} platforms.
     * @return this {@link SimulationManifest} object.
     */
    public SimulationManifest setPlatforms(Set<RunnerConfiguration.RunnerType> platforms) {
        this.platforms = platforms;
        return this;
    }

    /**
     * Sets the screen height in pixels.
     *
     * @param screenHeight the screen height in pixels.
     * @return this {@link SimulationManifest} object.
     */
    public SimulationManifest setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
        return this;
    }

    /**
     * Sets the screen width in pixels.
     *
     * @param screenWidth the screen width in pixels.
     * @return this {@link SimulationManifest} object.
     */
    public SimulationManifest setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
        return this;
    }

    /**
     * Gets the project title.
     *
     * @return the title of the project.
     */
    public String getTitle() { return title; }

    /**
     * Gets the initial state key that will be used during the simulation.
     *
     * @return the initial state key.
     */
    public String getInitialState() {
        return this.initialState;
    }

    /**
     * Gets the map of state keys to class names of `IState` implementations.
     *
     * @return a map of state keys and class names.
     */
    public Map<String, String> getStates() {
        return this.states;
    }

    /**
     * Gets the screen height in pixels.
     *
     * @return the screen height in pixels.
     */
    public int getScreenHeight() { return screenHeight; }

    /**
     * Gets the screen width in pixels.
     *
     * @return the screen width in pixels.
     */
    public int getScreenWidth() { return screenWidth; }

    /**
     * Gets the set of platforms the simulation project supports.
     *
     * @return a set of {@link RunnerConfiguration.RunnerType} platforms.
     */
    public Set<RunnerConfiguration.RunnerType> getPlatforms() { return platforms; }
}
