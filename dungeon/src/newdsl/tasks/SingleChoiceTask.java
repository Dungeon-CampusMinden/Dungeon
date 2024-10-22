package newdsl.tasks;

import newdsl.ast.ASTNodes;
import newdsl.foreigncode.PythonHandler;
import newdsl.interpreter.Environment;

import java.util.HashSet;
import java.util.Set;

public class SingleChoiceTask extends Task<ChoiceAnswer> {

    public SingleChoiceTask(String id, Environment env) {
        super(ASTNodes.TaskType.SINGLE_CHOICE, id, env);
    }

    @Override
    public float gradeTask(Set<ChoiceAnswer> givenAnswers) {
        if (!getCustomPointsCode().equals(DEFAULT_CUSTOM_POINTS_CODE)) {
            float score = PythonHandler.handleChoiceScoring(getCustomPointsCode(), givenAnswers, new HashSet<>(this.getAnswers()));
            return score;
        }

        float pointsPerAnswer = 1;

        ChoiceAnswer correctAnswer = this.getAnswers().stream().filter(ChoiceAnswer::isCorrect).findFirst().get();
        boolean providedCorrectAnswer = givenAnswers.stream().anyMatch(ans -> ans.getText().equals(correctAnswer.getText()));

        float score = providedCorrectAnswer ? pointsPerAnswer : 0;

        if (pass(score)) setState(TaskState.FINISHED_CORRECT);
        else setState(TaskState.FINISHED_WRONG);

        return score;
    }

    @Override
    public boolean pass(float points) {
        if (!getCustomPassCode().equals(DEFAULT_CUSTOM_PASS_CODE)) {
            boolean isPass = PythonHandler.handlePassForChoiceAnswer(getCustomPassCode(), points);
            return isPass;
        }

        return points > 0;
    }

    @Override
    public String correctAnswersAsString() {
        return String.join(",", this.getAnswers().stream().filter(ChoiceAnswer::isCorrect).map(ChoiceAnswer::getText).toList());
    }

}
