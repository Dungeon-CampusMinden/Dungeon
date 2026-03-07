package blockly.dgir.compiler.java;

import blockly.dgir.dialect.dg.DungeonDialect;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import dgir.core.Dialect;
import dgir.core.debug.Location;
import dgir.core.debug.ValueDebugInfo;
import dgir.core.ir.*;
import dgir.core.serialization.Utils;
import dgir.dialect.arith.ArithOps.BinaryOp;
import dgir.dialect.builtin.BuiltinOps;
import dgir.dialect.cf.CfOps;
import dgir.dialect.func.FuncOps;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

import static blockly.dgir.compiler.java.CompilerUtils.fromAstType;
import static blockly.dgir.compiler.java.CompilerUtils.isAccessibleFrom;
import static dgir.dialect.arith.ArithAttrs.BinModeAttr.BinMode;
import static dgir.dialect.arith.ArithOps.ConstantOp;
import static dgir.dialect.builtin.BuiltinAttrs.*;
import static dgir.dialect.builtin.BuiltinOps.ProgramOp;
import static dgir.dialect.builtin.BuiltinTypes.*;
import static dgir.dialect.func.FuncOps.FuncOp;
import static dgir.dialect.func.FuncOps.ReturnOp;
import static dgir.dialect.func.FuncTypes.FuncType;
import static dgir.dialect.scf.ScfOps.*;

public class JavaCompiler {
  static Logger logger = Logger.getLogger(JavaCompiler.class.getName());
  static boolean symbolSolverInitialized = false;

  protected JavaCompiler() {}

  public static @NotNull Optional<ProgramOp> compileSource(
      @NotNull String source, @NotNull String filename) {
    IntrinsicRegistry.init();
    CompilationUnit result;

    if (!symbolSolverInitialized) {
      symbolSolverInitialized = true;
      Map<String, String> intrinsics;
      try {
        intrinsics = IntrinsicRegistry.loadAllDungeonFiles();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      Path tempdir;
      try {
        tempdir = Files.createTempDirectory("dgir-java-compiler-intrinsics");
      } catch (IllegalArgumentException | IOException e) {
        throw new RuntimeException(
            "Failed to create temporary directory for JavaParser sources", e);
      }
      for (Map.Entry<String, String> entry : intrinsics.entrySet()) {
        Path path = tempdir.resolve(entry.getKey().replace('.', '/') + ".java");
        try {
          Files.createDirectories(path.getParent());
          Files.writeString(path, entry.getValue());
        } catch (IOException e) {
          throw new RuntimeException(
              "Failed to write Java source file for intrinsic " + entry.getKey(), e);
        }
      }

      CombinedTypeSolver typeSolver = new CombinedTypeSolver();
      typeSolver.add(new ReflectionTypeSolver());

      typeSolver.add(new JavaParserTypeSolver(tempdir));
      StaticJavaParser.getParserConfiguration().setSymbolResolver(new JavaSymbolSolver(typeSolver));
    }
    try {
      result = StaticJavaParser.parse(source);
    } catch (ParseProblemException e) {
      logger.severe("Failed to parse Java source code: " + e.getMessage());
      return Optional.empty();
    }

    JavaAstEmitter emitter = new JavaAstEmitter();
    return emitter.emit(result, filename).toOptional();
  }

  private static final class JavaAstEmitter extends VoidVisitorAdapter<EmitContext> {
    private EmitResult<ProgramOp> emit(CompilationUnit compilationUnit, String filename) {
      // Register all dialects so that we can use them during emission.
      Dialect.registerAllDialects();
      DungeonDialect dungeonDialect = new DungeonDialect();
      dungeonDialect.register();

      EmitContext context = new EmitContext(filename);
      compilationUnit.accept(this, context);
      context.printDiagnostics();
      return EmitResult.ofNullable(context.program);
    }

    @Override
    public void visit(CompilationUnit n, EmitContext context) {
      // Check that there are no unsupported imports.
      for (ImportDeclaration importDeclaration : n.getImports()) {
        switch (importDeclaration.getName().asString()) {
          case "Dungeon.Hero", "Dungeon.IO":
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
      // Program looks generally ok, create the root ProgramOp.
      ProgramOp program = new ProgramOp(context.loc(n));
      context.pushSymbolScope(true);
      context.setInsertionPoint(program.getEntryBlock(), -1);

      n.getTypes().forEach(t -> t.accept(this, context));

      if (context.compilationSuccessfull()) {
        context.program = program;
      } else {
        String incompleteProgram = Utils.getMapper(true).writeValueAsString(program);
        context.emitError(n, "Incorrect program", incompleteProgram);
      }
      context.popSymbolScope();
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
      ResolvedMethodDeclaration resolvedN = n.resolve();
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
      List<String> inputNames = new ArrayList<>();
      List<ResolvedType> resolvedInputTypes = new ArrayList<>();
      List<Type> inputTypes =
          new ArrayList<>(
              n.getParameters().stream()
                  .map(
                      param -> {
                        if (!param.getAnnotations().isEmpty())
                          context.emitWarning(
                              param,
                              "Annotations on function parameters are not supported.",
                              param.getAnnotations());
                        if (!param.getVarArgsAnnotations().isEmpty()) {
                          context.emitError(
                              param,
                              "Varargs annotations on function parameters are not supported.",
                              param.getVarArgsAnnotations());
                          return null;
                        }
                        // Try to get the DGIR Type of the parameter, and if that fails, return. The
                        // error is
                        // already in the diagnostics.
                        Optional<ResolvedType> resolvedType =
                            CompilerUtils.resolveType(param.getType(), context);
                        if (resolvedType.isEmpty()) {
                          return null;
                        }
                        resolvedInputTypes.add(resolvedType.get());
                        Optional<Type> type = fromAstType(resolvedType.get(), param, context);
                        if (type.isEmpty()) {
                          return null;
                        }
                        inputNames.add(param.getName().getIdentifier());
                        return type.get();
                      })
                  .toList());
      // Check that all types were resolved correctly
      if (inputTypes.stream().anyMatch(Objects::isNull)) {
        return;
      }

      // Get the return type of the method.
      Optional<Type> outputType = Optional.empty();
      if (!n.getType().isVoidType()) {
        Optional<ResolvedType> resolvedType = CompilerUtils.resolveType(n.getType(), context);
        if (resolvedType.isEmpty()) {
          return;
        }
        outputType = fromAstType(resolvedType.get(), n, context);
        if (outputType.isEmpty()) {
          return;
        }
      }

      // Create the function op.
      String fullyQualifiedMethodName =
          "main".equals(n.getNameAsString()) ? "main" : resolvedN.getQualifiedSignature();
      FuncOp funcOp =
          context.insert(
              new FuncOp(
                  context.loc(n),
                  fullyQualifiedMethodName,
                  new FuncType(inputTypes, outputType.orElse(null))));

      // Emit all statements in the method body. These will insert themselves into the function op.
      var previousInsertionPoint = context.setInsertionPoint(funcOp.getEntryBlock(), -1);
      // Put the function arguments in the symbol table so that they can be referenced in the body.
      context.pushSymbolScope(true);
      for (int i = 0; i < inputNames.size(); i++) {
        context.putSymbol(
            inputNames.get(i), funcOp.getArgument(i).orElseThrow(), resolvedInputTypes.get(i));
      }

      // Emit the body of the method.
      n.getBody().get().accept(this, context);

      context.popSymbolScope();
      // Set the insertion point to the parent scope.
      previousInsertionPoint.ifPresent(p -> context.setInsertionPoint(p.a, p.b));
      // Make sure we have an implicit return in case the method has a void return type and the last
      // statement is not a return statement.
      funcOp.addImplicitTerminators();
    }

    @Override
    public void visit(BlockStmt n, EmitContext context) {
      for (Statement stmt : n.getStatements()) {
        switch (stmt) {
          case BlockStmt blockStmt -> {
            var scope = context.insert(new ScopeOp(context.loc(n)));
            var pip = context.setInsertionPoint(scope.getEntryBlock(), -1);
            blockStmt.accept(this, context);
            scope.addImplicitTerminators();
            pip.ifPresent(p -> context.setInsertionPoint(p.a, p.b));
          }
          case BreakStmt ignored -> context.insert(new BreakOp(context.loc(n)));
          case ContinueStmt ignored -> context.insert(new ContinueOp(context.loc(n)));
          case EmptyStmt ignored -> {
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
    public void visit(ForStmt n, EmitContext context) {
      if (n.getCompare().isEmpty()) {
        context.emitError(n, "For loop without comparison is not supported.");
        return;
      }
      if (n.getUpdate().isEmpty()) {
        context.emitError(n, "For loop without update is not supported.");
        return;
      }

      // First emit the initialization outside the for loop.
      for (Expression initExpr : n.getInitialization()) {
        EmitResult<Optional<Value>> initValueRes = emitExpression(initExpr, context);
        if (initValueRes.isFailure()) {
          return;
        }
      }
      // We are using a while op so that we can support more complex update expressions.
      WhileOp whileOp = context.insert(new WhileOp(context.loc(n)));
      Block continueBlock = whileOp.getConditionRegion().addBlock(new Block());
      continueBlock.addOperation(new ContinueOp(context.loc(n)));
      Block breakBlock = whileOp.getConditionRegion().addBlock(new Block());
      breakBlock.addOperation(new BreakOp(context.loc(n)));

      // Open the new scope and place the comparison expression in it.
      context.pushSymbolScope(false);
      context.setInsertionPoint(whileOp.getConditionRegion().getEntryBlock(), -1);
      EmitResult<Optional<Value>> compareValueRes = emitExpression(n.getCompare().get(), context);
      if (compareValueRes.isFailure() || compareValueRes.get().isEmpty()) {
        return;
      }
      Value compareValue = compareValueRes.get().get();
      context.insert(
          new CfOps.BranchCondOp(
              context.loc(n.getCompare().get()), compareValue, continueBlock, breakBlock));

      context.setInsertionPoint(whileOp.getBodyRegion().getEntryBlock(), -1);
      n.getBody().accept(this, context);
    }

    @Override
    public void visit(ReturnStmt n, EmitContext arg) {
      Location trueLocation = arg.loc(n);
      // Move the debug location one further down than the actual return statement, so we can step
      // to the closing curly bracket of functions and inspect the values produced.
      Location debugLocation =
          new Location(trueLocation.file(), trueLocation.line() + 1, trueLocation.column());
      if (n.getExpression().isPresent()) {
        EmitResult<Optional<Value>> exprRes = emitExpression(n.getExpression().get(), arg);
        if (exprRes.isFailure() || exprRes.get().isEmpty()) {
          return;
        }
        arg.insert(new ReturnOp(debugLocation, exprRes.get().get()));
      } else {
        arg.insert(new ReturnOp(debugLocation));
      }
    }

    @Override
    public void visit(AssignExpr n, EmitContext context) {
      EmitResult<Optional<Value>> targetValueRes = emitExpression(n.getTarget(), context);
      if (targetValueRes.isFailure() || targetValueRes.get().isEmpty()) {
        return;
      }
      Value targetValue = targetValueRes.get().get();

      EmitResult<Optional<Value>> rhs = emitExpression(n.getValue(), context);
      if (rhs.isFailure() || rhs.get().isEmpty()) {
        return;
      }

      Value rhsValue = rhs.get().get();

      if (n.getOperator() == AssignExpr.Operator.ASSIGN) {
        EmitResult<Value> implicitCast =
            CompilerUtils.emitImplicitCastIfNeeded(
                n.getValue(),
                rhsValue,
                n.getValue().calculateResolvedType(),
                context.lookupType(targetValue).orElseThrow(),
                context);
        if (implicitCast.isFailure()) {
          return;
        }
        context.insert(new BuiltinOps.IdOp(context.loc(n), implicitCast.get(), targetValue));
        return;
      }

      EmitResult<Value> result =
          emitBinary(
              n,
              n.getOperator().toBinaryOperator().get(),
              targetValue,
              rhsValue,
              Optional.of(targetValue),
              context);

      if (result.isFailure()) {
        context.emitError(
            n, "Failed to emit binary operation for assignment operator " + n.getOperator());
      }
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
      EmitResult<Optional<Value>> initValueRes = emitExpression(initializer, context);
      if (initValueRes.isFailure() || initValueRes.get().isEmpty()) {
        return;
      }
      Value initValue = initValueRes.get().get();
      // Check that the init value and the target have the same value and emit cast statement if not
      Optional<ResolvedValueDeclaration> resolvedVariable =
          CompilerUtils.resolve(variableDeclarator, context);
      if (resolvedVariable.isEmpty()) {
        return;
      }
      EmitResult<Value> implicitCastRes =
          CompilerUtils.emitImplicitCastIfNeeded(
              variableDeclarator,
              initValue,
              variableDeclarator.getInitializer().get().calculateResolvedType(),
              resolvedVariable.get().getType(),
              context);

      if (implicitCastRes.isFailure()) {
        context.emitError(
            n,
            "Failed to emit implicit cast for variable "
                + variableDeclarator.getName()
                + " with initializer "
                + initializer);
      }

      bindName(
          variableDeclarator.getName().asString(),
          implicitCastRes.orElse(initValue),
          resolvedVariable.get().getType(),
          variableDeclarator,
          context);
    }

    private @NotNull EmitResult<Optional<Value>> emitExpression(
        Expression expression, EmitContext context) {
      return switch (expression) {
        case LiteralExpr literalExpr -> emitLiteral(literalExpr, context).map(Optional::of);
        case NameExpr nameExpr ->
            resolveName(nameExpr.getName().asString(), nameExpr, context)
                .map(value -> EmitResult.of(Optional.of(value)))
                .orElse(EmitResult.failure());
        case BinaryExpr binaryExpr ->
            emitBinary(binaryExpr, context, Optional.empty()).map(Optional::of);
        case MethodCallExpr methodCallExpr -> emitMethodCall(methodCallExpr, context);
        case UnaryExpr unaryExpr -> emitUnary(unaryExpr, context).map(Optional::of);
        default -> {
          context.emitError(
              expression,
              expression.getClass().getSimpleName()
                  + " '"
                  + expression
                  + "' is not supported in this context.");
          yield EmitResult.failure();
        }
      };
    }

    private @NotNull EmitResult<Value> emitLiteral(
        @NotNull LiteralExpr literalExpr, @NotNull EmitContext context) {
      Optional<TypedAttribute> attrOpt = valueAttrFromLiteralExpr(literalExpr, context);
      return attrOpt
          .map(
              typedAttribute ->
                  EmitResult.success(
                      context
                          .insert(new ConstantOp(context.loc(literalExpr), typedAttribute))
                          .getValue()))
          .orElseGet(EmitResult::failure);
    }

    private @NotNull EmitResult<Value> emitBinary(
        @NotNull Node site,
        @NotNull BinaryExpr.Operator operator,
        @NotNull Value lhs,
        @NotNull Value rhs,
        @NotNull Optional<Value> targetValue,
        @NotNull EmitContext context) {
      Optional<BinMode> binModeOpt =
          Optional.ofNullable(
              switch (operator) {
                case PLUS -> BinMode.ADD;
                case MINUS -> BinMode.SUB;
                case MULTIPLY -> BinMode.MUL;
                case DIVIDE -> BinMode.DIV;
                case REMAINDER -> BinMode.MOD;
                case OR -> BinMode.OR;
                case AND -> BinMode.AND;
                case BINARY_OR -> BinMode.BOR;
                case BINARY_AND -> BinMode.BAND;
                case XOR -> BinMode.XOR;
                case EQUALS -> BinMode.EQ;
                case NOT_EQUALS -> BinMode.NE;
                case LESS -> BinMode.LT;
                case GREATER -> BinMode.GT;
                case LESS_EQUALS -> BinMode.LE;
                case GREATER_EQUALS -> BinMode.GE;
                case LEFT_SHIFT -> BinMode.LSH;
                case SIGNED_RIGHT_SHIFT -> BinMode.RSHS;
                case UNSIGNED_RIGHT_SHIFT -> BinMode.RSHU;
                default -> null;
              });

      if (binModeOpt.isPresent()) {
        var binOp = context.insert(new BinaryOp(context.loc(site), lhs, rhs, binModeOpt.get()));
        targetValue.ifPresent(binOp::setOutputValue);
        return EmitResult.of(binOp.getResult());
      }
      return EmitResult.failure(
          context, site, "Binary operator " + operator + " is not supported.");
    }

    private @NotNull EmitResult<Value> emitBinary(
        @NotNull BinaryExpr binaryExpr,
        @NotNull EmitContext context,
        @NotNull Optional<Value> targetValue) {
      EmitResult<Optional<Value>> lhsRes = emitExpression(binaryExpr.getLeft(), context);
      EmitResult<Optional<Value>> rhsRes = emitExpression(binaryExpr.getRight(), context);
      if (lhsRes.isFailure()
          || lhsRes.get().isEmpty()
          || rhsRes.isFailure()
          || rhsRes.get().isEmpty()) {
        return EmitResult.failure(context, binaryExpr, "Could not resolve left or right operand.");
      }

      Value lhs = lhsRes.get().get();
      Value rhs = rhsRes.get().get();
      return emitBinary(binaryExpr, binaryExpr.getOperator(), lhs, rhs, targetValue, context);
    }

    /**
     * Emit a function call.
     *
     * @param methodCallExpr the method call expression to emit.
     * @param context the emit context.
     * @return the optional value resulting from the function call, or an empty optional if there
     *     was an error during emission.
     */
    private @NotNull EmitResult<Optional<Value>> emitMethodCall(
        @NotNull MethodCallExpr methodCallExpr, @NotNull EmitContext context) {
      Optional<ResolvedMethodDeclaration> targetMethod =
          CompilerUtils.resolve(methodCallExpr, context);
      if (targetMethod.isEmpty()) {
        return EmitResult.failure(
            context,
            methodCallExpr,
            "Could not resolve method call target. Make sure the method is defined in the same class or imported and that all necessary classes are imported.");
      }
      // Make sure the target method is accessible from the current context. This also checks that
      // the method is
      var callingClass =
          methodCallExpr.findAncestor(ClassOrInterfaceDeclaration.class).orElseThrow().resolve();
      if (isAccessibleFrom(callingClass, targetMethod.get())) {
        return EmitResult.failure(
            context,
            methodCallExpr,
            "Method callee " + targetMethod.get() + " is not visible from " + callingClass);
      }

      if (!targetMethod.get().isStatic()) {
        return EmitResult.failure(
            context, methodCallExpr, "Method calls to non-static methods are not supported.");
      }

      if (targetMethod.get().getNumberOfParams() != methodCallExpr.getArguments().size()) {
        return EmitResult.failure(
            context,
            methodCallExpr,
            "Method call arguments do not match the method signature. Expected "
                + targetMethod.get().getTypeParameters().size()
                + " arguments but found "
                + methodCallExpr.getArguments().size()
                + ".");
      }

      // Get the call args of the method
      List<Value> args = new ArrayList<>();
      for (Expression arg : methodCallExpr.getArguments()) {
        EmitResult<Optional<Value>> argValue = emitExpression(arg, context);
        if (argValue.isFailure() || argValue.get().isEmpty()) {
          return EmitResult.failure(
              context, methodCallExpr, "Could not resolve argument " + arg + " of method call.");
        }
        args.add(argValue.get().get());
      }

      // Check if the caller arguments with the callee param types and emit casts if necessary
      for (int i = 0; i < args.size(); i++) {
        Value callArg = args.get(i);
        ResolvedType targetMethodArgType = targetMethod.get().getParam(i).getType();
        EmitResult<Value> castCallArg =
            CompilerUtils.emitImplicitCastIfNeeded(
                targetMethod.get().getParam(i).toAst().orElseThrow(),
                callArg,
                methodCallExpr.getArgument(i).calculateResolvedType(),
                targetMethodArgType,
                context);
        if (castCallArg.isFailure()) {
          return EmitResult.failure(
              context,
              methodCallExpr,
              "Argument "
                  + callArg
                  + " cannot be implicitly cast to parameter type "
                  + targetMethodArgType.describe());
        }
        args.set(i, castCallArg.get());
      }

      String funcName = targetMethod.get().getQualifiedSignature();
      Optional<Type> returnType = Optional.empty();
      if (!targetMethod.get().getReturnType().isVoid()) {
        returnType = fromAstType(targetMethod.get().getReturnType(), methodCallExpr, context);
      }
      FuncOps.CallOp callOp =
          context.insert(
              new FuncOps.CallOp(
                  context.loc(methodCallExpr), funcName, args, returnType.orElse(null)));

      return EmitResult.success(callOp.getOutput().map(OperationResult::getValue));
    }

    private @NotNull EmitResult<Value> emitIncrement(
        @NotNull Node site, boolean positive, @NotNull Value target, @NotNull EmitContext context) {
      ConstantOp one = context.insert(new ConstantOp(context.loc(site), 1));
      if (positive) {
        return emitBinary(
            site, BinaryExpr.Operator.PLUS, target, one.getValue(), Optional.of(target), context);
      } else {
        return emitBinary(
            site, BinaryExpr.Operator.MINUS, target, one.getValue(), Optional.of(target), context);
      }
    }

    private @NotNull EmitResult<Value> emitUnary(
        @NotNull UnaryExpr unaryExpr, @NotNull EmitContext context) {
      EmitResult<Optional<Value>> operandRes = emitExpression(unaryExpr.getExpression(), context);
      if (operandRes.isFailure() || operandRes.get().isEmpty()) {
        return EmitResult.failure(
            context, unaryExpr, "Could not resolve operand of unary expression.");
      }
      Value operand = operandRes.get().get();
      if (!isNumeric(operand.getType())) {
        context.emitError(
            unaryExpr,
            "Unary operator can only be applied to numeric types. Found type " + operand.getType());
        return EmitResult.failure();
      }
      switch (unaryExpr.getOperator()) {
        case PLUS -> {
          return EmitResult.of(operand);
        }
        case MINUS -> {
          ConstantOp minusOne = context.insert(new ConstantOp(context.loc(unaryExpr), -1));
          return emitBinary(
                  unaryExpr,
                  BinaryExpr.Operator.MULTIPLY,
                  operand,
                  minusOne.getValue(),
                  Optional.empty(),
                  context)
              .or(
                  () ->
                      EmitResult.failure(
                          context, unaryExpr, "Failed to emit unary minus operator."));
        }
        case PREFIX_INCREMENT -> {
          return emitIncrement(unaryExpr, true, operand, context);
        }
        case PREFIX_DECREMENT -> {
          return emitIncrement(unaryExpr, false, operand, context);
        }
        case LOGICAL_COMPLEMENT -> {
          return EmitResult.of(
              context
                  .insert(new BinaryOp(context.loc(unaryExpr), operand, operand, BinMode.XOR))
                  .getResult());
        }
        case BITWISE_COMPLEMENT -> {
          return EmitResult.of(
              context
                  .insert(new BinaryOp(context.loc(unaryExpr), operand, operand, BinMode.BXOR))
                  .getResult());
        }
        case POSTFIX_INCREMENT -> {
          // Copy the value to a new value and return that so that we can modify the increment
          // target.
          BuiltinOps.IdOp idOp =
              context.insert(new BuiltinOps.IdOp(context.loc(unaryExpr), operand));
          emitIncrement(unaryExpr, true, operand, context);
          return EmitResult.of(idOp.getResult());
        }
        case POSTFIX_DECREMENT -> {
          // Copy the value to a new value and return that so that we can modify the increment
          // target.
          BuiltinOps.IdOp idOp =
              context.insert(new BuiltinOps.IdOp(context.loc(unaryExpr), operand));
          emitIncrement(unaryExpr, false, operand, context);
          return EmitResult.of(idOp.getResult());
        }
      }
      return EmitResult.failure(
          context, unaryExpr, "Unary operator " + unaryExpr.getOperator() + " is not supported.");
    }

    private @NotNull Optional<Value> resolveName(
        @NotNull String name, @NotNull Node site, EmitContext context) {
      var valueOpt = context.lookupSymbol(name);
      if (valueOpt.isEmpty()) {
        context.emitError(site, "Variable " + name + " is not defined in the current scope.");
        return Optional.empty();
      }
      return valueOpt;
    }

    private void bindName(
        @NotNull String name,
        @NotNull Value value,
        @NotNull ResolvedType resolvedType,
        @NotNull Node site,
        EmitContext context) {
      context.putSymbol(name, value, resolvedType);
      value.setDebugInfo(new ValueDebugInfo(context.loc(site), name));
    }

    private @NotNull Optional<TypedAttribute> valueAttrFromLiteralExpr(
        @NotNull LiteralExpr literalExpr, @NotNull EmitContext context) {
      return Optional.ofNullable(
          switch (literalExpr) {
            case BooleanLiteralExpr boolL ->
                new IntegerAttribute(boolL.getValue() ? 1 : 0, IntegerT.BOOL);
            case CharLiteralExpr charL ->
                new IntegerAttribute(charL.getValue().charAt(0), IntegerT.UINT16);
            case DoubleLiteralExpr doubleL -> {
              if (doubleL.getValue().contains("f") || doubleL.getValue().contains("F"))
                yield new FloatAttribute(Float.parseFloat(doubleL.getValue()), FloatT.FLOAT32);
              else yield new FloatAttribute(Double.parseDouble(doubleL.getValue()), FloatT.FLOAT64);
            }
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
  }
}
