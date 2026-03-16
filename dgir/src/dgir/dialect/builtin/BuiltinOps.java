package dgir.dialect.builtin;

import dgir.core.Dialect;
import dgir.core.DgirCoreUtils;
import dgir.core.debug.Location;
import dgir.core.ir.Block;
import dgir.core.ir.Op;
import dgir.core.ir.Operation;
import dgir.core.ir.Value;
import dgir.core.traits.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

import static dgir.dialect.func.FuncOps.FuncOp;

/**
 * Sealed marker interface for all operations in the {@link BuiltinDialect}.
 *
 * <p>Every concrete op must both extend {@link BuiltinOp} and implement this interface so that
 * {@link DgirCoreUtils.Dialect#allOps} can discover it automatically via reflection.
 */
public sealed interface BuiltinOps {
  /**
   * Abstract base class for all operations in the {@code builtin} dialect (namespace {@code ""}).
   *
   * <p>Concrete subclasses must implement {@link #getIdent()} and {@link #getVerifier()}, and must
   * implement {@link BuiltinOps} to be enumerated by {@link BuiltinDialect}.
   */
  abstract class BuiltinOp extends Op {

    // =========================================================================
    // Constructors
    // =========================================================================

    /** Default constructor used during dialect registration. */
    BuiltinOp() {
      super();
    }

    // =========================================================================
    // Op Info
    // =========================================================================

    @Contract(pure = true)
    @Override
    public @NotNull Class<? extends Dialect> getDialect() {
      return BuiltinDialect.class;
    }

    @Contract(pure = true)
    @Override
    public @NotNull String getNamespace() {
      return "";
    }
  }

  /**
   * Top-level container operation for a DGIR program.
   *
   * <p>{@code ProgramOp} holds a single region with a single block. That block must contain exactly
   * one {@link FuncOp} whose symbol name is {@code "main"}; this function serves as the program
   * entry point.
   *
   * <p>The op acts as a {@link ISymbolTable} anchor: all symbol look-ups issued by nested
   * operations resolve against this table.
   *
   * <p>Ident: {@code program}
   *
   * <pre>{@code
   * program {
   *   func.func @main() { ... }
   * }
   * }</pre>
   */
  final class ProgramOp extends BuiltinOp
      implements BuiltinOps,
          ISymbolTable,
          INoTerminator,
          IGlobalContainer,
          ISingleRegion,
          ISingleBlock {

    // =========================================================================
    // Type Info
    // =========================================================================

    @Contract(pure = true)
    @Override
    public @NotNull String getIdent() {
      return "program";
    }

    @Override
    public @NotNull Function<Operation, Boolean> getVerifier() {
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

  /**
   * Identity operation that returns its operand as the result. Used for assignment of values to an
   * existing value.
   */
  final class IdOp extends BuiltinOp implements BuiltinOps, ISingleOperand, IHasResult {
    @Contract(pure = true)
    @Override
    public @NotNull String getIdent() {
      return "id";
    }

    @Override
    public @NotNull Function<Operation, Boolean> getVerifier() {
      return operation -> {
        if (!operation
            .getOperandValue(0)
            .orElseThrow()
            .getType()
            .equals(operation.getOutputValue().orElseThrow().getType())) {
          operation.emitError("Result type must match operand type");
          return false;
        }
        return true;
      };
    }

    private IdOp() {}

    public IdOp(@NotNull Location location, @NotNull Value value) {
      setOperation(true, Operation.Create(location, this, List.of(value), null, value.getType()));
    }

    public IdOp(@NotNull Location location, @NotNull Value from, @NotNull Value to) {
      setOperation(true, Operation.Create(location, this, List.of(from), null, from.getType()));
      setOutputValue(to);
    }
  }
}
