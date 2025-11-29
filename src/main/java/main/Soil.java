package main;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public abstract class Soil extends Entity {
    private double nitrogen, waterRetention, soilph, organicMatter;

    public Soil(int x, int y, double mass, boolean scanned, String name, String type, double nitrogen,
                double waterRetention, double soilph, double organicMatter) {
        super(x, y, mass, scanned, name, type);
        this.nitrogen = nitrogen;
        this.waterRetention = waterRetention;
        this.soilph = soilph;
        this.organicMatter = organicMatter;
    }

    //interactiuni cu plant si water

    public abstract double initialSoilQuality();
    public abstract double probFormula();

    public double soilQuality() {
        double score = this.initialSoilQuality();
        double normalizeScore = Math.max(0, Math.min(100, score));
        return Math.round(normalizeScore * 100.0) / 100.0;
    }

    public String resultSoilQuality() {
        double score = this.soilQuality();
        if (score >= 70) {
            return "good";
        } else if (score >= 40) {
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
