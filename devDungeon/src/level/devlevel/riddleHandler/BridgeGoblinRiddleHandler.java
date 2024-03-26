package level.devlevel.riddleHandler;

import contrib.components.HealthComponent;
import contrib.components.InventoryComponent;
import contrib.entities.MiscFactory;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.TileLevel;
import core.level.utils.Coordinate;
import core.utils.components.MissingComponentException;
import entities.SignFactory;
import item.concreteItem.ItemPotionSpeedPotion;
import java.util.List;
import level.utils.ITickable;

public class BridgeGoblinRiddleHandler implements ITickable {

  private static final int RIDDLE_REWARD = 5;
  private final TileLevel level;
  private boolean rewardGiven = false;
  private Coordinate riddleRewardSpawn = new Coordinate(0, 0);

  public BridgeGoblinRiddleHandler(List<Coordinate> customPoints, TileLevel level) {

    this.level = level;
  }

  @Override
  public void onTick(boolean isFirstTick) {
    if (isFirstTick) {
      this.handleFirstTick();
    }
  }

  private void giveReward() {
    SignFactory.showTextPopup(
        "You will receive "
            + RIDDLE_REWARD
            + " additional maximum health points \nas a reward for solving this puzzle!",
        "Riddle solved");
    Game.hero()
        .flatMap(hero -> hero.fetch(HealthComponent.class))
        .ifPresent(
            hc -> {
              hc.maximalHealthpoints(hc.maximalHealthpoints() + RIDDLE_REWARD);
              hc.receiveHit(new Damage(-RIDDLE_REWARD, DamageType.HEAL, null));
              this.rewardGiven = true;
            });
  }

  private void handleFirstTick() {
    this.spawnSigns();
    this.spawnChest();
    this.level.tileAt(this.riddleRewardSpawn).tintColor(0x22FF22FF);
  }

  private void spawnSigns() {}

  private void spawnChest() {
    Entity speedPotionChest;
    try {
      speedPotionChest = MiscFactory.newChest(MiscFactory.FILL_CHEST.EMPTY);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create chest");
    }
    PositionComponent pc =
        speedPotionChest
            .fetch(PositionComponent.class)
            .orElseThrow(
                () -> MissingComponentException.build(speedPotionChest, PositionComponent.class));

    pc.position();

    InventoryComponent ic =
        speedPotionChest
            .fetch(InventoryComponent.class)
            .orElseThrow(
                () -> MissingComponentException.build(speedPotionChest, InventoryComponent.class));
    ic.add(new ItemPotionSpeedPotion());
    Game.add(speedPotionChest);
  }
}
