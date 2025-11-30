package main.air;

import main.Air;
import lombok.Getter;
import fileio.CommandInput;

@Getter
public final class Polar extends Air {
    private static final double OXYGEN_WEIGHT = 2.0;
    private static final double MAX_TEMP = 100.0;
    private static final double ICE_CRYSTAL_PENALTY = 0.05;
    private static final double STORM_PENALTY = 0.2;
    private static final int MAX_SCORE = 142;

    private final double iceCrystalConcentration;
    private double windSpeed;

    public Polar(final int x, final int y, final double mass, final boolean scanned,
                 final String name, final String type, final double humidity,
                 final double temperature, final double oxygenLevel,
                 final double iceCrystalConcentration) {
        super(x, y, mass, scanned, name, type, humidity, temperature, oxygenLevel);
        this.iceCrystalConcentration = iceCrystalConcentration;
        setAirQual(airQuality());
    }

    @Override
    public double initialAirQuality() {
        return (getOxygenLevel() * OXYGEN_WEIGHT) + (MAX_TEMP - Math.abs(getTemperature()))
                - (iceCrystalConcentration * ICE_CRYSTAL_PENALTY);
    }

    @Override
    public boolean event(final CommandInput cmd) {
        if (cmd.getType() != null && cmd.getType().trim().equals("polarStorm")) {
            this.windSpeed = cmd.getWindSpeed();
            setAirQual(getAirQual() - (this.windSpeed * STORM_PENALTY));
            return true;
        }
        return false;
    }

    @Override
    public int maxScore() {
        return MAX_SCORE;
    }
}
