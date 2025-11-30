package main.air;
import main.Air;
import lombok.Getter;
import fileio.CommandInput;

@Getter
public class Tropical extends Air {
    private final double co2Level;
    private double rainfall;
    public Tropical(int x, int y, double mass, boolean scanned, String name, String type, double humidity,
                    double temperature, double oxygenLevel, double co2Level) {
        super(x, y, mass, scanned, name, type, humidity, temperature, oxygenLevel);
        this.co2Level = Math.round(co2Level * 100.0) / 100.0;
        setAirQual(airQuality());
    }

    @Override
    public double initialAirQuality() {
        return (getOxygenLevel() * 2) + (getHumidity() * 0.5) - (co2Level * 0.01);
    }

    @Override
    public boolean event(CommandInput cmd) {
        if (cmd.getRainfall() > 0) {
            rainfall = cmd.getRainfall();
            double a = 0.3;
            setAirQual(getAirQual() + (rainfall * 0.3));
            return true;
        }
        return false;
    }

    @Override
    public int maxScore() {
        return 82;
    }
}
