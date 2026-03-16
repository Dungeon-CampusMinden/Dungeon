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
    for (int i = 0; i < 1_000_000; i++) {
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
    for (int i = 0; i < 100_000; i++) {
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

  @Test
  void scfIf() {
    String code =
"""
public class %ClassName {
  public static void main() {
    int x = 5;
    if (x > 0) {
      x += 10;
    } else if (x < 0) {
      x -= 10;
    } else {
      x = 0;
    }
  }
}
""";
    testSource(code);
  }

  @Test
  void scfIfWithNestedClassLookup() {
    String code =
"""
public class %ClassName {
  public static void main() {
    int x = 5;
    if (x > 0) {
      x += NestedClass1.add(5, 10);
    } else if (x < 0) {
      x -= NestedClass2.add(5, 10);
    } else {
      x = 0;
    }
  }

  private static class NestedClass1 {
    private static int add(int a, int b) {
      return a + b;
    }
  }

  public static class NestedClass2 {
    public static int add(int a, int b) {
      return a + b;
    }
  }
}
""";
    testSource(code);
  }

  @Test
  void scfWhileWithBreak() {
    String code =
"""
import Dungeon.IO;

public class %ClassName {
  public static void main() {
    int x = 0;
    while (x < 10) {
      x++;
      if (x == 5) {
        break;
      }
      IO.print("x is " + x + "\\n");
      IO.print("This should be in the same skip guard after lowering.\\n");
    }
    assert x == 5 : "Expected x to be 5, but got " + x;
  }
}
""";
    testSource(code);
  }

  @Test
  void scfForWithContinueAndBreak() {
    String code =
"""
public class %ClassName {
  public static void main() {
    int x = 0;
    for (int i = 0; i < 10; i++) {
      if (i % 2 == 0) {
        if (i * 2 == 8) {
          break;
        }
        continue;
      }
      x += i;
      if (x > 10) {
        break;
      }
    }
    assert x == 3 : "Expected x to be 3, but got " + x;
  }
}
""";
    testSource(code);
  }

  @Test
  void infiniteWhileLoop() {
    String code =
"""
import Dungeon.IO;

public class %ClassName {
  public static void main() {
    int i = 0;
    while (true) {
      i++;
      break;
    }
    while (true) {
      if (i++ == 3)
        break;
      // Infinite loop
      assert i <= 3 : "Break statement did not work, expected i to be at most 3, but got " + i;
      IO.print("This will run forever, and this message will appear 2 times!\\n");
    }
    assert i == 4 : "Expected i to be 4, but got " + i;
  }
}
""";
    testSource(code);
  }

  @Test
  void infiniteForLoop() {
    String code =
"""
import Dungeon.IO;

public class %ClassName {
  public static void main() {
    String message = "This infinite loop never ran.";
    for(int i = 0;; ++i) {
      // Infinite loop
      if (i == 0){
        message = "This infinite loop will run forever!\\n";
      }else{
        message = "This infinites loop break statement does not work!\\n";
      }
      break;
    }
    IO.print(message);
  }
}
""";
    testSource(code);
  }

  @Test
  void returnFromInsideLoop() {
    String code =
"""
public class %ClassName {
  public static void main() {
    int x = 0;
    for (int i = 0; i < 10; i++) {
      if (i == 5) {
        return;
      }
      x += i;
    }
    assert false : "This should not be reachable, because the return statement should exit the method when i == 5. If this assertion fails, it means the return statement did not work as expected.";
  }
}
""";
    testSource(code);
  }

  @Test
  void binaryLogicalWithSideEffects() {
    String code =
"""
public class %ClassName {
  public static void main() {
    int x = 0;
    boolean result = (x++ > 0) && (x++ > 1) || (x++ > 2);
    assert x == 2 : "Expected x to be 2, but got " + x;
    assert result == false : "Expected result to be false, but got " + result;
  }
}
""";
    testSource(code);
  }

  @Test
  void doLoop() {
    String code =
"""
public class %ClassName {
  public static void main() {
    int x = 0;
    do {
      x++;
    } while (x < 5);
    assert x == 5 : "Expected x to be 5, but got " + x;
  }
}
""";
    testSource(code);
  }

  @Test
  void doLoopWithBreak() {
    String code =
"""
public class %ClassName {
  public static void main() {
    int x = 0;
    do {
      break;
    } while (x < 5);
    assert x == 0 : "Expected x to be 0, but got " + x;
  }
}
""";
    testSource(code);
  }

  @Test
  void simpleSwitchStatement() {
    String code =
"""
public class %ClassName {
  public static void main() {
    int x = 2;
    String result = "";
    switch (x) {
      case 1:
        result = "One";
        break;
      case 2:
        result = "Two";
        break;
      case 3:
        result = "Three";
        break;
      default:
        result = "Other";
    }
    assert result.equals("Two") : "Expected result to be 'Two', but got " + result;
  }
}
""";
    testSource(code);
  }

  @Test
  void nestedSwitchStatement() {
    String code =
"""
public class %ClassName {
  public static void main() {
    int x = 2;
    int y = 3;
    String result = "";
    switch (x) {
      case 1:
        result = "One";
        break;
      case 2:
        switch (y) {
          case 3:
            result = "Two and Three";
            break;
          default:
            result = "Two and Other";
        }
        break;
      case 3:
        result = "Three";
        break;
      default:
        result = "Other";
    }
    assert result.equals("Two and Three") : "Expected result to be 'Two and Three', but got " + result;
  }
}
""";
    testSource(code);
  }

  @Test
  void switchExpressions() {
    String code =
"""
public class %ClassName {
  public static void main() {
    int x = 2;
    String result = switch (x) {
      case 1 -> "One";
      case 2 -> "Two";
      case 3 -> "Three";
      default -> "Other";
    };
    assert result.equals("Two") : "Expected result to be 'Two', but got " + result;
  }
}
""";
    testSource(code);
  }

  @Test
  void switchExpressionsWithNestedSwitch() {
    String code =
"""
public class %ClassName {
  public static void main() {
    int x = 2;
    int y = 3;
    String result = switch (x) {
      case 1 -> "One";
      case 2 -> switch (y) {
        case 3 -> "Two and Three";
        default -> "Two and Other";
      };
      case 3 -> "Three";
      default -> "Other";
    };
    assert result.equals("Two and Three") : "Expected result to be 'Two and Three', but got " + result;
  }
}
""";
    testSource(code);
  }
}
