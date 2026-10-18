package main;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CliTest {
    @TempDir
    Path directory;

    @Test
    void helpAndUsageHaveExplicitExitStatuses() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream error = new ByteArrayOutputStream();
        assertThat(Main.run(new String[]{"--help"}, new PrintStream(output), new PrintStream(error)))
                .isZero();
        assertThat(output.toString(StandardCharsets.UTF_8)).contains("Usage:");
        assertThat(error.size()).isZero();
        assertThat(Main.run(new String[]{}, new PrintStream(output), new PrintStream(error))).isEqualTo(2);
        assertThat(error.toString(StandardCharsets.UTF_8)).contains("Usage:");
    }

    @Test
    void inputErrorsProduceActionableMessagesAndNonzeroExit() {
        ByteArrayOutputStream error = new ByteArrayOutputStream();
        Path input = directory.resolve("missing.json");
        assertThat(Main.run(new String[]{input.toString(), directory.resolve("out.json").toString()},
                System.out, new PrintStream(error))).isEqualTo(1);
        assertThat(error.toString(StandardCharsets.UTF_8)).contains("missing.json");
        assertThat(directory.resolve("out.json")).doesNotExist();
    }

    @Test
    void successfulRunCreatesNestedOutputAndReplacesExistingResult() throws Exception {
        Path input = directory.resolve("input.json");
        Path output = directory.resolve("nested/output.json");
        SimulationFixtures.JSON.writeValue(input.toFile(), SimulationFixtures.input("""
                [{"command":"startSimulation","timestamp":0},
                 {"command":"planRoute","targetX":1,"targetY":1,"timestamp":1},
                 {"command":"getEnergyStatus","timestamp":1},
                 {"command":"moveTo","targetX":1,"targetY":1,"timestamp":1},
                 {"command":"getEnergyStatus","timestamp":1},
                 {"command":"endSimulation","timestamp":2}]
                """));
        Main.action(input.toString(), output.toString());
        var result = SimulationFixtures.JSON.readTree(output.toFile());
        assertThat(result.get(1).get("output").get("energyCost").asInt()).isEqualTo(6);
        assertThat(result.get(2).get("message").asText()).contains("100 energy points");
        assertThat(result.get(3).get("output")).isEqualTo(result.get(1).get("output"));
        assertThat(result.get(4).get("message").asText()).contains("94 energy points");
        Files.writeString(output, "old result");
        Main.action(input.toString(), output.toString());
        assertThat(SimulationFixtures.JSON.readTree(output.toFile())).isEqualTo(result);
        try (var paths = Files.list(output.getParent())) {
            assertThat(paths.map(path -> path.getFileName().toString())).containsExactly("output.json");
        }
    }

    @Test
    void sameInputAndOutputIsRejectedWithoutChangingInput() throws Exception {
        Path source = directory.resolve("input.json");
        String json = SimulationFixtures.input("[]").toString();
        Files.writeString(source, json);
        assertThatThrownBy(() -> Main.action(source.toString(), source.toString()))
                .isInstanceOf(java.io.IOException.class).hasMessageContaining("different files");
        assertThat(Files.readString(source)).isEqualTo(json);
    }

    @Test
    void outputFailureLeavesExistingFilesUntouched() throws Exception {
        Path source = directory.resolve("input.json");
        SimulationFixtures.JSON.writeValue(source.toFile(), SimulationFixtures.input("[]"));
        Path blocked = directory.resolve("blocker");
        Files.writeString(blocked, "keep");
        assertThatThrownBy(() -> Main.action(source.toString(), blocked.resolve("out.json").toString()))
                .isInstanceOf(java.io.IOException.class);
        assertThat(Files.readString(blocked)).isEqualTo("keep");
    }
}
