package core.game.render;

import java.awt.Graphics2D;

/**
 * Provides the current Graphics2D for LITIENGINE render systems.
 *
 * <p>Set by the active Screen during Screen.render(Graphics2D) and consumed by ECS systems
 * during ECSManagement.renderAll(...).
 */
public final class RenderContext {
  private static final ThreadLocal<Graphics2D> CURRENT = new ThreadLocal<>();

  private RenderContext() {}

  public static void set(Graphics2D g) {
    CURRENT.set(g);
  }

  public static Graphics2D get() {
    return CURRENT.get();
  }

  public static void clear() {
    CURRENT.remove();
  }
}
