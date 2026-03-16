package blockly.dgir.vm.dialect.dg;

import blockly.dgir.dialect.dg.DgAttrs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Bridge between the VM-side {@link DgRunners} and the game-side action system.
 *
 * <p>Each method schedules the corresponding hero action on the game (render) thread and calls
 * {@code onComplete} once the action has fully finished. The callback is thread-safe and is
 * intended to be wired to a {@link java.util.concurrent.CountDownLatch} that blocks the VM thread
 * until the game-side action completes.
 *
 * <p>Register a concrete implementation before starting the VM via {@link
 * #register(DgActionGateway)}.
 */
public interface DgActionGateway {

  /** Shared singleton holder. Set once at application startup via {@link #register}. */
  AtomicReference<@Nullable DgActionGateway> INSTANCE = new AtomicReference<>(null);

  /**
   * Returns the registered gateway.
   *
   * @throws IllegalStateException if no gateway has been registered yet.
   */
  static @NotNull DgActionGateway get() {
    DgActionGateway gw = INSTANCE.get();
    if (gw == null) {
      throw new IllegalStateException(
          "No DgActionGateway registered. Call DgActionGateway.register() before running the VM.");
    }
    return gw;
  }

  /**
   * Registers the game-side implementation.
   *
   * @param gateway the implementation to use; must not be {@code null}.
   */
  static void register(@NotNull DgActionGateway gateway) {
    INSTANCE.set(gateway);
  }

  // =========================================================================
  // Action methods — each schedules an action on the game thread and calls
  // onComplete when the action is fully done.
  // =========================================================================

  /** Move the hero one tile forward. */
  void move(@NotNull Runnable onComplete);

  /**
   * Turn the hero left or right.
   *
   * @param dir the turn direction (LEFT or RIGHT).
   */
  void turn(@NotNull DgAttrs.TurnDirectionAttr.TurnDir dir, @NotNull Runnable onComplete);

  /**
   * Interact with entities in a given direction relative to the hero.
   *
   * @param dir direction relative to the hero's facing direction (or HERE for current tile).
   */
  void use(@NotNull DgAttrs.UseDirectionAttr.UseDir dir, @NotNull Runnable onComplete);

  /** Push the entity in front of the hero. */
  void push(@NotNull Runnable onComplete);

  /** Pull the entity in front of the hero. */
  void pull(@NotNull Runnable onComplete);

  /**
   * Drop an item on the hero's current tile.
   *
   * @param dropType the type of item to drop.
   */
  void drop(@NotNull DgAttrs.DropTypeAttr.DropType dropType, @NotNull Runnable onComplete);

  /** Pick up an item on the hero's current tile. */
  void pickup(@NotNull Runnable onComplete);

  /** Shoot a fireball in the hero's facing direction. */
  void fireball(@NotNull Runnable onComplete);

  /** Make the hero rest (do nothing) for a brief period. */
  void rest(@NotNull Runnable onComplete);
}
