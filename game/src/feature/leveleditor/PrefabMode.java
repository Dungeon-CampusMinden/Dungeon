package feature.leveleditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import engine.Entity;
import engine.Game;
import engine.level.DungeonLevel;
import engine.systems.CameraSystem;
import engine.systems.input.InputManager;
import engine.utils.Point;
import engine.utils.Scene2dElementFactory;
import engine.utils.Vector2;
import feature.entities.deco.Deco;
import feature.leveleditor.ui.ActionSetting;
import feature.leveleditor.ui.BooleanSetting;
import feature.leveleditor.ui.ColorSetting;
import feature.leveleditor.ui.FloatSetting;
import feature.leveleditor.ui.IntegerSetting;
import feature.leveleditor.ui.LevelEditorUI;
import feature.leveleditor.ui.ModeDetailsPanel;
import feature.leveleditor.ui.NumberSliderSetting;
import feature.leveleditor.ui.PointSetting;
import feature.leveleditor.ui.PortPickerPopup;
import feature.leveleditor.ui.PrefabInstanceList;
import feature.leveleditor.ui.RegionSetting;
import feature.leveleditor.ui.SelectSetting;
import feature.leveleditor.ui.StringSetting;
import feature.leveleditor.ui.Vector2Setting;
import feature.prefabs.ArtifactOutput;
import feature.prefabs.ArtifactSlot;
import feature.prefabs.ImagePuzzleSettings;
import feature.prefabs.Placeholders;
import feature.prefabs.Prefab;
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
import java.util.Arrays;
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
import java.util.function.Consumer;
import java.util.function.Function;

/** Editor mode for authoring registered prefab instances in a level. */
public final class PrefabMode extends LevelEditorMode {

  private static final float PICK_DISTANCE = 1.4f;
  private static final float ANCHOR_HIT_DISTANCE = 0.38f;
  private static final float DRAG_START_DISTANCE = 0.14f;
  private static final float POINT_ASSIGNMENT_PREVIEW_RADIUS = 0.12f;
  private static final Color POINT_ASSIGNMENT_PREVIEW_COLOR = new Color(0.25f, 1f, 0.45f, 0.8f);
  private static final Color DRAG_ORIGINAL_COLOR = new Color(1f, 0.15f, 0.1f, 1f);
  private static final Color DRAG_PREVIEW_COLOR = new Color(0.25f, 1f, 0.45f, 0.9f);
  private static final int SETTINGS_PAD = 8;
  private static final Color INACTIVE_LABEL_COLOR = new Color(0.75f, 0.75f, 0.75f, 0.8f);
  private static final Color PICK_TARGET_COLOR = new Color(0.25f, 1f, 0.45f, 0.9f);
  private static final DebugDrawPrefabEditorFeedback.Style INACTIVE_STYLE =
      new DebugDrawPrefabEditorFeedback.Style(
          new Color(0.6f, 0.6f, 0.6f, 0.3f), new Color(0.8f, 0.8f, 0.8f, 0.4f), 0.09f);
  private static final float LINK_PULSE_SECONDS = 0.8f;
  private static final float LINK_LABEL_SPACING = 0.32f;

  private String selectedName;
  private Prefab selectedPrefab;
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
  private PrefabInstanceList instanceList;
  private Table listOrderTabs;
  private ListOrder listOrder = ListOrder.ALPHABETICAL;
  private SelectSetting<Prefab> prefabTypeSetting;
  private boolean rebuildPending;
  // While set, the secondary details show the parameters of one shader property instead.
  private ShaderEditTarget shaderEditTarget;
  private DetailsTab detailsTab = DetailsTab.PROPERTIES;
  private boolean showAllLinks;
  // While set, the next click on a prefab in the world links it to this port.
  private PrefabConnectionsTab.LinkEnd pendingLinkPick;
  // Instances with a port that fits the pending pick, computed when the pick starts.
  private Set<String> pickTargets = Set.of();
  private PortPickerPopup openPicker;
  private List<PrefabWiring.Problem> problems = List.of();
  private final PrefabConnectionsTab connectionsTab =
      new PrefabConnectionsTab(new ConnectionsHost());
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
    if (InputManager.isKeyJustPressed(SECONDARY_UP)
        && (pendingAnchorDrag == null || !pendingAnchorDrag.active())) {
      snapMode = snapMode.nextMode();
    }
    Point cursor = getCursorPosition();
    if (pendingLinkPick != null) {
      if (InputManager.isButtonJustPressed(Input.Buttons.RIGHT)) {
        cancelLinkPick();
      } else if (InputManager.isButtonJustPressed(Input.Buttons.LEFT)) {
        completeLinkPick(cursor);
      }
      return;
    }
    if (InputManager.isButtonJustPressed(Input.Buttons.LEFT)) {
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
        && InputManager.isButtonPressed(Input.Buttons.LEFT)
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
    if (pendingAnchorDrag != null && InputManager.isButtonJustReleased(Input.Buttons.LEFT)) {
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
    if (InputManager.isKeyJustPressed(TERTIARY)) {
      pendingPointAssignment = lastPointAssignment;
    }
  }

  @Override
  public void render() {
    DungeonLevel level = getLevel();
    Map<PrefabInstance, PrefabInstance> normalized = new IdentityHashMap<>();
    for (PrefabInstance source : level.prefabs()) {
      Prefab prefab = PrefabRegistry.require(source.type());
      PrefabInstance instance = normalizedRenderInstances.get(source);
      if (instance == null) instance = prefab.normalize(source);
      normalized.put(source, instance);
      Point highlighted =
          pendingAnchorDrag != null
                  && pendingAnchorDrag.active()
                  && pendingAnchorDrag.anchor().instanceName().equals(source.name())
              ? pendingAnchorDrag.anchor().displayPosition()
              : null;
      boolean selected = Objects.equals(selectedName, source.name());
      prefab.renderEditorFeedback(
          level,
          instance,
          selected || instance.active()
              ? new DebugDrawPrefabEditorFeedback(selected, highlighted)
              : new DebugDrawPrefabEditorFeedback(INACTIVE_STYLE),
          selected);
      if (!instance.active()) {
        linkAnchor(instance)
            .ifPresent(
                anchor ->
                    DebugDrawSystem.drawTextInWorldCoordsCentered(
                        "(inactive)", anchor.translate(0f, -0.35f), INACTIVE_LABEL_COLOR));
      }
    }
    normalizedRenderInstances = normalized;
    renderLinks(level);
    if (pendingLinkPick != null) {
      for (PrefabInstance instance : level.prefabs()) {
        if (pickTargets.contains(instance.name())) {
          linkAnchor(instance)
              .ifPresent(
                  anchor -> {
                    DebugDrawSystem.drawCircleOutline(anchor, 0.4f, PICK_TARGET_COLOR);
                    DebugDrawSystem.drawCircleOutline(anchor, 0.43f, PICK_TARGET_COLOR);
                  });
        }
      }
    }
    if (pendingAnchorDrag != null && pendingAnchorDrag.active()) {
      Point destination =
          feedbackPositionForCursor(
              getCursorPosition(), pendingAnchorDrag.anchor().feedbackOffset());
      DebugDrawSystem.drawPoint(
          pendingAnchorDrag.anchor().displayPosition(),
          POINT_ASSIGNMENT_PREVIEW_RADIUS,
          DRAG_ORIGINAL_COLOR);
      DebugDrawSystem.drawPoint(destination, POINT_ASSIGNMENT_PREVIEW_RADIUS, DRAG_PREVIEW_COLOR);
    }
    if (pendingPointAssignment != null) {
      DebugDrawSystem.drawPoint(
          feedbackPositionForCursor(getCursorPosition(), pendingPointAssignment.feedbackOffset()),
          POINT_ASSIGNMENT_PREVIEW_RADIUS,
          POINT_ASSIGNMENT_PREVIEW_COLOR);
    }
  }

  private void renderLinks(DungeonLevel level) {
    List<PrefabInstance> instances = level.prefabs();
    Map<String, PrefabInstance> byName = new HashMap<>();
    for (PrefabInstance instance : instances) byName.put(instance.name(), instance);
    Map<String, Integer> labelsPerPair = new HashMap<>();
    PrefabRuntimeState runtime = level.prefabRuntimeState();
    long now = System.nanoTime();
    for (PrefabWiring.Connection connection : PrefabWiring.connections(instances)) {
      String source = connection.link().source();
      String receiver = connection.receiver().name();
      boolean involved =
          Objects.equals(selectedName, source) || Objects.equals(selectedName, receiver);
      if (!involved && !showAllLinks) continue;
      PrefabInstance sourceInstance = byName.get(source);
      if (sourceInstance == null) continue;
      Optional<Point> from = linkAnchor(sourceInstance);
      Optional<Point> to = linkAnchor(connection.receiver());
      if (from.isEmpty() || to.isEmpty()) continue;

      boolean signal = connection.input().kind().isSignal();
      Color color =
          (signal ? PrefabConnectionsTab.SIGNAL_COLOR : PrefabConnectionsTab.ARTIFACT_COLOR).cpy();
      if (hasError(connection)) color.set(PrefabConnectionsTab.ERROR_COLOR);
      float pulse =
          runtime
              .lastSignal(PrefabRuntime.signalKey(connection))
              .map(time -> 1f - (now - time) / 1e9f / LINK_PULSE_SECONDS)
              .filter(value -> value > 0f)
              .orElse(0f);
      color.lerp(Color.WHITE, pulse * 0.6f);
      color.a = involved ? 0.9f : 0.3f;
      color.a = Math.max(color.a, pulse);
      float width = (involved ? 0.05f : 0.025f) * (1f + pulse * 1.5f);

      String pairKey =
          source.compareTo(receiver) < 0 ? source + "\n" + receiver : receiver + "\n" + source;
      int stackIndex = labelsPerPair.merge(pairKey, 1, Integer::sum) - 1;
      Point labelPosition;
      if (source.equals(receiver)) {
        Point loopCenter = from.get().translate(0f, 0.55f);
        DebugDrawSystem.drawCircleOutline(loopCenter, 0.3f, color);
        labelPosition = loopCenter.translate(0f, 0.45f + stackIndex * LINK_LABEL_SPACING);
      } else {
        DebugDrawSystem.drawLinkLine(from.get(), to.get(), width, color);
        labelPosition =
            new Point(
                (from.get().x() + to.get().x()) * 0.5f,
                (from.get().y() + to.get().y()) * 0.5f + 0.3f + stackIndex * LINK_LABEL_SPACING);
      }
      if (involved) {
        String output =
            PrefabWiring.outputPort(instances, connection.link())
                .map(PrefabPort::displayName)
                .orElse(connection.link().port());
        Color labelColor = color.cpy();
        labelColor.a = 1f;
        DebugDrawSystem.drawTextInWorldCoordsCentered(
            output + " -> " + connection.input().displayName(), labelPosition, labelColor);
      }
    }
  }

  private boolean hasError(PrefabWiring.Connection connection) {
    for (PrefabWiring.Problem problem : problems) {
      if (problem.severity() == PrefabWiring.Severity.ERROR
          && problem.instance().equals(connection.receiver().name())
          && problem.port().equals(connection.input().key())
          && problem.link().equals(connection.link())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the world position where links of an instance start and end: its first point, or the
   * center of its first region.
   *
   * @param instance normalized instance
   * @return link anchor, if the instance has a world position
   */
  private Optional<Point> linkAnchor(PrefabInstance instance) {
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
    Prefab[] prefabDefinitions = PrefabRegistry.all().toArray(Prefab[]::new);
    Arrays.sort(
        prefabDefinitions,
        Comparator.comparing(Prefab::displayName, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(Prefab::displayName));
    if (selectedPrefab == null && prefabDefinitions.length > 0) {
      selectedPrefab = prefabDefinitions[0];
    }
    listOrderTabs = new Table();
    rebuildListOrderTabs();
    content.add(listOrderTabs).growX().padBottom(4f).row();
    instanceList = new PrefabInstanceList(new InstanceListListener());
    var list = Scene2dElementFactory.createScrollPane(instanceList, false, true);
    list.setFlickScroll(false);
    content.add(list).growX().height(350f).row();

    Table actions = new Table();
    ImageButton add = Scene2dElementFactory.createIconButton("hud/plus.png", "blue-outline");
    add.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            addSelectedPrefab();
          }
        });
    ImageButton duplicate = Scene2dElementFactory.createIconButton("hud/copy.png", "default");
    duplicate.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            duplicateSelected();
          }
        });
    ImageButton delete = Scene2dElementFactory.createIconButton("hud/trash.png", "red-outline");
    delete.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            selected().ifPresent(i -> delete(i.name()));
          }
        });
    actions.defaults().minWidth(0).growX().uniformX().height(50f).pad(2f);
    actions.add(add);
    actions.add(duplicate);
    actions.add(delete);
    prefabTypeSetting =
        new SelectSetting<>(
            "Prefab Type",
            prefabDefinitions,
            () -> selectedPrefab,
            prefab -> selectedPrefab = prefab,
            Prefab::displayName,
            true);
    content.add(prefabTypeSetting).growX().padTop(6f).row();
    content.add(actions).growX().padTop(4f).row();
    content
        .add(
            new BooleanSetting("Show All Links", () -> showAllLinks, value -> showAllLinks = value))
        .growX()
        .padTop(6f)
        .row();
    rebuildDetails();
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
    secondaryScrollPane = Scene2dElementFactory.createScrollPane(secondaryContent, false, true);
    ScrollPane.ScrollPaneStyle scrollStyle =
        new ScrollPane.ScrollPaneStyle(secondaryScrollPane.getStyle());
    scrollStyle.background = null;
    scrollStyle.corner = null;
    secondaryScrollPane.setStyle(scrollStyle);
    secondaryScrollPane.setFlickScroll(false);
    secondaryScrollPane.setFadeScrollBars(false);
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
    if (prefabTypeSetting != null) prefabTypeSetting.refresh();
    if (rebuildPending) {
      rebuildPending = false;
      rebuildDetails();
    }
  }

  @Override
  public String additionalInformation() {
    if (pendingLinkPick != null) {
      return "Click a highlighted prefab to link '"
          + pendingLinkPick.port().displayName()
          + "'. Right click cancels.";
    }
    return "Snap Mode: "
        + snapMode.name()
        + (pendingPointAssignment == null ? "" : "\nWaiting for world point assignment");
  }

  @Override
  public Map<Integer, String> getControls() {
    Map<Integer, String> controls = new LinkedHashMap<>();
    controls.put(Input.Buttons.LEFT, "Select / drag prefab anchors / assign point");
    controls.put(SECONDARY_UP, "Change point and region snap mode");
    controls.put(TERTIARY, "Arm last world point assignment");
    controls.put(Input.Buttons.RIGHT, "Cancel linking in the world");
    return controls;
  }

  private void rebuildDetails() {
    rebuildDetails(true);
  }

  private void rebuildDetails(boolean rebuildSecondary) {
    if (instanceList == null) return;
    problems = PrefabWiring.problems(getLevel().prefabs());
    instanceList.setItems(listItems(), selectedName, listOrder == ListOrder.LOAD_ORDER);
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
              TextField name = Scene2dElementFactory.createTextField(instance.name(), 20);
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
              nameRow.add(name).growX().minWidth(0f).height(40f);
              TextButton active =
                  Scene2dElementFactory.createButton(
                      instance.active() ? "Active" : "Inactive",
                      instance.active() ? "green" : "default",
                      14);
              active.addListener(
                  new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                      setActive(instance.name(), !instance.active());
                    }
                  });
              nameRow.add(active).width(84f).height(40f).padLeft(4f);
              secondaryHeader.add(nameRow).growX().row();
              secondaryHeader
                  .add(
                      Scene2dElementFactory.createLabel(
                          prefab.displayName(),
                          14,
                          ModeDetailsPanel.TEXT_COLOR.cpy().mul(1f, 1f, 1f, .65f)))
                  .left()
                  .padBottom(5f)
                  .padLeft(5f)
                  .row();
              secondaryHeader.add(Scene2dElementFactory.createHorizontalDivider()).growX().row();
              Optional<PrefabProperty<ShaderParameters>> editedShader =
                  editedShaderProperty(prefab, instance);
              if (editedShader.isPresent()) {
                addShaderParameterEditor(editedShader.get(), instance);
                return;
              }
              secondaryHeader.add(detailsTabs(instance)).growX().padTop(4f).row();
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
    tabs.defaults().minWidth(0).growX().uniformX().height(34f).pad(0f, 2f, 0f, 2f);
    int links = PrefabConnectionsTab.linkCount(getLevel().prefabs(), instance);
    for (DetailsTab tab : DetailsTab.values()) {
      String label = tab == DetailsTab.CONNECTIONS ? tab.label + " (" + links + ")" : tab.label;
      TextButton button =
          Scene2dElementFactory.createButton(
              label, tab == detailsTab ? "blue-outline" : "default", 14);
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

  private void addProblemNotice(PrefabInstance instance) {
    long errors = 0;
    long warnings = 0;
    for (PrefabWiring.Problem problem : problemsOf(instance.name())) {
      if (problem.severity() == PrefabWiring.Severity.ERROR) errors++;
      else warnings++;
    }
    if (errors + warnings == 0) return;
    Label notice =
        Scene2dElementFactory.createLabel(
            (errors + warnings)
                + (errors + warnings == 1 ? " link needs" : " links need")
                + " attention, see Connections.",
            14,
            errors > 0 ? PrefabConnectionsTab.ERROR_COLOR : PrefabConnectionsTab.WARNING_COLOR);
    notice.setWrap(true);
    secondaryContent.add(notice).growX().padTop(SETTINGS_PAD).padLeft(5f).row();
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
    setting
        .add(
            Scene2dElementFactory.createLabel(
                property.displayName(), 18, ModeDetailsPanel.TEXT_COLOR))
        .left()
        .row();
    for (PrefabLink link : instance.links(slot)) {
      Table row = new Table();
      String output =
          PrefabWiring.outputPort(getLevel().prefabs(), link)
              .map(PrefabPort::displayName)
              .orElse(link.port());
      Label chip =
          Scene2dElementFactory.createLabel(
              "Artifact: " + link.source() + " > " + output,
              14,
              PrefabConnectionsTab.ARTIFACT_COLOR);
      chip.setEllipsis(true);
      row.add(chip).growX().minWidth(0f).left();
      TextButton go = Scene2dElementFactory.createButton("Go", "default", 13);
      go.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              focus(link.source());
            }
          });
      row.add(go).width(36f).height(26f);
      TextButton unlink = Scene2dElementFactory.createButton("X", "red-outline", 13);
      unlink.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              removeLink(instance.name(), slot.key(), link);
            }
          });
      row.add(unlink).width(30f).height(26f).padLeft(2f);
      setting.add(row).padTop(2f).row();
    }
    Label fallback =
        Scene2dElementFactory.createLabel(
            "Manual value kept for when the link is removed: "
                + instance.properties().get(property.key()),
            12,
            PrefabConnectionsTab.MUTED_COLOR);
    fallback.setWrap(true);
    setting.add(fallback).row();
    secondaryContent.add(setting).growX().padTop(SETTINGS_PAD).row();
  }

  private void addUseArtifactButton(PrefabInstance instance, ArtifactSlot<?> slot) {
    TextButton button =
        Scene2dElementFactory.createButton(
            "Use Artifact (" + slot.type().displayName() + ")...", "default", 13);
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
    for (PrefabPort port : prefab.ports(PrefabPort.Kind.ARTIFACT_SLOT)) {
      if (!(port instanceof ArtifactSlot<?> slot)
          || slot.placeholderProperty().filter(p -> p == property).isEmpty()) {
        continue;
      }
      List<String> aliases = new ArrayList<>();
      for (PrefabLink link : instance.links(slot)) aliases.add(link.alias());
      String text = String.valueOf(slot.placeholderProperty().get().get(instance));
      Set<String> unknown = new java.util.LinkedHashSet<>(Placeholders.names(text));
      unknown.removeAll(aliases);
      String hint =
          aliases.isEmpty()
              ? "Link '"
                  + slot.displayName()
                  + "' artifacts in Connections to use {alias} placeholders."
              : "Placeholders: "
                  + String.join(" ", aliases.stream().map(alias -> "{" + alias + "}").toList());
      Label hintLabel =
          Scene2dElementFactory.createLabel(hint, 12, PrefabConnectionsTab.MUTED_COLOR);
      hintLabel.setWrap(true);
      secondaryContent.add(hintLabel).growX().padLeft(5f).row();
      if (!unknown.isEmpty()) {
        Label warning =
            Scene2dElementFactory.createLabel(
                "Unknown placeholders (shown as written): "
                    + String.join(" ", unknown.stream().map(name -> "{" + name + "}").toList()),
                12,
                PrefabConnectionsTab.WARNING_COLOR);
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
        Scene2dElementFactory.createLabel(
            shaderProperty.displayName() + ": " + parameters.shader().displayName(),
            18,
            ModeDetailsPanel.TEXT_COLOR);
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
          .add(
              Scene2dElementFactory.createLabel(
                  "This shader has no parameters.",
                  14,
                  ModeDetailsPanel.TEXT_COLOR.cpy().mul(1f, 1f, 1f, .65f)))
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
        String[] values = p.choices().toArray(String[]::new);
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
                new SelectSetting<>(
                    "Deco",
                    Deco.values(),
                    () -> access.get(property).pieces().get(index).deco(),
                    deco -> {
                      ImagePuzzleSettings settings = access.get(property);
                      PuzzlePiecePlacement placement = settings.pieces().get(index);
                      access.set(
                          property, settings.withPiece(index, placement.withDeco(deco)), true);
                    },
                    PrefabMode::decoDisplayName,
                    true))
            .row();
      }
      secondaryContent.add(pieceControls).growX().padTop(SETTINGS_PAD).row();
    }
  }

  private static String decoDisplayName(Deco deco) {
    String name = deco.name();
    StringBuilder displayName = new StringBuilder(name.length() + 8);
    for (int index = 0; index < name.length(); index++) {
      char current = name.charAt(index);
      if (index > 0) {
        char previous = name.charAt(index - 1);
        boolean caseBoundary =
            Character.isUpperCase(current)
                && (Character.isLowerCase(previous)
                    || (Character.isUpperCase(previous)
                        && index + 1 < name.length()
                        && Character.isLowerCase(name.charAt(index + 1))));
        boolean numberBoundary =
            (Character.isDigit(current) && Character.isLetter(previous))
                || (Character.isLetter(current) && Character.isDigit(previous));
        boolean wordBoundary = caseBoundary || numberBoundary;
        if (wordBoundary) displayName.append(' ');
      }
      displayName.append(current);
    }
    return displayName.toString();
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
                  applyChange(() -> getLevel().replacePrefab(index, replacement), rebuild);
                }
              } catch (IllegalArgumentException exception) {
                LevelEditorSystem.showFeedback(exception.getMessage(), Color.YELLOW);
                requestRebuild();
              }
            });
  }

  /**
   * Lists the visible properties and shader parameters, which decide the generated controls.
   *
   * @param prefab prefab definition of the instance
   * @param instance normalized instance
   * @return keys of the visible properties and shader parameters
   */
  private static List<String> visibilitySignature(Prefab prefab, PrefabInstance instance) {
    List<String> keys = new ArrayList<>();
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
    PrefabInstance receiver = getLevel().prefabs().get(index);
    Prefab prefab = PrefabRegistry.require(receiver.type());
    List<PrefabLink> links = new ArrayList<>(receiver.links(input));
    if (input instanceof ArtifactSlot<?> slot && !slot.multiple()) links.clear();
    links.add(link);
    PrefabInstance replacement;
    try {
      replacement = prefab.normalize(receiver.withLinks(input.key(), links));
    } catch (IllegalArgumentException exception) {
      LevelEditorSystem.showFeedback(exception.getMessage(), Color.YELLOW);
      return;
    }
    applyChange(() -> getLevel().replacePrefab(index, replacement));
    Optional<PrefabPort> output = PrefabWiring.outputPort(getLevel().prefabs(), link);
    if (output.isPresent() && PrefabWiring.lacksPlayer(output.get(), input)) {
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
    Optional<PrefabInstance> owner = selectedInstance(end.instance());
    if (owner.isEmpty())
      owner = instances.stream().filter(i -> i.name().equals(end.instance())).findFirst();
    if (owner.isEmpty()) return List.of();
    List<PortPickerPopup.Option> options = new ArrayList<>();
    boolean endIsInput = end.port().kind().isInput();
    for (PrefabInstance candidate : instances) {
      if (onlyInstance != null && !candidate.name().equals(onlyInstance)) continue;
      Prefab prefab = PrefabRegistry.require(candidate.type());
      for (PrefabPort port : prefab.ports()) {
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
        boolean cycle =
            artifact
                && PrefabWiring.wouldCreateArtifactCycle(instances, link.source(), receiver.name());
        String detail = null;
        if (linked) detail = "already linked";
        else if (cycle) detail = "would make the value depend on itself";
        else if (PrefabWiring.lacksPlayer(output, input)) detail = "runs for every player";
        else if (input instanceof ArtifactSlot<?> slot
            && !slot.multiple()
            && !receiver.links(input).isEmpty()) {
          detail = "replaces the current link";
        }
        String label = port.displayName();
        if (port instanceof ArtifactOutput<?> artifactOutput) {
          label += " (" + artifactOutput.type().displayName() + ")";
        } else if (port instanceof ArtifactSlot<?> slot) {
          label += " (" + slot.type().displayName() + ")";
        }
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
                !linked && !cycle,
                () -> addLink(receiver.name(), input, link)));
      }
    }
    return options;
  }

  private void openLinkPicker(PrefabConnectionsTab.LinkEnd end, Actor anchor) {
    cancelLinkPick();
    LevelEditorUI ui = editorUi(anchor);
    if (ui == null || anchor.getStage() == null) return;
    com.badlogic.gdx.math.Vector2 position =
        anchor.localToStageCoordinates(new com.badlogic.gdx.math.Vector2(0f, anchor.getHeight()));
    showPicker(end, null, ui, position.x - 350f, position.y);
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
    PortPickerPopup[] picker = new PortPickerPopup[1];
    picker[0] =
        new PortPickerPopup(
            title,
            linkOptions(end, onlyInstance),
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
            new com.badlogic.gdx.math.Vector2(Gdx.input.getX(), Gdx.input.getY()));
    showPicker(end, target.get().name(), ui, position.x + 12f, position.y);
  }

  private void addSelectedPrefab() {
    if (selectedPrefab == null) {
      LevelEditorSystem.showFeedback("No prefab definitions are registered.", Color.YELLOW);
      return;
    }
    add(selectedPrefab);
  }

  private void add(Prefab prefab) {
    cancelAnchorDrag();
    clearPendingPointAssignment();
    String area = selected().map(i -> PrefabNames.areaPrefix(i.name())).orElse(null);
    String base = area == null ? prefab.type() : area + "/" + prefab.type();
    String name = containsName(base) ? PrefabNames.nextName(base, this::containsName) : base;
    PrefabInstance instance = prefab.newInstance(name);
    Point screenCenter =
        new Point(CameraSystem.camera().position.x, CameraSystem.camera().position.y);
    Optional<WorldAnchor> anchor = worldAnchors(instance).stream().findFirst();
    if (anchor.isPresent()) {
      Point spawnPosition = propertyPositionForCursor(screenCenter, anchor.get().feedbackOffset());
      Point anchorPosition = anchor.get().authoredPosition();
      instance =
          prefab.translate(
              instance,
              Vector2.of(
                  spawnPosition.x() - anchorPosition.x(), spawnPosition.y() - anchorPosition.y()));
    }
    PrefabInstance added = prefab.normalize(instance);
    applyChange(
        () -> {
          getLevel().addPrefab(added);
          applyGroupedOrder();
          selectedName = name;
        });
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

  private void rebuildListOrderTabs() {
    listOrderTabs.clearChildren();
    listOrderTabs.defaults().minWidth(0).growX().uniformX().height(34f).pad(0f, 2f, 0f, 2f);
    for (ListOrder order : ListOrder.values()) {
      TextButton tab =
          Scene2dElementFactory.createButton(
              order.label, order == listOrder ? "blue-outline" : "default", 14);
      tab.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              if (listOrder == order) return;
              listOrder = order;
              rebuildListOrderTabs();
              rebuildDetails(false);
            }
          });
      listOrderTabs.add(tab);
    }
  }

  private List<PrefabInstanceList.Item> listItems() {
    List<PrefabNames.Node> tree = PrefabNames.tree(getLevel().prefabs());
    if (listOrder == ListOrder.ALPHABETICAL) tree = PrefabNames.alphabetical(tree);
    Map<String, PrefabWiring.Severity> worst = new HashMap<>();
    for (PrefabWiring.Problem problem : problems) {
      worst.merge(problem.instance(), problem.severity(), (a, b) -> a.compareTo(b) >= 0 ? a : b);
    }
    return listItems(tree, "", instance -> badges(instance, worst.get(instance.name())));
  }

  private static List<PrefabInstanceList.Badge> badges(
      PrefabInstance instance, PrefabWiring.Severity problem) {
    List<PrefabInstanceList.Badge> badges = new ArrayList<>();
    if (!instance.active()) {
      badges.add(new PrefabInstanceList.Badge("off", new Color(0.55f, 0.55f, 0.55f, 1f)));
    }
    if (problem != null) {
      badges.add(
          new PrefabInstanceList.Badge(
              "!",
              problem == PrefabWiring.Severity.ERROR
                  ? PrefabConnectionsTab.ERROR_COLOR
                  : PrefabConnectionsTab.WARNING_COLOR));
    }
    return badges;
  }

  // Inside an area group, the area prefix is omitted from the displayed names.
  private static List<PrefabInstanceList.Item> listItems(
      List<PrefabNames.Node> nodes,
      String hiddenPrefix,
      Function<PrefabInstance, List<PrefabInstanceList.Badge>> badges) {
    List<PrefabInstanceList.Item> items = new ArrayList<>();
    for (PrefabNames.Node node : nodes) {
      switch (node) {
        case PrefabNames.Leaf leaf -> {
          String name = leaf.instance().name();
          items.add(
              new PrefabInstanceList.Entry(
                  leaf.id(),
                  name,
                  name.substring(hiddenPrefix.length()),
                  badges.apply(leaf.instance())));
        }
        case PrefabNames.Branch branch when branch.kind() == PrefabNames.BranchKind.AREA ->
            items.add(
                new PrefabInstanceList.Group(
                    branch.id(),
                    branch.key(),
                    listItems(branch.children(), branch.key() + "/", badges)));
        case PrefabNames.Branch branch ->
            items.add(
                new PrefabInstanceList.Group(
                    branch.id(),
                    branch.key().substring(hiddenPrefix.length()),
                    listItems(branch.children(), hiddenPrefix, badges)));
      }
    }
    return items;
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

  private void toggleSelection(String name) {
    cancelAnchorDrag();
    clearPendingPointAssignment();
    selectedName = Objects.equals(selectedName, name) ? null : name;
    rebuildPending = true;
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
    return getLevel().prefabs().stream().filter(i -> i.name().equals(selectedName)).findFirst();
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
    float best = Float.MAX_VALUE;
    for (WorldAnchor anchor : worldAnchors(instance)) {
      best = Math.min(best, (float) anchor.displayPosition().distance(cursor));
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

  private void applyChange(Runnable mutation) {
    applyChange(mutation, true);
  }

  private void applyChange(Runnable mutation, boolean rebuildDetails) {
    List<PrefabInstance> previousPrefabs = new ArrayList<>(getLevel().prefabs());
    String previousSelection = selectedName;
    try {
      mutation.run();
      discardRuntimeChanges(previousPrefabs);
      syncChanged();
      levelChanged();
      if (rebuildDetails) requestRebuild();
      else rebuildDetails(false);
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
    CONNECTIONS("Connections");

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

  /** Forwards list interactions to the editor mode. */
  private final class InstanceListListener implements PrefabInstanceList.Listener {
    @Override
    public void select(String name) {
      toggleSelection(name);
    }

    @Override
    public String renameGroup(String groupId, String label, String newLabel) {
      return PrefabMode.this.renameGroup(groupId, label, newLabel);
    }

    @Override
    public void moveItem(String parentId, String itemId, int insertionIndex) {
      PrefabMode.this.moveItem(parentId, itemId, insertionIndex);
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
