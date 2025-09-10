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

public class BluePortalSkill extends PortalSkill {

<<<<<<< HEAD
  private static final IPath TEXTURE = new SimpleIPath("skills/blue_projectile");
=======
  private static final IPath TEXTURE = new SimpleIPath("skills/blue_portal");
>>>>>>> 36adc3c1 (added green and blue portal variants)

  /**
   * Creates a new projectile skill.
   *
   * @param resourceCost Resource costs for casting.
   */
  public BluePortalSkill(Tuple<Resource, Integer>... resourceCost) {
    super(TEXTURE, resourceCost);
  }


  @Override
<<<<<<< HEAD
  protected void createPortal(Point position, Vector2 currentVelocity, Point projectilePosition) {
    if (Game.tileAt(position).get().levelElement() == LevelElement.PORTAL) {
      Portal.createBluePortal(position,currentVelocity,projectilePosition);
=======
  protected void createPortal(Point position) {
    if (Game.tileAt(position).get().levelElement() == LevelElement.BLUE_PORTAL) {
      Portal.createBluePortal(position);
>>>>>>> 36adc3c1 (added green and blue portal variants)
    }
  }
}
