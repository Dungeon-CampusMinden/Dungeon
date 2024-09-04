package newdsl.ast;

import newdsl.common.DSLError;
import newdsl.common.SourceLocation;
import newdsl.symboltable.*;
import newdsl.antlr.DSLBaseVisitor;
import newdsl.antlr.DSLParser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static newdsl.common.Utils.extractParams;

public class ParseTreeVisitor extends DSLBaseVisitor<ASTNodes.Visitable> {

    public final SymbolTable symbolTable = new SymbolTable();
    private ArrayList<DSLError> errors;

    public ParseTreeVisitor(ArrayList<DSLError> errors) {
        this.errors = errors;
    }

    public ArrayList<ASTNodes.Visitable> getContents(List<ParseTree> children) {
        ArrayList<ASTNodes.Visitable> contents = new ArrayList<>();

        for (ParseTree child : children) {
            if (child.toString().equals("<EOF>")) {
                // do nothing
            } else if (child instanceof DSLParser.Import_statementContext) {
                contents.add(visitImport_statement((DSLParser.Import_statementContext) child));
            } else if (child instanceof DSLParser.TaskContext) {
                contents.add(visitTask((DSLParser.TaskContext) child));
            } else if (child instanceof DSLParser.Task_compositionContext) {
                contents.add(visitTask_composition((DSLParser.Task_compositionContext) child));
            } else if (child instanceof DSLParser.Task_configContext) {
                contents.add(visitTask_config((DSLParser.Task_configContext) child));
            }

            else {
                throw new UnsupportedOperationException(String.format("Missing implementation for %s.", child.getClass().getName()));
            }

        }

        return contents;
    }

    @Override
    public ASTNodes.Visitable visitStart(DSLParser.StartContext ctx) {

        // Handle merged file contents
        if (ctx.invokingState == -1) {
            return new ASTNodes.StartNode(getContents(ctx.children), -1, -1, null);
        }

        return new ASTNodes.StartNode(getContents(ctx.children), ctx);
    }

    private void checkIfDefined(String symbolType, String name, Scope scope, SourceLocation sourceLocation) {
        if (scope.resolveInScope(name) != null) {
            String msg = String.format("%s '%s' is already defined %s: Please rename it %s to avoid conflicts", symbolType, name, scope.resolveInScope(name).getSourceLocation(), sourceLocation);
            DSLError error = new DSLError(msg, sourceLocation);

            if (errors.contains(error)) {
                return;
            }

            errors.add(error);
        }
    }

    private void checkIfDeclaredInTaskVariants(Scope scope) {
        List<Symbol> ownSymbols = scope.getSymbols().values().stream().filter(symbol -> !(symbol instanceof Task)).toList();
        List<Symbol> taskVariants = scope.getSymbols().values().stream().filter(symbol -> symbol instanceof Task).toList();

        if (ownSymbols.isEmpty() || taskVariants.isEmpty()) {
            return;
        }

        taskVariants.forEach(taskVariant -> ownSymbols.forEach(ownSymbol -> {
            String name = ownSymbol.getName();
            SourceLocation sl = ownSymbol.getSourceLocation();
            checkIfDefined("Attribute", name, (Task) taskVariant, sl);
        }));
    }

    public Task setTaskScope(ASTNodes.TaskHeaderNode taskHeaderNode) {
        String taskId = taskHeaderNode.id.value.toString();
        Scope currentScope = symbolTable.getCurrentScope();
        checkIfDefined("Task", taskId, currentScope, taskHeaderNode.sourceLocation);

        Task task = new Task(currentScope, new HashMap<>(), taskId, taskHeaderNode.sourceLocation);

        symbolTable.addSymbol(task);
        symbolTable.setScope(task);

        return task;
    }

    public Task setTaskVariantScope(ASTNodes.IdNode idNode) {
        String taskId = (String) idNode.value;
        Scope currentScope = symbolTable.getCurrentScope();
        checkIfDefined("TaskVariant", taskId, currentScope, idNode.sourceLocation);

        Task task = new Task(currentScope, new HashMap<>(), taskId, idNode.sourceLocation);

        symbolTable.addSymbol(task);
        symbolTable.setScope(task);

        return task;
    }

    public void createVariable(String id, SymbolTable.SymbolType symbolType, SourceLocation sourceLocation) {
        Scope currentScope = symbolTable.getCurrentScope();
        checkIfDefined(symbolType.toString(), id, currentScope, sourceLocation);
        Variable var = new Variable(id, symbolType, sourceLocation);
        symbolTable.addSymbol(var);
    }

    public void createVariable(ASTNodes.IdNode idNode, SymbolTable.SymbolType symbolType) {
        Scope currentScope = symbolTable.getCurrentScope();
        String id = (String) idNode.value;
        checkIfDefined(symbolType.toString(), id, currentScope, idNode.sourceLocation);
        Variable var = new Variable(id, symbolType, idNode.sourceLocation);
        symbolTable.addSymbol(var);
    }

    public void createParameter(String id, SourceLocation loc) {
        Variable var = new Variable(id, SymbolTable.SymbolType.PARAMETER, loc);
        if (!symbolTable.isDefined(id)) {
            symbolTable.addSymbol(var);
        }
    }

    private void handleParams(ASTNodes.TaskBodyNode bodyNode) {
        List<String> params = extractParams(bodyNode.desc.value.toString());
        params.forEach(param -> createParameter(param, bodyNode.desc.sourceLocation));
    }

    @Override
    public ASTNodes.Visitable visitTask(DSLParser.TaskContext ctx) {
        ASTNodes.TaskHeaderNode taskHeaderNode = (ASTNodes.TaskHeaderNode) visitTask_header(ctx.task_header());

        Task task = setTaskScope(taskHeaderNode);

        ParseTree child = ctx.task_content().getChild(0);

        if (child instanceof DSLParser.Task_bodyContext) {
            ASTNodes.TaskBodyNode taskBodyNode = (ASTNodes.TaskBodyNode) visitTask_body((DSLParser.Task_bodyContext) child);
            if (taskHeaderNode.taskType == ASTNodes.TaskType.CALCULATION) {
                handleParams(taskBodyNode);
            }
            symbolTable.popScope();
            return new ASTNodes.TaskNode(task, taskHeaderNode, taskBodyNode, ctx);
        } else if (child instanceof DSLParser.Task_variant_bodyContext) {
            ASTNodes.TaskVariantBodyNode taskVariantBodyNode = (ASTNodes.TaskVariantBodyNode) visitTask_variant_body((DSLParser.Task_variant_bodyContext) child);
            if (taskHeaderNode.taskType == ASTNodes.TaskType.CALCULATION) {
                taskVariantBodyNode.variants.variants.forEach(var -> {
                    if (taskHeaderNode.taskType == ASTNodes.TaskType.CALCULATION) {
                        handleParams(var.taskBody);
                    }
                });
            }
            symbolTable.popScope();
            return new ASTNodes.TaskNode(task, taskHeaderNode, taskVariantBodyNode, ctx);
        }

        return null;

    }

    @Override
    public ASTNodes.Visitable visitTaskBody(DSLParser.TaskBodyContext ctx) {
        return super.visitTaskBody(ctx);
    }

    @Override
    public ASTNodes.Visitable visitTaskVariantBody(DSLParser.TaskVariantBodyContext ctx) {
        return super.visitTaskVariantBody(ctx);
    }

    @Override
    public ASTNodes.Visitable visitTask_header(DSLParser.Task_headerContext ctx) {

        Token id = ctx.id;
        ASTNodes.TaskType type = ASTNodes.TaskType.fromString(ctx.type.getText());

        return new ASTNodes.TaskHeaderNode(new ASTNodes.IdNode(id), type, ctx);
    }

    @Override
    public ASTNodes.Visitable visitTask_body(DSLParser.Task_bodyContext ctx) {
        ASTNodes.TextNode desc = new ASTNodes.TextNode(ctx.desc);

        ParseTree child = ctx.task_answers().getChild(0);

        if (child instanceof DSLParser.Answer_listContext) {
            ASTNodes.AnswerListNode answerList = (ASTNodes.AnswerListNode) visitAnswer_list((DSLParser.Answer_listContext) child);
            ASTNodes.OptionalTaskContentListNode optionalContent = (ASTNodes.OptionalTaskContentListNode) visitOptional_task_content_list(ctx.optional_task_content_list());

            return new ASTNodes.TaskBodyNode(desc, answerList, optionalContent, ctx);
        } else if (child instanceof DSLParser.Answer_selection_expressionContext) {
            ASTNodes.AnswerSelectionExpressionNode expr = (ASTNodes.AnswerSelectionExpressionNode) handleAnswerSelectionExpression((DSLParser.Answer_selection_expressionContext) child);
            ASTNodes.OptionalTaskContentListNode optionalContent = (ASTNodes.OptionalTaskContentListNode) visitOptional_task_content_list(ctx.optional_task_content_list());

            return new ASTNodes.TaskBodyNode(desc, expr, optionalContent, ctx);
        }
        return null;
    }


    private ASTNodes.Visitable handleAnswerSelectionFactor(DSLParser.Answer_selection_factorContext child) {

        if (child instanceof DSLParser.ParenthesisContext) {
            return visitParenthesis((DSLParser.ParenthesisContext) child);
        } else if (child instanceof DSLParser.FactorContext) {
            return visitFactor((DSLParser.FactorContext) child);
        }

        return null;

    }

    private ASTNodes.Visitable handleAnswerSelectionTerm(DSLParser.Answer_selection_termContext child) {

        if (child instanceof DSLParser.SingleFactorContext) {
            return visitSingleFactor((DSLParser.SingleFactorContext) child);
        } else if (child instanceof DSLParser.AndContext) {
            return visitAnd((DSLParser.AndContext) child);
        }

        return null;

    }

    private ASTNodes.Visitable handleAnswerSelectionExpression(DSLParser.Answer_selection_expressionContext child) {
        if (child instanceof DSLParser.OrContext) {
            return visitOr((DSLParser.OrContext) child);
        } else if (child instanceof DSLParser.SingleTermContext) {
            return visitSingleTerm((DSLParser.SingleTermContext) child);
        }
        return null;

    }

    public ASTNodes.AnswerNode handleAnswer(DSLParser.AnswerContext ctx) {
        ParseTree child = ctx.children.get(0);
        if (child instanceof DSLParser.Choice_answerContext) {
            return (ASTNodes.AnswerNode) visitChoice_answer((DSLParser.Choice_answerContext) child);
        } else if (child instanceof DSLParser.Parameter_answerContext) {
            return (ASTNodes.AnswerNode) visitParameter_answer((DSLParser.Parameter_answerContext) child);
        } else if (child instanceof DSLParser.BothTextContext) {
            return (ASTNodes.AnswerNode) visitBothText((DSLParser.BothTextContext) child);
        } else if (child instanceof DSLParser.LeftBlankContext) {
            return (ASTNodes.AnswerNode) visitLeftBlank((DSLParser.LeftBlankContext) child);
        } else if (child instanceof DSLParser.RightBlankContext) {
            return (ASTNodes.AnswerNode) visitRightBlank((DSLParser.RightBlankContext) child);
        } else if (child instanceof DSLParser.InitialIngredientContext) {
            return (ASTNodes.AnswerNode) visitInitialIngredient((DSLParser.InitialIngredientContext) child);
        } else if (child instanceof DSLParser.RuleContext) {
            return (ASTNodes.AnswerNode) visitRule((DSLParser.RuleContext) child);
        } else if (child instanceof DSLParser.SolutionContext) {
            return (ASTNodes.AnswerNode) visitSolution((DSLParser.SolutionContext) child);
        }

        return null;

    }

    @Override
    public ASTNodes.Visitable visitTask_variant_body(DSLParser.Task_variant_bodyContext ctx) {

        ASTNodes.TaskVariantListNode variantList = (ASTNodes.TaskVariantListNode) visitTask_variant_list(ctx.task_variant_list());

        ASTNodes.OptionalTaskContentListNode optionalTaskContentNode = (ASTNodes.OptionalTaskContentListNode) visitOptional_task_content_list(ctx.optional_task_content_list());
        return new ASTNodes.TaskVariantBodyNode(variantList, optionalTaskContentNode, ctx);
    }

    @Override
    public ASTNodes.Visitable visitOptional_task_content_list(DSLParser.Optional_task_content_listContext ctx) {
        if (ctx.children != null) {
            List<ASTNodes.OptionalTaskContentNode> optionalContent = ctx.children.stream().filter(child -> child instanceof DSLParser.CustomCodeContext || child instanceof DSLParser.ExplainContext).map(child -> child instanceof DSLParser.CustomCodeContext ? (ASTNodes.OptionalTaskContentNode) visitCustomCode((DSLParser.CustomCodeContext) child) : (ASTNodes.OptionalTaskContentNode) visitExplain((DSLParser.ExplainContext) child)).toList();

            optionalContent.forEach(optionalTaskContentNode -> createVariable(optionalTaskContentNode.type.toString(), SymbolTable.SymbolType.OPTIONAL_CONTENT, optionalTaskContentNode.sourceLocation));

            return new ASTNodes.OptionalTaskContentListNode(optionalContent, ctx);
        }
        return null;
    }

    @Override
    public ASTNodes.Visitable visitCustomCode(DSLParser.CustomCodeContext ctx) {

        ASTNodes.CodeNode codeNode = new ASTNodes.CodeNode(ctx.code);
        ASTNodes.OptionalContentType optionalContentType = ASTNodes.OptionalContentType.fromString(ctx.type.getText());

        return new ASTNodes.CustomCodeNode(codeNode, optionalContentType, ctx);
    }

    @Override
    public ASTNodes.Visitable visitTask_variant(DSLParser.Task_variantContext ctx) {


        ASTNodes.IdNode variantId = new ASTNodes.IdNode(ctx.id);

        Task task = setTaskVariantScope(variantId);

        ASTNodes.TaskBodyNode taskBody = (ASTNodes.TaskBodyNode) visitTask_body(ctx.task_body());

        symbolTable.popScope();

        return new ASTNodes.TaskVariantNode(task, variantId, taskBody, ctx);
    }

    @Override
    public ASTNodes.Visitable visitInitialIngredient(DSLParser.InitialIngredientContext ctx) {
        return visitCrafting_ingredient(ctx.crafting_ingredient());
    }

    @Override
    public ASTNodes.Visitable visitRule(DSLParser.RuleContext ctx) {
        return visitCrafting_rule(ctx.crafting_rule());
    }

    @Override
    public ASTNodes.Visitable visitSolution(DSLParser.SolutionContext ctx) {
        ASTNodes.CraftingIngredientNode solution = (ASTNodes.CraftingIngredientNode) visitCrafting_ingredient(ctx.crafting_solution().crafting_ingredient());
        return new ASTNodes.CraftingSolutionNode(solution);
    }

    @Override
    public ASTNodes.Visitable visitCrafting_ingredient_list(DSLParser.Crafting_ingredient_listContext ctx) {

        List<ASTNodes.CraftingIngredientNode> ingredients = ctx.children.stream().filter(c -> c instanceof DSLParser.Crafting_ingredientContext).map(c -> (ASTNodes.CraftingIngredientNode) visitCrafting_ingredient((DSLParser.Crafting_ingredientContext) c)).toList();

        return new ASTNodes.CraftingIngredientListNode(ingredients, ctx);
    }

    @Override
    public ASTNodes.Visitable visitCrafting_ingredient(DSLParser.Crafting_ingredientContext ctx) {


        Token amount = ctx.amount;
        ASTNodes.NumberNode amountNode = new ASTNodes.NumberNode(amount, 1);
        ASTNodes.AliasTextNode aliasText = handleAliasText(ctx.alias_text());

        return new ASTNodes.CraftingIngredientNode(amountNode, aliasText, ctx);
    }

    @Override
    public ASTNodes.Visitable visitCrafting_rule(DSLParser.Crafting_ruleContext ctx) {


        ASTNodes.CraftingStrictness strictness = ASTNodes.CraftingStrictness.fromString(ctx.strictness.getText());
        ASTNodes.CraftingIngredientListNode ingredients = (ASTNodes.CraftingIngredientListNode) visitCrafting_ingredient_list((DSLParser.Crafting_ingredient_listContext) ctx.getChild(0));
        ASTNodes.CraftingIngredientListNode results = (ASTNodes.CraftingIngredientListNode) visitCrafting_ingredient_list((DSLParser.Crafting_ingredient_listContext) ctx.getChild(2));

        return new ASTNodes.CraftingRuleNode(ingredients, strictness, results, ctx);
    }

    @Override
    public ASTNodes.Visitable visitCrafting_solution(DSLParser.Crafting_solutionContext ctx) {
        return super.visitCrafting_solution(ctx);
    }

    @Override
    public ASTNodes.Visitable visitChoice_answer(DSLParser.Choice_answerContext ctx) {
        Token answerText = ctx.answer_text;
        ASTNodes.TextNode answerTextNode = new ASTNodes.TextNode(answerText);
        ASTNodes.ChoiceAnswerCorrectness correctness = ASTNodes.ChoiceAnswerCorrectness.fromString(ctx.prefix.getText());


        return new ASTNodes.ChoiceAnswerNode(correctness == ASTNodes.ChoiceAnswerCorrectness.CORRECT, answerTextNode, ctx);
    }

    public ASTNodes.AliasTextNode handleAliasText(DSLParser.Alias_textContext ctx) {


        if (ctx instanceof DSLParser.TextAnswerContext) {
            Token text = ((DSLParser.TextAnswerContext) ctx).text;
            ASTNodes.TextNode textNode = new ASTNodes.TextNode(text);

            return new ASTNodes.TextAnswerNode(textNode, ctx);
        } else if (ctx instanceof DSLParser.TextAliasAnswerContext) {
            Token text = ((DSLParser.TextAliasAnswerContext) ctx).text;
            ASTNodes.TextNode textNode = new ASTNodes.TextNode(text);
            Token alias = ((DSLParser.TextAliasAnswerContext) ctx).alias;
            ASTNodes.IdNode aliasNode = new ASTNodes.IdNode(alias);

            createVariable(aliasNode, SymbolTable.SymbolType.ALIAS);

            return new ASTNodes.TextAliasAnswerNode(aliasNode, textNode, ctx);
        } else if (ctx instanceof DSLParser.AliasAnswerContext) {
            Token alias = ((DSLParser.AliasAnswerContext) ctx).alias;
            ASTNodes.IdNode aliasNode = new ASTNodes.IdNode(alias);

            return new ASTNodes.AliasAnswerNode(aliasNode, ctx);
        }

        return null;
    }

    @Override
    public ASTNodes.Visitable visitLeftBlank(DSLParser.LeftBlankContext ctx) {
        ASTNodes.AliasTextNode right = handleAliasText(ctx.right);

        return new ASTNodes.MatchingAnswerNode(null, right, ctx);
    }

    @Override
    public ASTNodes.Visitable visitBothText(DSLParser.BothTextContext ctx) {
        ASTNodes.AliasTextNode left = handleAliasText(ctx.left);
        ASTNodes.AliasTextNode right = handleAliasText(ctx.right);


        return new ASTNodes.MatchingAnswerNode(left, right, ctx);
    }

    @Override
    public ASTNodes.Visitable visitRightBlank(DSLParser.RightBlankContext ctx) {
        ASTNodes.AliasTextNode left = handleAliasText(ctx.left);

        return new ASTNodes.MatchingAnswerNode(left, null, ctx);
    }

    @Override
    public ASTNodes.Visitable visitParameter_answer(DSLParser.Parameter_answerContext ctx) {
        ASTNodes.CodeNode parameter = new ASTNodes.CodeNode(ctx.parameter);

        ASTNodes.TextNode value = new ASTNodes.TextNode(ctx.value);
        return new ASTNodes.ParameterAnswerNode(parameter, value, ctx);
    }

    @Override
    public ASTNodes.Visitable visitExplain(DSLParser.ExplainContext ctx) {

        ASTNodes.TextNode textNode = new ASTNodes.TextNode(ctx.text);

        return new ASTNodes.ExplanationNode(textNode, ctx);
    }

    @Override
    public ASTNodes.Visitable visitTask_composition(DSLParser.Task_compositionContext ctx) {
        ASTNodes.IdNode id = new ASTNodes.IdNode(ctx.id);

        createVariable(id, SymbolTable.SymbolType.TASK_COMPOSITION);

        ASTNodes.TaskCompositionNodeList subtaskList = (ASTNodes.TaskCompositionNodeList) visitTask_composition_content_list(ctx.list);

        return new ASTNodes.TaskCompositionNode(id, subtaskList.subtasks, ctx);
    }

    @Override
    public ASTNodes.Visitable visitTask_composition_content_list(DSLParser.Task_composition_content_listContext ctx) {


        List<ASTNodes.TaskCompositionSubtaskNode> subtaskList = new ArrayList<>();

        for (ParseTree child : ctx.children) {
            if (child instanceof DSLParser.RequiredTaskContext) {
                subtaskList.add((ASTNodes.TaskCompositionSubtaskNode) visitRequiredTask(((DSLParser.RequiredTaskContext) child)));
            }
            if (child instanceof DSLParser.OptionalTaskContext) {
                subtaskList.add((ASTNodes.TaskCompositionSubtaskNode) visitOptionalTask(((DSLParser.OptionalTaskContext) child)));
            }
        }

        return new ASTNodes.TaskCompositionNodeList(subtaskList, ctx);
    }

    @Override
    public ASTNodes.Visitable visitRequiredTask(DSLParser.RequiredTaskContext ctx) {
        ASTNodes.IdNode id = new ASTNodes.IdNode(ctx.id);

        return new ASTNodes.TaskCompositionSubtaskNode(id, true, ctx);
    }

    @Override
    public ASTNodes.Visitable visitOptionalTask(DSLParser.OptionalTaskContext ctx) {
        ASTNodes.IdNode id = new ASTNodes.IdNode(ctx.id);

        return new ASTNodes.TaskCompositionSubtaskNode(id, false, ctx);
    }

    @Override
    public ASTNodes.Visitable visitImport_statement(DSLParser.Import_statementContext ctx) {
        ASTNodes.TextNode path = new ASTNodes.TextNode(ctx.path);

        return new ASTNodes.ImportStatementNode(path, ctx);
    }

    @Override
    public ASTNodes.Visitable visitTextAnswer(DSLParser.TextAnswerContext ctx) {
        return super.visitTextAnswer(ctx);
    }

    @Override
    public ASTNodes.Visitable visitTextAliasAnswer(DSLParser.TextAliasAnswerContext ctx) {
        return super.visitTextAliasAnswer(ctx);
    }

    @Override
    public ASTNodes.Visitable visitAliasAnswer(DSLParser.AliasAnswerContext ctx) {
        return super.visitAliasAnswer(ctx);
    }

    @Override
    public ASTNodes.Visitable visitTask_config(DSLParser.Task_configContext ctx) {
        ASTNodes.IdNode id = new ASTNodes.IdNode(ctx.config_id);

        createVariable(id, SymbolTable.SymbolType.TASK_CONFIGURATION);

        ASTNodes.TaskConfigContentNode taskConfigContentNode = (ASTNodes.TaskConfigContentNode) visitTask_config_content(ctx.config);

        return new ASTNodes.TaskConfigNode(id, taskConfigContentNode, ctx);
    }

    @Override
    public ASTNodes.Visitable visitTask_config_content(DSLParser.Task_config_contentContext ctx) {
        ASTNodes.IdNode id = new ASTNodes.IdNode(ctx.id);

        ASTNodes.TaskConfigContentNode config = null;
        ASTNodes.TaskConfigBranchNode branch = null;

        if (ctx.config_content != null) { // handle config content
            config = (ASTNodes.TaskConfigContentNode) visitTask_config_content(ctx.config_content);
        }
        if (ctx.config_branch != null) { // handle config branch
            branch = (ASTNodes.TaskConfigBranchNode) visitTask_config_branch(ctx.config_branch);
        }

        return new ASTNodes.TaskConfigContentNode(id, config, branch, ctx);
    }

    @Override
    public ASTNodes.Visitable visitTask_config_branch(DSLParser.Task_config_branchContext ctx) {
        ASTNodes.TaskConfigContentNode correctBranch = (ASTNodes.TaskConfigContentNode) visitTask_config_content(ctx.correctBranch);
        ASTNodes.TaskConfigContentNode falseBranch = (ASTNodes.TaskConfigContentNode) visitTask_config_content(ctx.falseBranch);

        return new ASTNodes.TaskConfigBranchNode(correctBranch, falseBranch, ctx);
    }

//    @Override
//    public ASTNodes.Visitable visitAnswerList(DSLParser.AnswerListContext ctx) {
//        return super.visitAnswerList(ctx);
//    }


    @Override
    public ASTNodes.Visitable visitChoiceAnswer(DSLParser.ChoiceAnswerContext ctx) {
        return super.visitChoiceAnswer(ctx);
    }

    @Override
    public ASTNodes.Visitable visitMatchingAnswer(DSLParser.MatchingAnswerContext ctx) {
        return super.visitMatchingAnswer(ctx);
    }

    @Override
    public ASTNodes.Visitable visitParameterAnswer(DSLParser.ParameterAnswerContext ctx) {
        return super.visitParameterAnswer(ctx);
    }

    @Override
    public ASTNodes.Visitable visitCraftingAnswer(DSLParser.CraftingAnswerContext ctx) {
        return super.visitCraftingAnswer(ctx);
    }

    @Override
    public ASTNodes.Visitable visitAnswer_list(DSLParser.Answer_listContext ctx) {
        List<ASTNodes.AnswerNode> answers = ctx.children.stream().filter(child -> child instanceof DSLParser.AnswerContext).map(child -> handleAnswer((DSLParser.AnswerContext) child)).toList();


        return new ASTNodes.AnswerListNode(answers, ctx);
    }

    @Override
    public ASTNodes.Visitable visitTask_variant_list(DSLParser.Task_variant_listContext ctx) {
        List<ASTNodes.TaskVariantNode> variants = ctx.children.stream().filter(child -> child instanceof DSLParser.Task_variantContext).map(child -> (ASTNodes.TaskVariantNode) visitTask_variant((DSLParser.Task_variantContext) child)).toList();


        return new ASTNodes.TaskVariantListNode(variants, ctx);
    }

    @Override
    public ASTNodes.Visitable visitTask_answers(DSLParser.Task_answersContext ctx) {
        return super.visitTask_answers(ctx);
    }

    @Override
    public ASTNodes.Visitable visitSingleTerm(DSLParser.SingleTermContext ctx) {
        ParseTree child = ctx.children.get(0);
        if (child instanceof DSLParser.AndContext) {
            return visitAnd((DSLParser.AndContext) child);
        } else if (child instanceof DSLParser.SingleFactorContext) {
            return visitSingleFactor((DSLParser.SingleFactorContext) child);
        }
        return null;
    }

    @Override
    public ASTNodes.Visitable visitOr(DSLParser.OrContext ctx) {
        ParseTree left = ctx.children.get(0);
        ParseTree right = ctx.children.get(2);

        ASTNodes.AnswerSelectionExpressionNode leftNode = null;
        ASTNodes.AnswerSelectionExpressionNode rightNode = null;

        if (left instanceof DSLParser.SingleTermContext) {
            leftNode = (ASTNodes.AnswerSelectionExpressionNode) visitSingleTerm((DSLParser.SingleTermContext) left);
        }

        if (right instanceof DSLParser.SingleFactorContext) {
            ASTNodes.Visitable x = visitSingleFactor((DSLParser.SingleFactorContext) right);
            rightNode = (ASTNodes.AnswerSelectionTermNode) x;
        } else if (right instanceof DSLParser.AndContext) {
            rightNode = (ASTNodes.AnswerSelectionTermNode) visitAnd((DSLParser.AndContext) right);
        }

        return new ASTNodes.AnswerSelectionOrNode(leftNode, rightNode, ctx);
    }

    @Override
    public ASTNodes.Visitable visitAnd(DSLParser.AndContext ctx) {
        ParseTree left = ctx.children.get(0);
        ParseTree right = ctx.children.get(2);

        ASTNodes.AnswerSelectionExpressionNode leftNode = null;
        ASTNodes.AnswerSelectionExpressionNode rightNode = null;

        if (left instanceof DSLParser.SingleFactorContext) {
            leftNode = (ASTNodes.AnswerSelectionExpressionNode) visitSingleFactor((DSLParser.SingleFactorContext) left);
        } else if (left instanceof DSLParser.AndContext) {
            leftNode = (ASTNodes.AnswerSelectionExpressionNode) visitAnd((DSLParser.AndContext) left);
        }


        if (right instanceof DSLParser.ParenthesisContext) {
            ASTNodes.Visitable x = visitParenthesis((DSLParser.ParenthesisContext) right);
            rightNode = (ASTNodes.AnswerSelectionExpressionNode) visitParenthesis((DSLParser.ParenthesisContext) right);
        } else if (right instanceof DSLParser.FactorContext) {
            rightNode = (ASTNodes.AnswerSelectionExpressionNode) visitFactor((DSLParser.FactorContext) right);
        }

        return new ASTNodes.AnswerSelectionAndNode(leftNode, rightNode, ctx);
    }

    @Override
    public ASTNodes.Visitable visitSingleFactor(DSLParser.SingleFactorContext ctx) {
        ParseTree child = ctx.children.get(0);
        if (child instanceof DSLParser.ParenthesisContext) {
            return visitParenthesis((DSLParser.ParenthesisContext) child);
        } else if (child instanceof DSLParser.FactorContext) {
            return visitFactor((DSLParser.FactorContext) child);
        }
        return null;
    }

    @Override
    public ASTNodes.Visitable visitParenthesis(DSLParser.ParenthesisContext ctx) {
        ParseTree expr = ctx.getChild(1);
        return visitOr((DSLParser.OrContext) expr);
    }

    @Override
    public ASTNodes.Visitable visitFactor(DSLParser.FactorContext ctx) {
        Token amount = ctx.amount;
        ASTNodes.NumberNode amountNode = new ASTNodes.NumberNode(amount, 1);
        ASTNodes.AnswerListNode answers = (ASTNodes.AnswerListNode) visitAnswer_list(ctx.answer_list());
        return new ASTNodes.AnswerSelectionNode(amountNode, answers, ctx);
    }
}
