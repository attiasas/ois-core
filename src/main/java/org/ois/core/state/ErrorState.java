package org.ois.core.state;

import com.badlogic.gdx.utils.ScreenUtils;
import org.ois.core.utils.log.Logger;

public class ErrorState implements IState {

    private static final Logger<ErrorState> log = Logger.get(ErrorState.class);

//    private final List<Exception> exceptions = new ArrayList<>();
    private boolean isActive = false;

    @Override
    public void enter(Object... parameters) {
        for (Object param : parameters) {
            if (param instanceof Exception) {
                Exception exception = (Exception) param;
                log.error("Caught Simulation Exception",exception);
            }
        }
        isActive = true;
    }

    @Override
    public void exit() {
        isActive = false;
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void render() {
        ScreenUtils.clear(1,1,1, 1f);
    }

    @Override
    public boolean update(float dt) {
        // TODO: any key will make the state exit
        return true;
    }

    @Override
    public void dispose() {

    }
}
