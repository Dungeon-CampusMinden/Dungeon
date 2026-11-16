package rooms.morphco.ui;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import engine.Game;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.HeadlessDialogGroup;
import feature.interaction.sorting.SortBoard;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import rooms.morphco.puzzles.CircumfixPuzzle;
import rooms.morphco.util.MorphCoText;

/**
 * The circumfix assembly station: a left bowl, a right bowl and a larger bowl in the middle.
 *
 * <p>The tray shows the circumfix parts in the inventory of the player at the station. Assembling
 * sends {@code LEFT|RIGHT} to the server, which removes the parts from the inventory. The station
 * stays open: a fitting pair leaves the tray and the circumfix appears in the middle bowl.
 */
final class StationUI extends MorphCoConsoleUI {

  private static final String LEFT = "left";
  private static final String RIGHT = "right";

  private final SortBoard board = new MorphCoSortBoard(area(), this::changed);
  private final PanelNode tray = area().addNode(new PanelNode("tray", PanelNode.Look.BELT, 10));
  private final Map<String, SortBoard.Slot> slots = new LinkedHashMap<>();
  private final SortBoard.Bin left;
  private final SortBoard.Bin right;
  private final PanelNode middle = area().addNode(new PanelNode("middle", PanelNode.Look.PLATE, 0));
  private final Table bowl = new Table();
  private final List<CircumfixPuzzle.Circumfix> assembled = new ArrayList<>();

  private StationUI(DialogContext context) {
    super(
        context,
        MorphCoText.text("ui.station.title"),
        MorphCoText.text("ui.station.instruction"),
        "");
    tray.content().add(MorphCoUI.tag(MorphCoText.text("ui.station.inventory"))).padTop(4);
    String[] fragments =
        context.find(MorphCoDialogs.FRAGMENTS, String[].class).orElse(new String[0]);
    for (int index = 0; index < fragments.length; index++) {
      String id = fragments[index] + "#" + index;
      CircumfixPuzzle.Fragment.parse(fragments[index])
          .ifPresent(fragment -> slots.put(id, board.item(id, fragment.label(), 21)));
    }

    left = board.bin(LEFT, MorphCoText.text("ui.station.left"), 1, 150);
    right = board.bin(RIGHT, MorphCoText.text("ui.station.right"), 1, 150);
    // The same edges as the name plates of the bowls next to it.
    middle.content().pad(6, 12, 12, 12);
    middle.content().defaults().left().growX().padBottom(6);
    Table heading = new Table();
    heading.setBackground(MorphCoUI.plate(MorphCoUI.TAG));
    heading.pad(2, 8, 4, 8);
    // Top aligned like the bowls' headings; a table would center the text in the plate.
    heading.top();
    heading.add(MorphCoUI.bold(MorphCoText.text("ui.station.middle"), 18, MorphCoUI.INK)).growX();
    middle.content().add(heading).height(34).padBottom(14).row();
    bowl.defaults().left().growX().padBottom(6);
    middle.content().add(bowl).row();
    String[] done = context.find(MorphCoDialogs.ASSEMBLED, String[].class).orElse(new String[0]);
    for (String name : done) assembled.add(CircumfixPuzzle.Circumfix.valueOf(name));
    showBowl();

    button("ui.station.assemble", true, this::assemble);
  }

  /**
   * Builds the dialog or a headless placeholder on the server.
   *
   * @param context dialog context
   * @return dialog group
   */
  static Group build(DialogContext context) {
    if (Game.isHeadless()) return new HeadlessDialogGroup();
    return new StationUI(context);
  }

  @Override
  protected float arrange(float width, float height) {
    float top = -MorphCoSortBoard.belt(tray, List.copyOf(slots.values()), 0, width) - 24;
    float column = (width - 24) / 3;
    float bowls = Math.max(Math.max(left.fit(column), right.fit(column)), middle.fit(column));
    left.size(column, bowls).position(0, top - bowls);
    middle.size(column, bowls).position(column + 12, top - bowls);
    right.size(column, bowls).position(2 * (column + 12), top - bowls);
    board.arrange();
    return -top + bowls;
  }

  private void showBowl() {
    bowl.clearChildren();
    if (assembled.isEmpty()) {
      bowl.add(MorphCoUI.label(MorphCoText.text("ui.station.empty"), 18, MorphCoUI.MUTED)).row();
    }
    for (CircumfixPuzzle.Circumfix circumfix : assembled) {
      bowl.add(
              MorphCoUI.bold(
                  circumfix.label() + " (" + circumfix.example() + ")", 19, MorphCoUI.TEXT))
          .row();
    }
  }

  private static Optional<String> inBowl(Map<String, String> placement, String bowl) {
    return placement.entrySet().stream()
        .filter(entry -> entry.getValue().equals(bowl))
        .map(Map.Entry::getKey)
        .findFirst();
  }

  private static CircumfixPuzzle.Fragment fragment(String item) {
    return CircumfixPuzzle.Fragment.valueOf(item.split("#")[0]);
  }

  private void assemble() {
    Map<String, String> placement = board.placement();
    Optional<String> leftItem = inBowl(placement, LEFT);
    Optional<String> rightItem = inBowl(placement, RIGHT);
    if (leftItem.isEmpty() || rightItem.isEmpty()) {
      feedback(MorphCoText.text("world.a12.station-missing"), false);
      return;
    }
    CircumfixPuzzle.Fragment leftPart = fragment(leftItem.get());
    CircumfixPuzzle.Fragment rightPart = fragment(rightItem.get());
    submit(leftPart.name() + "|" + rightPart.name());
    Optional<CircumfixPuzzle.Circumfix> circumfix = CircumfixPuzzle.combine(leftPart, rightPart);
    if (circumfix.isEmpty()) {
      feedback(MorphCoText.text("world.a12.station-wrong"), false);
      return;
    }
    for (String item : List.of(leftItem.get(), rightItem.get())) {
      board.removeItems(item);
      slots.remove(item);
    }
    assembled.add(circumfix.get());
    showBowl();
    changed();
    String text =
        MorphCoText.text(
            "world.a12.station-assembled", circumfix.get().label(), circumfix.get().example());
    if (assembled.size() == CircumfixPuzzle.Circumfix.values().length) {
      text += " " + MorphCoText.text("world.a12.station-complete");
    }
    feedback(text, true);
  }
}
