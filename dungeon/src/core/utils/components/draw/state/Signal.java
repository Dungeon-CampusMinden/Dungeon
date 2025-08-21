package core.utils.components.draw.state;

/**
 * Represents a simple signal that can be used to communicate events or messages within the
 * drawing/state system.
 *
 * <p>A signal consists of a name and optional associated data. This class is typically used to
 * trigger or notify about changes in state or animation.
 */
public class Signal {

  /** The name of the signal. */
  public final String signal;

  /** Optional data associated with the signal. Can be {@code null}. */
  public final Object data;

  /**
   * Constructs a signal with a name and associated data.
   *
   * @param signal the name of the signal
   * @param data optional data associated with the signal
   */
  public Signal(String signal, Object data) {
    this.signal = signal;
    this.data = data;
  }

  /**
   * Constructs a signal with only a name and no associated data.
   *
   * @param signal the name of the signal
   */
  public Signal(String signal) {
    this(signal, null);
  }
}
