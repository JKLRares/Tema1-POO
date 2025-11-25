package main;
import lombok.Getter;

@Getter
public class Matrix {
    private Cell[][] grid;
    private int height;
    private int width;

    public Matrix(int rows, int cols) {
        this.height = rows;
        this.width = cols;
        this.grid = new Cell[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = new Cell(j, (rows - 1) - i);
            }
        }
    }

    public void placeEntity(Entity e) {
        int inputX = e.getX();
        int inputY = e.getY();

        int matrixRow = (this.height - 1) - inputY;

        grid[matrixRow][inputX].addEntity(e);
    }

    public Cell getCell(int x, int y) {
        int matrixRow = (this.height - 1) - y;
        return grid[matrixRow][x];
    }
}