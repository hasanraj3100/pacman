package io.github.hasanraj3100.pacman;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private TextureAtlas atlas;
    private TextureRegion dotRegion;
    private TextureRegion pelletRegion;
    private Maze maze;
    private float time;

    @Override
    public void create() {
        batch = new SpriteBatch();
        atlas = new TextureAtlas(Gdx.files.internal("pacman.atlas"));
        dotRegion = atlas.findRegion("dot");
        pelletRegion = atlas.findRegion("pellet");
        maze = new Maze();
    }

    @Override
    public void render() {
        time += Gdx.graphics.getDeltaTime();
        ScreenUtils.clear(0.05f, 0.05f, 0.08f, 1f);
        batch.begin();
        maze.render(batch, dotRegion, pelletRegion, time);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        atlas.dispose();
        maze.dispose();
    }
}
