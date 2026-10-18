package main;

import main.animals.Carnivore;
import main.animals.Herbivore;
import main.plants.Ferns;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnimalBehaviorTest {
    @Test
    void animalCanFeedOnCurrentCellEvenWithoutAValidNeighbor() {
        Matrix map = DomainRegressionTest.map(1, 1);
        Herbivore animal = new Herbivore(0, 0, 10, true, "Deer", "Herbivores");
        map.placeEntity(animal);
        map.placeEntity(new Ferns(0, 0, "Ferns", "Fern", 2, 0, "YOUNG", true));
        ScannedAnimal tracked = new ScannedAnimal();
        tracked.addAnimal(animal);
        tracked.updateAll(1, map);
        assertThat(map.getCell(0, 0).getPlant()).isNull();
        assertThat(animal.getMass()).isEqualTo(12);
        assertThat(animal.getAnimalState()).isEqualTo("well-fed");
        tracked.updateAll(2, map);
        assertThat(map.getCell(0, 0).getSoil().getOrganicMatter()).isEqualTo(1.5);
    }

    @Test
    void feedingIsEveryTickWhileMovementIsEveryTwoTicks() {
        Matrix map = DomainRegressionTest.map(2, 2);
        Herbivore animal = new Herbivore(0, 0, 10, true, "Deer", "Herbivores");
        map.placeEntity(animal);
        ScannedAnimal tracked = new ScannedAnimal();
        tracked.addAnimal(animal);
        tracked.updateAll(1, map);
        assertThat(map.getCell(0, 0).getAnimal()).isSameAs(animal);
        tracked.updateAll(2, map);
        assertThat(map.getCell(0, 1).getAnimal()).isSameAs(animal);
        tracked.updateAll(3, map);
        assertThat(map.getCell(0, 1).getAnimal()).isSameAs(animal);
        tracked.updateAll(4, map);
        assertThat(map.getCell(1, 1).getAnimal()).isSameAs(animal);
    }

    @Test
    void predatorDoesNotConsumeItselfAndDrinkingConservesMass() {
        Matrix map = DomainRegressionTest.map(1, 1);
        Carnivore predator = new Carnivore(0, 0, 10, true, "Wolf", "Carnivores");
        Water water = new Water(0, 0, 100, true, "Pond", "pond", 0, 7.5, 100,
                0, 0, 0, false, 0);
        map.placeEntity(predator);
        map.placeEntity(water);
        ScannedAnimal tracked = new ScannedAnimal();
        tracked.addAnimal(predator);
        tracked.updateAll(1, map);
        assertThat(predator.getMass()).isEqualTo(10.8);
        assertThat(predator.getMass() + water.getMass()).isEqualTo(110);
        tracked.updateAll(2, map);
        assertThat(predator.getMass()).isCloseTo(11.664, org.assertj.core.data.Offset.offset(1e-12));
        assertThat(predator.getMass() + water.getMass()).isEqualTo(110);
        assertThat(map.getCell(0, 0).getAnimal()).isSameAs(predator);
    }
}
