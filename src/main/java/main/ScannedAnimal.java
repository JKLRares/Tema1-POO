package main;

import java.util.LinkedHashSet;
import java.util.Set;

/** Tracks scanned animals in scan order without a fixed capacity limit. */
public final class ScannedAnimal {
    private final Set<Animal> scannedAnimals = new LinkedHashSet<>();

    /** Registers each animal identity once. */
    public void addAnimal(final Animal animal) {
        scannedAnimals.add(animal);
    }

    /** Updates every tick after scanning, ignoring animals removed from the world. */
    public void updateAll(final int currentTimestamp, final Matrix map) {
        scannedAnimals.removeIf(animal -> animal.getMass() <= 0
                || map.getCell(animal.getX(), animal.getY()).getAnimal() != animal);
        for (Animal animal : scannedAnimals) {
            int elapsed = currentTimestamp - animal.getScanTimestamp();
            if (elapsed > 0
                    && map.getCell(animal.getX(), animal.getY()).getAnimal() == animal) {
                animal.tryToAct(map, currentTimestamp);
            }
        }
    }
}
