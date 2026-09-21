package rooms.systemRecovery.util;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Server-authoritative list of array identifiers discovered in accepted System Recovery code.
 *
 * <p>The interpreter deliberately keeps its capture context private. The Memory Watch therefore
 * extracts only identifiers that occur in a source submission after the server has already
 * accepted that submission. It is a display aid, not a second code validator.
 */
public final class SystemRecoveryMemoryWatch {

  private static final Pattern ARRAY_DECLARATION =
      Pattern.compile(
          "\\b(int|String|boolean)\\s*((?:\\[\\s*]\\s*)+)([A-Za-z][A-Za-z0-9_]*)\\s*=");
  private static final Pattern ARRAY_ACCESS =
      Pattern.compile("\\b([A-Za-z][A-Za-z0-9_]*)\\s*(?=\\[\\s*[^]]*])");
  private static final Pattern ARRAY_LENGTH =
      Pattern.compile("\\b([A-Za-z][A-Za-z0-9_]*)\\s*\\.\\s*length\\b");
  private static final Pattern ENHANCED_FOR_ARRAY =
      Pattern.compile(":\\s*([A-Za-z][A-Za-z0-9_]*)\\b");

  private final Set<String> arrayNames = new LinkedHashSet<>();
  private final Map<String, String> arrayTypes = new LinkedHashMap<>();

  /** Clears the memory view when a new System Recovery level is created. */
  public synchronized void clear() {
    arrayNames.clear();
    arrayTypes.clear();
  }

  /**
   * Records array identifiers from a source that the authoritative interpreter accepted.
   *
   * @param source complete accepted source text
   */
  public synchronized void recordAcceptedSource(String source) {
    if (source == null || source.isBlank()) return;
    collectDeclarations(ARRAY_DECLARATION.matcher(source));
    collect(ARRAY_ACCESS.matcher(source));
    collect(ARRAY_LENGTH.matcher(source));
    collect(ENHANCED_FOR_ARRAY.matcher(source));
  }

  /**
   * Extracts names from one accepted source without retaining state between calls.
   *
   * @param source accepted source text
   * @return array identifiers found in the source
   */
  public static String[] extractArrayNames(String source) {
    SystemRecoveryMemoryWatch memoryWatch = new SystemRecoveryMemoryWatch();
    memoryWatch.recordAcceptedSource(source);
    return memoryWatch.arrayNames();
  }

  /**
   * Returns the names in first-seen order for transport to the Memory Watch tab.
   *
   * @return defensive copy of accepted array names
   */
  public synchronized String[] arrayNames() {
    return arrayNames.toArray(new String[0]);
  }

  /**
   * Returns accepted array names and data types in a compact transport representation.
   *
   * @return entries encoded as {@code name<TAB>type}
   */
  public synchronized String[] arrayEntries() {
    return arrayNames.stream()
        .map(name -> name + "\t" + arrayTypes.getOrDefault(name, "?[]"))
        .toArray(String[]::new);
  }

  /**
   * Extracts array names and data types from one accepted source.
   *
   * @param source accepted source text
   * @return entries encoded as {@code name<TAB>type}
   */
  public static String[] extractArrayEntries(String source) {
    SystemRecoveryMemoryWatch memoryWatch = new SystemRecoveryMemoryWatch();
    memoryWatch.recordAcceptedSource(source);
    return memoryWatch.arrayEntries();
  }

  /**
   * Decodes one transport entry sent to the Memory Watch UI.
   *
   * @param encodedEntry entry encoded as {@code name<TAB>type}
   * @return decoded name and type
   */
  public static ArrayEntry parseEntry(String encodedEntry) {
    if (encodedEntry == null || encodedEntry.isBlank()) return new ArrayEntry("", "?[]");
    String[] fields = encodedEntry.split("\\t", 2);
    if (fields.length == 1) return new ArrayEntry(fields[0], "?[]");
    return new ArrayEntry(fields[0], fields[1].isBlank() ? "?[]" : fields[1]);
  }

  private void collect(Matcher matcher) {
    while (matcher.find()) {
      String name = matcher.group(1);
      register(name, null);
    }
  }

  private void collectDeclarations(Matcher matcher) {
    while (matcher.find()) {
      String type = matcher.group(1) + matcher.group(2).replaceAll("\\s+", "");
      register(matcher.group(3), type);
    }
  }

  private void register(String name, String type) {
    if (!isArrayIdentifier(name)) return;
    arrayNames.add(name);
    if (type != null) {
      arrayTypes.put(name, type);
    } else {
      arrayTypes.putIfAbsent(name, inferredType(name));
    }
  }

  private static String inferredType(String name) {
    return switch (name) {
      case "map" -> "int[][]";
      case "modules" -> "String[]";
      case "array", "sortArray" -> "int[]";
      default -> "?[]";
    };
  }

  private static boolean isArrayIdentifier(String name) {
    return name != null
        && !name.equals("int")
        && !name.equals("String")
        && !name.equals("boolean")
        && !name.equals("new");
  }

  /** Name and data type shown for one Memory Watch entry.
   *
   * @param name array identifier
   * @param type array data type
   */
  public record ArrayEntry(String name, String type) {}
}
