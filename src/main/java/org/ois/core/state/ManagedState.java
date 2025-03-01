package org.ois.core.state;

import org.ois.core.entities.EntityManager;

public abstract class ManagedState implements IState {

    protected EntityManager entityManager = new EntityManager();

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}
}
