package main.air;

import main.Air;
import lombok.Getter;

@Getter
public class Polar extends Air {
    private final double iceCrystalConcentration;
    public Polar(int x, int y, double mass, boolean scanned, String name, String type, double humidity,
                 double temperature, double oxygenLevel, double iceCrystalConcentration) {
        super(x, y, mass, scanned, name, type, humidity, temperature, oxygenLevel);
        this.iceCrystalConcentration = iceCrystalConcentration;
    }

    @Override
    public double initialAirQuality() {
        return (getOxygenLevel() * 2) + (100 - Math.abs(getTemperature())) - (iceCrystalConcentration * 0.05);
    }

    @Override
    public int maxScore() {
        return 142;
    }
}
