package portal.energyPellet;

import contrib.components.AIComponent;
import contrib.components.CollideComponent;
import contrib.components.SkillComponent;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.SimpleIPath;
import java.util.List;
import java.util.Map;

public class EnergyPelletLauncher {
  private static final SimpleIPath PELLET_LAUNCHER = new SimpleIPath("portal/pellet_launcher");
  private static int launcherNumber = 0;

  /**
   * Creates a new entity that can shoot energy pellets.
   *
   * @param position the position of the pellet launcher
   * @param direction the direction the pellet launcher is facing.
   * @param attackRange Maximum travel range of the energy pellet.
   * @param projectileLifetime Time in ms before the projectile is removed.
   * @return a new energyPelletLauncher entity.
   */
  public static Entity energyPelletLauncher(
      Point position, Direction direction, float attackRange, long projectileLifetime) {
    launcherNumber++;
    String uniqueName = "energyPelletLauncher_" + launcherNumber;
    Entity launcher = new Entity(uniqueName);
    launcher.add(new PositionComponent(position));
    DrawComponent dc = chooseTexture(direction, PELLET_LAUNCHER);
    launcher.add(dc);
    launcher.add(new CollideComponent());
    String uniqueSkillName = uniqueName + "_skill";
    Skill energyPelletSkill =
        new EnergyPelletSkill(
            uniqueSkillName,
            SkillTools::playerPositionAsPoint,
            EnergyPelletSkill.COOLDOWN,
            attackRange,
            projectileLifetime);
    launcher.add(
        new AIComponent(
            entity -> {},
            new PelletLauncherBehaviour(
                uniqueSkillName, position, attackRange, direction, energyPelletSkill),
            entity -> false));
    launcher.add(new SkillComponent(energyPelletSkill));

    return launcher;
  }

  /**
   * This method help to choose the correct single texture from an animationMap.
   *
   * @param direction the direction the entity is facing.
   * @param path the path of the texture.
   * @return a new DrawComponent including the correct StateMachine for the texture.
   */
  static DrawComponent chooseTexture(Direction direction, SimpleIPath path) {
    Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(path);
    StateMachine sm =
        switch (direction) {
          case DOWN -> {
            State top = State.fromMap(animationMap, "top");
            yield new StateMachine(List.of(top));
          }
          case LEFT -> {
            State right = State.fromMap(animationMap, "right");
            yield new StateMachine(List.of(right));
          }
          case RIGHT -> {
            State left = State.fromMap(animationMap, "left");
            yield new StateMachine(List.of(left));
          }
          default -> {
            State bottom = State.fromMap(animationMap, "bottom");
            yield new StateMachine(List.of(bottom));
          }
        };

    return new DrawComponent(sm);
  }
}
