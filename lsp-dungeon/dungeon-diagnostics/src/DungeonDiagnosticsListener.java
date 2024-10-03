// Generated from C:/Users/bjarn/VS_Projects/Dungeon/dungeon-diagnostics/DungeonDiagnostics.g4 by ANTLR 4.13.1
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
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#fn_def}.
	 * @param ctx the parse tree
	 */
	void enterFn_def(DungeonDiagnosticsParser.Fn_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#fn_def}.
	 * @param ctx the parse tree
	 */
	void exitFn_def(DungeonDiagnosticsParser.Fn_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#stmt}.
	 * @param ctx the parse tree
	 */
	void enterStmt(DungeonDiagnosticsParser.StmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#stmt}.
	 * @param ctx the parse tree
	 */
	void exitStmt(DungeonDiagnosticsParser.StmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code for_loop}
	 * labeled alternative in {@link DungeonDiagnosticsParser#loop_stmt}.
	 * @param ctx the parse tree
	 */
	void enterFor_loop(DungeonDiagnosticsParser.For_loopContext ctx);
	/**
	 * Exit a parse tree produced by the {@code for_loop}
	 * labeled alternative in {@link DungeonDiagnosticsParser#loop_stmt}.
	 * @param ctx the parse tree
	 */
	void exitFor_loop(DungeonDiagnosticsParser.For_loopContext ctx);
	/**
	 * Enter a parse tree produced by the {@code for_loop_counting}
	 * labeled alternative in {@link DungeonDiagnosticsParser#loop_stmt}.
	 * @param ctx the parse tree
	 */
	void enterFor_loop_counting(DungeonDiagnosticsParser.For_loop_countingContext ctx);
	/**
	 * Exit a parse tree produced by the {@code for_loop_counting}
	 * labeled alternative in {@link DungeonDiagnosticsParser#loop_stmt}.
	 * @param ctx the parse tree
	 */
	void exitFor_loop_counting(DungeonDiagnosticsParser.For_loop_countingContext ctx);
	/**
	 * Enter a parse tree produced by the {@code while_loop}
	 * labeled alternative in {@link DungeonDiagnosticsParser#loop_stmt}.
	 * @param ctx the parse tree
	 */
	void enterWhile_loop(DungeonDiagnosticsParser.While_loopContext ctx);
	/**
	 * Exit a parse tree produced by the {@code while_loop}
	 * labeled alternative in {@link DungeonDiagnosticsParser#loop_stmt}.
	 * @param ctx the parse tree
	 */
	void exitWhile_loop(DungeonDiagnosticsParser.While_loopContext ctx);
	/**
	 * Enter a parse tree produced by the {@code var_decl_assignment}
	 * labeled alternative in {@link DungeonDiagnosticsParser#var_decl}.
	 * @param ctx the parse tree
	 */
	void enterVar_decl_assignment(DungeonDiagnosticsParser.Var_decl_assignmentContext ctx);
	/**
	 * Exit a parse tree produced by the {@code var_decl_assignment}
	 * labeled alternative in {@link DungeonDiagnosticsParser#var_decl}.
	 * @param ctx the parse tree
	 */
	void exitVar_decl_assignment(DungeonDiagnosticsParser.Var_decl_assignmentContext ctx);
	/**
	 * Enter a parse tree produced by the {@code var_decl_type_decl}
	 * labeled alternative in {@link DungeonDiagnosticsParser#var_decl}.
	 * @param ctx the parse tree
	 */
	void enterVar_decl_type_decl(DungeonDiagnosticsParser.Var_decl_type_declContext ctx);
	/**
	 * Exit a parse tree produced by the {@code var_decl_type_decl}
	 * labeled alternative in {@link DungeonDiagnosticsParser#var_decl}.
	 * @param ctx the parse tree
	 */
	void exitVar_decl_type_decl(DungeonDiagnosticsParser.Var_decl_type_declContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(DungeonDiagnosticsParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(DungeonDiagnosticsParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code method_call_expression}
	 * labeled alternative in {@link DungeonDiagnosticsParser#expression_rhs}.
	 * @param ctx the parse tree
	 */
	void enterMethod_call_expression(DungeonDiagnosticsParser.Method_call_expressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code method_call_expression}
	 * labeled alternative in {@link DungeonDiagnosticsParser#expression_rhs}.
	 * @param ctx the parse tree
	 */
	void exitMethod_call_expression(DungeonDiagnosticsParser.Method_call_expressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code member_access_expression}
	 * labeled alternative in {@link DungeonDiagnosticsParser#expression_rhs}.
	 * @param ctx the parse tree
	 */
	void enterMember_access_expression(DungeonDiagnosticsParser.Member_access_expressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code member_access_expression}
	 * labeled alternative in {@link DungeonDiagnosticsParser#expression_rhs}.
	 * @param ctx the parse tree
	 */
	void exitMember_access_expression(DungeonDiagnosticsParser.Member_access_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#assignment}.
	 * @param ctx the parse tree
	 */
	void enterAssignment(DungeonDiagnosticsParser.AssignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#assignment}.
	 * @param ctx the parse tree
	 */
	void exitAssignment(DungeonDiagnosticsParser.AssignmentContext ctx);
	/**
	 * Enter a parse tree produced by the {@code assignee_func_call}
	 * labeled alternative in {@link DungeonDiagnosticsParser#assignee}.
	 * @param ctx the parse tree
	 */
	void enterAssignee_func_call(DungeonDiagnosticsParser.Assignee_func_callContext ctx);
	/**
	 * Exit a parse tree produced by the {@code assignee_func_call}
	 * labeled alternative in {@link DungeonDiagnosticsParser#assignee}.
	 * @param ctx the parse tree
	 */
	void exitAssignee_func_call(DungeonDiagnosticsParser.Assignee_func_callContext ctx);
	/**
	 * Enter a parse tree produced by the {@code assignee_qualified_name}
	 * labeled alternative in {@link DungeonDiagnosticsParser#assignee}.
	 * @param ctx the parse tree
	 */
	void enterAssignee_qualified_name(DungeonDiagnosticsParser.Assignee_qualified_nameContext ctx);
	/**
	 * Exit a parse tree produced by the {@code assignee_qualified_name}
	 * labeled alternative in {@link DungeonDiagnosticsParser#assignee}.
	 * @param ctx the parse tree
	 */
	void exitAssignee_qualified_name(DungeonDiagnosticsParser.Assignee_qualified_nameContext ctx);
	/**
	 * Enter a parse tree produced by the {@code assignee_identifier}
	 * labeled alternative in {@link DungeonDiagnosticsParser#assignee}.
	 * @param ctx the parse tree
	 */
	void enterAssignee_identifier(DungeonDiagnosticsParser.Assignee_identifierContext ctx);
	/**
	 * Exit a parse tree produced by the {@code assignee_identifier}
	 * labeled alternative in {@link DungeonDiagnosticsParser#assignee}.
	 * @param ctx the parse tree
	 */
	void exitAssignee_identifier(DungeonDiagnosticsParser.Assignee_identifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#logic_or}.
	 * @param ctx the parse tree
	 */
	void enterLogic_or(DungeonDiagnosticsParser.Logic_orContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#logic_or}.
	 * @param ctx the parse tree
	 */
	void exitLogic_or(DungeonDiagnosticsParser.Logic_orContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#logic_and}.
	 * @param ctx the parse tree
	 */
	void enterLogic_and(DungeonDiagnosticsParser.Logic_andContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#logic_and}.
	 * @param ctx the parse tree
	 */
	void exitLogic_and(DungeonDiagnosticsParser.Logic_andContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#equality}.
	 * @param ctx the parse tree
	 */
	void enterEquality(DungeonDiagnosticsParser.EqualityContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#equality}.
	 * @param ctx the parse tree
	 */
	void exitEquality(DungeonDiagnosticsParser.EqualityContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#comparison}.
	 * @param ctx the parse tree
	 */
	void enterComparison(DungeonDiagnosticsParser.ComparisonContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#comparison}.
	 * @param ctx the parse tree
	 */
	void exitComparison(DungeonDiagnosticsParser.ComparisonContext ctx);
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
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#factor}.
	 * @param ctx the parse tree
	 */
	void enterFactor(DungeonDiagnosticsParser.FactorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#factor}.
	 * @param ctx the parse tree
	 */
	void exitFactor(DungeonDiagnosticsParser.FactorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#unary}.
	 * @param ctx the parse tree
	 */
	void enterUnary(DungeonDiagnosticsParser.UnaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#unary}.
	 * @param ctx the parse tree
	 */
	void exitUnary(DungeonDiagnosticsParser.UnaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#func_call}.
	 * @param ctx the parse tree
	 */
	void enterFunc_call(DungeonDiagnosticsParser.Func_callContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#func_call}.
	 * @param ctx the parse tree
	 */
	void exitFunc_call(DungeonDiagnosticsParser.Func_callContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#stmt_block}.
	 * @param ctx the parse tree
	 */
	void enterStmt_block(DungeonDiagnosticsParser.Stmt_blockContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#stmt_block}.
	 * @param ctx the parse tree
	 */
	void exitStmt_block(DungeonDiagnosticsParser.Stmt_blockContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#stmt_list}.
	 * @param ctx the parse tree
	 */
	void enterStmt_list(DungeonDiagnosticsParser.Stmt_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#stmt_list}.
	 * @param ctx the parse tree
	 */
	void exitStmt_list(DungeonDiagnosticsParser.Stmt_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#return_stmt}.
	 * @param ctx the parse tree
	 */
	void enterReturn_stmt(DungeonDiagnosticsParser.Return_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#return_stmt}.
	 * @param ctx the parse tree
	 */
	void exitReturn_stmt(DungeonDiagnosticsParser.Return_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#conditional_stmt}.
	 * @param ctx the parse tree
	 */
	void enterConditional_stmt(DungeonDiagnosticsParser.Conditional_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#conditional_stmt}.
	 * @param ctx the parse tree
	 */
	void exitConditional_stmt(DungeonDiagnosticsParser.Conditional_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#else_stmt}.
	 * @param ctx the parse tree
	 */
	void enterElse_stmt(DungeonDiagnosticsParser.Else_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#else_stmt}.
	 * @param ctx the parse tree
	 */
	void exitElse_stmt(DungeonDiagnosticsParser.Else_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#ret_type_def}.
	 * @param ctx the parse tree
	 */
	void enterRet_type_def(DungeonDiagnosticsParser.Ret_type_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#ret_type_def}.
	 * @param ctx the parse tree
	 */
	void exitRet_type_def(DungeonDiagnosticsParser.Ret_type_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#param_def}.
	 * @param ctx the parse tree
	 */
	void enterParam_def(DungeonDiagnosticsParser.Param_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#param_def}.
	 * @param ctx the parse tree
	 */
	void exitParam_def(DungeonDiagnosticsParser.Param_defContext ctx);
	/**
	 * Enter a parse tree produced by the {@code map_param_type}
	 * labeled alternative in {@link DungeonDiagnosticsParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void enterMap_param_type(DungeonDiagnosticsParser.Map_param_typeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code map_param_type}
	 * labeled alternative in {@link DungeonDiagnosticsParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void exitMap_param_type(DungeonDiagnosticsParser.Map_param_typeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code task_types}
	 * labeled alternative in {@link DungeonDiagnosticsParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void enterTask_types(DungeonDiagnosticsParser.Task_typesContext ctx);
	/**
	 * Exit a parse tree produced by the {@code task_types}
	 * labeled alternative in {@link DungeonDiagnosticsParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void exitTask_types(DungeonDiagnosticsParser.Task_typesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code id_param_type}
	 * labeled alternative in {@link DungeonDiagnosticsParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void enterId_param_type(DungeonDiagnosticsParser.Id_param_typeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code id_param_type}
	 * labeled alternative in {@link DungeonDiagnosticsParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void exitId_param_type(DungeonDiagnosticsParser.Id_param_typeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code list_param_type}
	 * labeled alternative in {@link DungeonDiagnosticsParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void enterList_param_type(DungeonDiagnosticsParser.List_param_typeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code list_param_type}
	 * labeled alternative in {@link DungeonDiagnosticsParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void exitList_param_type(DungeonDiagnosticsParser.List_param_typeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code set_param_type}
	 * labeled alternative in {@link DungeonDiagnosticsParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void enterSet_param_type(DungeonDiagnosticsParser.Set_param_typeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code set_param_type}
	 * labeled alternative in {@link DungeonDiagnosticsParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void exitSet_param_type(DungeonDiagnosticsParser.Set_param_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#taskTypes}.
	 * @param ctx the parse tree
	 */
	void enterTaskTypes(DungeonDiagnosticsParser.TaskTypesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#taskTypes}.
	 * @param ctx the parse tree
	 */
	void exitTaskTypes(DungeonDiagnosticsParser.TaskTypesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#param_def_list}.
	 * @param ctx the parse tree
	 */
	void enterParam_def_list(DungeonDiagnosticsParser.Param_def_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#param_def_list}.
	 * @param ctx the parse tree
	 */
	void exitParam_def_list(DungeonDiagnosticsParser.Param_def_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#entity_type_def}.
	 * @param ctx the parse tree
	 */
	void enterEntity_type_def(DungeonDiagnosticsParser.Entity_type_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#entity_type_def}.
	 * @param ctx the parse tree
	 */
	void exitEntity_type_def(DungeonDiagnosticsParser.Entity_type_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#item_type_def}.
	 * @param ctx the parse tree
	 */
	void enterItem_type_def(DungeonDiagnosticsParser.Item_type_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#item_type_def}.
	 * @param ctx the parse tree
	 */
	void exitItem_type_def(DungeonDiagnosticsParser.Item_type_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#component_def_list}.
	 * @param ctx the parse tree
	 */
	void enterComponent_def_list(DungeonDiagnosticsParser.Component_def_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#component_def_list}.
	 * @param ctx the parse tree
	 */
	void exitComponent_def_list(DungeonDiagnosticsParser.Component_def_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#aggregate_value_def}.
	 * @param ctx the parse tree
	 */
	void enterAggregate_value_def(DungeonDiagnosticsParser.Aggregate_value_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#aggregate_value_def}.
	 * @param ctx the parse tree
	 */
	void exitAggregate_value_def(DungeonDiagnosticsParser.Aggregate_value_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#object_def}.
	 * @param ctx the parse tree
	 */
	void enterObject_def(DungeonDiagnosticsParser.Object_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#object_def}.
	 * @param ctx the parse tree
	 */
	void exitObject_def(DungeonDiagnosticsParser.Object_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#property_def_list}.
	 * @param ctx the parse tree
	 */
	void enterProperty_def_list(DungeonDiagnosticsParser.Property_def_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#property_def_list}.
	 * @param ctx the parse tree
	 */
	void exitProperty_def_list(DungeonDiagnosticsParser.Property_def_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#property_def}.
	 * @param ctx the parse tree
	 */
	void enterProperty_def(DungeonDiagnosticsParser.Property_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#property_def}.
	 * @param ctx the parse tree
	 */
	void exitProperty_def(DungeonDiagnosticsParser.Property_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#expression_list}.
	 * @param ctx the parse tree
	 */
	void enterExpression_list(DungeonDiagnosticsParser.Expression_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#expression_list}.
	 * @param ctx the parse tree
	 */
	void exitExpression_list(DungeonDiagnosticsParser.Expression_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#grouped_expression}.
	 * @param ctx the parse tree
	 */
	void enterGrouped_expression(DungeonDiagnosticsParser.Grouped_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#grouped_expression}.
	 * @param ctx the parse tree
	 */
	void exitGrouped_expression(DungeonDiagnosticsParser.Grouped_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#list_definition}.
	 * @param ctx the parse tree
	 */
	void enterList_definition(DungeonDiagnosticsParser.List_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#list_definition}.
	 * @param ctx the parse tree
	 */
	void exitList_definition(DungeonDiagnosticsParser.List_definitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#set_definition}.
	 * @param ctx the parse tree
	 */
	void enterSet_definition(DungeonDiagnosticsParser.Set_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#set_definition}.
	 * @param ctx the parse tree
	 */
	void exitSet_definition(DungeonDiagnosticsParser.Set_definitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimary(DungeonDiagnosticsParser.PrimaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimary(DungeonDiagnosticsParser.PrimaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#dot_def}.
	 * @param ctx the parse tree
	 */
	void enterDot_def(DungeonDiagnosticsParser.Dot_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#dot_def}.
	 * @param ctx the parse tree
	 */
	void exitDot_def(DungeonDiagnosticsParser.Dot_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#dot_stmt_list}.
	 * @param ctx the parse tree
	 */
	void enterDot_stmt_list(DungeonDiagnosticsParser.Dot_stmt_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#dot_stmt_list}.
	 * @param ctx the parse tree
	 */
	void exitDot_stmt_list(DungeonDiagnosticsParser.Dot_stmt_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#dot_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDot_stmt(DungeonDiagnosticsParser.Dot_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#dot_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDot_stmt(DungeonDiagnosticsParser.Dot_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#dot_edge_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDot_edge_stmt(DungeonDiagnosticsParser.Dot_edge_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#dot_edge_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDot_edge_stmt(DungeonDiagnosticsParser.Dot_edge_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#dot_node_list}.
	 * @param ctx the parse tree
	 */
	void enterDot_node_list(DungeonDiagnosticsParser.Dot_node_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#dot_node_list}.
	 * @param ctx the parse tree
	 */
	void exitDot_node_list(DungeonDiagnosticsParser.Dot_node_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#dot_edge_RHS}.
	 * @param ctx the parse tree
	 */
	void enterDot_edge_RHS(DungeonDiagnosticsParser.Dot_edge_RHSContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#dot_edge_RHS}.
	 * @param ctx the parse tree
	 */
	void exitDot_edge_RHS(DungeonDiagnosticsParser.Dot_edge_RHSContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#dot_node_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDot_node_stmt(DungeonDiagnosticsParser.Dot_node_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#dot_node_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDot_node_stmt(DungeonDiagnosticsParser.Dot_node_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#dot_attr_list}.
	 * @param ctx the parse tree
	 */
	void enterDot_attr_list(DungeonDiagnosticsParser.Dot_attr_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#dot_attr_list}.
	 * @param ctx the parse tree
	 */
	void exitDot_attr_list(DungeonDiagnosticsParser.Dot_attr_listContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dot_attr_id}
	 * labeled alternative in {@link DungeonDiagnosticsParser#dot_attr}.
	 * @param ctx the parse tree
	 */
	void enterDot_attr_id(DungeonDiagnosticsParser.Dot_attr_idContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dot_attr_id}
	 * labeled alternative in {@link DungeonDiagnosticsParser#dot_attr}.
	 * @param ctx the parse tree
	 */
	void exitDot_attr_id(DungeonDiagnosticsParser.Dot_attr_idContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dot_attr_dependency_type}
	 * labeled alternative in {@link DungeonDiagnosticsParser#dot_attr}.
	 * @param ctx the parse tree
	 */
	void enterDot_attr_dependency_type(DungeonDiagnosticsParser.Dot_attr_dependency_typeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dot_attr_dependency_type}
	 * labeled alternative in {@link DungeonDiagnosticsParser#dot_attr}.
	 * @param ctx the parse tree
	 */
	void exitDot_attr_dependency_type(DungeonDiagnosticsParser.Dot_attr_dependency_typeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dt_sequence}
	 * labeled alternative in {@link DungeonDiagnosticsParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void enterDt_sequence(DungeonDiagnosticsParser.Dt_sequenceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dt_sequence}
	 * labeled alternative in {@link DungeonDiagnosticsParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void exitDt_sequence(DungeonDiagnosticsParser.Dt_sequenceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dt_subtask_mandatory}
	 * labeled alternative in {@link DungeonDiagnosticsParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void enterDt_subtask_mandatory(DungeonDiagnosticsParser.Dt_subtask_mandatoryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dt_subtask_mandatory}
	 * labeled alternative in {@link DungeonDiagnosticsParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void exitDt_subtask_mandatory(DungeonDiagnosticsParser.Dt_subtask_mandatoryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dt_subtask_optional}
	 * labeled alternative in {@link DungeonDiagnosticsParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void enterDt_subtask_optional(DungeonDiagnosticsParser.Dt_subtask_optionalContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dt_subtask_optional}
	 * labeled alternative in {@link DungeonDiagnosticsParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void exitDt_subtask_optional(DungeonDiagnosticsParser.Dt_subtask_optionalContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dt_conditional_correct}
	 * labeled alternative in {@link DungeonDiagnosticsParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void enterDt_conditional_correct(DungeonDiagnosticsParser.Dt_conditional_correctContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dt_conditional_correct}
	 * labeled alternative in {@link DungeonDiagnosticsParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void exitDt_conditional_correct(DungeonDiagnosticsParser.Dt_conditional_correctContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dt_conditional_false}
	 * labeled alternative in {@link DungeonDiagnosticsParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void enterDt_conditional_false(DungeonDiagnosticsParser.Dt_conditional_falseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dt_conditional_false}
	 * labeled alternative in {@link DungeonDiagnosticsParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void exitDt_conditional_false(DungeonDiagnosticsParser.Dt_conditional_falseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dt_sequence_and}
	 * labeled alternative in {@link DungeonDiagnosticsParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void enterDt_sequence_and(DungeonDiagnosticsParser.Dt_sequence_andContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dt_sequence_and}
	 * labeled alternative in {@link DungeonDiagnosticsParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void exitDt_sequence_and(DungeonDiagnosticsParser.Dt_sequence_andContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dt_sequence_or}
	 * labeled alternative in {@link DungeonDiagnosticsParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void enterDt_sequence_or(DungeonDiagnosticsParser.Dt_sequence_orContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dt_sequence_or}
	 * labeled alternative in {@link DungeonDiagnosticsParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void exitDt_sequence_or(DungeonDiagnosticsParser.Dt_sequence_orContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#dungeonConfig}.
	 * @param ctx the parse tree
	 */
	void enterDungeonConfig(DungeonDiagnosticsParser.DungeonConfigContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#dungeonConfig}.
	 * @param ctx the parse tree
	 */
	void exitDungeonConfig(DungeonDiagnosticsParser.DungeonConfigContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#graph}.
	 * @param ctx the parse tree
	 */
	void enterGraph(DungeonDiagnosticsParser.GraphContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#graph}.
	 * @param ctx the parse tree
	 */
	void exitGraph(DungeonDiagnosticsParser.GraphContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#taskDependency}.
	 * @param ctx the parse tree
	 */
	void enterTaskDependency(DungeonDiagnosticsParser.TaskDependencyContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#taskDependency}.
	 * @param ctx the parse tree
	 */
	void exitTaskDependency(DungeonDiagnosticsParser.TaskDependencyContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#dependencyAttribute}.
	 * @param ctx the parse tree
	 */
	void enterDependencyAttribute(DungeonDiagnosticsParser.DependencyAttributeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#dependencyAttribute}.
	 * @param ctx the parse tree
	 */
	void exitDependencyAttribute(DungeonDiagnosticsParser.DependencyAttributeContext ctx);
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
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#dependencyGraphField}.
	 * @param ctx the parse tree
	 */
	void enterDependencyGraphField(DungeonDiagnosticsParser.DependencyGraphFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#dependencyGraphField}.
	 * @param ctx the parse tree
	 */
	void exitDependencyGraphField(DungeonDiagnosticsParser.DependencyGraphFieldContext ctx);
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
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#pairVal}.
	 * @param ctx the parse tree
	 */
	void enterPairVal(DungeonDiagnosticsParser.PairValContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#pairVal}.
	 * @param ctx the parse tree
	 */
	void exitPairVal(DungeonDiagnosticsParser.PairValContext ctx);
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
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#entity_type}.
	 * @param ctx the parse tree
	 */
	void enterEntity_type(DungeonDiagnosticsParser.Entity_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#entity_type}.
	 * @param ctx the parse tree
	 */
	void exitEntity_type(DungeonDiagnosticsParser.Entity_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#componentList}.
	 * @param ctx the parse tree
	 */
	void enterComponentList(DungeonDiagnosticsParser.ComponentListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#componentList}.
	 * @param ctx the parse tree
	 */
	void exitComponentList(DungeonDiagnosticsParser.ComponentListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#component}.
	 * @param ctx the parse tree
	 */
	void enterComponent(DungeonDiagnosticsParser.ComponentContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#component}.
	 * @param ctx the parse tree
	 */
	void exitComponent(DungeonDiagnosticsParser.ComponentContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#attributeList}.
	 * @param ctx the parse tree
	 */
	void enterAttributeList(DungeonDiagnosticsParser.AttributeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#attributeList}.
	 * @param ctx the parse tree
	 */
	void exitAttributeList(DungeonDiagnosticsParser.AttributeListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#attribute}.
	 * @param ctx the parse tree
	 */
	void enterAttribute(DungeonDiagnosticsParser.AttributeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#attribute}.
	 * @param ctx the parse tree
	 */
	void exitAttribute(DungeonDiagnosticsParser.AttributeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(DungeonDiagnosticsParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(DungeonDiagnosticsParser.ValueContext ctx);
}