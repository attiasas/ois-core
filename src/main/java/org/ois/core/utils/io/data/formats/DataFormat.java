package org.ois.core.utils.io.data.formats;

import org.ois.core.utils.io.data.DataNode;
import org.ois.core.utils.io.data.DataObject;

import java.io.*;

public interface DataFormat {
    DataNode deserialize(String data);
    String serialize(DataNode data);

    default String serialize(DataObject<?> data) {
        return serialize(data.convertToDataNode());
    }

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

    default <T extends DataObject<T>> T load(T objToLoad, InputStream inputStream) throws IOException {
        return objToLoad.loadData(load(inputStream));
    }

    default <T extends DataObject<T>> T load(T objToLoad, byte[] source) {
        return load(objToLoad, new String(source));
    }

    default <T extends DataObject<T>> T load(T objToLoad, String source) {
        return objToLoad.loadData(deserialize(source));
    }
}
