package main.plants;

import main.Plant;
import lombok.Getter;

@Getter
public final class FloweringPlants extends Plant {
    private static final double OXYGEN_OUTPUT = 6.0;
    private static final int STUCK_POSSIBILITY = 90;

    public FloweringPlants(final int x, final int y, final String type, final String name,
                           final double mass, final double growingLevel, final String level,
                           final boolean scanned) {
        super(x, y, type, name, mass, growingLevel, level, scanned);
    }

    @Override
    public double getOxygenOutput() {
        return OXYGEN_OUTPUT;
    }

    @Override
    public int getStuckPossibility() {
        return STUCK_POSSIBILITY;
    }
}
