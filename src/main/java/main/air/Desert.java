package main.air;

import main.Air;
import lombok.Getter;

@Getter
public class Desert extends Air {
    private final double dustParticles;
    public Desert(int x, int y, double mass, boolean scanned, String name, String type, double humidity,
                  double temperature, double oxygenLevel, double dustParticles) {
        super(x, y, mass, scanned, name, type, humidity, temperature, oxygenLevel);
        this.dustParticles = dustParticles;
    }

    @Override
    public double initialAirQuality() {
        return (getOxygenLevel() * 2) - (dustParticles * 0.2) - (getTemperature() * 0.3);
    }

    @Override
    public int maxScore() {
        return 65;
    }
}
