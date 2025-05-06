package level.devlevel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import contrib.components.InventoryComponent;
import contrib.entities.MiscFactory;
import contrib.item.HealthPotionType;
import contrib.item.concreteItem.ItemPotionHealth;
import contrib.level.DevDungeonLevel;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.tile.PitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Tuple;
import core.utils.components.MissingComponentException;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import entities.MonsterType;
import hotload.DynamicCompiler;
import item.concreteItem.ItemPotionSpeed;
import item.effects.SpeedEffect;
import java.util.*;
import java.util.function.Supplier;
import level.devlevel.riddleHandler.DamagedBridgeRiddleHandler;
import utils.EntityUtils;

/** The Damaged Bridge Riddle Level. */
public class DamagedBridgeRiddleLevel extends DevDungeonLevel {

  private static final IPath SOURCE_FILE = new SimpleIPath("src/hotload/MySpeedEffect.java");
  private static final String CLASS_NAME = "hotload.MySpeedEffect";

  // Difficulty (Mob Count, Mob Types)
  private static final int MOB_COUNT = 7;
  private static final MonsterType[] MONSTER_TYPES =
      new MonsterType[] {MonsterType.ORC_WARRIOR, MonsterType.ORC_SHAMAN};
  private static final MonsterType BOSS_TYPE = MonsterType.CHORT;

  // Spawn Points / Locations
  private final Coordinate bridgeMobSpawn;
  private final Tile[] secretWay;
  private final Coordinate[] mobSpawns;
  private final Coordinate levelBossSpawn;

  private final DamagedBridgeRiddleHandler riddleHandler;

  /**
   * Constructs the Damaged Bridge Riddle Level.
   *
   * @param layout The layout of the level.
   * @param designLabel The design label of the level.
   * @param customPoints The custom points of the level.
   */
  public DamagedBridgeRiddleLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(
        layout,
        designLabel,
        customPoints,
        "The Damaged Bridge",
        "I heard that a powerful artifact is hidden nearby. Rumor says it's just beyond an old bridge. Let's see if we can find it.");
    this.riddleHandler = new DamagedBridgeRiddleHandler(customPoints, this);
    this.bridgeMobSpawn = customPoints.get(8);

    this.secretWay = Arrays.stream(getCoordinates(11, 17)).map(this::tileAt).toArray(Tile[]::new);
    this.mobSpawns = getCoordinates(18, customPoints().size() - 2);
    this.levelBossSpawn = customPoints().getLast();
  }

  @Override
  protected void onFirstTick() {
    prepareBridge();
    Game.hero().get().fetch(InventoryComponent.class).get().add(new ItemPotionSpeed());
    Game.hero().get().fetch(InventoryComponent.class).get().add(new ItemPotionSpeed());
    Game.hero().get().fetch(InventoryComponent.class).get().add(new ItemPotionSpeed());
    Game.hero().get().fetch(InventoryComponent.class).get().add(new ItemPotionSpeed());

    // Prepare the secret way
    for (int i = 0; i < secretWay.length - 1; i++) {
      PitTile pitTile = (PitTile) secretWay[i];
      pitTile.timeToOpen(15 * 1000);
    }

    // Spawn all entities and it's content
    spawnChestsAndCauldrons();

    EntityUtils.spawnMobs(MOB_COUNT, MONSTER_TYPES, mobSpawns);
    EntityUtils.spawnBoss(BOSS_TYPE, levelBossSpawn);
    riddleHandler.onFirstTick();
  }

  @Override
  public void onTick() {
    riddleHandler.onTick();

    // reload speed effect on button press
    if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
      Game.hero()
          .flatMap(h -> h.fetch(InventoryComponent.class))
          .map(ic -> ic.items(ItemPotionSpeed.class))
          .ifPresent(
              s ->
                  s.forEach(
                      i -> {
                        try {
                          ((ItemPotionSpeed) i)
                              .setSpeedEffectSupplier(
                                  (Supplier<SpeedEffect>)
                                      DynamicCompiler.loadUserInstance(SOURCE_FILE, CLASS_NAME));
                        } catch (Exception e) {
                          throw new RuntimeException(e);
                        }
                      }));
    }
    if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
      Game.hero()
          .flatMap(h -> h.fetch(InventoryComponent.class))
          .map(ic -> ic.items(ItemPotionSpeed.class))
          .ifPresent(
              s ->
                  s.forEach(
                      i -> {
                        try {
                          ((ItemPotionSpeed) i)
                              .setSpeedEffectSupplier(
                                  (Supplier<SpeedEffect>)
                                      DynamicCompiler.loadUserInstance(
                                          SOURCE_FILE,
                                          CLASS_NAME,
                                          new Tuple<>(String.class, "Hello World"),
                                          new Tuple<>(int.class, 3)));
                        } catch (Exception e) {
                          throw new RuntimeException(e);
                        }
                      }));
    }
  }

  private void prepareBridge() {
    EntityUtils.spawnMonster(MonsterType.BRIDGE_MOB, bridgeMobSpawn);
    List<PitTile> bridge =
        pitTiles().stream().filter(pit -> pit.coordinate().y == bridgeMobSpawn.y).toList();
    int timeToOpen = 500;
    for (PitTile pitTile : bridge) {
      pitTile.timeToOpen(timeToOpen);
      if (timeToOpen >= 300) { // force after 3 pits to be 50
        timeToOpen -= 100;
      } else {
        timeToOpen = 50;
      }
    }
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
    pc.position(secretWay[secretWay.length - 1].coordinate().toCenteredPoint());
    InventoryComponent ic =
        chest
            .fetch(InventoryComponent.class)
            .orElseThrow(() -> MissingComponentException.build(chest, InventoryComponent.class));
    ic.add(new ItemPotionHealth(HealthPotionType.GREATER));

    Game.add(chest);
  }
}
