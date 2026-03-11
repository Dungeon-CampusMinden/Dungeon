import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CompilerTest extends CompilerTestBase {
  @Test
  void variableAssignment() {
    String code =
"""
public class %ClassName {
  public static void main() {
    int x = 5;
    x = 10;
  }
}
""";
    testSource(code);
  }

  @Test
  void variableAssignment_scoped() {
    String code =
"""
public class %ClassName {
  public static void main() {
    int x = 5;
    {
      int y = 10;
      x = y;
    }
    int z = x + 5;
  }
}
""";
    testSource(code);
  }

  @Test
  void variableAssignment_unaryExpr() {
    String code =
"""
public class %ClassName {
  public static void main() {
    int x = 5;
    x = -x;
    x = +x;
    x++;
    ++x;
    int z = x++;
    boolean y = true;
    y = !y;
  }
}
""";
    testSource(code);
  }

  @Test
  void variableAssignment_binaryExpr() {
    String code =
"""
public class %ClassName {
  public static void main() {
    int a = 5;
    int b = 10;
    int c = a + b;
    int d = c * 2;
    float e = d / 2;
    float f = e - 1;
    float g = f * 2;
    float h = g / 2;
    float i = h % 4;
    int j = 4 + 3;
    float k = 4 + 3f * 2 / 4;
  }
}
""";
    testSource(code);
  }

  @Test
  void functionCall() {
    String code =
"""
public class %ClassName {
  public static void main() {
    int result = add(5, 10);
  }

  public static int add(int a, int b) {
    return a + b;
  }
}
""";
    testSource(code);
  }

  @Test
  void functionCallWithOverload() {
    String code =
"""
public class %ClassName {
  public static void main() {
    int result = add(5, 10);
    float resultfloat = add(5f, 10f);
    float mixedFloatResult = add(5f, 10);
    float mixedIntResult = add(5, 10f);
  }

  public static int add(int a, int b) {
    return a + b;
  }

  public static float add(float a, float b) {
    return a + b;
  }
}
""";
    testSource(code);
  }

  @Test
  void assignmentOperators() {
    String code =
"""
public class %ClassName {
  public static void main() {
    int x = 5;
    x += 10;
    x -= 5;
    x *= 2;
    x /= 2;
    x %= 4;
  }
}
""";
    testSource(code);
  }

  @Test
  void wideningPrimitiveConversion() {
    String code =
"""
public class %ClassName {
  public static void main() {
    byte b = 1;
    short s = b;
    char c = 'a';
    int i = b;
    long l = b;
    float f = b;
    double d = b;

    b = 'a';
    assert b == 97 : "Expected byte value 97 from char 'a', but got " + b;
    s = 'b';
    assert s == 98 : "Expected short value 98 from char 'a', but got " + s;
    s = 1;
    c = 1;
    i = s;
    l = s;
    f = s;
    d = s;

    i = c;
    l = c;
    f = c;
    d = c;

    l = i;
    f = i;
    d = i;

    f = l;
    d = l;

    d = f;
  }
}
""";
    testSource(code);
  }

  @Test
  void multipleClassesWithStaticFunctions() {
    String code =
"""
public class %ClassName {
  public static void main() {
    int a = OtherClass.add(5, 10);
    float b = OtherClass.add(5f, 10f);
    float c = OtherClass.add(5f, 10);
  }
}

public class OtherClass {
  public static int add(int a, int b) {
    return a + b;
  }

  public static float add(float a, float b) {
    return a + b;
  }
}
""";
    testSource(code);
  }

  @Test
  void nestedClass() {
    String code =
"""
public class %ClassName {
  public static void main() {
    int a = NestedClass.add(5, 10);
    float b = NestedClass.add(5f, 10f);
    float c = NestedClass.add(5f, 10);
  }

  public static class NestedClass {
    public static int add(int a, int b) {
      return a + b;
    }

    public static float add(float a, float b) {
      return a + b;
    }
  }
}
""";
    testSource(code);
  }

  @Test
  void nestedClasses() {
    String code =
"""
public class %ClassName {
  public static void main() {
    int a = NestedClass1.add(5, 10);
    float b = NestedClass2.add(5f, 10f);
    float d = OtherClass.add(5f, 10f);
    float e = OtherClass.add(5f, 10);
    float f = OtherClass.NestedClass3.add(5f, 10f);
    float g = OtherClass.NestedClass3.add(5f, 10);
  }

  private static class NestedClass1 {
    private static int add(int a, int b) {
      return a + b;
    }
  }

  public static class NestedClass2 {
    public static float add(float a, float b) {
      return a + b;
    }

    public static int add(int a, int b) {
      return a + b;
    }
  }
}

class OtherClass {
  public static int add(int a, int b) {
    return a + b;
  }

  static float add(float a, float b) {
    return a + b;
  }

  public static class NestedClass3 {
    public static int add(int a, int b) {
      return nestedClasses.NestedClass2.add(a, b);
    }

    static float add(float a, float b) {
      return a + b;
    }
  }
}
""";
    testSource(code);
  }

  @Test
  void nestedClassesInvalidAccess() {
    String code =
"""
public class %ClassName {
  public static void main() {
    int a = NestedClass1.add(5, 10);
    float b = NestedClass2.add(5f, 10f);
    float d = OtherClass.add(5f, 10f);
    float e = OtherClass.add(5f, 10);
    float f = OtherClass.NestedClass3.add(5f, 10f);
    float g = OtherClass.NestedClass3.add(5f, 10);
  }

  private static class NestedClass1 {
    private static int add(int a, int b) {
      return a + b;
    }
  }

  public static class NestedClass2 {
    public static float add(float a, float b) {
      return a + b;
    }

    private static int add(int a, int b) {
      return a + b;
    }
  }
}

class OtherClass {
  public static int add(int a, int b) {
    return a + b;
  }

  static float add(float a, float b) {
    return a + b;
  }

  public static class NestedClass3 {
    public static int add(int a, int b) {
      return nestedClassesInvalidAccess.NestedClass2.add(a, b);
    }

    static float add(float a, float b) {
      return a + b;
    }
  }
}
""";
    Assertions.assertThrows(AssertionError.class, () -> testSource(code));
  }

  @Test
  void simpleForLoop() {
    String code =
"""
public class %ClassName {
  public static void main() {
    int x = 0;
    for (int i = 0; i < 1000000; i++) {
      x += i;
    }
  }
}
""";
    testSource(code);
  }

  @Test
  void forLoopWithNestedClassLookup() {
    String code =
"""
public class %ClassName {
  public static void main() {
    for (int i = 0; i < 100000; i++) {
      int a = NestedClass1.add(5, 10);
      float b = NestedClass2.add(5f, 10f);
      float d = OtherClass.add(5f, 10f);
      float e = OtherClass.add(5f, 10);
      float f = OtherClass.NestedClass3.add(5f, 10f);
      float g = OtherClass.NestedClass3.add(5f, 10);
    }
  }

  private static class NestedClass1 {
    private static int add(int a, int b) {
      return a + b;
    }
  }

  public static class NestedClass2 {
    public static float add(float a, float b) {
      return a + b;
    }

    public static int add(int a, int b) {
      return a + b;
    }
  }
}

class OtherClass {
  public static int add(int a, int b) {
    return a + b;
  }

  static float add(float a, float b) {
    return a + b;
  }

  public static class NestedClass3 {
    public static int add(int a, int b) {
      return forLoopWithNestedClassLookup.NestedClass2.add(a, b);
    }

    static float add(float a, float b) {
      return a + b;
    }
  }
}
""";
    testSource(code);
  }

  @Test
  void stringOperations() {
    String code =
"""
public class %ClassName {
  public static void main() {
    String s1 = "Hello, ";
    String s2 = "world!";
    String s3 = s1 + s2;
    boolean b1 = s3.equals("Hello, world!");

    assert b1 : "Expected true, but got " + b1;
    assert s3.length() == 13 : "Expected length 13, but got " + s3.length();
    assert s3.charAt(11) == 'd' : "Expected 'd', but got " + s3.charAt(6);
    assert s3.substring(0, 5).equals("Hello") : "Expected 'Hello', but got " + s3.substring(0, 5);
    assert s3.indexOf("world") == 7 : "Expected index of 'world' to be 7, but got " + s3.indexOf("world");
    assert s3.lastIndexOf("world") == 7 : "Expected index of 'world' to be 7, but got " + s3.lastIndexOf("world");
    assert s3.isEmpty() == false : "Expected false for isEmpty, but got " + s3.isEmpty();
    assert s3.toUpperCase().equals("HELLO, WORLD!") : "Expected 'HELLO, WORLD!', but got " + s3.toUpperCase();
    assert s3.toLowerCase().equals("hello, world!") : "Expected 'hello, world!', but got " + s3.toLowerCase();
    assert s3.startsWith("Hello") : "Expected 'Hello', but got " + s3.startsWith("Hello");
    assert s3.endsWith("!") : "Expected '!', but got " + s3.endsWith("!");

    assert "Hello".concat(" World").toUpperCase().equals("HELLO WORLD") : "Expected 'HELLO WORLD', but got " + "Hello".concat(" World").toUpperCase();
  }
}
""";
    testSource(code);
  }
}
