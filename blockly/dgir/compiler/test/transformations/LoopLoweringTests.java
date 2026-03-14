package transformations;

import org.junit.jupiter.api.Test;

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
}
