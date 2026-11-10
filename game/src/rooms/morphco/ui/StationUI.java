package rooms.morphco.ui;

import com.badlogic.gdx.scenes.scene2d.Group;
import engine.Game;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.HeadlessDialogGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import rooms.morphco.puzzles.CircumfixPuzzle;
import rooms.morphco.util.MorphCoText;

/**
 * The circumfix assembly station: a left bowl, a right bowl and a larger bowl in the middle.
 *
 * <p>The tray shows the circumfix parts in the inventory of the player at the station. Assembling
 * sends {@code LEFT|RIGHT} to the server, which removes the parts from the inventory.
 */
final class StationUI extends MorphCoConsoleUI {

  private static final String LEFT = "left";
  private static final String RIGHT = "right";

  private final SortBoard board = new SortBoard(area(), this::changed);
  private final PanelNode tray = area().addNode(new PanelNode("tray", PanelNode.Look.BELT, 10));
  private final List<SortBoard.Slot> slots = new ArrayList<>();
  private final SortBoard.Bin left;
  private final SortBoard.Bin right;
  private final PanelNode middle = area().addNode(new PanelNode("middle", PanelNode.Look.PLATE, 0));

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
      int position = index;
      CircumfixPuzzle.Fragment.parse(fragments[index])
          .ifPresent(
              fragment ->
                  slots.add(board.item(fragment.name() + "#" + position, fragment.label(), 21)));
    }

    left = board.bin(LEFT, MorphCoText.text("ui.station.left"), 1, 150);
    right = board.bin(RIGHT, MorphCoText.text("ui.station.right"), 1, 150);
    // The same edges as the name plates of the bowls next to it.
    middle.content().pad(6, 12, 12, 12);
    middle.content().defaults().left().growX().padBottom(6);
    middle
        .content()
        .add(MorphCoUI.tag(MorphCoText.text("ui.station.middle")))
        .height(34)
        .padBottom(14)
        .row();
    String[] assembled =
        context.find(MorphCoDialogs.ASSEMBLED, String[].class).orElse(new String[0]);
    if (assembled.length == 0) {
      middle
          .content()
          .add(MorphCoUI.label(MorphCoText.text("ui.station.empty"), 18, MorphCoUI.MUTED))
          .row();
    }
    for (String name : assembled) {
      for (CircumfixPuzzle.Circumfix circumfix : CircumfixPuzzle.Circumfix.values()) {
        if (circumfix.name().equals(name)) {
          middle
              .content()
              .add(
                  MorphCoUI.bold(
                      circumfix.label() + " (" + circumfix.example() + ")", 19, MorphCoUI.TEXT))
              .row();
        }
      }
    }

    button("ui.close", false, this::requestClose);
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
    float top = -SortBoard.belt(tray, slots, 0, width) - 24;
    float column = (width - 24) / 3;
    float bowls = Math.max(Math.max(left.fit(column), right.fit(column)), middle.fit(column));
    left.size(column, bowls).position(0, top - bowls);
    middle.size(column, bowls).position(column + 12, top - bowls);
    right.size(column, bowls).position(2 * (column + 12), top - bowls);
    board.arrange();
    return -top + bowls;
  }

  private Optional<CircumfixPuzzle.Fragment> inBowl(Map<String, String> placement, String bowl) {
    return placement.entrySet().stream()
        .filter(entry -> entry.getValue().equals(bowl))
        .findFirst()
        .flatMap(entry -> CircumfixPuzzle.Fragment.parse(entry.getKey().split("#")[0]));
  }

  private void assemble() {
    Map<String, String> placement = board.placement();
    Optional<CircumfixPuzzle.Fragment> leftPart = inBowl(placement, LEFT);
    Optional<CircumfixPuzzle.Fragment> rightPart = inBowl(placement, RIGHT);
    if (leftPart.isEmpty() || rightPart.isEmpty()) {
      feedback(MorphCoText.text("world.a12.station-missing"), false);
      return;
    }
    submit(leftPart.get().name() + "|" + rightPart.get().name());
    if (CircumfixPuzzle.combine(leftPart.get(), rightPart.get()).isEmpty()) {
      feedback(MorphCoText.text("world.a12.station-wrong"), false);
    }
  }
}
