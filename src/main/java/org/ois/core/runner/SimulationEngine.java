package org.ois.core.runner;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class SimulationEngine extends ApplicationAdapter {

    // The engine runner configuration with information from the dynamic project (Graphic, Meta-data...)
    private final RunnerConfiguration configuration;

    public SimulationEngine(RunnerConfiguration configuration) {
        this.configuration = configuration;
    }

    private SpriteBatch batch;
    private Texture image;

    @Override
    public void create() {
        batch = new SpriteBatch();
        image = new Texture("testimage.png");
    }

    @Override
    public void render() {
        ScreenUtils.clear(1f, 0f, 0f, 1f);
        batch.begin();
        batch.draw(image, 140, 210);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
    }
}
