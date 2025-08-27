package contrib.systems;

import core.System;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import lispy.Interpreter;
import lispy.Parser;
import lispy.ast.Expr;
import lispy.ast.Program;
import lispy.values.Env;
import lispy.values.Value;

/** System for Lispy interpreter. */
public class LispySystem extends System {
  // command pattern: representing each input as a task
  private record Task(String code, CountDownLatch done, List<String> results, List<String> error) {
    public static Task of(String code) {
      return new Task(code, new CountDownLatch(1), new ArrayList<>(), new ArrayList<>());
    }
  }

  // for synchronisation between I/O thread and interpreter
  // technically speaking, we only have a single task at each time, so we don't need a queue. but
  // that makes synchronisation easier: the interpreter doesn't need to know whether there is a new
  // task, it just needs to retrieve it from the queue. if there is nothing in there, then there
  // is no new task. alternatively, you would have to integrate and toggling a thread-safe boolean
  // indicating whether the task is new...
  private static BlockingQueue<Task> requestQueue = new LinkedBlockingQueue<>();
  private static AtomicBoolean ioStarted = new AtomicBoolean(false);

  // (global) environment for interpreter
  private static Env env = Interpreter.newGlobalEnv();

  // there can be only one
  private static LispySystem instance = new LispySystem();

  /** Starting the Lispy Interpreter. */
  private LispySystem() {
    super();

    // greetings
    println("Lispy – REPL (Java 21). Type :q to quit.");

    // start I/O thread (once)
    if (ioStarted.compareAndSet(false, true)) {
      Thread inputThread = new Thread(LispySystem::readInput, "console-input");
      inputThread.setDaemon(true); // will not block JVM exiting
      inputThread.start();
    }
  }

  /**
   * Starting the Lispy Interpreter.
   *
   * @return running Lispy Interpreter
   */
  public static LispySystem instance() {
    return instance;
  }

  /** REPL: interpreter part (non-blocking). */
  @Override
  public void execute() {
    // looking for new tasks
    Task task = requestQueue.poll();
    if (task == null) return;

    try {
      Program p = Parser.parseString(task.code);
      for (Expr e : p.expressions()) {
        Value v = Interpreter.evaluate(e, env);
        task.results.add(Value.pretty(v));
      }
    } catch (Exception e) {
      task.error.add("interpreter error: " + e.getMessage());
    } finally {
      task.done.countDown(); // release I/O thread
    }
  }

  // REPL: I/O part
  // will run as thread, blocks in readLine() and while waiting for results from interpreter
  private static void readInput() {
    try (BufferedReader br =
        new BufferedReader(new InputStreamReader(java.lang.System.in, StandardCharsets.UTF_8))) {
      StringBuilder buf = new StringBuilder();
      String promptBase = "lispy> ";
      String promptCont = ".....> ";
      String prompt = promptBase;

      while (ioStarted.get()) {
        // print prompt and wait for user input
        print(prompt);
        String line = br.readLine();

        // quit?
        if (line == null || line.trim().equals(":q")) {
          println("\n\nso long, and thanks for the fish\n\n");
          ioStarted.set(false);
          break;
        }

        // add line to buffer
        buf.append(line).append('\n');

        // read more if parenthesis not balanced
        if (needsMore(buf)) {
          prompt = promptCont;
          continue;
        }

        // wrap code as new task and enqueue task as new request
        Task task = Task.of(buf.toString().trim());
        requestQueue.offer(task);

        // clear prompt and input buffer
        buf.setLength(0);
        prompt = promptBase;

        // wait for results (synchronized with repl())
        try {
          task.done.await();
        } catch (InterruptedException ie) {
          if (!ioStarted.get()) break;
        }

        // print results or error
        task.error.forEach(LispySystem::println);
        task.results.forEach(LispySystem::println);
      }
    } catch (Exception e) {
      error("reader error: " + e.getMessage());
    } finally {
      ioStarted.set(false);
    }
  }

  // are all parenthesis balanced?
  private static boolean needsMore(CharSequence src) {
    boolean inString = false;
    int depth = 0;
    int n = src.length();
    for (int i = 0; i < n; i++) {
      char c = src.charAt(i);

      if (!inString && c == ';') {
        while (i < n && src.charAt(i) != '\n' && src.charAt(i) != '\r') i++;
        continue;
      }

      if (c == '"') {
        inString = !inString;
        continue;
      }

      if (!inString) {
        if (c == '(') depth++;
        else if (c == ')') depth--;
      }
    }
    return inString || depth > 0;
  }

  private static void print(String s) {
    java.lang.System.out.print(s);
    java.lang.System.out.flush();
  }

  private static void println(String s) {
    print(s + "\n");
  }

  private static void error(String s) {
    java.lang.System.err.println(s);
    java.lang.System.err.flush();
  }
}
