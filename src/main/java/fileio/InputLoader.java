package fileio;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Loads and validates a complete scenario before command execution begins. */
@Getter
public final class InputLoader {
    private static final long MAX_INPUT_BYTES = 10 * 1024 * 1024;
    private final List<SimulationInput> simulations;
    private final List<CommandInput> commands;

    public InputLoader(final String filePath) throws IOException {
        Path source = Path.of(filePath);
        if (Files.size(source) > MAX_INPUT_BYTES) {
            throw new IOException("Input exceeds the 10 MiB scenario limit");
        }
        ObjectMapper mapper = JsonMapper.builder()
                .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .build();
        JsonNode document = mapper.readTree(source.toFile());
        InputValidator.validate(document);
        InputRoot root = mapper.treeToValue(document, InputRoot.class);
        this.simulations = List.copyOf(root.simulationParams);
        this.commands = List.copyOf(root.commands);
    }

    @Data
    @NoArgsConstructor
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static final class InputRoot {
        private List<SimulationInput> simulationParams;
        private List<CommandInput> commands;
    }
}
