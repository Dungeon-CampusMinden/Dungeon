package rooms.morphco.level;

import engine.Entity;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.sound.SoundSpec;
import engine.utils.Rectangle;
import feature.components.InventoryComponent;
import feature.components.UIComponent;
import feature.entities.deco.Deco;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogFactory;
import feature.inventory.Item;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import rooms.morphco.items.CircumfixFragmentItem;
import rooms.morphco.puzzles.CircumfixPuzzle;
import rooms.morphco.ui.MorphCoDialogs;
import rooms.morphco.util.MorphCoAchievements;
import rooms.morphco.util.MorphCoText;
import rooms.morphco.util.MorphCoTracking;

/**
 * The optional circumfix side quest in A-12.
 *
 * <p>The pile of odds and ends hands out six broken circumfix parts. At the assembly station they
 * become {ge- ... -e}, {be- ... -ig} and {ge- ... -en} again. The parts live in the inventory of
 * the player who took them; a loaded savegame hands out the parts that were not assembled yet
 * again.
 */
final class CircumfixStation {

  private static final String STATION = "world.a12.station-title";
  private static final String PILE = "world.a12.pile-title";

  private final MorphCoLevel level;
  private final MorphCoState state;
  private boolean pileTaken;

  CircumfixStation(MorphCoLevel level) {
    this.level = level;
    this.state = level.state();
  }

  /** Creates the pile, the station and the boxes next to it. */
  void spawn() {
    // Inventories are not part of the checkpoint, so a loaded run can take the parts again.
    pileTaken = false;
    MorphCoProps.texture(
        "pile",
        level.point("pile"),
        MorphCoProps.ART + "pile.png",
        1.5f,
        new Rectangle(2f, 0.5f, 0.12f, 0f),
        (self, player) -> pile(player));
    MorphCoProps.texture(
        "station",
        level.point("station"),
        MorphCoProps.ART + "station.png",
        1.5f,
        new Rectangle(2.9f, 0.6f, 0.05f, 0f),
        (self, player) -> station(player));
    MorphCoProps.deco(
        "station-chair", level.point("station_chair"), Deco.ChairGrey, (s, p) -> station(p));
    for (int index = 0; index < 2; index++) {
      MorphCoProps.deco(
          "boxes" + index,
          level.point("boxes" + index),
          Deco.SoulweaverSmallCrate,
          (self, player) -> MorphCoProps.popup(player, "world.a12.boxes-title", "world.a12.boxes"));
    }
  }

  private void pile(Entity player) {
    if (pileTaken || missing().isEmpty()) {
      MorphCoProps.popup(player, PILE, "world.a12.pile-empty");
      return;
    }
    DialogFactory.showYesNoDialog(
        MorphCoText.key("world.a12.pile"),
        MorphCoText.key(PILE),
        () -> take(player),
        () -> {},
        player.id());
  }

  private void take(Entity player) {
    if (pileTaken) return;
    Optional<InventoryComponent> inventory = player.fetch(InventoryComponent.class);
    if (inventory.isEmpty()) return;
    List<CircumfixPuzzle.Fragment> parts = missing();
    boolean added = false;
    for (CircumfixPuzzle.Fragment fragment : parts) {
      added |= inventory.get().add(CircumfixFragmentItem.of(fragment));
    }
    if (!added) return;
    pileTaken = true;
    level.observe(MorphCoState.PILE_TAKEN);
    MorphCoTracking.started(MorphCoTracking.Puzzle.CIRCUMFIX);
    Game.audio().playGlobal(SoundSpec.builder("kenney_ui_drop_001").targets(player.id()));
    MorphCoProps.popup(player, PILE, "world.a12.pile-taken");
  }

  /**
   * Returns the parts that are still needed for the circumfixes that have not been assembled.
   *
   * @return parts in the order of the pile
   */
  private List<CircumfixPuzzle.Fragment> missing() {
    List<CircumfixPuzzle.Fragment> parts = new ArrayList<>(CircumfixPuzzle.PILE);
    for (CircumfixPuzzle.Circumfix circumfix : state.assembled()) {
      parts.remove(circumfix.front());
      parts.remove(circumfix.back());
    }
    return parts;
  }

  private void station(Entity player) {
    Set<CircumfixPuzzle.Circumfix> assembled = state.assembled();
    if (assembled.size() == CircumfixPuzzle.Circumfix.values().length) {
      MorphCoProps.popup(player, STATION, "world.a12.station-done");
      return;
    }
    List<CircumfixFragmentItem> parts = parts(player);
    if (parts.isEmpty()) {
      MorphCoProps.popup(player, STATION, "world.a12.station-empty");
      return;
    }
    DialogContext context =
        DialogContext.builder()
            .type(MorphCoDialogs.STATION)
            .put(
                MorphCoDialogs.FRAGMENTS,
                parts.stream().map(part -> part.fragment().name()).toArray(String[]::new))
            .put(
                MorphCoDialogs.ASSEMBLED, assembled.stream().map(Enum::name).toArray(String[]::new))
            .build();
    UIComponent ui = DialogFactory.show(context, player.id());
    ui.registerCallback(
        MorphCoDialogs.SUBMIT,
        payload -> {
          if (payload instanceof DialogResponseMessage.StringValue(String answer)) {
            assemble(answer, player, ui);
          }
        });
  }

  private static List<CircumfixFragmentItem> parts(Entity player) {
    return player
        .fetch(InventoryComponent.class)
        .map(
            inventory ->
                inventory.items(CircumfixFragmentItem.class).stream()
                    .map(CircumfixFragmentItem.class::cast)
                    .toList())
        .orElse(List.of());
  }

  private void assemble(String answer, Entity player, UIComponent ui) {
    String[] bowls = answer.split("\\|");
    if (bowls.length != 2) return;
    Optional<CircumfixPuzzle.Fragment> left = CircumfixPuzzle.Fragment.parse(bowls[0]);
    Optional<CircumfixPuzzle.Fragment> right = CircumfixPuzzle.Fragment.parse(bowls[1]);
    if (left.isEmpty() || right.isEmpty()) return;
    Optional<CircumfixPuzzle.Circumfix> circumfix =
        CircumfixPuzzle.combine(left.get(), right.get());
    MorphCoTracking.attempt(
        MorphCoTracking.Puzzle.CIRCUMFIX,
        "assembly-station",
        "pair",
        answer,
        circumfix.isPresent(),
        player);
    if (circumfix.isEmpty()) {
      Game.audio().playGlobal(SoundSpec.builder("kenney_ui_error_003").targets(player.id()));
      return;
    }
    if (!consume(player, left.get(), right.get())) return;
    UIUtils.closeDialog(ui, true);
    if (!state.assemble(circumfix.get())) return;
    level.save();
    Game.audio().playGlobal(SoundSpec.builder("kenney_ui_confirmation_002"));
    boolean complete = state.assembled().size() == CircumfixPuzzle.Circumfix.values().length;
    MorphCoProps.popupThen(
        player,
        STATION,
        "world.a12.station-assembled",
        () -> {
          if (complete) MorphCoProps.popup(player, STATION, "world.a12.station-complete");
        },
        circumfix.get().label(),
        circumfix.get().example());
    if (complete) {
      MorphCoTracking.solved(MorphCoTracking.Puzzle.CIRCUMFIX);
      MorphCoAchievements.COMBINATORIAL.unlock();
    }
  }

  private static boolean consume(
      Entity player, CircumfixPuzzle.Fragment left, CircumfixPuzzle.Fragment right) {
    InventoryComponent inventory = player.fetch(InventoryComponent.class).orElse(null);
    if (inventory == null) return false;
    List<CircumfixFragmentItem> parts = parts(player);
    Item leftItem = parts.stream().filter(part -> part.fragment() == left).findFirst().orElse(null);
    Item rightItem =
        parts.stream()
            .filter(part -> part.fragment() == right && part != leftItem)
            .findFirst()
            .orElse(null);
    if (leftItem == null || rightItem == null) return false;
    inventory.remove(leftItem);
    inventory.remove(rightItem);
    return true;
  }
}
