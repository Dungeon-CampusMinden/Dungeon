package feature.credits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import engine.language.Language;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class CreditsDefinitionTest {

  @Test
  void parsesOrderedLocalizedSectionsAndEntries() {
    CreditsDefinition definition = CreditsDefinition.parse(validJson());

    assertEquals("sample-room", definition.roomId());
    assertEquals("Sample Room", definition.title().text(Language.EN));
    assertEquals("Entwicklung", definition.sections().get(0).headline().text(Language.DE));
    assertEquals("Developer One", definition.sections().get(0).entries().get(0).name());
    assertEquals(
        "Lead Developer", definition.sections().get(0).entries().get(0).role().text(Language.EN));
    assertEquals(
        "Thanks for playing",
        definition.sections().get(1).entries().get(0).description().text(Language.EN));
  }

  @Test
  void localizedTextFallsBackToAvailableLanguage() {
    CreditsDefinition.LocalizedText text =
        new CreditsDefinition.LocalizedText(java.util.Map.of("de", "Danke"));

    assertEquals("Danke", text.text(Language.EN));
  }

  @Test
  void rejectsUnsupportedSchemaVersion() {
    String invalid = validJson().replace("\"schemaVersion\": 1", "\"schemaVersion\": 2");

    assertThrows(IllegalArgumentException.class, () -> CreditsDefinition.parse(invalid));
  }

  @Test
  void rejectsEntryWithoutNameOrDescription() {
    String invalid = validJson().replace("\"name\": \"Developer One\",", "");

    assertThrows(IllegalArgumentException.class, () -> CreditsDefinition.parse(invalid));
  }

  @Test
  void buildsSafeInternalAssetPath() {
    assertEquals("credits/sample-room.json", CreditsDefinition.resourcePath("sample-room"));
    assertThrows(IllegalArgumentException.class, () -> CreditsDefinition.resourcePath("../room"));
  }

  @Test
  void shippedRoomDefinitionsMatchTheirAssetNames() throws IOException {
    assertAssetMatchesRoom("the-last-hour");
    assertAssetMatchesRoom("system-recovery");
  }

  private void assertAssetMatchesRoom(String roomId) throws IOException {
    String assetPath = CreditsDefinition.resourcePath(roomId);
    try (var stream = getClass().getClassLoader().getResourceAsStream(assetPath)) {
      assertNotNull(stream, "Expected bundled credits asset " + assetPath);
      CreditsDefinition definition =
          CreditsDefinition.parse(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
      assertEquals(roomId, definition.roomId());
    }
  }

  private String validJson() {
    return """
        {
          "schemaVersion": 1,
          "roomId": "sample-room",
          "title": {"de": "Beispielraum", "en": "Sample Room"},
          "sections": [
            {
              "id": "development",
              "headline": {"de": "Entwicklung", "en": "Development"},
              "entries": [
                {
                  "name": "Developer One",
                  "role": {"de": "Leitende Entwicklung", "en": "Lead Developer"}
                }
              ]
            },
            {
              "id": "thanks",
              "headline": {"de": "Danke", "en": "Thanks"},
              "entries": [
                {"description": {"de": "Danke fürs Spielen", "en": "Thanks for playing"}}
              ]
            }
          ]
        }
        """;
  }
}
