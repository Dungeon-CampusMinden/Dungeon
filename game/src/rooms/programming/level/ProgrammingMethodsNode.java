package rooms.programming.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import engine.utils.FontHelper;
import engine.utils.Scene2dElementFactory;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasNode;
import feature.canvas.CanvasNodeType;
import feature.canvas.NodeState;
import feature.hud.UIUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import rooms.programming.modules.methods.MethodsRoute.Action;
import rooms.programming.modules.methods.MethodsWorkshop;
import rooms.programming.modules.methods.MethodsWorkshop.Block;
import rooms.programming.modules.methods.MethodsWorkshop.Definition;
import rooms.programming.modules.methods.MethodsWorkshop.Operation;
import rooms.programming.modules.methods.MethodsWorkshop.ResultMode;
import rooms.programming.modules.methods.MethodsWorkshop.State;
import tools.jackson.databind.json.JsonMapper;

/** Code windows and frameless loose groups share the same instruction rows and editors. */
final class ProgrammingMethodsNode extends CanvasNode {
  private static final JsonMapper JSON = JsonMapper.builder().build();
  private static final List<Block> ORIGINAL_PROGRAM = MethodsWorkshop.originalProgram();

  static void register() {
    if (!CanvasNodeType.isRegistered("programming.methods.blocks"))
      CanvasNodeType.register(
          "programming.methods.blocks",
          s ->
              new ProgrammingMethodsNode(
                  s.id().replace("methods-blocks-", ""), "Code", s.width(), s.height()));
  }

  @Override
  public String typeId() {
    return "programming.methods.blocks";
  }

  private final String container;
  private final String title;
  private List<String> looseIds = List.of();
  private final Table content = new Table();
  private final Map<String, String> drafts = new HashMap<>();
  private final Map<String, String> draftBases = new HashMap<>();
  private final Map<String, Integer> submissions = new HashMap<>();
  private final Set<String> selected = new HashSet<>();
  private final Map<String, CodeRow> rows = new HashMap<>();
  private String selectionAnchor = "";
  private int draftEditor = Integer.MIN_VALUE;
  private ProgrammingMethodsUI owner;
  private ScrollPane scroll;
  private float scrollY;
  private float alternateScrollY;
  private boolean showOriginal;
  private String expanded = "";
  private String revealedError = "";
  private boolean rebuilding;
  private String buildFailure = "";
  private Definition failedDraft;

  ProgrammingMethodsNode(String container, String title, float width, float height) {
    super("methods-blocks-" + container, width, height);
    this.container = container.startsWith("loose-") ? "scrap" : container;
    this.title = title;
    deletable(false);
    addCaptureListener(
        new InputListener() {
          private final Vector2 press = new Vector2();
          private final Vector2 current = new Vector2();
          private float initialWidth;
          private float initialHeight;
          private float top;
          private int resizingPointer = -1;

          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            if (loose() || button != Input.Buttons.RIGHT || resizingPointer >= 0) return false;
            resizingPointer = pointer;
            getParent().stageToLocalCoordinates(press.set(event.getStageX(), event.getStageY()));
            initialWidth = width();
            initialHeight = height();
            top = y() + height();
            event.stop();
            return true;
          }

          @Override
          public void touchDragged(InputEvent event, float x, float y, int pointer) {
            if (pointer != resizingPointer) return;
            getParent().stageToLocalCoordinates(current.set(event.getStageX(), event.getStageY()));
            float nextWidth =
                container.equals("draft")
                    ? Math.max(410, initialWidth + current.x - press.x)
                    : initialWidth;
            float nextHeight = Math.max(180, initialHeight - current.y + press.y);
            nextHeight = Math.min(nextHeight, top - ProgrammingMethodsUI.BOARD_BOTTOM);
            size(Math.min(nextWidth, ProgrammingMethodsUI.BOARD_RIGHT - x()), nextHeight);
            position(x(), top - height());
            event.stop();
          }

          @Override
          public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            if (pointer != resizingPointer || button != Input.Buttons.RIGHT) return;
            resizingPointer = -1;
            event.stop();
          }
        });
  }

  @Override
  public CanvasNode position(float x, float y) {
    return super.position(
        Math.max(
            ProgrammingMethodsUI.BOARD_LEFT,
            Math.min(x, ProgrammingMethodsUI.BOARD_RIGHT - width())),
        Math.max(
            ProgrammingMethodsUI.BOARD_BOTTOM,
            Math.min(y, ProgrammingMethodsUI.BOARD_TOP - height())));
  }

  void attach(ProgrammingMethodsUI owner) {
    this.owner = owner;
    if (loose()) {
      movable(false);
      refresh();
      return;
    }
    size(
        container.equals("palette")
            ? 300
            : container.equals("main")
                ? 410
                : Math.max(
                    410,
                    Math.min(
                        width(),
                        ProgrammingMethodsUI.BOARD_RIGHT - ProgrammingMethodsUI.BOARD_LEFT)),
        Math.max(
            180,
            Math.min(
                height(), ProgrammingMethodsUI.BOARD_TOP - ProgrammingMethodsUI.BOARD_BOTTOM)));
    position(x(), y());
    refresh();
  }

  boolean loose() {
    return id().startsWith("methods-blocks-loose-");
  }

  List<String> looseIds() {
    return looseIds;
  }

  void looseIds(List<String> ids) {
    if (!looseIds.equals(ids)) {
      looseIds = List.copyOf(ids);
      notifyStateChanged();
    }
  }

  @Override
  protected void writeProps(NodeState.Props props) {
    if (loose()) props.put("looseIds", String.join(",", looseIds));
  }

  @Override
  protected void readProps(NodeState state) {
    String ids = state.prop("looseIds", "");
    looseIds = ids.isEmpty() ? List.of() : List.of(ids.split(","));
  }

  private List<Block> panelBlocks(State state) {
    if (!loose()) return blocks(state, container);
    Map<String, Block> byId = new HashMap<>();
    state.scrap().forEach(block -> byId.put(block.id(), block));
    return looseIds.stream().filter(byId::containsKey).map(byId::get).toList();
  }

  void forgetMethodFields() {
    buildFailure = "";
    drafts.remove("method-name");
    drafts.remove("method-parameters");
    draftBases.remove("method-name");
    draftBases.remove("method-parameters");
    submissions.remove("method-name");
    submissions.remove("method-parameters");
  }

  void scroll(float amount) {
    if (scroll == null) return;
    scroll.validate();
    scroll.setScrollY(scroll.getScrollY() + amount * 42);
  }

  void refresh() {
    if (!owner.state().draft().equals(failedDraft)) buildFailure = "";
    reconcileDrafts();
    if (scroll != null) scrollY = scroll.getScrollY();
    Actor focus = getStage() == null ? null : getStage().getKeyboardFocus();
    String focusKey =
        focus instanceof TextField && focus.isDescendantOf(this) ? focus.getName() : null;
    int cursor = focusKey == null ? 0 : ((TextField) focus).getCursorPosition();
    rebuilding = true;
    rebuildContent();
    ensureContentBuilt();
    revealError();
    if (focusKey != null && getStage() != null) {
      Actor replacement = findActor(focusKey);
      if (replacement instanceof TextField field) {
        getStage().setKeyboardFocus(field);
        field.setCursorPosition(cursor);
      }
    }
    rebuilding = false;
  }

  private void revealError() {
    if (showOriginal) return;
    String failed =
        panelBlocks(owner.state()).stream()
            .map(Block::id)
            .filter(id -> rows.containsKey(id) && rows.get(id).failed)
            .findFirst()
            .orElse("");
    if (!failed.isEmpty() && !failed.equals(revealedError) && scroll != null) {
      scroll.validate();
      CodeRow row = rows.get(failed);
      scroll.scrollTo(0, row.getY(), row.getWidth(), row.getHeight());
      scroll.updateVisualScroll();
    }
    revealedError = failed;
  }

  // Keep local typing only until its server value changes or its block leaves this panel.
  private void reconcileDrafts() {
    int editor = owner.state().editorId();
    if (draftEditor != editor) {
      selected.clear();
      selectionAnchor = "";
      drafts.clear();
      draftBases.clear();
      submissions.clear();
      draftEditor = editor;
    }
    selected.retainAll(panelBlocks(owner.state()).stream().map(Block::id).toList());
    if (panelBlocks(owner.state()).stream().noneMatch(b -> b.id().equals(selectionAnchor)))
      selectionAnchor = "";
    Map<String, String> current = fieldValues(owner.state());
    submissions.keySet().retainAll(current.keySet());
    drafts
        .entrySet()
        .removeIf(
            entry -> {
              String server = current.get(entry.getKey());
              return server == null
                  || (server.equals(normalized(entry.getKey(), entry.getValue()))
                      && !submissions.containsKey(entry.getKey()))
                  || !server.equals(draftBases.get(entry.getKey()));
            });
    draftBases.keySet().retainAll(drafts.keySet());
  }

  private Map<String, String> fieldValues(State state) {
    Map<String, String> current = new HashMap<>();
    if (container.equals("draft")) {
      current.put("method-name", state.draft().name());
      current.put("method-parameters", String.join(", ", state.draft().parameters()));
    }
    List<Block> blocks = panelBlocks(state);
    for (Block block : blocks) {
      current.put(block.id() + ":operand", block.operand());
      current.put(block.id() + ":target", block.target());
      current.put(block.id() + ":arguments", String.join(", ", block.arguments()));
    }
    return current;
  }

  private static String normalized(String key, String value) {
    return key.equals("method-parameters") || key.endsWith(":arguments")
        ? String.join(", ", arguments(value))
        : value;
  }

  // Each queued edit acknowledges its own field before the panel consumes the new snapshot.
  private void acknowledgeField(String key, State state) {
    if (!submissions.containsKey(key)) return;
    int remaining = submissions.get(key) - 1;
    if (remaining == 0) submissions.remove(key);
    else submissions.put(key, remaining);
    String server = fieldValues(state).get(key);
    if (server == null) {
      drafts.remove(key);
      draftBases.remove(key);
      submissions.remove(key);
    } else if (drafts.containsKey(key)) {
      draftBases.put(key, server);
      if (remaining == 0 && server.equals(normalized(key, drafts.get(key)))) {
        drafts.remove(key);
        draftBases.remove(key);
      }
    }
  }

  @Override
  protected void buildContent() {
    if (owner == null) return;
    content.clearChildren();
    rows.clear();
    if (loose()) {
      content.top().left().pad(0);
      Table body = new Table();
      body.top().left();
      List<Block> blocks = panelBlocks(owner.state());
      for (int i = 0; i < blocks.size(); i++) blockRow(body, blocks.get(i), i, "");
      body.setWidth(width());
      float naturalHeight = Math.max(42, body.getPrefHeight());
      // Match the initial code-window height so even long loose groups stay usable on screen.
      float height = Math.min(490, naturalHeight);
      scroll = naturalHeight > height ? codeScroll(body) : null;
      content.add(scroll == null ? body : scroll).grow().minSize(0);
      addActor(content);
      float top = y() + height();
      size(width(), height);
      position(x(), top - height());
      content.setBounds(0, 0, width(), height());
      content.validate();
      if (scroll != null) {
        scroll.setScrollY(scrollY);
        scroll.updateVisualScroll();
      }
      return;
    }
    content.top().left().pad(12);
    int methodLines = owner.state().draft().body().size();
    boolean methodTooLong =
        container.equals("draft") && methodLines > MethodsWorkshop.MAX_METHOD_BLOCKS;
    Map<String, String> unreachable =
        container.equals("draft") ? owner.state().draft().unreachableBlocks() : Map.of();
    String count =
        switch (container) {
          case "main" -> " · " + owner.state().main().size() + " / 8";
          case "draft" -> " · " + methodLines + " / " + MethodsWorkshop.MAX_METHOD_BLOCKS;
          default -> "";
        };
    var heading =
        ProgrammingUI.zoomLabel(
            title + count,
            container.equals("main") ? 20 : 23,
            methodTooLong || !unreachable.isEmpty() ? ProgrammingUI.ERROR : ProgrammingUI.GOLD);
    Table header = new Table();
    header.add(heading).growX().minWidth(0);
    if (container.equals("main")) {
      TextButton toggle =
          ProgrammingUI.zoomButton(
              showOriginal ? "Mein Code" : "Original", showOriginal, this::toggleOriginal);
      toggle.setName("toggle-original-program");
      toggle.pad(4);
      header.add(toggle).width(106).height(30).padLeft(6);
    }
    content.add(header).growX().height(35).padBottom(8).row();
    if (showOriginal)
      content
          .add(
              ProgrammingUI.zoomLabel(
                  "Original · "
                      + ORIGINAL_PROGRAM.size()
                      + " Zeilen · nur Lesen\nAusführen startet dein eigenes Programm.",
                  15,
                  ProgrammingUI.MUTED))
          .growX()
          .padBottom(8)
          .row();
    if (methodTooLong)
      content
          .add(
              ProgrammingUI.zoomLabel(
                  "Entwurf zu lang: "
                      + methodLines
                      + " Zeilen, höchstens "
                      + MethodsWorkshop.MAX_METHOD_BLOCKS
                      + " erlaubt. Kürze die Methode, bevor du sie baust.",
                  16,
                  ProgrammingUI.ERROR))
          .growX()
          .padBottom(8)
          .row();
    if (!unreachable.isEmpty())
      content
          .add(
              ProgrammingUI.zoomLabel(
                  unreachable.values().iterator().next(), 16, ProgrammingUI.ERROR))
          .growX()
          .padBottom(8)
          .row();
    Table body = new Table();
    body.top().left();
    if (container.equals("main"))
      body.setBackground(ProgrammingUI.background(Color.valueOf("20282b"), false));
    body.padLeft(12).padRight(12);
    if (container.equals("palette")) palette(body);
    else {
      if (container.equals("draft")) methodHeader(body, !unreachable.isEmpty());
      List<Block> blocks =
          switch (container) {
            case "main" -> showOriginal ? ORIGINAL_PROGRAM : owner.state().main();
            case "draft" -> owner.state().draft().body();
            default -> owner.state().scrap();
          };
      for (int i = 0; i <= blocks.size(); i++) {
        if (!showOriginal) insertion(body, i, blocks.isEmpty());
        if (i < blocks.size())
          blockRow(body, blocks.get(i), i, unreachable.getOrDefault(blocks.get(i).id(), ""));
      }
    }
    scroll = codeScroll(body);
    content.add(scroll).grow().minSize(0);
    addActor(content);
    content.setBounds(0, 0, width(), height());
    content.validate();
    scroll.setScrollY(scrollY);
    scroll.updateVisualScroll();
  }

  private void toggleOriginal() {
    owner.flushFields();
    float previousScroll = scroll == null ? scrollY : scroll.getScrollY();
    scrollY = alternateScrollY;
    alternateScrollY = previousScroll;
    scroll = null;
    showOriginal = !showOriginal;
    selected.clear();
    selectionAnchor = "";
    expanded = "";
    owner.refreshPanels();
  }

  private ScrollPane codeScroll(Table body) {
    ScrollPane pane = new ScrollPane(body, UIUtils.defaultSkin());
    var style = new ScrollPane.ScrollPaneStyle(pane.getStyle());
    style.background = null;
    pane.setStyle(style);
    pane.setScrollingDisabled(true, false);
    pane.setFadeScrollBars(false);
    pane.setFlickScroll(false);
    return pane;
  }

  private void methodHeader(Table table, boolean unreachable) {
    var definition = owner.state().draft();
    table.add(ProgrammingUI.zoomLabel("Name", 15, ProgrammingUI.MUTED)).growX().row();
    table
        .add(
            field(
                "method-name",
                definition.name(),
                (value, ack) -> owner.send(Operation.NAME, ignored -> value, ack)))
        .growX()
        .height(36)
        .row();
    table
        .add(ProgrammingUI.zoomLabel("Eingaben · durch Kommas getrennt", 15, ProgrammingUI.MUTED))
        .growX()
        .padTop(5)
        .row();
    table
        .add(
            field(
                "method-parameters",
                String.join(", ", definition.parameters()),
                (value, ack) -> owner.send(Operation.PARAMETER, ignored -> value, ack)))
        .growX()
        .height(36)
        .row();
    Table controls = new Table();
    TextButton build =
        ProgrammingUI.zoomButton(
            "Methode bauen",
            true,
            () ->
                owner.send(
                    Operation.BUILD,
                    ignored -> "",
                    next -> {
                      buildFailure =
                          next.draft().equals(next.definitions().get(next.draft().name()))
                              ? ""
                              : next.feedback();
                      failedDraft = next.draft();
                    }));
    build.setDisabled(
        !owner.editable()
            || definition.body().size() > MethodsWorkshop.MAX_METHOD_BLOCKS
            || unreachable);
    controls.add(build).growX();
    controls
        .add(
            ProgrammingUI.zoomButton(
                "Neue Methode",
                false,
                () -> {
                  owner.switchMethod(Operation.NEW_METHOD, "");
                }))
        .growX()
        .padLeft(6);
    table.add(controls).growX().padTop(8).padBottom(10).row();
    if (!buildFailure.isEmpty())
      table
          .add(ProgrammingUI.zoomLabel(buildFailure, 17, ProgrammingUI.GOLD))
          .growX()
          .padBottom(10)
          .row();
  }

  private void palette(Table table) {
    table
        .add(ProgrammingUI.zoomLabel("Anweisungen hier herausziehen", 16, ProgrammingUI.MUTED))
        .growX()
        .padBottom(10)
        .row();
    for (Action action : Action.values()) {
      if (action == Action.CALL) continue;
      Block block =
          new Block(
              "",
              action,
              action == Action.TURN ? "RECHTS" : "1",
              action == Action.COLLECT ? "gesammelt" : action == Action.ASSIGN ? "kristalle" : "",
              "",
              List.of(),
              ResultMode.REPLACE);
      paletteRow(table, caption(action), block);
    }
    table
        .add(ProgrammingUI.zoomLabel("Gebaute Methoden", 22, ProgrammingUI.GOLD))
        .growX()
        .padTop(20)
        .padBottom(8)
        .row();
    if (owner.state().definitions().isEmpty())
      table
          .add(
              ProgrammingUI.zoomLabel(
                  "Baue deine erste Methode im Methodenfenster.", 17, ProgrammingUI.MUTED))
          .growX()
          .row();
    owner
        .state()
        .definitions()
        .forEach(
            (name, definition) -> {
              paletteRow(
                  table,
                  "Rune: " + name + "(" + String.join(", ", definition.parameters()) + ")",
                  new Block(
                      "",
                      Action.CALL,
                      "",
                      "",
                      name,
                      definition.parameters().stream().map(p -> "1").toList(),
                      ResultMode.REPLACE));
              table
                  .add(
                      ProgrammingUI.zoomButton(
                          "Bearbeiten",
                          false,
                          () -> {
                            owner.switchMethod(Operation.EDIT_METHOD, name);
                          }))
                  .right()
                  .width(120)
                  .height(32)
                  .padBottom(12)
                  .row();
            });
  }

  private void paletteRow(Table table, String text, Block block) {
    CodeRow row = new CodeRow(block.action() == Action.CALL);
    row.add(
            ProgrammingUI.zoomLabel(
                text, 18, block.action() == Action.CALL ? ProgrammingUI.GOLD : ProgrammingUI.TEXT))
        .growX()
        .minWidth(0);
    table.add(row).growX().minHeight(42).padBottom(6).row();
    source(row, block, true);
  }

  private void blockRow(Table table, Block block, int index, String staticError) {
    CodeRow row = new CodeRow(block.action() == Action.CALL);
    if (showOriginal) {
      row.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
      row.add(ProgrammingUI.zoomSyntaxLabel("[#a6aeaa]" + (index + 1) + ".  " + syntax(block), 18))
          .growX()
          .minWidth(0);
      table.add(row).growX().minHeight(42).padBottom(6).row();
      return;
    }
    String error =
        staticError.isEmpty()
            ? owner.state().blockErrors().getOrDefault(block.id(), "")
            : staticError;
    row.failed = !error.isEmpty();
    rows.put(block.id(), row);
    row.addListener(
        new ClickListener() {
          private boolean toggle;
          private boolean range;

          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            if (!owner.editable()
                || editorButton(event.getTarget(), row)
                || !super.touchDown(event, x, y, pointer, button)) return false;
            toggle =
                Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                    || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
            range =
                Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                    || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
            return true;
          }

          @Override
          public void clicked(InputEvent event, float x, float y) {
            if (!owner.editable()) return;
            if (loose() && !toggle && !range) {
              selected.clear();
              selected.addAll(looseIds);
              selectionAnchor = block.id();
              paintSelection();
            } else select(block.id(), toggle, range);
          }
        });
    source(row, block, false);
    if (!loose()) target(row, index, true);
    paintSelection();
    String line =
        container.equals("main") || container.equals("draft")
            ? "[#a6aeaa]" + (index + 1) + ".  "
            : "";
    row.add(ProgrammingUI.zoomSyntaxLabel(line + syntax(block), 18))
        .growX()
        .minWidth(0)
        .padRight(8);
    TextButton options =
        ProgrammingUI.zoomButton(
            "...",
            false,
            () -> {
              owner.flushFields();
              expanded = expanded.equals(block.id()) ? "" : block.id();
              refreshAll();
            });
    var optionsStyle = new TextButton.TextButtonStyle(options.getStyle());
    optionsStyle.up = null;
    optionsStyle.over = ProgrammingUI.background(Color.valueOf("465459"), false);
    optionsStyle.down = ProgrammingUI.background(Color.valueOf("20282b"), false);
    optionsStyle.checked = optionsStyle.over;
    options.setStyle(optionsStyle);
    options.setProgrammaticChangeEvents(false);
    options.setChecked(expanded.equals(block.id()));
    options.pad(0);
    row.add(options).width(28).height(26);
    if (row.failed) {
      row.row();
      row.add(ProgrammingUI.zoomLabel("Fehler: " + error, 16, CodeRow.ERROR))
          .colspan(2)
          .growX()
          .minWidth(0)
          .padTop(6);
    }
    table.add(row).growX().minHeight(42).row();
    if (expanded.equals(block.id())) {
      Table editor = new Table();
      editor.pad(10).setBackground(ProgrammingUI.background(Color.valueOf("20282b"), false));
      editor
          .add(ProgrammingUI.zoomLabel(caption(block.action()), 17, ProgrammingUI.GOLD))
          .growX()
          .padBottom(6)
          .row();
      if (block.action() == Action.CALL) {
        editField(
            editor,
            block,
            "arguments",
            "Argumente · durch Kommas getrennt",
            String.join(", ", block.arguments()));
        editField(
            editor, block, "target", "Ergebnis speichern in · leer = ignorieren", block.target());
        editor
            .add(
                ProgrammingUI.zoomButton(
                    resultModeLabel(block.mode()),
                    false,
                    () -> edit(block, "mode", nextResultMode(block.mode()).name(), ignored -> {})))
            .growX()
            .row();
      } else {
        if (block.action() != Action.OPEN_GATE
            && block.action() != Action.ACTIVATE_RUNE
            && block.action() != Action.COLLECT)
          editField(
              editor,
              block,
              "operand",
              block.action() == Action.TURN ? "Richtung oder Parameter" : "Wert oder Ausdruck",
              block.operand());
        if (block.action() == Action.COLLECT || block.action() == Action.ASSIGN)
          editField(editor, block, "target", "Variable", block.target());
      }
      editor
          .add(
              ProgrammingUI.zoomButton(
                  "Baustein löschen", false, () -> owner.send(Operation.DELETE_BLOCK, block.id())))
          .growX()
          .padTop(8)
          .row();
      table.add(editor).growX().row();
    }
  }

  /** Keep the canonical statement intact; only its argument/expression changes ink. */
  private static String syntax(Block block) {
    String source = MethodsWorkshop.blockSource(block);
    int start =
        switch (block.action()) {
          case MOVE, TURN, PLACE, CALL -> source.indexOf('(') + 1;
          case RETURN -> source.indexOf(' ') + 1;
          case ASSIGN -> source.indexOf('=') + 2;
          default -> source.length();
        };
    int end =
        switch (block.action()) {
          case MOVE, TURN, PLACE, CALL -> source.length() - 1;
          default -> source.length();
        };
    String command = block.action() == Action.CALL ? "d6ae70" : "eee5d4";
    if (start >= end) return "[#" + command + "]" + source.replace("[", "[[");
    return "[#"
        + command
        + "]"
        + source.substring(0, start).replace("[", "[[")
        + "[#a9c9c7]"
        + source.substring(start, end).replace("[", "[[")
        + "[#"
        + command
        + "]"
        + source.substring(end).replace("[", "[[");
  }

  /** The notch and lower tongue make a stack read as connected instructions. */
  private static final class CodeRow extends Table {
    private static final Color REST = Color.valueOf("344249");
    private static final Color HOVER = Color.valueOf("40535b");
    private static final Color SELECTED = Color.valueOf("444c45");
    private static final Color PRESSED = Color.valueOf("2b383f");
    private static final Color EDGE = Color.valueOf("65767a");
    private static final Color SHADOW = Color.valueOf("101719");
    private static final Color ERROR = Color.valueOf("ffb0a3");
    private static final Color ERROR_FILL = Color.valueOf("503438");
    private final boolean method;
    private final ClickListener pointer = new ClickListener();
    private boolean selected;
    private boolean failed;
    private int insertion;

    private CodeRow(boolean method) {
      this.method = method;
      setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
      padLeft(12).padRight(12).padTop(7).padBottom(8);
      addListener(pointer);
      setBackground(
          new BaseDrawable() {
            @Override
            public void draw(Batch batch, float x, float y, float width, float height) {
              float alpha = batch.getColor().a;
              Color fill =
                  pointer.isPressed()
                      ? PRESSED
                      : selected ? SELECTED : failed ? ERROR_FILL : pointer.isOver() ? HOVER : REST;
              float notch = Math.min(24, width / 4);
              float tongue = 20;
              // The top notch remains transparent; it never paints over its surrounding panel.
              CanvasGraphics.fill(batch, SHADOW, alpha, x + 1, y, width - 2, height - 5);
              CanvasGraphics.fill(batch, fill, alpha, x, y + 4, width, height - 8);
              CanvasGraphics.fill(batch, fill, alpha, x, y + height - 4, notch, 4);
              CanvasGraphics.fill(
                  batch,
                  fill,
                  alpha,
                  x + notch + tongue,
                  y + height - 4,
                  width - notch - tongue,
                  4);
              CanvasGraphics.fill(batch, fill, alpha, x + notch, y + 1, tongue, 3);
              CanvasGraphics.fill(batch, EDGE, alpha * .65f, x, y + height - 1, notch, 1);
              CanvasGraphics.fill(
                  batch,
                  EDGE,
                  alpha * .65f,
                  x + notch + tongue,
                  y + height - 1,
                  width - notch - tongue,
                  1);
              if (selected || method || failed)
                CanvasGraphics.fill(
                    batch,
                    failed ? ERROR : ProgrammingUI.GOLD,
                    alpha * (selected || failed ? 1 : .65f),
                    x,
                    y + 7,
                    selected || failed ? 3 : 2,
                    height - 14);
              if (insertion != 0)
                CanvasGraphics.fill(
                    batch,
                    ProgrammingUI.GOLD,
                    alpha,
                    x,
                    insertion > 0 ? y + height - 3 : y,
                    width,
                    3);
            }
          });
    }
  }

  private void editField(Table table, Block block, String field, String caption, String value) {
    table.add(ProgrammingUI.zoomLabel(caption, 15, ProgrammingUI.MUTED)).growX().row();
    table
        .add(field(block.id() + ":" + field, value, (next, ack) -> edit(block, field, next, ack)))
        .growX()
        .height(36)
        .padBottom(7)
        .row();
  }

  private void edit(Block old, String field, String value, Consumer<State> acknowledged) {
    owner.send(
        Operation.EDIT_BLOCK,
        state -> {
          Block current =
              java.util.stream.Stream.of(state.main(), state.scrap(), state.draft().body())
                  .flatMap(List::stream)
                  .filter(block -> block.id().equals(old.id()))
                  .findFirst()
                  .orElse(old);
          Block updated =
              new Block(
                  current.id(),
                  current.action(),
                  field.equals("operand") ? value : current.operand(),
                  field.equals("target") ? value : current.target(),
                  current.method(),
                  field.equals("arguments") ? arguments(value) : current.arguments(),
                  field.equals("mode") ? ResultMode.valueOf(value) : current.mode());
          return JSON.writeValueAsString(Map.of("id", old.id(), "block", updated));
        },
        acknowledged);
  }

  private static List<String> arguments(String value) {
    if (value.isBlank()) return List.of();
    var arguments = new ArrayList<String>();
    int depth = 0, start = 0;
    for (int i = 0; i < value.length(); i++) {
      switch (value.charAt(i)) {
        case '(' -> depth++;
        case ')' -> depth--;
        case ',' -> {
          if (depth == 0) {
            arguments.add(value.substring(start, i).trim());
            start = i + 1;
          }
        }
        default -> {}
      }
    }
    arguments.add(value.substring(start).trim());
    return List.copyOf(arguments);
  }

  private TextField field(String key, String initial, BiConsumer<String, Consumer<State>> commit) {
    TextField field = new CanvasTextField(drafts.getOrDefault(key, initial));
    var style = new TextField.TextFieldStyle(field.getStyle());
    style.font =
        FontHelper.getFont(Scene2dElementFactory.FONT_PATH, 18, ProgrammingUI.TEXT, 0, Color.BLACK);
    style.fontColor = ProgrammingUI.TEXT;
    style.background = ProgrammingUI.background(ProgrammingUI.SURFACE, true);
    style.focusedBackground = ProgrammingUI.background(Color.valueOf("35403f"), true);
    style.disabledBackground = ProgrammingUI.background(ProgrammingUI.INK, true);
    style.disabledFontColor = ProgrammingUI.MUTED;
    for (var background :
        List.of(style.background, style.focusedBackground, style.disabledBackground)) {
      background.setLeftWidth(8);
      background.setRightWidth(8);
    }
    field.setStyle(style);
    field.setName(key);
    field.setDisabled(!owner.editable());
    final String[] saved = {initial};
    Runnable save =
        () -> {
          if (!rebuilding && !field.getText().equals(saved[0])) {
            saved[0] = field.getText();
            draftBases.putIfAbsent(key, initial);
            drafts.put(key, saved[0]);
            submissions.merge(key, 1, Integer::sum);
            commit.accept(saved[0], state -> acknowledgeField(key, state));
          }
        };
    field.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            if (field.getText().equals(initial) && !submissions.containsKey(key)) {
              drafts.remove(key);
              draftBases.remove(key);
            } else {
              draftBases.putIfAbsent(key, initial);
              drafts.put(key, field.getText());
            }
          }
        });
    field.addListener(
        new FocusListener() {
          @Override
          public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
            if (!focused) save.run();
          }
        });
    field.setTextFieldListener(
        (textField, character) -> {
          if (character == '\r' || character == '\n') save.run();
        });
    return field;
  }

  /** Keeps field glyphs at the display resolution while preserving logical input coordinates. */
  private static final class CanvasTextField extends TextField {
    private final Vector2 origin = new Vector2();
    private final Vector2 axis = new Vector2();
    private int raster;

    private CanvasTextField(String text) {
      super(text, UIUtils.defaultSkin());
    }

    @Override
    public void draw(Batch batch, float alpha) {
      localToStageCoordinates(origin.set(0, 0));
      localToStageCoordinates(axis.set(128, 0));
      int required =
          (int)
              Math.ceil(
                  18
                      * Math.max(
                          1, Math.ceil(axis.dst(origin) / 128 * ProgrammingUI.density() * 4) / 4));
      if (raster != required) {
        raster = required;
        var style = new TextFieldStyle(getStyle());
        // A distinct cache key keeps field font scaling separate from label fonts.
        style.font =
            FontHelper.getFont(
                Scene2dElementFactory.FONT_PATH, raster, ProgrammingUI.TEXT, 0, Color.CLEAR);
        style.font.getData().setScale(18f / raster);
        style.font.setUseIntegerPositions(false);
        setStyle(style);
      }
      super.draw(batch, alpha);
    }
  }

  record Drag(
      ProgrammingMethodsNode source,
      String container,
      Block block,
      boolean copy,
      List<String> ids) {}

  private static List<Block> blocks(State state, String container) {
    return switch (container) {
      case "main" -> state.main();
      case "draft" -> state.draft().body();
      case "scrap" -> state.scrap();
      default -> List.of();
    };
  }

  private static boolean editorButton(Actor target, Actor row) {
    while (target != null && target != row) {
      if (target instanceof TextButton) return true;
      target = target.getParent();
    }
    return false;
  }

  private void select(String id, boolean toggle, boolean range) {
    List<String> ids = panelBlocks(owner.state()).stream().map(Block::id).toList();
    if (range && ids.contains(selectionAnchor)) {
      int a = ids.indexOf(selectionAnchor), b = ids.indexOf(id);
      if (!toggle) selected.clear();
      selected.addAll(ids.subList(Math.min(a, b), Math.max(a, b) + 1));
    } else {
      if (!toggle) selected.clear();
      if (!selected.add(id)) selected.remove(id);
      selectionAnchor = id;
    }
    paintSelection();
  }

  private void paintSelection() {
    for (Block block : panelBlocks(owner.state())) {
      CodeRow row = rows.get(block.id());
      if (row != null) row.selected = selected.contains(block.id());
    }
  }

  private void source(Actor actor, Block block, boolean copy) {
    boolean[] pressedEditor = {false};
    actor.addCaptureListener(
        new InputListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            pressedEditor[0] = editorButton(event.getTarget(), actor);
            return false;
          }
        });
    owner
        .dragging()
        .addSource(
            new DragAndDrop.Source(actor) {
              @Override
              public DragAndDrop.Payload dragStart(
                  InputEvent event, float x, float y, int pointer) {
                if (!owner.editable() || pressedEditor[0]) return null;
                if (!copy && !selected.contains(block.id())) {
                  if (loose()) {
                    selected.clear();
                    selected.addAll(looseIds);
                    paintSelection();
                  } else select(block.id(), false, false);
                }
                owner.flushFields();
                var payload = new DragAndDrop.Payload();
                payload.setObject(
                    new Drag(
                        ProgrammingMethodsNode.this,
                        container,
                        block,
                        copy,
                        copy
                            ? List.of()
                            : panelBlocks(owner.state()).stream()
                                .map(Block::id)
                                .filter(selected::contains)
                                .toList()));
                CodeRow ghost = new CodeRow(block.action() == Action.CALL);
                ghost.selected = true;
                ghost
                    .add(
                        ProgrammingUI.label(
                            (selected.size() > 1 && !copy
                                ? selected.size() + " Anweisungen"
                                : MethodsWorkshop.blockSource(block)),
                            18,
                            ProgrammingUI.TEXT))
                    .width(260);
                ghost.pack();
                payload.setDragActor(ghost);
                return payload;
              }

              @Override
              public void dragStop(
                  InputEvent event,
                  float x,
                  float y,
                  int pointer,
                  DragAndDrop.Payload payload,
                  DragAndDrop.Target target) {
                com.badlogic.gdx.Gdx.app.postRunnable(() -> refreshAll());
              }
            });
  }

  private void insertion(Table table, int index, boolean empty) {
    Table gap = new Table();
    gap.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
    gap.setBackground(ProgrammingUI.background(Color.valueOf("20282b"), false));
    if (empty)
      gap.add(ProgrammingUI.zoomLabel("Code hier ablegen", 18, ProgrammingUI.MUTED))
          .growX()
          .pad(14);
    table.add(gap).growX().height(empty ? 65 : 6).row();
    target(gap, index, false);
  }

  private void target(Table gap, int index, boolean row) {
    List<String> destinationIds = panelBlocks(owner.state()).stream().map(Block::id).toList();
    owner
        .dragging()
        .addTarget(
            new DragAndDrop.Target(gap) {
              @Override
              public boolean drag(
                  DragAndDrop.Source source,
                  DragAndDrop.Payload payload,
                  float x,
                  float y,
                  int pointer) {
                boolean valid = payload.getObject() instanceof Drag d && validDrag(d);
                if (row && gap instanceof CodeRow codeRow) {
                  codeRow.insertion = valid ? (y >= gap.getHeight() / 2 ? 1 : -1) : 0;
                } else {
                  gap.setBackground(
                      ProgrammingUI.background(
                          valid ? ProgrammingUI.GOLD : Color.valueOf("20282b"), false));
                }
                return valid;
              }

              @Override
              public void reset(DragAndDrop.Source source, DragAndDrop.Payload payload) {
                if (row && gap instanceof CodeRow codeRow) codeRow.insertion = 0;
                else gap.setBackground(ProgrammingUI.background(Color.valueOf("20282b"), false));
              }

              @Override
              public void drop(
                  DragAndDrop.Source source,
                  DragAndDrop.Payload payload,
                  float x,
                  float y,
                  int pointer) {
                if (!(payload.getObject() instanceof Drag d) || !validDrag(d)) return;
                // The visible rows can outlive a snapshot received during this drag.
                String anchor =
                    destinationIds.stream()
                        .skip(index + (row && y < gap.getHeight() / 2 ? 1 : 0))
                        .filter(id -> d.copy() || !d.ids().contains(id))
                        .findFirst()
                        .orElse("");
                queueDrop(d, insertionIndex(owner.state(), anchor));
              }
            });
  }

  /** Each queued move resolves its index after earlier moves and field edits are acknowledged. */
  private void queueDrop(Drag drag, int index) {
    String anchor =
        panelBlocks(owner.state()).stream()
            .skip(index)
            .map(Block::id)
            .filter(id -> drag.copy() || !drag.ids().contains(id))
            .findFirst()
            .orElse("");
    if (drag.copy()) {
      owner.send(
          Operation.ADD_BLOCK,
          state ->
              JSON.writeValueAsString(
                  Map.of(
                      "container",
                      container,
                      "index",
                      insertionIndex(state, anchor),
                      "block",
                      drag.block())),
          ignored -> {});
    } else {
      for (String id : drag.ids()) {
        owner.send(
            Operation.MOVE_BLOCK,
            state ->
                JSON.writeValueAsString(
                    Map.of(
                        "container", container, "index", insertionIndex(state, anchor), "id", id)),
            ignored -> {});
      }
    }
  }

  private int insertionIndex(State state, String anchor) {
    List<Block> destination = blocks(state, container);
    for (int i = 0; i < destination.size(); i++)
      if (destination.get(i).id().equals(anchor)) return i;
    return destination.size();
  }

  // Field acknowledgments can arrive during a drag; the move still refers to the live block ID.
  boolean validDrag(Drag drag) {
    if (showOriginal || drag.source().showOriginal || !owner.editable()) return false;
    if (drag.copy()) {
      if (drag.block().action() != Action.CALL) return true;
      var method = owner.state().definitions().get(drag.block().method());
      return method != null && method.parameters().size() == drag.block().arguments().size();
    }
    List<Block> source =
        switch (drag.container()) {
          case "main" -> owner.state().main();
          case "draft" -> owner.state().draft().body();
          case "scrap" -> owner.state().scrap();
          default -> List.of();
        };
    return !drag.ids().isEmpty() && source.stream().map(Block::id).toList().containsAll(drag.ids());
  }

  private void refreshAll() {
    owner.flushFields();
    owner.refreshPanels();
  }

  private static String caption(Action action) {
    return switch (action) {
      case TURN -> "Drehen";
      case MOVE -> "Gehen";
      case OPEN_GATE -> "Tor öffnen";
      case ACTIVATE_RUNE -> "Rune aktivieren";
      case COLLECT -> "Kristalle sammeln";
      case PLACE -> "Kristalle ablegen";
      case RETURN -> "Wert zurückgeben";
      case ASSIGN -> "Variable setzen";
      case CALL -> "Methode aufrufen";
    };
  }

  private static String resultModeLabel(ResultMode mode) {
    return switch (mode) {
      case REPLACE -> "Ergebnis ersetzen (=)";
      case ADD -> "Ergebnis addieren (+)";
      case SUBTRACT -> "Ergebnis subtrahieren (-)";
    };
  }

  private static ResultMode nextResultMode(ResultMode mode) {
    return switch (mode) {
      case REPLACE -> ResultMode.ADD;
      case ADD -> ResultMode.SUBTRACT;
      case SUBTRACT -> ResultMode.REPLACE;
    };
  }

  @Override
  protected void layoutContent() {
    content.setBounds(0, 0, width(), height());
  }

  @Override
  protected void drawBackground(Batch batch, float alpha) {
    if (loose()) return;
    CanvasGraphics.fill(batch, ProgrammingUI.INK, alpha, x(), y(), width(), height());
    CanvasGraphics.outline(
        batch,
        ProgrammingUI.GOLD,
        alpha * .7f,
        x(),
        y(),
        width(),
        height(),
        canvas() == null ? 1 : Math.max(1, 1.25f / canvas().zoom()));
  }
}
