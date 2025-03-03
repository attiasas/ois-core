package org.ois.core.utils.io.data;

import java.util.Hashtable;
import java.util.Map;

public interface Blueprint<T> {
    T create(Map<String,Object> params);
    default T create() {
        return create(new Hashtable<>());
    }
}
