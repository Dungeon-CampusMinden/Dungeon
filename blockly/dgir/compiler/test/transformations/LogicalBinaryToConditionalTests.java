package transformations;

import blockly.dgir.compiler.java.EmitContext;
import blockly.dgir.compiler.java.transformations.LogicalBinaryToConditional;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LogicalBinaryToConditionalTests extends TransformationTestBase {
  @Test
  void singleBinary() {
    String code =
"""
public class %ClassName {
  public void test() {
    boolean a = true;
    boolean b = false;
    boolean c = a && b;
  }
}
""";
    String expected =
"""
public class %ClassName {

    public void test() {
        boolean a = true;
        boolean b = false;
        boolean c = (a ? b : false);
    }
}
""";
    assertCode(expected, code);
  }

  @Test
  void singleOrBinary() {
    String code =
"""
public class %ClassName {
  public void test() {
    boolean a = true;
    boolean b = false;
    boolean c = a || b;
  }
}
""";
    String expected =
"""
public class %ClassName {

    public void test() {
        boolean a = true;
        boolean b = false;
        boolean c = (a ? true : b);
    }
}
""";
    assertCode(expected, code);
  }

  @Test
  void nestedAndBinary() {
    String code =
"""
public class %ClassName {
  public void test() {
    boolean a = true;
    boolean b = false;
    boolean c = true;
    boolean d = a && b && c;
  }
}
""";
    String expected =
"""
public class %ClassName {

    public void test() {
        boolean a = true;
        boolean b = false;
        boolean c = true;
        boolean d = ((a ? b : false) ? c : false);
    }
}
""";
    assertCode(expected, code);
  }

  @Test
  void nestedOrBinary() {
    String code =
"""
public class %ClassName {
  public void test() {
    boolean a = true;
    boolean b = false;
    boolean c = true;
    boolean d = a || (b || c);
  }
}
""";
    String expected =
"""
public class %ClassName {

    public void test() {
        boolean a = true;
        boolean b = false;
        boolean c = true;
        boolean d = (a ? true : ((b ? true : c)));
    }
}
""";
    assertCode(expected, code);
  }

  @Test
  void mixedAndOrBinary() {
    String code =
"""
public class %ClassName {
  public void test() {
    boolean a = true;
    boolean b = false;
    boolean c = true;
    boolean d = a && b || c;
  }
}
""";
    String expected =
"""
public class %ClassName {

    public void test() {
        boolean a = true;
        boolean b = false;
        boolean c = true;
        boolean d = ((a ? b : false) ? true : c);
    }
}
""";
    assertCode(expected, code);
  }

  @Test
  void leavesNonLogicalBinaryUntouched() {
    String code =
"""
public class %ClassName {
  public void test() {
    boolean a = true;
    boolean b = false;
    boolean c = a & b;
    boolean d = a == b;
  }
}
""";
    String expected =
"""
public class %ClassName {

    public void test() {
        boolean a = true;
        boolean b = false;
        boolean c = a & b;
        boolean d = a == b;
    }
}
""";
    assertCode(expected, code);
  }

  @Test
  void generatedConditionalKeepsOriginalAndLineRange() {
    String code =
"""
public class RangeClass {
  public void test() {
    boolean a = true;
    boolean b = false;
    boolean c = a && b;
  }
}
""";

    CompilationUnit cu = StaticJavaParser.parse(code);
    BinaryExpr originalAnd =
        cu.findFirst(BinaryExpr.class, binaryExpr -> binaryExpr.getOperator() == BinaryExpr.Operator.AND)
            .orElseThrow();
    int andLine = beginLine(originalAnd.getTokenRange().orElseThrow());

    new LogicalBinaryToConditional().visit(cu, new EmitContext("generatedConditionalKeepsOriginalAndLineRange"));

    ConditionalExpr lowered = cu.findFirst(ConditionalExpr.class).orElseThrow();
    assertEquals(andLine, beginLine(lowered.getTokenRange().orElseThrow()));
  }

  @Test
  void nestedConditionalsPreserveTopToBottomSourceOrder() {
    String code =
"""
public class RangeOrderClass {
  public void test() {
    boolean a = true;
    boolean b = false;
    boolean c = true;
    boolean d = a && b && c;
  }
}
""";

    CompilationUnit cu = StaticJavaParser.parse(code);
    BinaryExpr outerAnd =
        cu.findFirst(BinaryExpr.class, binaryExpr -> binaryExpr.toString().equals("a && b && c"))
            .orElseThrow();
    int outerLine = beginLine(outerAnd.getTokenRange().orElseThrow());

    new LogicalBinaryToConditional().visit(cu, new EmitContext("nestedConditionalsPreserveTopToBottomSourceOrder"));

    ConditionalExpr outer = cu.findFirst(ConditionalExpr.class).orElseThrow();
    ConditionalExpr inner = outer.getCondition().asEnclosedExpr().getInner().asConditionalExpr();

    assertEquals(outerLine, beginLine(outer.getTokenRange().orElseThrow()));
    assertEquals(outerLine, beginLine(inner.getTokenRange().orElseThrow()));
  }

  private static int beginLine(TokenRange tokenRange) {
    return tokenRange.getBegin().getRange().orElseThrow().begin.line;
  }
}
