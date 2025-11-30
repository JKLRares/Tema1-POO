package main.soils;

import main.Soil;
import lombok.Getter;

@Getter
public final class DesertSoil extends Soil {
    private static final double NITROGEN_WEIGHT = 0.5;
    private static final double WATER_RETENTION_WEIGHT = 0.3;
    private static final double SALINITY_PENALTY = 2.0;
    private static final double MAX_PERCENTAGE = 100.0;
    private static final double PROB_DIVISOR = 100.0;
    private static final double PROB_MULTIPLIER = 100.0;

    private final double salinity;

    public DesertSoil(final int x, final int y, final double mass, final boolean scanned,
                      final String name, final String type, final double nitrogen,
                      final double waterRetention, final double soilph,
                      final double organicMatter, final double salinity) {
        super(x, y, mass, scanned, name, type, nitrogen, waterRetention, soilph, organicMatter);
        this.salinity = salinity;
    }

    @Override
    public double initialSoilQuality() {
        return (getNitrogen() * NITROGEN_WEIGHT) + (getWaterRetention() * WATER_RETENTION_WEIGHT)
                - (salinity * SALINITY_PENALTY);
    }

    @Override
    public double probFormula() {
        return (MAX_PERCENTAGE - getWaterRetention() + salinity) / PROB_DIVISOR * PROB_MULTIPLIER;
    }
}
