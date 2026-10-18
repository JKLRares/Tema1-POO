package main.navigation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import main.Cell;
import main.Matrix;
import main.MovementCost;

/** Finds minimum-energy routes using Dijkstra's algorithm over the current world state. */
public final class RoutePlanner {
    private record Candidate(Position position, long cost, long order) { }

    /**
     * Plans without changing the world or robot. Entering a cell uses the same cost as moveRobot.
     * Equal-cost candidates are visited in discovery order; neighbors use N, E, S, W order.
     */
    public Route plan(final Matrix map, final Position origin, final Position destination) {
        map.getCell(origin.x(), origin.y());
        map.getCell(destination.x(), destination.y());
        long[][] distance = new long[map.getHeight()][map.getWidth()];
        Position[][] previous = new Position[map.getHeight()][map.getWidth()];
        for (long[] row : distance) {
            Arrays.fill(row, Long.MAX_VALUE);
        }
        PriorityQueue<Candidate> frontier = new PriorityQueue<>(Comparator
                .comparingLong(Candidate::cost).thenComparingLong(Candidate::order));
        long order = 0;
        distance[origin.y()][origin.x()] = 0;
        frontier.add(new Candidate(origin, 0, order++));
        while (!frontier.isEmpty()) {
            Candidate current = frontier.remove();
            Position position = current.position();
            if (current.cost() != distance[position.y()][position.x()]) {
                continue;
            }
            if (position.equals(destination)) {
                return reconstruct(previous, origin, destination, current.cost());
            }
            for (Cell neighbor : map.neighbors(position.x(), position.y())) {
                long candidate = current.cost() + MovementCost.toEnter(neighbor);
                int x = neighbor.getX();
                int y = neighbor.getY();
                if (candidate < distance[y][x]) {
                    distance[y][x] = candidate;
                    previous[y][x] = position;
                    frontier.add(new Candidate(new Position(x, y), candidate, order++));
                }
            }
        }
        throw new IllegalStateException(
                "A rectangular world must have a route between valid cells");
    }

    private Route reconstruct(final Position[][] previous, final Position origin,
                              final Position destination, final long cost) {
        List<Position> path = new ArrayList<>();
        Position current = destination;
        while (!current.equals(origin)) {
            path.add(current);
            current = previous[current.y()][current.x()];
        }
        path.add(origin);
        Collections.reverse(path);
        return new Route(path, cost);
    }
}
