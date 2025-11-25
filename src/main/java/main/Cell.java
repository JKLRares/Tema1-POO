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

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void addEntity(Entity e) {
        switch (e.getEntityType()) {
            case PLANT -> this.plant = (Plant) e;
            case ANIMAL -> this.animal = (Animal) e;
            case WATER -> this.water = (Water) e;
            case SOIL -> this.soil = (Soil) e;
            case AIR -> this.air = (Air) e;
        }
    }
}