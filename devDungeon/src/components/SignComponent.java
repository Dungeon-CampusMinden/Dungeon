package components;

import com.badlogic.gdx.utils.Align;
import contrib.hud.dialogs.TextDialog;
import core.Component;
import core.Entity;
import core.utils.Point;

/**
 * The SignComponent class implements the Component interface. It represents a sign in the game with
 * a title and text.
 *
 * @see entities.SignFactory#createSign(String, String, Point) SignFactory#createSign
 */
public class SignComponent implements Component {

  public static final int DEFAULT_WIDTH = 600;
  public static final int DEFAULT_HEIGHT = 300;
  public static final String DEFAULT_TITLE = "Schild";
  public static final int DEFAULT_ALIGNMENT = Align.top;
  public static final String DEFAULT_BUTTON_TEXT = "OK";

  private String text;
  private String title;

  /**
   * Constructs a new SignComponent with the specified text and title.
   *
   * @param text The text of the sign.
   * @param title The title of the sign.
   */
  public SignComponent(String text, String title) {
    this.text = text;
    this.title = title;
  }

  /**
   * Constructs a new SignComponent with the specified text and a default title ("Schild").
   *
   * @param text The text of the sign.
   */
  public SignComponent(String text) {
    this(text, DEFAULT_TITLE);
  }

  /**
   * Returns the text of the sign.
   *
   * @return The text of the sign.
   */
  public String text() {
    return this.text;
  }

  /**
   * Sets the text of the sign.
   *
   * @param text The text to set.
   */
  public void text(String text) {
    this.text = text;
  }

  /**
   * Returns the title of the sign.
   *
   * @return The title of the sign.
   */
  public String title() {
    return this.title;
  }

  /**
   * Sets the title of the sign.
   *
   * @param title The title to set.
   */
  public void title(String title) {
    this.title = title;
  }

  /**
   * Displays a dialog with the sign's text and title. The dialog has a default button text, width,
   * height, and alignment. (see {@link SignComponent})
   *
   * @return The dialog entity.
   */
  public Entity showDialog() {
    return TextDialog.textDialog(
        this.text,
        DEFAULT_BUTTON_TEXT,
        this.title,
        DEFAULT_WIDTH,
        DEFAULT_HEIGHT,
        DEFAULT_ALIGNMENT);
  }

  @Override
  public String toString() {
    return "SignComponent{title='" + this.title + "', text='" + this.text + "'}";
  }
}
