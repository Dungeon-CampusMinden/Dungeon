package rooms.systemRecovery.riddles;

import static rooms.systemRecovery.riddles.RiddleSupport.moveSortEntity;
import static rooms.systemRecovery.riddles.RiddleSupport.portraitPathFor;

import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PlayerComponent;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.sound.SoundSpec;
import engine.utils.Point;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.draw.shader.EnergyFillShader;
import engine.utils.components.draw.shader.OutlineShader;
import engine.utils.components.path.SimpleIPath;
import feature.entities.WorldItemBuilder;
import feature.hud.DialogUtils;
import feature.hud.dialogs.ChoiceOption;
import feature.hud.dialogs.DialogFactory;
import feature.systems.LevelEditorSystem;
import java.util.HashSet;
import java.util.Set;
import rooms.systemRecovery.entities.EntityFactory;
import rooms.systemRecovery.items.SortProgramStickItem;

/**
 * Riddle 5: compare adjacent containers; wrong answers reset the exercise.
 *
 * <p>Owned by one level instance. Gameplay and scheduled actions run on the authoritative server;
 * clients receive entity state through the existing snapshot protocol.
 */
public final class ManualSortingRiddle {
  private final DungeonLevel level;
  private final BubbleSortRiddle bubbleSort;
  private final int[] sortValues = {42, 17, 8, 31, 23};
  private final Entity[] sortData = new Entity[sortValues.length];
  private final Entity[] sortOriginalData = new Entity[sortValues.length];
  private Point[] sortPoints;
  private Entity sortDisplay;
  private int sortOuterIndex;
  private int sortInnerIndex;
  private boolean sortCompleted;
  private final Set<Integer> sortTriggeredPlayers = new HashSet<>();

  /** Creates the comparison exercise; the machine dependency prevents overlapping interactions. */
  public ManualSortingRiddle(DungeonLevel level, BubbleSortRiddle bubbleSort) {
    this.level = level;
    this.bubbleSort = bubbleSort;
  }

  /** Spawns the five containers and their comparison display. */
  public void setup() {
    sortPoints = new Point[5];
    for (int index = 0; index < sortPoints.length; index++) {
      String pointName = index == 0 ? "sort_data_0" : "sort_data" + index;
      sortPoints[index] = level.getPoint(pointName);
      sortData[index] = EntityFactory.sortingDataCrystal(sortPoints[index], sortValues[index]);
      sortOriginalData[index] = sortData[index];
      Game.add(sortData[index]);
    }
    sortOuterIndex = 0;
    sortInnerIndex = 0;
    sortCompleted = false;
    sortDisplay =
        EntityFactory.moduleDisplay(
            level.getPoint("sort_compare_display"), this::sortDisplayText, this::showSortChoice);
    sortDisplay.name("sort_compare_display");
    Game.add(sortDisplay);
    updateSortDisplay();
  }

  /** Shows the introduction once per player at sort_trigger, never while editing the level. */
  public void tick() {
    if (LevelEditorSystem.active()) {
      return;
    }

    Point triggerPoint;
    try {
      triggerPoint = level.getPoint("sort_trigger");
    } catch (RuntimeException ignored) {
      return;
    }

    Game.levelEntities(Set.of(PlayerComponent.class))
        .filter(player -> player.isPresent(PositionComponent.class))
        .filter(
            player ->
                player
                        .fetch(PositionComponent.class)
                        .orElseThrow()
                        .position()
                        .distanceSquared(triggerPoint)
                    <= 1.0)
        .filter(player -> sortTriggeredPlayers.add(player.id()))
        .forEach(
            player ->
                DialogFactory.showDialogDialog(
                    "Der Datenspeicher enthält unsortierte Sicherheitswerte. "
                        + "Bringe die Werte in eine aufsteigende Reihenfolge. "
                        + "Untersuche immer das aktuelle Paar und entscheide, ob die beiden Container getauscht werden.",
                    portraitPathFor(player),
                    () -> {},
                    player.id()));
  }

  private String sortDisplayText() {
    if (sortCompleted) return "Sortierung abgeschlossen";
    int left = sortValues[sortInnerIndex];
    int right = sortValues[sortInnerIndex + 1];
    return "Aktueller Vergleich\nContainer "
        + sortInnerIndex
        + ": ["
        + left
        + "]\nContainer "
        + (sortInnerIndex + 1)
        + ": ["
        + right
        + "]";
  }

  private void showSortChoice(Entity display, Entity player) {
    DialogFactory.showMultipleChoiceDialog(
        sortDisplayText(),
        "Bubble-Sort-Vergleich",
        ChoiceOption.ofList("TAUSCHEN", "NICHT TAUSCHEN"),
        true,
        payload -> {
          if (payload instanceof DialogResponseMessage.StringValue(String choice)) {
            applySortChoice("TAUSCHEN".equals(choice), player);
          }
        },
        () -> {},
        player.id());
  }

  void applySortChoice(boolean swap, Entity player) {
    if (sortCompleted || bubbleSort.running()) return;
    boolean shouldSwap = sortValues[sortInnerIndex] > sortValues[sortInnerIndex + 1];
    if (swap != shouldSwap) {
      resetSortStation();
      DialogUtils.showTextPopup(
          "Falsche Entscheidung. Die Sortiermaschine setzt den Datensatz zurück.",
          "Datenspeicher",
          player.id());
      Game.audio().playGlobal(SoundSpec.builder("retro_event_wrong"));
      return;
    }
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
      spawnSortProgramStick();
    }
  }

  /** Restores the original values and positions after a wrong comparison decision. */
  private void resetSortStation() {
    System.arraycopy(new int[] {42, 17, 8, 31, 23}, 0, sortValues, 0, sortValues.length);
    sortOuterIndex = 0;
    sortInnerIndex = 0;
    sortCompleted = false;
    for (int index = 0; index < sortData.length; index++) {
      sortData[index] = sortOriginalData[index];
      moveSortEntity(sortData[index], sortPoints[index]);
    }
    updateSortDisplay();
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
      EntityFactory.updateDisplayText(sortDisplay, sortDisplayText());
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
                                sortValue(data),
                                Color.CYAN,
                                TextureMap.instance()
                                    .textureAt(new SimpleIPath("objects/tech/CryoBox.png")))
                            .animMagnitude(0));
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
    } catch (RuntimeException ignored) {
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
