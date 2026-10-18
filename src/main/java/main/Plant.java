package main;

import java.util.Objects;
import lombok.Getter;

@Getter
public abstract class Plant extends Entity {
    private static final double GROWTH_THRESHOLD = 1.0;
    private static final double STUCK_PROB_DIVISOR = 100.0;
    private static final double MATURITY_YOUNG = 0.2;
    private static final double MATURITY_MATURE = 0.7;
    private static final double MATURITY_OLD = 0.4;

    private double growingLevel;
    private final double stuckProb;
    private PlantMaturity maturity;

    public Plant(final int x, final int y, final String type, final String name, final double mass,
                 final double growingLevel, final String level, final boolean scanned) {
        super(x, y, mass, scanned, name, type);
        setGrowingLevel(growingLevel);
        this.maturity = PlantMaturity.valueOf(Objects.requireNonNull(level));
        this.stuckProb = getStuckPossibility() / STUCK_PROB_DIVISOR;
    }

    /** Returns the species' oxygen contribution before its maturity modifier. */
    public abstract double getOxygenOutput();

    /** Returns the species' robot entanglement probability as a percentage. */
    public abstract int getStuckPossibility();

    /** Preserves the original string representation while keeping one lifecycle state. */
    public final String getLevel() {
        return maturity.name();
    }

    /** Rejects invalid growth values before they can corrupt the plant lifecycle. */
    public final void setGrowingLevel(final double growth) {
        if (!Double.isFinite(growth) || growth < 0) {
            throw new IllegalArgumentException("Plant growth must be finite and nonnegative");
        }
        growingLevel = growth;
    }

    /** Returns the oxygen modifier for the current lifecycle stage. */
    public final double getMaturityModifier() {
        return switch (maturity) {
            case YOUNG -> MATURITY_YOUNG;
            case MATURE -> MATURITY_MATURE;
            case OLD -> MATURITY_OLD;
            case DEAD -> 0.0;
        };
    }

    /** Advances one stage when accumulated growth reaches the threshold. */
    public final void grow() {
        if (growingLevel >= GROWTH_THRESHOLD && maturity != PlantMaturity.DEAD) {
            growingLevel = 0;
            maturity = switch (maturity) {
                case YOUNG -> PlantMaturity.MATURE;
                case MATURE -> PlantMaturity.OLD;
                case OLD, DEAD -> PlantMaturity.DEAD;
            };
        }
    }

    @Override
    public final void accept(final Cell cell) {
        cell.setPlant(this);
    }
}
