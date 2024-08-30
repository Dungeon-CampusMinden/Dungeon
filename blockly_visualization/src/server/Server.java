package server;

import com.badlogic.gdx.Gdx;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import contrib.utils.components.Debugger;
import contrib.utils.components.skill.FireballSkill;
import contrib.utils.components.skill.Skill;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** WTF? . */
public class Server {
  private static Entity hero;
  private static boolean if_flag = false;
  private static boolean else_flag = false;

  private static ArrayList<Boolean> while_is_repeating = new ArrayList<>();
  private static boolean current_while_cond_negative = false;
  private static ArrayList<ArrayList<String>> whileBodys = new ArrayList<>();
  private static Stack<String> while_conditions = new Stack<>();

  // This variable holds all active scopes in a stack. The value at the top of the stack is the current scope.
  // It can hold the following values: if, while.
  private static Stack<String> active_scopes = new Stack<>();
  private static Dictionary<String, Integer> variables = new Hashtable<>();

  /**
   * WTF? .
   *
   * @param hero
   */
  public Server(Entity hero) {
    Server.hero = hero;
  }

  /**
   * WTF? .
   *
   * @throws IOException
   */
  public void start() throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 8080), 0);
    HttpContext startContext = server.createContext("/start");
    startContext.setHandler(Server::handleStartRequest);
    HttpContext resetContext = server.createContext("/reset");
    resetContext.setHandler(Server::handleResetRequest);
    server.start();
  }

  private static void handleStartRequest(HttpExchange exchange) throws IOException {
    InputStream inStream = exchange.getRequestBody();
    String text = new String(inStream.readAllBytes(), StandardCharsets.UTF_8);

    String[] actions = text.split("\n");

    for (String action : actions) {
      processAction(action);
      // Repeat statements of while loop as long as while flag is set
      if (!active_scopes.isEmpty() && active_scopes.peek().equals("while")) {
        while (while_is_repeating.get(while_is_repeating.size() - 1)) {
          for (String whileAction : whileBodys.get(whileBodys.size() - 1)) {
            System.out.println("Repeating whileaction");
            System.out.println(whileAction);
            processAction(whileAction);
          }
        }
      }

    }

    PositionComponent pc = getHeroPosition();
    String response = pc.position().x + "," + pc.position().y;

    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    exchange.sendResponseHeaders(200, response.getBytes().length);
    OutputStream os = exchange.getResponseBody();
    os.write(response.getBytes());
    os.close();
  }

  private static void processAction(String action) {
    // Make sure we close the right scope
    if (action.equals("}") && !active_scopes.isEmpty()) {
      String current_scope = active_scopes.peek();
      if (current_scope.equals("if")) {
        ifEvaluation(action);
      } else if (current_scope.equals("while")) {
        whileBodys.get(whileBodys.size() - 1).add(action);
        whileEvaluation(action);
      }
    } else if (!active_scopes.isEmpty() && active_scopes.peek().equals("while") ) {
      // We must add action to all currently active while loops
      for (int i = 0; i < whileBodys.size(); i++) {
        if (!while_is_repeating.get(i)) {
          whileBodys.get(i).add(action);
          System.out.println(whileBodys.get(i));
        }
      }
    }
    ifEvaluation(action);
    whileEvaluation(action);
    variableEvaluation(action);
    System.out.println(current_while_cond_negative);
    System.out.println(active_scopes);

    // Do not perform any actions if current while condition is false
    if (current_while_cond_negative) {
      return;
    }

    if (if_flag || else_flag) {
      performAction(action.trim());
    } else {
      System.out.println("performing action");
      performAction(action);
    }
  }
  private static void handleResetRequest(HttpExchange exchange) throws IOException {
    Debugger.TELEPORT_TO_START();

    PositionComponent pc = getHeroPosition();
    String response = pc.position().x + "," + pc.position().y;

    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    exchange.sendResponseHeaders(200, response.getBytes().length);
    OutputStream os = exchange.getResponseBody();
    os.write(response.getBytes());
    os.close();
  }

  /**
   * Get the actual value from an expression. This is either the value itself or the expression contains a variable.
   * Return the value of the variable in this case.
   * @param value Value of the expression
   * @return Returns the value as an integer
   */
  private static int getActualValueFromExpression(String value) {
    if (variables.get(value) == null) {
      return Integer.parseInt(value);
    } else {
      return variables.get(value);
    }
  }

  /**
   * Execute an expression with the given values and operator
   * @param leftValue Left value of the expression
   * @param rightValue Right value of the expression
   * @param op Operator of the expression
   * @return Returns the result of the expression
   */
  private static int executeExpression(int leftValue, int rightValue, String op) {
    switch (op) {
      case "+":
        return leftValue + rightValue;
      case "-":
        return leftValue - rightValue;
      case "*":
        return leftValue * rightValue;
      case "/":
        return leftValue / rightValue;
      default:
        return 0;
    }
  }

  /**
   * Evaluation if we currently have a variable assignment
   * @param action Currently executed action
   */
  private static void variableEvaluation(String action) {
    Pattern pattern = Pattern.compile("int (\\w+) = (\\d+)");
    Matcher matcher = pattern.matcher(action);
    // If pattern matches we have a new variable
    if (matcher.find()) {
      variables.put(matcher.group(1), Integer.parseInt(matcher.group(2)));
    }
    Pattern patternAssign = Pattern.compile("(\\w+) = (\\w+) (\\+|-|\\*|/) (\\w+)");
    Matcher matcherAssign = patternAssign.matcher(action);
    // If pattern matches we have an assignment to an already existing variable
    if (matcherAssign.find()) {
      String varName = matcherAssign.group(1);
      // Get left value and right value
      int leftValue = getActualValueFromExpression(matcherAssign.group(2));
      int rightValue = getActualValueFromExpression(matcherAssign.group(4));
      String op = matcherAssign.group(3);
      // Update value of expression
      variables.put(varName, executeExpression(leftValue, rightValue, op));
    }
  }
  private static void whileEvaluation(String action) {
    Pattern pattern = Pattern.compile("solange \\((.*)\\)");
    if (action.equals("}") && !active_scopes.isEmpty() && active_scopes.peek().equals("while")) {
      String currentCondition = while_conditions.peek();
      // Complex eval is not necessary here, because we only allow wall left/right etc. as condition at the moment
      current_while_cond_negative = !evalComplexCondition(currentCondition, pattern);
      if (!current_while_cond_negative) {
        while_is_repeating.set(while_is_repeating.size() - 1, true);
      } else {
        while_is_repeating.set(while_is_repeating.size() - 1, false);
        active_scopes.pop();
        whileBodys.remove(whileBodys.size() - 1);
        current_while_cond_negative = false;
      }
      return;
    }
    if (action.contains("solange") && !current_while_cond_negative) {
      active_scopes.push("while");
      // Complex eval is not necessary here, because we only allow wall left/right etc. as condition at the moment
      current_while_cond_negative = !evalComplexCondition(action, pattern);
      while_conditions.push(action);
      whileBodys.add(new ArrayList<>());
      while_is_repeating.add(false);
    }
  }
  private static void ifEvaluation(String action) {
    if (action.equals("}") && !active_scopes.isEmpty() && active_scopes.peek().equals("if")) {
      if_flag = false;
      else_flag = false;
      active_scopes.pop();
      return;
    }
    if (action.contains("falls")) {
      active_scopes.push("if");
      Pattern pattern = Pattern.compile("falls \\((.*)\\)");
      if_flag = evalComplexCondition(action, pattern);
    }

    if (action.contains("sonst")) {
      else_flag = !if_flag;
      if_flag = false;
    }
  }

  /**
   * Check if the given string contains at least one string of a list
   * @param inputString String that will be tested
   * @param items List of strings that will be tested against the input string
   * @return Returns true if input string contains at least on string that is given in items. Otherwise, return false
   */
  private static boolean containsItemFromArray(String inputString, String[] items) {
    return Arrays.stream(items).anyMatch(inputString::contains);
  }

  /**
   * Eval a complex condition. This function is used when evaluating if or while conditions.
   * @param action String containing the condition
   * @param pattern Regex to extract only the condition from the action string
   * @return Returns true or false depending on the result of the condition
   */

  private static boolean evalComplexCondition(String action, Pattern pattern) {
    Matcher matcher = pattern.matcher(action);
    String condition = "";
    boolean retValue = false;
    if (matcher.find()) {
      condition = matcher.group(1);
      // There may be multiple conditions in one condition. We need to split it.
      // We split at each opening "(" without an immediate following ")"
      String[] splitted_condition = condition.split("\\((?=[^\\)])");
      String lastLogicOp = "";

      for (String currentCondition : splitted_condition) {
        // Skip if condition string is empty
        if (currentCondition.isEmpty()) {
          continue;
        }
        // Get left and right condition with regex
        Pattern condPattern = Pattern.compile("(.+?)(&&|\\|\\|)(.+)");
        Matcher condMatcher = condPattern.matcher(currentCondition);
        if (condMatcher.find()) {
          String leftCondition = condMatcher.group(1);
          String rightCondition = condMatcher.group(3);
          String logicOperator = condMatcher.group(2);

          // Eval res with logic operator
          boolean result = evalLogicOp(evalCondition(leftCondition), evalCondition(rightCondition), logicOperator);
          // Check if there is a logic operator from last result and check current result with last result
          if (!lastLogicOp.isEmpty()) {
            result = evalLogicOp(result, retValue, lastLogicOp);
          }
          // Right condition might contain a logic operator that needs to be used for eval of current result and next
          // result
          if (rightCondition.contains("||")) {
            lastLogicOp = "||";
          } else if (rightCondition.contains("&&")) {
            lastLogicOp = "&&";
          } else {
            lastLogicOp = "";
          }
          retValue = result;
        } else {
          retValue = evalCondition(currentCondition);
        }
      }
    }
    return retValue;
  }
  private static boolean evalLogicOp(Boolean leftCondition, Boolean rightCondition, String op) {
    switch(op) {
      case "&&":
        return leftCondition && rightCondition;
      case "||":
        return leftCondition || rightCondition;
      default:
        return false;
    }
  }
  private static boolean evalCondition(String condition) {
    if (condition.contains("wahr")) {
      return true;
    }
    if (condition.contains("naheWand()")) {
      return isNearWall();
    }
    if (condition.contains("WandOben()")) {
      return isNearWallUp();
    }
    if (condition.contains("WandUnten()")) {
      return isNearWallDown();
    }
    if (condition.contains("WandLinks()")) {
      return isNearWallLeft();
    }
    if (condition.contains("WandRechts()")) {
      return isNearWallRight();
    }
    String[] ops = {"==", "!=", "<", ">", "<=", ">="};
    if (containsItemFromArray(condition, ops)) {
      return evalCompareCondition(condition);
    }
    return false;
  }

  /**
   * Evaluate the given condition
   * @param action String that contains the condition.
   * @return Return true or false depending on the condition.
   */
  private static boolean evalCompareCondition(String action) {
    Pattern pattern = Pattern.compile("(\\w+)\\s(<|>|==|!=|<=|>=)\\s(\\w+)");
    Matcher matcher = pattern.matcher(action);
    if (matcher.find()) {
      int input_a = Integer.parseInt(matcher.group(1));
      int input_b =  Integer.parseInt(matcher.group(3));
      String op = matcher.group(2);
      switch (op) {
        case "==":
          return input_a == input_b;
        case "!=":
          return input_a != input_b;
        case "<=":
          return input_a <= input_b;
        case "<":
          return input_a < input_b;
        case ">=":
          return input_a >= input_b;
        case ">":
          return input_a > input_b;
        default:
          return false;
      }
    }
    return false;
  }

  private static void performAction(String action) {
    switch (action) {
      case "oben();" -> up();
      case "unten();" -> down();
      case "links();" -> left();
      case "rechts();" -> right();
        // case "interagieren();" -> interact();
      case "feuerballOben();" -> fireballUp();
      case "feuerballUnten();" -> fireballDown();
      case "feuerballLinks();" -> fireballLeft();
      case "feuerballRechts();" -> fireballRight();
    }
  }

  private static void up() {
    VelocityComponent vc =
        hero.fetch(VelocityComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, VelocityComponent.class));
    vc.currentYVelocity(1 * vc.yVelocity());

    waitDelta();
  }

  private static void down() {
    VelocityComponent vc =
        hero.fetch(VelocityComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, VelocityComponent.class));
    vc.currentYVelocity(-1 * vc.yVelocity());

    waitDelta();
  }

  private static void left() {
    VelocityComponent vc =
        hero.fetch(VelocityComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, VelocityComponent.class));
    vc.currentXVelocity(-1 * vc.xVelocity());

    waitDelta();
  }

  private static void right() {
    VelocityComponent vc =
        hero.fetch(VelocityComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, VelocityComponent.class));
    vc.currentXVelocity(1 * vc.xVelocity());

    waitDelta();
  }

  private static void waitDelta() {
    long timeout = (long) (Gdx.graphics.getDeltaTime() * 1000);
    try {
      TimeUnit.MILLISECONDS.sleep(timeout - 1);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public static boolean isNearWall() {
    boolean isNearWall;

    isNearWall = isNearWallUp() || isNearWallDown() || isNearWallLeft() || isNearWallRight();
    return isNearWall;
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public static boolean isNearWallUp() {
    boolean isNearWallUp;

    PositionComponent pc = getHeroPosition();
    Point newPositionUp = new Point(pc.position().x, pc.position().y + 0.3f);

    isNearWallUp = !Game.tileAT(newPositionUp).isAccessible();
    return isNearWallUp;
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public static boolean isNearWallDown() {
    boolean isNearWallDown;

    PositionComponent pc = getHeroPosition();
    Point newPositionDown = new Point(pc.position().x, pc.position().y - 0.3f);

    isNearWallDown = !Game.tileAT(newPositionDown).isAccessible();
    return isNearWallDown;
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public static boolean isNearWallLeft() {
    boolean isNearWallLeft;

    PositionComponent pc = getHeroPosition();
    Point newPositionLeft = new Point(pc.position().x - 0.3f, pc.position().y);

    isNearWallLeft = !Game.tileAT(newPositionLeft).isAccessible();
    return isNearWallLeft;
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public static boolean isNearWallRight() {
    boolean isNearWallRight;

    PositionComponent pc = getHeroPosition();
    Point newPositionRight = new Point(pc.position().x + 0.3f, pc.position().y);

    isNearWallRight = !Game.tileAT(newPositionRight).isAccessible();
    return isNearWallRight;
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public static PositionComponent getHeroPosition() {
    return hero.fetch(PositionComponent.class)
        .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));
  }

  /** WTF? . */
  public static void fireballUp() {
    Skill fireball =
        new Skill(
            new FireballSkill(
                new Supplier<Point>() {
                  @Override
                  public Point get() {
                    Point heroPoint =
                        new Point(getHeroPosition().position().x, getHeroPosition().position().y);
                    heroPoint.y += 1;
                    return heroPoint;
                  }
                }),
            1);
    fireball.execute(hero);
    waitDelta();
  }

  /** WTF? . */
  public static void fireballDown() {
    Skill fireball =
        new Skill(
            new FireballSkill(
                new Supplier<Point>() {
                  @Override
                  public Point get() {
                    Point heroPoint =
                        new Point(getHeroPosition().position().x, getHeroPosition().position().y);
                    heroPoint.y -= 1;
                    return heroPoint;
                  }
                }),
            1);
    fireball.execute(hero);
    waitDelta();
  }

  /** WTF? . */
  public static void fireballLeft() {
    Skill fireball =
        new Skill(
            new FireballSkill(
                new Supplier<Point>() {
                  @Override
                  public Point get() {
                    Point heroPoint =
                        new Point(getHeroPosition().position().x, getHeroPosition().position().y);
                    heroPoint.x -= 1;
                    return heroPoint;
                  }
                }),
            1);
    fireball.execute(hero);
    waitDelta();
  }

  /** WTF? . */
  public static void fireballRight() {
    Skill fireball =
        new Skill(
            new FireballSkill(
                new Supplier<Point>() {
                  @Override
                  public Point get() {
                    Point heroPoint =
                        new Point(getHeroPosition().position().x, getHeroPosition().position().y);
                    heroPoint.x += 1;
                    return heroPoint;
                  }
                }),
            1);
    fireball.execute(hero);
    waitDelta();
  }

  /*public static void interact() {
      InteractionTool.interactWithClosestInteractable(hero);
      waitDelta();
  }*/
}
