package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import fileio.InputLoader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** File-based application entry point, usable from the CLI or the original checker. */
public final class Main {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    public static final ObjectWriter WRITER = MAPPER.writerWithDefaultPrettyPrinter();

    private Main() {
    }

    /**
     * Runs a scenario and replaces the output only after successful execution and
     * serialization.
     */
    public static void action(final String inputPath, final String outputPath) throws IOException {
        Path source = Path.of(inputPath);
        Path destination = Path.of(outputPath).toAbsolutePath().normalize();
        if (Files.exists(destination) && Files.isSameFile(source, destination)) {
            throw new IOException("Input and output must be different files");
        }
        InputLoader loader = new InputLoader(inputPath);
        ArrayNode output = MAPPER.createArrayNode();
        new CommandExecutor(loader, MAPPER).executeCommands(output);
        Files.createDirectories(destination.getParent());
        Path temporary = Files.createTempFile(destination.getParent(), ".terrabot-", ".json");
        try {
            WRITER.writeValue(temporary.toFile(), output);
            try {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    /** Accepts an input scenario path and an output JSON path. */
    public static void main(final String[] args) {
        int status = run(args, System.out, System.err);
        if (status != 0) {
            System.exit(status);
        }
    }

    static int run(final String[] args, final PrintStream output, final PrintStream error) {
        if (args.length == 1 && (args[0].equals("--help") || args[0].equals("-h"))) {
            output.println("Usage: java -jar terrabot.jar <input.json> <output.json>");
            return 0;
        }
        if (args.length != 2) {
            error.println("Usage: java -jar terrabot.jar <input.json> <output.json>");
            return 2;
        }
        try {
            action(args[0], args[1]);
            return 0;
        } catch (IOException | IllegalArgumentException exception) {
            error.println("TerraBot: " + exception.getMessage());
            return 1;
        }
    }
}
