package newdsl.interpreter;

import dslinterop.nativescenariobuilder.NewDSLNativeScenarioBuilder;
import newdsl.ast.ASTNodes;
import newdsl.ast.ASTTraverser;
import newdsl.common.DSLError;
import newdsl.common.DSLErrorHandler;
import newdsl.common.Utils;
import newdsl.graph.TaskConfigGraphBuilder;
import newdsl.graph.TaskDependencyGraph;
import newdsl.graph.TaskEdge;
import newdsl.graph.TaskNode;
import newdsl.tasks.*;

import java.util.*;

public class DSLInterpreter extends ASTTraverser<ASTNodes.Visitable> {

    public Environment env;

    public TaskConfiguration currentAssignment;

    public DSLInterpreter() {
        this.env = new Environment(null);
    }

    public List<TaskConfiguration> getAllTaskConfigurations() {

        List<TaskConfiguration> values = this.env.getValues().entrySet().stream().filter(v -> v.getValue() instanceof TaskConfigGraphBuilder.Graph).map(config -> {
            TaskConfiguration conf = new TaskConfiguration();
            conf.setId(config.getKey());
            conf.setGraph(getTaskDependencyGraph(config.getKey()));

            return conf;
        }).toList();

        return values;
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
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.ImportStatementNode importStatementNode) {
        return null;
    }

    private Answer getAnswer(ASTNodes.AnswerNode answerNode) {
        if (answerNode instanceof ASTNodes.ChoiceAnswerNode choiceAnswerNode) {
            ChoiceAnswer choice = new ChoiceAnswer();
            choice.setCorrect(choiceAnswerNode.isCorrect);
            choice.setText(String.valueOf(choiceAnswerNode.text.value));
            return choice;
        } else if (answerNode instanceof ASTNodes.MatchingAnswerNode matchingAnswerNode) {
            MatchingAnswer match = new MatchingAnswer();
            if (matchingAnswerNode.left != null) {
                Optional<String> alias = matchingAnswerNode.left.alias != null ? Optional.of(String.valueOf(matchingAnswerNode.left.alias.value)) : Optional.empty();
                Optional<String> text = matchingAnswerNode.left.text != null ? Optional.of(String.valueOf(matchingAnswerNode.left.text.value)) : Optional.empty();
                match.setLeft(new Alias(alias, text));

                if (match.getLeft().getId().isPresent() && match.getLeft().getText().isPresent()) {
                    this.env.define(match.getLeft().getId().get(), match.getLeft().getText().get());
                }

            }
            if (matchingAnswerNode.right != null) {
                Optional<String> alias = matchingAnswerNode.right.alias != null ? Optional.of(String.valueOf(matchingAnswerNode.right.alias.value)) : Optional.empty();
                Optional<String> text = matchingAnswerNode.right.text != null ? Optional.of(String.valueOf(matchingAnswerNode.right.text.value)) : Optional.empty();
                match.setRight(new Alias(alias, text));

                if (match.getRight().getId().isPresent() && match.getRight().getText().isPresent()) {
                    this.env.define(match.getRight().getId().get(), match.getRight().getText().get());
                }

            }
            return match;
        } else if (answerNode instanceof ASTNodes.ParameterAnswerNode parameterAnswerNode) {
            ParameterAnswer param = new ParameterAnswer();
            param.setParameter(String.valueOf(parameterAnswerNode.parameter.value));
            param.setValue(String.valueOf(parameterAnswerNode.value.value));

            this.env.define(param.getParameter(), param.getValue());

            return param;
        } else if (answerNode instanceof ASTNodes.CraftingIngredientNode craftingIngredientNode) {
            CraftingIngredientAnswer ingredient = new CraftingIngredientAnswer();
            ingredient.setAmount(Integer.parseInt(String.valueOf(craftingIngredientNode.amount.value)));

            Optional<String> alias = craftingIngredientNode.aliasText.alias != null ? Optional.of(String.valueOf(craftingIngredientNode.aliasText.alias.value)) : Optional.empty();
            Optional<String> text = craftingIngredientNode.aliasText.text != null ? Optional.of(String.valueOf(craftingIngredientNode.aliasText.text.value)) : Optional.empty();

            ingredient.setAlias(new Alias(alias, text));

            if (ingredient.getAlias().getId().isPresent() && ingredient.getAlias().getText().isPresent()) {
                this.env.define(ingredient.getAlias().getId().get(), ingredient.getAlias().getText().get());
            }

            return ingredient;
        } else if (answerNode instanceof ASTNodes.CraftingSolutionNode craftingSolutionNode) {
            CraftingSolutionAnswer solution = new CraftingSolutionAnswer();
            solution.setAmount(Integer.parseInt(String.valueOf(craftingSolutionNode.amount.value)));

            Optional<String> alias = craftingSolutionNode.aliasText.alias != null ? Optional.of(String.valueOf(craftingSolutionNode.aliasText.alias.value)) : Optional.empty();
            Optional<String> text = craftingSolutionNode.aliasText.text != null ? Optional.of(String.valueOf(craftingSolutionNode.aliasText.text.value)) : Optional.empty();

            solution.setAlias(new Alias(alias, text));

            if (solution.getAlias().getId().isPresent() && solution.getAlias().getText().isPresent()) {
                this.env.define(solution.getAlias().getId().get(), solution.getAlias().getText().get());
            }

            return solution;
        } else if (answerNode instanceof ASTNodes.CraftingRuleNode craftingRuleNode) {
            CraftingRuleAnswer rule = new CraftingRuleAnswer();
            rule.setStrictness(craftingRuleNode.strictness);
            rule.setLeft(craftingRuleNode.left.stream().map(ingredient -> (CraftingIngredientAnswer) getAnswer(ingredient)).toList());
            rule.setRight(craftingRuleNode.right.stream().map(ingredient -> (CraftingIngredientAnswer) getAnswer(ingredient)).toList());

            return rule;
        }
        return null;
    }

    private <T extends Answer> List<T> getAnswers(ASTNodes.TaskAnswersNode answers) {
        if (answers instanceof ASTNodes.AnswerListNode) { // handle regular answers
            List<ASTNodes.AnswerNode> ans = ((ASTNodes.AnswerListNode) answers).answers;

            return Utils.shuffle(ans.stream().map(a -> (T) getAnswer(a)).toList());
        } else if (answers instanceof ASTNodes.AnswerSelectionExpressionNode) { // handle answer selection
            List<ASTNodes.AnswerNode> selectedAns = selectAnswers((ASTNodes.AnswerSelectionExpressionNode) answers, new ArrayList<ASTNodes.AnswerNode>());

            return Utils.shuffle(selectedAns.stream().map(a -> (T) getAnswer(a)).toList());
        }
        return null;
    }

    private List<ASTNodes.AnswerNode> selectAnswers(ASTNodes.AnswerSelectionExpressionNode answers, ArrayList<ASTNodes.AnswerNode> selectedAns) {

        if (answers instanceof ASTNodes.AnswerSelectionNode) {
            ASTNodes.NumberNode amount = ((ASTNodes.AnswerSelectionNode) answers).amount;
            List<ASTNodes.AnswerNode> ans = ((ASTNodes.AnswerSelectionNode) answers).answers.answers;

            for (int i = 0; i < Integer.parseInt(amount.value.toString()); i++) {
                selectedAns.add(ans.get(i));
            }

        }

        return selectedAns;
    }

    private Optional<String> getOptionalContent(ASTNodes.OptionalTaskContentListNode content, ASTNodes.OptionalContentType type) {
        if (content == null) {
            return Optional.empty();
        }

        Optional<ASTNodes.OptionalTaskContentNode> candidate = content.content.stream().filter(c -> c.type == type).findFirst();

        if (candidate.isEmpty()) {
            return Optional.empty();
        }

        String value = candidate.get().getValue();

        return Optional.of(value);

    }

    private <T extends Task<?>> void handleTaskCreation(ASTNodes.TaskNode taskNode, T task) {
        Environment prev = this.env;

        this.env = new Environment(this.env);

        if (taskNode.taskContent instanceof ASTNodes.TaskBodyNode) { // handle regular body, no variant
            task.setTitle((String) ((ASTNodes.TaskBodyNode) taskNode.taskContent).desc.value);
            task.setAnswers(getAnswers(((ASTNodes.TaskBodyNode) taskNode.taskContent).answers));

            Optional<String> explanation = getOptionalContent(((ASTNodes.TaskBodyNode) taskNode.taskContent).optionalContent, ASTNodes.OptionalContentType.EXPLANATION);
            explanation.ifPresent(task::setExplanation);

            Optional<String> pass = getOptionalContent(((ASTNodes.TaskBodyNode) taskNode.taskContent).optionalContent, ASTNodes.OptionalContentType.PASS);
            pass.ifPresent(task::setCustomPassCode);

            Optional<String> grade = getOptionalContent(((ASTNodes.TaskBodyNode) taskNode.taskContent).optionalContent, ASTNodes.OptionalContentType.GRADE);
            grade.ifPresent(task::setCustomPointsCode);

            if (taskNode.taskHeader.taskType == ASTNodes.TaskType.CALCULATION) {
                Optional<String> solution = getOptionalContent(((ASTNodes.TaskBodyNode) taskNode.taskContent).optionalContent, ASTNodes.OptionalContentType.SOLUTION);
                solution.ifPresent(task::setCustomSolution);
            }
            task.setEnv(this.env);
        } else if (taskNode.taskContent instanceof ASTNodes.TaskVariantBodyNode) {
            // choose variant randomly
            List<ASTNodes.TaskVariantNode> variants = ((ASTNodes.TaskVariantBodyNode) taskNode.taskContent).variants.variants;
            int randomIndex = new Random().nextInt(variants.size());

            ASTNodes.TaskVariantNode randomVariant = variants.get(randomIndex);
            ASTNodes.TaskHeaderNode taskHeader = taskNode.taskHeader;
            handleTaskCreation(new ASTNodes.TaskNode(taskNode.task, taskHeader, randomVariant.taskBody, taskNode.ctx), task);
            // TODO: optionaler Content
        }

        this.env = prev;
    }

    private void createTask(ASTNodes.TaskNode taskNode) {
        String id = (String) taskNode.taskHeader.id.value;

        Task<?> task = null;

        switch (taskNode.taskHeader.taskType) {
            case SINGLE_CHOICE -> {
                SingleChoiceTask sc = new SingleChoiceTask(id, new Environment(this.env));
                handleTaskCreation(taskNode, sc);
                task = sc;
            }

            case MULTIPLE_CHOICE -> {
                MultipleChoiceTask mc = new MultipleChoiceTask(id, new Environment(this.env));
                handleTaskCreation(taskNode, mc);
                task = mc;
            }

            case FILL_IN_THE_BLANK -> {
                FillInTheBlankTask fitb = new FillInTheBlankTask(id, new Environment(this.env));
                handleTaskCreation(taskNode, fitb);
                task = fitb;
            }

            case MATCHING -> {
                MatchingTask match = new MatchingTask(id, new Environment(this.env));
                handleTaskCreation(taskNode, match);
                task = match;
            }

            case CALCULATION -> {
                CalculationTask calc = new CalculationTask(id, new Environment(this.env));
                handleTaskCreation(taskNode, calc);
                task = calc;
            }

            case CRAFTING -> {
                CraftingTask craft = new CraftingTask(id, new Environment(this.env));
                handleTaskCreation(taskNode, craft);
                task = craft;
            }
        }

        this.env.define(id, task);
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.TaskNode taskNode) {
        createTask(taskNode);
        return null;
    }

    @Override
    public ASTNodes.Visitable visit(ASTNodes.TaskCompositionNode taskCompositionNode) {
        String id = (String) taskCompositionNode.id.value;

        TaskComposition taskComposition = new TaskComposition();
        taskComposition.setId(id);
        taskComposition.setSubtasks(taskCompositionNode.substasks.stream().map(subtask -> {
            TaskCompositionSubtask taskCompositionSubtask = new TaskCompositionSubtask();
            taskCompositionSubtask.setRequired(subtask.required);
            taskCompositionSubtask.setId((String) subtask.id.value);
            taskCompositionSubtask.setTask((Task<?>) this.env.get((String) subtask.id.value));

            return taskCompositionSubtask;
        }).toList());

        this.env.define(id, taskComposition);

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
        String id = (String) taskConfigNode.id.value;

        TaskConfigGraphBuilder.Graph graph = new TaskConfigGraphBuilder.Graph((String) taskConfigNode.taskConfigContentNode.id.value);
        TaskConfigGraphBuilder.buildGraph(taskConfigNode, graph);
        TaskConfigGraphBuilder.resolveGraph(graph, this.env);
        this.env.define(id, graph);
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

    public TaskDependencyGraph getTaskDependencyGraph(String configId) {
        ArrayList<TaskEdge> edges = new ArrayList<>();
        ArrayList<TaskNode> nodes = new ArrayList<>();

        TaskConfigGraphBuilder.Graph config = (TaskConfigGraphBuilder.Graph) this.env.get(configId);

        config.getNodes().forEach((nodeId, neighbors) -> {
            Object node = this.env.get(nodeId);

            if (node instanceof TaskComposition) {
                ((TaskComposition) node).getSubtasks().forEach(t -> nodes.add(new TaskNode(t.getTask())));
            } else if (node instanceof Task) {
                TaskNode task = new TaskNode((Task) node);
                nodes.add(task);
            }

        });

        config.getEdges().forEach(graphEdge -> {
            Optional<TaskNode> from = nodes.stream().filter(Objects::nonNull).filter(node -> node.getTask().getId().equals(graphEdge.fromId)).findFirst();
            Optional<TaskNode> to = nodes.stream().filter(Objects::nonNull).filter(node -> node.getTask().getId().equals(graphEdge.toId)).findFirst();

            if (from.isPresent() && to.isPresent()) {
                edges.add(new TaskEdge(graphEdge.type, from.get(), to.get()));
            }
        });

        return new TaskDependencyGraph(edges, nodes);
    }

    public float gradeTask(String taskId, Set<Answer> answers) {
        Task task = (Task) this.env.get(taskId);
        float points = task.gradeTask(answers);
        return points;
    }

    public Optional<Object> buildTask(Task task) {
        ASTNodes.TaskType taskType = task.getType();

        switch (taskType) {
            case SINGLE_CHOICE -> {
                return Optional.of(NewDSLNativeScenarioBuilder.buildSingleChoiceTask((SingleChoiceTask) task));
            }
            case MULTIPLE_CHOICE -> {
                return Optional.of(NewDSLNativeScenarioBuilder.buildMultipleChoiceTask((MultipleChoiceTask) task));
            }
            default -> {
                return Optional.empty();
            }
        }

    }

}
