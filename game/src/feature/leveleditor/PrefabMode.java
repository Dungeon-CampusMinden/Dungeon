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
import engine.systems.input.InputManager;
import engine.utils.Point;
import engine.utils.Scene2dElementFactory;
import engine.utils.Vector2;
import feature.leveleditor.ui.BooleanSetting;
import feature.leveleditor.ui.FloatSetting;
import feature.leveleditor.ui.ModeDetailsPanel;
import feature.leveleditor.ui.NumberSetting;
import feature.leveleditor.ui.PointSetting;
import feature.leveleditor.ui.SelectSetting;
import feature.leveleditor.ui.StringSetting;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabPropertyType;
import feature.prefabs.PrefabRegistry;
import feature.prefabs.PrefabSide;
import feature.prefabs.PrefabSpawner;
import feature.systems.LevelEditorSystem;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/** Editor mode for authoring registered prefab instances in a level. */
public final class PrefabMode extends LevelEditorMode {

  private static final float PICK_DISTANCE = 0.7f;
  private static final int SETTINGS_PAD = 6;

  private String selectedName;
  private Prefab selectedPrefab;
  private Consumer<Point> pendingPointAssignment;
  private SnapMode snapMode = SnapMode.OnGrid;
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
    clearPendingPointAssignment();
    selectedName = getLevel().prefabs().stream().map(PrefabInstance::name).findFirst().orElse(null);
    try {
      respawnAll();
    } catch (RuntimeException exception) {
      LevelEditorSystem.showFeedback(exception.getMessage(), Color.YELLOW);
    }
  }

  @Override
  public void onExit() {
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
  }

  @Override
  public void execute() {
    if (InputManager.isKeyJustPressed(SECONDARY_UP)) snapMode = snapMode.nextMode();
    Point cursor = getCursorPosition();
    if (InputManager.isButtonJustPressed(Input.Buttons.LEFT)) {
      if (pendingPointAssignment != null) {
        Consumer<Point> assignment = pendingPointAssignment;
        pendingPointAssignment = null;
        assignment.accept(snapMode.getPosition(cursor));
        return;
      }
      selectNear(cursor);
    }
    if (InputManager.isKeyJustPressed(TERTIARY)) {
      prefabNear(cursor).ifPresent(instance -> delete(instance.name()));
    }
  }

  @Override
  public void render() {
    DungeonLevel level = getLevel();
    for (PrefabInstance source : level.prefabs()) {
      Prefab prefab = PrefabRegistry.require(source.type());
      prefab.renderEditorFeedback(
          prefab.normalize(source),
          new DebugDrawPrefabEditorFeedback(Objects.equals(selectedName, source.name())),
          Objects.equals(selectedName, source.name()));
    }
  }

  @Override
  public void buildDetailsUI(Table content) {
    detailsContent = content;
    content.clearChildren();
    Prefab[] prefabDefinitions = PrefabRegistry.all().toArray(Prefab[]::new);
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
        + "\nWorld click selects the nearest prefab anchor"
        + (pendingPointAssignment == null ? "" : "\nWaiting for world cursor assignment");
  }

  @Override
  public Map<Integer, String> getControls() {
    Map<Integer, String> controls = new LinkedHashMap<>();
    controls.put(Input.Buttons.LEFT, "Select prefab / assign point");
    controls.put(SECONDARY_UP, "Change point snap mode");
    controls.put(TERTIARY, "Delete prefab under cursor");
    return controls;
  }

  private void rebuildDetails() {
    rebuildDetails(true);
  }

  private void rebuildDetails(boolean rebuildSecondary) {
    if (listContent == null) return;
    listContent.clearChildren();
    for (PrefabInstance instance : getLevel().prefabs()) {
      TextButton entry =
          Scene2dElementFactory.createButton(
              instance.name(),
              Objects.equals(selectedName, instance.name()) ? "blue-outline" : "default",
              14);
      entry.addListener(
          new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
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
                    p.displayName(), () -> p.get(current()), value -> setProperty(p, value)))
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
                new NumberSetting(
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
                    value -> value))
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
                    callback -> pendingPointAssignment = callback))
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
    selected()
        .ifPresent(
            source -> {
              try {
                Prefab prefab = PrefabRegistry.require(source.type());
                PrefabInstance replacement = prefab.normalize(property.set(source, value));
                int index = getLevel().prefabs().indexOf(source);
                if (index >= 0) {
                  applyChange(() -> getLevel().replacePrefab(index, replacement));
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
    clearPendingPointAssignment();
    String name = uniqueName(prefab.type());
    PrefabInstance instance = prefab.newInstance(name);
    Point cursor = snapMode.getPosition(getCursorPosition());
    Point anchor = null;
    for (PrefabProperty<?> descriptor : prefab.properties()) {
      if (descriptor.type() == PrefabPropertyType.POINT) {
        PrefabProperty<Point> point = cast(descriptor);
        anchor = point.get(instance);
        break;
      }
    }
    if (anchor != null) {
      instance =
          prefab.translate(instance, Vector2.of(cursor.x() - anchor.x(), cursor.y() - anchor.y()));
    }
    PrefabInstance added = prefab.normalize(instance);
    applyChange(
        () -> {
          getLevel().addPrefab(added);
          selectedName = name;
        });
  }

  private void duplicateSelected() {
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
    clearPendingPointAssignment();
    if (getLevel().prefabs().stream().noneMatch(instance -> instance.name().equals(name))) return;
    applyChange(
        () -> {
          getLevel().removePrefab(name);
          if (Objects.equals(selectedName, name))
            selectedName =
                getLevel().prefabs().stream().map(PrefabInstance::name).findFirst().orElse(null);
        });
  }

  private void rename(PrefabInstance source, String name) {
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

  private Optional<PrefabInstance> selected() {
    return getLevel().prefabs().stream().filter(i -> i.name().equals(selectedName)).findFirst();
  }

  private Optional<PrefabInstance> prefabNear(Point cursor) {
    return getLevel().prefabs().stream()
        .filter(i -> nearestDistance(i, cursor) <= PICK_DISTANCE)
        .min((a, b) -> Float.compare(nearestDistance(a, cursor), nearestDistance(b, cursor)));
  }

  private void selectNear(Point cursor) {
    prefabNear(cursor)
        .ifPresent(
            i -> {
              clearPendingPointAssignment();
              selectedName = i.name();
              requestRebuild();
            });
  }

  private float nearestDistance(PrefabInstance instance, Point cursor) {
    Prefab prefab = PrefabRegistry.require(instance.type());
    float best = Float.MAX_VALUE;
    for (PrefabProperty<?> property : prefab.properties()) {
      if (property.type() == PrefabPropertyType.POINT) {
        PrefabProperty<Point> pointProperty = cast(property);
        Point point = pointProperty.get(instance);
        best = Math.min(best, (float) point.distance(cursor));
      }
    }
    return best;
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
  }

  @SuppressWarnings("unchecked")
  private static <T> PrefabProperty<T> cast(PrefabProperty<?> property) {
    return (PrefabProperty<T>) property;
  }
}
