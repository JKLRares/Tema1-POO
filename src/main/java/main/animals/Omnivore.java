package main.animals;

import main.Animal;
import lombok.Getter;

@Getter
public class Omnivore extends Animal {
    public Omnivore(int x, int y, double mass, boolean scanned, String name, String type) {
        super(x, y, mass, scanned, name, type);
    }

    @Override
    public int getAttackPossibility() {
        return 60;
    }
}
