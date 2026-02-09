package io.github.hasanraj3100.pacman;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** Pacman: grid-aligned movement with a buffered turn, driven by the arrow keys or WASD. */
public class Player {
    public enum Direction { UP, DOWN, LEFT, RIGHT }

    private static final float SPEED = 4.5f * Maze.TILE_SIZE;

    private final Maze maze;
    private final Animation<TextureRegion> animation;

    private float x, y;
    private Direction direction = Direction.LEFT;
    private Direction desiredDirection = Direction.LEFT;
    private float animTime;
    private boolean moving;

    public Player(Maze maze, Animation<TextureRegion> animation) {
        this.maze = maze;
        this.animation = animation;
        resetPosition();
    }

    public final void resetPosition() {
        x = maze.colToX(maze.getPlayerStartCol());
        y = maze.rowToY(maze.getPlayerStartRow());
        direction = Direction.LEFT;
        desiredDirection = Direction.LEFT;
        moving = false;
        animTime = 0f;
    }

    public void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            desiredDirection = Direction.UP;
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            desiredDirection = Direction.DOWN;
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            desiredDirection = Direction.LEFT;
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            desiredDirection = Direction.RIGHT;
        }
    }

    public void update(float delta) {
        int col = maze.xToCol(x);
        int row = maze.yToRow(y);
        float tileX = maze.colToX(col);
        float tileY = maze.rowToY(row);
        boolean centered = Math.abs(x - tileX) < 1f && Math.abs(y - tileY) < 1f;

        if (centered) {
            x = tileX;
            y = tileY;
            if (canMove(col, row, desiredDirection)) {
                direction = desiredDirection;
            }
            moving = canMove(col, row, direction);
        }

        if (moving) {
            switch (direction) {
                case UP: y += SPEED * delta; break;
                case DOWN: y -= SPEED * delta; break;
                case LEFT: x -= SPEED * delta; break;
                case RIGHT: x += SPEED * delta; break;
            }
            animTime += delta;
        }
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

    public int getCol() {
        return maze.xToCol(x);
    }

    public int getRow() {
        return maze.yToRow(y);
    }

    public float getCenterX() {
        return x + Maze.TILE_SIZE / 2f;
    }

    public float getCenterY() {
        return y + Maze.TILE_SIZE / 2f;
    }

    public void render(SpriteBatch batch) {
        TextureRegion frame = moving ? animation.getKeyFrame(animTime, true) : animation.getKeyFrame(0);
        float size = Maze.TILE_SIZE;
        float rotation;
        switch (direction) {
            case RIGHT: rotation = 0f; break;
            case UP: rotation = 90f; break;
            case LEFT: rotation = 180f; break;
            case DOWN: default: rotation = 270f; break;
        }
        batch.draw(frame, x, y, size / 2f, size / 2f, size, size, 1f, 1f, rotation);
    }
}
