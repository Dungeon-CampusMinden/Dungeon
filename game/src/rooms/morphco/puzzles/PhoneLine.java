package rooms.morphco.puzzles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The numbers of the phone in the control room.
 *
 * <p>The note on the phone lists the mailbox, the hotline and the security service; Rosi's
 * extension is on the shift plan on the table. The phone answers every other number with "no
 * connection" right away; the server looks the number up again and lets the line answer.
 */
public enum PhoneLine {
  /** The mailbox with the message of the morning. */
  MAILBOX("99"),
  /** The maintenance hotline with staged hints. */
  HOTLINE("42"),
  /** The security service at the entrance; nobody answers. */
  SECURITY("11"),
  /** Rosi from the shift plan; she does not answer either. */
  ROSI("17"),
  /** The error codes of F-04 and A-12 from the production control system: an announcement. */
  ERROR_CODE("133", "204");

  private final List<String> numbers;

  PhoneLine(String... numbers) {
    this.numbers = List.of(numbers);
  }

  /**
   * Returns the number that reaches the line.
   *
   * @return number as it is dialed
   */
  public String number() {
    return numbers.getFirst();
  }

  /**
   * Looks up a dialed number.
   *
   * @param number dialed digits
   * @return the line, or empty if there is no connection under this number
   */
  public static Optional<PhoneLine> dial(String number) {
    return Arrays.stream(values()).filter(line -> line.numbers.contains(number)).findFirst();
  }
}
