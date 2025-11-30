package main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
import fileio.*;
import main.soils.*;
import main.plants.*;
import main.animals.*;
import main.air.*;

import java.util.List;

public class CommandExecutor {
    private Matrix matrix;
    private Robot robot;
    private final ObjectMapper mapper;
    private final InputLoader inputLoader;
    private ScannedAnimal scannedAnimals = new ScannedAnimal();
    private Inventory inventory = new Inventory();

    private boolean simulationRunning = false;
    private int simulationIndex = 0, robotChargingTimestamp = 0, changeWeatherTimestamp = 0, previousTimestamp = 0;

    public CommandExecutor(InputLoader inputLoader, ObjectMapper mapper) {
        this.inputLoader = inputLoader;
        this.mapper = mapper;
    }

    private void updateEnvironment(int timestamp) {
        for (int x = 0; x < matrix.getWidth(); x++) {
            for (int y = 0; y < matrix.getHeight(); y++) {
                Cell cell = matrix.getCell(x, y);

                if (cell.getSoil() != null && cell.getPlant() != null && cell.getPlant().isScanned()) {
                    cell.getPlant().setGrowingLevel(cell.getPlant().getGrowingLevel() + 0.2);
                }

                if (cell.getWater() != null && cell.getWater().isScanned()) {
                    if (timestamp % 2 != 0) {
                        if (cell.getAir() != null) {
                            cell.getAir().setHumidity(Math.round((cell.getAir().getHumidity() + 0.1) * 10.0) / 10.0);
                            cell.getAir().setAirQual(cell.getAir().airQuality());
                        }
                        if (cell.getSoil() != null) {
                            cell.getSoil().setWaterRetention(Math.round((cell.getSoil().getWaterRetention() + 0.1) *
                                    10.0) / 10.0);
                        }
                    }
                    if (cell.getPlant() != null && cell.getPlant().isScanned()) {
                        cell.getPlant().setGrowingLevel(cell.getPlant().getGrowingLevel() + 0.2);
                    }
                }
                if (cell.getPlant() != null && cell.getPlant().isScanned()) {
                    cell.getPlant().grow();
                    if (cell.getPlant().getLevel().equals("DEAD")) {
                        cell.setPlant(null);
                    }
                }
                if (cell.getPlant() != null && cell.getAir() != null && cell.getPlant().isScanned()) {
                    double oxygenAdd = cell.getPlant().getOxygenOutput() + cell.getPlant().getMaturityModifier();
                    cell.getAir().setOxygenLevel(Math.round((cell.getAir().getOxygenLevel() + oxygenAdd) * 10.0) /
                            10.0);
                    cell.getAir().setAirQual(cell.getAir().airQuality());
                }
            }
        }
        scannedAnimals.updateAll(timestamp, matrix);
    }

    public void executeCommands(ArrayNode outputArray) {
        List<CommandInput> commands = inputLoader.getCommands();

        for (CommandInput cmd : commands) {
            String commandName = cmd.getCommand();
            int timestamp = cmd.getTimestamp();
            ObjectNode resultNode = mapper.createObjectNode();
            resultNode.put("command", commandName);

            if (commandName.equals("startSimulation")) {
                if (simulationIndex < inputLoader.getSimulations().size()) {
                    SimulationInput simInput = inputLoader.getSimulations().get(simulationIndex);
                    this.matrix = loadWorld(simInput);
                    this.robot = new Robot(0, 0, simInput.getEnergyPoints());
                    simulationIndex++;
                    simulationRunning = true;
                    previousTimestamp = timestamp;
                    resultNode.put("message", "Simulation has started.");
                } else {
                    resultNode.put("message", "ERROR: Simulation already started. Cannot perform action");
                }

                resultNode.put("timestamp", timestamp);
                outputArray.add(resultNode);
                continue;
            }

            if (commandName.equals("endSimulation")) {
                if (!simulationRunning) {
                    resultNode.put("message", "ERROR: Simulation not started. Cannot perform action");
                } else {
                    simulationRunning = false;
                    resultNode.put("message", "Simulation has ended.");
                }
                resultNode.put("timestamp", timestamp);
                outputArray.add(resultNode);
                continue;
            }

            if (!simulationRunning) {
                resultNode.put("message", "ERROR: Simulation not started. Cannot perform action");
                resultNode.put("timestamp", timestamp);
                outputArray.add(resultNode);
                continue;
            } else {
                int cnt = timestamp - previousTimestamp;
                for (int i = 0; i < cnt; i++) {
                    updateEnvironment(previousTimestamp + i + 1);
                }
            }
            previousTimestamp = timestamp;

            switch (commandName) {
                case "printEnvConditions":
                    JsonNode conditions = getEnvConditions(robot.getRobotX(), robot.getRobotY());
                    if(timestamp < robotChargingTimestamp) {
                        resultNode.put("message", "ERROR: Robot still charging. Cannot perform action");
                    } else {
                        resultNode.set("output", conditions);
                    }
                    resultNode.put("timestamp", timestamp);
                    outputArray.add(resultNode);
                    break;

                case "printMap":
                    ArrayNode mapOutput = getMapOutput();
                    if(timestamp < robotChargingTimestamp) {
                        resultNode.put("message", "ERROR: Robot still charging. Cannot perform action");
                    } else {
                        resultNode.set("output", mapOutput);
                    }
                    resultNode.put("timestamp", timestamp);
                    outputArray.add(resultNode);
                    break;

                case "moveRobot":
                    if(timestamp < robotChargingTimestamp) {
                        resultNode.put("message", "ERROR: Robot still charging. Cannot perform action");
                    } else {
                        boolean moved = robot.moveRobot(matrix);
                        if (moved) {
                            String message = "The robot has successfully moved to position (" + robot.getRobotX() +
                                    ", " + robot.getRobotY() + ").";
                            resultNode.put("message", message);
                        } else {
                            resultNode.put("message", "ERROR: Not enough battery left. Cannot perform action");
                        }
                    }
                    resultNode.put("timestamp", timestamp);
                    outputArray.add(resultNode);
                    break;

                case "getEnergyStatus":
                    if(timestamp < robotChargingTimestamp) {
                        resultNode.put("message", "ERROR: Robot still charging. Cannot perform action");
                    } else if (robot != null) {
                        String msg = "TerraBot has " + robot.getRobotBattery() + " energy points left.";
                        resultNode.put("message", msg);
                    }
                    resultNode.put("timestamp", timestamp);
                    outputArray.add(resultNode);
                    break;

                case "rechargeBattery":
                    int time = cmd.getTimeToCharge();
                    if(timestamp < robotChargingTimestamp) {
                        resultNode.put("message", "ERROR: Robot still charging. Cannot perform action");
                    } else {
                        robot.setRobotBattery(robot.getRobotBattery() + time);
                        robotChargingTimestamp = timestamp + time;
                        resultNode.put("message", "Robot battery is charging.");
                    }
                    resultNode.put("timestamp", timestamp);
                    outputArray.add(resultNode);
                    break;

                case "changeWeatherConditions":
                    if(timestamp < robotChargingTimestamp) {
                        resultNode.put("message", "ERROR: Robot still charging. Cannot perform action");
                    } else {
                        boolean changed = false;
                        for (int x = 0; x < matrix.getWidth(); x++) {
                            for (int y = 0; y < matrix.getHeight(); y++) {
                                Cell cell = matrix.getCell(x, y);
                                if (cell.getAir() != null && cell.getAir().event(cmd)) {
                                    changed = true;
                                }
                            }
                        }
                        if(changed) {
                            changeWeatherTimestamp = timestamp + 2;
                            resultNode.put("message", "The weather has changed.");
                        } else {
                            resultNode.put("message", "ERROR: The weather change does not affect the environment. Cannot perform action");
                        }
                    }
                    resultNode.put("timestamp", timestamp);
                    outputArray.add(resultNode);
                    break;

                case "scanObject":
                    if(timestamp < robotChargingTimestamp) {
                        resultNode.put("message", "ERROR: Robot still charging. Cannot perform action");
                    } else {
                        int battery = robot.getRobotBattery() - 7;
                        boolean checked = false;
                        Cell cell = matrix.getCell(robot.getRobotX(), robot.getRobotY());
                        if(battery < 0) {
                            resultNode.put("message", "ERROR: Not enough energy to perform action");
                        } else {
                            if(cmd.getColor().equalsIgnoreCase("none") &&
                                    cmd.getSmell().equalsIgnoreCase("none") &&
                                    cmd.getSound().equalsIgnoreCase("none") && cell.getWater() != null) {
                                resultNode.put("message", "The scanned object is water.");
                                checked = true;
                                cell.getWater().setScanned(true);
                                inventory.addItem(cell.getWater().getName());
                            } else if(!cmd.getColor().equalsIgnoreCase("none") &&
                                    !cmd.getSmell().equalsIgnoreCase("none") &&
                                    cmd.getSound().equalsIgnoreCase("none") && cell.getPlant() != null) {
                                resultNode.put("message", "The scanned object is a plant.");
                                checked = true;
                                cell.getPlant().setScanned(true);
                                inventory.addItem(cell.getPlant().getName());
                            } else if(!cmd.getColor().equalsIgnoreCase("none") &&
                                    !cmd.getSmell().equalsIgnoreCase("none") &&
                                    !cmd.getSound().equalsIgnoreCase("none") && cell.getAnimal() != null) {
                                Animal foundAnimal = cell.getAnimal();
                                if(!foundAnimal.isScanned()) {
                                    foundAnimal.setScanned(true);
                                    foundAnimal.setScanTimestamp(timestamp);
                                    scannedAnimals.addAnimal(foundAnimal);
                                    inventory.addItem(cell.getAnimal().getName());
                                }
                                resultNode.put("message", "The scanned object is an animal.");
                                checked = true;
                            }
                            if(!checked) {
                                resultNode.put("message", "ERROR: Object not found. Cannot perform action");
                            } else {
                                robot.setRobotBattery(battery);
                            }
                        }
                    }
                    resultNode.put("timestamp", timestamp);
                    outputArray.add(resultNode);
                    break;

                case "learnFact":
                    if(timestamp < robotChargingTimestamp) {
                        resultNode.put("message", "ERROR: Robot still charging. Cannot perform action");
                    } else {
                        int cost = 2;
                        if (robot.getRobotBattery() < cost) {
                            resultNode.put("message",
                                    "ERROR: Not enough battery left. Cannot perform action");
                        } else {
                            String entityName = cmd.getComponents();
                            String factInfo = cmd.getSubject();

                            if (inventory.getCollectedItems().contains(entityName)) {
                                inventory.addFact(entityName, factInfo);
                                robot.setRobotBattery(robot.getRobotBattery() - cost);
                                resultNode.put("message",
                                        "The fact has been successfully saved in the database.");
                            } else {
                                resultNode.put("message",
                                        "ERROR: Subject not yet saved. Cannot perform action");
                            }
                        }
                    }
                    resultNode.put("timestamp", timestamp);
                    outputArray.add(resultNode);
                    break;

                case "printKnowledgeBase":
                    if(timestamp < robotChargingTimestamp) {
                        resultNode.put("message", "ERROR: Robot still charging. Cannot perform action");
                    } else {
                        ArrayNode output = mapper.createArrayNode();
                        List<String> items = inventory.getCollectedItems();
                        Map<String, List<String>> knowledgeBase = inventory.getKnowledgeBase();

                        for (String item : items) {
                            if (knowledgeBase.containsKey(item) && !knowledgeBase.get(item).isEmpty()) {
                                ObjectNode itemNode = mapper.createObjectNode();
                                itemNode.put("topic", item);

                                ArrayNode factsArray = mapper.createArrayNode();
                                for (String fact : knowledgeBase.get(item)) {
                                    factsArray.add(fact);
                                }
                                itemNode.set("facts", factsArray);
                                output.add(itemNode);
                            }
                        }
                        resultNode.set("output", output);
                    }
                    resultNode.put("timestamp", timestamp);
                    outputArray.add(resultNode);
                    break;

                case "improveEnvironment":
                    if(timestamp < robotChargingTimestamp) {
                        resultNode.put("message", "ERROR: Robot still charging. Cannot perform action");
                    } else {
                        int cost = 10;
                        if(robot.getRobotBattery() < cost) {
                            resultNode.put("message",
                                    "ERROR: Not enough battery left. Cannot perform action");
                        } else {
                            String improvementType = cmd.getImprovementType();
                            String name = cmd.getName();
                            if (!inventory.getCollectedItems().contains(name)) {
                                resultNode.put("message", "ERROR: Subject not yet saved. Cannot perform action");
                            } else if (!inventory.checkRequirement(name, improvementType)) {
                                resultNode.put("message", "ERROR: Fact not yet saved. Cannot perform action");
                            } else {
                                Cell currentCell = matrix.getCell(robot.getRobotX(), robot.getRobotY());
                                String successMessage = inventory.applyImprovement(currentCell, improvementType, name);
                                robot.setRobotBattery(robot.getRobotBattery() - 10);
                                resultNode.put("message", successMessage);
                            }
                        }
                    }
                    resultNode.put("timestamp", timestamp);
                    outputArray.add(resultNode);
                    break;
            }
            if (timestamp == changeWeatherTimestamp) {
                for (int x = 0; x < matrix.getWidth(); x++) {
                    for (int y = 0; y < matrix.getHeight(); y++) {
                        Cell cell = matrix.getCell(x, y);
                        if (cell.getAir() != null) {
                            cell.getAir().setAirQual(cell.getAir().airQuality());
                        }
                    }
                }
            }
        }
    }

    private Matrix loadWorld(SimulationInput params) {
        String dim = params.getTerritoryDim();
        String[] parts = dim.split("x");
        int rows = Integer.parseInt(parts[0]);
        int cols = Integer.parseInt(parts[1]);

        Matrix newMatrix = new Matrix(rows, cols);
        TerritorySectionParamsInput sections = params.getTerritorySectionParams();

        if (sections.getSoil() != null)
            parseSoils(sections.getSoil(), newMatrix);
        if (sections.getPlants() != null)
            parsePlants(sections.getPlants(), newMatrix);
        if (sections.getAnimals() != null)
            parseAnimals(sections.getAnimals(), newMatrix);
        if (sections.getWater() != null)
            parseWater(sections.getWater(), newMatrix);
        if (sections.getAir() != null)
            parseAir(sections.getAir(), newMatrix);

        return newMatrix;
    }

    private void parseSoils(List<SoilInput> list, Matrix matrix) {
        for (SoilInput entry : list) {
            String type = entry.getType();
            for (PairInput loc : entry.getSections()) {
                Soil newSoil = null;
                if (type.equalsIgnoreCase("ForestSoil")) {
                    newSoil = new ForestSoil(loc.getX(), loc.getY(), entry.getMass(), false, entry.getName(),
                            type, entry.getNitrogen(), entry.getWaterRetention(), entry.getSoilpH(),
                            entry.getOrganicMatter(), entry.getLeafLitter());
                } else if (type.equalsIgnoreCase("SwampSoil")) {
                    newSoil = new SwampSoil(loc.getX(), loc.getY(), entry.getMass(), false, entry.getName(),
                            type, entry.getNitrogen(), entry.getWaterRetention(), entry.getSoilpH(),
                            entry.getOrganicMatter(), entry.getWaterLogging());
                } else if (type.equalsIgnoreCase("TundraSoil")) {
                    newSoil = new TundraSoil(loc.getX(), loc.getY(), entry.getMass(), false, entry.getName(),
                            type, entry.getNitrogen(), entry.getWaterRetention(), entry.getSoilpH(),
                            entry.getOrganicMatter(), entry.getPermafrostDepth());
                } else if (type.equalsIgnoreCase("DesertSoil")) {
                    newSoil = new DesertSoil(loc.getX(), loc.getY(), entry.getMass(), false, entry.getName(),
                            type, entry.getNitrogen(), entry.getWaterRetention(), entry.getSoilpH(),
                            entry.getOrganicMatter(), entry.getSalinity());
                } else if (type.equalsIgnoreCase("GrasslandSoil")) {
                    newSoil = new GrasslandSoil(loc.getX(), loc.getY(), entry.getMass(), false, entry.getName(),
                            type, entry.getNitrogen(), entry.getWaterRetention(), entry.getSoilpH(),
                            entry.getOrganicMatter(), entry.getRootDensity());
                }
                if (newSoil != null) matrix.placeEntity(newSoil);
            }
        }
    }

    private void parsePlants(List<PlantInput> list, Matrix matrix) {
        for (PlantInput entry : list) {
            String type = entry.getType();
            String typeUpper = type.toUpperCase();

            for (PairInput loc : entry.getSections()) {
                Plant newPlant = null;
                if (typeUpper.contains("FLOWER")) {
                    newPlant = new FloweringPlants(loc.getX(), loc.getY(), type, entry.getName(), entry.getMass(),
                            0.0, "YOUNG", false);
                } else if (typeUpper.contains("FERN")) {
                    newPlant = new Ferns(loc.getX(), loc.getY(), type, entry.getName(), entry.getMass(),
                            0.0, "YOUNG", false);
                } else if (typeUpper.contains("ALGAE")) {
                    newPlant = new Algae(loc.getX(), loc.getY(), type, entry.getName(), entry.getMass(),
                            0.0, "YOUNG", false);
                } else if (typeUpper.contains("MOSS")) {
                    newPlant = new Mosses(loc.getX(), loc.getY(), type, entry.getName(), entry.getMass(),
                            0.0, "YOUNG", false);
                } else if (typeUpper.contains("GYMNOSPERM")) {
                    newPlant = new Gymnosperms(loc.getX(), loc.getY(), type, entry.getName(), entry.getMass(),
                            0.0, "YOUNG", false);
                }
                if (newPlant != null) matrix.placeEntity(newPlant);
            }
        }
    }

    private void parseAnimals(List<AnimalInput> list, Matrix matrix) {
        for (AnimalInput entry : list) {
            String type = entry.getType();
            String typeUpper = type.toUpperCase();

            for (PairInput loc : entry.getSections()) {
                Animal newAnimal = null;
                if (typeUpper.contains("HERBIVORE")) {
                    newAnimal = new Herbivore(loc.getX(), loc.getY(), entry.getMass(), false, entry.getName(),
                            type);
                } else if (typeUpper.contains("CARNIVORE")) {
                    newAnimal = new Carnivore(loc.getX(), loc.getY(), entry.getMass(), false, entry.getName(),
                            type);
                } else if (typeUpper.contains("OMNIVORE")) {
                    newAnimal = new Omnivore(loc.getX(), loc.getY(), entry.getMass(), false, entry.getName(),
                            type);
                } else if (typeUpper.contains("DETRITIVORE")) {
                    newAnimal = new Detritivores(loc.getX(), loc.getY(), entry.getMass(), false,
                            entry.getName(), type);
                } else if (typeUpper.contains("PARASITE")) {
                    newAnimal = new Parasites(loc.getX(), loc.getY(), entry.getMass(), false, entry.getName(),
                            type);
                }
                if (newAnimal != null) matrix.placeEntity(newAnimal);
            }
        }
    }

    private void parseWater(List<WaterInput> list, Matrix matrix) {
        for (WaterInput entry : list) {
            double waterQuality = entry.getPurity() / 100.0;
            int frozenScore = 0;

            for (PairInput loc : entry.getSections()) {
                Water newWater = new Water(loc.getX(), loc.getY(), entry.getMass(), false, entry.getName(),
                        entry.getType(), entry.getSalinity(), entry.getPH(), entry.getPurity(), entry.getTurbidity(),
                        entry.getContaminantIndex(), waterQuality, entry.isFrozen(), frozenScore);
                matrix.placeEntity(newWater);
            }
        }
    }

    private void parseAir(List<AirInput> list, Matrix matrix) {
        for (AirInput entry : list) {
            String type = entry.getType();
            for (PairInput loc : entry.getSections()) {
                Air newAir = null;
                double temp = entry.getTemperature();

                if (type.equalsIgnoreCase("MountainAir")) {
                    newAir = new Montan(loc.getX(), loc.getY(), entry.getMass(), false, entry.getName(), type,
                            entry.getHumidity(), temp, entry.getOxygenLevel(), entry.getAltitude());
                } else if (type.equalsIgnoreCase("TemperateAir")) {
                    newAir = new Temperat(loc.getX(), loc.getY(), entry.getMass(), false, entry.getName(), type,
                            entry.getHumidity(), temp, entry.getOxygenLevel(), entry.getPollenLevel());
                } else if (type.equalsIgnoreCase("TropicalAir")) {
                    newAir = new Tropical(loc.getX(), loc.getY(), entry.getMass(), false, entry.getName(), type,
                            entry.getHumidity(), temp, entry.getOxygenLevel(), entry.getCo2Level());
                } else if (type.equalsIgnoreCase("DesertAir")) {
                    newAir = new Desert(loc.getX(), loc.getY(), entry.getMass(), false, entry.getName(), type,
                            entry.getHumidity(), temp, entry.getOxygenLevel(), entry.getDustParticles());
                } else if (type.equalsIgnoreCase("PolarAir")) {
                    newAir = new Polar(loc.getX(), loc.getY(), entry.getMass(), false, entry.getName(), type,
                            entry.getHumidity(), temp, entry.getOxygenLevel(), entry.getIceCrystalConcentration());
                }
                if (newAir != null) matrix.placeEntity(newAir);
            }
        }
    }

    private JsonNode getEnvConditions(int x, int y) {
        ObjectNode envNode = mapper.createObjectNode();
        Cell cell = matrix.getCell(x, y);

        if (cell.getSoil() != null) {
            Soil s = cell.getSoil();
            ObjectNode sNode = mapper.createObjectNode();
            sNode.put("type", s.getType());
            sNode.put("name", s.getName());
            sNode.put("mass", s.getMass());
            sNode.put("nitrogen", s.getNitrogen());
            sNode.put("waterRetention", s.getWaterRetention());
            sNode.put("soilpH", s.getSoilph());
            sNode.put("organicMatter", s.getOrganicMatter());
            sNode.put("soilQuality", s.soilQuality());

            ObjectNode fullDump = mapper.valueToTree(s);
            removeCommonFields(fullDump);
            fullDump.remove(List.of("nitrogen", "waterRetention", "soilph", "organicMatter", "soilQuality"));
            sNode.setAll(fullDump);

            envNode.set("soil", sNode);
        }

        if (cell.getPlant() != null) {
            ObjectNode pNode = mapper.createObjectNode();
            pNode.put("type", cell.getPlant().getType());
            pNode.put("name", cell.getPlant().getName());
            pNode.put("mass", cell.getPlant().getMass());
            envNode.set("plants", pNode);
        }

        if (cell.getAnimal() != null) {
            ObjectNode aNode = mapper.createObjectNode();
            aNode.put("type", cell.getAnimal().getType());
            aNode.put("name", cell.getAnimal().getName());
            aNode.put("mass", cell.getAnimal().getMass());
            envNode.set("animals", aNode);
        }

        if (cell.getWater() != null) {
            ObjectNode wNode = mapper.createObjectNode();
            wNode.put("type", cell.getWater().getType());
            wNode.put("name", cell.getWater().getName());
            wNode.put("mass", cell.getWater().getMass());
            envNode.set("water", wNode);
        }

        if (cell.getAir() != null) {
            Air a = cell.getAir();
            ObjectNode airNode = mapper.createObjectNode();
            airNode.put("type", a.getType());
            airNode.put("name", a.getName());
            airNode.put("mass", a.getMass());
            airNode.put("humidity", a.getHumidity());
            airNode.put("temperature", a.getTemperature());
            airNode.put("oxygenLevel", a.getOxygenLevel());
            airNode.put("airQuality", a.getAirQual());
            ObjectNode fullDump = mapper.valueToTree(a);
            removeCommonFields(fullDump);
            fullDump.remove(List.of("humidity", "temperature", "oxygenLevel", "airQuality", "airQual", "dustParticles",
                    "season", "rainfall"));
            airNode.setAll(fullDump);
            envNode.set("air", airNode);
        }

        return envNode;
    }

    private ArrayNode getMapOutput() {
        ArrayNode mapArray = mapper.createArrayNode();
        int width = matrix.getWidth();
        int height = matrix.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Cell cell = matrix.getCell(x, y);
                ObjectNode sectionNode = mapper.createObjectNode();

                ArrayNode coords = mapper.createArrayNode();
                coords.add(x);
                coords.add(y);
                sectionNode.set("section", coords);

                int count = 0;
                if (cell.getPlant() != null)
                    count++;
                if (cell.getAnimal() != null)
                    count++;
                if (cell.getWater() != null)
                    count++;
                sectionNode.put("totalNrOfObjects", count);

                sectionNode.put("airQuality", cell.getAir().resultAirQaulity());
                sectionNode.put("soilQuality", cell.getSoil().resultSoilQuality());
                mapArray.add(sectionNode);
            }
        }
        return mapArray;
    }

    private void removeCommonFields(ObjectNode node) {
        node.remove("x");
        node.remove("y");
        node.remove("scanned");
        node.remove("entityType");
        node.remove("type");
        node.remove("name");
        node.remove("mass");
        if (node.has("specificType"))
            node.remove("specificType");
        if (node.has("variant"))
            node.remove("variant");
    }
}