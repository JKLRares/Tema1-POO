package main;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Water extends Entity{
    private double salinity, ph, purity, turbidity, contaminantIndex, waterQuality;
    private boolean isFrozen;
    private int frozenScore;

    public Water(int x, int y, double mass, boolean scanned, String name, String type, double salinity, double ph,
                 double purity, double turbidity, double contaminantIndex, double waterQuality, boolean isFrozen,
                 int frozenScore) {
        super(x, y, mass, scanned, name, type);
        this.salinity = 1 - (salinity / 350);
        this.ph = 1 - Math.abs(ph - 7.5) / 7.5;
        this.purity = purity / 100;
        this.turbidity = 1 - (turbidity / 100);
        this.contaminantIndex = 1 - (contaminantIndex / 100);
        this.frozenScore = normalizeIsFrozen(isFrozen);
        this.waterQuality = (0.3 * purity + 0.2 * ph + 0.15 * salinity + 0.1 * turbidity + 0.15 * contaminantIndex + 0.2 * frozenScore) * 100;
    }

    private int normalizeIsFrozen(boolean isFrozen) {
        if(isFrozen) {
            return 1;
        }
        return 0;
    }

    public String resultWaterQaulity(double waterQuality) {
        if(waterQuality >= 70) {
            return "Good";
        } else if(waterQuality >= 40) {
            return "Moderate";
        } else {
            return "Poor";
        }
    }

    // creste air soil si plant

    @Override
    public EntityType getEntityType() {
        return EntityType.WATER;
    }
}
