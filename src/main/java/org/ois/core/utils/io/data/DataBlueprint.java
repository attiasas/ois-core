package org.ois.core.utils.io.data;


public abstract class DataBlueprint<T> extends DataObject {
    public abstract <C extends T> C create();
}
