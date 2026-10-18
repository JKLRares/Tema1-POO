package main;

import lombok.Getter;
import java.util.Optional;
import main.navigation.Position;
import main.navigation.Route;
import main.navigation.RoutePlanner;

@Getter
public final class Robot {
    private int robotX;
    private int robotY;
    private int robotBattery;

    public Robot(final int robotX, final int robotY, final int robotBattery) {
        if (robotX < 0 || robotY < 0 || robotBattery < 0) {
            throw new IllegalArgumentException("Robot position and battery must be nonnegative");
        }
        this.robotX = robotX;
        this.robotY = robotY;
        this.robotBattery = robotBattery;
    }

    /** Changes stored energy, rejecting invalid state instead of allowing negative battery. */
    public void setRobotBattery(final int energy) {
        if (energy < 0) {
            throw new IllegalArgumentException("Battery cannot be negative");
        }
        robotBattery = energy;
    }

    /** Moves to the least costly neighbor; ties follow north, east, south, west order. */
    public boolean moveRobot(final Matrix map) {
        Cell best = null;
        int bestCost = Integer.MAX_VALUE;
        for (Cell neighbor : map.neighbors(robotX, robotY)) {
            int cost = MovementCost.toEnter(neighbor);
            if (best == null || cost < bestCost) {
                best = neighbor;
                bestCost = cost;
            }
        }
        if (best == null || robotBattery < bestCost) {
            return false;
        }
        robotBattery -= bestCost;
        robotX = best.getX();
        robotY = best.getY();
        return true;
    }
    /**
     * Executes an entire route atomically at the current timestamp, or leaves the robot
     * unchanged.
     */
    public Optional<Route> moveTo(final Matrix map, final int targetX, final int targetY) {
        Position destination = new Position(targetX, targetY);
        Route route = new RoutePlanner().plan(map, new Position(robotX, robotY), destination);
        if (route.energyCost() > robotBattery) {
            return Optional.empty();
        }
        robotBattery -= (int) route.energyCost();
        robotX = targetX;
        robotY = targetY;
        return Optional.of(route);
    }

}
