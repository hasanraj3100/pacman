package io.github.hasanraj3100.pacman;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

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
    private List<Ghost> ghosts;
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
        Animation<TextureRegion> redAnim = new Animation<>(0.2f, atlas.findRegions("ghost_red"), Animation.PlayMode.LOOP);
        Animation<TextureRegion> blueAnim = new Animation<>(0.2f, atlas.findRegions("ghost_blue"), Animation.PlayMode.LOOP);
        Animation<TextureRegion> pinkAnim = new Animation<>(0.2f, atlas.findRegions("ghost_pink"), Animation.PlayMode.LOOP);
        Animation<TextureRegion> orangeAnim = new Animation<>(0.2f, atlas.findRegions("ghost_orange"), Animation.PlayMode.LOOP);

        maze = new Maze();
        player = new Player(maze, pacmanAnim);
        ghosts = new ArrayList<>();
        ghosts.add(new Ghost(maze, redAnim, Ghost.Direction.LEFT));
        ghosts.add(new Ghost(maze, blueAnim, Ghost.Direction.RIGHT));
        ghosts.add(new Ghost(maze, pinkAnim, Ghost.Direction.UP));
        ghosts.add(new Ghost(maze, orangeAnim, Ghost.Direction.DOWN));
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        time += delta;
        player.handleInput();
        player.update(delta);
        for (Ghost g : ghosts) g.update(delta);
        score += maze.eatAt(player.getCol(), player.getRow());

        ScreenUtils.clear(0.05f, 0.05f, 0.08f, 1f);
        batch.begin();
        maze.render(batch, dotRegion, pelletRegion, time);
        player.render(batch);
        for (Ghost g : ghosts) g.render(batch);
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
