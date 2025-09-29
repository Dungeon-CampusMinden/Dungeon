package produsAdvanced.abstraction.portals.portalSkills;

import contrib.utils.components.skill.Resource;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import produsAdvanced.abstraction.portals.PortalColor;
import produsAdvanced.abstraction.portals.PortalFactory;

/**
 * Wrapper class for blue portals that defines the skill name, texture and behaviour when creating a
 * portal.
 */
public class BluePortalSkill extends PortalSkill {

  public static final String SKILL_NAME = "BLUE_PORTAL";
  private static final IPath TEXTURE = new SimpleIPath("skills/blue_projectile");

  /**
   * Creates a new blue projectile skill.
   *
   * @param resourceCost Resource costs for casting.
   */
  public BluePortalSkill(Tuple<Resource, Integer>... resourceCost) {
    super(SKILL_NAME, TEXTURE, resourceCost);
  }

  /**
   * Method that creates the blue portal via the PortalFactory class.
   *
   * @param position Position where the portal will be created
   */
  @Override
  protected void createPortal(Point position) {
    PortalFactory.createPortal(position, PortalColor.BLUE);
  }
}
