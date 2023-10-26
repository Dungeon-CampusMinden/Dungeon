package reporting;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import task.Quiz;
import task.quizquestion.SingleChoice;

import java.util.Set;

public class GradingFunctionsTest {

    private SingleChoice setupSingleChoiceTask() {
        SingleChoice sc = new SingleChoice("Dummy");
        sc.addAnswer(new Quiz.Content("A"));
        sc.addAnswer(new Quiz.Content("B"));
        sc.addCorrectAnswerIndex(1);
        sc.scoringFunction(GradingFunctions.singleChoiceGrading());
        return sc;
    }

    @Test
    public void singlechoice_correctAnswer() {
        SingleChoice sc = setupSingleChoiceTask();
        float points = 5f;
        sc.points(points);
        assertEquals(points, sc.executeScoringFunction(Set.of(sc.contentByIndex(1))), 0.00001f);
    }

    @Test
    public void singlechoice_wrongAnswer() {
        SingleChoice sc = setupSingleChoiceTask();
        float points = 5f;
        sc.points(points);
        assertEquals(0, sc.executeScoringFunction(Set.of(sc.contentByIndex(0))), 0.00001f);
    }
}
