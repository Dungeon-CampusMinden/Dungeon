package tasks;

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
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class DojoCompiler {
  public record TestResult(String testName, boolean passed, List<String> messages) {}

  private final String fileName;
  private final String className;

  public DojoCompiler(String fileName, String className) {
    this.fileName = fileName;
    this.className = className;
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
      return test0(testName, getSource().replace("\"7\"", "\"" + Integer.MAX_VALUE + "999\""));
    } catch (IOException e) {
      return new TestResult(testName, false, List.of(e.getMessage()));
    }
  }

  private TestResult test0(String testName, String source) {
    List<String> messages = new ArrayList<>();
    try {
      Class<?> cls = compile(source);
      messages.add("compile ok");
      Method method = cls.getDeclaredMethod("main", String[].class, PrintWriter.class);
      messages.add("method ok");

      ByteArrayOutputStream buf = new ByteArrayOutputStream();
      try (PrintWriter writer = new PrintWriter(buf, true)) {
        method.invoke(null, new String[] {}, writer);
        messages.add("invocation ok");
      }

      String actualOutput = buf.toString(Charset.defaultCharset());
      System.out.println(actualOutput);

      if (actualOutput != null
          && actualOutput.contains("Die Summe ist: 7")
          && actualOutput.contains("Die dritte Zahl ist: 7")) {
        messages.add("output ok");
        return new TestResult(testName, true, messages);
      } else {
        messages.add("output wrong: " + actualOutput);
      }
    } catch (Exception e) {
      messages.add(e.getMessage());
    }
    return new TestResult(testName, false, messages);
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
