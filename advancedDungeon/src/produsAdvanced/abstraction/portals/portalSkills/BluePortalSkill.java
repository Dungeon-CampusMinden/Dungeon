package produsAdvanced.abstraction.portals.portalSkills;

import contrib.utils.components.skill.Resource;
import core.Game;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import produsAdvanced.abstraction.portals.PortalFactory;

public class BluePortalSkill extends PortalSkill {

  public static final String SKILL_NAME = "BLUE_PORTAL";
  private static final IPath TEXTURE = new SimpleIPath("skills/blue_projectile");

  /**
   * Creates a new projectile skill.
   *
   * @param resourceCost Resource costs for casting.
   */
  public BluePortalSkill(Tuple<Resource, Integer>... resourceCost) {
    super(SKILL_NAME,TEXTURE, resourceCost);
  }


  @Override
  protected void createPortal(Point position) {
    if (Game.tileAt(position).get().levelElement() == LevelElement.PORTAL) {
      PortalFactory.createBluePortal(position);
    }
  }
}
