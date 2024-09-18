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
	 * Visit a parse tree produced by the {@code VarDeclStmt}
	 * labeled alternative in {@link grammerParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarDeclStmt(grammerParser.VarDeclStmtContext ctx);
	/**
	 * Visit a parse tree produced by the {@code OtherCodeStmt}
	 * labeled alternative in {@link grammerParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOtherCodeStmt(grammerParser.OtherCodeStmtContext ctx);
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
	 * Visit a parse tree produced by {@link grammerParser#otherCode}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOtherCode(grammerParser.OtherCodeContext ctx);
}