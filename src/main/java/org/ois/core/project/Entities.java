package org.ois.core.project;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import org.ois.core.entities.Entity;
import org.ois.core.entities.EntityBlueprint;
import org.ois.core.entities.EntityManager;
import org.ois.core.utils.ReflectionUtils;
import org.ois.core.utils.io.data.Blueprint;
import org.ois.core.utils.io.data.DataNode;
import org.ois.core.utils.io.data.formats.JsonFormat;
import org.ois.core.utils.log.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Map;

/** Shared entities information of the simulation project between all states **/
public class Entities {
    private static final Logger<Entities> log = Logger.get(Entities.class);
    public static final String LOG_TOPIC = "entities";

    /** The directory name inside the simulation directory that holds all the project entities blueprints for the simulation **/
    public static final String ENTITIES_DIRECTORY = "entities";

    private static final Map<String, Blueprint<Entity>> blueprints = new Hashtable<>();

    public static Blueprint<Entity> getBlueprint(String type) {
        return blueprints.get(type);
    }

    public static void loadBlueprints() throws ReflectionException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        // Entities are optional, make sure the prerequisites are valid before loading
        FileHandle entitiesDir = Gdx.files.internal(ENTITIES_DIRECTORY);
        if (!entitiesDir.exists() || !entitiesDir.isDirectory()) {
            // Nothing to do
            return;
        }
        FileHandle[] entitiesDirContent = entitiesDir.list();
        if (entitiesDirContent == null || entitiesDirContent.length == 0) {
            // Nothing to do
            return;
        }
        log.debug(LOG_TOPIC, "Loading Project entities blueprints");
        for (FileHandle entityDefDir : entitiesDirContent) {
            if (!entityDefDir.isDirectory()) {
                // entities information should be provided at a child directory named after the entity type
                continue;
            }
            String entityType = entityDefDir.name();
            FileHandle entityBlueprintFile = entityDefDir.child(String.format("%s.blueprint.ois", entityType));
            if (!entityBlueprintFile.exists() || entityBlueprintFile.isDirectory()) {
                log.warn(String.format("entity '%s' skipped, can't find valid blueprint at '%s.blueprint.ois'", entityType, entityType));
                continue;
            }
            byte[] data = entityBlueprintFile.readBytes();
            if (data == null) {
                throw new RuntimeException(String.format("Can't load '%s' blueprint", entityType));
            }
            String rawData = new String(data);
            DataNode dataNode = JsonFormat.compact().deserialize(rawData);
            Blueprint<Entity> entityBlueprint = dataNode.contains("blueprint-class") ? ReflectionUtils.newInstance(dataNode.get("blueprint-class").getString()) : new EntityBlueprint(entityType);
            log.debug(LOG_TOPIC, String.format("'%s' Blueprint (%s): %s", entityType, entityBlueprint.getClass().getName(), rawData));
            blueprints.put(entityType, entityBlueprint.loadData(dataNode));
        }
        log.debug(LOG_TOPIC, String.format("Loaded '%d' entities blueprints", blueprints.size()));
    }

    public static EntityManager loadManager(FileHandle stateManifestDir) {
        EntityManager manager = new EntityManager();
        FileHandle entityManagerManifest = stateManifestDir.child("entities.manifest.ois");
        if (entityManagerManifest.exists() && !entityManagerManifest.isDirectory()) {
            log.debug(String.format("found entities manifest at: %s", entityManagerManifest));
            manager.setManifest(entityManagerManifest);
        }
        return manager;
    }

    public static DataNode loadManifest(FileHandle entityManagerManifest) {
        byte[] data = entityManagerManifest.readBytes();
        if (data == null) {
            throw new RuntimeException(String.format("Can't load state '%s'", entityManagerManifest));
        }
        String rawData = new String(data);
        log.debug(LOG_TOPIC, String.format("Loaded entities manifest: %s", rawData));
        return JsonFormat.compact().deserialize(rawData);
    }
}
