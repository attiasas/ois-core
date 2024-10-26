package org.ois.core.project;

public class Assets {

    public static final String ASSETS_DIRECTORY = "assets";

    public static String get(String pathRelativeToAssetsDir) {
        return ASSETS_DIRECTORY + "/" + pathRelativeToAssetsDir;
    }
}
