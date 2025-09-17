package produsAdvanced.abstraction.portals.portalSkills;

import contrib.utils.components.skill.Resource;
import core.utils.Point;
import core.utils.Tuple;
<<<<<<< HEAD:advancedDungeon/src/produsAdvanced/abstraction/portalSkills/GreenPortalSkill.java
<<<<<<< HEAD
<<<<<<< HEAD
import core.utils.Vector2;
=======
>>>>>>> 36adc3c1 (added green and blue portal variants)
=======
import core.utils.Vector2;
>>>>>>> d483f6ff (added direction to portals)
=======
>>>>>>> 20f3a7f9 (restructed portal related files):advancedDungeon/src/produsAdvanced/abstraction/portals/portalSkills/GreenPortalSkill.java
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import produsAdvanced.abstraction.portals.PortalFactory;

public class GreenPortalSkill extends PortalSkill {

<<<<<<< HEAD:advancedDungeon/src/produsAdvanced/abstraction/portalSkills/GreenPortalSkill.java
<<<<<<< HEAD
<<<<<<< HEAD
=======
  public static final String SKILL_NAME = "GREEN_PORTAL";
>>>>>>> 20f3a7f9 (restructed portal related files):advancedDungeon/src/produsAdvanced/abstraction/portals/portalSkills/GreenPortalSkill.java
  private static final IPath TEXTURE = new SimpleIPath("skills/green_projectile");
=======
  private static final IPath TEXTURE = new SimpleIPath("skills/green_portal");
>>>>>>> 36adc3c1 (added green and blue portal variants)
=======
  private static final IPath TEXTURE = new SimpleIPath("skills/green_projectile");
>>>>>>> 077375b3 (updated all portal related assets to .json formats and moved them into advancedDungeon)

  /**
   * Creates a new projectile skill.
   *
   * @param resourceCost Resource costs for casting.
   */
  public GreenPortalSkill(Tuple<Resource, Integer>... resourceCost) {
    super(SKILL_NAME,TEXTURE, resourceCost);
  }

  @Override
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
  protected void createPortal(Point position, Vector2 currentVelocity, Point projectilePosition) {
    if (Game.tileAt(position).get().levelElement() == LevelElement.PORTAL) {
      Portal.createGreenPortal(position, currentVelocity, projectilePosition);
=======
  protected void createPortal(Point position) {
    if (Game.tileAt(position).get().levelElement() == LevelElement.GREEN_PORTAL) {
      Portal.createGreenPortal(position);
>>>>>>> 36adc3c1 (added green and blue portal variants)
=======
  protected void createPortal(Point position, Vector2 currentVelocity, Point projectilePosition) {
    if (Game.tileAt(position).get().levelElement() == LevelElement.PORTAL) {
      Portal.createGreenPortal(position, currentVelocity, projectilePosition);
>>>>>>> d483f6ff (added direction to portals)
    }
=======
  protected void createPortal(Point position) {
<<<<<<< HEAD:advancedDungeon/src/produsAdvanced/abstraction/portalSkills/GreenPortalSkill.java
    Portal.createGreenPortal(position);
>>>>>>> 5d963fb8 (fixed portal creating bug and added directions to the portals to smoothen the transition)
=======
    PortalFactory.createGreenPortal(position);
>>>>>>> 20f3a7f9 (restructed portal related files):advancedDungeon/src/produsAdvanced/abstraction/portals/portalSkills/GreenPortalSkill.java
  }
}
