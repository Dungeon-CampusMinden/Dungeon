// Generated from C:/Users/bjarn/VS_Projects/Dungeon/dungeon-diagnostics/SemanticAnalysis.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SemanticAnalysisParser}.
 */
public interface SemanticAnalysisListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SemanticAnalysisParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(SemanticAnalysisParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link SemanticAnalysisParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(SemanticAnalysisParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by the {@code VarDeclStmt}
	 * labeled alternative in {@link SemanticAnalysisParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterVarDeclStmt(SemanticAnalysisParser.VarDeclStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code VarDeclStmt}
	 * labeled alternative in {@link SemanticAnalysisParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitVarDeclStmt(SemanticAnalysisParser.VarDeclStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code OtherCodeStmt}
	 * labeled alternative in {@link SemanticAnalysisParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterOtherCodeStmt(SemanticAnalysisParser.OtherCodeStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code OtherCodeStmt}
	 * labeled alternative in {@link SemanticAnalysisParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitOtherCodeStmt(SemanticAnalysisParser.OtherCodeStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code var_decl_assignment}
	 * labeled alternative in {@link SemanticAnalysisParser#var_decl}.
	 * @param ctx the parse tree
	 */
	void enterVar_decl_assignment(SemanticAnalysisParser.Var_decl_assignmentContext ctx);
	/**
	 * Exit a parse tree produced by the {@code var_decl_assignment}
	 * labeled alternative in {@link SemanticAnalysisParser#var_decl}.
	 * @param ctx the parse tree
	 */
	void exitVar_decl_assignment(SemanticAnalysisParser.Var_decl_assignmentContext ctx);
	/**
	 * Enter a parse tree produced by the {@code var_decl_type_decl}
	 * labeled alternative in {@link SemanticAnalysisParser#var_decl}.
	 * @param ctx the parse tree
	 */
	void enterVar_decl_type_decl(SemanticAnalysisParser.Var_decl_type_declContext ctx);
	/**
	 * Exit a parse tree produced by the {@code var_decl_type_decl}
	 * labeled alternative in {@link SemanticAnalysisParser#var_decl}.
	 * @param ctx the parse tree
	 */
	void exitVar_decl_type_decl(SemanticAnalysisParser.Var_decl_type_declContext ctx);
	/**
	 * Enter a parse tree produced by {@link SemanticAnalysisParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(SemanticAnalysisParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SemanticAnalysisParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(SemanticAnalysisParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code map_param_type}
	 * labeled alternative in {@link SemanticAnalysisParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void enterMap_param_type(SemanticAnalysisParser.Map_param_typeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code map_param_type}
	 * labeled alternative in {@link SemanticAnalysisParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void exitMap_param_type(SemanticAnalysisParser.Map_param_typeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code task_types}
	 * labeled alternative in {@link SemanticAnalysisParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void enterTask_types(SemanticAnalysisParser.Task_typesContext ctx);
	/**
	 * Exit a parse tree produced by the {@code task_types}
	 * labeled alternative in {@link SemanticAnalysisParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void exitTask_types(SemanticAnalysisParser.Task_typesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code id_param_type}
	 * labeled alternative in {@link SemanticAnalysisParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void enterId_param_type(SemanticAnalysisParser.Id_param_typeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code id_param_type}
	 * labeled alternative in {@link SemanticAnalysisParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void exitId_param_type(SemanticAnalysisParser.Id_param_typeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code list_param_type}
	 * labeled alternative in {@link SemanticAnalysisParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void enterList_param_type(SemanticAnalysisParser.List_param_typeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code list_param_type}
	 * labeled alternative in {@link SemanticAnalysisParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void exitList_param_type(SemanticAnalysisParser.List_param_typeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code set_param_type}
	 * labeled alternative in {@link SemanticAnalysisParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void enterSet_param_type(SemanticAnalysisParser.Set_param_typeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code set_param_type}
	 * labeled alternative in {@link SemanticAnalysisParser#type_decl}.
	 * @param ctx the parse tree
	 */
	void exitSet_param_type(SemanticAnalysisParser.Set_param_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SemanticAnalysisParser#taskTypes}.
	 * @param ctx the parse tree
	 */
	void enterTaskTypes(SemanticAnalysisParser.TaskTypesContext ctx);
	/**
	 * Exit a parse tree produced by {@link SemanticAnalysisParser#taskTypes}.
	 * @param ctx the parse tree
	 */
	void exitTaskTypes(SemanticAnalysisParser.TaskTypesContext ctx);
	/**
	 * Enter a parse tree produced by {@link SemanticAnalysisParser#otherCode}.
	 * @param ctx the parse tree
	 */
	void enterOtherCode(SemanticAnalysisParser.OtherCodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SemanticAnalysisParser#otherCode}.
	 * @param ctx the parse tree
	 */
	void exitOtherCode(SemanticAnalysisParser.OtherCodeContext ctx);
}