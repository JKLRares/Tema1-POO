package main;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public abstract class Alive extends Entity {
	public Alive(int x, int y, double mass, boolean scanned, String name, String type) {
		super(x, y, mass, scanned, name, type);
	}

	public abstract void die(Entity entity);
}