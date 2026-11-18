package rooms.morphco.level;

import engine.Entity;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.sound.SoundSpec;
import feature.components.InventoryComponent;
import feature.components.UIComponent;
import feature.emote.Emote;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.inventory.Item;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import rooms.morphco.items.CircumfixFragmentItem;
import rooms.morphco.puzzles.CircumfixPuzzle;
import rooms.morphco.ui.MorphCoDialogs;
import rooms.morphco.util.MorphCoAchievements;
import rooms.morphco.util.MorphCoText;
import rooms.morphco.util.MorphCoTracking;

/**
 * The circumfixes of A-12.
 *
 * <p>Error 204 took the circumfixes of A-12 apart; their six parts lie in the pile of odds and
 * ends. At the assembly station they become {ge- ... -e}, {be- ... -ig} and {ge- ... -en} again and
 * go into the magazine of A-12, whose lever only moves with all three. The parts live in the
 * inventory of the player who took them. The pile hands out every part that is neither assembled
 * nor carried by a connected player, so parts lost with a player who left or with a loaded savegame
 * come back. A player who enters the game, including one who rejoins, carries no parts. The station
 * stays open after an assembly and shows the result itself.
 */
final class CircumfixStation {

  private static final String STATION = "world.a12.station-title";
  private static final String PILE = "world.a12.pile-title";

  private final MorphCoLevel level;
  private final MorphCoState state;

  CircumfixStation(MorphCoLevel level) {
    this.level = level;
    this.state = level.state();
  }

  /** Creates the pile, the station and the boxes next to it. */
  void spawn() {
    on("pile", (self, player) -> pile(player));
    on("station", (self, player) -> station(player));
    for (int index = 0; index < 2; index++) {
      on(
          "boxes" + index,
          (self, player) -> MorphCoProps.popup(player, "world.a12.boxes-title", "world.a12.boxes"));
    }
  }

  private static void on(String name, BiConsumer<Entity, Entity> onInteract) {
    MorphCoObjects.on(name, entity -> MorphCoProps.interact(entity, onInteract));
  }

  private void pile(Entity player) {
    if (inPile(state, Game.allPlayers()).isEmpty()) {
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
    Optional<InventoryComponent> inventory = player.fetch(InventoryComponent.class);
    if (inventory.isEmpty()) return;
    // Parts that do not fit into the inventory stay in the pile.
    boolean added = false;
    for (CircumfixPuzzle.Fragment fragment : inPile(state, Game.allPlayers())) {
      added |= inventory.get().add(CircumfixFragmentItem.of(fragment));
    }
    if (!added) return;
    level.observe(MorphCoState.PILE_TAKEN);
    MorphCoTracking.started(MorphCoTracking.Puzzle.CIRCUMFIX);
    Game.audio().playGlobal(SoundSpec.builder("kenney_ui_drop_001").targets(player.id()));
    MorphCoProps.popupReaction(player, Emote.QUESTION, PILE, "world.a12.pile-taken");
  }

  /**
   * Returns the parts in the pile: those of the circumfixes not assembled yet that no player
   * carries.
   *
   * @param state room state with the assembled circumfixes
   * @param players players whose inventories hold parts
   * @return parts in the order of the pile
   */
  static List<CircumfixPuzzle.Fragment> inPile(MorphCoState state, Stream<Entity> players) {
    List<CircumfixPuzzle.Fragment> parts = new ArrayList<>(CircumfixPuzzle.PILE);
    for (CircumfixPuzzle.Circumfix circumfix : state.assembled()) {
      parts.remove(circumfix.front());
      parts.remove(circumfix.back());
    }
    players
        .flatMap(player -> parts(player).stream())
        .forEach(part -> parts.remove(part.fragment()));
    return parts;
  }

  /**
   * Puts all parts a player carries back into the pile and closes their station dialog.
   *
   * <p>A rejoining player gets back their old hero with its inventory. The pile has handed out
   * those parts again in the meantime, so the hero must not keep them. The server would also show
   * the station dialog again with the old parts.
   *
   * @param player hero of a player who just entered the game
   */
  static void returnToPile(Entity player) {
    UIUtils.closeDialogs(MorphCoDialogs.STATION, player);
    player
        .fetch(InventoryComponent.class)
        .ifPresent(inventory ->
            inventory.remove(
                CircumfixFragmentItem.class, inventory.count(CircumfixFragmentItem.class)));
  }

  private void station(Entity player) {
    Set<CircumfixPuzzle.Circumfix> assembled = state.assembled();
    if (state.allAssembled()) {
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
            .put(DialogContextKeys.ENTITY, player.id())
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
            assemble(answer, player);
          }
        });
  }

  private static List<CircumfixFragmentItem> parts(Entity player) {
    return player
        .fetch(InventoryComponent.class)
        .map(inventory ->
            inventory.items(CircumfixFragmentItem.class).stream()
                .map(CircumfixFragmentItem.class::cast)
                .toList())
        .orElse(List.of());
  }

  private void assemble(String answer, Entity player) {
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
    if (!level.assemble(circumfix.get())) return;
    Game.audio().playGlobal(SoundSpec.builder("kenney_ui_confirmation_002"));
    if (state.allAssembled()) {
      MorphCoProps.emote(player, Emote.STARS);
      MorphCoTracking.solved(MorphCoTracking.Puzzle.CIRCUMFIX);
      MorphCoAchievements.COMBINATORIAL.unlock();
      if (!state.leverStuck()) MorphCoTracking.started(MorphCoTracking.Puzzle.LEVER);
      // Open close-ups of A-12 still show the empty magazine.
      UIUtils.closeDialogs(MorphCoDialogs.AFFIX_SORTER, context -> true);
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
