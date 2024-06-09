package dojo.compiler;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.*;

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

  private static final Logger LOGGER = Logger.getLogger(DojoCompiler.class.getName());

  static {
    LOGGER.addHandler(new ConsoleHandler());
    LOGGER.setLevel(Level.INFO);
  }

  private static final String ABSOLUTE_BUILD_PATH;

  static {
    String path = System.getProperty("dojoDungeonAbsBuildDir");
    if (path == null) {
      LOGGER.warning(
          "Path to build directory not set as system property 'dojoDungeonAbsBuildDir'!");
      throw new RuntimeException();
    }
    try {
      ABSOLUTE_BUILD_PATH = path.substring(1, path.length() - 1);
    } catch (IndexOutOfBoundsException e) {
      LOGGER.warning("Path to build directory ('dojoDungeonAbsBuildDir') is not valid!");
      throw new RuntimeException();
    }
    LOGGER.info("Using path to build directory: " + ABSOLUTE_BUILD_PATH);
  }

  private final List<String> messages = new ArrayList<>();
  private String pathToSourceFiles;
  private Class<?> cls;
  private Method method1;
  private Method method2;

  /**
   * Tests if the class is correct by certain criteria, step 1.
   *
   * <p>Checks if the class can be compiled and the methods are declared correctly.
   *
   * @param pathToSourceFiles the path to the source file (this is the path to the file to be
   *     compiled)
   * @param className the name of the class
   * @return a {@link TestResult} if the tests passed
   */
  public TestResult testWrongClass1_compilationAndInvocation(
      String pathToSourceFiles, String className) {
    String testName = "test1";
    if (stage_1_setPathToSourceFiles(pathToSourceFiles)
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
   * @param pathToSourceFiles the path to the source file (this is the path to the file to be
   *     compiled)
   * @param className the name of the class
   * @return a {@link TestResult} if the tests passed
   */
  public TestResult testWrongClass2_validInputValues(String pathToSourceFiles, String className) {
    String testName = "test2";
    if (stage_1_setPathToSourceFiles(pathToSourceFiles)
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
   * @param pathToSourceFiles the path to the source file (this is the path to the file to be
   *     compiled)
   * @param className the name of the class
   * @return a {@link TestResult} if the tests passed
   */
  public TestResult testWrongClass3_invalidInputValues(String pathToSourceFiles, String className) {
    String testName = "test3";
    if (stage_1_setPathToSourceFiles(pathToSourceFiles)
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
   * @param pathToSourceFiles the path to the source file (this is the path to the file to be
   *     compiled)
   * @param className the name of the class
   * @return a {@link TestResult} if the tests passed
   */
  public TestResult testMathematicalClass(String pathToSourceFiles, String className) {
    try {
      Class<?> cls2 = compile(pathToSourceFiles, className);
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

  private boolean stage_1_setPathToSourceFiles(String pathToSourceFiles) {
    this.pathToSourceFiles = pathToSourceFiles;
    if (!new File(this.pathToSourceFiles).isDirectory()) {
      messages.add("source not ok");
      return false;
    }
    messages.add("source ok");
    return true;
  }

  private boolean stage_2_checkCompilation(String className) {
    try {
      cls = compile(pathToSourceFiles, className);
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

  private Class<?> compile(String pathToSourceFiles, String fqClassName) throws Exception {
    Class<?> cls = Class.forName(fqClassName);

    String argBuildDir = ABSOLUTE_BUILD_PATH;
    String argToCompile = Paths.get(pathToSourceFiles, cls.getSimpleName() + ".java").toString();
    URL argToLoad = Paths.get(argBuildDir).toUri().toURL();
    LOGGER.info(
        "Compiling: "
            + fqClassName
            + " in: "
            + argToCompile
            + " ("
            + argBuildDir
            + ") and load: "
            + argToLoad);

    // Compile source file
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    compiler.run(null, null, null, "-d", argBuildDir, argToCompile);

    // Load compiled class
    try (URLClassLoader reloadClassLoader =
        new URLClassLoader(new URL[] {argToLoad}, cls.getClassLoader().getParent())) {
      return reloadClassLoader.loadClass(fqClassName);
    }
  }

  /**
   * Compile and load an existing class that depends on other classes (imports them).
   *
   * @param pathToSourceFiles the path to the source file.
   * @param fqClassName the fully qualified name of the class.
   * @return the loaded class.
   * @throws Exception if compiling or loading fails.
   */
  public static Class<?> compileClassDependentOnOthers(String pathToSourceFiles, String fqClassName)
      throws Exception {
    Class<?> cls = Class.forName(fqClassName);

    String argBuildDir = ABSOLUTE_BUILD_PATH;
    String argToCompile = Paths.get(pathToSourceFiles, cls.getSimpleName() + ".java").toString();
    File fileToLoad =
        Paths.get(argBuildDir, pathToSourceFiles.substring(4), cls.getSimpleName() + ".class")
            .toFile();
    LOGGER.info(
        "Compiling: "
            + fqClassName
            + " in: "
            + argToCompile
            + " ("
            + argBuildDir
            + ") and load: "
            + fileToLoad);

    // Compile source file
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    compiler.run(null, null, null, "-d", argBuildDir, argToCompile);

    // Load compiled class
    ClassLoader reloadClassLoader =
        new ClassLoader() {
          @Override
          public Class<?> loadClass(String name) throws ClassNotFoundException {
            if (name.equals(fqClassName)) {
              try (BufferedInputStream bis =
                  new BufferedInputStream(new FileInputStream(fileToLoad))) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int b;
                while ((b = bis.read()) != -1) {
                  bos.write(b);
                }
                byte[] buf = bos.toByteArray();
                return defineClass(name, buf, 0, buf.length);
              } catch (IOException e) {
                LOGGER.warning("Could not load class: " + e.getMessage());
                throw new ClassNotFoundException("", e);
              }
            }
            return getParent().loadClass(name);
          }
        };
    return reloadClassLoader.loadClass(fqClassName);
  }
}
