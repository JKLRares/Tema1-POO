package main;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Robot {
    private int robotX, robotY, robotBattery;

    public Robot(int robotX, int robotY, int robotBattery) {
        this.robotX = 0;
        this.robotY = 0;
        this.robotBattery = robotBattery;
    }

    private Cell bestNeigh(int x, int y, Matrix map) {
        int[] dx = {0, 1, 0, -1};
        int[] dy = {1, 0, -1, 0};
        int cellScore;
        int bestX = 0;
        int bestY = 0;
        int bestScore = 10000;

        for(int i = 0; i < 4; i++) {
            int tryX = x + dx[i];
            int tryY = y + dy[i];
            if(tryX >= 0 && tryX < map.getWidth() && tryY >= 0 && tryY < map.getHeight()) {
                Cell checkNeigh = map.getCell(tryX, tryY);
                int nr = 2;
                double score = Math.max(checkNeigh.getAir().airToxicity(), 0) + checkNeigh.getSoil().probFormula();
                if (checkNeigh.getAnimal() != null) {
                    score += checkNeigh.getAnimal().getAttackProb();
                    nr++;
                }
                if (checkNeigh.getPlant() != null) {
                    score += checkNeigh.getPlant().getStuckProb();
                    nr++;
                }
                double mean = Math.abs(score / nr);
                cellScore = (int) Math.round(mean);
                if (cellScore < bestScore) {
                    bestScore = cellScore;
                    bestX = tryX;
                    bestY = tryY;
                }
            }
        }
        return map.getCell(bestX, bestY);
    }

    private int calculateCost(Cell cell) {
        int nr = 2;
        double score = Math.max(cell.getAir().airToxicity(), 0) + cell.getSoil().probFormula();
        if (cell.getAnimal() != null) {
            score += cell.getAnimal().getAttackProb();
            nr++;
        }
        if (cell.getPlant() != null) {
            score += cell.getPlant().getStuckProb();
            nr++;
        }
        return (int) Math.round(Math.abs(score / nr));
    }

    public boolean moveRobot(Matrix map) {
        Cell bestCell = bestNeigh(robotX, robotY, map);
        int cost = calculateCost(bestCell);
        if(robotBattery < cost) {
            return false;
        } else {
            robotBattery -= cost;
            robotX = bestCell.getX();
            robotY = bestCell.getY();
        }
        return true;
    }
}
