package level.devlevel;

import contrib.components.InventoryComponent;
import contrib.entities.MiscFactory;
import contrib.item.HealthPotionType;
import contrib.item.concreteItem.ItemPotionHealth;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.ExitTile;
import core.level.elements.tile.PitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import entities.MonsterType;
import java.util.*;
import level.DevDungeonLevel;
import level.devlevel.riddleHandler.IllusionRiddleHandler;
import level.utils.ITickable;
import level.utils.Teleporter;
import systems.FogOfWarSystem;
import systems.TeleporterSystem;
import utils.EntityUtils;

/** The Illusion Riddle Level. */
public class IllusionRiddleLevel extends DevDungeonLevel implements ITickable {

  // Difficulty (Mob Types)
  public static final MonsterType[] MONSTER_TYPES =
      new MonsterType[] {MonsterType.ORC_WARRIOR, MonsterType.ORC_SHAMAN};
  private static final MonsterType BOSS_TYPE = MonsterType.CHORT;

  // Spawn Points / Locations
  private final List<DevDungeonRoom> rooms;
  private final Coordinate[][] secretPassages;
  private final Coordinate levelBossSpawn;
  private final Coordinate[] mobSpawns;

  private final IllusionRiddleHandler riddleHandler;
  private final int originalFogOfWarDistance = FogOfWarSystem.VIEW_DISTANCE;
  private DevDungeonRoom lastRoom = null;
  private boolean lastTorchState = false;

  public IllusionRiddleLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints);
    // ((FogOfWarSystem) Game.systems().get(FogOfWarSystem.class)).active(true);
    this.riddleHandler = new IllusionRiddleHandler(customPoints, this);

    this.rooms =
        List.of(
            new DevDungeonRoom(
                this.customPoints().get(0), // TopLeft
                this.customPoints().get(1), // BottomRight
                new Coordinate[] {}, // Torch Spawns
                new Coordinate[] {} // Mob Spawns
                ),
            new DevDungeonRoom(
                this.customPoints().get(2),
                this.customPoints().get(3),
                new Coordinate[] {this.customPoints().get(4)},
                new Coordinate[] {this.customPoints().get(5), this.customPoints().get(6)}),
            new DevDungeonRoom(
                this.customPoints().get(7),
                this.customPoints().get(8),
                new Coordinate[] {},
                new Coordinate[] {this.customPoints().get(9)}),
            new DevDungeonRoom(
                this.customPoints().get(10),
                this.customPoints().get(11),
                new Coordinate[] {},
                new Coordinate[] {this.customPoints().get(12), this.customPoints().get(13)}),
            new DevDungeonRoom(
                this.customPoints().get(14),
                this.customPoints().get(15),
                new Coordinate[] {this.customPoints().get(16)},
                new Coordinate[] {}),
            new DevDungeonRoom(
                this.customPoints().get(17),
                this.customPoints().get(18),
                new Coordinate[] {this.customPoints().get(19)},
                new Coordinate[] {}),
            new DevDungeonRoom(
                this.customPoints().get(20),
                this.customPoints().get(21),
                new Coordinate[] {this.customPoints().get(22), this.customPoints().get(23)},
                new Coordinate[] {this.customPoints().get(24)}),
            new DevDungeonRoom(
                this.customPoints().get(25),
                this.customPoints().get(26),
                new Coordinate[] {this.customPoints().get(27)},
                new Coordinate[] {
                  this.customPoints().get(28),
                  this.customPoints().get(29),
                  this.customPoints().get(30),
                  this.customPoints().get(31),
                  this.customPoints().get(32)
                }),
            new DevDungeonRoom(
                this.customPoints().get(33),
                this.customPoints().get(34),
                new Coordinate[] {},
                new Coordinate[] {this.customPoints().get(35), this.customPoints().get(36)}),
            new DevDungeonRoom(
                this.customPoints().get(37),
                this.customPoints().get(38),
                new Coordinate[] {this.customPoints().get(39), this.customPoints().get(40)},
                new Coordinate[] {
                  this.customPoints().get(41),
                  this.customPoints().get(42),
                  this.customPoints().get(43),
                  this.customPoints().get(44),
                  this.customPoints().get(45),
                  this.customPoints().get(46)
                }),
            new DevDungeonRoom(
                this.customPoints().get(47),
                this.customPoints().get(48),
                new Coordinate[] {},
                new Coordinate[] {this.customPoints().get(49)}),
            new DevDungeonRoom(
                this.customPoints().get(50),
                this.customPoints().get(51),
                new Coordinate[] {},
                new Coordinate[] {this.customPoints().get(52), this.customPoints().get(53)}),
            new DevDungeonRoom(
                this.customPoints().get(54),
                this.customPoints().get(55),
                new Coordinate[] {this.customPoints().get(56)},
                new Coordinate[] {
                  this.customPoints().get(57),
                  this.customPoints().get(58),
                  this.customPoints().get(59)
                }),
            new DevDungeonRoom(
                this.customPoints().get(60),
                this.customPoints().get(61),
                new Coordinate[] {this.customPoints().get(62), this.customPoints().get(63)},
                new Coordinate[] {this.customPoints().get(64)}));
    for (int i = 65; i < 127; i += 2) {
      TeleporterSystem.getInstance()
          .registerTeleporter(
              new Teleporter(this.customPoints().get(i), this.customPoints().get(i + 1)));
    }

    this.secretPassages =
        new Coordinate[][] {
          new Coordinate[] {new Coordinate(0, 0), new Coordinate(0, 0)},
          new Coordinate[] {new Coordinate(0, 0), new Coordinate(0, 0)}
        };
    this.levelBossSpawn = new Coordinate(0, 0);
    this.mobSpawns = new Coordinate[0];
  }

  @Override
  public void onTick(boolean isFirstTick) {
    if (isFirstTick) {
      ((ExitTile) this.endTile()).close(); // close exit at start (to force defeating the boss)
      this.doorTiles().forEach(DoorTile::close);
      this.pitTiles()
          .forEach(
              pit -> {
                pit.timeToOpen(50);
                pit.close();
              });
      this.rooms.forEach(DevDungeonRoom::spawnEntities);

      // Draw teleporter connections
      TeleporterSystem.getInstance().teleporter().stream()
          .map(Teleporter::from)
          .forEach((teleporter) -> this.tileAt(teleporter).tintColor(0x00FF00FF));
      TeleporterSystem.getInstance().teleporter().stream()
          .map(Teleporter::to)
          .forEach((teleporter) -> this.tileAt(teleporter).tintColor(0xFF0000FF));

      this.spawnChestsAndCauldrons();
    }

    if (this.lastRoom != this.getCurrentRoom()) {
      System.out.println("Room changed!");

      // Handle Mob AI (disable AI for mobs in other rooms, enable for mobs in current room)
      if (this.lastRoom != null) {
        this.lastRoom.mobAI(false);
      }
      this.lastRoom = this.getCurrentRoom();
      if (this.lastRoom != null) {
        this.lastRoom.mobAI(true);
      }

      // Open Pits for last room (boss room)
      if (this.rooms.getLast().equals(this.lastRoom)) {
        this.lastRoom.tiles().stream()
            .filter(tile -> tile.levelElement() == LevelElement.PIT)
            .map(tile -> (PitTile) tile)
            .forEach(PitTile::open);
      }
    }

    // Anti Torch Logic
    if (this.lastRoom != null && this.lastTorchState != this.lastRoom.isAnyTorchActive()) {
      this.lastTorchState = this.lastRoom.isAnyTorchActive();
      if (this.lastRoom.isAnyTorchActive()) {
        FogOfWarSystem.VIEW_DISTANCE = 3;
        ((FogOfWarSystem) Game.systems().get(FogOfWarSystem.class)).revert();
      } else {
        FogOfWarSystem.VIEW_DISTANCE = this.originalFogOfWarDistance;
        // no revert, is needed as the fog of war should only increase
        // revert is only needed if the fog of war decreases in distance
      }
    }

    this.riddleHandler.onTick(isFirstTick);
  }

  /**
   * Returns the current room the hero is in.
   *
   * @return The current room if the hero is present and in a room, null otherwise.
   */
  private DevDungeonRoom getCurrentRoom() {
    return Game.hero()
        .flatMap(hero -> hero.fetch(PositionComponent.class))
        .flatMap(
            heroPc ->
                this.rooms.stream()
                    .filter(room -> room.contains(heroPc.position().toCoordinate()))
                    .findFirst())
        .orElse(null);
  }

  private void offsetHero(Coordinate offset) {
    Entity hero = Game.hero().orElse(null);
    if (hero == null) {
      return;
    }
    PositionComponent heroPc =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));

    Point newPoint = new Point(heroPc.position().x + offset.x, heroPc.position().y + offset.y);

    EntityUtils.teleportHeroTo(newPoint);
  }

  /**
   * Spawns the chests and cauldrons in the riddle room.
   *
   * @throws RuntimeException if any of the entities could not be created
   */
  private void spawnChestsAndCauldrons() {
    Entity chest;
    try {
      chest = MiscFactory.newChest(MiscFactory.FILL_CHEST.EMPTY);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create speed potion chest");
    }
    PositionComponent pc =
        chest
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(chest, PositionComponent.class));
    // pc.position();
    InventoryComponent ic =
        chest
            .fetch(InventoryComponent.class)
            .orElseThrow(() -> MissingComponentException.build(chest, InventoryComponent.class));
    ic.add(new ItemPotionHealth(HealthPotionType.GREATER));

    Game.add(chest);
  }
}
