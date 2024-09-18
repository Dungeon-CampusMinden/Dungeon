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
	 * Enter a parse tree produced by the {@code VarDeclStmt}
	 * labeled alternative in {@link DungeonDiagnosticsParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterVarDeclStmt(DungeonDiagnosticsParser.VarDeclStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code VarDeclStmt}
	 * labeled alternative in {@link DungeonDiagnosticsParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitVarDeclStmt(DungeonDiagnosticsParser.VarDeclStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code OtherCodeStmt}
	 * labeled alternative in {@link DungeonDiagnosticsParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterOtherCodeStmt(DungeonDiagnosticsParser.OtherCodeStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code OtherCodeStmt}
	 * labeled alternative in {@link DungeonDiagnosticsParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitOtherCodeStmt(DungeonDiagnosticsParser.OtherCodeStmtContext ctx);
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
	 * Enter a parse tree produced by {@link DungeonDiagnosticsParser#otherCode}.
	 * @param ctx the parse tree
	 */
	void enterOtherCode(DungeonDiagnosticsParser.OtherCodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DungeonDiagnosticsParser#otherCode}.
	 * @param ctx the parse tree
	 */
	void exitOtherCode(DungeonDiagnosticsParser.OtherCodeContext ctx);
}