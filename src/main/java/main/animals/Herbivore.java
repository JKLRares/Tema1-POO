package main.animals;

import main.Animal;
import lombok.Getter;

@Getter
public class Herbivore extends Animal {
    public Herbivore(int x, int y, double mass, boolean scanned, String name, String type) {
        super(x, y, mass, scanned, name, type);
    }

    @Override
    public int getAttackPossibility() {
        return 85;
    }
}
