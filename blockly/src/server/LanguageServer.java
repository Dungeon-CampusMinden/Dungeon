package server;

import coderunner.BlocklyCommands;
import coderunner.HideLanguage;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import core.level.utils.LevelElement;
import core.utils.Direction;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This Class contains utility methods to generate completion items for the blockly language server.
 * It uses reflection to inspect the methods of the classes and generate JSON objects that can be
 * later used by the IDE for code completion.
 *
 * <p>If you use a jar file, the source files must be copied to the jar file. The source files must
 * be in the same package as the class files.
 */
public class LanguageServer {

  private static final Logger LOGGER = Logger.getLogger(LanguageServer.class.getName());
  private static final JavaParser javaParser = new JavaParser();
  private static final Map<String, Class<?>> classMap =
      Map.of(
          "hero",
          BlocklyCommands.class,
          "Direction",
          Direction.class,
          "LevelElement",
          LevelElement.class);
  private static final String[] SOURCE_PATHS = {
    "./src/", "blockly/src/", "dungeon/src/", "../src/", "../dungeon/src/"
  };

  /**
   * Generates completion items for the given class or enum.
   *
   * @param objectName the name of the class or enum
   * @return a JSON array of completion items, or an empty array if the class is not found
   */
  public static String GenerateCompletionItems(String objectName) {
    Class<?> clazz = classMap.get(objectName);
    if (clazz == null) {
      LOGGER.warning("Class not found for object name: " + objectName);
      return "[]";
    }
    return clazz.isEnum() ? GenerateEnumItems(clazz) : GenerateClassItems(clazz);
  }

  /**
   * Generates completion items for the given class.
   *
   * @param clazz the class to generate completion items for
   * @return a JSON array of completion items
   */
  public static String GenerateClassItems(Class<?> clazz) {
    StringBuilder json = new StringBuilder("[");
    Map<String, MethodJavadoc> docs = getJavadocForClass(clazz).methodDocs;

    boolean first = true;
    for (Method method : clazz.getDeclaredMethods()) {
      if (Modifier.isPublic(method.getModifiers())
          && !method.isAnnotationPresent(HideLanguage.class)) {
        if (!first) json.append(",");
        first = false;

        MethodJavadoc doc = docs.get(method.getName() + method.getParameterCount());
        String[] paramNames =
            doc == null
                ? getDefaultParamNames(method)
                : doc.parameters.keySet().toArray(new String[0]);
        String docText =
            doc == null
                ? escapeJson(getMethodSignature(method, paramNames))
                : escapeJson(doc.toMarkdown());

        json.append("\n  {")
            .append("\n    \"label\": \"")
            .append(escapeJson(method.getName()))
            .append("\",")
            .append("\n    \"kind\": 2,")
            .append("\n    \"detail\": \"")
            .append(escapeJson(getMethodDetailSignature(method, paramNames)))
            .append("\",")
            .append("\n    \"documentation\": \"")
            .append(docText)
            .append("\",")
            .append("\n    \"parameters\": ")
            .append(getParameterNamesJson(paramNames))
            .append(",")
            .append("\n    \"insertText\": \"")
            .append(escapeJson(getMethodSignature(method, paramNames)))
            .append("\",")
            .append("\n    \"insertTextFormat\": 2")
            .append("\n  }");
      }
    }
    return json.append("\n]").toString();
  }

  /**
   * Generates completion items for the given enum.
   *
   * @param clazz the enum class
   * @return a JSON array of completion items
   */
  public static String GenerateEnumItems(Class<?> clazz) {
    StringBuilder json = new StringBuilder("[");
    Map<String, MethodJavadoc> docs = getJavadocForClass(clazz).methodDocs;

    boolean first = true;
    for (Object constant : clazz.getEnumConstants()) {
      if (!first) json.append(",");
      first = false;

      String name = constant.toString();
      MethodJavadoc doc = docs.get(name);
      String docText = doc == null ? escapeJson(name) : escapeJson(doc.toMarkdown());

      json.append("\n  {")
          .append("\n    \"label\": \"")
          .append(escapeJson(name))
          .append("\",")
          .append("\n    \"kind\": 2,")
          .append("\n    \"detail\": \"")
          .append(escapeJson(clazz.getSimpleName() + "." + name))
          .append("\",")
          .append("\n    \"documentation\": \"")
          .append(docText)
          .append("\",")
          .append("\n    \"insertText\": \"")
          .append(escapeJson(name))
          .append("\"")
          .append("\n  }");
    }
    return json.append("\n]").toString();
  }

  private static String[] getDefaultParamNames(Method method) {
    return Arrays.stream(method.getParameterTypes())
        .map(Class::getSimpleName)
        .toArray(String[]::new);
  }

  private static String getParameterNamesJson(String[] parameterNames) {
    return Arrays.stream(parameterNames)
        .map(LanguageServer::escapeJson)
        .map(s -> "\"" + s + "\"")
        .collect(Collectors.joining(", ", "[", "]"));
  }

  private static String getMethodSignature(Method method, String[] parameterNames) {
    if (parameterNames.length == 0) {
      return method.getName() + "()";
    }

    StringBuilder sb = new StringBuilder();
    sb.append(method.getName()).append("(");

    for (int i = 0; i < parameterNames.length; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append("${").append(i + 1).append(":").append(parameterNames[i]).append("}");
    }

    sb.append(")");
    return sb.toString();
  }

  private static String escapeJson(String input) {
    return input
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t");
  }

  private static String getMethodDetailSignature(Method method, String[] parameters) {
    StringBuilder signature = new StringBuilder();
    signature
        .append(method.getReturnType().getSimpleName())
        .append(" ")
        .append(method.getName())
        .append("(");

    Class<?>[] params = method.getParameterTypes();

    if (params.length != parameters.length) {
      LOGGER.warning("Wrong number of parameters for method: " + method.getName());
      LOGGER.warning(
          "Expected: " + Arrays.toString(params) + ", Found: " + Arrays.toString(parameters));
      return method.getName() + "(?)";
    }

    for (int i = 0; i < params.length; i++) {
      if (i > 0) signature.append(", ");
      signature.append(params[i].getSimpleName()).append(" ").append(parameters[i]);
    }
    signature.append(")");
    return signature.toString();
  }

  private static ClassDocs getJavadocForClass(Class<?> clazz) {
    String classPath =
        clazz.getPackage().getName().replace('.', '/') + "/" + clazz.getSimpleName() + ".java";
    ClassDocs classDocs = new ClassDocs();

    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(getClassFileInputStream(classPath)))) {
      String fileContent = reader.lines().collect(Collectors.joining("\n"));
      javaParser
          .parse(fileContent)
          .getResult()
          .ifPresent(
              cu -> {
                cu.findAll(MethodDeclaration.class)
                    .forEach(
                        method ->
                            method
                                .getComment()
                                .ifPresent(
                                    comment ->
                                        classDocs.methodDocs.put(
                                            method.getNameAsString()
                                                + method.getParameters().size(),
                                            parseJavadoc(comment.getContent()))));
                cu.findAll(EnumConstantDeclaration.class)
                    .forEach(
                        enumConstant ->
                            enumConstant
                                .getComment()
                                .ifPresent(
                                    comment ->
                                        classDocs.methodDocs.put(
                                            enumConstant.getNameAsString(),
                                            parseJavadoc(comment.getContent()))));
              });
    } catch (IOException e) {
      LOGGER.warning("Error reading class file: " + e.getMessage());
    } catch (Exception e) {
      LOGGER.warning("Error parsing class file: " + e.getMessage());
    }
    return classDocs;
  }

  private static MethodJavadoc parseJavadoc(String javadocContent) {
    String[] lines = javadocContent.split("\n");
    StringBuilder mainText = new StringBuilder();
    Map<String, String> parameters = new HashMap<>();
    String returnDoc = "";
    Map<String, String> throwDoc = new HashMap<>();

    String currentTag = "";
    String currentTagName = "";

    for (String line : lines) {
      line = line.trim().replaceFirst("^\\s*\\*\\s*", "");
      if (line.isEmpty()) continue;

      if (line.startsWith("@param")) {
        String[] parts = line.substring(6).trim().split("\\s+", 2);
        currentTag = "@param";
        currentTagName = parts[0];
        parameters.put(currentTagName, parts.length > 1 ? parts[1] : "");
      } else if (line.startsWith("@return")) {
        currentTag = "@return";
        returnDoc = line.substring(7).trim();
      } else if (line.startsWith("@throws")) {
        String[] parts = line.substring(7).trim().split("\\s+", 2);
        currentTag = "@throws";
        currentTagName = parts[0];
        throwDoc.put(currentTagName, parts.length > 1 ? parts[1] : "");
      } else if (line.startsWith("@")) {
        currentTag = "";
      } else {
        switch (currentTag) {
          case "@param" -> parameters.merge(currentTagName, " " + line, String::concat);
          case "@return" -> returnDoc += " " + line;
          case "@throws" -> throwDoc.merge(currentTagName, " " + line, String::concat);
          default -> mainText.append(line).append(" ");
        }
      }
    }
    return new MethodJavadoc(mainText.toString().trim(), parameters, returnDoc.trim(), throwDoc);
  }

  private record ClassDocs(Map<String, MethodJavadoc> methodDocs) {
    ClassDocs() {
      this(new HashMap<>());
    }
  }

  private record MethodJavadoc(
      String mainText,
      Map<String, String> parameters,
      String returnDoc,
      Map<String, String> throwDoc) {

    /**
     * Converts the Javadoc to a Markdown format.
     *
     * <p>This method formats the main text, parameters, return documentation, and throw
     * documentation into a Markdown string.
     *
     * @return the formatted Markdown string
     */
    public String toMarkdown() {
      StringBuilder sb = new StringBuilder(mainText).append("\n\n");
      if (!parameters.isEmpty()) {
        sb.append("### Parameters:\n");
        parameters.forEach(
            (key, value) ->
                sb.append("- **").append(key).append(":** ").append(value).append("\n"));
      }
      if (!returnDoc.isEmpty()) sb.append("### Returns:\n").append(returnDoc).append("\n");
      if (!throwDoc.isEmpty()) {
        sb.append("### Throws:\n");
        throwDoc.forEach(
            (key, value) ->
                sb.append("- **").append(key).append(":** ").append(value).append("\n"));
      }
      return sb.toString()
          .replaceAll("\\{@code\\s*([^}]+)}", "`$1`")
          .replaceAll("\\{@link\\s*([^}]+)}", "$1");
    }
  }

  private static InputStream getClassFileInputStream(String classPath) throws IOException {
    InputStream inputStream = LanguageServer.class.getClassLoader().getResourceAsStream(classPath);
    if (inputStream != null) return inputStream;

    String jarPath =
        LanguageServer.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    if (jarPath.endsWith(".jar")) {
      try (java.util.jar.JarFile jarFile = new java.util.jar.JarFile(jarPath)) {
        java.util.jar.JarEntry entry = jarFile.getJarEntry(classPath);
        if (entry != null) return jarFile.getInputStream(entry);
      }
    }

    for (String sourcePath : SOURCE_PATHS) {
      Path path = Paths.get(sourcePath, classPath);
      if (Files.exists(path)) return Files.newInputStream(path);
    }

    throw new IOException("Could not find source file: " + classPath);
  }
}
