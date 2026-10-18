package main;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public final class Water extends Entity {
    private static final double SALINITY_DIVISOR = 350.0;
    private static final double PH_TARGET = 7.5;
    private static final double NORMALIZE_100 = 100.0;
    private static final double QUALITY_WEIGHT_PURITY = 0.3;
    private static final double QUALITY_WEIGHT_PH = 0.2;
    private static final double QUALITY_WEIGHT_SALINITY = 0.15;
    private static final double QUALITY_WEIGHT_TURBIDITY = 0.1;
    private static final double QUALITY_WEIGHT_CONTAMINANT = 0.15;
    private static final double QUALITY_WEIGHT_FROZEN = 0.2;
    private static final double THRESHOLD_GOOD = 70.0;
    private static final double THRESHOLD_MODERATE = 40.0;
    private static final int FROZEN_VALUE = 1;
    private static final int NOT_FROZEN_VALUE = 0;

    private double salinity;
    private double ph;
    private double purity;
    private double turbidity;
    private double contaminantIndex;
    private double waterQuality;
    private boolean isFrozen;
    private int scanTimestamp;
    private int frozenScore;

    public Water(final int x, final int y, final double mass, final boolean scanned,
                 final String name, final String type, final double salinity, final double ph,
                 final double purity, final double turbidity, final double contaminantIndex,
                 final double waterQuality, final boolean isFrozen, final int frozenScore) {
        super(x, y, mass, scanned, name, type);
        this.salinity = 1 - (salinity / SALINITY_DIVISOR);
        this.ph = 1 - Math.abs(ph - PH_TARGET) / PH_TARGET;
        this.purity = purity / NORMALIZE_100;
        this.turbidity = 1 - (turbidity / NORMALIZE_100);
        this.contaminantIndex = 1 - (contaminantIndex / NORMALIZE_100);
        this.isFrozen = isFrozen;
        this.frozenScore = normalizeIsFrozen(isFrozen);
        this.waterQuality = (QUALITY_WEIGHT_PURITY * this.purity
                + QUALITY_WEIGHT_PH * this.ph
                + QUALITY_WEIGHT_SALINITY * this.salinity
                + QUALITY_WEIGHT_TURBIDITY * this.turbidity
                + QUALITY_WEIGHT_CONTAMINANT * this.contaminantIndex
                + QUALITY_WEIGHT_FROZEN * this.frozenScore) * NORMALIZE_100;
    }

    private int normalizeIsFrozen(final boolean frozen) {
        if (frozen) {
            return FROZEN_VALUE;
        }
        return NOT_FROZEN_VALUE;
    }


    @Override
    public void accept(final Cell cell) {
        cell.setWater(this);
    }
}
