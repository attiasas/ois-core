package org.ois.core.runner;

import com.badlogic.gdx.Gdx;
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
        Gdx.graphics = new GdxGraphicsMock();
        Gdx.files = new GdxFilesMock();
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
}
