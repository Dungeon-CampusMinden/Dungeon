// Generated from C:/Users/bjarn/VS_Projects/Dungeon/Dungeon-Diagnostics/grammer.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link grammerParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface grammerVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link grammerParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(grammerParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefinition(grammerParser.DefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#fn_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFn_def(grammerParser.Fn_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStmt(grammerParser.StmtContext ctx);
	/**
	 * Visit a parse tree produced by the {@code for_loop}
	 * labeled alternative in {@link grammerParser#loop_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_loop(grammerParser.For_loopContext ctx);
	/**
	 * Visit a parse tree produced by the {@code for_loop_counting}
	 * labeled alternative in {@link grammerParser#loop_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_loop_counting(grammerParser.For_loop_countingContext ctx);
	/**
	 * Visit a parse tree produced by the {@code while_loop}
	 * labeled alternative in {@link grammerParser#loop_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhile_loop(grammerParser.While_loopContext ctx);
	/**
	 * Visit a parse tree produced by the {@code var_decl_assignment}
	 * labeled alternative in {@link grammerParser#var_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar_decl_assignment(grammerParser.Var_decl_assignmentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code var_decl_type_decl}
	 * labeled alternative in {@link grammerParser#var_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar_decl_type_decl(grammerParser.Var_decl_type_declContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(grammerParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code method_call_expression}
	 * labeled alternative in {@link grammerParser#expression_rhs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethod_call_expression(grammerParser.Method_call_expressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code member_access_expression}
	 * labeled alternative in {@link grammerParser#expression_rhs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMember_access_expression(grammerParser.Member_access_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#assignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment(grammerParser.AssignmentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code assignee_func_call}
	 * labeled alternative in {@link grammerParser#assignee}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignee_func_call(grammerParser.Assignee_func_callContext ctx);
	/**
	 * Visit a parse tree produced by the {@code assignee_qualified_name}
	 * labeled alternative in {@link grammerParser#assignee}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignee_qualified_name(grammerParser.Assignee_qualified_nameContext ctx);
	/**
	 * Visit a parse tree produced by the {@code assignee_identifier}
	 * labeled alternative in {@link grammerParser#assignee}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignee_identifier(grammerParser.Assignee_identifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#logic_or}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogic_or(grammerParser.Logic_orContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#logic_and}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogic_and(grammerParser.Logic_andContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#equality}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEquality(grammerParser.EqualityContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#comparison}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparison(grammerParser.ComparisonContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm(grammerParser.TermContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#factor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFactor(grammerParser.FactorContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#unary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnary(grammerParser.UnaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#func_call}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_call(grammerParser.Func_callContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#stmt_block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStmt_block(grammerParser.Stmt_blockContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#stmt_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStmt_list(grammerParser.Stmt_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#return_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturn_stmt(grammerParser.Return_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#conditional_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditional_stmt(grammerParser.Conditional_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#else_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElse_stmt(grammerParser.Else_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#ret_type_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRet_type_def(grammerParser.Ret_type_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#param_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam_def(grammerParser.Param_defContext ctx);
	/**
	 * Visit a parse tree produced by the {@code map_param_type}
	 * labeled alternative in {@link grammerParser#type_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMap_param_type(grammerParser.Map_param_typeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code task_types}
	 * labeled alternative in {@link grammerParser#type_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTask_types(grammerParser.Task_typesContext ctx);
	/**
	 * Visit a parse tree produced by the {@code id_param_type}
	 * labeled alternative in {@link grammerParser#type_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitId_param_type(grammerParser.Id_param_typeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code list_param_type}
	 * labeled alternative in {@link grammerParser#type_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitList_param_type(grammerParser.List_param_typeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code set_param_type}
	 * labeled alternative in {@link grammerParser#type_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_param_type(grammerParser.Set_param_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#taskTypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTaskTypes(grammerParser.TaskTypesContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#param_def_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam_def_list(grammerParser.Param_def_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#entity_type_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEntity_type_def(grammerParser.Entity_type_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#item_type_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitItem_type_def(grammerParser.Item_type_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#component_def_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComponent_def_list(grammerParser.Component_def_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#aggregate_value_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregate_value_def(grammerParser.Aggregate_value_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#object_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObject_def(grammerParser.Object_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#property_def_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProperty_def_list(grammerParser.Property_def_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#property_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProperty_def(grammerParser.Property_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#expression_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression_list(grammerParser.Expression_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#grouped_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrouped_expression(grammerParser.Grouped_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#list_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitList_definition(grammerParser.List_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#set_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_definition(grammerParser.Set_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimary(grammerParser.PrimaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#dot_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDot_def(grammerParser.Dot_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#dot_stmt_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDot_stmt_list(grammerParser.Dot_stmt_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#dot_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDot_stmt(grammerParser.Dot_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#dot_edge_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDot_edge_stmt(grammerParser.Dot_edge_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#dot_node_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDot_node_list(grammerParser.Dot_node_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#dot_edge_RHS}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDot_edge_RHS(grammerParser.Dot_edge_RHSContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#dot_node_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDot_node_stmt(grammerParser.Dot_node_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#dot_attr_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDot_attr_list(grammerParser.Dot_attr_listContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dot_attr_id}
	 * labeled alternative in {@link grammerParser#dot_attr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDot_attr_id(grammerParser.Dot_attr_idContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dot_attr_dependency_type}
	 * labeled alternative in {@link grammerParser#dot_attr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDot_attr_dependency_type(grammerParser.Dot_attr_dependency_typeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dt_sequence}
	 * labeled alternative in {@link grammerParser#dependency_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDt_sequence(grammerParser.Dt_sequenceContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dt_subtask_mandatory}
	 * labeled alternative in {@link grammerParser#dependency_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDt_subtask_mandatory(grammerParser.Dt_subtask_mandatoryContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dt_subtask_optional}
	 * labeled alternative in {@link grammerParser#dependency_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDt_subtask_optional(grammerParser.Dt_subtask_optionalContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dt_conditional_correct}
	 * labeled alternative in {@link grammerParser#dependency_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDt_conditional_correct(grammerParser.Dt_conditional_correctContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dt_conditional_false}
	 * labeled alternative in {@link grammerParser#dependency_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDt_conditional_false(grammerParser.Dt_conditional_falseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dt_sequence_and}
	 * labeled alternative in {@link grammerParser#dependency_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDt_sequence_and(grammerParser.Dt_sequence_andContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dt_sequence_or}
	 * labeled alternative in {@link grammerParser#dependency_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDt_sequence_or(grammerParser.Dt_sequence_orContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#dungeonConfig}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDungeonConfig(grammerParser.DungeonConfigContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#graph}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGraph(grammerParser.GraphContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#taskDependency}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTaskDependency(grammerParser.TaskDependencyContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#dependencyAttribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDependencyAttribute(grammerParser.DependencyAttributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#singleChoiceTask}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleChoiceTask(grammerParser.SingleChoiceTaskContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#multipleChoiceTask}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultipleChoiceTask(grammerParser.MultipleChoiceTaskContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#assignTask}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignTask(grammerParser.AssignTaskContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#field}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitField(grammerParser.FieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#dependencyGraphField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDependencyGraphField(grammerParser.DependencyGraphFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#descriptionField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescriptionField(grammerParser.DescriptionFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#answersField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnswersField(grammerParser.AnswersFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#correctAnswerIndexField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCorrectAnswerIndexField(grammerParser.CorrectAnswerIndexFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#correctAnswerIndicesField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCorrectAnswerIndicesField(grammerParser.CorrectAnswerIndicesFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#solutionField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSolutionField(grammerParser.SolutionFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#pair}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPair(grammerParser.PairContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#pairVal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPairVal(grammerParser.PairValContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#pointsField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPointsField(grammerParser.PointsFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#pointsToPassField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPointsToPassField(grammerParser.PointsToPassFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#explanationField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplanationField(grammerParser.ExplanationFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#gradingFunctionField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGradingFunctionField(grammerParser.GradingFunctionFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#scenarioBuilderField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScenarioBuilderField(grammerParser.ScenarioBuilderFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#entity_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEntity_type(grammerParser.Entity_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#componentList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComponentList(grammerParser.ComponentListContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#component}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComponent(grammerParser.ComponentContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#attributeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttributeList(grammerParser.AttributeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#attribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttribute(grammerParser.AttributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link grammerParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(grammerParser.ValueContext ctx);
}