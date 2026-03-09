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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class EmitContext {
  private static final java.util.logging.Logger logger =
      java.util.logging.Logger.getLogger(EmitContext.class.getName());
  private final @NotNull String filename;

  private final List<String> info = new ArrayList<>();
  private final List<String> warnings = new ArrayList<>();
  private final List<String> errors = new ArrayList<>();

  public @Nullable BuiltinOps.ProgramOp program = null;

  private final ScopedSymbolTable<String, Value> symbolTable = ScopedSymbolTable.createRoot();

  private @Nullable Block programBlock = null;

  /** The block into which the next IR operation will be inserted. */
  private @Nullable Block insertionBlock = null;

  /**
   * The index within the insertion block at which the next IR operation will be inserted. If the
   * index is not -1, it will be incremented after each insertion.
   */
  private int insertionIndex = -1;

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
  public Optional<Pair<Block, Integer>> setInsertionPoint(@Nullable Block block, int index) {
    Optional<Pair<Block, Integer>> oldInsertionPoint = Optional.empty();
    if (insertionBlock != null) {
      oldInsertionPoint = Optional.of(new Pair<>(insertionBlock, insertionIndex));
    }
    insertionBlock = block;
    insertionIndex = index;
    return oldInsertionPoint;
  }

  public @NotNull Operation insert(@NotNull Operation op) {
    assert insertionBlock != null : "Insertion block must be set before inserting an operation.";
    if (insertionIndex == -1) {
      insertionBlock.addOperation(op);
    } else {
      insertionBlock.addOperation(op, insertionIndex++);
    }
    return op;
  }

  <OpT extends Op> @NotNull OpT insert(@NotNull OpT op) {
    insert(op.getOperation());
    return op;
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
