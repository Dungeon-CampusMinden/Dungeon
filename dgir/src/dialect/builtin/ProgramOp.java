package dialect.builtin;

import core.ir.Block;
import core.ir.Operation;
import core.ir.Location;
import core.traits.*;
import dialect.func.FuncOp;
import java.util.function.Function;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Top-level container operation for a DGIR program.
 *
 * <p>{@code ProgramOp} holds a single region with a single block. That block must contain exactly
 * one {@link FuncOp} whose symbol name is {@code "main"}; this function serves as the program
 * entry point.
 *
 * <p>The op acts as a {@link ISymbolTable} anchor: all symbol look-ups issued by nested operations
 * resolve against this table.
 *
 * <p>Ident: {@code program}
 *
 * <pre>{@code
 * program {
 *   func.func @main() { ... }
 * }
 * }</pre>
 */
public final class ProgramOp extends BuiltinOp
    implements Builtin, ISymbolTable, INoTerminator, IGlobalContainer, ISingleRegion, ISingleBlock {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Contract(pure = true)
  @Override
  public @NotNull String getIdent() {
    return "program";
  }

  @Override
  public Function<Operation, Boolean> getVerifier() {
    return operation -> {
      // Make sure that there is a toplevel func op with symbol_name "main"
      boolean hasMainFunc = false;
      Block block = operation.getRegions().getFirst().getBlocks().getFirst();
      for (Operation op : block.getOperations()) {
        var funcOp = op.as(FuncOp.class);
        if (funcOp.isPresent()) {
          if (funcOp.get().getFuncName().equals("main")) {
            if (hasMainFunc) {
              operation.emitError("There must be exactly one function with name main");
              return false;
            }
            hasMainFunc = true;
          }
        }
      }
      if (!hasMainFunc) {
        operation.emitError("There must be exactly one function with name main");
        return false;
      }
      return true;
    };
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  /** Default constructor used during dialect registration. */
  private ProgramOp() {
    executeIfRegistered(
        ProgramOp.class,
        () -> setOperation(true, Operation.Create(Location.UNKNOWN, this, null, null, null, 1)));
  }

  /**
   * Create a new program op with the given source location.
   *
   * @param location the source location of this operation.
   */
  public ProgramOp(@NotNull Location location) {
    setOperation(true, Operation.Create(location, this, null, null, null, 1));
  }

  /**
   * Wrapping constructor that binds this op to an existing backing {@link Operation}.
   *
   * @param operation the backing operation state.
   */
  public ProgramOp(@NotNull Operation operation) {
    super(operation);
  }

  // =========================================================================
  // Functions
  // =========================================================================

  /**
   * Finds and returns the {@code main} function declared inside this program.
   *
   * @return the {@code main} {@link FuncOp}.
   * @throws IllegalStateException if no {@code main} function exists (should have been caught by
   *     verification).
   */
  @Contract(pure = true)
  public @NotNull FuncOp getMainFunc() {
    Block block = getBlock();
    for (Operation op : block.getOperations()) {
      var funcOp = op.as(FuncOp.class);
      if (funcOp.isPresent() && funcOp.get().getFuncName().equals("main")) {
        return funcOp.get();
      }
    }
    throw new IllegalStateException(
        "Could not find main function. This should have been caught by verification.");
  }
}
