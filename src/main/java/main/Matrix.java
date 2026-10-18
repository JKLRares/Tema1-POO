package main;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/** A fixed rectangular grid addressed with a Cartesian origin at the bottom left. */
@Getter
public final class Matrix {
    private final Cell[][] grid;
    private final int height;
    private final int width;

    public Matrix(final int rows, final int cols) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Matrix dimensions must be positive");
        }
        this.height = rows;
        this.width = cols;
        this.grid = new Cell[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = new Cell(j, rows - 1 - i);
            }
        }
    }

    /** Places an entity through its concrete type's cell attachment method. */
    public void placeEntity(final Entity entity) {
        entity.accept(getCell(entity.getX(), entity.getY()));
    }

    /** Returns the cell at (x, y), rejecting coordinates outside this grid. */
    public Cell getCell(final int x, final int y) {
        if (!contains(x, y)) {
            throw new IllegalArgumentException(
                    "Coordinates outside the world: (" + x + ", " + y + ")");
        }
        return grid[height - 1 - y][x];
    }

    /** Reports whether a coordinate belongs to this world. */
    public boolean contains(final int x, final int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /** Returns neighbors in deterministic north, east, south, west order. */
    public List<Cell> neighbors(final int x, final int y) {
        getCell(x, y);
        int[] dx = {0, 1, 0, -1};
        int[] dy = {1, 0, -1, 0};
        List<Cell> result = new ArrayList<>();
        for (int direction = 0; direction < dx.length; direction++) {
            int nx = x + dx[direction];
            int ny = y + dy[direction];
            if (contains(nx, ny)) {
                result.add(getCell(nx, ny));
            }
        }
        return List.copyOf(result);
    }

    /** Returns a copy of the grid structure; cell contents remain live domain objects. */
    public Cell[][] getGrid() {
        Cell[][] copy = new Cell[height][];
        for (int row = 0; row < height; row++) {
            copy[row] = grid[row].clone();
        }
        return copy;
    }
}
