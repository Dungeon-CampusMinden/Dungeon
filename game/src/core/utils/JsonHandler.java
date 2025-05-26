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
 *   <li>Supports String, Number, Boolean, null, and nested Map values.
 *   <li>Other value types are converted to strings using their {@code toString()} method and then
 *       JSON-escaped.
 *   <li>Does <em>not</em> recursively serialize Lists or arrays into JSON arrays. If a List is a
 *       value in the map, its {@code toString()} representation will be used.
 *   <li>Offers an option for pretty-printing the JSON output.
 * </ul>
 *
 * <p><b>Reading JSON:</b>
 *
 * <ul>
 *   <li>Parses JSON objects ({@code {...}}) into {@code Map<String, Object>}.
 *   <li>Supports JSON strings, numbers (parsed as Long or Double), booleans, and null.
 *   <li>JSON arrays (e.g., {@code [1, "two"]}) within the input JSON string are <em>not</em> parsed
 *       into Java Lists; instead, they are returned as their literal string representation (e.g.,
 *       the string {@code "[1, \"two\"]"}).
 *   <li>The parser is simplified and may not handle all JSON complexities or all forms of malformed
 *       JSON gracefully. It expects well-formed JSON objects as input.
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
   *   <li>{@code String}
   *   <li>{@code Number} (e.g., Integer, Double, Long)
   *   <li>{@code Boolean}
   *   <li>{@code null}
   * </ul>
   *
   * <p>Other value types will be converted to their string representation using {@code toString()}
   * and then properly JSON-escaped. Lists or arrays are not specially handled and will be
   * stringified.
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

      Object value = entry.getValue();
      if (value instanceof Map) {
        // We expect Map<String, Object> for nesting.
        // If a Map with other key types is provided, it might lead to
        // ClassCastException here or incorrect key serialization.
        @SuppressWarnings("unchecked")
        Map<String, Object> nestedMap = (Map<String, Object>) value;
        writeJsonRecursive(jsonBuilder, nestedMap, prettyPrint, nextIndent, indentIncrement);
      } else if (value instanceof String) {
        jsonBuilder.append("\"").append(escapeString((String) value)).append("\"");
      } else if (value instanceof Number || value instanceof Boolean) {
        jsonBuilder.append(value);
      } else if (value == null) {
        jsonBuilder.append("null");
      } else {
        // Fallback for other types (e.g., List, custom objects):
        // treat as string using toString().
        jsonBuilder.append("\"").append(escapeString(value.toString())).append("\"");
      }
      first = false;
    }

    if (prettyPrint && !data.isEmpty()) {
      jsonBuilder.append("\n").append(currentIndent);
    }
    jsonBuilder.append("}");
  }

  /**
   * Parses a JSON string representing a JSON object into a {@code Map<String, Object>}.
   *
   * <p>This parser supports:
   *
   * <ul>
   *   <li>Nested JSON objects (parsed as nested Maps).
   *   <li>JSON strings (unescaped into Java Strings).
   *   <li>JSON numbers (parsed as Long or Double).
   *   <li>JSON booleans (true/false).
   *   <li>JSON null.
   * </ul>
   *
   * <b>Important Limitation:</b> JSON arrays (e.g., {@code ["a", 1]}) are <em>not</em> parsed into
   * Java Lists. Instead, the literal string representation of the array (e.g., {@code "[\"a\",
   * 1]"}) will be returned as the value in the map.
   *
   * <p>The parser expects the input string to be a valid JSON object (i.e., starting with '{' and
   * ending with '}'). Keys must be double-quoted strings.
   *
   * @param jsonString The JSON string to parse. Must be a representation of a JSON object.
   * @return A {@code Map<String, Object>} representing the parsed JSON object.
   * @throws IllegalArgumentException if the jsonString is null, not a valid JSON object structure
   *     (e.g., doesn't start/end with braces), or contains malformed elements (e.g., invalid key,
   *     malformed pair).
   */
  public static Map<String, Object> readJson(String jsonString) {
    if (jsonString == null) {
      throw new IllegalArgumentException("Input JSON string cannot be null.");
    }
    String trimmedJson = jsonString.trim();
    if (!trimmedJson.startsWith("{") || !trimmedJson.endsWith("}")) {
      throw new IllegalArgumentException(
          "JSON string must be an object (start with '{' and end with '}').");
    }
    return parseObjectInternal(trimmedJson);
  }

  /**
   * Assumes {@code jsonObjectString} starts with '{' and ends with '}'.
   *
   * @param jsonObjectString The string content of a JSON object, including braces.
   * @return A map representing the parsed object.
   * @throws IllegalArgumentException for malformed JSON structures.
   */
  private static Map<String, Object> parseObjectInternal(String jsonObjectString) {
    Map<String, Object> data = new HashMap<>();
    // Remove surrounding braces for content processing
    String content = jsonObjectString.substring(1, jsonObjectString.length() - 1).trim();
    if (content.isEmpty()) {
      return data;
    }

    List<String> pairs = getPairs(content);

    for (String pair : pairs) {
      if (pair.isEmpty()) {
        continue;
      }

      int colonIndex = findTopLevelColon(pair);

      if (colonIndex <= 0 || colonIndex >= pair.length() - 1) {
        // colonIndex == 0 means key is missing or starts with colon.
        // colonIndex == pair.length() - 1 means value is missing.
        throw new IllegalArgumentException("Malformed JSON pair (invalid colon position): " + pair);
      }

      String keyString = pair.substring(0, colonIndex).trim();
      String valueString = pair.substring(colonIndex + 1).trim();

      String key;
      if (keyString.startsWith("\"") && keyString.endsWith("\"")) {
        key = unescapeString(keyString.substring(1, keyString.length() - 1));
      } else {
        throw new IllegalArgumentException(
            "JSON key must be a string enclosed in double quotes: " + keyString);
      }

      Object value = parseValue(valueString);
      data.put(key, value);
    }
    return data;
  }

  /**
   * Finds the index of the top-level colon (':') character that separates a key from a value in a
   * JSON pair string. It correctly skips colons within nested JSON objects/arrays or strings.
   *
   * @param pairString The string representing a single key-value pair (e.g., "\"name\":
   *     \"value\"").
   * @return The index of the top-level colon, or -1 if not found or if the structure is invalid for
   *     this simple check.
   */
  private static int findTopLevelColon(String pairString) {
    int nestingLevel = 0; // For tracking {} and []
    boolean inString = false;
    for (int i = 0; i < pairString.length(); i++) {
      char c = pairString.charAt(i);
      if (c == '"') {
        // Toggle inString state if this quote is not escaped
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
        } else if (c == ':' && nestingLevel == 0) {
          return i; // Found
        }
      }
    }
    return -1; // Colon not found
  }

  /**
   * Parses a JSON value string into its corresponding Java object. Supports strings, numbers
   * (Long/Double), booleans, null, and nested objects. JSON arrays are returned as their string
   * representation. Unquoted strings (not true, false, null, or numbers) are returned as strings.
   *
   * @param valueString The string representation of the JSON value.
   * @return The parsed Java object.
   * @throws IllegalArgumentException for malformed nested JSON objects.
   */
  private static Object parseValue(String valueString) {
    valueString = valueString.trim();
    if (valueString.startsWith("\"") && valueString.endsWith("\"")) {
      return unescapeString(valueString.substring(1, valueString.length() - 1));
    } else if (valueString.startsWith("{") && valueString.endsWith("}")) {
      return parseObjectInternal(valueString); // Recursive call for nested object
    } else if ("true".equalsIgnoreCase(valueString)) {
      return true;
    } else if ("false".equalsIgnoreCase(valueString)) {
      return false;
    } else if ("null".equalsIgnoreCase(valueString)) {
      return null;
    } else {
      // Try to parse as number
      try {
        if (valueString.contains(".") || valueString.toLowerCase().contains("e")) {
          return Double.parseDouble(valueString);
        } else {
          return Long.parseLong(valueString);
        }
      } catch (NumberFormatException e) {
        // If not a recognized type, it might be an unquoted string or Array.
        return valueString;
      }
    }
  }

  /**
   * Splits the content of a JSON object string (without the outer braces) into individual key-value
   * pair strings. This method attempts to correctly handle commas within nested structures
   * (objects, arrays treated as strings) and within quoted strings.
   *
   * @param content The inner content of a JSON object string.
   * @return A list of strings, where each string is a raw key-value pair.
   */
  private static List<String> getPairs(String content) {
    List<String> pairs = new ArrayList<>();
    if (content == null || content.isEmpty()) {
      return pairs;
    }
    int nestingLevel = 0; // For { } and [ ]
    boolean inString = false;
    int segmentStart = 0;

    for (int i = 0; i < content.length(); i++) {
      char c = content.charAt(i);
      if (c == '"') {
        // Toggle inString state if this quote is not escaped.
        // Counts preceding backslashes: if even, quote is active.
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
        } else if (c == ',' && nestingLevel == 0) {
          pairs.add(content.substring(segmentStart, i).trim());
          segmentStart = i + 1;
        }
      }
    }
    // Add the last segment after the last comma, or the only segment if no commas
    pairs.add(content.substring(segmentStart).trim());
    return pairs;
  }

  /**
   * Escapes special characters in a string for use in JSON. This includes quotes, backslashes, and
   * control characters.
   *
   * @param str The string to escape.
   * @return The escaped string, or null if input is null.
   */
  private static String escapeString(String str) {
    if (str == null) {
      return null; // Or return "null" if it should be a JSON null literal
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
        // Forward slash ('/') may be escaped but is not required.
        // For simplicity, not escaping '/' here.
        default:
          // Control characters (U+0000 through U+001F) must be escaped.
          if (c < ' ') {
            String hex = String.format("\\u%04x", (int) c);
            sb.append(hex);
          } else {
            sb.append(c);
          }
      }
    }
    return sb.toString();
  }

  /**
   * Unescapes JSON-escaped characters in a string. Handles common escape sequences like \", \\, \b,
   * \f, \n, \r, \t, and \\uXXXX unicode escapes. For invalid or truncated \\uXXXX sequences, it
   * appends "\\u" literally and continues parsing.
   *
   * @param str The JSON-escaped string.
   * @return The unescaped string, or null if input is null.
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
          case 'u': // Unicode escape
            if (i + 4 < str.length()) {
              try {
                String hex = str.substring(i + 1, i + 5);
                sb.append((char) Integer.parseInt(hex, 16));
                i += 4; // Advance past the 4 hex digits
              } catch (NumberFormatException e) {
                // Invalid hex sequence
                sb.append("\\u");
              }
            } else {
              sb.append("\\u");
            }
            break;
          default:
            // Unknown escape sequence, append the character itself
            // (e.g., if input was an invalid "\z")
            sb.append(c);
        }
        escaping = false;
      } else if (c == '\\') {
        escaping = true;
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }
}
