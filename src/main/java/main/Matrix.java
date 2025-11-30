package main;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Matrix {
    private Cell[][] grid;
    private int height, width;

    public Matrix(final int rows, final int cols) {
        this.height = rows;
        this.width = cols;
        this.grid = new Cell[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = new Cell(j, (rows - 1) - i);
            }
        }
    }

    public void placeEntity(final Entity e) {
        int inputX = e.getX();
        int inputY = e.getY();

        int matrixRow = (this.height - 1) - inputY;

        e.accept(grid[matrixRow][inputX]);
    }

    public Cell getCell(final int x, final int y) {
        int matrixRow = (this.height - 1) - y;
        return grid[matrixRow][x];
    }
}