package io.github.hasanraj3100.pacman;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** Owns the hardcoded maze grid: wall collision, dot/pellet state, and their rendering. */
public class Maze {
    public static final int TILE_SIZE = 32;

    private static final String[] LAYOUT = {
        "#################",
        "#P.............P#",
        "#.###.#.#.#.###.#",
        "#.###.#.#.#.###.#",
        "#...............#",
        "#.#.###.#.###.#.#",
        "#.#.....#.....#.#",
        "#.###.#####.###.#",
        "#...............#",
        "##.##.# X #.##.##",
        "#...............#",
        "#.###.#####.###.#",
        "#.#.....#.....#.#",
        "#.#.###.#.###.#.#",
        "#...............#",
        "#.###.#.#.#.###.#",
        "#.###.#.#.#.###.#",
        "#P.............P#",
        "#################",
    };

    private static final Color WALL_COLOR = new Color(0.30f, 0.65f, 1f, 1f);

    public final int cols;
    public final int rows;

    private final boolean[][] wall;
    private final boolean[][] dot;
    private final boolean[][] pellet;
    private int remaining;

    private final int playerStartCol;
    private final int playerStartRow;
    private final int ghostSpawnCol;
    private final int ghostSpawnRow;

    private final Texture whitePixel;

    public Maze() {
        rows = LAYOUT.length;
        cols = LAYOUT[0].length();
        wall = new boolean[rows][cols];
        dot = new boolean[rows][cols];
        pellet = new boolean[rows][cols];

        int startCol = cols / 2;
        int startRow = rows - 5;
        int spawnCol = cols / 2;
        int spawnRow = rows / 2;

        for (int r = 0; r < rows; r++) {
            String line = LAYOUT[r];
            for (int c = 0; c < cols; c++) {
                char ch = line.charAt(c);
                switch (ch) {
                    case '#':
                        wall[r][c] = true;
                        break;
                    case '.':
                        dot[r][c] = true;
                        break;
                    case 'P':
                        pellet[r][c] = true;
                        break;
                    case 'X':
                        spawnCol = c;
                        spawnRow = r;
                        break;
                    default:
                        break;
                }
            }
        }
        playerStartCol = startCol;
        playerStartRow = startRow;
        ghostSpawnCol = spawnCol;
        ghostSpawnRow = spawnRow;

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        whitePixel = new Texture(pixmap);
        pixmap.dispose();

        resetDots();
    }

    /** Restores every dot/pellet from the original layout (used on win/game-over restart). */
    public final void resetDots() {
        remaining = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                dot[r][c] = LAYOUT[r].charAt(c) == '.';
                pellet[r][c] = LAYOUT[r].charAt(c) == 'P';
                if (dot[r][c] || pellet[r][c]) remaining++;
            }
        }
    }

    public boolean isWalkable(int col, int row) {
        if (col < 0 || col >= cols || row < 0 || row >= rows) return false;
        return !wall[row][col];
    }

    public static final int EAT_NONE = 0;
    public static final int EAT_DOT = 10;
    public static final int EAT_PELLET = 50;

    /** Consumes whatever is on this tile, if anything, returning the points earned. */
    public int eatAt(int col, int row) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) return EAT_NONE;
        if (dot[row][col]) {
            dot[row][col] = false;
            remaining--;
            return EAT_DOT;
        }
        if (pellet[row][col]) {
            pellet[row][col] = false;
            remaining--;
            return EAT_PELLET;
        }
        return EAT_NONE;
    }

    public int getRemaining() {
        return remaining;
    }

    public int getPlayerStartCol() {
        return playerStartCol;
    }

    public int getPlayerStartRow() {
        return playerStartRow;
    }

    public int getGhostSpawnCol() {
        return ghostSpawnCol;
    }

    public int getGhostSpawnRow() {
        return ghostSpawnRow;
    }

    /** Screen-space x for the left edge of a column (y-up, origin bottom-left). */
    public float colToX(int col) {
        return col * TILE_SIZE;
    }

    /** Screen-space y for the bottom edge of a row (row 0 is the top of the maze). */
    public float rowToY(int row) {
        return (rows - 1 - row) * TILE_SIZE;
    }

    public int xToCol(float x) {
        return Math.round(x / TILE_SIZE);
    }

    public int yToRow(float y) {
        return rows - 1 - Math.round(y / TILE_SIZE);
    }

    public void render(SpriteBatch batch, TextureRegion dotRegion, TextureRegion pelletRegion, float time) {
        batch.setColor(WALL_COLOR);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (wall[r][c]) {
                    batch.draw(whitePixel, colToX(c), rowToY(r), TILE_SIZE, TILE_SIZE);
                }
            }
        }
        batch.setColor(Color.WHITE);

        float dotSize = TILE_SIZE * 0.2f;
        float pelletSize = TILE_SIZE * (0.45f + 0.1f * (float) Math.sin(time * 4f));
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float cx = colToX(c) + TILE_SIZE / 2f;
                float cy = rowToY(r) + TILE_SIZE / 2f;
                if (dot[r][c]) {
                    batch.draw(dotRegion, cx - dotSize / 2f, cy - dotSize / 2f, dotSize, dotSize);
                } else if (pellet[r][c]) {
                    batch.draw(pelletRegion, cx - pelletSize / 2f, cy - pelletSize / 2f, pelletSize, pelletSize);
                }
            }
        }
    }

    public void dispose() {
        whitePixel.dispose();
    }
}
