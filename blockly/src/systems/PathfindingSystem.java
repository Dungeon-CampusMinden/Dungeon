package systems;

import client.KeyboardConfig;
import com.badlogic.gdx.Gdx;
import contrib.systems.EventScheduler;
import core.System;
import core.level.utils.Coordinate;
import core.utils.Tuple;
import java.util.ArrayList;
import java.util.List;
import utils.pathfinding.Node;
import utils.pathfinding.PathfindingLogic;
import utils.pathfinding.PathfindingVisualizer;
import utils.pathfinding.TileState;

/**
 * This class is responsible for managing the pathfinding logic and visualizing the pathfinding
 * process.
 *
 * <p>It allows for both manual and automatic stepping through the pathfinding process.
 *
 * @see PathfindingLogic
 * @see TileState
 * @see PathfindingVisualizer
 */
public class PathfindingSystem extends System {
  private static final long STEP_DELAY = 100; // Delay in milliseconds

  private PathfindingLogic studentAlgorithm;
  private Coordinate startNode;
  private Coordinate endNode;

  private boolean autoStep = false;
  private boolean isRunning = false;
  private int stepCount = 0;
  private final List<EventScheduler.ScheduledAction> scheduledActions = new ArrayList<>();

  @Override
  public void execute() {
    if (studentAlgorithm == null) return;

    if (!Gdx.input.isKeyJustPressed(KeyboardConfig.STEP_PATHFINDING.value())) return;
    if (isRunning && autoStep) return;

    studentAlgorithm.performSearch(startNode, endNode);

    for (int i = stepCount; i < studentAlgorithm.steps().size(); i++) {
      Tuple<Node, TileState> step = studentAlgorithm.steps().get(i);
      scheduledActions.add(
          EventScheduler.scheduleAction(
              () -> PathfindingVisualizer.colorTile(step), autoStep ? STEP_DELAY * i : 0));

      // on the last step, color the final path
      if (i == studentAlgorithm.steps().size() - 1) {
        isRunning = autoStep; // prevent autoStep from running again
        displayFinalPath(i);
      }

      stepCount++;
      if (!autoStep) {
        break;
      }
    }
  }

  private void displayFinalPath(int delayStep) {
    scheduledActions.add(
        EventScheduler.scheduleAction(
            () ->
                studentAlgorithm
                    .finalPath(endNode)
                    .forEach(
                        node -> PathfindingVisualizer.colorTile(Tuple.of(node, TileState.PATH))),
            autoStep ? STEP_DELAY * delayStep : 0));
  }

  /**
   * Change the autoStep state.
   *
   * <p>If true, the pathfinding will automatically step through each iteration.
   *
   * @param autoStep true to enable auto stepping, false to disable
   * @see #STEP_DELAY
   */
  public void autoStep(boolean autoStep) {
    this.autoStep = autoStep;
  }

  /**
   * Update the pathfinding algorithm with new start and end nodes.
   *
   * @param pathFindingAlgorithm the pathfinding algorithm to use
   * @param startCoordinate the starting coordinate
   * @param endCoordinate the ending coordinate
   */
  public void updatePathfindingAlgorithm(
      PathfindingLogic pathFindingAlgorithm, Coordinate startCoordinate, Coordinate endCoordinate) {
    this.studentAlgorithm = pathFindingAlgorithm;
    this.startNode = startCoordinate;
    this.endNode = endCoordinate;

    reset();
  }

  /**
   * Resets the pathfinding system.
   *
   * <p>This method stops the current pathfinding process, clears the scheduled actions, and resets
   * the state of the system.
   */
  private void reset() {
    scheduledActions.forEach(EventScheduler::cancelAction);
    scheduledActions.clear();
    stepCount = 0;
    isRunning = false;
  }
}
