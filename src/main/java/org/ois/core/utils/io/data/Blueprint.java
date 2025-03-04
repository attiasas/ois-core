package org.ois.core.utils.io.data;


public interface Blueprint<T> extends DataObject<Blueprint<T>> {
    T create();
}
