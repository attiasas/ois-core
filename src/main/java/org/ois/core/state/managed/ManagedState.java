package org.ois.core.state.managed;

import org.ois.core.entities.EntityManager;
import org.ois.core.utils.io.data.DataNode;
import org.ois.core.utils.io.data.DataObject;

public abstract class ManagedState implements IManagedState, DataObject<ManagedState> {

    protected EntityManager entityManager = new EntityManager();

    @Override
    public void enter(Object... parameters) {

    }

    @Override
    public void exit() {
        entityManager.clear();
    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean update(float dt) {
        entityManager.update();
        return true;
    }

    @Override
    public void render() {

    }

    @Override
    public ManagedState loadData(DataNode data) {
        return null;
    }

    @Override
    public DataNode convertToDataNode() {
        return null;
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}
}
