package org.ois.core.project;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import org.ois.core.entities.Entity;
import org.ois.core.project.blueprints.EntityBlueprint;
import org.ois.core.entities.EntityManager;
import org.ois.core.utils.ReflectionUtils;
import org.ois.core.utils.io.data.DataBlueprint;
import org.ois.core.utils.io.data.DataNode;
import org.ois.core.utils.io.data.formats.JsonFormat;
import org.ois.core.utils.log.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Map;

/**
 * The {@code Entities} class is responsible for managing and loading entity blueprints and their corresponding
 * data. It provides methods to load entity blueprints from the project directory, load the entity manager,
 * and handle entity manifests.
 * <p>
 * This class also defines constants used for working with entities in the simulation project, such as directory
 * paths and property names used in blueprints.
 */
public class Entities {
    private static final Logger<Entities> log = Logger.get(Entities.class);

    /** The logging topic for entity-related operations. */
    public static final String LOG_TOPIC = "entities";

    /** The directory name inside the simulation directory that holds all the project entities blueprints for the simulation **/
    public static final String ENTITIES_DIRECTORY = "entities";

    /** The property name for entities in the simulation configuration. */
    public final static String ENTITIES_PROPERTY = "entities";
    /** The property name for the type of an entity. */
    public final static String TYPE_PROPERTY = "type";
    /** The property name to determine if an entity is enabled or not. */
    public final static String ENABLE_PROPERTY = "enable";
    /** The property name for a custom blueprint class in the entity blueprint. */
    public final static String BLUEPRINT_CUSTOM_CLASS_PROPERTY = "blueprint-class";
    /** The property name for a custom entity class in the entity blueprint. */
    public final static String ENTITY_CUSTOM_CLASS_PROPERTY = "class";

    /** A map that holds blueprints indexed by their entity type. */
    private static final Map<String, DataBlueprint<Entity>> blueprints = new Hashtable<>();

    /**
     * Retrieves the blueprint for the specified entity type.
     *
     * @param type The type of the entity.
     * @return The {@code DataBlueprint} associated with the specified entity type, or {@code null} if not found.
     */
    public static DataBlueprint<Entity> getBlueprint(String type) {
        return blueprints.get(type);
    }

    /**
     * Loads all the entity blueprints from the project directory, parsing the relevant files and creating the
     * corresponding {@code Blueprint} objects.
     *
     * @throws ReflectionException If a reflection operation fails while loading the blueprint class.
     * @throws InvocationTargetException If an exception occurs while invoking a method via reflection.
     * @throws NoSuchMethodException If a required method is not found via reflection.
     * @throws InstantiationException If the blueprint class cannot be instantiated.
     * @throws IllegalAccessException If an access control violation occurs during reflection.
     */
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
            log.debug(LOG_TOPIC, "loading '%s'", entityBlueprintFile);
            byte[] data = entityBlueprintFile.readBytes();
            if (data == null) {
                throw new RuntimeException(String.format("Can't load '%s' blueprint", entityType));
            }
            String rawData = new String(data);
            DataNode dataNode = JsonFormat.compact().deserialize(rawData);
            DataBlueprint<Entity> entityBlueprint = dataNode.contains(BLUEPRINT_CUSTOM_CLASS_PROPERTY) ? ReflectionUtils.newInstance(dataNode.get(BLUEPRINT_CUSTOM_CLASS_PROPERTY).getString()) : new EntityBlueprint(entityType);
            log.debug(LOG_TOPIC, "'%s' Blueprint (%s): %s", entityType, entityBlueprint.getClass().getName(), rawData);
            blueprints.put(entityType, entityBlueprint.loadData(dataNode));
        }
        log.debug(LOG_TOPIC, "Loaded '%d' entities blueprints", blueprints.size());
    }

    /**
     * Set the {@code EntityManager} manifest file located if exists in the state directory
     * @param manager The {@code EntityManager} instance created using the provided manifest
     * @param stateManifestDir The directory where the entity manager's manifest is located.
     */
    public static void setManagerManifest(EntityManager manager, FileHandle stateManifestDir) {
        FileHandle entityManagerManifest = stateManifestDir.child("entities.manifest.ois");
        if (!entityManagerManifest.exists() || entityManagerManifest.isDirectory()) {
            // Nothing to do
            return;
        }
        log.debug(String.format("found entities manifest at: %s", entityManagerManifest));
        manager.setManifest(entityManagerManifest);
    }

    /**
     * Loads and deserializes the manifest of entities from the provided manifest file.
     *
     * @param entityManagerManifest The file handle pointing to the entities manifest.
     * @return The deserialized {@code DataNode} containing the manifest data.
     */
    public static DataNode loadManifest(FileHandle entityManagerManifest) {
            byte[] data = entityManagerManifest.readBytes();
        if (data == null) {
            throw new RuntimeException(String.format("Can't load manifest '%s'", entityManagerManifest));
        }
        String rawData = new String(data);
        log.debug(LOG_TOPIC, "Loaded entities manifest: %s", rawData);
        return JsonFormat.compact().deserialize(rawData);
    }
}
