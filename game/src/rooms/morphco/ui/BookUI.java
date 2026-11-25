package rooms.morphco.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import engine.Game;
import engine.input.ContextSpec;
import engine.input.GameInput;
import engine.input.UiActions;
import engine.sound.Sounds;
import engine.utils.Cursors;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.path.SimpleIPath;
import feature.hud.dialogs.DialogCallbackResolver;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import java.util.ArrayList;
import java.util.List;
import rooms.morphco.util.MorphCoSounds;
import rooms.morphco.util.MorphCoText;

/**
 * A book of the factory, lying open over the dimmed game: {@code images/open-book.png} as large as
 * the window allows.
 *
 * <p>Subclasses fill {@link #sections} with blocks in their constructor. Every section starts on a
 * new page, and its blocks follow as long as they fit, measured at the size the book has in the
 * window. The arrows beside the book, the arrow keys and the pad turn two pages at a time; the
 * close button and Escape ask the server to close the dialog.
 */
abstract class BookUI extends Group {

  /** Input of the books: turning pages left and right, the pad cursor clicks the rest. */
  static final ContextSpec INPUT =
      UiActions.modal("morphco_book", UiActions.NAV_LEFT, UiActions.NAV_RIGHT, UiActions.CLICK);

  // open-book.png and the text area on each of its pages, in pixels of the image.
  private static final float BOOK_WIDTH = 1260;
  private static final float BOOK_HEIGHT = 900;
  private static final float LEFT_PAGE = 72;
  private static final float RIGHT_PAGE = 678;
  private static final float PAGE_WIDTH = 510;
  private static final float PAGE_BOTTOM = 88;
  private static final float PAGE_HEIGHT = 762;
  private static final float PAGE_NUMBER = 40;
  // Space between book, buttons and window edge; the arrows need SIDE on both sides of the book.
  private static final float MARGIN = 16;
  private static final float SIDE = 96;
  private static final float ARROW_WIDTH = 56;
  private static final float ARROW_HEIGHT = 88;

  /** Everything printed in the book, section by section. */
  final List<List<Table>> sections = new ArrayList<>();

  private final Image dim = new Image(MorphCoUI.fill(MorphCoUI.BACKDROP));
  private final Image book =
      new Image(TextureMap.instance().textureAt(new SimpleIPath("images/open-book.png")));
  private final Table left = new Table();
  private final Table right = new Table();
  private final Label leftNumber = MorphCoUI.label("", 16, MorphCoUI.INK);
  private final Label rightNumber = MorphCoUI.label("", 16, MorphCoUI.INK);
  private final TextButton previous;
  private final TextButton next;
  private final TextButton close;
  private List<List<Table>> pages = List.of();
  // Size of the book in the window, and the size its pages were filled for.
  private float scale;
  private float pageScale;
  // First block of the left page that is open.
  private Table open;
  private long pageFlip;

  /**
   * Creates the empty book.
   *
   * @param context dialog context of the server dialog
   */
  BookUI(DialogContext context) {
    setUserObject(Cursors.DEFAULT);
    // Turning a page plays the page flip instead of a click.
    previous = Sounds.ownSound(MorphCoUI.button("<", false, () -> turnBy(-2)));
    next = Sounds.ownSound(MorphCoUI.button(">", true, () -> turnBy(2)));
    close =
        MorphCoUI.button(
            MorphCoText.text("ui.close"),
            false,
            () ->
                DialogCallbackResolver.createButtonCallback(
                        context.dialogId(), DialogContextKeys.ON_CLOSE)
                    .accept(null));
    left.top();
    right.top();
    leftNumber.setAlignment(Align.center);
    rightNumber.setAlignment(Align.center);
    addActor(dim);
    addActor(book);
    addActor(left);
    addActor(right);
    addActor(leftNumber);
    addActor(rightNumber);
    addActor(previous);
    addActor(next);
    addActor(close);
    // The dialog factory centers the dialog by its size.
    setSize(Game.windowWidth(), Game.windowHeight());
  }

  /**
   * Distributes the blocks over pages for the current scale: every section starts on a new page,
   * and a page takes the next block of its section as long as it fits. A block higher than a page
   * gets a page of its own.
   *
   * @return the blocks of each page in reading order
   */
  private List<List<Table>> pages() {
    float width = PAGE_WIDTH * pageScale;
    float pageHeight = PAGE_HEIGHT * pageScale;
    List<List<Table>> pages = new ArrayList<>();
    for (List<Table> section : sections) {
      List<Table> page = new ArrayList<>();
      float used = 0;
      for (Table block : section) {
        float needed = MorphCoUI.prefHeight(block, width);
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

  /**
   * A part of a page that never breaks across pages.
   *
   * @param content content
   * @param space space below the block
   * @return the block
   */
  static Table block(Actor content, float space) {
    Table block = new Table();
    block.add(content).growX();
    block.padBottom(space);
    return block;
  }

  /**
   * Turns to the two pages with the given block, e.g. for a link in a table of contents.
   *
   * @param block a block of {@link #sections}
   */
  void turn(Table block) {
    show(block);
    Game.soundPlayer().stopByInstance(pageFlip);
    pageFlip = Sounds.playUi(MorphCoSounds.PAGE_FLIP);
  }

  // Nothing happens beyond the covers.
  private void turnBy(int count) {
    int page = pageOf(open) + count;
    if (page >= 0 && page < pages.size()) turn(pages.get(page).getFirst());
  }

  private int pageOf(Table block) {
    int page = 0;
    while (!pages.get(page).contains(block)) page++;
    return page;
  }

  private void show(Table block) {
    int page = pageOf(block);
    page -= page % 2;
    open = pages.get(page).getFirst();
    List<Table> facing = page + 1 < pages.size() ? pages.get(page + 1) : List.of();
    fill(left, pages.get(page));
    fill(right, facing);
    leftNumber.setText(String.valueOf(page + 1));
    rightNumber.setText(facing.isEmpty() ? "" : String.valueOf(page + 2));
    previous.setVisible(page > 0);
    next.setVisible(page + 2 < pages.size());
  }

  private static void fill(Table page, List<Table> blocks) {
    page.clearChildren();
    for (Table block : blocks) page.add(block).growX().row();
  }

  @Override
  public void act(float delta) {
    setSize(Game.windowWidth(), Game.windowHeight());
    if (scale != pageScale) {
      pageScale = scale;
      pages = pages();
      show(open == null ? sections.getFirst().getFirst() : open);
    }
    super.act(delta);
    if (GameInput.justPressed(UiActions.NAV_LEFT)) turnBy(-2);
    if (GameInput.justPressed(UiActions.NAV_RIGHT)) turnBy(2);
  }

  // Lays the book out for the window; act() fills its pages anew when the book changed its size.
  @Override
  protected void sizeChanged() {
    float width = getWidth();
    float height = getHeight();
    scale = Math.min((width - 2 * SIDE) / BOOK_WIDTH, (height - 2 * MARGIN) / BOOK_HEIGHT);
    float x = (width - BOOK_WIDTH * scale) / 2;
    float y = (height - BOOK_HEIGHT * scale) / 2;
    dim.setBounds(0, 0, width, height);
    book.setBounds(x, y, BOOK_WIDTH * scale, BOOK_HEIGHT * scale);
    left.setBounds(
        x + LEFT_PAGE * scale, y + PAGE_BOTTOM * scale, PAGE_WIDTH * scale, PAGE_HEIGHT * scale);
    right.setBounds(
        x + RIGHT_PAGE * scale, y + PAGE_BOTTOM * scale, PAGE_WIDTH * scale, PAGE_HEIGHT * scale);
    leftNumber.setBounds(x + LEFT_PAGE * scale, y + PAGE_NUMBER * scale, PAGE_WIDTH * scale, 20);
    rightNumber.setBounds(x + RIGHT_PAGE * scale, y + PAGE_NUMBER * scale, PAGE_WIDTH * scale, 20);
    previous.setBounds(
        x - MARGIN - ARROW_WIDTH, (height - ARROW_HEIGHT) / 2, ARROW_WIDTH, ARROW_HEIGHT);
    next.setBounds(
        x + BOOK_WIDTH * scale + MARGIN, (height - ARROW_HEIGHT) / 2, ARROW_WIDTH, ARROW_HEIGHT);
    close.pack();
    close.setPosition(width - close.getWidth() - MARGIN, height - close.getHeight() - MARGIN);
  }
}
