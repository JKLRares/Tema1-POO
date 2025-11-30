package main;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public abstract class Plant extends Entity {
    private static final String LEVEL_YOUNG = "YOUNG";
    private static final String LEVEL_MATURE = "MATURE";
    private static final String LEVEL_OLD = "OLD";
    private static final String LEVEL_DEAD = "DEAD";
    private static final double GROWTH_RESET = 0.0;
    private static final double GROWTH_THRESHOLD = 1.0;
    private static final double STUCK_PROB_DIVISOR = 100.0;
    private static final double MATURITY_YOUNG = 0.2;
    private static final double MATURITY_MATURE = 0.7;
    private static final double MATURITY_OLD = 0.4;
    private static final double MATURITY_DEFAULT = 0.0;

    private String level;
    private double growingLevel;
    private double stuckProb;

    private PlantMaturity maturity;

    public Plant(final int x, final int y, final String type, final String name, final double mass,
                 final double growingLevel, final String level, final boolean scanned) {
        super(x, y, mass, scanned, name, type);
        this.level = LEVEL_YOUNG;
        this.growingLevel = GROWTH_RESET;
        this.maturity = PlantMaturity.YOUNG;
        this.stuckProb = getStuckPossibility() / STUCK_PROB_DIVISOR;
    }

    /**
     * Gets the oxygen output of the plant.
     * @return the amount of oxygen produced.
     */
    public abstract double getOxygenOutput();

    /**
     * Gets the probability of a robot getting stuck in this plant.
     * @return the stuck probability percentage.
     */
    public abstract int getStuckPossibility();

    /**
     * Gets the modifier value based on the plant's current maturity.
     * @return the maturity modifier.
     */
    public final double getMaturityModifier() {
        return switch (this.maturity) {
            case YOUNG -> MATURITY_YOUNG;
            case MATURE -> MATURITY_MATURE;
            case OLD -> MATURITY_OLD;
            default -> MATURITY_DEFAULT;
        };
    }

    /**
     * Increases the plant's growth level and advances its maturity stage if the threshold is met.
     */
    public final void grow() {
        if (this.growingLevel >= GROWTH_THRESHOLD) {
            this.growingLevel = GROWTH_RESET;
            if (this.level.equals(LEVEL_YOUNG)) {
                this.level = LEVEL_MATURE;
                this.maturity = PlantMaturity.MATURE;
            } else if (this.level.equals(LEVEL_MATURE)) {
                this.level = LEVEL_OLD;
                this.maturity = PlantMaturity.OLD;
            } else if (this.level.equals(LEVEL_OLD)) {
                this.level = LEVEL_DEAD;
            }
        }
    }

    @Override
    public void accept(final Cell cell) {
        cell.setPlant(this);
    }
}
