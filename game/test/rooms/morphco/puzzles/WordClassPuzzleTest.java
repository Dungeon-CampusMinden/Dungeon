package rooms.morphco.puzzles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class WordClassPuzzleTest {

  private static Map<String, WordClassPuzzle.Crate> solution() {
    Map<String, WordClassPuzzle.Crate> placement = new HashMap<>();
    WordClassPuzzle.words()
        .forEach(word -> word.target().ifPresent(crate -> placement.put(word.id(), crate)));
    return placement;
  }

  private static String id(String text) {
    return WordClassPuzzle.words().stream()
        .filter(word -> word.text().equals(text))
        .findFirst()
        .orElseThrow()
        .id();
  }

  @Test
  void scriptHasTwoSentencesAndSixteenWords() {
    assertEquals(2, WordClassPuzzle.sentenceCount());
    assertEquals(16, WordClassPuzzle.words().size());
  }

  @Test
  void onlyInflectableWordsBelongIntoCrates() {
    assertTrue(
        WordClassPuzzle.words().stream()
            .filter(w -> w.text().equals("selten"))
            .allMatch(w -> w.target().isEmpty()));
    assertEquals(
        WordClassPuzzle.Crate.ADJEKTIVE,
        WordClassPuzzle.words().stream()
            .filter(w -> w.text().equals("spät"))
            .findFirst()
            .orElseThrow()
            .target()
            .orElseThrow());
    assertEquals(
        WordClassPuzzle.Crate.PRONOMINA,
        WordClassPuzzle.words().stream()
            .filter(w -> w.text().equals("diese"))
            .findFirst()
            .orElseThrow()
            .target()
            .orElseThrow());
    assertTrue(WordClassPuzzle.check(solution()).solved());
  }

  @Test
  void uninflectableWordInACrateIsMisplaced() {
    Map<String, WordClassPuzzle.Crate> placement = solution();
    placement.put(id("äußerst"), WordClassPuzzle.Crate.ADJEKTIVE);
    WordClassPuzzle.Result result = WordClassPuzzle.check(placement);
    assertFalse(result.solved());
    assertEquals(1, result.misplaced());
    assertEquals(0, result.missing());
  }

  @Test
  void missingAndWrongWordsAreCountedSeparately() {
    Map<String, WordClassPuzzle.Crate> placement = solution();
    placement.remove(id("Hamster"));
    placement.put(id("kommt"), WordClassPuzzle.Crate.NOMEN);
    WordClassPuzzle.Result result = WordClassPuzzle.check(placement);
    assertEquals(1, result.misplaced());
    assertEquals(1, result.missing());
  }

  @Test
  void encodedPlacementSurvivesTheRoundTrip() {
    Map<String, WordClassPuzzle.Crate> placement = solution();
    assertEquals(placement, WordClassPuzzle.decode(WordClassPuzzle.encode(placement)));
    assertTrue(WordClassPuzzle.decode("garbage;=;s0w0=UNKNOWN").isEmpty());
  }
}
