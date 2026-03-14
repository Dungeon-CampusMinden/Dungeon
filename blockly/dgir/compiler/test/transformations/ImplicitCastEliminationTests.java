package transformations;

import org.junit.jupiter.api.Test;

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
}
