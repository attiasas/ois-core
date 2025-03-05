package org.ois.core.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Disposable;

public class DebugManager implements Disposable {

    private final String devModeDir;

    private final Stage stage;
    private final Label label;
    private final StringBuilder stringBuilder;

    public DebugManager(String devModeDir) {
        this.devModeDir = devModeDir;

        stage = new Stage();
        BitmapFont font = new BitmapFont();
        label = new Label(" ", new Label.LabelStyle(font, Color.WHITE));
        stage.addActor(label);
        stringBuilder = new StringBuilder();
    }

    public boolean isDevMode() {
        return this.devModeDir != null && !this.devModeDir.isBlank();
    }

    public void render() {
        stringBuilder.setLength(0);
        stringBuilder.append(" FPS: ").append(Gdx.graphics.getFramesPerSecond());
        label.setText(stringBuilder);
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
