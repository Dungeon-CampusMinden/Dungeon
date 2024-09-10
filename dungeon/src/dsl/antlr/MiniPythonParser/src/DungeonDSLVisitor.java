// Generated from c:/Users/bjarn/VS_Projects/Dungeon/dungeon/src/dsl/antlr/DungeonDSL.g4 by ANTLR 4.13.1

    package antlr.main;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link DungeonDSLParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface DungeonDSLVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(DungeonDSLParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefinition(DungeonDSLParser.DefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#fn_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFn_def(DungeonDSLParser.Fn_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStmt(DungeonDSLParser.StmtContext ctx);
	/**
	 * Visit a parse tree produced by the {@code for_loop}
	 * labeled alternative in {@link DungeonDSLParser#loop_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_loop(DungeonDSLParser.For_loopContext ctx);
	/**
	 * Visit a parse tree produced by the {@code for_loop_counting}
	 * labeled alternative in {@link DungeonDSLParser#loop_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_loop_counting(DungeonDSLParser.For_loop_countingContext ctx);
	/**
	 * Visit a parse tree produced by the {@code while_loop}
	 * labeled alternative in {@link DungeonDSLParser#loop_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhile_loop(DungeonDSLParser.While_loopContext ctx);
	/**
	 * Visit a parse tree produced by the {@code var_decl_assignment}
	 * labeled alternative in {@link DungeonDSLParser#var_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar_decl_assignment(DungeonDSLParser.Var_decl_assignmentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code var_decl_type_decl}
	 * labeled alternative in {@link DungeonDSLParser#var_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar_decl_type_decl(DungeonDSLParser.Var_decl_type_declContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(DungeonDSLParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code method_call_expression}
	 * labeled alternative in {@link DungeonDSLParser#expression_rhs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethod_call_expression(DungeonDSLParser.Method_call_expressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code member_access_expression}
	 * labeled alternative in {@link DungeonDSLParser#expression_rhs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMember_access_expression(DungeonDSLParser.Member_access_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#assignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment(DungeonDSLParser.AssignmentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code assignee_func_call}
	 * labeled alternative in {@link DungeonDSLParser#assignee}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignee_func_call(DungeonDSLParser.Assignee_func_callContext ctx);
	/**
	 * Visit a parse tree produced by the {@code assignee_qualified_name}
	 * labeled alternative in {@link DungeonDSLParser#assignee}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignee_qualified_name(DungeonDSLParser.Assignee_qualified_nameContext ctx);
	/**
	 * Visit a parse tree produced by the {@code assignee_identifier}
	 * labeled alternative in {@link DungeonDSLParser#assignee}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignee_identifier(DungeonDSLParser.Assignee_identifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#logic_or}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogic_or(DungeonDSLParser.Logic_orContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#logic_and}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogic_and(DungeonDSLParser.Logic_andContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#equality}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEquality(DungeonDSLParser.EqualityContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#comparison}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparison(DungeonDSLParser.ComparisonContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm(DungeonDSLParser.TermContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#factor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFactor(DungeonDSLParser.FactorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#unary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnary(DungeonDSLParser.UnaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#func_call}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_call(DungeonDSLParser.Func_callContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#stmt_block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStmt_block(DungeonDSLParser.Stmt_blockContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#stmt_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStmt_list(DungeonDSLParser.Stmt_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#return_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturn_stmt(DungeonDSLParser.Return_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#conditional_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditional_stmt(DungeonDSLParser.Conditional_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#else_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElse_stmt(DungeonDSLParser.Else_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#ret_type_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRet_type_def(DungeonDSLParser.Ret_type_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#param_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam_def(DungeonDSLParser.Param_defContext ctx);
	/**
	 * Visit a parse tree produced by the {@code map_param_type}
	 * labeled alternative in {@link DungeonDSLParser#type_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMap_param_type(DungeonDSLParser.Map_param_typeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code id_param_type}
	 * labeled alternative in {@link DungeonDSLParser#type_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitId_param_type(DungeonDSLParser.Id_param_typeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code list_param_type}
	 * labeled alternative in {@link DungeonDSLParser#type_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitList_param_type(DungeonDSLParser.List_param_typeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code set_param_type}
	 * labeled alternative in {@link DungeonDSLParser#type_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_param_type(DungeonDSLParser.Set_param_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#param_def_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam_def_list(DungeonDSLParser.Param_def_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#entity_type_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEntity_type_def(DungeonDSLParser.Entity_type_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#item_type_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitItem_type_def(DungeonDSLParser.Item_type_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#component_def_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComponent_def_list(DungeonDSLParser.Component_def_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#aggregate_value_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregate_value_def(DungeonDSLParser.Aggregate_value_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#object_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObject_def(DungeonDSLParser.Object_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#property_def_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProperty_def_list(DungeonDSLParser.Property_def_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#property_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProperty_def(DungeonDSLParser.Property_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#expression_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression_list(DungeonDSLParser.Expression_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#grouped_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrouped_expression(DungeonDSLParser.Grouped_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#list_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitList_definition(DungeonDSLParser.List_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#set_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_definition(DungeonDSLParser.Set_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimary(DungeonDSLParser.PrimaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#dot_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDot_def(DungeonDSLParser.Dot_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#dot_stmt_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDot_stmt_list(DungeonDSLParser.Dot_stmt_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#dot_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDot_stmt(DungeonDSLParser.Dot_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#dot_edge_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDot_edge_stmt(DungeonDSLParser.Dot_edge_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#dot_node_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDot_node_list(DungeonDSLParser.Dot_node_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#dot_edge_RHS}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDot_edge_RHS(DungeonDSLParser.Dot_edge_RHSContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#dot_node_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDot_node_stmt(DungeonDSLParser.Dot_node_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDSLParser#dot_attr_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDot_attr_list(DungeonDSLParser.Dot_attr_listContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dot_attr_id}
	 * labeled alternative in {@link DungeonDSLParser#dot_attr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDot_attr_id(DungeonDSLParser.Dot_attr_idContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dot_attr_dependency_type}
	 * labeled alternative in {@link DungeonDSLParser#dot_attr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDot_attr_dependency_type(DungeonDSLParser.Dot_attr_dependency_typeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dt_sequence}
	 * labeled alternative in {@link DungeonDSLParser#dependency_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDt_sequence(DungeonDSLParser.Dt_sequenceContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dt_subtask_mandatory}
	 * labeled alternative in {@link DungeonDSLParser#dependency_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDt_subtask_mandatory(DungeonDSLParser.Dt_subtask_mandatoryContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dt_subtask_optional}
	 * labeled alternative in {@link DungeonDSLParser#dependency_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDt_subtask_optional(DungeonDSLParser.Dt_subtask_optionalContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dt_conditional_correct}
	 * labeled alternative in {@link DungeonDSLParser#dependency_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDt_conditional_correct(DungeonDSLParser.Dt_conditional_correctContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dt_conditional_false}
	 * labeled alternative in {@link DungeonDSLParser#dependency_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDt_conditional_false(DungeonDSLParser.Dt_conditional_falseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dt_sequence_and}
	 * labeled alternative in {@link DungeonDSLParser#dependency_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDt_sequence_and(DungeonDSLParser.Dt_sequence_andContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dt_sequence_or}
	 * labeled alternative in {@link DungeonDSLParser#dependency_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDt_sequence_or(DungeonDSLParser.Dt_sequence_orContext ctx);
}