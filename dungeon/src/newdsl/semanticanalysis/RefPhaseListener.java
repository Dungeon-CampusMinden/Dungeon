package newdsl.semanticanalysis;

import newdsl.common.DSLError;
import newdsl.common.SourceLocation;
import newdsl.common.Utils;
import newdsl.symboltable.Symbol;
import newdsl.symboltable.SymbolTable;
import newdsl.antlr.DSLBaseListener;
import newdsl.antlr.DSLParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RefPhaseListener extends DSLBaseListener {

    private static SymbolTable.SymbolType[] ALIAS = new SymbolTable.SymbolType[]{SymbolTable.SymbolType.ALIAS};
    private static SymbolTable.SymbolType[] TASK = new SymbolTable.SymbolType[]{SymbolTable.SymbolType.TASK};
    private static SymbolTable.SymbolType[] TASK_AND_TASK_COMPOSITION = new SymbolTable.SymbolType[]{SymbolTable.SymbolType.TASK, SymbolTable.SymbolType.TASK_COMPOSITION};
    private static SymbolTable.SymbolType[] PARAMS = new SymbolTable.SymbolType[]{SymbolTable.SymbolType.PARAMETER};
    SymbolTable symbolTable;
    ArrayList<DSLError> errors;

    public RefPhaseListener(SymbolTable symbolTable, ArrayList<DSLError> errors) {
        this.symbolTable = symbolTable;
        this.errors = errors;
    }

    private void checkIfDefined(String symbolType, ParserRuleContext ctx, String id, SymbolTable.SymbolType[] allowedTypes) {
        boolean isDefined = symbolTable.isDefined(id);
        if (!isDefined) { // is it defined?
            int row = Utils.getLine(ctx);
            int charPos = Utils.getCharPosInLine(ctx);
            String fileName = Utils.getFileName(ctx);
            SourceLocation location = new SourceLocation(row, charPos, fileName);

            boolean isGlobal = symbolTable.getCurrentScope().getName().equals("global");

            if (isGlobal) {
                errors.add(new DSLError(String.format("%s '%s' is not defined: Please check the spelling and ensure that %s '%s' is declared before use", symbolType, id, symbolType, id), location));
            } else {
                errors.add(new DSLError(String.format("%s '%s' is not defined: Please check the spelling and ensure that %s '%s' is declared in '%s' before use", symbolType, id, symbolType, id, symbolTable.getCurrentScope().getName()), location));

            }
        } else { // is it of the correct type?
            Symbol candidate = symbolTable.getCurrentScope().resolveInScope(id);
            if (!Arrays.stream(allowedTypes).toList().contains(candidate.getType())) {
                SourceLocation location = new SourceLocation(ctx);
                errors.add(new DSLError(String.format("'%s' is not the correct type: Please make sure to use the correct variable, expected %s and received %s", id, String.join(", ", Arrays.stream(allowedTypes).map(SymbolTable.SymbolType::toString).toList()), candidate.getType()), location));
            }
        }
    }

    @Override
    public void enterStart(DSLParser.StartContext ctx) {
        super.enterStart(ctx);
    }

    @Override
    public void exitStart(DSLParser.StartContext ctx) {
        super.exitStart(ctx);
    }

    @Override
    public void enterTask(DSLParser.TaskContext ctx) {
        super.enterTask(ctx);
    }

    @Override
    public void exitTask(DSLParser.TaskContext ctx) {
        symbolTable.popScope();
    }

    @Override
    public void enterTaskBody(DSLParser.TaskBodyContext ctx) {
        super.enterTaskBody(ctx);
    }

    @Override
    public void exitTaskBody(DSLParser.TaskBodyContext ctx) {
        super.exitTaskBody(ctx);
    }

    @Override
    public void enterTaskVariantBody(DSLParser.TaskVariantBodyContext ctx) {
        super.enterTaskVariantBody(ctx);
    }

    @Override
    public void exitTaskVariantBody(DSLParser.TaskVariantBodyContext ctx) {
        super.exitTaskVariantBody(ctx);
    }

    @Override
    public void enterTask_header(DSLParser.Task_headerContext ctx) {
        symbolTable.enterScope(ctx.ID().getText());
    }

    @Override
    public void exitTask_header(DSLParser.Task_headerContext ctx) {
        super.exitTask_header(ctx);
    }

    @Override
    public void enterTask_body(DSLParser.Task_bodyContext ctx) {
        super.enterTask_body(ctx);
    }

    @Override
    public void exitTask_body(DSLParser.Task_bodyContext ctx) {
        super.exitTask_body(ctx);
    }

    @Override
    public void enterTask_answers(DSLParser.Task_answersContext ctx) {
        super.enterTask_answers(ctx);
    }

    @Override
    public void exitTask_answers(DSLParser.Task_answersContext ctx) {
        super.exitTask_answers(ctx);
    }

    @Override
    public void enterSingleTerm(DSLParser.SingleTermContext ctx) {
        super.enterSingleTerm(ctx);
    }

    @Override
    public void exitSingleTerm(DSLParser.SingleTermContext ctx) {
        super.exitSingleTerm(ctx);
    }

    @Override
    public void enterOr(DSLParser.OrContext ctx) {
        super.enterOr(ctx);
    }

    @Override
    public void exitOr(DSLParser.OrContext ctx) {
        super.exitOr(ctx);
    }

    @Override
    public void enterAnd(DSLParser.AndContext ctx) {
        super.enterAnd(ctx);
    }

    @Override
    public void exitAnd(DSLParser.AndContext ctx) {
        super.exitAnd(ctx);
    }

    @Override
    public void enterSingleFactor(DSLParser.SingleFactorContext ctx) {
        super.enterSingleFactor(ctx);
    }

    @Override
    public void exitSingleFactor(DSLParser.SingleFactorContext ctx) {
        super.exitSingleFactor(ctx);
    }

    @Override
    public void enterParenthesis(DSLParser.ParenthesisContext ctx) {
        super.enterParenthesis(ctx);
    }

    @Override
    public void exitParenthesis(DSLParser.ParenthesisContext ctx) {
        super.exitParenthesis(ctx);
    }

    @Override
    public void enterFactor(DSLParser.FactorContext ctx) {
        super.enterFactor(ctx);
    }

    @Override
    public void exitFactor(DSLParser.FactorContext ctx) {
        super.exitFactor(ctx);
    }

    @Override
    public void enterAnswer_list(DSLParser.Answer_listContext ctx) {
        super.enterAnswer_list(ctx);
    }

    @Override
    public void exitAnswer_list(DSLParser.Answer_listContext ctx) {
        super.exitAnswer_list(ctx);
    }

    @Override
    public void enterChoiceAnswer(DSLParser.ChoiceAnswerContext ctx) {
        super.enterChoiceAnswer(ctx);
    }

    @Override
    public void exitChoiceAnswer(DSLParser.ChoiceAnswerContext ctx) {
        super.exitChoiceAnswer(ctx);
    }

    @Override
    public void enterMatchingAnswer(DSLParser.MatchingAnswerContext ctx) {
        super.enterMatchingAnswer(ctx);
    }

    @Override
    public void exitMatchingAnswer(DSLParser.MatchingAnswerContext ctx) {
        super.exitMatchingAnswer(ctx);
    }

    @Override
    public void enterParameterAnswer(DSLParser.ParameterAnswerContext ctx) {
        DSLParser.Parameter_answerContext param = (DSLParser.Parameter_answerContext) ctx.getChild(0);
        checkIfDefined("Parameter", ctx, param.parameter.getText(), PARAMS);
    }

    @Override
    public void exitParameterAnswer(DSLParser.ParameterAnswerContext ctx) {
        super.exitParameterAnswer(ctx);
    }

    @Override
    public void enterCraftingAnswer(DSLParser.CraftingAnswerContext ctx) {
        super.enterCraftingAnswer(ctx);
    }

    @Override
    public void exitCraftingAnswer(DSLParser.CraftingAnswerContext ctx) {
        super.exitCraftingAnswer(ctx);
    }

    @Override
    public void enterTask_variant_body(DSLParser.Task_variant_bodyContext ctx) {
        super.enterTask_variant_body(ctx);
    }

    @Override
    public void exitTask_variant_body(DSLParser.Task_variant_bodyContext ctx) {
        super.exitTask_variant_body(ctx);
    }

    @Override
    public void enterTask_variant_list(DSLParser.Task_variant_listContext ctx) {
        super.enterTask_variant_list(ctx);
    }

    @Override
    public void exitTask_variant_list(DSLParser.Task_variant_listContext ctx) {
        super.exitTask_variant_list(ctx);
    }

    @Override
    public void enterTask_variant(DSLParser.Task_variantContext ctx) {
        super.enterTask_variant(ctx);
    }

    @Override
    public void exitTask_variant(DSLParser.Task_variantContext ctx) {
        super.exitTask_variant(ctx);
    }

    @Override
    public void enterInitialIngredient(DSLParser.InitialIngredientContext ctx) {
        super.enterInitialIngredient(ctx);
    }

    @Override
    public void exitInitialIngredient(DSLParser.InitialIngredientContext ctx) {
        super.exitInitialIngredient(ctx);
    }

    @Override
    public void enterRule(DSLParser.RuleContext ctx) {
        super.enterRule(ctx);
    }

    @Override
    public void exitRule(DSLParser.RuleContext ctx) {
        super.exitRule(ctx);
    }

    @Override
    public void enterSolution(DSLParser.SolutionContext ctx) {
        super.enterSolution(ctx);
    }

    @Override
    public void exitSolution(DSLParser.SolutionContext ctx) {
        super.exitSolution(ctx);
    }

    @Override
    public void enterCrafting_ingredient_list(DSLParser.Crafting_ingredient_listContext ctx) {
        super.enterCrafting_ingredient_list(ctx);
    }

    @Override
    public void exitCrafting_ingredient_list(DSLParser.Crafting_ingredient_listContext ctx) {
        super.exitCrafting_ingredient_list(ctx);
    }

    @Override
    public void enterCrafting_ingredient(DSLParser.Crafting_ingredientContext ctx) {
        super.enterCrafting_ingredient(ctx);
    }

    @Override
    public void exitCrafting_ingredient(DSLParser.Crafting_ingredientContext ctx) {
        super.exitCrafting_ingredient(ctx);
    }

    @Override
    public void enterCrafting_rule(DSLParser.Crafting_ruleContext ctx) {
        super.enterCrafting_rule(ctx);
    }

    @Override
    public void exitCrafting_rule(DSLParser.Crafting_ruleContext ctx) {
        super.exitCrafting_rule(ctx);
    }

    @Override
    public void enterCrafting_solution(DSLParser.Crafting_solutionContext ctx) {
        super.enterCrafting_solution(ctx);
    }

    @Override
    public void exitCrafting_solution(DSLParser.Crafting_solutionContext ctx) {
        super.exitCrafting_solution(ctx);
    }

    @Override
    public void enterChoice_answer(DSLParser.Choice_answerContext ctx) {
        super.enterChoice_answer(ctx);
    }

    @Override
    public void exitChoice_answer(DSLParser.Choice_answerContext ctx) {
        super.exitChoice_answer(ctx);
    }

    @Override
    public void enterLeftBlank(DSLParser.LeftBlankContext ctx) {
        super.enterLeftBlank(ctx);
    }

    @Override
    public void exitLeftBlank(DSLParser.LeftBlankContext ctx) {
        super.exitLeftBlank(ctx);
    }

    @Override
    public void enterBothText(DSLParser.BothTextContext ctx) {
        super.enterBothText(ctx);
    }

    @Override
    public void exitBothText(DSLParser.BothTextContext ctx) {
        super.exitBothText(ctx);
    }

    @Override
    public void enterRightBlank(DSLParser.RightBlankContext ctx) {
        super.enterRightBlank(ctx);
    }

    @Override
    public void exitRightBlank(DSLParser.RightBlankContext ctx) {
        super.exitRightBlank(ctx);
    }

    @Override
    public void enterParameter_answer(DSLParser.Parameter_answerContext ctx) {
        super.enterParameter_answer(ctx);
    }

    @Override
    public void exitParameter_answer(DSLParser.Parameter_answerContext ctx) {
        super.exitParameter_answer(ctx);
    }

    @Override
    public void enterOptional_task_content_list(DSLParser.Optional_task_content_listContext ctx) {
        super.enterOptional_task_content_list(ctx);
    }

    @Override
    public void exitOptional_task_content_list(DSLParser.Optional_task_content_listContext ctx) {
        super.exitOptional_task_content_list(ctx);
    }

    @Override
    public void enterExplain(DSLParser.ExplainContext ctx) {
        super.enterExplain(ctx);
    }

    @Override
    public void exitExplain(DSLParser.ExplainContext ctx) {
        super.exitExplain(ctx);
    }

    @Override
    public void enterCustomCode(DSLParser.CustomCodeContext ctx) {
        super.enterCustomCode(ctx);
    }

    @Override
    public void exitCustomCode(DSLParser.CustomCodeContext ctx) {
        super.exitCustomCode(ctx);
    }

    @Override
    public void enterTask_composition(DSLParser.Task_compositionContext ctx) {
        super.enterTask_composition(ctx);
    }

    @Override
    public void exitTask_composition(DSLParser.Task_compositionContext ctx) {
        super.exitTask_composition(ctx);
    }

    @Override
    public void enterTask_composition_content_list(DSLParser.Task_composition_content_listContext ctx) {
        List<DSLParser.Task_composition_contentContext> requiredAndOptionalTasks = ctx.task_composition_content().stream().filter(task -> task instanceof DSLParser.RequiredTaskContext || task instanceof DSLParser.OptionalTaskContext).toList();
        requiredAndOptionalTasks.forEach(task -> {
            String taskId = task instanceof DSLParser.RequiredTaskContext ? ((DSLParser.RequiredTaskContext) task).id.getText() : ((DSLParser.OptionalTaskContext) task).id.getText();
            checkIfDefined("Task", task, taskId, new SymbolTable.SymbolType[]{SymbolTable.SymbolType.TASK});
        });
    }

    @Override
    public void exitTask_composition_content_list(DSLParser.Task_composition_content_listContext ctx) {
        super.exitTask_composition_content_list(ctx);
    }

    @Override
    public void enterRequiredTask(DSLParser.RequiredTaskContext ctx) {
        super.enterRequiredTask(ctx);
    }

    @Override
    public void exitRequiredTask(DSLParser.RequiredTaskContext ctx) {
        super.exitRequiredTask(ctx);
    }

    @Override
    public void enterOptionalTask(DSLParser.OptionalTaskContext ctx) {
        super.enterOptionalTask(ctx);
    }

    @Override
    public void exitOptionalTask(DSLParser.OptionalTaskContext ctx) {
        super.exitOptionalTask(ctx);
    }

    @Override
    public void enterTask_config(DSLParser.Task_configContext ctx) {
        super.enterTask_config(ctx);
    }

    @Override
    public void exitTask_config(DSLParser.Task_configContext ctx) {
        super.exitTask_config(ctx);
    }

    @Override
    public void enterImport_statement(DSLParser.Import_statementContext ctx) {
        super.enterImport_statement(ctx);
    }

    @Override
    public void exitImport_statement(DSLParser.Import_statementContext ctx) {
        super.exitImport_statement(ctx);
    }

    @Override
    public void enterTextAnswer(DSLParser.TextAnswerContext ctx) {
        super.enterTextAnswer(ctx);
    }

    @Override
    public void exitTextAnswer(DSLParser.TextAnswerContext ctx) {
        super.exitTextAnswer(ctx);
    }

    @Override
    public void enterTextAliasAnswer(DSLParser.TextAliasAnswerContext ctx) {
        super.enterTextAliasAnswer(ctx);
    }

    @Override
    public void exitTextAliasAnswer(DSLParser.TextAliasAnswerContext ctx) {
        super.exitTextAliasAnswer(ctx);
    }

    @Override
    public void enterAliasAnswer(DSLParser.AliasAnswerContext ctx) {
        String aliasId = ctx.alias.getText();
        checkIfDefined("Alias", ctx, aliasId, ALIAS);
    }

    @Override
    public void exitAliasAnswer(DSLParser.AliasAnswerContext ctx) {
        super.exitAliasAnswer(ctx);
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        super.enterEveryRule(ctx);
    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {
        super.exitEveryRule(ctx);
    }

    @Override
    public void visitTerminal(TerminalNode node) {
        super.visitTerminal(node);
    }

    @Override
    public void visitErrorNode(ErrorNode node) {
        super.visitErrorNode(node);
    }

    @Override
    public void enterTask_config_content(DSLParser.Task_config_contentContext ctx) {
        String id = ctx.id.getText();
        checkIfDefined("Task", ctx, id, TASK_AND_TASK_COMPOSITION);
    }

    @Override
    public void exitTask_config_content(DSLParser.Task_config_contentContext ctx) {
        super.exitTask_config_content(ctx);
    }

    @Override
    public void enterTask_config_branch(DSLParser.Task_config_branchContext ctx) {
        super.enterTask_config_branch(ctx);
    }

    @Override
    public void exitTask_config_branch(DSLParser.Task_config_branchContext ctx) {
        super.exitTask_config_branch(ctx);
    }

}
