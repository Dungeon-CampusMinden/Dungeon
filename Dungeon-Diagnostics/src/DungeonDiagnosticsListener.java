// Generated from C:/Users/bjarn/VS_Projects/Dungeon/Dungeon-Diagnostics/DungeonDiagnostics.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link DungeonDiagnosticsParser}.
 */
public interface DungeonDiagnosticsListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(DungeonDiagnosticsParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(DungeonDiagnosticsParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#definition}.
	 * @param ctx the parse tree
	 */
	void enterDefinition(DungeonDiagnosticsParser.DefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#definition}.
	 * @param ctx the parse tree
	 */
	void exitDefinition(DungeonDiagnosticsParser.DefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#singleChoiceTask}.
	 * @param ctx the parse tree
	 */
	void enterSingleChoiceTask(DungeonDiagnosticsParser.SingleChoiceTaskContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#singleChoiceTask}.
	 * @param ctx the parse tree
	 */
	void exitSingleChoiceTask(DungeonDiagnosticsParser.SingleChoiceTaskContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#multipleChoiceTask}.
	 * @param ctx the parse tree
	 */
	void enterMultipleChoiceTask(DungeonDiagnosticsParser.MultipleChoiceTaskContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#multipleChoiceTask}.
	 * @param ctx the parse tree
	 */
	void exitMultipleChoiceTask(DungeonDiagnosticsParser.MultipleChoiceTaskContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#assignTask}.
	 * @param ctx the parse tree
	 */
	void enterAssignTask(DungeonDiagnosticsParser.AssignTaskContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#assignTask}.
	 * @param ctx the parse tree
	 */
	void exitAssignTask(DungeonDiagnosticsParser.AssignTaskContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#field}.
	 * @param ctx the parse tree
	 */
	void enterField(DungeonDiagnosticsParser.FieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#field}.
	 * @param ctx the parse tree
	 */
	void exitField(DungeonDiagnosticsParser.FieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#descriptionField}.
	 * @param ctx the parse tree
	 */
	void enterDescriptionField(DungeonDiagnosticsParser.DescriptionFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#descriptionField}.
	 * @param ctx the parse tree
	 */
	void exitDescriptionField(DungeonDiagnosticsParser.DescriptionFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#answersField}.
	 * @param ctx the parse tree
	 */
	void enterAnswersField(DungeonDiagnosticsParser.AnswersFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#answersField}.
	 * @param ctx the parse tree
	 */
	void exitAnswersField(DungeonDiagnosticsParser.AnswersFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#correctAnswerIndexField}.
	 * @param ctx the parse tree
	 */
	void enterCorrectAnswerIndexField(DungeonDiagnosticsParser.CorrectAnswerIndexFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#correctAnswerIndexField}.
	 * @param ctx the parse tree
	 */
	void exitCorrectAnswerIndexField(DungeonDiagnosticsParser.CorrectAnswerIndexFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#correctAnswerIndicesField}.
	 * @param ctx the parse tree
	 */
	void enterCorrectAnswerIndicesField(DungeonDiagnosticsParser.CorrectAnswerIndicesFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#correctAnswerIndicesField}.
	 * @param ctx the parse tree
	 */
	void exitCorrectAnswerIndicesField(DungeonDiagnosticsParser.CorrectAnswerIndicesFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#solutionField}.
	 * @param ctx the parse tree
	 */
	void enterSolutionField(DungeonDiagnosticsParser.SolutionFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#solutionField}.
	 * @param ctx the parse tree
	 */
	void exitSolutionField(DungeonDiagnosticsParser.SolutionFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#pair}.
	 * @param ctx the parse tree
	 */
	void enterPair(DungeonDiagnosticsParser.PairContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#pair}.
	 * @param ctx the parse tree
	 */
	void exitPair(DungeonDiagnosticsParser.PairContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#term}.
	 * @param ctx the parse tree
	 */
	void enterTerm(DungeonDiagnosticsParser.TermContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#term}.
	 * @param ctx the parse tree
	 */
	void exitTerm(DungeonDiagnosticsParser.TermContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#pointsField}.
	 * @param ctx the parse tree
	 */
	void enterPointsField(DungeonDiagnosticsParser.PointsFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#pointsField}.
	 * @param ctx the parse tree
	 */
	void exitPointsField(DungeonDiagnosticsParser.PointsFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#pointsToPassField}.
	 * @param ctx the parse tree
	 */
	void enterPointsToPassField(DungeonDiagnosticsParser.PointsToPassFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#pointsToPassField}.
	 * @param ctx the parse tree
	 */
	void exitPointsToPassField(DungeonDiagnosticsParser.PointsToPassFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#explanationField}.
	 * @param ctx the parse tree
	 */
	void enterExplanationField(DungeonDiagnosticsParser.ExplanationFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#explanationField}.
	 * @param ctx the parse tree
	 */
	void exitExplanationField(DungeonDiagnosticsParser.ExplanationFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#gradingFunctionField}.
	 * @param ctx the parse tree
	 */
	void enterGradingFunctionField(DungeonDiagnosticsParser.GradingFunctionFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#gradingFunctionField}.
	 * @param ctx the parse tree
	 */
	void exitGradingFunctionField(DungeonDiagnosticsParser.GradingFunctionFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#scenarioBuilderField}.
	 * @param ctx the parse tree
	 */
	void enterScenarioBuilderField(DungeonDiagnosticsParser.ScenarioBuilderFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#scenarioBuilderField}.
	 * @param ctx the parse tree
	 */
	void exitScenarioBuilderField(DungeonDiagnosticsParser.ScenarioBuilderFieldContext ctx);
}