package server;

import client.HeroCommands;
import client.LevelCommands;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * This Class contains utility methods to generate completion items for the blockly language server.
 * It uses reflection to inspect the methods of the classes and generate JSON objects that can be
 * later used by the IDE for code completion.
 */
public class LanguageServer {

  private static final Logger LOGGER = Logger.getLogger(LanguageServer.class.getName());
  private static final JavaParser javaParser = new JavaParser();
  private static final Map<String, Class<?>> classMap =
      Map.of("level", LevelCommands.class, "hero", HeroCommands.class);

  /**
   * Generates completion items for the given class.
   *
   * <p>This method returns a JSON array of completion items for the public methods of the given
   * class. This is used to provide code completion suggestions in the IDE via the {@link Server}
   *
   * @param objectName the name of the class to generate completion items for
   * @return a JSON array of completion items, if the class is not found, it returns an empty array
   */
  public static String GenerateCompletionItems(String objectName) {
    Class<?> clazz = classMap.get(objectName);
    if (clazz == null) {
      LOGGER.warning("Class not found for object name: " + objectName);
      return "[]";
    }
    return GenerateCompletionItems(clazz);
  }

  /**
   * Generates completion items for the given class.
   *
   * <p>This method returns a JSON array of completion items for the public methods of the given
   * class. This is used to provide code completion suggestions in the IDE via the {@link Server}
   *
   * @param clazz the class to generate completion items for
   * @return a JSON array of completion items
   */
  public static String GenerateCompletionItems(Class<?> clazz) {
    StringBuilder json = new StringBuilder();
    json.append("[");

    Method[] methods = clazz.getDeclaredMethods();
    Map<String, LanguageServer.MethodJavadoc> docs = getJavadocForClass(clazz).methodDocs;
    boolean first = true;

    for (Method method : methods) {
      if (Modifier.isPublic(method.getModifiers())) {
        if (!first) {
          json.append(",");
        }
        first = false;

        MethodJavadoc methodJavadoc = docs.get(method.getName() + method.getParameterCount());

        if (methodJavadoc == null) {
          System.out.println("No Javadoc found for method: " + method.getName());
          continue;
        }

        // Extract parameter names
        String[] parameterNames = methodJavadoc.parameters.keySet().toArray(new String[0]);
        String parameterNamesJson = getParameterNamesJson(parameterNames);

        json.append("\n  {")
            .append("\n    \"label\": \"")
            .append(escapeJson(method.getName()))
            .append("\",")
            .append("\n    \"kind\": 2,")
            .append("\n    \"detail\": \"")
            .append(escapeJson(getMethodDetailSignature(method, parameterNames)))
            .append("\",")
            .append("\n    \"documentation\": \"")
            .append(escapeJson(methodJavadoc.toMarkdown()))
            .append("\",")
            .append("\n    \"parameters\": ")
            .append(parameterNamesJson)
            .append(",")
            .append("\n    \"insertText\": \"")
            .append(escapeJson(getMethodSignature(method, parameterNames)))
            .append("\",")
            .append("\n    \"insertTextFormat\": 2")
            .append("\n  }");
      }
    }

    json.append("\n]");
    return json.toString();
  }

  private static String getParameterNamesJson(String[] parameterNames) {
    if (parameterNames.length == 0) {
      return "[]";
    }

    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i = 0; i < parameterNames.length; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append("\"").append(escapeJson(parameterNames[i])).append("\"");
    }
    sb.append("]");
    return sb.toString();
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

  private static ServerDocs getJavadocForClass(Class<?> clazz) {
    String classPath =
        clazz.getPackage().getName().replace('.', '/') + "/" + clazz.getSimpleName() + ".java";
    ServerDocs serverDocs = new ServerDocs();

    try (InputStream inputStream = getClassFileInputStream(classPath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

      StringBuilder fileContent = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        fileContent.append(line).append("\n");
      }

      javaParser
          .parse(fileContent.toString())
          .getResult()
          .ifPresent(
              cu ->
                  cu.findAll(MethodDeclaration.class)
                      .forEach(
                          method -> {
                            method
                                .getComment()
                                .ifPresent(
                                    comment -> {
                                      String javadocContent = comment.getContent();
                                      serverDocs.methodDocs.put(
                                          method.getNameAsString() + method.getParameters().size(),
                                          parseJavadoc(javadocContent));
                                    });
                          }));
    } catch (IOException e) {
      LOGGER.warning("Error reading class file: " + e.getMessage());
    } catch (Exception e) {
      LOGGER.warning("Error parsing class file: " + e.getMessage());
    }
    return serverDocs;
  }

  private static MethodJavadoc parseJavadoc(String javadocContent) {
    MethodJavadoc doc = new MethodJavadoc();
    String[] lines = javadocContent.split("\n");
    StringBuilder mainText = new StringBuilder();

    String currentParam = "";
    String currentException = "";

    for (String line : lines) {
      line = line.trim().replaceFirst("^\\s*\\*\\s*", "");
      if (line.isEmpty()) continue;

      if (line.startsWith("@param")) {
        String[] parts = line.substring(6).trim().split("\\s+", 2);
        if (parts.length >= 2) {
          currentParam = parts[0];
          doc.parameters.put(currentParam, parts[1]);
        }
      } else if (line.startsWith("@return")) {
        doc.returnDoc = line.substring(7).trim();
      } else if (line.startsWith("@throws")) {
        String[] parts = line.substring(7).trim().split("\\s+", 2);
        if (parts.length >= 2) {
          currentException = parts[0];
          doc.throwDoc.put(currentException, parts[1]);
        }
      } else {
        if (!currentParam.isEmpty()) {
          doc.parameters.put(currentParam, doc.parameters.get(currentParam) + " " + line);
        } else if (!currentException.isEmpty()) {
          doc.throwDoc.put(currentException, doc.throwDoc.get(currentException) + " " + line);
        } else if (!doc.returnDoc.isEmpty()) {
          doc.returnDoc += " " + line;
        } else {
          mainText.append(line).append(" ");
        }
      }
    }

    doc.mainText = mainText.toString().trim();
    return doc;
  }

  private static class ServerDocs {
    public Map<String, MethodJavadoc> methodDocs = new HashMap<>();
  }

  private static class MethodJavadoc {
    public String mainText = "";
    public Map<String, String> parameters = new HashMap<>();
    public String returnDoc = "";
    public Map<String, String> throwDoc = new HashMap<>();

    public String toMarkdown() {
      StringBuilder sb = new StringBuilder();
      sb.append(mainText).append("\n\n");

      if (!parameters.isEmpty()) {
        sb.append("### Parameters:\n");
        parameters.forEach(
            (key, value) ->
                sb.append("- **").append(key).append(":** ").append(value).append("\n"));
      }

      if (!returnDoc.isEmpty()) {
        sb.append("### Returns:\n").append(returnDoc).append("\n");
      }

      if (!throwDoc.isEmpty()) {
        sb.append("### Throws:\n");
        throwDoc.forEach(
            (key, value) ->
                sb.append("- **").append(key).append(":** ").append(value).append("\n"));
      }

      // Replace {@code code} with markdown `code`
      String codeText =
          Pattern.compile("\\{@code\\s*([^}]+)}").matcher(sb.toString()).replaceAll("`$1`");

      // Replace {@link Text} with Text
      String finalText = Pattern.compile("\\{@link\\s*([^}]+)}").matcher(codeText).replaceAll("$1");

      return finalText;
    }
  }

  private static InputStream getClassFileInputStream(String classPath) throws IOException {
    // First try to find the file in the classpath resources
    InputStream inputStream = LanguageServer.class.getClassLoader().getResourceAsStream(classPath);
    if (inputStream != null) {
      return inputStream;
    }

    // If not found in resources, try to locate the file in the project directory or source paths
    String[] sourcePaths = {"src/main/java", "src", "."};
    for (String sourcePath : sourcePaths) {
      java.nio.file.Path path = Paths.get(sourcePath, classPath);
      if (Files.exists(path)) {
        return Files.newInputStream(path);
      }
    }

    throw new IOException("Could not find source file: " + classPath);
  }
}
