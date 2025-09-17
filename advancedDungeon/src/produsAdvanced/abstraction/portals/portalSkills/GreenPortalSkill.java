package produsAdvanced.abstraction.portals.portalSkills;

import contrib.utils.components.skill.Resource;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.Vector2;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import produsAdvanced.abstraction.portals.PortalFactory;

public class GreenPortalSkill extends PortalSkill {

  public static final String SKILL_NAME = "GREEN_PORTAL";
  private static final IPath TEXTURE = new SimpleIPath("skills/green_projectile");

  /**
   * Creates a new projectile skill.
   *
   * @param resourceCost Resource costs for casting.
   */
  public GreenPortalSkill(Tuple<Resource, Integer>... resourceCost) {
    super(SKILL_NAME,TEXTURE, resourceCost);
  }

  @Override
  protected void createPortal(Point position, Vector2 currentVelocity) {
    PortalFactory.createGreenPortal(position, currentVelocity);
  }
}
