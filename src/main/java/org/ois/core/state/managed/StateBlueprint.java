package org.ois.core.state.managed;

import org.ois.core.utils.io.data.Blueprint;
import org.ois.core.utils.io.data.DataNode;

public class StateBlueprint implements Blueprint<ManagedState> {

    @Override
    public ManagedState create() {
        return null;
    }

    @Override
    public StateBlueprint loadData(DataNode data) {
        return null;
    }

    @Override
    public DataNode convertToDataNode() {
        return null;
    }

}
