package portal.level;

import contrib.components.CollideComponent;
import contrib.components.SkillComponent;
import contrib.utils.components.skill.projectileSkill.FireballSkill;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import core.utils.components.path.SimpleIPath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import portal.lightWall.LightWallFactory;
import portal.util.AdvancedLevel;

/**
 * The player has to use the light wall to block the projectiles to reach the exit without dying.
 */
public class LightWallLevel_1 extends AdvancedLevel {
  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */

  // Shooter / Fireball-Logik
  private final List<Entity> shooters = new ArrayList<>();

  private final Map<Entity, Long> shooterLastShot = new HashMap<>();
  // Einfaches, konstantes Schussintervall in Millisekunden (nicht zur Laufzeit änderbar)
  private static final long CHORT_SHOOT_INTERVAL_MS = 200L;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public LightWallLevel_1(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Portal Demo Level");
  }

  @Override
  protected void onFirstTick() {

    spawnChortFireballShooter(
        namedPoints.get("monster1").x(), namedPoints.get("monster1").y(), Direction.DOWN);
    spawnChortFireballShooter(
        namedPoints.get("monster2").x(), namedPoints.get("monster2").y(), Direction.DOWN);
    spawnChortFireballShooter(
        namedPoints.get("monster3").x(), namedPoints.get("monster3").y(), Direction.DOWN);
    spawnChortFireballShooter(
        namedPoints.get("monster4").x(), namedPoints.get("monster4").y(), Direction.RIGHT);
    spawnChortFireballShooter(
        namedPoints.get("monster5").x(), namedPoints.get("monster5").y(), Direction.RIGHT);
    spawnChortFireballShooter(
        namedPoints.get("monster6").x(), namedPoints.get("monster6").y(), Direction.RIGHT);

    Entity emitter =
        LightWallFactory.createEmitter(namedPoints.get("emitter"), Direction.DOWN, false);
    Game.add(LevelCreatorTools.wallLever(emitter, getPoint("switch")));
    Game.add(emitter);
  }

  @Override
  protected void onTick() {
    long now = System.currentTimeMillis();
    for (Entity shooter : shooters) {
      long last = shooterLastShot.getOrDefault(shooter, 0L);
      if (now - last >= CHORT_SHOOT_INTERVAL_MS) {
        shooter
            .fetch(SkillComponent.class)
            .flatMap(sc -> sc.activeSkill())
            .ifPresent(skill -> skill.execute(shooter));
        shooterLastShot.put(shooter, now);
      }
    }
  }

  private void spawnChortFireballShooter(float x, float y, Direction direction) {
    Entity shooter = new Entity("ChortFireballShooter");
    shooter.add(new PositionComponent(new Point(x, y), direction));
    DrawComponent dc = new DrawComponent(new SimpleIPath("character/monster/chort"));
    shooter.add(dc);
    shooter.add(new CollideComponent());
    shooter.add(new SkillComponent());
    // Größere Reichweite (z.B. 25 Felder) damit Projektil bis x=1 kommt
    final Direction dir = direction;
    FireballSkill fireball =
        new FireballSkill(
            () ->
                shooter
                    .fetch(PositionComponent.class)
                    .map(
                        pc -> {
                          // Ziel weit in die gegebene Richtung setzen (Reichweite 25 Felder)
                          switch (dir) {
                            case DOWN:
                              // Inverted: in diesem Projekt wächst y offenbar nach oben
                              return new Point(pc.position().x(), pc.position().y() - 25f);
                            case UP:
                              return new Point(pc.position().x(), pc.position().y() + 25f);
                            case RIGHT:
                              return new Point(pc.position().x() + 25f, pc.position().y());
                            case LEFT:
                            default:
                              return new Point(pc.position().x() - 25f, pc.position().y());
                          }
                        })
                    .orElse(new Point(x, y)),
            0L,
            25f,
            true); // overload: (target, cooldown, range, ignoreFirstWall)
    shooter.fetch(SkillComponent.class).ifPresent(sc -> sc.addSkill(fireball));
    Game.add(shooter);
    shooters.add(shooter);
    shooterLastShot.put(shooter, 0L);
  }
}
