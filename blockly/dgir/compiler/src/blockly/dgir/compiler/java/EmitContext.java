package blockly.dgir.compiler.java;

import blockly.dgir.compiler.SymbolTable.ScopedSymbolTable;
import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static blockly.dgir.compiler.java.CompilerUtils.isSyntheticDebugNode;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
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
      previous().ifPresent(context::setInsertionPoint);
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
  }

  public static final class SymbolScope implements AutoCloseable {
    private final @NotNull EmitContext context;

    public SymbolScope(@NotNull EmitContext context, boolean isolatedFromAbove) {
      this.context = context;
      context.symbolTable.pushScope(isolatedFromAbove);
    }

    @Override
    public void close() {
      context.symbolTable.popScope();
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

  public EmitContext(@NotNull String filename) {
    this.filename = filename;
  }

  public boolean compilationSuccessful() {
    return errors.isEmpty();
  }

  @NotNull
  public Location loc(@NotNull Node node) {
    if (isSyntheticDebugNode(node)) {
      return new Location(filename, -1, -1);
    }
    if (node.getRange().isEmpty()) {
      logger.warning("No range information available for AST node, using default location.");
      return Location.UNKNOWN;
    }
    Range r = node.getRange().get();
    return new Location(filename, r.begin.line, r.begin.column);
  }

  public void putSymbol(@NotNull String name, @NotNull Value value) {
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

  @SafeVarargs
  public final @NotNull Optional<Operation> findAncestor(Class<? extends Op>... clazz) {
    assert insertionPoint != null : "Insertion block must be set before finding an ancestor.";
    Operation parent = insertionPoint.block.getParentOperation().orElseThrow();
    List<Class<? extends Op>> classes = Arrays.asList(clazz);
    while (parent != null && classes.stream().noneMatch(parent::isa)) {
      parent = parent.getParentOperation().orElse(null);
    }
    return Optional.ofNullable(parent);
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
