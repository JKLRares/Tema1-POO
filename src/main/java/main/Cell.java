package main;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public final class Cell {
    private final int x;
    private final int y;
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
