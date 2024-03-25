package level.compiler;

import core.Entity;
import core.components.DrawComponent;
import core.utils.components.path.SimpleIPath;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import level.rooms.Room;

public class DojoCompiler {
  public record TestResult(String testName, boolean passed, List<String> messages) {}

  private final String fileName;
  private final String className;

  public DojoCompiler(String fileName, String className) {
    this.fileName = fileName;
    this.className = className;
  }

  public boolean spawnMonsterToOpenTheDoor(Room currentRoom) throws Exception {
    String source = getSource();

    Class<?> cls = compile(source);
    System.out.println("cls = " + cls);

    Method method = cls.getMethod("spawnMonster", DrawComponent.class, int.class, float.class);
    System.out.println("method = " + method);

    Object instance = cls.getConstructor(Room.class).newInstance(currentRoom);
    System.out.println("instance = " + instance);

    Entity entity =
        (Entity)
            method.invoke(
                instance, new DrawComponent(new SimpleIPath("character/blue_knight")), 10, 10.0f);
    System.out.println("entity = " + entity);

    currentRoom.addEntityImmediately(entity);

    return true;
  }

  public TestResult test1() {
    String testName = "test1";
    try {
      return test0(testName, getSource());
    } catch (IOException e) {
      return new TestResult(testName, false, List.of(e.getMessage()));
    }
  }

  public TestResult test2() {
    String testName = "test2";
    try {
      return test0(testName, getSource().replace("10", "haha"));
    } catch (IOException e) {
      return new TestResult(testName, false, List.of(e.getMessage()));
    }
  }

  public TestResult test3() {
    String testName = "test3";
    try {
      return test0_2(testName, getSource().replace("10", "haha"));
    } catch (IOException e) {
      return new TestResult(testName, false, List.of(e.getMessage()));
    }
  }

  private TestResult test0(String testName, String source) {
    List<String> messages = new ArrayList<>();
    try {
      Class<?> cls = compile(source);
      messages.add("compile ok");

      // --- Test 1 ---
      boolean b1 = testMethod1(cls, messages);

      // --- Test 2 ---
      boolean b2 = testMethod2(cls, messages);

      if (b1 && b2) {
        // All tests passed! You're all set.
        return new TestResult(testName, true, messages);
      }

    } catch (Exception e) {
      messages.add(e.getMessage());
    }

    return new TestResult(testName, false, messages);
  }

  private TestResult test0_2(String testName, String source) {
    List<String> messages = new ArrayList<>();
    try {
      Class<?> cls = compile(source);
      messages.add("compile ok");

      // --- Test 1 ---
      boolean b1 = testMethod1(cls, messages);

      // --- Test 2 ---
      boolean b2 = testMethod2(cls, messages);

      // --- Test 3 ---
      Pattern pattern = Pattern.compile("try.+catch", Pattern.DOTALL);
      Matcher matcher = pattern.matcher(source);
      boolean b3 = matcher.find();
      if (b3) {
        messages.add("try-catch ok");
      } else {
        messages.add("try-catch wrong");
      }

      if (b1 && b2 && b3) {
        // All tests passed! You're all set.
        return new TestResult(testName, true, messages);
      }

    } catch (Exception e) {
      messages.add(e.getMessage());
    }

    return new TestResult(testName, false, messages);
  }

  public static boolean testMethod1(Class<?> clsToTest, List<String> messages) throws Exception {
    Method method1 = clsToTest.getDeclaredMethod("testExpectedOutput7", PrintWriter.class);
    messages.add("method1 ok");

    ByteArrayOutputStream buf1 = new ByteArrayOutputStream();
    try (PrintWriter writer = new PrintWriter(buf1, true)) {
      method1.invoke(null, writer);
      messages.add("invocation1 ok");
    }

    String actualOutput1 = buf1.toString(Charset.defaultCharset());
    if (actualOutput1 != null && actualOutput1.contains("Die Summe ist: 7")) {
      messages.add("output1 ok");
      return true;
    } else {
      messages.add("output1 wrong: " + actualOutput1);
    }

    return false;
  }

  public static boolean testMethod2(Class<?> clsToTest, List<String> messages) throws Exception {
    Method method2 = clsToTest.getDeclaredMethod("testExpectedOutput8", PrintWriter.class);
    messages.add("method2 ok");

    ByteArrayOutputStream buf2 = new ByteArrayOutputStream();
    try (PrintWriter writer = new PrintWriter(buf2, true)) {
      method2.invoke(null, writer);
      messages.add("invocation2 ok");
    }

    String actualOutput2 = buf2.toString(Charset.defaultCharset());
    if (actualOutput2 != null && actualOutput2.contains("Die dritte Zahl ist: 8")) {
      messages.add("output2 ok");
      return true;
    } else {
      messages.add("output2 wrong: " + actualOutput2);
    }

    return false;
  }

  private String getSource() throws IOException {
    return Files.readString(Paths.get(fileName));
  }

  private Class<?> compile(String source) throws Exception {
    // Save source in .java file.
    File root = Files.createTempDirectory("java").toFile();
    File sourceFile = new File(root, className + ".java");
    assert sourceFile.getParentFile().mkdirs();
    Files.writeString(sourceFile.toPath(), source);

    // Compile source file.
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    compiler.run(null, null, null, sourceFile.getPath());

    // Load and instantiate compiled class.
    URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] {root.toURI().toURL()});

    return Class.forName(className, true, classLoader);
  }
}
