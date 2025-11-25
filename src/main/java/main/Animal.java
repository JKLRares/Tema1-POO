package main;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public abstract class Animal extends Alive {
    private boolean fed = false;
    private String animalState = "hungry";
    private double airQual, waterQual;

    //cred ca trebuie sa dau ca input entitatile air si water de pe cellul respectiv

    public Animal(int x, int y, double mass, boolean scanned, String name, String type, double airQual,
                  double waterQual) {
        super(x, y, mass, scanned, name, type);
        this.airQual = airQual;
        this.waterQual = waterQual;
    }

    public void tryToAct(Entity[][] map) {
        if(isScanned()) {
            move(map);
        }
    }
    // parsare proasta de date in move, trebuie sa fac ca la eat pentru ca pot sa am si apa si planta si aici nu verific bine
    public void move(Entity[][] map) {
        int[] vx = {-1, 0, 1, 0}; // DE SCHIMBAT
        int[] vy = {0, 1, 0, -1};

        Entity best = null;
        double bestScore = -1.0;

        for(int i = 0; i < 4; i++) {
            int nx = getX() + vx[i];
            int ny = getY() + vy[i];
            if(nx >= 0 && nx < map.length && ny >= 0 && ny < map[0].length) {
                Entity neighbor = map[nx][ny];
                double score = calculateScore(neighbor);
                if(score > bestScore) {
                    bestScore = score;
                    best = neighbor;
                }
            }
        }
        if(best != null)
            System.out.println("fa schimbarea pe noile celluri in animal.java");
    }

    private double calculateScore(Entity e) {
        boolean hasPlant = e.getEntityType() == EntityType.PLANT;
        boolean hasWater = e.getEntityType() == EntityType.WATER;

        if(hasPlant && hasWater) {
            return 1000.0 + waterQual;
        } else if (hasPlant) {
            return 500.0;
        } else if (hasWater) {
            return 200.0 + waterQual;
        }
        return 0.0;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ANIMAL;
    }

    public abstract int getAttackProb();

    public void interact(Entity potentialVictim, Entity plantOnCell, Entity waterOnCell) {
        if(potentialVictim != null && potentialVictim.getEntityType() == EntityType.ANIMAL) {
            if(getType().equalsIgnoreCase("CARNIVORE") || getType().equalsIgnoreCase("PARASITES")) {
                eatAnimal(potentialVictim);
                fed = true;
            }
        }
        if(!fed) {
            if(plantOnCell != null && plantOnCell.getEntityType() == EntityType.PLANT) {
                if(plantOnCell.isScanned()) {
                    eatPlant(plantOnCell);
                    fed = true;
                }
            }
            if(waterOnCell != null && waterOnCell.getEntityType() == EntityType.WATER) {
                if(waterOnCell.isScanned()) {
                    drink(waterOnCell);
                    fed = true;
                }
            }
        }
        if(fed) {
            animalState = "well-fed";
        }

//        if(airQual == "toxic") {
//            animalState = "sick";
//        }
    }

    private void eatAnimal(Entity victim) {
//        System.out.println(name + " ate " + victim);
//        setMass(victim.getMass());
//        die();
//        //interactiunea animal soil
    }

    private void eatPlant(Entity plant) {
//        System.out.println(name + " ate plant");
        //trebuie citirea intai
    }

    private void drink(Entity water) {
//        System.out.println(name + " drank water");
        //trebuie citirea intai
    }

    @Override
    public void die () {

    }
}