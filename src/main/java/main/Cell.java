package main;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Cell {
    private int x, y;
    private Plant plant;
    private Animal animal;
    private Water water;
    private Soil soil;
    private Air air;

    public Cell(final int x, final int y) {
        this.x = x;
        this.y = y;
    }
}