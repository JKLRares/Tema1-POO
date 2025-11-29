package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import fileio.InputLoader; // Importing the provided loader

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

        InputLoader inputLoader = new InputLoader(inputPath);

        CommandExecutor executor = new CommandExecutor(inputLoader, MAPPER);
        executor.executeCommands(output);

        File outputFile = new File(outputPath);
        outputFile.getParentFile().mkdirs();
        WRITER.writeValue(outputFile, output);
    }
}