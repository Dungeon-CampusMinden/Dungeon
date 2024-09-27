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
import entities.VariableHUD;
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

  // This variable holds all active scopes in a stack. The value at the top of the stack is the current scope.
  // It can hold the following values: if, while, repeat, function.
  public static final Stack<String> active_scopes = new Stack<>();
  // This is public, so we can easily access it in the blocklyConditionVisitor
  public static final HashMap<String, Variable> variables = new HashMap<>();
  public static final HashMap<String, FuncStats> functions = new HashMap<>();

  public static final Stack<RepeatStats> active_repeats = new Stack<>();
  public static final Stack<WhileStats> active_whiles = new Stack<>();
  public static final Stack<IfStats> active_ifs = new Stack<>();
  public static final Stack<FuncStats> active_func_defs = new Stack<>();

  public static boolean interruptExecution = false;
  public static boolean errorOccurred = false;
  public static String errorMsg = "";

  private static final String[] reservedFunctions = {
    "oben",
    "unten",
    "links",
    "rechts",
    "feuerballOben",
    "feuerballUnten",
    "feuerballLinks",
    "feuerballRechts"
  };

  private static final Stack<String> currently_repeating_scope = new Stack<>();

  public static VariableHUD variableHUD = null;

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
    HttpContext clearContext = server.createContext("/clear");
    clearContext.setHandler(Server::handleClearRequest);
    server.start();
  }

  private static void handleStartRequest(HttpExchange exchange) throws IOException {
    InputStream inStream = exchange.getRequestBody();
    String text = new String(inStream.readAllBytes(), StandardCharsets.UTF_8);

    String[] actions = text.split("\n");

    String errAction = null;
    for (String action : actions) {
      action = action.trim();
      processAction(action);
      if (interruptExecution) {
        errAction = action;
        break;
      }

    }
    // Build response for blockly frontend
    String response;
    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    if (interruptExecution) {
      System.out.println("Interruption performed");
      if (errorOccurred) {
        String msg = "Anweisung: " + errAction + "\n";
        msg += "Fehlermeldung: " + errorMsg;
        response = msg;
        exchange.sendResponseHeaders(400, response.getBytes().length);
      } else {
        response = "Execution interrupted";
        exchange.sendResponseHeaders(205, response.getBytes().length);
      }
      clearGlobalValues();
    } else {
      response = "OK";
      exchange.sendResponseHeaders(200, response.getBytes().length);
    }

    OutputStream os = exchange.getResponseBody();
    os.write(response.getBytes());
    os.close();
  }

  private static void repeatHook(){
    if (!currently_repeating_scope.isEmpty()) {
      String currentLoop = currently_repeating_scope.peek();
      switch (currentLoop) {
        case "while" -> {
          if (!active_func_defs.isEmpty()) {
            active_whiles.pop();
            active_scopes.pop();
            currently_repeating_scope.pop();
            return;
          }
          WhileStats currentWhile = active_whiles.peek();
          while (currentWhile.isRepeating && !interruptExecution) {
            System.out.print("Repeating while loop");
            System.out.println(currentWhile);
            for (String whileAction : currentWhile.whileBody) {
              System.out.println("[In while loop]");
              processAction(whileAction);
              if (interruptExecution) {
                break;
              }
            }
          }
        }
        case "repeat" -> {
          if (!active_func_defs.isEmpty()) {
            active_repeats.pop();
            active_scopes.pop();
            currently_repeating_scope.pop();
            return;
          }
          RepeatStats currentRepeat = active_repeats.peek();
          while (currentRepeat.isRepeating && !interruptExecution) {
            System.out.print("Repeating repeat loop");
            System.out.println(currentRepeat);
            for (String repeatAction : currentRepeat.repeatBody) {
              System.out.println("[In repeat loop]");
              processAction(repeatAction);
              if (interruptExecution) {
                break;
              }
            }
          }
        }
        default -> System.out.println("Unknown repeating scope");
      }
    }
  }

  public static void processAction(String action) {
    System.out.print("Processing action: ");
    System.out.println(action);
    // Make sure we close the right scope
    addActionToWhileBody(action);
    addActionToRepeatBody(action);
    addActionToFunc(action);
    if (action.equals("}") && !active_scopes.isEmpty()) {
      System.out.println("End of if, while or repeat detected");
      String current_scope = active_scopes.peek();
      switch (current_scope) {
        case "if" -> {
          System.out.println("eval if cond");
          ifEvaluation(action);
          System.out.print("Scopes after eval: ");
          System.out.println(active_scopes);
          return;
        }
        case "while" -> {
          System.out.println("eval while loop");
          boolean whileIsRepeating = closeWhile(action);
          System.out.print("Scopes after eval: ");
          System.out.println(active_scopes);
          if (whileIsRepeating) {
            repeatHook();
          }
          return;
        }
        case "repeat" -> {
          System.out.println("eval repeat loop");
          boolean repeatIsRepeating = closeRepeat(action);
          System.out.print("Scopes after eval: ");
          System.out.println(active_scopes);
          if (repeatIsRepeating) {
            repeatHook();
          }
          return;
        }
        case "function" -> {
          System.out.println("eval function");
          closeFunc(action);
          System.out.print("Scopes after eval: ");
          System.out.println(active_scopes);
          return;
        }
      }
    }

    ifEvaluation(action);
    whileEvaluation(action);
    repeatEvaluation(action);
    funcEvaluation(action);

    // Do not perform any actions in func definition
    if (!active_func_defs.isEmpty()) {
      return;
    }
    // Do not perform any actions if current while condition is false
    if (!active_whiles.isEmpty() && !active_whiles.peek().conditionResult) {
      return;
    }
    // Do not perform any actions if current if condition is false
    if (!active_ifs.isEmpty() && !active_ifs.peek().executeAction()) {
      return;
    }

    // Variable and func call evaluation are not allowed to be actually performed if while or if condition is false or
    // a function is currently defined
    variableEvaluation(action);
    funcCallEvaluation(action);

    printScopes();

    performAction(action);

  }

  private static void printScopes() {
    System.out.print("Current scopes: ");
    System.out.println(active_scopes);
    System.out.print("Currently repeating scopes: ");
    System.out.println(currently_repeating_scope);
    System.out.print("Current variables: ");
    System.out.println(variables);
    System.out.print("Available functions: ");
    System.out.println(functions);
  }

  private static void handleResetRequest(HttpExchange exchange) throws IOException {
    // Reset values
    interruptExecution = true;

    Debugger.TELEPORT_TO_START();

    PositionComponent pc = getHeroPosition();
    String response = pc.position().x + "," + pc.position().y;

    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    exchange.sendResponseHeaders(200, response.getBytes().length);
    OutputStream os = exchange.getResponseBody();
    os.write(response.getBytes());
    os.close();
  }

  private static void handleClearRequest(HttpExchange exchange) throws IOException {
    clearGlobalValues();

    PositionComponent pc = getHeroPosition();
    String response = pc.position().x + "," + pc.position().y;

    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    exchange.sendResponseHeaders(200, response.getBytes().length);
    OutputStream os = exchange.getResponseBody();
    os.write(response.getBytes());
    os.close();
  }

  public static void clearGlobalValues() {
    // Reset values
    active_scopes.clear();
    currently_repeating_scope.clear();
    active_ifs.clear();
    active_whiles.clear();
    active_repeats.clear();
    active_func_defs.clear();
    variables.clear();
    functions.clear();
    interruptExecution = false;
    errorOccurred = false;
    errorMsg = "";
    System.out.println("Values cleared");
    printScopes();
  }

  private static void setError(String errMsg) {
    interruptExecution = true;
    errorOccurred = true;
    errorMsg = errMsg;
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
  public static int getActualValueFromExpression(String value) throws IllegalAccessException {
    // Process array access
    Pattern pattern = Pattern.compile("(\\w+)\\[(\\d+)]");
    Matcher matcher = pattern.matcher(value);
    if (matcher.find()) {
      Variable array_var = getArrayVariable(matcher.group(1));
      int index = Integer.parseInt(matcher.group(2));
      return array_var.arrayVal[index];
    }
    // We might have to return the array length
    Pattern patternArraySize = Pattern.compile("(\\w+)\\.length");
    Matcher matcherArraySize = patternArraySize.matcher(value);
    if (matcherArraySize.find()) {
      Variable array_var = getArrayVariable(matcherArraySize.group(1));
      return array_var.arrayVal.length;
    }
    // Process usual values
    if (variables.get(value) == null) {
      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException e) {
        throw new NumberFormatException(value + " is not a number or variable");
      }

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

  private static void funcCallEvaluation(String action) {
    Pattern pattern = Pattern.compile("(\\w+)\\(\\)");
    Matcher matcher = pattern.matcher(action);
    if (matcher.find()) {
      String funcName = matcher.group(1);
      if (Arrays.asList(reservedFunctions).contains(funcName)) {
        return;
      }
      FuncStats calledFunc = functions.get(funcName);
      if (calledFunc == null) {
        System.out.println("Could not find function " + funcName);
        return;
      }
      System.out.println("Executing function" + funcName);
      for (String funcAction: calledFunc.funcBody) {
        System.out.println("[In function " + funcName + "]");
        processAction(funcAction);
      }
    }

  }
  private static void closeFunc(String action) {
    if (action.equals("}") && active_scopes.peek().equals("function")) {
      FuncStats finishedFunc = active_func_defs.pop();
      active_scopes.pop();
      functions.put(finishedFunc.name, finishedFunc);
    }
  }
  private static void addActionToFunc(String action){
    if (!active_func_defs.isEmpty()) {
      for (FuncStats func: active_func_defs) {
        func.funcBody.add(action);
      }
    }
  }

  private static void funcEvaluation(String action) {
    Pattern pattern = Pattern.compile("public void (\\w+)\\(\\)");
    Matcher matcher = pattern.matcher(action);
    // If pattern matches we have a new func definition
    if (matcher.find()) {
      active_func_defs.push(new FuncStats(matcher.group(1)));
      active_scopes.push("function");
    }
  }

  private static void addBaseVar(String name, int value) {
    variables.put(name, new Variable(value));
    if (variableHUD != null) {
      variableHUD.addVariable(name, value);
    }
  }
  /**
   * Evaluation if we currently have a variable assignment
   * @param action Currently executed action
   */
  private static void variableEvaluation(String action) {
    // Check array creation
    Pattern patternArray = Pattern.compile("int\\[] (\\w+) = new int\\[(\\d+)]");
    Matcher matcherArray = patternArray.matcher(action);
    if (matcherArray.find()) {
      int array_size = Integer.parseInt(matcherArray.group(2));
      variables.put(matcherArray.group(1), new Variable(new int[array_size]));
      return;
    }
    // Check array assign
    if (checkArrayAssign(action)) return;
    // Check assign to normal variable with expression
    if (checkAssign(action)) return;

    // Simple assign
    Pattern pattern = Pattern.compile("int (\\w+) = (\\d+);");
    Matcher matcher = pattern.matcher(action);
    // If pattern matches we have a new variable
    if (matcher.find()) {
      addBaseVar(matcher.group(1), Integer.parseInt(matcher.group(2)));
    }
  }

  private static boolean checkAssign(String action) {
    // Check expression with operator
    Pattern pattern = Pattern.compile(
      "int (\\w+) = (\\w+(\\[\\d+])?(\\.length)?) (\\+|-|\\*|/) (\\w+(\\[\\d+])?(\\.length)?)"
    );
    Matcher matcher = pattern.matcher(action);
    if (matcher.find()) {
      String leftVal = matcher.group(2);
      String rightVal =  matcher.group(6);
      String varName = matcher.group(1);
      String op = matcher.group(5);
      try {
        int value = executeOperatorExpression(leftVal, rightVal, op);
        addBaseVar(varName, value);
        return true;
      } catch (IllegalAccessException | NoSuchElementException | IndexOutOfBoundsException | NumberFormatException e) {
        System.out.println(e.getMessage());
        setError(e.getMessage());
        return true;
      }
    }

    // Check single right value
    Pattern patternRightValue = Pattern.compile("int (\\w+) = (\\w+(\\[\\d+])?(\\.length)?)");
    Matcher matcherRightValue = patternRightValue.matcher(action);
    if (matcherRightValue.find()) {
      String varNameRightValue = matcherRightValue.group(1);
      String rightValue = matcherRightValue.group(2);
      try {
        int value = getActualValueFromExpression(rightValue);
        addBaseVar(varNameRightValue, value);
        return true;
      } catch (IllegalAccessException | NoSuchElementException | IndexOutOfBoundsException | NumberFormatException e) {
        System.out.println(e.getMessage());
        setError(e.getMessage());
        return true;
      }
    }
    return false;
  }

  private static boolean checkArrayAssign(String action) {
    Pattern pattern = Pattern.compile(
      "((\\w+)\\[(\\d+)]) = (\\w+(\\[\\d+])?(\\.length)?) (\\+|-|\\*|/) (\\w+(\\[\\d+])?(\\.length)?)"
    );
    Matcher matcher = pattern.matcher(action);
    if (matcher.find()) {
      int index = Integer.parseInt(matcher.group(3));
      String leftVal = matcher.group(4);
      String rightVal =  matcher.group(8);
      String varName = matcher.group(2);
      String op = matcher.group(7);
      try {
        int value = executeOperatorExpression(leftVal, rightVal, op);
        Variable arrayVar = getArrayVariable(varName);
        arrayVar.arrayVal[index] = value;
        return true;
      } catch (IllegalAccessException | NoSuchElementException | IndexOutOfBoundsException | NumberFormatException e) {
        System.out.println(e.getMessage());
        setError(e.getMessage());
        return true;
      }
    }

    // Check single right value
    Pattern patternRightValue = Pattern.compile("((\\w+)\\[(\\d+)]) = (\\w+(\\[\\d+])?(\\.length)?)");
    Matcher matcherRightValue = patternRightValue.matcher(action);
    if (matcherRightValue.find()) {
      String varNameRightValue = matcherRightValue.group(2);
      int indexRightValue = Integer.parseInt(matcherRightValue.group(3));
      String rightValue = matcherRightValue.group(4);
      try {
        int value = getActualValueFromExpression(rightValue);
        Variable arrayVar = getArrayVariable(varNameRightValue);
        arrayVar.arrayVal[indexRightValue] = value;
        return true;
      } catch (IllegalAccessException | NoSuchElementException | IndexOutOfBoundsException | NumberFormatException e) {
        System.out.println(e.getMessage());
        setError(e.getMessage());
        return true;
      }
    }
    // Regex did not match. Return false in this case
    return false;
  }

  private static int executeOperatorExpression(String leftVal, String rightVal, String op) throws IllegalAccessException {
    // Get left and right value
    int leftValue = getActualValueFromExpression(leftVal);
    int rightValue = getActualValueFromExpression(rightVal);

    return executeExpression(leftValue, rightValue, op);

  }

  private static boolean closeWhile(String action) {
    Pattern pattern = Pattern.compile("solange \\((.*)\\)");
    // Check if loop must be ended
    if (action.equals("}") && active_scopes.peek().equals("while")) {
      WhileStats currentWhile = active_whiles.peek();
      String condition = currentWhile.condition;
      boolean conditionResult = evalComplexCondition(condition, pattern);
      if (conditionResult) {
        if (!currentWhile.isRepeating) {
          currentWhile.isRepeating = true;
          currently_repeating_scope.push("while");
          return true;
        }
      } else {
        System.out.println("Ending while loop");
        if (currentWhile.isRepeating) {
          currentWhile.isRepeating = false;
          currently_repeating_scope.pop();
        }
        active_scopes.pop();
        active_whiles.pop();
        return false;
      }
    }
    return false;
  }

  private static void addActionToWhileBody(String action) {
    if (!active_whiles.isEmpty()) {
      if (active_whiles.peek().isRepeating) {
        return;
      }
      for (WhileStats whileLoop: active_whiles) {
        if (!whileLoop.isRepeating) {
          whileLoop.whileBody.add(action);
        }
      }
    }
  }
  private static void whileEvaluation(String action) {
    Pattern pattern = Pattern.compile("solange \\((.*)\\)");

    if (action.contains("solange")) {
      boolean currentConditionResult = evalComplexCondition(action, pattern);
      active_scopes.push("while");
      active_whiles.push(new WhileStats(action, currentConditionResult));
    }
  }

  private static boolean closeRepeat(String action) {
    // Check if repeat loop must end
    if (action.equals("}") && active_scopes.peek().equals("repeat")) {
      RepeatStats currentRepeat = active_repeats.peek();
      if (currentRepeat.evalRepeatComplete()) {
        if (currentRepeat.isRepeating) {
          currentRepeat.isRepeating = false;
          currently_repeating_scope.pop();
        }
        active_scopes.pop();
        active_repeats.pop();
        return false;
      } else {
        if (!currentRepeat.isRepeating) {
          currentRepeat.isRepeating = true;
          currently_repeating_scope.push("repeat");
          currentRepeat.increaseCounter();
          return true;
        }
        currentRepeat.increaseCounter();
      }
    }
    return false;
  }

  private static void addActionToRepeatBody(String action){
    if (!active_repeats.isEmpty()) {
      if (active_repeats.peek().isRepeating) {
        return;
      }
      // Add current action to bodies of all active repeats that are not repeating
      for (RepeatStats repeatLoop: active_repeats) {
        if (!repeatLoop.isRepeating) {
          repeatLoop.repeatBody.add(action);
        }
      }
    }
  }
  private static void repeatEvaluation(String action) {
    Pattern pattern = Pattern.compile("wiederhole (\\w+) Mal");
    Matcher matcher = pattern.matcher(action);
    if (matcher.find()) {
      String repeatString = matcher.group(1);
      try {
        int value = getActualValueFromExpression(repeatString);
        active_scopes.push("repeat");
        active_repeats.push(new RepeatStats(value));
      } catch (IllegalAccessException | NoSuchElementException | IndexOutOfBoundsException | NumberFormatException e) {
        System.out.println(e.getMessage());
      }
    }
  }
  private static void ifEvaluation(String action) {
    if (action.equals("}") && !active_scopes.isEmpty() && active_scopes.peek().equals("if")) {
      active_ifs.pop();
      active_scopes.pop();
      return;
    }
    if (action.contains("falls")) {
      active_scopes.push("if");
      Pattern pattern = Pattern.compile("falls \\((.*)\\)");
      active_ifs.push(new IfStats(evalComplexCondition(action, pattern)));
    }

    if (action.contains("sonst")) {
      IfStats currentIf = active_ifs.peek();
      currentIf.else_flag = !currentIf.if_flag;
      currentIf.if_flag = false;
    }
  }

  public static boolean evalComplexCondition(String action, Pattern pattern) {
    if (!active_func_defs.isEmpty()) {
      return false;
    }
    Matcher matcher = pattern.matcher(action);
    if (matcher.find()) {
      blocklyLexer lexer = new blocklyLexer(CharStreams.fromString(matcher.group(1)));
      CommonTokenStream tokens = new CommonTokenStream(lexer);
      blocklyParser parser = new blocklyParser(tokens);

      ParseTree tree = parser.start();
      blocklyConditionVisitor eval = new blocklyConditionVisitor();
      try {
        StartNode ast = (StartNode) eval.visit(tree);
        boolean result = ast.getBoolValue();
        System.out.println("Result of current condition: " + result);
        return result;
      } catch (NoSuchElementException e) {
        System.out.println(e.getMessage());
        setError(e.getMessage());
        return false;
      }
    }
    System.out.println("Detected condition that is not valid: " + action);
    setError("Detected condition that is not valid: " + action);
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
