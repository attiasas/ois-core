package org.ois.core.runner;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import org.ois.core.OIS;
import org.ois.core.debug.DebugManager;
import org.ois.core.project.Entities;
import org.ois.core.project.SimulationManifest;
import org.ois.core.project.States;
import org.ois.core.state.ErrorState;
import org.ois.core.state.IState;
import org.ois.core.state.StateManager;
import org.ois.core.utils.io.data.formats.JsonFormat;
import org.ois.core.utils.log.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * The main simulation engine for the OIS project.
 * This class manages the application's lifecycle, including loading and managing
 * the states of the simulation.
 */
public class SimulationEngine extends ApplicationAdapter {
    private static final Logger<SimulationEngine> log = Logger.get(SimulationEngine.class);

    /** The Gdx application **/
    private Application app;
    /** The engine runner configuration with information from the dynamic project (Graphic, Meta-data...) **/
    private final RunnerConfiguration configuration;
    /** The state manager that handles the states of the simulations provided by the project; **/
    public final StateManager stateManager;
    /** The Error state, if the stateManager throws an error, the engine will switch to this state **/
    public final ErrorState errorState;

    // Debug mode
    private DebugManager debugManager;

    /**
     * Constructs a new SimulationEngine with the specified configuration.
     *
     * @param configuration The configuration to be used by the simulation engine.
     */
    public SimulationEngine(RunnerConfiguration configuration) {
        this.configuration = configuration;
        this.stateManager = new StateManager();
        this.errorState = new ErrorState();
    }

    /**
     * Retrieves the runner configuration for this engine.
     *
     * @return The runner configuration.
     */
    public RunnerConfiguration getRunnerConfig() {
        return this.configuration;
    }

    @Override
    public void create() {
        this.app = Gdx.app;

        OIS.engine = this;
        OIS.stateManager = this.stateManager;

        Logger.setLogLevel(this.configuration.getLogLevel());
        Logger.setTopics(this.configuration.getLogTopics());

        try {
            if (configuration.getDebugMode()) {
                debugManager = new DebugManager(configuration.getDevModeDir());
                log.info("Engine running project in debug-mode (dev=%b)", debugManager.isDevMode());
            }
            loadProject();
        } catch (Exception e) {
            handleProgramException(new RuntimeException("Can't initialize engine", e));
        }
    }

    /**
     * Loads the project manifest if needed and initializes the project states using reflection from the manifest.
     *
     * @throws ReflectionException if there is an error during reflection.
     * @throws InvocationTargetException if a method cannot be invoked.
     * @throws NoSuchMethodException if the method to invoke cannot be found.
     * @throws InstantiationException if an instance cannot be created.
     * @throws IllegalAccessException if access to the method or constructor is denied.
     */
    public void loadProject() throws ReflectionException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        // Get/Load project manifest
        SimulationManifest manifest = getSimulationManifest();
        // Load project data
        log.debug("Loading entities blueprints if exists");
        Entities.loadBlueprints();
        log.info("Loading Project states to manager");
        Map<String, IState> states = States.loadStates(manifest, debugManager != null && debugManager.isDevMode());
        for (Map.Entry<String, IState> state : states.entrySet()) {
            this.stateManager.registerState(state.getKey(), state.getValue());
        }
        log.info("-- Loading completed --");
        this.stateManager.start(manifest.getInitialState());
    }

    private SimulationManifest getSimulationManifest() {
        SimulationManifest manifest = configuration.getSimulationManifest();
        if (manifest != null) {
            return manifest;
        }
        // For HTML, at Launcher we don't have access to resources.
        // This is the first time after the resources are available.
        log.info("Loading Project Manifest");
        byte[] data = Gdx.files.internal(SimulationManifest.DEFAULT_FILE_NAME).readBytes();
        if (data == null) {
            throw new RuntimeException("Can't load project manifest");
        }
        log.debug("Manifest:\n" + new String(data));
        configuration.setSimulationManifest(JsonFormat.compact().load(new SimulationManifest(), data));
        return configuration.getSimulationManifest();
    }

    /**
     * Stops the application gracefully.
     */
    public void stop() {
        if (this.app == null) {
            return;
        }
        this.app.exit();
    }

    @Override
    public void render() {
        try {
            OIS.deltaTime = Gdx.graphics.getDeltaTime();
            // If error occur, update and render Error-State
            if (errorState.isActive() && errorState.update(OIS.deltaTime)) {
                errorState.render();
                return;
            }
            // Update and render current state
            if (stateManager.update(OIS.deltaTime)) {
                stateManager.render();
                if (debugManager != null) {
                    // Render debug information on the screen
                    debugManager.render();
                }
            } else {
                // No Active states
                stop();
            }
        } catch (Exception e) {
            handleProgramException(e);
        }
    }

    @Override
    public void pause() {
        try {
            if (errorState.isActive()) {
                errorState.pause();
                return;
            }
            this.stateManager.pause();
        } catch (Exception e) {
            handleProgramException(e);
        }
    }

    @Override
    public void resume() {
        try {
            if (errorState.isActive()) {
                errorState.resume();
                return;
            }
            this.stateManager.resume();
        } catch (Exception e) {
            handleProgramException(e);
        }
    }

    @Override
    public void resize(int width, int height) {
        try {
            if (errorState.isActive()) {
                errorState.resize(width, height);
                return;
            }
            this.stateManager.resize(width, height);
            if (debugManager != null) {
                debugManager.resize(width, height);
            }
        } catch (Exception e) {
            handleProgramException(e);
        }
    }

    @Override
    public void dispose() {
        this.stateManager.dispose();
        this.errorState.dispose();
        if (debugManager != null) {
            debugManager.dispose();
        }
    }

    /**
     * Retrieves the current application width in pixel.
     *
     * @return The width of the application.
     */
    public int getAppWidth()
    {
        return Gdx.graphics.getWidth();
    }

    /**
     * Retrieves the current application height in pixel.
     *
     * @return The height of the application.
     */
    public int getAppHeight()
    {
        return Gdx.graphics.getHeight();
    }

    /**
     * Handles exceptions that occur during the execution of the program.
     *
     * @param exception The exception to handle.
     */
    public void handleProgramException(Exception exception) {
        if (errorState.isActive()) {
            stop();
            return;
        }
        errorState.enter(exception);
    }
}
