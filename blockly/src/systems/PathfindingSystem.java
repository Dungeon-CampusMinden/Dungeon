package systems;

import client.KeyboardConfig;
import com.badlogic.gdx.Gdx;
import core.System;
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
  private long stepDelay = 250; // Step delay in milliseconds if using autoStep

  @Override
  public void execute() {
    if (visualizer == null) return;

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
