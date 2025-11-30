package main.animals;

import main.Animal;
import lombok.Getter;

@Getter
public final class Detritivores extends Animal {
    private static final int ATTACK_POSSIBILITY = 90;

    public Detritivores(final int x, final int y, final double mass, final boolean scanned,
                        final String name, final String type) {
        super(x, y, mass, scanned, name, type);
    }

    @Override
    public int getAttackPossibility() {
        return ATTACK_POSSIBILITY;
    }
}
