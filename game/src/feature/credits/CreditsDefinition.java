package feature.credits;

import engine.language.Language;
import engine.utils.JsonHandler;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable, validated data loaded from one room's credits JSON file.
 *
 * @param schemaVersion JSON schema version
 * @param roomId room identifier matching the file name
 * @param title localized credits title
 * @param sections ordered credits sections
 */
public record CreditsDefinition(
    int schemaVersion, String roomId, LocalizedText title, List<Section> sections) {

  /** Current JSON schema version. */
  public static final int SCHEMA_VERSION = 1;

  /**
   * Creates and validates a credits definition.
   *
   * @param schemaVersion JSON schema version
   * @param roomId room identifier matching the file name
   * @param title localized credits title
   * @param sections ordered credits sections
   */
  public CreditsDefinition {
    if (schemaVersion != SCHEMA_VERSION) {
      throw new IllegalArgumentException("Unsupported credits schemaVersion: " + schemaVersion);
    }
    validateRoomId(roomId);
    Objects.requireNonNull(title, "title");
    sections = List.copyOf(Objects.requireNonNull(sections, "sections"));
    if (sections.isEmpty()) {
      throw new IllegalArgumentException("credits sections must not be empty");
    }
    Set<String> sectionIds = new HashSet<>();
    for (Section section : sections) {
      if (!sectionIds.add(section.id())) {
        throw new IllegalArgumentException("duplicate credits section id: " + section.id());
      }
    }
  }

  /**
   * Parses and validates a credits definition from JSON.
   *
   * @param json JSON document
   * @return parsed credits definition
   */
  public static CreditsDefinition parse(String json) {
    Map<String, Object> root = JsonHandler.readJson(json);
    int schemaVersion = integerValue(root.get("schemaVersion"), "schemaVersion");
    String roomId = stringValue(root.get("roomId"), "roomId");
    LocalizedText title = localizedText(root.get("title"), "title");
    List<?> rawSections = listValue(root.get("sections"), "sections");
    List<Section> sections = new ArrayList<>();
    for (Object rawSection : rawSections) {
      Map<String, Object> section = objectValue(rawSection, "section");
      String id = stringValue(section.get("id"), "section.id");
      LocalizedText headline = localizedText(section.get("headline"), "section.headline");
      List<?> rawEntries = listValue(section.get("entries"), "section.entries");
      List<Entry> entries = new ArrayList<>();
      for (Object rawEntry : rawEntries) {
        Map<String, Object> entry = objectValue(rawEntry, "section entry");
        String name = optionalString(entry.get("name"), "section entry.name");
        LocalizedText role = optionalLocalizedText(entry.get("role"), "section entry.role");
        LocalizedText description =
            optionalLocalizedText(entry.get("description"), "section entry.description");
        if (name == null && description == null) {
          throw new IllegalArgumentException(
              "Each credits entry must contain a name or a description");
        }
        entries.add(new Entry(name, role, description));
      }
      sections.add(new Section(id, headline, entries));
    }
    return new CreditsDefinition(schemaVersion, roomId, title, sections);
  }

  /**
   * Returns the internal asset path for a room's credits file.
   *
   * @param roomId room identifier
   * @return internal asset path
   */
  public static String resourcePath(String roomId) {
    validateRoomId(roomId);
    return "credits/" + roomId + ".json";
  }

  /**
   * Validates a room identifier safe to use in a credits asset path.
   *
   * @param roomId room identifier
   * @throws IllegalArgumentException if the identifier is blank or contains unsafe characters
   */
  public static void validateRoomId(String roomId) {
    if (roomId == null || !roomId.matches("[A-Za-z0-9_-]+")) {
      throw new IllegalArgumentException(
          "credits room ID must contain only letters, digits, '_' or '-'");
    }
  }

  /**
   * A section heading and its ordered contributor or funding entries.
   *
   * @param id stable section identifier
   * @param headline localized section heading
   * @param entries ordered entries in the section
   */
  public record Section(String id, LocalizedText headline, List<Entry> entries) {
    /**
     * Creates a section.
     *
     * @param id stable section identifier
     * @param headline localized section heading
     * @param entries ordered entries in the section
     */
    public Section {
      if (id == null || !id.matches("[A-Za-z0-9_-]+")) {
        throw new IllegalArgumentException(
            "section id must contain only letters, digits, '_' or '-'");
      }
      Objects.requireNonNull(headline, "headline");
      entries = List.copyOf(Objects.requireNonNull(entries, "entries"));
    }
  }

  /**
   * One named person or organization, with optional role and description.
   *
   * @param name person or organization name
   * @param role optional localized role
   * @param description optional localized description
   */
  public record Entry(String name, LocalizedText role, LocalizedText description) {
    /**
     * Creates and validates one credits entry.
     *
     * @param name person or organization name
     * @param role optional localized role
     * @param description optional localized description
     */
    public Entry {
      if ((name == null || name.isBlank()) && description == null) {
        throw new IllegalArgumentException("credits entry needs a name or description");
      }
    }
  }

  /**
   * Text available in German and English, with fallback to the other supplied language.
   *
   * @param values language code to text mapping
   */
  public record LocalizedText(Map<String, String> values) {
    /**
     * Creates localized text and rejects empty translations.
     *
     * @param values language code to text mapping
     */
    public LocalizedText {
      Objects.requireNonNull(values, "values");
      Map<String, String> copy = new LinkedHashMap<>();
      values.forEach(
          (language, value) -> {
            String normalizedLanguage = language == null ? "" : language.toLowerCase();
            if (!normalizedLanguage.equals("de") && !normalizedLanguage.equals("en")) {
              throw new IllegalArgumentException(
                  "credits text language must be 'de' or 'en': " + language);
            }
            if (value == null || value.isBlank()) {
              throw new IllegalArgumentException("credits translations must not be blank");
            }
            copy.put(normalizedLanguage, value.trim());
          });
      if (copy.isEmpty()) {
        throw new IllegalArgumentException("credits text must contain at least one translation");
      }
      values = Map.copyOf(copy);
    }

    /**
     * Resolves text for the requested language, falling back to the other provided language.
     *
     * @param language preferred language
     * @return localized text
     */
    public String text(Language language) {
      Objects.requireNonNull(language, "language");
      String requested = language.toString();
      String text = values.get(requested);
      if (text != null) return text;
      text = values.get("de");
      if (text != null) return text;
      return values.get("en");
    }
  }

  private static LocalizedText localizedText(Object value, String field) {
    Map<String, Object> rawValues = objectValue(value, field);
    Map<String, String> values = new LinkedHashMap<>();
    rawValues.forEach(
        (language, text) -> values.put(language, stringValue(text, field + "." + language)));
    return new LocalizedText(values);
  }

  private static LocalizedText optionalLocalizedText(Object value, String field) {
    return value == null ? null : localizedText(value, field);
  }

  private static String optionalString(Object value, String field) {
    if (value == null) return null;
    String string = stringValue(value, field);
    return string.isBlank() ? null : string;
  }

  private static String stringValue(Object value, String field) {
    if (!(value instanceof String string) || string.isBlank()) {
      throw new IllegalArgumentException(
          "credits field '" + field + "' must be a non-empty string");
    }
    return string.trim();
  }

  private static int integerValue(Object value, String field) {
    if (!(value instanceof Number number)) {
      throw new IllegalArgumentException("credits field '" + field + "' must be an integer");
    }
    int parsed = number.intValue();
    if (number.doubleValue() != parsed) {
      throw new IllegalArgumentException("credits field '" + field + "' must be an integer");
    }
    return parsed;
  }

  private static List<?> listValue(Object value, String field) {
    if (!(value instanceof List<?> list)) {
      throw new IllegalArgumentException("credits field '" + field + "' must be an array");
    }
    return list;
  }

  private static Map<String, Object> objectValue(Object value, String field) {
    if (!(value instanceof Map<?, ?> rawMap)) {
      throw new IllegalArgumentException("credits field '" + field + "' must be an object");
    }
    Map<String, Object> result = new LinkedHashMap<>();
    rawMap.forEach((key, nestedValue) -> result.put(String.valueOf(key), nestedValue));
    return result;
  }
}
