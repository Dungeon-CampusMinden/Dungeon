// Generated from D:/Documents/Forschungsprojekt/Dungeon/blockly_visualization/src/blockly.g4 by
// ANTLR 4.13.1
package antlr;

import antlr.main.blocklyBaseVisitor;
import antlr.main.blocklyParser;
import components.BlocklyMonsterComponent;
import components.BreadcrumbComponent;
import contrib.components.LeverComponent;
import core.level.elements.tile.FloorTile;
import core.level.elements.tile.PitTile;
import core.level.elements.tile.WallTile;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import nodes.*;
import nodes.INode;
import server.Server;
import server.Variable;
import utils.Direction;

/**
 * This class defines the visitor for the condition part of the blockly language. It is used to
 * evaluate the boolean value of a condition.
 */
public class BlocklyConditionVisitor extends blocklyBaseVisitor<INode> {

  private static final Logger LOGGER =
      Logger.getLogger(BlocklyConditionVisitor.class.getSimpleName());

  private final Server httpServer;

  /**
   * Calculate the boolean value of an integer compare operation. Returns the result of the
   * comparison.
   *
   * @param op Operator of the comparison
   * @param left Left integer of the comparison
   * @param right Right operator of the comparison
   * @return Returns the result of the comparison
   */
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
   * Create a new BlocklyConditionVisitor with a server.
   *
   * @param httpServer Server to use
   */
  public BlocklyConditionVisitor(Server httpServer) {
    this.httpServer = httpServer;
  }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling {@link #visitChildren} on {@code
   * ctx}.
   */
  @Override
  public INode visitStart(blocklyParser.StartContext ctx) {
    INode output = visitChildren(ctx);
    return new StartNode(output);
  }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling {@link #visitChildren} on {@code
   * ctx}.
   */
  @Override
  public INode visitParenthese_Expr(blocklyParser.Parenthese_ExprContext ctx) {
    return visit(ctx.ex);
  }

  /**
   * We assume that this function will only be called with int nodes or variable nodes.
   *
   * @param node Node to extract the value from
   * @return Returns the extracted int value
   */
  private int extractIntValueFromNode(INode node) {
    if (node.type.equals("base")) {
      return ((BaseNode) node).intVal;
    }
    if (node.type.equals("var")) {
      return ((VarNode) node).value;
    }
    return -1;
  }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling {@link #visitChildren} on {@code
   * ctx}.
   */
  @Override
  public INode visitCompare_Expr(blocklyParser.Compare_ExprContext ctx) {
    INode left = visit(ctx.left);
    INode right = visit(ctx.right);
    String op = ctx.op.getText();
    int leftValue = extractIntValueFromNode(left);
    int rightValue = extractIntValueFromNode(right);
    boolean boolVal = calculateIntCompare(op, leftValue, rightValue);

    BaseNode node = new BaseNode(Types.BOOLEAN);
    node.boolVal = boolVal;
    return node;
  }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling {@link #visitChildren} on {@code
   * ctx}.
   */
  @Override
  public INode visitLogic_Expr(blocklyParser.Logic_ExprContext ctx) {
    INode left = visit(ctx.left);
    INode right = visit(ctx.right);
    String op = ctx.op.getText();
    assert left.type.equals("base");
    assert right.type.equals("base");

    BaseNode leftBase = (BaseNode) left;
    BaseNode rightBase = (BaseNode) right;

    boolean leftBool = leftBase.boolVal;
    boolean rightBool = rightBase.boolVal;

    BaseNode node = new BaseNode(Types.BOOLEAN);
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
   * <p>The default implementation returns the result of calling {@link #visitChildren} on {@code
   * ctx}.
   */
  @Override
  public INode visitAtom_Expr(blocklyParser.Atom_ExprContext ctx) {
    return visitChildren(ctx);
  }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling {@link #visitChildren} on {@code
   * ctx}.
   */
  @Override
  public INode visitNot_Expr(blocklyParser.Not_ExprContext ctx) {
    INode right = visit(ctx.right);
    assert right.type.equals("base");

    BaseNode rightBase = (BaseNode) right;
    rightBase.boolVal = !rightBase.boolVal;
    return rightBase;
  }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling {@link #visitChildren} on {@code
   * ctx}.
   */
  @Override
  public INode visitRight_value(blocklyParser.Right_valueContext ctx) {
    return visitChildren(ctx);
  }

  /** {@inheritDoc} */
  @Override
  public INode visitFunc_call(blocklyParser.Func_callContext ctx) {
    String id = ctx.id.getText();
    boolean boolVal =
        switch (id) {
          case "naheWand" -> {
            // Check if arguments exist
            if (ctx.args == null) {
              LOGGER.warning("naheWand function: Expected 1 argument, got 0");
              yield false;
            }

            // Get the first argument and visit it
            INode argNode = visit(ctx.args.expr(0));
            String direction;

            if (argNode instanceof BaseNode && ((BaseNode) argNode).baseType == Types.STRING) {
              direction = ((BaseNode) argNode).strVal;
            } else {
              LOGGER.warning("Expected string argument for naheWand");
              yield false;
            }

            yield httpServer.isNearTile(WallTile.class, Direction.fromString(direction));
          }
          case "naheBoden" -> {
            // Check if arguments exist
            if (ctx.args == null) {
              LOGGER.warning("naheBoden function: Expected 1 argument, got 0");
              yield false;
            }

            // Get the first argument and visit it
            INode argNode = visit(ctx.args.expr(0));
            String direction;

            if (argNode instanceof BaseNode && ((BaseNode) argNode).baseType == Types.STRING) {
              direction = ((BaseNode) argNode).strVal;
            } else {
              LOGGER.warning("Expected string argument for naheBoden");
              yield false;
            }

            yield httpServer.isNearTile(FloorTile.class, Direction.fromString(direction));
          }
          case "nahePit" -> {
            // Check if arguments exist
            if (ctx.args == null) {
              LOGGER.warning("nahePit function: Expected 1 argument, got 0");
              yield false;
            }

            // Get the first argument and visit it
            INode argNode = visit(ctx.args.expr(0));
            String direction;

            if (argNode instanceof BaseNode && ((BaseNode) argNode).baseType == Types.STRING) {
              direction = ((BaseNode) argNode).strVal;
            } else {
              LOGGER.warning("Expected string argument for nahePit");
              yield false;
            }

            yield httpServer.isNearTile(PitTile.class, Direction.fromString(direction));
          }
          case "naheMonster" -> {
            // Check if arguments exist
            if (ctx.args == null) {
              LOGGER.warning("naheMonster function: Expected 1 argument, got 0");
              yield false;
            }

            // Get the first argument and visit it
            INode argNode = visit(ctx.args.expr(0));
            String direction;

            if (argNode instanceof BaseNode && ((BaseNode) argNode).baseType == Types.STRING) {
              direction = ((BaseNode) argNode).strVal;
            } else {
              LOGGER.warning("Expected string argument for naheMonster");
              yield false;
            }

            yield httpServer.isNearComponent(
                BlocklyMonsterComponent.class, Direction.fromString(direction));
          }
          case "naheSchalter" -> {
            // Check if arguments exist
            if (ctx.args == null) {
              LOGGER.warning("naheSchalter function: Expected 1 argument, got 0");
              yield false;
            }

            // Get the first argument and visit it
            INode argNode = visit(ctx.args.expr(0));
            String direction;

            if (argNode instanceof BaseNode && ((BaseNode) argNode).baseType == Types.STRING) {
              direction = ((BaseNode) argNode).strVal;
            } else {
              LOGGER.warning("Expected string argument for naheSchalter");
              yield false;
            }

            yield httpServer.isNearComponent(LeverComponent.class, Direction.fromString(direction));
          }
          case "naheBrotkrume" -> {
            // Check if arguments exist
            if (ctx.args == null) {
              LOGGER.warning("naheBrotkrume function: Expected 1 argument, got 0");
              yield false;
            }

            // Get the first argument and visit it
            INode argNode = visit(ctx.args.expr(0));
            String direction;

            if (argNode instanceof BaseNode && ((BaseNode) argNode).baseType == Types.STRING) {
              direction = ((BaseNode) argNode).strVal;
            } else {
              LOGGER.warning("Expected string argument for naheBrotkrume");
              yield false;
            }

            yield httpServer.isNearComponent(
                BreadcrumbComponent.class, Direction.fromString(direction));
          }
          default -> {
            LOGGER.warning("Unknown function " + id);
            yield false;
          }
        };
    BaseNode node = new BaseNode(Types.BOOLEAN);
    node.boolVal = boolVal;
    return node;
  }

  /** {@inheritDoc} */
  @Override
  public INode visitValue(blocklyParser.ValueContext ctx) {
    if (ctx.INT() != null) {
      int val = Integer.parseInt(ctx.INT().getText());
      BaseNode node = new BaseNode(Types.INTEGER);
      node.intVal = val;
      return node;
    } else if (ctx.STRING() != null) {
      // Get the string text and remove surrounding quotes
      String text = ctx.STRING().getText();
      String strVal = text.substring(1, text.length() - 1);
      BaseNode node = new BaseNode(Types.STRING);
      node.strVal = strVal;
      return node;
    } else if (ctx.BOOLEAN() != null) {
      String value = ctx.BOOLEAN().getText();
      BaseNode node = new BaseNode(Types.BOOLEAN);
      if (value.equals("wahr")) {
        node.boolVal = true;
      } else if (value.equals("falsch")) {
        node.boolVal = false;
      } else {
        throw new IllegalArgumentException("Invalid boolean value: " + value);
      }
      return node;
    }

    throw new IllegalArgumentException("Value node has invalid value.");
  }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling {@link #visitChildren} on {@code
   * ctx}.
   */
  @Override
  public INode visitArguments(blocklyParser.ArgumentsContext ctx) {
    return visitChildren(ctx);
  }

  @Override
  public INode visitVar(blocklyParser.VarContext ctx) {
    String id = ctx.getText();
    Variable value = httpServer.variables.get(id);
    if (value == null) {
      throw new NoSuchElementException("Variable " + id + " could not be found");
    }
    if (!value.type.equals("base")) {
      throw new NoSuchElementException("Variable " + id + " is not a base type variable");
    }
    return new VarNode(id, value.intVal);
  }
}
