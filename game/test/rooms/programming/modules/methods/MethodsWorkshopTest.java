package rooms.programming.modules.methods;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import rooms.programming.modules.methods.MethodsRoute.Action;
import rooms.programming.modules.methods.MethodsRoute.Direction;
import rooms.programming.modules.methods.MethodsRoute.Step;
import rooms.programming.modules.methods.MethodsWorkshop.Block;
import rooms.programming.modules.methods.MethodsWorkshop.CheckStatus;
import rooms.programming.modules.methods.MethodsWorkshop.Edit;
import rooms.programming.modules.methods.MethodsWorkshop.Intent;
import rooms.programming.modules.methods.MethodsWorkshop.Operation;
import rooms.programming.modules.methods.MethodsWorkshop.ResultMode;
import rooms.programming.modules.methods.MethodsWorkshop.RunState;
import tools.jackson.databind.json.JsonMapper;

class MethodsWorkshopTest {
  private static final JsonMapper JSON = JsonMapper.builder().build();

  @ParameterizedTest
  @CsvSource({"0, 0", "16, 0", "0, 3", "16, 3"})
  void expandedProgramRunsBeforeRefactoringAndReceivesActualCollectionValues(
      int stock, int carried) {
    var workshop = claimed();
    if (stock != 0)
      add(workshop, "main", block(Action.ASSIGN, Integer.toString(stock), "kristalle"));
    List<Block> original = workshop.state().main();
    assertEquals("\u00d6FFNE()", MethodsWorkshop.blockSource(original.getFirst()));
    assertTrue(workshop.state().feedback().contains("zur\u00fcck"));
    assertTrue(workshop.execute(1, intent(workshop, Operation.EXECUTE, "")));
    run(workshop, 3, 5);
    workshop.finish(true, carried);
    assertTrue(workshop.state().worldSolved());
    assertFalse(workshop.state().completed());
    assertFalse(workshop.state().compact());
    assertEquals(stock, workshop.state().crystals());
    assertEquals(original, workshop.state().main());
    var checks = workshop.state().checks();
    assertEquals(RunState.FINISHED, workshop.state().runState());
    assertEquals(CheckStatus.PASSED, checks.get(0).status());
    assertEquals(carried == 0 ? CheckStatus.PASSED : CheckStatus.FAILED, checks.get(1).status());
    assertEquals(stock == 0 ? CheckStatus.PASSED : CheckStatus.FAILED, checks.get(2).status());
    assertEquals(CheckStatus.FAILED, checks.get(3).status());
    assertEquals(CheckStatus.FAILED, checks.get(4).status());
    assertEquals(CheckStatus.FAILED, checks.get(5).status());
    assertTrue(
        workshop.state().feedback().contains(original.size() + " Blöcke, höchstens 8 erlaubt"));
    if (stock != 0) {
      assertTrue(workshop.state().feedback().contains("Variable kristalle: 16. Erwartet: 0."));
    }
    if (carried != 0)
      assertTrue(workshop.state().feedback().contains("Nox trägt 3 Kristalle. Erwartet: 0."));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "1 + %s - 1", "function_xy(%s, -1) - 1", "(%s + identity(1)) - 1"})
  void eightCallsExecuteReusableMethodsWithScopedArgumentsAndReturnedStock(String expression) {
    var workshop = canonical();
    if (!expression.isEmpty()) {
      build(workshop, "identity", "wert", List.of(block(Action.RETURN, "wert", "")));
      build(
          workshop,
          "function_xy",
          "wert, abzug",
          List.of(block(Action.RETURN, "identity(wert) - abzug", "")));
      for (Block old : workshop.state().main()) {
        if (old.target().isBlank()) continue;
        String argument =
            old.arguments().isEmpty() ? "" : "function_xy(" + old.arguments().getFirst() + ", 0)";
        String invocation = old.method() + "(" + argument + ")";
        String operand = expression.formatted(invocation);
        operand =
            switch (old.mode()) {
              case REPLACE -> operand;
              case ADD -> "kristalle + (" + operand + ")";
              case SUBTRACT -> "kristalle - (" + operand + ")";
            };
        edit(
            workshop,
            Operation.EDIT_BLOCK,
            new Edit(
                old.id(),
                null,
                0,
                new Block(
                    old.id(),
                    Action.ASSIGN,
                    operand,
                    old.target(),
                    "",
                    List.of(),
                    ResultMode.REPLACE)));
      }
    }
    assertTrue(workshop.execute(1, intent(workshop, Operation.EXECUTE, "")));
    List<Step> steps = run(workshop, 3, 5);
    workshop.finish(true, 0);
    assertTrue(workshop.state().completed(), workshop.state().feedback());
    assertTrue(
        workshop.state().checks().stream().allMatch(check -> check.status() == CheckStatus.PASSED));
    assertTrue(workshop.state().resultTitle().contains("Geschafft!"));
    assertTrue(workshop.state().parameterReuse());
    assertTrue(workshop.state().returnedValueUsed());
    assertEquals(0, workshop.state().crystals());
    assertFalse(workshop.state().variables().containsKey("menge"));
    assertFalse(workshop.state().variables().containsKey("gesammelt"));
    var expected = new ArrayList<Step>();
    for (var station : MethodsRoute.STATIONS)
      for (var step : station.body())
        expected.add(step.action() == Action.COLLECT ? Step.action(Action.COLLECT, 0) : step);
    assertEquals(expected, steps);
    edit(workshop, Operation.NAME, "neuerEntwurf");
    assertTrue(workshop.state().completed());
    assertEquals(RunState.FINISHED, workshop.state().runState());
    add(workshop, "main", block(Action.MOVE, "1", ""));
    assertFalse(workshop.state().completed());
    assertEquals(RunState.CHANGED, workshop.state().runState());
    assertEquals(CheckStatus.PENDING, workshop.state().checks().get(2).status());
  }

  @Test
  void retryClearsExecutionStateButPreservesTheProgram() {
    var workshop = canonical();
    List<Block> code = workshop.state().main();
    workshop.execute(1, intent(workshop, Operation.EXECUTE, ""));
    assertEquals(Action.OPEN_GATE, workshop.next().orElseThrow().action());
    workshop.actionResult(false, 0, "Kein Tor an dieser Position.");
    assertFalse(workshop.state().busy());
    assertEquals(1, workshop.state().errors());
    var errors = Map.of(code.getFirst().id(), "Kein Tor an dieser Position.");
    assertEquals(errors, workshop.state().blockErrors());
    assertEquals(RunState.FAILED, workshop.state().runState());
    assertEquals(CheckStatus.PENDING, workshop.state().checks().get(0).status());
    assertTrue(workshop.releaseEditor(1));
    edit(workshop, Operation.CLAIM, "");
    assertEquals(errors, workshop.state().blockErrors());
    var failed = workshop.state();
    assertEquals(
        failed, JSON.readValue(JSON.writeValueAsString(failed), MethodsWorkshop.State.class));
    assertTrue(workshop.execute(1, intent(workshop, Operation.EXECUTE, "")));
    assertTrue(workshop.state().blockErrors().isEmpty());
    assertEquals(RunState.RUNNING, workshop.state().runState());
    assertEquals(0, workshop.state().crystals());
    run(workshop, 3, 5);
    workshop.finish(true, 0);
    assertTrue(workshop.state().completed());
    assertEquals(code, workshop.state().main());
  }

  @Test
  void stopAcceptsProgressWithinThisRunButRejectsEarlierRunsAndOtherPlayers() {
    var workshop = claimed();
    assertTrue(workshop.execute(1, intent(workshop, Operation.EXECUTE, "")));
    assertEquals(Action.OPEN_GATE, workshop.next().orElseThrow().action());
    Intent stopAtGate = intent(workshop, Operation.STOP, "");
    Intent editAtGate = intent(workshop, Operation.NAME, "stale");
    workshop.actionResult(true, 0, "");
    assertEquals(Action.MOVE, workshop.next().orElseThrow().action());
    assertTrue(workshop.state().revision() > stopAtGate.revision());
    assertFalse(workshop.stop(2, stopAtGate));
    assertTrue(workshop.stop(1, stopAtGate));
    assertEquals(RunState.STOPPED, workshop.state().runState());
    assertFalse(workshop.state().busy());
    assertTrue(workshop.state().blockErrors().isEmpty());
    assertFalse(workshop.apply(1, editAtGate));

    assertTrue(workshop.execute(1, intent(workshop, Operation.EXECUTE, "")));
    Intent currentRunStop = intent(workshop, Operation.STOP, "");
    workshop.next();
    assertFalse(workshop.stop(1, stopAtGate));
    assertFalse(
        workshop.stop(1, new Intent(workshop.state().revision() + 1, 0, Operation.STOP, "")));
    assertFalse(workshop.stop(2, currentRunStop));
    assertTrue(workshop.state().busy());
    assertTrue(workshop.stop(1, currentRunStop));
  }

  @Test
  void wrongDirectionIsExecutedAndPhysicalFailureStopsTheProgram() {
    var workshop = canonical();
    Block old = workshop.state().main().get(2);
    Block wrong =
        new Block(
            old.id(),
            old.action(),
            old.operand(),
            old.target(),
            old.method(),
            List.of("BACK"),
            ResultMode.REPLACE);
    edit(workshop, Operation.EDIT_BLOCK, new Edit(old.id(), null, 0, wrong));
    workshop.execute(1, intent(workshop, Operation.EXECUTE, ""));
    Step step;
    do {
      step = workshop.next().orElseThrow();
      workshop.actionResult(true, 0, "");
    } while (step.action() != Action.TURN);
    assertEquals(Direction.BACK, step.direction());
    assertEquals(Action.MOVE, workshop.next().orElseThrow().action());
    workshop.actionResult(false, 0, "Wand im Weg.");
    assertFalse(workshop.state().busy());
    assertFalse(workshop.state().completed());
    assertEquals("Wand im Weg.", workshop.state().feedback());
    assertEquals(Map.of(old.id(), "Wand im Weg."), workshop.state().blockErrors());
    edit(workshop, Operation.EDIT_BLOCK, new Edit(old.id(), null, 0, old));
    assertTrue(workshop.state().blockErrors().isEmpty());
    assertEquals(RunState.CHANGED, workshop.state().runState());
    assertEquals(CheckStatus.PENDING, workshop.state().checks().get(2).status());
  }

  @Test
  void individualBlocksCanBeExtractedReorderedAndLeftDisconnected() {
    var workshop = claimed();
    Block first = workshop.state().main().getFirst();
    edit(workshop, Operation.MOVE_BLOCK, new Edit(first.id(), "draft", 0, null));
    assertEquals(first, workshop.state().draft().body().getFirst());
    assertFalse(workshop.state().main().contains(first));
    Block second = workshop.state().main().getFirst();
    edit(workshop, Operation.MOVE_BLOCK, new Edit(second.id(), "scrap", 0, null));
    edit(
        workshop,
        Operation.MOVE_BLOCK,
        new Edit(first.id(), "main", workshop.state().main().size(), null));
    assertEquals(first, workshop.state().main().getLast());
    assertEquals(second, workshop.state().scrap().getFirst());
    assertTrue(workshop.execute(1, intent(workshop, Operation.EXECUTE, "")));
    assertEquals(Action.OPEN_GATE, workshop.next().orElseThrow().action());
  }

  @Test
  void draftChangesOnlyTakeEffectAfterBuild() {
    var workshop = canonical();
    edit(workshop, Operation.EDIT_METHOD, "tor");
    Block old = workshop.state().draft().body().get(1);
    edit(workshop, Operation.EDIT_BLOCK, new Edit(old.id(), null, 0, block(Action.MOVE, "1", "")));
    workshop.execute(1, intent(workshop, Operation.EXECUTE, ""));
    workshop.next();
    workshop.actionResult(true, 0, "");
    assertEquals(4, workshop.next().orElseThrow().amount());
    assertTrue(workshop.stop(1, intent(workshop, Operation.STOP, "")));
    edit(workshop, Operation.BUILD, "");
    workshop.execute(1, intent(workshop, Operation.EXECUTE, ""));
    workshop.next();
    workshop.actionResult(true, 0, "");
    assertEquals(1, workshop.next().orElseThrow().amount());
    assertTrue(workshop.stop(1, intent(workshop, Operation.STOP, "")));
    while (workshop.state().draft().body().size() < 6)
      add(workshop, "draft", block(Action.MOVE, "1", ""));
    edit(workshop, Operation.BUILD, "");
    var sixLines = workshop.state().definitions().get("tor");
    assertEquals(6, sixLines.body().size());
    add(workshop, "draft", block(Action.RETURN, "1", ""));
    edit(workshop, Operation.BUILD, "");
    assertEquals(sixLines, workshop.state().definitions().get("tor"));
    assertEquals(7, workshop.state().draft().body().size());
    assertTrue(workshop.state().feedback().contains("7 Zeilen, höchstens 6 erlaubt"));
    edit(workshop, Operation.DELETE_BLOCK, workshop.state().draft().body().getFirst().id());
    edit(workshop, Operation.BUILD, "");
    assertEquals(workshop.state().draft(), workshop.state().definitions().get("tor"));
    var built = workshop.state().definitions().get("tor");
    String returnId = workshop.state().draft().body().getLast().id();
    edit(workshop, Operation.MOVE_BLOCK, new Edit(returnId, "draft", 2, null));
    var draft = workshop.state().draft();
    assertEquals(3, draft.unreachableBlocks().size());
    for (Block unreachable : draft.body().subList(3, 6))
      assertEquals(
          "Nicht erreichbar: Die Methode endet bereits in Zeile 3.",
          draft.unreachableBlocks().get(unreachable.id()));
    edit(workshop, Operation.BUILD, "");
    assertEquals(built, workshop.state().definitions().get("tor"));
    assertEquals(
        "Nicht erreichbar: Die Methode endet bereits in Zeile 3.", workshop.state().feedback());
    edit(workshop, Operation.MOVE_BLOCK, new Edit(returnId, "draft", 6, null));
    assertTrue(workshop.state().draft().unreachableBlocks().isEmpty());
    edit(workshop, Operation.BUILD, "");
    assertEquals(workshop.state().draft(), workshop.state().definitions().get("tor"));
  }

  @Test
  void unchangedDraftsDoNotAccumulateAndDeletionRecoversWorkspaceCapacity() {
    var workshop = canonical();
    edit(workshop, Operation.EDIT_METHOD, "tor");
    int total = workspaceSize(workshop);
    for (int i = 0; i < 100; i++) edit(workshop, Operation.EDIT_METHOD, "tor");
    assertEquals(total, workspaceSize(workshop));

    Block changed = workshop.state().draft().body().get(1);
    edit(
        workshop,
        Operation.EDIT_BLOCK,
        new Edit(changed.id(), null, 0, block(Action.MOVE, "2", "")));
    while (workspaceSize(workshop) < 256)
      edit(
          workshop,
          Operation.ADD_BLOCK,
          new Edit(null, "scrap", workshop.state().scrap().size(), block(Action.MOVE, "1", "")));
    var unsaved = workshop.state().draft();
    edit(workshop, Operation.EDIT_METHOD, "rune");
    assertEquals(unsaved, workshop.state().draft());
    assertEquals(256, workspaceSize(workshop));
    for (int i = 0; i < 4; i++)
      edit(workshop, Operation.DELETE_BLOCK, workshop.state().scrap().getFirst().id());
    edit(workshop, Operation.EDIT_METHOD, "rune");
    assertEquals("rune", workshop.state().draft().name());
    assertEquals(256, workspaceSize(workshop));
    assertTrue(workshop.state().scrap().containsAll(unsaved.body()));
    assertEquals("4", workshop.state().definitions().get("tor").body().get(1).operand());
  }

  private static int workspaceSize(MethodsWorkshop workshop) {
    var state = workshop.state();
    return state.main().size() + state.scrap().size() + state.draft().body().size();
  }

  @Test
  void localVariablesCannotReadTheCallersStockWithoutAParameter() {
    var workshop = canonical();
    edit(workshop, Operation.EDIT_METHOD, "sammeln");
    Block old = workshop.state().draft().body().getLast();
    edit(
        workshop,
        Operation.EDIT_BLOCK,
        new Edit(old.id(), null, 0, block(Action.RETURN, "kristalle", "")));
    edit(workshop, Operation.BUILD, "");
    workshop.execute(1, intent(workshop, Operation.EXECUTE, ""));
    run(workshop, 3, 5);
    assertFalse(workshop.state().busy());
    assertTrue(workshop.state().feedback().contains("Unbekannte Variable: kristalle"));
    assertEquals(
        Map.of(workshop.state().main().get(4).id(), "Unbekannte Variable: kristalle"),
        workshop.state().blockErrors());
    edit(workshop, Operation.BUILD, "");
    assertTrue(workshop.state().blockErrors().isEmpty());
  }

  @Test
  void malformedStaleAndForeignIntentsDoNotMutateTheProgram() {
    var workshop = claimed();
    var before = workshop.state();
    assertFalse(workshop.apply(2, intent(workshop, Operation.NAME, "fremd")));
    assertFalse(workshop.apply(1, new Intent(before.revision() - 1, 0, Operation.NEW_METHOD, "")));
    assertTrue(workshop.apply(1, intent(workshop, Operation.MOVE_BLOCK, "not json")));
    assertTrue(
        workshop.apply(
            1,
            intent(
                workshop,
                Operation.MOVE_BLOCK,
                JSON.writeValueAsString(new Edit("b0", "unknown", 0, null)))));
    assertEquals(before.main(), workshop.state().main());
    assertEquals(before.draft(), workshop.state().draft());
    var current = workshop.state();
    assertEquals(
        current, JSON.readValue(JSON.writeValueAsString(current), MethodsWorkshop.State.class));
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void recursiveProgramsStopAtTheExecutionBound(boolean expression) {
    var workshop = claimed();
    clearMain(workshop);
    build(
        workshop,
        "rekursiv",
        "",
        List.of(
            expression
                ? block(Action.RETURN, "1 + rekursiv()", "")
                : call("rekursiv", "", ResultMode.REPLACE)));
    add(
        workshop,
        "main",
        expression
            ? block(Action.ASSIGN, "rekursiv()", "kristalle")
            : call("rekursiv", "", ResultMode.REPLACE));
    workshop.execute(1, intent(workshop, Operation.EXECUTE, ""));
    assertTrue(workshop.next().isEmpty());
    assertFalse(workshop.state().busy());
    assertTrue(workshop.state().feedback().contains("verschachtelte"));
    assertEquals(
        Map.of(workshop.state().main().getFirst().id(), workshop.state().feedback()),
        workshop.state().blockErrors());
  }

  private static MethodsWorkshop canonical() {
    var workshop = claimed();
    clearMain(workshop);
    build(
        workshop, "tor", "", List.of(block(Action.OPEN_GATE, "", ""), block(Action.MOVE, "4", "")));
    build(
        workshop,
        "rune",
        "richtung",
        List.of(
            block(Action.MOVE, "1", ""),
            block(Action.TURN, "richtung", ""),
            block(Action.MOVE, "3", ""),
            block(Action.ACTIVATE_RUNE, "", "")));
    build(
        workshop,
        "sammeln",
        "",
        List.of(
            block(Action.MOVE, "1", ""),
            block(Action.COLLECT, "", "gesammelt"),
            block(Action.MOVE, "1", ""),
            block(Action.RETURN, "gesammelt", "")));
    build(
        workshop,
        "altar",
        "menge",
        List.of(
            block(Action.MOVE, "1", ""),
            block(Action.PLACE, "menge", ""),
            block(Action.MOVE, "1", ""),
            block(Action.RETURN, "menge", "")));
    for (Block block :
        List.of(
            call("tor", "", ResultMode.REPLACE),
            call("tor", "", ResultMode.REPLACE),
            call("rune", "", ResultMode.REPLACE, "RIGHT"),
            call("rune", "", ResultMode.REPLACE, "LEFT"),
            call("sammeln", "kristalle", ResultMode.REPLACE),
            call("sammeln", "kristalle", ResultMode.ADD),
            call("altar", "kristalle", ResultMode.SUBTRACT, "3"),
            call("altar", "kristalle", ResultMode.SUBTRACT, "5"))) add(workshop, "main", block);
    return workshop;
  }

  private static MethodsWorkshop claimed() {
    var w = new MethodsWorkshop();
    edit(w, Operation.CLAIM, "");
    return w;
  }

  private static void clearMain(MethodsWorkshop w) {
    for (Block b : List.copyOf(w.state().main()))
      edit(w, Operation.MOVE_BLOCK, new Edit(b.id(), "scrap", w.state().scrap().size(), null));
  }

  private static void build(MethodsWorkshop w, String name, String parameters, List<Block> body) {
    edit(w, Operation.NEW_METHOD, "");
    edit(w, Operation.NAME, name);
    edit(w, Operation.PARAMETER, parameters);
    body.forEach(b -> add(w, "draft", b));
    edit(w, Operation.BUILD, "");
  }

  private static Block block(Action action, String operand, String target) {
    return new Block("", action, operand, target, "", List.of(), ResultMode.REPLACE);
  }

  private static Block call(String method, String target, ResultMode mode, String... args) {
    return new Block("", Action.CALL, "", target, method, List.of(args), mode);
  }

  private static void add(MethodsWorkshop w, String container, Block b) {
    edit(
        w,
        Operation.ADD_BLOCK,
        new Edit(
            null,
            container,
            container.equals("main") ? w.state().main().size() : w.state().draft().body().size(),
            b));
  }

  private static Intent intent(MethodsWorkshop w, Operation op, String value) {
    return new Intent(w.state().revision(), 0, op, value);
  }

  private static void edit(MethodsWorkshop w, Operation op, Object value) {
    assertTrue(
        w.apply(1, intent(w, op, value instanceof String s ? s : JSON.writeValueAsString(value))),
        op + ": " + w.state().feedback());
  }

  private static List<Step> run(MethodsWorkshop w, int... amounts) {
    var steps = new ArrayList<Step>();
    int collected = 0;
    while (true) {
      var step = w.next();
      if (step.isEmpty()) return steps;
      steps.add(step.orElseThrow());
      int value = step.orElseThrow().action() == Action.COLLECT ? amounts[collected++] : 0;
      w.actionResult(true, value, "");
    }
  }
}
