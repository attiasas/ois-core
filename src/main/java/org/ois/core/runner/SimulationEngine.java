package org.ois.core.runner;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import org.ois.core.OIS;
import org.ois.core.project.SimulationManifest;
import org.ois.core.state.ErrorState;
import org.ois.core.state.StateManager;
import org.ois.core.utils.ReflectionUtils;
import org.ois.core.utils.io.data.formats.JsonFormat;
import org.ois.core.utils.log.ILogger;
import org.ois.core.utils.log.Logger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class SimulationEngine extends ApplicationAdapter {
    private static final Logger<SimulationEngine> log = Logger.get(SimulationEngine.class);

    // The Gdx application
    private Application app;
    // The engine runner configuration with information from the dynamic project (Graphic, Meta-data...)
    private final RunnerConfiguration configuration;

    // The state manager that handles the states of the simulations provided by the project;
    public final StateManager stateManager;
    public final ErrorState errorState;

    public SimulationEngine(RunnerConfiguration configuration) {
        this.configuration = configuration;
        this.stateManager = new StateManager();
        this.errorState = new ErrorState();
    }

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
            loadProject();
        } catch (Exception e) {
            handleProgramException(new RuntimeException("Can't initialize engine", e));
        }
    }

    private void loadProject() throws ReflectionException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
       SimulationManifest manifest = configuration.getSimulationManifest();
       if (manifest == null) {
           // For HTML, at Launcher we don't have access to resources.
           // This is the first time after the resources are available.
            log.info("Loading Project Manifest");
           byte[] data = Gdx.files.internal(SimulationManifest.DEFAULT_FILE_NAME).readBytes();
           if (data == null) {
               throw new RuntimeException("Can't load project manifest");
           }
           log.debug("Manifest:\n" + new String(data));
           configuration.setSimulationManifest(JsonFormat.compact().load(new SimulationManifest(), data));
           manifest = configuration.getSimulationManifest();
       }
        log.info("Loading Project states to manager");
        for (Map.Entry<String, String> entry : manifest.getStates().entrySet()) {
            this.stateManager.registerState(entry.getKey(), ReflectionUtils.newInstance(entry.getValue()));
            log.debug("State '" + entry.getKey() + "' loaded");
        }
        log.info("Loading completed");
        this.stateManager.start(manifest.getInitialState());
    }

    public void stop() {
        this.app.exit();
    }

    @Override
    public void render() {
        try {
            float dt = Gdx.graphics.getDeltaTime();
            if (errorState.isActive() && errorState.update(dt)) {
                errorState.render();
                return;
            }
            if (stateManager.update(dt)) {
                stateManager.render();
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
        } catch (Exception e) {
            handleProgramException(e);
        }
    }

    @Override
    public void dispose() {
        this.stateManager.dispose();
        this.errorState.dispose();
    }

    public int getAppWidth()
    {
        return Gdx.graphics.getWidth();
    }

    public int getAppHeight()
    {
        return Gdx.graphics.getHeight();
    }

    private void handleProgramException(Exception exception) {
        if (errorState.isActive()) {
            stop();
        }
        errorState.enter(exception);
    }
}
