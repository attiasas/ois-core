package org.ois.core.components;

public interface IComponent {
    /**
     * Called to update the state.
     *
     */
    void update();

    void setEnabled(boolean enabled);
    boolean isEnable();
}
