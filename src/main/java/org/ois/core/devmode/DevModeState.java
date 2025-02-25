package org.ois.core.devmode;

import org.ois.core.state.IState;

public class DevModeState implements IState {
    private final IState userState;

    public DevModeState(IState userState) {
        this.userState = userState;
    }

    @Override
    public void enter(Object... parameters) {
        userState.enter(parameters);
    }

    @Override
    public void exit() {
        userState.exit();
    }

    @Override
    public void pause() {
        userState.pause();
    }

    @Override
    public void resume() {
        userState.resume();
    }

    @Override
    public void resize(int width, int height) {
        userState.resize(width, height);
    }

    @Override
    public void render() {
        userState.render();
    }

    @Override
    public boolean update(float dt) {
        return userState.update(dt);
    }

    @Override
    public void dispose() {
        userState.dispose();
    }
}
