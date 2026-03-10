package blockly.dgir.compiler.java;

import blockly.dgir.compiler.SymbolTable.ScopedSymbolTable;
import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.utils.Pair;
import dgir.core.debug.Location;
import dgir.core.ir.Block;
import dgir.core.ir.Op;
import dgir.core.ir.Operation;
import dgir.core.ir.Value;
import dgir.dialect.builtin.BuiltinOps;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.*;

public final class EmitContext {
  public static final class InsertionPoint implements AutoCloseable {
    private final @NotNull Block block;
    private int index;
    private final @NotNull EmitContext context;
    private final @NotNull Optional<InsertionPoint> previous;

    public InsertionPoint(
        @NotNull Block block,
        int index,
        @NotNull EmitContext context,
        @NotNull Optional<InsertionPoint> previous) {
      this.block = block;
      this.index = index;
      this.context = context;
      this.previous = previous;
    }

    @Override
    public void close() {
      previous.ifPresent(context::setInsertionPoint);
    }

    public Block block() {
      return block;
    }

    public int index() {
      return index;
    }

    public EmitContext context() {
      return context;
    }

    public Optional<InsertionPoint> previous() {
      return previous;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) return true;
      if (obj == null || obj.getClass() != this.getClass()) return false;
      var that = (InsertionPoint) obj;
      return Objects.equals(this.block, that.block)
          && this.index == that.index
          && Objects.equals(this.context, that.context)
          && Objects.equals(this.previous, that.previous);
    }

    @Override
    public int hashCode() {
      return Objects.hash(block, index, context, previous);
    }

    @Override
    public String toString() {
      return "InsertionPoint["
          + "block="
          + block
          + ", "
          + "index="
          + index
          + ", "
          + "context="
          + context
          + ", "
          + "previous="
          + previous
          + ']';
    }
  }

  private static final java.util.logging.Logger logger =
      java.util.logging.Logger.getLogger(EmitContext.class.getName());
  private final @NotNull String filename;

  private final List<String> info = new ArrayList<>();
  private final List<String> warnings = new ArrayList<>();
  private final List<String> errors = new ArrayList<>();

  public @Nullable BuiltinOps.ProgramOp program = null;

  private final ScopedSymbolTable<String, Value> symbolTable = ScopedSymbolTable.createRoot();

  private @Nullable Block programBlock = null;

  /** The point at which the next operation will be inserted. */
  private @Nullable InsertionPoint insertionPoint = null;

  private @Nullable EmitResult<Optional<Value>> lastResult;

  EmitContext(@NotNull String filename) {
    this.filename = filename;
  }

  public boolean compilationSuccessfull() {
    return errors.isEmpty();
  }

  @NotNull
  public Location loc(@NotNull Node node) {
    if (node.getRange().isEmpty()) {
      logger.warning("No range information available for AST node, using default location.");
      return new Location(filename, 0, 0);
    }
    Range r = node.getRange().get();
    return new Location(filename, r.begin.line, r.begin.column);
  }

  public void pushSymbolScope(boolean isolatedFromAbove) {
    symbolTable.pushScope(isolatedFromAbove);
  }

  @NotNull
  public Pair<@NotNull Boolean, @NotNull Map<@NotNull String, @NotNull Value>> popSymbolScope() {
    return symbolTable.popScope();
  }

  public void putSymbol(
      @NotNull String name, @NotNull Value value, @NotNull ResolvedType resolvedType) {
    symbolTable.insertScoped(name, value);
  }

  public @NotNull Optional<Value> lookupSymbol(@NotNull String qualifiedMangledName) {
    return symbolTable.lookupScoped(qualifiedMangledName);
  }

  @Contract(pure = true)
  public @NotNull Optional<Block> getProgramBlock() {
    return Optional.ofNullable(programBlock);
  }

  public void setProgramBlock(@NotNull Block block) {
    programBlock = block;
  }

  /**
   * Set the insertion point for the next IR operation to be inserted.
   *
   * @param block the block to insert into. The new operation will be inserted at the index of this
   *     block.
   * @param index the index within the block to insert at. If index is -1, the new operation will be
   *     inserted at the end of the block.
   * @return an optional containing the old insertion point, or empty if there was no old insertion
   *     point.
   */
  public @Nullable InsertionPoint setInsertionPoint(@Nullable Block block, int index) {
    if (block != null) {
      insertionPoint = new InsertionPoint(block, index, this, Optional.ofNullable(insertionPoint));
    } else {
      insertionPoint = null;
    }
    return insertionPoint;
  }

  public void setInsertionPoint(@Nullable InsertionPoint insertionPoint) {
    this.insertionPoint = insertionPoint;
  }

  public @NotNull Operation insert(@NotNull Operation op) {
    assert insertionPoint != null : "Insertion block must be set before inserting an operation.";
    if (insertionPoint.index == -1) {
      insertionPoint.block.addOperation(op);
    } else {
      insertionPoint.block.addOperation(op, insertionPoint.index++);
    }
    return op;
  }

  <OpT extends Op> @NotNull OpT insert(@NotNull OpT op) {
    insert(op.getOperation());
    return op;
  }

  public void succeed() {
    lastResult = EmitResult.of(Optional.empty());
  }

  public void succeed(@NotNull Value value) {
    lastResult = EmitResult.of(Optional.of(value));
  }

  public void fail() {
    lastResult = EmitResult.failure();
  }

  public void fail(Node node, String message, Object... args) {
    lastResult = EmitResult.failure(this, node, message, args);
  }

  public @NotNull EmitResult<Optional<Value>> consumeLastResult() {
    if (lastResult == null) {
      throw new IllegalStateException("No value available to consume.");
    }
    EmitResult<Optional<Value>> result = lastResult;
    lastResult = null;
    return result;
  }

  /**
   * Creates a descriptive diagnostic message for the given AST node and message. The message should
   * be human-readable and should include the source location of the node for easier debugging.
   *
   * @param node the AST node
   * @param message the diagnostic message
   * @return a formatted diagnostic message
   */
  public @NotNull String formatDiagnostic(Node node, @NotNull String message, Object... args) {
    Location loc = loc(node);
    StringBuilder formated =
        new StringBuilder(
            MessageFormat.format(
                "{0}:{1}:{2} \"{3}\"\n{4}\n", loc.file(), loc.line(), loc.column(), node, message));
    // Append the string representation of the args to the message.
    if (args.length > 0) {
      formated.append("Additional info:\n");
      for (Object arg : args) {
        formated.append("- ").append(arg).append("\n");
      }
    }
    return formated.toString();
  }

  /**
   * Emit an error message for the given AST node and message.
   *
   * @param node the AST node
   * @param message the diagnostic message
   * @param args any additional objects to include in the diagnostic message. These will be appended
   *     to the message.
   */
  public void emitError(Node node, String message, Object... args) {
    errors.add(formatDiagnostic(node, message, args));
  }

  /**
   * Emit a warning message for the given AST node and message.
   *
   * @param node the AST node
   * @param message the diagnostic message
   * @param args any additional objects to include in the diagnostic message. These will be appended
   *     to the message.
   */
  public void emitWarning(Node node, String message, Object... args) {
    warnings.add(formatDiagnostic(node, message, args));
  }

  /**
   * Emit an info message for the given AST node and message.
   *
   * @param node the AST node
   * @param message the diagnostic message
   * @param args any additional objects to include in the diagnostic message. These will be appended
   *     to the message.
   */
  public void emitInfo(Node node, String message, Object... args) {
    info.add(formatDiagnostic(node, message, args));
  }

  public void printDiagnostics() {
    for (String error : errors) {
      System.err.println(error);
    }
    for (String warning : warnings) {
      System.err.println(warning);
    }
    for (String info : info) {
      System.out.println(info);
    }
  }
}
