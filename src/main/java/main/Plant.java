package main;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public abstract class Plant extends Alive {
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

    public Plant(int x, int y, String type, String name, double mass, double growingLevel,
                 String level, boolean scanned) {
        super(x, y, mass, scanned, name, type);
        this.level = LEVEL_YOUNG;
        this.growingLevel = GROWTH_RESET;
        this.maturity = PlantMaturity.YOUNG;
        this.stuckProb = getStuckPossibility() / STUCK_PROB_DIVISOR;
    }

    public abstract double getOxygenOutput();
    public abstract int getStuckPossibility();

    public double getMaturityModifier() {
        return switch (this.maturity) {
            case YOUNG -> MATURITY_YOUNG;
            case MATURE -> MATURITY_MATURE;
            case OLD -> MATURITY_OLD;
            default -> MATURITY_DEFAULT;
        };
    }

    public void grow() {
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
                // fa planta sa moara
            }
        }
    }

    @Override
    public void die(Entity entity) {

    }

    public EntityType getEntityType() {
        return EntityType.PLANT;
    }
}