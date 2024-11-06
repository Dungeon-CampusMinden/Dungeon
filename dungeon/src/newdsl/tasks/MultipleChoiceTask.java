package newdsl.tasks;


import newdsl.ast.ASTNodes;
import newdsl.foreigncode.PythonHandler;
import newdsl.interpreter.Environment;
import org.python.core.PyFloat;

import java.util.List;
import java.util.Set;

public class MultipleChoiceTask extends Task<ChoiceAnswer> implements Gradable<ChoiceAnswer> {

    public MultipleChoiceTask(String id, Environment env) {
        super(ASTNodes.TaskType.MULTIPLE_CHOICE, id, env);
    }


    @Override
    public float gradeTask(Set<ChoiceAnswer> givenAnswers) {
        if(!getCustomPassCode().equals(DEFAULT_CUSTOM_PASS_CODE)){
            PyFloat grade = (PyFloat) PythonHandler.exec(getCustomPointsCode(), "grade", null);
            return 0;
        }

        float reachedPoints = 0;
        float pointsPerAnswer = 1;

        List<ChoiceAnswer> correctAnswers = this.getAnswers().stream().filter(ChoiceAnswer::isCorrect).toList();

        for (ChoiceAnswer givenAnswer : givenAnswers) {
            if (correctAnswers.stream().anyMatch(correctAnswer -> correctAnswer.getText().equals(givenAnswer.getText()))) {
                reachedPoints += pointsPerAnswer;
            }
        }

        if (pass(reachedPoints)) setState(TaskState.FINISHED_CORRECT);
        else setState(TaskState.FINISHED_WRONG);

        return reachedPoints;
    }

    @Override
    public boolean pass(float points) {
        if(!getCustomPassCode().equals(DEFAULT_CUSTOM_PASS_CODE)){
            boolean isPass = PythonHandler.handlePassForChoiceAnswer(getCustomPassCode(), points);
            return isPass;
        }

        // Mindestens 50% richtig
        return points >= ((float) getAnswers().stream().filter(ans -> ans.isCorrect()).toList().size() / 2);
    }

    @Override
    public String correctAnswersAsString() {
        return String.join(",", this.getAnswers().stream().filter(ChoiceAnswer::isCorrect).map(ChoiceAnswer::getText).toList());
    }
}

