package rooms.morphco.ui;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import engine.Game;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.HeadlessDialogGroup;
import rooms.morphco.util.MorphCoText;

/**
 * Books from the shelves of the factory, shown as a sheet of paper on the work area.
 *
 * <p>{@link MorphCoDialogs#HANDBOOK} is the manual of the inflection machine. {@link
 * MorphCoDialogs#MORPH_1} and {@link MorphCoDialogs#MORPH_2} are the two volumes of "Morphemtypen
 * und wo sie zu finden sind", the classification handout of the room split into free and bound
 * morphemes.
 */
final class DocumentUI extends PaperUI {

  private static final String HANDBOOK = MorphCoDialogs.HANDBOOK;
  private static final String MORPH_1 = MorphCoDialogs.MORPH_1;
  private static final String MORPH_2 = MorphCoDialogs.MORPH_2;

  private DocumentUI(DialogContext context, String document) {
    super(context, title(document));
    sheet.defaults().growX().left();
    switch (document) {
      case MORPH_1 -> freeMorphemes();
      case MORPH_2 -> boundMorphemes();
      default -> sheet.add(MorphCoUI.rich(t("handbook"), 20, MorphCoUI.INK)).row();
    }
  }

  /**
   * Builds the dialog or a headless placeholder on the server.
   *
   * @param context dialog context with {@link MorphCoDialogs#DOCUMENT_ID}
   * @return dialog group
   */
  static Group build(DialogContext context) {
    if (Game.isHeadless()) return new HeadlessDialogGroup();
    return new DocumentUI(
        context, context.find(MorphCoDialogs.DOCUMENT_ID, String.class).orElse(HANDBOOK));
  }

  private static String title(String document) {
    return switch (document) {
      case MORPH_1 -> t("morph1-title");
      case MORPH_2 -> t("morph2-title");
      default -> t("handbook-title");
    };
  }

  private static String t(String key) {
    return MorphCoText.text("documents." + key);
  }

  private void header() {
    sheet.add(MorphCoUI.bold(t("morph-title"), 24, MorphCoUI.INK)).colspan(2).padBottom(4).row();
    sheet.add(MorphCoUI.rich(t("morph-header"), 17, MorphCoUI.INK)).colspan(2).padBottom(14).row();
  }

  private void footer(String note) {
    sheet.add(MorphCoUI.rich(note, 19, MorphCoUI.INK)).colspan(2).padTop(14).padBottom(14).row();
    sheet.add(MorphCoUI.rich(t("source"), 15, MorphCoUI.INK)).colspan(2).row();
  }

  private void freeMorphemes() {
    header();
    cell(t("morph-free"), true, 2);
    cell(t("morph-lexical"), true, 1);
    cell(t("morph-grammatical"), true, 1);
    for (String[] pair :
        new String[][] {
          {"free-verbal", "free-pronoun"},
          {"free-noun", "free-preposition"},
          {"free-adjective", "free-conjunction"},
          {"free-adverb", "free-article"}
        }) {
      cell(t(pair[0]), false, 1);
      cell(t(pair[1]), false, 1);
    }
    footer(t("portmanteau"));
  }

  private void boundMorphemes() {
    header();
    cell(t("morph-bound"), true, 2);
    cell(t("morph-lexical"), true, 1);
    cell(t("morph-grammatical"), true, 1);
    cell(t("bound-unique") + "[n][n]" + t("bound-verbal"), false, 1);
    cell(t("bound-inflection"), false, 1);
    cell(t("bound-derivation"), false, 2);
    footer(t("definitions"));
  }

  // A table cell of the handout; a full row ends after two columns.
  private void cell(String content, boolean heading, int colspan) {
    Label label = MorphCoUI.rich(content, heading ? 19 : 17, MorphCoUI.INK);
    BaseDrawable background =
        MorphCoUI.fill(heading ? MorphCoUI.PAPER_HEADING : MorphCoUI.PAPER_CELL);
    background.setLeftWidth(10);
    background.setRightWidth(10);
    background.setTopHeight(8);
    background.setBottomHeight(8);
    label.getStyle().background = background;
    var cell = sheet.add(label).colspan(colspan).uniformX().fill().pad(2);
    if (colspan == 2 || cell.getColumn() == 1) cell.row();
  }
}
