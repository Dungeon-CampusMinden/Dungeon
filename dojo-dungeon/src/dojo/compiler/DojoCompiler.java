package dojo.compiler;

import core.Entity;
import core.components.DrawComponent;
import core.utils.components.path.SimpleIPath;
import dojo.rooms.Room;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/** Class for compiling and testing sources at runtime. */
public class DojoCompiler {
  /**
   * Result of the tests.
   *
   * @param testName name of the tests
   * @param passed if all tests passed
   * @param messages list of messages during the tests
   */
  public record TestResult(String testName, boolean passed, List<String> messages) {}

  private final List<String> messages = new ArrayList<>();
  private String source;
  private Class<?> cls;
  private Method method1;
  private Method method2;

  /**
   * Tries to spawn a monster at runtime.
   *
   * @param fileName class file name to compile
   * @param className class name to compile
   * @param currentRoom the current room
   * @return a {@link TestResult} if the tests passed
   */
  public TestResult spawnMonsterToOpenTheDoor(String fileName, String className, Room currentRoom) {
    String testName = "spawnMonster";

    if (!stage_1_readSourceFile(fileName) || !stage_2_checkCompilation(className)) {
      return new TestResult(testName, false, messages);
    }

    Method method;
    try {
      method = cls.getMethod("spawnMonster", DrawComponent.class, int.class, float.class);
    } catch (NoSuchMethodException e) {
      messages.add("method not found");
      return new TestResult(testName, false, messages);
    }
    messages.add("method ok");

    Object instance;
    try {
      instance = cls.getConstructor(Room.class).newInstance(currentRoom);
    } catch (InstantiationException
        | NoSuchMethodException
        | InvocationTargetException
        | IllegalAccessException e) {
      messages.add("instance not found");
      return new TestResult(testName, false, messages);
    }
    messages.add("instance ok");

    Entity entity;
    try {
      entity =
          (Entity)
              method.invoke(
                  instance,
                  new DrawComponent(new SimpleIPath("character/monster/pumpkin_dude")),
                  10,
                  10.0f);
    } catch (IllegalAccessException | IOException | InvocationTargetException e) {
      messages.add("entity not found");
      return new TestResult(testName, false, messages);
    }
    messages.add("entity ok");

    currentRoom.addEntityImmediately(entity);

    // All ok.
    return new TestResult(testName, true, messages);
  }

  /**
   * Tests if the class is correct by certain criteria, step 1.
   *
   * <p>Checks if the class can be compiled and the methods are declared correctly.
   *
   * @param fileName the name of the source file
   * @param className the name of the class
   * @return a {@link TestResult} if the tests passed
   */
  public TestResult testWrongClass1_compilationAndInvocation(String fileName, String className) {
    String testName = "test1";
    if (stage_1_readSourceFile(fileName)
        && stage_2_checkCompilation(className)
        && stage_3_checkFirstMethodDeclaration()
        && stage_5_checkSecondMethodDeclaration()) {
      return new TestResult(testName, true, messages);
    }
    return new TestResult(testName, false, messages);
  }

  /**
   * Tests if the class is correct by certain criteria, step 2.
   *
   * <p>Checks if the class can be compiled and the methods are declared correctly.
   *
   * <p>Checks the methods output with valid input values.
   *
   * @param fileName the name of the source file
   * @param className the name of the class
   * @return a {@link TestResult} if the tests passed
   */
  public TestResult testWrongClass2_validInputValues(String fileName, String className) {
    String testName = "test2";
    if (stage_1_readSourceFile(fileName)
        && stage_2_checkCompilation(className)
        && stage_3_checkFirstMethodDeclaration()
        && stage_4_checkFirstOutput()
        && stage_5_checkSecondMethodDeclaration()
        && stage_6_checkSecondOutput()) {
      return new TestResult(testName, true, messages);
    }
    return new TestResult(testName, false, messages);
  }

  /**
   * Tests if the class is correct by certain criteria, step 3.
   *
   * <p>Checks if the class can be compiled and the methods are declared correctly.
   *
   * <p>Checks the methods output with valid input values.
   *
   * <p>Checks the methods output with invalid input values.
   *
   * @param fileName the name of the source file
   * @param className the name of the class
   * @return a {@link TestResult} if the tests passed
   */
  public TestResult testWrongClass3_invalidInputValues(String fileName, String className) {
    String testName = "test3";
    if (stage_1_readSourceFile(fileName)
        && stage_2_checkCompilation(className)
        && stage_3_checkFirstMethodDeclaration()
        && stage_4_checkFirstOutput()
        && stage_4_2_checkFirstOutput()
        && stage_5_checkSecondMethodDeclaration()
        && stage_6_checkSecondOutput()) {
      return new TestResult(testName, true, messages);
    }
    return new TestResult(testName, false, messages);
  }

  /**
   * Tests if a mathematical class is correct.
   *
   * <p>Tests if the methods calculateArea, calculatePerimeter and calculateVolume are correct, e.g.
   * returning the correct values.
   *
   * @param fileName the name of the source file
   * @param className the name of the class
   * @return a {@link TestResult} if the tests passed
   */
  public TestResult testMathematicalClass(String fileName, String className) {
    try {
      Class<?> cls2 = compile(getSource(fileName), className);
      Constructor<?> tor2 = cls2.getConstructor(float.class, float.class, float.class);
      Object inst2 = tor2.newInstance(10.0f, 30.0f, 20.0f);
      Method m1 = cls2.getMethod("calculateArea");
      Method m2 = cls2.getMethod("calculatePerimeter");
      Method m3 = cls2.getMethod("calculateVolume");
      float f1 = (float) m1.invoke(inst2);
      float f2 = (float) m2.invoke(inst2);
      float f3 = (float) m3.invoke(inst2);
      if (Math.round(f1) != 2200 || Math.round(f2) != 240 || Math.round(f3) != 6000) {
        throw new NoSuchElementException("wrong values ...");
      }
      messages.add("testMaths ok");
      return new TestResult("testMaths", true, messages);
    } catch (Exception ex) {
      messages.add("testMaths not ok");
      messages.add(ex.getMessage());
    }
    return new TestResult("testMaths", false, messages);
  }

  private boolean stage_1_readSourceFile(String fileName) {
    try {
      source = getSource(fileName);
    } catch (IOException ex) {
      messages.add("source not ok");
      return false;
    }
    if (source == null || source.isEmpty()) {
      messages.add("empty source");
      return false;
    }
    messages.add("source ok");
    return true;
  }

  private boolean stage_2_checkCompilation(String className) {
    try {
      cls = compile(source, className);
    } catch (Exception ex) {
      messages.add("compile not ok");
      return false;
    }
    messages.add("compile ok");
    return true;
  }

  private boolean stage_3_checkFirstMethodDeclaration() {
    try {
      method1 = cls.getDeclaredMethod("incrementByTwo", String.class);
    } catch (NoSuchMethodException ex) {
      messages.add("method1 not ok");
      return false;
    }
    messages.add("method1 ok");
    return true;
  }

  private boolean stage_4_checkFirstOutput() {
    try {
      for (int i = -10; i <= 10; i++) {
        String result = (String) method1.invoke(null, String.valueOf(i));
        if (!String.valueOf(i + 2).equals(result)) {
          messages.add("output1 wrong: " + result);
          return false;
        }
      }
      if (!String.valueOf(Integer.MAX_VALUE)
          .equals(method1.invoke(null, String.valueOf(Integer.MAX_VALUE - 2)))) {
        messages.add("output1 wrong");
        return false;
      }
      int r = new Random().nextInt(1000);
      if (!String.valueOf(r + 2).equals(method1.invoke(null, String.valueOf(r)))) {
        messages.add("output1 wrong");
        return false;
      }
    } catch (Exception ex) {
      messages.add("invocation1 not ok");
      return false;
    }
    messages.add("output1 ok");
    return true;
  }

  private boolean stage_4_2_checkFirstOutput() {
    try {
      if (!"nan".equalsIgnoreCase((String) method1.invoke(null, (String) null))) {
        messages.add("output1 wrong");
        return false;
      }
      if (!"nan".equalsIgnoreCase((String) method1.invoke(null, "coffee time"))) {
        messages.add("output1 wrong");
        return false;
      }
      if (!"nan".equalsIgnoreCase((String) method1.invoke(null, ""))) {
        messages.add("output1 wrong");
        return false;
      }
      if (!"integer overflow"
          .equalsIgnoreCase((String) method1.invoke(null, String.valueOf(Integer.MAX_VALUE - 1)))) {
        messages.add("output1 wrong");
        return false;
      }
      if (!"integer overflow"
          .equalsIgnoreCase((String) method1.invoke(null, String.valueOf(Integer.MAX_VALUE)))) {
        messages.add("output1 wrong");
        return false;
      }
    } catch (Exception ex) {
      messages.add("invocation1 not ok");
      return false;
    }
    messages.add("output1 ok");
    return true;
  }

  private boolean stage_5_checkSecondMethodDeclaration() {
    try {
      method2 = cls.getDeclaredMethod("getSum");
    } catch (NoSuchMethodException ex) {
      messages.add("method2 not ok");
      return false;
    }
    messages.add("method2 ok");
    return true;
  }

  private boolean stage_6_checkSecondOutput() {
    try {
      String sum = (String) method2.invoke(null);
      if (!"7".equals(sum)) {
        messages.add("output2 wrong: 7 expected");
        return false;
      }
    } catch (Exception ex) {
      messages.add("invocation2 not ok");
      return false;
    }
    messages.add("output2 ok");
    return true;
  }

  private String getSource(String fn) throws IOException {
    return Files.readString(Paths.get(fn));
  }

  private Class<?> compile(String source, String className) throws Exception {
    // Save source text to temporary file
    File root = Files.createTempDirectory("java").toFile();
    File sourceFile = new File(root, className + ".java");
    assert sourceFile.getParentFile().mkdirs();
    Files.writeString(sourceFile.toPath(), source);

    // Compile source file
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    compiler.run(null, null, null, sourceFile.getPath());

    // Load compiled class
    URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] {root.toURI().toURL()});

    return Class.forName(className, true, classLoader);
  }
}
