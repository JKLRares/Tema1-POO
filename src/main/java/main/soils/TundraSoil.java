package main.soils;

import main.Soil;
import lombok.Getter;

@Getter
public final class TundraSoil extends Soil {
    private static final double NITROGEN_WEIGHT = 0.7;
    private static final double ORGANIC_MATTER_WEIGHT = 0.5;
    private static final double PERMAFROST_PENALTY = 1.5;
    private static final double BASE_PROB = 50.0;
    private static final double PROB_DIVISOR = 50.0;
    private static final double PROB_MULTIPLIER = 100.0;

    private final double permafrostDepth;

    public TundraSoil(final int x, final int y, final double mass, final boolean scanned,
                      final String name, final String type, final double nitrogen,
                      final double waterRetention, final double soilph,
                      final double organicMatter, final double permafrostDepth) {
        super(x, y, mass, scanned, name, type, nitrogen, waterRetention, soilph, organicMatter);
        this.permafrostDepth = permafrostDepth;
    }

    @Override
    public double initialSoilQuality() {
        return (getNitrogen() * NITROGEN_WEIGHT) + (getOrganicMatter() * ORGANIC_MATTER_WEIGHT)
                - (permafrostDepth * PERMAFROST_PENALTY);
    }

    @Override
    public double probFormula() {
        return (BASE_PROB - permafrostDepth) / PROB_DIVISOR * PROB_MULTIPLIER;
    }
}
