package org.ois.core.utils.io.data;


public interface DataObject<T> {
    T loadData(DataNode data);
    DataNode convertToDataNode();
}
