package rooms.morphco.ui;

import com.badlogic.gdx.scenes.scene2d.Group;
import engine.Game;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.HeadlessDialogGroup;
import feature.interaction.sorting.SortBoard;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import rooms.morphco.puzzles.AffixPuzzle;
import rooms.morphco.util.MorphCoText;

/** The input shafts of A-12: freed affixes go to word formation or to inflection. */
final class ShaftUI extends MorphCoConsoleUI {

  private static final float MARGIN = 24;

  private final SortBoard board =
      new MorphCoSortBoard(area(), MorphCoSortBoard.Look.SHAFT, this::changed);
  private final PanelNode tray = area().addNode(new PanelNode("tray", PanelNode.Look.BELT, 10));
  private final PanelNode housing =
      area().addNode(new PanelNode("housing", PanelNode.Look.PLATE, 0));
  private final List<SortBoard.Slot> slots = new ArrayList<>();
  private final List<SortBoard.Bin> shafts = new ArrayList<>();

  private ShaftUI(DialogContext context) {
    super(
        context,
        MorphCoText.text("ui.affixes.shafts-title"),
        MorphCoText.text("ui.affixes.shafts-instruction"),
        MorphCoText.text("ui.affixes.shafts-help"));
    housing.z(-1);
    for (AffixPuzzle.FreedAffix affix : AffixPuzzle.affixes()) {
      slots.add(board.addItem(affix.id(), affix.label(), 21));
    }
    for (AffixPuzzle.Shaft shaft : AffixPuzzle.Shaft.values()) {
      shafts.add(
          board.addBin(shaft.name(), MorphCoText.text("ui.affixes." + shaft.name()), 0, 220));
    }
    button("ui.reset", false, board::clear);
    button("ui.check", true, this::check);
  }

  /**
   * Builds the dialog or a headless placeholder on the server.
   *
   * @param context dialog context
   * @return dialog group
   */
  static Group build(DialogContext context) {
    if (Game.isHeadless()) return new HeadlessDialogGroup();
    return new ShaftUI(context);
  }

  @Override
  protected float arrange(float width, float height) {
    float top = -MorphCoSortBoard.belt(tray, slots, 0, width) - 24;
    // The shafts open in the top of the sorter's housing.
    float machine =
        SortBoard.grid(shafts, shafts.size(), MARGIN, top - MARGIN, width - 2 * MARGIN)
            + 2 * MARGIN;
    housing.size(width, machine).position(0, top - machine);
    board.arrange();
    return -top + machine;
  }

  private void check() {
    Map<String, AffixPuzzle.Shaft> placement = new LinkedHashMap<>();
    board.placement().forEach((id, shaft) -> placement.put(id, AffixPuzzle.Shaft.valueOf(shaft)));
    submit(AffixPuzzle.encodeShafts(placement));
    if (AffixPuzzle.checkShafts(placement)) {
      feedback(MorphCoText.text("world.a12.shafts-correct"), true);
    } else {
      feedback(
          MorphCoText.text("ui.affixes.shafts-feedback", AffixPuzzle.wrongShafts(placement)),
          false);
    }
  }
}
