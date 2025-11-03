package level.devlevel;

import components.TorchComponent;
import contrib.components.AIComponent;
import contrib.components.HealthComponent;
import contrib.components.InteractionComponent;
import contrib.components.InventoryComponent;
import contrib.entities.MiscFactory;
import contrib.item.HealthPotionType;
import contrib.item.concreteItem.ItemPotionHealth;
import contrib.utils.EntityUtils;
import contrib.utils.components.ai.fight.AIRangeBehaviour;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.tile.PitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import entities.DevDungeonMonster;
import entities.levercommands.OpenPassageCommand;
import item.concreteItem.ItemPotionSpeed;
import java.util.*;
import java.util.function.Consumer;
import level.DevDungeonLevel;
import level.devlevel.riddleHandler.IllusionRiddleHandler;
import level.utils.Teleporter;
import systems.FogOfWarSystem;
import systems.TeleporterSystem;

/** The Illusion Riddle Level. TODO: Refactor this class */
public class IllusionRiddleLevel extends DevDungeonLevel {

  /** The types of monsters that can spawn in this level. */
  public static final DevDungeonMonster[] MONSTER_TYPES =
      new DevDungeonMonster[] {
        DevDungeonMonster.DARK_GOO,
        DevDungeonMonster.DARK_GOO, // 2/5 chance
        DevDungeonMonster.SMALL_DARK_GOO,
        DevDungeonMonster.SMALL_DARK_GOO, // 2/5 chance
        DevDungeonMonster.DOC // 1/5 chance
      };

  private static final DevDungeonMonster BOSS_TYPE = DevDungeonMonster.ILLUSION_BOSS;

  // Spawn Points / Locations
  private final List<DevDungeonRoom> rooms;
  private final Point levelBossSpawn;
  private final Point[][] secretPassages;
  private final Point[] leverSpawns;

  private final IllusionRiddleHandler riddleHandler;
  private final int originalFogOfWarDistance = FogOfWarSystem.currentViewDistance();
  private final Point[] chestSpawns;
  private DevDungeonRoom lastRoom = null;
  private boolean lastTorchState = false;
  private final TeleporterSystem teleporterSystem;

  /**
   * Creates the Illusion Riddle Level.
   *
   * @param layout The layout of the level.
   * @param designLabel The design label of the level.
   * @param namedPoints The custom points of the level.
   */
  public IllusionRiddleLevel(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(
        layout,
        designLabel,
        namedPoints,
        "The Illusion Riddle",
        "Wait, who turned off the lights? Try to find a way out of this dark place.");

    ((FogOfWarSystem) Game.systems().get(FogOfWarSystem.class)).active(true);
    this.riddleHandler = new IllusionRiddleHandler(namedPoints, this);
    this.teleporterSystem = (TeleporterSystem) Game.systems().get(TeleporterSystem.class);

    this.rooms =
        List.of(
            new DevDungeonRoom(
                getPoint(0), // TopLeft
                getPoint(1), // BottomRight
                new Point[] {}, // Torch Spawns
                new Point[] {} // Mob Spawns
                ),
            new DevDungeonRoom(
                getPoint(2),
                getPoint(3),
                new Point[] {getPoint(4)},
                new Point[] {getPoint(5), getPoint(6)}),
            new DevDungeonRoom(getPoint(7), getPoint(8), new Point[] {getPoint(9)}),
            new DevDungeonRoom(
                getPoint(10), getPoint(11), new Point[] {getPoint(12), getPoint(13)}),
            new DevDungeonRoom(
                getPoint(14), getPoint(15), new Point[] {getPoint(16)}, new Point[] {}),
            new DevDungeonRoom(
                getPoint(17), getPoint(18), new Point[] {getPoint(19)}, new Point[] {}),
            new DevDungeonRoom(
                getPoint(20),
                getPoint(21),
                new Point[] {getPoint(22), getPoint(23)},
                new Point[] {getPoint(24)}),
            new DevDungeonRoom(
                getPoint(25), getPoint(26), new Point[] {getPoint(27)}, getPoints(28, 32)),
            new DevDungeonRoom(
                getPoint(33), getPoint(34), new Point[] {getPoint(35), getPoint(36)}),
            new DevDungeonRoom(
                getPoint(37),
                getPoint(38),
                new Point[] {getPoint(39), getPoint(40)},
                getPoints(41, 46)),
            new DevDungeonRoom(getPoint(47), getPoint(48), new Point[] {getPoint(49)}),
            new DevDungeonRoom(
                getPoint(50), getPoint(51), new Point[] {getPoint(52), getPoint(53)}),
            new DevDungeonRoom(
                getPoint(54), getPoint(55), new Point[] {getPoint(56)}, getPoints(57, 59)),
            new DevDungeonRoom(
                getPoint(60),
                getPoint(61),
                new Point[] {getPoint(62), getPoint(63)},
                new Point[] {}));
    this.levelBossSpawn = getPoint(64);

    this.secretPassages =
        new Point[][] {
          new Point[] {getPoint(127), getPoint(128)},
          new Point[] {getPoint(129), getPoint(130)},
          new Point[] {getPoint(131), getPoint(132)}
        };
    this.leverSpawns = getPoints(133, 135);

    this.chestSpawns = new Point[] {getPoint(161)};
  }

  @Override
  protected void onFirstTick() {
    rooms.forEach(DevDungeonRoom::spawnEntities);

    // Create teleporters
    for (int i = 65; i < 127; i += 2) {
      teleporterSystem.registerTeleporter(new Teleporter(getPoint(i), getPoint(i + 1)));
    }

    // Setup TP Targets for TPBallSkill
    int[] roomIndices = {0, 1, 2, 3, 7};
    for (int ri : roomIndices) {
      addTPTarget(
          rooms.get(ri).tiles().stream()
              .filter(tile -> tile.levelElement() == LevelElement.FLOOR)
              .map(Tile::coordinate)
              .toArray(Coordinate[]::new));
    }

    // Open Pits for last room (boss room) and extinguish torches
    rooms.getLast().tiles().stream()
        .filter(t -> t.levelElement() == LevelElement.PIT)
        .map(t -> (PitTile) t)
        .forEach(PitTile::open);
    for (Entity torch : rooms.getLast().torches()) {
      torch
          .fetch(InteractionComponent.class)
          .orElseThrow(() -> MissingComponentException.build(torch, InteractionComponent.class))
          .triggerInteraction(torch, Game.player().orElse(null));
    }

    // Draw teleporter connections
    teleporterSystem.teleporter().stream()
        .map(Teleporter::from)
        .forEach(tp -> tileAt(tp).ifPresent(t -> t.tintColor(0x444444FF)));
    teleporterSystem.teleporter().stream()
        .map(Teleporter::to)
        .forEach(tp -> tileAt(tp).ifPresent(t -> t.tintColor(0x444444FF)));

    Entity b =
        utils.EntityUtils.spawnBoss(
            BOSS_TYPE,
            levelBossSpawn.toCoordinate(),
            (e) -> {
              ((FogOfWarSystem) Game.systems().get(FogOfWarSystem.class)).active(false);
              // turn of all torches on death
              DevDungeonRoom devDungeonRoom = getCurrentRoom();
              if (devDungeonRoom == null || devDungeonRoom != rooms.getLast()) {
                return; // should not happen, just if boss dies while not in boss room
              }
              lightTorch(devDungeonRoom, 0, false);
              lightTorch(devDungeonRoom, 1, false);

              exitTiles().forEach(tile -> tile.tintColor(-1)); // Workaround due to FogOfWar
            });
    HealthComponent bhc =
        b.fetch(HealthComponent.class)
            .orElseThrow(() -> MissingComponentException.build(b, HealthComponent.class));
    bhc.onHit(
        (cause, dmg) -> {
          int currentHealth = bhc.currentHealthpoints() - dmg.damageAmount();
          int maxHealth = bhc.maximalHealthpoints();

          DevDungeonRoom devDungeonRoom = getCurrentRoom();
          if (devDungeonRoom == null || devDungeonRoom != rooms.getLast()) {
            return;
          }

          double healthPercentage = (double) currentHealth / maxHealth;
          if (healthPercentage <= 0.5) {
            lightTorch(devDungeonRoom, 0, true);
            lightTorch(devDungeonRoom, 1, true);
          }
        });

    // Secret Passages
    EntityUtils.spawnLever(
        leverSpawns[0],
        new OpenPassageCommand(
            secretPassages[0][0].toCoordinate(), secretPassages[0][1].toCoordinate()));
    EntityUtils.spawnLever(
        leverSpawns[1],
        new OpenPassageCommand(
            secretPassages[1][0].toCoordinate(), secretPassages[1][1].toCoordinate()));
    EntityUtils.spawnLever(
        leverSpawns[2],
        new OpenPassageCommand(
            secretPassages[2][0].toCoordinate(), secretPassages[2][1].toCoordinate()));
    spawnChestsAndCauldrons();
    riddleHandler.onFirstTick();
  }

  @Override
  public void onTick() {
    if (lastRoom != getCurrentRoom()) {
      // Handle Mob AI (disable AI for mobs in other rooms, enable for mobs in current room)
      if (lastRoom != null) {
        lastRoom.mobAI(false);
      }
      if (getCurrentRoom() != null) {
        getCurrentRoom().mobAI(true);
      }

      if (getCurrentRoom() != null) {
        for (Entity mob : getCurrentRoom().mobs()) {
          Consumer<Entity> fightAI =
              mob.fetch(AIComponent.class)
                  .orElseThrow(() -> MissingComponentException.build(mob, AIComponent.class))
                  .fightBehavior();
          if (fightAI instanceof AIRangeBehaviour AIRangeBehaviour) {
            AIRangeBehaviour.skill().setLastUsedToNow();
          }
        }
      }

      this.lastRoom = getCurrentRoom();
    }

    // Anti Torch Logic
    if (lastRoom != null && lastTorchState != lastRoom.isAnyTorchActive()) {
      this.lastTorchState = lastRoom.isAnyTorchActive();
      if (lastRoom.isAnyTorchActive()) {
        FogOfWarSystem.currentViewDistance(3);
        ((FogOfWarSystem) Game.systems().get(FogOfWarSystem.class)).revert();
      } else {
        FogOfWarSystem.currentViewDistance(originalFogOfWarDistance);
        // no revert, is needed as the fog of war should only increase
        // revert is only needed if the fog of war decreases in distance
      }
    }

    riddleHandler.onTick();
  }

  /**
   * TODO: Refactor this method, and add JavaDoc.
   *
   * @param r Foo
   * @param i Foo
   * @param lit Foo
   */
  private void lightTorch(DevDungeonRoom r, int i, boolean lit) {
    if (r.torches()[i]
            .fetch(TorchComponent.class)
            .orElseThrow(
                () -> MissingComponentException.build(r.torches()[i], TorchComponent.class))
            .lit()
        == lit) return;
    r.torches()[i]
        .fetch(InteractionComponent.class)
        .orElseThrow(
            () -> MissingComponentException.build(r.torches()[i], InteractionComponent.class))
        .triggerInteraction(r.torches()[i], Game.player().orElse(null));
  }

  /**
   * Returns the current room the player is in.
   *
   * @return The current room if the player is present and in a room, null otherwise.
   */
  private DevDungeonRoom getCurrentRoom() {
    return Game.player()
        .flatMap(hero -> hero.fetch(PositionComponent.class))
        .flatMap(
            heroPc -> rooms.stream().filter(room -> room.contains(heroPc.position())).findFirst())
        .orElse(null);
  }

  /**
   * Spawns the chests and cauldrons in the riddle room.
   *
   * @throws RuntimeException if any of the entities could not be created
   */
  private void spawnChestsAndCauldrons() {
    for (Point chestSpawnPoint : chestSpawns) {
      Entity newIllusionRiddleLevelChestEntity;
      try {
        newIllusionRiddleLevelChestEntity = MiscFactory.newChest(MiscFactory.FILL_CHEST.EMPTY);
      } catch (Exception e) {
        throw new RuntimeException("Failed to create chest entity at " + chestSpawnPoint, e);
      }
      PositionComponent pc =
          newIllusionRiddleLevelChestEntity
              .fetch(PositionComponent.class)
              .orElseThrow(
                  () ->
                      MissingComponentException.build(
                          newIllusionRiddleLevelChestEntity, PositionComponent.class));
      pc.position(chestSpawnPoint);
      InventoryComponent ic =
          newIllusionRiddleLevelChestEntity
              .fetch(InventoryComponent.class)
              .orElseThrow(
                  () ->
                      MissingComponentException.build(
                          newIllusionRiddleLevelChestEntity, InventoryComponent.class));
      ic.add(new ItemPotionHealth(HealthPotionType.WEAK));
      ic.add(new ItemPotionSpeed());

      Game.add(newIllusionRiddleLevelChestEntity);
    }
  }
}
