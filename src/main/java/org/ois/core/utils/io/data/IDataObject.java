package org.ois.core.utils.io.data;

/**
 * Interface representing a data object that can load data from a {@link DataNode}
 * and convert itself to a {@link DataNode}.
 *
 * @param <T> the type of the data object
 */
public interface IDataObject<T> {
    /**
     * Loads data from the specified {@link DataNode} and populates the data object.
     *
     * @param data the {@link DataNode} containing the data to be loaded
     * @return the populated data object
     */
    T loadData(DataNode data);

    /**
     * Converts the data object to a {@link DataNode} representation.
     *
     * @return the {@link DataNode} representing the data object
     */
    DataNode convertToDataNode();
}
