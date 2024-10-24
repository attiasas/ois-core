package org.ois.core.utils.io.data.formats;

import org.ois.core.utils.io.data.DataNode;

public interface DataFormat {
    public DataNode deserialize(String data);
    public String serialize(DataNode data);
}
