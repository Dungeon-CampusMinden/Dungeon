package core.game.render;

import java.awt.Graphics2D;

/**
 * A thread-local context holder for the current Graphics2D rendering surface.
 *
 * <p>This utility class provides thread-safe access to the active Graphics2D object used during
 * rendering. It allows render systems and other components to retrieve the current graphics context
 * without passing it through method parameters.
 */
public final class RenderContext {
  private static final ThreadLocal<Graphics2D> CURRENT = new ThreadLocal<>();

  private RenderContext() {}

  /**
   * Sets the current Graphics2D rendering context for the calling thread.
   *
   * @param g the Graphics2D object to set as the current context
   */
  public static void set(Graphics2D g) {
    CURRENT.set(g);
  }

  /**
   * Gets the current Graphics2D rendering context for the calling thread.
   *
   * @return the Graphics2D object set for the current thread, or null if not set
   */
  public static Graphics2D get() {
    return CURRENT.get();
  }

  /** Clears the current Graphics2D rendering context for the calling thread. */
  public static void clear() {
    CURRENT.remove();
  }
}
