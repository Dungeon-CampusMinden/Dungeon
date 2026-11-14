package feature.interaction.phone;

import engine.Component;
import engine.Entity;
import feature.interaction.keypad.KeypadFactory;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Settings of a phone on the server: how many digits fit, the lines it reaches and what happens
 * when a player calls one of them or a number without a line.
 *
 * <p>Players use the phone through {@link PhoneFactory#use(Entity, Entity)}, which shows the dialog
 * and looks every dialed number up again.
 */
public class PhoneComponent implements Component {

  private final int maxDigits;
  private final Map<String, Line> lines = new LinkedHashMap<>();
  private String noConnection = "";
  private Consumer<Entity> onNoConnection = player -> {};

  /**
   * Creates a phone without lines.
   *
   * @param maxDigits digits a number can have, at most {@link PhoneFactory#MAX_DIGITS}
   */
  public PhoneComponent(int maxDigits) {
    this.maxDigits = maxDigits;
  }

  /**
   * Returns how many digits a number can have.
   *
   * @return the most digits the dialog takes
   */
  public int maxDigits() {
    return maxDigits;
  }

  /**
   * Adds a line. Calling it closes the dialog first, so the line can answer in a dialog of its own.
   *
   * @param line number, name and whether the note lists it
   * @param onCall called with the calling player, e.g. to play an announcement
   * @throws IllegalArgumentException if the number is not 1 to {@link #maxDigits()} digits or
   *     another line has it
   */
  public void addLine(PhoneLine line, Consumer<Entity> onCall) {
    if (!KeypadFactory.isNumericCode(line.number()) || line.number().length() > maxDigits) {
      throw new IllegalArgumentException(
          "Phone number must consist of 1 to " + maxDigits + " digits: " + line.number());
    }
    if (lines.containsKey(line.number())) {
      throw new IllegalArgumentException("Phone number is taken twice: " + line.number());
    }
    lines.put(line.number(), new Line(line, Objects.requireNonNull(onCall, "onCall")));
  }

  /**
   * Returns the lines in the order they were added; the note lists them in this order.
   *
   * @return the lines of the phone
   */
  public List<PhoneLine> lines() {
    return lines.values().stream().map(Line::line).toList();
  }

  /**
   * Returns the text the dialog shows for a number without a line.
   *
   * @return the text, or empty for the engine's default text
   */
  public String noConnection() {
    return noConnection;
  }

  /**
   * Sets the text the dialog shows for a number without a line.
   *
   * @param noConnection the text, or empty for the engine's default text
   */
  public void noConnection(String noConnection) {
    this.noConnection = Objects.requireNonNull(noConnection, "noConnection");
  }

  /**
   * Sets the callback called with a player who dialed a number without a line.
   *
   * @param onNoConnection callback receiving the player
   */
  public void onNoConnection(Consumer<Entity> onNoConnection) {
    this.onNoConnection = Objects.requireNonNull(onNoConnection, "onNoConnection");
  }

  Optional<Consumer<Entity>> call(String number) {
    return Optional.ofNullable(lines.get(number)).map(Line::onCall);
  }

  Consumer<Entity> noConnectionListener() {
    return onNoConnection;
  }

  private record Line(PhoneLine line, Consumer<Entity> onCall) {}
}
