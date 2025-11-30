package main.soils;

import main.Soil;
import lombok.Getter;

@Getter
public final class GrasslandSoil extends Soil {
    private static final double NITROGEN_WEIGHT = 1.3;
    private static final double ORGANIC_MATTER_WEIGHT = 1.5;
    private static final double ROOT_DENSITY_WEIGHT = 0.8;
    private static final double BASE_PROB = 50.0;
    private static final double WATER_RETENTION_FACTOR = 0.5;
    private static final double PROB_DIVISOR = 75.0;
    private static final double PROB_MULTIPLIER = 100.0;

    private final double rootDensity;

    public GrasslandSoil(final int x, final int y, final double mass, final boolean scanned,
                         final String name, final String type, final double nitrogen,
                         final double waterRetention, final double soilph,
                         final double organicMatter, final double rootDensity) {
        super(x, y, mass, scanned, name, type, nitrogen, waterRetention, soilph, organicMatter);
        this.rootDensity = rootDensity;
    }

    @Override
    public double initialSoilQuality() {
        return (getNitrogen() * NITROGEN_WEIGHT) + (getOrganicMatter() * ORGANIC_MATTER_WEIGHT)
                + (rootDensity * ROOT_DENSITY_WEIGHT);
    }

    @Override
    public double probFormula() {
        return ((BASE_PROB - rootDensity) + getWaterRetention() * WATER_RETENTION_FACTOR)
                / PROB_DIVISOR * PROB_MULTIPLIER;
    }
}
