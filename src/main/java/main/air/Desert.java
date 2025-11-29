package main.air;
import main.Air;
import lombok.Getter;
import fileio.CommandInput;

@Getter
public class Desert extends Air {
    private final double dustParticles;
    private boolean desertStorm = false;
    public Desert(int x, int y, double mass, boolean scanned, String name, String type, double humidity,
                  double temperature, double oxygenLevel, double dustParticles) {
        super(x, y, mass, scanned, name, type, humidity, temperature, oxygenLevel);
        this.dustParticles = dustParticles;
        setAirQual(airQuality());
    }

    @Override
    public double initialAirQuality() {
        return (getOxygenLevel() * 2) - (dustParticles * 0.2) - (getTemperature() * 0.3);
    }

    @Override
    public void event(CommandInput cmd) {
        this.desertStorm = cmd.isDesertStorm();
        double a = 30, b = 0;
        setAirQual(getAirQual() - (this.desertStorm ? 30 : 0));
    }

    @Override
    public int maxScore() {
        return 65;
    }
}
