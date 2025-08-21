package aiAdvanced.pathfinding;

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
  private List<Tuple<Coordinate, TileState>> path;
  private List<Coordinate> finalPath;

  private int stepCount = 0;

  /**
   * Constructor for PathfindingVisualizer.
   *
   * <p>Initializes the visualizer with the path steps and the final path.
   *
   * @param pathSteps A list of tuples containing coordinates and their corresponding tile states.
   * @param finalPath The final path as a list of coordinates.
   * @see PathfindingLogic
   */
  public PathfindingVisualizer(
      List<Tuple<Coordinate, TileState>> pathSteps, List<Coordinate> finalPath) {
    this.path = pathSteps;
    this.finalPath = finalPath;
  }

  /**
   * Default constructor for PathfindingVisualizer.
   *
   * <p>Initializes the visualizer with empty path steps and final path. Use {@link #updatePath} to
   * later set the path and final path.
   *
   * @see PathfindingLogic
   */
  public PathfindingVisualizer() {
    this.path = new ArrayList<>();
    this.finalPath = new ArrayList<>();
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
    stepCount = 1; // mark as running
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
            () ->
                finalPath.forEach(
                    coordinate -> {
                      colorTile(Tuple.of(coordinate, TileState.PATH));
                      stepCount = path.size(); // mark as finished
                    }),
            stepDelay * path.size()));
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
   * <p>If the Algo {@link #isFinished()} is true, it means the pathfinding process is not running.
   *
   * @return true if the pathfinding process is running, false otherwise
   */
  public boolean isRunning() {
    return 0 < stepCount && stepCount < path.size();
  }

  /**
   * Updates the pathfinding visualization with a new path and final path.
   *
   * <p>This method resets the current visualization and sets the new path and final path.
   *
   * @param newPath A list of tuples containing coordinates and their corresponding tile states.
   * @param finalPath The final path as a list of coordinates.
   */
  public void updatePath(List<Tuple<Coordinate, TileState>> newPath, List<Coordinate> finalPath) {
    reset();
    this.path = newPath;
    this.finalPath = finalPath;
  }

  /**
   * Colors a tile based on its pathfinding state.
   *
   * @param blockToColor A tuple containing the node and its tile state
   */
  private static void colorTile(Tuple<Coordinate, TileState> blockToColor) {
    Tile tile = Game.tileAt(blockToColor.a()).orElse(null);
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
