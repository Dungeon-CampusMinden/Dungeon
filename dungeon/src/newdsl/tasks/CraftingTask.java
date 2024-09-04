package newdsl.tasks;

import newdsl.ast.ASTNodes;
import newdsl.interpreter.Environment;

import java.util.Set;

public class CraftingTask extends Task<CraftingAnswer> {
    public CraftingTask(String id, Environment env) {
        super(ASTNodes.TaskType.CRAFTING, id, env);
    }

    @Override
    public float gradeTask(Set<CraftingAnswer> answers) {
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
