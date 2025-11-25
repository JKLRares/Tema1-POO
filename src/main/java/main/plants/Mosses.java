package main.plants;

import main.Plant;
import lombok.Getter;

@Getter
public class Mosses extends Plant{
    public Mosses(int x, int y, String type, String name, double mass, double growingLevel, String level, boolean scanned) {
        super(x, y, type, name, mass, growingLevel, level, scanned);
    }

    @Override
    public double getOxygenOutput() {
        return 0.8;
    }

    @Override
    public int getStuck() {
        return 40;
    }
}
