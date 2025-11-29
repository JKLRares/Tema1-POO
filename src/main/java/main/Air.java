package main;
import lombok.Getter;
import lombok.Setter;
import fileio.CommandInput;

@Getter @Setter
public abstract class Air extends Entity {
    private double humidity, temperature, oxygenLevel, airQual;
    public Air(int x, int y, double mass, boolean scanned, String name, String type, double humidity,
               double temperature, double oxygenLevel) {
        super(x, y, mass, scanned, name, type);
        this.humidity = humidity;
        this.temperature = temperature;
        this.oxygenLevel = oxygenLevel;
    }

    public abstract double initialAirQuality();
    public abstract int maxScore();
    public void event(CommandInput cmd) {

    }

    public double airQuality() {
        double normalizeScore = Math.max(0, Math.min(100, initialAirQuality()));
        return Math.round(normalizeScore * 100.0) / 100.0;
    }

    public double airToxicity() {
        double toxicityAq = 100 * (1 - getAirQual() / maxScore());
        return Math.round(toxicityAq * 100.0) / 100.0;
    }

    public String airIsToxic() {
        if(airToxicity() > (0.8 * maxScore())) {
            return "toxic";
        }
        return "pure";
    }

    public String resultAirQaulity() {
        double score = getAirQual();
        if(score >= 70) {
            return "good";
        } else if(score >= 40) {
            return "moderate";
        } else {
            return "poor";
        }
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.AIR;
    }
}
