package org.ois.core.utils.io.data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a node that can store various types of data, including objects, collections, and primitive values.
 *
 * <p>A DataNode can represent four types of data:
 * <ul>
 *   <li><b>Unknown</b>: Undefined or unclassified data</li>
 *   <li><b>Object</b>: A map of named attributes</li>
 *   <li><b>Collection</b>: A list of values</li>
 *   <li><b>Primitive</b>: A single primitive value, such as String, int, float, or boolean</li>
 * </ul>
 *
 * <p>This class provides methods to create and manipulate nodes of different types, convert between types,
 * and retrieve data from nodes in various formats such as collections or maps.
 *
 * <p>It supports iterable properties for object-like nodes (maps) and iterable content for collection-like nodes.
 * Nodes are mutable and support method chaining for ease of use.
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

    /**
     * Creates a new DataNode of type Collection.
     *
     * @return a new DataNode representing a collection
     */
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

    /**
     * Creates a new DataNode representing a primitive string value.
     *
     * @param value the string value to set as a primitive
     * @return a new DataNode representing the primitive string value
     */
    public static DataNode Primitive(String value) {
        return new DataNode(Type.Primitive).setValue(value);
    }

    /**
     * Creates a new DataNode representing a primitive integer value.
     *
     * @param value the integer value to set as a primitive
     * @return a new DataNode representing the primitive integer value
     */
    public static DataNode Primitive(int value) {
        return new DataNode(Type.Primitive).setValue(value);
    }

    /**
     * Creates a new DataNode representing a primitive float value.
     *
     * @param value the float value to set as a primitive
     * @return a new DataNode representing the primitive float value
     */
    public static DataNode Primitive(float value) {
        return new DataNode(Type.Primitive).setValue(value);
    }

    /**
     * Creates a new DataNode representing a primitive boolean value.
     *
     * @param value the boolean value to set as a primitive
     * @return a new DataNode representing the primitive boolean value
     */
    public static DataNode Primitive(boolean value) {
        return new DataNode(Type.Primitive).setValue(value);
    }

    // User methods to retrieve data stored at nodes

    /**
     * Returns the node as a collection of String values.
     *
     * @return a collection of Strings, representing the node's content
     */
    public String[] toStringCollection() {
        List<String> list = toStringCollection(new ArrayList<>());
        return list.toArray(String[]::new);
    }

    /**
     * Fills the provided collection with the node's content as String values.
     *
     * @param destination the collection to fill with Strings
     * @param <T> the type of the destination collection
     * @return the destination collection filled with String representations of the node's content
     */
    public <T extends Collection<String>> T toStringCollection(T destination) {
        destination.addAll(this.content.stream().map(DataNode::getString).collect(Collectors.toList()));
        return destination;
    }

    /**
     * Fills the provided collection with the node's content as int values.
     *
     * @param destination the collection to fill with integers
     * @param <T> the type of the destination collection
     * @return the destination collection filled with integers representations of the node's content
     */
    public <T extends Collection<Integer>> T toIntCollection(T destination) {
        destination.addAll(this.content.stream().map(DataNode::getInt).collect(Collectors.toList()));
        return destination;
    }

    /**
     * Fills the provided collection with the node's content as float values.
     *
     * @param destination the collection to fill with floats
     * @param <T> the type of the destination collection
     * @return the destination collection filled with float representations of the node's content
     */
    public <T extends Collection<Float>> T toFloatCollection(T destination) {
        destination.addAll(this.content.stream().map(DataNode::getFloat).collect(Collectors.toList()));
        return destination;
    }

    /**
     * Fills the provided collection with the node's content as boolean values.
     *
     * @param destination the collection to fill with booleans
     * @param <T> the type of the destination collection
     * @return the destination collection filled with boolean representations of the node's content
     */
    public <T extends Collection<Boolean>> T toBooleanCollection(T destination) {
        destination.addAll(this.content.stream().map(DataNode::getBoolean).collect(Collectors.toList()));
        return destination;
    }

    /**
     * Fills the provided map with the node's attributes.
     *
     * @param destination the map to fill with the node's attributes
     * @return the destination map filled with the node's attributes
     */
    public Map<String,DataNode> toMap(Map<String,DataNode> destination) {
        destination.putAll(this.attributes);
        return destination;
    }

    /**
     * Returns the node's attributes as a map.
     *
     * @return a map representing the node's attributes
     */
    public Map<String,DataNode> toMap() {
        return toMap(new Hashtable<>());
    }

    /**
     * Fills the provided map with the node's attributes as String values.
     *
     * @param destination the map to fill with the node's attributes
     * @return the destination map filled with the node's attributes as String values
     */
    public Map<String,String> toStringMap(Map<String,String> destination) {
        for (Map.Entry<String, DataNode> attribute : properties()) {
           destination.put(attribute.getKey(), attribute.getValue().getString());
        }
        return destination;
    }

    /**
     * Returns the node's attributes as a map of String values.
     *
     * @return a map representing the node's attributes as String values
     */
    public Map<String,String> toStringMap() {
        return toStringMap(new Hashtable<>());
    }

    /**
     * Fills the provided map with the node's attributes as Integer values.
     *
     * @param destination the map to fill with the node's attributes
     * @return the destination map filled with the node's attributes as Integer values
     */
    public Map<String,Integer> toIntMap(Map<String,Integer> destination) {
        for (Map.Entry<String, DataNode> attribute : properties()) {
            destination.put(attribute.getKey(), attribute.getValue().getInt());
        }
        return destination;
    }

    /**
     * Returns the node's attributes as a map of Integer values.
     *
     * @return a map representing the node's attributes as Integer values
     */
    public Map<String,Integer> toIntMap() {
        return toIntMap(new Hashtable<>());
    }

    /**
     * Fills the provided map with the node's attributes as Float values.
     *
     * @param destination the map to fill with the node's attributes
     * @return the destination map filled with the node's attributes as Float values
     */
    public Map<String,Float> toFloatMap(Map<String,Float> destination) {
        for (Map.Entry<String, DataNode> attribute : properties()) {
            destination.put(attribute.getKey(), attribute.getValue().getFloat());
        }
        return destination;
    }

    /**
     * Returns the node's attributes as a map of Float values.
     *
     * @return a map representing the node's attributes as Float values
     */
    public Map<String,Float> toFloatMap() {
        return toFloatMap(new Hashtable<>());
    }

    /**
     * Fills the provided map with the node's attributes as Boolean values.
     *
     * @param destination the map to fill with the node's attributes
     * @return the destination map filled with the node's attributes as Boolean values
     */
    public Map<String,Boolean> toBooleanMap(Map<String,Boolean> destination) {
        for (Map.Entry<String, DataNode> attribute : properties()) {
            destination.put(attribute.getKey(), attribute.getValue().getBoolean());
        }
        return destination;
    }

    /**
     * Returns the node's attributes as a map of Boolean values.
     *
     * @return a map representing the node's attributes as Boolean values
     */
    public Map<String,Boolean> toBooleanMap() {
        return toBooleanMap(new Hashtable<>());
    }

    /**
     * Returns the type of the DataNode based on its contents.
     *
     * @return the type of the DataNode, which can be Primitive, Collection, Object, or Unknown
     */
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

    /**
     * Sets an attribute in this node.
     *
     * @param key the attribute name
     * @param attributeValue the DataNode value of the attribute
     * @return the DataNode for chaining
     */
    public DataNode set(String key, DataNode attributeValue) {
        this.attributes.put(key,attributeValue);
        return this;
    }

    /**
     * Sets an attribute in this node with a String value.
     *
     * @param key the attribute name
     * @param attributeValue the String value of the attribute
     * @return the DataNode for chaining
     */
    public DataNode set(String key, String attributeValue) {
        return set(key, Primitive(attributeValue));
    }

    /**
     * Sets an attribute in this node with an int value.
     *
     * @param key the attribute name
     * @param attributeValue the int value of the attribute
     * @return the DataNode for chaining
     */
    public DataNode set(String key, int attributeValue) {
        return set(key, String.valueOf(attributeValue));
    }

    /**
     * Sets an attribute in this node with a float value.
     *
     * @param key the attribute name
     * @param attributeValue the float value of the attribute
     * @return the DataNode for chaining
     */
    public DataNode set(String key, float attributeValue) {
        return set(key, String.valueOf(attributeValue));
    }

    /**
     * Sets an attribute in this node with a boolean value.
     *
     * @param key the attribute name
     * @param attributeValue the boolean value of the attribute
     * @return the DataNode for chaining
     */
    public DataNode set(String key, boolean attributeValue) {
        return set(key, String.valueOf(attributeValue));
    }

    /**
     * Retrieves the attribute value corresponding to the given key.
     *
     * @param attributeNodeKeys the sequence of attribute keys to traverse
     * @return the DataNode corresponding to the final key, or null if not found
     */
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

    /**
     * Checks if the node contains the specified property.
     *
     * @param property the name of the property to check
     * @return true if the property exists; false otherwise
     */
    public boolean contains(String property) {
        return this.attributes.containsKey(property);
    }

    /**
     * Retrieves the property value corresponding to the given key sequence.
     * If the key is not found, a new DataNode is created if necessary.
     *
     * @param attributeNodeKeys the sequence of attribute keys to traverse
     * @return the DataNode corresponding to the final key, or a new DataNode if not found
     */
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
     * Returns the number of properties that the node contains.
     *
     * @return the number of attributes registered at the node
     */
    public int getPropertyCount() {
        return this.attributes.size();
    }

    /**
     * Represents the attributes of a DataNode, allowing iteration over its entries.
     */
    public static class Attributes implements Iterable<Map.Entry<String,DataNode>> {
        private final Map<String, DataNode> attributes;

        /**
         * Constructs an Attributes object with the specified map of attributes.
         *
         * @param attributes the map of attributes to be wrapped
         */
        public Attributes(Map<String, DataNode> attributes) {
            this.attributes = attributes;
        }

        @Override
        public Iterator<Map.Entry<String,DataNode>> iterator() {
            return this.attributes.entrySet().iterator();
        }

        /**
         * Returns the number of attributes contained in this Attributes object.
         *
         * @return the number of attributes
         */
        public int size() {
            return this.attributes.size();
        }
    }

    /**
     * Retrieves the Attributes object that implements Iterable on the node properties.
     * This can be used for 'foreach' on the attributes/entries.
     *
     * @return an Attributes object, iterable on the node attributes
     */
    public Attributes properties() {
        return new Attributes(this.attributes);
    }


    /**
     * Removes an attribute from an object-type DataNode.
     *
     * @param key the name of the attribute to remove
     * @return the removed DataNode, or null if the attribute was not present
     */
    public DataNode remove(String key) {
        return this.attributes.remove(key);
    }

    /**
     * Clears all attributes from an object-type DataNode.
     *
     * @return this DataNode, for method chaining
     */
    public DataNode clearAttributes() {
        this.attributes.clear();
        return this;
    }

    // Node as Collection

    /**
     * Adds a collection of primitive values to the collection node.
     *
     * @param primitiveValues a collection of primitive values to add to the collection node
     * @return this node, for chaining
     * @param <T> the type of primitive values being added
     */
    public <T> DataNode add(Collection<T> primitiveValues) {
        for(T data : primitiveValues) {
            this.content.add(DataNode.Primitive(String.valueOf(data)));
        }
        return this;
    }

    /**
     * Adds multiple DataNode values to the collection node.
     *
     * @param values the DataNode values to add to the collection
     * @return this node, for chaining
     */
    public DataNode add(DataNode... values) {
        this.content.addAll(List.of(values));
        return this;
    }

    /**
     * Adds multiple String values to the collection node.
     *
     * @param values the String values to add to the collection
     * @return this node, for chaining
     */
    public DataNode add(String... values) {
        return add(List.of(values));
    }

    /**
     * Adds multiple integer values to the collection node.
     *
     * @param values the integer values to add to the collection
     * @return this node, for chaining
     */
    public DataNode add(int... values) {
        List<Integer> list = new ArrayList<>(values.length);
        for (int data : values) {
            list.add(data);
        }
        return add(list);
    }

    /**
     * Adds multiple float values to the collection node.
     *
     * @param values the float values to add to the collection
     * @return this node, for chaining
     */
    public DataNode add(float... values) {
        List<Float> list = new ArrayList<>(values.length);
        for (float data : values) {
            list.add(data);
        }
        return add(list);
    }

    /**
     * Adds multiple boolean values to the collection node.
     *
     * @param values the boolean values to add to the collection
     * @return this node, for chaining
     */
    public DataNode add(boolean... values) {
        List<Boolean> list = new ArrayList<>(values.length);
        for (boolean data : values) {
            list.add(data);
        }
        return add(list);
    }

    /**
     * Sets the value at the specified index in the collection node to the given DataNode value.
     *
     * @param index the index at which to set the value
     * @param value the DataNode value to set
     * @return the DataNode at the specified index
     */
    public DataNode set(int index, DataNode value) {
        return this.content.set(index, value);
    }

    /**
     * Sets the value at the specified index in the collection node to the given String value.
     *
     * @param index the index at which to set the value
     * @param value the String value to set
     * @return the DataNode at the specified index
     */
    public DataNode set(int index, String value) {
        return set(index,Primitive(value));
    }

    /**
     * Sets the value at the specified index in the collection node to the given integer value.
     *
     * @param index the index at which to set the value
     * @param value the integer value to set
     * @return the DataNode at the specified index
     */
    public DataNode set(int index, int value) {
        return set(index,Primitive(value));
    }

    /**
     * Sets the value at the specified index in the collection node to the given float value.
     *
     * @param index the index at which to set the value
     * @param value the float value to set
     * @return the DataNode at the specified index
     */
    public DataNode set(int index, float value) {
        return set(index,Primitive(value));
    }

    /**
     * Sets the value at the specified index in the collection node to the given boolean value.
     *
     * @param index the index at which to set the value
     * @param value the boolean value to set
     * @return the DataNode at the specified index
     */
    public DataNode set(int index, boolean value) {
        return set(index,Primitive(value));
    }

    /**
     * Retrieves the DataNode value at the specified index in the collection node.
     *
     * @param index the index of the value to retrieve
     * @return the DataNode at the specified index
     */
    public DataNode get(int index) {
        return this.content.get(index);
    }

    /**
     * Retrieves the String value at the specified index in the collection node.
     *
     * @param index the index of the value to retrieve
     * @return the String value at the specified index
     */
    public String getString(int index) {
        return get(index).getString();
    }

    /**
     * Retrieves the integer value at the specified index in the collection node.
     *
     * @param index the index of the value to retrieve
     * @return the integer value at the specified index
     */
    public int getInt(int index) {
        return get(index).getInt();
    }

    /**
     * Retrieves the float value at the specified index in the collection node.
     *
     * @param index the index of the value to retrieve
     * @return the float value at the specified index
     */
    public float getFloat(int index) {
        return get(index).getFloat();
    }

    /**
     * Retrieves the boolean value at the specified index in the collection node.
     *
     * @param index the index of the value to retrieve
     * @return the boolean value at the specified index
     */
    public boolean getBoolean(int index) {
        return get(index).getBoolean();
    }

    /**
     * Get the amount of content stored at the collection node.
     *
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

    /**
     * Clears all content from a collection-type DataNode.
     *
     * @return this DataNode, for method chaining
     */
    public DataNode clearContent() {
        this.content.clear();
        return this;
    }

    // Node as Primitive

    /**
     * Sets the value of this node as a primitive String value.
     *
     * @param primitiveValue the value to set
     * @return the DataNode for chaining
     */
    public DataNode setValue(String primitiveValue) {
        this.value = primitiveValue;
        return this;
    }

    /**
     * Sets the value of this node as a primitive int value.
     *
     * @param primitiveValue the value to set
     * @return the DataNode for chaining
     */
    public DataNode setValue(int primitiveValue) {
        return setValue(String.valueOf(primitiveValue));
    }

    /**
     * Sets the value of this node as a primitive float value.
     *
     * @param primitiveValue the value to set
     * @return the DataNode for chaining
     */
    public DataNode setValue(float primitiveValue) {
        return setValue(String.valueOf(primitiveValue));
    }

    /**
     * Sets the value of this node as a primitive boolean value.
     *
     * @param primitiveValue the value to set
     * @return the DataNode for chaining
     */
    public DataNode setValue(boolean primitiveValue) {
        return setValue(String.valueOf(primitiveValue));
    }

    /**
     * Retrieves the value of this node as a String.
     *
     * @return the String representation of the value
     */
    public String getString() {
        if (value == null) {
            return "";
        }
        return this.value;
    }

    /**
     * Retrieves the value of this node as an int.
     *
     * @return the int representation of the value
     */
    public int getInt() {
        String val = getString();
        if (val.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(val);
    }

    /**
     * Retrieves the value of this node as a float.
     *
     * @return the float representation of the value
     */
    public float getFloat() {
        String val = getString();
        if (val.isEmpty()) {
            return 0;
        }
        return Float.parseFloat(val);
    }

    /**
     * Retrieves the value of this node as a boolean.
     *
     * @return the boolean representation of the value
     */
    public boolean getBoolean() {
        String val = getString();
        if (val.isEmpty()) {
            return false;
        }
        return Boolean.parseBoolean(val);
    }

    /**
     * Provides a deep copy of the DataNode.
     *
     * @return a new DataNode that is a copy of this node
     */
    public DataNode deepCopy() {
        DataNode copy = new DataNode(this.nodeType);
        if (this.nodeType == Type.Primitive) {
            copy.setValue(this.value);
        } else if (this.nodeType == Type.Collection) {
            for (DataNode child : this.content) {
                copy.add(child.deepCopy());
            }
        } else if (this.nodeType == Type.Object) {
            for (Map.Entry<String, DataNode> entry : this.attributes.entrySet()) {
                copy.set(entry.getKey(), entry.getValue().deepCopy());
            }
        }
        return copy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeType, value, content, attributes);
    }

    /**
     * Returns a string representation of the DataNode.
     *
     * <p>The representation varies depending on the type of the node:
     * <ul>
     *     <li>For a primitive node, it returns the string value</li>
     *     <li>For a collection node, it returns the string representation of the collection</li>
     *     <li>For an object node, it returns the string representation of the attributes</li>
     *     <li>For an unknown type, it returns an empty string</li>
     * </ul>
     *
     * @return a string representation of the node
     */
    @Override
    public String toString() {
        if (nodeType == Type.Primitive) {
            return value;
        }
        if (nodeType == Type.Collection) {
            return content.toString();
        }
        if (nodeType == Type.Object) {
            return attributes.toString();
        }
        return "";
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
