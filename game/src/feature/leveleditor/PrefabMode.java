package feature.leveleditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import engine.Game;
import engine.level.DungeonLevel;
import engine.systems.CameraSystem;
import engine.systems.input.InputManager;
import engine.utils.Point;
import engine.utils.Scene2dElementFactory;
import engine.utils.Vector2;
import feature.leveleditor.ui.BooleanSetting;
import feature.leveleditor.ui.ColorSetting;
import feature.leveleditor.ui.FloatSetting;
import feature.leveleditor.ui.ModeDetailsPanel;
import feature.leveleditor.ui.IntegerSetting;
import feature.leveleditor.ui.NumberSliderSetting;
import feature.leveleditor.ui.PointSetting;
import feature.leveleditor.ui.RegionSetting;
import feature.leveleditor.ui.SelectSetting;
import feature.leveleditor.ui.StringSetting;
import feature.leveleditor.ui.Vector2Setting;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabPropertyType;
import feature.prefabs.PrefabRegistry;
import feature.prefabs.PrefabSide;
import feature.prefabs.PrefabSpawner;
import feature.prefabs.Region;
import feature.systems.DebugDrawSystem;
import feature.systems.LevelEditorSystem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

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

  private String selectedName;
  private Prefab selectedPrefab;
  private PendingPointAssignment pendingPointAssignment;
  private PendingPointAssignment lastPointAssignment;
  private PendingAnchorDrag pendingAnchorDrag;
  private SnapMode snapMode = SnapMode.OnGrid;
  private SnapMode snapModeBeforeAnchorDrag;
  private Table detailsContent;
  private Table secondaryContent;
  private Table listContent;
  private SelectSetting<Prefab> prefabTypeSetting;
  private boolean rebuildPending;

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
    if (selectedName != null && selected().isEmpty()) selectedName = null;
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
    try {
      respawnAll();
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
    if (InputManager.isButtonJustPressed(Input.Buttons.LEFT)) {
      if (pendingPointAssignment != null) {
        PendingPointAssignment assignment = pendingPointAssignment;
        pendingPointAssignment = null;
        assignment.assignment().accept(snapMode.getPosition(cursor));
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
      snapMode = PointMode.snapModeFor(pendingAnchorDrag.anchor().authoredPosition());
    }
    if (pendingAnchorDrag != null
        && InputManager.isButtonJustReleased(Input.Buttons.LEFT)) {
      PendingAnchorDrag drag = pendingAnchorDrag;
      pendingAnchorDrag = null;
      if (drag.active()) {
        try {
          commitAnchorDrag(drag, snapMode.getPosition(cursor));
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
    for (PrefabInstance source : level.prefabs()) {
      Prefab prefab = PrefabRegistry.require(source.type());
      Point highlighted =
          pendingAnchorDrag != null
                  && pendingAnchorDrag.active()
                  && pendingAnchorDrag.anchor().instanceName().equals(source.name())
              ? pendingAnchorDrag.anchor().displayPosition()
              : null;
      prefab.renderEditorFeedback(
          level,
          prefab.normalize(source),
          new DebugDrawPrefabEditorFeedback(
              Objects.equals(selectedName, source.name()), highlighted),
          Objects.equals(selectedName, source.name()));
    }
    if (pendingAnchorDrag != null && pendingAnchorDrag.active()) {
      Point destination =
          feedbackPositionForCursor(
              getCursorPosition(), pendingAnchorDrag.anchor().feedbackOffset());
      DebugDrawSystem.drawPoint(
          pendingAnchorDrag.anchor().displayPosition(),
          POINT_ASSIGNMENT_PREVIEW_RADIUS,
          DRAG_ORIGINAL_COLOR);
      DebugDrawSystem.drawPoint(
          destination, POINT_ASSIGNMENT_PREVIEW_RADIUS, DRAG_PREVIEW_COLOR);
    }
    if (pendingPointAssignment != null) {
      DebugDrawSystem.drawPoint(
          feedbackPositionForCursor(
              getCursorPosition(), pendingPointAssignment.feedbackOffset()),
          POINT_ASSIGNMENT_PREVIEW_RADIUS,
          POINT_ASSIGNMENT_PREVIEW_COLOR);
    }
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
    listContent = new Table();
    listContent.top().defaults().growX();
    var list = Scene2dElementFactory.createScrollPane(listContent, false, true);
    content.add(list).growX().height(350f).row();

    Table actions = new Table();
    ImageButton add = Scene2dElementFactory.createIconButton("hud/check.png", "blue-outline");
    add.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            addSelectedPrefab();
          }
        });
    ImageButton duplicate =
        Scene2dElementFactory.createIconButton("hud/kenney/chess_king.png", "default");
    duplicate.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            duplicateSelected();
          }
        });
    ImageButton delete = Scene2dElementFactory.createIconButton("hud/cross.png", "red-outline");
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
    rebuildDetails();
  }

  @Override
  public boolean hasSecondaryDetailsUI() {
    return selected().isPresent();
  }

  @Override
  public void buildSecondaryDetailsUI(Table content) {
    secondaryContent = content;
    content.clearChildren();
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
    return "Snap Mode: "
        + snapMode.name()
        + "\nDrag a prefab point or region corner to move it"
        + "\nWorld click selects the nearest prefab anchor"
        + (pendingPointAssignment == null ? "" : "\nWaiting for world point assignment");
  }

  @Override
  public Map<Integer, String> getControls() {
    Map<Integer, String> controls = new LinkedHashMap<>();
    controls.put(Input.Buttons.LEFT, "Select / drag prefab anchors / assign point");
    controls.put(SECONDARY_UP, "Change point and region snap mode");
    controls.put(TERTIARY, "Arm last world point assignment");
    return controls;
  }

  private void rebuildDetails() {
    rebuildDetails(true);
  }

  private void rebuildDetails(boolean rebuildSecondary) {
    if (listContent == null) return;
    listContent.clearChildren();
    for (PrefabInstance instance :
        getLevel().prefabs().stream()
            .sorted(
                Comparator.comparing(
                        PrefabInstance::name, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(PrefabInstance::name))
            .toList()) {
      TextButton entry =
          Scene2dElementFactory.createButton(
              instance.name(),
              Objects.equals(selectedName, instance.name()) ? "blue-outline" : "default",
              14);
      entry.addListener(
          new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              cancelAnchorDrag();
              clearPendingPointAssignment();
              selectedName = Objects.equals(selectedName, instance.name()) ? null : instance.name();
              rebuildPending = true;
            }
          });
      Table entryContainer = new Table();
      entryContainer.add(entry).growX().height(30f);
      listContent.add(entryContainer).growX().pad(1f).padLeft(6f).padRight(6f).row();
    }
    if (!rebuildSecondary || secondaryContent == null) return;
    secondaryContent.clearChildren();
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
              secondaryContent.add(name).growX().height(40f).row();
              secondaryContent
                  .add(
                      Scene2dElementFactory.createLabel(
                          prefab.displayName(),
                          14,
                          ModeDetailsPanel.TEXT_COLOR.cpy().mul(1f, 1f, 1f, .65f)))
                  .left()
                  .padBottom(5f)
                  .padLeft(5f)
                  .row();
              secondaryContent.add(Scene2dElementFactory.createHorizontalDivider()).growX().row();
              for (PrefabProperty<?> property : prefab.properties())
                addProperty(property, instance);
            });
  }

  private void addProperty(PrefabProperty<?> property, PrefabInstance source) {
    PrefabPropertyType type = property.type();
    switch (type) {
      case STRING -> {
        PrefabProperty<String> p = cast(property);
        secondaryContent
            .add(
                new StringSetting(
                    p.displayName(), () -> p.get(current()), value -> setProperty(p, value), true))
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
                    () -> p.get(current()),
                    value -> setProperty(p, value)))
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
                    () -> p.get(current()),
                    value -> setProperty(p, value)))
            .growX()
            .padTop(SETTINGS_PAD)
            .row();
      }
      case NUMBER_SLIDER -> {
        PrefabProperty<Float> p = cast(property);
        float min = p.minimum().orElse(0f).floatValue();
        float max = p.maximum().orElse(1f).floatValue();
        float step = p.step().orElse(0f).floatValue();
        String instanceName = source.name();
        secondaryContent
            .add(
                new NumberSliderSetting(
                    p.displayName(),
                    min,
                    max,
                    step,
                    () ->
                        selected()
                            .filter(instance -> instance.name().equals(instanceName))
                            .map(p::get)
                            .orElse(p.defaultValue()),
                    value -> {
                      if (Objects.equals(selectedName, instanceName)) setProperty(p, value, false);
                    }))
            .growX()
            .padTop(SETTINGS_PAD)
            .row();
      }
      case BOOLEAN -> {
        PrefabProperty<Boolean> p = cast(property);
        secondaryContent
            .add(
                new BooleanSetting(
                    p.displayName(), () -> p.get(current()), value -> setProperty(p, value)))
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
                    () -> p.get(current()),
                    value -> setProperty(p, value),
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
                    () -> p.get(current()),
                    value -> setProperty(p, value),
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
                    () -> p.get(current()),
                    value -> setProperty(p, value),
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
                    p.displayName(), () -> p.get(current()), value -> setProperty(p, value)))
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
                    () -> p.get(current()),
                    value -> setProperty(p, value),
                    message ->
                        LevelEditorSystem.showFeedback(
                            p.displayName() + ": " + message, Color.YELLOW)))
            .growX()
            .padTop(SETTINGS_PAD)
            .row();
      }
    }
  }

  private PrefabInstance current() {
    return selected().orElseThrow(() -> new IllegalStateException("No prefab selected"));
  }

  private <T> void setProperty(PrefabProperty<T> property, T value) {
    setProperty(property, value, true);
  }

  private <T> void setProperty(
      PrefabProperty<T> property, T value, boolean rebuildSecondaryDetails) {
    selected()
        .ifPresent(
            source -> {
              try {
                Prefab prefab = PrefabRegistry.require(source.type());
                PrefabInstance replacement = prefab.normalize(property.set(source, value));
                int index = getLevel().prefabs().indexOf(source);
                if (index >= 0) {
                  applyChange(
                      () -> getLevel().replacePrefab(index, replacement),
                      rebuildSecondaryDetails);
                }
              } catch (IllegalArgumentException exception) {
                LevelEditorSystem.showFeedback(exception.getMessage(), Color.YELLOW);
                requestRebuild();
              }
            });
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
    String name = uniqueName(prefab.type());
    PrefabInstance instance = prefab.newInstance(name);
    Point screenCenter =
        new Point(CameraSystem.camera().position.x, CameraSystem.camera().position.y);
    Point spawnPosition = snapMode.getPosition(screenCenter);
    Optional<WorldAnchor> anchor = worldAnchors(instance).stream().findFirst();
    if (anchor.isPresent()) {
      Point anchorPosition = anchor.get().displayPosition();
      instance =
          prefab.translate(
              instance,
              Vector2.of(
                  spawnPosition.x() - anchorPosition.x(),
                  spawnPosition.y() - anchorPosition.y()));
    }
    PrefabInstance added = prefab.normalize(instance);
    applyChange(
        () -> {
          getLevel().addPrefab(added);
          selectedName = name;
        });
  }

  private void duplicateSelected() {
    cancelAnchorDrag();
    clearPendingPointAssignment();
    selected()
        .ifPresent(
            source -> {
              String name = uniqueName(source.name());
              applyChange(
                  () -> {
                    getLevel().addPrefab(source.withName(name));
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
    applyChange(
        () -> {
          getLevel().removePrefab(name);
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
    int index = getLevel().prefabs().indexOf(source);
    if (index >= 0) {
      applyChange(
          () -> {
            getLevel().replacePrefab(index, source.withName(name));
            selectedName = name;
          });
    }
  }

  private String uniqueName(String base) {
    String candidate = base;
    int suffix = 1;
    while (containsName(candidate)) candidate = base + "_" + suffix++;
    return candidate;
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
        .min(
            Comparator.comparingDouble(
                anchor -> anchor.displayPosition().distance(cursor)));
  }

  private List<WorldAnchor> worldAnchors(PrefabInstance instance) {
    Prefab prefab = PrefabRegistry.require(instance.type());
    List<WorldAnchor> anchors = new ArrayList<>();
    for (PrefabProperty<?> property : prefab.properties()) {
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
                AnchorCorner.POINT));
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
                AnchorCorner.BOTTOM_LEFT));
        anchors.add(
            new WorldAnchor(
                instance.name(),
                regionProperty,
                region.topRight(),
                region.topRight(),
                new Point(0f, 0f),
                AnchorCorner.TOP_RIGHT));
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
    // Both cursor assignment and dragging store the snapped property coordinate. The feedback
    // offset only moves its displayed marker, so do not subtract it when committing a drag.
    Point point = snappedPropertyPosition;
    try {
      PrefabInstance replacement;
      if (anchor.corner() == AnchorCorner.POINT) {
        replacement = cast(anchor.property()).set(source, point);
      } else {
        PrefabProperty<Region> regionProperty = cast(anchor.property());
        Region region = regionProperty.get(source);
        replacement =
            regionProperty.set(
                source,
                anchor.corner() == AnchorCorner.BOTTOM_LEFT
                    ? new Region(point, region.topRight())
                    : new Region(region.bottomLeft(), point));
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
    Point propertyPosition = snapMode.getPosition(cursor);
    return propertyPosition.translate(feedbackOffset.x(), feedbackOffset.y());
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
    despawnAll();
    try {
      for (PrefabSide side : activeSides()) {
        PrefabSpawner.spawn(getLevel(), side);
      }
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
      respawnAll();
      levelChanged();
      if (rebuildDetails) requestRebuild();
      else rebuildDetails(false);
    } catch (RuntimeException exception) {
      getLevel().prefabs().clear();
      getLevel().prefabs().addAll(previousPrefabs);
      selectedName = previousSelection;
      try {
        respawnAll();
      } catch (RuntimeException rollbackException) {
        exception.addSuppressed(rollbackException);
      }
      LevelEditorSystem.showFeedback(exception.getMessage(), Color.YELLOW);
      requestRebuild();
    }
  }

  private void despawnAll() {
    for (PrefabSide side : activeSides()) PrefabSpawner.clear(getLevel(), side);
  }

  private PrefabSide[] activeSides() {
    if (Game.isMultiplayerClient()) return new PrefabSide[] {PrefabSide.CLIENT};
    if (Game.isSingleplayer()) return new PrefabSide[] {PrefabSide.SERVER, PrefabSide.CLIENT};
    return new PrefabSide[] {PrefabSide.SERVER};
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

  private record PendingPointAssignment(
      Consumer<Point> assignment, Point feedbackOffset) {}

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
      AnchorCorner corner) {}

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
