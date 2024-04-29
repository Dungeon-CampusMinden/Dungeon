package server;

import core.game.ECSManagment;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import systems.VisualProgrammingSystem;
import tasks.CreateVariable;
import tasks.VisuTask;

@ServerEndpoint("/ws")
public class StandardEndpoint {

  private ScheduledExecutorService scheduler;
  VisualProgrammingSystem visualProgrammingSystem;

  @OnOpen
  public void onOpen(Session session) {
    System.out.println("Session opened with " + session.getId());
    startPingScheduler(session);
  }

  @OnClose
  public void onClose() {
    stopPingScheduler();
    System.out.println("Session was closed");
  }

  private void startPingScheduler(Session session) {
    scheduler = Executors.newScheduledThreadPool(1);

    scheduler.scheduleAtFixedRate(
        () -> {
          if (session != null && session.isOpen()) {
            try {
              session.getBasicRemote().sendPing(ByteBuffer.wrap("Ping".getBytes()));
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        },
        0,
        25,
        TimeUnit.SECONDS);

    // parseMessage(
    // "set a = 12; expr a_ * _12sf_2 * 13_12sd + 14 + c * doSomething((a + b), 32) + halloweld(aa +
    // cc)");
  }

  private void stopPingScheduler() {
    if (scheduler != null && !scheduler.isShutdown()) {
      scheduler.shutdown();
    }
  }

  @OnMessage
  public void handleMessage(String message) {
    System.out.println("Message recieved: " + message);
    if (visualProgrammingSystem == null)
      visualProgrammingSystem =
          (VisualProgrammingSystem) ECSManagment.systems().get(VisualProgrammingSystem.class);

    // Todo - react accordingly to message/create a new Task
    ArrayList<VisuTask> tasks = parseMessage(message);
    visualProgrammingSystem.addTask(tasks);
  }

  private ArrayList<VisuTask> parseMessage(String message) {
    ArrayList<VisuTask> tasks = new ArrayList<>();

    // declaration of variables
    if (message.startsWith("var")) {

      String newMessage = message.substring(4);

      Matcher identifierMatcher = Pattern.compile("\\w+").matcher(newMessage);
      identifierMatcher.find();
      String identifier = identifierMatcher.group();

      Matcher valueMatcher = Pattern.compile("(?<=\\=\\s)\\d+").matcher(newMessage);
      valueMatcher.find();
      int newValue = Integer.parseInt(valueMatcher.group());

      tasks.add(new CreateVariable(message, visualProgrammingSystem, identifier, newValue));

    } else if (message.startsWith("set")) {
      String newMessage = message.substring(4);

      Matcher identifierMatcher = Pattern.compile("\\w+").matcher(newMessage);
      identifierMatcher.find();
      String identifier = identifierMatcher.group();

      Matcher valueMatcher = Pattern.compile("(?<=\\=\\s)\\d+").matcher(newMessage);
      valueMatcher.find();
      int newValue = Integer.parseInt(valueMatcher.group());

      Matcher expressionMatcher = Pattern.compile("; expr ").matcher(newMessage);

      if (expressionMatcher.find()) {
        String expression = newMessage.substring(expressionMatcher.end());
        Matcher functionMatcher =
            Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*(?=\\()").matcher(expression);

        ArrayList<String> functions = new ArrayList<>();
        String exprWithoutFunc = expression;
        while (functionMatcher.find()) {
          functions.add(functionMatcher.group());
          int closingParenIndex = getClosingParens(exprWithoutFunc, functionMatcher.end() - 1);
          String functionParameter =
              exprWithoutFunc.substring(functionMatcher.end() + 1, closingParenIndex);
          exprWithoutFunc =
              exprWithoutFunc.substring(0, functionMatcher.start())
                  + exprWithoutFunc.substring(closingParenIndex + 1);
          functionMatcher =
              Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*(?=\\()").matcher(exprWithoutFunc);
        }

        Matcher variableMatcher =
            Pattern.compile("\\b[a-zA-Z_][a-zA-Z0-9_]*(?!\\()\\b").matcher(exprWithoutFunc);
        ArrayList<String> variables = new ArrayList<>();
        while (variableMatcher.find()) {
          variables.add(variableMatcher.group());
        }
      }
    } else {
      // todo - error
    }

    return tasks;
  }

  int getClosingParens(String str, int startIndex) {
    int parensCounter = 0;

    for (int i = startIndex; i < str.length(); i++) {
      char ch = str.charAt(i);
      if (ch == '(') {
        parensCounter++;
      } else if (ch == ')') {
        parensCounter--;
        if (parensCounter == 0) return i;
      }
    }
    return -1;
  }

  @OnMessage
  public void onPongMessage(Session session, PongMessage message) {
    System.out.println("Pong recieved!");
  }
}
