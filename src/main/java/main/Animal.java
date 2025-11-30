package main;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public abstract class Animal extends Alive {
    private boolean fed = false;
    private String animalState = "hungry";
    private double attackProb, nextOrganicMatter = 0.0;
    private int scanTimestamp = 0;

    public Animal(int x, int y, double mass, boolean scanned, String name, String type) {
        super(x, y, mass, scanned, name, type);
        this.attackProb = (100 - getAttackPossibility()) / 10.0;
    }

    public void tryToAct(Matrix map) {
        if(isScanned()) {
            Cell best = findBestNeighbor(map);
            interact(map.getCell(getX(), getY()), map, best);
            move(map, best);
        }
    }

    private Cell findBestNeighbor(Matrix map) {
        int[] vx = {0, 1, 0, -1};
        int[] vy = {1, 0, -1, 0};

        Cell best = null;
        double bestScore = -1.0;

        for(int i = 0; i < 4; i++) {
            int nx = getX() + vx[i];
            int ny = getY() + vy[i];
            if(nx >= 0 && nx < map.getWidth() && ny >= 0 && ny < map.getHeight()) {
                Cell neighbor = map.getCell(nx, ny);
                double score = calculateScore(neighbor);
                if(score > bestScore) {
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

        if(hasPlant && hasWater) {
            return 10000.0 + cell.getWater().getWaterQuality();
        } else if (hasPlant) {
            return 5000.0;
        } else if (hasWater) {
            return 200.0 + cell.getWater().getWaterQuality();
        }
        return 0.0;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ANIMAL;
    }

    public abstract int getAttackPossibility();

    public void interact(Cell cell, Matrix map, Cell best) {
        if (fed && !animalState.equals("sick") && cell.getSoil() != null) {
            cell.getSoil().setOrganicMatter(cell.getSoil().getOrganicMatter() + nextOrganicMatter);
        }
        fed = false;
        animalState = "hungry";
        nextOrganicMatter = 0;
        Animal potentialVictim = best.getAnimal();
        Plant plantOnCell = best.getPlant();
        Water waterOnCell = best.getWater();

        if((potentialVictim != null && potentialVictim.getEntityType() == EntityType.ANIMAL) &&
                (getType().equalsIgnoreCase("CARNIVORE") ||
                        getType().equalsIgnoreCase("PARASITES"))) {
            eatAnimal(cell, map, best);
            fed = true;
            nextOrganicMatter = 0.5;
        }

        if(!fed) {
            boolean plantOnCellExists = plantOnCell != null;
            boolean waterOnCellExists = waterOnCell != null;
            if(plantOnCellExists && plantOnCell.isScanned() && waterOnCellExists && waterOnCell.isScanned()) {
                eatPlant(cell, map, best);
                drink(cell, map, best);
                fed = true;
                nextOrganicMatter = 0.8;
            } else if(plantOnCellExists && plantOnCell.isScanned()) {
                eatPlant(cell, map, best);
                fed = true;
                nextOrganicMatter = 0.5;
            } else if(waterOnCellExists && waterOnCell.isScanned()) {
                drink(cell, map, best);
                fed = true;
                nextOrganicMatter = 0.5;
            }
        }

        if(fed) {
            animalState = "well-fed";
        }

        if(cell.getAir().airIsToxic().equalsIgnoreCase("toxic")) {
            animalState = "sick";
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
        double intakeRate = 0.08;
        double waterToDrink = Math.min(getMass() * intakeRate, victim.getWater().getMass());
        victim.getWater().setMass(victim.getWater().getMass() - waterToDrink);
        setMass(getMass() + waterToDrink);
        if(victim.getWater().getMass() <= 0) {
            victim.setWater(null);
        }
    }

    @Override
    public void die (Entity entity) {

    }
}