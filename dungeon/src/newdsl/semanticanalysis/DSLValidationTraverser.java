package newdsl.semanticanalysis;

import newdsl.ast.ASTNodes;
import newdsl.ast.ASTTraverser;
import newdsl.common.DSLError;
import newdsl.symboltable.SymbolTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DSLValidationTraverser extends ASTTraverser<ASTNodes.Visitable> {

    SymbolTable symbolTable;
    ArrayList<DSLError> errors;

    public DSLValidationTraverser(SymbolTable symbolTable, ArrayList<DSLError> errors) {
        this.symbolTable = symbolTable;
        this.errors = errors;
    }

    private void checkAnswerCompatibility(ASTNodes.TaskType taskType, ASTNodes.AnswerNode answerNode) {
        boolean isCompatible = AnswerCompatibilityChecker.isCompatible(taskType, answerNode);

        if (!isCompatible) {
            errors.add(new DSLError(String.format("Given answer is not compatible with a %s task: Please make sure to use the correct answer type for this question", taskType), answerNode.sourceLocation));
        }

    }

    private void checkAnswerValidity(ASTNodes.TaskType taskType, ASTNodes.TaskHeaderNode header, ASTNodes.AnswerListNode answers) {
        Optional<String> violation = AnswerValidator.isValid(taskType, answers);
        violation.ifPresent(violationText -> errors.add(new DSLError(String.format("Given answers violate the requirements for a %s task: %s", header.taskType, violationText), answers.sourceLocation)));
    }

    private void checkAnswers(ASTNodes.TaskType taskType, ASTNodes.TaskHeaderNode header, ASTNodes.TaskBodyNode body) {
        ASTNodes.TaskAnswersNode answers = body.answers;

        if (answers instanceof ASTNodes.AnswerListNode) {
            ((ASTNodes.AnswerListNode) answers).answers.forEach(answerNode -> {
                checkAnswerCompatibility(taskType, answerNode);
            });
            checkAnswerValidity(taskType, header, (ASTNodes.AnswerListNode) answers);
        }
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.StartNode startNode) {
        for (ASTNodes.Visitable child : startNode.children) {
            if (child instanceof ASTNodes.ImportStatementNode) {
                visit((ASTNodes.ImportStatementNode) child);
            } else if (child instanceof ASTNodes.TaskNode) {
                visit((ASTNodes.TaskNode) child);
            } else if (child instanceof ASTNodes.TaskCompositionNode) {
                visit((ASTNodes.TaskCompositionNode) child);
            } else if (child instanceof ASTNodes.TaskConfigNode) {
                visit((ASTNodes.TaskConfigNode) child);
            } else {
                throw new UnsupportedOperationException(String.format("Missing implementation for %s.", child.getClass().getName()));
            }

        }

        return startNode;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.ImportStatementNode importStatementNode) {
        return importStatementNode;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.TaskNode taskNode) {
        ASTNodes.TaskType taskType = taskNode.taskHeader.taskType;
        if (taskNode.taskContent instanceof ASTNodes.TaskBodyNode) { // Task Body, ohne Variant
            checkAnswers(taskType, taskNode.taskHeader, (ASTNodes.TaskBodyNode) taskNode.taskContent);
        } else if (taskNode.taskContent instanceof ASTNodes.TaskVariantBodyNode) { // Task Body, ohne Variant
            List<ASTNodes.TaskVariantNode> variants = ((ASTNodes.TaskVariantBodyNode) taskNode.taskContent).variants.variants;
            variants.forEach(variant -> checkAnswers(taskType, taskNode.taskHeader, variant.taskBody));
        }
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.TaskCompositionNode taskCompositionNode) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.TaskCompositionNodeList taskCompositionNodeList) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.TaskCompositionSubtaskNode taskCompositionSubtaskNode) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.TaskConfigNode taskConfigNode) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.TaskHeaderNode taskHeaderNode) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.TaskBodyNode taskBodyNode) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.TaskVariantBodyNode taskVariantBodyNode) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.TaskVariantNode taskVariantNode) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.TaskVariantListNode taskVariantListNode) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.OptionalTaskContentNode optionalTaskContentNode) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.ChoiceAnswerNode choiceAnswerNode) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.ParameterAnswerNode parameterAnswerNode) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.MatchingAnswerNode matchingAnswerNode) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.AnswerListNode answerListNode) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.AliasTextNode aliasTextNode) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.CraftingRuleNode craftingRuleNode) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.CraftingIngredientNode craftingIngredientNode) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.CraftingSolutionNode craftingSolutionNode) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.CraftingIngredientListNode craftingIngredientListNode) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.OptionalTaskContentListNode optionalTaskContentListNode) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.TextNode textNode) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.NumberNode numberNode) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.IdNode idNode) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.CodeNode codeNode) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.CustomCodeNode customCodeNode) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.ExplanationNode explanationNode) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.AnswerSelectionOrNode answerSelectionOrNode) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.AnswerSelectionAndNode answerSelectionAndNode) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.AnswerSelectionExpressionNode answerSelectionExpressionNode) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.TaskConfigContentNode taskConfigContentNode) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.TaskConfigBranchNode taskConfigBranchNode) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.AnswerSelectionTermNode node) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.AnswerSelectionFactorNode node) {
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.AnswerSelectionParenthesisNode node) {
        return null;
    }

}
