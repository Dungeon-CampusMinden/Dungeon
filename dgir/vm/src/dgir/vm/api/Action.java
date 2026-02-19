package dgir.vm.api;


import core.ir.Block;
import core.ir.Operation;
import core.ir.Region;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Optional;

public sealed interface Action permits Action.Next, Action.Jump, Action.Call, Action.StepInto, Action.Terminate, Action.Abort {
  /**
   * Executes the next operation in the current block.
   */
  public static @NotNull Action Next() {
    return new Next();
  }

  /**
   * Jumps to the given block and executes it. The block must be in the same region as the current block.
   *
   * @param target The target block.
   * @return An action that represents the jump.
   */
  public static @NotNull Action Jump(@NotNull Block target) {
    return new Jump(target);
  }

  /**
   * Calls the given function operation.
   *
   * @param funcOp The function operation to call.
   * @return An action that represents the call.
   */
  public static @NotNull Action Call(@NotNull Operation funcOp, @NotNull Object... args) {
    return new Call(funcOp, args);
  }

  /**
   * Steps into the given region.
   *
   * @param region            The region to step into. Must be a region that is a child of the current operation.
   * @param isolatedFromAbove Whether the new stack frame created for the region should be isolated from the above stack frame.
   * @param nextOperation     The operation to execute after returning from the region, or null if nothing should be executed after returning from the region.
   * @return An action that represents the step into.
   */
  public static @NotNull Action StepInto(@NotNull Region region, boolean isolatedFromAbove, @NotNull Optional<Operation> nextOperation, @NotNull Object... args) {
    return new StepInto(region, isolatedFromAbove, nextOperation, args);
  }

  /**
   * Terminates the current block and returns the given value. The value can be null if the block does not return anything.
   *
   * @param value The value to return. Can be null if the block does not return anything.
   * @return An action that represents the return.
   */
  public static @NotNull Action Terminate(@NotNull Optional<Object> value) {
    return new Terminate(value);
  }

  /**
   * Aborts the execution of the program with the given message.
   * The message should be a human-readable description of the error that occurred.
   *
   * @param message The error message.
   * @return An action that represents the abort.
   */
  public static @NotNull Action Abort(@NotNull String message, @Nullable Object... args) {
    return new Abort(MessageFormat.format(message, args));
  }

  /**
   * Executes the next operation in the current block.
   */
  public record Next() implements Action {
  }

  /**
   * Jumps to the given block and executes it. The block must be in the same region as the current block.
   *
   * @param target The target block.
   */
  public record Jump(@NotNull Block target) implements Action {
  }

  /**
   * Calls the given function operation.
   *
   * @param funcOp The function operation to call.
   */
  public record Call(@NotNull Operation funcOp, Object... args) implements Action {
  }

  /**
   * Steps into the given region.
   *
   * @param region            The region to step into. Must be a region that is a child of the current operation.
   * @param isolatedFromAbove Whether the new stack frame created for the region should be isolated from the above stack frame.
   *                          If true, values defined in the above stack frame will not be accessible in the new stack frame.
   * @param nextOperation     The operation to execute after returning from the region, or null if nothing should be executed
   *                          after returning from the region.
   */
  public record StepInto(@NotNull Region region,
                         boolean isolatedFromAbove,
                         @NotNull Optional<Operation> nextOperation,
                         Object... args) implements Action {
  }

  /**
   * Terminates the current block and returns the given value. The value can be null if the block does not return anything.
   *
   * @param value The value to return. Can be null if the block does not return anything.
   */
  public record Terminate(@NotNull Optional<Object> value) implements Action {
  }

  /**
   * Aborts the execution of the program with the given message.
   *
   * @param message The error message.
   */
  public record Abort(@NotNull String message) implements Action {
  }
}
