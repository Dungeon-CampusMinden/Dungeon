package transformations;

import blockly.dgir.compiler.java.EmitContext;
import blockly.dgir.compiler.java.transformations.ImplicitCastElimination;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImplicitCastEliminationTests extends TransformationTestBase {
  @Test
  void declarationWideningAddsExplicitCast() {
    String code =
"""
public class %ClassName {
  public void test() {
    long value = 1;
  }
}
""";
    String expected =
"""
public class %ClassName {

    public void test() {
        long value = (long) 1;
    }
}
""";
    assertCodeAfterImplicitCastElimination(expected, code);
  }

  @Test
  void assignmentNarrowingLiteralAddsExplicitCast() {
    String code =
"""
public class %ClassName {
  public void test() {
    byte b = 0;
    b = 1;
  }
}
""";
    String expected =
"""
public class %ClassName {

    public void test() {
        byte b = (byte) 0;
        b = (byte) 1;
    }
}
""";
    assertCodeAfterImplicitCastElimination(expected, code);
  }

  @Test
  void methodCallArgumentAddsParameterTypeCast() {

    String code =
"""
public class %ClassName {
  public void test() {
    int i = Long.compare(1, 2);
  }

  public void consume(short value) {
  }
}
""";
    String expected =
"""
public class invokeMethod {

    public void test() {
        int i = Long.compare((long) 1, (long) 2);
    }

    public void consume(short value) {
    }
}
""";
    assertCodeAfterImplicitCastElimination(expected, code);
  }

  @Test
  void matchingTypesStayUntouched() {
    String code =
"""
public class %ClassName {
  public void test() {
    int a = 1;
    int b = a;
  }
}
""";
    String expected =
"""
public class %ClassName {

    public void test() {
        int a = 1;
        int b = a;
    }
}
""";
    assertCodeAfterImplicitCastElimination(expected, code);
  }

  @Test
  void generatedCallArgumentCastsKeepSameLineSourceOrder() {
    String code =
        """
public class RangeCastClass {
  public void test() {
    int i = Long.compare(1, 2);
  }
}
""";

    CompilationUnit cu = StaticJavaParser.parse(code);
    MethodCallExpr originalCall = cu.findFirst(MethodCallExpr.class).orElseThrow();
    TokenRange firstArgRange = originalCall.getArgument(0).getTokenRange().orElseThrow();
    TokenRange secondArgRange = originalCall.getArgument(1).getTokenRange().orElseThrow();

    new ImplicitCastElimination().visit(cu, new EmitContext("generatedCallArgumentCastsKeepSameLineSourceOrder"));

    MethodCallExpr loweredCall = cu.findFirst(MethodCallExpr.class).orElseThrow();
    CastExpr firstCast = loweredCall.getArgument(0).asCastExpr();
    CastExpr secondCast = loweredCall.getArgument(1).asCastExpr();

    TokenRange firstCastRange = firstCast.getTokenRange().orElseThrow();
    TokenRange secondCastRange = secondCast.getTokenRange().orElseThrow();
    assertEquals(beginLine(firstArgRange), beginLine(firstCastRange));
    assertEquals(beginLine(secondArgRange), beginLine(secondCastRange));
    assertEquals(beginLine(firstCastRange), beginLine(secondCastRange));
    assertTrue(beginColumn(firstCastRange) < beginColumn(secondCastRange));
  }

  private static int beginLine(TokenRange tokenRange) {
    return tokenRange.getBegin().getRange().orElseThrow().begin.line;
  }

  private static int beginColumn(TokenRange tokenRange) {
    return tokenRange.getBegin().getRange().orElseThrow().begin.column;
  }
}
