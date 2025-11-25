package main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.soils.*;
import main.air.*;

import java.util.List;

public class CommandExecutor {

    private final Matrix matrix;
    private final ObjectMapper mapper;

    private boolean simulationRunning = false;

    public CommandExecutor(Matrix matrix, ObjectMapper mapper) {
        this.matrix = matrix;
        this.mapper = mapper;
    }

    public void executeCommands(JsonNode commands, ArrayNode outputArray) {
        for (JsonNode cmd : commands) {
            String commandName = cmd.get("command").asText();
            int timestamp = cmd.has("timestamp") ? cmd.get("timestamp").asInt() : 0;

            ObjectNode resultNode = mapper.createObjectNode();
            resultNode.put("command", commandName);

            if (commandName.equals("startSimulation")) {
                simulationRunning = true;
                resultNode.put("message", "Simulation has started.");
                resultNode.put("timestamp", timestamp);
                outputArray.add(resultNode);
                continue;
            }

            if (commandName.equals("endSimulation")) {
                simulationRunning = false;
                resultNode.put("message", "Simulation has ended.");
                resultNode.put("timestamp", timestamp);
                outputArray.add(resultNode);
                continue;
            }

            if (!simulationRunning) {
                resultNode.put("message", "ERROR: Simulation not started. Cannot perform action");
                resultNode.put("timestamp", timestamp);
                outputArray.add(resultNode);
                continue;
            }

            switch (commandName) {
                case "printEnvConditions":
                    JsonNode conditions = getEnvConditions(0, 0);
                    resultNode.set("output", conditions);
                    resultNode.put("timestamp", timestamp);
                    outputArray.add(resultNode);
                    break;

                case "printMap":
                    ArrayNode mapOutput = getMapOutput();
                    resultNode.set("output", mapOutput);
                    resultNode.put("timestamp", timestamp);
                    outputArray.add(resultNode);
                    break;
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
            airNode.put("airQuality", a.airQuality());

            ObjectNode fullDump = mapper.valueToTree(a);
            removeCommonFields(fullDump);
            fullDump.remove(List.of("humidity", "temperature", "oxygenLevel", "airQuality"));
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
                if (cell.getPlant() != null) count++;
                if (cell.getAnimal() != null) count++;
                if (cell.getWater() != null) count++;
                sectionNode.put("totalNrOfObjects", count);

                if (cell.getAir() != null) {
                    sectionNode.put("airQuality", interpretQuality(cell.getAir().airQuality()));
                } else {
                    sectionNode.put("airQuality", "poor");
                }

                if (cell.getSoil() != null) {
                    sectionNode.put("soilQuality", interpretQuality(cell.getSoil().soilQuality()));
                } else {
                    sectionNode.put("soilQuality", "poor");
                }
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
        if (node.has("specificType")) node.remove("specificType");
        if (node.has("variant")) node.remove("variant");
    }

    private String interpretQuality(double score) {
        if (score >= 70) return "good";
        if (score >= 40) return "moderate";
        return "poor";
    }
}