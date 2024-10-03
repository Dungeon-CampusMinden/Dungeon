// Generated from C:/Users/bjarn/VS_Projects/Dungeon/Dungeon-Diagnostics/SemanticAnalysis.g4 by ANTLR 4.13.1
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
	 * Enter a parse tree produced by the {@code VarDeclStmt}
	 * labeled alternative in {@link grammerParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterVarDeclStmt(grammerParser.VarDeclStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code VarDeclStmt}
	 * labeled alternative in {@link grammerParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitVarDeclStmt(grammerParser.VarDeclStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code OtherCodeStmt}
	 * labeled alternative in {@link grammerParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterOtherCodeStmt(grammerParser.OtherCodeStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code OtherCodeStmt}
	 * labeled alternative in {@link grammerParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitOtherCodeStmt(grammerParser.OtherCodeStmtContext ctx);
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
	 * Enter a parse tree produced by {@link grammerParser#otherCode}.
	 * @param ctx the parse tree
	 */
	void enterOtherCode(grammerParser.OtherCodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link grammerParser#otherCode}.
	 * @param ctx the parse tree
	 */
	void exitOtherCode(grammerParser.OtherCodeContext ctx);
}
