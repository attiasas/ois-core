package org.ois.core.state.managed;

import org.ois.core.entities.EntityManager;
import org.ois.core.utils.io.data.DataNode;
import org.ois.core.utils.io.data.DataObject;

public abstract class ManagedState implements IManagedState, DataObject<ManagedState> {

    protected EntityManager entityManager;

    @Override
    public void enter(Object... parameters) {
        entityManager.loadManifest(true);
    }

    @Override
    public void exit() {
        entityManager.clear();
    }

    @Override
    public void dispose() {
        entityManager.dispose();
    }

    @Override
    public boolean update(float dt) {
        entityManager.update();
        return true;
    }

    @Override
    public void render() {}

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void setEntityManager(EntityManager manager) {
        this.entityManager = manager;
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public ManagedState loadData(DataNode data) {

        entityManager.loadData(data.getProperty("entityManager"));

        return this;
    }

    @Override
    public DataNode convertToDataNode() {
        DataNode root = DataNode.Object();

        root.set("entityManager", entityManager.convertToDataNode());

        return root;
    }

}
