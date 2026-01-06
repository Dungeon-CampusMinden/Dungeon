package portal.portals.abstraction;

import contrib.hud.DialogUtils;
import contrib.utils.DynamicCompiler;
import core.Entity;
import core.Game;
import core.utils.Point;
import core.utils.components.path.SimpleIPath;
import java.util.Optional;
import portal.util.Tools;
import starter.PortalStarter;

public class PortalUtils {

  private static final SimpleIPath CALCULATIONS_PATH =
      new SimpleIPath("advancedDungeon/src/portal/riddles/MyCalculations.java");
  private static final String CALCULATIONS_CLASSNAME = "portal.riddles.MyCalculations";

  /** Name of the blue portal. */
  public static final String BLUE_PORTAL_NAME = "BLUE_PORTAL";

  /** Name of the green portal. */
  public static final String GREEN_PORTAL_NAME = "GREEN_PORTAL";

  /**
   * Returns the blue portal, if it exists.
   *
   * @return an {@link Optional} containing the blue portal entity, or empty if none exists
   */
  public static Optional<Entity> getBluePortal() {
    return Game.levelEntities()
        .filter(entity -> entity.name().equals(BLUE_PORTAL_NAME))
        .findFirst();
  }

  /**
   * Returns the green portal, if it exists.
   *
   * @return an {@link Optional} containing the green portal entity, or empty if none exists
   */
  public static Optional<Entity> getGreenPortal() {
    return Game.levelEntities()
        .filter(entity -> entity.name().equals(GREEN_PORTAL_NAME))
        .findFirst();
  }

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
    return Tools.getPositionComponent(portal)
        .position()
        .translate(Tools.getPositionComponent(portal).viewDirection());
  }
}
