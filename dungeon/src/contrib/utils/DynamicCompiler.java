package contrib.utils;

import core.utils.Tuple;
import core.utils.components.path.IPath;
import java.io.*;
import java.lang.reflect.Constructor;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.tools.*;

/**
 * A utility for compiling and dynamically loading Java classes from source files at runtime.
 *
 * <p>This is particularly useful in coding riddles, where users submit partial code or
 * implementations that need to be compiled and executed without restarting the game.
 *
 * <h2>Key Requirements and Design Considerations</h2>
 *
 * <ul>
 *   <li><strong>Correct Class Name:</strong> The provided class name <em>must</em> exactly match
 *       the package and class declaration inside the target source file. For example, if the class
 *       is declared as {@code package my.riddle; public class Answer { ... }}, then the input must
 *       be {@code "my.riddle.Answer"}.
 *   <li><strong>Isolation via Custom ClassLoader:</strong> A custom class loader is used to load
 *       freshly compiled classes from disk instead of reusing classes already loaded by the JVM.
 *       This is essential for scenarios where the same class name may be compiled multiple times
 *       with different contents (e.g., repeated puzzle attempts).
 *   <li><strong>Classes must not be part of the core project:</strong> If the class to be loaded
 *       dynamically is also part of the main project (i.e., already compiled and present on the
 *       default classpath), the system class loader will load it first, and our custom loader will
 *       be unable to override it. Therefore:
 *       <ul>
 *         <li>The class may exist as a source file in the project, but must not be directly
 *             referenced or compiled with the project. <br>
 *             <em>Rule of thumb:</em> The project must still compile and run without modification
 *             if you delete the target file.
 *         <li><strong>Relying on Abstractions:</strong> The main application should depend on
 *             abstractions (such as interfaces, abstract classes, or {@code Supplier<?>}) to
 *             support injecting implementations that are loaded dynamically at runtime. <br>
 *             <br>
 *             <em>Example:</em> Imagine a game where item effects can be coded by the user. The
 *             {@code Item} class should not hardcode any effect logic. Instead, it could hold a
 *             reference to a {@code Supplier<ItemEffect>} or implement an {@code ItemEffect}
 *             interface. The Java source file submitted by the user would then implement this
 *             interface or provide a valid {@code get()} method if using a supplier. The part of
 *             the code that triggers the dynamic loading (e.g., a riddle evaluator or configuration
 *             loader) would use {@code DynamicCompiler.compileAndLoad} to load the class and call a
 *             setter on the item (e.g., {@code item.setEffect(...)}) to inject the behavior
 *             dynamically. This pattern allows the main application to remain stable, while
 *             enabling highly customizable runtime extensions.
 *       </ul>
 * </ul>
 */
public class DynamicCompiler {
  /**
   * Compiles and loads a Java class from the specified source file.
   *
   * @param sourcePath Path to the source file containing the class.
   * @param className Fully qualified class name (must match declaration inside file).
   * @return The compiled and loaded {@link Class} object.
   * @throws Exception If compilation or loading fails.
   */
  public static Class<?> compileAndLoad(IPath sourcePath, String className) throws Exception {
    File outputRoot = new File(System.getProperty("BASEREFLECTIONDIR"));
    File outputFile = new File(outputRoot, className.replace('.', '/') + ".java");
    outputFile.getParentFile().mkdirs();

    Path filePath = Paths.get(sourcePath.pathString());
    String sourceCode = Files.readString(filePath);

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
      writer.write(sourceCode);
    }

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

    Iterable<? extends JavaFileObject> compilationUnits =
        fileManager.getJavaFileObjects(outputFile);
    JavaCompiler.CompilationTask task =
        compiler.getTask(null, fileManager, null, null, null, compilationUnits);

    boolean success = task.call();
    if (!success) {
      throw new Exception("Compilation failed.");
    }

    // Load compiled class using custom loader to override any previous versions
    MyClassLoader classLoader = new MyClassLoader(new URL[] {outputRoot.toURI().toURL()});
    Class<?> loadedClass = classLoader.loadClass(className);
    outputFile.delete();

    return loadedClass;
  }

  /**
   * Dynamically instantiates a class from a source file using an optional set of constructor
   * arguments.
   *
   * <p>This method compiles the provided Java source file, loads the resulting class using a custom
   * class loader, and creates a new instance using the specified constructor arguments.
   *
   * @param sourcePath Path to the Java source file.
   * @param className Fully qualified name of the class to load (including package name). This must
   *     exactly match the package and class declaration inside the file.
   * @param args Optional constructor arguments as {@code Tuple<Class<?>, Object>}: each tuple
   *     specifies the expected parameter type and the value to pass. The order of the arguments
   *     must match the parameter order in the target constructor.
   * @return A new instance of the loaded and instantiated class.
   * @throws Exception If compilation, class loading, or instantiation fails.
   *     <strong>Important:</strong> The target class must contain a constructor that exactly
   *     matches the parameter types and order given in {@code args}. Otherwise, a {@code
   *     NoSuchMethodException} will be thrown.
   */
  @SafeVarargs
  public static Object loadUserInstance(
      IPath sourcePath, String className, Tuple<Class<?>, Object>... args) throws Exception {
    if (args == null) throw new IllegalArgumentException("Args can not be null");

    Class<?> newClass = DynamicCompiler.compileAndLoad(sourcePath, className);
    Class<?>[] paramTypes = Arrays.stream(args).map(Tuple::a).toArray(Class[]::new);
    Object[] paramValues = Arrays.stream(args).map(Tuple::b).toArray();

    Constructor<?> ctor = newClass.getConstructor(paramTypes);
    return ctor.newInstance(paramValues);
  }

  /**
   * A custom class loader that prioritizes loading from a local compiled directory.
   *
   * <p>Tries to find and load a class from the given path before falling back to the system class
   * loader. This ensures that dynamically compiled classes can override existing ones, which the
   * standard class loader wouldn't allow.
   */
  private static class MyClassLoader extends URLClassLoader {

    private MyClassLoader(URL[] urls) {
      super(urls, ClassLoader.getSystemClassLoader());
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
      try {
        return findClass(name); // Try dynamic class first
      } catch (ClassNotFoundException e) {
        return super.loadClass(name); // Fallback to default classpath
      }
    }
  }
}
