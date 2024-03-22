package level.devlevel;

import components.TorchComponent;
import contrib.components.HealthComponent;
import contrib.components.InteractionComponent;
import contrib.components.InventoryComponent;
import contrib.entities.MiscFactory;
import contrib.item.HealthPotionType;
import contrib.item.concreteItem.ItemPotionHealth;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.ExitTile;
import core.level.elements.tile.PitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.components.MissingComponentException;
import entities.MonsterType;
import item.concreteItem.ItemPotionSpeedPotion;
import java.util.*;
import level.DevDungeonLevel;
import level.devlevel.riddleHandler.IllusionRiddleHandler;
import level.utils.ITickable;
import level.utils.Teleporter;
import systems.FogOfWarSystem;
import systems.TeleporterSystem;
import utils.EntityUtils;

/** The Illusion Riddle Level. TODO: Refactor this class */
public class IllusionRiddleLevel extends DevDungeonLevel implements ITickable {

  // Difficulty (Mob Types)
  public static final MonsterType[] MONSTER_TYPES =
      new MonsterType[] {MonsterType.DARK_GOO, MonsterType.SMALL_DARK_GOO, MonsterType.DOC};
  private static final MonsterType BOSS_TYPE = MonsterType.ILLUSION_BOSS;

  // Spawn Points / Locations
  private final List<DevDungeonRoom> rooms;
  private final Coordinate levelBossSpawn;
  private final Coordinate[][] secretPassages;
  private final Coordinate[] leverSpawns;

  private final IllusionRiddleHandler riddleHandler;
  private final int originalFogOfWarDistance = FogOfWarSystem.VIEW_DISTANCE;
  private final Coordinate[] chestSpawns;
  private DevDungeonRoom lastRoom = null;
  private boolean lastTorchState = false;

  public IllusionRiddleLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints);
    ((FogOfWarSystem) Game.systems().get(FogOfWarSystem.class)).active(true);
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
                new Coordinate[] {}));
    this.levelBossSpawn = this.customPoints().get(64);

    // Teleporters
    for (int i = 65; i < 127; i += 2) {
      TeleporterSystem.getInstance()
          .registerTeleporter(
              new Teleporter(this.customPoints().get(i), this.customPoints().get(i + 1)));
    }

    this.secretPassages =
        new Coordinate[][] {
          new Coordinate[] {this.customPoints().get(127), this.customPoints().get(128)},
          new Coordinate[] {this.customPoints().get(129), this.customPoints().get(130)},
          new Coordinate[] {this.customPoints().get(131), this.customPoints().get(132)}
        };
    this.leverSpawns =
        new Coordinate[] {
          this.customPoints().get(133), this.customPoints().get(134), this.customPoints().get(135),
        };

    this.chestSpawns = new Coordinate[] {this.customPoints().get(161)};
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

      // Setup TP Targets for TPBallSkill
      int[] roomIndices = {0, 1, 2, 3, 7};
      for (int index : roomIndices) {
        this.addTPTarget(
            this.rooms.get(index).tiles().stream()
                .filter(tile -> tile.levelElement() == LevelElement.FLOOR)
                .map(Tile::coordinate)
                .toArray(Coordinate[]::new));
      }

      // Open Pits for last room (boss room) and extinguish torches
      this.rooms.getLast().tiles().stream()
          .filter(tile -> tile.levelElement() == LevelElement.PIT)
          .map(tile -> (PitTile) tile)
          .forEach(PitTile::open);
      for (Entity torch : this.rooms.getLast().torches()) {
        torch
            .fetch(InteractionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(torch, InteractionComponent.class))
            .triggerInteraction(torch, Game.hero().orElse(null));
      }

      // Draw teleporter connections
      TeleporterSystem.getInstance().teleporter().stream()
          .map(Teleporter::from)
          .forEach((teleporter) -> this.tileAt(teleporter).tintColor(0x444444FF));
      TeleporterSystem.getInstance().teleporter().stream()
          .map(Teleporter::to)
          .forEach((teleporter) -> this.tileAt(teleporter).tintColor(0x444444FF));

      Entity boss =
          EntityUtils.spawnBoss(
              BOSS_TYPE,
              this.levelBossSpawn,
              (e) -> {
                ((FogOfWarSystem) Game.systems().get(FogOfWarSystem.class)).active(false);
                // turn of all torches on death
                DevDungeonRoom room = this.getCurrentRoom();
                if (room == null || room != this.rooms.getLast()) {
                  return; // should not happen, just if boss dies while not in boss room
                }
                this.lightTorch(room, 0, false);
                this.lightTorch(room, 1, false);
              });
      HealthComponent bossHc =
          boss.fetch(HealthComponent.class)
              .orElseThrow(() -> MissingComponentException.build(boss, HealthComponent.class));
      bossHc.onHit(
          (cause, damage) -> {
            int currentHealth = bossHc.currentHealthpoints() - damage.damageAmount();
            int maxHealth = bossHc.maximalHealthpoints();

            DevDungeonRoom room = this.getCurrentRoom();
            if (room == null || room != this.rooms.getLast()) {
              return;
            }

            double healthPercentage = (double) currentHealth / maxHealth;
            if (healthPercentage <= 0.5) {
              this.lightTorch(room, 0, true);
              this.lightTorch(room, 1, true);
            }
          });

      // Secret Passages
      EntityUtils.spawnLever(
          this.leverSpawns[0].toCenteredPoint(),
          (isOn, lever, who) -> {
            for (Tile tile :
                this.tilesInArea(this.secretPassages[0][0], this.secretPassages[0][1])) {
              this.changeTileElementType(tile, LevelElement.FLOOR);
            }
          });
      EntityUtils.spawnLever(
          this.leverSpawns[1].toCenteredPoint(),
          (isOn, lever, who) -> {
            for (Tile tile :
                this.tilesInArea(this.secretPassages[1][0], this.secretPassages[1][1])) {
              this.changeTileElementType(tile, LevelElement.FLOOR);
            }
          });
      EntityUtils.spawnLever(
          this.leverSpawns[2].toCenteredPoint(),
          (isOn, lever, who) -> {
            for (Tile tile :
                this.tilesInArea(this.secretPassages[2][0], this.secretPassages[2][1])) {
              this.changeTileElementType(tile, LevelElement.FLOOR);
            }
          });
      this.spawnChestsAndCauldrons();
    }

    if (this.lastRoom != this.getCurrentRoom()) {
      // Handle Mob AI (disable AI for mobs in other rooms, enable for mobs in current room)
      if (this.lastRoom != null) {
        this.lastRoom.mobAI(false);
      }
      this.lastRoom = this.getCurrentRoom();
      if (this.lastRoom != null) {
        this.lastRoom.mobAI(true);
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
   * Lights a specified torch in a given room.
   *
   * <p>This method retrieves the torch at the given index from the room's array of torches. It then
   * fetches the TorchComponent of the torch and sets its lit property to true.
   *
   * @param room The room in which the torch to be lit is located.
   * @param torchIndex The index of the torch to be lit in the room's array of torches.
   * @param lit The new state of the torch.
   * @throws IndexOutOfBoundsException if the torch index is out of bounds
   */
  private void lightTorch(DevDungeonRoom room, int torchIndex, boolean lit) {
    if (room.torches()[torchIndex]
            .fetch(TorchComponent.class)
            .orElseThrow(
                () ->
                    MissingComponentException.build(
                        room.torches()[torchIndex], TorchComponent.class))
            .lit()
        == lit) return;
    room.torches()[torchIndex]
        .fetch(InteractionComponent.class)
        .orElseThrow(
            () ->
                MissingComponentException.build(
                    room.torches()[torchIndex], InteractionComponent.class))
        .triggerInteraction(room.torches()[torchIndex], Game.hero().orElse(null));
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

  /**
   * Spawns the chests and cauldrons in the riddle room.
   *
   * @throws RuntimeException if any of the entities could not be created
   */
  private void spawnChestsAndCauldrons() {
    for (Coordinate chestSpawn : this.chestSpawns) {
      Entity chest;
      try {
        chest = MiscFactory.newChest(MiscFactory.FILL_CHEST.EMPTY);
      } catch (Exception e) {
        throw new RuntimeException("Failed to create chest entity at " + chestSpawn, e);
      }
      PositionComponent pc =
          chest
              .fetch(PositionComponent.class)
              .orElseThrow(() -> MissingComponentException.build(chest, PositionComponent.class));
      pc.position(chestSpawn.toCenteredPoint());
      InventoryComponent ic =
          chest
              .fetch(InventoryComponent.class)
              .orElseThrow(() -> MissingComponentException.build(chest, InventoryComponent.class));
      ic.add(new ItemPotionHealth(HealthPotionType.WEAK));
      ic.add(new ItemPotionSpeedPotion());

      Game.add(chest);
    }
  }
}
