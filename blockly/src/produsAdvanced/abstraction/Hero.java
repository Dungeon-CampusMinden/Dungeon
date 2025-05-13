package produsAdvanced.abstraction;

import com.badlogic.gdx.Input;
import contrib.utils.components.skill.FireballSkill;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.components.PlayerComponent;
import core.components.VelocityComponent;
import core.utils.Point;
import java.util.function.Supplier;

public class Hero {
  private Entity hero;
  private int FIREBALL_COOL_DOWN = 500;

  private Supplier<Point> fireballTarget = () -> new Point(0, 0);
  private Skill fireball = new Skill(new FireballSkill(fireballTarget), FIREBALL_COOL_DOWN);

  public Hero(Entity heroInstance) {
    this.hero = heroInstance;
    PlayerComponent pc = heroInstance.fetch(PlayerComponent.class).get();
    pc.removeCallbacks();
  }

  public void setController(PlayerController controller) {
    if (controller == null) return;
    PlayerComponent pc = hero.fetch(PlayerComponent.class).get();
    for (int key = 0; key <= Input.Keys.Z; key++) {
      int finalKey = key;
      pc.registerCallback(key, entity -> controller.processKey(Input.Keys.toString(finalKey)));
    }
  }

  public void setXSpeed(float speed) {
    hero.fetch(VelocityComponent.class).get().currentXVelocity(speed);
  }

  public void setYSpeed(float speed) {
    hero.fetch(VelocityComponent.class).get().currentYVelocity(speed);
  }

  public Point getMousePosition() {
    return SkillTools.cursorPositionAsPoint();
  }

  public void shootFireball(Point direction) {
    Supplier<Point> newTarget = () -> direction;
    Skill newFireball = new Skill(new FireballSkill(fireballTarget), FIREBALL_COOL_DOWN);
    if (fireball.canBeUsedAgain()) {
      newFireball.execute(hero);
      newFireball.setLastUsedToNow();
    }
    fireball = newFireball;
    fireballTarget = newTarget;
  }
}
