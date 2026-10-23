package engine.systems.input;

import engine.Game;
import engine.System;
import engine.network.messages.c2s.InputMessage;
import engine.utils.Direction;
import engine.utils.Vector2;

/**
 * Sends the movement direction the local player holds as one input per frame.
 *
 * <p>Input sources such as keyboard callbacks and controllers report held directions through {@link
 * #hold(Direction)} during the frame. This system runs after them and sends the combined direction.
 * The server keeps applying it until the client sends a zero direction, which this system repeats
 * for a few frames after release in case a single input packet gets lost.
 */
public final class MovementInputSystem extends System {
  private static final int STOP_REPEAT_FRAMES = 3;

  private static float heldX;
  private static float heldY;
  private int stopFramesLeft;

  /** Creates a new MovementInputSystem. */
  public MovementInputSystem() {
    super(AuthoritativeSide.CLIENT);
  }

  /**
   * Records a movement direction held during the current frame.
   *
   * @param direction the held direction
   */
  public static void hold(Direction direction) {
    heldX += direction.x();
    heldY += direction.y();
  }

  @Override
  public void execute() {
    Vector2 direction = Vector2.of(Math.signum(heldX), Math.signum(heldY));
    heldX = 0;
    heldY = 0;
    if (!direction.isZero()) {
      stopFramesLeft = STOP_REPEAT_FRAMES;
      Game.network().sendInput(InputMessage.move(direction));
    } else if (stopFramesLeft > 0) {
      stopFramesLeft--;
      Game.network().sendInput(InputMessage.move(Vector2.ZERO));
    }
  }

  /** Movement input must keep flowing while the game is paused so a release is never lost. */
  @Override
  public void stop() {
    this.run = true;
  }
}
