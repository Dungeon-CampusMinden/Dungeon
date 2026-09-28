package rooms.systemRecovery.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Server-authoritative snapshot of array declarations and literal values in accepted code. */
public final class SystemRecoveryMemoryWatch {

  private static final Pattern ARRAY_DECLARATION =
      Pattern.compile(
          "^(int|String|boolean)\\s*((?:\\[\\s*]){1,2})\\s+([A-Za-z][A-Za-z0-9_]*)\\s*=\\s*(.+)$");
  private static final Pattern ARRAY_ASSIGNMENT =
      Pattern.compile(
          "^([A-Za-z][A-Za-z0-9_]*)\\s*\\[\\s*(\\d+)\\s*]"
              + "(?:\\s*\\[\\s*(\\d+)\\s*])?\\s*=\\s*(.+)$");

  private final Map<String, ArrayData> arrays = new LinkedHashMap<>();

  /** Clears the memory view when a new System Recovery level is created. */
  public synchronized void clear() {
    arrays.clear();
  }

  /**
   * Applies a source after the authoritative interpreter has accepted it.
   *
   * <p>This intentionally models only literal declarations and indexed assignments used by the
   * escape-room exercises. It does not execute player code or evaluate loop expressions.
   *
   * @param source complete accepted source text
   */
  public synchronized void recordAcceptedSource(String source) {
    if (source == null || source.isBlank()) return;

    for (String statement : splitStatements(source)) {
      String normalized = statement.trim();
      if (normalized.isEmpty()) continue;

      Matcher declaration = ARRAY_DECLARATION.matcher(normalized);
      if (declaration.matches()) {
        declare(
            declaration.group(3),
            declaration.group(1) + declaration.group(2).replaceAll("\\s+", ""),
            declaration.group(4).trim());
        continue;
      }

      Matcher assignment = ARRAY_ASSIGNMENT.matcher(normalized);
      if (assignment.matches()) {
        assign(
            assignment.group(1),
            Integer.parseInt(assignment.group(2)),
            assignment.group(3) == null ? -1 : Integer.parseInt(assignment.group(3)),
            formatLiteral(assignment.group(4)));
      }

    }
  }

  /**
   * Restores a snapshot transported to a computer dialog when it opens.
   *
   * @param encodedEntries entries encoded as {@code name<TAB>type<TAB>values}
   */
  public synchronized void restoreEntries(String[] encodedEntries) {
    clear();
    if (encodedEntries == null) return;
    for (String encoded : encodedEntries) {
      ArrayEntry entry = parseEntry(encoded);
      if (entry.name().isBlank()) continue;
      arrays.put(entry.name(), ArrayData.fromEntry(entry));
    }
  }

  /**
   * Returns array names in first-seen order.
   *
   * @return defensive copy of tracked array names
   */
  public synchronized String[] arrayNames() {
    return arrays.keySet().toArray(String[]::new);
  }

  /**
   * Returns names, types and visible cell values in a compact transport representation.
   *
   * @return entries encoded as {@code name<TAB>type<TAB>values}
   */
  public synchronized String[] arrayEntries() {
    return arrays.values().stream().map(ArrayData::encode).toArray(String[]::new);
  }

  /**
   * Extracts array names and types from one accepted source.
   *
   * @param source accepted source
   * @return entries found in the source
   */
  public static String[] extractArrayEntries(String source) {
    SystemRecoveryMemoryWatch watch = new SystemRecoveryMemoryWatch();
    watch.recordAcceptedSource(source);
    return watch.arrayEntries();
  }

  /**
   * Decodes one transport entry.
   *
   * @param encodedEntry entry encoded as {@code name<TAB>type<TAB>values}
   * @return decoded name, type and values
   */
  public static ArrayEntry parseEntry(String encodedEntry) {
    if (encodedEntry == null || encodedEntry.isBlank()) {
      return new ArrayEntry("", "?[]", "?");
    }
    String[] fields = encodedEntry.split("\\t", 3);
    String type = fields.length < 2 || fields[1].isBlank() ? "?[]" : fields[1];
    String values = fields.length < 3 || fields[2].isBlank() ? "?" : fields[2];
    return new ArrayEntry(fields[0], type, values);
  }

  private void declare(String name, String type, String initializer) {
    ArrayData data = new ArrayData(name, type);
    String literal = bracedLiteral(initializer);
    if (literal != null) {
      data.setContents(parseLiteralContents(literal, data.dimensions()));
      data.knownContents = true;
    } else {
      List<Integer> dimensions = numericDimensions(initializer);
      if (!dimensions.isEmpty()) {
        data.setContents(defaultContents(dimensions, defaultValue(type)));
        data.knownContents = true;
      }
    }
    arrays.put(name, data);
  }

  private void assign(String name, int rowIndex, int columnIndex, String value) {
    ArrayData data = arrays.get(name);
    if (data == null) return;
    data.set(rowIndex, columnIndex, value);
  }

  private static List<String> splitStatements(String source) {
    List<String> statements = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    boolean inString = false;
    boolean escaped = false;
    int parenthesesDepth = 0;

    for (int index = 0; index < source.length(); index++) {
      char character = source.charAt(index);
      if (inString) {
        current.append(character);
        if (escaped) {
          escaped = false;
        } else if (character == '\\') {
          escaped = true;
        } else if (character == '"') {
          inString = false;
        }
        continue;
      }

      if (character == '"') {
        inString = true;
        current.append(character);
      } else if (character == '(') {
        parenthesesDepth++;
        current.append(character);
      } else if (character == ')') {
        parenthesesDepth = Math.max(0, parenthesesDepth - 1);
        current.append(character);
      } else if (character == '/' && index + 1 < source.length() && source.charAt(index + 1) == '/') {
        while (index < source.length() && source.charAt(index) != '\n') index++;
        current.append(' ');
      } else if (character == ';' && parenthesesDepth == 0) {
        statements.add(current.toString());
        current.setLength(0);
      } else {
        current.append(character);
      }
    }
    if (!current.toString().isBlank()) statements.add(current.toString());
    return statements;
  }

  private static String bracedLiteral(String initializer) {
    int start = initializer.indexOf('{');
    if (start < 0) return null;
    int depth = 0;
    boolean inString = false;
    boolean escaped = false;
    for (int index = start; index < initializer.length(); index++) {
      char character = initializer.charAt(index);
      if (inString) {
        if (escaped) {
          escaped = false;
        } else if (character == '\\') {
          escaped = true;
        } else if (character == '"') {
          inString = false;
        }
        continue;
      }
      if (character == '"') {
        inString = true;
      } else if (character == '{') {
        depth++;
      } else if (character == '}' && --depth == 0) {
        return initializer.substring(start, index + 1);
      }
    }
    return null;
  }

  private static List<Integer> numericDimensions(String initializer) {
    List<Integer> dimensions = new ArrayList<>();
    Matcher matcher = Pattern.compile("\\[\\s*(\\d+)\\s*]").matcher(initializer);
    while (matcher.find()) dimensions.add(Integer.parseInt(matcher.group(1)));
    return dimensions;
  }

  private static List<List<String>> defaultContents(List<Integer> dimensions, String defaultValue) {
    List<List<String>> result = new ArrayList<>();
    if (dimensions.size() == 1) {
      List<String> values = new ArrayList<>();
      for (int index = 0; index < dimensions.get(0); index++) {
        values.add(defaultValue);
      }
      result.add(values);
    } else if (dimensions.size() >= 2) {
      for (int row = 0; row < dimensions.get(0); row++) {
        List<String> cells = new ArrayList<>();
        for (int column = 0; column < dimensions.get(1); column++) cells.add(defaultValue);
        result.add(cells);
      }
    }
    return result;
  }

  private static List<List<String>> parseLiteralContents(String literal, int dimensions) {
    String body = literal.substring(1, literal.length() - 1).trim();
    List<List<String>> result = new ArrayList<>();
    if (body.isEmpty()) return result;
    List<String> values = splitTopLevel(body);
    if (dimensions == 1) {
      result.add(values);
    } else {
      for (String row : values) {
        String normalizedRow = row.trim();
        if (normalizedRow.startsWith("{") && normalizedRow.endsWith("}")) {
          result.add(splitTopLevel(normalizedRow.substring(1, normalizedRow.length() - 1)));
        } else {
          result.add(new ArrayList<>(List.of(formatLiteral(normalizedRow))));
        }
      }
    }
    return result;
  }

  private static List<List<String>> parseDisplayContents(String value, int dimensions) {
    String normalized = value.trim();
    if ("?".equals(normalized)) return new ArrayList<>();
    if (!normalized.startsWith("[") || !normalized.endsWith("]")) return new ArrayList<>();
    String body = normalized.substring(1, normalized.length() - 1).trim();
    List<List<String>> rows = new ArrayList<>();
    if (body.isEmpty()) return rows;
    List<String> values = splitTopLevel(body);
    if (dimensions == 2) {
      for (String row : values) {
        String normalizedRow = row.trim();
        if (normalizedRow.startsWith("[") && normalizedRow.endsWith("]")) {
          rows.add(splitTopLevel(normalizedRow.substring(1, normalizedRow.length() - 1)));
        }
      }
    } else {
      rows.add(values);
    }
    return rows;
  }

  private static List<String> splitTopLevel(String source) {
    List<String> values = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    boolean inString = false;
    boolean escaped = false;
    int nestedDepth = 0;
    for (int index = 0; index < source.length(); index++) {
      char character = source.charAt(index);
      if (inString) {
        current.append(character);
        if (escaped) {
          escaped = false;
        } else if (character == '\\') {
          escaped = true;
        } else if (character == '"') {
          inString = false;
        }
      } else if (character == '"') {
        inString = true;
        current.append(character);
      } else if (character == '{' || character == '[') {
        nestedDepth++;
        current.append(character);
      } else if (character == '}' || character == ']') {
        nestedDepth--;
        current.append(character);
      } else if (character == ',' && nestedDepth == 0) {
        values.add(formatLiteral(current.toString()));
        current.setLength(0);
      } else {
        current.append(character);
      }
    }
    values.add(formatLiteral(current.toString()));
    return values;
  }

  private static String formatLiteral(String value) {
    String literal = value.trim();
    return literal.endsWith(";") ? literal.substring(0, literal.length() - 1).trim() : literal;
  }

  private static String defaultValue(String type) {
    if (type.startsWith("int")) return "0";
    if (type.startsWith("boolean")) return "false";
    return "null";
  }

  /**
   * Name, Java type and visible current cell values for one array.
   *
   * @param name Java variable name
   * @param type Java array type
   * @param contents formatted current array contents
   */
  public record ArrayEntry(String name, String type, String contents) {
    /**
     * Legacy constructor retained for callers that have no values to show.
     *
     * @param name Java variable name
     * @param type Java array type
     */
    public ArrayEntry(String name, String type) {
      this(name, type, "?");
    }
  }

  private static final class ArrayData {
    private final String name;
    private String type;
    private List<List<String>> contents = new ArrayList<>();
    private boolean knownContents;

    private ArrayData(String name, String type) {
      this.name = name;
      this.type = type;
    }

    private static ArrayData fromEntry(ArrayEntry entry) {
      ArrayData data = new ArrayData(entry.name(), entry.type());
      data.contents = parseDisplayContents(entry.contents(), data.dimensions());
      data.knownContents = !"?".equals(entry.contents());
      return data;
    }

    private int dimensions() {
      if (type.endsWith("[][]")) return 2;
      return 1;
    }

    private void setContents(List<List<String>> values) {
      contents = values;
    }

    private void set(int row, int column, String value) {
      knownContents = true;
      if (dimensions() == 1) {
        ensureRows(1);
        List<String> cells = contents.get(0);
        while (cells.size() <= row) cells.add("?");
        cells.set(row, value);
        return;
      }
      ensureRows(row + 1);
      List<String> cells = contents.get(row);
      while (cells.size() <= column) cells.add("?");
      cells.set(column, value);
    }

    private void ensureRows(int count) {
      while (contents.size() < count) contents.add(new ArrayList<>());
    }

    private String displayContents() {
      if (!knownContents) return "?";
      if (dimensions() == 2) {
        return contents.stream().map(row -> "[" + String.join(", ", row) + "]").toList().toString();
      }
      if (contents.isEmpty()) return "[]";
      return "[" + String.join(", ", contents.get(0)) + "]";
    }

    private String encode() {
      return name + "\t" + type + "\t" + displayContents();
    }
  }
}
