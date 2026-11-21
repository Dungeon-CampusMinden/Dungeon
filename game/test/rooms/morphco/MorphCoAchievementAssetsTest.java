package rooms.morphco;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import rooms.morphco.util.MorphCoAchievements;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Checks that the code and the definition file know the same achievements. */
class MorphCoAchievementAssetsTest {

  @Test
  void everyAchievementOfTheCodeIsDefinedAndViceVersa() throws IOException {
    JsonNode definitions;
    try (InputStream in =
        getClass().getClassLoader().getResourceAsStream(MorphCoAchievements.DEFINITION_PATH)) {
      assertNotNull(in, MorphCoAchievements.DEFINITION_PATH);
      definitions =
          JsonMapper.builder()
              .build()
              .readTree(new String(in.readAllBytes(), StandardCharsets.UTF_8))
              .get("achievements");
    }
    // The platinum trophy unlocks by itself once all others are unlocked; the code never names it.
    Set<String> defined = new HashSet<>();
    for (JsonNode definition : definitions) {
      if (!definition.path("platinum").asBoolean()) defined.add(definition.get("id").asString());
    }
    Set<String> named =
        Arrays.stream(MorphCoAchievements.values())
            .map(MorphCoAchievements::id)
            .collect(Collectors.toSet());
    assertEquals(named, defined);
  }
}
