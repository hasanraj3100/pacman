package io.github.hasanraj3100.pacman;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private enum State { READY, PLAYING, WIN, GAME_OVER }

    private static final float READY_DURATION = 1.5f;
    private static final float HUD_HEIGHT = 48f;
    private static final float GHOST_CATCH_RADIUS = Maze.TILE_SIZE * 0.5f;

    private SpriteBatch batch;
    private TextureAtlas atlas;
    private BitmapFont font;
    private GlyphLayout layout;
    private TextureRegion dotRegion;
    private TextureRegion pelletRegion;

    private Maze maze;
    private Player player;
    private List<Ghost> ghosts;

    private State state;
    private float stateTimer;
    private float time;
    private int score;
    private int lives;

    @Override
    public void create() {
        batch = new SpriteBatch();
        atlas = new TextureAtlas(Gdx.files.internal("pacman.atlas"));
        font = new BitmapFont();
        font.getData().setScale(1.5f);
        layout = new GlyphLayout();
        dotRegion = atlas.findRegion("dot");
        pelletRegion = atlas.findRegion("pellet");

        Animation<TextureRegion> pacmanAnim = new Animation<>(0.08f, atlas.findRegions("pacman"), Animation.PlayMode.LOOP_PINGPONG);
        Animation<TextureRegion> redAnim = new Animation<>(0.2f, atlas.findRegions("ghost_red"), Animation.PlayMode.LOOP);
        Animation<TextureRegion> blueAnim = new Animation<>(0.2f, atlas.findRegions("ghost_blue"), Animation.PlayMode.LOOP);
        Animation<TextureRegion> pinkAnim = new Animation<>(0.2f, atlas.findRegions("ghost_pink"), Animation.PlayMode.LOOP);
        Animation<TextureRegion> orangeAnim = new Animation<>(0.2f, atlas.findRegions("ghost_orange"), Animation.PlayMode.LOOP);
        Animation<TextureRegion> scaredAnim = new Animation<>(0.2f, atlas.findRegions("ghost_scared"), Animation.PlayMode.LOOP);
        Animation<TextureRegion> scaredFlashAnim = new Animation<>(0.2f, atlas.findRegions("ghost_scared_flash"), Animation.PlayMode.LOOP);

        maze = new Maze();
        player = new Player(maze, pacmanAnim);
        ghosts = new ArrayList<>();
        ghosts.add(new Ghost(maze, redAnim, scaredAnim, scaredFlashAnim, Ghost.Direction.LEFT));
        ghosts.add(new Ghost(maze, blueAnim, scaredAnim, scaredFlashAnim, Ghost.Direction.RIGHT));
        ghosts.add(new Ghost(maze, pinkAnim, scaredAnim, scaredFlashAnim, Ghost.Direction.UP));
        ghosts.add(new Ghost(maze, orangeAnim, scaredAnim, scaredFlashAnim, Ghost.Direction.DOWN));

        startNewGame();
    }

    private void startNewGame() {
        score = 0;
        lives = 3;
        maze.resetDots();
        resetPositions();
        state = State.READY;
        stateTimer = READY_DURATION;
    }

    private void resetPositions() {
        player.resetPosition();
        for (Ghost g : ghosts) g.resetPosition();
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        time += delta;
        update(delta);
        draw();
    }

    private void update(float delta) {
        switch (state) {
            case READY:
                stateTimer -= delta;
                if (stateTimer <= 0f) state = State.PLAYING;
                break;
            case PLAYING:
                updatePlaying(delta);
                break;
            case WIN:
            case GAME_OVER:
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                    startNewGame();
                }
                break;
        }
    }

    private void updatePlaying(float delta) {
        player.handleInput();
        player.update(delta);
        for (Ghost g : ghosts) g.update(delta);

        int points = maze.eatAt(player.getCol(), player.getRow());
        if (points == Maze.EAT_PELLET) {
            for (Ghost g : ghosts) g.makeScared();
        }
        score += points;

        for (Ghost g : ghosts) {
            float dx = g.getCenterX() - player.getCenterX();
            float dy = g.getCenterY() - player.getCenterY();
            if (dx * dx + dy * dy <= GHOST_CATCH_RADIUS * GHOST_CATCH_RADIUS) {
                if (g.isScared()) {
                    g.resetPosition();
                    score += 200;
                } else {
                    loseLife();
                    return;
                }
            }
        }

        if (maze.getRemaining() <= 0) {
            state = State.WIN;
        }
    }

    private void loseLife() {
        lives--;
        if (lives <= 0) {
            state = State.GAME_OVER;
        } else {
            resetPositions();
            state = State.READY;
            stateTimer = READY_DURATION;
        }
    }

    private void draw() {
        ScreenUtils.clear(0.05f, 0.05f, 0.08f, 1f);
        batch.begin();
        maze.render(batch, dotRegion, pelletRegion, time);
        player.render(batch);
        for (Ghost g : ghosts) g.render(batch);
        drawHud();
        batch.end();
    }

    private void drawHud() {
        float mazeHeight = maze.rows * Maze.TILE_SIZE;
        float mazeWidth = maze.cols * Maze.TILE_SIZE;
        font.setColor(Color.WHITE);
        font.draw(batch, "SCORE: " + score, 12, mazeHeight + HUD_HEIGHT - 12);
        layout.setText(font, "LIVES: " + lives);
        font.draw(batch, layout, mazeWidth - layout.width - 12, mazeHeight + HUD_HEIGHT - 12);

        String message = null;
        switch (state) {
            case READY: message = "READY!"; break;
            case WIN: message = "YOU WIN!  Press Enter to restart"; break;
            case GAME_OVER: message = "GAME OVER  -  Press Enter to restart"; break;
            default: break;
        }
        if (message != null) {
            layout.setText(font, message);
            float x = (mazeWidth - layout.width) / 2f;
            float y = mazeHeight / 2f + layout.height / 2f;
            font.draw(batch, layout, x, y);
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        atlas.dispose();
        font.dispose();
        maze.dispose();
    }
}
