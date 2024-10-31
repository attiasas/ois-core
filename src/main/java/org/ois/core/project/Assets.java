package org.ois.core.project;

/**
 * Static access to resources
 */
public class Assets {
    /** The directory name inside the simulation directory that holds all the project assets for the simulation **/
    public static final String ASSETS_DIRECTORY = "assets";

    /**
     * Return the path to a project asset
     * @param pathRelativeToAssetsDir - a path relative from the project simulation/assets directory.
     * @return - the path to an asset
     */
    public static String get(String pathRelativeToAssetsDir) {
        return ASSETS_DIRECTORY + "/" + pathRelativeToAssetsDir;
    }
}
