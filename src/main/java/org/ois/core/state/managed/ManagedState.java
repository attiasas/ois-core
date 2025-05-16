package org.ois.core.state.managed;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import org.ois.core.components.ComponentManager;
import org.ois.core.entities.EntityManager;

public class ManagedState implements IManagedState {

    protected EntityManager entityManager = new EntityManager();
    protected ComponentManager<? extends IManagedState> components = new ComponentManager<>();

    @Override
    public void enter(Object... parameters) {
        entityManager.loadManifest(true);
    }

    @Override
    public void exit() {
        components.clear();
        entityManager.clear();
    }

    @Override
    public void dispose() {
        components.dispose();
        entityManager.dispose();
    }

    @Override
    public boolean update(float dt) {
        components.update();
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
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public ComponentManager<? extends IManagedState> getComponents() {
        return components;
    }
}
