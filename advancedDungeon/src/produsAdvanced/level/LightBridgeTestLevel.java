package produsAdvanced.level;

import contrib.entities.LeverFactory;
import contrib.utils.ICommand;
import contrib.components.SkillComponent;
import contrib.utils.components.skill.projectileSkill.FireballSkill;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import core.utils.components.path.SimpleIPath;
import entities.LightBridgeFactory;
import entities.LightWallFactory;
import entities.TractorBeamFactory;
import produsAdvanced.abstraction.portals.PortalColor;
import produsAdvanced.abstraction.portals.PortalFactory;

import java.util.*;
import level.AdvancedLevel;
import core.components.DrawComponent;
import contrib.components.CollideComponent;


/**
 * Testlevel für die Lichtbrücke.
 * Zeigt beim ersten Tick einen kurzen Hinweis.
 */
public class LightBridgeTestLevel extends AdvancedLevel {

  private static final Direction BRIDGE_DIRECTION = Direction.DOWN;
  private static final Direction WALL_DIRECTION = Direction.DOWN;

  private int levelWidth;
  private int levelHeight;

  // Hebel & Emitter
  private Entity wallLever;
  private Entity wallEmitter;
  private Entity bridgeLever;
  private Entity bridgeEmitter;
  private Entity bridgeLever2;
  private Entity bridgeEmitter2;
  private Entity extendLever;

  // Shooter / Fireball-Logik
  private final List<Entity> shooters = new ArrayList<>();
  private final Map<Entity, Long> shooterLastShot = new HashMap<>();
  private static final long CHORT_SHOOT_INTERVAL_MS = 700;

  private List<Entity> currentBridgeEntities = null; // speichert alle Entities der aktiven Bridge
  private List<Entity> tractorBeamEntities = null; // neu: Referenz auf TractorBeam

  public LightBridgeTestLevel(
    LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "LightBridgeWallTest");
    levelHeight = layout.length;
    levelWidth = layout.length > 0 ? layout[0].length : 0;
  }

  @Override
  protected void onFirstTick() {

    setupScenario();

  }

  private void setupScenario() {

    Point newBridgeEmitterPos = new Point(4, 7);
    bridgeEmitter = LightBridgeFactory.createLightBridge(newBridgeEmitterPos, BRIDGE_DIRECTION, false);
    Game.add(bridgeEmitter);

    Point newBridgeLeverPos = new Point(5, 7);
    bridgeLever = LeverFactory.createLever(newBridgeLeverPos, new ICommand() {
      @Override public void execute() { LightBridgeFactory.activateBridge(bridgeEmitter); }
      @Override public void undo() { LightBridgeFactory.deactivateBridge(bridgeEmitter);}
    });
    Game.add(bridgeLever);

    Point newBridgeEmitterPos2 = new Point(2, 7);
    bridgeEmitter2 = LightBridgeFactory.createLightBridge(newBridgeEmitterPos2, BRIDGE_DIRECTION, false);
    Game.add(bridgeEmitter2);

    Point newBridgeLeverPos2 = new Point(3, 7);
    bridgeLever2 = LeverFactory.createLever(newBridgeLeverPos2, new ICommand() {
      @Override public void execute() { LightBridgeFactory.activateBridge(bridgeEmitter2); }
      @Override public void undo() { LightBridgeFactory.deactivateBridge(bridgeEmitter2);}
    });
    Game.add(bridgeLever2);

    Point wallEmitterPos = new Point(8, 7); // direkt rechts daneben (Abstand 2 Tiles)
    wallEmitter = LightWallFactory.createLightWall(wallEmitterPos, WALL_DIRECTION, false);
    Game.add(wallEmitter);

    Point wallLeverPos = new Point(9, 7);
    wallLever = LeverFactory.createLever(wallLeverPos, new ICommand() {
      @Override public void execute() { LightWallFactory.activateWall(wallEmitter); }
      @Override public void undo() { LightWallFactory.deactivateWall(wallEmitter); }
    });
    Game.add(wallLever);

    extendLever = LeverFactory.createLever(new Point(11, 7), new ICommand() {
      @Override public void execute() {
        PortalFactory.createPortal(new Point(8, 0), PortalColor.BLUE);
        PortalFactory.createPortal(new Point(1, 0), PortalColor.GREEN);
      }
      @Override public void undo() {
        PortalFactory.clearAllPortals();
      }
    });
    Game.add(extendLever);

    //PortalFactory.createPortal(new Point(8, 0), PortalColor.BLUE);
    //PortalFactory.createPortal(new Point(1, 0), PortalColor.GREEN);

    spawnChortFireballShooter(15,5);
    spawnChortFireballShooter(15,4);
    spawnChortFireballShooter(15,3);
  }

  private void spawnChortFireballShooter(int x, int y) {
    Entity shooter = new Entity("ChortFireballShooter");
    shooter.add(new PositionComponent(new Point(x, y), Direction.LEFT));
    DrawComponent dc = new DrawComponent(new SimpleIPath("character/monster/chort"));
    shooter.add(dc);
    shooter.add(new CollideComponent());
    shooter.add(new SkillComponent());
    // Größere Reichweite (z.B. 25 Felder) damit Projektil bis x=1 kommt
    FireballSkill fireball = new FireballSkill(() -> {
      return shooter.fetch(PositionComponent.class)
        .map(pc -> new Point(1, pc.position().y()))
        .orElse(new Point(1, y));
    }, 400, 25f, false); // overload: (target, cooldown, range, ignoreFirstWall)
    shooter.fetch(SkillComponent.class).ifPresent(sc -> sc.addSkill(fireball));
    Game.add(shooter);
    shooters.add(shooter);
    shooterLastShot.put(shooter, 0L);
  }

  @Override
  protected void onTick() {
    long now = System.currentTimeMillis();
    for (Entity shooter : shooters) {
      long last = shooterLastShot.getOrDefault(shooter, 0L);
      if (now - last >= CHORT_SHOOT_INTERVAL_MS) {
        shooter.fetch(SkillComponent.class)
          .flatMap(sc -> sc.activeSkill())
          .ifPresent(skill -> skill.execute(shooter));
        shooterLastShot.put(shooter, now);
      }
    }
  }
}
