package org.ois.core.entities;

import org.ois.core.utils.ReflectionUtils;
import org.ois.core.utils.io.data.Blueprint;
import org.ois.core.utils.io.data.DataNode;


public class EntityBlueprint implements Blueprint<Entity> {

    protected final String type;

    protected String customClass;
    private static final String CUSTOM_CLASS_ATTRIB = "class";

    public EntityBlueprint(String type) {
        this.type = type;
    }

    @Override
    public Entity create() {
        if (isCustomClassBlueprint()) {
            try {
                return ReflectionUtils.newInstance(customClass);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return new Entity(type);
    }

    private boolean isCustomClassBlueprint() {
        return customClass != null && !customClass.isBlank();
    }

    @Override
    public EntityBlueprint loadData(DataNode data) {

        if (data.contains(CUSTOM_CLASS_ATTRIB)) {
            customClass = data.get(CUSTOM_CLASS_ATTRIB).getString();
        }

        return this;
    }

    @Override
    public DataNode convertToDataNode() {
        DataNode root = DataNode.Object();

        if (isCustomClassBlueprint()) {
            root.set(CUSTOM_CLASS_ATTRIB, customClass);
        }

        return root;
    }
}
