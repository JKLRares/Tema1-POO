package main.soils;
import lombok.Getter;

import main.Soil;

@Getter
public class SwampSoil extends Soil {
    private final double waterLogging;
    public SwampSoil(int x, int y, double mass, boolean scanned, String name, String type, double nitrogen,
                     double waterRetention, double soilph, double organicMatter, double waterLogging) {
        super(x, y, mass, scanned, name, type, nitrogen, waterRetention, soilph, organicMatter);
        this.waterLogging = waterLogging;
    }

    @Override
    public double initialSoilQuality() {
        return (getNitrogen() * 1.1) + (getOrganicMatter() * 2.2) - (waterLogging * 5);
    }

    @Override
    public double probFormula() {
        return waterLogging * 10;
    }
}
