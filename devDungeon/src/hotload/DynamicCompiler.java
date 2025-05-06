package hotload;

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

public class DynamicCompiler {

  public static Class<?> compileAndLoad(IPath sourcePath, String className) throws Exception {
    // Temporäre Datei erstellen
    File outputRoot = new File("build/hotload");
    File sourceFile = new File(outputRoot, className.replace('.', '/') + ".java");
    // Verzeichnisstruktur für die Datei sicherstellen
    sourceFile.getParentFile().mkdirs();

    // Pfad zur Datei
    Path filePath = Paths.get(sourcePath.pathString());
    String sourceCode = Files.readString(filePath);

    System.out.println("Lese Quellcode von: " + filePath);

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(sourceFile))) {
      writer.write(sourceCode);
    }

    // Kompilierung des Quellcodes
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

    Iterable<? extends JavaFileObject> compilationUnits =
        fileManager.getJavaFileObjects(sourceFile);
    JavaCompiler.CompilationTask task =
        compiler.getTask(null, fileManager, null, null, null, compilationUnits);

    // Kompilieren
    boolean success = task.call();
    if (!success) {
      throw new Exception("Compilation failed.");
    }

    // URLClassLoader verwenden
    MyClassLoader classLoader = new MyClassLoader(new URL[] {outputRoot.toURI().toURL()});

    // Klasse laden
    Class<?> loadedClass = classLoader.loadClass(className);

    sourceFile.delete();

    return loadedClass;
  }

  public static Object loadUserInstance(
      IPath sourcePath, String className, Tuple<Class<?>, Object>... args) throws Exception {
    Class<?> newClass = DynamicCompiler.compileAndLoad(sourcePath, className);
    Class<?>[] paramTypes;
    Object[] paramValues;
    if (args == null || args.length == 0) {
      paramTypes = new Class<?>[0];
      paramValues = new Object[0];
    } else {
      paramTypes = Arrays.stream(args).map(Tuple::a).toArray(Class[]::new);

      paramValues = Arrays.stream(args).map(Tuple::b).toArray();
    }

    Constructor<?> ctor = newClass.getConstructor(paramTypes);
    System.out.println("Erstelle Instanz der Klasse: " + newClass.getName());
    return ctor.newInstance(paramValues);
  }
}
