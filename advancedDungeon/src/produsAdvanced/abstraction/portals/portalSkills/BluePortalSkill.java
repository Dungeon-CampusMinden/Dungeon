package produsAdvanced.abstraction.portals.portalSkills;

import contrib.utils.components.skill.Resource;
import core.Game;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Tuple;
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD:advancedDungeon/src/produsAdvanced/abstraction/portalSkills/BluePortalSkill.java
=======
<<<<<<<< HEAD:advancedDungeon/src/produsAdvanced/abstraction/portalSkills/BluePortalSkill.java
>>>>>>> ac8cf0c7 (restructed portal related files)
<<<<<<< HEAD
<<<<<<< HEAD
import core.utils.Vector2;
=======
<<<<<<< HEAD
>>>>>>> 36adc3c1 (added green and blue portal variants)
=======
import core.utils.Vector2;
>>>>>>> d483f6ff (added direction to portals)
=======
>>>>>>> 20f3a7f9 (restructed portal related files):advancedDungeon/src/produsAdvanced/abstraction/portals/portalSkills/BluePortalSkill.java
=======
import core.utils.Vector2;
>>>>>>> cefa46bc (added PortalComponent to avoid unwanted portal on portal interactions)
=======
>>>>>>> ef71cb29 (added green and blue portal variants)
=======
import core.utils.Vector2;
>>>>>>> 878b072b (added direction to portals)
========
>>>>>>>> ac8cf0c7 (restructed portal related files):advancedDungeon/src/produsAdvanced/abstraction/portals/portalSkills/BluePortalSkill.java
>>>>>>> ac8cf0c7 (restructed portal related files)
=======
import core.utils.Vector2;
>>>>>>> efe893f0 (added PortalComponent to avoid unwanted portal on portal interactions)
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import produsAdvanced.abstraction.portals.PortalFactory;

public class BluePortalSkill extends PortalSkill {

<<<<<<< HEAD
<<<<<<< HEAD:advancedDungeon/src/produsAdvanced/abstraction/portalSkills/BluePortalSkill.java
<<<<<<< HEAD
<<<<<<< HEAD
=======
  public static final String SKILL_NAME = "BLUE_PORTAL";
>>>>>>> 20f3a7f9 (restructed portal related files):advancedDungeon/src/produsAdvanced/abstraction/portals/portalSkills/BluePortalSkill.java
  private static final IPath TEXTURE = new SimpleIPath("skills/blue_projectile");
=======
  private static final IPath TEXTURE = new SimpleIPath("skills/blue_portal");
>>>>>>> 36adc3c1 (added green and blue portal variants)
=======
  private static final IPath TEXTURE = new SimpleIPath("skills/blue_projectile");
>>>>>>> 077375b3 (updated all portal related assets to .json formats and moved them into advancedDungeon)
=======
  public static final String SKILL_NAME = "BLUE_PORTAL";
  private static final IPath TEXTURE = new SimpleIPath("skills/blue_projectile");
>>>>>>> ac8cf0c7 (restructed portal related files)

  /**
   * Creates a new projectile skill.
   *
   * @param resourceCost Resource costs for casting.
   */
  public BluePortalSkill(Tuple<Resource, Integer>... resourceCost) {
    super(SKILL_NAME,TEXTURE, resourceCost);
  }


  @Override
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> ac8cf0c7 (restructed portal related files)
  protected void createPortal(Point position, Vector2 currentVelocity, Point projectilePosition) {
    if (Game.tileAt(position).get().levelElement() == LevelElement.PORTAL) {
      Portal.createBluePortal(position,currentVelocity,projectilePosition);
=======
  protected void createPortal(Point position) {
    if (Game.tileAt(position).get().levelElement() == LevelElement.BLUE_PORTAL) {
      Portal.createBluePortal(position);
<<<<<<< HEAD
>>>>>>> 36adc3c1 (added green and blue portal variants)
=======
  protected void createPortal(Point position, Vector2 currentVelocity, Point projectilePosition) {
    if (Game.tileAt(position).get().levelElement() == LevelElement.PORTAL) {
      Portal.createBluePortal(position,currentVelocity,projectilePosition);
>>>>>>> d483f6ff (added direction to portals)
=======
  protected void createPortal(Point position) {
    if (Game.tileAt(position).get().levelElement() == LevelElement.PORTAL) {
<<<<<<< HEAD:advancedDungeon/src/produsAdvanced/abstraction/portalSkills/BluePortalSkill.java
      Portal.createBluePortal(position);
>>>>>>> 5d963fb8 (fixed portal creating bug and added directions to the portals to smoothen the transition)
=======
      PortalFactory.createBluePortal(position);
>>>>>>> 20f3a7f9 (restructed portal related files):advancedDungeon/src/produsAdvanced/abstraction/portals/portalSkills/BluePortalSkill.java
=======
  protected void createPortal(Point position, Vector2 currentVelocity) {
    if (Game.tileAt(position).get().levelElement() == LevelElement.PORTAL) {
      PortalFactory.createBluePortal(position, currentVelocity);
>>>>>>> cefa46bc (added PortalComponent to avoid unwanted portal on portal interactions)
=======
>>>>>>> ef71cb29 (added green and blue portal variants)
=======
  protected void createPortal(Point position, Vector2 currentVelocity, Point projectilePosition) {
    if (Game.tileAt(position).get().levelElement() == LevelElement.BLUE_PORTAL) {
      Portal.createBluePortal(position,currentVelocity,projectilePosition);
>>>>>>> 878b072b (added direction to portals)
=======
  protected void createPortal(Point position) {
    if (Game.tileAt(position).get().levelElement() == LevelElement.PORTAL) {
<<<<<<<< HEAD:advancedDungeon/src/produsAdvanced/abstraction/portalSkills/BluePortalSkill.java
      Portal.createBluePortal(position);
>>>>>>> 355d8064 (fixed portal creating bug and added directions to the portals to smoothen the transition)
========
      PortalFactory.createBluePortal(position);
>>>>>>>> ac8cf0c7 (restructed portal related files):advancedDungeon/src/produsAdvanced/abstraction/portals/portalSkills/BluePortalSkill.java
>>>>>>> ac8cf0c7 (restructed portal related files)
=======
  protected void createPortal(Point position, Vector2 currentVelocity) {
    if (Game.tileAt(position).get().levelElement() == LevelElement.PORTAL) {
      PortalFactory.createBluePortal(position, currentVelocity);
>>>>>>> efe893f0 (added PortalComponent to avoid unwanted portal on portal interactions)
    }
  }
}
