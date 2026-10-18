package main.air;

import main.Air;
import lombok.Getter;
import fileio.CommandInput;

@Getter
public final class Desert extends Air {
    private static final double OXYGEN_WEIGHT = 2.0;
    private static final double DUST_PENALTY = 0.2;
    private static final double TEMP_PENALTY = 0.3;
    private static final double STORM_PENALTY_VAL = 30.0;
    private static final int MAX_SCORE = 65;

    private final double dustParticles;
    private boolean desertStorm = false;

    public Desert(final int x, final int y, final double mass, final boolean scanned,
                  final String name, final String type, final double humidity,
                  final double temperature, final double oxygenLevel,
                  final double dustParticles) {
        super(x, y, mass, scanned, name, type, humidity, temperature, oxygenLevel);
        this.dustParticles = dustParticles;
        setAirQual(airQuality());
    }

    @Override
    public double initialAirQuality() {
        return (getOxygenLevel() * OXYGEN_WEIGHT) - (dustParticles * DUST_PENALTY)
                - (getTemperature() * TEMP_PENALTY);
    }

    @Override
    public boolean event(final CommandInput cmd) {
        if (cmd.getType() != null
                && "desertStorm".equals(cmd.getType().trim()) && cmd.isDesertStorm()) {
            this.desertStorm = cmd.isDesertStorm();
            applyWeatherEffect(-STORM_PENALTY_VAL, cmd.getTimestamp());
            return true;
        }
        return false;
    }

    @Override
    public void clearWeather() {
        desertStorm = false;
        super.clearWeather();
    }

    @Override
    public int maxScore() {
        return MAX_SCORE;
    }
}
