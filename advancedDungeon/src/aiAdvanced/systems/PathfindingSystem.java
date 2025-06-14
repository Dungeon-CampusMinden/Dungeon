package aiAdvanced.systems;

import aiAdvanced.pathfinding.PathfindingLogic;
import aiAdvanced.pathfinding.PathfindingVisualizer;
import aiAdvanced.pathfinding.TileState;
import aiAdvanced.starter.KeyboardConfig;
import com.badlogic.gdx.Gdx;
import contrib.components.PathComponent;
import core.Entity;
import core.System;
import core.level.utils.Coordinate;
import core.utils.Tuple;
import java.util.ArrayList;
import java.util.List;

/**
 * This system is responsible for visualizing multiple pathfinding logics.
 *
 * <p>It allows for both manual and automatic stepping through the pathfinding process.
 *
 * <p>It checks every frame 2 things: 1. If all pathfindings are finished and the runners are not
 * moving, it will start moving them on each final path, and stop the system.2. If the step
 * pathfinding key is pressed, it will visualize the pathfinding process.
 *
 * <p>Use {@link #updatePathfindingAlgorithm(Tuple[])} to start the system and set new pathfinding
 * algorithms.
 *
 * @see PathfindingLogic
 * @see TileState
 * @see PathfindingVisualizer
 */
public class PathfindingSystem extends System {
  private final List<PathfindingData> pathfindingAlgorithms = new ArrayList<>();

  private boolean autoStep = false;
  private long stepDelay = 100; // Step delay in milliseconds if using autoStep
  private int runningRunners = 0;

  @Override
  public void execute() {
    for (PathfindingData pathfindingData : pathfindingAlgorithms) {
      PathfindingVisualizer visualizer = pathfindingData.visualizer;
      Entity runner = pathfindingData.runner;

      if (runner.isPresent(PathComponent.class)) {
        continue;
      }

      if (isEveryAlgorithmFinished() && !isEveryRunnerRunning() && isStartMovingKeyPressed()) {
        runner.add(new PathComponent(pathfindingData.finalPath));
        runningRunners++;

        if (isEveryRunnerRunning()) this.stop();
        continue;
      }

      if (isStepPathfindingKeyPressed()) visualizer.visualizePathfinding(autoStep, stepDelay);
    }
  }

  private boolean isEveryRunnerRunning() {
    return runningRunners > 0 && runningRunners == pathfindingAlgorithms.size();
  }

  /**
   * Checks if every pathfinding algorithm is running.
   *
   * <p>A pathfinding algorithm is considered running if its visualizer is not finished.
   *
   * <p>If no pathfinding algorithms are present, it returns false.
   *
   * @return true if every pathfinding algorithm is running, false otherwise
   */
  public boolean isEveryAlgorithmRunning() {
    for (PathfindingData pathfindingData : pathfindingAlgorithms) {
      if (!pathfindingData.visualizer.isRunning()) {
        return false;
      }
    }
    return !pathfindingAlgorithms.isEmpty();
  }

  /**
   * Checks if every pathfinding algorithm is finished.
   *
   * <p>A pathfinding algorithm is considered finished if its visualizer is finished.
   *
   * <p>If no pathfinding algorithms are present, it returns false.
   *
   * @return true if every pathfinding algorithm is finished, false otherwise
   */
  public boolean isEveryAlgorithmFinished() {
    for (PathfindingData pathfindingData : pathfindingAlgorithms) {
      if (!pathfindingData.visualizer.isFinished()) {
        return false;
      }
    }
    return !pathfindingAlgorithms.isEmpty();
  }

  /**
   * Checks if the key for starting pathfinding movement is pressed.
   *
   * @return true if the key is just pressed, false otherwise
   */
  private boolean isStartMovingKeyPressed() {
    return Gdx.input.isKeyJustPressed(KeyboardConfig.START_MOVING_PATHFINDING.value());
  }

  /**
   * Checks if the key for stepping through pathfinding is pressed.
   *
   * @return true if the key is just pressed, false otherwise
   */
  private boolean isStepPathfindingKeyPressed() {
    return Gdx.input.isKeyJustPressed(KeyboardConfig.STEP_PATHFINDING.value());
  }

  /**
   * Change the autoStep state.
   *
   * <p>If true, the pathfinding will automatically step through each iteration.
   *
   * @param autoStep true to enable auto stepping, false to disable
   */
  public void autoStep(boolean autoStep) {
    this.autoStep = autoStep;
  }

  /**
   * Set the step delay.
   *
   * <p>This is the delay in milliseconds for each step in auto mode.
   *
   * @param stepDelay the delay in milliseconds. Must be non-negative.
   * @throws IllegalArgumentException if the step delay is negative
   */
  public void stepDelay(long stepDelay) {
    if (stepDelay < 0) {
      throw new IllegalArgumentException("Step delay cannot be negative");
    }

    this.stepDelay = stepDelay;
  }

  /**
   * Sets a new pathfinding algorithm and starts the pathfinding process.
   *
   * <p>This method starts this system, resets the current pathfinding processes, and initializes
   * new ones with the provided pathfinding algorithms.
   *
   * @param pathFindingAlgorithms the pathfinding algorithms to use
   */
  @SafeVarargs
  public final void updatePathfindingAlgorithm(
      Tuple<PathfindingLogic, Entity>... pathFindingAlgorithms) {
    this.run();
    reset();
    for (Tuple<PathfindingLogic, Entity> pathFindingAlgorithm : pathFindingAlgorithms) {
      pathFindingAlgorithm.a().performSearch(); // Perform the search to populate the path
      PathfindingData algo =
          new PathfindingData(
              new PathfindingVisualizer(
                  pathFindingAlgorithm.a().steps(), pathFindingAlgorithm.a().finalPath()),
              pathFindingAlgorithm.a().finalPath(),
              pathFindingAlgorithm.b());
      this.pathfindingAlgorithms.add(algo);
    }
  }

  /**
   * Resets the pathfinding system.
   *
   * <p>This method stops the current pathfinding process, clears the scheduled actions, and resets
   * the state of the system.
   */
  public void reset() {
    for (PathfindingData pathfindingData : pathfindingAlgorithms) {
      pathfindingData.visualizer.reset();
    }
    pathfindingAlgorithms.clear();
    runningRunners = 0;
  }

  private record PathfindingData(
      PathfindingVisualizer visualizer, List<Coordinate> finalPath, Entity runner) {}
}
