package rooms.morphco.puzzles;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MorphemeQuestionsTest {

  @Test
  void hatchOpensWithTheEightTypesOfFreeMorphemes() {
    assertTrue(MorphemeQuestions.hatchAnswer("8"));
    assertTrue(MorphemeQuestions.hatchAnswer(" 08 "));
    assertFalse(MorphemeQuestions.hatchAnswer("9"));
    assertFalse(MorphemeQuestions.hatchAnswer(null));
  }

  @Test
  void ueberlegungenConsistsOfFourMorphemes() {
    assertTrue(MorphemeQuestions.restartAnswer("4"));
    assertFalse(MorphemeQuestions.restartAnswer("3"));
    assertFalse(MorphemeQuestions.restartAnswer(""));
  }
}
