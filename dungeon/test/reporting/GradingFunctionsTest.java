package reporting;

import static org.junit.Assert.assertEquals;

import contrib.entities.EntityFactory;

import core.Game;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import task.*;
import task.quizquestion.MultipleChoice;
import task.quizquestion.SingleChoice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GradingFunctionsTest {

    @Before
    public void setup() {
        try {
            Game.hero(EntityFactory.newHero());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
        sc.points(points, points);
        assertEquals(points, sc.gradeTask(Set.of(sc.contentByIndex(1))), 0.00001f);
    }

    @Test
    public void singlechoice_wrongAnswer() {
        SingleChoice sc = setupSingleChoiceTask();
        float points = 5f;
        sc.points(points, points);
        assertEquals(0, sc.gradeTask(Set.of(sc.contentByIndex(0))), 0.00001f);
    }

    @Test
    public void singlechoice_multipleAnswers() {
        SingleChoice sc = setupSingleChoiceTask();
        float points = 5f;
        sc.points(points, points);
        assertEquals(0, sc.gradeTask(Set.of(sc.contentByIndex(0), sc.contentByIndex(1))), 0.00001f);
    }

    @Test
    public void multipechoice_correctAnswer() {
        MultipleChoice mc = setupMultipleChoiceTask();
        float points = 4f;
        mc.points(points, points);
        assertEquals(
                points, mc.gradeTask(Set.of(mc.contentByIndex(0), mc.contentByIndex(1))), 0.00001f);
    }

    @Test
    public void multipechoice_wrongAnswer() {
        MultipleChoice mc = setupMultipleChoiceTask();
        float points = 4f;
        mc.points(points, points);
        assertEquals(0, mc.gradeTask(Set.of(mc.contentByIndex(2), mc.contentByIndex(3))), 0.00001f);
    }

    @Test
    public void multipechoice_someRightSomeWrongAnswer() {
        MultipleChoice mc = setupMultipleChoiceTask();
        float points = 4f;
        mc.points(points, points);
        assertEquals(
                2,
                mc.gradeTask(
                        Set.of(mc.contentByIndex(0), mc.contentByIndex(1), mc.contentByIndex(2))),
                0.00001f);
    }

    @Test
    public void multiplechoice_oneRightAnswer() {
        MultipleChoice mc = setupMultipleChoiceTask();
        float points = 4f;
        mc.points(points, points);
        assertEquals(2, mc.gradeTask(Set.of(mc.contentByIndex(0))), 0.00001f);
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
        rt.points(points, points);
        assertEquals(points, rt.gradeTask(Set.of(a, b)), 0.00001f);
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
        rt.points(points, points);
        assertEquals(2, rt.gradeTask(Set.of(a)), 0.00001f);
    }

    @Test
    public void assign_correct_easy() {
        AssignTask task = new AssignTask();
        Element c = new Element(task, "container 1");
        Element e1 = new Element(task, "e1");
        Element e2 = new Element(task, "e2");
        Element c2 = new Element(task, "container 2");
        Element e3 = new Element(task, "e3");
        Element e4 = new Element(task, "e4");
        Map<Element, Set<Element>> sol = new HashMap<>();
        sol.put(c, Set.of(e1, e2));
        sol.put(c2, Set.of(e3, e4));

        Map<Element, Set<Element>> solCopy = new HashMap<>(sol);
        Element givenSol = new Element(task, solCopy);
        task.solution(sol);
        float points = 4f;
        task.points(points, points);
        task.scoringFunction(GradingFunctions.assignGradingEasy());
        assertEquals(points, task.gradeTask(Set.of(givenSol)), 0.00001f);
    }

    @Test
    public void assign_wrong_easy() {
        AssignTask task = new AssignTask();
        Element c = new Element(task, "container 1");
        Element e1 = new Element(task, "e1");
        Element e2 = new Element(task, "e2");
        Element c2 = new Element(task, "container 2");
        Element e3 = new Element(task, "e3");
        Element e4 = new Element(task, "e4");
        Map<Element, Set<Element>> sol = new HashMap<>();
        sol.put(c, Set.of(e1, e2));
        sol.put(c2, Set.of(e3, e4));

        Map<Element, Set<Element>> wrongSol = new HashMap<>();
        wrongSol.put(c, Set.of(e3, e4));
        wrongSol.put(c2, Set.of(e1, e2));

        Element givenSol = new Element(task, wrongSol);
        task.solution(sol);
        float points = 4f;
        task.points(points, points);
        task.scoringFunction(GradingFunctions.assignGradingEasy());
        assertEquals(0, task.gradeTask(Set.of(givenSol)), 0.00001f);
    }

    @Test
    public void assign_oneCorrectOneWrong_easy() {
        AssignTask task = new AssignTask();
        Element c = new Element(task, "container 1");
        Element e1 = new Element(task, "e1");
        Element e2 = new Element(task, "e2");
        Element c2 = new Element(task, "container 2");
        Element e3 = new Element(task, "e3");
        Element e4 = new Element(task, "e4");
        Map<Element, Set<Element>> sol = new HashMap<>();
        sol.put(c, Set.of(e1, e2));
        sol.put(c2, Set.of(e3, e4));

        Map<Element, Set<Element>> wrongSol = new HashMap<>();
        wrongSol.put(c, Set.of(e1, e2));
        wrongSol.put(c2, Set.of(e1, e2));

        Element givenSol = new Element(task, wrongSol);
        task.solution(sol);
        float points = 4f;
        task.points(points, points);
        task.scoringFunction(GradingFunctions.assignGradingEasy());
        assertEquals(2, task.gradeTask(Set.of(givenSol)), 0.00001f);
    }

    @Test
    public void assign_oneCorrectOneWrongPerContainer_easy() {
        AssignTask task = new AssignTask();
        Element c = new Element(task, "container 1");
        Element e1 = new Element(task, "e1");
        Element e2 = new Element(task, "e2");
        Element c2 = new Element(task, "container 2");
        Element e3 = new Element(task, "e3");
        Element e4 = new Element(task, "e4");
        Map<Element, Set<Element>> sol = new HashMap<>();
        sol.put(c, Set.of(e1, e2));
        sol.put(c2, Set.of(e3, e4));

        Map<Element, Set<Element>> wrongSol = new HashMap<>();
        wrongSol.put(c, Set.of(e1, e3));
        wrongSol.put(c2, Set.of(e4, e2));

        Element givenSol = new Element(task, wrongSol);
        task.solution(sol);
        float points = 4f;
        task.points(points, points);
        task.scoringFunction(GradingFunctions.assignGradingEasy());
        assertEquals(2, task.gradeTask(Set.of(givenSol)), 0.00001f);
    }

    @Test
    public void assign_correct_hard() {
        AssignTask task = new AssignTask();
        Element c = new Element(task, "container 1");
        Element e1 = new Element(task, "e1");
        Element e2 = new Element(task, "e2");
        Element c2 = new Element(task, "container 2");
        Element e3 = new Element(task, "e3");
        Element e4 = new Element(task, "e4");
        Map<Element, Set<Element>> sol = new HashMap<>();
        sol.put(c, Set.of(e1, e2));
        sol.put(c2, Set.of(e3, e4));

        Map<Element, Set<Element>> solCopy = new HashMap<>(sol);
        Element givenSol = new Element(task, solCopy);
        task.solution(sol);
        float points = 4f;
        task.points(points, points);
        task.scoringFunction(GradingFunctions.assignGradingHard());
        assertEquals(points, task.gradeTask(Set.of(givenSol)), 0.00001f);
    }

    @Test
    public void assign_wrong_hard() {
        AssignTask task = new AssignTask();
        Element c = new Element(task, "container 1");
        Element e1 = new Element(task, "e1");
        Element e2 = new Element(task, "e2");
        Element c2 = new Element(task, "container 2");
        Element e3 = new Element(task, "e3");
        Element e4 = new Element(task, "e4");
        Map<Element, Set<Element>> sol = new HashMap<>();
        sol.put(c, Set.of(e1, e2));
        sol.put(c2, Set.of(e3, e4));

        Map<Element, Set<Element>> wrongSol = new HashMap<>();
        wrongSol.put(c, Set.of(e3, e4));
        wrongSol.put(c2, Set.of(e1, e2));

        Element givenSol = new Element(task, wrongSol);
        task.solution(sol);
        float points = 4f;
        task.points(points, points);
        task.scoringFunction(GradingFunctions.assignGradingHard());
        assertEquals(0, task.gradeTask(Set.of(givenSol)), 0.00001f);
    }

    @Test
    public void assign_oneCorrectOneWrong_hard() {
        AssignTask task = new AssignTask();
        Element c = new Element(task, "container 1");
        Element e1 = new Element(task, "e1");
        Element e2 = new Element(task, "e2");
        Element c2 = new Element(task, "container 2");
        Element e3 = new Element(task, "e3");
        Element e4 = new Element(task, "e4");
        Map<Element, Set<Element>> sol = new HashMap<>();
        sol.put(c, Set.of(e1, e2));
        sol.put(c2, Set.of(e3, e4));

        Map<Element, Set<Element>> wrongSol = new HashMap<>();
        wrongSol.put(c, Set.of(e1, e2));
        wrongSol.put(c2, Set.of(e1, e2));

        Element givenSol = new Element(task, wrongSol);
        task.solution(sol);
        float points = 4f;
        task.points(points, points);
        task.scoringFunction(GradingFunctions.assignGradingHard());
        assertEquals(0, task.gradeTask(Set.of(givenSol)), 0.00001f);
    }

    @Test
    public void assign_oneCorrectOneWrongPerContainer_hard() {
        AssignTask task = new AssignTask();
        Element c = new Element(task, "container 1");
        Element e1 = new Element(task, "e1");
        Element e2 = new Element(task, "e2");
        Element c2 = new Element(task, "container 2");
        Element e3 = new Element(task, "e3");
        Element e4 = new Element(task, "e4");
        Map<Element, Set<Element>> sol = new HashMap<>();
        sol.put(c, Set.of(e1, e2));
        sol.put(c2, Set.of(e3, e4));

        Map<Element, Set<Element>> wrongSol = new HashMap<>();
        wrongSol.put(c, Set.of(e1, e3));
        wrongSol.put(c2, Set.of(e4, e2));

        Element givenSol = new Element(task, wrongSol);
        task.solution(sol);
        float points = 4f;
        task.points(points, points);
        task.scoringFunction(GradingFunctions.assignGradingHard());
        assertEquals(0, task.gradeTask(Set.of(givenSol)), 0.00001f);
    }

    @Test
    public void changeState_correct() {
        SingleChoice sc = setupSingleChoiceTask();
        sc.points(1, 1);
        sc.scoringFunction(GradingFunctions.singleChoiceGrading());
        sc.gradeTask(Set.of(sc.contentByIndex(1)));
        assertEquals(Task.TaskState.FINISHED_CORRECT, sc.state());
    }

    @Test
    public void changeState_wrong() {
        SingleChoice sc = setupSingleChoiceTask();
        sc.points(1, 1);
        sc.scoringFunction(GradingFunctions.singleChoiceGrading());
        sc.gradeTask(Set.of(sc.contentByIndex(0)));
        assertEquals(Task.TaskState.FINISHED_WRONG, sc.state());
    }

    @Test
    public void changeState_notEnoughCorrect() {
        MultipleChoice mc = setupMultipleChoiceTask();
        float points = 4f;
        mc.points(points, points);
        mc.gradeTask(Set.of(mc.contentByIndex(0)));
        assertEquals(Task.TaskState.FINISHED_WRONG, mc.state());
    }
}
