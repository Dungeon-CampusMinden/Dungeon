package feature.leveleditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
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
import feature.leveleditor.ui.EditorSetting;
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
import feature.prefabs.ArtifactSlot;
import feature.prefabs.EventPlayers;
import feature.prefabs.GameFlags;
import feature.prefabs.ImagePuzzleSettings;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabAction;
import feature.prefabs.PrefabEvent;
import feature.prefabs.PrefabGroups;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabLink;
import feature.prefabs.PrefabNames;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProblem;
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
  // Distance the list of the selection keeps to the edges of the view.
  private static final float CHIP_VIEW_MARGIN_PX = 6f;
  // Distance of the name of a stand-in to its dot, see StandIn.
  private static final float STAND_IN_LABEL_OFFSET_PX = CHIP_DOT_RADIUS_PX + 4f;
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
  private boolean loadOrder;
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
  // Problems of the level, see LevelProblems.
  private List<PrefabProblem> problems = List.of();
  // Links with an error problem, to color them while rendering.
  private final PrefabConnectionsTab connectionsTab =
      new PrefabConnectionsTab(new ConnectionsHost());
  // Places of the instances without a world position, next to the prefabs they are linked to.
  private Map<String, AttachedPrefabLayout.Chip> chips = Map.of();
  // Tree of the prefab list, updated with the list.
  private PrefabHierarchy hierarchy = PrefabHierarchy.empty();
  // Where the chips were drawn in the last frame, by name.
  private Map<String, ChipPlacement> chipPlacements = Map.of();
  // The selected world prefab at the start of its list while it is out of view, or null.
  private StandIn standIn;
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
    chips =
        AttachedPrefabLayout.layout(
            getLevel().prefabs(), connections, PrefabMode::hasWorldPosition);
    Set<String> related = relatedToSelection(connections);
    hoveredName = hoveredInWorld().orElse(null);
    Map<String, PrefabInstance> byName = new HashMap<>();
    for (PrefabInstance instance : level.prefabs()) byName.put(instance.name(), instance);
    placeChips(byName, related);

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
    renderLinks(level, byName, connections);
    renderChips(byName, related);
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
        inactiveSuffix(instance),
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

  /**
   * Decides where the attached prefabs are drawn this frame. The prefabs attached to a world prefab
   * are listed right of it while they are in focus: while the world prefab or one of them is
   * selected or hovered, linked to the selection or a target of a link pick. Otherwise they are
   * collapsed into a badge next to the world prefab, or hidden, see {@link LogicDisplay}.
   *
   * <p>Lists and badges stay next to their world prefab, also at the edge of the screen. Only the
   * list of the selection moves, to stay in view, see {@link #keepInView}: the list of a selected
   * attached prefab or of a selected world prefab, which is then drawn at its start while it is out
   * of view, see {@link StandIn}, also if nothing is attached to it. The list hides the lists and
   * badges of other world prefabs it covers.
   *
   * @param byName instances of the level by name
   * @param related names of the instances linked to the selection
   */
  private void placeChips(Map<String, PrefabInstance> byName, Set<String> related) {
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
    AttachedPrefabLayout.Chip selectedChip = selectedName == null ? null : chips.get(selectedName);
    // The world prefab whose list stays in view: the one of the selection.
    String keptRoot = selectedChip == null ? selectedName : selectedChip.root();
    if (keptRoot != null) {
      // Also without attached prefabs, so the selected world prefab itself stays in view.
      listedRoots.add(keptRoot);
      shown.computeIfAbsent(keptRoot, ignored -> new ArrayList<>());
    }
    Map<String, ChipPlacement> result = new HashMap<>();
    Rectangle inView = null;
    StandIn shownStandIn = null;
    for (Map.Entry<String, List<AttachedPrefabLayout.Chip>> entry : shown.entrySet()) {
      String rootName = entry.getKey();
      List<AttachedPrefabLayout.Chip> group = entry.getValue();
      PrefabInstance root = byName.get(rootName);
      Optional<Point> rootAnchor =
          root == null ? Optional.empty() : worldLinkAnchor(normalizedForRender(root));
      if (rootAnchor.isEmpty()) continue;
      com.badlogic.gdx.math.Vector2 base = EditorOverlay.toScreen(rootAnchor.get());
      boolean listed = listedRoots.contains(rootName);
      float badgeWidth =
          EditorOverlay.badgeSize(
                  Math.min(group.size(), BADGE_MAX_DOTS), badgeCount(group.size()), BADGE_FONT_SIZE)
              .x;
      Map<String, Float> widths = new HashMap<>();
      for (AttachedPrefabLayout.Chip chip : group) {
        PrefabInstance instance = byName.get(chip.name());
        widths.put(chip.name(), listed ? rowWidth(chip, instance) : badgeWidth);
      }
      if (listed && rootName.equals(keptRoot)) {
        String label =
            rootName.equals(selectedName) && !isInView(base)
                ? rootName + look(normalizedForRender(root), related, null).nameSuffix()
                : null;
        if (label != null || !group.isEmpty()) inView = keepInView(base, group, widths, label);
        if (label != null) {
          shownStandIn = new StandIn(rootName, label, base.cpy(), standInArea(base, label));
        }
      }
      for (AttachedPrefabLayout.Chip chip : group) {
        com.badlogic.gdx.math.Vector2 marker =
            listed ? rowMarker(base, chip.depth(), chip.row()) : rowMarker(base, 0, 0);
        Rectangle hitBox =
            new Rectangle(
                marker.x - CHIP_DOT_RADIUS_PX - 3f,
                marker.y - CHIP_ROW_PX / 2f,
                widths.get(chip.name()) + 6f,
                CHIP_ROW_PX);
        result.put(
            chip.name(),
            new ChipPlacement(chip, marker, EditorOverlay.toWorld(marker), listed, hitBox));
      }
    }
    if (inView != null) {
      // The list of the selection covers the lists and badges of other world prefabs below it.
      Rectangle covering = inView;
      result
          .values()
          .removeIf(
              placement ->
                  !placement.chip().root().equals(keptRoot)
                      && placement.hitBox().overlaps(covering));
    }
    chipPlacements = result;
    standIn = shownStandIn;
  }

  /**
   * Returns the screen position of the dot of a row in a list of attached prefabs.
   *
   * @param base screen position of the world prefab, the list starts right of it
   * @param depth indentation of the row
   * @param row index of the row, from the top
   * @return center of the dot
   */
  private static com.badlogic.gdx.math.Vector2 rowMarker(
      com.badlogic.gdx.math.Vector2 base, int depth, int row) {
    return new com.badlogic.gdx.math.Vector2(
        base.x + CHIP_OFFSET_PX + CHIP_DOT_RADIUS_PX + depth * CHIP_INDENT_PX,
        base.y - row * CHIP_ROW_PX);
  }

  /**
   * Returns the width of a listed row on the screen, from the left edge of its dot to the end of
   * its label.
   *
   * @param chip the attached prefab
   * @param source its authored instance, or {@code null} if it is missing
   * @return width in pixels
   */
  private float rowWidth(AttachedPrefabLayout.Chip chip, PrefabInstance source) {
    String text = source == null ? chip.name() : rowText(chip, normalizedForRender(source));
    return CHIP_DOT_RADIUS_PX * 2 + 14f + EditorOverlay.textSize(text, CHIP_FONT_SIZE).x;
  }

  private static String inactiveSuffix(PrefabInstance instance) {
    return instance.active() ? "" : "  (inactive)";
  }

  /**
   * Returns the label of a listed row: the name of the attached prefab, whether it is inactive and,
   * while it is selected, its summary.
   *
   * @param chip the attached prefab
   * @param instance its normalized instance
   * @return label text
   */
  private String rowText(AttachedPrefabLayout.Chip chip, PrefabInstance instance) {
    String text = chip.name() + inactiveSuffix(instance);
    if (!instance.name().equals(selectedName)) return text;
    Optional<String> summary = PrefabRegistry.require(instance.type()).editorSummary(instance);
    return summary.isPresent() ? text + "  (" + summary.get() + ")" : text;
  }

  /**
   * Moves the list of the selection as a whole, as little as needed to keep it in view: when the
   * camera moves away from its world prefab, the list stays at the edge of the screen, clear of the
   * panels, see {@link LevelEditorUI#keepInView}.
   *
   * @param base screen position of the world prefab the list starts at, moved in place
   * @param group the listed attached prefabs, empty if only the stand-in is kept in view
   * @param widths row widths by name, see {@link #rowWidth}
   * @param standInLabel name of the world prefab drawn at the start of the list, see {@link
   *     StandIn}, or {@code null} if it is not drawn there; required for an empty group
   * @return screen area of the list, after moving it
   */
  private Rectangle keepInView(
      com.badlogic.gdx.math.Vector2 base,
      List<AttachedPrefabLayout.Chip> group,
      Map<String, Float> widths,
      String standInLabel) {
    float width = 0f;
    int rows = 1;
    for (AttachedPrefabLayout.Chip chip : group) {
      width = Math.max(width, chip.depth() * CHIP_INDENT_PX + widths.get(chip.name()));
      rows = Math.max(rows, chip.row() + 1);
    }
    Rectangle list =
        new Rectangle(
            base.x + CHIP_OFFSET_PX,
            base.y + CHIP_ROW_PX / 2f - rows * CHIP_ROW_PX,
            width,
            rows * CHIP_ROW_PX);
    if (group.isEmpty()) list = standInArea(base, standInLabel);
    else if (standInLabel != null) list.merge(standInArea(base, standInLabel));
    float x = list.x;
    float y = list.y;
    LevelEditorUI ui = LevelEditorSystem.ui().orElse(null);
    if (ui != null) ui.keepInView(list, CHIP_VIEW_MARGIN_PX);
    base.add(list.x - x, list.y - y);
    return list;
  }

  /* Whether a screen position is in view: on the screen and not covered by a panel. */
  private boolean isInView(com.badlogic.gdx.math.Vector2 screen) {
    LevelEditorUI ui = LevelEditorSystem.ui().orElse(null);
    if (ui == null) return true;
    Rectangle point = new Rectangle(screen.x, screen.y, 0f, 0f);
    ui.keepInView(point, 0f);
    return point.x == screen.x && point.y == screen.y;
  }

  /* Screen area of the dot and the name of a stand-in at a position, see StandIn. */
  private static Rectangle standInArea(com.badlogic.gdx.math.Vector2 marker, String label) {
    com.badlogic.gdx.math.Vector2 size =
        EditorOverlay.badgeSize(0, label, PrefabOverlayFeedback.NAME_FONT_SIZE);
    Rectangle dot =
        new Rectangle(
            marker.x - CHIP_DOT_RADIUS_PX,
            marker.y - CHIP_DOT_RADIUS_PX,
            CHIP_DOT_RADIUS_PX * 2f,
            CHIP_DOT_RADIUS_PX * 2f);
    return dot.merge(
        new Rectangle(marker.x - size.x / 2f, marker.y + STAND_IN_LABEL_OFFSET_PX, size.x, size.y));
  }

  // Draws the instances without a world position next to the prefabs they are attached to.
  private void renderChips(Map<String, PrefabInstance> byName, Set<String> related) {
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
      String label = rowText(placement.chip(), instance);
      EditorOverlay.Look labelLook =
          look.namePriority() > EditorOverlay.PRIORITY_NORMAL
              ? look.nameLook()
              : instance.active() ? EditorTheme.LABEL_FOCUSED : EditorTheme.LABEL_INACTIVE;
      if (look.dimmed()) labelLook = EditorTheme.LABEL_INACTIVE;
      // Pinned: the rows are clicked where placeChips put them, other labels move out of the way.
      overlay.pinnedText(
          label,
          placement.marker(),
          EditorOverlay.Anchor.RIGHT,
          CHIP_DOT_RADIUS_PX + 4f,
          CHIP_FONT_SIZE,
          labelLook);
    }
    if (standIn != null) {
      overlay.dot(standIn.marker(), CHIP_DOT_RADIUS_PX, EditorTheme.SELECTION, Color.WHITE);
      overlay.pinnedText(
          standIn.label(),
          standIn.marker(),
          EditorOverlay.Anchor.ABOVE,
          STAND_IN_LABEL_OFFSET_PX,
          PrefabOverlayFeedback.NAME_FONT_SIZE,
          EditorTheme.LABEL_SELECTED);
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
      String count = badgeCount(ordered.size());
      boolean target =
          ordered.stream().anyMatch(placement -> pickTargets.contains(placement.chip().name()));
      com.badlogic.gdx.math.Vector2 marker = ordered.getFirst().marker();
      overlay.badge(
          dots,
          count,
          new com.badlogic.gdx.math.Vector2(marker.x - CHIP_DOT_RADIUS_PX, marker.y),
          EditorOverlay.Anchor.RIGHT,
          0f,
          BADGE_FONT_SIZE,
          target
              ? new EditorOverlay.Look(EditorTheme.TARGET, new Color(0.04f, 0.24f, 0.1f, 0.92f))
              : EditorTheme.LABEL_NORMAL);
    }
  }

  /* Text after the dots of the badge of a collapsed list, for the prefabs without a dot. */
  private static String badgeCount(int prefabs) {
    return prefabs > BADGE_MAX_DOTS ? "+" + (prefabs - BADGE_MAX_DOTS) : "";
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

  /**
   * Returns whether an instance has a position in the level, e.g. a point or a region, rather than
   * being attached to the prefabs it is linked to.
   *
   * @param instance the instance
   * @return true if it has a world position; false for invalid instances
   */
  static boolean hasWorldPosition(PrefabInstance instance) {
    try {
      return !worldAnchors(instance).isEmpty();
    } catch (IllegalArgumentException invalidInstance) {
      return false;
    }
  }

  /**
   * Draws the links of the selected instance with their port names, faint links of the hovered
   * instance, and all other links faintly if {@link LinkDisplay#ALL} is chosen.
   *
   * @param level edited level
   * @param byName instances of the level by name
   * @param connections links of the level
   */
  private void renderLinks(
      DungeonLevel level,
      Map<String, PrefabInstance> byName,
      List<PrefabWiring.Connection> connections) {
    List<PrefabInstance> instances = level.prefabs();
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
      boolean broken =
          PrefabConnectionsTab.problemOf(
                  problems, receiver, connection.input().key(), connection.link())
              .filter(problem -> problem.severity() == PrefabProblem.Severity.ERROR)
              .isPresent();
      if (broken) color.set(EditorTheme.ERROR);
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
   * Where an attached prefab was drawn in the last frame.
   *
   * @param chip its place in the list of its world prefab
   * @param marker screen position of its marker, or of the badge of its collapsed list
   * @param world world position of the marker, where its links start and end
   * @param listed whether it is listed with its name, not collapsed into a badge
   * @param hitBox screen area of its row or badge; a row selects it when clicked
   */
  private record ChipPlacement(
      AttachedPrefabLayout.Chip chip,
      com.badlogic.gdx.math.Vector2 marker,
      Point world,
      boolean listed,
      Rectangle hitBox) {}

  /**
   * A selected world prefab drawn at the start of its list of attached prefabs, if it has any,
   * while the prefab itself is out of view, so it stays on the screen: its dot with its name above.
   *
   * @param name instance name
   * @param label shown name
   * @param marker screen position of its dot
   * @param area screen area of its dot and name; clicking it keeps the prefab selected
   */
  private record StandIn(
      String name, String label, com.badlogic.gdx.math.Vector2 marker, Rectangle area) {}

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
    problems = LevelProblems.current();
    List<PrefabWiring.Connection> connections = PrefabWiring.connections(getLevel().prefabs());
    chips =
        AttachedPrefabLayout.layout(
            getLevel().prefabs(), connections, PrefabMode::hasWorldPosition);
    hierarchy = hierarchy(getLevel().prefabs(), connections, getLevel().prefabGroups(), loadOrder);
    outliner.setItems(listItems(), selectedName, loadOrder);
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
              nameRow.add(groupSelect(instance)).height(32f).minWidth(0f).maxWidth(120f);
              nameRow.add(name).growX().minWidth(0f).height(32f).padLeft(6f);
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
      EditorTheme.onChange(
          button,
          () -> {
            if (detailsTab == tab) return;
            detailsTab = tab;
            requestRebuild();
          });
      tabs.add(button);
    }
    return tabs;
  }

  /**
   * Choice of the group select box of the inspector.
   *
   * @param group group name, or {@code null} for none
   * @param create whether the choice adds a new group with the selected prefab
   */
  private record GroupChoice(String group, boolean create) {}

  private static String groupChoiceText(GroupChoice choice) {
    if (choice.create()) return "New group";
    return choice.group() == null ? "No group" : choice.group();
  }

  /* Number of groups containing a group, 0 for null. */
  private int groupDepth(String group) {
    return Math.max(0, getLevel().prefabGroups().path(group).size() - 1);
  }

  /**
   * Creates the select box of the group the selected prefab is listed in, marked by a folder. It
   * lists all groups and an entry that adds a new group with the prefab, inside the group it is
   * listed in. A prefab used by another prefab is listed with that prefab unless it is in a group
   * of its own, so it has no choice without a group.
   *
   * @param instance selected instance
   * @return the select box
   */
  private Actor groupSelect(PrefabInstance instance) {
    String name = instance.name();
    String current = hierarchy.groupOf(name).orElse(null);
    List<GroupChoice> choices = new ArrayList<>();
    if (current == null || hierarchy.hostOf(name).isEmpty()) {
      choices.add(new GroupChoice(null, false));
    }
    for (String group : getLevel().prefabGroups().names()) {
      choices.add(new GroupChoice(group, false));
    }
    choices.add(new GroupChoice(null, true));
    // The list shows how the groups are nested, the box only the chosen one.
    SelectBox<GroupChoice> box =
        EditorTheme.selectBox(
            choice -> "    ".repeat(groupDepth(choice.group())) + groupChoiceText(choice),
            PrefabMode::groupChoiceText);
    SelectBox.SelectBoxStyle style = EditorTheme.selectBoxStyle(13);
    // Room for the folder in front of the group name.
    for (Drawable background :
        List.of(style.background, style.backgroundOver, style.backgroundOpen)) {
      background.setLeftWidth(26f);
    }
    box.setStyle(style);
    box.setItems(choices.toArray(new GroupChoice[0]));
    box.setSelected(new GroupChoice(current, false));
    EditorTheme.onChange(
        box,
        () -> {
          GroupChoice choice = box.getSelected();
          if (choice.create()) {
            String group = createGroupWithSelection();
            if (outliner != null) outliner.renameLater(group);
          } else {
            movePrefab(name, choice.group(), null, false);
            if (outliner != null) outliner.revealLater();
          }
        });
    Image folder = EditorTheme.iconImage("folder", EditorTheme.FOLDER);
    folder.setTouchable(Touchable.disabled);
    Table overlay = new Table();
    overlay.setTouchable(Touchable.disabled);
    overlay.add(folder).size(14f).padLeft(8f).left().expandX();
    return new Stack(box, overlay);
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
    EditorTheme.onChange(active, () -> setActive(instance.name(), !instance.active()));
    EditorTheme.tooltip(
        active,
        instance.active()
            ? "Spawned when the level starts. Click to make it wait for an Activate action."
            : "Waits for an Activate action. Click to spawn it when the level starts.");
    row.add(active).height(26f).padRight(6f);
    ImageButton frame =
        EditorTheme.iconButton(
            EditorTheme.icon("frame"), EditorTheme.Variant.GHOST, "Center the camera on it (F)");
    EditorTheme.onChange(frame, () -> focus(instance.name()));
    row.add(frame).size(26f);
    ImageButton duplicate =
        EditorTheme.iconButton(
            EditorTheme.icon("copy"), EditorTheme.Variant.GHOST, "Duplicate (Ctrl+D)");
    EditorTheme.onChange(duplicate, () -> duplicateSelected());
    row.add(duplicate).size(26f);
    ImageButton delete =
        EditorTheme.iconButton(
            EditorTheme.icon("trash", EditorTheme.ERROR),
            EditorTheme.Variant.GHOST,
            "Delete, along with all links to it");
    EditorTheme.onChange(delete, () -> delete(instance.name()));
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
      box.add(EditorTheme.iconImage(icon, color)).size(14f).top().padRight(7f).padTop(1f);
    }
    Label label = EditorTheme.label(text, 13, color);
    label.setWrap(true);
    box.add(label).growX().minWidth(0f);
    return box;
  }

  private void addProblemNotice(PrefabInstance instance) {
    long errors = 0;
    long warnings = 0;
    for (PrefabProblem problem : problemsOf(instance.name())) {
      if (!problem.isLinkProblem()) {
        secondaryContent
            .add(
                notice(
                    problem.message(),
                    EditorTheme.severityColor(problem.severity()),
                    EditorTheme.severityIcon(problem.severity())))
            .growX()
            .padTop(SETTINGS_PAD)
            .row();
      } else if (problem.severity() == PrefabProblem.Severity.ERROR) {
        errors++;
      } else {
        warnings++;
      }
    }
    if (errors + warnings == 0) return;
    PrefabProblem.Severity worst =
        errors > 0 ? PrefabProblem.Severity.ERROR : PrefabProblem.Severity.WARNING;
    secondaryContent
        .add(
            notice(
                (errors + warnings)
                    + (errors + warnings == 1 ? " link needs" : " links need")
                    + " attention, see Links.",
                EditorTheme.severityColor(worst),
                EditorTheme.severityIcon(worst)))
        .growX()
        .padTop(SETTINGS_PAD)
        .row();
  }

  // The problems of an instance itself and of the links it stores or is the source of.
  private List<PrefabProblem> problemsOf(String instance) {
    List<PrefabProblem> result = new ArrayList<>();
    for (PrefabProblem problem : problems) {
      if (problem.instance().equals(instance)
          || (problem.isLinkProblem() && problem.link().source().equals(instance))) {
        result.add(problem);
      }
    }
    return result;
  }

  private void addLinkedProperty(
      PrefabInstance instance, PrefabProperty<?> property, ArtifactSlot<?> slot) {
    Table setting = new Table();
    setting.defaults().growX().minWidth(0f);
    Table title = new Table();
    title.add(EditorTheme.label(property.displayName(), 14, EditorTheme.TEXT_MUTED)).left().growX();
    property.description().ifPresent(text -> title.add(EditorTheme.infoButton(text)).padLeft(6f));
    setting.add(title).row();
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
      EditorTheme.onChange(go, () -> focus(link.source()));
      row.add(go).size(24f);
      ImageButton unlink =
          EditorTheme.iconButton(
              EditorTheme.icon("close", EditorTheme.ERROR),
              EditorTheme.Variant.GHOST,
              "Remove the link, the manual value below is used again");
      EditorTheme.onChange(unlink, () -> removeLink(instance.name(), slot.key(), link));
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
    EditorTheme.onChange(button, () -> openLinkPicker(end, button));
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
      // Unknown placeholders are listed with the problems of the prefab.
      String hint =
          aliases.isEmpty()
              ? "Link '" + slot.displayName() + "' artifacts in Links to use {alias} placeholders."
              : "Placeholders: "
                  + String.join(" ", aliases.stream().map(alias -> "{" + alias + "}").toList());
      Label hintLabel = EditorTheme.label(hint, 12, EditorTheme.TEXT_MUTED);
      hintLabel.setWrap(true);
      secondaryContent.add(hintLabel).growX().padLeft(5f).row();
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
        addSetting(
            new StringSetting(
                p.displayName(), () -> access.get(p), value -> access.set(p, value, true), true),
            p);
      }
      case INTEGER -> {
        PrefabProperty<Integer> p = cast(property);
        int min = p.minimum().orElse(Integer.MIN_VALUE).intValue();
        int max = p.maximum().orElse(Integer.MAX_VALUE).intValue();
        addSetting(
            new IntegerSetting(
                p.displayName(),
                min,
                max,
                () -> access.get(p),
                value -> access.set(p, value, true)),
            p);
      }
      case FLOAT -> {
        PrefabProperty<Float> p = cast(property);
        float min = p.minimum().orElse(-Float.MAX_VALUE).floatValue();
        float max = p.maximum().orElse(Float.MAX_VALUE).floatValue();
        addSetting(
            new FloatSetting(
                p.displayName(),
                min,
                max,
                () -> access.get(p),
                value -> access.set(p, value, true)),
            p);
      }
      case NUMBER_SLIDER -> {
        PrefabProperty<Float> p = cast(property);
        float min = p.minimum().orElse(0f).floatValue();
        float max = p.maximum().orElse(1f).floatValue();
        float step = p.step().orElse(0f).floatValue();
        addSetting(
            new NumberSliderSetting(
                p.displayName(),
                min,
                max,
                step,
                () -> access.get(p),
                value -> access.set(p, value, false)),
            p);
      }
      case BOOLEAN -> {
        PrefabProperty<Boolean> p = cast(property);
        addSetting(
            new BooleanSetting(
                p.displayName(), () -> access.get(p), value -> access.set(p, value, true)),
            p);
      }
      case ENUM -> {
        PrefabProperty<String> p = cast(property);
        List<String> choices = new ArrayList<>(p.choices());
        // Open selections accept values that are currently not offered, which stay selectable.
        String current = access.get(p);
        if (!choices.contains(current)) choices.addFirst(current);
        String[] values = choices.toArray(String[]::new);
        addSetting(
            new SelectSetting<>(
                p.displayName(),
                values,
                () -> access.get(p),
                value -> access.set(p, value, true),
                value -> value,
                true),
            p);
      }
      case DECO -> {
        PrefabProperty<String> p = cast(property);
        boolean optional = p.choices().contains(PrefabProperty.NO_DECORATION);
        List<Deco> choices =
            p.choices().stream()
                .filter(value -> !value.equals(PrefabProperty.NO_DECORATION))
                .map(Deco::valueOf)
                .toList();
        addSetting(
            new DecoSetting(
                p.displayName(),
                choices,
                optional,
                () -> decoOf(access.get(p)),
                deco ->
                    access.set(p, deco == null ? PrefabProperty.NO_DECORATION : deco.name(), true)),
            p);
      }
      case POINT -> {
        PrefabProperty<Point> p = cast(property);
        addSetting(
            new PointSetting(
                p.displayName(),
                () -> access.get(p),
                value -> access.set(p, value, true),
                callback ->
                    armPointAssignment(
                        new PendingPointAssignment(callback, p.editorFeedbackOffset())),
                true),
            p);
      }
      case REGION -> {
        PrefabProperty<Region> p = cast(property);
        addSetting(
            new RegionSetting(
                p.displayName(),
                () -> access.get(p),
                value -> access.set(p, value, true),
                callback ->
                    armPointAssignment(new PendingPointAssignment(callback, new Point(0f, 0f)))),
            p);
      }
      case VECTOR2 -> {
        PrefabProperty<Vector2> p = cast(property);
        addSetting(
            new Vector2Setting(
                p.displayName(), () -> access.get(p), value -> access.set(p, value, true)),
            p);
      }
      case COLOR -> {
        PrefabProperty<Color> p = cast(property);
        addSetting(
            new ColorSetting(
                p.displayName(),
                () -> access.get(p),
                value -> access.set(p, value, true),
                message ->
                    LevelEditorSystem.showFeedback(p.displayName() + ": " + message, Color.YELLOW)),
            p);
      }
      case SHADER_PARAMETERS -> {
        PrefabProperty<ShaderParameters> p = cast(property);
        PrefabShader<?>[] shaders = p.shaderChoices().toArray(PrefabShader<?>[]::new);
        addSetting(
            new SelectSetting<>(
                p.displayName(),
                shaders,
                () -> access.get(p).shader(),
                shader -> {
                  // Switching shaders starts from the new shader's defaults.
                  if (access.get(p).shader() != shader) access.set(p, shader.defaults(), true);
                },
                PrefabShader::displayName,
                true),
            p);
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

  /* Adds the control of a property, with the description next to its name, see describedAs. */
  private void addSetting(EditorSetting setting, PrefabProperty<?> property) {
    property.description().ifPresent(setting::describe);
    secondaryContent.add(setting).growX().padTop(SETTINGS_PAD).row();
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
                    false,
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
    Optional<PrefabProblem> problem =
        PrefabConnectionsTab.problemOf(LevelProblems.current(), receiverName, input.key(), link);
    if (problem.isPresent()) {
      LevelEditorSystem.showFeedback(
          "Linked, but " + problem.get().message() + ".", feedbackColor(problem.get()));
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

  /**
   * Selects a prefab and centers the camera on it, e.g. to fix one of its problems.
   *
   * @param name name of the prefab instance
   * @param links whether to show the links of the prefab rather than its properties
   */
  public void reveal(String name, boolean links) {
    if (library != null) toggleLibrary();
    detailsTab = links ? DetailsTab.CONNECTIONS : DetailsTab.PROPERTIES;
    focus(name);
  }

  private void focus(String name) {
    Optional<PrefabInstance> instance = instanceNamed(name);
    if (instance.isEmpty()) {
      LevelEditorSystem.showFeedback("Prefab '" + name + "' does not exist.", Color.YELLOW);
      return;
    }
    cancelAnchorDrag();
    clearPendingPointAssignment();
    selectedName = name;
    focusPoint(instance.get()).ifPresent(LevelEditorSystem::focusCamera);
    requestRebuild();
  }

  /**
   * Returns where the camera is centered to show an instance: its link anchor, for an attached
   * prefab its row next to its world prefab, also while its list is kept in view elsewhere.
   *
   * @param instance the instance
   * @return camera position, if the instance has a world position or is attached to a prefab
   */
  private Optional<Point> focusPoint(PrefabInstance instance) {
    AttachedPrefabLayout.Chip chip = chips.get(instance.name());
    if (chip == null) return linkAnchor(instance);
    return getLevel().prefabs().stream()
        .filter(root -> root.name().equals(chip.root()))
        .findFirst()
        .flatMap(root -> worldLinkAnchor(normalizedForRender(root)))
        .map(
            anchor ->
                EditorOverlay.toWorld(
                    rowMarker(EditorOverlay.toScreen(anchor), chip.depth(), chip.row())));
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
    EventPlayers players = EventPlayers.of(instances);
    for (PrefabInstance candidate : instances) {
      if (onlyInstance != null && !candidate.name().equals(onlyInstance)) continue;
      Prefab prefab = PrefabRegistry.require(candidate.type());
      boolean candidateInWorld = hasWorldPosition(candidate);
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
        String noPlayer = missingPlayer(players.of(link), receiver, input);
        String detail = null;
        if (linked) detail = "already linked";
        else if (cycle) detail = "would make the value depend on itself";
        else if (heldBy.isPresent()) detail = "held by " + heldBy.get();
        else if (noPlayer != null) {
          detail = noPlayer;
        } else if (input instanceof ArtifactSlot<?> slot
            && !slot.multiple()
            && !receiver.links(input).isEmpty()) {
          detail = "replaces the current link";
        }
        String label = PrefabConnectionsTab.portTitle(candidate, port);
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
    EventPlayers players = EventPlayers.of(instances);
    for (PrefabCatalog.Category category : PrefabCatalog.typeCategories()) {
      for (Prefab prefab : category.prefabs()) {
        PrefabInstance candidate = prefab.newInstance(prefab.type());
        boolean inWorld = hasWorldPosition(candidate);
        for (PrefabPort port : prefab.ports(candidate)) {
          if (port instanceof PrefabAction action && action.isLifecycle()) continue;
          PrefabPort output = endIsInput ? port : end.port();
          PrefabPort input = endIsInput ? end.port() : port;
          if (port.kind().isInput() == endIsInput || !PrefabWiring.compatible(output, input)) {
            continue;
          }
          EventPlayers.Presence presence = playersOfNewLink(players, end, output);
          PrefabInstance receiver =
              endIsInput ? owner.get() : forEventWithoutPlayer(candidate, presence, input);
          String noPlayer = missingPlayer(presence, receiver, input);
          boolean held =
              input instanceof ArtifactSlot<?> slot && slot.holder() && heldBy.isPresent();
          String detail = null;
          if (held) detail = "held by " + heldBy.get();
          else if (endIsInput
              && input instanceof ArtifactSlot<?> slot
              && !slot.multiple()
              && !receiver.links(input).isEmpty()) {
            detail = "replaces the current link";
          } else if (noPlayer != null) {
            detail = noPlayer;
          }
          options.add(
              new PortPickerPopup.Option(
                  prefab.type(),
                  prefab.displayName() + "  -  " + category.name(),
                  PrefabConnectionsTab.portTitle(candidate, port),
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
    PrefabLink link =
        endIsInput
            ? new PrefabLink(name, port.key())
            : new PrefabLink(owner.get().name(), end.port().key());
    // The new instance joins the group of the instance it is linked to.
    String group = hierarchy.groupOf(owner.get().name()).orElse(null);
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
        receiver = withLink(owner.get(), input, link);
        forEveryone = false;
      } else {
        EventPlayers.Presence presence =
            EventPlayers.of(getLevel().prefabs()).of(owner.get().name(), end.port().key());
        PrefabInstance adjusted = forEventWithoutPlayer(created, presence, input);
        forEveryone = !adjusted.equals(created);
        added = withLink(adjusted, input, link);
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
              getLevel().prefabGroups().assign(name, group);
              selectedName = name;
            });
    if (!applied) return;
    detailsTab = DetailsTab.PROPERTIES;
    Optional<PrefabProblem> problem =
        PrefabConnectionsTab.problemOf(LevelProblems.current(), receiver.name(), input.key(), link);
    if (problem.isPresent()) {
      LevelEditorSystem.showFeedback(
          "Added " + name + ", but " + problem.get().message() + ".", feedbackColor(problem.get()));
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
   * Makes a new instance act on every player if it is linked to an event that never has a player,
   * so the link is no problem, see {@link PrefabAction#forEveryone}.
   *
   * @param created new instance receiving the link
   * @param presence whether the event of the link carries a player
   * @param input input port of the new instance
   * @return the instance acting on every player, or {@code created} if nothing changes
   */
  private static PrefabInstance forEventWithoutPlayer(
      PrefabInstance created, EventPlayers.Presence presence, PrefabPort input) {
    if (!(input instanceof PrefabAction action)
        || presence != EventPlayers.Presence.NEVER
        || !action.needsPlayer(created)) {
      return created;
    }
    return action.forEveryone(created).orElse(created);
  }

  /**
   * Returns whether the event of a link to a new prefab carries a player.
   *
   * @param players players of the events of the level
   * @param end port of the instance the new prefab is linked to
   * @param output output port of the link
   * @return the presence of the player; {@link EventPlayers.Presence#NONE} for artifacts
   */
  private static EventPlayers.Presence playersOfNewLink(
      EventPlayers players, PrefabConnectionsTab.LinkEnd end, PrefabPort output) {
    if (!(output instanceof PrefabEvent<?> event)) return EventPlayers.Presence.NONE;
    if (!end.port().kind().isInput()) return players.of(end.instance(), event.key());
    // A new prefab has no links yet, its events have a player if they were declared with one.
    return event.carriesPlayer() ? EventPlayers.Presence.ALWAYS : EventPlayers.Presence.NEVER;
  }

  /**
   * Describes why an action that needs a player would run for every player when linked to an event,
   * following the players through the level, see {@link EventPlayers}.
   *
   * @param presence whether the event of the link carries a player
   * @param receiver instance receiving the link
   * @param input input port of the link
   * @return a short note, or {@code null} if the action needs no player or always gets one
   */
  private static String missingPlayer(
      EventPlayers.Presence presence, PrefabInstance receiver, PrefabPort input) {
    if (!(input instanceof PrefabAction action) || !action.needsPlayer(receiver)) return null;
    return switch (presence) {
      case NEVER -> "runs for every player";
      case SOMETIMES -> "may run for every player";
      default -> null;
    };
  }

  private static Color feedbackColor(PrefabProblem problem) {
    return problem.severity() == PrefabProblem.Severity.ERROR ? Color.RED : Color.YELLOW;
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
    LevelEditorUI ui = LevelEditorSystem.ui().orElse(null);
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
    LevelEditorUI ui = LevelEditorSystem.ui().orElse(null);
    if (ui == null || ui.getStage() == null) return;
    com.badlogic.gdx.math.Vector2 position = cursorOnStage(ui.getStage());
    showPicker(end, target.get().name(), ui, position.x + 12f, position.y);
  }

  private static com.badlogic.gdx.math.Vector2 cursorOnStage(Stage stage) {
    return stage.screenToStageCoordinates(
        new com.badlogic.gdx.math.Vector2(GameInput.pointer().x(), GameInput.pointer().y()));
  }

  private void toggleLibrary() {
    EditorTheme.hideTooltips();
    library = library == null ? new PrefabLibrary(new PaletteListener(), this::isAttached) : null;
    paletteDrag = null;
    showListOrLibrary();
    if (library != null) library.focusSearch();
  }

  private boolean isCursorOverUI() {
    LevelEditorUI ui = LevelEditorSystem.ui().orElse(null);
    return ui != null && ui.isCursorOverUI();
  }

  /**
   * Adds a new instance of a prefab without a world position and links it to another instance.
   *
   * @param prefab prefab to add, see {@link #isAttached(Prefab)}
   * @param partner instance to link the new one to, or {@code null} to add it unlinked
   */
  private void addAttached(Prefab prefab, String partner) {
    // The new instance joins the group of its partner.
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
    LevelEditorUI ui = LevelEditorSystem.ui().orElse(null);
    if (ui == null || ui.getStage() == null) return;
    com.badlogic.gdx.math.Vector2 position = cursorOnStage(ui.getStage());
    showOptions(
        "Link '" + name + "' and '" + partner + "':",
        options,
        false,
        List.of(),
        null,
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
    // New instances join the group of the selected instance.
    String group = groupForNew();
    applyChange(
        () -> {
          getLevel().addPrefab(added);
          getLevel().prefabGroups().assign(name, group);
          selectedName = name;
        });
    return containsName(name) ? name : null;
  }

  private String newInstanceName(Prefab prefab) {
    String base = prefab.type();
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
              String group = getLevel().prefabGroups().groupOf(source.name()).orElse(null);
              applyChange(
                  () -> {
                    List<PrefabInstance> prefabs = getLevel().prefabs();
                    int index = prefabs.indexOf(source);
                    getLevel().addPrefab(source.withName(name));
                    // The copy is created right behind its source.
                    PrefabInstance copy = prefabs.removeLast();
                    prefabs.add(index + 1, copy);
                    getLevel().prefabGroups().assign(name, group);
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
          getLevel().prefabGroups().removeInstance(name);
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
            getLevel().renamePrefabs(Map.of(current.name(), name));
            selectedName = name;
            if (shaderEditTarget != null
                && shaderEditTarget.instanceName().equals(current.name())) {
              shaderEditTarget = new ShaderEditTarget(name, shaderEditTarget.propertyKey());
            }
          });
    }
  }

  private void toggleListOrder() {
    loadOrder = !loadOrder;
    rebuildDetails(false);
  }

  /**
   * Sorts the prefabs of the level into the tree of the prefab list, see {@link PrefabHierarchy}.
   *
   * @param connections links between the prefabs
   * @return the tree
   */
  static PrefabHierarchy hierarchy(
      List<PrefabInstance> prefabs,
      List<PrefabWiring.Connection> connections,
      PrefabGroups groups,
      boolean loadOrder) {
    AttachedPrefabLayout.Hosts hosts =
        AttachedPrefabLayout.hosts(prefabs, connections, PrefabMode::hasWorldPosition);
    Comparator<PrefabInstance> order;
    if (loadOrder) {
      Map<String, Integer> indices = new HashMap<>();
      for (PrefabInstance instance : prefabs) indices.put(instance.name(), indices.size());
      order = Comparator.comparingInt(instance -> indices.get(instance.name()));
    } else {
      order = Comparator.comparing(PrefabInstance::name, PrefabNames.NATURAL_ORDER);
    }
    return PrefabHierarchy.build(prefabs, hosts, groups, order);
  }

  private List<PrefabOutliner.Item> listItems() {
    Map<String, PrefabProblem.Severity> worst = new HashMap<>();
    for (PrefabProblem problem : problems) {
      worst.merge(problem.instance(), problem.severity(), (a, b) -> a.compareTo(b) >= 0 ? a : b);
    }
    Function<PrefabInstance, PrefabOutliner.Status> status =
        instance -> {
          PrefabProblem.Severity severity = worst.get(instance.name());
          int problem = severity == null ? 0 : severity == PrefabProblem.Severity.ERROR ? 2 : 1;
          return new PrefabOutliner.Status(!instance.active(), isUnplaced(instance), problem);
        };
    List<PrefabOutliner.Item> items = new ArrayList<>();
    for (String group : hierarchy.groups()) items.add(groupItem(group, status));
    items.addAll(listItems(hierarchy.ungrouped(), status));
    return items;
  }

  /* A group: the groups inside of it, followed by its prefabs. */
  private PrefabOutliner.Group groupItem(
      String group, Function<PrefabInstance, PrefabOutliner.Status> status) {
    List<PrefabOutliner.Item> children = new ArrayList<>();
    for (String inside : hierarchy.subgroups(group)) children.add(groupItem(inside, status));
    children.addAll(listItems(hierarchy.roots(group), status));
    return new PrefabOutliner.Group(PrefabOutliner.groupId(group), group, children);
  }

  /* Instances without a world position that are not attached to a prefab in the world. */
  private boolean isUnplaced(PrefabInstance instance) {
    return !chips.containsKey(instance.name()) && !hasWorldPosition(instance);
  }

  private List<PrefabOutliner.Item> listItems(
      List<PrefabHierarchy.Node> nodes, Function<PrefabInstance, PrefabOutliner.Status> status) {
    List<PrefabOutliner.Item> items = new ArrayList<>();
    for (PrefabHierarchy.Node node : nodes) {
      PrefabInstance instance = node.instance();
      String name = instance.name();
      List<PrefabOutliner.Item> children = new ArrayList<>(listItems(node.children(), status));
      for (PrefabInstance used : node.references()) {
        children.add(
            new PrefabOutliner.Reference(
                "reference:" + name + ">" + used.name(),
                used.name(),
                () -> PrefabIcons.create(used.type(), 16f),
                !used.active()));
      }
      items.add(
          new PrefabOutliner.Entry(
              "entry:" + name,
              name,
              PrefabRegistry.find(instance.type()).map(Prefab::displayName).orElse(instance.type()),
              getLevel().prefabGroups().path(hierarchy.groupOf(name).orElse(null)),
              () -> PrefabIcons.create(instance.type(), 16f),
              status.apply(instance),
              children));
    }
    return items;
  }

  /**
   * Returns the group new prefabs join: the group of the selected prefab.
   *
   * @return group name, or {@code null} outside of all groups
   */
  private String groupForNew() {
    return selectedName == null ? null : hierarchy.groupOf(selectedName).orElse(null);
  }

  /**
   * Adds an empty group named "group", with a number if the name is taken.
   *
   * @param parent group to add it into, or {@code null} to add it outside of all groups
   * @return name of the new group, or {@code null} if it could not be added
   */
  private String createGroup(String parent) {
    if (parent != null && !getLevel().prefabGroups().contains(parent)) return null;
    cancelAnchorDrag();
    clearPendingPointAssignment();
    String name = "group";
    if (hasGroup(name)) name = PrefabNames.nextName(name, this::hasGroup);
    String group = name;
    return applyChange(() -> getLevel().prefabGroups().add(group, parent)) ? group : null;
  }

  /* Whether a group name is taken, ignoring the case to keep groups apart in the list. */
  private boolean hasGroup(String name) {
    return getLevel().prefabGroups().names().stream()
        .anyMatch(group -> group.equalsIgnoreCase(name));
  }

  /**
   * Adds a group with the selected prefab, along with the prefabs nested below it. The group is
   * added inside the group the prefab is listed in, e.g. for a puzzle in a room.
   *
   * @return name of the new group, or {@code null} if it could not be added
   */
  private String createGroupWithSelection() {
    String name = selectedName;
    String group = createGroup(groupForNew());
    if (group != null && name != null) movePrefab(name, group, null, false);
    return group;
  }

  /**
   * Renames a group. The prefabs keep their names.
   *
   * @param group current group name
   * @param newName requested name
   * @return whether the group was renamed
   */
  private boolean renameGroup(String group, String newName) {
    cancelAnchorDrag();
    clearPendingPointAssignment();
    boolean blank = newName == null || newName.isBlank();
    boolean taken = !blank && !newName.equalsIgnoreCase(group) && hasGroup(newName);
    if (blank || taken) {
      LevelEditorSystem.showFeedback(
          taken ? "There is a group named " + newName + " already." : "Name the group.",
          Color.YELLOW);
      requestRebuild();
      return false;
    }
    return applyChange(() -> getLevel().prefabGroups().rename(group, newName));
  }

  /**
   * Removes a group. Its prefabs and groups move to the group containing it, or outside of all
   * groups.
   *
   * @param group group name
   */
  private void removeGroup(String group) {
    cancelAnchorDrag();
    clearPendingPointAssignment();
    int members = hierarchy.members(group).size();
    if (!applyChange(() -> getLevel().prefabGroups().remove(group))) return;
    if (members > 0) {
      LevelEditorSystem.showFeedback(
          "Removed group "
              + group
              + ", kept its "
              + (members == 1 ? "prefab." : members + " prefabs."),
          Color.WHITE);
    }
  }

  /**
   * Moves a group, with everything inside of it, next to or into another group.
   *
   * @param group group name
   * @param target group it was dropped onto
   * @param placement where it goes relative to the target
   */
  private void moveGroup(String group, String target, PrefabOutliner.Placement placement) {
    PrefabGroups groups = getLevel().prefabGroups();
    if (!groups.contains(group) || !groups.contains(target)) return;
    boolean inside = placement == PrefabOutliner.Placement.INSIDE;
    String parent = inside ? target : groups.parentOf(target).orElse(null);
    if (parent != null && groups.isWithin(parent, group)) return;
    List<String> siblings = parent == null ? groups.roots() : groups.children(parent);
    int index =
        inside
            ? siblings.size()
            : siblings.indexOf(target) + (placement == PrefabOutliner.Placement.AFTER ? 1 : 0);
    cancelAnchorDrag();
    clearPendingPointAssignment();
    applyChange(() -> getLevel().prefabGroups().move(group, parent, index));
  }

  /**
   * Moves a prefab into a group, along with the prefabs nested below it, and optionally to another
   * place in the load order.
   *
   * @param name instance name
   * @param group group to move it into, or {@code null} for none
   * @param anchor prefab it is placed next to in load order, or {@code null} to keep its place
   * @param after whether it is placed behind the anchor instead of in front of it
   */
  private void movePrefab(String name, String group, String anchor, boolean after) {
    if (indexOf(name) < 0) return;
    PrefabGroups groups = getLevel().prefabGroups();
    PrefabGroups moved = groups.copy();
    if (!Objects.equals(hierarchy.groupOf(name).orElse(null), group)) {
      for (String nested : hierarchy.descendants(name)) moved.assign(nested, group);
    }
    moved.assign(name, group);
    List<PrefabInstance> order = new ArrayList<>(getLevel().prefabs());
    if (anchor != null && !anchor.equals(name) && indexOf(anchor) >= 0) {
      PrefabInstance instance = order.remove(indexOf(name));
      int target = order.indexOf(instanceNamed(anchor).orElseThrow());
      order.add(after ? target + 1 : target, instance);
    }
    boolean reordered = !order.equals(getLevel().prefabs());
    if (moved.equals(groups) && !reordered) return;
    cancelAnchorDrag();
    clearPendingPointAssignment();
    applyChange(
        () -> {
          getLevel().prefabGroups().set(moved);
          getLevel().prefabs().clear();
          getLevel().prefabs().addAll(order);
        });
    // Unchanged instances are not respawned by a sync, so respawn everything to apply the order.
    if (!reordered) return;
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
    if (standIn != null
        && standIn.name().equals(instance.name())
        && standIn.area().contains(EditorOverlay.toScreen(cursor))) {
      return 0f;
    }
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

  private static List<WorldAnchor> worldAnchors(PrefabInstance instance) {
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
    PrefabInstance source = instanceNamed(anchor.instanceName()).orElse(null);
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
            for (PrefabSide side : PrefabSide.localSides()) PrefabSpawner.spawn(getLevel(), side);
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
   * Applies an edit of the authored instances or their groups and respawns the changed instances. A
   * failed edit is rolled back and reported.
   *
   * @param mutation edit of the authored instances
   * @param rebuildDetails whether the details panel is rebuilt
   * @return whether the edit was applied
   */
  private boolean applyChange(Runnable mutation, boolean rebuildDetails) {
    List<PrefabInstance> previousPrefabs = new ArrayList<>(getLevel().prefabs());
    PrefabGroups previousGroups = getLevel().prefabGroups().copy();
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
      getLevel().prefabGroups().set(previousGroups);
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
          for (PrefabSide side : PrefabSide.localSides()) PrefabSpawner.sync(getLevel(), side);
        });
  }

  private void despawnAll() {
    for (PrefabSide side : PrefabSide.localSides()) PrefabSpawner.clear(getLevel(), side);
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
    public String createGroup(String parent) {
      return PrefabMode.this.createGroup(parent);
    }

    @Override
    public boolean renameGroup(String group, String newName) {
      return PrefabMode.this.renameGroup(group, newName);
    }

    @Override
    public void removeGroup(String group) {
      PrefabMode.this.removeGroup(group);
    }

    @Override
    public void moveGroup(String group, String target, PrefabOutliner.Placement placement) {
      PrefabMode.this.moveGroup(group, target, placement);
    }

    @Override
    public void movePrefab(String name, String group, String anchor, boolean after) {
      PrefabMode.this.movePrefab(name, group, anchor, after);
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
    if (!containsName(instanceName)) return;
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
