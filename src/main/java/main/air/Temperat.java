package main.air;
import main.Air;
import lombok.Getter;
import fileio.CommandInput;

@Getter
public class Temperat extends Air {
    private double pollenLevel;
    private String season;
    public Temperat(int x, int y, double mass, boolean scanned, String name, String type, double humidity,
                    double temperature, double oxygenLevel, double pollenLevel) {
        super(x, y, mass, scanned, name, type, humidity, temperature, oxygenLevel);
        this.pollenLevel = pollenLevel;
        setAirQual(airQuality());
    }

    @Override
    public double initialAirQuality() {
        return (getOxygenLevel() * 2) + (getHumidity() * 0.7) - (pollenLevel * 0.1);
    }

    @Override
    public void event(CommandInput cmd) {
        season = cmd.getSeason();
        double a = 15, b = 0;
        double seasonPenalty = season.equalsIgnoreCase("Spring") ? 15 : 0;
        setAirQual(getAirQual() - seasonPenalty);
    }

    @Override
    public int maxScore() {
        return 84;
    }
}
