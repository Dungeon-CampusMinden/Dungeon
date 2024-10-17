package newdsl.tasks;

import newdsl.ast.ASTNodes;
import newdsl.interpreter.Environment;

import java.util.Set;

public class MatchingTask extends Task<MatchingAnswer> {

    public MatchingTask(String id, Environment env) {
        super(ASTNodes.TaskType.MATCHING, id, env);
    }

    @Override
    public float gradeTask(Set<MatchingAnswer> answers) {
        return 0;
    }

    @Override
    public boolean pass(float points) {
        return false;
    }

    @Override
    public String correctAnswersAsString() {
        return getAnswers().toString();
    }
}
