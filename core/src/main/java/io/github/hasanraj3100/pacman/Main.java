package io.github.hasanraj3100.pacman;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private static final float HUD_HEIGHT = 48f;

    private SpriteBatch batch;
    private TextureAtlas atlas;
    private BitmapFont font;
    private TextureRegion dotRegion;
    private TextureRegion pelletRegion;
    private Maze maze;
    private Player player;
    private float time;
    private int score;

    @Override
    public void create() {
        batch = new SpriteBatch();
        atlas = new TextureAtlas(Gdx.files.internal("pacman.atlas"));
        font = new BitmapFont();
        font.getData().setScale(1.5f);
        dotRegion = atlas.findRegion("dot");
        pelletRegion = atlas.findRegion("pellet");
        Animation<TextureRegion> pacmanAnim = new Animation<>(0.08f, atlas.findRegions("pacman"), Animation.PlayMode.LOOP_PINGPONG);

        maze = new Maze();
        player = new Player(maze, pacmanAnim);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        time += delta;
        player.handleInput();
        player.update(delta);
        score += maze.eatAt(player.getCol(), player.getRow());

        ScreenUtils.clear(0.05f, 0.05f, 0.08f, 1f);
        batch.begin();
        maze.render(batch, dotRegion, pelletRegion, time);
        player.render(batch);
        font.draw(batch, "SCORE: " + score, 12, maze.rows * Maze.TILE_SIZE + HUD_HEIGHT - 12);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        atlas.dispose();
        font.dispose();
        maze.dispose();
    }
}
