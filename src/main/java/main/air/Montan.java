package main.air;
import lombok.Getter;
import main.Air;
import fileio.CommandInput;

@Getter
public class Montan extends Air {
    private final double altitude;
    public Montan(int x, int y, double mass, boolean scanned, String name, String type, double humidity,
                  double temperature, double oxygenLevel, double altitude) {
        super(x, y, mass, scanned, name, type, humidity, temperature, oxygenLevel);
        this.altitude = altitude;
        setAirQual(airQuality());
    }

    @Override
    public double initialAirQuality() {
        double oxygenFactor = getOxygenLevel() - (altitude / 1000 * 0.5);
        return (oxygenFactor * 2) + (getHumidity() * 0.6);
    }

    @Override
    public boolean event(CommandInput cmd) {
        if (cmd.getNumberOfHikers() > 0) {
            double a = 0.1;
            setAirQual(getAirQual() - (cmd.getNumberOfHikers() * a));
            return true;
        }
        return false;
    }

    @Override
    public int maxScore() {
        return 78;
    }
}
