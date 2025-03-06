package server;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class LanguageServer {
  public static String GenerateCompletionItems(Class<?> clazz) {
    StringBuilder json = new StringBuilder();
    json.append("[");

    Method[] methods = clazz.getDeclaredMethods();
    boolean first = true;

    for (Method method : methods) {
      if (Modifier.isPublic(method.getModifiers())) {
        if (!first) {
          json.append(",");
        }
        first = false;

        json.append("\n  {")
            .append("\n    \"label\": \"")
            .append(escapeJson(method.getName()))
            .append("\",")
            .append("\n    \"kind\": 2,")
            .append("\n    \"detail\": \"")
            .append(escapeJson(getMethodSignature(method)))
            .append("\",")
            .append("\n    \"documentation\": \"")
            .append(escapeJson(getMethodDocumentation(method)))
            .append("\",")
            .append("\n    \"insertText\": \"")
            .append(escapeJson(method.getName() + "();"))
            .append("\"")
            .append("\n  }");
      }
    }

    json.append("\n]");
    return json.toString();
  }

  private static String getMethodSignature(Method method) {
    StringBuilder signature = new StringBuilder();
    signature
        .append(method.getReturnType().getSimpleName())
        .append(" ")
        .append(method.getName())
        .append("(");

    Class<?>[] params = method.getParameterTypes();
    for (int i = 0; i < params.length; i++) {
      if (i > 0) signature.append(", ");
      signature.append(params[i].getSimpleName());
    }
    signature.append(")");
    return signature.toString();
  }

  private static String getMethodDocumentation(Method method) {
    return method.getDeclaringClass().getSimpleName() + "." + method.getName();
  }

  private static String escapeJson(String input) {
    return input
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t");
  }
}
