package compiler.java;

import static dialect.arith.ArithAttrs.BinModeAttr.BinMode;
import static dialect.arith.ArithAttrs.CompModeAttr.CompMode;
import static dialect.arith.ArithOps.ConstantOp;
import static dialect.builtin.BuiltinAttrs.*;
import static dialect.builtin.BuiltinOps.ProgramOp;
import static dialect.builtin.BuiltinTypes.FloatT;
import static dialect.builtin.BuiltinTypes.IntegerT;
import static dialect.func.FuncOps.FuncOp;
import static dialect.func.FuncOps.ReturnOp;
import static dialect.func.FuncTypes.FuncType;
import static dialect.scf.ScfOps.*;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.Pair;
import core.Dialect;
import core.debug.Location;
import core.debug.ValueDebugInfo;
import core.ir.*;
import core.serialization.Utils;
import core.traits.ITerminator;
import dialect.arith.ArithOps.BinaryOp;
import dialect.arith.ArithOps.CompareOp;
import dialect.dg.DungeonDialect;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JavaCompiler {
  static Logger logger = Logger.getLogger(JavaCompiler.class.getName());

  protected JavaCompiler() {}

  public static @NotNull Optional<ProgramOp> compileSource(
      @NotNull String source, @NotNull String filename) {
    CompilationUnit result;
    try {
      result = StaticJavaParser.parse(source);
    } catch (ParseProblemException e) {
      logger.severe("Failed to parse Java source code: " + e.getMessage());
      return Optional.empty();
    } catch (RuntimeException e) {
      logger.severe("Unexpected error while parsing Java source code: " + e.getMessage());
      return Optional.empty();
    }

    JavaAstEmitter emitter = new JavaAstEmitter();
    return emitter.emit(result, filename);
  }

  private static final class EmitContext {
    private final @NotNull String filename;

    private final List<String> info = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    private @Nullable ProgramOp program = null;

    /**
     * A stack of symbol tables representing the current scope. Each map maps variable names to
     * their corresponding IR values. The top of the stack is the innermost scope, and the bottom is
     * the outermost scope. This is used to resolve variable references during emission.
     *
     * <p>Everytime an IIsolatedFromAbove scope is entered, a new symbol table is pushed onto the
     * stack. Therefore, only the topmost symbol table should be used for variable resolution.
     * Otherwise, there is a chance that variables from outer scopes are accidentally captured,
     * which would be a bug.
     */
    private final @NotNull Deque<Map<@NotNull String, @NotNull Value>> scopedSymbolTable =
        new ArrayDeque<>();

    /** The block into which the next IR operation will be inserted. */
    private @Nullable Block insertionBlock = null;

    /**
     * The index within the insertion block at which the next IR operation will be inserted. If the
     * index is not -1, it will be incremented after each insertion.
     */
    private int insertionIndex = -1;

    public EmitContext(@NotNull String filename) {
      this.filename = filename;
    }

    public @NotNull Location loc(@NotNull Node node) {
      if (node.getRange().isEmpty()) {
        logger.warning("No range information available for AST node, using default location.");
        return new Location(filename, 0, 0);
      }
      Range r = node.getRange().get();
      return new Location(filename, r.begin.line, r.begin.column);
    }

    public void pushScope() {
      scopedSymbolTable.push(new HashMap<>());
    }

    public void popScope() {
      scopedSymbolTable.pop();
    }

    public void putValue(@NotNull String s, @NotNull Value value) {
      assert !scopedSymbolTable.isEmpty() : "There must be at least one scope to put a value in.";
      scopedSymbolTable.peek().put(s, value);
    }

    public @NotNull Optional<Value> getValue(@NotNull String s) {
      assert !scopedSymbolTable.isEmpty() : "There must be at least one scope to get a value from.";
      return Optional.ofNullable(scopedSymbolTable.peek().get(s));
    }

    /**
     * Set the insertion point for the next IR operation to be inserted.
     *
     * @param block the block to insert into. The new operation will be inserted at the index of
     *     this block.
     * @param index the index within the block to insert at. If index is -1, the new operation will
     *     be inserted at the end of the block.
     * @return an optional containing the old insertion point, or empty if there was no old
     *     insertion point.
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

    public <OpT extends Op> @NotNull OpT insert(@NotNull OpT op) {
      insert(op.getOperation());
      return op;
    }

    /**
     * Creates a descriptive diagnostic message for the given AST node and message. The message
     * should be human-readable and should include the source location of the node for easier
     * debugging.
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
                  "{0}:{1}:{2}: {3}\n{4}", loc.file(), loc.line(), loc.column(), message, node));
      // Append the string representation of the args to the message.
      if (args.length > 0) {
        formated.append("\nAdditional info:\n");
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
     * @param args any additional objects to include in the diagnostic message. These will be
     *     appended to the message.
     */
    public void emitError(Node node, String message, Object... args) {
      errors.add(formatDiagnostic(node, message, args));
    }

    /**
     * Emit a warning message for the given AST node and message.
     *
     * @param node the AST node
     * @param message the diagnostic message
     * @param args any additional objects to include in the diagnostic message. These will be
     *     appended to the message.
     */
    public void emitWarning(Node node, String message, Object... args) {
      warnings.add(formatDiagnostic(node, message, args));
    }

    /**
     * Emit an info message for the given AST node and message.
     *
     * @param node the AST node
     * @param message the diagnostic message
     * @param args any additional objects to include in the diagnostic message. These will be
     *     appended to the message.
     */
    public void emitInfo(Node node, String message, Object... args) {
      info.add(formatDiagnostic(node, message, args));
    }

    public void printDiagnostics() {
      for (String error : errors) {
        logger.severe(error);
      }
      for (String warning : warnings) {
        logger.warning(warning);
      }
      for (String info : info) {
        logger.info(info);
      }
    }
  }

  private static Optional<Type> fromAstType(
      com.github.javaparser.ast.type.Type type, EmitContext context) {
    if (!type.isPrimitiveType()) {
      context.emitError(type, "Only primitive types are supported.", type);
      return Optional.empty();
    }
    PrimitiveType primitiveType = type.asPrimitiveType();
    return switch (primitiveType.getType()) {
      case BOOLEAN -> Optional.of(IntegerT.BOOL);
      case CHAR, BYTE -> Optional.of(IntegerT.INT8);
      case SHORT -> Optional.of(IntegerT.INT16);
      case INT -> Optional.of(IntegerT.INT32);
      case LONG -> Optional.of(IntegerT.INT64);
      case FLOAT -> Optional.of(FloatT.FLOAT32);
      case DOUBLE -> Optional.of(FloatT.FLOAT64);
      default -> {
        context.emitError(type, "Unknown primitive type: " + primitiveType.getType(), type);
        yield Optional.empty();
      }
    };
  }

  private static final class JavaAstEmitter extends VoidVisitorAdapter<EmitContext> {
    private @NotNull Optional<Value> resolveName(
        @NotNull String name, @NotNull Node site, EmitContext context) {
      Optional<Value> valueOpt = context.getValue(name);
      if (valueOpt.isEmpty()) {
        context.emitError(site, "Variable " + name + " is not defined in the current scope.");
      }
      return valueOpt;
    }

    private void bindName(
        @NotNull String name, @NotNull Value value, @NotNull Node site, EmitContext context) {
      context.putValue(name, value);
      value.setDebugInfo(new ValueDebugInfo(context.loc(site), name));
    }

    private Optional<ProgramOp> emit(CompilationUnit compilationUnit, String filename) {
      // Register all dialects so that we can use them during emission.
      Dialect.registerAllDialects();
      DungeonDialect dungeonDialect = new DungeonDialect();
      dungeonDialect.register();

      EmitContext context = new EmitContext(filename);
      compilationUnit.accept(this, context);
      context.printDiagnostics();
      return Optional.ofNullable(context.program);
    }

    @Override
    public void visit(CompilationUnit n, EmitContext context) {
      // Check that there are no unsupported imports.
      for (ImportDeclaration importDeclaration : n.getImports()) {
        switch (importDeclaration.getName().asString()) {
          case "Dungeon.Hero":
            continue;
          default:
            context.emitError(n, "Import " + importDeclaration.getName() + " is not supported.");
            return;
        }
      }
      if (n.getModule().isPresent()) {
        context.emitError(
            n, "Module " + n.getModule() + " and models in general are not supported.");
        return;
      }
      if (n.getPackageDeclaration().isPresent()) {
        context.emitError(
            n, "Package declaration " + n.getPackageDeclaration() + " is not supported.");
        return;
      }
      if (n.getTypes().size() != 1) {
        context.emitError(
            n,
            "Only one class is supported and that is the class containing the main method."
                + " Found "
                + n.getTypes().size()
                + " classes.");
        return;
      }
      // Check that there is a main method which takes no arguments.
      if (n.getType(0).getMethodsBySignature("main").isEmpty()) {
        logger.severe("No main method found.");
        return;
      }
      // Program looks generally ok, create the root ProgramOp.
      ProgramOp program = new ProgramOp(context.loc(n));
      context.pushScope();
      context.setInsertionPoint(program.getEntryBlock(), -1);

      n.getTypes().forEach(t -> t.accept(this, context));

      if (context.errors.isEmpty()) {
        context.program = program;
      } else {
        String incompleteProgram = Utils.getMapper(true).writeValueAsString(context.errors);
        context.emitError(n, "Incomplete program: ", incompleteProgram);
      }
      context.popScope();
    }

    @Override
    public void visit(ImportDeclaration n, EmitContext context) {
      context.emitError(n, "Import " + n.getName() + " is not supported.");
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, EmitContext context) {
      if (!n.getExtendedTypes().isEmpty()) {
        context.emitError(
            n, "Class " + n.getName() + " extends a class. Extending is not supported.");
        return;
      }
      if (!n.getImplementedTypes().isEmpty()) {
        context.emitError(
            n, "Class " + n.getName() + " implements an interface. Implementing is not supported.");
        return;
      }
      if (!n.getTypeParameters().isEmpty()) {
        context.emitError(
            n,
            "Class " + n.getName() + " has type parameters. Generics classes are not supported.");
        return;
      }
      if (n.getMembers().stream().anyMatch(m -> !(m instanceof MethodDeclaration))) {
        context.emitError(
            n,
            "Class "
                + n.getName()
                + " has members which arent methods. Classes with non method members are not supported.");
        return;
      }
      if (!n.isPublic()) {
        context.emitWarning(
            n,
            "Class " + n.getName() + " is not public. It is recommended to make the class public.");
      }
      if (!n.getAnnotations().isEmpty()) {
        context.emitWarning(
            n,
            "Class "
                + n.getName()
                + " has annotations. Annotations are not supported and will be ignored.");
      }

      // Emit all methods in the class. These will insert themselves into the program op
      n.getMethods().forEach(m -> m.accept(this, context));
    }

    @Override
    public void visit(MethodDeclaration n, EmitContext context) {
      if (!n.isStatic()) {
        context.emitError(
            n, "Method " + n.getName() + " is not static. Only static methods are supported.");
        return;
      }
      if (!n.getThrownExceptions().isEmpty()) {
        context.emitError(
            n,
            "Method " + n.getName() + " throws an exception. Throwing is not supported.",
            n.getThrownExceptions());
        return;
      }
      if (!n.getTypeParameters().isEmpty()) {
        context.emitError(
            n,
            "Method " + n.getName() + " has type parameters. Generics methods are not supported.",
            n.getTypeParameters());
        return;
      }
      if (n.getReceiverParameter().isPresent()) {
        context.emitError(
            n,
            "Method "
                + n.getName()
                + " has a receiver parameter. Receivers are not supported. (Who are you to speak in such strange ways?)",
            n.getReceiverParameter());
        return;
      }
      if (n.getBody().isEmpty()) {
        context.emitError(
            n, "Method " + n.getName() + " has no body. Methods with no body are not supported.");
        return;
      }

      if (!n.isPublic()) {
        context.emitWarning(
            n,
            "Method "
                + n.getName()
                + " is not public. It is recommended to make the method public.");
      }
      if (!n.getAnnotations().isEmpty())
        context.emitWarning(
            n,
            "Method "
                + n.getName()
                + " has annotations. Annotations are not supported and will be ignored.");

      // Get the input types of the method and their names.
      List<Type> inputTypes = new ArrayList<>();
      List<String> inputNames = new ArrayList<>();
      for (Parameter param : n.getParameters()) {
        n.getAnnotations()
            .forEach(
                a ->
                    context.emitWarning(
                        a, "Annotations on function parameters are not supported.", a));
        if (!param.getVarArgsAnnotations().isEmpty()) {
          context.emitError(
              param,
              "Method "
                  + n.getName()
                  + " has a varargs parameter. Varargs parameters are not supported.");
          return;
        }
        // Try to get the DGIR Type of the parameter, and if that fails, return. The error is
        // already in the diagnostics.
        Optional<Type> typeOpt = fromAstType(param.getType(), context);
        if (typeOpt.isEmpty()) {
          return;
        }
        inputTypes.add(typeOpt.get());
        inputNames.add(param.getName().getIdentifier());
      }

      // Get the return type of the method.
      Optional<Type> outputType = Optional.empty();
      if (!n.getType().isVoidType()) {
        outputType = fromAstType(n.getType(), context);
        if (outputType.isEmpty()) {
          return;
        }
      }

      // Create the function op.
      FuncOp funcOp =
          context.insert(
              new FuncOp(
                  context.loc(n),
                  n.getName().asString(),
                  new FuncType(inputTypes, outputType.orElse(null))));

      // Emit all statements in the method body. These will insert themselves into the function op.
      var previousInsertionPoint = context.setInsertionPoint(funcOp.getEntryBlock(), -1);
      // Put the function arguments in the symbol table so that they can be referenced in the body.
      context.pushScope();
      for (int i = 0; i < inputNames.size(); i++) {
        context.putValue(inputNames.get(i), funcOp.getArgument(i).orElseThrow());
      }

      // Emit the body of the method.
      n.getBody().get().accept(this, context);

      context.popScope();
      // Set the insertion point to the parent scope.
      previousInsertionPoint.ifPresent(p -> context.setInsertionPoint(p.a, p.b));
      // Make sure we have an implicit return in case the method has a void return type and the last
      // statement is not a return statement.
      for (Block block : funcOp.getRegion().getBlocks()) {
        if (block.getOperations().isEmpty()
            || !block.getOperations().getLast().hasTrait(ITerminator.class)) {
          block.addOperation(new ReturnOp(context.loc(n)));
        }
      }
    }

    @Override
    public void visit(BlockStmt n, EmitContext context) {
      for (Statement stmt : n.getStatements()) {
        switch (stmt) {
          case BlockStmt blockStmt -> {
            var scope = context.insert(new ScopeOp(context.loc(n)));
            var pip = context.setInsertionPoint(scope.getEntryBlock(), -1);
            blockStmt.accept(this, context);
            pip.ifPresent(p -> context.setInsertionPoint(p.a, p.b));
          }
          case BreakStmt breakStmt -> context.insert(new BreakOp(context.loc(n)));
          case ContinueStmt continueStmt -> context.insert(new ContinueOp(context.loc(n)));
          case EmptyStmt emptyStmt -> {
            // Do nothing for empty statements.
          }
          case ExpressionStmt expressionStmt -> expressionStmt.accept(this, context);
          case ForStmt forStmt -> forStmt.accept(this, context);
          case IfStmt ifStmt -> ifStmt.accept(this, context);
          case ReturnStmt returnStmt -> returnStmt.accept(this, context);
          default -> {
            context.emitError(n, "Statement " + stmt + " is not supported.");
          }
        }
      }
    }

    @Override
    public void visit(ExpressionStmt n, EmitContext context) {
      switch (n.getExpression()) {
        case AssignExpr assignExpr -> assignExpr.accept(this, context);
        case VariableDeclarationExpr variableDeclarationExpr ->
            variableDeclarationExpr.accept(this, context);
        default -> emitExpression(n.getExpression(), context);
      }
    }

    @Override
    public void visit(AssignExpr n, EmitContext context) {
      if (!n.getTarget().isNameExpr()) {
        context.emitError(n, "Assignment target " + n.getTarget() + " is not a variable.");
        return;
      }

      String varName = n.getTarget().asNameExpr().getName().asString();
      if (resolveName(varName, n, context).isEmpty()) {
        return;
      }

      Optional<Value> rhs = emitExpression(n.getValue(), context);
      if (rhs.isEmpty()) {
        return;
      }

      context.putValue(varName, rhs.get());
      rhs.get().setDebugInfo(new ValueDebugInfo(context.loc(n), varName));
    }

    private @NotNull Optional<TypedAttribute> valueAttrFromLiteralExpr(
        LiteralExpr literalExpr, EmitContext context) {
      return Optional.ofNullable(
          switch (literalExpr) {
            case BooleanLiteralExpr boolL ->
                new IntegerAttribute(boolL.getValue() ? 1 : 0, IntegerT.BOOL);
            case CharLiteralExpr charL ->
                new IntegerAttribute((byte) charL.getValue().charAt(0), IntegerT.INT8);
            case DoubleLiteralExpr doubleL ->
                new FloatAttribute(Double.parseDouble(doubleL.getValue()), FloatT.FLOAT64);
            case IntegerLiteralExpr intL ->
                new IntegerAttribute(Integer.parseInt(intL.getValue()), IntegerT.INT32);
            case LongLiteralExpr longL ->
                new IntegerAttribute(Long.parseLong(longL.getValue()), IntegerT.INT64);
            case StringLiteralExpr stringL -> new StringAttribute(stringL.getValue());
            default -> {
              context.emitError(
                  literalExpr,
                  "Literal expression "
                      + literalExpr
                      + " is not supported. Only boolean, char, double, integer, long, and string literals are supported.");
              yield null;
            }
          });
    }

    @Override
    public void visit(VariableDeclarationExpr n, EmitContext context) {
      if (n.getVariables().size() != 1) {
        context.emitError(n, "Only one variable declaration is supported.");
        return;
      }
      VariableDeclarator variableDeclarator = n.getVariables().get(0);
      if (variableDeclarator.getInitializer().isEmpty()) {
        context.emitError(n, "Variable " + variableDeclarator.getName() + " is not initialized.");
        return;
      }
      Expression initializer = variableDeclarator.getInitializer().get();
      Optional<Value> initValue = emitExpression(initializer, context);
      if (initValue.isEmpty()) {
        return;
      }
      bindName(
          variableDeclarator.getName().asString(), initValue.get(), variableDeclarator, context);
    }

    private @NotNull Optional<Value> emitExpression(Expression expression, EmitContext context) {
      return switch (expression) {
        case LiteralExpr literalExpr -> emitLiteral(literalExpr, context);
        case NameExpr nameExpr -> resolveName(nameExpr.getName().asString(), expression, context);
        case BinaryExpr binaryExpr -> emitBinary(binaryExpr, context);
        default -> {
          context.emitError(
              expression, "Expression %s is not supported in this context.", expression);
          yield Optional.empty();
        }
      };
    }

    private @NotNull Optional<Value> emitLiteral(LiteralExpr literalExpr, EmitContext context) {
      Optional<TypedAttribute> attrOpt = valueAttrFromLiteralExpr(literalExpr, context);
      return attrOpt.map(
          typedAttribute ->
              context.insert(new ConstantOp(context.loc(literalExpr), typedAttribute)).getValue());
    }

    private @NotNull Optional<Value> emitBinary(BinaryExpr binaryExpr, EmitContext context) {
      Optional<Value> lhs = emitExpression(binaryExpr.getLeft(), context);
      Optional<Value> rhs = emitExpression(binaryExpr.getRight(), context);
      if (lhs.isEmpty() || rhs.isEmpty()) {
        return Optional.empty();
      }

      Optional<BinMode> binModeOpt =
          Optional.ofNullable(
              switch (binaryExpr.getOperator()) {
                case PLUS -> BinMode.ADD;
                case MINUS -> BinMode.SUB;
                case MULTIPLY -> BinMode.MUL;
                case DIVIDE -> BinMode.DIV;
                case REMAINDER -> BinMode.MOD;
                default -> null;
              });

      if (binModeOpt.isPresent()) {
        var binOp =
            context.insert(
                new BinaryOp(context.loc(binaryExpr), lhs.get(), rhs.get(), binModeOpt.get()));
        return Optional.of(binOp.getResult());
      }

      Optional<CompMode> compModeOpt =
          Optional.ofNullable(
              switch (binaryExpr.getOperator()) {
                case EQUALS -> CompMode.EQ;
                case NOT_EQUALS -> CompMode.NE;
                case LESS -> CompMode.LT;
                case GREATER -> CompMode.GT;
                case LESS_EQUALS -> CompMode.LE;
                case GREATER_EQUALS -> CompMode.GE;
                default -> null;
              });
      if (compModeOpt.isPresent()) {
        var compOp =
            context.insert(
                new CompareOp(context.loc(binaryExpr), lhs.get(), rhs.get(), compModeOpt.get()));
        return Optional.of(compOp.getResult());
      }
      context.emitError(
          binaryExpr, "Binary operator " + binaryExpr.getOperator() + " is not supported.");
      return Optional.empty();
    }
  }
}
