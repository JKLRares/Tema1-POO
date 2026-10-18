package main.air;

import main.Air;
import lombok.Getter;
import fileio.CommandInput;

@Getter
public final class Tropical extends Air {
    private static final double OXYGEN_WEIGHT = 2.0;
    private static final double HUMIDITY_WEIGHT = 0.5;
    private static final double CO2_PENALTY = 0.01;
    private static final double RAINFALL_BONUS = 0.3;
    private static final int MAX_SCORE = 82;
    private static final double CO2_ROUNDING = 100.0;

    private final double co2Level;
    private double rainfall;

    public Tropical(final int x, final int y, final double mass, final boolean scanned,
                    final String name, final String type, final double humidity,
                    final double temperature, final double oxygenLevel, final double co2Level) {
        super(x, y, mass, scanned, name, type, humidity, temperature, oxygenLevel);
        this.co2Level = Math.round(co2Level * CO2_ROUNDING) / CO2_ROUNDING;
        setAirQual(airQuality());
    }

    @Override
    public double initialAirQuality() {
        return (getOxygenLevel() * OXYGEN_WEIGHT) + (getHumidity() * HUMIDITY_WEIGHT)
                - (co2Level * CO2_PENALTY);
    }

    @Override
    public boolean event(final CommandInput cmd) {
        if (cmd.getType() != null
                && "rainfall".equals(cmd.getType().trim()) && cmd.getRainfall() > 0) {
            rainfall = cmd.getRainfall();
            applyWeatherEffect(rainfall * RAINFALL_BONUS, cmd.getTimestamp());
            return true;
        }
        return false;
    }

    @Override
    public int maxScore() {
        return MAX_SCORE;
    }
}
