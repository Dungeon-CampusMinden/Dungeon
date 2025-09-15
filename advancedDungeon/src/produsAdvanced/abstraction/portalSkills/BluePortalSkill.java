package produsAdvanced.abstraction.portalSkills;

import contrib.utils.components.skill.Resource;
import core.Game;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Tuple;
<<<<<<< HEAD
<<<<<<< HEAD
import core.utils.Vector2;
=======
>>>>>>> ef71cb29 (added green and blue portal variants)
=======
import core.utils.Vector2;
>>>>>>> 878b072b (added direction to portals)
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import produsAdvanced.abstraction.Portal;

public class BluePortalSkill extends PortalSkill {

  private static final IPath TEXTURE = new SimpleIPath("skills/blue_portal");

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
<<<<<<< HEAD
  protected void createPortal(Point position, Vector2 currentVelocity, Point projectilePosition) {
    if (Game.tileAt(position).get().levelElement() == LevelElement.BLUE_PORTAL) {
      Portal.createBluePortal(position,currentVelocity,projectilePosition);
=======
  protected void createPortal(Point position) {
    if (Game.tileAt(position).get().levelElement() == LevelElement.BLUE_PORTAL) {
      Portal.createBluePortal(position);
>>>>>>> ef71cb29 (added green and blue portal variants)
=======
  protected void createPortal(Point position, Vector2 currentVelocity, Point projectilePosition) {
    if (Game.tileAt(position).get().levelElement() == LevelElement.BLUE_PORTAL) {
      Portal.createBluePortal(position,currentVelocity,projectilePosition);
>>>>>>> 878b072b (added direction to portals)
    }
  }
}
