package org.ois.core.utils.io.data;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;

public class DataNodeTest {

    private DataNode objectNode;
    private DataNode collectionNode;
    private DataNode primitiveNode;

    @BeforeMethod
    public void setUp() {
        objectNode = DataNode.Object();
        collectionNode = DataNode.Collection();
        primitiveNode = DataNode.Primitive("testValue");
    }

    @Test
    public void testObjectNodeCreation() {
        Assert.assertEquals(objectNode.getType(), DataNode.Type.Object, "Object node type mismatch.");
        Assert.assertTrue(objectNode.attributes.isEmpty(), "Object node should have empty attributes.");
    }

    @Test
    public void testCollectionNodeCreation() {
        Assert.assertEquals(collectionNode.getType(), DataNode.Type.Collection, "Collection node type mismatch.");
        Assert.assertTrue(collectionNode.content.isEmpty(), "Collection node should have empty content.");
    }

    @Test
    public void testPrimitiveNodeCreation() {
        Assert.assertEquals(primitiveNode.getType(), DataNode.Type.Primitive, "Primitive node type mismatch.");
        Assert.assertEquals(primitiveNode.getString(), "testValue", "Primitive node value mismatch.");
    }

    @Test
    public void testSetAndGetObjectNodeAttributes() {
        DataNode valueNode = DataNode.Primitive("attributeValue");
        objectNode.set("attributeKey", valueNode);

        Assert.assertTrue(objectNode.contains("attributeKey"), "Object node should contain the attribute.");
        Assert.assertEquals(objectNode.get("attributeKey"), valueNode, "Retrieved attribute value mismatch.");
    }

    @Test
    public void testSetAndGetCollectionContent() {
        DataNode node1 = DataNode.Primitive("value1");
        DataNode node2 = DataNode.Primitive("value2");

        collectionNode.add(node1, node2);

        Assert.assertEquals(collectionNode.contentCount(), 2, "Collection node should contain 2 values.");
        Assert.assertEquals(collectionNode.get(0), node1, "First element of collection mismatch.");
        Assert.assertEquals(collectionNode.get(1), node2, "Second element of collection mismatch.");
    }

    @Test
    public void testSetAndGetPrimitiveValue() {
        primitiveNode.setValue("newValue");

        Assert.assertEquals(primitiveNode.getString(), "newValue", "Primitive node value mismatch after update.");
    }

    @Test
    public void testToMap() {
        DataNode attribute1 = DataNode.Primitive("value1");
        DataNode attribute2 = DataNode.Primitive("value2");

        objectNode.set("key1", attribute1);
        objectNode.set("key2", attribute2);

        Map<String, DataNode> map = objectNode.toMap();
        Assert.assertEquals(map.size(), 2, "Map size mismatch.");
        Assert.assertEquals(map.get("key1"), attribute1, "Map entry mismatch for key1.");
        Assert.assertEquals(map.get("key2"), attribute2, "Map entry mismatch for key2.");
    }

    @Test
    public void testToStringMap() {
        DataNode attribute1 = DataNode.Primitive("value1");
        DataNode attribute2 = DataNode.Primitive("value2");

        objectNode.set("key1", attribute1);
        objectNode.set("key2", attribute2);

        Map<String, String> stringMap = objectNode.toStringMap();
        Assert.assertEquals(stringMap.size(), 2, "String map size mismatch.");
        Assert.assertEquals(stringMap.get("key1"), "value1", "String map entry mismatch for key1.");
        Assert.assertEquals(stringMap.get("key2"), "value2", "String map entry mismatch for key2.");
    }

    @Test
    public void testEqualsForSameType() {
        DataNode node1 = DataNode.Primitive("value1");
        DataNode node2 = DataNode.Primitive("value1");

        Assert.assertEquals(node1, node2, "Nodes with same type and value should be equal.");
    }

    @Test
    public void testEqualsForDifferentTypes() {
        DataNode node1 = DataNode.Primitive("value1");
        DataNode node2 = DataNode.Object();

        Assert.assertNotEquals(node1, node2, "Nodes with different types should not be equal.");
    }

    @Test
    public void testGetPropertyWithDefaultCreation() {
        DataNode defaultNode = objectNode.getProperty("nonExistentKey", "subKey");

        Assert.assertTrue(objectNode.contains("nonExistentKey"), "Parent object node should contain new attribute.");
        Assert.assertEquals(defaultNode.getType(), DataNode.Type.Unknown, "Newly created node type mismatch.");
        Assert.assertEquals(objectNode.get("nonExistentKey").getType(), DataNode.Type.Object, "Newly created node type mismatch.");
    }

    @Test
    public void testAddPrimitiveCollection() {
        List<Integer> intList = Arrays.asList(1, 2, 3);
        DataNode collection = DataNode.Collection(intList);

        Assert.assertEquals(collection.contentCount(), 3, "Collection node size mismatch.");
        Assert.assertEquals(collection.getInt(0), 1, "First element mismatch.");
        Assert.assertEquals(collection.getInt(1), 2, "Second element mismatch.");
        Assert.assertEquals(collection.getInt(2), 3, "Third element mismatch.");
    }

    @Test
    public void testAddMultipleTypesToCollection() {
        collectionNode.add(DataNode.Primitive("string"), DataNode.Primitive(123), DataNode.Primitive(3.14f));

        Assert.assertEquals(collectionNode.contentCount(), 3, "Collection node size mismatch.");
        Assert.assertEquals(collectionNode.getString(0), "string", "First element string mismatch.");
        Assert.assertEquals(collectionNode.getInt(1), 123, "Second element int mismatch.");
        Assert.assertEquals(collectionNode.getFloat(2), 3.14f, "Third element float mismatch.");
    }

    @Test
    public void testDeepCopy() {
        DataNode original = DataNode.Object();
        original.set("key1", DataNode.Primitive("value1"));
        DataNode deepCopy = original.deepCopy();

        Assert.assertNotSame(original, deepCopy, "Deep copy should create a new object.");
        Assert.assertEquals(original, deepCopy, "Deep copy should be equal to the original.");
        Assert.assertNotSame(original.get("key1"), deepCopy.get("key1"), "Deep copy's attributes should not reference the same objects as the original.");
    }

    @Test
    public void testRemoveAttribute() {
        DataNode objectNode = DataNode.Object();
        objectNode.set("key", DataNode.Primitive("value"));
        DataNode removed = objectNode.remove("key");

        Assert.assertEquals(removed.getString(), "value", "Removed value mismatch.");
        Assert.assertFalse(objectNode.contains("key"), "Object node should no longer contain the removed attribute.");
    }

    @Test
    public void testClearContent() {
        DataNode collectionNode = DataNode.Collection();
        collectionNode.add(DataNode.Primitive("item1"), DataNode.Primitive("item2"));

        Assert.assertEquals(collectionNode.contentCount(), 2, "Collection node should initially contain 2 items.");

        collectionNode.clearContent();

        Assert.assertEquals(collectionNode.contentCount(), 0, "Collection node should be empty after clearContent.");
    }

    @Test
    public void testClearAttributes() {
        DataNode objectNode = DataNode.Object();
        objectNode.set("key1", DataNode.Primitive("value1"));
        objectNode.set("key2", DataNode.Primitive("value2"));

        Assert.assertEquals(objectNode.toMap().size(), 2, "Object node should initially contain 2 attributes.");

        objectNode.clearAttributes();

        Assert.assertEquals(objectNode.toMap().size(), 0, "Object node should have no attributes after clearAttributes.");
    }
}
