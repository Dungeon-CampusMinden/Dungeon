package mushRoom.modules.journal;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import core.Game;
import mushRoom.Sounds;

/**
 * UI component representing a journal book with multiple entries, allowing navigation between
 * pages.
 */
public class JournalUI extends Group {

  private static final float BOOK_WIDTH = 1260f;
  private static final float BOOK_HEIGHT = 900f;

  private static final float PAGE_PAD_TOP = 80f;
  private static final float PAGE_PAD_BOTTOM = 60f;
  private static final float PAGE_PAD_SIDE = 50f;

  // Components
  private final Image bookImage;
  private final Button btnLeft;
  private final Button btnRight;
  private final Table rightPageContent;
  private final Label pageCounterLabel;

  // Data
  private final Array<BookEntry> entries = new Array<>();
  private int currentPageIndex = 0;
  private final Skin skin;
  private long soundHandle = -1;

  /**
   * Constructs a new JournalUI with the specified skin and book background.
   *
   * @param skin The UI skin to use
   * @param bookBackground The drawable for the book background
   */
  public JournalUI(Skin skin, Drawable bookBackground) {
    this.skin = skin;

    bookImage = new Image(bookBackground);
    bookImage.setSize(BOOK_WIDTH, BOOK_HEIGHT);
    addActor(bookImage);

    // Navigation Buttons
    btnLeft = new TextButton("<", skin);
    btnRight = new TextButton(">", skin);

    btnLeft.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            prevPage();
          }
        });

    btnRight.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            nextPage();
          }
        });

    addActor(btnLeft);
    addActor(btnRight);

    // Right Page Container
    rightPageContent = new Table();
    rightPageContent.top();
    addActor(rightPageContent);

    // Initialize Page Counter
    pageCounterLabel = new Label("0 / 0", skin, "blank-black");
    pageCounterLabel.setAlignment(Align.center);

    this.setSize(Game.windowWidth(), Game.windowHeight());
  }

  /**
   * Because Group does not implement Layout, we override setSize. This ensures that whenever you
   * resize this element (e.g. on screen resize), the internal contents re-center themselves.
   */
  @Override
  public void setSize(float width, float height) {
    super.setSize(width, height);
    repositionElements();
  }

  /** Calculates positions based on the current width/height of this Group. */
  private void repositionElements() {
    float screenW = getWidth();
    float screenH = getHeight();

    float bookX = (screenW - BOOK_WIDTH) / 2f;
    float bookY = (screenH - BOOK_HEIGHT) / 2f;
    bookImage.setPosition(bookX, bookY);

    // Position Buttons (Left and Right of the book)
    float btnMargin = 20f;
    if (btnLeft.getWidth() == 0) btnLeft.pack();
    if (btnRight.getWidth() == 0) btnRight.pack();
    btnLeft.setPosition(
        bookX - btnLeft.getWidth() - btnMargin, screenH / 2f - btnLeft.getHeight() / 2f);
    btnRight.setPosition(bookX + BOOK_WIDTH + btnMargin, screenH / 2f - btnRight.getHeight() / 2f);

    // Position the Content Table strictly over the RIGHT page
    float rightPageX = bookX + (BOOK_WIDTH / 2f);
    rightPageContent.setPosition(rightPageX, bookY);
    rightPageContent.setSize(BOOK_WIDTH / 2f, BOOK_HEIGHT);
    rightPageContent.pad(PAGE_PAD_TOP, PAGE_PAD_SIDE, PAGE_PAD_BOTTOM, PAGE_PAD_SIDE);

    rightPageContent.invalidate();
  }

  /**
   * Adds a new entry to the journal book.
   *
   * @param image The texture representing the entry
   * @param explanation The descriptive text for the entry
   */
  public void addEntry(Texture image, String explanation) {
    entries.add(new BookEntry(image, explanation));
    refreshPage();
  }

  private void nextPage() {
    if (currentPageIndex + 2 < entries.size) {
      currentPageIndex += 2;
      refreshPage();
    }
    playPageFlipSound();
  }

  private void prevPage() {
    if (currentPageIndex - 2 >= 0) {
      currentPageIndex -= 2;
      refreshPage();
    }
    playPageFlipSound();
  }

  private void playPageFlipSound() {
    Game.audio().stopInstance(soundHandle);
    soundHandle = Sounds.FLIP_BOOK_PAGE.play();
  }

  private void refreshPage() {
    rightPageContent.clearChildren();

    int currentVisualPage = (currentPageIndex / 2) + 1;
    int totalVisualPages = (int) Math.ceil(entries.size / 2f);
    if (totalVisualPages == 0) totalVisualPages = 1;

    // Element 1
    if (currentPageIndex < entries.size) {
      addEntryToTable(entries.get(currentPageIndex));
    }

    // Spacer
    rightPageContent.add().growY();
    rightPageContent.row();

    // Element 2
    if (currentPageIndex + 1 < entries.size) {
      addEntryToTable(entries.get(currentPageIndex + 1));
    } else {
      rightPageContent.add().expand().fill();
      rightPageContent.row();
    }

    // Spacer to push footer
    rightPageContent.add().growY();
    rightPageContent.row();

    // Footer
    pageCounterLabel.setText(currentVisualPage + " / " + totalVisualPages);
    rightPageContent.add(pageCounterLabel).bottom().expandX();

    // Update Button visibility
    btnLeft.setVisible(currentPageIndex > 0);
    btnRight.setVisible(currentPageIndex + 2 < entries.size);
  }

  private void addEntryToTable(BookEntry entry) {
    Image icon = new Image(entry.texture);
    rightPageContent.add(icon).size(200).maxSize(250).padBottom(10);
    rightPageContent.row();

    Label text = new Label(entry.text, skin, "blank-black");
    text.setFontScale(0.5f);
    text.setWrap(true);
    text.setAlignment(Align.center);
    rightPageContent.add(text).width(450f).padBottom(30);
    rightPageContent.row();
  }

  /**
   * A single entry in the journal book, consisting of a texture and descriptive text.
   *
   * @param texture The texture representing the entry
   * @param text The descriptive text for the entry
   */
  public record BookEntry(Texture texture, String text) {}
}
