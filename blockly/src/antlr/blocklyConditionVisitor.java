// Generated from D:/Documents/Forschungsprojekt/Dungeon/blockly_visualization/src/blockly.g4 by ANTLR 4.13.1
package antlr;
import nodes.INode;
import nodes.*;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import server.Server;
import server.Variable;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.NoSuchElementException;

/**
 *
 */
@SuppressWarnings("CheckReturnValue")
public class blocklyConditionVisitor extends blocklyBaseVisitor<INode> {

  public boolean calculateIntCompare(String op, int left, int right) {
    return switch (op) {
      case "==" -> left == right;
      case "!=" -> left != right;
      case "<=" -> left <= right;
      case ">=" -> left >= right;
      case ">" -> left > right;
      case "<" -> left < right;
      default -> throw new IllegalArgumentException("Unknown operator " + op);
    };
  }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public INode visitStart(blocklyParser.StartContext ctx) {
    INode output = visitChildren(ctx);
    return new StartNode("start",  output);
  }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public INode visitParenthese_Expr(blocklyParser.Parenthese_ExprContext ctx) {
    INode node = visit(ctx.ex);
    if(node.type.equals("expr")){
      ExprNode expr = (ExprNode) node;
      expr.parentheseExpr = true;
    }
    return node;
  }

  /**
   * We assume that this function will only be called with int nodes or variable nodes
   * @param node Node to extract the value from
   * @return Returns the extracted int value
   */
  private int extractIntValueFromNode(INode node) {
    if (node.type.equals("base")) {
      return ((BaseNode)node).intVal;
    }
    if (node.type.equals("var")) {
      return ((VarNode)node).value;
    }
    return -1;
  }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public INode visitCompare_Expr(blocklyParser.Compare_ExprContext ctx) {
    INode left = visit(ctx.left);
    INode right = visit(ctx.right);
    String op = ctx.op.getText();
    int leftValue = extractIntValueFromNode(left);
    int rightValue = extractIntValueFromNode(right);
    boolean boolVal = calculateIntCompare(op, leftValue , rightValue);

    BaseNode node = new BaseNode("base", Types.BOOLEAN);
    node.boolVal = boolVal;
    return node;

  }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public INode visitLogic_Expr(blocklyParser.Logic_ExprContext ctx) {
    INode left = visit(ctx.left);
    INode right = visit(ctx.right);
    String op = ctx.op.getText();
    assert left.type.equals("base");
    assert right.type.equals("base");

    BaseNode leftBase = (BaseNode) left;
    BaseNode rightBase = (BaseNode) right;

    boolean leftBool = leftBase.boolVal;
    boolean rightBool = rightBase.boolVal;

    BaseNode node = new BaseNode("base", Types.BOOLEAN);
    if (op.equals("&&")) {
      node.boolVal = leftBool && rightBool;
    } else if (op.equals("||")) {
      node.boolVal = leftBool || rightBool;
    }
    return node;
  }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public INode visitAtom_Expr(blocklyParser.Atom_ExprContext ctx) { return visitChildren(ctx); }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public INode visitNot_Expr(blocklyParser.Not_ExprContext ctx) {
    INode right = visit(ctx.right);
    assert right.type.equals("base");

    BaseNode rightBase = (BaseNode) right;
    rightBase.boolVal = !rightBase.boolVal;
    return rightBase;
  }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public INode visitRight_value(blocklyParser.Right_valueContext ctx) { return visitChildren(ctx); }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public INode visitFunc_call(blocklyParser.Func_callContext ctx) {
    String id =  ctx.id.getText();
    // Get boolean value
    boolean boolVal = switch (id) {
      case "naheWand" -> Server.isNearWall();
      case "WandOben" -> Server.isNearWallUp();
      case "WandUnten" -> Server.isNearWallDown();
      case "WandLinks" -> Server.isNearWallLeft();
      case "WandRechts" -> Server.isNearWallRight();
      default -> false;
    };
    BaseNode node = new BaseNode("base", Types.BOOLEAN);
    node.boolVal = boolVal;
    return node;
  }

  private boolean checkIfInteger(String token) {
    try{
      Integer.valueOf(token);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public INode visitValue(blocklyParser.ValueContext ctx) {
    String value = ctx.getText();

    if (checkIfInteger(value)){
      int val = Integer.parseInt(value);
      BaseNode node = new BaseNode("base", Types.INTEGER);
      node.intVal = val;
      return node;
    }

    BaseNode node = new BaseNode("base", Types.BOOLEAN);
    if (value.equals("wahr")){
      node.boolVal = true;
    } else if (value.equals("falsch")) {
      node.boolVal = false;
    } else {
      throw new IllegalArgumentException("Value node has invalid value.");
    }

    return node;
  }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public INode visitArguments(blocklyParser.ArgumentsContext ctx) { return visitChildren(ctx); }

  @Override public INode visitVar(blocklyParser.VarContext ctx) {
    String id = ctx.getText();
    Variable value = Server.variables.get(id);
    if (value == null) {
      throw new NoSuchElementException("Variable " + id + " could not be found");
    }
    if (!value.type.equals("base")) {
      throw new NoSuchElementException("Variable " + id + " is not a base type variable");
    }
    return new VarNode("var", id, value.intVal);
  }
}


