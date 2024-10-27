package org.ois.core.state;

/**
 * The interface representing a state in the simulation.
 * States are used to manage different phases of the simulation, and are the main class to implement by the user.
 * allowing for entry, exit, and various state-specific operations.
 */
public interface IState {
    /**
     * Called when entering the state.
     *
     * @param parameters Optional parameters to configure the state upon entry.
     */
    void enter(Object... parameters);

    /**
     * Called when exiting the state.
     */
    void exit();

    /**
     * Called to pause the state.
     */
    void pause();

    /**
     * Called to resume the state after it has been paused.
     */
    void resume();

    /**
     * Called to resize the state, typically in response to window resizing.
     *
     * @param width  The new width of the state.
     * @param height The new height of the state.
     */
    void resize(int width, int height);

    /**
     * Called to render the state.
     */
    void render();

    /**
     * Called to update the state.
     *
     * @param dt The delta time since the last update.
     * @return True if the state should continue, false otherwise (will cause the state to exit).
     */
    boolean update(float dt);

    /**
     * Called to dispose of resources used by the state.
     */
    void dispose();
}
