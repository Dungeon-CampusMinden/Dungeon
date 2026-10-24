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
 * The server keeps applying it until the client sends a zero direction. That stop goes out over UDP
 * for low latency and over TCP so it cannot get lost; the server applies whichever copy arrives
 * first and drops the other by its sequence number.
 */
public final class MovementInputSystem extends System {
  private static float heldX;
  private static float heldY;
  private boolean moving;

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
      moving = true;
      Game.network().sendInput(InputMessage.move(direction));
    } else if (moving) {
      moving = false;
      InputMessage stop = InputMessage.move(Vector2.ZERO);
      Game.network().sendInput(stop);
      Game.network().send((short) 0, stop, true);
    }
  }

  /** Movement input must keep flowing while the game is paused so a release is never lost. */
  @Override
  public void stop() {
    this.run = true;
  }
}
