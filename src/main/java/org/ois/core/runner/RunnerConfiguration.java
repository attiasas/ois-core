package org.ois.core.runner;

import org.ois.core.project.SimulationManifest;
import org.ois.core.utils.io.data.formats.JsonFormat;
import org.ois.core.utils.log.ILogger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * The Unified Configuration to be used in the Simulation Engine.
 * This class provides a way to set and retrieve configuration parameters for
 * running simulations across different platforms.
 */
@SuppressWarnings("unused")
public class RunnerConfiguration {

    /**
     * Enum representing the supported application running platforms by the runners.
     */
    public enum RunnerType {
        Desktop, Html, Android
    }

    /**
     * Converts a string representation of a platform to a {@link RunnerType}.
     *
     * @param platform The string representation of the platform (e.g., "html", "android", "desktop").
     * @return The corresponding {@link RunnerType}.
     * @throws RuntimeException if the platform is not supported.
     */
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

    /**
     * Sets the type of the runner configuration.
     *
     * @param type The runner type to set.
     * @return The current instance of {@link RunnerConfiguration} for method chaining.
     */
    public RunnerConfiguration setType(RunnerType type) {
        this.type = type;
        return this;
    }

    /**
     * Sets the simulation manifest for this runner configuration.
     *
     * @param manifest The simulation manifest to set.
     * @return The current instance of {@link RunnerConfiguration} for method chaining.
     */
    public RunnerConfiguration setSimulationManifest(SimulationManifest manifest) {
        this.simulationManifest = manifest;
        return this;
    }

    /**
     * Sets the log topics for this runner configuration.
     *
     * @param logTopics An array of log topics to set.
     */
    public void setLogTopics(String[] logTopics) {
        this.logTopics = logTopics;
    }

    /**
     * Sets the log level for this runner configuration.
     *
     * @param logLevel The log level to set.
     */
    public void setLogLevel(ILogger.Level logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * Retrieves the log topics set for this runner configuration.
     *
     * @return An array of log topics.
     */
    public String[] getLogTopics() {
        return logTopics;
    }

    /**
     * Retrieves the log level set for this runner configuration.
     *
     * @return The log level.
     */
    public ILogger.Level getLogLevel() {
        return logLevel;
    }

    /**
     * Retrieves the runner type set for this configuration.
     *
     * @return The runner type.
     */
    public RunnerType getType() {
        return this.type;
    }

    /**
     * Retrieves the simulation manifest associated with this runner configuration.
     *
     * @return The simulation manifest.
     */
    public SimulationManifest getSimulationManifest() {
        return this.simulationManifest;
    }

    /**
     * Creates a {@link RunnerConfiguration} instance from a simulation configuration file stream.
     *
     * @param simulationConfigFileStream The input stream of the simulation configuration file.
     * @return A {@link RunnerConfiguration} instance populated with data from the configuration file.
     * @throws IOException if an error occurs while reading the configuration file.
     */
    public static RunnerConfiguration getRunnerConfigurations(InputStream simulationConfigFileStream) throws IOException {
        return new RunnerConfiguration().setSimulationManifest(JsonFormat.compact().load(new SimulationManifest(), simulationConfigFileStream));
    }
}
