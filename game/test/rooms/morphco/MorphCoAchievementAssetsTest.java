package rooms.morphco;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import rooms.morphco.util.MorphCoAchievements;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Checks the achievement definitions, their images and their texts. */
class MorphCoAchievementAssetsTest {

  private static JsonNode json(String path) throws IOException {
    try (InputStream in =
        MorphCoAchievementAssetsTest.class.getClassLoader().getResourceAsStream(path)) {
      assertNotNull(in, path);
      return JsonMapper.builder()
          .build()
          .readTree(new String(in.readAllBytes(), StandardCharsets.UTF_8));
    }
  }

  @Test
  void everyAchievementHasAnImageAndTextsInBothLanguages() throws IOException {
    JsonNode definitions = json(MorphCoAchievements.DEFINITION_PATH).get("achievements");
    JsonNode german = json("language/morphCo/de.json").get("achievements");
    JsonNode english = json("language/morphCo/en.json").get("achievements");
    Set<String> ids = new HashSet<>();
    int platinum = 0;
    for (JsonNode definition : definitions) {
      String id = definition.get("id").asString();
      ids.add(id);
      if (definition.has("platinum") && definition.get("platinum").asBoolean()) platinum++;
      assertNotNull(
          getClass().getClassLoader().getResource(definition.get("imagePath").asString()), id);
      for (JsonNode texts : new JsonNode[] {german, english}) {
        assertTrue(texts.has(id), id);
        assertTrue(texts.get(id).has("name") && texts.get(id).has("description"), id);
      }
    }
    assertEquals(1, platinum);
    for (MorphCoAchievements achievement : MorphCoAchievements.values()) {
      assertTrue(ids.contains(achievement.id()), achievement.id());
    }
    assertEquals(MorphCoAchievements.values().length + 1, ids.size());
  }
}
