package main;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public final class Robot {
    private static final int INITIAL_BEST_SCORE = 10000;
    private static final int DIRECTION_COUNT = 4;

    private int robotX, robotY, robotBattery;

    public Robot(final int robotX, final int robotY, final int robotBattery) {
        this.robotX = 0;
        this.robotY = 0;
        this.robotBattery = robotBattery;
    }

    private Cell bestNeigh(final int x, final int y, final Matrix map) {
        int[] dx = {0, 1, 0, -1};
        int[] dy = {1, 0, -1, 0};
        int cellScore;
        int bestX = 0;
        int bestY = 0;
        int bestScore = INITIAL_BEST_SCORE;

        for (int i = 0; i < DIRECTION_COUNT; i++) {
            int tryX = x + dx[i];
            int tryY = y + dy[i];
            if (tryX >= 0 && tryX < map.getWidth() && tryY >= 0 && tryY < map.getHeight()) {
                Cell checkNeigh = map.getCell(tryX, tryY);
                int nr = 2;
                double score = Math.max(checkNeigh.getAir().airToxicity(), 0)
                        + checkNeigh.getSoil().probFormula();

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

    private int calculateCost(final Cell cell) {
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

    /**
     * Attempts to move the robot to the best neighboring cell based on calculated scores.
     * Consumes battery if movement is successful.
     *
     * @param map the matrix representing the world map
     * @return true if the robot moved successfully, false if battery was insufficient
     */
    public boolean moveRobot(final Matrix map) {
        Cell bestCell = bestNeigh(robotX, robotY, map);
        int cost = calculateCost(bestCell);
        if (robotBattery < cost) {
            return false;
        } else {
            robotBattery -= cost;
            robotX = bestCell.getX();
            robotY = bestCell.getY();
        }
        return true;
    }
}
