package dgir.vm.api;

import core.ir.Block;
import core.ir.Operation;
import core.ir.Region;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

public sealed interface Action
    permits Action.Next, Action.Jump, Action.Call, Action.StepIntoRegion, Action.Terminate, Action.Abort {
  /** Executes the next operation in the current block. */
  static @NotNull Action Next() {
    return new Next();
  }

  /**
   * Jumps to the given block and executes it. The block must be in the same region as the current
   * block.
   *
   * @param target The target block.
   * @return An action that represents the jump.
   */
  static @NotNull Action Jump(@NotNull Block target) {
    return new Jump(target);
  }

  /**
   * Calls the given function operation.
   *
   * @param funcOp The function operation to call.
   * @return An action that represents the call.
   */
  static @NotNull Action Call(@NotNull Operation funcOp, @NotNull Object... args) {
    return new Call(funcOp, List.of(args));
  }

  /**
   * Steps into the given region.
   *
   * @param region The region to step into. Must be a region that is a child of the current
   *     operation.
   * @param isolatedFromAbove Whether the new stack frame created for the region should be isolated
   *     from the above stack frame.
   * @return An action that represents the step into.
   */
  static @NotNull Action StepIntoRegion(
      @NotNull Region region,
      boolean isolatedFromAbove,
      @NotNull Object... args) {
    return new StepIntoRegion(region, isolatedFromAbove, List.of(args));
  }

  /**
   * Terminates the current block and returns the given value. The value can be null if the block
   * does not return anything.
   *
   * @param value The value to return. Can be null if the block does not return anything.
   * @return An action that represents the return.
   */
  static @NotNull Action Terminate(@Nullable Object value) {
    return new Terminate(value);
  }

  /**
   * Aborts the execution of the program with the given message. The message should be a
   * human-readable description of the error that occurred.
   *
   * @param message The error message.
   * @return An action that represents the abort.
   */
  static @NotNull Action Abort(@NotNull Optional<Exception> exception, @NotNull String message, @Nullable Object... args) {
    return new Abort(MessageFormat.format(message, args), exception);
  }

  /** Executes the next operation in the current block. */
  record Next() implements Action {}

  /**
   * Jumps to the given block and executes it. The block must be in the same region as the current
   * block.
   *
   * @param target The target block.
   */
  record Jump(@NotNull Block target) implements Action {}

  /**
   * Calls the given function operation.
   *
   * @param funcOp The function operation to call.
   */
  record Call(@NotNull Operation funcOp, @NotNull List<@NotNull Object> args) implements Action {
    public Call {
      args = List.copyOf(args);
    }
  }

  /**
   * Steps into the given region.
   *
   * @param region The region to step into. Must be a region that is a child of the current
   *     operation.
   * @param isolatedFromAbove Whether the new stack frame created for the region should be isolated
   *     from the above stack frame. If true, values defined in the above stack frame will not be
   *     accessible in the new stack frame.
   */
  record StepIntoRegion(
      @NotNull Region region,
      boolean isolatedFromAbove,
      List<Object> args)
      implements Action {
    public StepIntoRegion {
      args = List.copyOf(args);
    }
  }

  /**
   * Terminates the current block and returns the given value. The value can be null if the block
   * does not return anything.
   *
   * @param value The value to return. Can be null if the block does not return anything.
   */
  record Terminate(@Nullable Object value) implements Action {}

  /**
   * Aborts the execution of the program with the given message.
   *
   * @param message The error message.
   */
  record Abort(@NotNull String message, @NotNull Optional<Exception> e) implements Action {}
}
