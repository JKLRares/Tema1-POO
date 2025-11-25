package main.soils;

import main.Soil;
import lombok.Getter;

@Getter
public class TundraSoil extends Soil {
    private final double permafrostDepth;
    public TundraSoil(int x, int y, double mass, boolean scanned, String name, String type, double nitrogen,
                      double waterRetention, double soilph, double organicMatter, double permafrostDepth) {
        super(x, y, mass, scanned, name, type, nitrogen, waterRetention, soilph, organicMatter);
        this.permafrostDepth = permafrostDepth;
    }

    @Override
    public double initialSoilQuality() {
        return (getNitrogen() * 0.7) + (getOrganicMatter() * 0.5) - (permafrostDepth * 1.5);
    }

    @Override
    public double probFormula() {
        return (50 - permafrostDepth) / 50 * 100;
    }
}
