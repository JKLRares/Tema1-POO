package main.soils;

import main.Soil;
import lombok.Getter;

@Getter
public final class SwampSoil extends Soil {
    private static final double NITROGEN_WEIGHT = 1.1;
    private static final double ORGANIC_MATTER_WEIGHT = 2.2;
    private static final double WATER_LOGGING_PENALTY = 5.0;
    private static final double PROB_MULTIPLIER = 10.0;

    private final double waterLogging;

    public SwampSoil(final int x, final int y, final double mass, final boolean scanned,
                     final String name, final String type, final double nitrogen,
                     final double waterRetention, final double soilph,
                     final double organicMatter, final double waterLogging) {
        super(x, y, mass, scanned, name, type, nitrogen, waterRetention, soilph, organicMatter);
        this.waterLogging = waterLogging;
    }

    @Override
    public double initialSoilQuality() {
        return (getNitrogen() * NITROGEN_WEIGHT) + (getOrganicMatter() * ORGANIC_MATTER_WEIGHT)
                - (waterLogging * WATER_LOGGING_PENALTY);
    }

    @Override
    public double probFormula() {
        return waterLogging * PROB_MULTIPLIER;
    }
}
