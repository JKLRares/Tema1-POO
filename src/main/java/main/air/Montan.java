package main.air;

import lombok.Getter;
import main.Air;
import fileio.CommandInput;

@Getter
public final class Montan extends Air {
    private static final double ALTITUDE_DIVISOR = 1000.0;
    private static final double ALTITUDE_PENALTY = 0.5;
    private static final double OXYGEN_WEIGHT = 2.0;
    private static final double HUMIDITY_WEIGHT = 0.6;
    private static final double HIKER_PENALTY = 0.1;
    private static final int MAX_SCORE = 78;

    private final double altitude;

    public Montan(final int x, final int y, final double mass, final boolean scanned,
                  final String name, final String type, final double humidity,
                  final double temperature, final double oxygenLevel, final double altitude) {
        super(x, y, mass, scanned, name, type, humidity, temperature, oxygenLevel);
        this.altitude = altitude;
        setAirQual(airQuality());
    }

    @Override
    public double initialAirQuality() {
        double oxygenFactor = getOxygenLevel() - (altitude / ALTITUDE_DIVISOR * ALTITUDE_PENALTY);
        return (oxygenFactor * OXYGEN_WEIGHT) + (getHumidity() * HUMIDITY_WEIGHT);
    }

    @Override
    public boolean event(final CommandInput cmd) {
        if (cmd.getNumberOfHikers() > 0) {
            setAirQual(getAirQual() - (cmd.getNumberOfHikers() * HIKER_PENALTY));
            return true;
        }
        return false;
    }

    @Override
    public int maxScore() {
        return MAX_SCORE;
    }
}
