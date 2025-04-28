package utils.pathfinding;

import contrib.systems.EventScheduler;
import core.Game;
import core.level.Tile;
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
   * Visualizes the pathfinding process.
   *
   * <p>This method only steps one iteration at a time. It is intended for manual stepping through
   * the pathfinding process.
   *
   * <p>For automatic stepping, use {@link #visualizePathfinding(boolean, long)}.
   *
   * @see EventScheduler
   * @see #visualizePathfinding(boolean, long)
   */
  public void visualizePathfinding() {
    visualizePathfinding(false, DEFAULT_STEP_DELAY);
  }

  /**
   * Visualizes the pathfinding process.
   *
   * <p>If in manual mode, shows the next step. If in auto mode, schedule all remaining steps with
   * appropriate delays.
   *
   * @param autoStep If true, the pathfinding will automatically step through each iteration,
   *     otherwise it will step manually
   * @param stepDelay Delay in milliseconds for each step in auto mode, if not using autoStep it
   *     will be ignored
   * @see EventScheduler
   * @see #visualizePathfinding()
   */
  public void visualizePathfinding(boolean autoStep, long stepDelay) {
    stepDelay = autoStep ? stepDelay : 0; // No delay in manual mode
    if (autoStep && isRunning) {
      return;
    }

    List<Tuple<Node, TileState>> steps = pathfindingLogic.steps();
    for (int i = stepCount; i < steps.size(); i++) {
      Tuple<Node, TileState> step = steps.get(i);

      // Schedule coloring action for the current step
      long delay = stepDelay * i;
      scheduledActions.add(
          EventScheduler.scheduleAction(() -> PathfindingVisualizer.colorTile(step), delay));

      // On the last step, visualize the final path
      if (i == steps.size() - 1) {
        isRunning = autoStep; // Prevent autoStep from running again
        displayFinalPath(delay + 1); // ensure final path is shown after all steps
      }

      stepCount++;
      // In manual mode, process only one step at a time
      if (!autoStep) {
        break;
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
  }

  /**
   * Schedules visualization of the final path found by the algorithm.
   *
   * @param delay Delay in milliseconds for the final path visualization
   */
  private void displayFinalPath(long delay) {
    scheduledActions.add(
        EventScheduler.scheduleAction(
            () ->
                pathfindingLogic
                    .finalPath()
                    .forEach(node -> colorTile(Tuple.of(node, TileState.PATH))),
            delay));
  }

  /**
   * Colors a tile based on its pathfinding state.
   *
   * @param blockToColor A tuple containing the node and its tile state
   */
  private static void colorTile(Tuple<Node, TileState> blockToColor) {
    Tile tile = Game.tileAT(blockToColor.a().coordinate());
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
