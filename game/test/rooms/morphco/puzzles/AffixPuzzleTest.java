package rooms.morphco.puzzles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import rooms.morphco.puzzles.AffixPuzzle.Bin;
import rooms.morphco.puzzles.AffixPuzzle.Shaft;
import rooms.morphco.puzzles.AffixPuzzle.WordAnswer;

class AffixPuzzleTest {

  private static WordAnswer answer(List<Integer> cuts, Bin... bins) {
    return new WordAnswer(cuts, new ArrayList<>(Arrays.asList(bins)));
  }

  private static Map<String, WordAnswer> solution() {
    Map<String, WordAnswer> answers = new HashMap<>();
    answers.put("w0", answer(List.of(2, 4, 8), Bin.PRAEFIX, Bin.PRAEFIX, null, Bin.SUFFIX));
    answers.put("w1", answer(List.of(), Bin.KEIN_AFFIX));
    answers.put("w2", answer(List.of(2, 6), Bin.ZIRKUMFIX, null, Bin.ZIRKUMFIX));
    answers.put("w3", answer(List.of(4), null, Bin.SUFFIX));
    answers.put("w4", answer(List.of(3), null, Bin.SUFFIX));
    answers.put("w5", answer(List.of(), Bin.KEIN_AFFIX));
    answers.put("w6", answer(List.of(), Bin.KEIN_AFFIX));
    return answers;
  }

  @Test
  void scriptSolutionIsAccepted() {
    AffixPuzzle.Result result = AffixPuzzle.check(solution());
    assertTrue(result.solved());
    assertEquals(7, result.correctWords());
  }

  @Test
  void splitCutsAtTheGivenPositions() {
    assertEquals(
        List.of("un", "be", "zahl", "bar"),
        AffixPuzzle.split("unbezahlbar", List.of(8, 2, 4, 4, 99)));
    assertEquals(List.of("Hamster"), AffixPuzzle.split("Hamster", List.of()));
  }

  @Test
  void missingPrefixMakesTheWordWrong() {
    Map<String, WordAnswer> answers = solution();
    answers.put("w0", answer(List.of(2, 8), Bin.PRAEFIX, null, Bin.SUFFIX));
    AffixPuzzle.Result result = AffixPuzzle.check(answers);
    assertFalse(result.solved());
    assertEquals(6, result.correctWords());
  }

  @Test
  void circumfixPartsMustNotBeSortedAsPrefixAndSuffix() {
    Map<String, WordAnswer> answers = solution();
    answers.put("w2", answer(List.of(2, 6), Bin.PRAEFIX, null, Bin.SUFFIX));
    assertFalse(AffixPuzzle.check(answers).solved());
  }

  @Test
  void wordsWithoutAffixMustStayWhole() {
    Map<String, WordAnswer> answers = solution();
    answers.put("w6", answer(List.of(3), Bin.KEIN_AFFIX, null));
    assertFalse(AffixPuzzle.check(answers).solved());
  }

  @Test
  void stemsInABinAreWrong() {
    Map<String, WordAnswer> answers = solution();
    answers.put("w3", answer(List.of(4), Bin.KEIN_AFFIX, Bin.SUFFIX));
    assertFalse(AffixPuzzle.check(answers).solved());
  }

  @Test
  void finerAnalysesOfTheInflectedFormsAreAccepted() {
    Map<String, WordAnswer> answers = solution();
    answers.put("w4", answer(List.of(3, 5), null, Bin.SUFFIX, Bin.SUFFIX));
    answers.put("w2", answer(List.of(2, 7), Bin.ZIRKUMFIX, null, Bin.ZIRKUMFIX));
    assertTrue(AffixPuzzle.check(answers).solved());
  }

  @Test
  void encodedAnswersSurviveTheRoundTrip() {
    Map<String, WordAnswer> answers = solution();
    Map<String, WordAnswer> decoded = AffixPuzzle.decodeWords(AffixPuzzle.encodeWords(answers));
    assertTrue(AffixPuzzle.check(decoded).solved());
    assertEquals(0, AffixPuzzle.check(AffixPuzzle.decodeWords("w0:x:P;broken")).correctWords());
  }

  @Test
  void shaftsSeparateDerivationFromInflection() {
    Map<String, Shaft> placement = new HashMap<>();
    AffixPuzzle.affixes().forEach(affix -> placement.put(affix.id(), affix.target()));
    assertTrue(AffixPuzzle.checkShafts(placement));
    assertEquals(Shaft.FLEXION, placement.get("ge_et"));
    assertEquals(Shaft.WORTBILDUNG, placement.get("ung"));
    placement.put("ten", Shaft.WORTBILDUNG);
    assertFalse(AffixPuzzle.checkShafts(placement));
    assertEquals(1, AffixPuzzle.wrongShafts(placement));
    assertEquals(placement, AffixPuzzle.decodeShafts(AffixPuzzle.encodeShafts(placement)));
  }
}
