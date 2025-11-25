package main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.File;
import java.io.IOException;

public final class Main {

    private Main() {
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();
    public static final ObjectWriter WRITER = MAPPER.writer().withDefaultPrettyPrinter();

    public static void action(final String inputPath,
                              final String outputPath) throws IOException {

        ArrayNode output = MAPPER.createArrayNode();

        File inputFile = new File(inputPath);
        JsonNode jsonInput = MAPPER.readTree(inputFile);

        InputLoader inputLoader = new InputLoader();
        Matrix matrix = inputLoader.loadWorld(jsonInput);

        if (jsonInput.has("commands")) {
            CommandExecutor executor = new CommandExecutor(matrix, MAPPER);
            executor.executeCommands(jsonInput.get("commands"), output);
        }

        File outputFile = new File(outputPath);
        outputFile.getParentFile().mkdirs();
        WRITER.writeValue(outputFile, output);
    }
}