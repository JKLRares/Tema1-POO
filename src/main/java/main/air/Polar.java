package main.air;
import main.Air;
import lombok.Getter;
import fileio.CommandInput;

@Getter
public class Polar extends Air {
    private final double iceCrystalConcentration;
    private double windSpeed;
    public Polar(int x, int y, double mass, boolean scanned, String name, String type, double humidity,
                 double temperature, double oxygenLevel, double iceCrystalConcentration) {
        super(x, y, mass, scanned, name, type, humidity, temperature, oxygenLevel);
        this.iceCrystalConcentration = iceCrystalConcentration;
        setAirQual(airQuality());
    }

    @Override
    public double initialAirQuality() {
        return (getOxygenLevel() * 2) + (100 - Math.abs(getTemperature())) - (iceCrystalConcentration * 0.05);
    }

    @Override
    public boolean event(CommandInput cmd) {
        if(cmd.getType() != null && cmd.getType().trim().equals("polarStorm")) {
            this.windSpeed = cmd.getWindSpeed();
            double a = 0.2;
            setAirQual(getAirQual() - (this.windSpeed * a));
            return true;
        }
        return false;
    }

    @Override
    public int maxScore() {
        return 142;
    }
}
