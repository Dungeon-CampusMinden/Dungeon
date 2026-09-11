package rooms.systemRecovery.modules.display;

import engine.Component;

/** Stores the text currently shown by a synchronized System Recovery display. */
public final class DisplayTextComponent implements Component {
  private String text;

  /**
   * Creates a display text component.
   *
   * @param text initial display text
   */
  public DisplayTextComponent(String text) {
    this.text = text;
  }

  /**
   * @return current display text
   */
  public String text() {
    return text;
  }

  /**
   * @param text new display text
   */
  public void text(String text) {
    this.text = text;
  }
}
