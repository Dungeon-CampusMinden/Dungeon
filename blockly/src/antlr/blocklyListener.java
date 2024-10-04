// Generated from D:/Documents/Forschungsprojekt/Dungeon/blockly_visualization/src/blockly.g4 by
// ANTLR 4.13.1
package antlr;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by {@link blocklyParser}.
 */
public interface blocklyListener extends ParseTreeListener {
  /**
   * Enter a parse tree produced by {@link blocklyParser#start}.
   *
   * @param ctx the parse tree
   */
  void enterStart(blocklyParser.StartContext ctx);

  /**
   * Exit a parse tree produced by {@link blocklyParser#start}.
   *
   * @param ctx the parse tree
   */
  void exitStart(blocklyParser.StartContext ctx);

  /**
   * Enter a parse tree produced by the {@code Parenthese_Expr} labeled alternative in {@link
   * blocklyParser#expr}.
   *
   * @param ctx the parse tree
   */
  void enterParenthese_Expr(blocklyParser.Parenthese_ExprContext ctx);

  /**
   * Exit a parse tree produced by the {@code Parenthese_Expr} labeled alternative in {@link
   * blocklyParser#expr}.
   *
   * @param ctx the parse tree
   */
  void exitParenthese_Expr(blocklyParser.Parenthese_ExprContext ctx);

  /**
   * Enter a parse tree produced by the {@code Compare_Expr} labeled alternative in {@link
   * blocklyParser#expr}.
   *
   * @param ctx the parse tree
   */
  void enterCompare_Expr(blocklyParser.Compare_ExprContext ctx);

  /**
   * Exit a parse tree produced by the {@code Compare_Expr} labeled alternative in {@link
   * blocklyParser#expr}.
   *
   * @param ctx the parse tree
   */
  void exitCompare_Expr(blocklyParser.Compare_ExprContext ctx);

  /**
   * Enter a parse tree produced by the {@code Logic_Expr} labeled alternative in {@link
   * blocklyParser#expr}.
   *
   * @param ctx the parse tree
   */
  void enterLogic_Expr(blocklyParser.Logic_ExprContext ctx);

  /**
   * Exit a parse tree produced by the {@code Logic_Expr} labeled alternative in {@link
   * blocklyParser#expr}.
   *
   * @param ctx the parse tree
   */
  void exitLogic_Expr(blocklyParser.Logic_ExprContext ctx);

  /**
   * Enter a parse tree produced by the {@code Atom_Expr} labeled alternative in {@link
   * blocklyParser#expr}.
   *
   * @param ctx the parse tree
   */
  void enterAtom_Expr(blocklyParser.Atom_ExprContext ctx);

  /**
   * Exit a parse tree produced by the {@code Atom_Expr} labeled alternative in {@link
   * blocklyParser#expr}.
   *
   * @param ctx the parse tree
   */
  void exitAtom_Expr(blocklyParser.Atom_ExprContext ctx);

  /**
   * Enter a parse tree produced by the {@code Not_Expr} labeled alternative in {@link
   * blocklyParser#expr}.
   *
   * @param ctx the parse tree
   */
  void enterNot_Expr(blocklyParser.Not_ExprContext ctx);

  /**
   * Exit a parse tree produced by the {@code Not_Expr} labeled alternative in {@link
   * blocklyParser#expr}.
   *
   * @param ctx the parse tree
   */
  void exitNot_Expr(blocklyParser.Not_ExprContext ctx);

  /**
   * Enter a parse tree produced by {@link blocklyParser#right_value}.
   *
   * @param ctx the parse tree
   */
  void enterRight_value(blocklyParser.Right_valueContext ctx);

  /**
   * Exit a parse tree produced by {@link blocklyParser#right_value}.
   *
   * @param ctx the parse tree
   */
  void exitRight_value(blocklyParser.Right_valueContext ctx);

  /**
   * Enter a parse tree produced by {@link blocklyParser#func_call}.
   *
   * @param ctx the parse tree
   */
  void enterFunc_call(blocklyParser.Func_callContext ctx);

  /**
   * Exit a parse tree produced by {@link blocklyParser#func_call}.
   *
   * @param ctx the parse tree
   */
  void exitFunc_call(blocklyParser.Func_callContext ctx);

  /**
   * Enter a parse tree produced by {@link blocklyParser#var}.
   *
   * @param ctx the parse tree
   */
  void enterVar(blocklyParser.VarContext ctx);

  /**
   * Exit a parse tree produced by {@link blocklyParser#var}.
   *
   * @param ctx the parse tree
   */
  void exitVar(blocklyParser.VarContext ctx);

  /**
   * Enter a parse tree produced by {@link blocklyParser#value}.
   *
   * @param ctx the parse tree
   */
  void enterValue(blocklyParser.ValueContext ctx);

  /**
   * Exit a parse tree produced by {@link blocklyParser#value}.
   *
   * @param ctx the parse tree
   */
  void exitValue(blocklyParser.ValueContext ctx);

  /**
   * Enter a parse tree produced by {@link blocklyParser#arguments}.
   *
   * @param ctx the parse tree
   */
  void enterArguments(blocklyParser.ArgumentsContext ctx);

  /**
   * Exit a parse tree produced by {@link blocklyParser#arguments}.
   *
   * @param ctx the parse tree
   */
  void exitArguments(blocklyParser.ArgumentsContext ctx);
}
