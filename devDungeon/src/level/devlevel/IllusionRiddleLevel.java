package level.devlevel;

import components.TorchComponent;
import contrib.components.AIComponent;
import contrib.components.HealthComponent;
import contrib.components.InteractionComponent;
import contrib.components.InventoryComponent;
import contrib.entities.MiscFactory;
import contrib.entities.deco.Deco;
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
import core.utils.Tuple;
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
          getPoint("Point0"), // TopLeft
          getPoint("Point1"), // BottomRight
          new Point[] {}, // Torch Spawns
          new Point[] {} // Mob Spawns
        ),
        new DevDungeonRoom(
          getPoint("Point2"),
          getPoint("Point3"),
          new Point[] { getPoint("Point4") },
          new Point[] { getPoint("Point5"), getPoint("Point6") }),
        new DevDungeonRoom(
          getPoint("Point7"),
          getPoint("Point8"),
          new Point[] { getPoint("Point9") }),
        new DevDungeonRoom(
          getPoint("Point10"),
          getPoint("Point11"),
          new Point[] { getPoint("Point12"), getPoint("Point13") }),
        new DevDungeonRoom(
          getPoint("Point14"),
          getPoint("Point15"),
          new Point[] { getPoint("Point16") },
          new Point[] {}),
        new DevDungeonRoom(
          getPoint("Point17"),
          getPoint("Point18"),
          new Point[] { getPoint("Point19") },
          new Point[] {}),
        new DevDungeonRoom(
          getPoint("Point20"),
          getPoint("Point21"),
          new Point[] { getPoint("Point22"), getPoint("Point23") },
          new Point[] { getPoint("Point24") }),
        new DevDungeonRoom(
          getPoint("Point25"),
          getPoint("Point26"),
          new Point[] { getPoint("Point27") },
          getPoints("Point", 28, 32)),
        new DevDungeonRoom(
          getPoint("Point33"),
          getPoint("Point34"),
          new Point[] { getPoint("Point35"), getPoint("Point36") }),
        new DevDungeonRoom(
          getPoint("Point37"),
          getPoint("Point38"),
          new Point[] { getPoint("Point39"), getPoint("Point40") },
          getPoints("Point", 41, 46)),
        new DevDungeonRoom(
          getPoint("Point47"),
          getPoint("Point48"),
          new Point[] { getPoint("Point49") }),
        new DevDungeonRoom(
          getPoint("Point50"),
          getPoint("Point51"),
          new Point[] { getPoint("Point52"), getPoint("Point53") }),
        new DevDungeonRoom(
          getPoint("Point54"),
          getPoint("Point55"),
          new Point[] { getPoint("Point56") },
          getPoints("Point", 57, 59)),
        new DevDungeonRoom(
          getPoint("Point60"),
          getPoint("Point61"),
          new Point[] { getPoint("Point62"), getPoint("Point63") },
          new Point[] {}));
    this.levelBossSpawn = getPoint("Point64");

    this.secretPassages =
      new Point[][] {
        new Point[] { getPoint("Point127"), getPoint("Point128") },
        new Point[] { getPoint("Point129"), getPoint("Point130") },
        new Point[] { getPoint("Point131"), getPoint("Point132") }
      };
    this.leverSpawns = getPoints("Point", 133, 135);

    this.chestSpawns = new Point[] { getPoint("Point161") };
  }

  @Override
  protected void onFirstTick() {
    rooms.forEach(DevDungeonRoom::spawnEntities);

    // Create teleporters
    for (int i = 65; i < 127; i += 2) {
      teleporterSystem.registerTeleporter(
          new Teleporter(getPoint("Point" + i), getPoint("Point" + (i + 1))));
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
        new OpenPassageCommand(secretPassages[0][0].toCoordinate(), secretPassages[0][1].toCoordinate()));
    EntityUtils.spawnLever(
        leverSpawns[1],
        new OpenPassageCommand(secretPassages[1][0].toCoordinate(), secretPassages[1][1].toCoordinate()));
    EntityUtils.spawnLever(
        leverSpawns[2],
        new OpenPassageCommand(secretPassages[2][0].toCoordinate(), secretPassages[2][1].toCoordinate()));
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
        throw new RuntimeException(
            "Failed to create chest entity at " + chestSpawnPoint, e);
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
