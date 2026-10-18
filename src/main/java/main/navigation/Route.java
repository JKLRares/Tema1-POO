package main.navigation;

import java.util.List;

/** A route through a snapshot of the world, including its origin and destination. */
public record Route(List<Position> path, long energyCost) {
    /** Copies the path to prevent later changes to a planned route. */
    public Route {
        path = List.copyOf(path);
        if (path.isEmpty() || energyCost < 0) {
            throw new IllegalArgumentException("A route needs a path and a nonnegative cost");
        }
    }
}
