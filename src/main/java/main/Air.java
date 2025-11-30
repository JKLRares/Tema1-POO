package main;

import fileio.CommandInput;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public abstract class Air extends Entity {
    private static final double MIN_BOUND = 0.0;
    private static final double MAX_BOUND = 100.0;
    private static final double ROUNDING_FACTOR = 100.0;
    private static final double TOXICITY_THRESHOLD = 0.8;
    private static final double THRESHOLD_GOOD = 70.0;
    private static final double THRESHOLD_MODERATE = 40.0;
    private static final String STATE_TOXIC = "toxic";
    private static final String STATE_PURE = "pure";
    private static final String QUALITY_GOOD = "good";
    private static final String QUALITY_MODERATE = "moderate";
    private static final String QUALITY_POOR = "poor";

    private double humidity;
    private double temperature;
    private double oxygenLevel;
    private double airQual;

    public Air(final int x, final int y, final double mass, final boolean scanned,
               final String name, final String type, final double humidity,
               final double temperature, final double oxygenLevel) {
        super(x, y, mass, scanned, name, type);
        this.humidity = humidity;
        this.temperature = temperature;
        this.oxygenLevel = oxygenLevel;
    }

    /**
     * Calculates the initial air quality based on its properties.
     * @return the initial air quality value.
     */
    public abstract double initialAirQuality();

    /**
     * Returns the maximum possible score for this air type.
     * @return the max score.
     */
    public abstract int maxScore();

    /**
     * Processes a weather event affecting the air.
     * @param cmd the command input containing event details.
     * @return true if the event affected the air, false otherwise.
     */
    public abstract boolean event(CommandInput cmd);

    /**
     * Calculates the current air quality, normalized within bounds.
     * @return the normalized air quality.
     */
    public final double airQuality() {
        double normalizeScore = Math.max(MIN_BOUND, Math.min(MAX_BOUND, initialAirQuality()));
        return Math.round(normalizeScore * ROUNDING_FACTOR) / ROUNDING_FACTOR;
    }

    /**
     * Calculates the toxicity level of the air.
     * @return the air toxicity value.
     */
    public final double airToxicity() {
        double toxicityAq = MAX_BOUND * (1 - getAirQual() / maxScore());
        return Math.round(toxicityAq * ROUNDING_FACTOR) / ROUNDING_FACTOR;
    }

    /**
     * Determines if the air is toxic based on the toxicity threshold.
     * @return "toxic" if the air is toxic, "pure" otherwise.
     */
    public final String airIsToxic() {
        if (airToxicity() > (TOXICITY_THRESHOLD * maxScore())) {
            return STATE_TOXIC;
        }
        return STATE_PURE;
    }

    /**
     * Returns a textual description of the air quality (good, moderate, poor).
     * @return the quality description string.
     */
    public final String resultAirQaulity() {
        double score = getAirQual();
        if (score >= THRESHOLD_GOOD) {
            return QUALITY_GOOD;
        } else if (score >= THRESHOLD_MODERATE) {
            return QUALITY_MODERATE;
        } else {
            return QUALITY_POOR;
        }
    }

    public void updateEnvironmentParams(final double humidityInc, final double roundingFactor) {
        double newHum = this.humidity + humidityInc;
        this.humidity = Math.round(newHum * roundingFactor) / roundingFactor;
        this.airQual = airQuality();
    }

    @Override
    public void accept(final Cell cell) {
        cell.setAir(this);
    }
}
