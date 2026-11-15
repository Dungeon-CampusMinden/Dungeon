package rooms.morphco;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import rooms.morphco.util.MorphCoText;
import rooms.morphco.util.MorphCoTranslator;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Checks that both language files are complete and that every key used by the room exists. */
class MorphCoLocalizationTest {

  private static final JsonMapper JSON = JsonMapper.builder().build();
  private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\d+");

  @AfterEach
  void restoreLanguage() {
    Game.localization().currentLanguage(Language.EN);
  }

  private static JsonNode read(String language) throws IOException {
    try (InputStream in =
        MorphCoLocalizationTest.class
            .getClassLoader()
            .getResourceAsStream("language/morphCo/" + language + ".json")) {
      return JSON.readTree(new String(in.readAllBytes(), StandardCharsets.UTF_8));
    }
  }

  private static void collect(String prefix, JsonNode node, Map<String, String> out) {
    if (node.isObject()) {
      for (Iterator<Map.Entry<String, JsonNode>> it = node.properties().iterator();
          it.hasNext(); ) {
        Map.Entry<String, JsonNode> entry = it.next();
        collect(
            prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey(),
            entry.getValue(),
            out);
      }
    } else {
      out.put(prefix, node.asString());
    }
  }

  private static Map<String, String> flat(String language) throws IOException {
    Map<String, String> out = new java.util.TreeMap<>();
    collect("", read(language), out);
    return out;
  }

  private static Set<String> placeholders(String text) {
    Set<String> found = new TreeSet<>();
    Matcher matcher = PLACEHOLDER.matcher(text);
    while (matcher.find()) found.add(matcher.group());
    return found;
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
  void everyLiteralKeyInTheSourcesExists() throws IOException {
    Map<String, String> german = flat("de");
    Pattern literal =
        Pattern.compile(
            "\"((?:world|pc|ui|story|hotline|documents|items|ending|intro|menu)\\.[a-z0-9.\\-]*[a-z0-9])\"");
    List<String> missing = new ArrayList<>();
    try (Stream<Path> files = Files.walk(Path.of("src/rooms/morphco"))) {
      for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
        Matcher matcher = literal.matcher(Files.readString(file));
        while (matcher.find()) {
          String key = "morphCo." + matcher.group(1);
          if (!german.containsKey(key)
              && german.keySet().stream()
                  .noneMatch(k -> k.startsWith(key + "-") || k.startsWith(key + "."))) {
            missing.add(file.getFileName() + ": " + matcher.group(1));
          }
        }
      }
    }
    assertTrue(missing.isEmpty(), "Missing keys: " + missing);
  }

  @Test
  void translatorResolvesTransportKeysWithArguments() {
    MorphCo.initLocalization();
    Game.localization().currentLanguage(Language.DE);
    MorphCoTranslator translator = new MorphCoTranslator();
    String text =
        translator.translate(
            MorphCoText.key("world.a12.station-assembled", "{ge- ... -e}", "Gebirge"));
    assertEquals("Im mittleren Schälchen erscheint {ge- ... -e} - wie in \"Gebirge\".", text);
    String quest = translator.translate(MorphCoText.questKey("main.tab"));
    assertEquals("Auftrag", quest);
    assertFalse(
        translator.translate(MorphCoText.arrival(MorphCoText.PLAYER_IMAGE)).contains("morphCo."));
  }
}
