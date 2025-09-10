package produsAdvanced.abstraction.portalSkills;

import contrib.utils.components.skill.Resource;
import core.Game;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import produsAdvanced.abstraction.Portal;

public class GreenPortalSkill extends PortalSkill {

  private static final IPath TEXTURE = new SimpleIPath("skills/green_portal");

  /**
   * Creates a new projectile skill.
   *
   * @param resourceCost Resource costs for casting.
   */
  public GreenPortalSkill(Tuple<Resource, Integer>... resourceCost) {
    super(TEXTURE, resourceCost);
  }

  @Override
  protected void createPortal(Point position) {
    if (Game.tileAt(position).get().levelElement() == LevelElement.GREEN_PORTAL) {
      Portal.createGreenPortal(position);
    }
  }
}
