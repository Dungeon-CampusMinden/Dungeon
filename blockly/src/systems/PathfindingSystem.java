package systems;

import client.KeyboardConfig;
import com.badlogic.gdx.Gdx;
import contrib.systems.EventScheduler;
import core.System;
import core.level.utils.Coordinate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import utils.pathfinding.IPathfindingLogic;
import utils.pathfinding.PathfindingState;
import utils.pathfinding.PathfindingVisualizer;

/**
 * This class is responsible for managing the pathfinding process using a specified algorithm. It
 * initializes the algorithm with start and end coordinates, performs steps in the pathfinding
 * process, and visualizes the current state of the pathfinding.
 *
 * <p>It allows for both manual and automatic stepping through the pathfinding process.
 *
 * @see IPathfindingLogic
 * @see PathfindingState
 * @see PathfindingVisualizer
 */
public class PathfindingSystem extends System {
  private static final long STEP_DELAY = 100; // Delay in milliseconds

  private IPathfindingLogic studentAlgorithm;
  private Coordinate startNode;
  private Coordinate endNode;

  private boolean isRunning = false;
  private boolean isFinished = false;
  private boolean autoStep = false;
  private final List<EventScheduler.ScheduledAction> scheduledActions = new ArrayList<>();

  private void startPathfinding() {
    if (isRunning || isFinished) {
      return;
    }

    studentAlgorithm.initialize(startNode, endNode);
    isRunning = true;
    isFinished = false;
  }

  @Override
  public void execute() {
    if (studentAlgorithm == null) return;

    if (!Gdx.input.isKeyJustPressed(KeyboardConfig.STEP_PATHFINDING.value())) return;
    if (isRunning && autoStep) return; // no manual stepping if autoStep is enabled

    startPathfinding();

    scheduledActions.add(
        EventScheduler.scheduleAction(this::stepVisualization, autoStep ? 0 : STEP_DELAY));
  }

  private void stepVisualization() {
    studentAlgorithm.performStep();

    isFinished = studentAlgorithm.isSearchFinished();

    Set<Coordinate> openSet = studentAlgorithm.openSetCoordinates();
    Set<Coordinate> closedSet = studentAlgorithm.closedSetCoordinates();
    Coordinate lastProcessed = studentAlgorithm.lastProcessedNode();
    List<Coordinate> finalPath = List.of(); // Default empty

    if (isFinished) {
      List<Coordinate> resultPath = studentAlgorithm.finalPath();
      if (resultPath != null) {
        finalPath = resultPath;
      }
    }

    PathfindingState currentState =
        new PathfindingState(openSet, closedSet, finalPath, lastProcessed, isFinished);

    PathfindingVisualizer.drawVisualization(currentState);

    if (isFinished) {
      isRunning = false;
    }

    // If autoStep is true, schedule the next step
    if (autoStep && !isFinished) {
      scheduledActions.add(EventScheduler.scheduleAction(this::stepVisualization, STEP_DELAY));
    }
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
      IPathfindingLogic pathFindingAlgorithm,
      Coordinate startCoordinate,
      Coordinate endCoordinate) {
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
    isRunning = false;
    isFinished = false;
    scheduledActions.forEach(EventScheduler::cancelAction);
    scheduledActions.clear();
  }
}
