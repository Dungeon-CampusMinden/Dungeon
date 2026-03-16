package transformations;

import blockly.dgir.compiler.java.transformations.LoopLowering;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.WhileStmt;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoopLoweringTests extends TransformationTestBase {
  @Test
  void postfixForUpdateRewrittenToPrefix() {
    String code =
"""
public class %ClassName {
  public void test() {
    for (int i = 0; i < 3; i++) {
      int x = i;
    }
  }
}
""";
    String expected =
"""
public class %ClassName {

    public void test() {
        for (int i = 0; i < 3; ++i) {
            int x = i;
        }
    }
}
""";
    assertCodeAfterLoopLowering(expected, code);
  }

  @Test
  void continueInForInsertsSkipFlagsAndGuard() {
    String code =
"""
public class %ClassName {
  public void test() {
    for (int i = 0; i < 3; i++) {
      continue;
      int x = i;
    }
  }
}
""";
    String expected =
"""
public class %ClassName {

    public void test() {
        for (int i = 0; i < 3; ++i) {
            boolean skip = false;
            boolean skipBreak = false;
            {
                skip = true;
            }
            if (!skip) {
                int x = i;
            }
        }
    }
}
""";
    assertCodeAfterLoopLowering(expected, code);
  }

  @Test
  void breakInWhileSetsSkipAndSkipBreak() {
    String code =
"""
public class %ClassName {
  public void test() {
    while (true) {
      break;
      int x = 1;
    }
  }
}
""";
    String expected =
"""
public class %ClassName {

    public void test() {
        while (true) {
            boolean skip = false;
            boolean skipBreak = false;
            {
                skipBreak = true;
                skip = true;
            }
            if (!skip) {
                int x = 1;
            }
        }
    }
}
""";
    assertCodeAfterLoopLowering(expected, code);
  }

  @Test
  void singleStatementLoopBodyWrappedIntoBlock() {
    String code =
"""
public class %ClassName {
  public void test() {
    while (false)
      test();
  }
}
""";
    String expected =
"""
public class %ClassName {

    public void test() {
        while (false) {
            test();
        }
    }
}
""";
    assertCodeAfterLoopLowering(expected, code);
  }

  @Test
  void generatedBreakUpdateAndSkipGuardKeepOriginalLineRanges() {
    String code =
"""
public class RangeLoopClass {
  public void test() {
    while (true) {
      break;
      int x = 1;
    }
  }
}
""";

    CompilationUnit cu = StaticJavaParser.parse(code);
    BreakStmt originalBreak = cu.findFirst(BreakStmt.class).orElseThrow();
    int breakLine = beginLine(originalBreak.getTokenRange().orElseThrow());
    int nextLine =
        beginLine(
            originalBreak
                .findAncestor(BlockStmt.class)
                .orElseThrow()
                .getStatement(1)
                .getTokenRange()
                .orElseThrow());

    cu.accept(new LoopLowering(), false);

    WhileStmt loweredWhile = cu.findFirst(WhileStmt.class).orElseThrow();
    BlockStmt loweredBody = loweredWhile.getBody().asBlockStmt();
    Statement loweredBreakUpdate = loweredBody.getStatement(2);
    IfStmt loweredGuard = loweredBody.getStatement(3).asIfStmt();

    assertEquals(breakLine, beginLine(loweredBreakUpdate.getTokenRange().orElseThrow()));
    assertEquals(nextLine, beginLine(loweredGuard.getTokenRange().orElseThrow()));
  }

  private static int beginLine(TokenRange tokenRange) {
    return tokenRange.getBegin().getRange().orElseThrow().begin.line;
  }
}
