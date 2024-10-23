package org.ois.core.state;

import org.ois.core.utils.log.Logger;

import java.util.*;

public class StateManager {
    private static final Logger<StateManager> log = Logger.get(StateManager.class);
    // All known states.
    private final Map<String, IState> states = new HashMap<>();
    // Active state stack by keys
    private final Stack<String> stateStack = new Stack<>();

    // Container management

    public void registerState(String key, IState state) {
        if (key == null || state == null) {
            throw new IllegalArgumentException("Can't add null values (key: '" + key + "', state: " + state + ")");
        }
        if (states.containsKey(key)) {
            throw new IllegalArgumentException("State with key '" + key + "' already exists.");
        }
        log.info("Adding state '" + key + "' <" + state.getClass() + "> into the manager.");
        this.states.put(key, state);
    }

    public void start(String initialState) {
        changeState(initialState);
    }

    public void changeState(String key, Object... params) {
        if (key == null || !this.states.containsKey(key)) {
            throw new IllegalArgumentException("Can't find state '" + key + "' in the registered states.");
        }
        while (hasActiveState()) {
            exitCurrentState();
        }
        enterState(key, params);
    }

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
        log.info(logMsg);
        try {
            inState.enter(params);
            this.stateStack.push(key);
        } catch (Exception e) {
            log.error("Caught exception trying to enter state '" + key + "'", e);
        }
    }

    private IState exitCurrentState() {
        if (!hasActiveState()) {
            return null;
        }
        String outStateKey = this.stateStack.pop();
        log.info("Exit state '" + outStateKey + "'");
        IState out = states.get(outStateKey);
        out.exit();
        return out;
    }

    // IState interface

    public boolean update(float delta) throws Exception {
        IState current = getCurrentState();
        if (current == null) {
            return false;
        }
        try {
            log.debug("Update current state '" + this.stateStack.peek() + "', delta-time: " + delta);
            if (!current.update(delta)) {
                exitCurrentState();
            }
        } catch (Exception e) {
            handleCurrentStateException("Update", e);
        }
        return hasActiveState();
    }

    public void render() throws Exception {
        IState current = getCurrentState();
        if (current == null) {
            return;
        }
        try {
            log.debug("Render current state '" + this.stateStack.peek() + "'.");
            current.render();
        } catch (Exception e) {
            handleCurrentStateException("Render", e);
        }
    }

    public void pause() throws Exception {
        IState current = getCurrentState();
        if (current == null) {
            return;
        }
        try {
            log.info("Pause current state '" + this.stateStack.peek() + "'.");
            current.pause();
        } catch (Exception e) {
            handleCurrentStateException("Pause", e);
        }
    }

    public void resume() throws Exception {
        IState current = getCurrentState();
        if (current == null) {
            return;
        }
        try {
            log.info("Resume current state '" + this.stateStack.peek() + "'.");
            current.resume();
        } catch (Exception e) {
            handleCurrentStateException("Resume", e);
        }
    }

    public void resize(int width, int height) throws Exception {
        IState current = getCurrentState();
        if (current == null) {
            return;
        }
        try {
            log.debug("Resize current state '" + this.stateStack.peek() + "' to [" + width + ", " + height + "]");
            // TODO: When allowing multiple states at stack, make sure other states are okay
            current.resize(width, height);
        } catch (Exception e) {
            handleCurrentStateException("Resize", e);
        }
    }

    public void dispose()
    {
        while (hasActiveState())
        {
            exitCurrentState();
        }
        for (IState state : states.values())
        {
            try {
                log.info("Dispose state '" + this.stateStack.peek() + "'.");
                state.dispose();
            } catch (Exception e) {
                log.error("[Dispose] Caught exception from the state <" + state.getClass() + ">", e);
            }
        }
    }

    private void handleCurrentStateException(String topic, Exception e) throws Exception {
        String outState = this.stateStack.peek();
        String msg = "[" + topic + "] Caught exception from the current state '" + outState + "'";
        if (exitCurrentState() == null) {
            log.error(msg);
            throw e;
        } else {
            log.error(msg, e);
        }
    }

    // Getters

    public IState getCurrentState() {
        if(!hasActiveState()) {
            return null;
        }
        return this.states.get(this.stateStack.peek());
    }

    public boolean hasActiveState() {
        return !this.stateStack.isEmpty();
    }

    public Set<String> states() {
        return this.states.keySet();
    }
}
