package rooms.morphco.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import engine.Game;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.HeadlessDialogGroup;
import rooms.morphco.util.MorphCoText;

/**
 * Books from the shelves of the factory.
 *
 * <p>{@link MorphCoDialogs#HANDBOOK} is the manual of the inflection machine. {@link
 * MorphCoDialogs#MORPH_1} and {@link MorphCoDialogs#MORPH_2} are the two volumes of "Morphemtypen
 * und wo sie zu finden sind", the classification handout of the room split into free and bound
 * morphemes.
 */
final class DocumentUI extends MorphCoPanel {

  private static final Color HEADING = new Color(0.8f, 0.84f, 0.9f, 1f);

  private static final String HANDBOOK = MorphCoDialogs.HANDBOOK;
  private static final String MORPH_1 = MorphCoDialogs.MORPH_1;
  private static final String MORPH_2 = MorphCoDialogs.MORPH_2;

  private DocumentUI(DialogContext context, String document) {
    super(context, title(document), "", "");
    body.defaults().growX().left();
    switch (document) {
      case MORPH_1 -> freeMorphemes();
      case MORPH_2 -> boundMorphemes();
      default -> body.add(text(t("handbook"), 22)).row();
    }
    button("documents.close", "green", this::close);
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
    body.add(text("[size=26]" + t("morph-title"), 26)).padBottom(4).row();
    body.add(text(t("morph-header"), 18)).padBottom(12).row();
  }

  private void freeMorphemes() {
    header();
    Table table = new Table(skin);
    table.defaults().grow().uniformX().pad(2);
    table.add(cell("[size=22]" + t("morph-free"), true, true)).colspan(2).row();
    table.add(cell(t("morph-lexical"), true));
    table.add(cell(t("morph-grammatical"), true)).row();
    table.add(cell(t("free-verbal"), false));
    table.add(cell(t("free-pronoun"), false)).row();
    table.add(cell(t("free-noun"), false));
    table.add(cell(t("free-preposition"), false)).row();
    table.add(cell(t("free-adjective"), false));
    table.add(cell(t("free-conjunction"), false)).row();
    table.add(cell(t("free-adverb"), false));
    table.add(cell(t("free-article"), false)).row();
    body.add(table).padBottom(12).row();
    body.add(text(t("portmanteau"), 20)).padBottom(12).row();
    body.add(text("[color=black]" + t("source"), 16)).row();
  }

  private void boundMorphemes() {
    header();
    Table table = new Table(skin);
    table.defaults().grow().uniformX().pad(2);
    table.add(cell("[size=22]" + t("morph-bound"), true, true)).colspan(2).row();
    table.add(cell(t("morph-lexical"), true));
    table.add(cell(t("morph-grammatical"), true)).row();
    table.add(cell(t("bound-unique") + "[n][n]" + t("bound-verbal"), false));
    table.add(cell(t("bound-inflection"), false)).row();
    table.add(cell(t("bound-derivation"), false, true)).colspan(2).row();
    body.add(table).padBottom(12).row();
    body.add(text(t("definitions"), 20)).padBottom(12).row();
    body.add(text("[color=black]" + t("source"), 16)).row();
  }

  private Table cell(String content, boolean heading) {
    return cell(content, heading, false);
  }

  private Table cell(String content, boolean heading, boolean wide) {
    Table cell = new Table(skin);
    if (heading) cell.setBackground(skin.newDrawable("white", HEADING));
    else cell.setBackground("generic-area");
    cell.pad(8).top().left();
    float width = wide ? contentWidth() - 50f : contentWidth() / 2f - 40f;
    cell.add(text(content, heading ? 20 : 18, width)).growX().left();
    return cell;
  }
}
