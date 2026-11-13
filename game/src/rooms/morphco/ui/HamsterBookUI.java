package rooms.morphco.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import engine.Game;
import engine.sound.ISound;
import engine.sound.Sounds;
import engine.utils.Cursors;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.HeadlessDialogGroup;
import rooms.morphco.util.MorphCoText;

/**
 * "100 anspruchsvolle Hamsterwitze für den täglichen Gebrauch" from the shelf of A-12.
 *
 * <p>The book falls open at the joke the server picked, see {@link MorphCoDialogs#JOKE}. From
 * there, players turn the pages: the title page with the contents, one page per chapter and the
 * hamster wisdom of the day at the end.
 */
final class HamsterBookUI extends PaperUI {

  private static final Color LINK = Color.valueOf("1f5fa8");
  private static final ISound PAGE_FLIP = new Sound("flipping_through_book", 1);
  // First joke of each chapter.
  private static final int[] CHAPTERS = {1, 21, 41, 61, 76, 91};
  private static final int CONTENTS = 0;
  private static final int WISDOM = CHAPTERS.length + 1;

  private long pageFlip;

  private HamsterBookUI(DialogContext context) {
    super(context, MorphCoText.text("world.a12.book-hamster"));
    sheet.defaults().left().top();
    int joke = context.require(MorphCoDialogs.JOKE, Integer.class);
    line(MorphCoUI.bold(t("number", joke), 24, MorphCoUI.INK), 12);
    line(MorphCoUI.rich(t("joke-" + joke), 22, MorphCoUI.INK), 0);
    pageButton("hamster-jokes.next", true, CONTENTS);
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

  private void turn(int page) {
    sheet.clearChildren();
    clearButtons();
    if (page == CONTENTS) contents();
    else if (page == WISDOM) wisdom();
    else chapter(page);
    if (page > CONTENTS) pageButton("hamster-jokes.back", false, page - 1);
    if (page < WISDOM) pageButton("hamster-jokes.next", true, page + 1);
    Game.soundPlayer().stopByInstance(pageFlip);
    pageFlip = Sounds.playUi(PAGE_FLIP);
    scrollToTop();
    changed();
  }

  // Turning a page plays the page flip instead of a click.
  private void pageButton(String key, boolean primary, int page) {
    Sounds.ownSound(button(key, primary, () -> turn(page)));
  }

  private void contents() {
    line(MorphCoUI.bold(MorphCoText.text("world.a12.book-hamster"), 28, MorphCoUI.INK), 10);
    line(MorphCoUI.rich(t("subtitle"), 19, MorphCoUI.INK), 24);
    line(MorphCoUI.bold(t("contents"), 22, MorphCoUI.INK), 10);
    for (int chapter = 1; chapter <= CHAPTERS.length; chapter++) {
      int target = chapter;
      Label entry = MorphCoUI.label(t("chapter-" + chapter), 19, LINK);
      entry.setTouchable(Touchable.enabled);
      entry.setUserObject(Cursors.INTERACT);
      entry.addListener(
          new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              turn(target);
            }
          });
      line(entry, 8);
    }
  }

  private void chapter(int chapter) {
    line(MorphCoUI.bold(t("chapter-" + chapter), 24, MorphCoUI.INK), 16);
    int last = chapter < CHAPTERS.length ? CHAPTERS[chapter] - 1 : MorphCoDialogs.HAMSTER_JOKES;
    for (int joke = CHAPTERS[chapter - 1]; joke <= last; joke++) {
      Label number = MorphCoUI.bold(joke + ".", 19, MorphCoUI.INK);
      number.setWrap(false);
      number.setAlignment(Align.topRight);
      sheet.add(number).fillX().padRight(12).padBottom(12);
      sheet.add(MorphCoUI.rich(t("joke-" + joke), 19, MorphCoUI.INK)).growX().padBottom(12).row();
    }
  }

  private void wisdom() {
    line(MorphCoUI.bold(t("wisdom-title"), 24, MorphCoUI.INK), 14);
    line(MorphCoUI.rich(t("wisdom"), 22, MorphCoUI.INK), 0);
  }

  // A line across both columns of the sheet.
  private void line(Label label, float padBottom) {
    sheet.add(label).colspan(2).growX().padBottom(padBottom).row();
  }

  private record Sound(String soundName, float volume) implements ISound {}
}
