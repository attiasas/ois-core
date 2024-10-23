package org.ois.core.state;

public interface IState {
    void enter(Object... parameters);
    void exit();

    void pause();
    void resume();

    void resize(int width, int height);
    void render();
    boolean update(float dt);

    void dispose();
}
