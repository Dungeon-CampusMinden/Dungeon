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
import java.util.function.Consumer;
import rooms.programming.modules.methods.MethodsRoute.Action;
import rooms.programming.modules.methods.MethodsRoute.Direction;
import rooms.programming.modules.methods.MethodsRoute.Step;
import tools.jackson.databind.json.JsonMapper;

/** Server-owned block editor and incremental interpreter. Only connected main blocks execute. */
public final class MethodsWorkshop {
  /** Each instruction block, including calls and returns, counts toward this method limit. */
  public static final int MAX_METHOD_BLOCKS = 6;

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

    /** Every statement after the first unconditional return is unreachable. */
    public Map<String, String> unreachableBlocks() {
      Map<String, String> errors = new LinkedHashMap<>();
      int returnLine = 0;
      for (int index = 0; index < body.size(); index++) {
        Block block = body.get(index);
        if (returnLine > 0)
          errors.put(
              block.id(),
              "Nicht erreichbar: Die Methode endet bereits in Zeile " + returnLine + ".");
        else if (block.action() == Action.RETURN) returnLine = index + 1;
      }
      return Map.copyOf(errors);
    }
  }

  /**
   * JSON payload for block moves, insertion and replacement; containers are main, draft or scrap.
   */
  public record Edit(String id, String container, int index, Block block) {}

  /** Distinguishes a completed evaluation from an interrupted run or changed code. */
  public enum RunState {
    NOT_RUN,
    RUNNING,
    FINISHED,
    FAILED,
    STOPPED,
    CHANGED
  }

  /** A condition is only evaluated after normal program termination, except the block limit. */
  public enum CheckStatus {
    PASSED,
    FAILED,
    PENDING
  }

  /** One visible condition for opening the exit. */
  public record Check(CheckStatus status, String message) {}

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
      Map<String, String> blockErrors,
      List<Step> trace,
      int activeStep,
      Map<String, String> variables,
      boolean compact,
      boolean parameterReuse,
      boolean returnedValueUsed,
      boolean worldSolved,
      RunState runState,
      int remainingCrystals) {
    public State {
      main = List.copyOf(main);
      scrap = List.copyOf(scrap);
      definitions = Map.copyOf(definitions);
      blockErrors = Map.copyOf(blockErrors);
      trace = List.copyOf(trace);
      variables = Map.copyOf(variables);
    }

    /** Uses the same six conditions for the completion decision and the player's checklist. */
    public List<Check> checks() {
      boolean evaluated = runState == RunState.FINISHED;
      return List.of(
          check(
              evaluated,
              worldSolved,
              worldSolved && evaluated
                  ? "Alle Arbeitsstellen erledigt."
                  : "Alle Arbeitsstellen erledigen: Tore, Runen und beide Altäre."),
          check(
              evaluated,
              remainingCrystals == 0,
              evaluated
                  ? "Nox trägt " + remainingCrystals + " Kristalle. Erwartet: 0."
                  : "Nox soll am Ende keine Kristalle mehr tragen."),
          check(
              evaluated,
              crystals == 0,
              evaluated
                  ? "Variable kristalle: "
                      + crystals
                      + ". Erwartet: 0."
                      + (crystals == 0 ? "" : " Prüfe die Rechnung beim Sammeln und Ablegen.")
                  : "Die Variable kristalle muss am Ende 0 sein."),
          check(
              true,
              compact,
              "Hauptprogramm: "
                  + main.size()
                  + " Blöcke, höchstens 8 erlaubt."
                  + (compact ? "" : " Fasse Anweisungen in Methoden zusammen.")),
          check(
              evaluated,
              parameterReuse,
              parameterReuse && evaluated
                  ? "Methode mit Parametern mehrfach aufgerufen."
                  : "Rufe dieselbe Methode mit Parametern mindestens zweimal auf."),
          check(
              evaluated,
              returnedValueUsed,
              returnedValueUsed && evaluated
                  ? "Rückgabewert im Aufrufer verwendet."
                  : "Verwende einen Rückgabewert in einer Zuweisung oder einem Ausdruck."));
    }

    /** Short, explicit outcome shared by the canvas and observation view. */
    public String resultTitle() {
      return switch (runState) {
        case NOT_RUN -> "Noch nicht geprüft";
        case RUNNING -> "Programm läuft";
        case FINISHED ->
            completed
                ? "Geschafft! Nebenausgang offen."
                : "Noch nicht geschafft: "
                    + checks().stream()
                        .filter(check -> check.status() == CheckStatus.PASSED)
                        .count()
                    + " / 6 Bedingungen erfüllt";
        case FAILED -> "Ausführung wegen eines Fehlers abgebrochen";
        case STOPPED -> "Programm angehalten. Prüfung nicht abgeschlossen.";
        case CHANGED -> "Code geändert. Bitte erneut ausführen.";
      };
    }

    private static Check check(boolean evaluated, boolean passed, String message) {
      return new Check(
          !evaluated ? CheckStatus.PENDING : passed ? CheckStatus.PASSED : CheckStatus.FAILED,
          message);
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
  private RunState runState = RunState.NOT_RUN;
  private int remainingCrystals;
  private String feedback =
      "Verbinde dein Hauptprogramm. Jeder Start setzt Nox und alle Arbeitsstellen zurück.";
  private final List<Step> trace = new ArrayList<>();
  private final Deque<Frame> stack = new ArrayDeque<>();
  private final Map<String, Integer> calls = new HashMap<>();
  private final Map<String, String> mainVariables = new LinkedHashMap<>();
  private final Map<String, String> blockErrors = new LinkedHashMap<>();
  private String activeMainBlockId = "";
  private Block pending;

  private static final class Frame {
    final List<Block> body;
    final Map<String, String> variables;
    final Consumer<String> returnValue;
    final Deque<Expression> expressions = new ArrayDeque<>();
    final Deque<String> values = new ArrayDeque<>();
    Block active;
    int pc;

    Frame(List<Block> body, Map<String, String> variables, Consumer<String> returnValue) {
      this.body = body;
      this.variables = variables;
      this.returnValue = returnValue;
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
        blockErrors,
        trace,
        trace.size() - 1,
        stack.isEmpty() ? mainVariables : stack.peek().variables,
        main.size() <= 8,
        parameterReuse,
        returnedValueUsed,
        worldSolved,
        runState,
        remainingCrystals);
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
          if (draft.body().size() > MAX_METHOD_BLOCKS)
            return rejectEdit(
                "Methode zu lang: "
                    + draft.body().size()
                    + " Zeilen, höchstens "
                    + MAX_METHOD_BLOCKS
                    + " erlaubt.");
          var unreachable = draft.unreachableBlocks();
          if (!unreachable.isEmpty()) return rejectEdit(unreachable.values().iterator().next());
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
          invalidateResult();
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
        if (!main.equals(b)) invalidateResult();
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

  private void invalidateResult() {
    blockErrors.clear();
    completed = false;
    if (runState != RunState.NOT_RUN) {
      runState = RunState.CHANGED;
      feedback = "Code geändert. Führe das Hauptprogramm erneut aus, um es zu prüfen.";
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
    blockErrors.clear();
    activeMainBlockId = "";
    pending = null;
    instructions = 0;
    parameterReuse = false;
    returnedValueUsed = false;
    worldSolved = false;
    completed = false;
    busy = true;
    runState = RunState.RUNNING;
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
        if (frame.active == null) {
          if (frame.pc >= frame.body.size()) {
            returnFrom(null);
            continue;
          }
          frame.active = frame.body.get(frame.pc++);
          // Keep the caller selected while nested methods or physical actions are running.
          if (stack.size() == 1) activeMainBlockId = frame.active.id();
          if (++instructions > 512)
            throw new IllegalArgumentException("Mehr als 512 Anweisungen. Lauf abgebrochen.");
          switch (frame.active.action()) {
            case CALL -> frame.active.arguments().forEach(a -> parse(a, frame.expressions));
            case RETURN, ASSIGN, TURN, MOVE, PLACE ->
                parse(frame.active.operand(), frame.expressions);
            default -> {}
          }
        }
        if (!frame.expressions.isEmpty()) {
          evaluateNext(frame);
          continue;
        }
        Block block = frame.active;
        frame.active = null;
        switch (block.action()) {
          case CALL -> {
            invoke(
                block.method(),
                arguments(frame, block.arguments().size()),
                value -> {
                  if (!block.target().isBlank()) {
                    requireReturn(block.method(), value);
                    assign(frame.variables, block.target(), value, block.mode());
                    returnedValueUsed = true;
                  }
                });
          }
          case RETURN -> {
            String value = frame.values.pop();
            returnFrom(value);
            trace.add(Step.action(Action.RETURN, numeric(value)));
            revision++;
          }
          case ASSIGN -> {
            String value = frame.values.pop();
            assign(frame.variables, block.target(), value, block.mode());
            trace.add(Step.action(Action.ASSIGN, numeric(value)));
            revision++;
          }
          default -> {
            Step step =
                switch (block.action()) {
                  case TURN -> Step.turn(direction(frame.values.pop()));
                  case MOVE, PLACE -> Step.action(block.action(), numeric(frame.values.pop()));
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
            stack.peek().variables, pending.target(), Integer.toString(value), ResultMode.REPLACE);
      pending = null;
      revision++;
    } catch (IllegalArgumentException ex) {
      fail(ex.getMessage());
    }
  }

  private void returnFrom(String value) {
    Frame finished = stack.pop();
    if (finished.returnValue != null) finished.returnValue.accept(value);
  }

  private void invoke(String name, List<String> arguments, Consumer<String> returnValue) {
    Definition method = definitions.get(name);
    if (method == null) throw new IllegalArgumentException("Methode nicht gebaut: " + name);
    if (method.parameters().size() != arguments.size())
      throw new IllegalArgumentException("Argumentanzahl passt nicht zu " + name);
    if (stack.size() >= 16) throw new IllegalArgumentException("Zu viele verschachtelte Aufrufe.");
    Map<String, String> locals = new LinkedHashMap<>();
    for (int i = 0; i < arguments.size(); i++)
      locals.put(method.parameters().get(i), arguments.get(i));
    int count = calls.merge(name, 1, Integer::sum);
    if (!method.parameters().isEmpty() && count >= 2) parameterReuse = true;
    stack.push(new Frame(method.body(), locals, returnValue));
  }

  private static void requireReturn(String method, String value) {
    if (value == null)
      throw new IllegalArgumentException("Methode " + method + " gibt keinen Wert zurück.");
  }

  private static List<String> arguments(Frame frame, int count) {
    var arguments = new ArrayList<String>();
    for (int i = 0; i < count; i++) arguments.addFirst(frame.values.pop());
    return arguments;
  }

  /** True after the main program has returned and all physical actions have completed. */
  public boolean exhausted() {
    return busy && pending == null && stack.isEmpty();
  }

  /** Evaluates the visible goal against observed world state after normal program termination. */
  public void finish(boolean solved, int inventory) {
    if (!exhausted()) return;
    worldSolved = solved;
    remainingCrystals = inventory;
    runState = RunState.FINISHED;
    completed = state().checks().stream().allMatch(check -> check.status() == CheckStatus.PASSED);
    busy = false;
    feedback =
        completed
            ? "Alle Bedingungen erfüllt. Der Nebenausgang ist offen."
            : (solved
                    ? "Alle Arbeitsstellen sind erledigt. Für den Ausgang fehlt noch:\n"
                    : "Für den Ausgang fehlt noch:\n")
                + state().checks().stream()
                    .filter(check -> check.status() == CheckStatus.FAILED)
                    .map(check -> "- " + check.message())
                    .collect(java.util.stream.Collectors.joining("\n"));
    revision++;
  }

  /** Keeps the last trace and physical effects visible after a runtime error. */
  public void fail(String reason) {
    busy = false;
    runState = RunState.FAILED;
    pending = null;
    errors++;
    feedback = reason == null ? "Lauf abgebrochen." : reason;
    if (!activeMainBlockId.isEmpty()) blockErrors.put(activeMainBlockId, feedback);
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
    runState = RunState.STOPPED;
    blockErrors.clear();
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

  private sealed interface Expression {}

  private record Value(String text) implements Expression {}

  private record Arithmetic(char operator) implements Expression {}

  private record Function(String name, int argumentCount) implements Expression {}

  /**
   * Evaluates left to right, suspending the current block while a method runs physical actions.
   *
   * @param frame scope and saved progress of the current expression
   */
  private void evaluateNext(Frame frame) {
    switch (frame.expressions.removeFirst()) {
      case Value value -> {
        String input = value.text();
        if (input.matches("-?[0-9]+")) {
          numeric(input);
          frame.values.push(input);
        } else if (List.of("LEFT", "RIGHT", "BACK", "LINKS", "RECHTS", "HINTEN").contains(input)) {
          frame.values.push(input);
        } else {
          String result = frame.variables.get(input);
          if (result == null) throw new IllegalArgumentException("Unbekannte Variable: " + input);
          frame.values.push(result);
        }
      }
      case Arithmetic arithmetic -> {
        int b = numeric(frame.values.pop()), a = numeric(frame.values.pop());
        try {
          frame.values.push(
              Integer.toString(
                  arithmetic.operator() == '+' ? Math.addExact(a, b) : Math.subtractExact(a, b)));
        } catch (ArithmeticException ex) {
          throw new IllegalArgumentException("Zahl zu groß.");
        }
      }
      case Function function ->
          invoke(
              function.name(),
              arguments(frame, function.argumentCount()),
              value -> {
                requireReturn(function.name(), value);
                frame.values.push(value);
                returnedValueUsed = true;
              });
    }
  }

  private static void parse(String expression, Deque<Expression> output) {
    if (expression.isBlank() || expression.length() > 100)
      throw new IllegalArgumentException("Ausdruck fehlt.");
    var parser = new ExpressionParser(expression, output);
    parser.sum();
    parser.whitespace();
    if (parser.position != expression.length()) throw parser.invalid();
  }

  /** Compiles sums, parentheses and nested calls into operations whose progress survives a tick. */
  private static final class ExpressionParser {
    private final String input;
    private final Deque<Expression> output;
    private int position;

    ExpressionParser(String input, Deque<Expression> output) {
      this.input = input;
      this.output = output;
    }

    void sum() {
      atom();
      while (true) {
        if (take('+')) {
          atom();
          output.addLast(new Arithmetic('+'));
        } else if (take('-')) {
          atom();
          output.addLast(new Arithmetic('-'));
        } else return;
      }
    }

    private void atom() {
      if (take('(')) {
        sum();
        expect(')');
        return;
      }
      if (take('+')) {
        atom();
        return;
      }
      boolean negative = take('-');
      whitespace();
      int start = position;
      while (position < input.length()
          && input.charAt(position) >= '0'
          && input.charAt(position) <= '9') position++;
      if (position > start) {
        output.addLast(new Value((negative ? "-" : "") + input.substring(start, position)));
        return;
      }
      if (negative) {
        output.addLast(new Value("0"));
        atom();
        output.addLast(new Arithmetic('-'));
        return;
      }
      while (position < input.length()) {
        int character = input.codePointAt(position);
        if (!Character.isLetterOrDigit(character) && character != '_') break;
        position += Character.charCount(character);
      }
      String name = input.substring(start, position);
      if (!identifier(name)) throw invalid();
      if (!take('(')) {
        output.addLast(new Value(name));
        return;
      }
      int count = 0;
      if (!take(')')) {
        do {
          sum();
          count++;
        } while (take(','));
        expect(')');
      }
      output.addLast(new Function(name, count));
    }

    private boolean take(char character) {
      whitespace();
      if (position >= input.length() || input.charAt(position) != character) return false;
      position++;
      return true;
    }

    private void expect(char character) {
      if (!take(character)) throw invalid();
    }

    private void whitespace() {
      while (position < input.length() && Character.isWhitespace(input.charAt(position)))
        position++;
    }

    private IllegalArgumentException invalid() {
      return new IllegalArgumentException("Ungültiger Ausdruck: " + input);
    }
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
