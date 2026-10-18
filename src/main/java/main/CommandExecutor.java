package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.CommandInput;
import fileio.InputLoader;
import fileio.SimulationInput;
import java.util.List;
import java.util.Map;
import main.navigation.Position;
import main.navigation.Route;
import main.navigation.RoutePlanner;

/** Coordinates simulation sessions and commands; ecological rules live in the domain. */
public final class CommandExecutor {
    private static final int SCAN_COST = 7;
    private static final int LEARN_FACT_COST = 2;
    private static final int IMPROVE_ENV_COST = 10;
    private static final String NOT_RUNNING =
            "ERROR: Simulation not started. Cannot perform action";
    private static final String LOW_BATTERY =
            "ERROR: Not enough battery left. Cannot perform action";

    private final WorldFactory worldFactory = new WorldFactory();
    private final EnvironmentUpdater environmentUpdater = new EnvironmentUpdater();
    private final ObjectMapper mapper;
    private final InputLoader inputLoader;
    private final SimulationRenderer renderer;
    private Matrix matrix;
    private Robot robot;
    private ScannedAnimal scannedAnimals = new ScannedAnimal();
    private Inventory inventory = new Inventory();
    private boolean simulationRunning;
    private int simulationIndex;
    private long robotChargingTimestamp;
    private int previousTimestamp;

    public CommandExecutor(final InputLoader inputLoader, final ObjectMapper mapper) {
        this.inputLoader = inputLoader;
        this.mapper = mapper;
        this.renderer = new SimulationRenderer(mapper);
    }

    /** Executes one document in order, emitting one result for every command. */
    public void executeCommands(final ArrayNode outputArray) {
        for (CommandInput command : inputLoader.getCommands()) {
            ObjectNode result = mapper.createObjectNode().put("command", command.getCommand());
            if (command.getCommand().equals("startSimulation")) {
                result.put("message", start(command.getTimestamp()));
            } else if (command.getCommand().equals("endSimulation")) {
                result.put("message", simulationRunning ? "Simulation has ended." : NOT_RUNNING);
                simulationRunning = false;
            } else if (!simulationRunning) {
                result.put("message", NOT_RUNNING);
            } else {
                advanceTo(command.getTimestamp());
                if (command.getTimestamp() < robotChargingTimestamp) {
                    result.put("message", "ERROR: Robot still charging. Cannot perform action");
                } else {
                    executeRuntime(command, result);
                }
            }
            result.put("timestamp", command.getTimestamp());
            outputArray.add(result);
        }
    }

    private String start(final int timestamp) {
        if (simulationRunning || simulationIndex >= inputLoader.getSimulations().size()) {
            return "ERROR: Simulation already started. Cannot perform action";
        }
        SimulationInput simulation = inputLoader.getSimulations().get(simulationIndex++);
        matrix = worldFactory.create(simulation);
        robot = new Robot(0, 0, simulation.getEnergyPoints());
        scannedAnimals = new ScannedAnimal();
        inventory = new Inventory();
        previousTimestamp = timestamp;
        robotChargingTimestamp = 0;
        simulationRunning = true;
        return "Simulation has started.";
    }

    private void advanceTo(final int timestamp) {
        while (previousTimestamp < timestamp) {
            previousTimestamp++;
            environmentUpdater.update(matrix, scannedAnimals, previousTimestamp);
        }
    }

    private void executeRuntime(final CommandInput command, final ObjectNode result) {
        switch (command.getCommand()) {
            case "printEnvConditions" -> result.set("output",
                    renderer.conditions(matrix, robot.getRobotX(), robot.getRobotY()));
            case "printMap" -> result.set("output", renderer.map(matrix));
            case "moveRobot" -> {
                if (matrix.neighbors(robot.getRobotX(), robot.getRobotY()).isEmpty()) {
                    result.put("message", "ERROR: No neighboring cell. Cannot perform action");
                } else {
                    result.put("message", robot.moveRobot(matrix)
                            ? "The robot has successfully moved to position (" + robot.getRobotX()
                            + ", " + robot.getRobotY() + ")." : LOW_BATTERY);
                }
            }
            case "getEnergyStatus" -> result.put("message", "TerraBot has "
                    + robot.getRobotBattery() + " energy points left.");
            case "rechargeBattery" -> recharge(command, result);
            case "changeWeatherConditions" -> changeWeather(command, result);
            case "scanObject" -> result.put("message", scan(command));
            case "learnFact" -> result.put("message", learn(command));
            case "printKnowledgeBase" -> result.set("output", knowledge());
            case "improveEnvironment" -> result.put("message", improve(command));
            case "planRoute", "moveTo" -> route(command, result);
            default -> result.put("message", "ERROR: Unsupported command. Cannot perform action");
        }
    }

    private void recharge(final CommandInput command, final ObjectNode result) {
        long energy = (long) robot.getRobotBattery() + command.getTimeToCharge();
        if (energy > Integer.MAX_VALUE) {
            result.put("message", "ERROR: Battery capacity exceeded. Cannot perform action");
            return;
        }
        robot.setRobotBattery((int) energy);
        robotChargingTimestamp = (long) command.getTimestamp() + command.getTimeToCharge();
        result.put("message", "Robot battery is charging.");
    }

    private void changeWeather(final CommandInput command, final ObjectNode result) {
        boolean changed = false;
        for (int x = 0; x < matrix.getWidth(); x++) {
            for (int y = 0; y < matrix.getHeight(); y++) {
                changed |= matrix.getCell(x, y).getAir().event(command);
            }
        }
        result.put("message", changed ? "The weather has changed."
                : "ERROR: The weather change does not affect "
                + "the environment. Cannot perform action");
    }

    private String scan(final CommandInput command) {
        if (robot.getRobotBattery() < SCAN_COST) {
            return "ERROR: Not enough energy to perform action";
        }
        Cell cell = matrix.getCell(robot.getRobotX(), robot.getRobotY());
        boolean colored = !command.getColor().equalsIgnoreCase("none");
        boolean scented = !command.getSmell().equalsIgnoreCase("none");
        boolean audible = !command.getSound().equalsIgnoreCase("none");
        Entity found;
        String kind;
        if (!colored && !scented && !audible && cell.getWater() != null) {
            if (!cell.getWater().isScanned()) {
                cell.getWater().setScanTimestamp(command.getTimestamp());
            }
            found = cell.getWater();
            kind = "water";
        } else if (colored && scented && !audible && cell.getPlant() != null) {
            found = cell.getPlant();
            kind = "a plant";
        } else if (colored && scented && audible && cell.getAnimal() != null) {
            Animal animal = cell.getAnimal();
            if (!animal.isScanned()) {
                animal.setScanTimestamp(command.getTimestamp());
                scannedAnimals.addAnimal(animal);
            }
            found = animal;
            kind = "an animal";
        } else {
            return "ERROR: Object not found. Cannot perform action";
        }
        found.setScanned(true);
        inventory.addItem(found.getName());
        robot.setRobotBattery(robot.getRobotBattery() - SCAN_COST);
        return "The scanned object is " + kind + ".";
    }

    private String learn(final CommandInput command) {
        if (robot.getRobotBattery() < LEARN_FACT_COST) {
            return LOW_BATTERY;
        }
        if (!inventory.addFact(command.getComponents(), command.getSubject())) {
            return "ERROR: Subject not yet saved. Cannot perform action";
        }
        robot.setRobotBattery(robot.getRobotBattery() - LEARN_FACT_COST);
        return "The fact has been successfully saved in the database.";
    }

    private ArrayNode knowledge() {
        ArrayNode output = mapper.createArrayNode();
        Map<String, List<String>> facts = inventory.getKnowledgeBase();
        for (String item : inventory.getCollectedItems()) {
            if (facts.containsKey(item) && !facts.get(item).isEmpty()) {
                ObjectNode topic = output.addObject().put("topic", item);
                ArrayNode entries = topic.putArray("facts");
                facts.get(item).forEach(entries::add);
            }
        }
        return output;
    }

    private String improve(final CommandInput command) {
        if (robot.getRobotBattery() < IMPROVE_ENV_COST) {
            return LOW_BATTERY;
        }
        if (!inventory.getCollectedItems().contains(command.getName())) {
            return "ERROR: Subject not yet saved. Cannot perform action";
        }
        if (!inventory.checkRequirement(command.getName(), command.getImprovementType())) {
            return "ERROR: Fact not yet saved. Cannot perform action";
        }
        String message = inventory.applyImprovement(
                matrix.getCell(robot.getRobotX(), robot.getRobotY()),
                command.getImprovementType(), command.getName());
        robot.setRobotBattery(robot.getRobotBattery() - IMPROVE_ENV_COST);
        return message;
    }
    private void route(final CommandInput command, final ObjectNode result) {
        if (!matrix.contains(command.getTargetX(), command.getTargetY())) {
            result.put("message", "ERROR: Destination outside the world. Cannot perform action");
            return;
        }
        Route route;
        if (command.getCommand().equals("moveTo")) {
            var movement = robot.moveTo(matrix, command.getTargetX(), command.getTargetY());
            if (movement.isEmpty()) {
                result.put("message", LOW_BATTERY);
                return;
            }
            route = movement.orElseThrow();
        } else {
            route = new RoutePlanner().plan(matrix,
                    new Position(robot.getRobotX(), robot.getRobotY()),
                    new Position(command.getTargetX(), command.getTargetY()));
        }
        ObjectNode output = result.putObject("output");
        output.put("energyCost", route.energyCost());
        output.put("steps", route.path().size() - 1);
        output.put("affordable", command.getCommand().equals("moveTo")
                || route.energyCost() <= robot.getRobotBattery());
        ArrayNode path = output.putArray("path");
        for (Position position : route.path()) {
            path.addArray().add(position.x()).add(position.y());
        }
    }

}
