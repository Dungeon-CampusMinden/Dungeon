package utils.pathfinding;

import contrib.systems.EventScheduler;
import core.Game;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.utils.Tuple;
import java.util.ArrayList;
import java.util.List;

/**
 * Visualizes pathfinding algorithms by coloring tiles based on their search state.
 *
 * <p>This class manages the visual representation of pathfinding steps, allowing either
 * step-by-step manual visualization or automatic execution with configurable delays.
 *
 * @see PathfindingLogic
 * @see TileState
 */
public class PathfindingVisualizer {
  private final List<EventScheduler.ScheduledAction> scheduledActions = new ArrayList<>();
  private final List<Tuple<Coordinate, TileState>> path;
  private final List<Coordinate> finalPath;

  private int stepCount = 0;

  /**
   * Creates a visualizer with default settings.
   *
   * @param pathfindingLogic The pathfinding logic to visualize
   */
  public PathfindingVisualizer(PathfindingLogic pathfindingLogic) {
    pathfindingLogic.performSearch(); // Perform the search to populate the path

    this.path = pathfindingLogic.steps();
    this.finalPath = pathfindingLogic.finalPath();
  }

  /**
   * Returns the final path.
   *
   * <p>This is the path found by the pathfinding algorithm.
   *
   * @return The final path as a list of coordinates
   */
  public List<Coordinate> finalPath() {
    return finalPath;
  }

  /**
   * Visualizes the pathfinding process either manually or automatically.
   *
   * @param autoStep If true, the pathfinding will automatically step through each iteration,
   *     otherwise it will step manually
   * @param stepDelay Delay in milliseconds for each step in auto mode, ignored in manual mode
   * @see #stepManually()
   * @see #runAutomatically(long)
   */
  public void visualizePathfinding(boolean autoStep, long stepDelay) {
    if (autoStep) {
      runAutomatically(stepDelay);
    } else {
      stepManually();
    }
  }

  /**
   * Takes a single step in the pathfinding visualization. Used for manual stepping through the
   * algorithm.
   */
  private void stepManually() {
    if (isFinished()) {
      return;
    }

    if (stepCount >= path.size()) {
      return;
    }

    // Process just one step
    Tuple<Coordinate, TileState> step = path.get(stepCount);
    PathfindingVisualizer.colorTile(step);
    stepCount++;

    // If this was the last step, show the final path
    if (isFinished())
      finalPath.forEach(coordinate -> colorTile(Tuple.of(coordinate, TileState.PATH)));
  }

  /**
   * Runs the pathfinding visualization automatically with the specified delay.
   *
   * @param stepDelay Delay in milliseconds between steps
   */
  private void runAutomatically(long stepDelay) {
    if (isRunning() || isFinished()) {
      return;
    }

    // Schedule all remaining steps
    for (int i = 0; i < path.size(); i++) {
      Tuple<Coordinate, TileState> step = path.get(i);
      long delay = stepDelay * i;
      scheduledActions.add(
          EventScheduler.scheduleAction(() -> PathfindingVisualizer.colorTile(step), delay));
    }
    // final path
    scheduledActions.add(
        EventScheduler.scheduleAction(
            () -> finalPath.forEach(coordinate -> colorTile(Tuple.of(coordinate, TileState.PATH))),
            stepDelay * path.size()));

    stepCount = path.size(); // mark as finished
  }

  /**
   * Resets the pathfinding visualization.
   *
   * <p>Clears all scheduled actions and resets the visualization state.
   */
  public void reset() {
    clearScheduledActions();
    stepCount = 0;
  }

  /**
   * Returns whether the pathfinding process is finished.
   *
   * @return true if the pathfinding process is finished, false otherwise
   */
  public boolean isFinished() {
    return stepCount == path.size();
  }

  /**
   * Returns whether the pathfinding process is currently running.
   *
   * @return true if the pathfinding process is running, false otherwise
   */
  public boolean isRunning() {
    return 0 < stepCount && stepCount < path.size();
  }

  /**
   * Colors a tile based on its pathfinding state.
   *
   * @param blockToColor A tuple containing the node and its tile state
   */
  private static void colorTile(Tuple<Coordinate, TileState> blockToColor) {
    Tile tile = Game.tileAT(blockToColor.a());
    if (tile == null) {
      return;
    }

    tile.tintColor(blockToColor.b().color());
  }

  /** Cancels all scheduled actions and clears the action list. */
  private void clearScheduledActions() {
    for (EventScheduler.ScheduledAction scheduledAction : scheduledActions) {
      EventScheduler.cancelAction(scheduledAction);
    }
    scheduledActions.clear();
  }
}
