package feature.leveleditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Scaling;
import engine.Entity;
import engine.Game;
import engine.input.ActionId;
import engine.input.GameInput;
import engine.level.DungeonLevel;
import engine.level.utils.Coordinate;
import engine.level.utils.LevelElement;
import engine.systems.CameraSystem;
import engine.utils.Point;
import engine.utils.Vector2;
import feature.entities.deco.Deco;
import feature.leveleditor.ui.ActionSetting;
import feature.leveleditor.ui.BooleanSetting;
import feature.leveleditor.ui.ColorSetting;
import feature.leveleditor.ui.DecoSetting;
import feature.leveleditor.ui.EditorTheme;
import feature.leveleditor.ui.FloatSetting;
import feature.leveleditor.ui.IntegerSetting;
import feature.leveleditor.ui.LevelEditorUI;
import feature.leveleditor.ui.NumberSliderSetting;
import feature.leveleditor.ui.PointSetting;
import feature.leveleditor.ui.PortPickerPopup;
import feature.leveleditor.ui.PrefabIcons;
import feature.leveleditor.ui.PrefabLibrary;
import feature.leveleditor.ui.PrefabOutliner;
import feature.leveleditor.ui.RegionSetting;
import feature.leveleditor.ui.SelectSetting;
import feature.leveleditor.ui.StringSetting;
import feature.leveleditor.ui.Vector2Setting;
import feature.leveleditor.ui.ViewOptionsBar;
import feature.prefabs.ArtifactOutput;
import feature.prefabs.ArtifactSlot;
import feature.prefabs.GameFlags;
import feature.prefabs.ImagePuzzleSettings;
import feature.prefabs.Placeholders;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabAction;
import feature.prefabs.PrefabEvent;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabLink;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabPropertyType;
import feature.prefabs.PrefabRegistry;
import feature.prefabs.PrefabRuntime;
import feature.prefabs.PrefabRuntimeState;
import feature.prefabs.PrefabSide;
import feature.prefabs.PrefabSpawner;
import feature.prefabs.PrefabWiring;
import feature.prefabs.PuzzlePiecePlacement;
import feature.prefabs.Region;
import feature.prefabs.shaders.PrefabShader;
import feature.prefabs.shaders.ShaderParameters;
import feature.systems.DebugDrawSystem;
import feature.systems.LevelEditorSystem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/** Editor mode for authoring registered prefab instances in a level. */
public final class PrefabMode extends LevelEditorMode {

  private static final float PICK_DISTANCE = 1.4f;
  // How far from the instance it is linked to a new prefab may be placed, in tiles.
  private static final int PLACEMENT_RADIUS = 8;
  private static final float ANCHOR_HIT_DISTANCE = 0.38f;
  private static final float DRAG_START_DISTANCE = 0.14f;
  private static final float POINT_ASSIGNMENT_PREVIEW_RADIUS = 0.12f;
  private static final Color POINT_ASSIGNMENT_PREVIEW_COLOR = new Color(0.25f, 1f, 0.45f, 0.8f);
  private static final Color DRAG_PREVIEW_COLOR = new Color(0.25f, 1f, 0.45f, 0.9f);
  private static final int SETTINGS_PAD = 8;
  private static final float LINK_PULSE_SECONDS = 0.8f;
  // Attached prefabs are listed right of their world prefab, in screen pixels.
  private static final float CHIP_OFFSET_PX = 14f;
  private static final float CHIP_INDENT_PX = 14f;
  private static final float CHIP_ROW_PX = 22f;
  private static final float CHIP_DOT_RADIUS_PX = 4f;
  private static final int CHIP_FONT_SIZE = 14;
  private static final int BADGE_FONT_SIZE = 12;
  private static final int BADGE_MAX_DOTS = 4;
  private static final int LINK_FONT_SIZE = 13;

  private String selectedName;
  private PendingPointAssignment pendingPointAssignment;
  private PendingPointAssignment lastPointAssignment;
  private PendingAnchorDrag pendingAnchorDrag;
  private SnapMode snapMode = SnapMode.OnGrid;
  private SnapMode snapModeBeforeAnchorDrag;
  private Table detailsContent;
  private Table secondaryContent;
  private Table secondaryHeader;
  private ScrollPane secondaryScrollPane;
  private String secondaryScrollInstanceName;
  private PrefabOutliner outliner;
  private ListOrder listOrder = ListOrder.ALPHABETICAL;
  private boolean rebuildPending;
  // While set, the library of prefabs to add is shown in place of the prefab list.
  private PrefabLibrary library;
  // Prefab currently dragged from the library, previewed at the cursor while over the level.
  private Prefab paletteDrag;
  // While set, the secondary details show the parameters of one shader property instead.
  private ShaderEditTarget shaderEditTarget;
  private DetailsTab detailsTab = DetailsTab.PROPERTIES;
  // How names, links and attached prefabs are shown in the world. Remembered between mode switches,
  // which recreate the mode.
  private static NameDisplay nameDisplay = NameDisplay.FOCUS;
  private static LinkDisplay linkDisplay = LinkDisplay.SELECTION;
  private static LogicDisplay logicDisplay = LogicDisplay.GROUPED;
  private final EditorOverlay overlay = new EditorOverlay();
  private ViewOptionsBar viewOptions;
  // Instance under the cursor in the world, updated every frame.
  private String hoveredName;
  // Instance under the cursor in the prefab list.
  private String listHoveredName;
  // Instances of the group under the cursor in the prefab list.
  private Set<String> listHoveredGroup = Set.of();
  // While set, the next click on a prefab in the world links it to this port.
  private PrefabConnectionsTab.LinkEnd pendingLinkPick;
  // Instances with a port that fits the pending pick, computed when the pick starts.
  private Set<String> pickTargets = Set.of();
  private PortPickerPopup openPicker;
  private List<PrefabWiring.Problem> problems = List.of();
  // Links with an error problem, to color them while rendering.
  private Set<LinkKey> errorLinks = Set.of();
  private final PrefabConnectionsTab connectionsTab =
      new PrefabConnectionsTab(new ConnectionsHost());
  // Places of the instances without a world position, next to the prefabs they are linked to.
  private Map<String, AttachedPrefabLayout.Chip> chips = Map.of();
  // Where the chips were drawn in the last frame, by name.
  private Map<String, ChipPlacement> chipPlacements = Map.of();
  // Authored instances are immutable, so their normalized form is reused between frames.
  private Map<PrefabInstance, PrefabInstance> normalizedRenderInstances = new IdentityHashMap<>();

  /**
   * Creates a prefab editor mode.
   *
   * @param levelChangedCallback callback invoked after authored prefab changes
   */
  public PrefabMode(Runnable levelChangedCallback) {
    super("Prefabs", levelChangedCallback);
  }

  @Override
  public String getHeader() {
    return "";
  }

  @Override
  public void onEnter() {
    cancelAnchorDrag();
    clearPendingPointAssignment();
    cancelLinkPick();
    if (selectedName != null && selected().isEmpty()) selectedName = null;
    if (applyGroupedOrder()) levelChanged();
    getLevel().prefabRuntimeState().revertActivity(getLevel().prefabs());
    GameFlags.clear();
    try {
      respawnAll();
    } catch (RuntimeException exception) {
      LevelEditorSystem.showFeedback(exception.getMessage(), Color.YELLOW);
    }
    rebuildPending = false;
    rebuildDetails();
  }

  @Override
  public void onExit() {
    cancelAnchorDrag();
    clearPendingPointAssignment();
    cancelLinkPick();
    paletteDrag = null;
    try {
      syncChanged();
    } catch (RuntimeException exception) {
      LevelEditorSystem.showFeedback(exception.getMessage(), Color.YELLOW);
    }
  }

  @Override
  public void onCursorLeaveWorld() {
    // A cursor assignment intentionally remains armed while the UI has focus.
    cancelAnchorDrag();
  }

  @Override
  public void execute() {
    if (GameInput.justPressed(EditorActions.SECONDARY_NEXT)
        && (pendingAnchorDrag == null || !pendingAnchorDrag.active())) {
      snapMode = snapMode.nextMode();
    }

    Point cursor = getCursorPosition();
    if (pendingLinkPick != null) {
      if (GameInput.justPressed(EditorActions.ALT)) {
        cancelLinkPick();
      } else if (GameInput.justPressed(EditorActions.PLACE)) {
        completeLinkPick(cursor);
      }
      return;
    }
    if (GameInput.justPressed(EditorActions.PLACE)) {
      if (pendingPointAssignment != null) {
        PendingPointAssignment assignment = pendingPointAssignment;
        pendingPointAssignment = null;
        assignment
            .assignment()
            .accept(propertyPositionForCursor(cursor, assignment.feedbackOffset()));
        return;
      }
      Optional<WorldAnchor> anchor = anchorNear(cursor);
      if (anchor.isPresent()) {
        clearPendingPointAssignment();
        pendingAnchorDrag = new PendingAnchorDrag(anchor.get(), cursor);
      } else {
        selectNear(cursor);
      }
    }
    if (pendingAnchorDrag != null
        && GameInput.pressed(EditorActions.PLACE)
        && !pendingAnchorDrag.active()
        && pendingAnchorDrag.startPosition().distance(cursor) > DRAG_START_DISTANCE) {
      pendingAnchorDrag = pendingAnchorDrag.activate();
      snapModeBeforeAnchorDrag = snapMode;
      SnapMode predictedSnapMode =
          PointMode.snapModeFor(pendingAnchorDrag.anchor().authoredPosition());
      if (predictedSnapMode.isMorePreciseThan(snapMode)) {
        snapMode = predictedSnapMode;
      }
    }
    if (pendingAnchorDrag != null && GameInput.justReleased(EditorActions.PLACE)) {
      PendingAnchorDrag drag = pendingAnchorDrag;
      pendingAnchorDrag = null;
      if (drag.active()) {
        try {
          commitAnchorDrag(drag, propertyPositionForCursor(cursor, drag.anchor().feedbackOffset()));
        } finally {
          restoreSnapModeAfterAnchorDrag();
        }
      } else {
        restoreSnapModeAfterAnchorDrag();
        selectAnchorInstance(drag.anchor().instanceName());
      }
    }
    if (pendingAnchorDrag != null && !GameInput.pressed(EditorActions.PLACE)) {
      cancelAnchorDrag();
    }
    if (GameInput.justPressed(EditorActions.TERTIARY)) {
      pendingPointAssignment = lastPointAssignment;
    }
  }

  @Override
  public void executeShortcuts() {
    if (GameInput.justPressed(EditorActions.CANCEL)) cancelOrDeselect();
    if (GameInput.justPressed(EditorActions.FRAME_SELECTION)) {
      selected().ifPresent(i -> focus(i.name()));
    }
    if (GameInput.justPressed(EditorActions.SEARCH) && library == null && outliner != null) {
      outliner.focusSearch();
    }
    if (GameInput.justPressed(EditorActions.DUPLICATE)) duplicateSelected();
  }

  /*
   * Esc ends what is in progress, a link pick or a point assignment, closes the library, or else
   * clears the selection.
   */
  private void cancelOrDeselect() {
    if (pendingLinkPick != null) {
      cancelLinkPick();
    } else if (pendingPointAssignment != null) {
      pendingPointAssignment = null;
    } else if (library != null) {
      toggleLibrary();
    } else if (selectedName != null) {
      cancelAnchorDrag();
      selectedName = null;
      requestRebuild();
    }
  }

  @Override
  public Table viewOptionsUI() {
    if (viewOptions == null) {
      viewOptions =
          new ViewOptionsBar()
              .option(
                  "Names",
                  () -> nameDisplay.label(),
                  () -> nameDisplay = next(nameDisplay),
                  "Focus: names of the selected, hovered and linked prefabs. All: every name"
                      + " that fits. Hold Alt to see all names for a moment.")
              .option(
                  "Links",
                  () -> linkDisplay.label(),
                  () -> linkDisplay = next(linkDisplay),
                  "Selection: links of the selected and the hovered prefab. All: every link,"
                      + " the ones of the selection highlighted. Hold Ctrl to see all links for a"
                      + " moment.")
              .option(
                  "Logic",
                  () -> logicDisplay.label(),
                  () -> logicDisplay = next(logicDisplay),
                  "Dialogs, sounds and logic have no place in the level and are shown next to"
                      + " the prefab they belong to. Grouped: a badge, listed when in focus."
                      + " Listed: always listed. Hidden: only shown when in focus.");
    }
    return viewOptions;
  }

  private static <E extends Enum<E>> E next(E value) {
    E[] values = value.getDeclaringClass().getEnumConstants();
    return values[(value.ordinal() + 1) % values.length];
  }

  @Override
  public void render() {
    DungeonLevel level = getLevel();
    List<PrefabWiring.Connection> connections = PrefabWiring.connections(level.prefabs());
    Map<PrefabInstance, PrefabInstance> normalized = new IdentityHashMap<>();
    for (PrefabInstance source : level.prefabs()) {
      PrefabInstance instance = normalizedRenderInstances.get(source);
      if (instance == null) instance = PrefabRegistry.require(source.type()).normalize(source);
      normalized.put(source, instance);
    }
    normalizedRenderInstances = normalized;
    chips = layoutChips(connections);
    Set<String> related = relatedToSelection(connections);
    hoveredName = hoveredInWorld().orElse(null);
    placeChips(level, related);

    for (PrefabInstance source : level.prefabs()) {
      if (chips.containsKey(source.name())) continue;
      PrefabInstance instance = normalized.get(source);
      Point highlighted =
          pendingAnchorDrag != null
                  && pendingAnchorDrag.active()
                  && pendingAnchorDrag.anchor().instanceName().equals(source.name())
              ? pendingAnchorDrag.anchor().displayPosition()
              : null;
      boolean selected = Objects.equals(selectedName, source.name());
      PrefabRegistry.require(source.type())
          .renderEditorFeedback(
              level,
              instance,
              new PrefabOverlayFeedback(overlay, look(instance, related, highlighted)),
              selected);
    }
    renderLinks(level, connections);
    renderChips(level, related);
    if (pendingLinkPick != null) {
      for (PrefabInstance instance : level.prefabs()) {
        if (pickTargets.contains(instance.name())) highlightTarget(instance);
      }
    }
    if (pendingAnchorDrag != null && pendingAnchorDrag.active()) {
      Point destination =
          feedbackPositionForCursor(
              getCursorPosition(), pendingAnchorDrag.anchor().feedbackOffset());
      DebugDrawSystem.drawPoint(destination, POINT_ASSIGNMENT_PREVIEW_RADIUS, DRAG_PREVIEW_COLOR);
    }
    if (pendingPointAssignment != null) {
      DebugDrawSystem.drawPoint(
          feedbackPositionForCursor(getCursorPosition(), pendingPointAssignment.feedbackOffset()),
          POINT_ASSIGNMENT_PREVIEW_RADIUS,
          POINT_ASSIGNMENT_PREVIEW_COLOR);
    }
    if (paletteDrag != null && !isCursorOverUI() && isAttached(paletteDrag)) {
      Point cursor = getCursorPosition();
      prefabNear(cursor).ifPresent(this::highlightTarget);
      com.badlogic.gdx.math.Vector2 screen = EditorOverlay.toScreen(cursor);
      overlay.dot(screen, CHIP_DOT_RADIUS_PX, EditorTheme.SELECTION, null);
      overlay.text(
          paletteDrag.displayName(),
          screen,
          EditorOverlay.Anchor.RIGHT,
          CHIP_DOT_RADIUS_PX + 5f,
          CHIP_FONT_SIZE,
          EditorTheme.LABEL_SELECTED,
          EditorOverlay.PRIORITY_SELECTED,
          true);
    } else if (paletteDrag != null && !isCursorOverUI()) {
      try {
        PrefabInstance preview =
            placedInstance(paletteDrag, newInstanceName(paletteDrag), getCursorPosition());
        paletteDrag.renderEditorFeedback(
            level,
            preview,
            new PrefabOverlayFeedback(
                overlay,
                new PrefabOverlayFeedback.Look(
                    categoryColor(preview),
                    true,
                    false,
                    true,
                    false,
                    true,
                    EditorTheme.LABEL_SELECTED,
                    EditorOverlay.PRIORITY_SELECTED,
                    "",
                    null)),
            true);
      } catch (RuntimeException invalidPreview) {
        // The preview is skipped, dropping the prefab reports the problem.
      }
    }
    overlay.flush();
  }

  /**
   * Decides how an instance is drawn: its focus state, whether its name is shown and how.
   *
   * @param instance normalized instance
   * @param related names of the instances linked to the selection
   * @param highlighted marker drawn as the original position of a drag, or {@code null}
   * @return the look of the instance
   */
  private PrefabOverlayFeedback.Look look(
      PrefabInstance instance, Set<String> related, Point highlighted) {
    String name = instance.name();
    boolean selected = name.equals(selectedName);
    boolean hovered =
        name.equals(hoveredName) || name.equals(listHoveredName) || listHoveredGroup.contains(name);
    boolean target = pendingLinkPick != null && pickTargets.contains(name);
    boolean dimmed = pendingLinkPick != null && !target && !selected;
    EditorOverlay.Look look;
    int priority;
    if (selected) {
      look = EditorTheme.LABEL_SELECTED;
      priority = EditorOverlay.PRIORITY_SELECTED;
    } else if (hovered) {
      look = EditorTheme.LABEL_FOCUSED;
      priority = EditorOverlay.PRIORITY_HOVERED;
    } else if (target) {
      look = EditorTheme.LABEL_TARGET;
      priority = EditorOverlay.PRIORITY_TARGET;
    } else if (related.contains(name)) {
      look = EditorTheme.LABEL_FOCUSED;
      priority = EditorOverlay.PRIORITY_RELATED;
    } else {
      look = instance.active() ? EditorTheme.LABEL_NORMAL : EditorTheme.LABEL_INACTIVE;
      priority = EditorOverlay.PRIORITY_NORMAL;
    }
    boolean showName =
        selected
            || hovered
            || target
            || (pendingLinkPick == null && related.contains(name))
            || (pendingLinkPick == null && allNamesShown());
    return new PrefabOverlayFeedback.Look(
        categoryColor(instance),
        selected,
        hovered,
        instance.active(),
        dimmed,
        showName,
        look,
        priority,
        instance.active() ? "" : "  (inactive)",
        highlighted);
  }

  /* Whether all links are drawn, see linkDisplay; holding Ctrl draws them too. */
  private static boolean allLinksShown() {
    return linkDisplay == LinkDisplay.ALL || GameInput.ctrl();
  }

  /* Whether the names of all instances are shown, see nameDisplay; holding Alt shows them too. */
  private static boolean allNamesShown() {
    return nameDisplay == NameDisplay.ALL || GameInput.pressed(EditorActions.SHOW_NAMES);
  }

  private static Color categoryColor(PrefabInstance instance) {
    return EditorTheme.categoryColor(PrefabCatalog.categoryOf(instance.type()));
  }

  /**
   * Returns the instance under the cursor in the world, if the cursor is in the world and nothing
   * is being dragged.
   *
   * @return the hovered instance
   */
  private Optional<String> hoveredInWorld() {
    if (isCursorOverUI() || paletteDrag != null) return Optional.empty();
    if (pendingAnchorDrag != null && pendingAnchorDrag.active()) {
      return Optional.of(pendingAnchorDrag.anchor().instanceName());
    }
    if (GameInput.pressed(EditorActions.PLACE)) return Optional.ofNullable(hoveredName);
    return prefabNear(getCursorPosition()).map(PrefabInstance::name);
  }

  /**
   * Returns the instances linked to the selected instance, in both directions.
   *
   * @param connections links of the level
   * @return names of the linked instances, without the selection itself
   */
  private Set<String> relatedToSelection(List<PrefabWiring.Connection> connections) {
    if (selectedName == null) return Set.of();
    Set<String> related = new HashSet<>();
    for (PrefabWiring.Connection connection : connections) {
      String source = connection.link().source();
      String receiver = connection.receiver().name();
      if (source.equals(selectedName)) related.add(receiver);
      if (receiver.equals(selectedName)) related.add(source);
    }
    related.remove(selectedName);
    return related;
  }

  private Map<String, AttachedPrefabLayout.Chip> layoutChips(
      List<PrefabWiring.Connection> connections) {
    return AttachedPrefabLayout.layout(getLevel().prefabs(), connections, this::worldLinkAnchor);
  }

  /**
   * Decides where the attached prefabs are drawn this frame. The prefabs attached to a world prefab
   * are listed next to it while they are in focus: while the world prefab or one of them is
   * selected or hovered, linked to the selection or a target of a link pick. Otherwise they are
   * collapsed into a badge next to the world prefab, or hidden, see {@link LogicDisplay}.
   *
   * @param level edited level
   * @param related names of the instances linked to the selection
   */
  private void placeChips(DungeonLevel level, Set<String> related) {
    // While picking a link, the related prefabs are not listed, the pick targets are shown instead.
    Set<String> focus = new HashSet<>(pendingLinkPick == null ? related : Set.of());
    if (selectedName != null) focus.add(selectedName);
    if (hoveredName != null) focus.add(hoveredName);
    if (listHoveredName != null) focus.add(listHoveredName);
    Set<String> listedRoots = new HashSet<>();
    Map<String, List<AttachedPrefabLayout.Chip>> shown = new LinkedHashMap<>();
    for (AttachedPrefabLayout.Chip chip : chips.values()) {
      boolean inFocus =
          focus.contains(chip.name())
              || chip.root().equals(selectedName)
              || chip.root().equals(hoveredName)
              || chip.root().equals(listHoveredName);
      boolean target = pendingLinkPick != null && pickTargets.contains(chip.name());
      if (inFocus || logicDisplay == LogicDisplay.EXPANDED) listedRoots.add(chip.root());
      if (inFocus || target || logicDisplay != LogicDisplay.HIDDEN) {
        shown.computeIfAbsent(chip.root(), ignored -> new ArrayList<>()).add(chip);
      }
    }
    Map<String, PrefabInstance> byName = new HashMap<>();
    for (PrefabInstance instance : level.prefabs()) byName.put(instance.name(), instance);
    Rectangle area = visibleArea();
    Map<String, ChipPlacement> result = new HashMap<>();
    shown.forEach(
        (rootName, group) -> {
          PrefabInstance root = byName.get(rootName);
          Optional<Point> rootAnchor =
              root == null ? Optional.empty() : worldLinkAnchor(normalizedForRender(root));
          if (rootAnchor.isEmpty()) return;
          com.badlogic.gdx.math.Vector2 base = EditorOverlay.toScreen(rootAnchor.get());
          boolean listed = listedRoots.contains(rootName);
          Map<String, Float> widths = new HashMap<>();
          float listWidth = 0f;
          int rows = 1;
          for (AttachedPrefabLayout.Chip chip : group) {
            PrefabInstance instance = byName.get(chip.name());
            String label =
                instance == null
                    ? chip.name()
                    : chipLabel(chip, instance) + (instance.active() ? "" : "  (inactive)");
            float width =
                listed
                    ? CHIP_DOT_RADIUS_PX * 2 + 14f + EditorOverlay.textSize(label, CHIP_FONT_SIZE).x
                    : BADGE_MAX_DOTS * 10f + 30f;
            widths.put(chip.name(), width);
            listWidth = Math.max(listWidth, chip.depth() * CHIP_INDENT_PX + width);
            rows = Math.max(rows, chip.row() + 1);
          }
          // Lists that do not fit right of their world prefab are drawn left of it.
          boolean left =
              base.x + CHIP_OFFSET_PX + listWidth > area.x + area.width
                  && base.x - CHIP_OFFSET_PX - listWidth >= area.x;
          // Lists that would reach below the visible area are moved up.
          float lowest = base.y - (listed ? rows - 1 : 0) * CHIP_ROW_PX - CHIP_ROW_PX / 2f;
          float shift = Math.max(0f, area.y + 4f - lowest);
          float side = left ? -1f : 1f;
          for (AttachedPrefabLayout.Chip chip : group) {
            int depth = listed ? chip.depth() : 0;
            int row = listed ? chip.row() : 0;
            com.badlogic.gdx.math.Vector2 marker =
                new com.badlogic.gdx.math.Vector2(
                    base.x + side * (CHIP_OFFSET_PX + CHIP_DOT_RADIUS_PX + depth * CHIP_INDENT_PX),
                    base.y + shift - row * CHIP_ROW_PX);
            float width = widths.get(chip.name());
            Rectangle hitBox =
                left
                    ? new Rectangle(
                        marker.x + CHIP_DOT_RADIUS_PX + 3f - width - 6f,
                        marker.y - CHIP_ROW_PX / 2f,
                        width + 6f,
                        CHIP_ROW_PX)
                    : new Rectangle(
                        marker.x - CHIP_DOT_RADIUS_PX - 3f,
                        marker.y - CHIP_ROW_PX / 2f,
                        width + 6f,
                        CHIP_ROW_PX);
            result.put(
                chip.name(),
                new ChipPlacement(
                    chip, marker, EditorOverlay.toWorld(marker), listed, left, hitBox));
          }
        });
    chipPlacements = result;
  }

  // The part of the screen where the level is not covered by panels.
  private Rectangle visibleArea() {
    LevelEditorUI ui = detailsContent == null ? null : editorUi(detailsContent);
    if (ui == null) return new Rectangle(0f, 0f, Game.windowWidth(), Game.windowHeight());
    return ui.worldArea();
  }

  // Draws the instances without a world position next to the prefabs they are attached to.
  private void renderChips(DungeonLevel level, Set<String> related) {
    Map<String, PrefabInstance> byName = new HashMap<>();
    for (PrefabInstance instance : level.prefabs()) byName.put(instance.name(), instance);
    Map<String, List<ChipPlacement>> collapsed = new LinkedHashMap<>();
    for (ChipPlacement placement : chipPlacements.values()) {
      PrefabInstance source = byName.get(placement.chip().name());
      if (source == null) continue;
      PrefabInstance instance = normalizedForRender(source);
      if (!placement.listed()) {
        collapsed
            .computeIfAbsent(placement.chip().root(), ignored -> new ArrayList<>())
            .add(placement);
        continue;
      }
      PrefabOverlayFeedback.Look look = look(instance, related, null);
      Color fill =
          look.selected()
              ? EditorTheme.SELECTION
              : instance.active()
                  ? EditorTheme.alpha(look.color(), look.dimmed() ? 0.35f : 1f)
                  : EditorTheme.TEXT_DISABLED;
      overlay.dot(
          placement.marker(),
          CHIP_DOT_RADIUS_PX,
          fill,
          look.selected() || look.hovered() ? Color.WHITE : null);
      String label = chipLabel(placement.chip(), instance) + look.nameSuffix();
      if (look.selected()) {
        Optional<String> summary = PrefabRegistry.require(instance.type()).editorSummary(instance);
        if (summary.isPresent()) label += "  (" + summary.get() + ")";
      }
      EditorOverlay.Look labelLook =
          look.namePriority() > EditorOverlay.PRIORITY_NORMAL
              ? look.nameLook()
              : instance.active() ? EditorTheme.LABEL_FOCUSED : EditorTheme.LABEL_INACTIVE;
      if (look.dimmed()) labelLook = EditorTheme.LABEL_INACTIVE;
      overlay.text(
          label,
          placement.marker(),
          placement.left() ? EditorOverlay.Anchor.LEFT : EditorOverlay.Anchor.RIGHT,
          CHIP_DOT_RADIUS_PX + 4f,
          CHIP_FONT_SIZE,
          labelLook,
          Math.max(look.namePriority(), EditorOverlay.PRIORITY_CHIP + 15),
          true);
    }
    for (List<ChipPlacement> group : collapsed.values()) {
      List<ChipPlacement> ordered = new ArrayList<>(group);
      ordered.sort(Comparator.comparingInt(placement -> placement.chip().row()));
      List<Color> dots = new ArrayList<>();
      for (ChipPlacement placement : ordered) {
        if (dots.size() == BADGE_MAX_DOTS) break;
        PrefabInstance instance = byName.get(placement.chip().name());
        dots.add(
            instance == null || !instance.active()
                ? EditorTheme.TEXT_DISABLED
                : EditorTheme.alpha(categoryColor(instance), pendingLinkPick != null ? 0.35f : 1f));
      }
      String count = ordered.size() > BADGE_MAX_DOTS ? "+" + (ordered.size() - BADGE_MAX_DOTS) : "";
      boolean target =
          ordered.stream().anyMatch(placement -> pickTargets.contains(placement.chip().name()));
      ChipPlacement first = ordered.getFirst();
      com.badlogic.gdx.math.Vector2 marker = first.marker();
      overlay.badge(
          dots,
          count,
          new com.badlogic.gdx.math.Vector2(
              marker.x + (first.left() ? CHIP_DOT_RADIUS_PX : -CHIP_DOT_RADIUS_PX), marker.y),
          first.left() ? EditorOverlay.Anchor.LEFT : EditorOverlay.Anchor.RIGHT,
          0f,
          BADGE_FONT_SIZE,
          target
              ? new EditorOverlay.Look(EditorTheme.TARGET, new Color(0.04f, 0.24f, 0.1f, 0.92f))
              : EditorTheme.LABEL_NORMAL,
          EditorOverlay.PRIORITY_WARNING + 1);
    }
  }

  /* Inside the area of the world prefab a chain starts at, the area prefix is omitted. */
  private static String chipLabel(AttachedPrefabLayout.Chip chip, PrefabInstance instance) {
    String area = PrefabNames.areaPrefix(chip.name());
    if (area == null || !area.equals(PrefabNames.areaPrefix(chip.root()))) return chip.name();
    return chip.name().substring(area.length() + 1);
  }

  // Rings an instance that can be clicked, e.g. to finish a link.
  private void highlightTarget(PrefabInstance instance) {
    ChipPlacement chip = chipPlacements.get(instance.name());
    if (chip != null) {
      if (chip.listed()) {
        overlay.dot(chip.marker(), CHIP_DOT_RADIUS_PX + 3f, EditorTheme.TARGET, null);
      }
      return;
    }
    worldLinkAnchor(normalizedForRender(instance))
        .ifPresent(
            anchor -> {
              DebugDrawSystem.drawCircleOutline(anchor, 0.4f, EditorTheme.TARGET);
              DebugDrawSystem.drawCircleOutline(anchor, 0.43f, EditorTheme.TARGET);
            });
  }

  private PrefabInstance normalizedForRender(PrefabInstance source) {
    PrefabInstance instance = normalizedRenderInstances.get(source);
    return instance == null ? PrefabRegistry.require(source.type()).normalize(source) : instance;
  }

  /**
   * Returns whether instances of a prefab have no world position, so the editor draws them next to
   * the prefabs they are linked to.
   *
   * @param prefab prefab definition
   * @return true if the prefab has no point or region property
   */
  private boolean isAttached(Prefab prefab) {
    return worldAnchors(prefab.newInstance(prefab.type())).isEmpty();
  }

  private boolean isAttached(PrefabInstance instance) {
    try {
      return worldAnchors(instance).isEmpty();
    } catch (IllegalArgumentException invalidInstance) {
      return false;
    }
  }

  /**
   * Draws the links of the selected instance with their port names, faint links of the hovered
   * instance, and all other links faintly if {@link LinkDisplay#ALL} is chosen.
   *
   * @param level edited level
   * @param connections links of the level
   */
  private void renderLinks(DungeonLevel level, List<PrefabWiring.Connection> connections) {
    List<PrefabInstance> instances = level.prefabs();
    Map<String, PrefabInstance> byName = new HashMap<>();
    for (PrefabInstance instance : instances) byName.put(instance.name(), instance);
    Map<String, List<String>> labelsByPair = new LinkedHashMap<>();
    Map<String, Point> labelPositions = new HashMap<>();
    Map<String, Color> labelColors = new HashMap<>();
    String hovered = hoveredName != null ? hoveredName : listHoveredName;
    PrefabRuntimeState runtime = level.prefabRuntimeState();
    long now = System.nanoTime();
    for (PrefabWiring.Connection connection : connections) {
      String source = connection.link().source();
      String receiver = connection.receiver().name();
      boolean ofSelection =
          Objects.equals(selectedName, source) || Objects.equals(selectedName, receiver);
      boolean ofHovered = Objects.equals(hovered, source) || Objects.equals(hovered, receiver);
      if (!ofSelection && !ofHovered && !allLinksShown()) continue;
      PrefabInstance sourceInstance = byName.get(source);
      if (sourceInstance == null) continue;
      Optional<Point> from = linkAnchor(sourceInstance);
      Optional<Point> to = linkAnchor(connection.receiver());
      if (from.isEmpty() || to.isEmpty()) continue;

      boolean signal = connection.input().kind().isSignal();
      Color color =
          (signal ? PrefabConnectionsTab.SIGNAL_COLOR : PrefabConnectionsTab.ARTIFACT_COLOR).cpy();
      if (errorLinks.contains(LinkKey.of(connection))) color.set(PrefabConnectionsTab.ERROR_COLOR);
      float pulse =
          runtime
              .lastSignal(PrefabRuntime.signalKey(connection))
              .map(time -> 1f - (now - time) / 1e9f / LINK_PULSE_SECONDS)
              .filter(value -> value > 0f)
              .orElse(0f);
      color.lerp(Color.WHITE, pulse * 0.6f);
      float baseAlpha = ofSelection ? 0.95f : ofHovered ? 0.6f : 0.3f;
      color.a = Math.max(baseAlpha, pulse);
      float width = (ofSelection ? 0.05f : 0.03f) * (1f + pulse * 1.5f);

      Point labelPosition;
      if (source.equals(receiver)) {
        Point loopCenter = from.get().translate(0f, 0.55f);
        DebugDrawSystem.drawCircleOutline(loopCenter, 0.3f, color);
        labelPosition = loopCenter.translate(0f, 0.3f);
      } else {
        DebugDrawSystem.drawLinkLine(from.get(), to.get(), width, color);
        labelPosition =
            new Point(
                (from.get().x() + to.get().x()) * 0.5f, (from.get().y() + to.get().y()) * 0.5f);
      }
      if (!ofSelection || withinChipGroup(source, receiver)) continue;
      String pairKey =
          source.compareTo(receiver) < 0 ? source + "\n" + receiver : receiver + "\n" + source;
      String output = PrefabConnectionsTab.outputName(instances, connection.link());
      labelsByPair
          .computeIfAbsent(pairKey, ignored -> new ArrayList<>())
          .add(output + " -> " + connection.input().displayName());
      labelPositions.putIfAbsent(pairKey, labelPosition);
      Color labelColor = color.cpy();
      labelColor.a = 1f;
      labelColors.putIfAbsent(pairKey, labelColor);
    }
    labelsByPair.forEach(
        (pair, lines) ->
            overlay.text(
                String.join("\n", lines),
                EditorOverlay.toScreen(labelPositions.get(pair)),
                EditorOverlay.Anchor.ABOVE,
                8f,
                LINK_FONT_SIZE,
                new EditorOverlay.Look(labelColors.get(pair), EditorTheme.PLATE),
                EditorOverlay.PRIORITY_LINK,
                false));
  }

  /**
   * Returns whether a link connects an attached prefab with the world prefab its chip group is
   * drawn at, or with another chip of the group. These links are too short for a readable label.
   *
   * @param source name of the source instance
   * @param receiver name of the receiving instance
   * @return true if at least one end is an attached prefab and both ends share a group
   */
  private boolean withinChipGroup(String source, String receiver) {
    AttachedPrefabLayout.Chip sourceChip = chips.get(source);
    AttachedPrefabLayout.Chip receiverChip = chips.get(receiver);
    if (sourceChip == null && receiverChip == null) return false;
    String sourceRoot = sourceChip == null ? source : sourceChip.root();
    String receiverRoot = receiverChip == null ? receiver : receiverChip.root();
    return sourceRoot.equals(receiverRoot);
  }

  /**
   * Identity of one link, to look up its problems.
   *
   * @param receiver name of the receiving instance
   * @param port key of the receiving input port
   * @param link the link
   */
  private record LinkKey(String receiver, String port, PrefabLink link) {
    static LinkKey of(PrefabWiring.Connection connection) {
      return new LinkKey(connection.receiver().name(), connection.input().key(), connection.link());
    }
  }

  /**
   * Where an attached prefab was drawn in the last frame.
   *
   * @param chip its place in the list of its world prefab
   * @param marker screen position of its marker, or of the badge of its collapsed list
   * @param world world position of the marker, where its links start and end
   * @param listed whether it is listed with its name, not collapsed into a badge
   * @param left whether the list is drawn left of its world prefab, because it does not fit right
   * @param hitBox screen area that selects it when clicked, if it is listed
   */
  private record ChipPlacement(
      AttachedPrefabLayout.Chip chip,
      com.badlogic.gdx.math.Vector2 marker,
      Point world,
      boolean listed,
      boolean left,
      Rectangle hitBox) {}

  /**
   * Returns the world position where links of an instance start and end: its first point, the
   * center of its first region, or its marker next to the prefab it is attached to.
   *
   * @param instance normalized instance
   * @return link anchor, if the instance has a world position or is attached to a prefab
   */
  private Optional<Point> linkAnchor(PrefabInstance instance) {
    Optional<Point> anchor = worldLinkAnchor(instance);
    if (anchor.isPresent()) return anchor;
    ChipPlacement chip = chipPlacements.get(instance.name());
    return chip == null ? Optional.empty() : Optional.of(chip.world());
  }

  /**
   * Returns the link anchor of an instance from its own world position: its first point, or the
   * center of its first region.
   *
   * @param instance normalized instance
   * @return link anchor, if the instance has a world position
   */
  private Optional<Point> worldLinkAnchor(PrefabInstance instance) {
    List<WorldAnchor> anchors;
    try {
      anchors = worldAnchors(instance);
    } catch (IllegalArgumentException invalidInstance) {
      return Optional.empty();
    }
    if (anchors.isEmpty()) return Optional.empty();
    WorldAnchor first = anchors.getFirst();
    if (first.corner() == AnchorCorner.BOTTOM_LEFT && anchors.size() > 1) {
      Point other = anchors.get(1).displayPosition();
      return Optional.of(
          new Point(
              (first.displayPosition().x() + other.x()) * 0.5f,
              (first.displayPosition().y() + other.y()) * 0.5f));
    }
    return Optional.of(first.displayPosition());
  }

  @Override
  public void buildDetailsUI(Table content) {
    detailsContent = content;
    content.clearChildren();
    outliner = new PrefabOutliner(new OutlinerListener());
    showListOrLibrary();
    rebuildDetails();
  }

  // The left panel shows either the prefab list or, while adding, the library.
  private void showListOrLibrary() {
    if (detailsContent == null || outliner == null) return;
    detailsContent.clearChildren();
    detailsContent.add(library != null ? library : outliner).grow().minHeight(0f);
    listHoveredName = null;
    listHoveredGroup = Set.of();
  }

  @Override
  public boolean fillsDetailsHeight() {
    return true;
  }

  @Override
  public boolean hasSecondaryDetailsUI() {
    return selected().isPresent();
  }

  @Override
  public void buildSecondaryDetailsUI(Table content) {
    content.clearChildren();
    secondaryHeader = new Table();
    secondaryHeader.defaults().growX().minWidth(0f);
    secondaryContent = new Table();
    secondaryContent.top().left();
    secondaryContent.defaults().growX().minWidth(0f);
    secondaryScrollPane = EditorTheme.scrollPane(secondaryContent);
    secondaryScrollPane.setScrollbarsOnTop(false);
    content.add(secondaryHeader).growX().minWidth(0f).row();
    content.add(secondaryScrollPane).grow().minWidth(0f).minHeight(0f);
    secondaryScrollInstanceName = null;
    rebuildDetails();
  }

  @Override
  public void updateSecondaryDetailsUI() {
    if (rebuildPending) {
      rebuildPending = false;
      rebuildDetails();
    }
  }

  @Override
  public void updateDetailsUI() {
    if (rebuildPending) {
      rebuildPending = false;
      rebuildDetails();
    }
  }

  @Override
  public Table toolbarUI() {
    return null;
  }

  @Override
  public String additionalInformation() {
    return "";
  }

  @Override
  public String statusText() {
    return "Snap: " + snapMode.name();
  }

  @Override
  public String prompt() {
    if (pendingLinkPick != null) {
      return "Click a highlighted prefab to link '"
          + pendingLinkPick.port().displayName()
          + "'   -   "
          + GameInput.label(EditorActions.ALT)
          + " or "
          + GameInput.label(EditorActions.CANCEL)
          + " cancels";
    }
    if (pendingPointAssignment != null) {
      return "Click in the level to set the point   -   "
          + GameInput.label(EditorActions.CANCEL)
          + " cancels";
    }
    return null;
  }

  @Override
  public Map<ActionId, String> controls() {
    Map<ActionId, String> controls = new LinkedHashMap<>();
    controls.put(EditorActions.PLACE, "Select, drag points");
    controls.put(EditorActions.SECONDARY_NEXT, "Snap mode");
    controls.put(EditorActions.TERTIARY, "Pick last point again");
    controls.put(EditorActions.ALT, "Cancel link pick");
    controls.put(EditorActions.SHOW_NAMES, "Show all names");
    controls.put(EditorActions.FRAME_SELECTION, "Center selection");
    controls.put(EditorActions.CANCEL, "Deselect");
    return controls;
  }

  private void rebuildDetails() {
    rebuildDetails(true);
  }

  private void rebuildDetails(boolean rebuildSecondary) {
    if (outliner == null) return;
    problems = PrefabWiring.problems(getLevel().prefabs());
    errorLinks = new HashSet<>();
    for (PrefabWiring.Problem problem : problems) {
      if (problem.severity() == PrefabWiring.Severity.ERROR) {
        errorLinks.add(new LinkKey(problem.instance(), problem.port(), problem.link()));
      }
    }
    List<PrefabWiring.Connection> connections = PrefabWiring.connections(getLevel().prefabs());
    chips = layoutChips(connections);
    outliner.setItems(listItems(), selectedName, listOrder == ListOrder.LOAD_ORDER);
    if (!rebuildSecondary || secondaryContent == null || secondaryHeader == null) return;
    boolean preserveScroll =
        Objects.equals(secondaryScrollInstanceName, selectedName) && secondaryScrollPane != null;
    float scrollY = preserveScroll ? secondaryScrollPane.getScrollY() : 0f;
    secondaryHeader.clearChildren();
    secondaryContent.clearChildren();
    if (shaderEditTarget != null && !shaderEditTarget.instanceName().equals(selectedName)) {
      shaderEditTarget = null;
    }
    selected()
        .ifPresent(
            instance -> {
              Prefab prefab = PrefabRegistry.require(instance.type());
              TextField name = EditorTheme.textField(instance.name(), 16);
              name.setMessageText("Prefab name");
              final String[] lastSubmittedName = {instance.name()};
              Consumer<String> commitName =
                  value -> {
                    if (Objects.equals(value, lastSubmittedName[0])) return;
                    lastSubmittedName[0] = value;
                    rename(instance, value);
                  };
              name.setTextFieldListener(
                  (field, character) -> {
                    if (character == '\r' || character == '\n') {
                      commitName.accept(field.getText());
                    }
                  });
              name.addListener(
                  new FocusListener() {
                    @Override
                    public void keyboardFocusChanged(
                        FocusListener.FocusEvent event, Actor actor, boolean focused) {
                      if (!focused) commitName.accept(name.getText());
                    }
                  });
              Table nameRow = new Table();
              nameRow.add(PrefabIcons.create(instance.type(), 24f)).size(24f).padRight(8f);
              nameRow.add(name).growX().minWidth(0f).height(32f);
              secondaryHeader.add(nameRow).growX().row();
              secondaryHeader.add(inspectorActions(prefab, instance)).growX().padTop(6f).row();
              if (isUnplaced(instance)) {
                secondaryHeader
                    .add(
                        notice(
                            "Not shown in the level until it is linked to a placed prefab. Drop it"
                                + " from Add onto a prefab, or link it in Links.",
                            EditorTheme.TEXT_MUTED,
                            null))
                    .growX()
                    .padTop(8f)
                    .row();
              }
              Optional<PrefabProperty<ShaderParameters>> editedShader =
                  editedShaderProperty(prefab, instance);
              if (editedShader.isPresent()) {
                addShaderParameterEditor(editedShader.get(), instance);
                return;
              }
              secondaryHeader.add(detailsTabs(instance)).growX().padTop(10f).padBottom(2f).row();
              if (detailsTab == DetailsTab.CONNECTIONS) {
                connectionsTab.build(secondaryContent, instance, problems);
                return;
              }
              addProblemNotice(instance);
              PropertyAccess access = new InstancePropertyAccess(instance.name());
              for (PrefabProperty<?> property : prefab.visibleProperties(instance)) {
                Optional<ArtifactSlot<?>> slot = prefab.slotFor(property);
                if (slot.isPresent() && !instance.links(slot.get()).isEmpty()) {
                  addLinkedProperty(instance, property, slot.get());
                  continue;
                }
                addProperty(property, instance.name(), access);
                slot.ifPresent(s -> addUseArtifactButton(instance, s));
                addPlaceholderHint(prefab, instance, property);
              }
            });
    secondaryScrollInstanceName = selectedName;
    secondaryScrollPane.setScrollY(scrollY);
  }

  private Table detailsTabs(PrefabInstance instance) {
    Table tabs = new Table();
    tabs.setBackground(EditorTheme.rounded(EditorTheme.FIELD, null, 5));
    tabs.pad(3f);
    tabs.defaults().minWidth(0).growX().uniformX().height(26f);
    int links = PrefabConnectionsTab.linkCount(getLevel().prefabs(), instance);
    for (DetailsTab tab : DetailsTab.values()) {
      String label = tab == DetailsTab.CONNECTIONS ? tab.label + "  " + links : tab.label;
      TextButton button =
          EditorTheme.button(
              label, tab == detailsTab ? EditorTheme.Variant.ACTIVE : EditorTheme.Variant.GHOST);
      button.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              if (detailsTab == tab) return;
              detailsTab = tab;
              requestRebuild();
            }
          });
      tabs.add(button);
    }
    return tabs;
  }

  /**
   * Builds the row below the name of the selected instance: its prefab, whether it is active, and
   * buttons to center the camera on it, duplicate it and delete it.
   *
   * @param prefab prefab of the instance
   * @param instance selected instance
   * @return the row
   */
  private Table inspectorActions(Prefab prefab, PrefabInstance instance) {
    Table row = new Table();
    Label type = EditorTheme.label(prefab.displayName(), 13, EditorTheme.TEXT_MUTED);
    type.setEllipsis(true);
    row.add(type).growX().minWidth(0f).left().padLeft(2f);
    TextButton active =
        EditorTheme.button(
            instance.active() ? "Active" : "Inactive", EditorTheme.Variant.DEFAULT, 13);
    active.getLabel().setColor(instance.active() ? EditorTheme.SUCCESS : EditorTheme.TEXT_MUTED);
    active.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            setActive(instance.name(), !instance.active());
          }
        });
    EditorTheme.tooltip(
        active,
        instance.active()
            ? "Spawned when the level starts. Click to make it wait for an Activate action."
            : "Waits for an Activate action. Click to spawn it when the level starts.");
    row.add(active).height(26f).padRight(6f);
    ImageButton frame =
        EditorTheme.iconButton(
            EditorTheme.icon("frame"), EditorTheme.Variant.GHOST, "Center the camera on it (F)");
    frame.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            focus(instance.name());
          }
        });
    row.add(frame).size(26f);
    ImageButton duplicate =
        EditorTheme.iconButton(
            EditorTheme.icon("copy"), EditorTheme.Variant.GHOST, "Duplicate (Ctrl+D)");
    duplicate.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            duplicateSelected();
          }
        });
    row.add(duplicate).size(26f);
    ImageButton delete =
        EditorTheme.iconButton(
            EditorTheme.icon("trash", EditorTheme.ERROR),
            EditorTheme.Variant.GHOST,
            "Delete, along with all links to it");
    delete.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            delete(instance.name());
          }
        });
    row.add(delete).size(26f);
    return row;
  }

  /**
   * Creates a notice box, e.g. about problems of the selected instance.
   *
   * @param text notice text
   * @param color text color, also tints the box
   * @param icon icon name, or {@code null}
   * @return the box
   */
  private static Table notice(String text, Color color, String icon) {
    Table box = new Table();
    box.setBackground(
        EditorTheme.padded(EditorTheme.rounded(EditorTheme.alpha(color, 0.12f), null, 5), 8f, 6f));
    if (icon != null) {
      Image image = new Image(EditorTheme.icon(icon, color));
      image.setScaling(Scaling.fit);
      box.add(image).size(14f).top().padRight(7f).padTop(1f);
    }
    Label label = EditorTheme.label(text, 13, color);
    label.setWrap(true);
    box.add(label).growX().minWidth(0f);
    return box;
  }

  private void addProblemNotice(PrefabInstance instance) {
    long errors = 0;
    long warnings = 0;
    for (PrefabWiring.Problem problem : problemsOf(instance.name())) {
      if (problem.severity() == PrefabWiring.Severity.ERROR) errors++;
      else warnings++;
    }
    if (errors + warnings == 0) return;
    secondaryContent
        .add(
            notice(
                (errors + warnings)
                    + (errors + warnings == 1 ? " link needs" : " links need")
                    + " attention, see Links.",
                errors > 0 ? EditorTheme.ERROR : EditorTheme.WARNING,
                errors > 0 ? "error" : "warning"))
        .growX()
        .padTop(SETTINGS_PAD)
        .row();
  }

  private List<PrefabWiring.Problem> problemsOf(String instance) {
    List<PrefabWiring.Problem> result = new ArrayList<>();
    for (PrefabWiring.Problem problem : problems) {
      if (problem.instance().equals(instance) || problem.link().source().equals(instance)) {
        result.add(problem);
      }
    }
    return result;
  }

  private void addLinkedProperty(
      PrefabInstance instance, PrefabProperty<?> property, ArtifactSlot<?> slot) {
    Table setting = new Table();
    setting.defaults().growX().minWidth(0f);
    setting.add(EditorTheme.label(property.displayName(), 14, EditorTheme.TEXT_MUTED)).left().row();
    for (PrefabLink link : instance.links(slot)) {
      Table row = new Table();
      String output =
          PrefabWiring.outputPort(getLevel().prefabs(), link)
              .map(PrefabPort::displayName)
              .orElse(link.port());
      row.setBackground(
          EditorTheme.padded(
              EditorTheme.rounded(
                  EditorTheme.alpha(PrefabConnectionsTab.ARTIFACT_COLOR, 0.12f), null, 4),
              8f,
              2f));
      Image linkIcon = new Image(EditorTheme.icon("link", PrefabConnectionsTab.ARTIFACT_COLOR));
      row.add(linkIcon).size(13f).padRight(6f);
      Label chip =
          EditorTheme.label(
              link.source() + "  >  " + output, 14, PrefabConnectionsTab.ARTIFACT_COLOR);
      chip.setEllipsis(true);
      row.add(chip).growX().minWidth(0f).left();
      ImageButton go =
          EditorTheme.iconButton(
              EditorTheme.icon("go"), EditorTheme.Variant.GHOST, "Select " + link.source());
      go.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              focus(link.source());
            }
          });
      row.add(go).size(24f);
      ImageButton unlink =
          EditorTheme.iconButton(
              EditorTheme.icon("close", EditorTheme.ERROR),
              EditorTheme.Variant.GHOST,
              "Remove the link, the manual value below is used again");
      unlink.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              removeLink(instance.name(), slot.key(), link);
            }
          });
      row.add(unlink).size(24f);
      setting.add(row).height(28f).padTop(3f).row();
    }
    Label fallback =
        EditorTheme.label(
            "Manual value kept for when the link is removed: "
                + instance.properties().get(property.key()),
            12,
            EditorTheme.TEXT_DISABLED);
    fallback.setWrap(true);
    setting.add(fallback).row();
    secondaryContent.add(setting).growX().padTop(SETTINGS_PAD).row();
  }

  private void addUseArtifactButton(PrefabInstance instance, ArtifactSlot<?> slot) {
    TextButton button =
        EditorTheme.button(
            "Use Artifact (" + slot.type().displayName() + ")...", EditorTheme.Variant.GHOST, 13);
    button.getLabel().setColor(PrefabConnectionsTab.ARTIFACT_COLOR);
    PrefabConnectionsTab.LinkEnd end = new PrefabConnectionsTab.LinkEnd(instance.name(), slot);
    button.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            openLinkPicker(end, button);
          }
        });
    secondaryContent.add(button).right().height(26f).padTop(2f).row();
  }

  private void addPlaceholderHint(
      Prefab prefab, PrefabInstance instance, PrefabProperty<?> property) {
    for (PrefabPort port : prefab.ports(instance, PrefabPort.Kind.ARTIFACT_SLOT)) {
      if (!(port instanceof ArtifactSlot<?> slot)
          || slot.placeholderProperty().filter(p -> p == property).isEmpty()) {
        continue;
      }
      List<String> aliases = new ArrayList<>();
      for (PrefabLink link : instance.links(slot)) aliases.add(link.alias());
      String text = String.valueOf(slot.placeholderProperty().get().get(instance));
      Set<String> unknown = new LinkedHashSet<>(Placeholders.names(text));
      unknown.removeAll(aliases);
      String hint =
          aliases.isEmpty()
              ? "Link '"
                  + slot.displayName()
                  + "' artifacts in Connections to use {alias} placeholders."
              : "Placeholders: "
                  + String.join(" ", aliases.stream().map(alias -> "{" + alias + "}").toList());
      Label hintLabel = EditorTheme.label(hint, 12, EditorTheme.TEXT_MUTED);
      hintLabel.setWrap(true);
      secondaryContent.add(hintLabel).growX().padLeft(5f).row();
      if (!unknown.isEmpty()) {
        Label warning =
            EditorTheme.label(
                "Unknown placeholders (shown as written): "
                    + String.join(" ", unknown.stream().map(name -> "{" + name + "}").toList()),
                12,
                EditorTheme.WARNING);
        warning.setWrap(true);
        secondaryContent.add(warning).growX().padLeft(5f).row();
      }
    }
  }

  private Optional<PrefabProperty<ShaderParameters>> editedShaderProperty(
      Prefab prefab, PrefabInstance instance) {
    if (shaderEditTarget == null) return Optional.empty();
    Optional<PrefabProperty<ShaderParameters>> property =
        prefab.visibleProperties(instance).stream()
            .filter(p -> p.type() == PrefabPropertyType.SHADER_PARAMETERS)
            .filter(p -> p.key().equals(shaderEditTarget.propertyKey()))
            .findFirst()
            .map(PrefabMode::cast);
    if (property.isEmpty()) shaderEditTarget = null;
    return property;
  }

  private void addShaderParameterEditor(
      PrefabProperty<ShaderParameters> shaderProperty, PrefabInstance instance) {
    secondaryContent
        .add(new ActionSetting("Back", () -> editShaderParameters(null), false))
        .growX()
        .padTop(SETTINGS_PAD)
        .row();
    ShaderParameters parameters = shaderProperty.get(instance);
    var shaderLabel =
        EditorTheme.boldLabel(
            shaderProperty.displayName() + ": " + parameters.shader().displayName(),
            15,
            EditorTheme.TEXT);
    shaderLabel.setWrap(true);
    secondaryContent.add(shaderLabel).growX().padTop(SETTINGS_PAD).padLeft(5f).row();
    PropertyAccess access = new ShaderPropertyAccess(instance.name(), shaderProperty);
    boolean empty = true;
    for (PrefabProperty<?> parameter : parameters.shader().parameters()) {
      if (!parameter.isVisible(parameters.values())) continue;
      addProperty(parameter, instance.name(), access);
      empty = false;
    }
    if (empty) {
      secondaryContent
          .add(EditorTheme.label("This shader has no parameters.", 14, EditorTheme.TEXT_MUTED))
          .left()
          .padTop(SETTINGS_PAD)
          .padLeft(5f)
          .row();
    }
  }

  private void editShaderParameters(ShaderEditTarget target) {
    shaderEditTarget = target;
    requestRebuild();
  }

  private void addProperty(PrefabProperty<?> property, String instanceName, PropertyAccess access) {
    PrefabPropertyType type = property.type();
    switch (type) {
      case STRING -> {
        PrefabProperty<String> p = cast(property);
        secondaryContent
            .add(
                new StringSetting(
                    p.displayName(),
                    () -> access.get(p),
                    value -> access.set(p, value, true),
                    true))
            .growX()
            .padTop(SETTINGS_PAD)
            .row();
      }
      case INTEGER -> {
        PrefabProperty<Integer> p = cast(property);
        int min = p.minimum().orElse(Integer.MIN_VALUE).intValue();
        int max = p.maximum().orElse(Integer.MAX_VALUE).intValue();
        secondaryContent
            .add(
                new IntegerSetting(
                    p.displayName(),
                    min,
                    max,
                    () -> access.get(p),
                    value -> access.set(p, value, true)))
            .growX()
            .padTop(SETTINGS_PAD)
            .row();
      }
      case FLOAT -> {
        PrefabProperty<Float> p = cast(property);
        float min = p.minimum().orElse(-Float.MAX_VALUE).floatValue();
        float max = p.maximum().orElse(Float.MAX_VALUE).floatValue();
        secondaryContent
            .add(
                new FloatSetting(
                    p.displayName(),
                    min,
                    max,
                    () -> access.get(p),
                    value -> access.set(p, value, true)))
            .growX()
            .padTop(SETTINGS_PAD)
            .row();
      }
      case NUMBER_SLIDER -> {
        PrefabProperty<Float> p = cast(property);
        float min = p.minimum().orElse(0f).floatValue();
        float max = p.maximum().orElse(1f).floatValue();
        float step = p.step().orElse(0f).floatValue();
        secondaryContent
            .add(
                new NumberSliderSetting(
                    p.displayName(),
                    min,
                    max,
                    step,
                    () -> access.get(p),
                    value -> access.set(p, value, false)))
            .growX()
            .padTop(SETTINGS_PAD)
            .row();
      }
      case BOOLEAN -> {
        PrefabProperty<Boolean> p = cast(property);
        secondaryContent
            .add(
                new BooleanSetting(
                    p.displayName(), () -> access.get(p), value -> access.set(p, value, true)))
            .growX()
            .padTop(SETTINGS_PAD)
            .row();
      }
      case ENUM -> {
        PrefabProperty<String> p = cast(property);
        List<String> choices = new ArrayList<>(p.choices());
        // Open selections accept values that are currently not offered, which stay selectable.
        String current = access.get(p);
        if (!choices.contains(current)) choices.addFirst(current);
        String[] values = choices.toArray(String[]::new);
        secondaryContent
            .add(
                new SelectSetting<>(
                    p.displayName(),
                    values,
                    () -> access.get(p),
                    value -> access.set(p, value, true),
                    value -> value,
                    true))
            .growX()
            .padTop(SETTINGS_PAD)
            .row();
      }
      case DECO -> {
        PrefabProperty<String> p = cast(property);
        boolean optional = p.choices().contains(PrefabProperty.NO_DECORATION);
        List<Deco> choices =
            p.choices().stream()
                .filter(value -> !value.equals(PrefabProperty.NO_DECORATION))
                .map(Deco::valueOf)
                .toList();
        secondaryContent
            .add(
                new DecoSetting(
                    p.displayName(),
                    choices,
                    optional,
                    () -> decoOf(access.get(p)),
                    deco ->
                        access.set(
                            p, deco == null ? PrefabProperty.NO_DECORATION : deco.name(), true)))
            .growX()
            .padTop(SETTINGS_PAD)
            .row();
      }
      case POINT -> {
        PrefabProperty<Point> p = cast(property);
        secondaryContent
            .add(
                new PointSetting(
                    p.displayName(),
                    () -> access.get(p),
                    value -> access.set(p, value, true),
                    callback ->
                        armPointAssignment(
                            new PendingPointAssignment(callback, p.editorFeedbackOffset())),
                    true))
            .growX()
            .padTop(SETTINGS_PAD)
            .row();
      }
      case REGION -> {
        PrefabProperty<Region> p = cast(property);
        secondaryContent
            .add(
                new RegionSetting(
                    p.displayName(),
                    () -> access.get(p),
                    value -> access.set(p, value, true),
                    callback ->
                        armPointAssignment(
                            new PendingPointAssignment(callback, new Point(0f, 0f)))))
            .growX()
            .padTop(SETTINGS_PAD)
            .row();
      }
      case VECTOR2 -> {
        PrefabProperty<Vector2> p = cast(property);
        secondaryContent
            .add(
                new Vector2Setting(
                    p.displayName(), () -> access.get(p), value -> access.set(p, value, true)))
            .growX()
            .padTop(SETTINGS_PAD)
            .row();
      }
      case COLOR -> {
        PrefabProperty<Color> p = cast(property);
        secondaryContent
            .add(
                new ColorSetting(
                    p.displayName(),
                    () -> access.get(p),
                    value -> access.set(p, value, true),
                    message ->
                        LevelEditorSystem.showFeedback(
                            p.displayName() + ": " + message, Color.YELLOW)))
            .growX()
            .padTop(SETTINGS_PAD)
            .row();
      }
      case SHADER_PARAMETERS -> {
        PrefabProperty<ShaderParameters> p = cast(property);
        PrefabShader<?>[] shaders = p.shaderChoices().toArray(PrefabShader<?>[]::new);
        secondaryContent
            .add(
                new SelectSetting<>(
                    p.displayName(),
                    shaders,
                    () -> access.get(p).shader(),
                    shader -> {
                      // Switching shaders starts from the new shader's defaults.
                      if (access.get(p).shader() != shader) access.set(p, shader.defaults(), true);
                    },
                    PrefabShader::displayName,
                    true))
            .growX()
            .padTop(SETTINGS_PAD)
            .row();
        secondaryContent
            .add(
                new ActionSetting(
                    "Edit Shader Parameters",
                    () -> editShaderParameters(new ShaderEditTarget(instanceName, p.key())),
                    false))
            .growX()
            .padTop(SETTINGS_PAD)
            .row();
      }
      case IMAGE_PUZZLE_SETTINGS -> {
        addImagePuzzleSettings(cast(property), access);
      }
    }
  }

  private void addImagePuzzleSettings(
      PrefabProperty<ImagePuzzleSettings> property, PropertyAccess access) {
    secondaryContent
        .add(
            new IntegerSetting(
                "Piece Count",
                2,
                Integer.MAX_VALUE,
                () -> access.get(property).pieceCount(),
                count -> access.set(property, access.get(property).withPieceCount(count), true)))
        .growX()
        .padTop(SETTINGS_PAD)
        .row();
    secondaryContent
        .add(
            new StringSetting(
                "Seed",
                () -> Long.toString(access.get(property).seed()),
                value -> {
                  try {
                    access.set(
                        property, access.get(property).withSeed(Long.parseLong(value)), true);
                  } catch (NumberFormatException exception) {
                    LevelEditorSystem.showFeedback("Seed must be a 64-bit integer.", Color.YELLOW);
                    requestRebuild();
                  }
                },
                true))
        .growX()
        .padTop(SETTINGS_PAD)
        .row();

    for (int pieceIndex = 0; pieceIndex < access.get(property).pieceCount(); pieceIndex++) {
      final int index = pieceIndex;
      Table pieceControls = new Table();
      pieceControls.defaults().growX().minWidth(0f);
      pieceControls
          .add(
              new PointSetting(
                  "Piece " + (index + 1) + " Position",
                  () -> access.get(property).pieces().get(index).position(),
                  position -> {
                    ImagePuzzleSettings settings = access.get(property);
                    PuzzlePiecePlacement placement = settings.pieces().get(index);
                    access.set(
                        property,
                        settings.withPiece(index, placement.withPosition(position)),
                        true);
                  },
                  callback ->
                      armPointAssignment(new PendingPointAssignment(callback, new Point(0f, 0f))),
                  true))
          .row();
      pieceControls
          .add(
              new SelectSetting<>(
                  "Placement",
                  new PuzzlePiecePlacement.Destination[] {
                    PuzzlePiecePlacement.Destination.WORLD_ITEM,
                    PuzzlePiecePlacement.Destination.CHEST,
                    PuzzlePiecePlacement.Destination.DECO_INVENTORY
                  },
                  () -> access.get(property).pieces().get(index).destination(),
                  destination -> {
                    ImagePuzzleSettings settings = access.get(property);
                    PuzzlePiecePlacement placement = settings.pieces().get(index);
                    access.set(
                        property,
                        settings.withPiece(index, placement.withDestination(destination)),
                        true);
                  },
                  destination ->
                      switch (destination) {
                        case WORLD_ITEM -> "World Item";
                        case CHEST -> "Chest";
                        case DECO_INVENTORY -> "Deco Inventory";
                      },
                  true))
          .row();
      if (access.get(property).pieces().get(index).destination()
          == PuzzlePiecePlacement.Destination.DECO_INVENTORY) {
        pieceControls
            .add(
                new DecoSetting(
                    "Deco",
                    List.of(Deco.values()),
                    () -> access.get(property).pieces().get(index).deco(),
                    deco -> {
                      ImagePuzzleSettings settings = access.get(property);
                      PuzzlePiecePlacement placement = settings.pieces().get(index);
                      access.set(
                          property, settings.withPiece(index, placement.withDeco(deco)), true);
                    }))
            .row();
      }
      secondaryContent.add(pieceControls).growX().padTop(SETTINGS_PAD).row();
    }
  }

  // The decoration of a decoration property value, null for none.
  private static Deco decoOf(String value) {
    return value.equals(PrefabProperty.NO_DECORATION) ? null : Deco.valueOf(value);
  }

  private <T> void setProperty(
      PrefabProperty<T> property, T value, boolean rebuildSecondaryDetails) {
    selected()
        .ifPresent(
            source -> {
              try {
                Prefab prefab = PrefabRegistry.require(source.type());
                PrefabInstance replacement = prefab.normalize(property.set(source, value));
                boolean rebuild =
                    rebuildSecondaryDetails
                        || !visibilitySignature(prefab, source)
                            .equals(visibilitySignature(prefab, replacement));
                int index = getLevel().prefabs().indexOf(source);
                if (index >= 0) {
                  applyChange(
                      () -> {
                        getLevel().replacePrefab(index, replacement);
                        dropLinksToUnavailableOutputs(replacement.name());
                      },
                      rebuild);
                }
              } catch (IllegalArgumentException exception) {
                LevelEditorSystem.showFeedback(exception.getMessage(), Color.YELLOW);
                requestRebuild();
              }
            });
  }

  /**
   * Removes the links to outputs an instance no longer offers, e.g. after its item list was
   * shortened.
   *
   * @param name name of the changed instance
   */
  private void dropLinksToUnavailableOutputs(String name) {
    int dropped = PrefabWiring.linksToUnavailableOutputs(getLevel().prefabs(), name).size();
    if (dropped == 0) return;
    List<PrefabInstance> unlinked =
        PrefabWiring.withoutLinksToUnavailableOutputs(getLevel().prefabs(), name);
    getLevel().prefabs().clear();
    getLevel().prefabs().addAll(unlinked);
    LevelEditorSystem.showFeedback(
        "Removed "
            + dropped
            + (dropped == 1 ? " link" : " links")
            + " to outputs "
            + name
            + " no longer has.",
        Color.YELLOW);
  }

  /**
   * Lists the visible properties, shader parameters and available ports, which decide the generated
   * controls.
   *
   * @param prefab prefab definition of the instance
   * @param instance normalized instance
   * @return keys of the visible properties, shader parameters and available ports
   */
  private static List<String> visibilitySignature(Prefab prefab, PrefabInstance instance) {
    List<String> keys = new ArrayList<>();
    for (PrefabPort port : prefab.ports(instance)) keys.add("port:" + port.key());
    for (PrefabProperty<?> property : prefab.visibleProperties(instance)) {
      keys.add(property.key());
      if (property.type() != PrefabPropertyType.SHADER_PARAMETERS) continue;
      ShaderParameters parameters = PrefabMode.<ShaderParameters>cast(property).get(instance);
      keys.add(property.key() + ":" + parameters.shader().id());
      for (PrefabProperty<?> parameter : parameters.shader().parameters()) {
        if (parameter.isVisible(parameters.values())) {
          keys.add(property.key() + "." + parameter.key());
        }
      }
    }
    return keys;
  }

  private void setActive(String name, boolean active) {
    int index = indexOf(name);
    if (index < 0) return;
    PrefabInstance current = getLevel().prefabs().get(index);
    applyChange(() -> getLevel().replacePrefab(index, current.withActive(active)));
  }

  private void addLink(String receiverName, PrefabPort input, PrefabLink link) {
    int index = indexOf(receiverName);
    if (index < 0) return;
    PrefabInstance replacement;
    try {
      replacement = withLink(getLevel().prefabs().get(index), input, link);
    } catch (IllegalArgumentException exception) {
      LevelEditorSystem.showFeedback(exception.getMessage(), Color.YELLOW);
      return;
    }
    if (!applyChange(() -> getLevel().replacePrefab(index, replacement))) return;
    Optional<PrefabPort> output = PrefabWiring.outputPort(getLevel().prefabs(), link);
    if (output.isPresent() && PrefabWiring.lacksPlayer(output.get(), replacement, input)) {
      LevelEditorSystem.showFeedback(
          "'"
              + output.get().displayName()
              + "' has no player, so '"
              + input.displayName()
              + "' runs for every player.",
          Color.YELLOW);
    } else {
      LevelEditorSystem.showFeedback(
          "Linked " + link.source() + " > " + receiverName + "." + input.displayName(),
          Color.GREEN);
    }
  }

  /**
   * Adds a link to an input of an instance. A single artifact slot loses its current link.
   *
   * @param receiver instance storing the link
   * @param input input port of the receiver
   * @param link the new link
   * @return the normalized receiver with the link
   */
  private static PrefabInstance withLink(
      PrefabInstance receiver, PrefabPort input, PrefabLink link) {
    List<PrefabLink> links = new ArrayList<>(receiver.links(input));
    if (input instanceof ArtifactSlot<?> slot && !slot.multiple()) links.clear();
    links.add(link);
    return PrefabRegistry.require(receiver.type())
        .normalize(receiver.withLinks(input.key(), links));
  }

  private void removeLink(String receiverName, String port, PrefabLink link) {
    int index = indexOf(receiverName);
    if (index < 0) return;
    PrefabInstance receiver = getLevel().prefabs().get(index);
    List<PrefabLink> links = new ArrayList<>(receiver.links(port));
    if (!links.remove(link)) return;
    applyChange(() -> getLevel().replacePrefab(index, receiver.withLinks(port, links)));
  }

  private void setAlias(String receiverName, String port, PrefabLink link, String alias) {
    int index = indexOf(receiverName);
    if (index < 0) return;
    if (!PrefabLink.isValidAlias(alias)) {
      LevelEditorSystem.showFeedback(
          "Aliases may only contain letters, digits, '_' and '-'.", Color.YELLOW);
      requestRebuild();
      return;
    }
    PrefabInstance receiver = getLevel().prefabs().get(index);
    List<PrefabLink> links = new ArrayList<>(receiver.links(port));
    int linkIndex = links.indexOf(link);
    if (linkIndex < 0) return;
    links.set(linkIndex, link.withAlias(alias));
    try {
      PrefabInstance replacement =
          PrefabRegistry.require(receiver.type()).normalize(receiver.withLinks(port, links));
      applyChange(() -> getLevel().replacePrefab(index, replacement));
    } catch (IllegalArgumentException exception) {
      LevelEditorSystem.showFeedback(exception.getMessage(), Color.YELLOW);
      requestRebuild();
    }
  }

  private void focus(String name) {
    Optional<PrefabInstance> instance =
        getLevel().prefabs().stream().filter(i -> i.name().equals(name)).findFirst();
    if (instance.isEmpty()) {
      LevelEditorSystem.showFeedback("Prefab '" + name + "' does not exist.", Color.YELLOW);
      return;
    }
    cancelAnchorDrag();
    clearPendingPointAssignment();
    selectedName = name;
    linkAnchor(instance.get()).ifPresent(LevelEditorSystem::focusCamera);
    requestRebuild();
  }

  @SuppressWarnings("unchecked")
  private void test(String name, PrefabPort port) {
    if (Game.isMultiplayerClient()) return;
    Optional<Entity> player = Game.allPlayers().findFirst();
    try {
      if (PrefabRuntime.find(getLevel(), name).isEmpty()) {
        LevelEditorSystem.showFeedback("'" + name + "' was removed in this session.", Color.YELLOW);
        return;
      }
      if (port instanceof PrefabEvent<?> event) {
        PrefabRuntime.fire(
            getLevel(),
            name,
            (PrefabEvent<Object>) event,
            event.carriesPlayer() ? player.orElse(null) : null);
      } else {
        PrefabRuntime.runAction(getLevel(), name, port.key(), player);
      }
      LevelEditorSystem.showFeedback(
          (port instanceof PrefabEvent<?> ? "Sent " : "Ran ") + name + "." + port.displayName(),
          Color.GREEN);
    } catch (RuntimeException exception) {
      LevelEditorSystem.showFeedback(exception.getMessage(), Color.YELLOW);
    }
    requestRebuild();
  }

  /**
   * Lists the ports that can be linked to a port of the selected instance.
   *
   * @param end port of the selected instance
   * @param onlyInstance only list ports of this instance, or {@code null} for all instances
   * @return choosable ports, grouped by instance
   */
  private List<PortPickerPopup.Option> linkOptions(
      PrefabConnectionsTab.LinkEnd end, String onlyInstance) {
    List<PrefabInstance> instances = getLevel().prefabs();
    Optional<PrefabInstance> owner = instanceNamed(end.instance());
    if (owner.isEmpty()) return List.of();
    List<PortPickerPopup.Option> options = new ArrayList<>();
    boolean endIsInput = end.port().kind().isInput();
    BiPredicate<String, String> artifactCycle = PrefabWiring.artifactCycleCheck(instances);
    for (PrefabInstance candidate : instances) {
      if (onlyInstance != null && !candidate.name().equals(onlyInstance)) continue;
      Prefab prefab = PrefabRegistry.require(candidate.type());
      boolean candidateInWorld = !isAttached(candidate);
      for (PrefabPort port : prefab.ports(candidate)) {
        PrefabPort output = endIsInput ? port : end.port();
        PrefabPort input = endIsInput ? end.port() : port;
        if (port.kind().isInput() == endIsInput || !PrefabWiring.compatible(output, input)) {
          continue;
        }
        PrefabInstance receiver = endIsInput ? owner.get() : candidate;
        PrefabLink link =
            endIsInput
                ? new PrefabLink(candidate.name(), port.key())
                : new PrefabLink(end.instance(), end.port().key());
        boolean artifact = !input.kind().isSignal();
        if (artifact && link.source().equals(receiver.name())) continue;
        boolean linked = receiver.links(input).stream().anyMatch(l -> l.sameOutput(link));
        boolean cycle = artifact && artifactCycle.test(link.source(), receiver.name());
        // An item can only be held in one place.
        Optional<String> heldBy =
            input instanceof ArtifactSlot<?> holding && holding.holder()
                ? PrefabWiring.holders(instances, link.source(), link.port()).stream()
                    .filter(
                        holder ->
                            !holder.receiver().name().equals(receiver.name())
                                || !holder.input().key().equals(input.key()))
                    .map(holder -> holder.receiver().name())
                    .findFirst()
                : Optional.empty();
        String detail = null;
        if (linked) detail = "already linked";
        else if (cycle) detail = "would make the value depend on itself";
        else if (heldBy.isPresent()) detail = "held by " + heldBy.get();
        else if (PrefabWiring.lacksPlayer(output, receiver, input)) {
          detail = "runs for every player";
        } else if (input instanceof ArtifactSlot<?> slot
            && !slot.multiple()
            && !receiver.links(input).isEmpty()) {
          detail = "replaces the current link";
        }
        String label = pickerLabel(prefab, candidate, port);
        String group =
            candidate.name().equals(end.instance())
                ? candidate.name() + " (this prefab)"
                : candidate.name() + "  -  " + prefab.displayName();
        options.add(
            new PortPickerPopup.Option(
                candidate.name(),
                group,
                label,
                detail,
                !linked && !cycle && heldBy.isEmpty(),
                candidateInWorld,
                () -> addLink(receiver.name(), input, link)));
      }
    }
    return options;
  }

  /**
   * Returns the label of a port in the link picker, naming the value type of artifact ports.
   *
   * @param prefab prefab owning the port
   * @param instance instance owning the port
   * @param port the port
   * @return label of the port
   */
  private static String pickerLabel(Prefab prefab, PrefabInstance instance, PrefabPort port) {
    String label = prefab.portDisplayName(instance, port);
    if (port instanceof ArtifactOutput<?> artifactOutput) {
      label += " (" + artifactOutput.type().displayName() + ")";
    } else if (port instanceof ArtifactSlot<?> slot) {
      label += " (" + slot.type().displayName() + ")";
    }
    return label;
  }

  /**
   * Lists the ports of new prefabs that can be linked to a port of the selected instance, see
   * {@link #createLinked}.
   *
   * <p>Lifecycle actions are left out: a new instance is active and has nothing to reset.
   *
   * @param end port of the selected instance
   * @return choosable ports, grouped by prefab in palette order
   */
  private List<PortPickerPopup.Option> newPrefabOptions(PrefabConnectionsTab.LinkEnd end) {
    List<PrefabInstance> instances = getLevel().prefabs();
    Optional<PrefabInstance> owner = instanceNamed(end.instance());
    if (owner.isEmpty()) return List.of();
    boolean endIsInput = end.port().kind().isInput();
    // An item can only be held in one place, a new prefab cannot take it either.
    Optional<String> heldBy =
        endIsInput
            ? Optional.empty()
            : PrefabWiring.holders(instances, end.instance(), end.port().key()).stream()
                .map(holder -> holder.receiver().name())
                .findFirst();
    List<PortPickerPopup.Option> options = new ArrayList<>();
    for (PrefabCatalog.Category category : PrefabCatalog.typeCategories()) {
      for (Prefab prefab : category.prefabs()) {
        PrefabInstance candidate = prefab.newInstance(prefab.type());
        boolean inWorld = !isAttached(candidate);
        for (PrefabPort port : prefab.ports(candidate)) {
          if (port instanceof PrefabAction action && action.isLifecycle()) continue;
          PrefabPort output = endIsInput ? port : end.port();
          PrefabPort input = endIsInput ? end.port() : port;
          if (port.kind().isInput() == endIsInput || !PrefabWiring.compatible(output, input)) {
            continue;
          }
          PrefabInstance receiver =
              endIsInput ? owner.get() : forEventWithoutPlayer(candidate, output, input);
          boolean held =
              input instanceof ArtifactSlot<?> slot && slot.holder() && heldBy.isPresent();
          String detail = null;
          if (held) detail = "held by " + heldBy.get();
          else if (endIsInput
              && input instanceof ArtifactSlot<?> slot
              && !slot.multiple()
              && !receiver.links(input).isEmpty()) {
            detail = "replaces the current link";
          } else if (PrefabWiring.lacksPlayer(output, receiver, input)) {
            detail = "runs for every player";
          }
          options.add(
              new PortPickerPopup.Option(
                  prefab.type(),
                  prefab.displayName() + "  -  " + category.name(),
                  pickerLabel(prefab, candidate, port),
                  detail,
                  !held,
                  inWorld,
                  () -> createLinked(end, prefab, port)));
        }
      }
    }
    return options;
  }

  /**
   * Adds a new instance of a prefab, links one of its ports to a port of another instance and
   * selects the new instance.
   *
   * <p>Prefabs with a world position are placed near the other instance, see {@link
   * #placementNear}, the others are attached to it. A new instance linked to an event without a
   * player acts on every player, see {@link #forEventWithoutPlayer}.
   *
   * @param end port of the other instance
   * @param prefab prefab of the new instance
   * @param port port of the new instance to link to {@code end}
   */
  private void createLinked(PrefabConnectionsTab.LinkEnd end, Prefab prefab, PrefabPort port) {
    cancelAnchorDrag();
    clearPendingPointAssignment();
    cancelLinkPick();
    Optional<PrefabInstance> owner = instanceNamed(end.instance());
    if (owner.isEmpty()) return;
    boolean endIsInput = end.port().kind().isInput();
    PrefabPort output = endIsInput ? port : end.port();
    PrefabPort input = endIsInput ? end.port() : port;
    String name = newInstanceName(prefab);
    PrefabInstance added;
    // Stores the link: the other instance for a new event, the new instance for a new action.
    PrefabInstance receiver;
    boolean forEveryone;
    try {
      PrefabInstance created =
          isAttached(prefab)
              ? prefab.newInstance(name)
              : placedInstance(prefab, name, placementNear(owner.get(), prefab));
      if (endIsInput) {
        added = created;
        receiver = withLink(owner.get(), input, new PrefabLink(name, port.key()));
        forEveryone = false;
      } else {
        PrefabInstance adjusted = forEventWithoutPlayer(created, output, input);
        forEveryone = !adjusted.equals(created);
        added = withLink(adjusted, input, new PrefabLink(owner.get().name(), end.port().key()));
        receiver = added;
      }
    } catch (IllegalArgumentException exception) {
      LevelEditorSystem.showFeedback(exception.getMessage(), Color.YELLOW);
      return;
    }
    boolean applied =
        applyChange(
            () -> {
              if (endIsInput) getLevel().replacePrefab(indexOf(owner.get().name()), receiver);
              getLevel().addPrefab(added);
              applyGroupedOrder();
              selectedName = name;
            });
    if (!applied) return;
    detailsTab = DetailsTab.PROPERTIES;
    if (PrefabWiring.lacksPlayer(output, receiver, input)) {
      LevelEditorSystem.showFeedback(
          "Added "
              + name
              + ", but '"
              + output.displayName()
              + "' has no player, so '"
              + input.displayName()
              + "' runs for every player.",
          Color.YELLOW);
    } else if (forEveryone) {
      LevelEditorSystem.showFeedback(
          "Added " + name + " for every player, as '" + output.displayName() + "' has no player.",
          Color.GREEN);
    } else {
      LevelEditorSystem.showFeedback(
          "Added " + name + ", linked to " + owner.get().name() + "." + end.port().displayName(),
          Color.GREEN);
    }
  }

  /**
   * Makes a new instance act on every player if it is linked to an event without a player, so the
   * link does not need a warning, see {@link PrefabAction#forEveryone}.
   *
   * @param created new instance receiving the link
   * @param output output port of the link
   * @param input input port of the new instance
   * @return the instance acting on every player, or {@code created} if nothing changes
   */
  private static PrefabInstance forEventWithoutPlayer(
      PrefabInstance created, PrefabPort output, PrefabPort input) {
    if (!(input instanceof PrefabAction action)
        || !PrefabWiring.lacksPlayer(output, created, input)) {
      return created;
    }
    return action.forEveryone(created).orElse(created);
  }

  /**
   * Finds where a new prefab linked to an instance is placed, near the instance: on the nearest
   * free tile that suits the prefab (see {@link Prefab#suitsTile}), else on the nearest tile that
   * suits it, e.g. a door that already has a door control, else on the nearest free floor tile.
   *
   * <p>Tiles holding the instance or a point of another prefab are not free. The designer moves the
   * prefab to its final place anyway.
   *
   * @param owner instance the new prefab is linked to
   * @param prefab prefab to place
   * @return center of the chosen tile, to place the prefab like a cursor position would
   */
  private Point placementNear(PrefabInstance owner, Prefab prefab) {
    Point origin =
        linkAnchor(owner)
            .orElseGet(
                () ->
                    new Point(CameraSystem.camera().position.x, CameraSystem.camera().position.y));
    Coordinate center = origin.toCoordinate();
    Set<Coordinate> taken = new HashSet<>();
    taken.add(center);
    for (PrefabInstance instance : getLevel().prefabs()) {
      try {
        for (WorldAnchor anchor : worldAnchors(instance)) {
          if (anchor.corner() == AnchorCorner.POINT) {
            taken.add(anchor.displayPosition().toCoordinate());
          }
        }
      } catch (IllegalArgumentException invalidInstance) {
        // An invalid instance takes no tile.
      }
    }
    List<Coordinate> around = new ArrayList<>();
    for (int dy = -PLACEMENT_RADIUS; dy <= PLACEMENT_RADIUS; dy++) {
      for (int dx = -PLACEMENT_RADIUS; dx <= PLACEMENT_RADIUS; dx++) {
        around.add(new Coordinate(center.x() + dx, center.y() + dy));
      }
    }
    around.sort(Comparator.comparingInt(tile -> tile.distance(center)));
    Predicate<Coordinate> free = tile -> !taken.contains(tile);
    Predicate<Coordinate> suits =
        tile -> getLevel().tileAt(tile).filter(prefab::suitsTile).isPresent();
    Predicate<Coordinate> floor =
        tile ->
            getLevel().tileAt(tile).filter(t -> t.levelElement() == LevelElement.FLOOR).isPresent();
    for (Predicate<Coordinate> rule : List.of(free.and(suits), suits, free.and(floor))) {
      for (Coordinate tile : around) {
        if (rule.test(tile)) return tile.toCenteredPoint();
      }
    }
    return center.toCenteredPoint();
  }

  private void openLinkPicker(PrefabConnectionsTab.LinkEnd end, Actor anchor) {
    cancelLinkPick();
    LevelEditorUI ui = editorUi(anchor);
    if (ui == null || anchor.getStage() == null) return;
    com.badlogic.gdx.math.Vector2 position =
        anchor.localToStageCoordinates(new com.badlogic.gdx.math.Vector2(0f, anchor.getHeight()));
    showPicker(end, null, ui, position.x - PortPickerPopup.WIDTH - 10f, position.y);
  }

  private void showPicker(
      PrefabConnectionsTab.LinkEnd end, String onlyInstance, Group parent, float x, float y) {
    if (openPicker != null) openPicker.close();
    String verb = end.port().kind().isInput() ? "Link to '" : "Link '";
    String title =
        verb
            + end.port().displayName()
            + (end.port().kind().isInput() ? "' from:" : "' to:")
            + (onlyInstance == null ? "" : " (" + onlyInstance + ")");
    boolean allInstances = onlyInstance == null;
    showOptions(
        title,
        linkOptions(end, onlyInstance),
        allInstances,
        allInstances ? newPrefabOptions(end) : List.of(),
        "Adds the prefab next to '" + end.instance() + "' and links it.",
        parent,
        x,
        y);
  }

  private void showOptions(
      String title,
      List<PortPickerPopup.Option> options,
      boolean worldTabs,
      Group parent,
      float x,
      float y) {
    showOptions(title, options, worldTabs, List.of(), null, parent, x, y);
  }

  private void showOptions(
      String title,
      List<PortPickerPopup.Option> options,
      boolean worldTabs,
      List<PortPickerPopup.Option> newOptions,
      String newHint,
      Group parent,
      float x,
      float y) {
    if (openPicker != null) openPicker.close();
    PortPickerPopup[] picker = new PortPickerPopup[1];
    picker[0] =
        new PortPickerPopup(
            title,
            options,
            worldTabs,
            newOptions,
            newHint,
            () -> {
              if (openPicker == picker[0]) openPicker = null;
            });
    openPicker = picker[0];
    picker[0].show(parent, x, y);
  }

  private LevelEditorUI editorUi(Actor actor) {
    Actor current = actor;
    while (current != null && !(current instanceof LevelEditorUI)) current = current.getParent();
    return (LevelEditorUI) current;
  }

  private void armLinkPick(PrefabConnectionsTab.LinkEnd end) {
    cancelAnchorDrag();
    clearPendingPointAssignment();
    if (openPicker != null) openPicker.close();
    Set<String> targets = new HashSet<>();
    for (PortPickerPopup.Option option : linkOptions(end, null)) {
      if (option.enabled()) targets.add(option.instance());
    }
    if (targets.isEmpty()) {
      LevelEditorSystem.showFeedback(
          "No prefab in this level has a port that fits '" + end.port().displayName() + "'.",
          Color.YELLOW);
      return;
    }
    pickTargets = targets;
    pendingLinkPick = end;
  }

  private void cancelLinkPick() {
    pendingLinkPick = null;
    pickTargets = Set.of();
    if (openPicker != null) openPicker.close();
  }

  private void completeLinkPick(Point cursor) {
    PrefabConnectionsTab.LinkEnd end = pendingLinkPick;
    Set<String> targets = pickTargets;
    cancelLinkPick();
    Optional<PrefabInstance> target =
        getLevel().prefabs().stream()
            .filter(i -> targets.contains(i.name()))
            .filter(i -> nearestDistance(i, cursor) <= PICK_DISTANCE)
            .min(Comparator.comparingDouble(i -> nearestDistance(i, cursor)));
    if (target.isEmpty()) {
      LevelEditorSystem.showFeedback("No prefab with a matching port was clicked.", Color.YELLOW);
      return;
    }
    List<PortPickerPopup.Option> enabled =
        linkOptions(end, target.get().name()).stream()
            .filter(PortPickerPopup.Option::enabled)
            .toList();
    if (enabled.size() == 1) {
      enabled.getFirst().choose().run();
      return;
    }
    LevelEditorUI ui = detailsContent == null ? null : editorUi(detailsContent);
    Stage stage = ui == null ? null : ui.getStage();
    if (stage == null) return;
    com.badlogic.gdx.math.Vector2 position =
        stage.screenToStageCoordinates(
            new com.badlogic.gdx.math.Vector2(GameInput.pointer().x(), GameInput.pointer().y()));
    showPicker(end, target.get().name(), ui, position.x + 12f, position.y);
  }

  private void toggleLibrary() {
    EditorTheme.hideTooltips();
    library = library == null ? new PrefabLibrary(new PaletteListener(), this::isAttached) : null;
    paletteDrag = null;
    showListOrLibrary();
    if (library != null) library.focusSearch();
  }

  private boolean isCursorOverUI() {
    LevelEditorUI ui = detailsContent == null ? null : editorUi(detailsContent);
    return ui != null && ui.isCursorOverUI();
  }

  /**
   * Adds a new instance of a prefab without a world position and links it to another instance.
   *
   * @param prefab prefab to add, see {@link #isAttached(Prefab)}
   * @param partner instance to link the new one to, or {@code null} to add it unlinked
   */
  private void addAttached(Prefab prefab, String partner) {
    // The new instance joins the area of its partner.
    if (partner != null) selectedName = partner;
    String name = add(prefab, getCursorPosition());
    if (name == null || partner == null) return;
    detailsTab = DetailsTab.CONNECTIONS;
    offerLinks(name, partner);
  }

  /**
   * Links two instances: directly if exactly one link between them is possible, otherwise through a
   * list of the possible links.
   *
   * @param name instance whose ports are linked
   * @param partner instance the ports are linked to
   */
  private void offerLinks(String name, String partner) {
    Optional<PrefabInstance> instance = selectedInstance(name);
    if (instance.isEmpty()) return;
    Prefab prefab = PrefabRegistry.require(instance.get().type());
    List<PortPickerPopup.Option> options = new ArrayList<>();
    for (PrefabPort port : prefab.ports(instance.get())) {
      String portName = prefab.portDisplayName(instance.get(), port);
      for (PortPickerPopup.Option option :
          linkOptions(new PrefabConnectionsTab.LinkEnd(name, port), partner)) {
        if (!option.enabled()) continue;
        String label =
            port.kind().isInput()
                ? option.label() + "  ->  " + portName
                : portName + "  ->  " + option.label();
        options.add(
            new PortPickerPopup.Option(
                option.instance(),
                partner + "  -  " + name,
                label,
                option.detail(),
                true,
                option.inWorld(),
                option.choose()));
      }
    }
    if (options.isEmpty()) {
      LevelEditorSystem.showFeedback(
          "'" + name + "' has no port that fits a port of '" + partner + "'.", Color.YELLOW);
      return;
    }
    // The option links through addLink, which reports the result.
    if (options.size() == 1) {
      options.getFirst().choose().run();
      return;
    }
    LevelEditorUI ui = detailsContent == null ? null : editorUi(detailsContent);
    Stage stage = ui == null ? null : ui.getStage();
    if (stage == null) return;
    com.badlogic.gdx.math.Vector2 position =
        stage.screenToStageCoordinates(
            new com.badlogic.gdx.math.Vector2(GameInput.pointer().x(), GameInput.pointer().y()));
    showOptions(
        "Link '" + name + "' and '" + partner + "':",
        options,
        false,
        ui,
        position.x + 12f,
        position.y);
  }

  /**
   * Adds a new instance of a prefab and selects it.
   *
   * @param prefab prefab to add
   * @param cursor world position the first anchor of the new instance is snapped to
   * @return name of the added instance, or {@code null} if it could not be added
   */
  private String add(Prefab prefab, Point cursor) {
    cancelAnchorDrag();
    clearPendingPointAssignment();
    cancelLinkPick();
    String name = newInstanceName(prefab);
    PrefabInstance added;
    try {
      added = placedInstance(prefab, name, cursor);
    } catch (IllegalArgumentException exception) {
      LevelEditorSystem.showFeedback(exception.getMessage(), Color.YELLOW);
      return null;
    }
    applyChange(
        () -> {
          getLevel().addPrefab(added);
          applyGroupedOrder();
          selectedName = name;
        });
    return containsName(name) ? name : null;
  }

  // New instances join the area of the selected instance.
  private String newInstanceName(Prefab prefab) {
    String area = selected().map(i -> PrefabNames.areaPrefix(i.name())).orElse(null);
    String base = area == null ? prefab.type() : area + "/" + prefab.type();
    return containsName(base) ? PrefabNames.nextName(base, this::containsName) : base;
  }

  private PrefabInstance placedInstance(Prefab prefab, String name, Point cursor) {
    PrefabInstance instance = prefab.newInstance(name);
    Optional<WorldAnchor> anchor = worldAnchors(instance).stream().findFirst();
    if (anchor.isPresent()) {
      Point spawnPosition = propertyPositionForCursor(cursor, anchor.get().feedbackOffset());
      Point anchorPosition = anchor.get().authoredPosition();
      instance =
          prefab.translate(
              instance,
              Vector2.of(
                  spawnPosition.x() - anchorPosition.x(), spawnPosition.y() - anchorPosition.y()));
    }
    return prefab.normalize(instance);
  }

  private void duplicateSelected() {
    cancelAnchorDrag();
    clearPendingPointAssignment();
    selected()
        .ifPresent(
            source -> {
              String name = PrefabNames.nextName(source.name(), this::containsName);
              applyChange(
                  () -> {
                    List<PrefabInstance> prefabs = getLevel().prefabs();
                    String base = PrefabNames.baseName(name);
                    int index = prefabs.indexOf(source);
                    for (int i = 0; i < prefabs.size(); i++) {
                      if (PrefabNames.baseName(prefabs.get(i).name()).equals(base)) index = i;
                    }
                    getLevel().addPrefab(source.withName(name));
                    // Place the copy at the end of its group, or right behind its source.
                    PrefabInstance copy = prefabs.removeLast();
                    prefabs.add(index + 1, copy);
                    applyGroupedOrder();
                    selectedName = name;
                  });
            });
  }

  private void delete(String name) {
    cancelAnchorDrag();
    clearPendingPointAssignment();
    int deletedIndex = indexOf(name);
    if (deletedIndex < 0) return;
    int selectionIndex = deletedIndex;
    int removedLinks = PrefabWiring.countLinksTo(getLevel().prefabs(), Set.of(name));
    if (removedLinks > 0) {
      LevelEditorSystem.showFeedback(
          "Deleted "
              + name
              + " and "
              + removedLinks
              + (removedLinks == 1 ? " link" : " links")
              + " to it.",
          Color.YELLOW);
    }
    applyChange(
        () -> {
          getLevel().removePrefab(name);
          List<PrefabInstance> unlinked =
              PrefabWiring.removeLinksTo(getLevel().prefabs(), Set.of(name));
          getLevel().prefabs().clear();
          getLevel().prefabs().addAll(unlinked);
          if (Objects.equals(selectedName, name)) {
            selectedName =
                selectionIndex < getLevel().prefabs().size()
                    ? getLevel().prefabs().get(selectionIndex).name()
                    : null;
          }
        });
  }

  private void rename(PrefabInstance source, String name) {
    cancelAnchorDrag();
    clearPendingPointAssignment();
    if (name == null
        || name.isBlank()
        || getLevel().prefabs().stream()
            .anyMatch(i -> !i.name().equals(source.name()) && i.name().equals(name))) {
      LevelEditorSystem.showFeedback("Prefab name must be non-empty and unique.", Color.YELLOW);
      requestRebuild();
      return;
    }
    int index = indexOf(source.name());
    if (index >= 0) {
      PrefabInstance current = getLevel().prefabs().get(index);
      applyChange(
          () -> {
            getLevel().replacePrefab(index, current.withName(name));
            List<PrefabInstance> relinked =
                PrefabWiring.renameSources(getLevel().prefabs(), Map.of(current.name(), name));
            getLevel().prefabs().clear();
            getLevel().prefabs().addAll(relinked);
            applyGroupedOrder();
            selectedName = name;
            if (shaderEditTarget != null
                && shaderEditTarget.instanceName().equals(current.name())) {
              shaderEditTarget = new ShaderEditTarget(name, shaderEditTarget.propertyKey());
            }
          });
    }
  }

  private void toggleListOrder() {
    listOrder = listOrder == ListOrder.ALPHABETICAL ? ListOrder.LOAD_ORDER : ListOrder.ALPHABETICAL;
    rebuildDetails(false);
  }

  private List<PrefabOutliner.Item> listItems() {
    List<PrefabNames.Node> tree = PrefabNames.tree(getLevel().prefabs());
    if (listOrder == ListOrder.ALPHABETICAL) {
      // Areas are listed first, like folders in a file browser.
      tree = new ArrayList<>(PrefabNames.alphabetical(tree));
      tree.sort(
          Comparator.comparingInt(
              node ->
                  node instanceof PrefabNames.Branch branch
                          && branch.kind() == PrefabNames.BranchKind.AREA
                      ? 0
                      : 1));
    }
    Map<String, PrefabWiring.Severity> worst = new HashMap<>();
    for (PrefabWiring.Problem problem : problems) {
      worst.merge(problem.instance(), problem.severity(), (a, b) -> a.compareTo(b) >= 0 ? a : b);
    }
    return listItems(
        tree,
        "",
        instance -> {
          PrefabWiring.Severity severity = worst.get(instance.name());
          int problem = severity == null ? 0 : severity == PrefabWiring.Severity.ERROR ? 2 : 1;
          return new PrefabOutliner.Status(!instance.active(), isUnplaced(instance), problem);
        });
  }

  /* Instances without a world position that are not attached to a prefab in the world. */
  private boolean isUnplaced(PrefabInstance instance) {
    return !chips.containsKey(instance.name()) && isAttached(instance);
  }

  // Inside an area group, the area prefix is omitted from the displayed names.
  private static List<PrefabOutliner.Item> listItems(
      List<PrefabNames.Node> nodes,
      String hiddenPrefix,
      Function<PrefabInstance, PrefabOutliner.Status> status) {
    List<PrefabOutliner.Item> items = new ArrayList<>();
    for (PrefabNames.Node node : nodes) {
      switch (node) {
        case PrefabNames.Leaf leaf -> {
          PrefabInstance instance = leaf.instance();
          String name = instance.name();
          String type = instance.type();
          items.add(
              new PrefabOutliner.Entry(
                  leaf.id(),
                  name,
                  name.substring(hiddenPrefix.length()),
                  PrefabRegistry.find(type).map(Prefab::displayName).orElse(type),
                  () -> PrefabIcons.create(type, 16f),
                  status.apply(instance)));
        }
        case PrefabNames.Branch branch when branch.kind() == PrefabNames.BranchKind.AREA ->
            items.add(
                new PrefabOutliner.Group(
                    branch.id(),
                    branch.key(),
                    true,
                    null,
                    listItems(branch.children(), branch.key() + "/", status)));
        case PrefabNames.Branch branch -> {
          String type = commonType(branch);
          items.add(
              new PrefabOutliner.Group(
                  branch.id(),
                  branch.key().substring(hiddenPrefix.length()),
                  false,
                  type == null ? null : () -> PrefabIcons.create(type, 16f),
                  listItems(branch.children(), hiddenPrefix, status)));
        }
      }
    }
    return items;
  }

  /* The prefab type all instances of a group share, or null if they differ. */
  private static String commonType(PrefabNames.Branch branch) {
    String type = null;
    for (PrefabInstance instance : PrefabNames.instances(branch)) {
      if (type == null) type = instance.type();
      else if (!type.equals(instance.type())) return null;
    }
    return type;
  }

  /**
   * Makes the load order match the list order by keeping every name group contiguous.
   *
   * @return whether the order changed
   */
  private boolean applyGroupedOrder() {
    List<PrefabInstance> prefabs = getLevel().prefabs();
    List<PrefabInstance> ordered = PrefabNames.groupedOrder(prefabs);
    if (ordered.equals(prefabs)) return false;
    prefabs.clear();
    prefabs.addAll(ordered);
    return true;
  }

  /**
   * Renames the shared name part of all prefabs in a group.
   *
   * @param groupId ID of an area or suffix group
   * @param label displayed group label, which is a suffix of the shared name part
   * @param newLabel requested label
   * @return ID of the renamed group, or {@code null} if the rename was rejected
   */
  private String renameGroup(String groupId, String label, String newLabel) {
    cancelAnchorDrag();
    clearPendingPointAssignment();
    if (!(PrefabNames.find(PrefabNames.tree(getLevel().prefabs()), groupId)
        instanceof PrefabNames.Branch branch)) {
      requestRebuild();
      return null;
    }
    boolean area = branch.kind() == PrefabNames.BranchKind.AREA;
    if (newLabel == null || newLabel.isBlank() || (area && newLabel.contains("/"))) {
      LevelEditorSystem.showFeedback(
          area ? "Area name must not be empty or contain '/'." : "Group name must not be empty.",
          Color.YELLOW);
      requestRebuild();
      return null;
    }
    // The label omits a hidden area prefix, which is kept when renaming.
    String oldKey = branch.key();
    String newKey = oldKey.substring(0, oldKey.length() - label.length()) + newLabel;
    List<PrefabInstance> members = PrefabNames.instances(branch);
    List<PrefabInstance> renamed = new ArrayList<>();
    Map<String, String> newNames = new HashMap<>();
    String renamedSelection = selectedName;
    for (PrefabInstance instance : getLevel().prefabs()) {
      if (!members.contains(instance)) {
        renamed.add(instance);
        continue;
      }
      String name =
          area
              ? newKey + instance.name().substring(oldKey.length())
              : newKey + PrefabNames.groupSuffix(instance.name());
      if (instance.name().equals(selectedName)) renamedSelection = name;
      newNames.put(instance.name(), name);
      renamed.add(instance.withName(name));
    }
    if (renamed.stream().map(PrefabInstance::name).distinct().count() != renamed.size()) {
      LevelEditorSystem.showFeedback(
          "Renaming the group would create duplicate prefab names.", Color.YELLOW);
      requestRebuild();
      return null;
    }
    String selection = renamedSelection;
    List<PrefabInstance> relinked = PrefabWiring.renameSources(renamed, newNames);
    applyChange(
        () -> {
          getLevel().prefabs().clear();
          getLevel().prefabs().addAll(relinked);
          applyGroupedOrder();
          selectedName = selection;
        });
    return PrefabNames.groupId(branch.kind(), newKey);
  }

  private void moveItem(String parentId, String itemId, int insertionIndex) {
    List<PrefabNames.Node> moved =
        PrefabNames.moveChild(
            PrefabNames.tree(getLevel().prefabs()), parentId, itemId, insertionIndex);
    if (moved != null) reorder(PrefabNames.flatten(moved));
  }

  private void reorder(List<PrefabInstance> order) {
    if (order.equals(getLevel().prefabs())) return;
    cancelAnchorDrag();
    clearPendingPointAssignment();
    applyChange(
        () -> {
          getLevel().prefabs().clear();
          getLevel().prefabs().addAll(order);
        });
    // Unchanged instances are not respawned by a sync, so respawn everything to apply the order.
    try {
      respawnAll();
    } catch (RuntimeException exception) {
      LevelEditorSystem.showFeedback(exception.getMessage(), Color.YELLOW);
    }
  }

  private boolean containsName(String name) {
    return getLevel().prefabs().stream().anyMatch(i -> i.name().equals(name));
  }

  private int indexOf(String name) {
    for (int index = 0; index < getLevel().prefabs().size(); index++) {
      if (getLevel().prefabs().get(index).name().equals(name)) return index;
    }
    return -1;
  }

  private Optional<PrefabInstance> selected() {
    return instanceNamed(selectedName);
  }

  private Optional<PrefabInstance> instanceNamed(String name) {
    return getLevel().prefabs().stream().filter(i -> i.name().equals(name)).findFirst();
  }

  private Optional<PrefabInstance> prefabNear(Point cursor) {
    return getLevel().prefabs().stream()
        .filter(i -> nearestDistance(i, cursor) <= PICK_DISTANCE)
        .min((a, b) -> Float.compare(nearestDistance(a, cursor), nearestDistance(b, cursor)));
  }

  private void selectNear(Point cursor) {
    Optional<PrefabInstance> near = prefabNear(cursor);
    if (near.isPresent()) {
      cancelAnchorDrag();
      clearPendingPointAssignment();
      selectedName = near.get().name();
      requestRebuild();
    } else if (selected().isPresent()) {
      cancelAnchorDrag();
      clearPendingPointAssignment();
      selectedName = null;
      requestRebuild();
    }
  }

  private float nearestDistance(PrefabInstance instance, Point cursor) {
    ChipPlacement chip = chipPlacements.get(instance.name());
    if (chip != null) {
      // Listed attached prefabs are hit on their row, collapsed ones only through their world
      // prefab.
      boolean hit = chip.listed() && chip.hitBox().contains(EditorOverlay.toScreen(cursor));
      return hit ? 0f : Float.MAX_VALUE;
    }
    float best = Float.MAX_VALUE;
    for (WorldAnchor anchor : worldAnchors(instance)) {
      best = Math.min(best, (float) anchor.displayPosition().distance(cursor));
    }
    // The link anchor is where links and pick highlights are drawn, e.g. the center of a region.
    Optional<Point> linkAnchor = worldLinkAnchor(instance);
    if (linkAnchor.isPresent()) {
      best = Math.min(best, (float) linkAnchor.get().distance(cursor));
    }
    return best;
  }

  private Optional<WorldAnchor> anchorNear(Point cursor) {
    Optional<WorldAnchor> selectedAnchor =
        selected().flatMap(instance -> nearestAnchorNear(instance, cursor));
    if (selectedAnchor.isPresent()) return selectedAnchor;

    for (PrefabInstance instance : getLevel().prefabs()) {
      Optional<WorldAnchor> anchor = nearestAnchorNear(instance, cursor);
      if (anchor.isPresent()) return anchor;
    }
    return Optional.empty();
  }

  private Optional<WorldAnchor> nearestAnchorNear(PrefabInstance instance, Point cursor) {
    return worldAnchors(instance).stream()
        .filter(anchor -> anchor.displayPosition().distance(cursor) <= ANCHOR_HIT_DISTANCE)
        .min(Comparator.comparingDouble(anchor -> anchor.displayPosition().distance(cursor)));
  }

  private List<WorldAnchor> worldAnchors(PrefabInstance instance) {
    Prefab prefab = PrefabRegistry.require(instance.type());
    List<WorldAnchor> anchors = new ArrayList<>();
    for (PrefabProperty<?> property : prefab.visibleProperties(instance)) {
      if (property.type() == PrefabPropertyType.POINT) {
        PrefabProperty<Point> pointProperty = cast(property);
        Point point = pointProperty.get(instance);
        Point offset = pointProperty.editorFeedbackOffset();
        anchors.add(
            new WorldAnchor(
                instance.name(),
                pointProperty,
                point,
                point.translate(offset.x(), offset.y()),
                offset,
                AnchorCorner.POINT,
                -1));
      } else if (property.type() == PrefabPropertyType.REGION) {
        PrefabProperty<Region> regionProperty = cast(property);
        Region region = regionProperty.get(instance);
        anchors.add(
            new WorldAnchor(
                instance.name(),
                regionProperty,
                region.bottomLeft(),
                region.bottomLeft(),
                new Point(0f, 0f),
                AnchorCorner.BOTTOM_LEFT,
                -1));
        anchors.add(
            new WorldAnchor(
                instance.name(),
                regionProperty,
                region.topRight(),
                region.topRight(),
                new Point(0f, 0f),
                AnchorCorner.TOP_RIGHT,
                -1));
      } else if (property.type() == PrefabPropertyType.IMAGE_PUZZLE_SETTINGS) {
        PrefabProperty<ImagePuzzleSettings> settingsProperty = cast(property);
        ImagePuzzleSettings settings = settingsProperty.get(instance);
        for (int pieceIndex = 0; pieceIndex < settings.pieceCount(); pieceIndex++) {
          Point position = settings.piecePosition(pieceIndex);
          anchors.add(
              new WorldAnchor(
                  instance.name(),
                  settingsProperty,
                  position,
                  position,
                  new Point(0f, 0f),
                  AnchorCorner.POINT,
                  pieceIndex));
        }
      }
    }
    return anchors;
  }

  private void commitAnchorDrag(PendingAnchorDrag drag, Point snappedPropertyPosition) {
    WorldAnchor anchor = drag.anchor();
    if (!Float.isFinite(snappedPropertyPosition.x())
        || !Float.isFinite(snappedPropertyPosition.y())) {
      return;
    }
    PrefabInstance source =
        getLevel().prefabs().stream()
            .filter(instance -> instance.name().equals(anchor.instanceName()))
            .findFirst()
            .orElse(null);
    if (source == null) return;
    // The feedback offset was already accounted for when snapping the property coordinate.
    Point point = snappedPropertyPosition;
    try {
      PrefabInstance replacement;
      if (anchor.corner() == AnchorCorner.POINT && anchor.pieceIndex() >= 0) {
        PrefabProperty<ImagePuzzleSettings> settingsProperty = cast(anchor.property());
        ImagePuzzleSettings settings = settingsProperty.get(source);
        if (anchor.pieceIndex() >= settings.pieceCount()) {
          requestRebuild();
          return;
        }
        PuzzlePiecePlacement piece = settings.pieces().get(anchor.pieceIndex());
        replacement =
            settingsProperty.set(
                source, settings.withPiece(anchor.pieceIndex(), piece.withPosition(point)));
      } else if (anchor.corner() == AnchorCorner.POINT) {
        replacement = cast(anchor.property()).set(source, point);
      } else {
        PrefabProperty<Region> regionProperty = cast(anchor.property());
        Region region = regionProperty.get(source);
        Region moved;
        if (region.bottomLeft().equals(region.topRight())) {
          // A region collapsed to a single point is moved as a whole, it can only be expanded
          // through the details panel.
          moved = new Region(point, point);
        } else if (anchor.corner() == AnchorCorner.BOTTOM_LEFT) {
          moved = new Region(point, region.topRight());
        } else {
          moved = new Region(region.bottomLeft(), point);
        }
        replacement = regionProperty.set(source, moved);
      }
      replacement = PrefabRegistry.require(source.type()).normalize(replacement);
      int index = getLevel().prefabs().indexOf(source);
      if (index >= 0) {
        PrefabInstance committed = replacement;
        applyChange(
            () -> {
              getLevel().replacePrefab(index, committed);
              selectedName = anchor.instanceName();
            });
      }
    } catch (IllegalArgumentException exception) {
      LevelEditorSystem.showFeedback(exception.getMessage(), Color.YELLOW);
      requestRebuild();
    }
  }

  private Point feedbackPositionForCursor(Point cursor, Point feedbackOffset) {
    Point propertyPosition = propertyPositionForCursor(cursor, feedbackOffset);
    return propertyPosition.translate(feedbackOffset.x(), feedbackOffset.y());
  }

  /**
   * Snaps the cursor to a property coordinate whose displayed marker sits under the cursor.
   *
   * <p>Tile-based snap modes select the tile under the cursor, whose marker is the tile position
   * plus the offset (e.g. the tile center). Finer snap modes snap the marker itself, so the offset
   * is removed before snapping.
   *
   * @param cursor world cursor position
   * @param feedbackOffset offset of the displayed marker from the property coordinate
   * @return snapped property coordinate
   */
  private Point propertyPositionForCursor(Point cursor, Point feedbackOffset) {
    if (!snapMode.isAligned(feedbackOffset)) return snapMode.getPosition(cursor);
    return snapMode.getPosition(cursor.translate(-feedbackOffset.x(), -feedbackOffset.y()));
  }

  private void requestRebuild() {
    if (detailsContent == null) return;
    rebuildPending = true;
    Gdx.app.postRunnable(
        () -> {
          if (rebuildPending) {
            rebuildPending = false;
            rebuildDetails();
          }
        });
  }

  private void respawnAll() {
    try {
      PrefabSpawner.batch(
          () -> {
            despawnAll();
            for (PrefabSide side : activeSides()) PrefabSpawner.spawn(getLevel(), side);
          });
    } catch (RuntimeException exception) {
      despawnAll();
      throw exception;
    }
  }

  private boolean applyChange(Runnable mutation) {
    return applyChange(mutation, true);
  }

  /**
   * Applies an edit of the authored instances and respawns the changed ones. A failed edit is
   * rolled back and reported.
   *
   * @param mutation edit of the authored instances
   * @param rebuildDetails whether the details panel is rebuilt
   * @return whether the edit was applied
   */
  private boolean applyChange(Runnable mutation, boolean rebuildDetails) {
    List<PrefabInstance> previousPrefabs = new ArrayList<>(getLevel().prefabs());
    String previousSelection = selectedName;
    try {
      mutation.run();
      discardRuntimeChanges(previousPrefabs);
      syncChanged();
      levelChanged();
      if (rebuildDetails) requestRebuild();
      else rebuildDetails(false);
      return true;
    } catch (RuntimeException exception) {
      getLevel().prefabs().clear();
      getLevel().prefabs().addAll(previousPrefabs);
      selectedName = previousSelection;
      try {
        syncChanged();
      } catch (RuntimeException rollbackException) {
        exception.addSuppressed(rollbackException);
      }
      LevelEditorSystem.showFeedback(exception.getMessage(), Color.YELLOW);
      requestRebuild();
      return false;
    }
  }

  /**
   * Drops runtime changes of level handlers for every edited instance, so the edited authored data
   * is what gets spawned.
   *
   * @param previousPrefabs authored instances before the edit
   */
  private void discardRuntimeChanges(List<PrefabInstance> previousPrefabs) {
    Map<String, PrefabInstance> previous = new HashMap<>();
    for (PrefabInstance instance : previousPrefabs) previous.put(instance.name(), instance);
    Set<String> changed = new HashSet<>();
    for (PrefabInstance instance : getLevel().prefabs()) {
      if (!instance.equals(previous.remove(instance.name()))) changed.add(instance.name());
    }
    changed.addAll(previous.keySet());
    getLevel().prefabRuntimeState().discard(changed);
  }

  /** Respawns only the prefab instances that were added, changed, renamed or removed. */
  private void syncChanged() {
    PrefabSpawner.batch(
        () -> {
          for (PrefabSide side : activeSides()) PrefabSpawner.sync(getLevel(), side);
        });
  }

  private void despawnAll() {
    for (PrefabSide side : activeSides()) PrefabSpawner.clear(getLevel(), side);
  }

  /** Tabs of the selected instance's details. */
  private enum DetailsTab {
    PROPERTIES("Properties"),
    CONNECTIONS("Links");

    private final String label;

    DetailsTab(String label) {
      this.label = label;
    }
  }

  /** Connects the connections tab to the editor mode. */
  private final class ConnectionsHost implements PrefabConnectionsTab.Host {
    @Override
    public List<PrefabInstance> instances() {
      return getLevel().prefabs();
    }

    @Override
    public void connect(PrefabConnectionsTab.LinkEnd end, Actor anchor) {
      openLinkPicker(end, anchor);
    }

    @Override
    public void pickInWorld(PrefabConnectionsTab.LinkEnd end) {
      armLinkPick(end);
    }

    @Override
    public void removeLink(String receiver, String port, PrefabLink link) {
      PrefabMode.this.removeLink(receiver, port, link);
    }

    @Override
    public void setAlias(String receiver, String port, PrefabLink link, String alias) {
      PrefabMode.this.setAlias(receiver, port, link, alias);
    }

    @Override
    public void focus(String instance) {
      PrefabMode.this.focus(instance);
    }

    @Override
    public void test(String instance, PrefabPort port) {
      PrefabMode.this.test(instance, port);
    }

    @Override
    public boolean canTest() {
      return !Game.isMultiplayerClient();
    }

    @Override
    public void refresh() {
      requestRebuild();
    }
  }

  /** Order in which the prefab instance list is shown. */
  private enum ListOrder {
    ALPHABETICAL("Alphabetical"),
    LOAD_ORDER("Load Order");

    private final String label;

    ListOrder(String label) {
      this.label = label;
    }
  }

  /** Which names of prefabs are shown in the world. */
  enum NameDisplay {
    /** Names of the selected, hovered and linked prefabs. */
    FOCUS("Focus"),
    /** Names of all prefabs, as far as they fit. */
    ALL("All");

    private final String label;

    NameDisplay(String label) {
      this.label = label;
    }

    String label() {
      return label;
    }
  }

  /** Which links are drawn in the world. */
  enum LinkDisplay {
    /** Links of the selected and the hovered prefab. */
    SELECTION("Selection"),
    /** All links, the ones of the selection highlighted. */
    ALL("All");

    private final String label;

    LinkDisplay(String label) {
      this.label = label;
    }

    String label() {
      return label;
    }
  }

  /** How prefabs without a world position are shown next to the prefabs they are attached to. */
  enum LogicDisplay {
    /** A badge per world prefab, listed while in focus. */
    GROUPED("Grouped"),
    /** Always listed with their names. */
    EXPANDED("Listed"),
    /** Only shown while in focus. */
    HIDDEN("Hidden");

    private final String label;

    LogicDisplay(String label) {
      this.label = label;
    }

    String label() {
      return label;
    }
  }

  /** Places prefabs dragged from the library into the level. */
  private final class PaletteListener implements PrefabLibrary.Listener {
    @Override
    public void dragStarted(Prefab prefab) {
      cancelAnchorDrag();
      clearPendingPointAssignment();
      cancelLinkPick();
      paletteDrag = prefab;
    }

    @Override
    public void dropped(Prefab prefab) {
      paletteDrag = null;
      // The library may have been closed or the mode switched while dragging.
      if (library == null || library.getStage() == null || isCursorOverUI()) return;
      if (isAttached(prefab)) {
        addAttached(prefab, prefabNear(getCursorPosition()).map(PrefabInstance::name).orElse(null));
      } else {
        add(prefab, getCursorPosition());
      }
      PrefabLibrary.remember(prefab.type());
    }

    @Override
    public void clicked(Prefab prefab) {
      if (isAttached(prefab)) {
        addAttached(prefab, selected().map(PrefabInstance::name).orElse(null));
        PrefabLibrary.remember(prefab.type());
        return;
      }
      LevelEditorSystem.showFeedback(
          "Drag '" + prefab.displayName() + "' into the level to place it.", Color.WHITE);
    }

    @Override
    public void close() {
      if (library != null) toggleLibrary();
    }
  }

  /** Forwards list interactions to the editor mode. */
  private final class OutlinerListener implements PrefabOutliner.Listener {
    @Override
    public void select(String name) {
      if (Objects.equals(selectedName, name)) return;
      cancelAnchorDrag();
      clearPendingPointAssignment();
      selectedName = name;
      requestRebuild();
    }

    @Override
    public void frame(String name) {
      focus(name);
    }

    @Override
    public void hover(String name) {
      listHoveredName = name;
    }

    @Override
    public void hoverGroup(List<String> names) {
      listHoveredGroup = Set.copyOf(names);
    }

    @Override
    public String renameGroup(String groupId, String label, String newLabel) {
      return PrefabMode.this.renameGroup(groupId, label, newLabel);
    }

    @Override
    public void moveItem(String parentId, String itemId, int insertionIndex) {
      PrefabMode.this.moveItem(parentId, itemId, insertionIndex);
    }

    @Override
    public void toggleOrder() {
      toggleListOrder();
    }

    @Override
    public void add() {
      toggleLibrary();
    }
  }

  private PrefabSide[] activeSides() {
    return PrefabSide.localSides();
  }

  private void clearPendingPointAssignment() {
    pendingPointAssignment = null;
    lastPointAssignment = null;
  }

  private void cancelAnchorDrag() {
    pendingAnchorDrag = null;
    restoreSnapModeAfterAnchorDrag();
  }

  private void restoreSnapModeAfterAnchorDrag() {
    if (snapModeBeforeAnchorDrag != null) {
      snapMode = snapModeBeforeAnchorDrag;
      snapModeBeforeAnchorDrag = null;
    }
  }

  private void selectAnchorInstance(String instanceName) {
    if (selected().filter(instance -> instance.name().equals(instanceName)).isPresent()) return;
    if (getLevel().prefabs().stream().noneMatch(instance -> instance.name().equals(instanceName))) {
      return;
    }
    selectedName = instanceName;
    requestRebuild();
  }

  private void armPointAssignment(PendingPointAssignment assignment) {
    lastPointAssignment = assignment;
    pendingPointAssignment = assignment;
  }

  private record PendingPointAssignment(Consumer<Point> assignment, Point feedbackOffset) {}

  private record ShaderEditTarget(String instanceName, String propertyKey) {}

  private Optional<PrefabInstance> selectedInstance(String instanceName) {
    return selected().filter(instance -> instance.name().equals(instanceName));
  }

  /** Reads and writes the values edited by generated property controls. */
  private interface PropertyAccess {
    <T> T get(PrefabProperty<T> property);

    <T> void set(PrefabProperty<T> property, T value, boolean rebuildSecondaryDetails);
  }

  /**
   * Accesses the properties of one prefab instance.
   *
   * <p>Controls may still fire after the selection changed, so reads and writes only apply while
   * the instance is still selected.
   */
  private final class InstancePropertyAccess implements PropertyAccess {
    private final String instanceName;

    private InstancePropertyAccess(String instanceName) {
      this.instanceName = instanceName;
    }

    @Override
    public <T> T get(PrefabProperty<T> property) {
      return selectedInstance(instanceName)
          .map(instance -> property.get(instance))
          .orElse(property.defaultValue());
    }

    @Override
    public <T> void set(PrefabProperty<T> property, T value, boolean rebuildSecondaryDetails) {
      if (selectedInstance(instanceName).isPresent()) {
        setProperty(property, value, rebuildSecondaryDetails);
      }
    }
  }

  /** Accesses the parameters of the shader selected in a shader property of one instance. */
  private final class ShaderPropertyAccess implements PropertyAccess {
    private final String instanceName;
    private final PrefabProperty<ShaderParameters> shaderProperty;

    private ShaderPropertyAccess(
        String instanceName, PrefabProperty<ShaderParameters> shaderProperty) {
      this.instanceName = instanceName;
      this.shaderProperty = shaderProperty;
    }

    @Override
    public <T> T get(PrefabProperty<T> parameter) {
      return parameters(parameter)
          .map(parameters -> parameters.get(parameter))
          .orElse(parameter.defaultValue());
    }

    @Override
    public <T> void set(PrefabProperty<T> parameter, T value, boolean rebuildSecondaryDetails) {
      Optional<ShaderParameters> parameters = parameters(parameter);
      if (parameters.isEmpty()) return;
      ShaderParameters updated;
      try {
        updated = parameters.get().with(parameter, value);
      } catch (IllegalArgumentException exception) {
        LevelEditorSystem.showFeedback(exception.getMessage(), Color.YELLOW);
        requestRebuild();
        return;
      }
      setProperty(shaderProperty, updated, rebuildSecondaryDetails);
    }

    // Empty if the instance is no longer selected or another shader was selected meanwhile.
    private Optional<ShaderParameters> parameters(PrefabProperty<?> parameter) {
      return selectedInstance(instanceName)
          .map(instance -> shaderProperty.get(instance))
          .filter(
              parameters ->
                  parameters.shader().parameter(parameter.key()).orElse(null) == parameter);
    }
  }

  private enum AnchorCorner {
    POINT,
    BOTTOM_LEFT,
    TOP_RIGHT
  }

  private record WorldAnchor(
      String instanceName,
      PrefabProperty<?> property,
      Point authoredPosition,
      Point displayPosition,
      Point feedbackOffset,
      AnchorCorner corner,
      int pieceIndex) {}

  private record PendingAnchorDrag(WorldAnchor anchor, Point startPosition, boolean active) {

    private PendingAnchorDrag(WorldAnchor anchor, Point startPosition) {
      this(anchor, startPosition, false);
    }

    private PendingAnchorDrag activate() {
      return new PendingAnchorDrag(anchor, startPosition, true);
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> PrefabProperty<T> cast(PrefabProperty<?> property) {
    return (PrefabProperty<T>) property;
  }
}
