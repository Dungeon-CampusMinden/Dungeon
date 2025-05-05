package systems;

import client.KeyboardConfig;
import com.badlogic.gdx.Gdx;
import contrib.components.PathComponent;
import core.Game;
import core.System;
import core.level.utils.Coordinate;
import java.util.ArrayList;
import java.util.List;
import utils.pathfinding.PathfindingLogic;
import utils.pathfinding.PathfindingVisualizer;
import utils.pathfinding.TileState;

/**
 * This class is responsible for managing the pathfinding logic and visualizing the pathfinding
 * process.
 *
 * <p>It allows for both manual and automatic stepping through the pathfinding process.
 *
 * <p>It checks every frame 2 things: 1. If the pathfinding is finished and the hero is not moving,
 * it will start moving the hero on the final path, and stop the system.2. If the step pathfinding
 * key is pressed, it will visualize the pathfinding process.
 *
 * <p>Use {@link #updatePathfindingAlgorithm(PathfindingLogic)} to start the system and set a new
 * pathfinding algorithm.
 *
 * @see PathfindingLogic
 * @see TileState
 * @see PathfindingVisualizer
 */
public class PathfindingSystem extends System {
  private final PathfindingVisualizer visualizer = new PathfindingVisualizer();
  private List<Coordinate> finalPath = new ArrayList<>();

  private boolean autoStep = false;
  private long stepDelay = 100; // Step delay in milliseconds if using autoStep
  private boolean isHeroMoving = false;

  @Override
  public void execute() {
    if (visualizer.isFinished() && !isHeroMoving && isStartMovingKeyPressed()) {
      Game.hero()
          .ifPresent(
              hero -> {
                isHeroMoving = true;
                hero.add(new PathComponent(finalPath));
              });
      this.stop();
      return;
    }

    if (isStepPathfindingKeyPressed()) visualizer.visualizePathfinding(autoStep, stepDelay);
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
   * <p>This method starts this system, resets the current pathfinding process, and initializes a
   * new one with the provided pathfinding algorithm.
   *
   * @param pathFindingAlgorithm the pathfinding algorithm to use
   */
  public void updatePathfindingAlgorithm(PathfindingLogic pathFindingAlgorithm) {
    this.run();
    reset();
    pathFindingAlgorithm.performSearch(); // Perform the search to populate the path
    this.finalPath = pathFindingAlgorithm.finalPath();
    this.visualizer.updatePath(pathFindingAlgorithm.steps(), pathFindingAlgorithm.finalPath());
  }

  /**
   * Resets the pathfinding system.
   *
   * <p>This method stops the current pathfinding process, clears the scheduled actions, and resets
   * the state of the system.
   */
  private void reset() {
    visualizer.reset();
  }
}
