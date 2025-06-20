// Generated from D:/Documents/Forschungsprojekt/Dungeon/blockly_visualization/src/blockly.g4 by
// ANTLR 4.13.1
package antlr;

import antlr.main.blocklyBaseVisitor;
import antlr.main.blocklyParser;
import components.BreadcrumbComponent;
import components.CloverComponent;
import contrib.components.AIComponent;
import contrib.components.LeverComponent;
import contrib.utils.Direction;
import core.Component;
import core.level.utils.LevelElement;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import nodes.*;
import nodes.INode;
import server.Server;
import server.Variable;
import utils.BlocklyCommands;

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
          case "naheWand" -> nearTile(ctx, LevelElement.WALL);
          case "naheBoden" -> nearTile(ctx, LevelElement.FLOOR);
          case "nahePit" -> nearTile(ctx, LevelElement.PIT);
          case "naheMonster" -> nearComponent(ctx, AIComponent.class);
          case "naheSchalter" -> nearComponent(ctx, LeverComponent.class);
          case "naheBrotkrume" -> nearComponent(ctx, BreadcrumbComponent.class);
          case "naheKleeblatt" -> nearComponent(ctx, CloverComponent.class);
          case "aktiv" -> active(ctx);
          case "bossBlickrichtung" -> checkBossViewDirection(ctx);
          default -> {
            LOGGER.warning("Unknown function " + id);
            yield false;
          }
        };
    BaseNode node = new BaseNode(Types.BOOLEAN);
    node.boolVal = boolVal;
    return node;
  }

  private boolean active(blocklyParser.Func_callContext ctx) {
    if (ctx.args == null) {
      LOGGER.warning("active operation function: Expected 1 argument, got 0");
      return false;
    }

    // Get the first argument and visit it
    INode argNode = visit(ctx.args.expr(0));
    String direction;

    if (argNode instanceof BaseNode && ((BaseNode) argNode).baseType == Types.STRING) {
      direction = ((BaseNode) argNode).strVal;
    } else {
      LOGGER.warning("Expected string argument for active operation");
      return false;
    }

    return BlocklyCommands.active(Direction.fromString(direction));
  }

  private boolean nearComponent(
      blocklyParser.Func_callContext ctx, Class<? extends Component> comonentType) {
    if (ctx.args == null) {
      LOGGER.warning("naheComponent operation function: Expected 1 argument, got 0");
      return false;
    }

    // Get the first argument and visit it
    INode argNode = visit(ctx.args.expr(0));
    String direction;

    if (argNode instanceof BaseNode && ((BaseNode) argNode).baseType == Types.STRING) {
      direction = ((BaseNode) argNode).strVal;
    } else {
      LOGGER.warning("Expected string argument for naheComponent operation");
      return false;
    }

    return BlocklyCommands.isNearComponent(comonentType, Direction.fromString(direction));
  }

  private Boolean nearTile(blocklyParser.Func_callContext ctx, LevelElement tileType) {
    // Check if arguments exist
    if (ctx.args == null) {
      LOGGER.warning("naheTile operation function: Expected 1 argument, got 0");
      return false;
    }

    // Get the first argument and visit it
    INode argNode = visit(ctx.args.expr(0));
    String direction;

    if (argNode instanceof BaseNode && ((BaseNode) argNode).baseType == Types.STRING) {
      direction = ((BaseNode) argNode).strVal;
    } else {
      LOGGER.warning("Expected string argument for naheTile operation");
      return false;
    }

    if (tileType == LevelElement.FLOOR) {
      return BlocklyCommands.isNearTile(LevelElement.FLOOR, Direction.fromString(direction))
          || BlocklyCommands.isNearTile(LevelElement.EXIT, Direction.fromString(direction));
    } else return BlocklyCommands.isNearTile(tileType, Direction.fromString(direction));
  }

  private boolean checkBossViewDirection(blocklyParser.Func_callContext ctx) {
    if (ctx.args == null) {
      LOGGER.warning("checkBossViewDirection operation function: Expected 1 argument, got 0");
      return false;
    }

    // Get the first argument and visit it
    INode argNode = visit(ctx.args.expr(0));
    String direction;

    if (argNode instanceof BaseNode && ((BaseNode) argNode).baseType == Types.STRING) {
      direction = ((BaseNode) argNode).strVal;
    } else {
      LOGGER.warning("Expected string argument for checkBossViewDirection operation");
      return false;
    }

    return BlocklyCommands.checkBossViewDirection(Direction.fromString(direction));
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
