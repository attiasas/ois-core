package org.ois.core.project;

//import org.ois.core.utils.io.data.DataNode;
//import org.ois.core.utils.io.data.DataObject;
//import org.ois.core.utils.io.data.formats.JsonFormat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Hashtable;
import java.util.Map;

public class SimulationManifest {//implements DataObject<SimulationManifest> {
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

//    @Override
//    public SimulationManifest loadData(DataNode data) {
//        initialState = data.get("initialState").getString();
//        states = data.get("states").toMap();
//        return this;
//    }
//
//    @Override
//    public DataNode convertToDataNode() {
//        DataNode root = new DataNode();
//
//        root.set("initialState", initialState);
//        root.set("states", DataNode.MapNode(states));
//
//        return root;
//    }
//
//    /**
//     * Load a project configuration object with the values that are stored in the given file
//     * @param path - path to the Json file or directory that contains {@link SimulationManifest#DEFAULT_FILE_NAME} file
//     * @return the project simulation manifest
//     * @throws IOException - on reading the file
//     */
//    public static SimulationManifest load(Path path) throws IOException {
//        Path actualPath = path;
//        if (path.toFile().isDirectory()) {
//            actualPath = path.resolve(DEFAULT_FILE_NAME);
//        }
//        return JsonFormat.HUMAN_READABLE.loadFromFile(new SimulationManifest(), actualPath);
//    }
//
//    public static SimulationManifest load(InputStream in) throws IOException {
//        return JsonFormat.HUMAN_READABLE.load(new SimulationManifest(),in);
//    }
//
//    public void save(Path destination) throws IOException {
//        JsonFormat.HUMAN_READABLE.writeToFile(this,destination);
//    }


//    public static void main(String[] args) throws IOException {
//        SimulationManifest manifest = new SimulationManifest();
//        manifest.initialState = "testy";
//        manifest.states.put("t_[0]", "State1");
//        manifest.states.put("t_[1]", "State2");
//        manifest.states.put("t_[2]", "State3");
//
//        DataNode node = new DataNode(DataNode.Type.Object);
//        node.getProperty("test.what.items").add(1);
//        node.getProperty("test.what.items").add(2);
//        node.getProperty("test").set("empty_arr", DataNode.CollectionNode());
//        node.getProperty("test").set("arr_one_val", DataNode.CollectionNode(("ava \"afa fef\" ava")));
//        node.getProperty("empty_str").setValue("");
//        node.set("empty_map", DataNode.MapNode(new HashMap<>()));
//        node.getProperty("other").set("map", DataNode.MapNode(Map.of("key1","val1")));
//
//        // TODO: array of objects....
//        // TODO: array of arrays...
//    }
}
