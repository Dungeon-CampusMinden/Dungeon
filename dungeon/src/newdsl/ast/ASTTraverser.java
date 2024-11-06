package newdsl.ast;

public abstract class ASTTraverser<T> {
    public abstract T visit(ASTNodes.StartNode startNode);

    public abstract T visit(ASTNodes.ImportStatementNode importStatementNode);

    public abstract T visit(ASTNodes.TaskNode taskNode);

    public abstract T visit(ASTNodes.TaskCompositionNode taskCompositionNode);

    public abstract T visit(ASTNodes.TaskCompositionNodeList taskCompositionNodeList);

    public abstract T visit(ASTNodes.TaskCompositionSubtaskNode taskCompositionSubtaskNode);

    public abstract T visit(ASTNodes.TaskConfigNode taskConfigNode);

    public abstract T visit(ASTNodes.TaskHeaderNode taskHeaderNode);

    public abstract T visit(ASTNodes.TaskBodyNode taskBodyNode);

    public abstract T visit(ASTNodes.TaskVariantBodyNode taskVariantBodyNode);

    public abstract T visit(ASTNodes.TaskVariantNode taskVariantNode);

    public abstract T visit(ASTNodes.TaskVariantListNode taskVariantListNode);

    public abstract T visit(ASTNodes.OptionalTaskContentNode optionalTaskContentNode);

    public abstract T visit(ASTNodes.ChoiceAnswerNode choiceAnswerNode);

    public abstract T visit(ASTNodes.ParameterAnswerNode parameterAnswerNode);

    public abstract T visit(ASTNodes.MatchingAnswerNode matchingAnswerNode);

    public abstract T visit(ASTNodes.AnswerListNode answerListNode);

    public abstract T visit(ASTNodes.AliasTextNode aliasTextNode);

    public abstract T visit(ASTNodes.CraftingRuleNode craftingRuleNode);

    public abstract T visit(ASTNodes.CraftingIngredientNode craftingIngredientNode);

    public abstract T visit(ASTNodes.CraftingSolutionNode craftingSolutionNode);

    public abstract T visit(ASTNodes.CraftingIngredientListNode craftingIngredientListNode);

    public abstract T visit(ASTNodes.OptionalTaskContentListNode optionalTaskContentListNode);

    public abstract T visit(ASTNodes.TextNode textNode);

    public abstract T visit(ASTNodes.NumberNode numberNode);

    public abstract T visit(ASTNodes.IdNode idNode);

    public abstract T visit(ASTNodes.CodeNode codeNode);

    public abstract T visit(ASTNodes.CustomCodeNode customCodeNode);

    public abstract T visit(ASTNodes.ExplanationNode explanationNode);

    public abstract T visit(ASTNodes.AnswerSelectionOrNode answerSelectionOrNode);

    public abstract T visit(ASTNodes.AnswerSelectionAndNode answerSelectionAndNode);

    public abstract T visit(ASTNodes.AnswerSelectionExpressionNode answerSelectionExpressionNode);

    public abstract T visit(ASTNodes.TaskConfigContentNode taskConfigContentNode);

    public abstract T visit(ASTNodes.TaskConfigBranchNode taskConfigBranchNode);


    public abstract T visit(ASTNodes.AnswerSelectionTermNode node);

    public abstract T visit(ASTNodes.AnswerSelectionFactorNode node);

    public abstract T visit(ASTNodes.AnswerSelectionParenthesisNode node);


}
