package main.soils;

import main.Soil;
import lombok.Getter;

@Getter
public class GrasslandSoil extends Soil {
    double rootDensity;
    public GrasslandSoil(int x, int y, double mass, boolean scanned, String name, String type, double nitrogen,
                         double waterRetention, double soilph, double organicMatter, double rootDensity) {
        super(x, y, mass, scanned, name, type, nitrogen, waterRetention, soilph, organicMatter);
        this.rootDensity = rootDensity;
    }

    @Override
    public double initialSoilQuality() {
        return (getNitrogen() * 1.3) + (getOrganicMatter() * 1.5) + (rootDensity * 0.8);
    }

    @Override
    public double probFormula() {
        return ((50 - rootDensity) + getWaterRetention() * 0.5) / 75 * 100;
    }
}
