package produsAdvanced.abstraction;

import contrib.utils.components.skill.Resource;
import contrib.utils.components.skill.SkillTools;
import contrib.utils.components.skill.projectileSkill.ProjectileSkill;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.utils.*;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

import java.util.Optional;
import java.util.function.Consumer;

public class PortalSkill extends ProjectileSkill {

  /** Name of the Skill. */
  public static final String SKILL_NAME = "BLUE_PORTAL";

  private static final IPath TEXTURE = new SimpleIPath("skills/blue_portal");
  private static final IPath SOUND = new SimpleIPath("sounds/fireball.wav");
  private static final float SPEED = 22f;
  private static final float RANGE = 10f;
  private static final Vector2 HIT_BOX_SIZE = Vector2.of(1, 1);
  private static final long COOLDOWN = 500;

  /**
   * Creates a new projectile skill.
   *
   * @param resourceCost Resource costs for casting.
   */
  public PortalSkill(Tuple<Resource, Integer>... resourceCost) {
    super(SKILL_NAME, COOLDOWN, TEXTURE, SPEED, RANGE, HIT_BOX_SIZE, resourceCost);
  }

  @Override
  protected Point end(Entity caster) {
    return SkillTools.cursorPositionAsPoint();
  }

  @Override
  protected Consumer<Entity> onWallHit(Entity caster) {
    return e ->  {
      Optional<Entity> first = Game.allEntities().filter(entity -> entity.name().equals(name() + "_projectile")).findFirst();
      System.out.println(first.get().fetch(PositionComponent.class).get().coordinate());
      Game.remove(e);
    };
  }


}

