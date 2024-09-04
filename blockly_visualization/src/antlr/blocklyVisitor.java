// Generated from D:/Documents/Forschungsprojekt/Dungeon/blockly_visualization/src/blockly.g4 by ANTLR 4.13.1
package antlr;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link blocklyParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface blocklyVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link blocklyParser#start}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStart(blocklyParser.StartContext ctx);
	/**
	 * Visit a parse tree produced by {@link blocklyParser#condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCondition(blocklyParser.ConditionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Parenthese_Expr}
	 * labeled alternative in {@link blocklyParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenthese_Expr(blocklyParser.Parenthese_ExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Operator_Expr}
	 * labeled alternative in {@link blocklyParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperator_Expr(blocklyParser.Operator_ExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Compare_Expr}
	 * labeled alternative in {@link blocklyParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompare_Expr(blocklyParser.Compare_ExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Logic_Expr}
	 * labeled alternative in {@link blocklyParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogic_Expr(blocklyParser.Logic_ExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Atom_Expr}
	 * labeled alternative in {@link blocklyParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtom_Expr(blocklyParser.Atom_ExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Not_Expr}
	 * labeled alternative in {@link blocklyParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNot_Expr(blocklyParser.Not_ExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Unary_Expr}
	 * labeled alternative in {@link blocklyParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnary_Expr(blocklyParser.Unary_ExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link blocklyParser#right_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRight_value(blocklyParser.Right_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link blocklyParser#func_call}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_call(blocklyParser.Func_callContext ctx);
	/**
	 * Visit a parse tree produced by {@link blocklyParser#var}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar(blocklyParser.VarContext ctx);
	/**
	 * Visit a parse tree produced by {@link blocklyParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(blocklyParser.ValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link blocklyParser#arguments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArguments(blocklyParser.ArgumentsContext ctx);
}