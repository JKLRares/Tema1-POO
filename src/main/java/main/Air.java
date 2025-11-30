package main;

import lombok.Getter;
import lombok.Setter;
import fileio.CommandInput;

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

    public Air(int x, int y, double mass, boolean scanned, String name, String type,
               double humidity, double temperature, double oxygenLevel) {
        super(x, y, mass, scanned, name, type);
        this.humidity = humidity;
        this.temperature = temperature;
        this.oxygenLevel = oxygenLevel;
    }

    public abstract double initialAirQuality();
    public abstract int maxScore();
    public abstract boolean event(CommandInput cmd);

    public double airQuality() {
        double normalizeScore = Math.max(MIN_BOUND, Math.min(MAX_BOUND, initialAirQuality()));
        return Math.round(normalizeScore * ROUNDING_FACTOR) / ROUNDING_FACTOR;
    }

    public double airToxicity() {
        double toxicityAq = MAX_BOUND * (1 - getAirQual() / maxScore());
        return Math.round(toxicityAq * ROUNDING_FACTOR) / ROUNDING_FACTOR;
    }

    public String airIsToxic() {
        if (airToxicity() > (TOXICITY_THRESHOLD * maxScore())) {
            return STATE_TOXIC;
        }
        return STATE_PURE;
    }

    public String resultAirQaulity() {
        double score = getAirQual();
        if (score >= THRESHOLD_GOOD) {
            return QUALITY_GOOD;
        } else if (score >= THRESHOLD_MODERATE) {
            return QUALITY_MODERATE;
        } else {
            return QUALITY_POOR;
        }
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.AIR;
    }
}