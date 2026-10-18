package fileio;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Validates the JSON contract before any simulation or output file is created. */
final class InputValidator {
    private static final long MAX_CELLS = 100_000;
    private static final int MAX_TIMESTAMP = 100_000;
    private static final long MAX_CELL_WORK = 10_000_000;
    private static final Set<String> COMMANDS = Set.of("startSimulation", "endSimulation",
            "printEnvConditions", "printMap", "moveRobot", "getEnergyStatus", "rechargeBattery",
            "changeWeatherConditions", "scanObject", "learnFact", "printKnowledgeBase",
            "improveEnvironment", "planRoute", "moveTo");
    private static final Set<String> IMPROVEMENTS = Set.of("plantVegetation", "fertilizeSoil",
            "increaseHumidity", "waterRetention", "increaseMoisture");
    private static final Map<String, Set<String>> TYPES = Map.of(
            "soil", Set.of("forestsoil", "swampsoil", "tundrasoil", "desertsoil", "grasslandsoil"),
            "air", Set.of("mountainair", "temperateair", "tropicalair", "desertair", "polarair"),
            "plants", Set.of("floweringplants", "ferns", "algae", "mosses", "gymnospermsplants",
                    "gymnosperms"),
            "animals", Set.of("herbivore", "herbivores", "carnivore", "carnivores", "omnivore",
                    "omnivores", "detritivore", "detritivores", "parasite", "parasites"));
    private static final Map<String, String> VARIANT_FIELDS = Map.of(
            "forestsoil", "leafLitter", "swampsoil", "waterLogging",
            "tundrasoil", "permafrostDepth",
            "desertsoil", "salinity", "grasslandsoil", "rootDensity", "mountainair", "altitude",
            "temperateair", "pollenLevel", "tropicalair", "co2Level",
            "desertair", "dustParticles", "polarair", "iceCrystalConcentration");

    private InputValidator() {
    }

    static void validate(final JsonNode root) throws IOException {
        require(root != null && root.isObject(), "$", "expected an object");
        array(root.get("simulationParams"), "$.simulationParams");
        array(root.get("commands"), "$.commands");
        int index = 0;
        for (JsonNode simulation : root.get("simulationParams")) {
            validateWorld(simulation, "$.simulationParams[" + index++ + "]");
        }
        int previous = -1;
        index = 0;
        for (JsonNode command : root.get("commands")) {
            String path = "$.commands[" + index++ + "]";
            require(command.isObject(), path, "expected an object");
            String name = text(command, "command", path);
            require(COMMANDS.contains(name), path + ".command", "unsupported command: " + name);
            int timestamp = integer(command, "timestamp", path);
            require(timestamp >= 0 && timestamp >= previous && timestamp <= MAX_TIMESTAMP,
                    path + ".timestamp",
                    "must be nonnegative, nondecreasing and at most " + MAX_TIMESTAMP);
            previous = timestamp;
            validateCommand(command, name, path);
        }
        validateWorkBudget(root);
        validateNumbers(root, "$");
    }

    private static void validateWorkBudget(final JsonNode root) throws IOException {
        int simulationIndex = 0;
        int previousTimestamp = 0;
        long cells = 0;
        long work = 0;
        boolean running = false;
        for (JsonNode command : root.get("commands")) {
            String name = command.get("command").textValue();
            int timestamp = command.get("timestamp").intValue();
            if (name.equals("startSimulation")) {
                if (!running && simulationIndex < root.get("simulationParams").size()) {
                    JsonNode simulation = root.get("simulationParams").get(simulationIndex++);
                    String[] dimensions = simulation.get("territoryDim").textValue().split("x");
                    cells = Long.parseLong(dimensions[0]) * Long.parseLong(dimensions[1]);
                    previousTimestamp = timestamp;
                    running = true;
                }
            } else if (name.equals("endSimulation")) {
                running = false;
            } else if (running) {
                work += (timestamp - previousTimestamp) * cells;
                if (Set.of("printMap", "planRoute", "moveTo").contains(name)) {
                    work += cells;
                }
                previousTimestamp = timestamp;
                require(work <= MAX_CELL_WORK, "$.commands",
                        "scenario exceeds the " + MAX_CELL_WORK + " cell-work budget");
            }
        }
    }

    private static void validateWorld(final JsonNode world, final String path) throws IOException {
        require(world.isObject(), path, "expected an object");
        String dimensions = text(world, "territoryDim", path);
        require(dimensions.matches("[1-9][0-9]*x[1-9][0-9]*"), path + ".territoryDim",
                "expected HEIGHTxWIDTH with positive dimensions");
        String[] parts = dimensions.split("x");
        long height;
        long width;
        try {
            height = Long.parseLong(parts[0]);
            width = Long.parseLong(parts[1]);
        } catch (NumberFormatException exception) {
            throw new IOException(path + ".territoryDim: dimensions are too large", exception);
        }
        require(height <= MAX_CELLS && width <= MAX_CELLS && height * width <= MAX_CELLS,
                path + ".territoryDim", "world must contain at most " + MAX_CELLS + " cells");
        require(integer(world, "energyPoints", path) >= 0, path + ".energyPoints",
                "must be nonnegative");
        JsonNode groups = world.get("territorySectionParams");
        require(groups != null && groups.isObject(), path + ".territorySectionParams",
                "expected an object");
        for (String group : Set.of("soil", "air", "plants", "animals", "water")) {
            String groupPath = path + ".territorySectionParams." + group;
            JsonNode entries = groups.get(group);
            if (entries == null && !group.equals("soil") && !group.equals("air")) {
                continue;
            }
            entries = array(entries, groupPath);
            Set<Long> occupied = new HashSet<>();
            int index = 0;
            for (JsonNode entry : entries) {
                String entryPath = groupPath + "[" + index++ + "]";
                validateEntity(entry, group, entryPath);
                array(entry.get("sections"), entryPath + ".sections");
                for (JsonNode coordinates : entry.get("sections")) {
                    int x = integer(coordinates, "x", entryPath + ".sections");
                    int y = integer(coordinates, "y", entryPath + ".sections");
                    require(x >= 0 && x < width && y >= 0 && y < height,
                            entryPath + ".sections", "coordinates outside the world");
                    require(occupied.add(y * width + x), entryPath + ".sections",
                            "duplicate " + group + " at (" + x + ", " + y + ")");
                }
            }
            if (group.equals("soil") || group.equals("air")) {
                require(occupied.size() == height * width, groupPath,
                        "every cell must have exactly one " + group + " entry");
            }
        }
    }

    private static void validateEntity(final JsonNode entry, final String group,
                                       final String path) throws IOException {
        require(entry.isObject(), path, "expected an object");
        String type = text(entry, "type", path).toLowerCase(Locale.ROOT);
        text(entry, "name", path);
        require(number(entry, "mass", path) >= 0, path + ".mass", "must be nonnegative");
        if (TYPES.containsKey(group)) {
            require(TYPES.get(group).contains(type), path + ".type",
                    "unsupported " + group + " type");
        }
        String[] required = switch (group) {
            case "soil" -> new String[]{"nitrogen", "waterRetention", "soilpH", "organicMatter"};
            case "air" -> new String[]{"humidity", "temperature", "oxygenLevel"};
            case "water" -> new String[]{"purity", "salinity", "turbidity",
                    "contaminantIndex", "pH"};
            default -> new String[0];
        };
        for (String field : required) {
            double value = number(entry, field, path);
            require(field.equals("temperature") || value >= 0, path + "." + field,
                    "must be nonnegative");
        }
        if (VARIANT_FIELDS.containsKey(type)) {
            String field = VARIANT_FIELDS.get(type);
            require(number(entry, field, path) >= 0, path + "." + field, "must be nonnegative");
        }
        if (group.equals("water")) {
            require(entry.has("isFrozen") && entry.get("isFrozen").isBoolean(),
                    path + ".isFrozen", "expected a boolean");
        }
    }

    private static void validateCommand(final JsonNode command, final String name,
                                        final String path) throws IOException {
        switch (name) {
            case "scanObject" -> {
                text(command, "color", path);
                text(command, "smell", path);
                text(command, "sound", path);
            }
            case "rechargeBattery" -> require(integer(command, "timeToCharge", path) >= 0,
                    path + ".timeToCharge", "must be nonnegative");
            case "learnFact" -> {
                text(command, "components", path);
                text(command, "subject", path);
            }
            case "improveEnvironment" -> {
                text(command, "name", path);
                require(IMPROVEMENTS.contains(text(command, "improvementType", path)),
                        path + ".improvementType", "unsupported improvement");
            }
            case "planRoute", "moveTo" -> {
                require(integer(command, "targetX", path) >= 0, path + ".targetX",
                        "must be nonnegative");
                require(integer(command, "targetY", path) >= 0, path + ".targetY",
                        "must be nonnegative");
            }
            case "changeWeatherConditions" -> validateWeather(command, path);
            default -> { }
        }
    }

    private static void validateWeather(final JsonNode command, final String path)
            throws IOException {
        String type = text(command, "type", path).trim();
        switch (type) {
            case "rainfall", "polarStorm" -> {
                String field = type.equals("rainfall") ? "rainfall" : "windSpeed";
                require(number(command, field, path) >= 0, path + "." + field,
                        "must be nonnegative");
            }
            case "peopleHiking" -> require(integer(command, "numberOfHikers", path) >= 0,
                    path + ".numberOfHikers", "must be nonnegative");
            case "newSeason" -> text(command, "season", path);
            case "desertStorm" -> require(command.has("desertStorm")
                    && command.get("desertStorm").isBoolean(), path + ".desertStorm",
                    "expected a boolean");
            default -> throw new IOException(path + ".type: unsupported weather event");
        }
    }

    private static void validateNumbers(final JsonNode node, final String path) throws IOException {
        if (node.isNumber()) {
            require(Double.isFinite(node.doubleValue()), path, "expected a finite number");
        } else if (node.isContainerNode()) {
            if (node.isArray()) {
                int index = 0;
                for (JsonNode item : node) {
                    validateNumbers(item, path + "[" + index++ + "]");
                }
            } else {
                for (Map.Entry<String, JsonNode> field : node.properties()) {
                    validateNumbers(field.getValue(), path + "." + field.getKey());
                }
            }
        }
    }

    private static String text(final JsonNode node, final String field, final String path)
            throws IOException {
        JsonNode value = node.get(field);
        require(value != null && value.isTextual() && !value.textValue().isBlank(),
                path + "." + field, "expected a nonblank string");
        return value.textValue();
    }

    private static int integer(final JsonNode node, final String field, final String path)
            throws IOException {
        JsonNode value = node.get(field);
        require(value != null && value.isIntegralNumber() && value.canConvertToInt(),
                path + "." + field, "expected a 32-bit integer");
        return value.intValue();
    }

    private static double number(final JsonNode node, final String field, final String path)
            throws IOException {
        JsonNode value = node.get(field);
        require(value != null && value.isNumber() && Double.isFinite(value.doubleValue()),
                path + "." + field, "expected a finite number");
        return value.doubleValue();
    }

    private static JsonNode array(final JsonNode node, final String path) throws IOException {
        if (node == null || !node.isArray()) {
            throw new IOException(path + ": expected an array");
        }
        return node;
    }

    private static void require(final boolean valid, final String path, final String message)
            throws IOException {
        if (!valid) {
            throw new IOException(path + ": " + message);
        }
    }
}
