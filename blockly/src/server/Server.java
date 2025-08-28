package server;

import client.Client;
import com.badlogic.gdx.Gdx;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import contrib.utils.EntityUtils;
import core.Game;
import core.level.elements.ILevel;
import core.level.loader.DungeonLoader;
import core.utils.Point;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import level.BlocklyLevel;
import utils.BlocklyCodeRunner;

/**
 * This class controls the communication between the blockly frontend and the dungeon. It has three
 * interfaces that can be used by the blockly frontend: /start /reset /clear The main interface is
 * "/start". This interface receives a string representing the code that should be executed in the
 * dungeon. The string can contain multiple lines or just one line. The "/reset" interface will stop
 * the current execution and will reset all values. The "/clear" interface will reset all values and
 * will typically be called after the blockly program run into an error or the execution finished
 * (when the whole program was executed and not just a step). The most important function of this
 * class is the function "processAction". This function will be executed for each action that needs
 * to be performed. If you add a block in the blockly-frontend this function is a good starting
 * point. You will probably want to add a new function to evaluate if the current action was
 * produced by your new block. You can easily check this with simple regex matching. If your regex
 * matched, perform the logic of your block. Place your function on the right place in the
 * processAction and think about side effects. For example: Does your block influence if other
 * action may be performed? Does your block needs its own scope? If yes, at least add it to the
 * active_scopes stack. Compare to while-loops/repeat-loops/if-statements and func definitions.
 * Don't forget to add a test to the TestServer class for your new block.
 */
public class Server {

  private static final Logger LOGGER = Logger.getLogger(Server.class.getSimpleName());

  // Singleton
  private static Server instance;

  /** Default port for the server. */
  private static final int DEFAULT_PORT = 8080;

  /**
   * This variable holds all active scopes in a stack. The value at the top of the stack is the
   * current scope. It can hold the following values: if, while, repeat, function.
   */
  public final Stack<String> active_scopes = new Stack<>();

  /**
   * Hashmap storing all variables. This is public, so we can easily access it in the
   * BlocklyConditionVisitor
   */
  public final HashMap<String, Variable> variables = new HashMap<>();

  /** Hashmap storing all functions. */
  private final HashMap<String, FuncStats> functions = new HashMap<>();

  /** Stack containing all active scopes. */
  public final Stack<RepeatStats> active_repeats = new Stack<>();

  /** Stack containing all active while loops. */
  public final Stack<WhileStats> active_whiles = new Stack<>();

  /** Stack containing all active ifs. */
  public final Stack<IfStats> active_ifs = new Stack<>();

  /** Stack containing all active func defs. */
  public final Stack<FuncStats> active_func_defs = new Stack<>();

  /**
   * This boolean will be set to true on error or if the user clicked the reset button in the
   * blockly frontend. The execution of the current program will stop if this variable is true.
   */
  public boolean interruptExecution = false;

  /** This boolean will be set to true on error. */
  public boolean errorOccurred = false;

  /** This variable cotnains the error message if an error occured during the execution. */
  public String errorMsg = "";

  private boolean clearHUD = false;
  private final Stack<String> currently_repeating_scope = new Stack<>();

  /** Constructor of the server. */
  private Server() {}

  /**
   * Singleton pattern. Get the instance of the server. If the server does not exist, create a new
   * server object. The servers run on the {@link #DEFAULT_PORT}.
   *
   * @return Returns the server object.
   */
  public static Server instance() {
    if (instance == null) {
      instance = new Server();
    }
    return instance;
  }

  /**
   * Start the server and listen on the start, reset, clear interfaces. This server will be started
   * by the client.
   *
   * @return Returns the server object.
   * @throws IOException Throws an IOException if the server could not be started.
   */
  public HttpServer start() throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress("localhost", DEFAULT_PORT), 0);
    HttpContext resetContext = server.createContext("/reset");
    resetContext.setHandler(this::handleResetRequest);
    HttpContext clearContext = server.createContext("/clear");
    clearContext.setHandler(this::handleClearRequest);
    HttpContext levelsContext = server.createContext("/levels");
    levelsContext.setHandler(this::handleLevelsRequest);
    HttpContext levelContext = server.createContext("/level");
    levelContext.setHandler(this::handleLevelRequest);
    HttpContext codeContext = server.createContext("/code");
    codeContext.setHandler(this::handleCodeRequest);
    HttpContext languageContext = server.createContext("/language");
    languageContext.setHandler(this::handleLanguageRequest);
    HttpContext statusContext = server.createContext("/status");
    statusContext.setHandler(this::handleStatusRequest);
    server.start();
    return server;
  }

  /**
   * Handles the reset request. This function will set the boolean interruptExecution. The execution
   * will be stopped.
   *
   * @param exchange Exchange object. The function will send a success response to the blockly
   *     frontend
   * @throws IOException If an error occurs while sending the response
   */
  private void handleResetRequest(HttpExchange exchange) throws IOException {
    BlocklyCodeRunner.instance().stopCode();
    // Reset values
    interruptExecution = true;
    Client.restart();
    sendHeroPosition(exchange);
  }

  /**
   * Handles the clear request. This function will clear all global variables that may have been
   * modified. It will empty all stacks, hashmaps and resets all global variables to their default
   * value.
   *
   * @param exchange Exchange object. The function will send a success response to the blockly
   *     frontend
   * @throws IOException If an error occurs while sending the response
   */
  private void handleClearRequest(HttpExchange exchange) throws IOException {
    clearGlobalValues();

    sendHeroPosition(exchange);
  }

  private void sendHeroPosition(HttpExchange exchange) throws IOException {
    Point heroPos = EntityUtils.getHeroPosition();
    if (heroPos == null) {
      heroPos = new Point(0, 0);
    }
    String response = heroPos.toString();
    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    exchange.sendResponseHeaders(200, response.getBytes().length);
    OutputStream os = exchange.getResponseBody();
    os.write(response.getBytes());
    os.close();
  }

  /**
   * Handles the levels request. This function will send all available levels to the blockly
   * frontend.
   *
   * @param exchange Exchange object. The function will send a success response to the blockly
   *     frontend
   */
  private void handleLevelsRequest(HttpExchange exchange) throws IOException {
    StringBuilder response = new StringBuilder();
    for (String levelName : DungeonLoader.levelOrder()) {
      response.append(levelName).append("\n");
    }
    response.deleteCharAt(response.length() - 1); // Remove last newline

    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    exchange.sendResponseHeaders(200, response.toString().getBytes().length);
    OutputStream os = exchange.getResponseBody();
    os.write(response.toString().getBytes());
    os.close();
  }

  /**
   * Handles the level request. This function will send the current level with its blocked blocks or
   * if give will first change the level to the given one and then send the current level with its
   * blocked blocks.
   *
   * @param exchange Exchange object. The function will send a success response to the blockly
   *     frontend
   */
  private void handleLevelRequest(HttpExchange exchange) throws IOException {
    StringBuilder response = new StringBuilder();

    // Query parameter 'level'
    String query = exchange.getRequestURI().getQuery();
    String levelName = query != null && query.contains("levelName=") ? query.split("=")[1] : null;

    if (levelName != null && !levelName.equals(DungeonLoader.currentLevel())) {
      // if given and the level is not the current one, load it
      DungeonLoader.loadLevel(levelName);
      waitDelta(); // waiting for all systems to update once
    }

    response.append(DungeonLoader.currentLevel()).append(" ");
    for (String blockedBlock : blockedBlocksForLevel(Game.currentLevel().orElse(null))) {
      response.append(blockedBlock).append(" ");
    }
    response.deleteCharAt(response.length() - 1); // Remove last space

    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    exchange.sendResponseHeaders(200, response.length());
    OutputStream os = exchange.getResponseBody();
    os.write(response.toString().getBytes());
    os.close();
  }

  private Set<String> blockedBlocksForLevel(ILevel level) {
    Set<String> blockedBlocks = new HashSet<>();
    if (level instanceof BlocklyLevel blocklyLevel) {
      blockedBlocks.addAll(blocklyLevel.blockedBlocklyElements());
    }
    return blockedBlocks;
  }

  /**
   * Handles the code request. This function will execute the given java code. The code must be
   * formatted as a string and will be executed in the dungeon.
   *
   * @param exchange Exchange object
   * @throws IOException If an error occurs while sending the response
   */
  private void handleCodeRequest(HttpExchange exchange) throws IOException {
    // Check if this is a stop request
    String query = exchange.getRequestURI().getQuery();
    boolean isStopRequest = query != null && query.contains("stop=1");
    int sleepAfterEachLine = -1;
    if (query != null && query.contains("sleep=")) {
      String[] params = query.split("&");
      for (String param : params) {
        if (param.startsWith("sleep=")) {
          try {
            sleepAfterEachLine = Math.max(Integer.parseInt(param.split("=")[1]), 0);
          } catch (NumberFormatException e) {
            LOGGER.warning("Invalid sleep parameter: " + param);
          }
        }
      }
    }

    if (isStopRequest) {
      handleStopCodeExecution(exchange);
      return;
    }

    // Handle normal code execution request
    if (BlocklyCodeRunner.instance().isCodeRunning()) {
      String response = "Another code execution is already running. Please stop it first.";
      exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
      exchange.sendResponseHeaders(400, response.getBytes().length);
      OutputStream os = exchange.getResponseBody();
      os.write(response.getBytes());
      os.close();
      return;
    }

    InputStream inStream = exchange.getRequestBody();
    String text = new String(inStream.readAllBytes(), StandardCharsets.UTF_8);

    // Start code execution
    interruptExecution = false;
    try {
      if (sleepAfterEachLine >= 0) {
        BlocklyCodeRunner.instance().executeJavaCode(text, sleepAfterEachLine);
      } else {
        BlocklyCodeRunner.instance().executeJavaCode(text);
      }

      // Wait 1 second to check for errors or completion
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        // Ignore
      }

      String response;
      int statusCode;

      if (interruptExecution) {
        response = errorMsg.isEmpty() ? "Code execution interrupted" : "Error: " + errorMsg;
        statusCode = 400;
        BlocklyCodeRunner.instance().stopCode();
      } else if (!BlocklyCodeRunner.instance().isCodeRunning()) {
        // Code completed execution within 1 second
        response = "OK - Code executed successfully";
        statusCode = 200;
      } else {
        // Code is still running after 1 second
        response = "OK - Code execution started";
        statusCode = 200;
      }

      exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
      exchange.sendResponseHeaders(statusCode, response.getBytes().length);
      OutputStream os = exchange.getResponseBody();
      os.write(response.getBytes());
      os.close();
    } catch (Exception e) {
      LOGGER.severe("Error executing code: " + e);
      setError(e.getMessage());
      String response = errorMsg;
      exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
      exchange.sendResponseHeaders(400, response.getBytes().length);
      OutputStream os = exchange.getResponseBody();
      os.write(response.getBytes());
      os.close();
      BlocklyCodeRunner.instance().stopCode();
    }
  }

  /**
   * Handles stop request for currently running code execution.
   *
   * @param exchange Exchange object
   * @throws IOException If an error occurs while sending the response
   */
  private void handleStopCodeExecution(HttpExchange exchange) throws IOException {
    String response;
    int statusCode = 200;

    if (BlocklyCodeRunner.instance().isCodeRunning()) {
      BlocklyCodeRunner.instance().stopCode();
      interruptExecution = true;
      response = "Code execution stopped";
    } else {
      response = "No code execution running";
    }

    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    exchange.sendResponseHeaders(statusCode, response.getBytes().length);
    OutputStream os = exchange.getResponseBody();
    os.write(response.getBytes());
    os.close();
  }

  /**
   * Handles the language request. This function will return the current language of the dungeon.
   *
   * @param exchange Exchange object. The function will send a success response to the blockly
   *     frontend
   * @throws IOException If an error occurs while sending the response
   */
  private void handleLanguageRequest(HttpExchange exchange) throws IOException {
    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

    // Query parameter 'object'
    String query = exchange.getRequestURI().getQuery();
    String objectName = query != null && query.contains("object=") ? query.split("=")[1] : "server";

    String response = LanguageServer.GenerateCompletionItems(objectName);
    exchange.sendResponseHeaders(200, response.getBytes().length);
    OutputStream os = exchange.getResponseBody();
    os.write(response.getBytes());
    os.close();
  }

  /**
   * Handles the status request. This function will return the current status of the Blockly -
   * UserScript Code.
   *
   * @param exchange Exchange object
   * @throws IOException If an error occurs while sending the response
   */
  private void handleStatusRequest(HttpExchange exchange) throws IOException {
    String response;
    if (BlocklyCodeRunner.instance().isCodeRunning()) {
      response = "running";
    } else {
      response = "completed";
    }

    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    exchange.sendResponseHeaders(200, response.getBytes().length);
    OutputStream os = exchange.getResponseBody();
    os.write(response.getBytes());
    os.close();
  }

  /**
   * Clear all global variables that may have been modified. Empty all stacks, hashmaps and reset
   * all global variables to their default value. Also clear the variable and array HUD in the
   * dungeon.
   */
  public void clearGlobalValues() {
    BlocklyCodeRunner.instance().stopCode();

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
    // Set clear HUD to true so the HUD will be cleared next time the start route will be used.
    clearHUD = true;
    System.out.println("Values cleared");
    printScopes();
  }

  /**
   * Set the error flag and stop the execution. Also set the error message to the provided message.
   *
   * @param errMsg Error message that will be sent to the blockly frontend.
   */
  private void setError(String errMsg) {
    if (errMsg.trim().isEmpty()) {
      errMsg = "Unknown error";
    }
    interruptExecution = true;
    errorOccurred = true;
    errorMsg = errMsg;
  }

  /** Prints all current scopes, repeating scopes, variables and functions. */
  private void printScopes() {
    System.out.print("Current scopes: ");
    System.out.println(active_scopes);
    System.out.print("Currently repeating scopes: ");
    System.out.println(currently_repeating_scope);
    System.out.print("Current variables: ");
    System.out.println(variables);
    System.out.print("Available functions: ");
    System.out.println(functions);
  }

  /**
   * Wait for the delta time of the current frame.
   *
   * <p>Used to wait for the game loop to finish the current frame before executing the next action.
   */
  public static void waitDelta() {
    long timeout = (long) (Gdx.graphics.getDeltaTime() * 1000);
    try {
      TimeUnit.MILLISECONDS.sleep(timeout - 1);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
