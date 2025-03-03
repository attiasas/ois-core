package org.ois.core.runner;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;

public class GdxFilesMock implements Files {

    FileHandle dir;

    public GdxFilesMock() {
        this("");
    }

    public GdxFilesMock(String dirPath) {
        dir = new FileHandle(dirPath);
    }

    @Override
    public FileHandle getFileHandle(String path, FileType type) {
        return dir;
    }

    @Override
    public FileHandle classpath(String path) {
        return dir;
    }

    @Override
    public FileHandle internal(String path) {
        return dir;
    }

    @Override
    public FileHandle external(String path) {
        return dir;
    }

    @Override
    public FileHandle absolute(String path) {
        return dir;
    }

    @Override
    public FileHandle local(String path) {
        return dir;
    }

    @Override
    public String getExternalStoragePath() {
        return dir.path();
    }

    @Override
    public boolean isExternalStorageAvailable() {
        return false;
    }

    @Override
    public String getLocalStoragePath() {
        return dir.path();
    }

    @Override
    public boolean isLocalStorageAvailable() {
        return false;
    }
}
