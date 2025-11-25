package main;

import com.fasterxml.jackson.databind.JsonNode;
import main.soils.*;
import main.plants.*;
import main.animals.*;
import main.air.*;

public class InputLoader {
    public Matrix loadWorld(JsonNode jsonInput) {

        JsonNode paramsArray = jsonInput.get("simulationParams");
        JsonNode params = paramsArray.get(0);

        String dim = params.get("territoryDim").asText();
        String[] parts = dim.split("x");
        int rows = Integer.parseInt(parts[0]);
        int cols = Integer.parseInt(parts[1]);

        Matrix matrix = new Matrix(rows, cols);

        JsonNode sectionParams = params.get("territorySectionParams");

        if (sectionParams.has("soil"))    parseSoils(sectionParams.get("soil"), matrix);
        if (sectionParams.has("plants"))  parsePlants(sectionParams.get("plants"), matrix);
        if (sectionParams.has("animals")) parseAnimals(sectionParams.get("animals"), matrix);
        if (sectionParams.has("water"))   parseWater(sectionParams.get("water"), matrix);
        if (sectionParams.has("air"))     parseAir(sectionParams.get("air"), matrix);

        return matrix;
    }

    private void parseSoils(JsonNode list, Matrix matrix) {
        for (JsonNode entry : list) {

            String type = entry.get("type").asText();
            String name = entry.get("name").asText();
            double mass = entry.get("mass").asDouble();
            double nitrogen = entry.get("nitrogen").asDouble();
            double waterRetention = entry.get("waterRetention").asDouble();
            double ph = entry.get("soilpH").asDouble();
            double organic = entry.get("organicMatter").asDouble();

            JsonNode coords = entry.get("sections");

            for (JsonNode loc : coords) {
                int x = loc.get("x").asInt();
                int y = loc.get("y").asInt();

                Soil newSoil = null;

                if (type.equalsIgnoreCase("ForestSoil")) {
                    double leafLitter = entry.get("leafLitter").asDouble();
                    newSoil = new ForestSoil(x, y, mass, false, name, type, nitrogen, waterRetention, ph, organic, leafLitter);
                }
                else if (type.equalsIgnoreCase("SwampSoil")) {
                    double waterLogging = entry.get("waterLogging").asDouble();
                    newSoil = new SwampSoil(x, y, mass, false, name, type, nitrogen, waterRetention, ph, organic, waterLogging);
                }
                else if (type.equalsIgnoreCase("TundraSoil")) {
                    double permafrost = entry.get("permafrostDepth").asDouble();
                    newSoil = new TundraSoil(x, y, mass, false, name, type, nitrogen, waterRetention, ph, organic, permafrost);
                }
                else if (type.equalsIgnoreCase("DesertSoil")) {
                    double salinity = entry.get("salinity").asDouble();
                    newSoil = new DesertSoil(x, y, mass, false, name, type, nitrogen, waterRetention, ph, organic, salinity);
                }
                else if (type.equalsIgnoreCase("GrasslandSoil")) {
                    double rootDensity = entry.get("rootDensity").asDouble();
                    newSoil = new GrasslandSoil(x, y, mass, false, name, type, nitrogen, waterRetention, ph, organic, rootDensity);
                }

                if (newSoil != null) matrix.placeEntity(newSoil);
            }
        }
    }

    private void parsePlants(JsonNode list, Matrix matrix) {
        for (JsonNode entry : list) {

            String type = entry.get("type").asText();
            String name = entry.get("name").asText();
            double mass = entry.get("mass").asDouble();

            JsonNode coords = entry.get("sections");

            for (JsonNode loc : coords) {
                int x = loc.get("x").asInt();
                int y = loc.get("y").asInt();

                Plant newPlant = null;

                if (type.equalsIgnoreCase("FloweringPlants")) {
                    newPlant = new FloweringPlants(x, y, type, name, mass, 0.0, "YOUNG", false);
                }
                else if (type.equalsIgnoreCase("Ferns")) {
                    newPlant = new Ferns(x, y, type, name, mass, 0.0, "YOUNG", false);
                }
                else if (type.equalsIgnoreCase("Algae")) {
                    newPlant = new Algae(x, y, type, name, mass, 0.0, "YOUNG", false);
                }
                else if (type.equalsIgnoreCase("Mosses")) {
                    newPlant = new Mosses(x, y, type, name, mass, 0.0, "YOUNG", false);
                }
                else if (type.equalsIgnoreCase("Gymnosperms")) {
                    newPlant = new Gymnosperms(x, y, type, name, mass, 0.0, "YOUNG", false);
                }

                if (newPlant != null) matrix.placeEntity(newPlant);
            }
        }
    }

    private void parseAnimals(JsonNode list, Matrix matrix) {
        for (JsonNode entry : list) {

            String typeStr = entry.get("type").asText();
            String name = entry.get("name").asText();
            double mass = entry.get("mass").asDouble();

            JsonNode coords = entry.get("sections");

            for (JsonNode loc : coords) {
                int x = loc.get("x").asInt();
                int y = loc.get("y").asInt();

                Animal newAnimal = null;

                if (typeStr.toUpperCase().contains("HERBIVORE")) {
                    newAnimal = new Herbivore(x, y, mass, false, name, typeStr, 0.0, 0.0);
                }
                else if (typeStr.toUpperCase().contains("CARNIVORE")) {
                    newAnimal = new Carnivore(x, y, mass, false, name, typeStr, 0.0, 0.0);
                }
                else if (typeStr.toUpperCase().contains("OMNIVORE")) {
                    newAnimal = new Omnivore(x, y, mass, false, name, typeStr, 0.0, 0.0);
                }
                else if (typeStr.toUpperCase().contains("DETRITIVORE")) {
                    newAnimal = new Detritivores(x, y, mass, false, name, typeStr, 0.0, 0.0);
                }
                else if (typeStr.toUpperCase().contains("PARASITE")) {
                    newAnimal = new Parasites(x, y, mass, false, name, typeStr, 0.0, 0.0);
                }

                if (newAnimal != null) matrix.placeEntity(newAnimal);
            }
        }
    }

    private void parseWater(JsonNode list, Matrix matrix) {
        for (JsonNode entry : list) {

            String type = entry.get("type").asText();
            String name = entry.get("name").asText();
            double mass = entry.get("mass").asDouble();

            double purity = entry.get("purity").asDouble();
            double salinity = entry.get("salinity").asDouble();
            double ph = entry.get("pH").asDouble();
            double turbidity = entry.get("turbidity").asDouble();
            double contaminant = entry.get("contaminantIndex").asDouble();
            boolean isFrozen = entry.get("isFrozen").asBoolean();

            double waterQuality = purity / 100.0;
            int frozenScore = 0;

            JsonNode coords = entry.get("sections");

            for (JsonNode loc : coords) {
                int x = loc.get("x").asInt();
                int y = loc.get("y").asInt();

                Water newWater = new Water(x, y, mass, false, name, type, salinity, ph, purity, turbidity, contaminant, waterQuality, isFrozen, frozenScore);
                matrix.placeEntity(newWater);
            }
        }
    }

    private void parseAir(JsonNode list, Matrix matrix) {
        for (JsonNode entry : list) {

            String type = entry.get("type").asText();
            String name = entry.get("name").asText();
            double mass = entry.get("mass").asDouble();
            double humidity = entry.get("humidity").asDouble();
            double oxygen = entry.get("oxygenLevel").asDouble();

            double temp = entry.path("temperature").asDouble(20.0);

            JsonNode coords = entry.get("sections");

            for (JsonNode loc : coords) {
                int x = loc.get("x").asInt();
                int y = loc.get("y").asInt();

                Air newAir = null;

                if (type.equalsIgnoreCase("MountainAir")) {
                    double altitude = entry.get("altitude").asDouble();
                    newAir = new Montan(x, y, mass, false, name, type, humidity, temp, oxygen, altitude);
                }
                else if (type.equalsIgnoreCase("TemperateAir")) {
                    double pollen = entry.get("pollenLevel").asDouble();
                    newAir = new Temperat(x, y, mass, false, name, type, humidity, temp, oxygen, pollen);
                }
                else if (type.equalsIgnoreCase("TropicalAir")) {
                    double co2 = entry.get("co2Level").asDouble();
                    newAir = new Tropical(x, y, mass, false, name, type, humidity, temp, oxygen, co2);
                }
                else if (type.equalsIgnoreCase("DesertAir")) {
                    double dustParticles = entry.get("dustParticles").asDouble();
                    newAir = new Desert(x, y, mass, false, name, type, humidity, temp, oxygen, dustParticles);
                }
                else if (type.equalsIgnoreCase("PolarAir")) {
                    double iceCrystal = entry.get("iceCrystalConcentration").asDouble();
                    newAir = new Polar(x, y, mass, false, name, type, humidity, temp, oxygen, iceCrystal);
                }

                if (newAir != null) matrix.placeEntity(newAir);
            }
        }
    }
}