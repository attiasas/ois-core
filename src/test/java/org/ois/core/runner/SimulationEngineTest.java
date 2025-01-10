package org.ois.core.runner;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import org.ois.core.project.SimulationManifest;
import org.ois.core.state.ErrorState;
import org.ois.core.state.IState;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.Assert;

import java.util.Map;

public class SimulationEngineTest {

    private RunnerConfiguration configuration;
    private SimulationEngine engine;

    @BeforeMethod
    public void setup() {
        Gdx.graphics = new MockGraphics();
        // Setup real objects
        configuration = new RunnerConfiguration();
        engine = new SimulationEngine(configuration);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testLoadProjectThrowsExceptionWhenManifestNotLoaded() throws Exception {
        // Test loading project without a manifest, which should throw an exception
        engine.loadProject();
    }

    @Test
    public void testCreateInitializesStateManagerNoStates() {
        // Test the creation of the engine and initialization of state manager
        engine.create();
        Assert.assertNotNull(engine.stateManager, "StateManager should be initialized.");
        // No states - Error state should be active and store loadProject exception
        Assert.assertNotNull(engine.errorState, "ErrorState should be initialized.");
        Assert.assertTrue(engine.errorState.isActive());
    }

    @Test
    public void testLoadProjectLoadsManifestAndStartsInitialState() throws Exception {
        // Create a dummy manifest and configuration
        SimulationManifest manifest = new SimulationManifest();
        manifest.setInitialState("state1");
        manifest.setStates(Map.of("state1", "org.ois.core.state.ErrorState"));
        configuration.setSimulationManifest(manifest);

        // Initialize OIS, set up necessary variables and Call loadProject
        engine.create();
        // Check if the states are loaded
        Assert.assertTrue(engine.stateManager.states().contains("state1"), "State 'state1' should be loaded.");
        // Load only takes affect after update
        Assert.assertNull(engine.stateManager.getCurrentState());
        Assert.assertTrue(engine.stateManager.update(0));
        // Make sure it was created with reflection and entered the state
        Assert.assertTrue(((ErrorState)engine.stateManager.getCurrentState()).isActive());
    }

    @Test
    public void testRenderWithActiveState() {
        // Test rendering when there is an active state
        SimulationManifest manifest = new SimulationManifest();
        manifest.setInitialState("mockState");
        manifest.setStates(Map.of("mockState", MockState.class.getName()));
        configuration.setSimulationManifest(manifest);

        engine.create();

        engine.render(); // Should not throw an exception
        Assert.assertFalse(engine.errorState.isActive(), "ErrorState should not be active during rendering with a valid state.");

        // Check MockState properties
        MockState currentState = (MockState) engine.stateManager.getCurrentState();
        Assert.assertTrue(currentState.isEntered(), "MockState should have been entered.");
        Assert.assertFalse(currentState.isExited(), "MockState should not have exited yet.");
    }

    @Test
    public void testRenderWithActiveStateException() {
        // Test rendering when there is an active state
        SimulationManifest manifest = new SimulationManifest();
        manifest.setInitialState("mockState");
        manifest.setStates(Map.of("mockState", MockState.class.getName()));
        configuration.setSimulationManifest(manifest);

        engine.create();

        // Load only takes affect after update
        Assert.assertNull(engine.stateManager.getCurrentState());
        try {
            Assert.assertTrue(engine.stateManager.update(0));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        MockState currentState = (MockState) engine.stateManager.getCurrentState();
        currentState.throwErr = true;

        engine.render(); // Should not throw an exception
        // Check changes
        Assert.assertNull(engine.stateManager.getCurrentState());
        Assert.assertTrue(engine.errorState.isActive(), "ErrorState should be the active after rendering with a failed state.");
    }

    @Test
    public void testResizeWithoutErrorState() {
        // Test resizing without an active error state
        engine.stateManager.registerState("mockState", new MockState());
        engine.stateManager.start("mockState");

        // Load only takes affect after update
        Assert.assertNull(engine.stateManager.getCurrentState());
        try {
            Assert.assertTrue(engine.stateManager.update(0));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        engine.resize(800, 600); // Resize without error state
        Assert.assertFalse(engine.errorState.isActive(), "ErrorState should not be active during resize without an exception.");

        // Check if resize was called in MockState
        MockState currentState = (MockState) engine.stateManager.getCurrentState();
        Assert.assertTrue(currentState.isResized(), "MockState should have processed resize.");
    }

    @Test
    public void testDisposeCallsDisposeOnStates() {
        // Test that dispose is called on both the state manager and error state
        engine.stateManager.registerState("mockState", new MockState());
        engine.stateManager.start("mockState");

        // Load only takes affect after update
        Assert.assertNull(engine.stateManager.getCurrentState());
        try {
            Assert.assertTrue(engine.stateManager.update(0));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        engine.dispose();
        Assert.assertFalse(engine.errorState.isActive(), "ErrorState should be disposed and inactive.");

        // Check if dispose was called in MockState
        Assert.assertNull(engine.stateManager.getCurrentState());
        MockState state = (MockState) engine.stateManager.getState("mockState");
        Assert.assertTrue(state.isExited(), "MockState should have exit.");
        Assert.assertTrue(state.isDisposed(), "MockState should have been disposed.");
    }

    @Test
    public void testHandleProgramExceptionActivatesErrorState() {
        // Test if the error state gets activated after an exception
        Exception exception = new Exception("Test exception");
        engine.handleProgramException(exception);
        Assert.assertTrue(engine.errorState.isActive(), "ErrorState should be active after handling an exception.");
    }

    public static class MockState implements IState {
        private boolean entered;
        private boolean exited;
        private boolean resized;
        private boolean disposed;

        private boolean throwErr;

        @Override
        public void enter(Object... parameters) {
            entered = true;
        }

        @Override
        public void exit() {
            exited = true;
        }

        @Override
        public void pause() { }

        @Override
        public void resume() { }

        @Override
        public void resize(int width, int height) {
            resized = true;
        }

        @Override
        public void render() {
            if (throwErr) {
                throw new RuntimeException("some err");
            }
        }

        @Override
        public boolean update(float dt) { return true; }

        @Override
        public void dispose() {
            disposed = true;
        }

        public boolean isEntered() {
            return entered;
        }

        public boolean isExited() {
            return exited;
        }

        public boolean isResized() {
            return resized;
        }

        public boolean isDisposed() {
            return disposed;
        }
    }

    public static class MockGraphics implements Graphics {
        @Override
        public boolean isGL30Available() {return false;}
        @Override
        public boolean isGL31Available() {return false;}
        @Override
        public boolean isGL32Available() {return false;}
        @Override
        public GL20 getGL20() {return null;}
        @Override
        public GL30 getGL30() {return null;}
        @Override
        public GL31 getGL31() {return null;}
        @Override
        public GL32 getGL32() {return null;}
        @Override
        public void setGL20(GL20 gl20) {}
        @Override
        public void setGL30(GL30 gl30) {}
        @Override
        public void setGL31(GL31 gl31) {}
        @Override
        public void setGL32(GL32 gl32) {}
        @Override
        public int getWidth() {return 0;}
        @Override
        public int getHeight() {return 0;}
        @Override
        public int getBackBufferWidth() {return 0;}
        @Override
        public int getBackBufferHeight() {return 0;}
        @Override
        public float getBackBufferScale() {return 0;}
        @Override
        public int getSafeInsetLeft() {return 0;}
        @Override
        public int getSafeInsetTop() {return 0;}
        @Override
        public int getSafeInsetBottom() {return 0;}
        @Override
        public int getSafeInsetRight() {return 0;}
        @Override
        public long getFrameId() {return 0;}
        @Override
        public float getDeltaTime() {return 0;}
        @Override
        public float getRawDeltaTime() {return 0;}
        @Override
        public int getFramesPerSecond() {return 0;}
        @Override
        public GraphicsType getType() {return null;}
        @Override
        public GLVersion getGLVersion() {return null;}
        @Override
        public float getPpiX() {return 0;}
        @Override
        public float getPpiY() {return 0;}
        @Override
        public float getPpcX() {return 0;}
        @Override
        public float getPpcY() {return 0;}
        @Override
        public float getDensity() {return 0;}
        @Override
        public boolean supportsDisplayModeChange() {return false;}
        @Override
        public Monitor getPrimaryMonitor() {return null;}
        @Override
        public Monitor getMonitor() {return null;}
        @Override
        public Monitor[] getMonitors() {return new Monitor[0];}
        @Override
        public DisplayMode[] getDisplayModes() {return new DisplayMode[0];}
        @Override
        public DisplayMode[] getDisplayModes(Monitor monitor) {return new DisplayMode[0];}
        @Override
        public DisplayMode getDisplayMode() {return null;}
        @Override
        public DisplayMode getDisplayMode(Monitor monitor) {return null;}
        @Override
        public boolean setFullscreenMode(DisplayMode displayMode) {return false;}
        @Override
        public boolean setWindowedMode(int width, int height) {return false;}
        @Override
        public void setTitle(String title) {}
        @Override
        public void setUndecorated(boolean undecorated) {}
        @Override
        public void setResizable(boolean resizable) {}
        @Override
        public void setVSync(boolean vsync) {}
        @Override
        public void setForegroundFPS(int fps) {}
        @Override
        public BufferFormat getBufferFormat() {return null;}
        @Override
        public boolean supportsExtension(String extension) {return false;}
        @Override
        public void setContinuousRendering(boolean isContinuous) {}
        @Override
        public boolean isContinuousRendering() {return false;}
        @Override
        public void requestRendering() {}
        @Override
        public boolean isFullscreen() {return false;}
        @Override
        public Cursor newCursor(Pixmap pixmap, int xHotspot, int yHotspot) {return null;}
        @Override
        public void setCursor(Cursor cursor) {}
        @Override
        public void setSystemCursor(Cursor.SystemCursor systemCursor) {}
    }
}
