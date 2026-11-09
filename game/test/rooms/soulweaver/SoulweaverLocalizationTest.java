package rooms.soulweaver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Game;
import engine.language.Language;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rooms.soulweaver.level.SoulweaverHelp;
import rooms.soulweaver.modules.decisions.DecisionMaze;
import rooms.soulweaver.modules.methods.MethodsRoute;
import rooms.soulweaver.modules.methods.MethodsWorkshop;
import rooms.soulweaver.modules.variables.GolemProperty;
import rooms.soulweaver.modules.variables.SoulVessel;
import rooms.soulweaver.util.SoulweaverText;
import rooms.soulweaver.util.SoulweaverTranslator;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Checks that both language files match and that the room only uses keys that exist. */
class SoulweaverLocalizationTest {

  private static final JsonMapper JSON = JsonMapper.builder().build();
  private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\d+");
  private static final Pattern TEXT_CALL = Pattern.compile("SoulweaverText\\.(?:key|text)\\(");
  private static final Pattern KEY_LITERAL =
      Pattern.compile("\"([a-z][A-Za-z0-9_-]*(?:\\.[A-Za-z0-9_-]*)+)\"");
  private static final List<String> PUZZLES =
      List.of(
          "vessels",
          "essences",
          "cellar-0",
          "cellar-1",
          "cellar-2",
          "cellar-3",
          "cellar-4",
          "methods",
          "decisions");

  private Language previousLanguage;

  @BeforeEach
  void registerTranslations() {
    previousLanguage = Game.localization().currentLanguage();
    Game.localization().registerTranslationFile(Language.DE, "language/soulweaver/de.json");
    Game.localization().registerTranslationFile(Language.EN, "language/soulweaver/en.json");
  }

  @AfterEach
  void restoreLanguage() {
    Game.localization().currentLanguage(previousLanguage);
    SoulweaverHelp.reset();
  }

  @Test
  void germanAndEnglishFilesHaveTheSameKeysAndPlaceholders() throws IOException {
    Map<String, String> german = flat("de");
    Map<String, String> english = flat("en");
    assertEquals(german.keySet(), english.keySet());
    german.forEach(
        (key, text) -> assertEquals(placeholders(text), placeholders(english.get(key)), key));
  }

  @Test
  void everyKeyLiteralInTheSourcesExists() throws IOException {
    Set<String> keys = flat("de").keySet();
    List<String> missing = new ArrayList<>();
    try (Stream<Path> files = Files.walk(Path.of("src/rooms/soulweaver"))) {
      for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
        String source = Files.readString(file);
        Matcher call = TEXT_CALL.matcher(source);
        while (call.find()) {
          Matcher literal = KEY_LITERAL.matcher(firstArgument(source, call.end()));
          while (literal.find()) {
            String key = "soulweaver." + literal.group(1);
            // A literal ending in a separator is the prefix of a key completed at runtime.
            boolean prefix = key.endsWith(".") || key.endsWith("-");
            if (prefix ? keys.stream().noneMatch(k -> k.startsWith(key)) : !keys.contains(key))
              missing.add(file.getFileName() + ": " + literal.group(1));
          }
        }
      }
    }
    assertTrue(missing.isEmpty(), "Missing keys: " + missing);
  }

  @Test
  void keysCompletedAtRuntimeResolveInBothLanguages() {
    SoulweaverHelp.receive(SoulweaverHelp.encode(helpWithEveryHint()));
    SoulweaverHelp.State help = SoulweaverHelp.state().orElseThrow();
    for (Language language : List.of(Language.DE, Language.EN)) {
      Game.localization().currentLanguage(language);
      List<String> texts = new ArrayList<>();
      for (GolemProperty property : GolemProperty.values()) {
        texts.add(SoulweaverText.text(property.labelKey()));
        texts.add(
            SoulweaverText.text("binding.book.note." + property.name().toLowerCase(Locale.ROOT)));
      }
      for (SoulVessel vessel : SoulVessel.values()) {
        texts.add(SoulweaverText.text(vessel.labelKey()));
        texts.add(SoulweaverText.text(vessel.capacityKey()));
      }
      for (MethodsRoute.Action action : MethodsRoute.Action.values())
        texts.add(SoulweaverText.text("methods.action." + action.name().toLowerCase(Locale.ROOT)));
      for (MethodsWorkshop.ResultMode mode : MethodsWorkshop.ResultMode.values())
        texts.add(SoulweaverText.text("methods.mode." + mode.name().toLowerCase(Locale.ROOT)));
      for (MethodsWorkshop.CheckStatus status : MethodsWorkshop.CheckStatus.values())
        texts.add(
            SoulweaverText.text("methods.ui.check." + status.name().toLowerCase(Locale.ROOT)));
      for (int junction = 0; junction < 6; junction++) {
        texts.add(DecisionMaze.rune(junction));
        texts.add(SoulweaverText.translate(DecisionMaze.event(junction)));
      }
      texts.add(DecisionMaze.start(0).label());
      for (int checkpoint = 0; checkpoint < 5; checkpoint++)
        texts.add(SoulweaverText.text("help.rune-location." + checkpoint));
      for (SoulweaverHelp.ReleasedHint hint : help.history()) {
        texts.add(SoulweaverText.translate(hint.title()));
        texts.add(SoulweaverText.translate(hint.text()));
      }
      for (String puzzle : PUZZLES) {
        SoulweaverHelp.receive(
            SoulweaverHelp.encode(
                new SoulweaverHelp.State(puzzle, "", "", List.of(), 0, false, false, "")));
        texts.add(SoulweaverText.translate(SoulweaverHelp.state().orElseThrow().goal()));
      }
      texts.forEach(
          text ->
              assertFalse(text.contains("{soulweaver."), language + ": missing key in " + text));
    }
  }

  @Test
  void translatorResolvesNestedArgumentsAndKeepsMarkup() {
    SoulweaverTranslator translator = new SoulweaverTranslator();
    String feedback =
        SoulweaverText.key(
            "binding.feedback.vessel-set",
            SoulweaverText.key(GolemProperty.NAME.labelKey()),
            SoulweaverText.key(SoulVessel.PARCHMENT.labelKey()));
    String questLog =
        "[color=#dbb463]"
            + SoulweaverText.key("progress.rune-title", 3, "do-while")
            + "[/color]\n> "
            + SoulweaverText.key("progress.task.vessels");

    Game.localization().currentLanguage(Language.DE);
    assertEquals("Name: Pergament eingesetzt.", translator.translate(feedback));
    assertEquals(
        "[color=#dbb463]Schleifenrune 3 · do-while[/color]\n> "
            + "Ordne jeder Eigenschaft ein passendes Gefäß zu.",
        translator.translate(questLog));

    Game.localization().currentLanguage(Language.EN);
    assertEquals("Name: Parchment inserted.", translator.translate(feedback));
    assertEquals(
        "[color=#dbb463]Loop rune 3 · do-while[/color]\n> Give each property a matching vessel.",
        translator.translate(questLog));
  }

  @Test
  void trackedFailureReasonsStayGermanInAnEnglishProcess() {
    Game.localization().currentLanguage(Language.EN);
    String reason = SoulweaverText.key("loops.failure.pit-ahead");

    assertEquals("Pit ahead. Step stopped.", SoulweaverText.translate(reason));
    assertEquals("Grube voraus. Schritt gestoppt.", SoulweaverText.german(reason));
  }

  /**
   * @param source Java source
   * @param start index directly after the opening parenthesis of a call
   * @return source of the call's first argument, which may be a ternary or a concatenation
   */
  private static String firstArgument(String source, int start) {
    int depth = 0;
    boolean string = false;
    for (int index = start; index < source.length(); index++) {
      char character = source.charAt(index);
      if (string) {
        if (character == '\\') index++;
        else if (character == '"') string = false;
      } else if (character == '"') string = true;
      else if (character == '(') depth++;
      else if (character == ')' && depth-- == 0 || character == ',' && depth == 0)
        return source.substring(start, index);
    }
    return source.substring(start);
  }

  private static SoulweaverHelp.State helpWithEveryHint() {
    List<SoulweaverHelp.ReleasedHint> hints = new ArrayList<>();
    for (String puzzle : PUZZLES)
      for (int step = 1; step <= 3; step++)
        hints.add(new SoulweaverHelp.ReleasedHint(puzzle, "", step, ""));
    return new SoulweaverHelp.State("vessels", "", "", hints, 0, false, false, "");
  }

  private static Map<String, String> flat(String language) throws IOException {
    try (InputStream in =
        SoulweaverLocalizationTest.class
            .getClassLoader()
            .getResourceAsStream("language/soulweaver/" + language + ".json")) {
      Map<String, String> out = new TreeMap<>();
      collect("", JSON.readTree(new String(in.readAllBytes(), StandardCharsets.UTF_8)), out);
      return out;
    }
  }

  private static void collect(String prefix, JsonNode node, Map<String, String> out) {
    if (!node.isObject()) {
      out.put(prefix, node.asString());
      return;
    }
    node.properties()
        .forEach(
            entry ->
                collect(
                    prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey(),
                    entry.getValue(),
                    out));
  }

  private static Set<String> placeholders(String text) {
    Set<String> found = new TreeSet<>();
    Matcher matcher = PLACEHOLDER.matcher(text);
    while (matcher.find()) found.add(matcher.group());
    return found;
  }
}
