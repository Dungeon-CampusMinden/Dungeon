package rooms.systemRecovery.riddles;

import static rooms.systemRecovery.riddles.RiddleSupport.moveSortEntity;

import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.level.DungeonLevel;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.sound.SoundSpec;
import engine.utils.Point;
import engine.utils.components.draw.shader.EnergyFillShader;
import engine.utils.components.draw.shader.OutlineShader;
import feature.entities.WorldItemBuilder;
import feature.hud.DialogUtils;
import feature.hud.dialogs.ChoiceOption;
import feature.hud.dialogs.DialogFactory;
import java.util.ArrayList;
import java.util.List;
import rooms.systemRecovery.SystemRecovery;
import rooms.systemRecovery.entities.SortingEntityFactory;
import rooms.systemRecovery.entities.SystemRecoveryDisplayFactory;
import rooms.systemRecovery.items.SortProgramStickItem;
import rooms.systemRecovery.level.SystemRecoveryLevel;
import rooms.systemRecovery.riddles.support.RiddleCallbacks;
import rooms.systemRecovery.story.SystemRecoveryStoryDialogs;
import rooms.systemRecovery.util.SystemRecoveryText;
import rooms.systemRecovery.util.shaders.EnergyGlow;

/**
 * Riddle 5: compare adjacent containers; wrong answers reset the exercise.
 *
 * <p>Owned by one level instance. Gameplay and scheduled actions run on the authoritative server;
 * clients receive entity state through the existing snapshot protocol.
 */
public final class ManualSortingRiddle {
  /** Values are deliberately spread across the shader's 0..100 fill range. */
  private static final int[] INITIAL_SORT_VALUES = {90, 30, 10, 70, 50};

  private final DungeonLevel level;
  private final BubbleSortRiddle bubbleSort;
  private final RiddleCallbacks callbacks;
  private final int[] sortValues = INITIAL_SORT_VALUES.clone();
  private final Entity[] sortData = new Entity[sortValues.length];
  private final Entity[] sortOriginalData = new Entity[sortValues.length];
  private Point[] sortPoints;
  private Entity sortDisplay;
  private int sortOuterIndex;
  private int sortInnerIndex;

  /** Player currently owning the shared comparison station, or {@code -1} when it is free. */
  private int sortOwnerPlayerId = -1;

  private boolean sortCompleted;

  /**
   * Returns whether all comparison decisions have been completed correctly.
   *
   * @return whether the manual sorting riddle is complete
   */
  public boolean completed() {
    return sortCompleted;
  }

  /**
   * Creates the comparison exercise; the machine dependency prevents overlapping interactions.
   *
   * @param level level that owns the sorting entities
   * @param bubbleSort machine that shares the conveyor area
   */
  public ManualSortingRiddle(DungeonLevel level, BubbleSortRiddle bubbleSort) {
    this(level, bubbleSort, RiddleCallbacks.noop());
  }

  /**
   * Creates the comparison exercise with callbacks for successful and failed choices.
   *
   * @param level level that owns the sorting entities
   * @param bubbleSort machine that shares the conveyor area
   * @param callbacks success, failure and completion callbacks
   */
  public ManualSortingRiddle(
      DungeonLevel level, BubbleSortRiddle bubbleSort, RiddleCallbacks callbacks) {
    this.level = level;
    this.bubbleSort = bubbleSort;
    this.callbacks = callbacks;
  }

  /** Spawns the five containers and their comparison display. */
  public void setup() {
    sortPoints = new Point[5];
    for (int index = 0; index < sortPoints.length; index++) {
      String pointName = index == 0 ? "sort_data_0" : "sort_data" + index;
      sortPoints[index] = level.getPoint(pointName);
      sortData[index] = SortingEntityFactory.dataCrystal(sortPoints[index], sortValues[index]);
      sortOriginalData[index] = sortData[index];
      Game.add(sortData[index]);
    }
    sortOuterIndex = 0;
    sortInnerIndex = 0;
    sortOwnerPlayerId = -1;
    sortCompleted = false;
    sortDisplay =
        SystemRecoveryDisplayFactory.moduleDisplay(
            level.getPoint("sort_compare_display"), this::sortDisplayText, this::showSortChoice);
    sortDisplay.name("sort_compare_display");
    Game.add(sortDisplay);
    updateSortDisplay();
  }

  private String sortDisplayText() {
    if (sortCompleted) return SystemRecoveryText.key("world.sort.display-complete");
    int left = sortValues[sortInnerIndex];
    int right = sortValues[sortInnerIndex + 1];
    return SystemRecoveryText.key("world.sort.display", sortInnerIndex, left, right);
  }

  private String sortDialogText() {
    if (sortCompleted) return SystemRecoveryText.key("world.sort.display-complete");
    int left = sortValues[sortInnerIndex];
    int right = sortValues[sortInnerIndex + 1];
    return SystemRecoveryText.key("world.sort.dialog-values", left, right);
  }

  private void showSortChoice(Entity display, Entity player) {
    if (sortCompleted) {
      DialogFactory.showDialogDialog(
          SystemRecoveryText.echoCall("sort-complete"), () -> {}, player.id());
      return;
    }
    if (!claimSortStation(player.id())) {
      DialogUtils.showTextPopup(
          SystemRecoveryText.key("world.sort.in-use"),
          SystemRecoveryText.key("world.sort.title"),
          player.id());
      return;
    }
    List<ChoiceOption> choices =
        new ArrayList<>(
            List.of(
                ChoiceOption.of(SystemRecoveryText.key("world.sort.swap"), "swap"),
                ChoiceOption.of(SystemRecoveryText.key("world.sort.keep"), "keep")));
    if (SystemRecovery.debugMode()) {
      choices.add(ChoiceOption.of(SystemRecoveryText.key("world.sort.skip"), "skip"));
    }
    DialogFactory.showMultipleChoiceDialog(
        sortDialogText(),
        SystemRecoveryText.key("world.sort.title"),
        choices,
        true,
        payload -> {
          if (payload instanceof DialogResponseMessage.StringValue(String choice)) {
            if ("skip".equals(choice)) {
              skipForDebug(player);
            } else {
              applySortChoice("swap".equals(choice), player);
            }
          }
        },
        () -> releaseSortStation(player.id()),
        player.id());
  }

  /**
   * Completes the manual sorting riddle for local debug sessions.
   *
   * @param player player receiving the debug reward and story feedback
   */
  void skipForDebug(Entity player) {
    if (!SystemRecovery.debugMode() || sortCompleted) return;
    sortValuesAndEntities();
    sortOuterIndex = sortValues.length - 1;
    sortInnerIndex = 0;
    sortCompleted = true;
    releaseSortStation(player.id());
    callbacks.success("debug-skip", player.id());
    callbacks.solved();
    updateSortDisplay();
    spawnSortProgramStick();
    SystemRecoveryLevel.announceStoryToAllPlayers(SystemRecoveryStoryDialogs.BUBBLE_SORT_CODE);
  }

  private void sortValuesAndEntities() {
    for (int outerIndex = 0; outerIndex < sortValues.length - 1; outerIndex++) {
      for (int innerIndex = 0; innerIndex < sortValues.length - 1 - outerIndex; innerIndex++) {
        if (sortValues[innerIndex] <= sortValues[innerIndex + 1]) continue;
        int value = sortValues[innerIndex];
        sortValues[innerIndex] = sortValues[innerIndex + 1];
        sortValues[innerIndex + 1] = value;
        Entity entity = sortData[innerIndex];
        sortData[innerIndex] = sortData[innerIndex + 1];
        sortData[innerIndex + 1] = entity;
        moveSortEntity(sortData[innerIndex], sortPoints[innerIndex]);
        moveSortEntity(sortData[innerIndex + 1], sortPoints[innerIndex + 1]);
      }
    }
  }

  synchronized void applySortChoice(boolean swap, Entity player) {
    if (sortCompleted) return;
    // Direct calls are useful for authoritative tests and debug commands; normal UI interaction
    // claims the station in showSortChoice before reaching this method.
    if (sortOwnerPlayerId == -1) {
      sortOwnerPlayerId = player.id();
    }
    if (sortOwnerPlayerId != player.id()) {
      callbacks.failure("not-owner", player.id());
      return;
    }
    if (bubbleSort.running()) {
      callbacks.failure("blocked-machine", player.id());
      return;
    }
    boolean shouldSwap = sortValues[sortInnerIndex] > sortValues[sortInnerIndex + 1];
    if (swap != shouldSwap) {
      callbacks.failure(swap ? "swap" : "keep", player.id());
      resetSortStation();
      // Keep the failure feedback non-modal. A blocking popup would freeze the player after the
      // station has already been reset and would require a second, unrelated interaction before
      // the next comparison can be selected.
      Game.audio().playGlobal(SoundSpec.builder("retro_event_wrong"));
      return;
    }
    callbacks.success(swap ? "swap" : "keep", player.id());
    if (shouldSwap) {
      int value = sortValues[sortInnerIndex];
      sortValues[sortInnerIndex] = sortValues[sortInnerIndex + 1];
      sortValues[sortInnerIndex + 1] = value;
      Entity entity = sortData[sortInnerIndex];
      sortData[sortInnerIndex] = sortData[sortInnerIndex + 1];
      sortData[sortInnerIndex + 1] = entity;
      moveSortEntity(sortData[sortInnerIndex], sortPoints[sortInnerIndex]);
      moveSortEntity(sortData[sortInnerIndex + 1], sortPoints[sortInnerIndex + 1]);
    }
    advanceSortComparison();
    updateSortDisplay();
    Game.audio().playGlobal(SoundSpec.builder("retro_event_correct"));
    if (sortCompleted) {
      releaseSortStation(player.id());
      callbacks.solved();
      spawnSortProgramStick();
      SystemRecoveryLevel.announceStoryToAllPlayers(SystemRecoveryStoryDialogs.BUBBLE_SORT_CODE);
    } else {
      // The next comparison is a new server-authoritative dialog for the same player. A wrong
      // answer returns above after resetting the station and therefore never opens a follow-up.
      showSortChoice(sortDisplay, player);
    }
  }

  /** Restores the original values and positions after a wrong comparison decision. */
  private void resetSortStation() {
    System.arraycopy(INITIAL_SORT_VALUES, 0, sortValues, 0, sortValues.length);
    sortOuterIndex = 0;
    sortInnerIndex = 0;
    sortCompleted = false;
    for (int index = 0; index < sortData.length; index++) {
      sortData[index] = sortOriginalData[index];
      moveSortEntity(sortData[index], sortPoints[index]);
    }
    updateSortDisplay();
  }

  private synchronized boolean claimSortStation(int playerId) {
    if (sortOwnerPlayerId == -1 || sortOwnerPlayerId == playerId) {
      sortOwnerPlayerId = playerId;
      return true;
    }
    return false;
  }

  private synchronized void releaseSortStation(int playerId) {
    if (sortOwnerPlayerId == playerId) {
      sortOwnerPlayerId = -1;
    }
  }

  private void spawnSortProgramStick() {
    if (Game.entityAtPoint(level.getPoint("chip_spawn"))
        .anyMatch(e -> e.name().contains("Sortierchip"))) {
      return;
    }
    Game.add(
        WorldItemBuilder.buildWorldItem(new SortProgramStickItem(), level.getPoint("chip_spawn")));
  }

  private void advanceSortComparison() {
    sortInnerIndex++;
    if (sortInnerIndex >= sortValues.length - 1 - sortOuterIndex) {
      sortOuterIndex++;
      sortInnerIndex = 0;
    }
    if (sortOuterIndex >= sortValues.length - 1) sortCompleted = true;
  }

  private void updateSortDisplay() {
    if (sortDisplay != null) {
      SystemRecoveryDisplayFactory.updateDisplayText(sortDisplay, sortDisplayText());
    }
    updateSortComparisonHighlight();
  }

  /** Highlights exactly the adjacent values currently compared by the bubble-sort step. */
  private void updateSortComparisonHighlight() {
    // TextureMap and shader construction require the client-side LibGDX runtime. The server
    // publishes the comparison entity IDs through snapshots; clients render the highlight.
    if (Game.isHeadless()) {
      return;
    }
    for (Entity data : sortData) {
      if (data == null) continue;
      data.fetch(DrawComponent.class)
          .ifPresent(
              draw -> {
                draw.shaders().remove("sortComparison");
                draw.shaders().remove("sortComparisonFill");
                draw.shaders()
                    .add(
                        "sortComparisonFill",
                        new EnergyFillShader(
                                sortValue(data), Color.CYAN, "objects/tech/CryoBox.png")
                            .animMagnitude(0));
                EnergyGlow.addTo(draw);
              });
    }
    if (sortCompleted) return;
    for (int index = sortInnerIndex; index <= sortInnerIndex + 1; index++) {
      sortData[index]
          .fetch(DrawComponent.class)
          .ifPresent(
              draw -> {
                draw.shaders().add("sortComparison", new OutlineShader(2, Color.CYAN));
              });
    }
  }

  private float sortValue(Entity data) {
    String name = data.name();
    int separator = name.lastIndexOf('_');
    try {
      return Integer.parseInt(name.substring(separator + 1)) / 100f;
    } catch (NumberFormatException ignored) {
      return 0f;
    }
  }

  /**
   * Returns the server-authoritative entity IDs of the pair currently compared by the sorter.
   *
   * <p>The network snapshot translator uses these IDs to reproduce the cyan comparison highlight on
   * every connected client. The sorter state itself is intentionally not accepted from clients.
   *
   * @return two entity IDs, or {@code {-1, -1}} when no comparison is active
   */
  public int[] currentSortComparisonEntityIds() {
    if (sortCompleted
        || sortInnerIndex < 0
        || sortInnerIndex + 1 >= sortData.length
        || sortData[sortInnerIndex] == null
        || sortData[sortInnerIndex + 1] == null) {
      return new int[] {-1, -1};
    }
    return new int[] {sortData[sortInnerIndex].id(), sortData[sortInnerIndex + 1].id()};
  }
}
