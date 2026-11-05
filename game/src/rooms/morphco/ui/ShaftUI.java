package rooms.morphco.ui;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import engine.Game;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.HeadlessDialogGroup;
import java.util.LinkedHashMap;
import java.util.Map;
import rooms.morphco.puzzles.AffixPuzzle;
import rooms.morphco.util.MorphCoText;

/** The input shafts of A-12: freed affixes go to word formation or to inflection. */
final class ShaftUI extends MorphCoPanel {

  private final SortBoard board;

  private ShaftUI(DialogContext context) {
    super(
        context,
        MorphCoText.text("ui.affixes.shafts-title"),
        MorphCoText.text("ui.affixes.shafts-instruction"),
        MorphCoText.text("ui.affixes.shafts-help"));
    board = new SortBoard(skin, this::clearFeedback);

    HorizontalGroup tray = new HorizontalGroup();
    tray.space(10);
    for (AffixPuzzle.FreedAffix affix : AffixPuzzle.affixes()) {
      board.item(affix.id(), affix.label());
      tray.addActor(board.homeChip(affix.id()));
    }
    body.add(tray).left().padBottom(18).row();

    Table shafts = new Table();
    shafts.defaults().pad(6).grow().uniformX().width(contentWidth() / 2f - 14f);
    for (AffixPuzzle.Shaft shaft : AffixPuzzle.Shaft.values()) {
      shafts.add(board.bin(shaft.name(), MorphCoText.text("ui.affixes." + shaft.name()), 0, 160));
    }
    body.add(shafts).growX();
    board.refresh();

    button("ui.reset", "blue-outline", board::clear);
    button("ui.check", "green", this::check);
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

  private void check() {
    Map<String, AffixPuzzle.Shaft> placement = new LinkedHashMap<>();
    board.placement().forEach((id, shaft) -> placement.put(id, AffixPuzzle.Shaft.valueOf(shaft)));
    submit(AffixPuzzle.encodeShafts(placement));
    if (AffixPuzzle.checkShafts(placement)) {
      feedback(MorphCoText.text("world.a12.shafts-solved"), true);
    } else {
      feedback(
          MorphCoText.text("ui.affixes.shafts-feedback", AffixPuzzle.wrongShafts(placement)),
          false);
    }
  }
}
