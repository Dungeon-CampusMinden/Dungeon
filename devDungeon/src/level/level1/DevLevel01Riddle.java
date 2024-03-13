package level.level1;

import components.SignComponent;
import components.TorchComponent;
import contrib.components.HealthComponent;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.level.TileLevel;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import entities.SignFactory;
import java.util.List;
import java.util.Optional;
import level.utils.LevelUtils;
import utils.ArrayUtils;

public class DevLevel01Riddle {

  /** Maximum value a riddle torch can have */
  static final int UPPER_RIDDLE_BOUND = 15;

  /** Minimum value a riddle torch can have */
  static final int LOWER_RIDDLE_BOUND = 5;

  /** The reward for solving the riddle (max health points) */
  private static final int RIDDLE_REWARD = 5;

  /** The sign next to the riddle door */
  private final Entity riddleSign;

  private final TileLevel level;
  private final Coordinate riddleDoor;
  private final Coordinate[] riddleRoomBounds;
  private final Coordinate riddleCenter;
  private int riddleSearchedSum;
  private boolean rewardGiven = false;

  public DevLevel01Riddle(List<Coordinate> customPoints, TileLevel level) {
    this.level = level;
    this.riddleRoomBounds =
        new Coordinate[] {customPoints.get(1), customPoints.get(2)}; // TopLeft, BottomRight
    // First point is the riddle door
    this.riddleDoor = customPoints.getFirst();
    // Next one is the center of the torch circle
    this.riddleCenter = customPoints.get(17);

    this.riddleSign =
        SignFactory.createSign(
            "",
            "Riddle",
            new Point(this.riddleDoor.x - 1 + 0.5f, this.riddleDoor.y - 1 + 0.5f),
            (e1, e2) ->
                updateRiddleSign(
                    getSumOfLitTorches())); // Updates content based on random riddle values
  }

  void onTick(boolean firstTick) {
    if (firstTick) {
      this.handleFirstTick();
    }
    this.riddle();
  }

  private void handleFirstTick() {
    Game.add(this.riddleSign);
    updateRiddleSign(getSumOfLitTorches());
  }

  /**
   * Checks if the sum of the values of all lit torches equals the searched sum. If it does, the
   * riddle is solved.
   */
  private void riddle() {
    int sum = getSumOfLitTorches();

    if (sum == this.riddleSearchedSum) {
      solveRiddle();
      if (!this.rewardGiven && checkIfHeroIsInCenter()) {
        giveReward();
      }
    }
  }

  /**
   * Gives the reward to the hero if the riddle is solved. A popup message is displayed to inform
   * the hero about the reward. The reward is only given once, controlled by the rewardGiven flag.
   */
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

    if (this.rewardGiven) {
      // Once the reward is given, all torches are extinguished
      Game.entityStream()
          .filter(e -> e.isPresent(TorchComponent.class))
          .forEach(
              e -> {
                e.fetch(TorchComponent.class).ifPresent(tc -> tc.lit(false));
                e.fetch(DrawComponent.class).ifPresent(dc -> dc.currentAnimation("off"));
              });
    }
  }

  /**
   * Checks if the hero is in the center of the riddle room.
   *
   * @return true if the hero is in the center of the riddle room, false otherwise.
   */
  private boolean checkIfHeroIsInCenter() {
    Optional<Entity> hero = Game.hero();
    return hero.filter(entity -> level.tileAtEntity(entity).equals(level.tileAt(this.riddleCenter)))
        .isPresent();
  }

  /**
   * Solves the riddle by opening the door and making the riddle room visible.
   *
   * @see LevelUtils#ChangeVisibilityForArea(Coordinate, Coordinate, boolean)
   */
  private void solveRiddle() {
    DoorTile door = (DoorTile) level.tileAt(this.riddleDoor);
    door.open();
    LevelUtils.ChangeVisibilityForArea(this.riddleRoomBounds[0], this.riddleRoomBounds[1], true);
  }

  /**
   * Calculates the sum of the values of all lit torches in the game.
   *
   * @return The sum of the values of all lit torches.
   */
  private int getSumOfLitTorches() {
    return Game.entityStream()
        .filter(e -> e.isPresent(TorchComponent.class) && e.fetch(TorchComponent.class).get().lit())
        .map(
            e ->
                e.fetch(TorchComponent.class)
                    .orElseThrow(() -> MissingComponentException.build(e, TorchComponent.class)))
        .mapToInt(TorchComponent::value)
        .sum();
  }

  /**
   * Updates the text of the riddle sign to display the current sum of the values of all lit
   * torches.
   *
   * @param currentSum The current sum of the values of all lit torches.
   */
  private void updateRiddleSign(int currentSum) {
    this.riddleSign
        .fetch(SignComponent.class)
        .orElseThrow(() -> MissingComponentException.build(this.riddleSign, SignComponent.class))
        .text(
            "\n\nAll torches combined should sum up to: \n'"
                + this.riddleSearchedSum
                + "' You already have: '"
                + currentSum
                + "'");
  }

  /**
   * Returns a random sum of from an array of elements
   *
   * @param numbers the array of elements
   * @return the sum of the random elements
   */
  private int getRandomSumOfNElements(List<Integer> numbers) {
    int amount = this.level.RANDOM.nextInt(2, numbers.size());
    List<Integer> randomNumbers =
        ArrayUtils.getRandomElements(numbers.toArray(new Integer[0]), amount);
    return randomNumbers.stream().mapToInt(Integer::intValue).sum();
  }

  /**
   * Sets the solution for the riddle.
   *
   * @param torchNumbers The values of all riddle related torches.
   * @see #getRandomSumOfNElements(List)
   */
  void setRiddleSolution(List<Integer> torchNumbers) {
    this.riddleSearchedSum = getRandomSumOfNElements(torchNumbers);
  }
}
