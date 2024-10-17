package newdsl.tasks;

import newdsl.ast.ASTNodes;
import newdsl.foreigncode.PythonHandler;
import newdsl.interpreter.Environment;

import java.util.Set;

public class CalculationTask extends Task<ParameterAnswer> {

    public CalculationTask(String id, Environment env) {
        super(ASTNodes.TaskType.CALCULATION, id, env);
    }

    @Override
    public float gradeTask(Set<ParameterAnswer> answers) {
        boolean isPass = PythonHandler.gradeCalculation(getCustomSolution(), getAnswers(), answers.iterator().next());
        float score = isPass ? 1 : 0;

        if (pass(score)) setState(TaskState.FINISHED_CORRECT);
        else setState(TaskState.FINISHED_WRONG);

        return score;
    }

    @Override
    public boolean pass(float points) {
        return points > 0;
    }

    @Override
    public String correctAnswersAsString() {
        return "";
    }
}
