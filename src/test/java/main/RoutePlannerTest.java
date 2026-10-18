package main;

import main.navigation.Position;
import main.navigation.Route;
import main.navigation.RoutePlanner;
import main.soils.SwampSoil;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoutePlannerTest {
    private final RoutePlanner planner = new RoutePlanner();

    @Test
    void prefersLongerRouteWhenItCostsLessEnergy() {
        Matrix map = DomainRegressionTest.map(3, 3);
        map.getCell(1, 1).getSoil().setWaterRetention(100);
        Route route = planner.plan(map, new Position(0, 1), new Position(2, 1));
        assertThat(route.energyCost()).isEqualTo(12);
        assertThat(route.path()).containsExactly(new Position(0, 1), new Position(0, 2),
                new Position(1, 2), new Position(2, 2), new Position(2, 1));
    }

    @Test
    void equalCostRoutesUseStableNorthThenEastOrder() {
        Matrix map = DomainRegressionTest.map(2, 2);
        Route route = planner.plan(map, new Position(0, 0), new Position(1, 1));
        assertThat(route.path()).containsExactly(new Position(0, 0), new Position(0, 1),
                new Position(1, 1));
        assertThat(route).isEqualTo(planner.plan(map, new Position(0, 0), new Position(1, 1)));
        assertThatThrownBy(() -> route.path().clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void zeroCostCyclesTerminateAndDoNotIntroduceRepeatedPositions() {
        Matrix map = DomainRegressionTest.map(3, 3);
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                map.placeEntity(new SwampSoil(x, y, 10, false, "Soil", "SwampSoil", 0, 0, 7, 0, 0));
                map.getCell(x, y).getAir().setAirQual(100);
            }
        }
        Route route = planner.plan(map, new Position(0, 0), new Position(2, 2));
        assertThat(route.energyCost()).isZero();
        assertThat(route.path()).doesNotHaveDuplicates().hasSize(5);
    }

    @Test
    void insufficientBatteryLeavesRobotUnchanged() {
        Matrix map = DomainRegressionTest.map(2, 2);
        Robot robot = new Robot(0, 0, 5);
        assertThat(robot.moveTo(map, 1, 1)).isEmpty();
        assertThat(robot.getRobotX()).isZero();
        assertThat(robot.getRobotY()).isZero();
        assertThat(robot.getRobotBattery()).isEqualTo(5);
    }

    @Test
    void exactBatteryAndOriginDestinationAreSupported() {
        Matrix map = DomainRegressionTest.map(2, 2);
        Robot robot = new Robot(0, 0, 6);
        assertThat(robot.moveTo(map, 1, 1)).isPresent();
        assertThat(robot.getRobotBattery()).isZero();
        assertThat(robot.getRobotX()).isEqualTo(1);
        assertThat(robot.getRobotY()).isEqualTo(1);
        Route stationary = robot.moveTo(map, 1, 1).orElseThrow();
        assertThat(stationary.path()).containsExactly(new Position(1, 1));
        assertThat(stationary.energyCost()).isZero();
    }

    @Test
    void costUsesLongWhenRouteExceedsIntegerRange() {
        Matrix map = DomainRegressionTest.map(1, 3);
        map.getCell(1, 0).getSoil().setWaterRetention(1e12);
        map.getCell(2, 0).getSoil().setWaterRetention(1e12);
        assertThat(planner.plan(map, new Position(0, 0), new Position(2, 0)).energyCost())
                .isEqualTo(2L * Integer.MAX_VALUE);
    }

    @Test
    void invalidDestinationIsRejectedBeforeMovement() {
        Robot robot = new Robot(0, 0, 100);
        assertThatThrownBy(() -> robot.moveTo(DomainRegressionTest.map(2, 3), 3, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(robot.getRobotBattery()).isEqualTo(100);
    }

    @Test
    void distancesAgreeWithIndependentBellmanFordOnSeededWorlds() {
        Random random = new Random(42);
        for (int trial = 0; trial < 40; trial++) {
            Matrix map = DomainRegressionTest.map(3, 3);
            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < 3; y++) {
                    map.getCell(x, y).getSoil().setWaterRetention(random.nextInt(100));
                    map.getCell(x, y).getAir().setAirQual(random.nextInt(101));
                }
            }
            for (int destination = 0; destination < 9; destination++) {
                Position end = new Position(destination % 3, destination / 3);
                Route route = planner.plan(map, new Position(0, 0), end);
                assertThat(route.energyCost()).as("world %s, destination %s", trial, end)
                        .isEqualTo(bellmanFord(map, end));
                long actualCost = 0;
                List<Position> path = route.path();
                for (int i = 1; i < path.size(); i++) {
                    Position previous = path.get(i - 1);
                    Position current = path.get(i);
                    assertThat(Math.abs(previous.x() - current.x())
                            + Math.abs(previous.y() - current.y())).isEqualTo(1);
                    actualCost += MovementCost.toEnter(map.getCell(current.x(), current.y()));
                }
                assertThat(actualCost).isEqualTo(route.energyCost());
            }
        }
    }

    private long bellmanFord(Matrix map, Position end) {
        long[] costs = new long[9];
        java.util.Arrays.fill(costs, Long.MAX_VALUE / 2);
        costs[0] = 0;
        for (int pass = 0; pass < 8; pass++) {
            for (int from = 0; from < 9; from++) {
                for (int to = 0; to < 9; to++) {
                    if (Math.abs(from % 3 - to % 3) + Math.abs(from / 3 - to / 3) == 1) {
                        costs[to] = Math.min(costs[to], costs[from]
                                + MovementCost.toEnter(map.getCell(to % 3, to / 3)));
                    }
                }
            }
        }
        return costs[end.y() * 3 + end.x()];
    }
}
