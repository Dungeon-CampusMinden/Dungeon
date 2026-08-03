package wizard.runner.canonical;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/** Deterministic complete project-input identity tests. */
final class HostInputIdentityTest {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  void matchesDocumentedFoundationExampleHostInputHash() throws IOException {
    Path project = Path.of("examples", "foundation-v0.3").toAbsolutePath().normalize();

    assertEquals(
        "dae5bb87ac99829db98f0c15be6c515ce5836dd35a89dce0af5c3b0dc62fd47b",
        HostInputIdentity.sha256(MAPPER.readTree(project.resolve("deer.json").toFile())));
  }

  @Test
  void ignoresPropertyOrderAndWhitespace() throws Exception {
    JsonNode first = MAPPER.readTree("{\"room\":\"stable\",\"seed\":7}");
    JsonNode reordered = MAPPER.readTree("{\n  \"seed\": 7,\n  \"room\": \"stable\"\n}");

    String identity = HostInputIdentity.sha256(first);

    assertEquals(identity, HostInputIdentity.sha256(reordered));
    assertEquals(64, identity.length());
  }

  @Test
  void completeDeerContentAffectsIdentity() throws Exception {
    JsonNode firstSeed = MAPPER.readTree("{\"room\":\"stable\",\"seed\":7}");
    JsonNode secondSeed = MAPPER.readTree("{\"room\":\"stable\",\"seed\":8}");
    JsonNode changedSource =
        MAPPER.readTree("{\"room\":\"stable\",\"seed\":7,\"source\":{\"license\":\"CC0\"}}");

    String baseline = HostInputIdentity.sha256(firstSeed);

    assertNotEquals(baseline, HostInputIdentity.sha256(secondSeed));
    assertNotEquals(baseline, HostInputIdentity.sha256(changedSource));
  }
}
