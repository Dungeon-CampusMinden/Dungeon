package systems;

import components.AmmunitionComponent;
import contrib.components.CollideComponent;
import contrib.utils.EntityUtils;
import contrib.utils.components.skill.projectileSkill.FireballSkill;
import core.Entity;
import core.Game;
import core.System;
import core.utils.MissingHeroException;
import core.utils.components.MissingComponentException;
import server.Server;

/**
 * System that allows the hero in Blockly to shoot a fireball.
 *
 * <p>Since Blockly code is executed in a separate thread, the libGDX context does not exist in that
 * thread, and no textures can be loaded there.
 *
 * <p>This system makes it possible to schedule the shooting of a fireball, which will then be
 * executed inside the ECS thread. This ensures that the textures for the fireball can be loaded.
 *
 * <p>Only one shot can be scheduled at a time. Make sure to call {@link server.Server#waitDelta()}
 * accordingly.
 */
public class ScheduleShootFireballSystem extends System {

  private static final float FIREBALL_RANGE = Integer.MAX_VALUE;
  private static final float FIREBALL_SPEED = 15f;
  private static final int FIREBALL_DMG = 1;
  private final FireballSkill fireballSkill =
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
          FIREBALL_DMG);
  private boolean shoot = false;

  @Override
  public void execute() {
    if (shoot) {
      Entity hero = Game.hero().orElseThrow(MissingHeroException::new);
      hero.fetch(AmmunitionComponent.class)
          .filter(AmmunitionComponent::checkAmmunition)
          .ifPresent(ac -> aimAndShoot(ac, hero));
      shoot = false;
    }
  }

  /** Schedule to shoot a fireball. */
  public void scheduleShoot() {
    shoot = true;
  }

  /**
   * Shoots a fireball in direction the hero is facing.
   *
   * @param ac AmmunitionComponent of the hero, ammunition amount will be reduced by 1
   * @param hero Entity to be used as hero for positioning
   */
  private void aimAndShoot(AmmunitionComponent ac, Entity hero) {
    fireballSkill.execute(hero);
    ac.spendAmmo();
    Server.waitDelta();
  }
}
