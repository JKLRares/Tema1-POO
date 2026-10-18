package main;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public abstract class Entity {
    private int x;
    private int y;
    private double mass;
    private boolean scanned;
    private String name;
    private String type;

    public Entity(final int x, final int y, final double mass, final boolean scanned,
                  final String name, final String type) {
        this.x = x;
        this.y = y;
        this.mass = mass;
        this.scanned = scanned;
        this.name = name;
        this.type = type;
    }

    /** Attaches this concrete entity to its corresponding slot in a cell. */

    public abstract void accept(Cell cell);
}
