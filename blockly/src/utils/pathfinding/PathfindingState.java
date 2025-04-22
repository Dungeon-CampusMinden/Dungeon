package utils.pathfinding;

import core.level.utils.Coordinate;
import java.util.List;
import java.util.Set;

/**
 * Represents the state of a pathfinding process.
 *
 * @param openSet The set of coordinates that are yet to be processed.
 * @param closedSet The set of coordinates that have already been processed.
 * @param finalPath The list of coordinates representing the final path from start to end.
 * @param lastProcessedNode The last coordinate that was processed during the pathfinding process.
 * @param isFinished Indicates whether the pathfinding process is complete.
 */
public record PathfindingState(
    Set<Coordinate> openSet,
    Set<Coordinate> closedSet,
    List<Coordinate> finalPath,
    Coordinate lastProcessedNode,
    boolean isFinished) {}
