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
import core.sound.player.IPlayHandle;
import java.util.Optional;
import mushRoom.Sounds;

public class JournalUI extends Group {

  // Constants
  private static final float BOOK_WIDTH = 1260f;
  private static final float BOOK_HEIGHT = 900f;

  // Internal padding
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
  private IPlayHandle soundHandle;

  public JournalUI(Skin skin, Drawable bookBackground) {
    this.skin = skin;

    // 2. The Book Background
    bookImage = new Image(bookBackground);
    bookImage.setSize(BOOK_WIDTH, BOOK_HEIGHT);
    addActor(bookImage);

    // 3. Navigation Buttons
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

    // 4. Right Page Container
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

    // Center the book
    float bookX = (screenW - BOOK_WIDTH) / 2f;
    float bookY = (screenH - BOOK_HEIGHT) / 2f;
    bookImage.setPosition(bookX, bookY);

    // Position Buttons (Left and Right of the book)
    float btnMargin = 20f;

    // We need to validate buttons have a size. If using TextButton, they usually auto-size.
    // If they are 0, pack() ensures they have dimensions.
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

    // Force the table to re-calculate its internal layout
    rightPageContent.invalidate();
  }

  public void addEntry(Texture image, String explanation) {
    entries.add(new BookEntry(image, explanation));
    refreshPage();
  }

  public void setEntries(Array<BookEntry> newEntries) {
    entries.clear();
    entries.addAll(newEntries);
    currentPageIndex = 0;
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
    if (soundHandle != null && soundHandle.isPlaying()) {
      soundHandle.stop();
    }
    Optional<IPlayHandle> handle = Sounds.FLIP_BOOK_PAGE.play();
    handle.ifPresent(h -> soundHandle = h);
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

  public static class BookEntry {
    Texture texture;
    String text;

    public BookEntry(Texture texture, String text) {
      this.texture = texture;
      this.text = text;
    }
  }
}
