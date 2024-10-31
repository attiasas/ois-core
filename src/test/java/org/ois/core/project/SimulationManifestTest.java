package org.ois.core.project;

import org.ois.core.runner.RunnerConfiguration;

import org.ois.core.utils.io.data.DataNode;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SimulationManifestTest {
    private SimulationManifest manifest;

    @BeforeMethod
    public void setUp() {
        manifest = new SimulationManifest();
    }

    @Test
    public void testLoadData_withRequiredFields() {
        // Arrange
        DataNode dataNode = DataNode.Object();
        dataNode.set("initialState", "StartState");
        dataNode.set("states", DataNode.Map(Map.of("StartState", "StartClass")));

        // Act
        manifest.loadData(dataNode);

        // Assert
        assertEquals(manifest.getInitialState(), "StartState");
        assertEquals(manifest.getStates(), Map.of("StartState", "StartClass"));
    }

    @Test
    public void testLoadData_withOptionalFields() {
        // Arrange
        DataNode dataNode = DataNode.Object();
        dataNode.set("initialState", "StartState");
        dataNode.set("states", DataNode.Map(Map.of("StartState", "StartClass")));
        dataNode.set("title", "Simulation Title");
        dataNode.getProperty("runner", "screenWidth").setValue(1920);
        dataNode.getProperty("runner", "screenHeight").setValue(1080);

        Set<String> platformSet = new HashSet<>();
        platformSet.add("Html");
        dataNode.getProperty("runner").set("platforms", DataNode.Collection(platformSet));

        // Act
        manifest.loadData(dataNode);

        // Assert
        assertEquals(manifest.getInitialState(), "StartState");
        assertEquals(manifest.getStates(), Map.of("StartState", "StartClass"));
        assertEquals(manifest.getTitle(), "Simulation Title");
        assertEquals(manifest.getScreenWidth(), 1920);
        assertEquals(manifest.getScreenHeight(), 1080);
        assertEquals(manifest.getPlatforms(), Set.of(RunnerConfiguration.RunnerType.Html));
    }

    @Test
    public void testConvertToDataNode() {
        // Arrange
        manifest.setInitialState("StartState");
        manifest.setStates(Map.of("StartState", "StartClass"));
        manifest.setTitle("Simulation Title");
        manifest.setScreenWidth(1920);
        manifest.setScreenHeight(1080);
        manifest.setPlatforms(Set.of(RunnerConfiguration.RunnerType.Html));

        // Act
        DataNode result = manifest.convertToDataNode();

        // Assert
        assertNotNull(result.get("title"));
        assertEquals(result.get("title").getString(), "Simulation Title");
        assertNotNull(result.get("initialState"));
        assertEquals(result.get("initialState").getString(), "StartState");
        assertNotNull(result.get("states"));
        assertEquals(result.get("states").toStringMap(), Map.of("StartState", "StartClass"));
        assertEquals(result.getProperty("runner", "screenWidth").getInt(), 1920);
        assertEquals(result.getProperty("runner", "screenHeight").getInt(), 1080);
        assertEquals(result.getProperty("runner", "platforms").toStringCollection(new HashSet<>()), Set.of("Html"));
    }
}
