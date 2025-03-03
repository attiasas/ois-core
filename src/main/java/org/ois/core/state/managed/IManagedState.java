package org.ois.core.state.managed;

import org.ois.core.entities.EntityManager;
import org.ois.core.state.IState;

public interface IManagedState extends IState {
    void setEntityManager(EntityManager manager);
    EntityManager getEntityManager();
}
