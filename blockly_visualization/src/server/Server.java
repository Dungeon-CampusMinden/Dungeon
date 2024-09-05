package server;

import antlr.blocklyConditionVisitor;
import antlr.blocklyLexer;
import antlr.blocklyParser;
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
import nodes.StartNode;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

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
  private static boolean if_active = false;

  private static final ArrayList<Boolean> while_is_repeating = new ArrayList<>();
  private static boolean current_while_cond_negative = false;
  private static final ArrayList<ArrayList<String>> whileBodys = new ArrayList<>();
  private static final Stack<String> while_conditions = new Stack<>();

  // This variable holds all active scopes in a stack. The value at the top of the stack is the current scope.
  // It can hold the following values: if, while.
  private static final Stack<String> active_scopes = new Stack<>();
  // This is public, so we can easily access it in the blocklyConditionVisitor
  public static final HashMap<String, Variable> variables = new HashMap<>();

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
      System.out.print("Current action: ");
      System.out.println(action);
      processAction(action);
      System.out.println("Current scopes");
      System.out.println(active_scopes);
      System.out.println("Current while is repeating values");
      System.out.println(while_is_repeating);
      System.out.println("Current variables");
      System.out.println(variables);

      // Repeat statements of while loop as long as while flag is set
      if (!active_scopes.isEmpty() && whileBodys.size() > 0) {
        while (while_is_repeating.get(while_is_repeating.size() - 1)) {
          for (String whileAction : whileBodys.get(whileBodys.size() - 1)) {
            processAction(whileAction);
            System.out.println("in while action");
            System.out.println(whileAction);
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
    if (!active_scopes.isEmpty() && whileBodys.size() > 0) {
      // We must add action to all currently active while loops
      for (int i = 0; i < whileBodys.size(); i++) {
        if (!while_is_repeating.get(i)) {
          whileBodys.get(i).add(action.trim());
        }
      }
    }
    // Make sure we close the right scope
    if (action.trim().equals("}") && !active_scopes.isEmpty()) {
      System.out.println("End of if or while detected");
      String current_scope = active_scopes.peek();
      if (current_scope.equals("if")) {
        System.out.println("eval if cond");
        ifEvaluation(action.trim());
        return;
      } else if (current_scope.equals("while")) {
        System.out.println("eval while loop");
        whileEvaluation(action.trim());
        return;
      }
    }
    ifEvaluation(action);
    whileEvaluation(action);
    variableEvaluation(action);

    // Do not perform any actions if current while condition is false
    if (current_while_cond_negative) {
      return;
    }
    // Do not perform action if currently in if and condition is false
    if (if_active && !if_flag && !else_flag) {
      return;
    }

    if (if_flag || else_flag) {
      performAction(action.trim());
    } else if (!active_scopes.isEmpty() && active_scopes.peek().equals("while")) {
      performAction(action.trim());
    } else {
      performAction(action);
    }
  }
  private static void handleResetRequest(HttpExchange exchange) throws IOException {
    // Reset values
    if_flag = false;
    if_active = false;
    else_flag = false;
    current_while_cond_negative = false;
    while_is_repeating.clear();
    whileBodys.clear();
    while_conditions.clear();
    active_scopes.clear();
    variables.clear();
    Debugger.TELEPORT_TO_START();

    PositionComponent pc = getHeroPosition();
    String response = pc.position().x + "," + pc.position().y;

    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    exchange.sendResponseHeaders(200, response.getBytes().length);
    OutputStream os = exchange.getResponseBody();
    os.write(response.getBytes());
    os.close();


  }

  private static Variable getArrayVariable(String varName) throws IllegalAccessException {
    Variable array_var = variables.get(varName);
    // Throw exception if nothing found
    if (array_var == null) {
      throw new NoSuchElementException(String.format("Variable not found %s", varName));
    }
    if (!array_var.type.equals("array")) {
      throw new IllegalAccessException(
        String.format("Expected array variable. Got %s for variable %s", array_var.type, varName)
      );
    }
    return array_var;
  }

  /**
   * Get the actual value from an expression. This might be the value itself or the expression contains a variable.
   * Return the value of the variable or the value of an array expression if given.
   * Return the value of the variable in this case.
   * @param value Value of the expression
   * @return Returns the value as an integer
   */
  private static int getActualValueFromExpression(String value) throws IllegalAccessException {
    // Process array access
    Pattern pattern = Pattern.compile("(\\w+)\\[(\\d+)]");
    Matcher matcher = pattern.matcher(value);
    if (matcher.find()) {
      System.out.println("Array access detected");
      Variable array_var = getArrayVariable(matcher.group(1));
      int index = Integer.parseInt(matcher.group(2));
      return array_var.arrayVal[index];
    }
    // We might have to return the array length
    Pattern patternArraySize = Pattern.compile("(\\w+)\\.length");
    Matcher matcherArraySize = patternArraySize.matcher(value);
    if (matcherArraySize.find()) {
      System.out.println("Array .length detected");
      Variable array_var = getArrayVariable(matcherArraySize.group(1));
      return array_var.arrayVal.length;
    }
    // Process usual values
    if (variables.get(value) == null) {
      return Integer.parseInt(value);
    }
    // Process int variable access
    Variable var = variables.get(value);
    if (var.type.equals("base")) {
      return var.intVal;
    }
    throw new IllegalAccessException(
      String.format("Expected base variable. Got %s for variable %s", var.type, value)
    );
  }

  /**
   * Execute an expression with the given values and operator
   * @param leftValue Left value of the expression
   * @param rightValue Right value of the expression
   * @param op Operator of the expression
   * @return Returns the result of the expression
   */
  private static int executeExpression(int leftValue, int rightValue, String op) {
    return switch (op) {
      case "+" -> leftValue + rightValue;
      case "-" -> leftValue - rightValue;
      case "*" -> leftValue * rightValue;
      case "/" -> leftValue / rightValue;
      default -> 0;
    };
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
      variables.put(matcher.group(1), new Variable(Integer.parseInt(matcher.group(2))));
      return;
    }
    // We may have a creation of an array
    Pattern patternArray = Pattern.compile("int\\[] (\\w+) = new int\\[(\\d+)]");
    Matcher matcherArray = patternArray.matcher(action);
    if (matcherArray.find()) {
      int array_size = Integer.parseInt(matcherArray.group(2));
      variables.put(matcherArray.group(1), new Variable(new int[array_size]));
      return;
    }

    // We might have an assignment to an already existing variable
    // Check expression with operator
    Pattern patternAssign = Pattern.compile("(\\w+) = (\\w+(\\[\\d+])?(\\.length)?) (\\+|-|\\*|/) (\\w+(\\[\\d+])?(\\.length)?)");
    int leftGroup = 2;
    int rightGroup = 6;
    int varNameGroup = 1;
    int opGroup = 5;
    if (checkOperatorExpression(action, patternAssign, leftGroup, rightGroup, varNameGroup, opGroup)) return;

    // Check single right value
    Pattern patternAssignRightValue = Pattern.compile("(\\w+) = (\\w+(\\[\\d+])?(\\.length)?)");
    int varNameGroup_RightValue = 1;
    int valueGroup = 2;
    if (checkRightValueExpression(action, patternAssignRightValue, varNameGroup_RightValue, valueGroup)) return;
  }

  private static boolean checkRightValueExpression(String action, Pattern patternAssignRightValue, int varNameGroup_RightValue, int valueGroup) {
    Matcher matcherAssignRightValue = patternAssignRightValue.matcher(action);
    if (matcherAssignRightValue.find()) {
      String varName = matcherAssignRightValue.group(varNameGroup_RightValue);
      int value;
      try {
        value = getActualValueFromExpression(matcherAssignRightValue.group(valueGroup));
      } catch (IllegalAccessException | NoSuchElementException e) {
        System.out.println(e.getMessage());
        return true;
      }
      variables.put(varName, new Variable(value));
      return true;
    }
    return false;
  }

  private static boolean checkOperatorExpression(String action, Pattern patternAssign, int leftGroup, int rightGroup, int varNameGroup, int opGroup) {

    Matcher matcherAssign = patternAssign.matcher(action);
    if (matcherAssign.find()) {
      String varName = matcherAssign.group(varNameGroup);
      // Get left and right value
      int leftValue;
      int rightValue;
      try {
        leftValue = getActualValueFromExpression(matcherAssign.group(leftGroup));
        rightValue = getActualValueFromExpression(matcherAssign.group(rightGroup));
      } catch (IllegalAccessException | NoSuchElementException e) {
        System.out.println(e.getMessage());
        return true;
      }
      // Get operator
      String op = matcherAssign.group(opGroup);
      // Update value of expression
      variables.put(varName, new Variable(executeExpression(leftValue, rightValue, op)));
      return true;
    }
    return false;
  }

  private static void whileEvaluation(String action) {
    Pattern pattern = Pattern.compile("solange \\((.*)\\)");
    if (action.equals("}") && !active_scopes.isEmpty() && active_scopes.peek().equals("while")) {
      String currentCondition = while_conditions.peek();
      // Complex eval is not necessary here, because we only allow wall left/right etc. as condition at the moment
      current_while_cond_negative = !evalComplexCondition(currentCondition, pattern);
      if (!current_while_cond_negative) {
        // Repeat the loop
        System.out.println("Starting the loop");
        while_is_repeating.set(while_is_repeating.size() - 1, true);
      } else {
        System.out.println("Ending the loop");
        // End the loop
        while_is_repeating.remove(while_is_repeating.size() - 1);
        active_scopes.pop();
        whileBodys.remove(whileBodys.size() - 1);
        while_conditions.pop();
        current_while_cond_negative = false;
      }
      return;
    }
    if (action.contains("solange") && !current_while_cond_negative) {
      // Start the loop
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
      if_active = false;
      active_scopes.pop();
      return;
    }
    if (action.contains("falls")) {
      if_active = true;
      active_scopes.push("if");
      Pattern pattern = Pattern.compile("falls \\((.*)\\)");
      if_flag = evalComplexCondition(action, pattern);
      System.out.print("IF condition result: ");
      System.out.println(if_flag);
    }

    if (action.contains("sonst")) {
      else_flag = !if_flag;
      if_flag = false;
    }
  }

  private static boolean evalComplexCondition(String action, Pattern pattern) {
    Matcher matcher = pattern.matcher(action);
    if (matcher.find()) {
      blocklyLexer lexer = new blocklyLexer(CharStreams.fromString(matcher.group(1)));
      CommonTokenStream tokens = new CommonTokenStream(lexer);
      blocklyParser parser = new blocklyParser(tokens);

      ParseTree tree = parser.start();
      blocklyConditionVisitor eval = new blocklyConditionVisitor();
      StartNode ast = (StartNode) eval.visit(tree);

      return ast.getBoolValue();
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
