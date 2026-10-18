package main;

import fileio.CommandInput;
import main.air.Desert;
import main.air.Temperat;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnvironmentTest {
    @Test
    void waterEffectsStartTwoTicksAfterScanRegardlessOfTimestampParity() {
        for (int scanTime : new int[]{3, 4}) {
            Matrix map = DomainRegressionTest.map(1, 1);
            Water water = new Water(0, 0, 100, true, "Water", "pond", 0, 7.5, 100,
                    0, 0, 0, false, 0);
            water.setScanTimestamp(scanTime);
            map.placeEntity(water);
            EnvironmentUpdater updater = new EnvironmentUpdater();
            ScannedAnimal animals = new ScannedAnimal();
            updater.update(map, animals, scanTime + 1);
            assertThat(map.getCell(0, 0).getSoil().getWaterRetention()).isEqualTo(4.0);
            updater.update(map, animals, scanTime + 2);
            assertThat(map.getCell(0, 0).getSoil().getWaterRetention()).isEqualTo(4.1);
            assertThat(map.getCell(0, 0).getAir().getHumidity()).isEqualTo(60.1);
        }
    }

    @Test
    void waterDoesNotDiscardExistingSoilPrecision() {
        Matrix map = DomainRegressionTest.map(1, 1);
        map.getCell(0, 0).getSoil().setWaterRetention(0.72);
        map.placeEntity(new Water(0, 0, 100, true, "Water", "pond", 0, 7.5, 100,
                0, 0, 0, false, 0));
        new EnvironmentUpdater().update(map, new ScannedAnimal(), 2);
        assertThat(map.getCell(0, 0).getSoil().getWaterRetention()).isEqualTo(0.82);
    }

    @Test
    void weatherSurvivesRecalculationAndQualityRemainsBounded() {
        Temperat air = new Temperat(0, 0, 100, false, "Air", "TemperateAir", 100, 20, 100, 0);
        CommandInput command = new CommandInput();
        command.setType("newSeason");
        command.setSeason("Spring");
        assertThat(air.event(command)).isTrue();
        assertThat(air.getAirQual()).isEqualTo(85);
        air.updateEnvironmentParams(0.1, 10);
        assertThat(air.getAirQual()).isEqualTo(85);
        air.clearWeather();
        assertThat(air.getAirQual()).isEqualTo(100);
    }

    @Test
    void desertStormIsClampedAndClearsItsMetadata() {
        Desert air = new Desert(0, 0, 100, false, "Air", "DesertAir", 15, 38, 19, 22);
        CommandInput command = new CommandInput();
        command.setType("desertStorm");
        command.setDesertStorm(true);
        air.event(command);
        assertThat(air.getAirQual()).isZero();
        assertThat(air.isDesertStorm()).isTrue();
        air.clearWeather();
        assertThat(air.isDesertStorm()).isFalse();
        assertThat(air.getAirQual()).isEqualTo(22.2);
    }

    @Test
    void frozenWaterPreservesItsDeclaredState() {
        Water water = new Water(0, 0, 100, false, "Ice", "lake", 0, 7.5, 100,
                0, 0, 0, true, 0);
        assertThat(water.isFrozen()).isTrue();
        assertThat(water.getFrozenScore()).isEqualTo(1);
    }
    @Test
    void plantHonorsInitialGrowthAndHasOneConsistentLifecycleState() {
        Plant plant = new main.plants.Ferns(0, 0, "Ferns", "Fern", 1, 0.8, "YOUNG", true);
        assertThat(plant.getGrowingLevel()).isEqualTo(0.8);
        plant.setGrowingLevel(plant.getGrowingLevel() + 0.2);
        plant.grow();
        assertThat(plant.getMaturity()).isEqualTo(PlantMaturity.MATURE);
        assertThat(plant.getLevel()).isEqualTo("MATURE");
        plant.setGrowingLevel(1);
        plant.grow();
        assertThat(plant.getMaturity()).isEqualTo(PlantMaturity.OLD);
        plant.setGrowingLevel(1);
        plant.grow();
        assertThat(plant.getLevel()).isEqualTo("DEAD");
        assertThat(plant.getMaturityModifier()).isZero();
    }

    @Test
    void laterWeatherInAnotherClimateDoesNotExtendAnEarlierEvent() {
        Matrix map = DomainRegressionTest.map(1, 2);
        Desert desert = new Desert(1, 0, 100, false, "Air", "DesertAir", 15, 38, 19, 22);
        map.placeEntity(desert);
        CommandInput spring = new CommandInput();
        spring.setType("newSeason");
        spring.setSeason("Spring");
        spring.setTimestamp(0);
        map.getCell(0, 0).getAir().event(spring);
        CommandInput storm = new CommandInput();
        storm.setType("desertStorm");
        storm.setDesertStorm(true);
        storm.setTimestamp(1);
        desert.event(storm);
        EnvironmentUpdater updater = new EnvironmentUpdater();
        updater.update(map, new ScannedAnimal(), 2);
        assertThat(map.getCell(0, 0).getAir().getAirQual()).isEqualTo(82);
        assertThat(desert.isDesertStorm()).isTrue();
        updater.update(map, new ScannedAnimal(), 3);
        assertThat(desert.isDesertStorm()).isFalse();
        assertThat(desert.getAirQual()).isEqualTo(22.2);
    }

}
