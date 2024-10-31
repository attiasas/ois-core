package org.ois.core.state;

import com.badlogic.gdx.utils.ScreenUtils;
import org.ois.core.utils.log.Logger;

/**
 * Represents the error state in the simulation.
 * This state is activated when the state manager encounters an error,
 * allowing the simulation to handle exceptions gracefully.
 */
public class ErrorState implements IState {

    private static final Logger<ErrorState> log = Logger.get(ErrorState.class);

//    private final List<Exception> exceptions = new ArrayList<>();

    /** Indicates whether the error state is currently active. **/
    private boolean isActive = false;

    /**
     * Checks if the error state is currently active.
     *
     * @return True if the error state is active, false otherwise.
     */
    public boolean isActive() {return isActive;}

    /**
     * Called when entering the error state.
     * Logs any provided exceptions and sets the state to active.
     *
     * @param parameters Optional parameters, typically containing exceptions.
     */
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
    public void exit() {isActive = false;}

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
    public void resize(int width, int height) {}

    @Override
    public void dispose() {isActive = false; }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

}
