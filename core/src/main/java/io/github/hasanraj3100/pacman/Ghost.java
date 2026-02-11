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

    private final Maze maze;
    private final Animation<TextureRegion> normalAnimation;
    private final Direction startDirection;

    private float x, y;
    private Direction direction;
    private float animTime;

    public Ghost(Maze maze, Animation<TextureRegion> normalAnimation, Direction startDirection) {
        this.maze = maze;
        this.normalAnimation = normalAnimation;
        this.startDirection = startDirection;
        resetPosition();
    }

    public final void resetPosition() {
        x = maze.colToX(maze.getGhostSpawnCol());
        y = maze.rowToY(maze.getGhostSpawnRow());
        direction = startDirection;
        animTime = 0f;
    }

    public void update(float delta) {
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

        switch (direction) {
            case UP: y += SPEED * delta; break;
            case DOWN: y -= SPEED * delta; break;
            case LEFT: x -= SPEED * delta; break;
            case RIGHT: x += SPEED * delta; break;
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
        TextureRegion frame = normalAnimation.getKeyFrame(animTime, true);
        batch.draw(frame, x, y, Maze.TILE_SIZE, Maze.TILE_SIZE);
    }
}
