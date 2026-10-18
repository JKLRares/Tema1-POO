package main;

import main.animals.Carnivore;
import main.animals.Herbivore;
import main.air.Temperat;
import main.soils.ForestSoil;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainRegressionTest {
    static Matrix map(int height, int width) {
        Matrix map = new Matrix(height, width);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map.placeEntity(new Temperat(x, y, 1000, false, "Air", "TemperateAir",
                        60, 20, 20, 0));
                map.placeEntity(new ForestSoil(x, y, 1000, false, "Soil", "ForestSoil",
                        2, 4, 7, 1, 0));
            }
        }
        return map;
    }

    @Test
    void robotHonorsInitialPosition() {
        Robot robot = new Robot(2, 3, 20);
        assertThat(robot.getRobotX()).isEqualTo(2);
        assertThat(robot.getRobotY()).isEqualTo(3);
    }

    @Test
    void isolatedRobotDoesNotSpendEnergyOrReportMovement() {
        Robot robot = new Robot(0, 0, 100);
        assertThat(robot.moveRobot(map(1, 1))).isFalse();
        assertThat(robot.getRobotBattery()).isEqualTo(100);
    }

    @Test
    void eatenScannedAnimalCannotReturnAndOverwritePredator() {
        Matrix map = map(1, 2);
        Carnivore predator = new Carnivore(0, 0, 10, false, "Wolf", "Carnivores");
        Herbivore prey = new Herbivore(1, 0, 2, false, "Deer", "Herbivores");
        map.placeEntity(predator);
        map.placeEntity(prey);
        prey.setScanned(true);
        ScannedAnimal tracked = new ScannedAnimal();
        tracked.addAnimal(prey);
        predator.setScanned(true);
        predator.tryToAct(map, 2);
        tracked.updateAll(2, map);
        assertThat(map.getCell(1, 0).getAnimal()).isSameAs(predator);
        assertThat(map.getCell(0, 0).getAnimal()).isNull();
    }

    @Test
    void constructorPreservesScannedState() {
        Herbivore animal = new Herbivore(0, 0, 2, true, "Deer", "Herbivores");
        assertThat(animal.isScanned()).isTrue();
    }

    @Test
    void inventoryDoesNotExposeMutableKnowledge() {
        Inventory inventory = new Inventory();
        inventory.addItem("Fern");
        inventory.addFact("Fern", "Method to plant Fern");
        assertThatThrownBy(() -> inventory.getCollectedItems().clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> inventory.getKnowledgeBase().get("Fern").clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void unknownImprovementCannotBeAuthorizedByEmptyFact() {
        Inventory inventory = new Inventory();
        inventory.addItem("Fern");
        inventory.addFact("Fern", "");
        assertThat(inventory.checkRequirement("Fern", "unknown")).isFalse();
    }
    @Test
    void trackingSupportsMoreThanOneHundredAnimals() {
        Matrix map = map(1, 202);
        ScannedAnimal tracked = new ScannedAnimal();
        Herbivore last = null;
        for (int i = 0; i < 101; i++) {
            Herbivore animal = new Herbivore(i * 2, 0, 2, true, "Deer" + i, "Herbivores");
            map.placeEntity(animal);
            tracked.addAnimal(animal);
            tracked.addAnimal(animal);
            last = animal;
        }
        tracked.updateAll(2, map);
        assertThat(last.getX()).isEqualTo(201);
    }

    @Test
    void humidityRecipeAcceptsTheTwoExistingPunctuationVariants() {
        for (String fact : java.util.List.of("Method to increase humidity", "Method to increase humidity.")) {
            Inventory inventory = new Inventory();
            inventory.addItem("Water");
            assertThat(inventory.addFact("Water", fact)).isTrue();
            assertThat(inventory.checkRequirement("Water", "increaseHumidity")).isTrue();
        }
    }

}
