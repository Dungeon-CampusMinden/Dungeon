// Generated from C:/Users/bjarn/VS_Projects/Dungeon/dungeon-diagnostics/SemanticAnalysis.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link SemanticAnalysisParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface SemanticAnalysisVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link SemanticAnalysisParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(SemanticAnalysisParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by the {@code VarDeclStmt}
	 * labeled alternative in {@link SemanticAnalysisParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarDeclStmt(SemanticAnalysisParser.VarDeclStmtContext ctx);
	/**
	 * Visit a parse tree produced by the {@code OtherCodeStmt}
	 * labeled alternative in {@link SemanticAnalysisParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOtherCodeStmt(SemanticAnalysisParser.OtherCodeStmtContext ctx);
	/**
	 * Visit a parse tree produced by the {@code var_decl_assignment}
	 * labeled alternative in {@link SemanticAnalysisParser#var_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar_decl_assignment(SemanticAnalysisParser.Var_decl_assignmentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code var_decl_type_decl}
	 * labeled alternative in {@link SemanticAnalysisParser#var_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar_decl_type_decl(SemanticAnalysisParser.Var_decl_type_declContext ctx);
	/**
	 * Visit a parse tree produced by {@link SemanticAnalysisParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(SemanticAnalysisParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code map_param_type}
	 * labeled alternative in {@link SemanticAnalysisParser#type_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMap_param_type(SemanticAnalysisParser.Map_param_typeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code task_types}
	 * labeled alternative in {@link SemanticAnalysisParser#type_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTask_types(SemanticAnalysisParser.Task_typesContext ctx);
	/**
	 * Visit a parse tree produced by the {@code id_param_type}
	 * labeled alternative in {@link SemanticAnalysisParser#type_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitId_param_type(SemanticAnalysisParser.Id_param_typeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code list_param_type}
	 * labeled alternative in {@link SemanticAnalysisParser#type_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitList_param_type(SemanticAnalysisParser.List_param_typeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code set_param_type}
	 * labeled alternative in {@link SemanticAnalysisParser#type_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_param_type(SemanticAnalysisParser.Set_param_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link SemanticAnalysisParser#taskTypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTaskTypes(SemanticAnalysisParser.TaskTypesContext ctx);
	/**
	 * Visit a parse tree produced by {@link SemanticAnalysisParser#otherCode}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOtherCode(SemanticAnalysisParser.OtherCodeContext ctx);
}