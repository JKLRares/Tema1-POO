package main.soils;

import main.Soil;
import lombok.Getter;

@Getter
public final class ForestSoil extends Soil {
    private static final double NITROGEN_WEIGHT = 1.2;
    private static final double ORGANIC_MATTER_WEIGHT = 2.0;
    private static final double WATER_RETENTION_WEIGHT = 1.5;
    private static final double LEAF_LITTER_WEIGHT = 0.3;
    private static final double PROB_RETENTION_WEIGHT = 0.6;
    private static final double PROB_LITTER_WEIGHT = 0.4;
    private static final double PROB_DIVISOR = 80.0;
    private static final double PROB_MULTIPLIER = 100.0;

    private final double leafLitter;

    public ForestSoil(final int x, final int y, final double mass, final boolean scanned,
                      final String name, final String type, final double nitrogen,
                      final double waterRetention, final double soilph,
                      final double organicMatter, final double leafLitter) {
        super(x, y, mass, scanned, name, type, nitrogen, waterRetention, soilph, organicMatter);
        this.leafLitter = leafLitter;
    }

    @Override
    public double initialSoilQuality() {
        return (getNitrogen() * NITROGEN_WEIGHT) + (getOrganicMatter() * ORGANIC_MATTER_WEIGHT)
                + (getWaterRetention() * WATER_RETENTION_WEIGHT)
                + (leafLitter * LEAF_LITTER_WEIGHT);
    }

    @Override
    public double probFormula() {
        return (getWaterRetention() * PROB_RETENTION_WEIGHT + leafLitter * PROB_LITTER_WEIGHT)
                / PROB_DIVISOR * PROB_MULTIPLIER;
    }
}
