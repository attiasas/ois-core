package org.ois.core.state.managed;

import org.ois.core.utils.io.data.Blueprint;
import org.ois.core.utils.io.data.DataNode;
import org.ois.core.utils.io.data.DataObject;

import java.util.Map;

public class StateBlueprint implements Blueprint<ManagedState>, DataObject<StateBlueprint> {

    @Override
    public ManagedState create(Map<String,Object> ignored) {
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
