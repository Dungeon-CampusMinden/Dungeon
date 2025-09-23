package systems;

import components.AmmunitionComponent;
import contrib.components.CollideComponent;
import contrib.systems.EventScheduler;
import contrib.utils.EntityUtils;
import contrib.utils.components.skill.projectileSkill.FireballSkill;
import core.Entity;
import core.Game;
import core.utils.MissingHeroException;
import core.utils.components.MissingComponentException;

/**
 * Utilty class that allows the hero in Blockly to shoot a fireball.
 *
 * <p>Since Blockly code is executed in a separate thread, the libGDX context does not exist in that
 * thread, and no textures can be loaded there.
 *
 * <p>This Class makes it possible to schedule the shooting of a fireball, which will then be
 * executed inside the ECS thread. This ensures that the textures for the fireball can be loaded.
 */
public class FireballSchedular {

  private static final float FIREBALL_RANGE = Integer.MAX_VALUE;
  private static final float FIREBALL_SPEED = 15f;
  private static final int FIREBALL_DMG = 1;
  private static final boolean IGNORE_FIRST_WALL = false;
  private static final FireballSkill fireballSkill =
      new FireballSkill(
          () -> {
            Entity hero = Game.hero().orElseThrow(MissingHeroException::new);
            return hero.fetch(CollideComponent.class)
                .map(cc -> cc.center(hero))
                .map(p -> p.translate(EntityUtils.getViewDirection(hero)))
                .orElseThrow(() -> MissingComponentException.build(hero, CollideComponent.class));
          },
          1,
          FIREBALL_SPEED,
          FIREBALL_RANGE,
          FIREBALL_DMG,
          IGNORE_FIRST_WALL);

  private boolean shoot = false;

  /**
   * Shoot a fireball in the viewdirection of the hero.
   *
   * <p>This uses the {@link EventScheduler}
   */
  public static void shoot() {
    EventScheduler.scheduleAction(
        () -> {
          Entity hero = Game.hero().orElseThrow(MissingHeroException::new);
          hero.fetch(AmmunitionComponent.class)
              .filter(AmmunitionComponent::checkAmmunition)
              .ifPresent(ac -> aimAndShoot(ac, hero));
        },
        0);
  }

  /**
   * Shoots a fireball in direction the hero is facing.
   *
   * @param ac AmmunitionComponent of the hero, ammunition amount will be reduced by 1
   * @param hero Entity to be used as hero for positioning
   */
  private static void aimAndShoot(AmmunitionComponent ac, Entity hero) {
    fireballSkill.execute(hero);
    ac.spendAmmo();
  }
}
