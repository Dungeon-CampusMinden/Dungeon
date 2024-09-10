// Generated from C:/Users/bjarn/VS_Projects/Dungeon/Dungeon-Diagnostics/grammer.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link grammerParser}.
 */
public interface grammerListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link grammerParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(grammerParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(grammerParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#definition}.
	 * @param ctx the parse tree
	 */
	void enterDefinition(grammerParser.DefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#definition}.
	 * @param ctx the parse tree
	 */
	void exitDefinition(grammerParser.DefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#fn_def}.
	 * @param ctx the parse tree
	 */
	void enterFn_def(grammerParser.Fn_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#fn_def}.
	 * @param ctx the parse tree
	 */
	void exitFn_def(grammerParser.Fn_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#stmt}.
	 * @param ctx the parse tree
	 */
	void enterStmt(grammerParser.StmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#stmt}.
	 * @param ctx the parse tree
	 */
	void exitStmt(grammerParser.StmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code for_loop}
	 * labeled alternative in {@link grammerParser#loop_stmt}.
	 * @param ctx the parse tree
	 */
	void enterFor_loop(grammerParser.For_loopContext ctx);
	/**
	 * Exit a parse tree produced by the {@code for_loop}
	 * labeled alternative in {@link grammerParser#loop_stmt}.
	 * @param ctx the parse tree
	 */
	void exitFor_loop(grammerParser.For_loopContext ctx);
	/**
	 * Enter a parse tree produced by the {@code for_loop_counting}
	 * labeled alternative in {@link grammerParser#loop_stmt}.
	 * @param ctx the parse tree
	 */
	void enterFor_loop_counting(grammerParser.For_loop_countingContext ctx);
	/**
	 * Exit a parse tree produced by the {@code for_loop_counting}
	 * labeled alternative in {@link grammerParser#loop_stmt}.
	 * @param ctx the parse tree
	 */
	void exitFor_loop_counting(grammerParser.For_loop_countingContext ctx);
	/**
	 * Enter a parse tree produced by the {@code while_loop}
	 * labeled alternative in {@link grammerParser#loop_stmt}.
	 * @param ctx the parse tree
	 */
	void enterWhile_loop(grammerParser.While_loopContext ctx);
	/**
	 * Exit a parse tree produced by the {@code while_loop}
	 * labeled alternative in {@link grammerParser#loop_stmt}.
	 * @param ctx the parse tree
	 */
	void exitWhile_loop(grammerParser.While_loopContext ctx);
	/**
	 * Enter a parse tree produced by the {@code var_decl_assignment}
	 * labeled alternative in {@link grammerParser#var_decl}.
	 * @param ctx the parse tree
	 */
	void enterVar_decl_assignment(grammerParser.Var_decl_assignmentContext ctx);
	/**
	 * Exit a parse tree produced by the {@code var_decl_assignment}
	 * labeled alternative in {@link grammerParser#var_decl}.
	 * @param ctx the parse tree
	 */
	void exitVar_decl_assignment(grammerParser.Var_decl_assignmentContext ctx);
	/**
	 * Enter a parse tree produced by the {@code var_decl_type_decl}
	 * labeled alternative in {@link grammerParser#var_decl}.
	 * @param ctx the parse tree
	 */
	void enterVar_decl_type_decl(grammerParser.Var_decl_type_declContext ctx);
	/**
	 * Exit a parse tree produced by the {@code var_decl_type_decl}
	 * labeled alternative in {@link grammerParser#var_decl}.
	 * @param ctx the parse tree
	 */
	void exitVar_decl_type_decl(grammerParser.Var_decl_type_declContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(grammerParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(grammerParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code method_call_expression}
	 * labeled alternative in {@link grammerParser#expression_rhs}.
	 * @param ctx the parse tree
	 */
	void enterMethod_call_expression(grammerParser.Method_call_expressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code method_call_expression}
	 * labeled alternative in {@link grammerParser#expression_rhs}.
	 * @param ctx the parse tree
	 */
	void exitMethod_call_expression(grammerParser.Method_call_expressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code member_access_expression}
	 * labeled alternative in {@link grammerParser#expression_rhs}.
	 * @param ctx the parse tree
	 */
	void enterMember_access_expression(grammerParser.Member_access_expressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code member_access_expression}
	 * labeled alternative in {@link grammerParser#expression_rhs}.
	 * @param ctx the parse tree
	 */
	void exitMember_access_expression(grammerParser.Member_access_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#assignment}.
	 * @param ctx the parse tree
	 */
	void enterAssignment(grammerParser.AssignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#assignment}.
	 * @param ctx the parse tree
	 */
	void exitAssignment(grammerParser.AssignmentContext ctx);
	/**
	 * Enter a parse tree produced by the {@code assignee_func_call}
	 * labeled alternative in {@link grammerParser#assignee}.
	 * @param ctx the parse tree
	 */
	void enterAssignee_func_call(grammerParser.Assignee_func_callContext ctx);
	/**
	 * Exit a parse tree produced by the {@code assignee_func_call}
	 * labeled alternative in {@link grammerParser#assignee}.
	 * @param ctx the parse tree
	 */
	void exitAssignee_func_call(grammerParser.Assignee_func_callContext ctx);
	/**
	 * Enter a parse tree produced by the {@code assignee_qualified_name}
	 * labeled alternative in {@link grammerParser#assignee}.
	 * @param ctx the parse tree
	 */
	void enterAssignee_qualified_name(grammerParser.Assignee_qualified_nameContext ctx);
	/**
	 * Exit a parse tree produced by the {@code assignee_qualified_name}
	 * labeled alternative in {@link grammerParser#assignee}.
	 * @param ctx the parse tree
	 */
	void exitAssignee_qualified_name(grammerParser.Assignee_qualified_nameContext ctx);
	/**
	 * Enter a parse tree produced by the {@code assignee_identifier}
	 * labeled alternative in {@link grammerParser#assignee}.
	 * @param ctx the parse tree
	 */
	void enterAssignee_identifier(grammerParser.Assignee_identifierContext ctx);
	/**
	 * Exit a parse tree produced by the {@code assignee_identifier}
	 * labeled alternative in {@link grammerParser#assignee}.
	 * @param ctx the parse tree
	 */
	void exitAssignee_identifier(grammerParser.Assignee_identifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#logic_or}.
	 * @param ctx the parse tree
	 */
	void enterLogic_or(grammerParser.Logic_orContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#logic_or}.
	 * @param ctx the parse tree
	 */
	void exitLogic_or(grammerParser.Logic_orContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#logic_and}.
	 * @param ctx the parse tree
	 */
	void enterLogic_and(grammerParser.Logic_andContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#logic_and}.
	 * @param ctx the parse tree
	 */
	void exitLogic_and(grammerParser.Logic_andContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#equality}.
	 * @param ctx the parse tree
	 */
	void enterEquality(grammerParser.EqualityContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#equality}.
	 * @param ctx the parse tree
	 */
	void exitEquality(grammerParser.EqualityContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#comparison}.
	 * @param ctx the parse tree
	 */
	void enterComparison(grammerParser.ComparisonContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#comparison}.
	 * @param ctx the parse tree
	 */
	void exitComparison(grammerParser.ComparisonContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#term}.
	 * @param ctx the parse tree
	 */
	void enterTerm(grammerParser.TermContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#term}.
	 * @param ctx the parse tree
	 */
	void exitTerm(grammerParser.TermContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#factor}.
	 * @param ctx the parse tree
	 */
	void enterFactor(grammerParser.FactorContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#factor}.
	 * @param ctx the parse tree
	 */
	void exitFactor(grammerParser.FactorContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#unary}.
	 * @param ctx the parse tree
	 */
	void enterUnary(grammerParser.UnaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#unary}.
	 * @param ctx the parse tree
	 */
	void exitUnary(grammerParser.UnaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#func_call}.
	 * @param ctx the parse tree
	 */
	void enterFunc_call(grammerParser.Func_callContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#func_call}.
	 * @param ctx the parse tree
	 */
	void exitFunc_call(grammerParser.Func_callContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#stmt_block}.
	 * @param ctx the parse tree
	 */
	void enterStmt_block(grammerParser.Stmt_blockContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#stmt_block}.
	 * @param ctx the parse tree
	 */
	void exitStmt_block(grammerParser.Stmt_blockContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#stmt_list}.
	 * @param ctx the parse tree
	 */
	void enterStmt_list(grammerParser.Stmt_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#stmt_list}.
	 * @param ctx the parse tree
	 */
	void exitStmt_list(grammerParser.Stmt_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#return_stmt}.
	 * @param ctx the parse tree
	 */
	void enterReturn_stmt(grammerParser.Return_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#return_stmt}.
	 * @param ctx the parse tree
	 */
	void exitReturn_stmt(grammerParser.Return_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#conditional_stmt}.
	 * @param ctx the parse tree
	 */
	void enterConditional_stmt(grammerParser.Conditional_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#conditional_stmt}.
	 * @param ctx the parse tree
	 */
	void exitConditional_stmt(grammerParser.Conditional_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#else_stmt}.
	 * @param ctx the parse tree
	 */
	void enterElse_stmt(grammerParser.Else_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#else_stmt}.
	 * @param ctx the parse tree
	 */
	void exitElse_stmt(grammerParser.Else_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#ret_type_def}.
	 * @param ctx the parse tree
	 */
	void enterRet_type_def(grammerParser.Ret_type_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#ret_type_def}.
	 * @param ctx the parse tree
	 */
	void exitRet_type_def(grammerParser.Ret_type_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#param_def}.
	 * @param ctx the parse tree
	 */
	void enterParam_def(grammerParser.Param_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#param_def}.
	 * @param ctx the parse tree
	 */
	void exitParam_def(grammerParser.Param_defContext ctx);
	/**
	 * Enter a parse tree produced by the {@code map_param_type}
	 * labeled alternative in {@link grammerParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void enterMap_param_type(grammerParser.Map_param_typeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code map_param_type}
	 * labeled alternative in {@link grammerParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void exitMap_param_type(grammerParser.Map_param_typeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code task_types}
	 * labeled alternative in {@link grammerParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void enterTask_types(grammerParser.Task_typesContext ctx);
	/**
	 * Exit a parse tree produced by the {@code task_types}
	 * labeled alternative in {@link grammerParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void exitTask_types(grammerParser.Task_typesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code id_param_type}
	 * labeled alternative in {@link grammerParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void enterId_param_type(grammerParser.Id_param_typeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code id_param_type}
	 * labeled alternative in {@link grammerParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void exitId_param_type(grammerParser.Id_param_typeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code list_param_type}
	 * labeled alternative in {@link grammerParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void enterList_param_type(grammerParser.List_param_typeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code list_param_type}
	 * labeled alternative in {@link grammerParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void exitList_param_type(grammerParser.List_param_typeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code set_param_type}
	 * labeled alternative in {@link grammerParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void enterSet_param_type(grammerParser.Set_param_typeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code set_param_type}
	 * labeled alternative in {@link grammerParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void exitSet_param_type(grammerParser.Set_param_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#taskTypes}.
	 * @param ctx the parse tree
	 */
	void enterTaskTypes(grammerParser.TaskTypesContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#taskTypes}.
	 * @param ctx the parse tree
	 */
	void exitTaskTypes(grammerParser.TaskTypesContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#param_def_list}.
	 * @param ctx the parse tree
	 */
	void enterParam_def_list(grammerParser.Param_def_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#param_def_list}.
	 * @param ctx the parse tree
	 */
	void exitParam_def_list(grammerParser.Param_def_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#entity_type_def}.
	 * @param ctx the parse tree
	 */
	void enterEntity_type_def(grammerParser.Entity_type_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#entity_type_def}.
	 * @param ctx the parse tree
	 */
	void exitEntity_type_def(grammerParser.Entity_type_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#item_type_def}.
	 * @param ctx the parse tree
	 */
	void enterItem_type_def(grammerParser.Item_type_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#item_type_def}.
	 * @param ctx the parse tree
	 */
	void exitItem_type_def(grammerParser.Item_type_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#component_def_list}.
	 * @param ctx the parse tree
	 */
	void enterComponent_def_list(grammerParser.Component_def_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#component_def_list}.
	 * @param ctx the parse tree
	 */
	void exitComponent_def_list(grammerParser.Component_def_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#aggregate_value_def}.
	 * @param ctx the parse tree
	 */
	void enterAggregate_value_def(grammerParser.Aggregate_value_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#aggregate_value_def}.
	 * @param ctx the parse tree
	 */
	void exitAggregate_value_def(grammerParser.Aggregate_value_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#object_def}.
	 * @param ctx the parse tree
	 */
	void enterObject_def(grammerParser.Object_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#object_def}.
	 * @param ctx the parse tree
	 */
	void exitObject_def(grammerParser.Object_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#property_def_list}.
	 * @param ctx the parse tree
	 */
	void enterProperty_def_list(grammerParser.Property_def_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#property_def_list}.
	 * @param ctx the parse tree
	 */
	void exitProperty_def_list(grammerParser.Property_def_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#property_def}.
	 * @param ctx the parse tree
	 */
	void enterProperty_def(grammerParser.Property_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#property_def}.
	 * @param ctx the parse tree
	 */
	void exitProperty_def(grammerParser.Property_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#expression_list}.
	 * @param ctx the parse tree
	 */
	void enterExpression_list(grammerParser.Expression_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#expression_list}.
	 * @param ctx the parse tree
	 */
	void exitExpression_list(grammerParser.Expression_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#grouped_expression}.
	 * @param ctx the parse tree
	 */
	void enterGrouped_expression(grammerParser.Grouped_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#grouped_expression}.
	 * @param ctx the parse tree
	 */
	void exitGrouped_expression(grammerParser.Grouped_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#list_definition}.
	 * @param ctx the parse tree
	 */
	void enterList_definition(grammerParser.List_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#list_definition}.
	 * @param ctx the parse tree
	 */
	void exitList_definition(grammerParser.List_definitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#set_definition}.
	 * @param ctx the parse tree
	 */
	void enterSet_definition(grammerParser.Set_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#set_definition}.
	 * @param ctx the parse tree
	 */
	void exitSet_definition(grammerParser.Set_definitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimary(grammerParser.PrimaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimary(grammerParser.PrimaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#dot_def}.
	 * @param ctx the parse tree
	 */
	void enterDot_def(grammerParser.Dot_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#dot_def}.
	 * @param ctx the parse tree
	 */
	void exitDot_def(grammerParser.Dot_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#dot_stmt_list}.
	 * @param ctx the parse tree
	 */
	void enterDot_stmt_list(grammerParser.Dot_stmt_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#dot_stmt_list}.
	 * @param ctx the parse tree
	 */
	void exitDot_stmt_list(grammerParser.Dot_stmt_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#dot_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDot_stmt(grammerParser.Dot_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#dot_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDot_stmt(grammerParser.Dot_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#dot_edge_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDot_edge_stmt(grammerParser.Dot_edge_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#dot_edge_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDot_edge_stmt(grammerParser.Dot_edge_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#dot_node_list}.
	 * @param ctx the parse tree
	 */
	void enterDot_node_list(grammerParser.Dot_node_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#dot_node_list}.
	 * @param ctx the parse tree
	 */
	void exitDot_node_list(grammerParser.Dot_node_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#dot_edge_RHS}.
	 * @param ctx the parse tree
	 */
	void enterDot_edge_RHS(grammerParser.Dot_edge_RHSContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#dot_edge_RHS}.
	 * @param ctx the parse tree
	 */
	void exitDot_edge_RHS(grammerParser.Dot_edge_RHSContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#dot_node_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDot_node_stmt(grammerParser.Dot_node_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#dot_node_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDot_node_stmt(grammerParser.Dot_node_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#dot_attr_list}.
	 * @param ctx the parse tree
	 */
	void enterDot_attr_list(grammerParser.Dot_attr_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#dot_attr_list}.
	 * @param ctx the parse tree
	 */
	void exitDot_attr_list(grammerParser.Dot_attr_listContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dot_attr_id}
	 * labeled alternative in {@link grammerParser#dot_attr}.
	 * @param ctx the parse tree
	 */
	void enterDot_attr_id(grammerParser.Dot_attr_idContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dot_attr_id}
	 * labeled alternative in {@link grammerParser#dot_attr}.
	 * @param ctx the parse tree
	 */
	void exitDot_attr_id(grammerParser.Dot_attr_idContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dot_attr_dependency_type}
	 * labeled alternative in {@link grammerParser#dot_attr}.
	 * @param ctx the parse tree
	 */
	void enterDot_attr_dependency_type(grammerParser.Dot_attr_dependency_typeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dot_attr_dependency_type}
	 * labeled alternative in {@link grammerParser#dot_attr}.
	 * @param ctx the parse tree
	 */
	void exitDot_attr_dependency_type(grammerParser.Dot_attr_dependency_typeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dt_sequence}
	 * labeled alternative in {@link grammerParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void enterDt_sequence(grammerParser.Dt_sequenceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dt_sequence}
	 * labeled alternative in {@link grammerParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void exitDt_sequence(grammerParser.Dt_sequenceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dt_subtask_mandatory}
	 * labeled alternative in {@link grammerParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void enterDt_subtask_mandatory(grammerParser.Dt_subtask_mandatoryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dt_subtask_mandatory}
	 * labeled alternative in {@link grammerParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void exitDt_subtask_mandatory(grammerParser.Dt_subtask_mandatoryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dt_subtask_optional}
	 * labeled alternative in {@link grammerParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void enterDt_subtask_optional(grammerParser.Dt_subtask_optionalContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dt_subtask_optional}
	 * labeled alternative in {@link grammerParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void exitDt_subtask_optional(grammerParser.Dt_subtask_optionalContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dt_conditional_correct}
	 * labeled alternative in {@link grammerParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void enterDt_conditional_correct(grammerParser.Dt_conditional_correctContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dt_conditional_correct}
	 * labeled alternative in {@link grammerParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void exitDt_conditional_correct(grammerParser.Dt_conditional_correctContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dt_conditional_false}
	 * labeled alternative in {@link grammerParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void enterDt_conditional_false(grammerParser.Dt_conditional_falseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dt_conditional_false}
	 * labeled alternative in {@link grammerParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void exitDt_conditional_false(grammerParser.Dt_conditional_falseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dt_sequence_and}
	 * labeled alternative in {@link grammerParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void enterDt_sequence_and(grammerParser.Dt_sequence_andContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dt_sequence_and}
	 * labeled alternative in {@link grammerParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void exitDt_sequence_and(grammerParser.Dt_sequence_andContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dt_sequence_or}
	 * labeled alternative in {@link grammerParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void enterDt_sequence_or(grammerParser.Dt_sequence_orContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dt_sequence_or}
	 * labeled alternative in {@link grammerParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void exitDt_sequence_or(grammerParser.Dt_sequence_orContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#dungeonConfig}.
	 * @param ctx the parse tree
	 */
	void enterDungeonConfig(grammerParser.DungeonConfigContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#dungeonConfig}.
	 * @param ctx the parse tree
	 */
	void exitDungeonConfig(grammerParser.DungeonConfigContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#graph}.
	 * @param ctx the parse tree
	 */
	void enterGraph(grammerParser.GraphContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#graph}.
	 * @param ctx the parse tree
	 */
	void exitGraph(grammerParser.GraphContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#taskDependency}.
	 * @param ctx the parse tree
	 */
	void enterTaskDependency(grammerParser.TaskDependencyContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#taskDependency}.
	 * @param ctx the parse tree
	 */
	void exitTaskDependency(grammerParser.TaskDependencyContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#dependencyAttribute}.
	 * @param ctx the parse tree
	 */
	void enterDependencyAttribute(grammerParser.DependencyAttributeContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#dependencyAttribute}.
	 * @param ctx the parse tree
	 */
	void exitDependencyAttribute(grammerParser.DependencyAttributeContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#singleChoiceTask}.
	 * @param ctx the parse tree
	 */
	void enterSingleChoiceTask(grammerParser.SingleChoiceTaskContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#singleChoiceTask}.
	 * @param ctx the parse tree
	 */
	void exitSingleChoiceTask(grammerParser.SingleChoiceTaskContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#multipleChoiceTask}.
	 * @param ctx the parse tree
	 */
	void enterMultipleChoiceTask(grammerParser.MultipleChoiceTaskContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#multipleChoiceTask}.
	 * @param ctx the parse tree
	 */
	void exitMultipleChoiceTask(grammerParser.MultipleChoiceTaskContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#assignTask}.
	 * @param ctx the parse tree
	 */
	void enterAssignTask(grammerParser.AssignTaskContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#assignTask}.
	 * @param ctx the parse tree
	 */
	void exitAssignTask(grammerParser.AssignTaskContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#field}.
	 * @param ctx the parse tree
	 */
	void enterField(grammerParser.FieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#field}.
	 * @param ctx the parse tree
	 */
	void exitField(grammerParser.FieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#dependencyGraphField}.
	 * @param ctx the parse tree
	 */
	void enterDependencyGraphField(grammerParser.DependencyGraphFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#dependencyGraphField}.
	 * @param ctx the parse tree
	 */
	void exitDependencyGraphField(grammerParser.DependencyGraphFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#descriptionField}.
	 * @param ctx the parse tree
	 */
	void enterDescriptionField(grammerParser.DescriptionFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#descriptionField}.
	 * @param ctx the parse tree
	 */
	void exitDescriptionField(grammerParser.DescriptionFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#answersField}.
	 * @param ctx the parse tree
	 */
	void enterAnswersField(grammerParser.AnswersFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#answersField}.
	 * @param ctx the parse tree
	 */
	void exitAnswersField(grammerParser.AnswersFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#correctAnswerIndexField}.
	 * @param ctx the parse tree
	 */
	void enterCorrectAnswerIndexField(grammerParser.CorrectAnswerIndexFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#correctAnswerIndexField}.
	 * @param ctx the parse tree
	 */
	void exitCorrectAnswerIndexField(grammerParser.CorrectAnswerIndexFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#correctAnswerIndicesField}.
	 * @param ctx the parse tree
	 */
	void enterCorrectAnswerIndicesField(grammerParser.CorrectAnswerIndicesFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#correctAnswerIndicesField}.
	 * @param ctx the parse tree
	 */
	void exitCorrectAnswerIndicesField(grammerParser.CorrectAnswerIndicesFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#solutionField}.
	 * @param ctx the parse tree
	 */
	void enterSolutionField(grammerParser.SolutionFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#solutionField}.
	 * @param ctx the parse tree
	 */
	void exitSolutionField(grammerParser.SolutionFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#pair}.
	 * @param ctx the parse tree
	 */
	void enterPair(grammerParser.PairContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#pair}.
	 * @param ctx the parse tree
	 */
	void exitPair(grammerParser.PairContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#pairVal}.
	 * @param ctx the parse tree
	 */
	void enterPairVal(grammerParser.PairValContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#pairVal}.
	 * @param ctx the parse tree
	 */
	void exitPairVal(grammerParser.PairValContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#pointsField}.
	 * @param ctx the parse tree
	 */
	void enterPointsField(grammerParser.PointsFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#pointsField}.
	 * @param ctx the parse tree
	 */
	void exitPointsField(grammerParser.PointsFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#pointsToPassField}.
	 * @param ctx the parse tree
	 */
	void enterPointsToPassField(grammerParser.PointsToPassFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#pointsToPassField}.
	 * @param ctx the parse tree
	 */
	void exitPointsToPassField(grammerParser.PointsToPassFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#explanationField}.
	 * @param ctx the parse tree
	 */
	void enterExplanationField(grammerParser.ExplanationFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#explanationField}.
	 * @param ctx the parse tree
	 */
	void exitExplanationField(grammerParser.ExplanationFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#gradingFunctionField}.
	 * @param ctx the parse tree
	 */
	void enterGradingFunctionField(grammerParser.GradingFunctionFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#gradingFunctionField}.
	 * @param ctx the parse tree
	 */
	void exitGradingFunctionField(grammerParser.GradingFunctionFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#scenarioBuilderField}.
	 * @param ctx the parse tree
	 */
	void enterScenarioBuilderField(grammerParser.ScenarioBuilderFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#scenarioBuilderField}.
	 * @param ctx the parse tree
	 */
	void exitScenarioBuilderField(grammerParser.ScenarioBuilderFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#entity_type}.
	 * @param ctx the parse tree
	 */
	void enterEntity_type(grammerParser.Entity_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#entity_type}.
	 * @param ctx the parse tree
	 */
	void exitEntity_type(grammerParser.Entity_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#componentList}.
	 * @param ctx the parse tree
	 */
	void enterComponentList(grammerParser.ComponentListContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#componentList}.
	 * @param ctx the parse tree
	 */
	void exitComponentList(grammerParser.ComponentListContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#component}.
	 * @param ctx the parse tree
	 */
	void enterComponent(grammerParser.ComponentContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#component}.
	 * @param ctx the parse tree
	 */
	void exitComponent(grammerParser.ComponentContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#attributeList}.
	 * @param ctx the parse tree
	 */
	void enterAttributeList(grammerParser.AttributeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#attributeList}.
	 * @param ctx the parse tree
	 */
	void exitAttributeList(grammerParser.AttributeListContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#attribute}.
	 * @param ctx the parse tree
	 */
	void enterAttribute(grammerParser.AttributeContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#attribute}.
	 * @param ctx the parse tree
	 */
	void exitAttribute(grammerParser.AttributeContext ctx);
	/**
	 * Enter a parse tree produced by {@link grammerParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(grammerParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(grammerParser.ValueContext ctx);
}