package org.ois.core.utils.io.data.formats;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeTest;
import static org.testng.Assert.assertEquals;

import org.ois.core.utils.io.data.DataNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JsonFormatTest {

    Path testFilesDirPath = Paths.get(".").toAbsolutePath().normalize().resolve(Paths.get("src","test","resources", "json"));
    DataNode node;

    @BeforeTest
    public void prepareTestNode() {
        node = new DataNode(DataNode.Type.Object);

        node.getProperty("test","what","items").add(1);
        node.getProperty("test","what","items").add(2.5f);
        node.getProperty("test").set("empty_arr", DataNode.Collection(new ArrayList<>()));
        node.getProperty("test").set("arr_one_val", DataNode.Collection(("ava \"afa fef\" ava")));
        node.getProperty("empty_str").setValue("");
        node.set("empty_map", DataNode.Map(new HashMap<>()));
        node.getProperty("other").set("map", DataNode.Map(Map.of("key1","val1")));
        node.getProperty("str").setValue("some-value");
    }

    @Test
    public void testSerialize() throws IOException {
        // Human readable
        String actual = JsonFormat.humanReadable().serialize(node);
        String expected = Files.readString(testFilesDirPath.resolve("testNode.json")).replaceAll("\r","");
        assertEquals(actual, expected);

        // Compact
        actual = JsonFormat.compact().serialize(node);
        expected = Files.readString(testFilesDirPath.resolve("testNodeCompact.json"));
        assertEquals(actual, expected);
    }

    @Test
    public void testDeserialize() throws IOException {
        DataNode actual = JsonFormat.compact().deserialize(Files.readString(testFilesDirPath.resolve("testNode.json")));
        assertEquals(actual, node);
    }
}
