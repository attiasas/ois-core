package org.ois.core.utils.io.data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a node that can store various types of data, including objects, collections, and primitive values.
 */
public class DataNode implements Iterable<DataNode> {

    /**
     * The possible data types that a node can represent
     */
    public enum Type {
        Unknown, Object, Collection, Primitive
    }

    protected String value;
    protected List<DataNode> content;
    protected Map<String, DataNode> attributes;
    protected final Type nodeType;

    /**
     * Creates a new DataNode of the specified type.
     *
     * @param type the type of the node
     */
    public DataNode(Type type) {
        this.nodeType = type;
        content = new ArrayList<>();
        // LinkedHashMap, to preserve the order of insertion (maintain order of attribute insertion for readability)
        attributes = new LinkedHashMap<>();
    }

    /**
     * Creates a new DataNode of type Unknown.
     */
    public DataNode() {
        this(Type.Unknown);
    }

    // User methods to create object and store data

    /**
     * Creates a new DataNode of type Object.
     *
     * @return a new DataNode representing an object
     */
    public static DataNode Object() {
        return new DataNode(Type.Object);
    }

    /**
     * Creates a new DataNode representing a map from the given values.
     *
     * @param values a map of string keys to DataNode values
     * @return a new DataNode representing the map
     */
    public static DataNode NodeMap(Map<String, DataNode> values) {
        DataNode mapNode = new DataNode(Type.Object);
        mapNode.attributes.putAll(values);
        return mapNode;
    }

    /**
     * Creates a new DataNode representing a map from the given values.
     *
     * @param values a map of string keys to values of type T, must be primitive
     * @param <T> the type of the values, must be primitive
     * @return a new DataNode representing the map
     */
    public static <T> DataNode Map(Map<String, T> values) {
        DataNode mapNode = new DataNode(Type.Object);
        for (Map.Entry<String,T> data : values.entrySet()) {
            mapNode.set(data.getKey(), String.valueOf(data.getValue()));
        }
        return mapNode;
    }

    public static DataNode Collection() {
        return new DataNode(Type.Collection);
    }

    /**
     * Creates a new DataNode representing a collection of the given values.
     *
     * @param values a collection of values of type T, must be primitive
     * @param <T> the type of the values, must be primitive
     * @return a new DataNode representing the collection
     */
    public static <T> DataNode Collection(Collection<T> values) {
        DataNode collectionNode = new DataNode(Type.Collection);
        for (T data : values) {
            collectionNode.add(String.valueOf(data));
        }
        return collectionNode;
    }

    /**
     * Creates a new DataNode representing a collection from the given DataNode values.
     *
     * @param values the DataNode values to add to the collection
     * @return a new DataNode representing the collection
     */
    public static DataNode Collection(DataNode... values) {
        return new DataNode(Type.Collection).add(values);
    }

    /**
     * Creates a new DataNode representing a collection from the given String values.
     *
     * @param values the String values to add to the collection
     * @return a new DataNode representing the collection
     */
    public static DataNode Collection(String... values) {
        return new DataNode(Type.Collection).add(values);
    }

    /**
     * Creates a new DataNode representing a collection from the given int values.
     *
     * @param values the int values to add to the collection
     * @return a new DataNode representing the collection
     */
    public static DataNode Collection(int... values) {
        return new DataNode(Type.Collection).add(values);
    }

    /**
     * Creates a new DataNode representing a collection from the given float values.
     *
     * @param values the float values to add to the collection
     * @return a new DataNode representing the collection
     */
    public static DataNode Collection(float... values) {
        return new DataNode(Type.Collection).add(values);
    }

    /**
     * Creates a new DataNode representing a collection from the given boolean values.
     *
     * @param values the boolean values to add to the collection
     * @return a new DataNode representing the collection
     */
    public static DataNode Collection(boolean... values) {
        return new DataNode(Type.Collection).add(values);
    }

    public static DataNode Primitive(String value) {
        return new DataNode(Type.Primitive).setValue(value);
    }
    public static DataNode Primitive(int value) {
        return new DataNode(Type.Primitive).setValue(value);
    }
    public static DataNode Primitive(float value) {
        return new DataNode(Type.Primitive).setValue(value);
    }
    public static DataNode Primitive(boolean value) {
        return new DataNode(Type.Primitive).setValue(value);
    }

    // User methods to retrieve data stored at nodes

    public String[] toStringCollection() {
        List<String> list = toStringCollection(new ArrayList<>());
        return list.toArray(String[]::new);
    }

    public <T extends Collection<String>> T toStringCollection(T destination) {
        destination.addAll(this.content.stream().map(DataNode::getString).collect(Collectors.toList()));
        return destination;
    }

    public <T extends Collection<Integer>> T toIntCollection(T destination) {
        destination.addAll(this.content.stream().map(DataNode::getInt).collect(Collectors.toList()));
        return destination;
    }

    public <T extends Collection<Float>> T toFloatCollection(T destination) {
        destination.addAll(this.content.stream().map(DataNode::getFloat).collect(Collectors.toList()));
        return destination;
    }

    public <T extends Collection<Boolean>> T toBooleanCollection(T destination) {
        destination.addAll(this.content.stream().map(DataNode::getBoolean).collect(Collectors.toList()));
        return destination;
    }

    public Map<String,DataNode> toMap(Map<String,DataNode> destination) {
        destination.putAll(this.attributes);
        return destination;
    }

    public Map<String,DataNode> toMap() {
        return toMap(new Hashtable<>());
    }

    public Map<String,String> toStringMap(Map<String,String> destination) {
        for (Map.Entry<String, DataNode> attribute : properties()) {
           destination.put(attribute.getKey(), attribute.getValue().getString());
        }
        return destination;
    }

    public Map<String,String> toStringMap() {
        return toStringMap(new Hashtable<>());
    }

    public Map<String,Integer> toIntMap(Map<String,Integer> destination) {
        for (Map.Entry<String, DataNode> attribute : properties()) {
            destination.put(attribute.getKey(), attribute.getValue().getInt());
        }
        return destination;
    }

    public Map<String,Integer> toIntMap() {
        return toIntMap(new Hashtable<>());
    }

    public Map<String,Float> toFloatMap(Map<String,Float> destination) {
        for (Map.Entry<String, DataNode> attribute : properties()) {
            destination.put(attribute.getKey(), attribute.getValue().getFloat());
        }
        return destination;
    }

    public Map<String,Float> toFloatMap() {
        return toFloatMap(new Hashtable<>());
    }

    public Map<String,Boolean> toBooleanMap(Map<String,Boolean> destination) {
        for (Map.Entry<String, DataNode> attribute : properties()) {
            destination.put(attribute.getKey(), attribute.getValue().getBoolean());
        }
        return destination;
    }

    public Map<String,Boolean> toBooleanMap() {
        return toBooleanMap(new Hashtable<>());
    }

    public Type getType() {
        if (!Type.Unknown.equals(nodeType)) {
            return nodeType;
        }
        if (value != null) {
            return Type.Primitive;
        }
        if (!content.isEmpty()) {
            return Type.Collection;
        }
        // Last check is attributes to allow values/collection to have meta-attributes
        if (!attributes.isEmpty()) {
            return Type.Object;
        }
        return Type.Unknown;
    }

    // Node as Map/Object (Object = Map of attributes)

    public DataNode set(String key, DataNode attributeValue) {
        this.attributes.put(key,attributeValue);
        return this;
    }

    public DataNode set(String key, String attributeValue) {
        return set(key, Primitive(attributeValue));
    }

    public DataNode set(String key, int attributeValue) {
        return set(key, String.valueOf(attributeValue));
    }

    public DataNode set(String key, float attributeValue) {
        return set(key, String.valueOf(attributeValue));
    }

    public DataNode set(String key, boolean attributeValue) {
        return set(key, String.valueOf(attributeValue));
    }

    public DataNode get(String... attributeNodeKeys) {
        DataNode currentNode = this;
        for (String key : attributeNodeKeys) {
            if (!currentNode.attributes.containsKey(key)) {
                return null;
            }
            currentNode = currentNode.attributes.get(key);
        }
        return currentNode;
    }

    public boolean contains(String property) {
        return this.attributes.containsKey(property);
    }

    // for optional attributes
    public DataNode getProperty(String... attributeNodeKeys) {
        DataNode currentNode = this;
        for (int i = 0; i < attributeNodeKeys.length; i++) {
            String key = attributeNodeKeys[i];
            if (!currentNode.attributes.containsKey(key)) {
                // If we have multiple attributes, we can know that an attribute is an Object if it has attributes
                // So if we are not at the last provided attributeKey, the node is Object
                currentNode.attributes.put(key, new DataNode(i < attributeNodeKeys.length - 1 ? Type.Object : Type.Unknown));
            }
            currentNode = currentNode.attributes.get(key);
        }
        return currentNode;
    }

    /**
     * The amount of properties that the node contains
     * @return - the number of attributes registered at the node
     */
    public int getPropertyCount() {
        return this.attributes.size();
    }

    // We implement Iterable on the node attributes.
    public static class Attributes implements Iterable<Map.Entry<String,DataNode>> {
        private final Map<String, DataNode> attributes;
        public Attributes(Map<String, DataNode> attributes) {
            this.attributes = attributes;
        }
        @Override
        public Iterator<Map.Entry<String,DataNode>> iterator() {
            return this.attributes.entrySet().iterator();
        }

        public int size() {
            return this.attributes.size();
        }
    }

    /**
     * Get the node Attributes object that implements Iterable on the node properties.
     * Can be used for 'foreach' on the attributes/entries
     * @return Attributes object, iterable on the node attributes
     */
    public Attributes properties() {
        return new Attributes(this.attributes);
    }

    // Node as Collection

    /**
     * Add a collection of primitive values to add to the collection node
     * @param primitiveValues a collection of primitive values to add to the collection node
     * @return this node, for chaining
     * @param <T> primitive values
     */
    public <T> DataNode add(Collection<T> primitiveValues) {
        for(T data : primitiveValues) {
            this.content.add(DataNode.Primitive(String.valueOf(data)));
        }
        return this;
    }

    public DataNode add(DataNode... values) {
        this.content.addAll(List.of(values));
        return this;
    }

    public DataNode add(String... values) {
        return add(List.of(values));
    }

    public DataNode add(int... values) {
        List<Integer> list = new ArrayList<>(values.length);
        for (int data : values) {
            list.add(data);
        }
        return add(list);
    }

    public DataNode add(float... values) {
        List<Float> list = new ArrayList<>(values.length);
        for (float data : values) {
            list.add(data);
        }
        return add(list);
    }

    public DataNode add(boolean... values) {
        List<Boolean> list = new ArrayList<>(values.length);
        for (boolean data : values) {
            list.add(data);
        }
        return add(list);
    }

    public DataNode set(int index, DataNode value) {
        return this.content.set(index, value);
    }

    public DataNode set(int index, String value) {
        return set(index,Primitive(value));
    }

    public DataNode set(int index, int value) {
        return set(index,Primitive(value));
    }

    public DataNode set(int index, float value) {
        return set(index,Primitive(value));
    }

    public DataNode set(int index, boolean value) {
        return set(index,Primitive(value));
    }

    public DataNode get(int index) {
        return this.content.get(index);
    }

    public String getString(int index) {
        return get(index).getString();
    }

    public int getInt(int index) {
        return get(index).getInt();
    }

    public float getFloat(int index) {
        return get(index).getFloat();
    }

    public boolean getBoolean(int index) {
        return get(index).getBoolean();
    }

    /**
     * Get the amount of content stored at the collection node.
     * @return - the number of values stored at the node
     */
    public int contentCount() {
        return this.content.size();
    }

    /** For nodes that represents Collection, go over the content values  **/
    @Override
    public Iterator<DataNode> iterator() {
        return this.content.iterator();
    }

    // Node as Primitive

    /**
     * Set the value of a node, for primitive nodes (single primitive value)
     * @param primitiveValue - value of the node
     * @return - the data node for chaining
     */
    public DataNode setValue(String primitiveValue) {
        this.value = primitiveValue;
        return this;
    }

    /**
     * Set the value of a node, for primitive nodes (single primitive value)
     * @param primitiveValue - value of the node
     * @return - the data node for chaining
     */
    public DataNode setValue(int primitiveValue) {
        return setValue(String.valueOf(primitiveValue));
    }

    /**
     * Set the value of a node, for primitive nodes (single primitive value)
     * @param primitiveValue - value of the node
     * @return - the data node for chaining
     */
    public DataNode setValue(float primitiveValue) {
        return setValue(String.valueOf(primitiveValue));
    }

    /**
     * Set the value of a node, for primitive nodes (single primitive value)
     * @param primitiveValue - value of the node
     * @return - the data node for chaining
     */
    public DataNode setValue(boolean primitiveValue) {
        return setValue(String.valueOf(primitiveValue));
    }

    /**
     * Get the value of the primitive node, and interpret it as a String
     * @return the value of the node as a String
     */
    public String getString() {
        if (value == null) {
            return "";
        }
        return this.value;
    }

    /**
     * Get the value of the primitive node, and interpret it as an Integer
     * @return the value of the node as an Integer
     */
    public int getInt() {
        String val = getString();
        if (val.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(val);
    }

    /**
     * Get the value of the primitive node, and interpret it as a Float
     * @return the value of the node as a Float
     */
    public float getFloat() {
        String val = getString();
        if (val.isEmpty()) {
            return 0;
        }
        return Float.parseFloat(val);
    }

    /**
     * Get the value of the primitive node, and interpret it as a Boolean
     * @return the value of the node as a Boolean
     */
    public boolean getBoolean() {
        String val = getString();
        if (val.isEmpty()) {
            return false;
        }
        return Boolean.parseBoolean(val);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DataNode other = (DataNode) obj;

        // Check node type
        if (nodeType != other.nodeType) {
            return false;
        }

        // Compare value for Primitive type
        if (nodeType == Type.Primitive) {
            return Objects.equals(value, other.value);
        }

        // Compare attributes for Object type
        if (nodeType == Type.Object) {
            return Objects.equals(attributes, other.attributes);
        }

        // Compare content for Collection type
        if (nodeType == Type.Collection) {
            return Objects.equals(content, other.content);
        }

        // For Unknown type, it's ambiguous, so return false
        return nodeType == Type.Unknown;
    }
}
