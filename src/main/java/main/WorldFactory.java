package main;

import fileio.AirInput;
import fileio.AnimalInput;
import fileio.PairInput;
import fileio.PlantInput;
import fileio.SimulationInput;
import fileio.SoilInput;
import fileio.TerritorySectionParamsInput;
import fileio.WaterInput;
import java.util.List;
import java.util.Locale;
import main.air.Desert;
import main.air.Montan;
import main.air.Polar;
import main.air.Temperat;
import main.air.Tropical;
import main.animals.Carnivore;
import main.animals.Detritivores;
import main.animals.Herbivore;
import main.animals.Omnivore;
import main.animals.Parasites;
import main.plants.Algae;
import main.plants.Ferns;
import main.plants.FloweringPlants;
import main.plants.Gymnosperms;
import main.plants.Mosses;
import main.soils.DesertSoil;
import main.soils.ForestSoil;
import main.soils.GrasslandSoil;
import main.soils.SwampSoil;
import main.soils.TundraSoil;

/** Converts input records into independent entities for each occupied cell. */
public final class WorldFactory {
    private static final double PURITY_DIVISOR = 100.0;
    private static final double INITIAL_GROWTH = 0.0;

    /** Builds a fresh domain world from validated simulation parameters. */
    public Matrix create(final SimulationInput params) {
        String dim = params.getTerritoryDim();
        String[] parts = dim.split("x");
        int rows = Integer.parseInt(parts[0]);
        int cols = Integer.parseInt(parts[1]);

        Matrix newMatrix = new Matrix(rows, cols);
        TerritorySectionParamsInput sections = params.getTerritorySectionParams();

        if (sections.getSoil() != null) {
            parseSoils(sections.getSoil(), newMatrix);
        }
        if (sections.getPlants() != null) {
            parsePlants(sections.getPlants(), newMatrix);
        }
        if (sections.getAnimals() != null) {
            parseAnimals(sections.getAnimals(), newMatrix);
        }
        if (sections.getWater() != null) {
            parseWater(sections.getWater(), newMatrix);
        }
        if (sections.getAir() != null) {
            parseAir(sections.getAir(), newMatrix);
        }

        return newMatrix;
    }

    private void parseSoils(final List<SoilInput> list, final Matrix world) {
        for (SoilInput entry : list) {
            String type = entry.getType();
            for (PairInput loc : entry.getSections()) {
                Soil newSoil = null;
                if (type.equalsIgnoreCase("ForestSoil")) {
                    newSoil = new ForestSoil(loc.getX(), loc.getY(), entry.getMass(), false,
                            entry.getName(), type, entry.getNitrogen(), entry.getWaterRetention(),
                            entry.getSoilpH(), entry.getOrganicMatter(), entry.getLeafLitter());
                } else if (type.equalsIgnoreCase("SwampSoil")) {
                    newSoil = new SwampSoil(loc.getX(), loc.getY(), entry.getMass(), false,
                            entry.getName(), type, entry.getNitrogen(), entry.getWaterRetention(),
                            entry.getSoilpH(), entry.getOrganicMatter(), entry.getWaterLogging());
                } else if (type.equalsIgnoreCase("TundraSoil")) {
                    newSoil = new TundraSoil(loc.getX(), loc.getY(), entry.getMass(), false,
                            entry.getName(), type, entry.getNitrogen(), entry.getWaterRetention(),
                            entry.getSoilpH(), entry.getOrganicMatter(),
                            entry.getPermafrostDepth());
                } else if (type.equalsIgnoreCase("DesertSoil")) {
                    newSoil = new DesertSoil(loc.getX(), loc.getY(), entry.getMass(), false,
                            entry.getName(), type, entry.getNitrogen(), entry.getWaterRetention(),
                            entry.getSoilpH(), entry.getOrganicMatter(), entry.getSalinity());
                } else if (type.equalsIgnoreCase("GrasslandSoil")) {
                    newSoil = new GrasslandSoil(loc.getX(), loc.getY(), entry.getMass(), false,
                            entry.getName(), type, entry.getNitrogen(), entry.getWaterRetention(),
                            entry.getSoilpH(), entry.getOrganicMatter(), entry.getRootDensity());
                }
                if (newSoil != null) {
                    world.placeEntity(newSoil);
                }
            }
        }
    }

    private void parsePlants(final List<PlantInput> list, final Matrix world) {
        for (PlantInput entry : list) {
            String type = entry.getType();
            String typeUpper = type.toUpperCase(Locale.ROOT);

            for (PairInput loc : entry.getSections()) {
                Plant newPlant = null;
                if (typeUpper.contains("FLOWER")) {
                    newPlant = new FloweringPlants(loc.getX(), loc.getY(), type, entry.getName(),
                            entry.getMass(), INITIAL_GROWTH, "YOUNG", false);
                } else if (typeUpper.contains("FERN")) {
                    newPlant = new Ferns(loc.getX(), loc.getY(), type, entry.getName(),
                            entry.getMass(), INITIAL_GROWTH, "YOUNG", false);
                } else if (typeUpper.contains("ALGAE")) {
                    newPlant = new Algae(loc.getX(), loc.getY(), type, entry.getName(),
                            entry.getMass(), INITIAL_GROWTH, "YOUNG", false);
                } else if (typeUpper.contains("MOSS")) {
                    newPlant = new Mosses(loc.getX(), loc.getY(), type, entry.getName(),
                            entry.getMass(), INITIAL_GROWTH, "YOUNG", false);
                } else if (typeUpper.contains("GYMNOSPERM")) {
                    newPlant = new Gymnosperms(loc.getX(), loc.getY(), type, entry.getName(),
                            entry.getMass(), INITIAL_GROWTH, "YOUNG", false);
                }
                if (newPlant != null) {
                    world.placeEntity(newPlant);
                }
            }
        }
    }

    private void parseAnimals(final List<AnimalInput> list, final Matrix world) {
        for (AnimalInput entry : list) {
            String type = entry.getType();
            String typeUpper = type.toUpperCase(Locale.ROOT);

            for (PairInput loc : entry.getSections()) {
                Animal newAnimal = null;
                if (typeUpper.contains("HERBIVORE")) {
                    newAnimal = new Herbivore(loc.getX(), loc.getY(), entry.getMass(), false,
                            entry.getName(), type);
                } else if (typeUpper.contains("CARNIVORE")) {
                    newAnimal = new Carnivore(loc.getX(), loc.getY(), entry.getMass(), false,
                            entry.getName(), type);
                } else if (typeUpper.contains("OMNIVORE")) {
                    newAnimal = new Omnivore(loc.getX(), loc.getY(), entry.getMass(), false,
                            entry.getName(), type);
                } else if (typeUpper.contains("DETRITIVORE")) {
                    newAnimal = new Detritivores(loc.getX(), loc.getY(), entry.getMass(), false,
                            entry.getName(), type);
                } else if (typeUpper.contains("PARASITE")) {
                    newAnimal = new Parasites(loc.getX(), loc.getY(), entry.getMass(), false,
                            entry.getName(), type);
                }
                if (newAnimal != null) {
                    world.placeEntity(newAnimal);
                }
            }
        }
    }

    private void parseWater(final List<WaterInput> list, final Matrix world) {
        for (WaterInput entry : list) {
            double waterQuality = entry.getPurity() / PURITY_DIVISOR;
            int frozenScore = 0;

            for (PairInput loc : entry.getSections()) {
                Water newWater = new Water(loc.getX(), loc.getY(), entry.getMass(), false,
                        entry.getName(), entry.getType(), entry.getSalinity(), entry.getPH(),
                        entry.getPurity(), entry.getTurbidity(), entry.getContaminantIndex(),
                        waterQuality, entry.isFrozen(), frozenScore);
                world.placeEntity(newWater);
            }
        }
    }

    private void parseAir(final List<AirInput> list, final Matrix world) {
        for (AirInput entry : list) {
            String type = entry.getType();
            for (PairInput loc : entry.getSections()) {
                Air newAir = null;
                double temp = entry.getTemperature();

                if (type.equalsIgnoreCase("MountainAir")) {
                    newAir = new Montan(loc.getX(), loc.getY(), entry.getMass(), false,
                            entry.getName(), type, entry.getHumidity(), temp,
                            entry.getOxygenLevel(), entry.getAltitude());
                } else if (type.equalsIgnoreCase("TemperateAir")) {
                    newAir = new Temperat(loc.getX(), loc.getY(), entry.getMass(), false,
                            entry.getName(), type, entry.getHumidity(), temp,
                            entry.getOxygenLevel(), entry.getPollenLevel());
                } else if (type.equalsIgnoreCase("TropicalAir")) {
                    newAir = new Tropical(loc.getX(), loc.getY(), entry.getMass(), false,
                            entry.getName(), type, entry.getHumidity(), temp,
                            entry.getOxygenLevel(), entry.getCo2Level());
                } else if (type.equalsIgnoreCase("DesertAir")) {
                    newAir = new Desert(loc.getX(), loc.getY(), entry.getMass(), false,
                            entry.getName(), type, entry.getHumidity(), temp,
                            entry.getOxygenLevel(), entry.getDustParticles());
                } else if (type.equalsIgnoreCase("PolarAir")) {
                    newAir = new Polar(loc.getX(), loc.getY(), entry.getMass(), false,
                            entry.getName(), type, entry.getHumidity(), temp,
                            entry.getOxygenLevel(), entry.getIceCrystalConcentration());
                }
                if (newAir != null) {
                    world.placeEntity(newAir);
                }
            }
        }
    }

}
