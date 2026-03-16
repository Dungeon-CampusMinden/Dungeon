package transformations;

import blockly.dgir.compiler.java.EmitContext;
import blockly.dgir.compiler.java.transformations.SwitchToIf;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.SwitchExpr;
import com.github.javaparser.ast.stmt.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SwitchToIfTests extends TransformationTestBase {
  @Test
  void switchStatementBecomesIfElseChain() {
    String code =
"""
public class %ClassName {
  public void test(int x) {
    switch (x) {
      case 1:
        a();
        break;
      default:
        b();
        break;
    }
  }

  private void a() {}
  private void b() {}
}
""";
    String expected =
"""
public class %ClassName {

    public void test(int x) {
        {
            if (x == 1) {
                a();
            } else {
                b();
            }
        }
    }

    private void a() {
    }

    private void b() {
    }
}
""";
    assertCodeAfterSwitchToIf(expected, code);
  }

  @Test
  void fallthroughLabelsBecomeOrCondition() {
    String code =
"""
public class %ClassName {
  public void test(int x) {
    switch (x) {
      case 1:
      case 2:
        a();
        break;
      case 3:
        b();
        break;
      default:
        c();
        break;
    }
  }

  private void a() {}
  private void b() {}
  private void c() {}
}
""";
    String expected =
"""
public class %ClassName {

    public void test(int x) {
        {
            if (x == 1 || x == 2) {
                a();
            } else if (x == 3) {
                b();
            } else {
                c();
            }
        }
    }

    private void a() {
    }

    private void b() {
    }

    private void c() {
    }
}
""";
    assertCodeAfterSwitchToIf(expected, code);
  }

  @Test
  void stringAndNullCasesUseCorrectEqualityChecks() {
    String code =
"""
public class %ClassName {
  public void test(String value) {
    switch (value) {
      case "x":
        a();
        break;
      case null:
        b();
        break;
      default:
        c();
        break;
    }
  }

  private void a() {}
  private void b() {}
  private void c() {}
}
""";
    String expected =
"""
public class %ClassName {

    public void test(String value) {
        {
            if (value.equals("x")) {
                a();
            } else if (value == null) {
                b();
            } else {
                c();
            }
        }
    }

    private void a() {
    }

    private void b() {
    }

    private void c() {
    }
}
""";
    assertCodeAfterSwitchToIf(expected, code);
  }

  @Test
  void switchExpressionBecomesNestedTernary() {
    String code =
"""
public class %ClassName {
  public int test(int x) {
    return switch (x) {
      case 1 -> 10;
      case 2 -> 20;
      default -> 0;
    };
  }
}
""";
    String expected =
"""
public class %ClassName {

    public int test(int x) {
        return x == 1 ? 10 : x == 2 ? 20 : 0;
    }
}
""";
    assertCodeAfterSwitchToIf(expected, code);
  }

  @Test
  void switchExpressionWithYieldBlockBecomesTernary() {
    String code =
"""
public class %ClassName {
  public int test(int x) {
    return switch (x) {
      case 1 -> {
        yield 10;
      }
      default -> {
        yield 0;
      }
    };
  }
}
""";
    String expected =
"""
public class %ClassName {

    public int test(int x) {
        return x == 1 ? 10 : 0;
    }
}
""";
    assertCodeAfterSwitchToIf(expected, code);
  }

  @Test
  void nonReducibleSwitchExpressionInReturnFallsBackToIfElse() {
    String code =
"""
public class %ClassName {
  public int test(int x) {
    return switch (x) {
      case 1 -> {
        int y = x + 1;
        yield y;
      }
      default -> 0;
    };
  }
}
""";
    String expected =
"""
public class %ClassName {

    public int test(int x) {
        {
            if (x == 1) {
                int y = x + 1;
                return y;
            } else {
                return 0;
            }
        }
    }
}
""";
    assertCodeAfterSwitchToIf(expected, code);
  }

  @Test
  void nestedSwitchAndSwitchExpr() {
    String code =
"""
public class %ClassName {
  public int test(int x, int y) {
    switch (x) {
      case 1:
        return switch (y) {
          case 2 -> 10;
          default -> 20;
        };
      default:
        return 0;
    }
  }
}
""";
    String expected =
"""
public class %ClassName {

    public int test(int x, int y) {
        {
            if (x == 1) {
                return y == 2 ? 10 : 20;
            } else {
                return 0;
            }
        }
    }
}
""";
    assertCodeAfterSwitchToIf(expected, code);
  }

  @Test
  void generatedIfChainKeepsCaseSourceOrderInTokenRanges() {
    String code =
"""
public class RangeClass {
  public int test(int x) {
    switch (x) {
      case 1:
        return 1;
      case 2:
        return 2;
      default:
        return 3;
    }
  }
}
""";

    CompilationUnit cu = StaticJavaParser.parse(code);
    SwitchStmt original = cu.findFirst(SwitchStmt.class).orElseThrow();
    int case1Line = beginLine(original.getEntry(0).getTokenRange().orElseThrow());
    int case2Line = beginLine(original.getEntry(1).getTokenRange().orElseThrow());
    int defaultLine = beginLine(original.getEntry(2).getTokenRange().orElseThrow());

    new SwitchToIf().visit(cu, new EmitContext("generatedIfChainKeepsCaseSourceOrderInTokenRanges"));

    IfStmt firstIf = cu.findFirst(IfStmt.class).orElseThrow();
    assertEquals(case1Line, beginLine(firstIf.getTokenRange().orElseThrow()));

    IfStmt elseIf = firstIf.getElseStmt().flatMap(Statement::toIfStmt).orElseThrow();
    assertEquals(case2Line, beginLine(elseIf.getTokenRange().orElseThrow()));

    BlockStmt elseBlock = elseIf.getElseStmt().flatMap(Statement::toBlockStmt).orElseThrow();
    assertEquals(defaultLine, beginLine(elseBlock.getTokenRange().orElseThrow()));
  }

  @Test
  void generatedTernaryKeepsBranchSourceOrderInTokenRanges() {
    String code =
"""
public class RangeExprClass {
  public int test(int x) {
    return switch (x) {
      case 1 -> 10;
      case 2 -> 20;
      default -> 0;
    };
  }
}
""";

    CompilationUnit cu = StaticJavaParser.parse(code);
    SwitchExpr original = cu.findFirst(SwitchExpr.class).orElseThrow();
    int case1Line = beginLine(original.getEntry(0).getTokenRange().orElseThrow());
    int case2Line = beginLine(original.getEntry(1).getTokenRange().orElseThrow());

    new SwitchToIf().visit(cu, new EmitContext("generatedTernaryKeepsBranchSourceOrderInTokenRanges"));

    ReturnStmt returnStmt =
        cu.findFirst(ReturnStmt.class, rs -> rs.getExpression().isPresent()).orElseThrow();
    ConditionalExpr outer = returnStmt.getExpression().orElseThrow().asConditionalExpr();
    assertEquals(case1Line, beginLine(outer.getTokenRange().orElseThrow()));

    ConditionalExpr inner = outer.getElseExpr().asConditionalExpr();
    assertEquals(case2Line, beginLine(inner.getTokenRange().orElseThrow()));
  }

  private static int beginLine(TokenRange tokenRange) {
    return tokenRange.getBegin().getRange().orElseThrow().begin.line;
  }
}
