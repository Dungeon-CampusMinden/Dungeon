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
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#fn_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFn_def(DungeonDiagnosticsParser.Fn_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStmt(DungeonDiagnosticsParser.StmtContext ctx);
	/**
	 * Visit a parse tree produced by the {@code for_loop}
	 * labeled alternative in {@link DungeonDiagnosticsParser#loop_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_loop(DungeonDiagnosticsParser.For_loopContext ctx);
	/**
	 * Visit a parse tree produced by the {@code for_loop_counting}
	 * labeled alternative in {@link DungeonDiagnosticsParser#loop_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_loop_counting(DungeonDiagnosticsParser.For_loop_countingContext ctx);
	/**
	 * Visit a parse tree produced by the {@code while_loop}
	 * labeled alternative in {@link DungeonDiagnosticsParser#loop_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhile_loop(DungeonDiagnosticsParser.While_loopContext ctx);
	/**
	 * Visit a parse tree produced by the {@code var_decl_assignment}
	 * labeled alternative in {@link DungeonDiagnosticsParser#var_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar_decl_assignment(DungeonDiagnosticsParser.Var_decl_assignmentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code var_decl_type_decl}
	 * labeled alternative in {@link DungeonDiagnosticsParser#var_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar_decl_type_decl(DungeonDiagnosticsParser.Var_decl_type_declContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(DungeonDiagnosticsParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code method_call_expression}
	 * labeled alternative in {@link DungeonDiagnosticsParser#expression_rhs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethod_call_expression(DungeonDiagnosticsParser.Method_call_expressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code member_access_expression}
	 * labeled alternative in {@link DungeonDiagnosticsParser#expression_rhs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMember_access_expression(DungeonDiagnosticsParser.Member_access_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#assignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment(DungeonDiagnosticsParser.AssignmentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code assignee_func_call}
	 * labeled alternative in {@link DungeonDiagnosticsParser#assignee}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignee_func_call(DungeonDiagnosticsParser.Assignee_func_callContext ctx);
	/**
	 * Visit a parse tree produced by the {@code assignee_qualified_name}
	 * labeled alternative in {@link DungeonDiagnosticsParser#assignee}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignee_qualified_name(DungeonDiagnosticsParser.Assignee_qualified_nameContext ctx);
	/**
	 * Visit a parse tree produced by the {@code assignee_identifier}
	 * labeled alternative in {@link DungeonDiagnosticsParser#assignee}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignee_identifier(DungeonDiagnosticsParser.Assignee_identifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#logic_or}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogic_or(DungeonDiagnosticsParser.Logic_orContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#logic_and}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogic_and(DungeonDiagnosticsParser.Logic_andContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#equality}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEquality(DungeonDiagnosticsParser.EqualityContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#comparison}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparison(DungeonDiagnosticsParser.ComparisonContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm(DungeonDiagnosticsParser.TermContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#factor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFactor(DungeonDiagnosticsParser.FactorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#unary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnary(DungeonDiagnosticsParser.UnaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#func_call}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_call(DungeonDiagnosticsParser.Func_callContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#stmt_block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStmt_block(DungeonDiagnosticsParser.Stmt_blockContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#stmt_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStmt_list(DungeonDiagnosticsParser.Stmt_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#return_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturn_stmt(DungeonDiagnosticsParser.Return_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#conditional_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditional_stmt(DungeonDiagnosticsParser.Conditional_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#else_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElse_stmt(DungeonDiagnosticsParser.Else_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#ret_type_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRet_type_def(DungeonDiagnosticsParser.Ret_type_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#param_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam_def(DungeonDiagnosticsParser.Param_defContext ctx);
	/**
	 * Visit a parse tree produced by the {@code map_param_type}
	 * labeled alternative in {@link DungeonDiagnosticsParser#type_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMap_param_type(DungeonDiagnosticsParser.Map_param_typeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code task_types}
	 * labeled alternative in {@link DungeonDiagnosticsParser#type_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTask_types(DungeonDiagnosticsParser.Task_typesContext ctx);
	/**
	 * Visit a parse tree produced by the {@code id_param_type}
	 * labeled alternative in {@link DungeonDiagnosticsParser#type_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitId_param_type(DungeonDiagnosticsParser.Id_param_typeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code list_param_type}
	 * labeled alternative in {@link DungeonDiagnosticsParser#type_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitList_param_type(DungeonDiagnosticsParser.List_param_typeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code set_param_type}
	 * labeled alternative in {@link DungeonDiagnosticsParser#type_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_param_type(DungeonDiagnosticsParser.Set_param_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#taskTypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTaskTypes(DungeonDiagnosticsParser.TaskTypesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#param_def_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam_def_list(DungeonDiagnosticsParser.Param_def_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#entity_type_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEntity_type_def(DungeonDiagnosticsParser.Entity_type_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#item_type_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitItem_type_def(DungeonDiagnosticsParser.Item_type_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#component_def_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComponent_def_list(DungeonDiagnosticsParser.Component_def_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#aggregate_value_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregate_value_def(DungeonDiagnosticsParser.Aggregate_value_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#object_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObject_def(DungeonDiagnosticsParser.Object_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#property_def_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProperty_def_list(DungeonDiagnosticsParser.Property_def_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#property_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProperty_def(DungeonDiagnosticsParser.Property_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#expression_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression_list(DungeonDiagnosticsParser.Expression_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#grouped_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrouped_expression(DungeonDiagnosticsParser.Grouped_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#list_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitList_definition(DungeonDiagnosticsParser.List_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#set_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_definition(DungeonDiagnosticsParser.Set_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimary(DungeonDiagnosticsParser.PrimaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#dot_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDot_def(DungeonDiagnosticsParser.Dot_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#dot_stmt_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDot_stmt_list(DungeonDiagnosticsParser.Dot_stmt_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#dot_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDot_stmt(DungeonDiagnosticsParser.Dot_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#dot_edge_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDot_edge_stmt(DungeonDiagnosticsParser.Dot_edge_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#dot_node_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDot_node_list(DungeonDiagnosticsParser.Dot_node_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#dot_edge_RHS}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDot_edge_RHS(DungeonDiagnosticsParser.Dot_edge_RHSContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#dot_node_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDot_node_stmt(DungeonDiagnosticsParser.Dot_node_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#dot_attr_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDot_attr_list(DungeonDiagnosticsParser.Dot_attr_listContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dot_attr_id}
	 * labeled alternative in {@link DungeonDiagnosticsParser#dot_attr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDot_attr_id(DungeonDiagnosticsParser.Dot_attr_idContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dot_attr_dependency_type}
	 * labeled alternative in {@link DungeonDiagnosticsParser#dot_attr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDot_attr_dependency_type(DungeonDiagnosticsParser.Dot_attr_dependency_typeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dt_sequence}
	 * labeled alternative in {@link DungeonDiagnosticsParser#dependency_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDt_sequence(DungeonDiagnosticsParser.Dt_sequenceContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dt_subtask_mandatory}
	 * labeled alternative in {@link DungeonDiagnosticsParser#dependency_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDt_subtask_mandatory(DungeonDiagnosticsParser.Dt_subtask_mandatoryContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dt_subtask_optional}
	 * labeled alternative in {@link DungeonDiagnosticsParser#dependency_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDt_subtask_optional(DungeonDiagnosticsParser.Dt_subtask_optionalContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dt_conditional_correct}
	 * labeled alternative in {@link DungeonDiagnosticsParser#dependency_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDt_conditional_correct(DungeonDiagnosticsParser.Dt_conditional_correctContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dt_conditional_false}
	 * labeled alternative in {@link DungeonDiagnosticsParser#dependency_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDt_conditional_false(DungeonDiagnosticsParser.Dt_conditional_falseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dt_sequence_and}
	 * labeled alternative in {@link DungeonDiagnosticsParser#dependency_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDt_sequence_and(DungeonDiagnosticsParser.Dt_sequence_andContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dt_sequence_or}
	 * labeled alternative in {@link DungeonDiagnosticsParser#dependency_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDt_sequence_or(DungeonDiagnosticsParser.Dt_sequence_orContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#dungeonConfig}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDungeonConfig(DungeonDiagnosticsParser.DungeonConfigContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#graph}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGraph(DungeonDiagnosticsParser.GraphContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#taskDependency}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTaskDependency(DungeonDiagnosticsParser.TaskDependencyContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#dependencyAttribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDependencyAttribute(DungeonDiagnosticsParser.DependencyAttributeContext ctx);
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
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#dependencyGraphField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDependencyGraphField(DungeonDiagnosticsParser.DependencyGraphFieldContext ctx);
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
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#pairVal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPairVal(DungeonDiagnosticsParser.PairValContext ctx);
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
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#entity_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEntity_type(DungeonDiagnosticsParser.Entity_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#componentList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComponentList(DungeonDiagnosticsParser.ComponentListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#component}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComponent(DungeonDiagnosticsParser.ComponentContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#attributeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttributeList(DungeonDiagnosticsParser.AttributeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#attribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttribute(DungeonDiagnosticsParser.AttributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(DungeonDiagnosticsParser.ValueContext ctx);
}