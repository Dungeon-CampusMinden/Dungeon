package rooms.systemRecovery.riddles.support;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Success, failure and completion hooks shared by System Recovery riddle controllers.
 *
 * <p>The callback receives the submitted interaction label and the authoritative player ID. A value
 * below zero means that the engine interaction did not expose an actor (for example a lever
 * command). Such callbacks can still record puzzle starts and completions, while per-player answer
 * events are naturally omitted.
 */
public record RiddleCallbacks(
    Consumer<Attempt> onSuccess, Consumer<Attempt> onFailure, Runnable onSolved) {

  public RiddleCallbacks {
    onSuccess = Objects.requireNonNullElse(onSuccess, attempt -> {});
    onFailure = Objects.requireNonNullElse(onFailure, attempt -> {});
    onSolved = Objects.requireNonNullElse(onSolved, () -> {});
  }

  /** Keeps the two-hook constructor convenient for isolated riddle tests. */
  public RiddleCallbacks(Consumer<Attempt> onSuccess, Consumer<Attempt> onFailure) {
    this(onSuccess, onFailure, null);
  }

  /**
   * @return callbacks that deliberately do nothing, useful for isolated unit tests
   */
  public static RiddleCallbacks noop() {
    return new RiddleCallbacks(null, null, null);
  }

  /** Invokes the success callback. */
  public void success(String input, int playerId) {
    onSuccess.accept(new Attempt(input, playerId));
  }

  /** Invokes the failure callback. */
  public void failure(String input, int playerId) {
    onFailure.accept(new Attempt(input, playerId));
  }

  /** Invokes the completion callback after the authoritative riddle state changed. */
  public void solved() {
    onSolved.run();
  }

  /** One concrete interaction attempt. */
  public record Attempt(String input, int playerId) {}
}
