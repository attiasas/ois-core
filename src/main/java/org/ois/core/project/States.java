package org.ois.core.project;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import org.ois.core.state.IState;
import org.ois.core.state.managed.IManagedState;
import org.ois.core.utils.ReflectionUtils;
import org.ois.core.utils.log.Logger;

import java.lang.reflect.InvocationTargetException;

public class States {

    private static final Logger<States> log = Logger.get(States.class);
    public static final String LOG_TOPIC = "states";

    /** The directory name inside the simulation directory that holds all the project states manifests files for the simulation **/
    public static final String STATES_DIRECTORY = "states";

    public static IState loadState(String stateKey, String stateClass) throws ReflectionException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        IState state = ReflectionUtils.newInstance(stateClass);
        if (!(state instanceof IManagedState)) {
            // Nothing to do
            return state;
        }
        IManagedState managedState = (IManagedState)state;

        managedState.setEntityManager(Entities.loadManager(Gdx.files.internal(STATES_DIRECTORY).child(stateKey)));

        return state;
    }
}
