// Generated from c:/Users/bjarn/VS_Projects/Dungeon/dungeon/src/dsl/antlr/DungeonDSL.g4 by ANTLR 4.13.1

    package antlr.main;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link DungeonDSLParser}.
 */
public interface DungeonDSLListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(DungeonDSLParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(DungeonDSLParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#definition}.
	 * @param ctx the parse tree
	 */
	void enterDefinition(DungeonDSLParser.DefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#definition}.
	 * @param ctx the parse tree
	 */
	void exitDefinition(DungeonDSLParser.DefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#fn_def}.
	 * @param ctx the parse tree
	 */
	void enterFn_def(DungeonDSLParser.Fn_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#fn_def}.
	 * @param ctx the parse tree
	 */
	void exitFn_def(DungeonDSLParser.Fn_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#stmt}.
	 * @param ctx the parse tree
	 */
	void enterStmt(DungeonDSLParser.StmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#stmt}.
	 * @param ctx the parse tree
	 */
	void exitStmt(DungeonDSLParser.StmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code for_loop}
	 * labeled alternative in {@link DungeonDSLParser#loop_stmt}.
	 * @param ctx the parse tree
	 */
	void enterFor_loop(DungeonDSLParser.For_loopContext ctx);
	/**
	 * Exit a parse tree produced by the {@code for_loop}
	 * labeled alternative in {@link DungeonDSLParser#loop_stmt}.
	 * @param ctx the parse tree
	 */
	void exitFor_loop(DungeonDSLParser.For_loopContext ctx);
	/**
	 * Enter a parse tree produced by the {@code for_loop_counting}
	 * labeled alternative in {@link DungeonDSLParser#loop_stmt}.
	 * @param ctx the parse tree
	 */
	void enterFor_loop_counting(DungeonDSLParser.For_loop_countingContext ctx);
	/**
	 * Exit a parse tree produced by the {@code for_loop_counting}
	 * labeled alternative in {@link DungeonDSLParser#loop_stmt}.
	 * @param ctx the parse tree
	 */
	void exitFor_loop_counting(DungeonDSLParser.For_loop_countingContext ctx);
	/**
	 * Enter a parse tree produced by the {@code while_loop}
	 * labeled alternative in {@link DungeonDSLParser#loop_stmt}.
	 * @param ctx the parse tree
	 */
	void enterWhile_loop(DungeonDSLParser.While_loopContext ctx);
	/**
	 * Exit a parse tree produced by the {@code while_loop}
	 * labeled alternative in {@link DungeonDSLParser#loop_stmt}.
	 * @param ctx the parse tree
	 */
	void exitWhile_loop(DungeonDSLParser.While_loopContext ctx);
	/**
	 * Enter a parse tree produced by the {@code var_decl_assignment}
	 * labeled alternative in {@link DungeonDSLParser#var_decl}.
	 * @param ctx the parse tree
	 */
	void enterVar_decl_assignment(DungeonDSLParser.Var_decl_assignmentContext ctx);
	/**
	 * Exit a parse tree produced by the {@code var_decl_assignment}
	 * labeled alternative in {@link DungeonDSLParser#var_decl}.
	 * @param ctx the parse tree
	 */
	void exitVar_decl_assignment(DungeonDSLParser.Var_decl_assignmentContext ctx);
	/**
	 * Enter a parse tree produced by the {@code var_decl_type_decl}
	 * labeled alternative in {@link DungeonDSLParser#var_decl}.
	 * @param ctx the parse tree
	 */
	void enterVar_decl_type_decl(DungeonDSLParser.Var_decl_type_declContext ctx);
	/**
	 * Exit a parse tree produced by the {@code var_decl_type_decl}
	 * labeled alternative in {@link DungeonDSLParser#var_decl}.
	 * @param ctx the parse tree
	 */
	void exitVar_decl_type_decl(DungeonDSLParser.Var_decl_type_declContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(DungeonDSLParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(DungeonDSLParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code method_call_expression}
	 * labeled alternative in {@link DungeonDSLParser#expression_rhs}.
	 * @param ctx the parse tree
	 */
	void enterMethod_call_expression(DungeonDSLParser.Method_call_expressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code method_call_expression}
	 * labeled alternative in {@link DungeonDSLParser#expression_rhs}.
	 * @param ctx the parse tree
	 */
	void exitMethod_call_expression(DungeonDSLParser.Method_call_expressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code member_access_expression}
	 * labeled alternative in {@link DungeonDSLParser#expression_rhs}.
	 * @param ctx the parse tree
	 */
	void enterMember_access_expression(DungeonDSLParser.Member_access_expressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code member_access_expression}
	 * labeled alternative in {@link DungeonDSLParser#expression_rhs}.
	 * @param ctx the parse tree
	 */
	void exitMember_access_expression(DungeonDSLParser.Member_access_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#assignment}.
	 * @param ctx the parse tree
	 */
	void enterAssignment(DungeonDSLParser.AssignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#assignment}.
	 * @param ctx the parse tree
	 */
	void exitAssignment(DungeonDSLParser.AssignmentContext ctx);
	/**
	 * Enter a parse tree produced by the {@code assignee_func_call}
	 * labeled alternative in {@link DungeonDSLParser#assignee}.
	 * @param ctx the parse tree
	 */
	void enterAssignee_func_call(DungeonDSLParser.Assignee_func_callContext ctx);
	/**
	 * Exit a parse tree produced by the {@code assignee_func_call}
	 * labeled alternative in {@link DungeonDSLParser#assignee}.
	 * @param ctx the parse tree
	 */
	void exitAssignee_func_call(DungeonDSLParser.Assignee_func_callContext ctx);
	/**
	 * Enter a parse tree produced by the {@code assignee_qualified_name}
	 * labeled alternative in {@link DungeonDSLParser#assignee}.
	 * @param ctx the parse tree
	 */
	void enterAssignee_qualified_name(DungeonDSLParser.Assignee_qualified_nameContext ctx);
	/**
	 * Exit a parse tree produced by the {@code assignee_qualified_name}
	 * labeled alternative in {@link DungeonDSLParser#assignee}.
	 * @param ctx the parse tree
	 */
	void exitAssignee_qualified_name(DungeonDSLParser.Assignee_qualified_nameContext ctx);
	/**
	 * Enter a parse tree produced by the {@code assignee_identifier}
	 * labeled alternative in {@link DungeonDSLParser#assignee}.
	 * @param ctx the parse tree
	 */
	void enterAssignee_identifier(DungeonDSLParser.Assignee_identifierContext ctx);
	/**
	 * Exit a parse tree produced by the {@code assignee_identifier}
	 * labeled alternative in {@link DungeonDSLParser#assignee}.
	 * @param ctx the parse tree
	 */
	void exitAssignee_identifier(DungeonDSLParser.Assignee_identifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#logic_or}.
	 * @param ctx the parse tree
	 */
	void enterLogic_or(DungeonDSLParser.Logic_orContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#logic_or}.
	 * @param ctx the parse tree
	 */
	void exitLogic_or(DungeonDSLParser.Logic_orContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#logic_and}.
	 * @param ctx the parse tree
	 */
	void enterLogic_and(DungeonDSLParser.Logic_andContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#logic_and}.
	 * @param ctx the parse tree
	 */
	void exitLogic_and(DungeonDSLParser.Logic_andContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#equality}.
	 * @param ctx the parse tree
	 */
	void enterEquality(DungeonDSLParser.EqualityContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#equality}.
	 * @param ctx the parse tree
	 */
	void exitEquality(DungeonDSLParser.EqualityContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#comparison}.
	 * @param ctx the parse tree
	 */
	void enterComparison(DungeonDSLParser.ComparisonContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#comparison}.
	 * @param ctx the parse tree
	 */
	void exitComparison(DungeonDSLParser.ComparisonContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#term}.
	 * @param ctx the parse tree
	 */
	void enterTerm(DungeonDSLParser.TermContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#term}.
	 * @param ctx the parse tree
	 */
	void exitTerm(DungeonDSLParser.TermContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#factor}.
	 * @param ctx the parse tree
	 */
	void enterFactor(DungeonDSLParser.FactorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#factor}.
	 * @param ctx the parse tree
	 */
	void exitFactor(DungeonDSLParser.FactorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#unary}.
	 * @param ctx the parse tree
	 */
	void enterUnary(DungeonDSLParser.UnaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#unary}.
	 * @param ctx the parse tree
	 */
	void exitUnary(DungeonDSLParser.UnaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#func_call}.
	 * @param ctx the parse tree
	 */
	void enterFunc_call(DungeonDSLParser.Func_callContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#func_call}.
	 * @param ctx the parse tree
	 */
	void exitFunc_call(DungeonDSLParser.Func_callContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#stmt_block}.
	 * @param ctx the parse tree
	 */
	void enterStmt_block(DungeonDSLParser.Stmt_blockContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#stmt_block}.
	 * @param ctx the parse tree
	 */
	void exitStmt_block(DungeonDSLParser.Stmt_blockContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#stmt_list}.
	 * @param ctx the parse tree
	 */
	void enterStmt_list(DungeonDSLParser.Stmt_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#stmt_list}.
	 * @param ctx the parse tree
	 */
	void exitStmt_list(DungeonDSLParser.Stmt_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#return_stmt}.
	 * @param ctx the parse tree
	 */
	void enterReturn_stmt(DungeonDSLParser.Return_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#return_stmt}.
	 * @param ctx the parse tree
	 */
	void exitReturn_stmt(DungeonDSLParser.Return_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#conditional_stmt}.
	 * @param ctx the parse tree
	 */
	void enterConditional_stmt(DungeonDSLParser.Conditional_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#conditional_stmt}.
	 * @param ctx the parse tree
	 */
	void exitConditional_stmt(DungeonDSLParser.Conditional_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#else_stmt}.
	 * @param ctx the parse tree
	 */
	void enterElse_stmt(DungeonDSLParser.Else_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#else_stmt}.
	 * @param ctx the parse tree
	 */
	void exitElse_stmt(DungeonDSLParser.Else_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#ret_type_def}.
	 * @param ctx the parse tree
	 */
	void enterRet_type_def(DungeonDSLParser.Ret_type_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#ret_type_def}.
	 * @param ctx the parse tree
	 */
	void exitRet_type_def(DungeonDSLParser.Ret_type_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#param_def}.
	 * @param ctx the parse tree
	 */
	void enterParam_def(DungeonDSLParser.Param_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#param_def}.
	 * @param ctx the parse tree
	 */
	void exitParam_def(DungeonDSLParser.Param_defContext ctx);
	/**
	 * Enter a parse tree produced by the {@code map_param_type}
	 * labeled alternative in {@link DungeonDSLParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void enterMap_param_type(DungeonDSLParser.Map_param_typeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code map_param_type}
	 * labeled alternative in {@link DungeonDSLParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void exitMap_param_type(DungeonDSLParser.Map_param_typeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code id_param_type}
	 * labeled alternative in {@link DungeonDSLParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void enterId_param_type(DungeonDSLParser.Id_param_typeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code id_param_type}
	 * labeled alternative in {@link DungeonDSLParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void exitId_param_type(DungeonDSLParser.Id_param_typeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code list_param_type}
	 * labeled alternative in {@link DungeonDSLParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void enterList_param_type(DungeonDSLParser.List_param_typeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code list_param_type}
	 * labeled alternative in {@link DungeonDSLParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void exitList_param_type(DungeonDSLParser.List_param_typeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code set_param_type}
	 * labeled alternative in {@link DungeonDSLParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void enterSet_param_type(DungeonDSLParser.Set_param_typeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code set_param_type}
	 * labeled alternative in {@link DungeonDSLParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void exitSet_param_type(DungeonDSLParser.Set_param_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#param_def_list}.
	 * @param ctx the parse tree
	 */
	void enterParam_def_list(DungeonDSLParser.Param_def_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#param_def_list}.
	 * @param ctx the parse tree
	 */
	void exitParam_def_list(DungeonDSLParser.Param_def_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#entity_type_def}.
	 * @param ctx the parse tree
	 */
	void enterEntity_type_def(DungeonDSLParser.Entity_type_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#entity_type_def}.
	 * @param ctx the parse tree
	 */
	void exitEntity_type_def(DungeonDSLParser.Entity_type_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#item_type_def}.
	 * @param ctx the parse tree
	 */
	void enterItem_type_def(DungeonDSLParser.Item_type_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#item_type_def}.
	 * @param ctx the parse tree
	 */
	void exitItem_type_def(DungeonDSLParser.Item_type_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#component_def_list}.
	 * @param ctx the parse tree
	 */
	void enterComponent_def_list(DungeonDSLParser.Component_def_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#component_def_list}.
	 * @param ctx the parse tree
	 */
	void exitComponent_def_list(DungeonDSLParser.Component_def_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#aggregate_value_def}.
	 * @param ctx the parse tree
	 */
	void enterAggregate_value_def(DungeonDSLParser.Aggregate_value_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#aggregate_value_def}.
	 * @param ctx the parse tree
	 */
	void exitAggregate_value_def(DungeonDSLParser.Aggregate_value_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#object_def}.
	 * @param ctx the parse tree
	 */
	void enterObject_def(DungeonDSLParser.Object_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#object_def}.
	 * @param ctx the parse tree
	 */
	void exitObject_def(DungeonDSLParser.Object_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#property_def_list}.
	 * @param ctx the parse tree
	 */
	void enterProperty_def_list(DungeonDSLParser.Property_def_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#property_def_list}.
	 * @param ctx the parse tree
	 */
	void exitProperty_def_list(DungeonDSLParser.Property_def_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#property_def}.
	 * @param ctx the parse tree
	 */
	void enterProperty_def(DungeonDSLParser.Property_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#property_def}.
	 * @param ctx the parse tree
	 */
	void exitProperty_def(DungeonDSLParser.Property_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#expression_list}.
	 * @param ctx the parse tree
	 */
	void enterExpression_list(DungeonDSLParser.Expression_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#expression_list}.
	 * @param ctx the parse tree
	 */
	void exitExpression_list(DungeonDSLParser.Expression_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#grouped_expression}.
	 * @param ctx the parse tree
	 */
	void enterGrouped_expression(DungeonDSLParser.Grouped_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#grouped_expression}.
	 * @param ctx the parse tree
	 */
	void exitGrouped_expression(DungeonDSLParser.Grouped_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#list_definition}.
	 * @param ctx the parse tree
	 */
	void enterList_definition(DungeonDSLParser.List_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#list_definition}.
	 * @param ctx the parse tree
	 */
	void exitList_definition(DungeonDSLParser.List_definitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#set_definition}.
	 * @param ctx the parse tree
	 */
	void enterSet_definition(DungeonDSLParser.Set_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#set_definition}.
	 * @param ctx the parse tree
	 */
	void exitSet_definition(DungeonDSLParser.Set_definitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimary(DungeonDSLParser.PrimaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimary(DungeonDSLParser.PrimaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#dot_def}.
	 * @param ctx the parse tree
	 */
	void enterDot_def(DungeonDSLParser.Dot_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#dot_def}.
	 * @param ctx the parse tree
	 */
	void exitDot_def(DungeonDSLParser.Dot_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#dot_stmt_list}.
	 * @param ctx the parse tree
	 */
	void enterDot_stmt_list(DungeonDSLParser.Dot_stmt_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#dot_stmt_list}.
	 * @param ctx the parse tree
	 */
	void exitDot_stmt_list(DungeonDSLParser.Dot_stmt_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#dot_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDot_stmt(DungeonDSLParser.Dot_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#dot_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDot_stmt(DungeonDSLParser.Dot_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#dot_edge_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDot_edge_stmt(DungeonDSLParser.Dot_edge_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#dot_edge_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDot_edge_stmt(DungeonDSLParser.Dot_edge_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#dot_node_list}.
	 * @param ctx the parse tree
	 */
	void enterDot_node_list(DungeonDSLParser.Dot_node_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#dot_node_list}.
	 * @param ctx the parse tree
	 */
	void exitDot_node_list(DungeonDSLParser.Dot_node_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#dot_edge_RHS}.
	 * @param ctx the parse tree
	 */
	void enterDot_edge_RHS(DungeonDSLParser.Dot_edge_RHSContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#dot_edge_RHS}.
	 * @param ctx the parse tree
	 */
	void exitDot_edge_RHS(DungeonDSLParser.Dot_edge_RHSContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#dot_node_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDot_node_stmt(DungeonDSLParser.Dot_node_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#dot_node_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDot_node_stmt(DungeonDSLParser.Dot_node_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link DungeonDSLParser#dot_attr_list}.
	 * @param ctx the parse tree
	 */
	void enterDot_attr_list(DungeonDSLParser.Dot_attr_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDSLParser#dot_attr_list}.
	 * @param ctx the parse tree
	 */
	void exitDot_attr_list(DungeonDSLParser.Dot_attr_listContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dot_attr_id}
	 * labeled alternative in {@link DungeonDSLParser#dot_attr}.
	 * @param ctx the parse tree
	 */
	void enterDot_attr_id(DungeonDSLParser.Dot_attr_idContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dot_attr_id}
	 * labeled alternative in {@link DungeonDSLParser#dot_attr}.
	 * @param ctx the parse tree
	 */
	void exitDot_attr_id(DungeonDSLParser.Dot_attr_idContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dot_attr_dependency_type}
	 * labeled alternative in {@link DungeonDSLParser#dot_attr}.
	 * @param ctx the parse tree
	 */
	void enterDot_attr_dependency_type(DungeonDSLParser.Dot_attr_dependency_typeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dot_attr_dependency_type}
	 * labeled alternative in {@link DungeonDSLParser#dot_attr}.
	 * @param ctx the parse tree
	 */
	void exitDot_attr_dependency_type(DungeonDSLParser.Dot_attr_dependency_typeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dt_sequence}
	 * labeled alternative in {@link DungeonDSLParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void enterDt_sequence(DungeonDSLParser.Dt_sequenceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dt_sequence}
	 * labeled alternative in {@link DungeonDSLParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void exitDt_sequence(DungeonDSLParser.Dt_sequenceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dt_subtask_mandatory}
	 * labeled alternative in {@link DungeonDSLParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void enterDt_subtask_mandatory(DungeonDSLParser.Dt_subtask_mandatoryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dt_subtask_mandatory}
	 * labeled alternative in {@link DungeonDSLParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void exitDt_subtask_mandatory(DungeonDSLParser.Dt_subtask_mandatoryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dt_subtask_optional}
	 * labeled alternative in {@link DungeonDSLParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void enterDt_subtask_optional(DungeonDSLParser.Dt_subtask_optionalContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dt_subtask_optional}
	 * labeled alternative in {@link DungeonDSLParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void exitDt_subtask_optional(DungeonDSLParser.Dt_subtask_optionalContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dt_conditional_correct}
	 * labeled alternative in {@link DungeonDSLParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void enterDt_conditional_correct(DungeonDSLParser.Dt_conditional_correctContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dt_conditional_correct}
	 * labeled alternative in {@link DungeonDSLParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void exitDt_conditional_correct(DungeonDSLParser.Dt_conditional_correctContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dt_conditional_false}
	 * labeled alternative in {@link DungeonDSLParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void enterDt_conditional_false(DungeonDSLParser.Dt_conditional_falseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dt_conditional_false}
	 * labeled alternative in {@link DungeonDSLParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void exitDt_conditional_false(DungeonDSLParser.Dt_conditional_falseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dt_sequence_and}
	 * labeled alternative in {@link DungeonDSLParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void enterDt_sequence_and(DungeonDSLParser.Dt_sequence_andContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dt_sequence_and}
	 * labeled alternative in {@link DungeonDSLParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void exitDt_sequence_and(DungeonDSLParser.Dt_sequence_andContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dt_sequence_or}
	 * labeled alternative in {@link DungeonDSLParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void enterDt_sequence_or(DungeonDSLParser.Dt_sequence_orContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dt_sequence_or}
	 * labeled alternative in {@link DungeonDSLParser#dependency_type}.
	 * @param ctx the parse tree
	 */
	void exitDt_sequence_or(DungeonDSLParser.Dt_sequence_orContext ctx);
}