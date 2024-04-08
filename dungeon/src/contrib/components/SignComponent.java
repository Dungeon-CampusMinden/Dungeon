package contrib.components;

import contrib.entities.DialogFactory;
import contrib.hud.dialogs.OkDialog;
import core.Component;
import core.Entity;
import core.utils.Point;
import java.util.function.BiConsumer;

/**
 * The SignComponent class implements the Component interface. It represents a sign in the game with
 * a title and text.
 *
 * @see DialogFactory#createSign(String, String, Point, BiConsumer) createSign
 */
public class SignComponent implements Component {
  public static final String DEFAULT_TITLE = "Schild";
  private String text;
  private String title;

  /**
   * Constructs a new SignComponent with the specified text and title.
   *
   * @param text The text of the sign.
   * @param title The title of the sign.
   */
  public SignComponent(String text, String title) {
    // removes newlines and empty spaces and multiple spaces from the title and text
    title = title.replaceAll("\\s+", " ").trim();
    text = text.replaceAll("\\s+", " ").trim();
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
   * Displays a dialog with the sign's text and title.
   *
   * @return The dialog entity.
   */
  public Entity showDialog() {
    return OkDialog.showOkDialog(this.text, this.title, () -> {});
  }

  @Override
  public String toString() {
    return "SignComponent{title='" + this.title + "', text='" + this.text + "'}";
  }
}
