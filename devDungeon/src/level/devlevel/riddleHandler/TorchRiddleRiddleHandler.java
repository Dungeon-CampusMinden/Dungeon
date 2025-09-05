package level.devlevel.riddleHandler;

import components.TorchComponent;
import contrib.components.SignComponent;
import contrib.components.SkillComponent;
import contrib.entities.SignFactory;
import contrib.hud.DialogUtils;
import contrib.utils.components.skill.SkillTools;
import contrib.utils.components.skill.projectileSkill.BurningFireballSkill;
import contrib.utils.components.skill.projectileSkill.FireballSkill;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.level.DungeonLevel;
import core.level.elements.ILevel;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.LevelUtils;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import utils.ArrayUtils;

/**
 * The TorchRiddleRiddleHandler class is used to handle the riddle of the torches. The riddle
 * consists of a series of torches that the hero has to light up. The hero can light up a certain
 * torches to receive a reward.
 */
public class TorchRiddleRiddleHandler {

  /** Maximum value a riddle torch can have. */
  public static final int UPPER_RIDDLE_BOUND = 15;

  /** Minimum value a riddle torch can have. */
  public static final int LOWER_RIDDLE_BOUND = 5;

  /** The sign next to the riddle door. */
  private final Entity riddleSign;

  private final DungeonLevel level;
  private final Coordinate riddleDoor;
  private final Coordinate[] riddleRoomBounds;
  private final Coordinate riddleCenter;
  private int riddleSearchedSum;
  private boolean rewardGiven = false;
  private boolean broken = false;

  /**
   * Constructs a new TorchRiddleRiddleHandler with the given custom points and level.
   *
   * @param customPoints The custom points of the riddle room.
   * @param level The level of the riddle room.
   */
  public TorchRiddleRiddleHandler(List<Coordinate> customPoints, DungeonLevel level) {
    this.level = level;
    // First point is the riddle door
    this.riddleDoor = customPoints.getFirst();
    this.riddleRoomBounds =
        new Coordinate[] {customPoints.get(1), customPoints.get(2)}; // TopLeft, BottomRight
    // Next one is the center of the torch circle
    this.riddleCenter = customPoints.get(17);

    this.riddleSign =
        SignFactory.createSign(
            "",
            "Riddle: The Torch Riddle",
            new Point(riddleDoor.x() - 1 + 0.5f, riddleDoor.y() - 1 + 0.5f),
            (sign, hero) -> {
              try {
                // Updates content based on random riddle values
                updateRiddleSign(getSumOfLitTorches());
              } catch (UnsupportedOperationException e) {
                sign.fetch(SignComponent.class)
                    .ifPresent(
                        sc -> {
                          sc.text("The Riddle seems to be broken.");
                        });
              }
            });
  }

  /** Handles the first tick of the riddle. */
  public void onFirstTick() {
    Game.add(riddleSign);
    level.tileAt(riddleCenter).ifPresent(tile -> tile.tintColor(0x22FF22FF));
    try {
      updateRiddleSign(getSumOfLitTorches());
    } catch (UnsupportedOperationException e) {
      this.broken = true;
    }
  }

  /** Handles the tick of the riddle, if it is not broken. */
  public void onTick() {
    if (broken) return;

    riddle();
  }

  /**
   * Checks if the sum of the values of all lit torches equals the searched sum. If it does, the
   * riddle is solved.
   */
  private void riddle() {
    int sum = getSumOfLitTorches();

    testSum(sum);

    if (sum == riddleSearchedSum) {
      solveRiddle();
      if (!rewardGiven && checkIfHeroIsInCenter()) {
        giveReward();
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
    DialogUtils.showTextPopup(
        "You will receive the new burning fireball skill\nas a reward for solving this puzzle!"
            + "Your fireballs will now deal extra burning damage.",
        "Riddle solved");
    Game.hero()
        .orElseThrow()
        .fetch(SkillComponent.class)
        .orElseThrow()
        .removeSkill(FireballSkill.class);
    Game.hero()
        .orElseThrow()
        .fetch(SkillComponent.class)
        .orElseThrow()
        .addSkill(new BurningFireballSkill(SkillTools::cursorPositionAsPoint));
    this.rewardGiven = true;

    // Once the reward is given, all torches are extinguished
    Game.levelEntities()
        .filter(e -> e.isPresent(TorchComponent.class))
        .forEach(
            e -> {
              e.fetch(TorchComponent.class).ifPresent(tc -> tc.lit(false));
              e.fetch(DrawComponent.class).ifPresent(dc -> dc.sendSignal("off"));
            });

    level.tileAt(riddleCenter).ifPresent(tile -> tile.tintColor(-1));
  }

  /**
   * Checks if the hero is in the center of the riddle room.
   *
   * @return true if the hero is in the center of the riddle room, false otherwise.
   */
  private boolean checkIfHeroIsInCenter() {
    Optional<Entity> hero = Game.hero();
    return hero.isPresent()
        && level
            .tileAtEntity(hero.get())
            .map(heroTile -> heroTile.equals(level.tileAt(riddleCenter).orElse(null)))
            .orElse(false);
  }

  /**
   * Solves the riddle by opening the door and making the riddle room visible.
   *
   * @see LevelUtils#changeVisibilityForArea(Coordinate, Coordinate, boolean)
   */
  private void solveRiddle() {
    level
        .tileAt(riddleDoor)
        .filter(tile -> tile instanceof DoorTile)
        .map(tile -> (DoorTile) tile)
        .ifPresent(DoorTile::open);
    LevelUtils.changeVisibilityForArea(riddleRoomBounds[0], riddleRoomBounds[1], true);
  }

  /**
   * Updates the text of the riddle sign to display the current sum of the values of all lit
   * torches.
   *
   * @param currentSum The current sum of the values of all lit torches.
   */
  private void updateRiddleSign(int currentSum) {
    riddleSign
        .fetch(SignComponent.class)
        .orElseThrow(() -> MissingComponentException.build(riddleSign, SignComponent.class))
        .text(
            "\n\nAll torches combined should sum up to: \n'"
                + riddleSearchedSum
                + "' You already have: '"
                + currentSum
                + "'");
  }

  /**
   * Returns a random sum of from an array of elements.
   *
   * @param numbers the array of elements
   * @return the sum of the random elements
   */
  private int getRandomSumOfNElements(List<Integer> numbers) {
    int amount = ILevel.RANDOM.nextInt(2, numbers.size());
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
    this.riddleSearchedSum = getRandomSumOfNElements(torchNumbers);
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
