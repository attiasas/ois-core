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
import org.ois.core.state.StateManager;
import org.ois.core.utils.ReflectionUtils;
import org.ois.core.utils.io.data.formats.JsonFormat;
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

    public SimulationEngine(RunnerConfiguration configuration) {
        this.configuration = configuration;
        this.stateManager = new StateManager();
    }

    private SpriteBatch batch;
    private Texture image;

    public RunnerConfiguration getRunnerConfig() {
        return this.configuration;
    }

    @Override
    public void create() {
        try {
            this.app = Gdx.app;
            OIS.engine = this;

            loadProject();
        } catch (Exception e) {
            log.error("Can't initialize engine", e);
            throw new RuntimeException(e);
        }

        if (!this.stateManager.states().isEmpty()) {
            return;
        }
        batch = new SpriteBatch();
        image = new Texture("testimage.png");
    }

    private void loadProject() throws ReflectionException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        log.info("Loading Project states to manager");
        SimulationManifest manifest = JsonFormat.compact().load(new SimulationManifest(), Gdx.files.internal(SimulationManifest.DEFAULT_FILE_NAME).readBytes());
        if (manifest != null) {
            for (Map.Entry<String, String> entry : manifest.getStates().entrySet()) {
                this.stateManager.registerState(entry.getKey(), ReflectionUtils.newInstance(entry.getValue()));
                log.error("State '" + entry.getKey() + "' loaded");
            }
            this.stateManager.start(manifest.getInitialState());
            return;
        }
        if (this.stateManager.states().isEmpty()) {
            log.error("log state fail, fallback to backup");
            this.stateManager.registerState("Blue", ReflectionUtils.newInstance("org.ois.example.BlueState"));
            this.stateManager.registerState("Green", ReflectionUtils.newInstance("org.ois.example.GreenState"));
            this.stateManager.registerState("Red", ReflectionUtils.newInstance("org.ois.example.RedState"));
            this.stateManager.start("Red");
        }
    }

    public void stop() {
        this.app.exit();
    }

    @Override
    public void render() {
        try {
            float dt = Gdx.graphics.getDeltaTime();
            if (stateManager.update(dt)) {
                stateManager.render();
            }
        } catch (Exception e) {
            handleProgramException(e);
        }

        if (!this.stateManager.states().isEmpty()) {
            return;
        }
        ScreenUtils.clear(1f, 1f, 1f, 1f);
        batch.begin();
        batch.draw(image, 140, 210);
        batch.end();
    }

    @Override
    public void pause() {
        try {
            this.stateManager.pause();
        } catch (Exception e) {
            handleProgramException(e);
        }
    }

    @Override
    public void resume() {
        try {
            this.stateManager.resume();
        } catch (Exception e) {
            handleProgramException(e);
        }
    }

    @Override
    public void resize(int width, int height) {
        try {
            this.stateManager.resize(width, height);
        } catch (Exception e) {
            handleProgramException(e);
        }
    }

    @Override
    public void dispose() {
        this.stateManager.dispose();
        if (!this.stateManager.states().isEmpty()) {
            return;
        }
        batch.dispose();
        image.dispose();
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
        stop();
        throw new RuntimeException(exception);
    }
}
