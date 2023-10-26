package reporting;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Test;

import task.Element;
import task.Quiz;
import task.ReplacementTask;
import task.Task;
import task.quizquestion.MultipleChoice;
import task.quizquestion.SingleChoice;

import java.util.ArrayList;
import java.util.Set;

public class GradingFunctionsTest {

    @After
    public void cleanup() {
        Task.cleanupAllTask();
    }

    private SingleChoice setupSingleChoiceTask() {
        SingleChoice sc = new SingleChoice("Dummy");
        sc.addAnswer(new Quiz.Content("A"));
        sc.addAnswer(new Quiz.Content("B"));
        sc.addCorrectAnswerIndex(1);
        sc.scoringFunction(GradingFunctions.singleChoiceGrading());
        return sc;
    }

    private MultipleChoice setupMultipleChoiceTask() {
        MultipleChoice mc = new MultipleChoice("Dummy");
        mc.addAnswer(new Quiz.Content("A"));
        mc.addAnswer(new Quiz.Content("B"));
        mc.addAnswer(new Quiz.Content("C"));
        mc.addAnswer(new Quiz.Content("C"));
        mc.addCorrectAnswerIndex(0);
        mc.addCorrectAnswerIndex(1);
        mc.scoringFunction(GradingFunctions.multipeChoiceGrading());
        return mc;
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

    @Test
    public void multipechoice_correctAnswer() {
        MultipleChoice mc = setupMultipleChoiceTask();
        float points = 4f;
        mc.points(points);
        assertEquals(
                points,
                mc.executeScoringFunction(Set.of(mc.contentByIndex(0), mc.contentByIndex(1))),
                0.00001f);
    }

    @Test
    public void multipechoice_wrongAnswer() {
        MultipleChoice mc = setupMultipleChoiceTask();
        float points = 4f;
        mc.points(points);
        assertEquals(
                0,
                mc.executeScoringFunction(Set.of(mc.contentByIndex(2), mc.contentByIndex(3))),
                0.00001f);
    }

    @Test
    public void multipechoice_someRightSomeWrongAnswer() {
        MultipleChoice mc = setupMultipleChoiceTask();
        float points = 4f;
        mc.points(points);
        assertEquals(
                2,
                mc.executeScoringFunction(
                        Set.of(mc.contentByIndex(0), mc.contentByIndex(1), mc.contentByIndex(2))),
                0.00001f);
    }

    @Test
    public void multipechoice_oneRightAnswer() {
        MultipleChoice mc = setupMultipleChoiceTask();
        float points = 4f;
        mc.points(points);
        assertEquals(2, mc.executeScoringFunction(Set.of(mc.contentByIndex(0))), 0.00001f);
    }

    @Test
    public void replacement_correctAnswer() {
        ReplacementTask rt = new ReplacementTask(new ArrayList<>());
        rt.scoringFunction(GradingFunctions.replacementGrading());
        Element a = new Element(rt, "Dummy A");
        Element b = new Element(rt, "Dummy B");
        rt.addSolution(a);
        rt.addSolution(b);
        float points = 4f;
        rt.points(points);
        assertEquals(points, rt.executeScoringFunction(Set.of(a, b)), 0.00001f);
    }

    @Test
    public void replacement_oneRightAnswer() {
        ReplacementTask rt = new ReplacementTask(new ArrayList<>());
        rt.scoringFunction(GradingFunctions.replacementGrading());
        Element a = new Element(rt, "Dummy A");
        Element b = new Element(rt, "Dummy B");
        rt.addSolution(a);
        rt.addSolution(b);
        float points = 4f;
        rt.points(points);
        assertEquals(2, rt.executeScoringFunction(Set.of(a)), 0.00001f);
    }
}
