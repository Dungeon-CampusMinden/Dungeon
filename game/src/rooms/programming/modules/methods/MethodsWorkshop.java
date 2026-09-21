package rooms.programming.modules.methods;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import rooms.programming.modules.methods.MethodsRoute.Action;
import rooms.programming.modules.methods.MethodsRoute.Direction;
import rooms.programming.modules.methods.MethodsRoute.Step;
import tools.jackson.databind.json.JsonMapper;

/** Server-owned block editor and incremental interpreter. Only connected main blocks execute. */
public final class MethodsWorkshop {
  /** Edits and run controls accepted from the player holding the editor. */
  public enum Operation {
    CLAIM,
    RELEASE,
    MOVE_BLOCK,
    ADD_BLOCK,
    EDIT_BLOCK,
    DELETE_BLOCK,
    NAME,
    PARAMETER,
    BUILD,
    NEW_METHOD,
    EDIT_METHOD,
    EXECUTE,
    STOP
  }

  /** Revision and room phase identify the exact authoritative snapshot being edited. */
  public record Intent(long revision, int stage, Operation operation, String value) {}

  /** Controls how a returned or assigned value is written to its target variable. */
  public enum ResultMode {
    REPLACE(" = "),
    ADD(" += "),
    SUBTRACT(" -= ");

    private final String operator;

    ResultMode(String operator) {
      this.operator = operator;
    }

    String operator() {
      return operator;
    }
  }

  /**
   * A draggable statement: operand is an expression; target receives assignment or call results.
   */
  public record Block(
      String id,
      Action action,
      String operand,
      String target,
      String method,
      List<String> arguments,
      ResultMode mode) {
    public Block {
      arguments = List.copyOf(arguments);
      Objects.requireNonNull(mode);
    }
  }

  /** Detached editable or built body with its own parameter and local-variable scope. */
  public record Definition(String name, List<String> parameters, List<Block> body) {
    public Definition {
      parameters = List.copyOf(parameters);
      body = List.copyOf(body);
    }
  }

  /**
   * JSON payload for block moves, insertion and replacement; containers are main, draft or scrap.
   */
  public record Edit(String id, String container, int index, Block block) {}

  /** Immutable editor snapshot, interpreter observations and visible control-rune criteria. */
  public record State(
      int stage,
      long revision,
      int editorId,
      int crystals,
      int errors,
      boolean busy,
      boolean completed,
      List<Block> main,
      List<Block> scrap,
      Definition draft,
      Map<String, Definition> definitions,
      String feedback,
      List<Step> trace,
      int activeStep,
      Map<String, String> variables,
      boolean compact,
      boolean parameterReuse,
      boolean returnedValueUsed,
      boolean worldSolved) {
    public State {
      main = List.copyOf(main);
      scrap = List.copyOf(scrap);
      definitions = Map.copyOf(definitions);
      trace = List.copyOf(trace);
      variables = Map.copyOf(variables);
    }
  }

  private static final JsonMapper JSON = JsonMapper.builder().build();
  private final List<Block> main = new ArrayList<>(), scrap = new ArrayList<>();
  private final Map<String, Definition> definitions = new LinkedHashMap<>();
  private Definition draft = new Definition("", List.of(), List.of());
  private String editingName = "";
  private long revision;
  private long executionRevision;
  private int editorId = -1, errors, instructions;
  private boolean busy, completed, parameterReuse, returnedValueUsed, worldSolved;
  private String feedback =
      "Verbinde dein Hauptprogramm. Jeder Start setzt Nox und alle Arbeitsstellen zurück.";
  private final List<Step> trace = new ArrayList<>();
  private final Deque<Frame> stack = new ArrayDeque<>();
  private final Map<String, Integer> calls = new HashMap<>();
  private final Map<String, String> mainVariables = new LinkedHashMap<>();
  private Block pending;

  private static final class Frame {
    final List<Block> body;
    final Map<String, String> variables;
    final Block caller;
    int pc;

    Frame(List<Block> body, Map<String, String> variables, Block caller) {
      this.body = body;
      this.variables = variables;
      this.caller = caller;
    }
  }

  /** Starts with the expanded, executable route, including explicit caller bookkeeping. */
  public MethodsWorkshop() {
    int id = 0;
    for (var station : MethodsRoute.STATIONS) {
      for (var step : station.body())
        main.add(
            new Block(
                "b" + (id++),
                step.action(),
                step.action() == Action.TURN
                    ? step.direction().label()
                    : Integer.toString(step.amount()),
                step.action() == Action.COLLECT ? "gesammelt" : "",
                "",
                List.of(),
                ResultMode.REPLACE));
      if (station.kind() == MethodsRoute.Kind.COLLECT)
        main.add(
            new Block(
                "b" + (id++),
                Action.ASSIGN,
                "kristalle + gesammelt",
                "kristalle",
                "",
                List.of(),
                ResultMode.REPLACE));
      if (station.kind() == MethodsRoute.Kind.ALTAR)
        main.add(
            new Block(
                "b" + (id++),
                Action.ASSIGN,
                "kristalle - " + station.amount(),
                "kristalle",
                "",
                List.of(),
                ResultMode.REPLACE));
    }
    mainVariables.put("kristalle", "0");
  }

  /** Returns a snapshot; current-frame variables reveal parameter bindings during execution. */
  public State state() {
    return new State(
        0,
        revision,
        editorId,
        Integer.parseInt(mainVariables.getOrDefault("kristalle", "0")),
        errors,
        busy,
        completed,
        main,
        scrap,
        draft,
        definitions,
        feedback,
        trace,
        trace.size() - 1,
        stack.isEmpty() ? mainVariables : stack.peek().variables,
        main.size() <= 8,
        parameterReuse,
        returnedValueUsed,
        worldSolved);
  }

  private boolean current(Intent i) {
    return i != null
        && i.revision() == revision
        && i.stage() == 0
        && i.operation() != null
        && i.value() != null;
  }

  /** Applies current edits only for the owner. Invalid content is acknowledged with feedback. */
  public boolean apply(int actor, Intent intent) {
    if (!current(intent)) return false;
    if (intent.operation() == Operation.CLAIM) {
      if (editorId >= 0) return false;
      editorId = actor;
      revision++;
      return true;
    }
    if (intent.operation() == Operation.RELEASE) return releaseEditor(actor);
    if (editorId != actor || busy) return false;
    try {
      switch (intent.operation()) {
        case NAME -> {
          if (intent.value().length() > 40)
            return rejectEdit("Name zu lang (höchstens 40 Zeichen).");
          draft = new Definition(intent.value(), draft.parameters(), draft.body());
        }
        case PARAMETER -> {
          List<String> names =
              intent.value().isBlank()
                  ? List.of()
                  : java.util.Arrays.stream(intent.value().split(",", -1))
                      .map(String::trim)
                      .toList();
          if (names.size() > 8 || names.stream().anyMatch(n -> n.length() > 40))
            return rejectEdit("Höchstens 8 Parameter mit je 40 Zeichen.");
          draft = new Definition(draft.name(), names, draft.body());
        }
        case DELETE_BLOCK -> {
          boolean removed = false;
          for (String container : List.of("main", "draft", "scrap")) {
            var blocks = new ArrayList<>(body(container));
            if (blocks.removeIf(block -> block.id().equals(intent.value()))) {
              setBody(container, blocks);
              removed = true;
            }
          }
          if (!removed) return rejectEdit("Block nicht mehr vorhanden.");
        }
        case NEW_METHOD -> {
          archiveChangedDraft();
          draft = new Definition("", List.of(), List.of());
          editingName = "";
        }
        case EDIT_METHOD -> {
          var found = definitions.get(intent.value());
          if (found == null) return rejectEdit("Methode ist noch nicht gebaut.");
          int retained = totalBlocks() - (draftUnchanged() ? draft.body().size() : 0);
          if (retained + found.body().size() > 256)
            return rejectEdit("Die Arbeitsfläche enthält höchstens 256 Blöcke.");
          archiveChangedDraft();
          draft =
              new Definition(
                  found.name(),
                  found.parameters(),
                  found.body().stream()
                      .map(
                          b ->
                              new Block(
                                  java.util.UUID.randomUUID().toString(),
                                  b.action(),
                                  b.operand(),
                                  b.target(),
                                  b.method(),
                                  b.arguments(),
                                  b.mode()))
                      .toList());
          editingName = found.name();
        }
        case BUILD -> {
          if (!identifier(draft.name()) || draft.body().isEmpty())
            return rejectEdit("Methode braucht einen gültigen Namen und einen Methodenrumpf.");
          if (draft.parameters().stream().anyMatch(n -> !identifier(n))
              || draft.parameters().stream().distinct().count() != draft.parameters().size())
            return rejectEdit("Parameter brauchen gültige, unterschiedliche Namen.");
          if (!editingName.isEmpty() && !editingName.equals(draft.name())) {
            feedback =
                "Für eine vorhandene Methode bleibt der Name gleich. Neue Methode erstellt eine eigene Rune.";
            revision++;
            return true;
          }
          if (definitions.size() >= 32 && !definitions.containsKey(draft.name()))
            return rejectEdit("Die Sammlung enthält höchstens 32 Methoden.");
          definitions.put(draft.name(), draft);
          editingName = draft.name();
          feedback = "Methode " + draft.name() + " gebaut. Die Rune kann jetzt aufgerufen werden.";
        }
        case ADD_BLOCK, MOVE_BLOCK, EDIT_BLOCK -> {
          Edit edit = JSON.readValue(intent.value(), Edit.class);
          if (intent.operation() == Operation.EDIT_BLOCK) {
            if (!valid(edit.block()) || edit.id() == null) return rejectEdit("Ungültiger Block.");
            boolean found = false;
            for (String container : List.of("main", "scrap", "draft")) {
              var body = new ArrayList<>(body(container));
              for (int n = 0; n < body.size(); n++)
                if (body.get(n).id().equals(edit.id())) {
                  Block b = edit.block();
                  body.set(
                      n,
                      new Block(
                          edit.id(),
                          b.action(),
                          b.operand(),
                          b.target(),
                          b.method(),
                          b.arguments(),
                          b.mode()));
                  found = true;
                }
              setBody(container, body);
            }
            if (!found) return rejectEdit("Block nicht mehr vorhanden.");
          } else {
            var destination = body(edit.container());
            if (edit.index() < 0 || edit.index() > destination.size())
              return rejectEdit("Einfügeposition nicht mehr vorhanden.");
            Block block = edit.block();
            String origin = null;
            int sourceIndex = -1;
            if (intent.operation() == Operation.MOVE_BLOCK) {
              for (String c : List.of("main", "draft", "scrap"))
                for (int n = 0; n < body(c).size(); n++)
                  if (body(c).get(n).id().equals(edit.id())) {
                    origin = c;
                    sourceIndex = n;
                    block = body(c).get(n);
                  }
              if (origin == null) return rejectEdit("Block nicht mehr vorhanden.");
            } else {
              if (!valid(block)) return rejectEdit("Ungültiger Block.");
              if (totalBlocks() >= 256)
                return rejectEdit("Die Arbeitsfläche enthält höchstens 256 Blöcke.");
              block =
                  new Block(
                      java.util.UUID.randomUUID().toString(),
                      block.action(),
                      block.operand(),
                      block.target(),
                      block.method(),
                      block.arguments(),
                      block.mode());
            }
            int index = edit.index();
            if (origin != null) {
              var from = new ArrayList<>(body(origin));
              from.remove(sourceIndex);
              setBody(origin, from);
              if (origin.equals(edit.container()) && sourceIndex < index) index--;
            }
            var to = new ArrayList<>(body(edit.container()));
            to.add(index, block);
            setBody(edit.container(), to);
          }
        }
        default -> {
          return false;
        }
      }
      completed = false;
      revision++;
      return true;
    } catch (RuntimeException invalid) {
      return rejectEdit("Ungültige Bearbeitung. Bitte erneut versuchen.");
    }
  }

  // Built definitions already preserve unchanged drafts; only unsaved edits need a scrap copy.
  private void archiveChangedDraft() {
    if (!draftUnchanged()) scrap.addAll(draft.body());
  }

  private boolean draftUnchanged() {
    var saved = definitions.get(editingName);
    if (saved == null
        || !saved.name().equals(draft.name())
        || !saved.parameters().equals(draft.parameters())
        || saved.body().size() != draft.body().size()) return false;
    for (int index = 0; index < saved.body().size(); index++) {
      Block a = saved.body().get(index), b = draft.body().get(index);
      if (a.action() != b.action()
          || !a.operand().equals(b.operand())
          || !a.target().equals(b.target())
          || !a.method().equals(b.method())
          || !a.arguments().equals(b.arguments())
          || a.mode() != b.mode()) return false;
    }
    return true;
  }

  private boolean rejectEdit(String reason) {
    feedback = reason;
    revision++;
    return true;
  }

  private int totalBlocks() {
    return main.size() + scrap.size() + draft.body().size();
  }

  private static boolean valid(Block b) {
    return b != null
        && b.action() != null
        && b.operand() != null
        && b.target() != null
        && b.method() != null
        && b.arguments() != null
        && b.arguments().size() <= 8
        && b.operand().length() <= 100
        && b.target().length() <= 40
        && b.method().length() <= 40
        && b.arguments().stream().allMatch(a -> a != null && a.length() <= 100);
  }

  private List<Block> body(String c) {
    return switch (c) {
      case "main" -> main;
      case "scrap" -> scrap;
      case "draft" -> draft.body();
      default -> throw new IllegalArgumentException();
    };
  }

  private void setBody(String c, List<Block> b) {
    switch (c) {
      case "main" -> {
        main.clear();
        main.addAll(b);
      }
      case "scrap" -> {
        scrap.clear();
        scrap.addAll(b);
      }
      case "draft" -> draft = new Definition(draft.name(), draft.parameters(), b);
      default -> throw new IllegalArgumentException();
    }
  }

  /**
   * Releases the editor on close, disconnect or departure without interrupting a running program.
   */
  public boolean releaseEditor(int actor) {
    if (editorId != actor) return false;
    editorId = -1;
    revision++;
    return true;
  }

  /**
   * Starts a fresh interpreter; the runtime must reset all physical objects before asking next().
   */
  public boolean execute(int actor, Intent intent) {
    if (!current(intent) || editorId != actor || busy || intent.operation() != Operation.EXECUTE)
      return false;
    mainVariables.clear();
    mainVariables.put("kristalle", "0");
    stack.clear();
    stack.push(new Frame(List.copyOf(main), mainVariables, null));
    calls.clear();
    trace.clear();
    pending = null;
    instructions = 0;
    parameterReuse = false;
    returnedValueUsed = false;
    worldSolved = false;
    completed = false;
    busy = true;
    feedback = "Nox führt das Hauptprogramm aus.";
    revision++;
    executionRevision = revision;
    return true;
  }

  /** Produces one physical action; variables and calls are interpreted only when reached. */
  public Optional<Step> next() {
    if (!busy || pending != null) return Optional.empty();
    try {
      while (!stack.isEmpty()) {
        Frame frame = stack.peek();
        if (frame.pc >= frame.body.size()) {
          returnFrom(null);
          continue;
        }
        if (++instructions > 512)
          throw new IllegalArgumentException("Mehr als 512 Anweisungen. Lauf abgebrochen.");
        Block block = frame.body.get(frame.pc++);
        switch (block.action()) {
          case CALL -> {
            Definition method = definitions.get(block.method());
            if (method == null)
              throw new IllegalArgumentException("Methode nicht gebaut: " + block.method());
            if (method.parameters().size() != block.arguments().size())
              throw new IllegalArgumentException("Argumentanzahl passt nicht zu " + method.name());
            if (stack.size() >= 16)
              throw new IllegalArgumentException("Zu viele verschachtelte Aufrufe.");
            Map<String, String> locals = new LinkedHashMap<>();
            for (int i = 0; i < method.parameters().size(); i++)
              locals.put(
                  method.parameters().get(i), evaluate(block.arguments().get(i), frame.variables));
            int count = calls.merge(method.name(), 1, Integer::sum);
            if (!method.parameters().isEmpty() && count >= 2) parameterReuse = true;
            stack.push(new Frame(method.body(), locals, block));
          }
          case RETURN -> {
            String value = evaluate(block.operand(), frame.variables);
            returnFrom(value);
            trace.add(Step.action(Action.RETURN, numeric(value)));
            revision++;
          }
          case ASSIGN -> {
            String value = evaluate(block.operand(), frame.variables);
            assign(frame.variables, block.target(), value, block.mode());
            trace.add(Step.action(Action.ASSIGN, numeric(value)));
            revision++;
          }
          default -> {
            Step step =
                switch (block.action()) {
                  case TURN -> Step.turn(direction(evaluate(block.operand(), frame.variables)));
                  case MOVE, PLACE ->
                      Step.action(
                          block.action(), numeric(evaluate(block.operand(), frame.variables)));
                  default -> Step.action(block.action(), 0);
                };
            if (step.action() == Action.MOVE && (step.amount() < 1 || step.amount() > 32))
              throw new IllegalArgumentException("GEHE braucht eine Entfernung von 1 bis 32.");
            pending = block;
            trace.add(step);
            revision++;
            return Optional.of(step);
          }
        }
      }
    } catch (IllegalArgumentException | ArithmeticException ex) {
      fail(ex.getMessage());
    }
    return Optional.empty();
  }

  /** Delivers the observed physical result, notably the actual collection count, to this scope. */
  public void actionResult(boolean success, int value, String reason) {
    if (!busy || pending == null) return;
    if (!success) {
      fail(reason);
      return;
    }
    try {
      if (pending.action() == Action.COLLECT)
        assign(
            stack.peek().variables,
            pending.target(),
            Integer.toString(value),
            ResultMode.REPLACE);
      pending = null;
      revision++;
    } catch (IllegalArgumentException ex) {
      fail(ex.getMessage());
    }
  }

  private void returnFrom(String value) {
    Frame finished = stack.pop();
    if (finished.caller == null) return;
    Block call = finished.caller;
    if (!call.target().isBlank()) {
      if (value == null)
        throw new IllegalArgumentException(
            "Methode " + call.method() + " gibt keinen Wert zurück.");
      assign(stack.peek().variables, call.target(), value, call.mode());
      returnedValueUsed = true;
    }
  }

  /** True after the main program has returned and all physical actions have completed. */
  public boolean exhausted() {
    return busy && pending == null && stack.isEmpty();
  }

  /** Evaluates the visible goal against observed world state after normal program termination. */
  public void finish(boolean solved, int inventory) {
    if (!exhausted()) return;
    worldSolved = solved;
    completed =
        solved
            && inventory == 0
            && state().crystals() == 0
            && main.size() <= 8
            && parameterReuse
            && returnedValueUsed;
    busy = false;
    feedback =
        completed
            ? "Kontrollrune aktiv. Nebenausgang offen."
            : solved
                ? "Arbeitsstellen gelöst. Kontrollrune: höchstens 8 Hauptblöcke, parametrisierte Methode mehrfach aufrufen, Rückgabe übernehmen, kristalle = 0."
                : "Programm beendet. Noch nicht alle Arbeitsstellen sind gelöst.";
    revision++;
  }

  /** Keeps the last trace and physical effects visible after a runtime error. */
  public void fail(String reason) {
    busy = false;
    pending = null;
    errors++;
    feedback = reason == null ? "Lauf abgebrochen." : reason;
    revision++;
  }

  /** Accepts stop snapshots from this run despite progress; the runtime also cancels movement. */
  public boolean stop(int actor, Intent intent) {
    if (intent == null
        || editorId != actor
        || !busy
        || intent.operation() != Operation.STOP
        || intent.stage() != 0
        || intent.value() == null
        || intent.revision() < executionRevision
        || intent.revision() > revision) return false;
    fail("Programm angehalten. Der nächste Start setzt die Welt zurück.");
    return true;
  }

  private static boolean identifier(String value) {
    return value != null && value.matches("[\\p{L}_][\\p{L}\\p{N}_]{0,39}");
  }

  private static void assign(
      Map<String, String> vars, String target, String value, ResultMode mode) {
    if (!identifier(target))
      throw new IllegalArgumentException("Ungültiger Variablenname: " + target);
    int amount = numeric(value);
    if (mode != ResultMode.REPLACE) {
      if (!vars.containsKey(target))
        throw new IllegalArgumentException("Unbekannte Variable: " + target);
      int current = numeric(vars.get(target));
      value =
          Integer.toString(
              mode == ResultMode.ADD
                  ? Math.addExact(current, amount)
                  : Math.subtractExact(current, amount));
    }
    vars.put(target, value);
  }

  private static int numeric(String value) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("Zahl erwartet: " + value);
    }
  }

  private static Direction direction(String value) {
    return switch (value) {
      case "LEFT", "LINKS" -> Direction.LEFT;
      case "RIGHT", "RECHTS" -> Direction.RIGHT;
      case "BACK", "HINTEN" -> Direction.BACK;
      default -> throw new IllegalArgumentException("Richtung erwartet: " + value);
    };
  }

  private static String evaluate(String expression, Map<String, String> vars) {
    String input = expression.trim();
    if (input.length() > 100 || input.isEmpty())
      throw new IllegalArgumentException("Ausdruck fehlt.");
    for (int i = input.length() - 1; i > 0; i--)
      if (input.charAt(i) == '+' || input.charAt(i) == '-') {
        int a = numeric(evaluate(input.substring(0, i), vars)),
            b = numeric(evaluate(input.substring(i + 1), vars));
        try {
          return Integer.toString(
              input.charAt(i) == '+' ? Math.addExact(a, b) : Math.subtractExact(a, b));
        } catch (ArithmeticException ex) {
          throw new IllegalArgumentException("Zahl zu groß.");
        }
      }
    if (input.matches("-?[0-9]+")) {
      numeric(input);
      return input;
    }
    if (List.of("LEFT", "RIGHT", "BACK", "LINKS", "RECHTS", "HINTEN").contains(input)) return input;
    String value = vars.get(input);
    if (value == null) throw new IllegalArgumentException("Unbekannte Variable: " + input);
    return value;
  }

  /** Renders the actual editable statement, including arguments and result assignment. */
  public static String blockSource(Block b) {
    return switch (b.action()) {
      case MOVE -> "GEHE(" + b.operand() + ")";
      case TURN -> "DREHE(" + b.operand() + ")";
      case OPEN_GATE -> "ÖFFNE()";
      case ACTIVATE_RUNE -> "AKTIVIERE()";
      case COLLECT -> b.target() + " = SAMMLE_ALLE()";
      case PLACE -> "LEGE_AB(" + b.operand() + ")";
      case RETURN -> "GIB_ZURÜCK " + b.operand();
      case ASSIGN -> b.target() + b.mode().operator() + b.operand();
      case CALL ->
          (b.target().isBlank() ? "" : b.target() + b.mode().operator())
              + b.method()
              + "("
              + String.join(", ", b.arguments())
              + ")";
    };
  }

  /** Renders a method signature and its stored body. */
  public static List<String> definitionSource(Definition d) {
    var lines = new ArrayList<String>();
    lines.add(d.name() + "(" + String.join(", ", d.parameters()) + ") {");
    d.body().forEach(b -> lines.add("  " + blockSource(b)));
    lines.add("}");
    return List.copyOf(lines);
  }
}
