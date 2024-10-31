package org.ois.core.state;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.util.Stack;

public class StateManagerTest {

    private StateManager stateManager;
    private TestState initialState;
    private TestState secondState;
    private ExceptionState exceptionState;

    @BeforeMethod
    public void setUp() {
        stateManager = new StateManager();
        initialState = new TestState("initial");
        secondState = new TestState("second");
        exceptionState = new ExceptionState("exceptionState");
    }

    @Test
    public void testRegisterState() {
        stateManager.registerState("initial", initialState);
        assertTrue(stateManager.states().contains("initial"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRegisterStateWithDuplicateKey() {
        stateManager.registerState("initial", initialState);
        stateManager.registerState("initial", new TestState("duplicate"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRegisterStateWithNullKeyOrState() {
        stateManager.registerState(null, initialState); // should throw exception
    }

    @Test
    public void testStartState() {
        stateManager.registerState("initial", initialState);
        stateManager.start("initial");
        assertEquals(stateManager.getCurrentState(), initialState);
        assertTrue(initialState.hasEntered);
    }

    @Test
    public void testChangeState() {
        stateManager.registerState("initial", initialState);
        stateManager.registerState("second", secondState);

        stateManager.start("initial");
        assertFalse(secondState.hasEntered);

        stateManager.changeState("second");
        assertEquals(stateManager.getCurrentState(), secondState);
        assertTrue(secondState.hasEntered);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testChangeToUnknownState() {
        stateManager.changeState("unknown"); // should throw an exception
    }

    @Test
    public void testExitCurrentStateOnChange() {
        stateManager.registerState("initial", initialState);
        stateManager.registerState("second", secondState);

        stateManager.start("initial");
        stateManager.changeState("second");

        assertTrue(initialState.hasExited);
        assertTrue(secondState.hasEntered);
        assertTrue(stateManager.hasActiveState());
    }

    @Test
    public void testUpdateCurrentState() throws Exception {
        stateManager.registerState("initial", initialState);
        stateManager.start("initial");

        assertTrue(stateManager.update(0.1f));
        assertTrue(initialState.hasUpdated);
    }

    @Test
    public void testRenderCurrentState() throws Exception {
        stateManager.registerState("initial", initialState);
        stateManager.start("initial");

        stateManager.render();
        assertTrue(initialState.hasRendered);
    }

    @Test
    public void testPauseResumeState() throws Exception {
        stateManager.registerState("initial", initialState);
        stateManager.start("initial");

        stateManager.pause();
        assertTrue(initialState.hasPaused);

        stateManager.resume();
        assertTrue(initialState.hasResumed);
    }

    @Test
    public void testResizeState() throws Exception {
        stateManager.registerState("initial", initialState);
        stateManager.start("initial");

        stateManager.resize(800, 600);
        assertEquals(initialState.width, 800);
        assertEquals(initialState.height, 600);
    }

    @Test
    public void testDisposeState() throws Exception {
        stateManager.registerState("initial", initialState);
        stateManager.start("initial");

        stateManager.dispose();
        assertTrue(initialState.hasExited);
        assertTrue(initialState.hasDisposed);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testEnterStateThrowsException() {
        // Simulate exception during state enter
        exceptionState.throwOnEnter = true;

        stateManager.registerState("exception", exceptionState);

        // This should throw a RuntimeException due to exception in state enter
        stateManager.start("exception");
    }

    @Test(expectedExceptions = Exception.class)
    public void testUpdateStateThrowsException() throws Exception {
        // Simulate exception during state update
        exceptionState.throwOnUpdate = true;

        stateManager.registerState("exception", exceptionState);
        stateManager.start("exception");

        // This should throw a RuntimeException due to exception in state update
        stateManager.update(0.1f);
    }

    @Test(expectedExceptions = Exception.class)
    public void testRenderStateThrowsException() throws Exception {
        // Simulate exception during state render
        exceptionState.throwOnRender = true;

        stateManager.registerState("exception", exceptionState);
        stateManager.start("exception");

        // This should throw a RuntimeException due to exception in state render
        stateManager.render();
    }

    // Mock test class representing IState with exception throwing behavior
    private static class ExceptionState implements IState {
        private final String name;
        boolean throwOnEnter = false;
        boolean throwOnUpdate = false;
        boolean throwOnRender = false;

        public ExceptionState(String name) {
            this.name = name;
        }

        @Override
        public void enter(Object... params) {
            if (throwOnEnter) {
                throw new RuntimeException("Exception in enter state");
            }
        }

        @Override
        public void exit() {
            // Exit logic
        }

        @Override
        public boolean update(float delta) {
            if (throwOnUpdate) {
                throw new RuntimeException("Exception in update state");
            }
            return true;
        }

        @Override
        public void render() {
            if (throwOnRender) {
                throw new RuntimeException("Exception in render state");
            }
        }

        @Override
        public void pause() {
            // Pause logic
        }

        @Override
        public void resume() {
            // Resume logic
        }

        @Override
        public void resize(int width, int height) {
            // Resize logic
        }

        @Override
        public void dispose() {
            // Dispose logic
        }
    }

    // Mock test class representing IState
    private static class TestState implements IState {
        private final String name;
        boolean hasEntered = false;
        boolean hasExited = false;
        boolean hasUpdated = false;
        boolean hasRendered = false;
        boolean hasPaused = false;
        boolean hasResumed = false;
        boolean hasDisposed = false;
        int width, height;

        public TestState(String name) {
            this.name = name;
        }

        @Override
        public void enter(Object... params) {
            hasEntered = true;
        }

        @Override
        public void exit() {
            hasExited = true;
        }

        @Override
        public boolean update(float delta) {
            hasUpdated = true;
            return true;
        }

        @Override
        public void render() {
            hasRendered = true;
        }

        @Override
        public void pause() {
            hasPaused = true;
        }

        @Override
        public void resume() {
            hasResumed = true;
        }

        @Override
        public void resize(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public void dispose() {
            hasDisposed = true;
        }
    }

}
