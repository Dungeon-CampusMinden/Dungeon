package transformations;

import org.junit.jupiter.api.Test;

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
}
