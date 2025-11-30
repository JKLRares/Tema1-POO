package main;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public abstract class Soil extends Entity {
    private static final double MIN_SCORE = 0.0;
    private static final double MAX_SCORE = 100.0;
    private static final double ROUNDING_FACTOR = 100.0;
    private static final double THRESHOLD_GOOD = 70.0;
    private static final double THRESHOLD_MODERATE = 40.0;

    private double nitrogen;
    private double waterRetention;
    private double soilph;
    private double organicMatter;

    public Soil(int x, int y, double mass, boolean scanned, String name, String type,
                double nitrogen, double waterRetention, double soilph, double organicMatter) {
        super(x, y, mass, scanned, name, type);
        this.nitrogen = nitrogen;
        this.waterRetention = waterRetention;
        this.soilph = soilph;
        this.organicMatter = organicMatter;
    }

    // interactiuni cu plant si water

    public abstract double initialSoilQuality();
    public abstract double probFormula();

    public double soilQuality() {
        double score = this.initialSoilQuality();
        double normalizeScore = Math.max(MIN_SCORE, Math.min(MAX_SCORE, score));
        return Math.round(normalizeScore * ROUNDING_FACTOR) / ROUNDING_FACTOR;
    }

    public String resultSoilQuality() {
        double score = this.soilQuality();
        if (score >= THRESHOLD_GOOD) {
            return "good";
        } else if (score >= THRESHOLD_MODERATE) {
            return "moderate";
        } else {
            return "poor";
        }
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.SOIL;
    }
}