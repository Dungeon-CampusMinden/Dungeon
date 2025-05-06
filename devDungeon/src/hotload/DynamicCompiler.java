package hotload;

import java.io.*;
import java.net.*;
import javax.tools.*;

public class DynamicCompiler {

  public static Class<?> compileAndLoad(String className, String sourceCode) throws Exception {
    // Temporäre Datei erstellen
    File tempDir = new File(System.getProperty("java.io.tmpdir"));
    File sourceFile = new File(tempDir, className.replace('.', '/') + ".java");

    // Quelle in eine Datei schreiben
    sourceFile.getParentFile().mkdirs(); // Verzeichnisse erstellen
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
    MyClassLoader classLoader = new MyClassLoader(new URL[] {tempDir.toURI().toURL()});

    // Klasse laden
    Class<?> loadedClass = classLoader.loadClass(className);

    // Optionale Bereinigung: temporäre Datei löschen
    sourceFile.delete();

    return loadedClass;
  }
}
