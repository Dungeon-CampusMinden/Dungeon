package produsAdvanced.abstraction.portals.portalSkills;

import contrib.utils.components.skill.Resource;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import produsAdvanced.abstraction.portals.PortalColor;
import produsAdvanced.abstraction.portals.PortalFactory;

/**
 * Wrapper class for green portals that defines the skill name, texture and behaviour when creating
 * a portal.
 */
public class GreenPortalSkill extends PortalSkill {

  /** Name of the Skill. */
  public static final String SKILL_NAME = "GREEN_PORTAL";

  private static final IPath TEXTURE = new SimpleIPath("skills/green_projectile");

  /**
   * Creates a new green projectile skill.
   *
   * @param resourceCost Resource costs for casting.
   */
  public GreenPortalSkill(Tuple<Resource, Integer>... resourceCost) {
    super(SKILL_NAME, TEXTURE, resourceCost);
  }

  /**
   * Method that creates the green portal via the PortalFactory class.
   *
   * @param position Position where the portal will be created
   */
  @Override
  protected void createPortal(Point position) {
    PortalFactory.createPortal(position, PortalColor.GREEN);
  }
}
