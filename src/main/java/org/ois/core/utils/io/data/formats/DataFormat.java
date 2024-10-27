package org.ois.core.utils.io.data.formats;

import org.ois.core.utils.io.data.DataNode;
import org.ois.core.utils.io.data.DataObject;

import java.io.*;

/**
 * Interface for defining data formats for serialization and deserialization
 * of {@link DataNode} and {@link DataObject} instances.
 */
public interface DataFormat {

    /**
     * Deserializes a string representation of data into a {@link DataNode}.
     *
     * @param data the string representation of the data to be deserialized
     * @return the {@link DataNode} populated with the deserialized data
     */
    DataNode deserialize(String data);

    /**
     * Serializes a {@link DataNode} into its string representation.
     *
     * @param data the {@link DataNode} to be serialized
     * @return the string representation of the serialized data
     */
    String serialize(DataNode data);

    /**
     * Serializes a {@link DataObject} into its string representation
     * by first converting it to a {@link DataNode}.
     *
     * @param data the {@link DataObject} to be serialized
     * @return the string representation of the serialized data
     */
    default String serialize(DataObject<?> data) {
        return serialize(data.convertToDataNode());
    }

    /**
     * Loads data from an {@link InputStream} and deserializes it into a {@link DataNode}.
     *
     * @param inputStream the input stream containing the data
     * @return the {@link DataNode} populated with the loaded data
     * @throws IOException if an I/O error occurs while reading the stream
     */
    default DataNode load(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line.trim());
            }
            return deserialize(jsonBuilder.toString());
        }
    }

    /**
     * Loads data from an {@link InputStream} into the specified {@link DataObject}.
     *
     * @param objToLoad the {@link DataObject} to populate with data
     * @param inputStream the input stream containing the data
     * @param <T> the type of the data object
     * @return the populated {@link DataObject}
     * @throws IOException if an I/O error occurs while reading the stream
     */
    default <T extends DataObject<T>> T load(T objToLoad, InputStream inputStream) throws IOException {
        return objToLoad.loadData(load(inputStream));
    }

    /**
     * Loads data from a byte array into the specified {@link DataObject}.
     *
     * @param objToLoad the {@link DataObject} to populate with data
     * @param source the byte array containing the data
     * @param <T> the type of the data object
     * @return the populated {@link DataObject}
     */
    default <T extends DataObject<T>> T load(T objToLoad, byte[] source) {
        return load(objToLoad, new String(source));
    }

    /**
     * Loads data from a string into the specified {@link DataObject}.
     *
     * @param objToLoad the {@link DataObject} to populate with data
     * @param source the string containing the data
     * @param <T> the type of the data object
     * @return the populated {@link DataObject}
     */
    default <T extends DataObject<T>> T load(T objToLoad, String source) {
        return objToLoad.loadData(deserialize(source));
    }
}
