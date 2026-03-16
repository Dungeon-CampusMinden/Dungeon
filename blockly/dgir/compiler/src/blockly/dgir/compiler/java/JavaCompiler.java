package blockly.dgir.compiler.java;

import blockly.dgir.compiler.java.transformations.DeadCodeElimination;
import blockly.dgir.compiler.java.transformations.ImplicitCastElimination;
import blockly.dgir.compiler.java.transformations.LogicalBinaryToConditional;
import blockly.dgir.compiler.java.transformations.LoopLowering;
import blockly.dgir.dialect.dg.DgAttrs;
import blockly.dgir.dialect.dg.DgOps;
import blockly.dgir.dialect.dg.DungeonDialect;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.modules.ModuleDeclaration;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import dgir.core.Dialect;
import dgir.core.SymbolTable;
import dgir.core.debug.Location;
import dgir.core.debug.ValueDebugInfo;
import dgir.core.ir.Block;
import dgir.core.ir.Operation;
import dgir.core.ir.Type;
import dgir.core.ir.Value;
import dgir.core.serialization.Utils;
import dgir.dialect.arith.ArithAttrs;
import dgir.dialect.arith.ArithOps;
import dgir.dialect.arith.ArithOps.BinaryOp;
import dgir.dialect.builtin.BuiltinOps;
import dgir.dialect.cf.CfOps;
import dgir.dialect.func.FuncOps;
import dgir.dialect.io.IoOps;
import dgir.dialect.scf.ScfOps;
import dgir.dialect.str.StrOps;
import dgir.dialect.str.StrTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

import static blockly.dgir.compiler.java.Access.isDeclarationAccessibleFrom;
import static blockly.dgir.compiler.java.Access.isTypeUseAccessibleFrom;
import static blockly.dgir.compiler.java.CompilerUtils.*;
import static dgir.dialect.arith.ArithAttrs.BinModeAttr.BinMode;
import static dgir.dialect.arith.ArithOps.ConstantOp;
import static dgir.dialect.builtin.BuiltinAttrs.FloatAttribute;
import static dgir.dialect.builtin.BuiltinAttrs.IntegerAttribute;
import static dgir.dialect.builtin.BuiltinOps.ProgramOp;
import static dgir.dialect.builtin.BuiltinTypes.FloatT;
import static dgir.dialect.builtin.BuiltinTypes.IntegerT;
import static dgir.dialect.func.FuncOps.FuncOp;
import static dgir.dialect.func.FuncOps.ReturnOp;
import static dgir.dialect.func.FuncTypes.FuncType;

@SuppressWarnings({"unchecked"})
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

    EmitContext context = new EmitContext(filename);

    new DeadCodeElimination().visit(result, null);
    new LoopLowering().visit(result, null);
    Boolean castEliminationResult = new ImplicitCastElimination().visit(result, context);
    if (castEliminationResult != null) {
      context.printDiagnostics();
      return Optional.empty();
    }
    new LogicalBinaryToConditional().visit(result, context);
    new DeadCodeElimination().visit(result, null);

    // Register all dialects so that we can use them during emission.
    Dialect.registerAllDialects();
    DungeonDialect.get().register();

    JavaAstEmitter emitter = new JavaAstEmitter();
    EmitResult<Optional<Value>> emitResult = emitter.visit(result, context);
    context.printDiagnostics();
    if (emitResult.isFailure()) {
      return Optional.empty();
    }
    return Optional.ofNullable(context.program);
  }

  private static final class JavaAstEmitter
      extends GenericVisitorAdapter<@Nullable EmitResult<Optional<Value>>, @NotNull EmitContext> {
    private @NotNull EmitResult<Boolean> visitNodeList(
        @NotNull NodeList<?> members, @NotNull EmitContext context) {
      List<EmitResult<Optional<Value>>> results =
          members.stream()
              .collect(
                  ArrayList::new,
                  (emitResults, node) ->
                      emitResults.add(EmitResult.ofNullable(node.accept(this, context))),
                  List::addAll);
      List<Object> failedMembers = new ArrayList<>();
      for (int i = 0; i < members.size(); i++) {
        if (results.get(i).isFailure()) failedMembers.add(members.get(i));
      }
      return failedMembers.isEmpty() ? EmitResult.of(true) : EmitResult.failure();
    }

    private @NotNull EmitResult<List<Value>> visitNodeListWithResult(
        @NotNull NodeList<?> members, @NotNull EmitContext context) {
      List<EmitResult<Optional<Value>>> results =
          members.stream()
              .collect(
                  ArrayList::new,
                  (emitResults, node) ->
                      emitResults.add(EmitResult.ofNullable(node.accept(this, context))),
                  List::addAll);
      return results.stream()
              .anyMatch(
                  optionalEmitResult ->
                      optionalEmitResult.isFailure() || optionalEmitResult.get().isEmpty())
          ? EmitResult.failure()
          : EmitResult.of(
              results.stream()
                  .map(optionalEmitResult -> optionalEmitResult.get().orElseThrow())
                  .toList());
    }

    @Override
    public @NonNull EmitResult<Optional<Value>> visit(CompilationUnit n, EmitContext context) {
      ProgramOp program = new ProgramOp(context.loc(n));
      try (var programSymScope = new EmitContext.SymbolScope(context, true)) {
        context.setProgramBlock(program.getEntryBlock());

        try (var programInsertion = context.setInsertionPoint(program.getEntryBlock(), -1)) {
          {
            EmitResult<Boolean> result = visitNodeList(n.getImports(), context);
            if (result.isFailure()) return EmitResult.failure(context, n, "Failed to emit imports");
          }
          if (n.getModule().isPresent()) {
            EmitResult<Optional<Value>> result =
                EmitResult.ofNullable(n.getModule().get().accept(this, context));
            if (result.isFailure()) return result;
          }
          if (n.getPackageDeclaration().isPresent()) {
            EmitResult<Optional<Value>> result =
                EmitResult.ofNullable(n.getPackageDeclaration().get().accept(this, context));
            if (result.isFailure()) return result;
          }
          {
            EmitResult<Boolean> result = visitNodeList(n.getTypes(), context);
            if (result.isFailure()) return EmitResult.failure();
          }
          if (context.compilationSuccessful()) {
            context.program = program;
          } else {
            String incompleteProgram = Utils.getMapper(true).writeValueAsString(program);
            context.emitError(n, "Incorrect program", incompleteProgram);
          }
        }
        return context.compilationSuccessful()
            ? EmitResult.success(Optional.empty())
            : EmitResult.failure();
      }
    }

    @Override
    public EmitResult<Optional<Value>> visit(ImportDeclaration n, EmitContext context) {
      return switch (n.getNameAsString()) {
        case "Dungeon.Hero", "Dungeon.IO" -> EmitResult.success(Optional.empty());
        default ->
            EmitResult.failure(
                context, n, "Import of " + n.getNameAsString() + " is not supported.");
      };
    }

    @Override
    public EmitResult<Optional<Value>> visit(ModuleDeclaration n, EmitContext context) {
      return EmitResult.failure(context, n, "Modules are not supported.");
    }

    @Override
    public EmitResult<Optional<Value>> visit(PackageDeclaration n, EmitContext context) {
      return EmitResult.success(Optional.empty());
    }

    @Override
    public @NotNull EmitResult<Optional<Value>> visit(
        ClassOrInterfaceDeclaration n, EmitContext context) {
      {
        if (n.isInterface() || n.isRecordDeclaration() || n.isAnnotationDeclaration()) {
          return EmitResult.failure(
              context,
              n,
              "Class "
                  + n.getName()
                  + " is an interface. Interfaces are not supported. Type: "
                  + (n.isInterface() ? "interface" : "record"));
        }
        if (n.isEnumDeclaration()) {
          return EmitResult.failure(
              context, n, "Class " + n.getName() + " is an enum. Enums are not supported.");
        }
        if (!n.isStatic() && n.findAncestor(ClassOrInterfaceDeclaration.class).isPresent()) {
          return EmitResult.failure(
              context,
              n,
              "Class "
                  + n.getName()
                  + " is a non-static inner class. Inner classes must be static.");
        }
      }
      {
        if (n.getExtendedTypes().isNonEmpty())
          return EmitResult.failure(
              context,
              n,
              "Class "
                  + n.getName()
                  + " extends a class. Extending is not supported. Extended types: "
                  + n.getExtendedTypes());
      }
      {
        if (n.getImplementedTypes().isNonEmpty())
          return EmitResult.failure(
              context,
              n,
              "Class "
                  + n.getName()
                  + " implements an interface. Implementing is not supported. Implemented types: "
                  + n.getImplementedTypes());
      }
      {
        if (n.getTypeParameters().isNonEmpty())
          return EmitResult.failure(
              context,
              n,
              "Class "
                  + n.getName()
                  + " has type parameters. Generics classes are not supported. Type parameters: "
                  + n.getTypeParameters());
      }
      {
        if (n.getTypeParameters().isNonEmpty())
          return EmitResult.failure(
              context,
              n,
              "Class "
                  + n.getName()
                  + " has type parameters. Generics classes are not supported. Type parameters: "
                  + n.getTypeParameters());
      }

      {
        if (n.getAnnotations().isNonEmpty())
          context.emitWarning(
              n,
              "Class "
                  + n.getName()
                  + " has annotations. Annotations are not supported and will be ignored. Annotations: "
                  + n.getAnnotations());
      }

      {
        EmitResult<Boolean> result = visitNodeList(n.getMembers(), context);
        if (result.isFailure()) return EmitResult.failure();
      }

      return EmitResult.success(Optional.empty());
    }

    @Override
    public EmitResult<Optional<Value>> visit(NormalAnnotationExpr n, EmitContext context) {
      context.emitWarning(
          n, "Annotation " + n.getNameAsString() + " is not supported and will be ignored.");
      return EmitResult.success(Optional.empty());
    }

    @Override
    public EmitResult<Optional<Value>> visit(SingleMemberAnnotationExpr n, EmitContext context) {
      context.emitWarning(
          n, "Annotation " + n.getNameAsString() + " is not supported and will be ignored.");
      return EmitResult.success(Optional.empty());
    }

    @Override
    public EmitResult<Optional<Value>> visit(MarkerAnnotationExpr n, EmitContext context) {
      context.emitWarning(
          n, "Annotation " + n.getNameAsString() + " is not supported and will be ignored.");
      return EmitResult.success(Optional.empty());
    }

    @Override
    public EmitResult<Optional<Value>> visit(MethodDeclaration n, EmitContext context) {
      {
        for (Modifier modifier : n.getModifiers()) {
          switch (modifier.getKeyword()) {
            case PUBLIC, PROTECTED, PRIVATE, STATIC -> {}
            default -> {
              return EmitResult.failure(
                  context,
                  n,
                  "Method "
                      + n.getName()
                      + " has modifier "
                      + modifier
                      + ". Modifier "
                      + modifier
                      + " is not supported. Supported modifiers are: public, protected, private, static.");
            }
          }
        }
      }

      if (n.getReceiverParameter().isPresent()) {
        return EmitResult.failure(
            context,
            n,
            "Method "
                + n.getName()
                + " has a receiver parameter. Receiver parameters are not supported. Receiver parameter: "
                + n.getReceiverParameter().get());
      }

      {
        if (n.getThrownExceptions().isNonEmpty())
          return EmitResult.failure(
              context,
              n,
              "Method "
                  + n.getName()
                  + " declares thrown exceptions. Throwing exceptions is not supported. Thrown exceptions: "
                  + n.getThrownExceptions());
      }

      {
        if (n.getAnnotations().isNonEmpty()) {
          context.emitWarning(
              n,
              "Method "
                  + n.getName()
                  + " has annotations. Annotations are not supported and will be ignored. Annotations: "
                  + n.getAnnotations());
        }
      }

      List<ParameterInfo> parameterInfos;
      {
        parameterInfos =
            new ArrayList<>(
                n.getParameters().stream()
                    .map(parameter -> resolveParameter(parameter, context).orElse(null))
                    .toList());
        if (parameterInfos.stream().anyMatch(Objects::isNull)) {
          return EmitResult.failure();
        }
      }

      Type returnType = null;
      if (!n.getType().isVoidType()) {
        Optional<TypeInfo> resolvedType = resolveType(n.getType(), context);
        if (resolvedType.isEmpty()) {
          return EmitResult.failure();
        }
        returnType = resolvedType.get().type();
      }

      Optional<ResolvedMethodDeclaration> resolvedN = resolve(n, context);
      if (resolvedN.isEmpty()) {
        return EmitResult.failure();
      }

      try (var methodInsertion =
          context.setInsertionPoint(context.getProgramBlock().orElseThrow(), -1)) {

        // Create the function op.
        String fullyQualifiedMethodName =
            "main".equals(n.getNameAsString()) ? "main" : resolvedN.get().getQualifiedSignature();

        FuncOp funcOp =
            context.insert(
                new FuncOp(
                    context.loc(n),
                    fullyQualifiedMethodName,
                    FuncType.of(
                        parameterInfos.stream().map(ParameterInfo::type).toList(), returnType)));

        // Emit all statements in the method body. These will insert themselves into the function
        // op. Also, put the function arguments in the symbol table so that they can be referenced
        // in the body.
        try (var funcBodyInsertion = context.setInsertionPoint(funcOp.getEntryBlock(), -1);
            var funcBodySymScope = new EmitContext.SymbolScope(context, true)) {
          for (int i = 0; i < parameterInfos.size(); i++) {
            context.putSymbol(parameterInfos.get(i).name, funcOp.getArgument(i).orElseThrow());
          }

          EmitResult<Optional<Value>> result;
          if (n.getBody().isPresent()) {
            result = EmitResult.ofNullable(n.getBody().get().accept(this, context));
          } else {
            return EmitResult.failure(
                context,
                n,
                "Method "
                    + n.getNameAsString()
                    + " has no body. Abstract methods are not supported.");
          }

          // Make sure we have an implicit return in case the method has a void return type and the
          // last statement is not a return statement.
          funcOp.addImplicitTerminators();

          if (result.isFailure()) return result;
        }
      }

      return EmitResult.success(Optional.empty());
    }

    @Override
    public EmitResult<Optional<Value>> visit(AssertStmt n, EmitContext context) {
      EmitResult<Optional<Value>> checkResult;
      {
        checkResult = EmitResult.ofNullable(n.getCheck().accept(this, context));
        if (checkResult.isFailure() || checkResult.get().isEmpty()) return checkResult;
      }

      EmitResult<Optional<Value>> messageResult = null;
      if (n.getMessage().isPresent()) {
        messageResult = EmitResult.ofNullable(n.getMessage().get().accept(this, context));
        if (messageResult.isFailure() || messageResult.get().isEmpty()) return messageResult;
      }

      if (messageResult != null) {
        context.insert(
            new CfOps.AssertOp(context.loc(n), checkResult.get().get(), messageResult.get().get()));
      } else {
        context.insert(new CfOps.AssertOp(context.loc(n), checkResult.get().get()));
      }
      return EmitResult.success(Optional.empty());
    }

    @Override
    public EmitResult<Optional<Value>> visit(BlockStmt n, EmitContext context) {
      EmitResult<Boolean> result;
      {
        result = visitNodeList(n.getStatements(), context);
        if (result.isFailure()) return EmitResult.failure();
      }
      return EmitResult.success(Optional.empty());
    }

    @Override
    public EmitResult<Optional<Value>> visit(BreakStmt n, EmitContext context) {
      if (n.getParentNode().isEmpty() || n.getParentNode().get().getParentNode().isEmpty()) {
        return EmitResult.failure(context, n, "Break statement has no parent node.");
      }

      Optional<Operation> ancestorLoopOpt =
          context.findAncestor(ScfOps.WhileOp.class, ScfOps.ForOp.class);
      if (ancestorLoopOpt.isEmpty()) {
        return EmitResult.failure(context, n, "Break statements are not supported outside loops.");
      }
      Operation ancestorLoop = ancestorLoopOpt.get();

      {
        var breakTrue = context.insert(new ConstantOp(context.loc(n), true));
        var skipTrue = context.insert(new ConstantOp(context.loc(n), true));
        // If we are inside a loop, set the break value to true.
        if (ancestorLoop.isa(ScfOps.ForOp.class)) {
          breakTrue.setOutputValue(
              ancestorLoop.getFirstRegionOrThrow().getBodyValue(1).orElseThrow());
          skipTrue.setOutputValue(
              ancestorLoop.getFirstRegionOrThrow().getBodyValue(2).orElseThrow());
        } else {
          breakTrue.setOutputValue(ancestorLoop.getRegionOrThrow(1).getBodyValue(0).orElseThrow());
          skipTrue.setOutputValue(ancestorLoop.getRegionOrThrow(1).getBodyValue(1).orElseThrow());
        }
      }

      return EmitResult.success(Optional.empty());
    }

    @Override
    public EmitResult<Optional<Value>> visit(ContinueStmt n, EmitContext context) {
      context.insert(new ScfOps.ContinueOp(context.loc(n)));
      return EmitResult.success(Optional.empty());
    }

    @Override
    public EmitResult<Optional<Value>> visit(DoStmt n, EmitContext context) {
      // Same as while loop but body is placed in the condition region before the condition check
      ScfOps.WhileOp whileOp = context.insert(new ScfOps.WhileOp(context.loc(n)));
      {
        try (var conditionRegionSymScope = new EmitContext.SymbolScope(context, false)) {
          Block conditionBlock = whileOp.getConditionRegion().addBlock(new Block());
          Block continueBlock = whileOp.getConditionRegion().addBlock(new Block());
          continueBlock.addOperation(new ScfOps.ContinueOp(context.loc(n.getCondition())));
          Block breakBlock = whileOp.getConditionRegion().addBlock(new Block());
          breakBlock.addOperation(new ScfOps.EndOp(context.loc(n.getCondition())));

          // Open the new scope and place the comparison expression in it.
          try (var bodyInsertion =
              context.setInsertionPoint(whileOp.getConditionRegion().getEntryBlock(), -1)) {

            EmitResult<Optional<Value>> bodyResult;
            {
              bodyResult = EmitResult.ofNullable(n.getBody().accept(this, context));
              if (bodyResult.isFailure()) return bodyResult;
            }

            // If there was a call to break or continue the skip and break flags are added
            // we need to jump to the correct block
            // If not jump to the regular condition check
            if (containsLocalFlag(n.getBody(), "skipBreak")) {
              // Create the branch to break or continue
              // The second operation is the constant op defining the skip-break flag
              context.insert(
                  new CfOps.BranchCondOp(
                      context.loc(n),
                      whileOp
                          .getConditionRegion()
                          .getEntryBlock()
                          .getOperations()
                          .get(1)
                          .getOutputValueOrThrow(),
                      breakBlock,
                      conditionBlock));
            } else {
              context.insert(new CfOps.BranchOp(context.loc(n), conditionBlock));
            }
          }
          try (var conditionInsertion = context.setInsertionPoint(conditionBlock, -1)) {
            EmitResult<Optional<Value>> conditionResult;
            {
              conditionResult = EmitResult.ofNullable(n.getCondition().accept(this, context));
              if (conditionResult.isFailure() || conditionResult.get().isEmpty())
                return conditionResult;
              Value compareValue = conditionResult.get().get();
              context.insert(
                  new CfOps.BranchCondOp(
                      context.loc(n.getCondition()), compareValue, continueBlock, breakBlock));
            }
          }
        }

        try (var continueInsertion =
                context.setInsertionPoint(whileOp.getBodyRegion().getEntryBlock(), -1);
            var continueSymScope = new EmitContext.SymbolScope(context, false)) {
          // Just add an implicit continue at the end of the loop body to jump back to the
          // condition.
          context.insert(new ScfOps.ContinueOp(context.loc(n)));
        }
      }
      whileOp.addImplicitTerminators();
      return EmitResult.success(Optional.empty());
    }

    @Override
    public EmitResult<Optional<Value>> visit(EmptyStmt n, EmitContext context) {
      return EmitResult.success(Optional.empty());
    }

    @Override
    public EmitResult<Optional<Value>> visit(
        ExplicitConstructorInvocationStmt n, EmitContext context) {
      return EmitResult.failure(
          context, n, "Explicit constructor invocation statements are not supported.");
    }

    @Override
    public EmitResult<Optional<Value>> visit(ExpressionStmt n, EmitContext context) {
      var result = EmitResult.ofNullable(n.getExpression().accept(this, context));
      if (result.isFailure()) return result;
      return EmitResult.success(Optional.empty());
    }

    @Override
    public EmitResult<Optional<Value>> visit(ForEachStmt n, EmitContext context) {
      return EmitResult.failure(context, n, "For each statements are not supported.");
    }

    @Override
    public EmitResult<Optional<Value>> visit(ForStmt n, EmitContext context) {
      // First emit the initialization outside the for loop.
      {
        EmitResult<Boolean> initResult;
        {
          initResult = visitNodeList(n.getInitialization(), context);
          if (initResult.isFailure()) return EmitResult.failure();
        }
      }

      // We are using a while op so that we can support more complex update expressions.
      ScfOps.WhileOp whileOp = context.insert(new ScfOps.WhileOp(context.loc(n)));
      // Open the new scope and place the comparison expression in it.
      try (var conditionSymScope = new EmitContext.SymbolScope(context, false)) {
        try (var conditionInsertion =
            context.setInsertionPoint(whileOp.getConditionRegion().getEntryBlock(), -1)) {
          Block conditionContinueBlock = whileOp.getConditionRegion().addBlock(new Block());
          conditionContinueBlock.addOperation(new ScfOps.ContinueOp(context.loc(n)));
          Block conditionBreakBlock = whileOp.getConditionRegion().addBlock(new Block());
          conditionBreakBlock.addOperation(new ScfOps.EndOp(context.loc(n)));

          if (n.getCompare().isPresent()) {
            EmitResult<Optional<Value>> compareResult =
                EmitResult.ofNullable(n.getCompare().get().accept(this, context));
            if (compareResult.isFailure() || compareResult.get().isEmpty()) {
              return compareResult;
            }
            Value compareValue = compareResult.get().get();
            context.insert(
                new CfOps.BranchCondOp(
                    context.loc(n.getCompare().get()),
                    compareValue,
                    conditionContinueBlock,
                    conditionBreakBlock));
          }
        }
      }

      // Open a new scope and place the body and update expressions inside it.
      try (var bodySymbolScope = new EmitContext.SymbolScope(context, false)) {
        // Create the body block.
        try (var bodyInsertion =
            context.setInsertionPoint(whileOp.getBodyRegion().getEntryBlock(), -1)) {
          EmitResult<Optional<Value>> bodyResult =
              EmitResult.ofNullable(n.getBody().accept(this, context));
          if (bodyResult.isFailure()) {
            return bodyResult;
          }

          if (n.getBody().isBlockStmt()) {
            BlockStmt blockStmt = n.getBody().asBlockStmt();
            // If there was a call to break or continue the skip and break flags are added
            // In case they exists we need to create a block for the update and one for the
            // terminate condition
            // Otherwise, just emit the update result.
            if (containsLocalFlag(blockStmt, "skip")) {
              // Create the update block. This is only called if there are no break statements in
              // the loop
              // body.
              Block updateBlock = whileOp.getBodyRegion().addBlock(new Block());
              try (var updateInsertion = context.setInsertionPoint(updateBlock, -1)) {
                EmitResult<Boolean> updateResult = visitNodeList(n.getUpdate(), context);
                if (updateResult.isFailure()) {
                  return EmitResult.failure();
                }
              }

              // Create the break block. This is called if there was a break statement in the loop
              // body.
              Block breakBlock = whileOp.getBodyRegion().addBlock(new Block());
              breakBlock.addOperation(new ScfOps.EndOp(context.loc(n)));

              // Create the branch to break or continue
              // The second operation is the constant op defining the skip flag
              context.insert(
                  new CfOps.BranchCondOp(
                      context.loc(n),
                      whileOp
                          .getBodyRegion()
                          .getEntryBlock()
                          .getOperations()
                          .get(1)
                          .getOutputValueOrThrow(),
                      breakBlock,
                      updateBlock));
            } else {
              EmitResult<Boolean> updateResult = visitNodeList(n.getUpdate(), context);
              if (updateResult.isFailure()) {
                return EmitResult.failure();
              }
            }
          } else {
            return EmitResult.failure(context, n, "Failed to emit body of for loop");
          }
        }
      }
      whileOp.addImplicitTerminators();
      return EmitResult.success(Optional.empty());
    }

    @Override
    public EmitResult<Optional<Value>> visit(IfStmt n, EmitContext context) {
      EmitResult<Optional<Value>> conditionResult;
      {
        conditionResult = EmitResult.ofNullable(n.getCondition().accept(this, context));
        if (conditionResult.isFailure()) return conditionResult;
      }
      var ifOp =
          context.insert(
              new ScfOps.IfOp(
                  context.loc(n), conditionResult.get().orElseThrow(), n.hasElseBlock()));

      try (var thenInsertion =
          context.setInsertionPoint(ifOp.getThenRegion().getEntryBlock(), -1)) {
        EmitResult<Optional<Value>> thenResult;
        {
          thenResult = EmitResult.ofNullable(n.getThenStmt().accept(this, context));
          if (thenResult.isFailure()) return thenResult;
        }
      }

      if (n.hasElseBlock())
        try (var elseInsertion =
            context.setInsertionPoint(ifOp.getElseRegion().orElseThrow().getEntryBlock(), -1)) {
          EmitResult<Optional<Value>> elseResult = null;
          if (n.getElseStmt().isPresent()) {
            elseResult = EmitResult.ofNullable(n.getElseStmt().get().accept(this, context));
            if (elseResult.isFailure()) return elseResult;
          }
        }

      ifOp.addImplicitTerminators();
      return EmitResult.success(Optional.empty());
    }

    @Override
    public EmitResult<Optional<Value>> visit(LabeledStmt n, EmitContext context) {
      return EmitResult.failure(context, n, "Labeled statements are not supported.");
    }

    @Override
    public EmitResult<Optional<Value>> visit(LocalClassDeclarationStmt n, EmitContext context) {
      return EmitResult.failure(
          context, n, "Local class declaration statements are not supported.");
    }

    @Override
    public EmitResult<Optional<Value>> visit(LocalRecordDeclarationStmt n, EmitContext context) {
      return EmitResult.failure(
          context, n, "Local record declaration statements are not supported.");
    }

    @Override
    public EmitResult<Optional<Value>> visit(ReturnStmt n, EmitContext context) {
      Location trueLocation = context.loc(n);
      // Move the debug location one further down than the actual return statement, so we can step
      // to the closing curly bracket of functions and inspect the values produced.
      Location debugLocation =
          new Location(trueLocation.file(), trueLocation.line() + 1, trueLocation.column());
      if (n.getExpression().isPresent()) {
        EmitResult<Optional<Value>> exprRes =
            EmitResult.ofNullable(n.getExpression().get().accept(this, context));
        if (exprRes.isFailure() || exprRes.get().isEmpty()) {
          return EmitResult.failure(
              context, n, "Failed to emit return expression of return statement");
        }
        context.insert(new ReturnOp(debugLocation, exprRes.get().get()));
      } else {
        context.insert(new ReturnOp(debugLocation));
      }
      return EmitResult.success(Optional.empty());
    }

    @Override
    public EmitResult<Optional<Value>> visit(SwitchStmt n, EmitContext context) {
      return EmitResult.failure(context, n, "Switch statements are not supported.");
    }

    @Override
    public EmitResult<Optional<Value>> visit(SynchronizedStmt n, EmitContext context) {
      return EmitResult.failure(context, n, "Synchronized statements are not supported.");
    }

    @Override
    public EmitResult<Optional<Value>> visit(ThrowStmt n, EmitContext context) {
      return EmitResult.failure(context, n, "Throw statements are not supported.");
    }

    @Override
    public EmitResult<Optional<Value>> visit(TryStmt n, EmitContext context) {
      return EmitResult.failure(context, n, "Try statements are not supported.");
    }

    @Override
    public EmitResult<Optional<Value>> visit(UnparsableStmt n, EmitContext context) {
      return EmitResult.failure(context, n, "Why are u gey?");
    }

    @Override
    public EmitResult<Optional<Value>> visit(WhileStmt n, EmitContext context) {
      ScfOps.WhileOp whileOp = context.insert(new ScfOps.WhileOp(context.loc(n)));
      {
        // Open the new scope and place the comparison expression in it.
        try (var conditionInsertion =
                context.setInsertionPoint(whileOp.getConditionRegion().getEntryBlock(), -1);
            var conditionSymScope = new EmitContext.SymbolScope(context, false)) {
          Block continueBlock = whileOp.getConditionRegion().addBlock(new Block());
          continueBlock.addOperation(new ScfOps.ContinueOp(context.loc(n.getCondition())));
          Block breakBlock = whileOp.getConditionRegion().addBlock(new Block());
          breakBlock.addOperation(new ScfOps.EndOp(context.loc(n.getCondition())));

          EmitResult<Optional<Value>> conditionResult;
          {
            conditionResult = EmitResult.ofNullable(n.getCondition().accept(this, context));
            if (conditionResult.isFailure() || conditionResult.get().isEmpty())
              return conditionResult;
            Value compareValue = conditionResult.get().get();
            context.insert(
                new CfOps.BranchCondOp(
                    context.loc(n.getCondition()), compareValue, continueBlock, breakBlock));
          }
        }

        try (var bodyInsertion =
                context.setInsertionPoint(whileOp.getBodyRegion().getEntryBlock(), -1);
            var conditionSymScope = new EmitContext.SymbolScope(context, false)) {
          EmitResult<Optional<Value>> bodyResult;
          {
            bodyResult = EmitResult.ofNullable(n.getBody().accept(this, context));
            if (bodyResult.isFailure()) return bodyResult;
          }

          // If there was a call to break or continue the skip and break flags are added
          // In case they exists we need to create a block for the update and one for the
          // terminate condition
          // Otherwise, just emit the update result.
          if (containsLocalFlag(n.getBody(), "skipBreak")) {
            // Create the continue block. This is only called if there are no break statements
            // where hit.
            Block continueBlock = whileOp.getBodyRegion().addBlock(new Block());
            continueBlock.addOperation(new ScfOps.ContinueOp(context.loc(n)));
            // Create the break block. This is called if there was a break statement in the loop
            // body.
            Block breakBlock = whileOp.getBodyRegion().addBlock(new Block());
            breakBlock.addOperation(new ScfOps.EndOp(context.loc(n)));

            // Create the branch to break or continue
            // The second operation is the constant op defining the skip flag
            context.insert(
                new CfOps.BranchCondOp(
                    context.loc(n),
                    whileOp
                        .getBodyRegion()
                        .getEntryBlock()
                        .getOperations()
                        .get(1)
                        .getOutputValueOrThrow(),
                    breakBlock,
                    continueBlock));
          } else {
            return EmitResult.failure(context, n, "Failed to emit body of for loop");
          }
        }
      }
      whileOp.addImplicitTerminators();
      return EmitResult.success(Optional.empty());
    }

    @Override
    public EmitResult<Optional<Value>> visit(AssignExpr n, EmitContext context) {
      EmitResult<Optional<Value>> targetRes;
      EmitResult<Optional<Value>> valueRes;
      {
        targetRes = EmitResult.ofNullable(n.getTarget().accept(this, context));
        if (targetRes.isFailure() || targetRes.get().isEmpty()) return targetRes;
      }
      {
        valueRes = EmitResult.ofNullable(n.getValue().accept(this, context));
        if (valueRes.isFailure() || valueRes.get().isEmpty()) return valueRes;
      }

      try {
        var id =
            context.insert(
                new BuiltinOps.IdOp(context.loc(n), valueRes.get().get(), targetRes.get().get()));
        return EmitResult.of(Optional.of(id.getResult()));
      } catch (AssertionError e) {
        return EmitResult.failure(
            context,
            n,
            "Failed to emit assignment of value " + n.getValue() + " to " + n.getTarget(),
            e);
      }
    }

    @Override
    public EmitResult<Optional<Value>> visit(BinaryExpr n, EmitContext context) {
      EmitResult<Optional<Value>> lhsResult;
      {
        lhsResult = EmitResult.ofNullable(n.getLeft().accept(this, context));
        if (lhsResult.isFailure() || lhsResult.get().isEmpty()) return lhsResult;
      }
      EmitResult<Optional<Value>> rhsResult;
      {
        rhsResult = EmitResult.ofNullable(n.getRight().accept(this, context));
        if (rhsResult.isFailure() || rhsResult.get().isEmpty()) return rhsResult;
      }
      Value lhs = lhsResult.get().get();
      Value rhs = rhsResult.get().get();

      if (lhs.getType().equals(StrTypes.StringT.INSTANCE)
          || rhs.getType().equals(StrTypes.StringT.INSTANCE)) {
        if (n.getOperator() != BinaryExpr.Operator.PLUS) {
          context.emitError(n, "Only string concatenation is supported for strings.");
          return EmitResult.failure();
        }
        var concatOp = context.insert(new StrOps.ConcatOp(context.loc(n), lhs, rhs));
        return EmitResult.of(Optional.of(concatOp.getResult()));
      } else {
        Optional<BinMode> binModeOpt =
            Optional.of(
                switch (n.getOperator()) {
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
                });

        var binOp = context.insert(new BinaryOp(context.loc(n), lhs, rhs, binModeOpt.get()));
        return EmitResult.of(Optional.of(binOp.getResult()));
      }
    }

    @Override
    public EmitResult<Optional<Value>> visit(BooleanLiteralExpr n, EmitContext context) {
      return EmitResult.of(
          Optional.of(context.insert(new ConstantOp(context.loc(n), n.getValue())).getResult()));
    }

    @Override
    public EmitResult<Optional<Value>> visit(CastExpr n, EmitContext context) {
      EmitResult<Optional<Value>> expressionResult;
      {
        expressionResult = EmitResult.ofNullable(n.getExpression().accept(this, context));
        if (expressionResult.isFailure() || expressionResult.get().isEmpty())
          return EmitResult.failure(
              context, n.getExpression(), "Failed to emit expression of cast");
      }

      TypeInfo typeInfo;
      {
        var resolvedTypeInfo = resolveType(n.getType(), context);
        if (resolvedTypeInfo.isEmpty()) {
          return EmitResult.failure(
              context,
              n,
              "Failed to emit cast of type "
                  + expressionResult.get().get().getType()
                  + " to "
                  + n.getType());
        }
        typeInfo = resolvedTypeInfo.get();
      }

      Value value = expressionResult.get().get();
      Type castType = typeInfo.type();
      return EmitResult.of(
          Optional.of(
              context.insert(new ArithOps.CastOp(context.loc(n), value, castType)).getResult()));
    }

    @Override
    public EmitResult<Optional<Value>> visit(CharLiteralExpr n, EmitContext context) {
      return EmitResult.of(
          Optional.of(
              context
                  .insert(
                      new ConstantOp(
                          context.loc(n), new IntegerAttribute(n.asChar(), IntegerT.UINT16)))
                  .getResult()));
    }

    @Override
    public EmitResult<Optional<Value>> visit(ConditionalExpr n, EmitContext context) {
      EmitResult<Optional<Value>> conditionRes =
          EmitResult.ofNullable(n.getCondition().accept(this, context));
      if (conditionRes.isFailure() || conditionRes.get().isEmpty()) return conditionRes;

      ResolvedType resolvedOutputType;
      try {
        resolvedOutputType = n.calculateResolvedType();
      } catch (Exception e) {
        return EmitResult.failure(
            context, n, "Failed to resolve type of conditional expression", e);
      }
      Optional<Type> outputTypeOpt = fromAstType(resolvedOutputType, n, context);
      if (outputTypeOpt.isEmpty()) {
        return EmitResult.failure(context, n, "Failed to resolve type of conditional expression");
      }

      var ifOp =
          context.insert(
              new ScfOps.IfOp(context.loc(n), conditionRes.get().get(), true, outputTypeOpt.get()));
      try (var thenInsertion =
          context.setInsertionPoint(ifOp.getThenRegion().getEntryBlock(), -1)) {
        EmitResult<Optional<Value>> thenRes =
            EmitResult.ofNullable(n.getThenExpr().accept(this, context));
        if (thenRes.isFailure() || thenRes.get().isEmpty()) return thenRes;
        context.insert(new ScfOps.YieldOp(context.loc(n), thenRes.get().get()));
      }

      try (var elseInsertion =
          context.setInsertionPoint(ifOp.getElseRegion().orElseThrow().getEntryBlock(), -1)) {
        EmitResult<Optional<Value>> elseRes =
            EmitResult.ofNullable(n.getElseExpr().accept(this, context));
        if (elseRes.isFailure() || elseRes.get().isEmpty()) return elseRes;
        context.insert(new ScfOps.YieldOp(context.loc(n), elseRes.get().get()));
      }

      return EmitResult.success(Optional.of(ifOp.getOutputValue().orElseThrow()));
    }

    @Override
    public EmitResult<Optional<Value>> visit(DoubleLiteralExpr n, EmitContext context) {
      boolean isFloat = n.getValue().toLowerCase(Locale.ROOT).endsWith("f");
      return EmitResult.of(
          Optional.of(
              context
                  .insert(
                      new ConstantOp(
                          context.loc(n),
                          new FloatAttribute(
                              n.asDouble(), isFloat ? FloatT.FLOAT32 : FloatT.FLOAT64)))
                  .getResult()));
    }

    @Override
    public EmitResult<Optional<Value>> visit(EnclosedExpr n, EmitContext context) {
      return EmitResult.ofNullable(n.getInner().accept(this, context));
    }

    @Override
    public EmitResult<Optional<Value>> visit(FieldAccessExpr n, EmitContext context) {
      return EmitResult.failure(context, n, "Field access is not supported.");
    }

    @Override
    public EmitResult<Optional<Value>> visit(IntegerLiteralExpr n, EmitContext context) {
      return EmitResult.of(
          Optional.of(
              context
                  .insert(
                      new ConstantOp(
                          context.loc(n),
                          new IntegerAttribute(n.asNumber().longValue(), IntegerT.INT32)))
                  .getResult()));
    }

    @Override
    public EmitResult<Optional<Value>> visit(LongLiteralExpr n, EmitContext context) {
      return EmitResult.of(
          Optional.of(
              context
                  .insert(
                      new ConstantOp(
                          context.loc(n),
                          new IntegerAttribute(n.asNumber().longValue(), IntegerT.INT64)))
                  .getResult()));
    }

    @Override
    public EmitResult<Optional<Value>> visit(MethodCallExpr n, EmitContext context) {
      if (n.getTypeArguments().isPresent()) {
        return EmitResult.failure(
            context,
            n,
            "Method call "
                + n.getName()
                + " has type arguments. Method calls with type arguments are not supported. Type arguments: "
                + n.getTypeArguments().get());
      }

      Optional<ResolvedMethodDeclaration> targetMethodOpt = resolve(n, context);
      if (targetMethodOpt.isEmpty()) {
        return EmitResult.failure(context, n, "Failed to resolve method call " + n.getName());
      }
      ResolvedMethodDeclaration targetMethod = targetMethodOpt.get();
      // If at any point we want to use the string just emit all the methods upfront.
      if (targetMethod.declaringType().getQualifiedName().equals("java.lang.String")) {
        emitStringIntrinsicMethods(targetMethod.declaringType(), context);
      }

      ResolvedReferenceTypeDeclaration callingClass;
      {
        // Make sure the target method is accessible from the current context. This also checks that
        // the method is
        var callingClassOpt = n.findAncestor(ClassOrInterfaceDeclaration.class);
        if (callingClassOpt.isEmpty()) {
          return EmitResult.failure(
              context,
              n,
              "Method call "
                  + n.getName()
                  + " is not in a class or interface. Method calls must be in a class or interface.");
        }
        var resolvedCallingClassOpt = resolve(callingClassOpt.get(), context);
        if (resolvedCallingClassOpt.isEmpty()) {
          return EmitResult.failure(
              context,
              n,
              "Failed to resolve class or interface "
                  + callingClassOpt.get().getNameAsString()
                  + " of method call "
                  + n.getName());
        }
        callingClass = resolvedCallingClassOpt.get();
        if (!isDeclarationAccessibleFrom(callingClass, targetMethod)) {
          return EmitResult.failure(
              context,
              n,
              "Method callee "
                  + targetMethod.getQualifiedName()
                  + " is not visible from "
                  + callingClass.getQualifiedName());
        }
      }

      EmitResult<Optional<Value>> scopeResult = null;
      if (n.getScope().isPresent() && !targetMethod.isStatic()) {
        scopeResult = EmitResult.ofNullable(n.getScope().get().accept(this, context));
        if (scopeResult.isFailure())
          return EmitResult.failure(
              context, n, "Failed to emit scope of method call " + n.getName());
      }

      List<Value> args;
      {
        EmitResult<List<Value>> argumentsResult;
        argumentsResult = visitNodeListWithResult(n.getArguments(), context);
        if (argumentsResult.isFailure())
          return EmitResult.failure(
              context, n, "Failed to emit arguments of method call " + n.getName());

        args = new ArrayList<>(argumentsResult.get());

        // Check if the caller arguments with the callee param types and emit casts if necessary
        int varargsIndex = -1;
        for (int i = 0; i < args.size(); i++) {
          Value callArg = args.get(i);
          if (varargsIndex == -1) {
            if (targetMethod.getParam(i).isVariadic()) {
              varargsIndex = i;
            }
          }
          args.set(i, callArg);
        }
      }

      if (!targetMethod.isStatic()) {
        if (scopeResult == null)
          return EmitResult.failure(
              context,
              n,
              "Method call "
                  + n.getName()
                  + " is an instance method call but has no scope. Instance method calls must have a scope.");
        args.addFirst(scopeResult.get().orElseThrow());
      }

      if (IntrinsicRegistry.signatureToOpCode.containsKey(targetMethod.getQualifiedSignature())) {
        context.emitInfo(n, "Intrinsic: " + targetMethod.getQualifiedSignature());
        return emitIntrinsic(n, targetMethod.getQualifiedSignature(), args, context);
      }

      String funcName = targetMethod.getQualifiedSignature();
      Optional<Type> returnType = Optional.empty();
      if (!targetMethod.getReturnType().isVoid()) {
        returnType = fromAstType(targetMethod.getReturnType(), n, context);
        if (returnType.isEmpty()) {
          return EmitResult.failure(
              context, n, "Failed to resolve return type of method " + n.getNameAsString());
        }
      }
      FuncOps.CallOp callOp =
          context.insert(
              new FuncOps.CallOp(context.loc(n), funcName, args, returnType.orElse(null)));

      return EmitResult.success(callOp.getOutputValue());
    }

    @Override
    public EmitResult<Optional<Value>> visit(NameExpr n, EmitContext context) {
      Optional<Value> resolved = resolveName(n.getName().asString(), n, context);
      if (resolved.isEmpty()) {
        return EmitResult.failure(context, n, "Failed to resolve name " + n.getName());
      }
      return EmitResult.of(resolved);
    }

    @Override
    public EmitResult<Optional<Value>> visit(NullLiteralExpr n, EmitContext context) {
      return EmitResult.failure(context, n, "Null literals are not supported.");
    }

    @Override
    public EmitResult<Optional<Value>> visit(StringLiteralExpr n, EmitContext context) {
      return EmitResult.of(
          Optional.of(
              context
                  .insert(new ConstantOp(context.loc(n), n.getValue().replace("\\n", "\n")))
                  .getResult()));
    }

    @Override
    public EmitResult<Optional<Value>> visit(UnaryExpr n, EmitContext context) {
      EmitResult<Optional<Value>> operandResult;
      {
        operandResult = EmitResult.ofNullable(n.getExpression().accept(this, context));
        if (operandResult.isFailure() || operandResult.get().isEmpty())
          return EmitResult.failure(context, n, "Failed to emit expression of unary expression");
      }
      Value operand = operandResult.get().get();

      boolean postfix = false;
      boolean invalid = false;
      ArithAttrs.UnaryModeAttr.UnaryMode unaryMode =
          switch (n.getOperator()) {
            case PLUS -> null;
            case MINUS -> ArithAttrs.UnaryModeAttr.UnaryMode.NEGATE;
            case PREFIX_INCREMENT -> ArithAttrs.UnaryModeAttr.UnaryMode.INCREMENT;
            case PREFIX_DECREMENT -> ArithAttrs.UnaryModeAttr.UnaryMode.DECREMENT;
            case LOGICAL_COMPLEMENT -> ArithAttrs.UnaryModeAttr.UnaryMode.LOGICAL_COMPLEMENT;
            case BITWISE_COMPLEMENT -> ArithAttrs.UnaryModeAttr.UnaryMode.COMPLEMENT;
            case POSTFIX_INCREMENT -> {
              postfix = true;
              yield ArithAttrs.UnaryModeAttr.UnaryMode.INCREMENT;
            }
            case POSTFIX_DECREMENT -> {
              postfix = true;
              yield ArithAttrs.UnaryModeAttr.UnaryMode.DECREMENT;
            }
            default -> {
              invalid = true;
              yield null;
            }
          };

      if (unaryMode == null) {
        if (invalid)
          return EmitResult.failure(context, n, "Unsupported unary operator " + n.getOperator());
        else return EmitResult.success(operandResult.get());
      }

      Value result = null;
      if (postfix) {
        // Copy the value to a new value and return that so we can modify the increment target.
        BuiltinOps.IdOp idOp = context.insert(new BuiltinOps.IdOp(context.loc(n), operand));
        result = idOp.getResult();
      }
      ArithOps.UnaryOp unary =
          context.insert(new ArithOps.UnaryOp(context.loc(n), operand, unaryMode));
      result = result == null ? unary.getResult() : result;
      return EmitResult.of(Optional.of(result));
    }

    @Override
    public @NotNull EmitResult<Optional<Value>> visit(
        @NotNull VariableDeclarationExpr n, @NotNull EmitContext context) {
      {
        if (n.getAnnotations().isNonEmpty())
          context.emitWarning(
              n,
              "Variable declaration has annotations. Annotations are not supported and will be ignored. Annotations: "
                  + n.getAnnotations());
      }
      if (n.getVariables().isEmpty()) {
        return EmitResult.failure(
            context, n, "Variable declaration must declare at least one variable.");
      }

      for (VariableDeclarator varDecl : n.getVariables()) {
        if (varDecl.getInitializer().isEmpty()) {
          return EmitResult.failure(context, n, "Variable declaration must have an initializer.");
        }

        EmitResult<Optional<Value>> initializerResult =
            EmitResult.ofNullable(varDecl.getInitializer().orElseThrow().accept(this, context));
        if (initializerResult.isFailure() || initializerResult.get().isEmpty())
          return initializerResult;
        Value initValue = initializerResult.get().get();

        // Get the resolved variable declaration so that we can get the type of the variable and
        // check
        // that it is accessible from the current context.
        Optional<TypeInfo> initializerTypeInfo = resolveType(varDecl.getType(), context);
        if (initializerTypeInfo.isEmpty()) {
          return EmitResult.failure();
        }
        // Check that the target variable type is accessible in the current context
        {
          // The class in which the variable is declared
          var contextClass =
              resolve(
                  varDecl.findAncestor(ClassOrInterfaceDeclaration.class).orElseThrow(), context);
          if (contextClass.isEmpty()) {
            return EmitResult.failure();
          }

          if (!isTypeUseAccessibleFrom(
              contextClass.get(), initializerTypeInfo.get().resolvedType())) {
            return EmitResult.failure(
                context,
                varDecl,
                "Variable type "
                    + initializerTypeInfo.get().resolvedType()
                    + " for variable "
                    + varDecl.getName()
                    + " is not visible from "
                    + contextClass);
          }
        }

        bindName(varDecl.getName().asString(), initValue, varDecl, context);
      }

      return EmitResult.of(Optional.empty());
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
        @NotNull String name, @NotNull Value value, @NotNull Node site, EmitContext context) {
      context.putSymbol(name, value);
      value.setDebugInfo(new ValueDebugInfo(context.loc(site), name));
    }

    private record ParameterInfo(String name, Type type, ResolvedType resolvedType) {}

    private EmitResult<ParameterInfo> resolveParameter(Parameter n, EmitContext context) {
      {
        if (n.getAnnotations().isNonEmpty()) {
          context.emitWarning(
              n,
              "Parameter "
                  + n.getName()
                  + " has annotations. Annotations are not supported and will be ignored. Annotations: "
                  + n.getAnnotations());
        }
      }

      {
        if (n.getVarArgsAnnotations().isNonEmpty()) {
          context.emitError(
              n,
              "Parameter "
                  + n.getName()
                  + " has varargs annotations. Varargs annotations are not supported. Annotations: "
                  + n.getVarArgsAnnotations());
          return EmitResult.failure();
        }
      }

      Optional<TypeInfo> typeInfo = resolveType(n.getType(), context);
      return typeInfo
          .map(
              info ->
                  EmitResult.of(
                      new ParameterInfo(
                          n.getName().getIdentifier(), info.type(), info.resolvedType())))
          .orElseGet(
              () ->
                  EmitResult.failure(
                      context, n, "Failed to resolve type of parameter " + n.getName()));
    }

    private EmitResult<Optional<Value>> emitIntrinsic(
        MethodCallExpr n, String intrinsicName, List<Value> args, EmitContext context) {
      switch (intrinsicName) {

        // Hero Operations

        case "Dungeon.Hero.move()" -> {
          context.insert(new DgOps.MoveOp(context.loc(n)));
          return EmitResult.of(Optional.empty());
        }
        case "Dungeon.Hero.turnLeft()" -> {
          context.insert(new DgOps.TurnOp(context.loc(n), DgAttrs.TurnDirectionAttr.TurnDir.LEFT));
          return EmitResult.of(Optional.empty());
        }
        case "Dungeon.Hero.turnRight()" -> {
          context.insert(new DgOps.TurnOp(context.loc(n), DgAttrs.TurnDirectionAttr.TurnDir.RIGHT));
          return EmitResult.of(Optional.empty());
        }
        case "Dungeon.Hero.useHere()" -> {
          context.insert(new DgOps.UseOp(context.loc(n), DgAttrs.UseDirectionAttr.UseDir.HERE));
          return EmitResult.of(Optional.empty());
        }
        case "Dungeon.Hero.useLeft()" -> {
          context.insert(new DgOps.UseOp(context.loc(n), DgAttrs.UseDirectionAttr.UseDir.LEFT));
          return EmitResult.of(Optional.empty());
        }
        case "Dungeon.Hero.useRight()" -> {
          context.insert(new DgOps.UseOp(context.loc(n), DgAttrs.UseDirectionAttr.UseDir.RIGHT));
          return EmitResult.of(Optional.empty());
        }
        case "Dungeon.Hero.useUp()" -> {
          context.insert(new DgOps.UseOp(context.loc(n), DgAttrs.UseDirectionAttr.UseDir.UP));
          return EmitResult.of(Optional.empty());
        }
        case "Dungeon.Hero.useDown()" -> {
          context.insert(new DgOps.UseOp(context.loc(n), DgAttrs.UseDirectionAttr.UseDir.DOWN));
          return EmitResult.of(Optional.empty());
        }
        case "Dungeon.Hero.push()" -> {
          context.insert(new DgOps.PushOp(context.loc(n)));
          return EmitResult.of(Optional.empty());
        }
        case "Dungeon.Hero.pull()" -> {
          context.insert(new DgOps.PullOp(context.loc(n)));
          return EmitResult.of(Optional.empty());
        }
        case "Dungeon.Hero.dropClover()" -> {
          context.insert(new DgOps.DropOp(context.loc(n), DgAttrs.DropTypeAttr.DropType.CLOVER));
          return EmitResult.of(Optional.empty());
        }
        case "Dungeon.Hero.dropBreadCrumbs()" -> {
          context.insert(
              new DgOps.DropOp(context.loc(n), DgAttrs.DropTypeAttr.DropType.BREADCRUMBS));
          return EmitResult.of(Optional.empty());
        }
        case "Dungeon.Hero.pickUp()" -> {
          context.insert(new DgOps.PickupOp(context.loc(n)));
          return EmitResult.of(Optional.empty());
        }
        case "Dungeon.Hero.fireball()" -> {
          context.insert(new DgOps.FireballOp(context.loc(n)));
          return EmitResult.of(Optional.empty());
        }
        case "Dungeon.Hero.rest()" -> {
          context.insert(new DgOps.RestOp(context.loc(n)));
          return EmitResult.of(Optional.empty());
        }

        // IO Operations

        case "Dungeon.IO.print(java.lang.String)" -> {
          context.insert(new IoOps.PrintOp(context.loc(n), args.getFirst()));
          return EmitResult.of(Optional.empty());
        }
        case "Dungeon.IO.println(java.lang.String)" -> {
          var formatString = context.insert(new ConstantOp(context.loc(n), "%s\n"));
          context.insert(
              new IoOps.PrintOp(context.loc(n), formatString.getResult(), args.getFirst()));
          return EmitResult.of(Optional.empty());
        }
        case "Dungeon.IO.printf(java.lang.String, java.lang.Object...)" -> {
          context.insert(new IoOps.PrintOp(context.loc(n), args));
          return EmitResult.of(Optional.empty());
        }
        case "Dungeon.IO.nextFloat()" -> {
          var result = context.insert(new IoOps.ConsoleInOp(context.loc(n), FloatT.FLOAT32));
          return EmitResult.of(Optional.of(result.getResult()));
        }
        case "Dungeon.IO.nextDouble()" -> {
          var result = context.insert(new IoOps.ConsoleInOp(context.loc(n), FloatT.FLOAT64));
          return EmitResult.of(Optional.of(result.getResult()));
        }
        case "Dungeon.IO.nextBoolean()" -> {
          var result = context.insert(new IoOps.ConsoleInOp(context.loc(n), IntegerT.BOOL));
          return EmitResult.of(Optional.of(result.getResult()));
        }
        case "Dungeon.IO.nextByte()" -> {
          var result = context.insert(new IoOps.ConsoleInOp(context.loc(n), IntegerT.INT8));
          return EmitResult.of(Optional.of(result.getResult()));
        }
        case "Dungeon.IO.nextShort()" -> {
          var result = context.insert(new IoOps.ConsoleInOp(context.loc(n), IntegerT.INT16));
          return EmitResult.of(Optional.of(result.getResult()));
        }
        case "Dungeon.IO.nextInt()" -> {
          var result = context.insert(new IoOps.ConsoleInOp(context.loc(n), IntegerT.INT32));
          return EmitResult.of(Optional.of(result.getResult()));
        }
        case "Dungeon.IO.nextLong()" -> {
          var result = context.insert(new IoOps.ConsoleInOp(context.loc(n), IntegerT.INT64));
          return EmitResult.of(Optional.of(result.getResult()));
        }
        case "Dungeon.IO.nextLine()" -> {
          var result =
              context.insert(new IoOps.ConsoleInOp(context.loc(n), StrTypes.StringT.INSTANCE));
          return EmitResult.of(Optional.of(result.getResult()));
        }

        default -> context.emitError(n, "Intrinsic method " + intrinsicName + " is not supported.");
      }
      return EmitResult.success(Optional.empty());
    }

    /**
     * Emit intrinsic methods for the String class, such as length() and charAt(int index).
     *
     * @param n the class declaration to check if it is the String class and emit the intrinsic
     *     methods for it.
     * @param context the emit context.
     */
    private void emitStringIntrinsicMethods(
        @NotNull ResolvedReferenceTypeDeclaration n, @NotNull EmitContext context) {
      if (!n.getName().equals("String")) {
        return;
      }
      Location loc = Location.UNKNOWN;

      if (SymbolTable.lookupSymbolIn(
              context.getProgramBlock().orElseThrow().getParentOperation().orElseThrow(),
              "java.lang.String.length()")
          != null) {
        return;
      }

      // Insert the operations at the end of the program
      try (var endOfProgramInsertion =
          context.setInsertionPoint(
              context.getProgramBlock().orElseThrow(),
              context.getProgramBlock().orElseThrow().getOperations().size())) {

        // Emit length() method
        {
          FuncOp lengthFunc =
              context.insert(
                  new FuncOp(
                      loc,
                      "java.lang.String.length()",
                      FuncType.of(List.of(StrTypes.StringT.INSTANCE), IntegerT.INT32)));
          try (var bodyInsertion = context.setInsertionPoint(lengthFunc.getEntryBlock(), -1)) {
            StrOps.LengthOp lengthOp =
                context.insert(new StrOps.LengthOp(loc, lengthFunc.getArgument(0).orElseThrow()));
            context.insert(new ReturnOp(loc, lengthOp.getResult()));
          }
        }

        // Emit equals() method
        {
          FuncOp equalsFunc =
              context.insert(
                  new FuncOp(
                      loc,
                      "java.lang.String.equals(java.lang.Object)",
                      FuncType.of(
                          List.of(StrTypes.StringT.INSTANCE, StrTypes.StringT.INSTANCE),
                          IntegerT.BOOL)));
          try (var bodyInsertion = context.setInsertionPoint(equalsFunc.getEntryBlock(), -1)) {
            StrOps.EqualsOp equalsOp =
                context.insert(
                    new StrOps.EqualsOp(
                        loc,
                        equalsFunc.getArgument(0).orElseThrow(),
                        equalsFunc.getArgument(1).orElseThrow()));
            context.insert(new ReturnOp(loc, equalsOp.getResult()));
          }
        }

        // Emit charAt(int index) method
        {
          FuncOp charAtFunc =
              context.insert(
                  new FuncOp(
                      loc,
                      "java.lang.String.charAt(int)",
                      FuncType.of(
                          List.of(StrTypes.StringT.INSTANCE, IntegerT.INT32), IntegerT.UINT16)));
          try (var bodyInsertion = context.setInsertionPoint(charAtFunc.getEntryBlock(), -1)) {
            StrOps.CharAtOp charAtOp =
                context.insert(
                    new StrOps.CharAtOp(
                        loc,
                        charAtFunc.getArgument(0).orElseThrow(),
                        charAtFunc.getArgument(1).orElseThrow()));
            context.insert(new ReturnOp(loc, charAtOp.getResult()));
          }
        }

        // Emit isEmpty() method
        {
          FuncOp func =
              context.insert(
                  new FuncOp(
                      loc,
                      "java.lang.String.isEmpty()",
                      FuncType.of(List.of(StrTypes.StringT.INSTANCE), IntegerT.BOOL)));
          try (var bodyInsertion = context.setInsertionPoint(func.getEntryBlock(), -1)) {
            StrOps.IsEmptyOp op =
                context.insert(new StrOps.IsEmptyOp(loc, func.getArgument(0).orElseThrow()));
            context.insert(new ReturnOp(loc, op.getResult()));
          }
        }

        // Emit toLowerCase(Locale) method
        {
          FuncOp func =
              context.insert(
                  new FuncOp(
                      loc,
                      "java.lang.String.toLowerCase()",
                      FuncType.of(List.of(StrTypes.StringT.INSTANCE), StrTypes.StringT.INSTANCE)));
          try (var bodyInsertion = context.setInsertionPoint(func.getEntryBlock(), -1)) {
            StrOps.ToLowerCaseOp op =
                context.insert(new StrOps.ToLowerCaseOp(loc, func.getArgument(0).orElseThrow()));
            context.insert(new ReturnOp(loc, op.getResult()));
          }
        }

        // Emit toUpperCase(Locale) method
        {
          FuncOp func =
              context.insert(
                  new FuncOp(
                      loc,
                      "java.lang.String.toUpperCase()",
                      FuncType.of(List.of(StrTypes.StringT.INSTANCE), StrTypes.StringT.INSTANCE)));
          try (var bodyInsertion = context.setInsertionPoint(func.getEntryBlock(), -1)) {
            StrOps.ToUpperCaseOp op =
                context.insert(new StrOps.ToUpperCaseOp(loc, func.getArgument(0).orElseThrow()));
            context.insert(new ReturnOp(loc, op.getResult()));
          }
        }

        // Emit trim() method
        {
          FuncOp func =
              context.insert(
                  new FuncOp(
                      loc,
                      "java.lang.String.trim()",
                      FuncType.of(List.of(StrTypes.StringT.INSTANCE), StrTypes.StringT.INSTANCE)));
          try (var bodyInsertion = context.setInsertionPoint(func.getEntryBlock(), -1)) {
            StrOps.TrimOp op =
                context.insert(new StrOps.TrimOp(loc, func.getArgument(0).orElseThrow()));
            context.insert(new ReturnOp(loc, op.getResult()));
          }
        }

        // Emit substring(int) method
        {
          FuncOp func =
              context.insert(
                  new FuncOp(
                      loc,
                      "java.lang.String.substring(int)",
                      FuncType.of(
                          List.of(StrTypes.StringT.INSTANCE, IntegerT.INT32),
                          StrTypes.StringT.INSTANCE)));
          try (var bodyInsertion = context.setInsertionPoint(func.getEntryBlock(), -1)) {
            StrOps.SubstringOp op =
                context.insert(
                    new StrOps.SubstringOp(
                        loc, func.getArgument(0).orElseThrow(), func.getArgument(1).orElseThrow()));
            context.insert(new ReturnOp(loc, op.getResult()));
          }
        }

        // Emit substring(int, int) method
        {
          FuncOp func =
              context.insert(
                  new FuncOp(
                      loc,
                      "java.lang.String.substring(int, int)",
                      FuncType.of(
                          List.of(StrTypes.StringT.INSTANCE, IntegerT.INT32, IntegerT.INT32),
                          StrTypes.StringT.INSTANCE)));
          try (var bodyInsertion = context.setInsertionPoint(func.getEntryBlock(), -1)) {
            StrOps.SubstringOp op =
                context.insert(
                    new StrOps.SubstringOp(
                        loc,
                        func.getArgument(0).orElseThrow(),
                        func.getArgument(1).orElseThrow(),
                        func.getArgument(2).orElseThrow()));
            context.insert(new ReturnOp(loc, op.getResult()));
          }
        }

        // Emit concat(String) method
        {
          FuncOp func =
              context.insert(
                  new FuncOp(
                      loc,
                      "java.lang.String.concat(java.lang.String)",
                      FuncType.of(
                          List.of(StrTypes.StringT.INSTANCE, StrTypes.StringT.INSTANCE),
                          StrTypes.StringT.INSTANCE)));
          try (var bodyInsertion = context.setInsertionPoint(func.getEntryBlock(), -1)) {
            StrOps.ConcatOp op =
                context.insert(
                    new StrOps.ConcatOp(
                        loc, func.getArgument(0).orElseThrow(), func.getArgument(1).orElseThrow()));
            context.insert(new ReturnOp(loc, op.getResult()));
          }
        }

        // Emit startsWith(String) method
        {
          FuncOp func =
              context.insert(
                  new FuncOp(
                      loc,
                      "java.lang.String.startsWith(java.lang.String)",
                      FuncType.of(
                          List.of(StrTypes.StringT.INSTANCE, StrTypes.StringT.INSTANCE),
                          IntegerT.BOOL)));
          try (var bodyInsertion = context.setInsertionPoint(func.getEntryBlock(), -1)) {
            StrOps.StartsWithOp op =
                context.insert(
                    new StrOps.StartsWithOp(
                        loc, func.getArgument(0).orElseThrow(), func.getArgument(1).orElseThrow()));
            context.insert(new ReturnOp(loc, op.getResult()));
          }
        }

        // Emit endsWith(String) method
        {
          FuncOp func =
              context.insert(
                  new FuncOp(
                      loc,
                      "java.lang.String.endsWith(java.lang.String)",
                      FuncType.of(
                          List.of(StrTypes.StringT.INSTANCE, StrTypes.StringT.INSTANCE),
                          IntegerT.BOOL)));
          try (var bodyInsertion = context.setInsertionPoint(func.getEntryBlock(), -1)) {
            StrOps.EndsWithOp op =
                context.insert(
                    new StrOps.EndsWithOp(
                        loc, func.getArgument(0).orElseThrow(), func.getArgument(1).orElseThrow()));
            context.insert(new ReturnOp(loc, op.getResult()));
          }
        }

        // Emit indexOf(String) method
        {
          FuncOp func =
              context.insert(
                  new FuncOp(
                      loc,
                      "java.lang.String.indexOf(java.lang.String)",
                      FuncType.of(
                          List.of(StrTypes.StringT.INSTANCE, StrTypes.StringT.INSTANCE),
                          IntegerT.INT32)));
          try (var bodyInsertion = context.setInsertionPoint(func.getEntryBlock(), -1)) {
            StrOps.IndexOfOp op =
                context.insert(
                    new StrOps.IndexOfOp(
                        loc, func.getArgument(0).orElseThrow(), func.getArgument(1).orElseThrow()));
            context.insert(new ReturnOp(Location.UNKNOWN, op.getResult()));
          }
        }

        // Emit lastIndexOf(String) method
        {
          FuncOp func =
              context.insert(
                  new FuncOp(
                      loc,
                      "java.lang.String.lastIndexOf(java.lang.String)",
                      FuncType.of(
                          List.of(StrTypes.StringT.INSTANCE, StrTypes.StringT.INSTANCE),
                          IntegerT.INT32)));
          try (var bodyInsertion = context.setInsertionPoint(func.getEntryBlock(), -1)) {
            StrOps.LastIndexOfOp op =
                context.insert(
                    new StrOps.LastIndexOfOp(
                        loc, func.getArgument(0).orElseThrow(), func.getArgument(1).orElseThrow()));
            context.insert(new ReturnOp(loc, op.getResult()));
          }
        }
      }
    }
  }
}
