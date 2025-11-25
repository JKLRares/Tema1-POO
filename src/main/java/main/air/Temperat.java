package main.air;

import main.Air;
import lombok.Getter;

@Getter
public class Temperat extends Air {
    double pollenLevel;
    public Temperat(int x, int y, double mass, boolean scanned, String name, String type, double humidity,
                    double temperature, double oxygenLevel, double pollenLevel) {
        super(x, y, mass, scanned, name, type, humidity, temperature, oxygenLevel);
        this.pollenLevel = pollenLevel;
    }

    @Override
    public double initialAirQuality() {
        return (getOxygenLevel() * 2) + (getHumidity() * 0.7) - (pollenLevel * 0.1);
    }

    @Override
    public int maxScore() {
        return 84;
    }
}
