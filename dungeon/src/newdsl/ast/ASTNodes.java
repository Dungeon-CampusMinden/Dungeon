package newdsl.ast;

import newdsl.common.SourceLocation;
import newdsl.common.Utils;
import newdsl.symboltable.Task;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ASTNodes {

    public enum NodeType {
        ALIAS_TEXT_ALIAS_ANSWER, //
        ALIAS_TEXT_TEXT_ALIAS_ANSWER, //
        ALIAS_TEXT_TEXT_ANSWER, //
        ANSWER_LIST, //
        CRAFTING_INGREDIENT, //
        CRAFTING_INGREDIENT_LIST, //
        CRAFTING_RULE, //
        CRAFTING_SOLUTION, //
        CHOICE_ANSWER, //
        CODE, //
        ID, //
        IMPORT_STATEMENT, //
        MATCHING_ANSWER, //
        NUMBER, //
        OPTIONAL_TASK_CONTENT_CUSTOM_CODE, //
        OPTIONAL_TASK_CONTENT_EXPLAIN, //
        OPTIONAL_TASK_CONTENT_LIST, //
        PARAMETER_ANSWER, //
        START, //
        TASK, //
        TASK_BODY, //
        TASK_COMPOSITION, //
        TASK_COMPOSITION_CONTENT, //
        TASK_COMPOSITION_CONTENT_LIST, //
        TASK_CONFIG, //
        TASK_CONFIG_BRANCH, //
        TASK_CONFIG_CONTENT, //
        TASK_HEADER, //
        TASK_VARIANT, //
        TASK_VARIANT_BODY, //
        TASK_VARIANT_LIST, //
        TEXT, //
        ANSWER_SELECTION_EXPRESSION, //
        ANSWER_SELECTION_EXPRESSION_OR, //
        ANSWER_SELECTION_EXPRESSION_SINGLE_TERM, //
        ANSWER_SELECTION_TERM, //
        ANSWER_SELECTION_TERM_AND, //
        ANSWER_SELECTION_TERM_SINGLE_FACTOR, //
        ANSWER_SELECTION_FACTOR, //
        ANSWER_SELECTION_FACTOR_PARENTHESIS, //
        ANSWER_SELECTION_FACTOR_FACTOR, //
    }

    public enum TaskType {
        MULTIPLE_CHOICE, SINGLE_CHOICE, FILL_IN_THE_BLANK, MATCHING, CRAFTING, CALCULATION;

        public static TaskType fromString(String text) {
            return switch (text) {
                case "multiple-choice", "multiple" -> TaskType.MULTIPLE_CHOICE;
                case "single-choice", "single" -> TaskType.SINGLE_CHOICE;
                case "fill-in-the-blank", "blank" -> TaskType.FILL_IN_THE_BLANK;
                case "matching", "match" -> TaskType.MATCHING;
                case "crafting", "craft" -> TaskType.CRAFTING;
                case "calculation", "calc" -> TaskType.CALCULATION;
                default -> null;
            };
        }

        @Override
        public String toString() {
            return switch (this) {
                case MULTIPLE_CHOICE -> "Multiple Choice";
                case SINGLE_CHOICE -> "Single Choice";
                case FILL_IN_THE_BLANK -> "Fill In The Blank";
                case MATCHING -> "Matching";
                case CRAFTING -> "Crafting";
                case CALCULATION -> "Calculation";
            };
        }
    }

    public enum ChoiceAnswerCorrectness {
        FALSE, CORRECT;

        public static ChoiceAnswerCorrectness fromString(String text) {
            return switch (text) {
                case "[]", "[ ]" -> ChoiceAnswerCorrectness.FALSE;
                case "[X]" -> ChoiceAnswerCorrectness.CORRECT;
                default -> null;
            };
        }
    }

    public enum CraftingStrictness {
        UNORDERED, ORDERED;

        public static CraftingStrictness fromString(String text) {
            return switch (text) {
                case "~>" -> CraftingStrictness.UNORDERED;
                case "->" -> CraftingStrictness.ORDERED;
                default -> null;
            };
        }
    }

    public enum ConfigRelation {
        DEFAULT, FALSE, CORRECT;

        public static ConfigRelation fromString(String text) {
            return switch (text) {
                case "->" -> ConfigRelation.DEFAULT;
                case "[] ->", "[ ] ->" -> ConfigRelation.FALSE;
                case "[X] ->" -> ConfigRelation.CORRECT;
                default -> null;
            };
        }
    }

    public enum OptionalContentType {
        EXPLANATION, GRADE, PASS, SCENARIO, SOLUTION;

        public static OptionalContentType fromString(String text) {
            return switch (text) {
                case "points" -> OptionalContentType.GRADE;
                case "pass" -> OptionalContentType.PASS;
                case "scenario" -> OptionalContentType.SCENARIO;
                case "solution" -> OptionalContentType.SOLUTION;
                default -> null;
            };
        }
    }

    public interface Visitable {
        Visitable accept(ASTTraverser<Visitable> visitor);
    }

    public abstract static class Node {
        public NodeType nodeType;
        public SourceLocation sourceLocation;
        public ParserRuleContext ctx;

        public Node(NodeType nodeType, int line, int charPos, String fileName) {

            this.nodeType = nodeType;
            this.sourceLocation = new SourceLocation(line, charPos, fileName);

        }

        public Node(NodeType nodeType, SourceLocation sourceLocation) {
            this.nodeType = nodeType;
            this.sourceLocation = sourceLocation;
        }

        public Node(NodeType nodeType, ParserRuleContext ctx) {
            this.nodeType = nodeType;
            this.sourceLocation = new SourceLocation(Utils.getLine(ctx), Utils.getCharPosInLine(ctx), Utils.getFileName(ctx));
            this.ctx = ctx;
        }

        @Override
        public String toString() {
            return String.format("%s, %s", this.nodeType, this.sourceLocation);
        }
    }


    public static class StartNode extends Node implements Visitable {
        public ArrayList<Visitable> children;

        public StartNode(ArrayList<Visitable> children, int line, int charPos, String fileName) {
            super(NodeType.START, line, charPos, fileName);
            this.children = children;
        }

        public StartNode(ArrayList<Visitable> children, ParserRuleContext ctx) {
            super(NodeType.START, ctx);
            this.children = children;
        }

        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public static class ImportStatementNode extends Node implements Visitable {
        public TextNode path;

        public ImportStatementNode(TextNode path, ParserRuleContext ctx) {
            super(NodeType.IMPORT_STATEMENT, ctx);
            this.path = path;
        }

        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public abstract static class TaskContentNode extends Node {

        public TaskContentNode(NodeType nodeType, ParserRuleContext ctx) {
            super(nodeType, ctx);
        }
    }

    public abstract static class TaskAnswersNode extends Node {

        public TaskAnswersNode(NodeType nodeType, ParserRuleContext ctx) {
            super(nodeType, ctx);
        }
    }

    public static class TaskNode extends Node implements Visitable {
        public Task task;
        public TaskHeaderNode taskHeader;
        public TaskContentNode taskContent;

        public TaskNode(Task task, TaskHeaderNode taskHeaderNode, TaskContentNode taskContentNode, ParserRuleContext ctx) {
            super(NodeType.TASK, ctx);
            this.taskHeader = taskHeaderNode;
            this.taskContent = taskContentNode;
            this.task = task;
        }

        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public static class TaskVariantNode extends Node implements Visitable {

        public Task task;
        public IdNode variantId;
        public TaskBodyNode taskBody;

        public TaskVariantNode(Task task, IdNode variantId, TaskBodyNode taskBody, ParserRuleContext ctx) {
            super(NodeType.TASK_VARIANT, ctx);
            this.variantId = variantId;
            this.taskBody = taskBody;
            this.task = task;
        }

        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public static class TaskVariantListNode extends Node implements Visitable {

        public List<TaskVariantNode> variants;

        public TaskVariantListNode(List<TaskVariantNode> variants, ParserRuleContext ctx) {
            super(NodeType.TASK_VARIANT_LIST, ctx);
            this.variants = variants;
        }

        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public static class TaskVariantBodyNode extends TaskContentNode implements Visitable {

        public TaskVariantListNode variants;
        public OptionalTaskContentListNode optionalContent;

        public TaskVariantBodyNode(TaskVariantListNode variants, OptionalTaskContentListNode optionalContent, ParserRuleContext ctx) {
            super(NodeType.TASK_VARIANT_BODY, ctx);
            this.variants = variants;
            this.optionalContent = optionalContent;
        }

        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public abstract static class OptionalTaskContentNode extends Node {
        public OptionalContentType type;

        public OptionalTaskContentNode(OptionalContentType type, NodeType nodeType, ParserRuleContext ctx) {
            super(nodeType, ctx);
            this.type = type;
        }

        public abstract String getValue();

    }

    public static class CustomCodeNode extends OptionalTaskContentNode implements Visitable {

        public CodeNode code;

        public CustomCodeNode(CodeNode code, OptionalContentType type, ParserRuleContext ctx) {
            super(type, NodeType.OPTIONAL_TASK_CONTENT_CUSTOM_CODE, ctx);
            this.code = code;
        }

        @Override
        public String getValue() {
            return (String) code.value;
        }

        @Override
        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public static class ExplanationNode extends OptionalTaskContentNode implements Visitable {

        public TextNode explanation;

        public ExplanationNode(TextNode explanation, ParserRuleContext ctx) {
            super(OptionalContentType.EXPLANATION, NodeType.OPTIONAL_TASK_CONTENT_EXPLAIN, ctx);
            this.explanation = explanation;
        }

        @Override
        public String getValue() {
            return (String) explanation.value;
        }

        @Override
        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public static class OptionalTaskContentListNode extends Node implements Visitable {

        public List<OptionalTaskContentNode> content;

        public OptionalTaskContentListNode(List<OptionalTaskContentNode> content, ParserRuleContext ctx) {
            super(NodeType.OPTIONAL_TASK_CONTENT_LIST, ctx);
            this.content = content;
        }

        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public static class AnswerListNode extends TaskAnswersNode implements Visitable {
        public List<AnswerNode> answers;

        public AnswerListNode(List<AnswerNode> answers, ParserRuleContext ctx) {
            super(NodeType.ANSWER_LIST, ctx);
            this.answers = answers;
        }

        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public static class AnswerSelectionExpressionNode extends TaskAnswersNode {

        public AnswerSelectionExpressionNode(NodeType type, ParserRuleContext ctx) {
            super(type, ctx);
        }
    }

    public static class AnswerSelectionOrNode extends AnswerSelectionExpressionNode implements Visitable {

        public AnswerSelectionExpressionNode left;
        public AnswerSelectionExpressionNode right;

        public AnswerSelectionOrNode(AnswerSelectionExpressionNode left, AnswerSelectionExpressionNode right, ParserRuleContext ctx) {
            super(NodeType.ANSWER_SELECTION_EXPRESSION_OR, ctx);
            this.left = left;
            this.right = right;
        }

        @Override
        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public static class AnswerSelectionTermNode extends AnswerSelectionExpressionNode {

        public AnswerSelectionTermNode(NodeType type, ParserRuleContext ctx) {
            super(type, ctx);
        }
    }

    public static class AnswerSelectionAndNode extends AnswerSelectionTermNode implements Visitable {

        public AnswerSelectionExpressionNode left;
        public AnswerSelectionExpressionNode right;

        public AnswerSelectionAndNode(AnswerSelectionExpressionNode term, AnswerSelectionExpressionNode factor, ParserRuleContext ctx) {
            super(NodeType.ANSWER_SELECTION_TERM_AND, ctx);
            this.left = term;
            this.right = factor;
        }

        @Override
        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public static class AnswerSelectionFactorNode extends AnswerSelectionTermNode {

        public AnswerSelectionFactorNode(NodeType type, ParserRuleContext ctx) {
            super(type, ctx);
        }
    }

    public static class AnswerSelectionParenthesisNode extends AnswerSelectionFactorNode implements Visitable {

        public AnswerSelectionExpressionNode expr;

        public AnswerSelectionParenthesisNode(AnswerSelectionExpressionNode expr, ParserRuleContext ctx) {
            super(NodeType.ANSWER_SELECTION_FACTOR_PARENTHESIS, ctx);
            this.expr = expr;
        }

        @Override
        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public static class AnswerSelectionNode extends AnswerSelectionFactorNode implements Visitable {

        public NumberNode amount;
        public AnswerListNode answers;

        public AnswerSelectionNode(NumberNode amount, AnswerListNode answers, ParserRuleContext ctx) {
            super(NodeType.ANSWER_SELECTION_FACTOR_FACTOR, ctx);
            this.amount = amount;
            this.answers = answers;
        }

        @Override
        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public static class CraftingIngredientListNode extends Node implements Visitable {
        public List<CraftingIngredientNode> ingredients;

        public CraftingIngredientListNode(List<CraftingIngredientNode> ingredients, ParserRuleContext ctx) {
            super(NodeType.CRAFTING_INGREDIENT_LIST, ctx);
            this.ingredients = ingredients;
        }

        @Override
        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public static class TaskHeaderNode extends Node implements Visitable {
        public IdNode id;
        public TaskType taskType;

        public TaskHeaderNode(IdNode id, TaskType taskType, ParserRuleContext ctx) {
            super(NodeType.TASK_HEADER, ctx);
            this.id = id;
            this.taskType = taskType;
        }

        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public static class TaskBodyNode extends TaskContentNode implements Visitable {
        public TextNode desc;
        public TaskAnswersNode answers;

        public OptionalTaskContentListNode optionalContent;

        public TaskBodyNode(TextNode desc, TaskAnswersNode answers, OptionalTaskContentListNode optionalContent, ParserRuleContext ctx) {
            super(NodeType.TASK_BODY, ctx);
            this.desc = desc;
            this.answers = answers;
            this.optionalContent = optionalContent;
        }

        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public abstract static class AnswerNode extends Node {

        public AnswerNode(NodeType nodeType, int line, int charPos, String fileName) {
            super(nodeType, line, charPos, fileName);
        }

        public AnswerNode(NodeType nodeType, ParserRuleContext ctx) {
            super(nodeType, ctx);
        }
    }

    public static class ChoiceAnswerNode extends AnswerNode implements Visitable {
        public boolean isCorrect;
        public TextNode text;

        public ChoiceAnswerNode(boolean isCorrect, TextNode text, ParserRuleContext ctx) {
            super(NodeType.CHOICE_ANSWER, ctx);
            this.isCorrect = isCorrect;
            this.text = text;
        }

        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public static class MatchingAnswerNode extends AnswerNode implements Visitable {
        public AliasTextNode left;
        public AliasTextNode right;

        public MatchingAnswerNode(AliasTextNode left, AliasTextNode right, ParserRuleContext ctx) {
            super(NodeType.MATCHING_ANSWER, ctx);
            this.left = left;
            this.right = right;
        }

        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public static class ParameterAnswerNode extends AnswerNode implements Visitable {
        public CodeNode parameter;
        public TextNode value;

        public ParameterAnswerNode(CodeNode parameter, TextNode value, ParserRuleContext ctx) {
            super(NodeType.PARAMETER_ANSWER, ctx);
            this.parameter = parameter;
            this.value = value;
        }

        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public abstract static class AliasTextNode extends Node {
        public IdNode alias;
        public TextNode text;

        public AliasTextNode(NodeType nodeType, IdNode alias, TextNode text, ParserRuleContext ctx) {
            super(nodeType, ctx);
            this.alias = alias;
            this.text = text;
        }
    }

    public static class TextAnswerNode extends AliasTextNode implements Visitable {

        public TextAnswerNode(TextNode text, ParserRuleContext ctx) {
            super(NodeType.ALIAS_TEXT_TEXT_ANSWER, null, text, ctx);
        }

        @Override
        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public static class TextAliasAnswerNode extends AliasTextNode implements Visitable {

        public TextAliasAnswerNode(IdNode alias, TextNode text, ParserRuleContext ctx) {
            super(NodeType.ALIAS_TEXT_TEXT_ALIAS_ANSWER, alias, text, ctx);
        }

        @Override
        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public static class AliasAnswerNode extends AliasTextNode implements Visitable {

        public AliasAnswerNode(IdNode alias, ParserRuleContext ctx) {
            super(NodeType.ALIAS_TEXT_ALIAS_ANSWER, alias, null, ctx);
        }

        @Override
        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public static class CraftingRuleNode extends AnswerNode implements Visitable {
        public List<CraftingIngredientNode> left;
        public CraftingStrictness strictness;
        public List<CraftingIngredientNode> right;

        public CraftingRuleNode(CraftingIngredientListNode left, CraftingStrictness strictness, CraftingIngredientListNode right, ParserRuleContext ctx) {
            super(NodeType.CRAFTING_RULE, ctx);
            this.left = (left != null) ? left.ingredients : null;
            this.strictness = strictness;
            this.right = (right != null) ? right.ingredients : null;
        }

        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public static class CraftingIngredientNode extends AnswerNode implements Visitable {
        public NumberNode amount;
        public AliasTextNode aliasText;

        public CraftingIngredientNode(NumberNode amount, AliasTextNode aliasText, ParserRuleContext ctx) {
            super(NodeType.CRAFTING_INGREDIENT, ctx);
            this.amount = amount;
            this.aliasText = aliasText;
        }

        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public static class CraftingSolutionNode extends AnswerNode implements Visitable {
        public NumberNode amount;
        public AliasTextNode aliasText;

        public CraftingSolutionNode(NumberNode amount, AliasTextNode aliasText, ParserRuleContext ctx) {
            super(NodeType.CRAFTING_SOLUTION, ctx);
            this.amount = amount;
            this.aliasText = aliasText;
        }

        public CraftingSolutionNode(CraftingIngredientNode node) {
            super(NodeType.CRAFTING_SOLUTION, node.sourceLocation.getRow(), node.sourceLocation.getColumn(), node.sourceLocation.getAbsoluteFilePath());
            this.amount = node.amount;
            this.aliasText = node.aliasText;
        }

        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public static class TaskCompositionNode extends Node implements Visitable {
        public IdNode id;
        public List<TaskCompositionSubtaskNode> substasks;

        public TaskCompositionNode(IdNode id, List<TaskCompositionSubtaskNode> substasks, ParserRuleContext ctx) {
            super(NodeType.TASK_COMPOSITION, ctx);
            this.id = id;
            this.substasks = substasks;
        }

        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public static class TaskCompositionNodeList extends Node implements Visitable {
        public List<TaskCompositionSubtaskNode> subtasks;

        public TaskCompositionNodeList(List<TaskCompositionSubtaskNode> subtasks, ParserRuleContext ctx) {
            super(NodeType.TASK_COMPOSITION_CONTENT_LIST, ctx);
            this.subtasks = subtasks;
        }

        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public static class TaskCompositionSubtaskNode extends Node implements Visitable {
        public IdNode id;
        public boolean required;

        public TaskCompositionSubtaskNode(IdNode id, boolean required, ParserRuleContext ctx) {
            super(NodeType.TASK_COMPOSITION_CONTENT, ctx);
            this.id = id;
            this.required = required;
        }

        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public static class TaskConfigNode extends Node implements Visitable {
        public IdNode id;

        public TaskConfigContentNode taskConfigContentNode;

        public TaskConfigNode(IdNode id, TaskConfigContentNode taskConfigContentNode, ParserRuleContext ctx) {
            super(NodeType.TASK_CONFIG, ctx);
            this.id = id;
            this.taskConfigContentNode = taskConfigContentNode;
        }

        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public static class TaskConfigContentNode extends Node implements Visitable {
        public IdNode id;

        public TaskConfigContentNode followingConfig;
        public TaskConfigBranchNode followingBranch;

        public TaskConfigContentNode(IdNode id, TaskConfigContentNode followingConfig, TaskConfigBranchNode followingBranch, ParserRuleContext ctx) {
            super(NodeType.TASK_CONFIG_CONTENT, ctx);
            this.id = id;
            this.followingBranch = followingBranch;
            this.followingConfig = followingConfig;
        }

        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public static class TaskConfigBranchNode extends Node implements Visitable {
        public TaskConfigContentNode correctBranch;
        public TaskConfigContentNode falseBranch;

        public TaskConfigBranchNode(TaskConfigContentNode correctBranch, TaskConfigContentNode falseBranch, ParserRuleContext ctx) {
            super(NodeType.TASK_CONFIG_BRANCH, ctx);
            this.correctBranch = correctBranch;
            this.falseBranch = falseBranch;
        }

        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public abstract static class TerminalNode extends Node {
        public Serializable value;

        public TerminalNode(NodeType nodeType, String value, ParserRuleContext ctx) {
            super(nodeType, ctx);
            this.value = value;
        }

        public TerminalNode(NodeType nodeType, Token token) {
            super(nodeType, Utils.getLine(token), Utils.getCharPosInLine(token), Utils.getFileName(token));
            this.value = getValue(nodeType, token, null);
        }

        public TerminalNode(NodeType nodeType, Token token, Serializable defaultValue) {
            super(nodeType, Utils.getLine(token), Utils.getCharPosInLine(token), Utils.getFileName(token));
            this.value = getValue(nodeType, token, defaultValue);
        }

        private Serializable getValue(NodeType nodeType, Token token, Serializable defaultValue) {
            if (token == null) {
                return defaultValue;
            }

            switch (nodeType) {
                case NUMBER -> {
                    return Integer.parseInt(token.getText());
                }
                default -> {
                    return token.getText();
                }
            }
        }
    }

    public static class TextNode extends TerminalNode implements Visitable {

        public TextNode(Token t) {
            super(NodeType.TEXT, t);
        }

        public TextNode(String value, ParserRuleContext ctx) {
            super(NodeType.TEXT, value, ctx);
        }

        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public static class NumberNode extends TerminalNode implements Visitable {


        public NumberNode(Token t) {
            super(NodeType.NUMBER, t);
        }

        public NumberNode(Token t, int defaultValue) {
            super(NodeType.NUMBER, t, defaultValue);
        }

        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public static class IdNode extends TerminalNode implements Visitable {

        public IdNode(Token t) {
            super(NodeType.ID, t);
        }

        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

    public static class CodeNode extends TerminalNode implements Visitable {

        public CodeNode(Token t) {
            super(NodeType.CODE, t);
        }

        public Visitable accept(ASTTraverser<Visitable> visitor) {
            return visitor.visit(this);
        }
    }

}
