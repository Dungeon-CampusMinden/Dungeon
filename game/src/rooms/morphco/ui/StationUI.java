package rooms.morphco.ui;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import engine.Game;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.HeadlessDialogGroup;
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
final class StationUI extends MorphCoPanel {

  private static final String LEFT = "left";
  private static final String RIGHT = "right";

  private final SortBoard board;

  private StationUI(DialogContext context) {
    super(
        context,
        MorphCoText.text("ui.station.title"),
        MorphCoText.text("ui.station.instruction"),
        "");
    board = new SortBoard(skin, this::clearFeedback);

    String[] fragments =
        context.find(MorphCoDialogs.FRAGMENTS, String[].class).orElse(new String[0]);
    HorizontalGroup tray = new HorizontalGroup();
    tray.space(10);
    for (int index = 0; index < fragments.length; index++) {
      int position = index;
      CircumfixPuzzle.Fragment.parse(fragments[index])
          .ifPresent(
              fragment -> {
                String id = fragment.name() + "#" + position;
                board.item(id, fragment.label());
                tray.addActor(board.homeChip(id));
              });
    }
    body.add(text(MorphCoText.text("ui.station.inventory"), 20)).left().row();
    body.add(tray).left().padBottom(16).row();

    Table bowls = new Table();
    bowls.defaults().pad(6).grow().uniformX().width(contentWidth() / 3f - 14f);
    bowls.add(board.bin(LEFT, MorphCoText.text("ui.station.left"), 1, 80));
    Table middle = new Table(skin);
    middle.setBackground("generic-area");
    middle.pad(8).top().left();
    middle.defaults().left();
    middle
        .add(text(MorphCoText.text("ui.station.middle"), 20, contentWidth() / 3f - 30f))
        .left()
        .row();
    String[] assembled =
        context.find(MorphCoDialogs.ASSEMBLED, String[].class).orElse(new String[0]);
    if (assembled.length == 0) {
      middle
          .add(text(MorphCoText.text("ui.station.empty"), 18, contentWidth() / 3f - 30f))
          .left()
          .row();
    }
    for (String name : assembled) {
      for (CircumfixPuzzle.Circumfix circumfix : CircumfixPuzzle.Circumfix.values()) {
        if (circumfix.name().equals(name)) {
          middle
              .add(
                  text(
                      circumfix.label() + " (" + circumfix.example() + ")",
                      20,
                      contentWidth() / 3f - 30f))
              .left()
              .row();
        }
      }
    }
    bowls.add(middle);
    bowls.add(board.bin(RIGHT, MorphCoText.text("ui.station.right"), 1, 80));
    body.add(bowls).growX();
    board.refresh();

    button("ui.close", "blue-outline", this::close);
    button("ui.station.assemble", "green", this::assemble);
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

  private Optional<CircumfixPuzzle.Fragment> inBowl(Map<String, String> placement, String bowl) {
    return placement.entrySet().stream()
        .filter(entry -> entry.getValue().equals(bowl))
        .findFirst()
        .flatMap(entry -> CircumfixPuzzle.Fragment.parse(entry.getKey().split("#")[0]));
  }

  private void assemble() {
    Map<String, String> placement = board.placement();
    Optional<CircumfixPuzzle.Fragment> left = inBowl(placement, LEFT);
    Optional<CircumfixPuzzle.Fragment> right = inBowl(placement, RIGHT);
    if (left.isEmpty() || right.isEmpty()) {
      feedback(MorphCoText.text("world.a12.station-missing"), false);
      return;
    }
    submit(left.get().name() + "|" + right.get().name());
    if (CircumfixPuzzle.combine(left.get(), right.get()).isEmpty()) {
      feedback(MorphCoText.text("world.a12.station-wrong"), false);
    }
  }
}
