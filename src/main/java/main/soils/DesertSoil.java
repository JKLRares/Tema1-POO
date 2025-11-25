package main.soils;

import main.Soil;
import lombok.Getter;

@Getter
public class DesertSoil extends Soil {
    private final double salinity;
    public DesertSoil(int x, int y, double mass, boolean scanned, String name, String type, double nitrogen,
                      double waterRetention, double soilph, double organicMatter, double salinity) {
        super(x, y, mass, scanned, name, type, nitrogen, waterRetention, soilph, organicMatter);
        this.salinity = salinity;
    }

    @Override
    public double initialSoilQuality() {
        return (getNitrogen() * 0.5) + (getWaterRetention() * 0.3) - (salinity * 2);
    }

    @Override
    public double probFormula() {
        return (100 - getWaterRetention() + salinity) / 100 * 100;
    }
}
