package level.devlevel.riddleHandler;

import components.MagicShieldComponent;
import contrib.components.HealthComponent;
import contrib.components.InteractionComponent;
import contrib.components.InventoryComponent;
import contrib.entities.DialogFactory;
import contrib.entities.IHealthObserver;
import contrib.entities.MiscFactory;
import contrib.hud.dialogs.OkDialog;
import contrib.item.HealthPotionType;
import contrib.item.concreteItem.ItemPotionHealth;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.level.TileLevel;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.FloorTile;
import core.level.elements.tile.PitTile;
import core.level.elements.tile.WallTile;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import core.utils.IVoidFunction;
import core.utils.components.MissingComponentException;
import entities.levercommands.BridgeControlCommand;
import item.concreteItem.ItemPotionAttackSpeedPotion;
import java.util.ArrayList;
import java.util.List;
import level.utils.ITickable;
import level.utils.LevelUtils;
import systems.DevHealthSystem;
import task.game.hud.QuizUI;
import task.game.hud.UIAnswerCallback;
import task.tasktype.Quiz;
import task.tasktype.quizquestion.SingleChoice;
import utils.DevRiddle;
import utils.EntityUtils;
import utils.RegexRiddle;

public class BridgeGuardRiddleHandler implements ITickable, IHealthObserver {
  private final TileLevel level;

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

  public BridgeGuardRiddleHandler(List<Coordinate> customPoints, TileLevel level) {
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

    this.setupRiddles();
  }

  @Override
  public void onTick(boolean isFirstTick) {
    if (isFirstTick) {
      this.handleFirstTick();
    }

    Coordinate heroPos = EntityUtils.getHeroCoordinate();
    if (heroPos == null) return;

    if (!this.rewardGiven && this.riddleRewardSpawn.equals(heroPos)) {
      this.giveReward();
    }
  }

  private void handleFirstTick() {
    LevelUtils.changeVisibilityForArea(this.riddleRoomBounds[0], this.riddleRoomBounds[1], false);
    this.prepareBridge();
    this.spawnChestAndCauldron();
    this.level.tileAt(this.riddleRewardSpawn).tintColor(0x22AAFFFF);
  }

  private void prepareBridge() {
    this.level.tilesInArea(this.bridgePitsBounds[0], this.bridgePitsBounds[1]).stream()
        .filter(tile -> tile instanceof PitTile || tile instanceof FloorTile)
        .map(
            tile -> {
              this.level.changeTileElementType(tile, LevelElement.PIT);
              return (PitTile) this.level.tileAt(tile.coordinate());
            })
        .forEach(PitTile::open);
    this.level.tilesInArea(this.bridgePitsBounds[0], this.bridgePitsBounds[1]).stream()
        .filter(tile -> tile instanceof WallTile)
        .peek(wallTile -> this.level.changeTileElementType(wallTile, LevelElement.FLOOR))
        .forEach(
            tile -> {
              EntityUtils.spawnTorch(tile.coordinate().toCenteredPoint(), true, false, 0);
            });

    this.level.tilesInArea(this.bridgeBounds[0], this.bridgeBounds[1]).stream()
        .map(tile -> (PitTile) tile)
        .forEach(
            pitTile -> {
              pitTile.timeToOpen(99999999);
              pitTile.close();
            });

    this.prepareBridgeEntities();
  }

  private void prepareBridgeEntities() {
    EntityUtils.spawnLever(
        this.bridgeLever.toCenteredPoint(),
        new BridgeControlCommand(this.bridgeBounds[0], this.bridgeBounds[1]));
    EntityUtils.spawnSign(
        "Pull the lever to raise and lower the bridge",
        "Bridge Control",
        this.bridgeLeverSign.toCenteredPoint());
    this.bridgeGuard =
        EntityUtils.spawnBridgeGuard(
            this.bridgeGuardSpawn.toCenteredPoint(), this.riddles, this.lastTask());
    this.bridgeGuard
        .fetch(HealthComponent.class)
        .ifPresent(
            hc ->
                hc.onHit(
                    ((entity, damage) -> {
                      if (damage.damageType() == DamageType.HEAL
                          || damage.damageType() == DamageType.FALL) return;
                      hc.receiveHit(
                          new Damage(-damage.damageAmount(), DamageType.HEAL, this.bridgeGuard));
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
                          this.bridgeGuard.remove(InteractionComponent.class);
                          this.bridgeGuard.add(
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

  public void onHeathEvent(
      Entity entity, HealthComponent healthComponent, HealthEvent healthEvent) {
    if (healthEvent == HealthEvent.DEATH && entity.equals(this.bridgeGuard)) {
      LevelUtils.changeVisibilityForArea(this.riddleRoomBounds[0], this.riddleRoomBounds[1], true);
      ((DoorTile) this.level.tileAt(this.riddleRoomEntrance)).open();
      ((DoorTile) this.level.tileAt(this.riddleRoomExit)).open();
    }
  }

  private void giveReward() {
    DialogFactory.showTextPopup(
        "You will receive a magic shield that can absorb damage as a reward for solving this puzzle!",
        "Riddle solved");
    Entity hero = Game.hero().orElse(null);
    if (hero == null) return;
    hero.add(new MagicShieldComponent());
    this.rewardGiven = true;
    this.level.tileAt(this.riddleRewardSpawn).tintColor(-1);
  }

  // Riddle Methods
  private void addRiddle(String question, String[] answers, int correctAnswerIndex) {
    Quiz riddle = new SingleChoice(question);
    riddle.taskName("Riddle: Bridge Guard");
    for (String answer : answers) {
      riddle.addAnswer(new Quiz.Content(answer));
    }
    riddle.addCorrectAnswerIndex(correctAnswerIndex);
    this.riddles.add(riddle);
  }

  private void setupRiddles() {
    List<DevRiddle> riddles = RegexRiddle.getRandRiddles(5);
    for (DevRiddle riddle : riddles) {
      this.addRiddle(
          riddle.question(), riddle.answers().toArray(new String[0]), riddle.correctAnswerIndex());
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

    pc.position(this.riddleRoomChest.toCenteredPoint());

    InventoryComponent ic =
        chest
            .fetch(InventoryComponent.class)
            .orElseThrow(() -> MissingComponentException.build(chest, InventoryComponent.class));
    ic.add(new ItemPotionAttackSpeedPotion());
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
    pc.position(
        new Coordinate(this.riddleRoomChest.x + 1, this.riddleRoomChest.y).toCenteredPoint());
    Game.add(cauldron);
  }
}
