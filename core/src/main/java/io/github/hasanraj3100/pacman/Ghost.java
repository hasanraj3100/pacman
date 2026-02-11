package io.github.hasanraj3100.pacman;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.List;

/** A ghost that wanders the maze: at each tile center it picks a random open, non-reverse direction. */
public class Ghost {
    public enum Direction { UP, DOWN, LEFT, RIGHT }

    private static final float SPEED = 4f * Maze.TILE_SIZE;
    private static final float SCARED_DURATION = 6f;
    private static final float FLASH_DURATION = 2f;

    private final Maze maze;
    private final Animation<TextureRegion> normalAnimation;
    private final Animation<TextureRegion> scaredAnimation;
    private final Animation<TextureRegion> scaredFlashAnimation;
    private final Direction startDirection;

    private float x, y;
    private Direction direction;
    private float animTime;
    private float scaredTimer;

    public Ghost(Maze maze, Animation<TextureRegion> normalAnimation, Animation<TextureRegion> scaredAnimation,
                 Animation<TextureRegion> scaredFlashAnimation, Direction startDirection) {
        this.maze = maze;
        this.normalAnimation = normalAnimation;
        this.scaredAnimation = scaredAnimation;
        this.scaredFlashAnimation = scaredFlashAnimation;
        this.startDirection = startDirection;
        resetPosition();
    }

    public final void resetPosition() {
        x = maze.colToX(maze.getGhostSpawnCol());
        y = maze.rowToY(maze.getGhostSpawnRow());
        direction = startDirection;
        scaredTimer = 0f;
        animTime = 0f;
    }

    public void makeScared() {
        scaredTimer = SCARED_DURATION;
    }

    public boolean isScared() {
        return scaredTimer > 0f;
    }

    public void update(float delta) {
        if (scaredTimer > 0f) scaredTimer = Math.max(0f, scaredTimer - delta);
        animTime += delta;

        int col = maze.xToCol(x);
        int row = maze.yToRow(y);
        float tileX = maze.colToX(col);
        float tileY = maze.rowToY(row);
        boolean centered = Math.abs(x - tileX) < 1f && Math.abs(y - tileY) < 1f;

        if (centered) {
            x = tileX;
            y = tileY;
            direction = pickDirection(col, row);
        }

        float speed = SPEED * (isScared() ? 0.6f : 1f);
        switch (direction) {
            case UP: y += speed * delta; break;
            case DOWN: y -= speed * delta; break;
            case LEFT: x -= speed * delta; break;
            case RIGHT: x += speed * delta; break;
        }
    }

    private Direction pickDirection(int col, int row) {
        Direction reverse = opposite(direction);
        List<Direction> options = new ArrayList<>();
        for (Direction d : Direction.values()) {
            if (d != reverse && canMove(col, row, d)) options.add(d);
        }
        if (options.isEmpty()) {
            return canMove(col, row, reverse) ? reverse : direction;
        }
        return options.get(MathUtils.random(options.size() - 1));
    }

    private boolean canMove(int col, int row, Direction d) {
        int nc = col;
        int nr = row;
        switch (d) {
            case UP: nr--; break;
            case DOWN: nr++; break;
            case LEFT: nc--; break;
            case RIGHT: nc++; break;
        }
        return maze.isWalkable(nc, nr);
    }

    private Direction opposite(Direction d) {
        switch (d) {
            case UP: return Direction.DOWN;
            case DOWN: return Direction.UP;
            case LEFT: return Direction.RIGHT;
            default: return Direction.LEFT;
        }
    }

    public float getCenterX() {
        return x + Maze.TILE_SIZE / 2f;
    }

    public float getCenterY() {
        return y + Maze.TILE_SIZE / 2f;
    }

    public void render(SpriteBatch batch) {
        Animation<TextureRegion> anim = normalAnimation;
        if (isScared()) {
            boolean flashOn = scaredTimer < FLASH_DURATION && ((int) (scaredTimer * 6f) % 2 == 0);
            anim = flashOn ? scaredFlashAnimation : scaredAnimation;
        }
        TextureRegion frame = anim.getKeyFrame(animTime, true);
        batch.draw(frame, x, y, Maze.TILE_SIZE, Maze.TILE_SIZE);
    }
}
