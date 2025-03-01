package org.ois.core.utils.io.data;

public interface Blueprint<T> {
    T create(Object... params);
}
