package rooms.systemRecovery.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Game;
import engine.language.Language;
import engine.language.Localization;
import engine.utils.JsonHandler;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;

/** Regression tests for client-local System Recovery and quest-log translations. */
class SystemRecoveryTranslatorTest {

  private static final String[] HINT_STAGES = {"orientation", "approach", "near", "solution"};
  private static final Pattern TEXT_KEY_PATTERN =
      Pattern.compile(
          "SystemRecoveryText\\.(key|text|story|phoneCall|questKey|quest)\\(\\s*\"([^\"]+)\"");

  private final Localization localization = Game.localization();
  private final SystemRecoveryTranslator translator = new SystemRecoveryTranslator();

  @BeforeEach
  void setUp() {
    localization.registerTranslationFile(Language.DE, "language/escapeRoom/de.json");
    localization.registerTranslationFile(Language.EN, "language/escapeRoom/en.json");
    localization.registerTranslationFile(Language.DE, "language/systemRecovery/de.json");
    localization.registerTranslationFile(Language.EN, "language/systemRecovery/en.json");
    localization.setCurrentTranslator(translator);
  }

  @AfterEach
  void tearDown() {
    localization.currentLanguage(Language.EN);
  }

  @Test
  void dynamicWorldKeyUsesTheCurrentClientLanguage() {
    String displayKey = SystemRecoveryText.key("world.module.display-length", 5, 2);
    String scannerDisplayKey = SystemRecoveryText.key("world.scanner.display-complete", 5, 4);

    localization.currentLanguage(Language.EN);
    assertEquals(
        "Array length: 5\nGPU fault record: defective entry at index 2\nNext: write both values as two digits (add a leading zero if needed) and enter them in this order at the inventory scanner keypad.",
        translator.translate(displayKey));
    assertEquals(
        "INVENTORY SCANNER\n\nSCAN COMPLETE\n\nStorage overview:\n- Capacity: 5\n- Occupied modules: 4",
        translator.translate(scannerDisplayKey));

    localization.currentLanguage(Language.DE);
    assertEquals(
        "Array-Länge: 5\nGPU-Fehlerprotokoll: defekter Eintrag an Index 2\nAls Nächstes: Notiere beide Werte zweistellig (bei Bedarf mit führender Null) und gib sie in dieser Reihenfolge am Keypad zum Inventarscanner ein.",
        translator.translate(displayKey));
    assertEquals(
        "INVENTARSCANNER\n\nSCAN ABGESCHLOSSEN\n\nSpeicherübersicht:\n- Kapazität: 5\n- Belegte Module: 4",
        translator.translate(scannerDisplayKey));
  }

  @Test
  void arrayValueDisplaysUseListsWithoutSlotOrZeroBasedIndexNotation() {
    List<String> displayKeys =
        List.of(
            "world.energy.display-values",
            "world.module.display-values",
            "world.transport.display-values",
            "world.matrix.display-values");

    for (Language language : List.of(Language.DE, Language.EN)) {
      localization.currentLanguage(language);
      for (String displayKey : displayKeys) {
        String display = translator.translate(SystemRecoveryText.key(displayKey));
        String normalized = display.toLowerCase(Locale.ROOT);

        assertFalse(normalized.contains("slot"), displayKey + " still mentions slots");
        assertFalse(normalized.contains("index"), displayKey + " still mentions array indexes");
        assertFalse(display.contains("["), displayKey + " still uses array-index brackets");
        assertTrue(display.contains("\n- "), displayKey + " is not formatted as a list");
      }
    }
  }

  @Test
  void sameQuestLogKeyResolvesLocallyForEachClientLanguage() {
    String key = SystemRecoveryText.questKey("riddle1.tab");

    localization.currentLanguage(Language.EN);
    String english = translator.translate(key);
    localization.currentLanguage(Language.DE);
    String german = translator.translate(key);

    assertEquals("Riddle 1: Energy Supply", english);
    assertEquals("Rätsel 1: Energieversorgung", german);
    assertNotEquals(english, german);
  }

  @Test
  void everyLearningStepHasFourDedicatedHintsAndNoGenericFallback() throws IOException {
    Map<String, Object> german = languageFile(Language.DE);
    Map<String, Object> english = languageFile(Language.EN);

    assertNotNull(valueAt(german, "systemRecovery.hints.steps"));
    assertNotNull(valueAt(english, "systemRecovery.hints.steps"));
    assertEquals(null, valueAt(german, "systemRecovery.hints.generic"));
    assertEquals(null, valueAt(english, "systemRecovery.hints.generic"));

    for (SystemRecoveryLearningStep step : SystemRecoveryLearningStep.values()) {
      if (!step.isLearningStep()) continue;
      for (String stage : HINT_STAGES) {
        String key = "systemRecovery.hints.steps." + step.hintKey() + "." + stage;
        String deText = (String) valueAt(german, key);
        String enText = (String) valueAt(english, key);
        assertNotNull(deText, key + " missing in German");
        assertNotNull(enText, key + " missing in English");
        assertFalse(deText.isBlank(), key + " is blank in German");
        assertFalse(enText.isBlank(), key + " is blank in English");
        assertNotEquals(deText, enText, key + " is not translated");
      }
    }
  }

  @Test
  void systemRecoveryAndQuestLogJsonHaveMatchingLeafKeys() throws IOException {
    Map<String, Object> german = languageFile(Language.DE);
    Map<String, Object> english = languageFile(Language.EN);

    assertEquals(
        leafPaths(valueAt(german, "systemRecovery")),
        leafPaths(valueAt(english, "systemRecovery")));
    assertEquals(leafPaths(valueAt(german, "questlog")), leafPaths(valueAt(english, "questlog")));
  }

  @Test
  void everyStaticSystemRecoveryTextReferenceExistsInBothLanguages() throws IOException {
    Map<String, Object> german = languageFile(Language.DE);
    Map<String, Object> english = languageFile(Language.EN);
    Set<String> sourceReferences = staticTextReferences();

    assertFalse(sourceReferences.isEmpty());
    for (String reference : sourceReferences) {
      String[] parts = reference.split("\\|", 2);
      String root =
          parts[0].equals("questKey") || parts[0].equals("quest") ? "questlog" : "systemRecovery";
      String path =
          parts[0].equals("story") || parts[0].equals("phoneCall") ? "story." + parts[1] : parts[1];
      if (path.endsWith(".")) continue;
      assertNotNull(valueAt(german, root + "." + path), "Missing DE key: " + root + "." + path);
      assertNotNull(valueAt(english, root + "." + path), "Missing EN key: " + root + "." + path);
    }
  }

  private static Map<String, Object> languageFile(Language language) throws IOException {
    Path path = Path.of("assets/language/systemRecovery/" + language + ".json");
    return JsonHandler.readJson(Files.readString(path));
  }

  private static Set<String> staticTextReferences() throws IOException {
    Set<String> references = new TreeSet<>();
    try (var paths = Files.walk(Path.of("src/rooms/systemRecovery"))) {
      for (Path path : paths.filter(file -> file.toString().endsWith(".java")).toList()) {
        Matcher matcher = TEXT_KEY_PATTERN.matcher(Files.readString(path));
        while (matcher.find()) {
          references.add(matcher.group(1) + "|" + matcher.group(2));
        }
      }
    }
    return references;
  }

  private static Object valueAt(Map<String, Object> root, String dottedPath) {
    Object current = root;
    for (String segment : dottedPath.split("\\.")) {
      if (!(current instanceof Map<?, ?> object)) return null;
      current = object.get(segment);
      if (current == null) return null;
    }
    return current;
  }

  private static Set<String> leafPaths(Object value) {
    Set<String> leaves = new HashSet<>();
    collectLeafPaths(value, "", leaves);
    return leaves;
  }

  private static void collectLeafPaths(Object value, String prefix, Set<String> leaves) {
    if (value instanceof Map<?, ?> object) {
      object.forEach(
          (key, child) ->
              collectLeafPaths(
                  child, prefix.isEmpty() ? key.toString() : prefix + "." + key, leaves));
      return;
    }
    leaves.add(prefix);
  }
}
