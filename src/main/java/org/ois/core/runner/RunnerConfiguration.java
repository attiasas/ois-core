package org.ois.core.runner;

import org.ois.core.project.SimulationManifest;

import java.io.IOException;
import java.io.InputStream;

/**
 * The Unified Configuration to be used in the Simulation Engine.
 * Passing the specific runner (platform) variables in a unified way
 */
@SuppressWarnings("unused")
public class RunnerConfiguration {
    // The environment variable, if exists, is used to tell the runner where the project 'simulation' resources directory exists.
    // If not provided, will use the default location of the 'simulation' directory in the project.
//    public static final String ENV_PROJECT_ASSETS_PATH = "OIS_PROJECT_ASSETS_PATH";

    // The Supported application running platforms by the runners
    public enum RunnerType {
        Desktop, Html, Android
    }

    private RunnerType type;
    private SimulationManifest simulationManifest;

    public RunnerConfiguration setType(RunnerType type) {
        this.type = type;
        return this;
    }

    public RunnerConfiguration setSimulationManifest(SimulationManifest manifest) {
        this.simulationManifest = manifest;
        return this;
    }

    public RunnerType getType() {
        return this.type;
    }

    public SimulationManifest getSimulationManifest() {
        return this.simulationManifest;
    }

    public static RunnerConfiguration getRunnerConfigurations(InputStream assetsProjectConfiguration) throws IOException {
        return new RunnerConfiguration();//.setSimulationManifest(SimulationManifest.load(assetsProjectConfiguration));
    }


    // The environment variable, if exists (default value is 'true'), is used to tell the runner that it's running not in production mode.
    public static final String ENV_DEV_MODE = "OIS_RUNNER_DEV_MODE";



    // The environment variable (MANDATORY) tells the runner where the compiled project jar to be used on the runner.
    public static final String ENV_PROJECT_JAR_PATH = "OIS_PROJECT_JAR_PATH";
    // The environment variable, if exists, is used to tell the runner where the android sdk exists
    // If not provided, it will search the value from environment variable named 'ANDROID_HOME';
    public static final String ENV_ANDROID_SDK_PATH = "OIS_RUNNER_ANDROID_SDK_PATH";




}
