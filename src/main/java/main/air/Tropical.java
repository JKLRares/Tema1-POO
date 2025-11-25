package main.air;

import main.Air;
import lombok.Getter;

@Getter
public class Tropical extends Air {
    private final double co2Level;
    public Tropical(int x, int y, double mass, boolean scanned, String name, String type, double humidity,
                    double temperature, double oxygenLevel, double co2Level) {
        super(x, y, mass, scanned, name, type, humidity, temperature, oxygenLevel);
        this.co2Level = co2Level;
    }

    @Override
    public double initialAirQuality() {
        return (getOxygenLevel() * 2) + (getHumidity() * 0.5) - (co2Level * 0.01);
    }

    @Override
    public int maxScore() {
        return 82;
    }
}
