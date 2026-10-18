package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

final class SimulationFixtures {
    static final ObjectMapper JSON = new ObjectMapper();

    private SimulationFixtures() {
    }

    static ObjectNode input(String commands) throws Exception {
        ObjectNode root = JSON.createObjectNode();
        root.putArray("simulationParams").add(world(2, 2));
        root.set("commands", JSON.readTree(commands));
        return root;
    }

    static ObjectNode world(int height, int width) {
        ObjectNode world = JSON.createObjectNode();
        world.put("territoryDim", height + "x" + width);
        world.put("energyPoints", 100);
        ObjectNode sections = world.putObject("territorySectionParams");
        ObjectNode soil = sections.putArray("soil").addObject();
        soil.put("type", "ForestSoil").put("name", "Forest").put("mass", 1000)
                .put("nitrogen", 2).put("waterRetention", 4).put("soilpH", 7)
                .put("organicMatter", 1).put("leafLitter", 0);
        ObjectNode air = sections.putArray("air").addObject();
        air.put("type", "TemperateAir").put("name", "Air").put("mass", 1000)
                .put("humidity", 60).put("temperature", 20).put("oxygenLevel", 20)
                .put("pollenLevel", 0);
        ArrayNode coordinates = soil.putArray("sections");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                coordinates.addObject().put("x", x).put("y", y);
            }
        }
        air.set("sections", coordinates.deepCopy());
        return world;
    }
}
