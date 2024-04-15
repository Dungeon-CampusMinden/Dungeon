package level.devlevel.riddleHandler;

import components.TorchComponent;
import contrib.components.SignComponent;
import contrib.entities.DialogFactory;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.level.TileLevel;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import entities.BurningFireballSkill;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import level.utils.ITickable;
import level.utils.LevelUtils;
import utils.ArrayUtils;

public class TorchRiddleRiddleHandler implements ITickable {

  /** Maximum value a riddle torch can have */
  public static final int UPPER_RIDDLE_BOUND = 15;

  /** Minimum value a riddle torch can have */
  public static final int LOWER_RIDDLE_BOUND = 5;

  /** The sign next to the riddle door */
  private final Entity riddleSign;

  private final TileLevel level;
  private final Coordinate riddleDoor;
  private final Coordinate[] riddleRoomBounds;
  private final Coordinate riddleCenter;
  private int riddleSearchedSum;
  private boolean rewardGiven = false;
  private boolean broken = false;

  public TorchRiddleRiddleHandler(List<Coordinate> customPoints, TileLevel level) {
    this.level = level;
    // First point is the riddle door
    this.riddleDoor = customPoints.getFirst();
    this.riddleRoomBounds =
        new Coordinate[] {customPoints.get(1), customPoints.get(2)}; // TopLeft, BottomRight
    // Next one is the center of the torch circle
    this.riddleCenter = customPoints.get(17);

    this.riddleSign =
        DialogFactory.createSign(
            "",
            "Riddle: The Torch Riddle",
            new Point(this.riddleDoor.x - 1 + 0.5f, this.riddleDoor.y - 1 + 0.5f),
            (sign, hero) -> {
              try {
                // Updates content based on random riddle values
                this.updateRiddleSign(this.getSumOfLitTorches());
              } catch (UnsupportedOperationException e) {
                sign.fetch(SignComponent.class)
                    .ifPresent(
                        sc -> {
                          sc.text("The Riddle seems to be broken.");
                        });
              }
            });
  }

  @Override
  public void onTick(boolean firstTick) {
    if (firstTick) {
      this.handleFirstTick();
    }
    if (!this.broken) {
      this.riddle();
    }
  }

  private void handleFirstTick() {
    Game.add(this.riddleSign);
    this.level.tileAt(this.riddleCenter).tintColor(0x22FF22FF);
    try {
      this.updateRiddleSign(this.getSumOfLitTorches());
    } catch (UnsupportedOperationException e) {
      this.broken = true;
    }
  }

  /**
   * Checks if the sum of the values of all lit torches equals the searched sum. If it does, the
   * riddle is solved.
   */
  private void riddle() {
    int sum = this.getSumOfLitTorches();

    this.testSum(sum);

    if (sum == this.riddleSearchedSum) {
      this.solveRiddle();
      if (!this.rewardGiven && this.checkIfHeroIsInCenter()) {
        this.giveReward();
      }
    }
  }

  /**
   * This method checks if the sum of the torches is within the expected range.
   *
   * @param sum The sum of the torches to be checked.
   * @throws AssertionError if the sum is out of the expected range.
   */
  private void testSum(int sum) {
    int lower = 0; // There are no negative torches
    int upper = (UPPER_RIDDLE_BOUND * 6); // Maximum possible sum
    if (sum < lower || sum > upper) {
      throw new AssertionError("The sum of the torches is out of bounds: " + sum);
    }
  }

  /**
   * Gives the reward to the hero if the riddle is solved. A popup message is displayed to inform
   * the hero about the reward. The reward is only given once, controlled by the rewardGiven flag.
   */
  private void giveReward() {
    DialogFactory.showTextPopup(
        "You will receive the new burning fireball skill\nas a reward for solving this puzzle!"
            + "Your fireballs will now deal extra burning damage.",
        "Riddle solved");
    BurningFireballSkill.UNLOCKED = true;
    this.rewardGiven = true;

    // Once the reward is given, all torches are extinguished
    Game.entityStream()
        .filter(e -> e.isPresent(TorchComponent.class))
        .forEach(
            e -> {
              e.fetch(TorchComponent.class).ifPresent(tc -> tc.lit(false));
              e.fetch(DrawComponent.class).ifPresent(dc -> dc.currentAnimation("off"));
            });

    this.level.tileAt(this.riddleCenter).tintColor(-1);
  }

  /**
   * Checks if the hero is in the center of the riddle room.
   *
   * @return true if the hero is in the center of the riddle room, false otherwise.
   */
  private boolean checkIfHeroIsInCenter() {
    Optional<Entity> hero = Game.hero();
    return hero.filter(
            entity -> this.level.tileAtEntity(entity).equals(this.level.tileAt(this.riddleCenter)))
        .isPresent();
  }

  /**
   * Solves the riddle by opening the door and making the riddle room visible.
   *
   * @see LevelUtils#changeVisibilityForArea(Coordinate, Coordinate, boolean)
   */
  private void solveRiddle() {
    DoorTile door = (DoorTile) this.level.tileAt(this.riddleDoor);
    door.open();
    LevelUtils.changeVisibilityForArea(this.riddleRoomBounds[0], this.riddleRoomBounds[1], true);
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
  public void setRiddleSolution(List<Integer> torchNumbers) {
    this.riddleSearchedSum = this.getRandomSumOfNElements(torchNumbers);
  }

  /**
   * Calculates the sum of the values of all lit torches in the game. This method is intended to be
   * implemented using stream operations.
   *
   * <p>The implementation should follow these steps:<br>
   * 1. Obtain a stream of all entities in the game. (See {@link Game})<br>
   * 2. Filter the stream to include only entities that have a TorchComponent and whose torches are
   * lit and maybe even a value greater than 0. (See {@link TorchComponent})<br>
   * 3. Map the filtered entities to their values.<br>
   * 4. Calculate the sum of these values. (E.g. via {@link IntStream})<br>
   *
   * <p>TODO: Implement the method using the described stream operations.
   *
   * @return The sum of the values of all lit torches in the game.
   */
  private int getSumOfLitTorches() {
    throw new UnsupportedOperationException("Not implemented yet.");
  }
}
