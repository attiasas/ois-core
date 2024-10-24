package org.ois.core.project;

import org.ois.core.utils.io.data.DataNode;
import org.ois.core.utils.io.data.DataObject;
import org.ois.core.utils.io.data.formats.JsonFormat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class SimulationManifest implements DataObject<SimulationManifest> {
    // THe default name of the project configuration file that will be searched at the root project
    public static final String DEFAULT_FILE_NAME = "simulation.ois";

    // The initial state key that will be used when initializing the engine
    private String initialState;
    // State key -> class name of IState implementation, must have at least one entry
    private Map<String, String> states = new Hashtable<>();

    public String getInitialState() {
        return this.initialState;
    }

    public Map<String, String> getStates() {
        return this.states;
    }

    @Override
    public SimulationManifest loadData(DataNode data) {
        initialState = data.get("initialState").getString();
        states = data.get("states").toStringMap();
        return this;
    }

    @Override
    public DataNode convertToDataNode() {
        DataNode root = DataNode.Object();

        root.set("initialState", initialState);
        root.set("states", DataNode.Map(states));

        return root;
    }

    public static void main(String[] args) throws IOException {
        SimulationManifest manifest = new SimulationManifest();
        manifest.initialState = "testy";
        manifest.states.put("t_[0]", "State1");
        manifest.states.put("t_[1]", "State2");
        manifest.states.put("t_[2]", "State3");

        Path testDir = new File("C:\\Users\\assaf\\OneDrive\\Documents\\code\\ois\\ois-core").toPath();
        try (BufferedWriter writer = Files.newBufferedWriter(testDir.resolve("testManifest.json"), StandardCharsets.UTF_8)) {
            writer.write(JsonFormat.humanReadable().serialize(manifest));
        }
        try (BufferedWriter writer = Files.newBufferedWriter(testDir.resolve("testManifestCompact.json"), StandardCharsets.UTF_8)) {
            writer.write(JsonFormat.compact().serialize(manifest));
        }
        try (InputStream inputStream = Files.newInputStream(testDir.resolve("testManifest.json"))) {
            SimulationManifest other = JsonFormat.compact().load(new SimulationManifest(), inputStream);
            System.out.println("Manifest equals:" + manifest.equals(other));
            try (BufferedWriter writer = Files.newBufferedWriter(testDir.resolve("testManifestAfter.json"), StandardCharsets.UTF_8)) {
                writer.write(JsonFormat.humanReadable().serialize(other));
            }
        }
        try (InputStream inputStream = Files.newInputStream(testDir.resolve("testManifestCompact.json"))) {
            SimulationManifest other = JsonFormat.compact().load(new SimulationManifest(), inputStream);
            System.out.println("testManifestCompact equals:" + manifest.equals(other));
            try (BufferedWriter writer = Files.newBufferedWriter(testDir.resolve("testManifestCompactAfter.json"), StandardCharsets.UTF_8)) {
                writer.write(JsonFormat.compact().serialize(other));
            }
        }


        DataNode node = new DataNode(DataNode.Type.Object);
        node.getProperty("test","what","items").add(1);
        node.getProperty("test","what","items").add(2.5f);
        node.getProperty("test").set("empty_arr", DataNode.Collection(new ArrayList<>()));
        node.getProperty("test").set("arr_one_val", DataNode.Collection(("ava \"afa fef\" ava")));
        node.getProperty("empty_str").setValue("");
        node.set("empty_map", DataNode.Map(new HashMap<>()));
        node.getProperty("other").set("map", DataNode.Map(Map.of("key1","val1")));

        try (BufferedWriter writer = Files.newBufferedWriter(testDir.resolve("testNode.json"), StandardCharsets.UTF_8)) {
            writer.write(JsonFormat.humanReadable().serialize(node));
        }
        try (BufferedWriter writer = Files.newBufferedWriter(testDir.resolve("testNodeCompact.json"), StandardCharsets.UTF_8)) {
            writer.write(JsonFormat.compact().serialize(node));
        }
        try (InputStream inputStream = Files.newInputStream(testDir.resolve("testNode.json"))) {
            DataNode other = JsonFormat.compact().load(inputStream);
            System.out.println("testNode equals:" + node.equals(other));
            try (BufferedWriter writer = Files.newBufferedWriter(testDir.resolve("testNodeAfter.json"), StandardCharsets.UTF_8)) {
                writer.write(JsonFormat.humanReadable().serialize(other));
            }
        }
        try (InputStream inputStream = Files.newInputStream(testDir.resolve("testNodeCompact.json"))) {
            DataNode other = JsonFormat.compact().load(inputStream);
            System.out.println("testNodeCompact equals:" + node.equals(other));
            try (BufferedWriter writer = Files.newBufferedWriter(testDir.resolve("testNodeCompactAfter.json"), StandardCharsets.UTF_8)) {
                writer.write(JsonFormat.compact().serialize(other));
            }
        }

    }
}
