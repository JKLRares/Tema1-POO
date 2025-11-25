package main.animals;

import main.Animal;
import lombok.Getter;

@Getter
public class Omnivore extends Animal {
    public Omnivore(int x, int y, double mass, boolean scanned, String name, String type, double airQual, double waterQual) {
        super(x, y, mass, scanned, name, type, airQual, waterQual);
    }

    @Override
    public int getAttackProb() {
        return 60;
    }
}
