package org.ois.core;

import org.ois.core.runner.SimulationEngine;
import org.ois.core.state.StateManager;

/**
 * Static access to OIS utilities and shared objects
 */
public class OIS {
    /** The active OIS engine that runs the simulation. **/
    public static SimulationEngine engine;
    /** The State managers of the simulation **/
    public static StateManager stateManager;
    /** The current delta time that passed from the last frame **/
    public static float deltaTime;
}
