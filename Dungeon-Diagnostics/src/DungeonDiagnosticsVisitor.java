// Generated from C:/Users/bjarn/VS_Projects/Dungeon/Dungeon-Diagnostics/DungeonDiagnostics.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link DungeonDiagnosticsParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface DungeonDiagnosticsVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(DungeonDiagnosticsParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by the {@code VarDeclStmt}
	 * labeled alternative in {@link DungeonDiagnosticsParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarDeclStmt(DungeonDiagnosticsParser.VarDeclStmtContext ctx);
	/**
	 * Visit a parse tree produced by the {@code OtherCodeStmt}
	 * labeled alternative in {@link DungeonDiagnosticsParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOtherCodeStmt(DungeonDiagnosticsParser.OtherCodeStmtContext ctx);
	/**
	 * Visit a parse tree produced by the {@code var_decl_assignment}
	 * labeled alternative in {@link DungeonDiagnosticsParser#var_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar_decl_assignment(DungeonDiagnosticsParser.Var_decl_assignmentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code var_decl_type_decl}
	 * labeled alternative in {@link DungeonDiagnosticsParser#var_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar_decl_type_decl(DungeonDiagnosticsParser.Var_decl_type_declContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(DungeonDiagnosticsParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code map_param_type}
	 * labeled alternative in {@link DungeonDiagnosticsParser#type_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMap_param_type(DungeonDiagnosticsParser.Map_param_typeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code task_types}
	 * labeled alternative in {@link DungeonDiagnosticsParser#type_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTask_types(DungeonDiagnosticsParser.Task_typesContext ctx);
	/**
	 * Visit a parse tree produced by the {@code id_param_type}
	 * labeled alternative in {@link DungeonDiagnosticsParser#type_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitId_param_type(DungeonDiagnosticsParser.Id_param_typeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code list_param_type}
	 * labeled alternative in {@link DungeonDiagnosticsParser#type_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitList_param_type(DungeonDiagnosticsParser.List_param_typeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code set_param_type}
	 * labeled alternative in {@link DungeonDiagnosticsParser#type_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_param_type(DungeonDiagnosticsParser.Set_param_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#taskTypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTaskTypes(DungeonDiagnosticsParser.TaskTypesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DungeonDiagnosticsParser#otherCode}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOtherCode(DungeonDiagnosticsParser.OtherCodeContext ctx);
}