package level.devlevel.riddleHandler;

import components.MagicShieldComponent;
import contrib.components.HealthComponent;
import contrib.components.InteractionComponent;
import contrib.components.InventoryComponent;
import contrib.entities.MiscFactory;
import contrib.hud.DialogUtils;
import contrib.hud.dialogs.OkDialog;
import contrib.item.HealthPotionType;
import contrib.item.concreteItem.ItemPotionHealth;
import contrib.systems.HealthSystem;
import contrib.utils.EntityUtils;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.health.IHealthObserver;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.level.DungeonLevel;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.FloorTile;
import core.level.elements.tile.PitTile;
import core.level.elements.tile.WallTile;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import core.level.utils.LevelUtils;
import core.utils.IVoidFunction;
import core.utils.components.MissingComponentException;
import entities.levercommands.BridgeControlCommand;
import item.concreteItem.ItemPotionAttackSpeed;
import java.util.ArrayList;
import java.util.List;
import systems.DevHealthSystem;
import task.game.hud.QuizUI;
import task.game.hud.UIAnswerCallback;
import task.tasktype.Quiz;
import task.tasktype.quizquestion.SingleChoice;
import utils.RegexRiddle;

/**
 * The BridgeGuardRiddleHandler class is used to handle the riddles and entities in the Bridge Guard
 * Riddle Level. The Bridge Guard Riddle Level contains a bridge guard that asks riddles to the
 * player. The player must answer the riddles correctly to pass the bridge guard and receive a
 * reward.
 */
public class BridgeGuardRiddleHandler implements IHealthObserver {
  private final DungeonLevel level;

  // Spawn Points / Locations
  private final Coordinate[] bridgeBounds;
  private final Coordinate[] bridgePitsBounds;
  private final Coordinate bridgeLever;
  private final Coordinate bridgeLeverSign;
  private final Coordinate bridgeGuardSpawn;
  private final Coordinate riddleRoomEntrance;
  private final Coordinate[] riddleRoomBounds;
  private final Coordinate riddleRoomChest;
  private final Coordinate riddleRewardSpawn;
  private final Coordinate riddleRoomExit;
  // Riddles
  private final List<Quiz> riddles = new ArrayList<>();
  private Entity bridgeGuard;
  private boolean rewardGiven = false;

  /**
   * Constructs a new BridgeGuardRiddleHandler with the given custom points and level.
   *
   * @param customPoints The custom points of the level.
   * @param level The level of the riddle handler.
   */
  public BridgeGuardRiddleHandler(List<Coordinate> customPoints, DungeonLevel level) {
    this.level = level;
    this.bridgeBounds = new Coordinate[] {customPoints.get(0), customPoints.get(1)};
    this.bridgePitsBounds = new Coordinate[] {customPoints.get(2), customPoints.get(3)};
    this.bridgeLever = customPoints.get(4);
    this.bridgeLeverSign = customPoints.get(5);
    this.bridgeGuardSpawn = customPoints.get(6);
    this.riddleRoomEntrance = customPoints.get(7);
    this.riddleRoomBounds = new Coordinate[] {customPoints.get(8), customPoints.get(9)};
    this.riddleRoomChest = customPoints.get(10);
    this.riddleRewardSpawn = customPoints.get(11);
    this.riddleRoomExit = customPoints.get(12);

    setupRiddles();
  }

  /** Handles the first tick of the riddle handler. */
  public void onFirstTick() {
    LevelUtils.changeVisibilityForArea(riddleRoomBounds[0], riddleRoomBounds[1], false);
    prepareBridge();
    spawnChestAndCauldron();
    level.tileAt(riddleRewardSpawn).ifPresent(tile -> tile.tintColor(0x22AAFFFF));
  }

  /** Handles the ticks of the riddle handler. */
  public void onTick() {
    Coordinate heroPos = EntityUtils.getHeroCoordinate();
    if (heroPos == null) return;

    if (!rewardGiven && riddleRewardSpawn.equals(heroPos)) {
      giveReward();
    }
  }

  private void prepareBridge() {
    LevelUtils.tilesInArea(bridgePitsBounds[0], bridgePitsBounds[1]).stream()
        .filter(tile -> tile instanceof PitTile || tile instanceof FloorTile)
        .peek(tile -> level.changeTileElementType(tile, LevelElement.PIT))
        .flatMap(tile -> level.tileAt(tile.coordinate()).stream())
        .map(tile -> (PitTile) tile)
        .forEach(PitTile::open);

    LevelUtils.tilesInArea(bridgePitsBounds[0], bridgePitsBounds[1]).stream()
        .filter(tile -> tile instanceof WallTile)
        .peek(wallTile -> level.changeTileElementType(wallTile, LevelElement.FLOOR))
        .forEach(
            tile -> {
              utils.EntityUtils.spawnTorch(tile.coordinate().toPoint(), true, false, 0);
            });

    LevelUtils.tilesInArea(bridgeBounds[0], bridgeBounds[1]).stream()
        .map(tile -> (PitTile) tile)
        .forEach(
            pitTile -> {
              pitTile.timeToOpen(99999999);
              pitTile.close();
            });

    prepareBridgeEntities();
  }

  private void prepareBridgeEntities() {
    EntityUtils.spawnLever(
        bridgeLever.toPoint(), new BridgeControlCommand(bridgeBounds[0], bridgeBounds[1]));
    EntityUtils.spawnSign(
        "Pull the lever to raise and lower the bridge",
        "Bridge Control",
        bridgeLeverSign.toPoint());
    this.bridgeGuard =
        utils.EntityUtils.spawnBridgeGuard(bridgeGuardSpawn.toPoint(), riddles, lastTask());
    bridgeGuard
        .fetch(HealthComponent.class)
        .ifPresent(
            hc ->
                hc.onHit(
                    ((entity, damage) -> {
                      if (damage.damageType() == DamageType.HEAL
                          || damage.damageType() == DamageType.FALL) return;
                      hc.receiveHit(
                          new Damage(-damage.damageAmount(), DamageType.HEAL, bridgeGuard));
                      if (entity.isPresent(PlayerComponent.class)) {
                        OkDialog.showOkDialog(
                            "Haha, you cannot harm me! I am invincible!",
                            "Riddle: Bridge Guard",
                            () -> {});
                      }
                    })));
    ((DevHealthSystem) Game.systems().get(DevHealthSystem.class)).registerObserver(this);
  }

  private IVoidFunction lastTask() {
    Quiz lastRiddle = new SingleChoice("What is my favorite number?");
    lastRiddle.taskName("Riddle: Bridge Guard");

    for (int i = 0; i < 6; i++) {
      lastRiddle.addAnswer(new Quiz.Content("" + (int) (Math.random() * Integer.MAX_VALUE)));
    }
    lastRiddle.addCorrectAnswerIndex(0);

    return () -> {
      QuizUI.showQuizDialog(
          lastRiddle,
          (Entity hudEntity) ->
              UIAnswerCallback.uiCallback(
                  lastRiddle,
                  hudEntity,
                  (task, taskContents) -> {
                    task.gradeTask(taskContents);
                    String output = "You have incorrectly solved the task";

                    OkDialog.showOkDialog(
                        output,
                        "Result",
                        () -> {
                          bridgeGuard.remove(InteractionComponent.class);
                          bridgeGuard.add(
                              new InteractionComponent(
                                  2.5f,
                                  true,
                                  (me, who) -> {
                                    OkDialog.showOkDialog(
                                        "Haha, you failed the riddle! You shall not pass!",
                                        "Riddle: Bridge Guard",
                                        () -> {});
                                  }));
                        });
                  }));
    };
  }

  @Override
  public void onHealthEvent(HealthSystem.HSData hsData, HealthEvent healthEvent) {
    if (healthEvent == HealthEvent.DEATH && hsData.e().equals(bridgeGuard)) {
      LevelUtils.changeVisibilityForArea(riddleRoomBounds[0], riddleRoomBounds[1], true);
      level
          .tileAt(riddleRoomEntrance)
          .filter(tile -> tile instanceof DoorTile)
          .map(tile -> (DoorTile) tile)
          .ifPresent(DoorTile::open);
      level
          .tileAt(riddleRoomEntrance)
          .filter(tile -> tile instanceof DoorTile)
          .map(tile -> (DoorTile) tile)
          .ifPresent(DoorTile::open);
    }
  }

  private void giveReward() {
    DialogUtils.showTextPopup(
        "You will receive a magic shield that can absorb damage as a reward for solving this puzzle!",
        "Riddle solved");
    Entity hero = Game.hero().orElse(null);
    if (hero == null) return;
    hero.add(new MagicShieldComponent());
    this.rewardGiven = true;
    level.tileAt(riddleRewardSpawn).ifPresent(tile -> tile.tintColor(-1));
  }

  // Riddle Methods
  private void setupRiddles() {
    List<SingleChoice> riddles = RegexRiddle.getRandRiddles(5);
    for (SingleChoice riddle : riddles) {
      riddle.taskName("Riddle: Bridge Guard");
      this.riddles.add(riddle);
    }
  }

  // Spawn Methods

  private void spawnChestAndCauldron() {
    Entity chest;
    try {
      chest = MiscFactory.newChest(MiscFactory.FILL_CHEST.EMPTY);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create chest");
    }
    PositionComponent pc =
        chest
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(chest, PositionComponent.class));

    pc.position(riddleRoomChest.toPoint());

    InventoryComponent ic =
        chest
            .fetch(InventoryComponent.class)
            .orElseThrow(() -> MissingComponentException.build(chest, InventoryComponent.class));
    ic.add(new ItemPotionAttackSpeed());
    ic.add(new ItemPotionHealth(HealthPotionType.NORMAL));
    Game.add(chest);

    Entity cauldron;
    try {
      cauldron = MiscFactory.newCraftingCauldron();
    } catch (Exception e) {
      throw new RuntimeException("Failed to create cauldron");
    }
    pc =
        cauldron
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(cauldron, PositionComponent.class));
    pc.position(new Coordinate(riddleRoomChest.x() + 1, riddleRoomChest.y()).toPoint());
    Game.add(cauldron);
  }
}
