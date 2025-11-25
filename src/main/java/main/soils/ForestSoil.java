package main.soils;

import main.Soil;
import lombok.Getter;

@Getter
public class ForestSoil extends Soil {
    private final double leafLitter;
    public ForestSoil(int x, int y, double mass, boolean scanned, String name, String type, double nitrogen,
                      double waterRetention, double soilph, double organicMatter, double leafLitter) {
        super(x, y, mass, scanned, name, type, nitrogen, waterRetention, soilph, organicMatter);
        this.leafLitter = leafLitter;
    }

    @Override
    public double initialSoilQuality() {
        return (getNitrogen() * 1.2) + (getOrganicMatter() * 2) + (getWaterRetention() * 1.5) + (leafLitter *
                0.3);
    }

    @Override
    public double probFormula() {
        return (getWaterRetention() * 0.6 + leafLitter * 0.4) / 80 * 100;
    }
}
