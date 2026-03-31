package portal.riddles.utils;

import contrib.hud.DialogUtils;
import contrib.utils.DynamicCompiler;
import core.Entity;
import core.Game;
import core.utils.Point;
import core.utils.components.path.SimpleIPath;
import java.util.Optional;
import portal.portals.abstraction.Calculations;
import starter.PortalStarter;

/**
 * Utility class for portal-related engine functionality.
 *
 * <p>This class provides helper methods to locate portal entities within the current level and to
 * execute portal-related calculations supplied by user-defined code.
 *
 * <p>It acts as a bridge between engine code and dynamically compiled gameplay logic.
 */
public class PortalUtils {

  /** Path to the user-defined calculations source file. */
  private static final SimpleIPath CALCULATIONS_PATH =
      new SimpleIPath("advancedDungeon/src/portal/riddles/MyCalculations.java");

  /** Fully qualified class name of the user-defined calculations implementation. */
  private static final String CALCULATIONS_CLASSNAME = "portal.riddles.MyCalculations";

  /** Name of the blue portal entity. */
  public static final String BLUE_PORTAL_NAME = "BLUE_PORTAL";

  /** Name of the green portal entity. */
  public static final String GREEN_PORTAL_NAME = "GREEN_PORTAL";

  /**
   * Returns the blue portal entity, if present in the current level.
   *
   * @return an {@link Optional} containing the blue portal entity, or {@link Optional#empty()} if
   *     none exists
   */
  public static Optional<Entity> getBluePortal() {
    return Game.levelEntities()
        .filter(entity -> entity.name().equals(BLUE_PORTAL_NAME))
        .findFirst();
  }

  /**
   * Returns the green portal entity, if present in the current level.
   *
   * @return an {@link Optional} containing the green portal entity, or {@link Optional#empty()} if
   *     none exists
   */
  public static Optional<Entity> getGreenPortal() {
    return Game.levelEntities()
        .filter(entity -> entity.name().equals(GREEN_PORTAL_NAME))
        .findFirst();
  }

  /**
   * Calculates the exit position for a portal traversal.
   *
   * <p>This method dynamically loads the user-provided implementation of {@link Calculations} and
   * delegates the exit position computation to it.
   *
   * <p>If loading, execution, or the returned value fails, a fallback position is computed based on
   * the portal's current position and view direction.
   *
   * <p>In debug mode, exceptions are printed to the console. In all failure cases, a user-facing
   * error dialog is shown.
   *
   * @param portal the portal entity that was used for traversal
   * @return the calculated exit position, or a fallback position if an error occurs
   */
  public static Point calculatePortalExit(Entity portal) {
    Object o = null;
    try {
      o = DynamicCompiler.loadUserInstance(CALCULATIONS_PATH, CALCULATIONS_CLASSNAME);
      Point returnPoint = ((Calculations) o).calculatePortalExit(portal);
      if (returnPoint == null) throw new Exception();
      return returnPoint;
    } catch (Exception e) {
      if (PortalStarter.DEBUG_MODE) e.printStackTrace();
      DialogUtils.showTextPopup("Da stimmt etwas nicht mit meinen Berechnungen.", "Code Error");
    }

    // Fallback: place exit point in front of the portal based on its view direction
    return Tools.getPositionComponent(portal)
        .position()
        .translate(Tools.getPositionComponent(portal).viewDirection());
  }
}
