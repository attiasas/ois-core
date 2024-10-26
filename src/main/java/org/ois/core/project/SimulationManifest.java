package org.ois.core.project;

import org.ois.core.runner.RunnerConfiguration;
import org.ois.core.utils.io.data.DataNode;
import org.ois.core.utils.io.data.DataObject;

import java.util.*;
import java.util.stream.Collectors;

public class SimulationManifest implements DataObject<SimulationManifest> {
    // THe default name of the project configuration file that will be searched at the root project
    public static final String DEFAULT_FILE_NAME = "simulation.ois";

    private String title;

    // The initial state key that will be used when initializing the engine
    private String initialState;
    // State key -> class name of IState implementation, must have at least one entry
    private Map<String, String> states = new Hashtable<>();

    private Set<RunnerConfiguration.RunnerType> platforms = new HashSet<>();

    private int screenWidth;
    private int screenHeight;

    public SimulationManifest setTitle(String title) {
        this.title = title;
        return this;
    }

    public SimulationManifest setPlatforms(Set<RunnerConfiguration.RunnerType> platforms) {
        this.platforms = platforms;
        return this;
    }

    public SimulationManifest setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
        return this;
    }

    public SimulationManifest setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
        return this;
    }

    public String getTitle() { return title; }

    public String getInitialState() {
        return this.initialState;
    }

    public Map<String, String> getStates() {
        return this.states;
    }

    public int getScreenHeight() { return screenHeight; }

    public int getScreenWidth() { return screenWidth; }

    public Set<RunnerConfiguration.RunnerType> getPlatforms() { return platforms; }

    @Override
    public SimulationManifest loadData(DataNode data) {
        // Required
        initialState = data.get("initialState").getString();
        states = data.get("states").toStringMap();
        // Optional
        title = data.getProperty("title").getString();
        platforms = data.getProperty("runner","platforms").toStringCollection(new ArrayList<>()).stream().map(RunnerConfiguration::toPlatform).collect(Collectors.toSet());
        screenWidth = data.getProperty("runner","screenWidth").getInt();
        screenHeight = data.getProperty("runner", "screenHeight").getInt();
        return this;
    }

    @Override
    public DataNode convertToDataNode() {
        DataNode root = DataNode.Object();

        if (title != null && !title.isBlank()) {
            root.set("title", title);
        }

        root.set("initialState", initialState);
        root.set("states", DataNode.Map(states));

        if (screenHeight != 0) {
            root.getProperty("runner","screenWidth").setValue(screenWidth);
        }
        if (screenWidth != 0) {
            root.getProperty("runner","screenHeight").setValue(screenHeight);
        }
        if (platforms != null && !platforms.isEmpty()) {
            root.getProperty("runner").set("platforms", DataNode.Collection(platforms));
        }

        return root;
    }

//    public static void main(String[] args) throws IOException {
//        SimulationManifest manifest = new SimulationManifest();
//        manifest.initialState = "testy";
//        manifest.states.put("Blue", "org.ois.example.BlueState");
//        manifest.states.put("Green", "org.ois.example.GreenState");
//        manifest.states.put("Red", "org.ois.example.RedState");
//
//        Path testDir = new File("C:\\Users\\assaf\\OneDrive\\Documents\\code\\ois\\ois-core").toPath();
//        try (BufferedWriter writer = Files.newBufferedWriter(testDir.resolve("testManifest.json"), StandardCharsets.UTF_8)) {
//            writer.write(JsonFormat.humanReadable().serialize(manifest));
//        }
//        try (BufferedWriter writer = Files.newBufferedWriter(testDir.resolve("testManifestCompact.json"), StandardCharsets.UTF_8)) {
//            writer.write(JsonFormat.compact().serialize(manifest));
//        }
//        try (InputStream inputStream = Files.newInputStream(testDir.resolve("testManifest.json"))) {
//            SimulationManifest other = JsonFormat.compact().load(new SimulationManifest(), inputStream);
//            System.out.println("Manifest equals:" + manifest.equals(other));
//            try (BufferedWriter writer = Files.newBufferedWriter(testDir.resolve("testManifestAfter.json"), StandardCharsets.UTF_8)) {
//                writer.write(JsonFormat.humanReadable().serialize(other));
//            }
//        }
//        try (InputStream inputStream = Files.newInputStream(testDir.resolve("testManifestCompact.json"))) {
//            SimulationManifest other = JsonFormat.compact().load(new SimulationManifest(), inputStream);
//            System.out.println("testManifestCompact equals:" + manifest.equals(other));
//            try (BufferedWriter writer = Files.newBufferedWriter(testDir.resolve("testManifestCompactAfter.json"), StandardCharsets.UTF_8)) {
//                writer.write(JsonFormat.compact().serialize(other));
//            }
//        }
//
//
//        DataNode node = new DataNode(DataNode.Type.Object);
//        node.getProperty("test","what","items").add(1);
//        node.getProperty("test","what","items").add(2.5f);
//        node.getProperty("test").set("empty_arr", DataNode.Collection(new ArrayList<>()));
//        node.getProperty("test").set("arr_one_val", DataNode.Collection(("ava \"afa fef\" ava")));
//        node.getProperty("empty_str").setValue("");
//        node.set("empty_map", DataNode.Map(new HashMap<>()));
//        node.getProperty("other").set("map", DataNode.Map(Map.of("key1","val1")));
//
//        try (BufferedWriter writer = Files.newBufferedWriter(testDir.resolve("testNode.json"), StandardCharsets.UTF_8)) {
//            writer.write(JsonFormat.humanReadable().serialize(node));
//        }
//        try (BufferedWriter writer = Files.newBufferedWriter(testDir.resolve("testNodeCompact.json"), StandardCharsets.UTF_8)) {
//            writer.write(JsonFormat.compact().serialize(node));
//        }
//        try (InputStream inputStream = Files.newInputStream(testDir.resolve("testNode.json"))) {
//            DataNode other = JsonFormat.compact().load(inputStream);
//            System.out.println("testNode equals:" + node.equals(other));
//            try (BufferedWriter writer = Files.newBufferedWriter(testDir.resolve("testNodeAfter.json"), StandardCharsets.UTF_8)) {
//                writer.write(JsonFormat.humanReadable().serialize(other));
//            }
//        }
//        try (InputStream inputStream = Files.newInputStream(testDir.resolve("testNodeCompact.json"))) {
//            DataNode other = JsonFormat.compact().load(inputStream);
//            System.out.println("testNodeCompact equals:" + node.equals(other));
//            try (BufferedWriter writer = Files.newBufferedWriter(testDir.resolve("testNodeCompactAfter.json"), StandardCharsets.UTF_8)) {
//                writer.write(JsonFormat.compact().serialize(other));
//            }
//        }
//
//    }
}
