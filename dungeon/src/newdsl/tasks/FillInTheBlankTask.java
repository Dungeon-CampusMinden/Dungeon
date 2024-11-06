package newdsl.tasks;

import newdsl.ast.ASTNodes;
import newdsl.interpreter.Environment;

import java.util.Set;

public class FillInTheBlankTask extends Task<ChoiceAnswer> {

    public FillInTheBlankTask(String id, Environment env) {
        super(ASTNodes.TaskType.FILL_IN_THE_BLANK, id, env);
    }

    @Override
    public float gradeTask(Set<ChoiceAnswer> answers) {
        return 0;
    }

    @Override
    public boolean pass(float points) {
        return false;
    }

    @Override
    public String correctAnswersAsString() {
        return "";
    }
}
