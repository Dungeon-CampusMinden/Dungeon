package rooms.morphco.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import engine.Game;
import engine.utils.Cursors;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.HeadlessDialogGroup;
import java.util.ArrayList;
import java.util.List;
import rooms.morphco.util.MorphCoText;

/**
 * "100 anspruchsvolle Hamsterwitze für den täglichen Gebrauch" from the shelf of A-12.
 *
 * <p>The book falls open at the joke the server picked, see {@link MorphCoDialogs#JOKE}, next to
 * the title page. Then come the contents, the six chapters and the hamster wisdom of the day.
 */
final class HamsterBookUI extends BookUI {

  private static final Color LINK = Color.valueOf("1f5fa8");
  // First joke of each chapter.
  private static final int[] CHAPTERS = {1, 21, 41, 61, 76, 91};

  private final float numberWidth;

  private HamsterBookUI(DialogContext context) {
    super(context);
    Label widest = MorphCoUI.bold(MorphCoDialogs.HAMSTER_JOKES + ".", 19, MorphCoUI.INK);
    widest.setWrap(false);
    numberWidth = widest.getPrefWidth();
    sections.add(opening(context.require(MorphCoDialogs.JOKE, Integer.class)));
    sections.add(titlePage());
    List<List<Table>> chapters = new ArrayList<>();
    for (int chapter = 1; chapter <= CHAPTERS.length; chapter++) chapters.add(chapter(chapter));
    sections.add(contents(chapters));
    sections.addAll(chapters);
    sections.add(wisdom());
  }

  /**
   * Builds the dialog or a headless placeholder on the server.
   *
   * @param context dialog context with {@link MorphCoDialogs#JOKE}
   * @return dialog group
   */
  static Group build(DialogContext context) {
    if (Game.isHeadless()) return new HeadlessDialogGroup();
    return new HamsterBookUI(context);
  }

  private static String t(String key, Object... values) {
    return MorphCoText.text("hamster-jokes." + key, values);
  }

  // Number and joke stay together on the first page, even joke 100 in English.
  private static List<Table> opening(int joke) {
    Table opening = block(MorphCoUI.bold(t("number", joke), 24, MorphCoUI.INK), 0);
    opening.row().padTop(12);
    opening.add(MorphCoUI.rich(t("joke-" + joke), 19, MorphCoUI.INK)).growX();
    return List.of(opening);
  }

  private static List<Table> titlePage() {
    return List.of(
        block(MorphCoUI.bold(MorphCoText.text("world.a12.book-hamster"), 28, MorphCoUI.INK), 16),
        block(MorphCoUI.rich(t("subtitle"), 19, MorphCoUI.INK), 0));
  }

  // Each chapter of the contents links to the first page of the chapter.
  private List<Table> contents(List<List<Table>> chapters) {
    List<Table> blocks = new ArrayList<>();
    blocks.add(block(MorphCoUI.bold(t("contents"), 22, MorphCoUI.INK), 14));
    for (int chapter = 1; chapter <= chapters.size(); chapter++) {
      Table target = chapters.get(chapter - 1).getFirst();
      Label link = MorphCoUI.label(t("chapter-" + chapter), 19, LINK);
      link.setTouchable(Touchable.enabled);
      link.setUserObject(Cursors.INTERACT);
      link.addListener(
          new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              turn(target);
            }
          });
      blocks.add(block(link, 10));
    }
    return blocks;
  }

  private List<Table> chapter(int chapter) {
    List<Table> blocks = new ArrayList<>();
    blocks.add(block(MorphCoUI.bold(t("chapter-" + chapter), 22, MorphCoUI.INK), 16));
    int last = chapter < CHAPTERS.length ? CHAPTERS[chapter] - 1 : MorphCoDialogs.HAMSTER_JOKES;
    for (int joke = CHAPTERS[chapter - 1]; joke <= last; joke++) {
      Label number = MorphCoUI.bold(joke + ".", 19, MorphCoUI.INK);
      number.setWrap(false);
      number.setAlignment(Align.topRight);
      Table block = new Table();
      block.add(number).width(numberWidth).fillX().top().padRight(12);
      block.add(MorphCoUI.rich(t("joke-" + joke), 19, MorphCoUI.INK)).growX();
      block.padBottom(12);
      blocks.add(block);
    }
    return blocks;
  }

  private static List<Table> wisdom() {
    return List.of(
        block(MorphCoUI.bold(t("wisdom-title"), 24, MorphCoUI.INK), 14),
        block(MorphCoUI.rich(t("wisdom"), 22, MorphCoUI.INK), 0));
  }
}
