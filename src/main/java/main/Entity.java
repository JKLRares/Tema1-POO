package main;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public abstract class Entity {
	private final int x, y;
    private double mass;
    private boolean scanned;
    private String name;
    private String type;

	public Entity(int x, int y, double mass, boolean scanned, String name, String type) {
		this.x = x;
        this.y = y;
        this.mass = mass;
        this.scanned = false;
        this.name = name;
        this.type = type;
	}

    public abstract EntityType getEntityType();
}