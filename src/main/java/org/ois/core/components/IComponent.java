package org.ois.core.components;

public interface IComponent {
    /**
     * Called to update the state.
     *
     */
    void update();
    /**
     * Called to resize the state, typically in response to window resizing.
     *
     * @param width  The new width of the state.
     * @param height The new height of the state.
     */
    void resize(int width, int height);
    /**
     * Called to render the state.
     */
    void render();

    void setEnabled(boolean enabled);
    boolean isEnable();
}
