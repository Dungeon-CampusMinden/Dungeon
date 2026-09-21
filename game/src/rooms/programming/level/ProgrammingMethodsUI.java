package rooms.programming.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import engine.Entity;
import engine.Game;
import engine.components.CameraComponent;
import engine.game.ECSManagement;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.systems.CameraSystem;
import engine.utils.CursorUtil;
import engine.utils.Cursors;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasLayout;
import feature.canvas.CanvasNode;
import feature.canvas.CanvasOptions;
import feature.canvas.CanvasSnapshot;
import feature.canvas.CanvasUI;
import feature.canvas.NodeOrigin;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import rooms.programming.modules.methods.MethodsRoute;
import rooms.programming.modules.methods.MethodsWorkshop;
import rooms.programming.modules.methods.MethodsWorkshop.Block;
import rooms.programming.modules.methods.MethodsWorkshop.Operation;
import rooms.programming.modules.methods.MethodsWorkshop.RunState;
import rooms.programming.modules.methods.MethodsWorkshop.State;
import tools.jackson.databind.json.JsonMapper;

/** A free code canvas and a world observation view share the same authoritative program. */
final class ProgrammingMethodsUI extends CanvasUI implements CursorUtil.CursorOverride {
  private static final JsonMapper JSON = JsonMapper.builder().build();
  static final float BOARD_LEFT = -2115;
  static final float BOARD_BOTTOM = -1717.5f;
  static final float BOARD_RIGHT = BOARD_LEFT + 5400;
  static final float BOARD_TOP = BOARD_BOTTOM + 3600;
  private final int viewer;
  private final Table shell = new Table();
  private final Table workspace = new Table();
  private final Table evaluation = new Table();
  private final Label feedback = ProgrammingUI.label("", 17, ProgrammingUI.GOLD);
  private final Label observation = ProgrammingUI.label("", 20, ProgrammingUI.TEXT);
  private final DragAndDrop dragging = new DragAndDrop();
  private boolean dropAllowed;
  private ProgrammingMethodsNode.Drag activeDrag;
  private final ArrayDeque<Edit> edits = new ArrayDeque<>();
  private final Map<Entity, CameraComponent> previousCameras = new LinkedHashMap<>();
  private final TextButton run;
  private final TextButton watch;
  private final TextButton helpButton;
  private State state;
  private long pending = -1;
  private Edit pendingEdit;
  private float pendingSeconds;
  private boolean clearingFocus;
  private boolean observing;
  private boolean help;
  private boolean positioned;
  private boolean closing;
  private boolean simplified;
  private ProgrammingHelpUI helpView;
  private Entity followed;
  private float previousZoom;

  private record Edit(
      Operation operation, Function<State, String> value, Consumer<State> acknowledged) {}

  ProgrammingMethodsUI(String dialogId, State initial, int viewer) {
    super(
        ProgrammingMethods.ID + "-blocks",
        new CanvasLayout(
            "",
            1,
            1,
            new CanvasOptions()
                .backgroundColor(ProgrammingUI.INK)
                .grid(32, true)
                .gridColor(com.badlogic.gdx.graphics.Color.valueOf("242c2e"))
                .zoom(.5f, 1.6f)
                .selectionEnabled(false)
                .multiSelectEnabled(false)
                .rubberBandEnabled(false)
                .keyboardShortcutsEnabled(false),
            false),
        snapshot(),
        dialogId,
        nodes());
    this.viewer = viewer;
    state = initial;
    // CanvasArea owns scroll focus; route window wheels before either native listener runs.
    area()
        .addCaptureListener(
            new InputListener() {
              @Override
              public boolean scrolled(
                  InputEvent event, float x, float y, float amountX, float amountY) {
                Actor target = area().hit(x, y, true);
                while (target != null && target != area()) {
                  if (target instanceof ProgrammingMethodsNode panel) {
                    panel.scroll(amountY);
                    event.stop();
                    return true;
                  }
                  target = target.getParent();
                }
                return false;
              }
            });
    clearChildren();
    setUserObject(Cursors.DEFAULT);
    shell.setFillParent(true);
    shell.top().pad(14);
    Table actions = new Table();
    helpButton =
        ProgrammingUI.referenceButton(
            "Hilfe",
            () -> {
              if (help) {
                helpView.returnToPuzzle();
                return;
              }
              if (!help)
                ProgrammingHelp.state().ifPresent(s -> helpEvent("help.open", s.puzzleId()));
              help = !help;
              layoutBody();
            });
    helpButton.setUserObject(Cursors.HELP);
    actions.add(helpButton).width(90).height(44);
    actions
        .add(
            ProgrammingUI.button(
                "Quest-Log",
                false,
                () ->
                    ProgrammingHelp.state()
                        .ifPresent(s -> helpEvent("help.questlog", s.puzzleId()))))
        .width(120)
        .height(44)
        .padLeft(8);
    helpView =
        new ProgrammingHelpUI(
            "Code ordnen: Zeilen ziehen. Strg-Klick wählt einzelne Zeilen, Umschalt-Klick einen Bereich.\n"
                + "Methode bauen: Namen und Eingaben festlegen, Code hineinziehen, Methode bauen wählen. Die neue Rune ins Hauptprogramm ziehen.\n"
                + "Werte ändern: ... an einer Zeile öffnen. Enter oder Verlassen des Felds speichert.\n"
                + "Arbeitsfläche: Mittlere Maustaste oder Leertaste + Ziehen. Mausrad über Hintergrund zoomt, über Code scrollt es.\n"
                + "Fenster: Titel ziehen; rechte Maustaste halten und ziehen zum Vergrößern.\n"
                + "Ausdrücke: Zahlen, Variablen, +, -, Klammern und eigene Methoden. Eine Methode hat höchstens "
                + MethodsWorkshop.MAX_METHOD_BLOCKS
                + " Zeilen.",
            this::helpEvent,
            () -> {
              help = false;
              layoutBody();
            },
            () -> editable() && pending < 0 && edits.isEmpty());
    run =
        ProgrammingUI.button(
            "Ausführen",
            true,
            () -> {
              if (state.busy()) send(Operation.STOP, "");
              else {
                send(Operation.EXECUTE, "");
                observing = true;
                layoutBody();
              }
            });
    actions.add(run).width(130).height(44).padLeft(8);
    watch =
        ProgrammingUI.button(
            "Raum ansehen",
            false,
            () -> {
              observing = !observing;
              layoutBody();
            });
    actions.add(watch).width(145).height(44).padLeft(8);
    Table heading = new Table();
    heading.setBackground(ProgrammingUI.background(ProgrammingUI.INK, false));
    heading
        .add(ProgrammingUI.header("Nox · Methodenwerkstatt", actions, this::requestClose))
        .growX()
        .row();
    shell.add(heading).growX().row();
    shell.add(evaluation).growX().row();
    shell.add(workspace).grow().minSize(0).padTop(10).row();
    addActor(shell);
    for (CanvasNode node : area().nodes())
      if (node instanceof ProgrammingMethodsNode panel) panel.attach(this);
    refreshPanels();
    layoutBody();
    update(initial);
  }

  private static List<CanvasNode> nodes() {
    return List.of(
        new ProgrammingMethodsNode("main", "Hauptprogramm", 410, 490).position(0, 0),
        new ProgrammingMethodsNode("draft", "Eigene Methode", 410, 490).position(435, 0),
        new ProgrammingMethodsNode("palette", "Bausteine & Runen", 300, 490).position(870, 0));
  }

  private static CanvasSnapshot snapshot() {
    return new CanvasSnapshot(
        nodes().stream()
            .map(CanvasNode::toState)
            .map(s -> s.withOrigin(NodeOrigin.DEFAULT))
            .toList());
  }

  private void helpEvent(String action, String puzzleId) {
    flushFields();
    area().fireServerEvent(action, new DialogResponseMessage.StringValue(puzzleId));
  }

  boolean simplified() {
    return simplified;
  }

  State state() {
    return state;
  }

  DragAndDrop dragging() {
    return dragging;
  }

  void dragStarted(ProgrammingMethodsNode.Drag drag) {
    activeDrag = drag;
    dropAllowed = false;
  }

  void dropAllowed(boolean allowed) {
    dropAllowed = allowed;
  }

  @Override
  public Optional<Cursors> cursorOverride() {
    for (CanvasNode node : area().nodes()) {
      if (node instanceof ProgrammingMethodsNode panel) {
        var cursor = panel.manipulationCursor();
        if (cursor.isPresent()) return cursor;
      }
    }
    if (!dragging.isDragging()) return Optional.empty();
    return Optional.of(
        dropAllowed && activeDrag != null && activeDrag.source().validDrag(activeDrag)
            ? activeDrag.copy() ? Cursors.COPY : Cursors.GRABBING
            : Cursors.DISABLED);
  }

  boolean editable() {
    return state.editorId() == viewer && !state.busy();
  }

  void send(Operation operation, String value) {
    send(operation, ignored -> value, ignored -> {});
  }

  void send(Operation operation, Function<State, String> value, Consumer<State> acknowledged) {
    if (state.editorId() != viewer || (state.busy() && operation != Operation.STOP)) return;
    flushFields();
    edits.add(new Edit(operation, value, acknowledged));
  }

  /** Commits the active text field before a local action removes or replaces its actor. */
  void flushFields() {
    if (clearingFocus || getStage() == null) return;
    clearingFocus = true;
    try {
      getStage().setKeyboardFocus(null);
    } finally {
      clearingFocus = false;
    }
  }

  void switchMethod(Operation operation, String value) {
    send(operation, value);
    for (CanvasNode node : area().nodes())
      if (node instanceof ProgrammingMethodsNode panel) panel.forgetMethodFields();
  }

  private void update(State next) {
    boolean changed = state == null || next.revision() != state.revision();
    state = next;
    if (changed) {
      if (pendingEdit != null && state.editorId() == viewer)
        pendingEdit.acknowledged().accept(state);
      pendingEdit = null;
      pending = -1;
      if (state.editorId() != viewer) {
        edits.clear();
        closing = false;
      }
      if (!dragging.isDragging()) refreshPanels();
      refreshEvaluation();
    }
    run.setText(state.busy() ? "Stoppen" : "Ausführen");
    run.setDisabled(state.editorId() != viewer);
    feedback.setText(
        state.feedback()
            + (state.editorId() != viewer ? " · Ein anderer Spieler bearbeitet den Code." : ""));
    observation.setText(observationText());
    observation.setColor(resultColor());
  }

  private Color resultColor() {
    if (state.completed()) return ProgrammingUI.SUCCESS;
    if (state.runState() == RunState.FAILED) return ProgrammingUI.ERROR;
    return state.runState() == RunState.FINISHED ? ProgrammingUI.GOLD : ProgrammingUI.TEXT;
  }

  private String failureLocation() {
    for (int i = 0; i < state.main().size(); i++) {
      String error = state.blockErrors().get(state.main().get(i).id());
      if (error != null) return "Hauptprogramm, Zeile " + (i + 1) + ": " + error;
    }
    return state.feedback();
  }

  private String observationText() {
    String title = state.resultTitle();
    return switch (state.runState()) {
      case FINISHED -> state.completed() ? title : title + "\n\n" + state.feedback();
      case FAILED ->
          title
              + "\n\n"
              + failureLocation()
              + "\n\nMit Zum Code kommst du zurück zur markierten Fehlerzeile.";
      case STOPPED -> title + "\n\nDer nächste Start setzt Nox und den Raum zurück.";
      case NOT_RUN, CHANGED -> title + "\n\nStarte das Hauptprogramm mit Ausführen.";
      case RUNNING ->
          title
              + (state.activeStep() >= 0 && state.activeStep() < state.trace().size()
                  ? "\nAktuell: " + MethodsRoute.source(state.trace().get(state.activeStep()))
                  : "")
              + "\n\nAktuelle Variablen:\n"
              + state.variables().entrySet().stream()
                  .map(entry -> entry.getKey() + " = " + entry.getValue())
                  .collect(java.util.stream.Collectors.joining("\n"));
    };
  }

  /** Keep the result visible above the canvas, independently of its pan and zoom. */
  private void refreshEvaluation() {
    evaluation.clearChildren();
    if (observing || help) {
      evaluation.pad(0);
      return;
    }
    evaluation.top().left().pad(12);
    evaluation.setBackground(ProgrammingUI.background(ProgrammingUI.SURFACE, false));
    evaluation
        .add(ProgrammingUI.label(state.resultTitle(), 21, resultColor()))
        .colspan(2)
        .growX()
        .padBottom(8)
        .row();
    if (state.runState() == RunState.FAILED)
      evaluation
          .add(ProgrammingUI.label(failureLocation(), 17, ProgrammingUI.ERROR))
          .colspan(2)
          .growX()
          .padBottom(8)
          .row();
    int index = 0;
    for (var check : state.checks()) {
      Color color =
          switch (check.status()) {
            case PASSED -> ProgrammingUI.SUCCESS;
            case FAILED -> ProgrammingUI.GOLD;
            case PENDING -> ProgrammingUI.MUTED;
          };
      String status =
          switch (check.status()) {
            case PASSED -> "Erfüllt";
            case FAILED -> "Offen";
            case PENDING -> "Ungeprüft";
          };
      Table item = new Table();
      item.top().left();
      item.add(ProgrammingUI.label(status, 16, color)).width(88).top().padRight(8);
      item.add(ProgrammingUI.label(check.message(), 16, ProgrammingUI.TEXT)).growX().minWidth(0);
      evaluation
          .add(item)
          .uniformX()
          .growX()
          .minWidth(0)
          .top()
          .padRight(index % 2 == 0 ? 24 : 0)
          .padBottom(5);
      if (++index % 2 == 0) evaluation.row();
    }
  }

  /** Rebuild targets together so the background remains behind every instruction target. */
  void refreshPanels() {
    reconcileLooseGroups();
    dragging.clear();
    for (CanvasNode node : area().nodes())
      if (node instanceof ProgrammingMethodsNode panel) panel.refresh();
    dragging.addTarget(
        new DragAndDrop.Target(area()) {
          @Override
          public boolean drag(
              DragAndDrop.Source source,
              DragAndDrop.Payload payload,
              float x,
              float y,
              int pointer) {
            boolean valid =
                payload.getObject() instanceof ProgrammingMethodsNode.Drag drag
                    && drag.source().validDrag(drag)
                    && freeCanvas(x, y);
            dropAllowed(valid);
            return valid;
          }

          @Override
          public void reset(DragAndDrop.Source source, DragAndDrop.Payload payload) {
            dropAllowed(false);
          }

          @Override
          public void drop(
              DragAndDrop.Source source,
              DragAndDrop.Payload payload,
              float x,
              float y,
              int pointer) {
            if (payload.getObject() instanceof ProgrammingMethodsNode.Drag drag
                && drag.source().validDrag(drag)
                && freeCanvas(x, y)) dropLoose(drag, area().areaToWorld(x, y));
          }
        });
  }

  private boolean freeCanvas(float x, float y) {
    if (help || observing || x < 0 || y < 0 || x >= area().getWidth() || y >= area().getHeight())
      return false;
    Vector2 world = area().areaToWorld(x, y);
    if (world.x < BOARD_LEFT
        || world.x > BOARD_RIGHT
        || world.y < BOARD_BOTTOM
        || world.y > BOARD_TOP) return false;
    Actor hit = area().hit(x, y, true);
    while (hit != null && hit != area()) {
      if (hit instanceof CanvasNode) return false;
      hit = hit.getParent();
    }
    return hit == area();
  }

  private void reconcileLooseGroups() {
    Set<String> available = new HashSet<>(state.scrap().stream().map(Block::id).toList());
    for (CanvasNode node : List.copyOf(area().nodes())) {
      if (!(node instanceof ProgrammingMethodsNode panel) || !panel.loose()) continue;
      List<String> ids = panel.looseIds().stream().filter(available::remove).toList();
      if (ids.isEmpty()) area().removeNode(panel);
      else panel.looseIds(ids);
    }
    int index =
        (int)
            area().nodes().stream()
                .filter(node -> node instanceof ProgrammingMethodsNode panel && panel.loose())
                .count();
    for (Block block : state.scrap()) {
      if (!available.contains(block.id())) continue;
      ProgrammingMethodsNode group =
          newLooseGroup(
              "loose-" + UUID.randomUUID(),
              BOARD_LEFT + 24 + (index % 13) * 400,
              -35 - (index / 13) * 65);
      index++;
      group.looseIds(List.of(block.id()));
      group.attach(this);
    }
  }

  private ProgrammingMethodsNode newLooseGroup(String id, float x, float top) {
    ProgrammingMethodsNode group = new ProgrammingMethodsNode(id, "", 362, 42);
    group.position(x, top - group.height());
    area().addNode(group);
    return group;
  }

  private void placeLoose(String groupId, String blockId, Vector2 position) {
    if (state.scrap().stream().noneMatch(block -> block.id().equals(blockId))) return;
    ProgrammingMethodsNode destination = null;
    for (CanvasNode node : area().nodes()) {
      if (!(node instanceof ProgrammingMethodsNode panel) || !panel.loose()) continue;
      if (panel.id().equals("methods-blocks-" + groupId)) destination = panel;
      else panel.looseIds(panel.looseIds().stream().filter(id -> !id.equals(blockId)).toList());
    }
    if (destination == null) destination = newLooseGroup(groupId, position.x, position.y);
    List<String> ids = new ArrayList<>(destination.looseIds());
    if (!ids.contains(blockId)) ids.add(blockId);
    destination.looseIds(ids);
    destination.attach(this);
  }

  private void dropLoose(ProgrammingMethodsNode.Drag drag, Vector2 position) {
    if (!drag.copy() && drag.source().loose() && drag.ids().equals(drag.source().looseIds())) {
      drag.source().position(position.x, position.y - drag.source().height());
      return;
    }
    String group = "loose-" + UUID.randomUUID();
    if (drag.copy()) {
      Set<String> previous = new HashSet<>();
      send(
          Operation.ADD_BLOCK,
          current -> {
            previous.addAll(current.scrap().stream().map(Block::id).toList());
            return JSON.writeValueAsString(
                Map.of(
                    "container", "scrap", "index", current.scrap().size(), "block", drag.block()));
          },
          next ->
              next.scrap().stream()
                  .filter(block -> !previous.contains(block.id()))
                  .findFirst()
                  .ifPresent(block -> placeLoose(group, block.id(), position)));
    } else if (drag.container().equals("scrap")) {
      for (String id : drag.ids()) placeLoose(group, id, position);
    } else {
      for (String id : drag.ids())
        send(
            Operation.MOVE_BLOCK,
            current ->
                JSON.writeValueAsString(
                    Map.of("container", "scrap", "index", current.scrap().size(), "id", id)),
            next -> placeLoose(group, id, position));
    }
  }

  private void layoutBody() {
    flushFields();
    workspace.clearChildren();
    workspace.top().left();
    helpButton.setChecked(help);
    shell.setBackground(observing ? null : ProgrammingUI.background(ProgrammingUI.INK, false));
    watch.setText(observing ? "Zum Code" : "Raum ansehen");
    refreshEvaluation();
    if (help) {
      workspace.add(helpView).grow().minSize(0);
      if (getStage() != null) getStage().setScrollFocus(null);
    } else if (observing) {
      workspace.bottom().left();
      Table box = new Table();
      box.pad(18).setBackground(ProgrammingUI.background(ProgrammingUI.INK, true));
      box.add(observation).width(440);
      workspace.add(box).left().bottom();
    } else {
      // CanvasArea is a Group, so a Stack supplies its viewport bounds through layout.
      Actor edge =
          new Actor() {
            @Override
            public void draw(Batch batch, float alpha) {
              Vector2 lower = area().worldToArea(BOARD_LEFT, BOARD_BOTTOM);
              Vector2 upper = area().worldToArea(BOARD_RIGHT, BOARD_TOP);
              float left = Math.max(0, lower.x), right = Math.min(getWidth(), upper.x);
              float bottom = Math.max(0, lower.y), top = Math.min(getHeight(), upper.y);
              if (left > 0)
                CanvasGraphics.fill(
                    batch, ProgrammingUI.INK, alpha, getX(), getY(), left, getHeight());
              if (right < getWidth())
                CanvasGraphics.fill(
                    batch,
                    ProgrammingUI.INK,
                    alpha,
                    getX() + right,
                    getY(),
                    getWidth() - right,
                    getHeight());
              if (bottom > 0)
                CanvasGraphics.fill(
                    batch, ProgrammingUI.INK, alpha, getX() + left, getY(), right - left, bottom);
              if (top < getHeight())
                CanvasGraphics.fill(
                    batch,
                    ProgrammingUI.INK,
                    alpha,
                    getX() + left,
                    getY() + top,
                    right - left,
                    getHeight() - top);
              if (lower.x >= 0 && lower.x < getWidth())
                CanvasGraphics.fill(
                    batch,
                    ProgrammingUI.GOLD,
                    alpha * .55f,
                    getX() + lower.x,
                    getY() + bottom,
                    1,
                    top - bottom);
              if (upper.x > 0 && upper.x <= getWidth())
                CanvasGraphics.fill(
                    batch,
                    ProgrammingUI.GOLD,
                    alpha * .55f,
                    getX() + upper.x - 1,
                    getY() + bottom,
                    1,
                    top - bottom);
              if (lower.y >= 0 && lower.y < getHeight())
                CanvasGraphics.fill(
                    batch,
                    ProgrammingUI.GOLD,
                    alpha * .55f,
                    getX() + left,
                    getY() + lower.y,
                    right - left,
                    1);
              if (upper.y > 0 && upper.y <= getHeight())
                CanvasGraphics.fill(
                    batch,
                    ProgrammingUI.GOLD,
                    alpha * .55f,
                    getX() + left,
                    getY() + upper.y - 1,
                    right - left,
                    1);
            }
          };
      edge.setTouchable(Touchable.disabled);
      workspace.add(new Stack(area(), edge)).grow().minSize(0);
      if (getStage() != null) getStage().setScrollFocus(area());
    }
    if (!observing) restoreCamera();
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    if (!ancestorsVisible()) {
      restoreCamera();
      return;
    }
    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) requestClose();
    ProgrammingMethods.state().ifPresent(this::update);
    boolean aid = ProgrammingHelpUI.simplified("methods");
    if (aid != simplified && !dragging.isDragging()) {
      flushFields();
      simplified = aid;
      refreshPanels();
    }
    if (pending >= 0 && (pendingSeconds += delta) > 2) {
      feedback.setText("Warte auf Bestätigung. Deine Änderungen bleiben vorgemerkt.");
    }
    // A queued control may be obsolete after the preceding control or the run finishes.
    while (pending < 0
        && !edits.isEmpty()
        && ((edits.peekFirst().operation() == Operation.STOP && !state.busy())
            || (edits.peekFirst().operation() == Operation.EXECUTE && state.busy()))) {
      edits.removeFirst();
    }
    if (closing && pending < 0 && edits.isEmpty()) {
      closing = false;
      super.requestClose();
    }
    if (pending < 0 && !edits.isEmpty()) {
      Edit edit = edits.removeFirst();
      pendingEdit = edit;
      pending = state.revision();
      pendingSeconds = 0;
      area()
          .fireServerEvent(
              "intent",
              new DialogResponseMessage.StringValue(
                  ProgrammingMethods.encodeIntent(
                      new MethodsWorkshop.Intent(
                          state.revision(),
                          state.stage(),
                          edit.operation(),
                          edit.value().apply(state)))));
    }
    if (observing) followNox();
    else restoreCamera();
    constrainView();
  }

  private void constrainView() {
    float zoom = area().zoom();
    Vector2 pan = area().pan();
    float x = boundedPan(pan.x, area().getWidth(), BOARD_LEFT * zoom, BOARD_RIGHT * zoom);
    float y = boundedPan(pan.y, area().getHeight(), BOARD_BOTTOM * zoom, BOARD_TOP * zoom);
    if (x != pan.x || y != pan.y) area().pan(x, y);
  }

  private static float boundedPan(float pan, float viewport, float lower, float upper) {
    return viewport >= upper - lower
        ? (viewport - lower - upper) / 2
        : Math.max(viewport - upper, Math.min(pan, -lower));
  }

  @Override
  public void draw(Batch batch, float alpha) {
    setSize(Game.windowWidth(), Game.windowHeight());
    shell.validate();
    workspace.validate();
    if (!positioned && area().getWidth() > 10) {
      float left = Float.POSITIVE_INFINITY, bottom = Float.POSITIVE_INFINITY;
      float right = Float.NEGATIVE_INFINITY, top = Float.NEGATIVE_INFINITY;
      for (CanvasNode node : area().nodes()) {
        left = Math.min(left, node.x());
        bottom = Math.min(bottom, node.y());
        right = Math.max(right, node.x() + node.width());
        top = Math.max(top, node.y() + node.height());
      }
      area()
          .zoom(
              Math.min(
                  1,
                  Math.min(
                      (area().getWidth() - 48) / (right - left),
                      (area().getHeight() - 48) / (top - bottom))));
      float zoom = area().zoom();
      area()
          .pan(
              (area().getWidth() - (left + right) * zoom) / 2,
              (area().getHeight() - (bottom + top) * zoom) / 2);
      positioned = true;
    }
    constrainView();
    super.draw(batch, alpha);
  }

  @Override
  public void requestClose() {
    if (helpView != null) helpView.dismissConfirmation();
    flushFields();
    closing = true;
    if (pending < 0 && edits.isEmpty()) {
      closing = false;
      super.requestClose();
    }
  }

  private void followNox() {
    if (followed != null || getStage() == null) return;
    ProgrammingTerminal.state()
        .flatMap(t -> Game.findEntityById(t.golemId()))
        .ifPresent(
            golem -> {
              ECSManagement.entities()
                  .filter(e -> e.isPresent(CameraComponent.class))
                  .toList()
                  .forEach(
                      e -> {
                        previousCameras.put(e, e.fetch(CameraComponent.class).orElseThrow());
                        e.remove(CameraComponent.class);
                      });
              followed = golem;
              previousZoom = CameraSystem.camera().zoom;
              CameraSystem.camera().zoom = previousZoom * 1.2f;
              golem.add(new CameraComponent());
            });
  }

  private void restoreCamera() {
    if (followed == null) return;
    followed.remove(CameraComponent.class);
    followed = null;
    CameraSystem.camera().zoom = previousZoom;
    previousCameras.forEach(Entity::add);
    previousCameras.clear();
  }

  @Override
  public void setVisible(boolean visible) {
    if (!visible) restoreCamera();
    super.setVisible(visible);
  }

  @Override
  protected void setStage(Stage stage) {
    if (stage == null) restoreCamera();
    super.setStage(stage);
  }
}
