package main.plants;

import main.Plant;
import lombok.Getter;

@Getter
public class Ferns extends Plant{
    public Ferns(int x, int y, String type, String name, double mass, double growingLevel, String level, boolean scanned) {
        super(x, y, type, name, mass, growingLevel, level, scanned);
    }

    @Override
    public double getOxygenOutput() {
        return 0;
    }

    @Override
    public int getStuckPossibility() {
        return 30;
    }
}
