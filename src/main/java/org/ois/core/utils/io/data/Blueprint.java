package org.ois.core.utils.io.data;


public interface Blueprint<T> extends IDataObject<Blueprint<T>> {
    T create();
}
