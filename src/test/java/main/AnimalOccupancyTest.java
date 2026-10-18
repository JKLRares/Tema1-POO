package main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.animals.Carnivore;
import main.animals.Detritivores;
import main.animals.Herbivore;
import main.animals.Omnivore;
import main.animals.Parasites;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.data.Offset.offset;

class AnimalOccupancyTest {
    @TempDir
    Path directory;

    enum Species {
        CARNIVORE, PARASITE, HERBIVORE, OMNIVORE, DETRITIVORE;

        Animal create(int x, int y) {
            return switch (this) {
                case CARNIVORE -> new Carnivore(x, y, 10, true, "Wolf", "Carnivores");
                case PARASITE -> new Parasites(x, y, 10, true, "Parasite", "Parasites");
                case HERBIVORE -> new Herbivore(x, y, 10, true, "Deer", "Herbivores");
                case OMNIVORE -> new Omnivore(x, y, 10, true, "Bear", "Omnivores");
                case DETRITIVORE -> new Detritivores(x, y, 10, true, "Worm", "Detritivores");
            };
        }
    }

    private Water water(double mass, boolean scanned) {
        return new Water(0, 0, mass, scanned, "Pond", "pond", 0, 7.5, 100,
                0, 0, 0, false, 0);
    }

    private Animal prey(int x, int y) {
        return new Herbivore(x, y, 2, false, "Prey", "Herbivores");
    }

    @Test
    void originalJsonScenarioConsumesPreyAndPreservesItsMass() throws Exception {
        ObjectNode input = SimulationFixtures.input("""
                [{"command":"startSimulation","timestamp":0},
                 {"command":"scanObject","color":"none","smell":"none","sound":"none","timestamp":0},
                 {"command":"scanObject","color":"gray","smell":"musky","sound":"howl","timestamp":0},
                 {"command":"printMap","timestamp":0},
                 {"command":"printEnvConditions","timestamp":1},
                 {"command":"printMap","timestamp":2},
                 {"command":"printEnvConditions","timestamp":2},
                 {"command":"moveTo","targetX":1,"targetY":0,"timestamp":2},
                 {"command":"printEnvConditions","timestamp":2}]
                """);
        ObjectNode world = SimulationFixtures.world(1, 2);
        input.putArray("simulationParams").add(world);
        ObjectNode sections = (ObjectNode) world.get("territorySectionParams");
        var animals = sections.putArray("animals");
        ObjectNode wolf = animals.addObject().put("type", "Carnivores")
                .put("name", "Wolf").put("mass", 10);
        wolf.putArray("sections").addObject().put("x", 0).put("y", 0);
        ObjectNode deer = animals.addObject().put("type", "Herbivores")
                .put("name", "Deer").put("mass", 2);
        deer.putArray("sections").addObject().put("x", 1).put("y", 0);
        ObjectNode pond = sections.putArray("water").addObject().put("type", "pond")
                .put("name", "Pond").put("mass", 100).put("purity", 100)
                .put("salinity", 0).put("turbidity", 0).put("contaminantIndex", 0)
                .put("pH", 7.5).put("isFrozen", false);
        pond.putArray("sections").addObject().put("x", 0).put("y", 0);
        Path source = directory.resolve("input.json");
        Path output = directory.resolve("output.json");
        SimulationFixtures.JSON.writeValue(source.toFile(), input);

        Main.action(source.toString(), output.toString());

        JsonNode result = SimulationFixtures.JSON.readTree(output.toFile());
        assertThat(result.get(3).get("output").get(0).get("totalNrOfObjects").asInt()).isEqualTo(2);
        assertThat(result.get(5).get("output").get(0).get("totalNrOfObjects").asInt()).isEqualTo(1);
        assertThat(result.get(5).get("output").get(1).get("totalNrOfObjects").asInt()).isEqualTo(1);
        JsonNode current = result.get(6).get("output");
        assertThat(current.has("animals")).isFalse();
        assertThat(current.get("water").get("mass").asDouble()).isEqualTo(98.34);
        JsonNode destination = result.get(8).get("output");
        assertThat(destination.get("animals").get("name").asText()).isEqualTo("Wolf");
        assertThat(destination.get("animals").get("mass").asDouble()).isEqualTo(13.66);
        assertThat(destination.get("animals").get("mass").asDouble()
                + current.get("water").get("mass").asDouble()).isEqualTo(112);
    }

    @ParameterizedTest
    @EnumSource(value = Species.class, names = {"CARNIVORE", "PARASITE"})
    void fedPredatorConsumesTheOccupantBeforeMovingAndTransfersItsMass(Species species) {
        Matrix map = DomainRegressionTest.map(1, 2);
        Animal predator = species.create(0, 0);
        predator.setScanTimestamp(5);
        Animal prey = prey(1, 0);
        Water water = water(100, true);
        map.placeEntity(predator);
        map.placeEntity(prey);
        map.placeEntity(water);
        ScannedAnimal tracked = new ScannedAnimal();
        tracked.addAnimal(predator);

        tracked.updateAll(6, map);
        tracked.updateAll(7, map);

        assertThat(predator.isFed()).isTrue();
        assertThat(predator.getX()).isEqualTo(1);
        assertThat(predator.getY()).isZero();
        assertThat(map.getCell(0, 0).getAnimal()).isNull();
        assertThat(map.getCell(1, 0).getAnimal()).isSameAs(predator);
        assertThat(prey.getMass()).isZero();
        assertThat(predator.getMass()).isCloseTo(13.664, offset(1e-12));
        assertThat(predator.getFoodEatenCount()).isEqualTo(3);
        assertThat(predator.getMass() + prey.getMass() + water.getMass()).isCloseTo(112, offset(1e-12));
    }

    @ParameterizedTest
    @EnumSource(value = Species.class, names = {"CARNIVORE", "PARASITE"})
    void hungryPredatorConsumesPreyBeforeEnteringAndPreyCannotActAgain(Species species) {
        Matrix map = DomainRegressionTest.map(1, 2);
        Animal predator = species.create(0, 0);
        Animal prey = prey(1, 0);
        prey.setScanned(true);
        map.placeEntity(predator);
        map.placeEntity(prey);
        ScannedAnimal tracked = new ScannedAnimal();
        tracked.addAnimal(predator);
        tracked.addAnimal(prey);

        tracked.updateAll(1, map);
        assertThat(map.getCell(0, 0).getAnimal()).isSameAs(predator);
        assertThat(map.getCell(1, 0).getAnimal()).isSameAs(prey);
        tracked.updateAll(2, map);

        assertThat(map.getCell(0, 0).getAnimal()).isNull();
        assertThat(map.getCell(1, 0).getAnimal()).isSameAs(predator);
        assertThat(predator.getMass()).isEqualTo(12);
        assertThat(predator.getFoodEatenCount()).isEqualTo(1);
        assertThat(prey.getMass()).isZero();
        tracked.updateAll(3, map);
        assertThat(map.getCell(1, 0).getAnimal()).isSameAs(predator);
        assertThat(predator.getMass()).isEqualTo(12);
    }

    @ParameterizedTest
    @EnumSource(Species.class)
    void allAnimalTypesCanStillMoveIntoAnEmptyDestination(Species species) {
        Matrix map = DomainRegressionTest.map(1, 2);
        Animal animal = species.create(0, 0);
        map.placeEntity(animal);
        ScannedAnimal tracked = new ScannedAnimal();
        tracked.addAnimal(animal);

        tracked.updateAll(2, map);

        assertThat(map.getCell(0, 0).getAnimal()).isNull();
        assertThat(map.getCell(1, 0).getAnimal()).isSameAs(animal);
        assertThat(animal.getX()).isEqualTo(1);
        assertThat(animal.getMass()).isEqualTo(10);
    }

    @Test
    void directMovementRejectsAnOccupiedCellWithoutMutatingEitherAnimal() {
        Matrix map = DomainRegressionTest.map(1, 2);
        Animal animal = Species.CARNIVORE.create(0, 0);
        Animal occupant = prey(1, 0);
        map.placeEntity(animal);
        map.placeEntity(occupant);

        assertThatThrownBy(() -> animal.move(map, map.getCell(1, 0)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("occupied");

        assertThat(map.getCell(0, 0).getAnimal()).isSameAs(animal);
        assertThat(map.getCell(1, 0).getAnimal()).isSameAs(occupant);
        assertThat(animal.getX()).isZero();
        assertThat(occupant.getX()).isEqualTo(1);
        assertThat(animal.getMass()).isEqualTo(10);
        assertThat(occupant.getMass()).isEqualTo(2);
    }

    @ParameterizedTest
    @EnumSource(value = Species.class, names = {"CARNIVORE", "PARASITE"})
    void fedPredatorKeepsNeighborPriorityAndConsumesAnOccupiedDestination(Species species) {
        Matrix map = DomainRegressionTest.map(2, 2);
        Animal predator = species.create(0, 0);
        Animal prey = prey(0, 1);
        map.placeEntity(predator);
        map.placeEntity(prey);
        map.placeEntity(water(100, true));
        ScannedAnimal tracked = new ScannedAnimal();
        tracked.addAnimal(predator);

        tracked.updateAll(1, map);
        tracked.updateAll(2, map);

        assertThat(map.getCell(0, 0).getAnimal()).isNull();
        assertThat(map.getCell(0, 1).getAnimal()).isSameAs(predator);
        assertThat(map.getCell(1, 0).getAnimal()).isNull();
        assertThat(prey.getMass()).isZero();
        assertThat(predator.getMass()).isCloseTo(13.664, offset(1e-12));
    }

    @ParameterizedTest
    @EnumSource(value = Species.class, names = {"CARNIVORE", "PARASITE"})
    void predatorCanConsumeAnotherPreyOnALaterMovementTick(Species species) {
        Matrix map = DomainRegressionTest.map(1, 3);
        Animal predator = species.create(0, 0);
        Animal prey = prey(1, 0);
        Animal nextPrey = prey(2, 0);
        Water water = water(0.8, false);
        map.placeEntity(predator);
        map.placeEntity(prey);
        map.placeEntity(nextPrey);
        map.placeEntity(water);
        ScannedAnimal tracked = new ScannedAnimal();
        tracked.addAnimal(predator);

        tracked.updateAll(1, map);
        water.setScanned(true);
        tracked.updateAll(2, map);
        assertThat(map.getCell(0, 0).getWater()).isNull();
        assertThat(map.getCell(0, 0).getAnimal()).isNull();
        assertThat(map.getCell(1, 0).getAnimal()).isSameAs(predator);
        assertThat(prey.getMass()).isZero();
        assertThat(predator.getMass()).isEqualTo(12.8);
        tracked.updateAll(3, map);
        assertThat(map.getCell(1, 0).getAnimal()).isSameAs(predator);
        assertThat(map.getCell(2, 0).getAnimal()).isSameAs(nextPrey);
        assertThat(predator.isFed()).isFalse();
        tracked.updateAll(4, map);

        assertThat(map.getCell(0, 0).getAnimal()).isNull();
        assertThat(map.getCell(1, 0).getAnimal()).isNull();
        assertThat(map.getCell(2, 0).getAnimal()).isSameAs(predator);
        assertThat(prey.getMass()).isZero();
        assertThat(nextPrey.getMass()).isZero();
        assertThat(predator.getMass()).isEqualTo(14.8);
        assertThat(predator.getFoodEatenCount()).isEqualTo(3);
    }

    @ParameterizedTest
    @EnumSource(value = Species.class, names = {"HERBIVORE", "OMNIVORE", "DETRITIVORE"})
    void nonPredatorsContinueToRejectOccupiedDestinations(Species species) {
        Matrix map = DomainRegressionTest.map(1, 2);
        Animal animal = species.create(0, 0);
        Animal occupant = prey(1, 0);
        map.placeEntity(animal);
        map.placeEntity(occupant);
        ScannedAnimal tracked = new ScannedAnimal();
        tracked.addAnimal(animal);

        tracked.updateAll(2, map);

        assertThat(map.getCell(0, 0).getAnimal()).isSameAs(animal);
        assertThat(map.getCell(1, 0).getAnimal()).isSameAs(occupant);
        assertThat(animal.getMass()).isEqualTo(10);
        assertThat(occupant.getMass()).isEqualTo(2);
    }
}
