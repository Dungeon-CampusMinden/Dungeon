package transformations;

import org.junit.jupiter.api.Test;

public class DeadCodeEliminationTests extends TransformationTestBase {
  @Test
  void forBodyOnlyContinueBecomesEmptyBlock() {
    String code =
"""
public class %ClassName {
  public void test() {
    for (int i = 0; i < 3; i++)
      continue;
  }
}
""";
    String expected =
"""
public class %ClassName {

    public void test() {
        for (int i = 0; i < 3; i++) {
        }
    }
}
""";
    assertCodeAfterDeadCodeElimination(expected, code);
  }

  @Test
  void trailingContinueInForBlockRemoved() {
    String code =
"""
public class %ClassName {
  public void test() {
    for (int i = 0; i < 3; i++) {
      int x = i;
      continue;
    }
  }
}
""";
    String expected =
"""
public class %ClassName {

    public void test() {
        for (int i = 0; i < 3; i++) {
            int x = i;
        }
    }
}
""";
    assertCodeAfterDeadCodeElimination(expected, code);
  }

  @Test
  void statementsAfterBreakAndReturnRemoved() {
    String code =
"""
public class %ClassName {
  public int test(boolean b) {
    while (true) {
      break;
      int x = 1;
    }
    if (b) {
      return 1;
      int y = 2;
    }
    return 0;
  }
}
""";
    String expected =
"""
public class %ClassName {

    public int test(boolean b) {
        while (true) {
            break;
        }
        if (b) {
            return 1;
        }
        return 0;
    }
}
""";
    assertCodeAfterDeadCodeElimination(expected, code);
  }

  @Test
  void singleNestedBlockFlattens() {
    String code =
"""
public class %ClassName {
  public void test() {
    {
      {
        int x = 1;
      }
    }
  }
}
""";
    String expected =
"""
public class %ClassName {

    public void test() {
        int x = 1;
    }
}
""";
    assertCodeAfterDeadCodeElimination(expected, code);
  }
}
