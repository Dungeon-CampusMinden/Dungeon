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
  private final Coordinate levelBossSpawn;
  private final Coordinate[][] secretPassages;
  private final Coordinate[] leverSpawns;

  private final IllusionRiddleHandler riddleHandler;
  private final int originalFogOfWarDistance = FogOfWarSystem.currentViewDistance();
  private final Coordinate[] chestSpawns;
  private DevDungeonRoom lastRoom = null;
  private boolean lastTorchState = false;
  private final TeleporterSystem teleporterSystem;

  /**
   * Creates the Illusion Riddle Level.
   *
   * @param layout The layout of the level.
   * @param designLabel The design label of the level.
   * @param customPoints The custom points of the level.
   */
  public IllusionRiddleLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(
        layout,
        designLabel,
        customPoints,
        "The Illusion Riddle",
        "Wait, who turned off the lights? Try to find a way out of this dark place.");

    ((FogOfWarSystem) Game.systems().get(FogOfWarSystem.class)).active(true);
    this.riddleHandler = new IllusionRiddleHandler(customPoints, this);
    this.teleporterSystem = (TeleporterSystem) Game.systems().get(TeleporterSystem.class);

    this.rooms =
        List.of(
            new DevDungeonRoom(
                customPoints().get(0), // TopLeft
                customPoints().get(1), // BottomRight
                new Coordinate[] {}, // Torch Spawns
                new Coordinate[] {} // Mob Spawns
                ),
            new DevDungeonRoom(
                customPoints().get(2),
                customPoints().get(3),
                new Coordinate[] {customPoints().get(4)},
                new Coordinate[] {customPoints().get(5), customPoints().get(6)}),
            new DevDungeonRoom(
                customPoints().get(7),
                customPoints().get(8),
                new Coordinate[] {customPoints().get(9)}),
            new DevDungeonRoom(
                customPoints().get(10),
                customPoints().get(11),
                new Coordinate[] {customPoints().get(12), customPoints().get(13)}),
            new DevDungeonRoom(
                customPoints().get(14),
                customPoints().get(15),
                new Coordinate[] {customPoints().get(16)},
                new Coordinate[] {}),
            new DevDungeonRoom(
                customPoints().get(17),
                customPoints().get(18),
                new Coordinate[] {customPoints().get(19)},
                new Coordinate[] {}),
            new DevDungeonRoom(
                customPoints().get(20),
                customPoints().get(21),
                new Coordinate[] {customPoints().get(22), customPoints().get(23)},
                new Coordinate[] {customPoints().get(24)}),
            new DevDungeonRoom(
                customPoints().get(25),
                customPoints().get(26),
                new Coordinate[] {customPoints().get(27)},
                getCoordinates(28, 32)),
            new DevDungeonRoom(
                customPoints().get(33),
                customPoints().get(34),
                new Coordinate[] {customPoints().get(35), customPoints().get(36)}),
            new DevDungeonRoom(
                customPoints().get(37),
                customPoints().get(38),
                new Coordinate[] {customPoints().get(39), customPoints().get(40)},
                getCoordinates(41, 46)),
            new DevDungeonRoom(
                customPoints().get(47),
                customPoints().get(48),
                new Coordinate[] {customPoints().get(49)}),
            new DevDungeonRoom(
                customPoints().get(50),
                customPoints().get(51),
                new Coordinate[] {customPoints().get(52), customPoints().get(53)}),
            new DevDungeonRoom(
                customPoints().get(54),
                customPoints().get(55),
                new Coordinate[] {customPoints().get(56)},
                getCoordinates(57, 59)),
            new DevDungeonRoom(
                customPoints().get(60),
                customPoints().get(61),
                new Coordinate[] {customPoints().get(62), customPoints().get(63)},
                new Coordinate[] {}));
    this.levelBossSpawn = customPoints().get(64);

    this.secretPassages =
        new Coordinate[][] {
          new Coordinate[] {customPoints().get(127), customPoints().get(128)},
          new Coordinate[] {customPoints().get(129), customPoints().get(130)},
          new Coordinate[] {customPoints().get(131), customPoints().get(132)}
        };
    this.leverSpawns = getCoordinates(133, 135);

    this.chestSpawns = new Coordinate[] {customPoints().get(161)};
  }

  @Override
  protected void onFirstTick() {
    rooms.forEach(DevDungeonRoom::spawnEntities);

    // Create teleporters
    for (int i = 65; i < 127; i += 2) {
      teleporterSystem.registerTeleporter(
          new Teleporter(customPoints().get(i), customPoints().get(i + 1)));
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
          .triggerInteraction(torch, Game.hero().orElse(null));
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
            levelBossSpawn,
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
        leverSpawns[0].toPoint(),
        new OpenPassageCommand(secretPassages[0][0], secretPassages[0][1]));
    EntityUtils.spawnLever(
        leverSpawns[1].toPoint(),
        new OpenPassageCommand(secretPassages[1][0], secretPassages[1][1]));
    EntityUtils.spawnLever(
        leverSpawns[2].toPoint(),
        new OpenPassageCommand(secretPassages[2][0], secretPassages[2][1]));
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
        .triggerInteraction(r.torches()[i], Game.hero().orElse(null));
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
            heroPc -> rooms.stream().filter(room -> room.contains(heroPc.coordinate())).findFirst())
        .orElse(null);
  }

  /**
   * Spawns the chests and cauldrons in the riddle room.
   *
   * @throws RuntimeException if any of the entities could not be created
   */
  private void spawnChestsAndCauldrons() {
    for (Coordinate chestSpawnTileCoordinate : chestSpawns) {
      Entity newIllusionRiddleLevelChestEntity;
      try {
        newIllusionRiddleLevelChestEntity = MiscFactory.newChest(MiscFactory.FILL_CHEST.EMPTY);
      } catch (Exception e) {
        throw new RuntimeException(
            "Failed to create chest entity at " + chestSpawnTileCoordinate, e);
      }
      PositionComponent pc =
          newIllusionRiddleLevelChestEntity
              .fetch(PositionComponent.class)
              .orElseThrow(
                  () ->
                      MissingComponentException.build(
                          newIllusionRiddleLevelChestEntity, PositionComponent.class));
      pc.position(chestSpawnTileCoordinate.toPoint());
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
