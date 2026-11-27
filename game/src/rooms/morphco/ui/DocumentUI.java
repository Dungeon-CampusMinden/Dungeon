package rooms.morphco.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import engine.Game;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.HeadlessDialogGroup;
import feature.hud.elements.RichLabel;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import rooms.morphco.util.MorphCoText;

/**
 * Books from the shelves of the factory, opened like the hamster book.
 *
 * <p>{@link MorphCoDialogs#HANDBOOK} is the manual of the inflection machine. {@link
 * MorphCoDialogs#MORPH_1} and {@link MorphCoDialogs#MORPH_2} are the two volumes of "Morphemtypen
 * und wo sie zu finden sind", the classification handout of the room split into free and bound
 * morphemes. {@link MorphCoDialogs#HANDOUT} holds both tables, the complete edition that replaces
 * both volumes once a player has taken the second one. {@link MorphCoDialogs#COLLECTED} adds the
 * manual in front of them. Each book starts with its title; its paragraphs and table rows flow over
 * the pages.
 */
final class DocumentUI extends BookUI {

  private static final String HANDBOOK = MorphCoDialogs.HANDBOOK;
  private static final String MORPH_1 = MorphCoDialogs.MORPH_1;
  private static final String MORPH_2 = MorphCoDialogs.MORPH_2;
  private static final String HANDOUT = MorphCoDialogs.HANDOUT;
  private static final String COLLECTED = MorphCoDialogs.COLLECTED;
  private static final Pattern PARAGRAPH = Pattern.compile(Pattern.quote("[n][n]"));

  private DocumentUI(DialogContext context, String document) {
    super(context);
    List<Table> blocks = new ArrayList<>();
    blocks.add(block(MorphCoUI.bold(title(document), 24, MorphCoUI.INK), 12));
    switch (document) {
      case MORPH_1 -> blocks.addAll(handout(freeMorphemes(), "portmanteau"));
      case MORPH_2 -> blocks.addAll(handout(boundMorphemes(), "definitions"));
      case HANDOUT -> blocks.addAll(completeHandout());
      case COLLECTED -> {
        blocks.addAll(paragraphs(t("handbook")));
        blocks.add(block(MorphCoUI.bold(t("handout-title"), 24, MorphCoUI.INK), 12));
        blocks.addAll(completeHandout());
      }
      default -> blocks.addAll(paragraphs(t("handbook")));
    }
    sections.add(blocks);
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
      case HANDOUT -> t("handout-title");
      case COLLECTED -> t("collected-title");
      default -> t("handbook-title");
    };
  }

  private static String t(String key) {
    return MorphCoText.text("documents." + key);
  }

  // A page break may fall between two paragraphs of the handbook.
  private static List<Table> paragraphs(String text) {
    return PARAGRAPH
        .splitAsStream(text)
        .map(paragraph -> block(MorphCoUI.rich(paragraph, 20, MorphCoUI.INK), 16))
        .toList();
  }

  private static List<Table> completeHandout() {
    List<Table> rows = new ArrayList<>(freeMorphemes());
    rows.addAll(boundMorphemes());
    return handout(rows, "portmanteau", "definitions");
  }

  // The classification header, the table rows, the notes below the table and the source.
  private static List<Table> handout(List<Table> rows, String... notes) {
    List<Table> blocks = new ArrayList<>();
    blocks.add(block(MorphCoUI.rich(t("morph-header"), 17, MorphCoUI.INK), 14));
    blocks.addAll(rows);
    for (String note : notes) {
      Table paragraph = block(MorphCoUI.rich(t(note), 19, MorphCoUI.INK), 14);
      paragraph.padTop(12);
      blocks.add(paragraph);
    }
    blocks.add(block(MorphCoUI.rich(t("source"), 15, MorphCoUI.INK), 0));
    return blocks;
  }

  private static List<Table> freeMorphemes() {
    List<Table> blocks = new ArrayList<>();
    blocks.add(row(cell(t("morph-free"), true)));
    blocks.add(row(cell(t("morph-lexical"), true), cell(t("morph-grammatical"), true)));
    for (String[] pair :
        new String[][] {
          {"free-verbal", "free-pronoun"},
          {"free-noun", "free-preposition"},
          {"free-adjective", "free-conjunction"},
          {"free-adverb", "free-article"}
        }) {
      blocks.add(row(cell(t(pair[0]), false), cell(t(pair[1]), false)));
    }
    return blocks;
  }

  private static List<Table> boundMorphemes() {
    List<Table> blocks = new ArrayList<>();
    blocks.add(row(cell(t("morph-bound"), true)));
    blocks.add(row(cell(t("morph-lexical"), true), cell(t("morph-grammatical"), true)));
    blocks.add(
        row(
            cell(t("bound-unique") + "[n][n]" + t("bound-verbal"), false),
            cell(t("bound-inflection"), false)));
    blocks.add(row(cell(t("bound-derivation"), false)));
    return blocks;
  }

  // A row of the handout: one cell across both columns or two cells of the same width.
  private static Table row(Actor... cells) {
    Table row = new Table();
    for (Actor cell : cells) row.add(cell).uniformX().grow().pad(2);
    return row;
  }

  private static Container<RichLabel> cell(String content, boolean heading) {
    BaseDrawable background =
        MorphCoUI.fill(heading ? MorphCoUI.PAPER_HEADING : MorphCoUI.PAPER_CELL);
    background.setLeftWidth(10);
    background.setRightWidth(10);
    background.setTopHeight(8);
    background.setBottomHeight(8);
    return new Container<>(MorphCoUI.rich(content, heading ? 19 : 17, MorphCoUI.INK))
        .background(background)
        .fillX()
        .top();
  }
}
