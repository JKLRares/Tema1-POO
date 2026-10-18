package main;

/** Shared energy rules for local exploration and route planning. */
public final class MovementCost {
    private MovementCost() {
    }

    /** Calculates the rounded hazard mean for entering a destination cell. */
    public static int toEnter(final Cell cell) {
        int components = 2;
        double score = Math.max(cell.getAir().airToxicity(), 0) + cell.getSoil().probFormula();
        if (cell.getAnimal() != null) {
            score += cell.getAnimal().getAttackProb();
            components++;
        }
        if (cell.getPlant() != null) {
            score += cell.getPlant().getStuckProb();
            components++;
        }
        long rounded = Math.round(Math.abs(score / components));
        return (int) Math.min(Integer.MAX_VALUE, rounded);
    }
}
