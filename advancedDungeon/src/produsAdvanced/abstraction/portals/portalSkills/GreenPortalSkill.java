package produsAdvanced.abstraction.portals.portalSkills;

import contrib.utils.components.skill.Resource;
import core.utils.Point;
import core.utils.Tuple;
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD:advancedDungeon/src/produsAdvanced/abstraction/portalSkills/GreenPortalSkill.java
=======
<<<<<<<< HEAD:advancedDungeon/src/produsAdvanced/abstraction/portalSkills/GreenPortalSkill.java
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
>>>>>>> 20f3a7f9 (restructed portal related files):advancedDungeon/src/produsAdvanced/abstraction/portals/portalSkills/GreenPortalSkill.java
=======
import core.utils.Vector2;
>>>>>>> cefa46bc (added PortalComponent to avoid unwanted portal on portal interactions)
=======
>>>>>>> ef71cb29 (added green and blue portal variants)
=======
import core.utils.Vector2;
>>>>>>> 878b072b (added direction to portals)
========
>>>>>>>> ac8cf0c7 (restructed portal related files):advancedDungeon/src/produsAdvanced/abstraction/portals/portalSkills/GreenPortalSkill.java
>>>>>>> ac8cf0c7 (restructed portal related files)
=======
import core.utils.Vector2;
>>>>>>> efe893f0 (added PortalComponent to avoid unwanted portal on portal interactions)
=======
>>>>>>> c71f73d8 (decluttered portal create method)
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import produsAdvanced.abstraction.portals.PortalColor;
import produsAdvanced.abstraction.portals.PortalFactory;

/**
 * Wrapper class for green portals that defines the skill name, texture and behaviour when creating
 * a portal.
 */
public class GreenPortalSkill extends PortalSkill {

<<<<<<< HEAD
<<<<<<< HEAD
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
=======
=======
  /** Name of the Skill. */
>>>>>>> 4eca951b (fixed javadocs)
  public static final String SKILL_NAME = "GREEN_PORTAL";

  private static final IPath TEXTURE = new SimpleIPath("skills/green_projectile");
>>>>>>> ac8cf0c7 (restructed portal related files)

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
<<<<<<< HEAD
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
      Portal.createGreenPortal(position, currentVelocity, projectilePosition);
=======
  protected void createPortal(Point position) {
    if (Game.tileAt(position).get().levelElement() == LevelElement.GREEN_PORTAL) {
      Portal.createGreenPortal(position);
<<<<<<< HEAD
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
=======
  protected void createPortal(Point position, Vector2 currentVelocity) {
    PortalFactory.createGreenPortal(position, currentVelocity);
>>>>>>> cefa46bc (added PortalComponent to avoid unwanted portal on portal interactions)
=======
>>>>>>> ef71cb29 (added green and blue portal variants)
=======
  protected void createPortal(Point position, Vector2 currentVelocity, Point projectilePosition) {
    if (Game.tileAt(position).get().levelElement() == LevelElement.GREEN_PORTAL) {
      Portal.createGreenPortal(position, currentVelocity, projectilePosition);
>>>>>>> 878b072b (added direction to portals)
    }
=======
  protected void createPortal(Point position) {
<<<<<<<< HEAD:advancedDungeon/src/produsAdvanced/abstraction/portalSkills/GreenPortalSkill.java
    Portal.createGreenPortal(position);
>>>>>>> 355d8064 (fixed portal creating bug and added directions to the portals to smoothen the transition)
========
    PortalFactory.createGreenPortal(position);
>>>>>>>> ac8cf0c7 (restructed portal related files):advancedDungeon/src/produsAdvanced/abstraction/portals/portalSkills/GreenPortalSkill.java
>>>>>>> ac8cf0c7 (restructed portal related files)
=======
  protected void createPortal(Point position, Vector2 currentVelocity) {
    PortalFactory.createGreenPortal(position, currentVelocity);
>>>>>>> efe893f0 (added PortalComponent to avoid unwanted portal on portal interactions)
=======
  protected void createPortal(Point position) {
    PortalFactory.createPortal(position, PortalColor.GREEN);
>>>>>>> c71f73d8 (decluttered portal create method)
  }
}
