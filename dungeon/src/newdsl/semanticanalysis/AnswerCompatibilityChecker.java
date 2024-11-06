package newdsl.semanticanalysis;

import newdsl.ast.ASTNodes;

import java.util.Arrays;
import java.util.HashMap;

public class AnswerCompatibilityChecker {

    public static boolean isCompatible(ASTNodes.TaskType taskType, ASTNodes.AnswerNode answerNode) {
        HashMap<ASTNodes.TaskType, ASTNodes.NodeType[]> map = new HashMap<>();

        map.put(ASTNodes.TaskType.SINGLE_CHOICE, new ASTNodes.NodeType[]{ASTNodes.NodeType.CHOICE_ANSWER});
        map.put(ASTNodes.TaskType.MULTIPLE_CHOICE, new ASTNodes.NodeType[]{ASTNodes.NodeType.CHOICE_ANSWER});
        map.put(ASTNodes.TaskType.FILL_IN_THE_BLANK, new ASTNodes.NodeType[]{ASTNodes.NodeType.CHOICE_ANSWER});
        map.put(ASTNodes.TaskType.MATCHING, new ASTNodes.NodeType[]{ASTNodes.NodeType.MATCHING_ANSWER});
        map.put(ASTNodes.TaskType.CRAFTING, new ASTNodes.NodeType[]{ASTNodes.NodeType.CRAFTING_INGREDIENT, ASTNodes.NodeType.CRAFTING_RULE, ASTNodes.NodeType.CRAFTING_SOLUTION});
        map.put(ASTNodes.TaskType.CALCULATION, new ASTNodes.NodeType[]{ASTNodes.NodeType.PARAMETER_ANSWER});

        return Arrays.stream(map.get(taskType)).toList().contains(answerNode.nodeType);
    }

}
