package rooms.morphco.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import engine.Game;
import engine.sound.Sounds;
import engine.utils.Cursors;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.path.SimpleIPath;
import feature.canvas.CanvasNode;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.HeadlessDialogGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleFunction;
import rooms.morphco.util.MorphCoSounds;
import rooms.morphco.util.MorphCoText;

/**
 * "100 anspruchsvolle Hamsterwitze für den täglichen Gebrauch" from the shelf of A-12, shown as an
 * open book.
 *
 * <p>The book falls open at the joke the server picked, see {@link MorphCoDialogs#JOKE}, next to
 * the title page. Then come the contents, the six chapters and the hamster wisdom of the day. Each
 * of them starts on a new page; the jokes are spread over as many pages as their measured height
 * needs at the current size of the book, and players turn two pages at a time.
 */
final class HamsterBookUI extends MorphCoConsoleUI {

  private static final Color LINK = Color.valueOf("1f5fa8");
  // First joke of each chapter.
  private static final int[] CHAPTERS = {1, 21, 41, 61, 76, 91};
  // open-book.png and the text area on each of its pages, in pixels of the image.
  private static final float BOOK_WIDTH = 1260;
  private static final float BOOK_HEIGHT = 900;
  private static final float LEFT_PAGE = 72;
  private static final float RIGHT_PAGE = 678;
  private static final float PAGE_WIDTH = 510;
  private static final float PAGE_BOTTOM = 88;
  private static final float PAGE_HEIGHT = 762;
  private static final float PAGE_NUMBER = 40;

  private final Book book = area().addNode(new Book());
  // Everything printed in the book, section by section.
  private final List<List<Table>> sections = new ArrayList<>();
  private final float numberWidth;
  private List<List<Table>> pages = List.of();
  private float pageScale;
  // First block of the left page that is open.
  private Table open;
  private long pageFlip;

  private HamsterBookUI(DialogContext context) {
    super(context, MorphCoText.text("world.a12.book-hamster"), "", "");
    Label widest = MorphCoUI.bold(MorphCoDialogs.HAMSTER_JOKES + ".", 19, MorphCoUI.INK);
    widest.setWrap(false);
    numberWidth = widest.getPrefWidth();
    int joke = context.require(MorphCoDialogs.JOKE, Integer.class);
    // Number and joke stay together on the first page, even joke 100 in English.
    Table opening = block(MorphCoUI.bold(t("number", joke), 24, MorphCoUI.INK), 0);
    opening.row().padTop(12);
    opening.add(MorphCoUI.rich(t("joke-" + joke), 19, MorphCoUI.INK)).growX();
    sections.add(List.of(opening));
    sections.add(
        List.of(
            block(
                MorphCoUI.bold(MorphCoText.text("world.a12.book-hamster"), 28, MorphCoUI.INK), 16),
            block(MorphCoUI.rich(t("subtitle"), 19, MorphCoUI.INK), 0)));
    List<List<Table>> chapters = new ArrayList<>();
    for (int chapter = 1; chapter <= CHAPTERS.length; chapter++) chapters.add(chapter(chapter));
    List<Table> contents = new ArrayList<>();
    contents.add(block(MorphCoUI.bold(t("contents"), 22, MorphCoUI.INK), 14));
    for (int chapter = 1; chapter <= CHAPTERS.length; chapter++)
      contents.add(block(link(t("chapter-" + chapter), chapters.get(chapter - 1).getFirst()), 10));
    sections.add(contents);
    sections.addAll(chapters);
    sections.add(
        List.of(
            block(MorphCoUI.bold(t("wisdom-title"), 24, MorphCoUI.INK), 14),
            block(MorphCoUI.rich(t("wisdom"), 22, MorphCoUI.INK), 0)));
    open = sections.getFirst().getFirst();
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

  /**
   * Distributes blocks over pages: every section starts on a new page, and a page takes the next
   * block of its section as long as it fits. A block higher than a page gets a page of its own.
   *
   * @param sections blocks, section by section
   * @param height height of a block including the space below it
   * @param pageHeight height of a page
   * @param <T> type of the blocks
   * @return the blocks of each page in reading order
   */
  static <T> List<List<T>> pages(
      List<List<T>> sections, ToDoubleFunction<T> height, double pageHeight) {
    List<List<T>> pages = new ArrayList<>();
    for (List<T> section : sections) {
      List<T> page = new ArrayList<>();
      double used = 0;
      for (T block : section) {
        double needed = height.applyAsDouble(block);
        if (!page.isEmpty() && used + needed > pageHeight) {
          pages.add(page);
          page = new ArrayList<>();
          used = 0;
        }
        page.add(block);
        used += needed;
      }
      pages.add(page);
    }
    return pages;
  }

  private static String t(String key, Object... values) {
    return MorphCoText.text("hamster-jokes." + key, values);
  }

  // A part of a page that never breaks across pages, with the space below it.
  private static Table block(Label label, float space) {
    Table block = new Table();
    block.add(label).growX();
    block.padBottom(space);
    return block;
  }

  // Wrapped labels learn their width in a layout pass; the second pass measures their height.
  private static float height(Table block, float width) {
    float height = block.getPrefHeight();
    for (int pass = 0; pass < 2; pass++) {
      block.setSize(width, height);
      block.validate();
      block.invalidate();
      height = block.getPrefHeight();
    }
    return height;
  }

  @Override
  protected float arrange(float width, float height) {
    // The work area starts to scroll 4 pixels before the book fills its height.
    float scale = Math.min(width / BOOK_WIDTH, (height - 4) / BOOK_HEIGHT);
    float bookWidth = BOOK_WIDTH * scale;
    float bookHeight = BOOK_HEIGHT * scale;
    book.size(bookWidth, bookHeight);
    book.position(Math.round((width - bookWidth) / 2), -Math.round((height + bookHeight) / 2));
    if (scale != pageScale) {
      pageScale = scale;
      pages = pages(sections, block -> height(block, PAGE_WIDTH * scale), PAGE_HEIGHT * scale);
      show(open);
    }
    return bookHeight;
  }

  // Opens the book at the two pages with the given block.
  private void show(Table block) {
    int left = 0;
    while (!pages.get(left).contains(block)) left++;
    left -= left % 2;
    open = pages.get(left).getFirst();
    book.show(left, pages.get(left), left + 1 < pages.size() ? pages.get(left + 1) : List.of());
    clearButtons();
    if (left > 0) pageButton("hamster-jokes.back", false, pages.get(left - 1).getFirst());
    if (left + 2 < pages.size())
      pageButton("hamster-jokes.next", true, pages.get(left + 2).getFirst());
  }

  private void turn(Table block) {
    show(block);
    Game.soundPlayer().stopByInstance(pageFlip);
    pageFlip = Sounds.playUi(MorphCoSounds.PAGE_FLIP);
  }

  // Turning a page plays the page flip instead of a click.
  private void pageButton(String key, boolean primary, Table target) {
    Sounds.ownSound(button(key, primary, () -> turn(target)));
  }

  private Label link(String text, Table target) {
    Label link = MorphCoUI.label(text, 19, LINK);
    link.setTouchable(Touchable.enabled);
    link.setUserObject(Cursors.INTERACT);
    link.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            turn(target);
          }
        });
    return link;
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

  /** The open book on the work area: a column of blocks and the page number on each page. */
  private static final class Book extends CanvasNode {
    private final Texture image =
        TextureMap.instance().textureAt(new SimpleIPath("images/open-book.png"));
    private final Table left = new Table();
    private final Table right = new Table();
    private final Label leftNumber = MorphCoUI.label("", 16, MorphCoUI.INK);
    private final Label rightNumber = MorphCoUI.label("", 16, MorphCoUI.INK);

    private Book() {
      super("book", BOOK_WIDTH, BOOK_HEIGHT);
      movable(false);
      selectable(false);
      deletable(false);
    }

    // Shows the left page with the given index and the page after it.
    private void show(int index, List<Table> leftBlocks, List<Table> rightBlocks) {
      fill(left, leftBlocks);
      fill(right, rightBlocks);
      leftNumber.setText(String.valueOf(index + 1));
      rightNumber.setText(rightBlocks.isEmpty() ? "" : String.valueOf(index + 2));
    }

    private static void fill(Table page, List<Table> blocks) {
      page.clearChildren();
      for (Table block : blocks) page.add(block).growX().row();
    }

    @Override
    protected void buildContent() {
      left.top();
      right.top();
      leftNumber.setAlignment(Align.center);
      rightNumber.setAlignment(Align.center);
      addActor(left);
      addActor(right);
      addActor(leftNumber);
      addActor(rightNumber);
    }

    @Override
    protected void layoutContent() {
      float scale = width() / BOOK_WIDTH;
      left.setBounds(
          LEFT_PAGE * scale, PAGE_BOTTOM * scale, PAGE_WIDTH * scale, PAGE_HEIGHT * scale);
      right.setBounds(
          RIGHT_PAGE * scale, PAGE_BOTTOM * scale, PAGE_WIDTH * scale, PAGE_HEIGHT * scale);
      leftNumber.setBounds(LEFT_PAGE * scale, PAGE_NUMBER * scale, PAGE_WIDTH * scale, 20);
      rightNumber.setBounds(RIGHT_PAGE * scale, PAGE_NUMBER * scale, PAGE_WIDTH * scale, 20);
    }

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      Color previous = batch.getColor().cpy();
      batch.setColor(1, 1, 1, alpha);
      batch.draw(image, x(), y(), width(), height());
      batch.setColor(previous);
    }
  }
}
