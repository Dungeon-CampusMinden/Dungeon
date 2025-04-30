package systems;

import client.KeyboardConfig;
import com.badlogic.gdx.Gdx;
import contrib.components.PathComponent;
import core.Game;
import core.System;
import core.level.utils.Coordinate;
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
 * @see PathfindingLogic
 * @see TileState
 * @see PathfindingVisualizer
 */
public class PathfindingSystem extends System {
  private PathfindingVisualizer visualizer = null;

  private boolean autoStep = false;
  private long stepDelay = 100; // Step delay in milliseconds if using autoStep
  private boolean isHeroMoving = false;
  private List<Coordinate> finalPath = List.of();

  @Override
  public void execute() {
    if (visualizer == null) return;

    if (visualizer.isFinished() && !isHeroMoving) {
      if (!Gdx.input.isKeyJustPressed(KeyboardConfig.START_MOVING_PATHFINDING.value())) return;
      Game.hero()
          .ifPresent(
              hero -> {
                isHeroMoving = true;
                hero.add(new PathComponent(finalPath));
              });
      return;
    }

    if (!Gdx.input.isKeyJustPressed(KeyboardConfig.STEP_PATHFINDING.value())) return;

    visualizer.visualizePathfinding(autoStep, stepDelay);
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
   * Update the pathfinding algorithm.
   *
   * <p>This method resets the current pathfinding process and initializes a new one with the
   * provided pathfinding algorithm.
   *
   * @param pathFindingAlgorithm the pathfinding algorithm to use
   */
  public void updatePathfindingAlgorithm(PathfindingLogic pathFindingAlgorithm) {
    reset();
    pathFindingAlgorithm.performSearch();
    this.visualizer = new PathfindingVisualizer(pathFindingAlgorithm);
    this.finalPath = pathFindingAlgorithm.finalPath();
  }

  /**
   * Resets the pathfinding system.
   *
   * <p>This method stops the current pathfinding process, clears the scheduled actions, and resets
   * the state of the system.
   */
  private void reset() {
    if (visualizer == null) return;

    visualizer.reset();
  }
}
