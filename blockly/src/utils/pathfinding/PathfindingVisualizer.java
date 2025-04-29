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
  private static final long DEFAULT_STEP_DELAY = 100; // Delay in milliseconds

  private final List<EventScheduler.ScheduledAction> scheduledActions = new ArrayList<>();
  private final PathfindingLogic pathfindingLogic;

  private boolean isRunning = false;
  private boolean isFinished = false;
  private int stepCount = 0;

  /**
   * Creates a visualizer with default settings.
   *
   * @param pathfindingLogic The pathfinding algorithm to visualize
   */
  public PathfindingVisualizer(PathfindingLogic pathfindingLogic) {
    this.pathfindingLogic = pathfindingLogic;
  }

  /**
   * Visualizes a single step of the pathfinding process manually.
   *
   * <p>This method only steps one iteration at a time. It is intended for manual stepping through
   * the pathfinding process.
   *
   * @see #runAutomatically(long)
   */
  public void visualizePathfinding() {
    stepManually();
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
    if (isFinished) {
      return;
    }

    List<Tuple<Coordinate, TileState>> steps = pathfindingLogic.steps();
    if (stepCount >= steps.size()) {
      return;
    }

    // Process just one step
    Tuple<Coordinate, TileState> step = steps.get(stepCount);
    PathfindingVisualizer.colorTile(step);
    stepCount++;

    // If this was the last step, show the final path
    if (stepCount == steps.size()) {
      displayFinalPath(0);
    }
  }

  /**
   * Runs the pathfinding visualization automatically with the specified delay.
   *
   * @param stepDelay Delay in milliseconds between steps
   */
  private void runAutomatically(long stepDelay) {
    if (isRunning || isFinished) {
      return;
    }

    isRunning = true;
    List<Tuple<Coordinate, TileState>> steps = pathfindingLogic.steps();

    // Schedule all remaining steps
    for (int i = 0; i < steps.size(); i++) {
      Tuple<Coordinate, TileState> step = steps.get(i);
      long delay = stepDelay * i;
      scheduledActions.add(
          EventScheduler.scheduleAction(() -> PathfindingVisualizer.colorTile(step), delay));

      if (i == steps.size() - 1) {
        displayFinalPath(delay + 1);
      }
    }
  }

  /**
   * Resets the pathfinding visualization.
   *
   * <p>Clears all scheduled actions and resets the visualization state.
   */
  public void reset() {
    clearScheduledActions();
    stepCount = 0;
    isRunning = false;
    isFinished = false;
  }

  /**
   * Schedules visualization of the final path found by the algorithm.
   *
   * @param delay Delay in milliseconds for the final path visualization
   */
  private void displayFinalPath(long delay) {
    scheduledActions.add(
        EventScheduler.scheduleAction(
            () -> {
              pathfindingLogic
                  .finalPath()
                  .forEach(node -> colorTile(Tuple.of(node, TileState.PATH)));
              isFinished = true;
            },
            delay));
  }

  /**
   * Returns whether the pathfinding process is finished.
   *
   * @return true if the pathfinding process is finished, false otherwise
   */
  public boolean isFinished() {
    return isFinished;
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
