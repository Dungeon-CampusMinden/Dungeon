package transformations;

import blockly.dgir.compiler.java.EmitContext;
import blockly.dgir.compiler.java.transformations.DeadCodeElimination;
import blockly.dgir.compiler.java.transformations.ImplicitCastElimination;
import blockly.dgir.compiler.java.transformations.LogicalBinaryToConditional;
import blockly.dgir.compiler.java.transformations.LoopLowering;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransformationTestBase {
  static {
    CombinedTypeSolver typeSolver = new CombinedTypeSolver();
    typeSolver.add(new ReflectionTypeSolver());

    StaticJavaParser.getParserConfiguration().setSymbolResolver(new JavaSymbolSolver(typeSolver));
  }

  public static void assertCode(String expected, String source) {
    String callerName = dgir.core.Utils.getCallingMethodName(3);
    EmitContext context = new EmitContext(callerName);
    assertCodeAfterTransformation(
        expected, source, cu -> new LogicalBinaryToConditional().visit(cu, context), context);
  }

  public static void assertCodeAfterDeadCodeElimination(String expected, String source) {
    assertCodeAfterTransformation(
        expected, source, cu -> new DeadCodeElimination().visit(cu, null), null);
  }

  public static void assertCodeAfterLoopLowering(String expected, String source) {
    String callerName = dgir.core.Utils.getCallingMethodName(3);
    EmitContext context = new EmitContext(callerName);
    assertCodeAfterTransformation(
        expected, source, cu -> new LoopLowering().visit(cu, false), context);
  }

  public static void assertCodeAfterImplicitCastElimination(String expected, String source) {
    String callerName = dgir.core.Utils.getCallingMethodName(3);
    EmitContext context = new EmitContext(callerName);
    assertCodeAfterTransformation(
        expected, source, cu -> new ImplicitCastElimination().visit(cu, context), context);
  }

  private static void assertCodeAfterTransformation(
      String expected,
      String source,
      Consumer<CompilationUnit> transform,
      @Nullable EmitContext context) {
    String callerName = dgir.core.Utils.getCallingMethodName(3);
    String formatedCode = source.replace("%ClassName", callerName);
    String formatedExpected = expected.replace("%ClassName", callerName);

    CompilationUnit cu = StaticJavaParser.parse(formatedCode);
    transform.accept(cu);

    if (context != null) {
      context.printDiagnostics();
    }

    System.out.println(cu);
    assertEquals(formatedExpected, cu.toString());
  }
}
