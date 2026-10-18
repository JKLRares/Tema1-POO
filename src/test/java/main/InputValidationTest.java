package main;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.InputLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InputValidationTest {
    @TempDir
    Path directory;

    private InputLoader load(String json) throws Exception {
        Path source = directory.resolve("input.json");
        java.nio.file.Files.writeString(source, json);
        return new InputLoader(source.toString());
    }

    static Stream<Arguments> invalidDocuments() {
        return Stream.of(
                invalid("missing commands", root -> root.remove("commands")),
                invalid("null commands", root -> root.putNull("commands")),
                invalid("negative dimensions", root -> world(root).put("territoryDim", "-1x2")),
                invalid("zero dimensions", root -> world(root).put("territoryDim", "0x2")),
                invalid("huge dimensions", root -> world(root).put("territoryDim", "100000x100000")),
                invalid("overflow dimensions", root -> world(root).put("territoryDim", "9999999999999999999999x2")),
                invalid("negative energy", root -> world(root).put("energyPoints", -1)),
                invalid("missing energy", root -> world(root).remove("energyPoints")),
                invalid("unsupported entity", root -> soil(root).put("type", "MoonSoil")),
                invalid("missing numeric field", root -> soil(root).remove("waterRetention")),
                invalid("negative mass", root -> soil(root).put("mass", -1)),
                invalid("numeric string", root -> soil(root).put("nitrogen", "2")),
                invalid("incomplete air", root -> sections(root).putArray("air")),
                invalid("duplicate cell", root -> ((ArrayNode) soil(root).get("sections")).addObject().put("x", 0).put("y", 0)),
                invalid("outside coordinate", root -> ((ObjectNode) soil(root).get("sections").get(0)).put("x", 2)),
                invalid("fractional coordinate", root -> ((ObjectNode) soil(root).get("sections").get(0)).put("y", 0.5)),
                invalid("unknown command", root -> command(root).put("command", "fly")),
                invalid("missing timestamp", root -> command(root).remove("timestamp")),
                invalid("negative timestamp", root -> command(root).put("timestamp", -1)),
                invalid("large timestamp", root -> command(root).put("timestamp", 100001)),
                invalid("missing scan sensor", root -> command(root).put("command", "scanObject")),
                invalid("negative recharge", root -> command(root).put("command", "rechargeBattery").put("timeToCharge", -1)),
                invalid("unknown improvement", root -> command(root).put("command", "improveEnvironment")
                        .put("name", "Fern").put("improvementType", "unknown")),
                invalid("missing destination", root -> command(root).put("command", "planRoute")),
                invalid("unknown property", root -> root.put("typo", true)),
                invalid("unknown weather", root -> command(root).put("command", "changeWeatherConditions").put("type", "unknown")),
                invalid("decreasing time", root -> root.withArray("commands").addObject()
                        .put("command", "printMap").put("timestamp", 0))
        );
    }

    private static Arguments invalid(String name, Consumer<ObjectNode> edit) {
        return Arguments.of(name, edit);
    }

    private static ObjectNode world(ObjectNode root) {
        return (ObjectNode) root.get("simulationParams").get(0);
    }

    private static ObjectNode sections(ObjectNode root) {
        return (ObjectNode) world(root).get("territorySectionParams");
    }

    private static ObjectNode soil(ObjectNode root) {
        return (ObjectNode) sections(root).get("soil").get(0);
    }

    private static ObjectNode command(ObjectNode root) {
        return (ObjectNode) root.get("commands").get(0);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidDocuments")
    void rejectsInvalidDocumentBeforeExecution(String name, Consumer<ObjectNode> edit) throws Exception {
        ObjectNode input = SimulationFixtures.input("[{\"command\":\"startSimulation\",\"timestamp\":1}]");
        edit.accept(input);
        assertThatThrownBy(() -> load(input.toString())).as(name).isInstanceOf(IOException.class);
    }

    @Test
    void rejectsDuplicateKeysTrailingDocumentsAndNonfiniteNumbers() throws Exception {
        String valid = SimulationFixtures.input("[]").toString();
        assertThatThrownBy(() -> load(valid + " {}" )).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> load("{\"simulationParams\":[],\"commands\":[],\"commands\":[]}"))
                .isInstanceOf(IOException.class);
        assertThatThrownBy(() -> load(valid.replace("\"mass\":1000", "\"mass\":1e999")))
                .isInstanceOf(IOException.class);
    }

    @Test
    void rectangularWorldPreservesCartesianCoordinatesAndBounds() throws Exception {
        ObjectNode root = SimulationFixtures.input("[]");
        root.putArray("simulationParams").add(SimulationFixtures.world(2, 3));
        Matrix map = new WorldFactory().create(load(root.toString()).getSimulations().getFirst());
        assertThat(map.getWidth()).isEqualTo(3);
        assertThat(map.getHeight()).isEqualTo(2);
        assertThat(map.getCell(2, 1).getX()).isEqualTo(2);
        assertThat(map.getCell(2, 1).getY()).isEqualTo(1);
        assertThatThrownBy(() -> map.getCell(3, 1)).isInstanceOf(IllegalArgumentException.class);
        Cell[][] copy = map.getGrid();
        copy[0][0] = null;
        assertThat(map.getCell(0, 1)).isNotNull();
    }
    @Test
    void rejectsExcessiveSimulationWorkBeforeExecutingAnyCommand() throws Exception {
        ObjectNode input = SimulationFixtures.input("""
                [{"command":"startSimulation","timestamp":0},
                 {"command":"getEnergyStatus","timestamp":100000}]
                """);
        input.putArray("simulationParams").add(SimulationFixtures.world(1, 101));
        assertThatThrownBy(() -> load(input.toString())).isInstanceOf(IOException.class)
                .hasMessageContaining("cell-work budget");
    }

}
