package core.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple utility class for writing Java Maps to JSON strings and parsing JSON strings into Java
 * Maps.
 *
 * <p><b>Writing JSON:</b>
 *
 * <ul>
 *   <li>Supports String, Number, Boolean, null, nested Map values, and List values.
 *   <li>Lists are serialized as JSON arrays with recursive handling of nested structures.
 *   <li>Other value types are converted to strings using their {@code toString()} method and then
 *       JSON-escaped.
 *   <li>Offers an option for pretty-printing the JSON output.
 * </ul>
 *
 * <p><b>Reading JSON:</b>
 *
 * <ul>
 *   <li>Parses JSON objects ({@code {...}}) into {@code Map<String, Object>}.
 *   <li>Parses JSON arrays ({@code [...]}) into {@code List<Object>}.
 *   <li>Supports JSON strings, numbers (parsed as Long or Double), booleans, and null.
 *   <li>Supports nested objects and arrays with full recursive parsing.
 *   <li>The parser expects well-formed JSON as input and provides detailed error messages for
 *       malformed JSON.
 *   <li>Keys in JSON objects must be strings enclosed in double quotes.
 * </ul>
 */
public class JsonHandler {

  /**
   * Converts a {@code Map<String, Object>} to a JSON string.
   *
   * <p>This method calls {@link #writeJson(Map, boolean)} with pretty printing disabled.
   *
   * @param data The map to convert. Must not be null.
   * @return The JSON string representation of the map.
   * @throws IllegalArgumentException if the input data map is null.
   */
  public static String writeJson(Map<String, Object> data) {
    if (data == null) {
      throw new IllegalArgumentException("Input map cannot be null.");
    }
    return writeJson(data, false);
  }

  /**
   * Converts a {@code Map<String, Object>} to a JSON string, with an option for pretty printing.
   *
   * <p>Supported value types in the map:
   *
   * <ul>
   *   <li>{@code Map<String, Object>} (for nested objects)
   *   <li>{@code List<Object>} (for arrays)
   *   <li>{@code String}
   *   <li>{@code Number} (e.g., Integer, Double, Long)
   *   <li>{@code Boolean}
   *   <li>{@code null}
   * </ul>
   *
   * <p>Other value types will be converted to their string representation using {@code toString()}
   * and then properly JSON-escaped.
   *
   * @param data The map to convert. Must not be null.
   * @param prettyPrint If true, the JSON output will be formatted with indentation and newlines for
   *     readability.
   * @return The JSON string representation of the map.
   * @throws IllegalArgumentException if the input data map is null.
   */
  public static String writeJson(Map<String, Object> data, boolean prettyPrint) {
    if (data == null) {
      throw new IllegalArgumentException("Input map cannot be null.");
    }
    StringBuilder jsonBuilder = new StringBuilder();
    writeJsonRecursive(jsonBuilder, data, prettyPrint, "", prettyPrint ? "  " : "");
    return jsonBuilder.toString();
  }

  /**
   * Recursively builds the JSON string for a map.
   *
   * @param jsonBuilder The StringBuilder to append JSON content to.
   * @param data The current map to serialize.
   * @param prettyPrint Whether to format the output.
   * @param currentIndent The current indentation string (for pretty printing).
   * @param indentIncrement The string used for one level of indentation.
   */
  private static void writeJsonRecursive(
      StringBuilder jsonBuilder,
      Map<String, Object> data,
      boolean prettyPrint,
      String currentIndent,
      String indentIncrement) {
    jsonBuilder.append("{");
    if (prettyPrint && !data.isEmpty()) {
      jsonBuilder.append("\n");
    }
    boolean first = true;
    String nextIndent = prettyPrint ? currentIndent + indentIncrement : "";

    for (Map.Entry<String, Object> entry : data.entrySet()) {
      if (!first) {
        jsonBuilder.append(",");
        if (prettyPrint) {
          jsonBuilder.append("\n");
        }
      }
      if (prettyPrint) {
        jsonBuilder.append(nextIndent);
      }
      jsonBuilder
          .append("\"")
          .append(escapeString(entry.getKey()))
          .append("\":")
          .append(prettyPrint ? " " : "");

      writeValue(jsonBuilder, entry.getValue(), prettyPrint, nextIndent, indentIncrement);
      first = false;
    }

    if (prettyPrint && !data.isEmpty()) {
      jsonBuilder.append("\n").append(currentIndent);
    }
    jsonBuilder.append("}");
  }

  /**
   * Writes a single value to the JSON string builder.
   *
   * @param jsonBuilder The StringBuilder to append to.
   * @param value The value to write.
   * @param prettyPrint Whether to format the output.
   * @param currentIndent The current indentation string.
   * @param indentIncrement The string used for one level of indentation.
   */
  private static void writeValue(
      StringBuilder jsonBuilder,
      Object value,
      boolean prettyPrint,
      String currentIndent,
      String indentIncrement) {
    if (value == null) {
      jsonBuilder.append("null");
    } else if (value instanceof String) {
      jsonBuilder.append("\"").append(escapeString((String) value)).append("\"");
    } else if (value instanceof Number || value instanceof Boolean) {
      jsonBuilder.append(value);
    } else if (value instanceof Map) {
      if (isStringObjectMap(value)) {
        @SuppressWarnings("unchecked")
        Map<String, Object> nestedMap = (Map<String, Object>) value;
        writeJsonRecursive(jsonBuilder, nestedMap, prettyPrint, currentIndent, indentIncrement);
      } else {
        // Non-String keys: convert to string representation
        jsonBuilder.append("\"").append(escapeString(value.toString())).append("\"");
      }
    } else if (value instanceof List) {
      @SuppressWarnings("unchecked")
      List<Object> list = (List<Object>) value;
      writeArray(jsonBuilder, list, prettyPrint, currentIndent, indentIncrement);
    } else {
      // Fallback for other types: treat as string using toString()
      jsonBuilder.append("\"").append(escapeString(value.toString())).append("\"");
    }
  }

  /**
   * Writes a JSON array to the string builder.
   *
   * @param jsonBuilder The StringBuilder to append to.
   * @param list The list to serialize.
   * @param prettyPrint Whether to format the output.
   * @param currentIndent The current indentation string.
   * @param indentIncrement The string used for one level of indentation.
   */
  private static void writeArray(
      StringBuilder jsonBuilder,
      List<Object> list,
      boolean prettyPrint,
      String currentIndent,
      String indentIncrement) {
    jsonBuilder.append("[");
    if (prettyPrint && !list.isEmpty()) {
      jsonBuilder.append("\n");
    }
    String nextIndent = prettyPrint ? currentIndent + indentIncrement : "";

    for (int i = 0; i < list.size(); i++) {
      if (i > 0) {
        jsonBuilder.append(",");
        if (prettyPrint) {
          jsonBuilder.append("\n");
        }
      }
      if (prettyPrint) {
        jsonBuilder.append(nextIndent);
      }
      writeValue(jsonBuilder, list.get(i), prettyPrint, nextIndent, indentIncrement);
    }

    if (prettyPrint && !list.isEmpty()) {
      jsonBuilder.append("\n").append(currentIndent);
    }
    jsonBuilder.append("]");
  }

  /**
   * Checks if a Map has String keys and can be safely cast to {@code Map<String, Object>}.
   *
   * @param value The object to check.
   * @return true if the object is a Map with String keys, false otherwise.
   */
  private static boolean isStringObjectMap(Object value) {
    if (!(value instanceof Map)) {
      return false;
    }
    Map<?, ?> map = (Map<?, ?>) value;
    for (Object key : map.keySet()) {
      if (!(key instanceof String)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Parses a JSON string representing a JSON object into a {@code Map<String, Object>}.
   *
   * <p>This parser supports:
   *
   * <ul>
   *   <li>Nested JSON objects (parsed as nested Maps).
   *   <li>JSON arrays (parsed as Lists).
   *   <li>JSON strings (unescaped into Java Strings).
   *   <li>JSON numbers (parsed as Long or Double).
   *   <li>JSON booleans (true/false).
   *   <li>JSON null.
   * </ul>
   *
   * <p>The parser expects the input string to be a valid JSON object (i.e., starting with '{' and
   * ending with '}'). Keys must be double-quoted strings.
   *
   * @param jsonString The JSON string to parse. Must be a representation of a JSON object.
   * @return A {@code Map<String, Object>} representing the parsed JSON object.
   * @throws IllegalArgumentException if the jsonString is null, not a valid JSON object structure,
   *     or contains malformed elements with detailed error context.
   */
  public static Map<String, Object> readJson(String jsonString) {
    if (jsonString == null) {
      throw new IllegalArgumentException("Input JSON string cannot be null.");
    }
    String trimmedJson = jsonString.trim();
    if (trimmedJson.isEmpty()) {
      throw new IllegalArgumentException("Input JSON string cannot be empty.");
    }
    if (!trimmedJson.startsWith("{") || !trimmedJson.endsWith("}")) {
      throw new IllegalArgumentException(
          "JSON string must be an object (start with '{' and end with '}'). Got: "
              + truncateForError(trimmedJson));
    }
    try {
      return parseObjectInternal(trimmedJson);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Failed to parse JSON object: " + e.getMessage(), e);
    }
  }

  /**
   * Assumes {@code jsonObjectString} starts with '{' and ends with '}'.
   *
   * @param jsonObjectString The string content of a JSON object, including braces.
   * @return A map representing the parsed object.
   * @throws IllegalArgumentException for malformed JSON structures with detailed context.
   */
  private static Map<String, Object> parseObjectInternal(String jsonObjectString) {
    Map<String, Object> data = new HashMap<>();
    String content = jsonObjectString.substring(1, jsonObjectString.length() - 1).trim();
    if (content.isEmpty()) {
      return data;
    }

    List<String> pairs;
    try {
      pairs = getPairs(content);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "Failed to split object content into key-value pairs: "
              + e.getMessage()
              + ". Content: "
              + truncateForError(content),
          e);
    }

    for (String pair : pairs) {
      if (pair.isEmpty()) {
        continue;
      }

      int colonIndex = findTopLevelColon(pair);

      if (colonIndex <= 0) {
        throw new IllegalArgumentException(
            "Missing or invalid key in JSON pair (no colon found or colon at start): "
                + truncateForError(pair));
      }
      if (colonIndex >= pair.length() - 1) {
        throw new IllegalArgumentException(
            "Missing value in JSON pair (colon at end): " + truncateForError(pair));
      }

      String keyString = pair.substring(0, colonIndex).trim();
      String valueString = pair.substring(colonIndex + 1).trim();

      String key;
      if (keyString.startsWith("\"") && keyString.endsWith("\"") && keyString.length() >= 2) {
        try {
          key = unescapeString(keyString.substring(1, keyString.length() - 1));
        } catch (IllegalArgumentException e) {
          throw new IllegalArgumentException(
              "Failed to unescape JSON key: " + truncateForError(keyString) + ". " + e.getMessage(),
              e);
        }
      } else {
        throw new IllegalArgumentException(
            "JSON key must be a string enclosed in double quotes. Got: "
                + truncateForError(keyString));
      }

      try {
        Object value = parseValue(valueString);
        data.put(key, value);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
            "Failed to parse value for key '"
                + key
                + "': "
                + e.getMessage()
                + ". Value: "
                + truncateForError(valueString),
            e);
      }
    }
    return data;
  }

  /**
   * Parses a JSON array string into a {@code List<Object>}.
   *
   * @param jsonArrayString The JSON array string, including brackets.
   * @return A List representing the parsed array.
   * @throws IllegalArgumentException for malformed JSON arrays.
   */
  private static List<Object> parseArrayInternal(String jsonArrayString) {
    List<Object> list = new ArrayList<>();
    String content = jsonArrayString.substring(1, jsonArrayString.length() - 1).trim();
    if (content.isEmpty()) {
      return list;
    }

    List<String> elements;
    try {
      elements = getArrayElements(content);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "Failed to split array content into elements: "
              + e.getMessage()
              + ". Content: "
              + truncateForError(content),
          e);
    }

    for (String element : elements) {
      if (element.isEmpty()) {
        continue;
      }
      try {
        Object value = parseValue(element);
        list.add(value);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
            "Failed to parse array element: "
                + e.getMessage()
                + ". Element: "
                + truncateForError(element),
            e);
      }
    }
    return list;
  }

  /**
   * Finds the index of the top-level colon (':') character that separates a key from a value in a
   * JSON pair string. It correctly skips colons within nested JSON objects/arrays or strings.
   *
   * @param pairString The string representing a single key-value pair.
   * @return The index of the top-level colon, or -1 if not found.
   * @throws IllegalArgumentException if the string structure is invalid (unmatched
   *     quotes/brackets).
   */
  private static int findTopLevelColon(String pairString) {
    int nestingLevel = 0;
    boolean inString = false;
    for (int i = 0; i < pairString.length(); i++) {
      char c = pairString.charAt(i);
      if (c == '"') {
        int backslashCount = 0;
        for (int k = i - 1; k >= 0 && pairString.charAt(k) == '\\'; k--) {
          backslashCount++;
        }
        if (backslashCount % 2 == 0) {
          inString = !inString;
        }
      } else if (!inString) {
        if (c == '{' || c == '[') {
          nestingLevel++;
        } else if (c == '}' || c == ']') {
          nestingLevel--;
          if (nestingLevel < 0) {
            throw new IllegalArgumentException(
                "Unmatched closing bracket/brace in: " + truncateForError(pairString));
          }
        } else if (c == ':' && nestingLevel == 0) {
          return i;
        }
      }
    }
    if (inString) {
      throw new IllegalArgumentException(
          "Unclosed string in JSON pair: " + truncateForError(pairString));
    }
    if (nestingLevel != 0) {
      throw new IllegalArgumentException(
          "Unmatched brackets/braces in JSON pair: " + truncateForError(pairString));
    }
    return -1;
  }

  /**
   * Parses a JSON value string into its corresponding Java object.
   *
   * @param valueString The string representation of the JSON value.
   * @return The parsed Java object.
   * @throws IllegalArgumentException for malformed values with detailed context.
   */
  private static Object parseValue(String valueString) {
    valueString = valueString.trim();
    if (valueString.isEmpty()) {
      throw new IllegalArgumentException("Empty value string");
    }

    if (valueString.startsWith("\"") && valueString.endsWith("\"")) {
      if (valueString.length() < 2) {
        throw new IllegalArgumentException("Invalid string value: " + valueString);
      }
      return unescapeString(valueString.substring(1, valueString.length() - 1));
    } else if (valueString.startsWith("{") && valueString.endsWith("}")) {
      return parseObjectInternal(valueString);
    } else if (valueString.startsWith("[") && valueString.endsWith("]")) {
      return parseArrayInternal(valueString);
    } else if ("true".equals(valueString)) {
      return true;
    } else if ("false".equals(valueString)) {
      return false;
    } else if ("null".equals(valueString)) {
      return null;
    } else {
      try {
        if (valueString.contains(".") || valueString.toLowerCase().contains("e")) {
          return Double.parseDouble(valueString);
        } else {
          return Long.parseLong(valueString);
        }
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException(
            "Invalid JSON value (not a valid string, number, boolean, null, object, or array): "
                + truncateForError(valueString));
      }
    }
  }

  /**
   * Splits the content of a JSON object string into individual key-value pair strings.
   *
   * @param content The inner content of a JSON object string.
   * @return A list of strings, where each string is a raw key-value pair.
   * @throws IllegalArgumentException if the content has structural issues.
   */
  private static List<String> getPairs(String content) {
    List<String> pairs = new ArrayList<>();
    if (content == null || content.isEmpty()) {
      return pairs;
    }
    return splitTopLevel(content, ',');
  }

  /**
   * Splits the content of a JSON array string into individual element strings.
   *
   * @param content The inner content of a JSON array string.
   * @return A list of strings, where each string is a raw array element.
   * @throws IllegalArgumentException if the content has structural issues.
   */
  private static List<String> getArrayElements(String content) {
    if (content == null || content.isEmpty()) {
      return new ArrayList<>();
    }
    return splitTopLevel(content, ',');
  }

  /**
   * Splits a string by a delimiter at the top level only (not within nested structures or strings).
   *
   * @param content The content to split.
   * @param delimiter The delimiter character.
   * @return A list of split segments.
   * @throws IllegalArgumentException if the content has structural issues.
   */
  private static List<String> splitTopLevel(String content, char delimiter) {
    List<String> segments = new ArrayList<>();
    int nestingLevel = 0;
    boolean inString = false;
    int segmentStart = 0;

    for (int i = 0; i < content.length(); i++) {
      char c = content.charAt(i);
      if (c == '"') {
        int backslashCount = 0;
        for (int k = i - 1; k >= 0 && content.charAt(k) == '\\'; k--) {
          backslashCount++;
        }
        if (backslashCount % 2 == 0) {
          inString = !inString;
        }
      }

      if (!inString) {
        if (c == '{' || c == '[') {
          nestingLevel++;
        } else if (c == '}' || c == ']') {
          nestingLevel--;
          if (nestingLevel < 0) {
            throw new IllegalArgumentException(
                "Unmatched closing bracket/brace at position "
                    + i
                    + " in: "
                    + truncateForError(content));
          }
        } else if (c == delimiter && nestingLevel == 0) {
          segments.add(content.substring(segmentStart, i).trim());
          segmentStart = i + 1;
        }
      }
    }

    if (inString) {
      throw new IllegalArgumentException("Unclosed string in: " + truncateForError(content));
    }
    if (nestingLevel != 0) {
      throw new IllegalArgumentException(
          "Unmatched brackets/braces in: " + truncateForError(content));
    }

    segments.add(content.substring(segmentStart).trim());
    return segments;
  }

  /**
   * Escapes special characters in a string for use in JSON.
   *
   * @param str The string to escape.
   * @return The escaped string, or null if input is null.
   */
  private static String escapeString(String str) {
    if (str == null) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    for (char c : str.toCharArray()) {
      switch (c) {
        case '"':
          sb.append("\\\"");
          break;
        case '\\':
          sb.append("\\\\");
          break;
        case '\b':
          sb.append("\\b");
          break;
        case '\f':
          sb.append("\\f");
          break;
        case '\n':
          sb.append("\\n");
          break;
        case '\r':
          sb.append("\\r");
          break;
        case '\t':
          sb.append("\\t");
          break;
        default:
          if (c < ' ') {
            sb.append(String.format("\\u%04x", (int) c));
          } else {
            sb.append(c);
          }
      }
    }
    return sb.toString();
  }

  /**
   * Unescapes JSON-escaped characters in a string.
   *
   * @param str The JSON-escaped string.
   * @return The unescaped string, or null if input is null.
   * @throws IllegalArgumentException for invalid escape sequences with context.
   */
  private static String unescapeString(String str) {
    if (str == null) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    boolean escaping = false;
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      if (escaping) {
        switch (c) {
          case '"':
            sb.append('"');
            break;
          case '\\':
            sb.append('\\');
            break;
          case '/':
            sb.append('/');
            break;
          case 'b':
            sb.append('\b');
            break;
          case 'f':
            sb.append('\f');
            break;
          case 'n':
            sb.append('\n');
            break;
          case 'r':
            sb.append('\r');
            break;
          case 't':
            sb.append('\t');
            break;
          case 'u':
            if (i + 4 < str.length()) {
              try {
                String hex = str.substring(i + 1, i + 5);
                sb.append((char) Integer.parseInt(hex, 16));
                i += 4;
              } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    "Invalid unicode escape sequence: \\u"
                        + str.substring(i + 1, Math.min(i + 5, str.length())));
              }
            } else {
              throw new IllegalArgumentException(
                  "Incomplete unicode escape sequence at end of string: \\u"
                      + str.substring(i + 1));
            }
            break;
          default:
            throw new IllegalArgumentException("Invalid escape sequence: \\" + c);
        }
        escaping = false;
      } else if (c == '\\') {
        escaping = true;
      } else {
        sb.append(c);
      }
    }
    if (escaping) {
      throw new IllegalArgumentException("String ends with incomplete escape sequence");
    }
    return sb.toString();
  }

  /**
   * Truncates a string for error messages.
   *
   * <p>This method ensures that the string is no longer than 100 characters, appending an ellipsis
   * if it exceeds that length. If the string is null, it returns "null".
   *
   * @param str The string to truncate.
   * @return The truncated string with ellipsis if needed.
   */
  private static String truncateForError(String str) {
    if (str == null) {
      return "null";
    }
    if (str.length() <= 100) {
      return str;
    }
    return str.substring(0, 100) + "...";
  }
}
