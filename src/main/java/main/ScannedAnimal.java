package main;

public class ScannedAnimal {
    private final Animal[] scannedAnimals;
    private int count;

    public ScannedAnimal() {
        this.scannedAnimals = new Animal[100];
        this.count = 0;
    }

    public void addAnimal(Animal animal) {
        for (int i = 0; i < count; i++) {
            if (scannedAnimals[i] == animal) {
                return;
            }
        }

        if (count < scannedAnimals.length) {
            scannedAnimals[count] = animal;
            count++;
        }
    }

    public void updateAll(final int currentTimestamp, final Matrix map) {
        for (int i = 0; i < count; i++) {
            Animal animal = scannedAnimals[i];

            if (animal != null && animal.getMass() > 0) {

                int scanTime = animal.getScanTimestamp();
                int timeDiff = currentTimestamp - scanTime;
                if (timeDiff % 2 == 0) {
                    animal.tryToAct(map, currentTimestamp);
                }
            }
        }
    }
}
