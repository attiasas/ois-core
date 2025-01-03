package org.ois.core.state;

import org.ois.core.utils.log.Logger;

import java.util.*;

/**
 * Manages a collection of states within an application, allowing registration, switching,
 * and lifecycle control (enter, update, render, pause, resume, and exit) of states.
 *
 * <p>This class maintains an internal stack of active states and supports changing between states,
 * updating and rendering the active state, and managing state-specific resources. It provides logging
 * functionality to track state transitions and errors during state operations.
 */
public class StateManager {
    private static final Logger<StateManager> log = Logger.get(StateManager.class);
    private static final String LOG_TOPIC = "states";

    /** All known states. **/
    private final Map<String, IState> states = new HashMap<>();
    /** Active state stack by keys **/
    private final Stack<String> stateStack = new Stack<>();

    // Container management

    /**
     * Registers a state with a given key in the StateManager.
     *
     * @param key   the unique key to identify the state
     * @param state the state instance to register
     * @throws IllegalArgumentException if key or state is null, or if the key already exists
     */
    public void registerState(String key, IState state) {
        if (key == null || state == null) {
            throw new IllegalArgumentException("Can't add null values (key: '" + key + "', state: " + state + ")");
        }
        if (states.containsKey(key)) {
            throw new IllegalArgumentException("State with key '" + key + "' already exists.");
        }
        log.info(LOG_TOPIC,"Adding state '" + key + "' <" + state.getClass() + "> into the manager.");
        this.states.put(key, state);
    }

    /**
     * Starts the StateManager with the given initial state.
     *
     * @param initialState the key of the state to start with
     * @throws IllegalArgumentException if the state key does not exist
     */
    public void start(String initialState) {
        log.info(LOG_TOPIC, "Starting StateManager with state '" + initialState + "'");
        changeState(initialState);
    }

    /**
     * Changes the current state to the one identified by the given key, exiting the current state if necessary.
     *
     * @param key    the key of the new state
     * @param params optional parameters to pass to the new state's enter method
     * @throws IllegalArgumentException if the state key is not registered
     */
    public void changeState(String key, Object... params) {
        if (key == null || !this.states.containsKey(key)) {
            throw new IllegalArgumentException("Can't find state '" + key + "' in the registered states.");
        }
        while (hasActiveState()) {
            exitCurrentState();
        }
        enterState(key, params);
    }

    /**
     * Enters a new state by key and passes optional parameters.
     *
     * @param key    the key of the state to enter
     * @param params optional parameters to pass to the state
     * @throws IllegalArgumentException if the state key is not registered
     * @throws IllegalStateException    if the state is already in the active state stack
     * @throws RuntimeException         if there is an error entering the state
     */
    private void enterState(String key, Object... params) {
        if (key == null || !this.states.containsKey(key)) {
            throw new IllegalArgumentException("Can't find state '" + key + "' in the registered states.");
        }
        if (this.stateStack.contains(key)) {
            throw new IllegalStateException("State '" + key + "' already started.");
        }
        IState inState = this.states.get(key);
        String logMsg = "Enter state '" + key + "'";
        if (params.length > 0) {
            logMsg += ", with params: " + Arrays.toString(params);
        }
        log.info(LOG_TOPIC, logMsg);
        try {
            inState.enter(params);
            this.stateStack.push(key);
        } catch (Exception e) {
            throw new RuntimeException("Caught exception trying to enter state '" + key + "'", e);
        }
    }

    /**
     * Exits the current active state, if there is one.
     *
     * @return the exited state, or null if no active state exists
     */
    private IState exitCurrentState() {
        if (!hasActiveState()) {
            return null;
        }
        String outStateKey = this.stateStack.pop();
        log.info(LOG_TOPIC, "Exit state '" + outStateKey + "'");
        IState out = states.get(outStateKey);
        out.exit();
        return out;
    }

    // IState interface

    /**
     * Updates the current active state with the given delta time.
     *
     * @param delta the time since the last update
     * @return true if there is still an active state, false otherwise
     * @throws Exception if an error occurs during the state update
     */
    public boolean update(float delta) throws Exception {
        IState current = getCurrentState();
        if (current == null) {
            return false;
        }
        try {
            log.debug(LOG_TOPIC, "Update current state '" + this.stateStack.peek() + "', delta-time: " + delta);
            if (!current.update(delta)) {
                exitCurrentState();
            }
        } catch (Exception e) {
            handleCurrentStateException("Update", e);
        }
        return hasActiveState();
    }

    /**
     * Renders the current active state.
     *
     * @throws Exception if an error occurs during rendering
     */
    public void render() throws Exception {
        IState current = getCurrentState();
        if (current == null) {
            return;
        }
        try {
            log.debug(LOG_TOPIC, "Render current state '" + this.stateStack.peek() + "'.");
            current.render();
        } catch (Exception e) {
            handleCurrentStateException("Render", e);
        }
    }

    /**
     * Pauses the current active state.
     *
     * @throws Exception if an error occurs during the pause
     */
    public void pause() throws Exception {
        IState current = getCurrentState();
        if (current == null) {
            return;
        }
        try {
            log.info(LOG_TOPIC, "Pause current state '" + this.stateStack.peek() + "'.");
            current.pause();
        } catch (Exception e) {
            handleCurrentStateException("Pause", e);
        }
    }

    /**
     * Resumes the current paused state.
     *
     * @throws Exception if an error occurs during the resume
     */
    public void resume() throws Exception {
        IState current = getCurrentState();
        if (current == null) {
            return;
        }
        try {
            log.info(LOG_TOPIC, "Resume current state '" + this.stateStack.peek() + "'.");
            current.resume();
        } catch (Exception e) {
            handleCurrentStateException("Resume", e);
        }
    }

    /**
     * Resizes the current active state to the given dimensions.
     *
     * @param width  the new width
     * @param height the new height
     * @throws Exception if an error occurs during resizing
     */
    public void resize(int width, int height) throws Exception {
        IState current = getCurrentState();
        if (current == null) {
            return;
        }
        try {
            log.debug(LOG_TOPIC, "Resize current state '" + this.stateStack.peek() + "' to [" + width + ", " + height + "]");
            // TODO: When allowing multiple states at stack, make sure other states are okay
            current.resize(width, height);
        } catch (Exception e) {
            handleCurrentStateException("Resize", e);
        }
    }

    /**
     * Disposes all registered states, clearing the state stack and disposing of each state's resources.
     */
    public void dispose()
    {
        while (hasActiveState())
        {
            exitCurrentState();
        }
        for (Map.Entry<String,IState> state : states.entrySet())
        {
            try {
                log.info(LOG_TOPIC, "Dispose state '" + state.getKey() + "'.");
                state.getValue().dispose();
            } catch (Exception e) {
                log.error("[Dispose] Caught exception from the state <" + state.getClass() + ">", e);
            }
        }
    }

    /**
     * Handles exceptions that occur during state operations, such as update, render, etc.
     *
     * @param topic the state operation during which the exception occurred
     * @param e     the exception
     * @throws Exception if the exception is not handled by the state
     */
    private void handleCurrentStateException(String topic, Exception e) throws Exception {
        if (this.stateStack.isEmpty()) {
            throw e;
        }
        String outState = this.stateStack.peek();
        String msg = "[" + topic + "] Caught exception from the current state '" + outState + "'";
        this.stateStack.pop();
        if (!hasActiveState()) {
            throw new Exception(msg, e);
        } else {
            log.error(msg, e);
        }
    }

    // Getters

    /**
     * Returns the current active state.
     *
     * @return the current state, or null if no active state exists
     */
    public IState getCurrentState() {
        if(!hasActiveState()) {
            return null;
        }
        return this.states.get(this.stateStack.peek());
    }

    /**
     * Checks whether there is an active state in the state stack.
     *
     * @return true if there is an active state, false otherwise
     */
    public boolean hasActiveState() {
        return !this.stateStack.isEmpty();
    }

    /**
    * Returns the set of registered states.
     *
     * @return a set of Strings, the keys of the registered states
    */
    public Set<String> states() {
        return this.states.keySet();
    }

    /**
     * Return a state given its key.
     *
     * @param key - the key id of the state
     * @return a state registered with the key or null if not
     */
    public IState getState(String key) {
        return this.states.get(key);
    }
}
