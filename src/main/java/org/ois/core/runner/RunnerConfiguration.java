package org.ois.core.runner;

import org.ois.core.project.SimulationManifest;
import org.ois.core.utils.io.data.formats.JsonFormat;
import org.ois.core.utils.log.ILogger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * The Unified Configuration to be used in the Simulation Engine.
 * Passing the specific runner (platform) variables in a unified way
 */
@SuppressWarnings("unused")
public class RunnerConfiguration {

    // The Supported application running platforms by the runners
    public enum RunnerType {
        Desktop, Html, Android
    }

    public static RunnerType toPlatform(String platform) {
        switch (platform.trim().toLowerCase()) {
            case "html": return RunnerType.Html;
            case "android": return RunnerType.Android;
            case "desktop": return RunnerType.Desktop;
        }
        throw new RuntimeException("Platform '" + platform + "' not supported. Options: " + Arrays.toString(RunnerType.values()));
    }

    private ILogger.Level logLevel;
    private String[] logTopics;
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

    public void setLogTopics(String[] logTopics) {
        this.logTopics = logTopics;
    }

    public void setLogLevel(ILogger.Level logLevel) {
        this.logLevel = logLevel;
    }

    public String[] getLogTopics() {
        return logTopics;
    }

    public ILogger.Level getLogLevel() {
        return logLevel;
    }

    public RunnerType getType() {
        return this.type;
    }

    public SimulationManifest getSimulationManifest() {
        return this.simulationManifest;
    }

    public static RunnerConfiguration getRunnerConfigurations(InputStream simulationConfigFileStream) throws IOException {
        return new RunnerConfiguration().setSimulationManifest(JsonFormat.compact().load(new SimulationManifest(), simulationConfigFileStream));
    }
}
