package main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SimulationRegressionTest {
    @TempDir
    Path directory;

    private JsonNode run(ObjectNode input) throws Exception {
        Path source = directory.resolve("input.json");
        Path output = directory.resolve("output.json");
        SimulationFixtures.JSON.writeValue(source.toFile(), input);
        Main.action(source.toString(), output.toString());
        return SimulationFixtures.JSON.readTree(output.toFile());
    }

    @Test
    void duplicateStartDoesNotConsumeNextWorldOrResetBattery() throws Exception {
        ObjectNode input = SimulationFixtures.input("""
                [{"command":"startSimulation","timestamp":0},
                 {"command":"scanObject","color":"none","smell":"none","sound":"none","timestamp":1},
                 {"command":"startSimulation","timestamp":2},
                 {"command":"endSimulation","timestamp":3},
                 {"command":"startSimulation","timestamp":4},
                 {"command":"getEnergyStatus","timestamp":5}]
                """);
        ((ArrayNode) input.get("simulationParams")).add(SimulationFixtures.world(1, 1));
        JsonNode output = run(input);
        assertThat(output.get(2).path("message").asText()).startsWith("ERROR:");
        assertThat(output.get(4).path("message").asText()).isEqualTo("Simulation has started.");
        assertThat(output.get(5).path("message").asText()).contains("100 energy points");
    }

    @Test
    void newSimulationDoesNotInheritChargingDeadline() throws Exception {
        ObjectNode input = SimulationFixtures.input("""
                [{"command":"startSimulation","timestamp":0},
                 {"command":"rechargeBattery","timeToCharge":50,"timestamp":1},
                 {"command":"endSimulation","timestamp":2},
                 {"command":"startSimulation","timestamp":3},
                 {"command":"getEnergyStatus","timestamp":4}]
                """);
        ((ArrayNode) input.get("simulationParams")).add(SimulationFixtures.world(1, 1));
        assertThat(run(input).get(4).path("message").asText()).contains("100 energy points");
    }

    @Test
    void weatherExpiresEvenWhenCommandsSkipDeadline() throws Exception {
        ObjectNode input = SimulationFixtures.input("""
                [{"command":"startSimulation","timestamp":0},
                 {"command":"changeWeatherConditions","type":"newSeason","season":"Spring","timestamp":1},
                 {"command":"printEnvConditions","timestamp":5}]
                """);
        assertThat(run(input).get(2).path("output").path("air").path("airQuality").asDouble())
                .isEqualTo(82.0);
    }

    @Test
    void replayOfIdenticalInputProducesIdenticalJson() throws Exception {
        ObjectNode input = SimulationFixtures.input("""
                [{"command":"startSimulation","timestamp":0},
                 {"command":"moveRobot","timestamp":1},
                 {"command":"printEnvConditions","timestamp":2},
                 {"command":"endSimulation","timestamp":3}]
                """);
        assertThat(run(input)).isEqualTo(run(input));
    }

    @Test
    void invalidInputLeavesExistingOutputUntouched() throws Exception {
        Path source = directory.resolve("bad.json");
        Path output = directory.resolve("existing.json");
        Files.writeString(source, "{\"simulationParams\":null,\"commands\":[]}");
        Files.writeString(output, "existing result");
        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> Main.action(source.toString(), output.toString()))
                .isInstanceOf(java.io.IOException.class);
        assertThat(Files.readString(output)).isEqualTo("existing result");
    }
    @Test
    void overflowRechargeIsRejectedWithoutChangingBatteryOrChargingState() throws Exception {
        ObjectNode input = SimulationFixtures.input("""
                [{"command":"startSimulation","timestamp":0},
                 {"command":"rechargeBattery","timeToCharge":1,"timestamp":1},
                 {"command":"getEnergyStatus","timestamp":1}]
                """);
        ((ObjectNode) input.get("simulationParams").get(0)).put("energyPoints", Integer.MAX_VALUE);
        JsonNode output = run(input);
        assertThat(output.get(1).path("message").asText()).contains("capacity exceeded");
        assertThat(output.get(2).path("message").asText()).contains("2147483647 energy points");
    }

    @Test
    void outOfBoundsRouteIsACommandErrorAndDoesNotPreventLaterCommands() throws Exception {
        ObjectNode input = SimulationFixtures.input("""
                [{"command":"startSimulation","timestamp":0},
                 {"command":"moveTo","targetX":2,"targetY":0,"timestamp":1},
                 {"command":"getEnergyStatus","timestamp":1}]
                """);
        JsonNode output = run(input);
        assertThat(output.get(1).path("message").asText()).contains("Destination outside");
        assertThat(output.get(2).path("message").asText()).contains("100 energy points");
    }

    @Test
    void routeCommandsRespectChargingAndFailedMovesDoNotSpendEnergy() throws Exception {
        ObjectNode input = SimulationFixtures.input("""
                [{"command":"startSimulation","timestamp":0},
                 {"command":"moveTo","targetX":1,"targetY":1,"timestamp":1},
                 {"command":"getEnergyStatus","timestamp":1},
                 {"command":"rechargeBattery","timeToCharge":10,"timestamp":2},
                 {"command":"planRoute","targetX":1,"targetY":1,"timestamp":3}]
                """);
        ((ObjectNode) input.get("simulationParams").get(0)).put("energyPoints", 5);
        JsonNode output = run(input);
        assertThat(output.get(1).path("message").asText()).contains("Not enough battery");
        assertThat(output.get(2).path("message").asText()).contains("5 energy points");
        assertThat(output.get(4).path("message").asText()).contains("still charging");
    }

}
