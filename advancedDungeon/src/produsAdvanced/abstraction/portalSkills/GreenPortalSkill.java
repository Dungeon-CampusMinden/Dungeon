package produsAdvanced.abstraction.portalSkills;

import contrib.utils.components.skill.Resource;
import core.Game;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Tuple;
<<<<<<< HEAD
import core.utils.Vector2;
=======
>>>>>>> 36adc3c1 (added green and blue portal variants)
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import produsAdvanced.abstraction.Portal;

public class GreenPortalSkill extends PortalSkill {

<<<<<<< HEAD
  private static final IPath TEXTURE = new SimpleIPath("skills/green_projectile");
=======
  private static final IPath TEXTURE = new SimpleIPath("skills/green_portal");
>>>>>>> 36adc3c1 (added green and blue portal variants)

  /**
   * Creates a new projectile skill.
   *
   * @param resourceCost Resource costs for casting.
   */
  public GreenPortalSkill(Tuple<Resource, Integer>... resourceCost) {
    super(TEXTURE, resourceCost);
  }

  @Override
<<<<<<< HEAD
  protected void createPortal(Point position, Vector2 currentVelocity, Point projectilePosition) {
    if (Game.tileAt(position).get().levelElement() == LevelElement.PORTAL) {
      Portal.createGreenPortal(position, currentVelocity, projectilePosition);
=======
  protected void createPortal(Point position) {
    if (Game.tileAt(position).get().levelElement() == LevelElement.GREEN_PORTAL) {
      Portal.createGreenPortal(position);
>>>>>>> 36adc3c1 (added green and blue portal variants)
    }
  }
}
