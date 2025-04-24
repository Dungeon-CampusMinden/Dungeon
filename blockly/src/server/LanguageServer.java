package server;

import core.level.utils.LevelElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import utils.BlocklyCommands;
import utils.Direction;
import utils.HideLanguage;

/**
 * This Class contains utility methods to generate completion items for the blockly language server.
 * It uses reflection to inspect the methods of the classes and generate JSON objects that can be
 * later used by the IDE for code completion.
 */
public class LanguageServer {

  private static final Logger LOGGER = Logger.getLogger(LanguageServer.class.getName());
  private static final Map<String, Class<?>> classMap =
      Map.of(
          "hero",
          BlocklyCommands.class,
          "Direction",
          Direction.class,
          "LevelElement",
          LevelElement.class);

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

    // Check if the class is an enum
    if (clazz.isEnum()) {
      return GenerateCompletionItems((Enum<?>) clazz.getEnumConstants()[0]);
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
    boolean first = true;

    for (Method method : methods) {
      if (!Modifier.isPublic(method.getModifiers())
          || method.isAnnotationPresent(HideLanguage.class)) {
        continue;
      }

      if (!first) {
        json.append(",");
      }
      first = false;

      String[] parameterNames = new String[method.getParameterCount()];
      Class<?>[] parameterTypes = method.getParameterTypes();
      for (int i = 0; i < parameterTypes.length; i++) {
        parameterNames[i] = parameterTypes[i].getSimpleName();
      }

      json.append("\n  {")
          .append("\n    \"label\": \"")
          .append(escapeJson(method.getName()))
          .append("\",")
          .append("\n    \"kind\": 2,")
          .append("\n    \"detail\": \"")
          .append(escapeJson(getMethodDetailSignature(method, parameterNames)))
          .append("\",")
          .append("\n    \"documentation\": \"")
          .append(escapeJson(getJavaDocForMethod(method)))
          .append("\",")
          .append("\n    \"parameters\": ")
          .append(getParameterNamesJson(parameterNames))
          .append(",")
          .append("\n    \"insertText\": \"")
          .append(escapeJson(getMethodSignature(method, parameterNames)))
          .append("\",")
          .append("\n    \"insertTextFormat\": 2")
          .append("\n  }");
    }

    json.append("\n]");

    return json.toString();
  }

  /**
   * Generates completion items for the given enum.
   *
   * <p>This method returns a JSON array of completion items for the enum constants of the given
   * class. This is used to provide code completion suggestions in the IDE via the {@link Server}
   *
   * @param enumClass the enum to generate completion items for
   * @return a JSON array of completion items
   * @see #GenerateCompletionItems(Class)
   */
  public static String GenerateCompletionItems(Enum<?> enumClass) {
    StringBuilder json = new StringBuilder();
    json.append("[");

    Enum<?>[] enumConstants = enumClass.getDeclaringClass().getEnumConstants();
    boolean first = true;

    for (Enum<?> constant : enumConstants) {
      if (!first) {
        json.append(",");
      }
      first = false;

      json.append("\n  {")
          .append("\n    \"label\": \"")
          .append(escapeJson(constant.name()))
          .append("\",")
          .append("\n    \"kind\": 2,")
          .append("\n    \"detail\": \"")
          .append(escapeJson(constant.getDeclaringClass().getSimpleName()))
          .append("\",")
          .append("\n    \"documentation\": \"")
          .append(escapeJson(getJavaDocForEnum(constant)))
          .append("\",")
          .append("\n    \"insertText\": \"")
          .append(escapeJson(constant.name()))
          .append("\"")
          .append("\n  }");
    }

    json.append("\n]");

    return json.toString();
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

  // TODO: Change to use Javadoc comments
  private static String getJavaDocForMethod(Method method) {
    return method.getDeclaringClass().getSimpleName() + "." + method.getName();
  }

  // TODO: Change to use Javadoc comments
  private static String getJavaDocForEnum(Enum<?> enumConstant) {
    return enumConstant.getDeclaringClass().getSimpleName();
  }
}
