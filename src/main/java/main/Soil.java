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

    public Soil(final int x, final int y, final double mass, final boolean scanned,
                final String name, final String type, final double nitrogen,
                final double waterRetention, final double soilph, final double organicMatter) {
        super(x, y, mass, scanned, name, type);
        this.nitrogen = nitrogen;
        this.waterRetention = waterRetention;
        this.soilph = soilph;
        this.organicMatter = organicMatter;
    }

    /**
     * Calculates the initial quality of the soil based on its properties.
     * @return the initial soil quality value.
     */
    public abstract double initialSoilQuality();

    /**
     * Calculates the probability formula for the soil.
     * @return the probability value.
     */
    public abstract double probFormula();

    /**
     * Calculates the normalized soil quality score.
     * @return the soil quality score.
     */
    public final double soilQuality() {
        double score = this.initialSoilQuality();
        double normalizeScore = Math.max(MIN_SCORE, Math.min(MAX_SCORE, score));
        return Math.round(normalizeScore * ROUNDING_FACTOR) / ROUNDING_FACTOR;
    }

    /**
     * Determines the textual representation of the soil quality (good, moderate, poor).
     * @return a string representing the soil quality category.
     */
    public final String resultSoilQuality() {
        double score = this.soilQuality();
        if (score >= THRESHOLD_GOOD) {
            return "good";
        } else if (score >= THRESHOLD_MODERATE) {
            return "moderate";
        } else {
            return "poor";
        }
    }

    /** Adds retention while preserving the caller's chosen decimal precision. */
    public final void updateWaterRetention(final double retentionInc, final double roundingFactor) {
        double newRet = this.waterRetention + retentionInc;
        this.waterRetention = Math.round(newRet * roundingFactor) / roundingFactor;
    }

    @Override
    public final void accept(final Cell cell) {
        cell.setSoil(this);
    }
}
