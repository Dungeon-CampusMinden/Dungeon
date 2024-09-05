// Generated from C:/Users/bjarn/VS_Projects/Dungeon/Dungeon-Diagnostics/DungeonDiagnostics.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link DungeonDiagnosticsParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface DungeonDiagnosticsVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(DungeonDiagnosticsParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefinition(DungeonDiagnosticsParser.DefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#singleChoiceTask}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleChoiceTask(DungeonDiagnosticsParser.SingleChoiceTaskContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#multipleChoiceTask}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultipleChoiceTask(DungeonDiagnosticsParser.MultipleChoiceTaskContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#assignTask}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignTask(DungeonDiagnosticsParser.AssignTaskContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#field}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitField(DungeonDiagnosticsParser.FieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#descriptionField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescriptionField(DungeonDiagnosticsParser.DescriptionFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#answersField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnswersField(DungeonDiagnosticsParser.AnswersFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#correctAnswerIndexField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCorrectAnswerIndexField(DungeonDiagnosticsParser.CorrectAnswerIndexFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#correctAnswerIndicesField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCorrectAnswerIndicesField(DungeonDiagnosticsParser.CorrectAnswerIndicesFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#solutionField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSolutionField(DungeonDiagnosticsParser.SolutionFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#pair}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPair(DungeonDiagnosticsParser.PairContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm(DungeonDiagnosticsParser.TermContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#pointsField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPointsField(DungeonDiagnosticsParser.PointsFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#pointsToPassField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPointsToPassField(DungeonDiagnosticsParser.PointsToPassFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#explanationField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplanationField(DungeonDiagnosticsParser.ExplanationFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#gradingFunctionField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGradingFunctionField(DungeonDiagnosticsParser.GradingFunctionFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#scenarioBuilderField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScenarioBuilderField(DungeonDiagnosticsParser.ScenarioBuilderFieldContext ctx);
}