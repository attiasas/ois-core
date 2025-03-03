package org.ois.core.project;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import org.ois.core.entities.EntityBlueprint;
import org.ois.core.entities.EntityManager;
import org.ois.core.utils.io.data.DataNode;
import org.ois.core.utils.io.data.formats.JsonFormat;
import org.ois.core.utils.log.Logger;

import java.util.Hashtable;
import java.util.Map;

/** Shared entities information of the simulation project between all states **/
public class Entities {
    private static final Logger<Entities> log = Logger.get(Entities.class);
    public static final String LOG_TOPIC = "entities";

    /** The directory name inside the simulation directory that holds all the project entities blueprints for the simulation **/
    public static final String ENTITIES_DIRECTORY = "entities";

    private static final Map<String, EntityBlueprint> blueprints = new Hashtable<>();

    public static EntityBlueprint getBlueprint(String type) {
        return blueprints.get(type);
    }

    public static void loadBlueprints() {
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
        for (FileHandle fileHandle : entitiesDirContent) {
            String entityType = fileHandle.name();
            if (!fileHandle.isDirectory()) {
                // entities information should be provided at a child directory named after the entity type
                continue;
            }
            FileHandle entityBlueprint = fileHandle.child(String.format("%s.blueprint.ois", entityType));
            if (!entityBlueprint.exists() || entityBlueprint.isDirectory()) {
                log.warn(String.format("entity '%s' skipped, can't find valid blueprint at '%s.blueprint.ois'", entityType, entityType));
                continue;
            }
            byte[] data = entityBlueprint.readBytes();
            if (data == null) {
                throw new RuntimeException(String.format("Can't load '%s' blueprint", entityType));
            }
            String rawData = new String(data);
            log.debug(LOG_TOPIC, String.format("'%s' Blueprint: %s", entityType, rawData));
            blueprints.put(entityType, JsonFormat.compact().load(new EntityBlueprint(entityType), rawData));
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
