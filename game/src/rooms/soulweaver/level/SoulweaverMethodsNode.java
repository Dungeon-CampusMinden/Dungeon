package rooms.soulweaver.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Align;
import engine.utils.CursorUtil;
import engine.utils.Cursors;
import engine.utils.FontHelper;
import engine.utils.FontSpec;
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
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import rooms.soulweaver.modules.methods.MethodsRoute.Action;
import rooms.soulweaver.modules.methods.MethodsWorkshop;
import rooms.soulweaver.modules.methods.MethodsWorkshop.Block;
import rooms.soulweaver.modules.methods.MethodsWorkshop.Definition;
import rooms.soulweaver.modules.methods.MethodsWorkshop.Operation;
import rooms.soulweaver.modules.methods.MethodsWorkshop.ResultMode;
import rooms.soulweaver.modules.methods.MethodsWorkshop.State;
import tools.jackson.databind.json.JsonMapper;

/** Code windows and frameless loose groups share the same instruction rows and editors. */
final class SoulweaverMethodsNode extends CanvasNode implements CursorUtil.CursorOverride {
  private static final JsonMapper JSON = JsonMapper.builder().build();
  private static final List<Block> ORIGINAL_PROGRAM = MethodsWorkshop.originalProgram();

  static void register() {
    if (!CanvasNodeType.isRegistered("soulweaver.methods.blocks"))
      CanvasNodeType.register(
          "soulweaver.methods.blocks",
          s ->
              new SoulweaverMethodsNode(
                  s.id().replace("methods-blocks-", ""), "Code", s.width(), s.height()));
  }

  @Override
  public String typeId() {
    return "soulweaver.methods.blocks";
  }

  private final String container;
  private final String title;
  private List<String> looseIds = List.of();
  private final Table content = new Table();
  private final Map<String, EditorField> fields = new HashMap<>();
  private State renderedState;
  private List<Block> renderedBlocks = List.of();
  private boolean renderedSimplified;
  private String renderedBuildFailure = "";
  private final Set<String> selected = new HashSet<>();
  private final Map<String, CodeRow> rows = new HashMap<>();
  private final List<DragAndDrop.Source> dragSources = new ArrayList<>();
  private final List<DragAndDrop.Target> dragTargets = new ArrayList<>();
  private String selectionAnchor = "";
  private int draftEditor = Integer.MIN_VALUE;
  private SoulweaverMethodsUI owner;
  private ScrollPane scroll;
  private float scrollY;
  private float alternateScrollY;
  private boolean showOriginal;
  private String expanded = "";
  private String revealedError = "";
  private boolean rebuilding;
  private String buildFailure = "";
  private Definition failedDraft;
  private int resizingPointer = -1;
  private int resizingButton = -1;
  private int resizingEdges;
  private boolean moving;

  SoulweaverMethodsNode(String container, String title, float width, float height) {
    super("methods-blocks-" + container, width, height);
    this.container = container.startsWith("loose-") ? "scrap" : container;
    this.title = title;
    setUserObject(Cursors.GRAB);
    deletable(false);
    addCaptureListener(
        new InputListener() {
          private final Vector2 press = new Vector2();
          private final Vector2 current = new Vector2();
          private float left;
          private float right;
          private float bottom;
          private float top;

          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            if (loose()) return false;
            if (resizingPointer >= 0) {
              event.stop();
              return false;
            }
            int edges =
                button == Input.Buttons.RIGHT
                    ? Align.bottom | (container.equals("draft") ? Align.right : 0)
                    : button == Input.Buttons.LEFT && !Gdx.input.isKeyPressed(Input.Keys.SPACE)
                        ? resizeEdges(x, y)
                        : 0;
            if (edges == 0) return false;
            resizingPointer = pointer;
            resizingButton = button;
            resizingEdges = edges;
            getParent().stageToLocalCoordinates(press.set(event.getStageX(), event.getStageY()));
            left = x();
            right = x() + width();
            bottom = y();
            top = y() + height();
            if (canvas() != null) canvas().bringToFront(SoulweaverMethodsNode.this);
            event.stop();
            return true;
          }

          @Override
          public void touchDragged(InputEvent event, float x, float y, int pointer) {
            if (pointer != resizingPointer) return;
            getParent().stageToLocalCoordinates(current.set(event.getStageX(), event.getStageY()));
            float dx = current.x - press.x;
            float dy = current.y - press.y;
            float nextLeft =
                (resizingEdges & Align.left) != 0
                    ? Math.clamp(left + dx, SoulweaverMethodsUI.BOARD_LEFT, right - minimumWidth())
                    : left;
            float nextRight =
                (resizingEdges & Align.right) != 0
                    ? Math.clamp(right + dx, left + minimumWidth(), SoulweaverMethodsUI.BOARD_RIGHT)
                    : right;
            float nextBottom =
                (resizingEdges & Align.bottom) != 0
                    ? Math.clamp(bottom + dy, SoulweaverMethodsUI.BOARD_BOTTOM, top - 180)
                    : bottom;
            float nextTop =
                (resizingEdges & Align.top) != 0
                    ? Math.clamp(top + dy, bottom + 180, SoulweaverMethodsUI.BOARD_TOP)
                    : top;
            size(nextRight - nextLeft, nextTop - nextBottom);
            position(nextLeft, nextBottom);
            event.stop();
          }

          @Override
          public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            if (pointer != resizingPointer || button != resizingButton) return;
            resizingPointer = -1;
            resizingButton = -1;
            resizingEdges = 0;
            event.stop();
          }
        });
  }

  private float minimumWidth() {
    return container.equals("palette") ? 300 : 410;
  }

  /**
   * The invisible resize band stays six screen pixels wide at every canvas zoom.
   *
   * @param x pointer position relative to the window's left edge
   * @param y pointer position relative to the window's bottom edge
   * @return resize edges as {@link Align} flags, or zero outside the border
   */
  private int resizeEdges(float x, float y) {
    if (loose() || x < 0 || y < 0 || x >= width() || y >= height()) return 0;
    float margin = 6 / (canvas() == null ? 1 : canvas().zoom());
    int edges = x < margin ? Align.left : x >= width() - margin ? Align.right : 0;
    return edges | (y < margin ? Align.bottom : y >= height() - margin ? Align.top : 0);
  }

  private static Cursors resizeCursor(int edges) {
    boolean horizontal = (edges & (Align.left | Align.right)) != 0;
    boolean vertical = (edges & (Align.top | Align.bottom)) != 0;
    if (!horizontal) return Cursors.RESIZE_VERTICAL;
    if (!vertical) return Cursors.RESIZE_HORIZONTAL;
    return (edges & (Align.top | Align.left)) == (Align.top | Align.left)
            || (edges & (Align.bottom | Align.right)) == (Align.bottom | Align.right)
        ? Cursors.RESIZE_DIAGONAL
        : Cursors.RESIZE_DIAGONAL_REVERSE;
  }

  @Override
  public Optional<Cursors> cursorOverride() {
    Optional<Cursors> active = owner == null ? manipulationCursor() : owner.cursorOverride();
    if (active.isPresent() || getStage() == null || Gdx.input.isKeyPressed(Input.Keys.SPACE))
      return active;
    Vector2 pointer = screenToLocalCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
    int edges = resizeEdges(pointer.x, pointer.y);
    return edges == 0 ? Optional.empty() : Optional.of(resizeCursor(edges));
  }

  /** Keeps moved and restored windows, including loose code groups, inside the board. */
  @Override
  public CanvasNode position(float x, float y) {
    return super.position(
        Math.clamp(x, SoulweaverMethodsUI.BOARD_LEFT, SoulweaverMethodsUI.BOARD_RIGHT - width()),
        Math.clamp(y, SoulweaverMethodsUI.BOARD_BOTTOM, SoulweaverMethodsUI.BOARD_TOP - height()));
  }

  @Override
  public CanvasNode size(float width, float height) {
    super.size(
        Math.clamp(width, 1, SoulweaverMethodsUI.BOARD_RIGHT - SoulweaverMethodsUI.BOARD_LEFT),
        Math.clamp(height, 1, SoulweaverMethodsUI.BOARD_TOP - SoulweaverMethodsUI.BOARD_BOTTOM));
    position(x(), y());
    return this;
  }

  Optional<Cursors> manipulationCursor() {
    if (resizingPointer >= 0) return Optional.of(resizeCursor(resizingEdges));
    return moving ? Optional.of(Cursors.GRABBING) : Optional.empty();
  }

  @Override
  public void onMove(float dx, float dy) {
    if (movable()) moving = true;
    super.onMove(dx, dy);
  }

  @Override
  public void onDrop(float worldX, float worldY) {
    moving = false;
    super.onDrop(worldX, worldY);
  }

  @Override
  public void onClick(float localX, float localY, int button) {
    moving = false;
    super.onClick(localX, localY, button);
  }

  void attach(SoulweaverMethodsUI owner) {
    this.owner = owner;
    if (loose()) {
      movable(false);
      refresh();
      return;
    }
    size(Math.max(minimumWidth(), width()), Math.max(180, height()));
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
    size(width(), height());
  }

  private List<Block> panelBlocks(State state) {
    if (!loose()) return blocks(state, container);
    Map<String, Block> byId = new HashMap<>();
    state.scrap().forEach(block -> byId.put(block.id(), block));
    return looseIds.stream().filter(byId::containsKey).map(byId::get).toList();
  }

  void forgetMethodFields() {
    if (container.equals("draft")) renderedState = null;
    buildFailure = "";
    fields.remove("method-name");
    fields.remove("method-parameters");
  }

  void scroll(float amount) {
    if (scroll == null) return;
    scroll.validate();
    scroll.setScrollY(scroll.getScrollY() + amount * 42);
  }

  /** Updates field values in place; only structural changes rebuild this window. */
  void update() {
    State state = owner.state();
    if (!state.draft().equals(failedDraft)) buildFailure = "";
    reconcileFields();
    List<Block> blocks = panelBlocks(state);
    boolean rebuild =
        renderedState == null
            || renderedState.editorId() != state.editorId()
            || renderedState.busy() != state.busy()
            || renderedSimplified != owner.simplified()
            || !renderedBuildFailure.equals(buildFailure)
            || !sameRows(renderedBlocks, blocks)
            || (loose() && !renderedBlocks.equals(blocks))
            || !renderedState.blockErrors().equals(state.blockErrors())
            || (container.equals("palette")
                && !renderedState.definitions().equals(state.definitions()));
    if (rebuild) {
      refresh();
      return;
    }
    for (int i = 0; i < blocks.size(); i++) {
      CodeRow row = rows.get(blocks.get(i).id());
      if (row != null) row.source.setText(lineSource(blocks.get(i), i));
    }
    renderedState = state;
    renderedBlocks = blocks;
  }

  private static boolean sameRows(List<Block> previous, List<Block> next) {
    if (previous.size() != next.size()) return false;
    for (int i = 0; i < previous.size(); i++) {
      Block a = previous.get(i), b = next.get(i);
      if (!a.id().equals(b.id())
          || a.action() != b.action()
          || !a.method().equals(b.method())
          || a.mode() != b.mode()) return false;
    }
    return true;
  }

  void refresh() {
    if (!owner.state().draft().equals(failedDraft)) buildFailure = "";
    reconcileFields();
    if (scroll != null) scrollY = scroll.getScrollY();
    Actor focus = getStage() == null ? null : getStage().getKeyboardFocus();
    rebuilding = true;
    releaseDragTargets();
    rebuildContent();
    ensureContentBuilt();
    revealError();
    // Structural edits can reparent a surviving field; keep the actor and its selection intact.
    if (focus instanceof EditorField && focus.isDescendantOf(this) && getStage() != null)
      getStage().setKeyboardFocus(focus);
    rebuilding = false;
    renderedState = owner.state();
    renderedBlocks = panelBlocks(renderedState);
    renderedSimplified = owner.simplified();
    renderedBuildFailure = buildFailure;
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

  private void reconcileFields() {
    int editor = owner.state().editorId();
    if (draftEditor != editor) {
      selected.clear();
      selectionAnchor = "";
      fields.clear();
      draftEditor = editor;
    }
    List<String> ids = panelBlocks(owner.state()).stream().map(Block::id).toList();
    selected.retainAll(ids);
    if (!ids.contains(selectionAnchor)) selectionAnchor = "";
    Map<String, String> current = fieldValues(owner.state());
    fields.keySet().retainAll(current.keySet());
    fields.forEach((key, field) -> field.receive(current.get(key)));
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
        SoulweaverUI.zoomLabel(
            title + count,
            container.equals("main") ? 20 : 23,
            methodTooLong || !unreachable.isEmpty() ? SoulweaverUI.ERROR : SoulweaverUI.GOLD);
    Table header = new Table();
    header.add(heading).growX().minWidth(0);
    if (container.equals("main")) {
      TextButton toggle =
          SoulweaverUI.zoomButton(
              showOriginal ? "Mein Code" : "Original", showOriginal, this::toggleOriginal);
      toggle.setName("toggle-original-program");
      toggle.pad(4);
      header.add(toggle).width(106).height(30).padLeft(6);
    }
    content.add(header).growX().height(35).padBottom(8).row();
    if (showOriginal)
      content
          .add(
              SoulweaverUI.zoomLabel(
                  "Original · "
                      + ORIGINAL_PROGRAM.size()
                      + " Zeilen · nur Lesen\nAusführen startet dein eigenes Programm.",
                  15,
                  SoulweaverUI.MUTED))
          .growX()
          .padBottom(8)
          .row();
    if (methodTooLong)
      content
          .add(
              SoulweaverUI.zoomLabel(
                  "Entwurf zu lang: "
                      + methodLines
                      + " Zeilen, höchstens "
                      + MethodsWorkshop.MAX_METHOD_BLOCKS
                      + " erlaubt. Kürze die Methode, bevor du sie baust.",
                  16,
                  SoulweaverUI.ERROR))
          .growX()
          .padBottom(8)
          .row();
    if (!unreachable.isEmpty())
      content
          .add(
              SoulweaverUI.zoomLabel(
                  unreachable.values().iterator().next(), 16, SoulweaverUI.ERROR))
          .growX()
          .padBottom(8)
          .row();
    Table body = new Table();
    body.top().left();
    body.setUserObject(Cursors.DEFAULT);
    if (container.equals("main"))
      body.setBackground(SoulweaverUI.background(Color.valueOf("20282b"), false));
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
    if (!container.equals("palette") && !showOriginal)
      target(scroll, panelBlocks(owner.state()).size(), false);
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
    pane.setUserObject(Cursors.DEFAULT);
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
    table.add(SoulweaverUI.zoomLabel("Name", 15, SoulweaverUI.MUTED)).growX().row();
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
        .add(SoulweaverUI.zoomLabel("Eingaben · durch Kommas getrennt", 15, SoulweaverUI.MUTED))
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
        SoulweaverUI.zoomButton(
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
            editButton(
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
          .add(SoulweaverUI.zoomLabel(buildFailure, 17, SoulweaverUI.GOLD))
          .growX()
          .padBottom(10)
          .row();
  }

  private void palette(Table table) {
    if (owner.simplified())
      table
          .add(
              SoulweaverUI.zoomLabel(
                  "Gold markiert wiederholte Folgen und Bausteine für Eingaben und Rückgaben. Unterschiedliche Mengen werden zu Eingaben.",
                  16,
                  SoulweaverUI.GOLD))
          .growX()
          .padBottom(12)
          .row();
    table
        .add(SoulweaverUI.zoomLabel("Anweisungen hier herausziehen", 16, SoulweaverUI.MUTED))
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
        .add(SoulweaverUI.zoomLabel("Gebaute Methoden", 22, SoulweaverUI.GOLD))
        .growX()
        .padTop(20)
        .padBottom(8)
        .row();
    if (owner.state().definitions().isEmpty())
      table
          .add(
              SoulweaverUI.zoomLabel(
                  "Baue deine erste Methode im Methodenfenster.", 17, SoulweaverUI.MUTED))
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
                      editButton(
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
    row.aided =
        owner.simplified()
            && (block.action() == Action.RETURN
                || block.action() == Action.COLLECT
                || block.action() == Action.CALL);
    row.add(
            SoulweaverUI.zoomLabel(
                text, 18, block.action() == Action.CALL ? SoulweaverUI.GOLD : SoulweaverUI.TEXT))
        .growX()
        .minWidth(0);
    table.add(row).growX().minHeight(42).padBottom(6).row();
    source(row, block, true);
  }

  /**
   * Marks recurring adjacent actions without changing code or the user's selection.
   *
   * @param index block position in the current panel
   * @return whether the adjacent action pair appears elsewhere in the panel
   */
  private boolean repeatedSequence(int index) {
    List<Block> blocks = showOriginal ? ORIGINAL_PROGRAM : panelBlocks(owner.state());
    for (int start = Math.max(0, index - 1); start <= index && start + 1 < blocks.size(); start++) {
      for (int other = 0; other + 1 < blocks.size(); other++) {
        if (Math.abs(other - start) < 2) continue;
        if (blocks.get(start).action() == blocks.get(other).action()
            && blocks.get(start + 1).action() == blocks.get(other + 1).action()) return true;
      }
    }
    return false;
  }

  private void blockRow(Table table, Block block, int index, String staticError) {
    CodeRow row = new CodeRow(block.action() == Action.CALL);
    row.aided = owner.simplified() && repeatedSequence(index);
    if (owner.simplified()) {
      String note =
          switch (block.action()) {
            case COLLECT -> "Sammelmenge als Rückgabe nutzen";
            case RETURN -> "Diesen Wert am Aufruf übernehmen";
            case CALL -> "Eingaben am Aufruf anpassen";
            default -> "";
          };
      if (!note.isEmpty()) {
        table.add(SoulweaverUI.zoomLabel(note, 14, SoulweaverUI.GOLD)).growX().padTop(6).row();
        row.aided = true;
      }
    }
    if (showOriginal) {
      row.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
      row.add(SoulweaverUI.zoomSyntaxLabel("[#a6aeaa]" + (index + 1) + ".  " + syntax(block), 18))
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
    row.source = SoulweaverUI.zoomSyntaxLabel(lineSource(block, index), 18);
    row.add(row.source).growX().minWidth(0).padRight(8);
    TextButton options =
        SoulweaverUI.zoomButton(
            "...",
            false,
            () -> {
              owner.flushFields();
              expanded = expanded.equals(block.id()) ? "" : block.id();
              refreshAll();
            });
    var optionsStyle = new TextButton.TextButtonStyle(options.getStyle());
    optionsStyle.up = null;
    optionsStyle.over = SoulweaverUI.background(Color.valueOf("465459"), false);
    optionsStyle.down = SoulweaverUI.background(Color.valueOf("20282b"), false);
    optionsStyle.checked = optionsStyle.over;
    options.setStyle(optionsStyle);
    options.setProgrammaticChangeEvents(false);
    options.setChecked(expanded.equals(block.id()));
    options.pad(0);
    row.add(options).width(28).height(26);
    if (row.failed) {
      row.row();
      row.add(SoulweaverUI.zoomLabel("Fehler: " + error, 16, CodeRow.ERROR))
          .colspan(2)
          .growX()
          .minWidth(0)
          .padTop(6);
    }
    table.add(row).growX().minHeight(42).row();
    if (expanded.equals(block.id())) {
      Table editor = new Table();
      editor.pad(10).setBackground(SoulweaverUI.background(Color.valueOf("20282b"), false));
      editor
          .add(SoulweaverUI.zoomLabel(caption(block.action()), 17, SoulweaverUI.GOLD))
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
                editButton(
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
              editButton(
                  "Baustein löschen", false, () -> owner.send(Operation.DELETE_BLOCK, block.id())))
          .growX()
          .padTop(8)
          .row();
      table.add(editor).growX().row();
    }
  }

  private String lineSource(Block block, int index) {
    String line =
        container.equals("main") || container.equals("draft")
            ? "[#a6aeaa]" + (index + 1) + ".  "
            : "";
    return line + syntax(block);
  }

  /**
   * Keep the canonical statement intact; only its argument/expression changes ink.
   *
   * @param block statement being displayed
   * @return statement markup with its expression highlighted
   */
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
    private Label source;
    private final boolean method;
    private final ClickListener pointer = new ClickListener();
    private boolean selected;
    private boolean failed;
    private boolean aided;
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
                      : selected
                          ? SELECTED
                          : failed
                              ? ERROR_FILL
                              : aided ? SELECTED : pointer.isOver() ? HOVER : REST;
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
              if (selected || method || failed || aided)
                CanvasGraphics.fill(
                    batch,
                    failed ? ERROR : SoulweaverUI.GOLD,
                    alpha * (selected || failed ? 1 : .65f),
                    x,
                    y + 7,
                    selected || failed ? 3 : 2,
                    height - 14);
              if (insertion != 0)
                CanvasGraphics.fill(
                    batch,
                    SoulweaverUI.GOLD,
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
    table.add(SoulweaverUI.zoomLabel(caption, 15, SoulweaverUI.MUTED)).growX().row();
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

  private TextButton editButton(String caption, boolean primary, Runnable action) {
    TextButton button = SoulweaverUI.zoomButton(caption, primary, action);
    button.setDisabled(!owner.editable());
    return button;
  }

  private TextField field(String key, String initial, BiConsumer<String, Consumer<State>> commit) {
    EditorField field = fields.computeIfAbsent(key, ignored -> new EditorField(key, initial));
    field.commit = commit;
    field.receive(initial);
    field.setDisabled(!owner.editable());
    return field;
  }

  /** A retained field owns its unsent text and pending saves, independently of snapshots. */
  private final class EditorField extends CanvasTextField {
    private String server;
    private String submitted;
    private int pending;
    private BiConsumer<String, Consumer<State>> commit;

    private EditorField(String key, String initial) {
      super(initial);
      server = initial;
      submitted = initial;
      var style = new TextFieldStyle(getStyle());
      style.font = fieldFont(18);
      style.fontColor = SoulweaverUI.TEXT;
      style.background = SoulweaverUI.background(SoulweaverUI.SURFACE, true);
      style.focusedBackground = SoulweaverUI.background(Color.valueOf("35403f"), true);
      style.disabledBackground = SoulweaverUI.background(SoulweaverUI.INK, true);
      style.disabledFontColor = SoulweaverUI.MUTED;
      for (var background :
          List.of(style.background, style.focusedBackground, style.disabledBackground)) {
        background.setLeftWidth(8);
        background.setRightWidth(8);
      }
      setStyle(style);
      setName(key);
      setUserObject(Cursors.TEXT);
      addListener(
          new FocusListener() {
            @Override
            public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
              if (!focused && !rebuilding) save();
            }
          });
      setTextFieldListener(
          (field, character) -> {
            if (character == '\r' || character == '\n') save();
          });
    }

    private void receive(String value) {
      if (pending == 0 && !server.equals(value)) {
        setText(value);
        submitted = value;
      }
      server = value;
    }

    private void save() {
      if (getText().equals(submitted) || !owner.editable()) return;
      submitted = getText();
      pending++;
      commit.accept(
          submitted,
          state -> {
            pending--;
            String value = fieldValues(state).get(getName());
            if (value == null) return;
            server = value;
            if (pending == 0 && normalized(getName(), getText()).equals(value)) {
              setText(value);
              submitted = value;
            }
          });
    }
  }

  private static com.badlogic.gdx.graphics.g2d.BitmapFont fieldFont(int raster) {
    var font =
        FontHelper.getFont(
            FontSpec.of(Scene2dElementFactory.FONT_PATH, raster, SoulweaverUI.TEXT),
            FontHelper.FontRole.TEXT_FIELD);
    font.getData().setScale(18f / raster);
    font.setUseIntegerPositions(false);
    return font;
  }

  /** Keeps field glyphs at the display resolution while preserving logical input coordinates. */
  private static class CanvasTextField extends TextField {
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
                          1, Math.ceil(axis.dst(origin) / 128 * SoulweaverUI.density() * 4) / 4));
      if (raster != required) {
        raster = required;
        var style = new TextFieldStyle(getStyle());
        style.font = fieldFont(raster);
        setStyle(style);
      }
      super.draw(batch, alpha);
    }
  }

  record Drag(
      SoulweaverMethodsNode source,
      String container,
      Block block,
      boolean copy,
      List<String> ids,
      float grabX,
      float grabFromTop) {}

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

  void releaseDragTargets() {
    dragSources.forEach(owner.dragging()::removeSource);
    dragTargets.forEach(owner.dragging()::removeTarget);
    dragSources.clear();
    dragTargets.clear();
  }

  private void addSource(DragAndDrop.Source source) {
    dragSources.add(source);
    owner.dragging().addSource(source);
  }

  private void addTarget(DragAndDrop.Target target) {
    dragTargets.add(target);
    owner.dragging().addTarget(target);
  }

  private void source(Actor actor, Block block, boolean copy) {
    actor.setUserObject(owner.editable() ? copy ? Cursors.COPY : Cursors.GRAB : Cursors.DISABLED);
    boolean[] pressedEditor = {false};
    actor.addCaptureListener(
        new InputListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            pressedEditor[0] = editorButton(event.getTarget(), actor);
            return false;
          }
        });
    addSource(
        new DragAndDrop.Source(actor) {
          @Override
          public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
            if (!owner.editable() || pressedEditor[0]) return null;
            if (!copy && !selected.contains(block.id())) {
              if (loose()) {
                selected.clear();
                selected.addAll(looseIds);
                paintSelection();
              } else select(block.id(), false, false);
            }
            owner.flushFields();
            List<Block> draggedBlocks =
                copy
                    ? List.of(block)
                    : panelBlocks(owner.state()).stream()
                        .filter(selectedBlock -> selected.contains(selectedBlock.id()))
                        .toList();
            Table preview = new Table();
            preview.top().left();
            float grabFromTop = actor.getHeight() - y;
            boolean beforeGrabbed = true;
            for (Block draggedBlock : draggedBlocks) {
              CodeRow original = rows.get(draggedBlock.id());
              float rowHeight = original == null ? actor.getHeight() : original.getHeight();
              if (draggedBlock.id().equals(block.id())) beforeGrabbed = false;
              if (beforeGrabbed) grabFromTop += rowHeight;
              CodeRow ghost = new CodeRow(draggedBlock.action() == Action.CALL);
              ghost.selected = true;
              ghost.add(SoulweaverUI.zoomSyntaxLabel(syntax(draggedBlock), 18)).growX().minWidth(0);
              preview.add(ghost).width(actor.getWidth()).height(rowHeight).row();
            }
            preview.pack();
            Vector2 origin = actor.localToStageCoordinates(new Vector2());
            Vector2 extent =
                actor.localToStageCoordinates(new Vector2(actor.getWidth(), actor.getHeight()));
            float scaleX = (extent.x - origin.x) / actor.getWidth();
            float scaleY = (extent.y - origin.y) / actor.getHeight();
            preview.setTransform(true);
            preview.setScale(scaleX, scaleY);
            Group ghost = new Group();
            ghost.setSize(preview.getWidth() * scaleX, preview.getHeight() * scaleY);
            ghost.addActor(preview);
            owner
                .dragging()
                .setDragActorPosition(
                    ghost.getWidth() - x * scaleX, grabFromTop * scaleY - ghost.getHeight());
            // Moving a whole loose group preserves its current scroll position as well.
            float dropGrabFromTop =
                !copy && loose() && draggedBlocks.size() == looseIds.size()
                    ? height()
                        - actor.localToAscendantCoordinates(
                                SoulweaverMethodsNode.this, new Vector2(x, y))
                            .y
                    : grabFromTop;
            var payload = new DragAndDrop.Payload();
            payload.setObject(
                new Drag(
                    SoulweaverMethodsNode.this,
                    container,
                    block,
                    copy,
                    copy ? List.of() : draggedBlocks.stream().map(Block::id).toList(),
                    x,
                    dropGrabFromTop));
            owner.dragStarted((Drag) payload.getObject());
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
    gap.setBackground(SoulweaverUI.background(Color.valueOf("20282b"), false));
    if (empty)
      gap.add(SoulweaverUI.zoomLabel("Code hier ablegen", 18, SoulweaverUI.MUTED)).growX().pad(14);
    table.add(gap).growX().height(empty ? 65 : 6).row();
    target(gap, index, false);
  }

  private void target(Actor gap, int index, boolean row) {
    List<String> destinationIds = panelBlocks(owner.state()).stream().map(Block::id).toList();
    addTarget(
        new DragAndDrop.Target(gap) {
          @Override
          public boolean drag(
              DragAndDrop.Source source,
              DragAndDrop.Payload payload,
              float x,
              float y,
              int pointer) {
            boolean valid = payload.getObject() instanceof Drag d && validDrag(d);
            owner.dropAllowed(valid);
            if (row && gap instanceof CodeRow codeRow) {
              codeRow.insertion = valid ? (y >= gap.getHeight() / 2 ? 1 : -1) : 0;
            } else if (gap instanceof Table table) {
              table.setBackground(
                  SoulweaverUI.background(
                      valid ? SoulweaverUI.GOLD : Color.valueOf("20282b"), false));
            }
            return valid;
          }

          @Override
          public void reset(DragAndDrop.Source source, DragAndDrop.Payload payload) {
            owner.dropAllowed(false);
            if (row && gap instanceof CodeRow codeRow) codeRow.insertion = 0;
            else if (gap instanceof Table table)
              table.setBackground(SoulweaverUI.background(Color.valueOf("20282b"), false));
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

  /**
   * Moves an entire selection atomically, using a stable destination anchor.
   *
   * @param drag dragged blocks and destination container
   * @param index block position in the current panel
   */
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
      owner.send(
          Operation.MOVE_BLOCKS,
          ignored ->
              JSON.writeValueAsString(new MethodsWorkshop.Move(drag.ids(), container, anchor)),
          ignored -> {});
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
    return !drag.ids().isEmpty()
        && blocks(owner.state(), drag.container()).stream()
            .map(Block::id)
            .toList()
            .containsAll(drag.ids());
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
    CanvasGraphics.fill(batch, SoulweaverUI.INK, alpha, x(), y(), width(), height());
    CanvasGraphics.outline(
        batch,
        SoulweaverUI.GOLD,
        alpha * .7f,
        x(),
        y(),
        width(),
        height(),
        canvas() == null ? 1 : Math.max(1, 1.25f / canvas().zoom()));
  }
}
