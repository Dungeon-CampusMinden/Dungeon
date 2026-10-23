package feature.audience;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

/** Tests how free-text and code votes are normalized before they are counted. */
class AudienceContextTest {

  private static Optional<String> normalized(int digits, String vote) {
    return new AudienceContext.Text(null, digits)
        .normalize(AudienceContext.JSON.stringNode(vote))
        .map(JsonNode::asString);
  }

  @Test
  void freeTextIgnoresWhitespaceAndCase() {
    assertEquals(Optional.of("o(logn)"), normalized(0, " O(log n) "));
    assertEquals(normalized(0, "o(logn)"), normalized(0, "O (LOG\tN)"));
  }

  @Test
  void blankFreeTextIsNoVote() {
    assertTrue(normalized(0, "  ").isEmpty());
  }

  @Test
  void codesKeepLeadingZerosAndNeedAllDigits() {
    assertEquals(Optional.of("047"), normalized(3, " 0 47"));
    assertTrue(normalized(3, "47").isEmpty());
  }
}
