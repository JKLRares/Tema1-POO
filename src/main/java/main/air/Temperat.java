package main.air;

import main.Air;
import lombok.Getter;
import fileio.CommandInput;

@Getter
public final class Temperat extends Air {
    private static final double OXYGEN_WEIGHT = 2.0;
    private static final double HUMIDITY_WEIGHT = 0.7;
    private static final double POLLEN_PENALTY = 0.1;
    private static final double SEASON_PENALTY_VAL = 15.0;
    private static final int MAX_SCORE = 84;

    private double pollenLevel;
    private String season;

    public Temperat(final int x, final int y, final double mass, final boolean scanned,
                    final String name, final String type, final double humidity,
                    final double temperature, final double oxygenLevel, final double pollenLevel) {
        super(x, y, mass, scanned, name, type, humidity, temperature, oxygenLevel);
        this.pollenLevel = pollenLevel;
        setAirQual(airQuality());
    }

    @Override
    public double initialAirQuality() {
        return (getOxygenLevel() * OXYGEN_WEIGHT) + (getHumidity() * HUMIDITY_WEIGHT)
                - (pollenLevel * POLLEN_PENALTY);
    }

    @Override
    public boolean event(final CommandInput cmd) {
        if (cmd.getSeason() != null) {
            season = cmd.getSeason();
            double seasonPenalty = season.equalsIgnoreCase("Spring") ? SEASON_PENALTY_VAL : 0;
            setAirQual(getAirQual() - seasonPenalty);
            return true;
        }
        return false;
    }

    @Override
    public int maxScore() {
        return MAX_SCORE;
    }
}
