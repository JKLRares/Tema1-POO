package main;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public abstract class Animal extends Alive {
    private static final String STATE_HUNGRY = "hungry";
    private static final String STATE_SICK = "sick";
    private static final String STATE_WELL_FED = "well-fed";
    private static final String TYPE_CARNIVORE = "CARNIVORE";
    private static final String TYPE_CARNIVORES = "CARNIVORES";
    private static final String TYPE_PARASITES = "PARASITES";
    private static final String TOXIC_AIR = "toxic";
    private static final double ATTACK_BASE = 100.0;
    private static final double ATTACK_DIVISOR = 10.0;
    private static final double SCORE_BOTH = 10000.0;
    private static final double SCORE_PLANT = 5000.0;
    private static final double SCORE_WATER = 200.0;
    private static final double MIN_SCORE = 0.0;
    private static final double INTAKE_RATE = 0.08;
    private static final double ORGANIC_LOW = 0.5;
    private static final double ORGANIC_HIGH = 0.8;

    private boolean fed = false;
    private String animalState = STATE_HUNGRY;
    private double attackProb;
    private double nextOrganicMatter = 0.0;
    private int scanTimestamp = 0;
    private int foodEatenCount = 0;

    public Animal(int x, int y, double mass, boolean scanned, String name, String type) {
        super(x, y, mass, scanned, name, type);
        this.attackProb = (ATTACK_BASE - getAttackPossibility()) / ATTACK_DIVISOR;
    }

    public void tryToAct(Matrix map, int timestamp) {
        System.out.println("DEBUG [Time " + timestamp + "] " + getName() + " at (" + getX()
                + "," + getY() + "): TryToAct called. IsScanned: " + isScanned());
        if (isScanned()) {
            Cell best = findBestNeighbor(map);
            if (best != null) {
                System.out.println("DEBUG [Time " + timestamp + "] " + getName()
                        + ": DECISION -> Moving to (" + best.getX() + "," + best.getY() + ")");
                interact(map.getCell(getX(), getY()), map, best);
                move(map, best);
            }
        }
    }

    private Cell findBestNeighbor(Matrix map) {
        int[] vx = {0, 1, 0, -1};
        int[] vy = {1, 0, -1, 0};

        Cell best = null;
        double bestScore = -1.0;

        for (int i = 0; i < 4; i++) {
            int nx = getX() + vx[i];
            int ny = getY() + vy[i];
            if (nx >= 0 && nx < map.getWidth() && ny >= 0 && ny < map.getHeight()) {
                Cell neighbor = map.getCell(nx, ny);
                double score = calculateScore(neighbor);
                boolean isCarnivore = getType().equalsIgnoreCase(TYPE_CARNIVORES) ||
                        getType().equalsIgnoreCase(TYPE_PARASITES);

                if (!isCarnivore && neighbor.getAnimal() != null) {
                    continue;
                }
                System.out.println("DEBUG:   Checking neighbor (" + nx + "," + ny
                        + ") -> Score: " + score);
                if (score > bestScore) {
                    bestScore = score;
                    best = neighbor;
                }
            }
        }
        return best;
    }

    public void move(Matrix map, Cell best) {
        Cell oldCell = map.getCell(getX(), getY());
        oldCell.setAnimal(null);
        setX(best.getX());
        setY(best.getY());
        best.setAnimal(this);
    }

    private double calculateScore(Cell cell) {
        boolean hasPlant = cell.getPlant() != null && cell.getPlant().isScanned();
        boolean hasWater = cell.getWater() != null && cell.getWater().isScanned();
        if (cell.getPlant() != null || cell.getWater() != null) {
            String plantStatus = (cell.getPlant() == null) ? "None" :
                    (cell.getPlant().getName() + " (Scanned=" + cell.getPlant().isScanned() + ")");
            String waterStatus = (cell.getWater() == null) ? "None" :
                    (cell.getWater().getName() + " (Scanned=" + cell.getWater().isScanned() + ")");

            System.out.println("DEBUG:     Inside (" + cell.getX() + "," + cell.getY()
                    + ") saw: Plant=[" + plantStatus + "] Water=[" + waterStatus + "]");
        }
        if (hasPlant && hasWater) {
            return SCORE_BOTH + cell.getWater().getWaterQuality();
        } else if (hasPlant) {
            return SCORE_PLANT;
        } else if (hasWater) {
            return SCORE_WATER + cell.getWater().getWaterQuality();
        }
        return MIN_SCORE;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ANIMAL;
    }

    public abstract int getAttackPossibility();

    public void interact(Cell cell, Matrix map, Cell best) {
        if (fed && !animalState.equals(STATE_SICK) && cell.getSoil() != null) {
            cell.getSoil().setOrganicMatter(cell.getSoil().getOrganicMatter() + nextOrganicMatter);
        }
        fed = false;
        animalState = STATE_HUNGRY;
        Animal potentialVictim = best.getAnimal();
        Plant plantOnCell = best.getPlant();
        Water waterOnCell = best.getWater();

        boolean isPredator = getType().equalsIgnoreCase(TYPE_CARNIVORE) ||
                getType().equalsIgnoreCase(TYPE_PARASITES);
        boolean victimExists = potentialVictim != null
                && potentialVictim.getEntityType() == EntityType.ANIMAL;

        if (victimExists && isPredator) {
            eatAnimal(cell, map, best);
            fed = true;
            foodEatenCount++;
            updateOrganicMatter();
        }

        if (!fed) {
            boolean plantOnCellExists = plantOnCell != null;
            boolean waterOnCellExists = waterOnCell != null;
            if (plantOnCellExists && plantOnCell.isScanned()
                    && waterOnCellExists && waterOnCell.isScanned()) {
                eatPlant(cell, map, best);
                drink(cell, map, best);
                fed = true;
                foodEatenCount += 2;
                updateOrganicMatter();
            } else if (plantOnCellExists && plantOnCell.isScanned()) {
                eatPlant(cell, map, best);
                fed = true;
                foodEatenCount++;
                updateOrganicMatter();
            } else if (waterOnCellExists && waterOnCell.isScanned()) {
                drink(cell, map, best);
                fed = true;
                foodEatenCount++;
                updateOrganicMatter();
            }
        }

        if (fed) {
            animalState = STATE_WELL_FED;
        }

        if (cell.getAir().airIsToxic().equalsIgnoreCase(TOXIC_AIR)) {
            animalState = STATE_SICK;
        }
    }

    private void updateOrganicMatter() {
        if (foodEatenCount == 1) {
            nextOrganicMatter = ORGANIC_LOW;
        } else if (foodEatenCount >= 2) {
            nextOrganicMatter = ORGANIC_HIGH;
        }
    }

    private void eatAnimal(Cell cell, Matrix map, Cell victim) {
        setMass(getMass() + victim.getAnimal().getMass());
        victim.setAnimal(null);
    }

    private void eatPlant(Cell cell, Matrix map, Cell victim) {
        setMass((getMass() + victim.getPlant().getMass()));
        victim.setPlant(null);
    }

    private void drink(Cell cell, Matrix map, Cell victim) {
        double waterToDrink = Math.min(getMass() * INTAKE_RATE, victim.getWater().getMass());
        victim.getWater().setMass(victim.getWater().getMass() - waterToDrink);
        setMass(getMass() + waterToDrink);
        if (victim.getWater().getMass() <= 0) {
            victim.setWater(null);
        }
    }

    @Override
    public void die(Entity entity) {

    }
}