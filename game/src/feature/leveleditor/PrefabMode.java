package feature.leveleditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.level.DungeonLevel;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.systems.input.InputManager;
import engine.utils.Point;
import engine.utils.Scene2dElementFactory;
import engine.utils.Vector2;
import feature.components.CollideComponent;
import feature.hud.dialogs.DialogFactory;
import feature.leveleditor.ui.BooleanSetting;
import feature.leveleditor.ui.FiniteFloatSetting;
import feature.leveleditor.ui.ModeDetailsPanel;
import feature.leveleditor.ui.NumberSetting;
import feature.leveleditor.ui.PointSetting;
import feature.leveleditor.ui.SelectSetting;
import feature.leveleditor.ui.StringSetting;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabPropertyType;
import feature.prefabs.PrefabRegistry;
import feature.prefabs.PrefabSide;
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

  private final List<Entity> previewEntities = new ArrayList<>();
  private String selectedName;
  private Prefab selectedPrefab;
  private Consumer<Point> pendingPointAssignment;
  private SnapMode snapMode = SnapMode.OnGrid;
  private Table detailsContent;
  private Table listContent;
  private Table propertyContent;
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
  public void onEnter() {
    clearPendingPointAssignment();
    selectedName = getLevel().prefabs().stream().map(PrefabInstance::name).findFirst().orElse(null);
    rebuildPreview();
  }

  @Override
  public void onExit() {
    clearPendingPointAssignment();
    removePreviews();
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
    prefabTypeSetting =
        new SelectSetting<>(
            "Prefab Type",
            prefabDefinitions,
            () -> selectedPrefab,
            prefab -> selectedPrefab = prefab,
            prefab -> prefab.displayName() + " (" + prefab.type() + ")");
    content.add(prefabTypeSetting).growX().row();

    listContent = new Table();
    listContent.top().defaults().growX().pad(2f);
    ScrollPane list = new ScrollPane(listContent);
    list.setFadeScrollBars(false);
    content
        .add(Scene2dElementFactory.createLabel("Prefab Instances", 16, ModeDetailsPanel.TEXT_COLOR))
        .growX()
        .left()
        .row();
    content.add(list).growX().height(150f).row();

    Table actions = new Table();
    TextButton add = Scene2dElementFactory.createButton("Add Prefab", "default", 16);
    add.addListener(
        new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            addSelectedPrefab();
          }
        });
    TextButton rename = Scene2dElementFactory.createButton("Rename", "default", 16);
    rename.addListener(
        new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            selected().ifPresent(i -> showRenameDialog(i));
          }
        });
    TextButton duplicate = Scene2dElementFactory.createButton("Duplicate", "default", 16);
    duplicate.addListener(
        new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            duplicateSelected();
          }
        });
    TextButton delete = Scene2dElementFactory.createButton("Delete", "red-outline", 16);
    delete.addListener(
        new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            selected().ifPresent(i -> delete(i.name()));
          }
        });
    actions.add(add).growX();
    actions.add(rename).growX();
    actions.row();
    actions.add(duplicate).growX();
    actions.add(delete).growX();
    content.add(actions).growX().padTop(4f).row();
    propertyContent = new Table();
    propertyContent.top().defaults().growX().padTop(5f);
    content.add(propertyContent).growX().row();
    rebuildDetails();
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
    if (listContent == null || propertyContent == null) return;
    listContent.clearChildren();
    for (PrefabInstance instance : getLevel().prefabs()) {
      Prefab prefab = PrefabRegistry.require(instance.type());
      TextButton entry =
          Scene2dElementFactory.createButton(
              instance.name() + " (" + prefab.displayName() + ")",
              Objects.equals(selectedName, instance.name()) ? "blue-outline" : "default",
              14);
      entry.addListener(
          new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              clearPendingPointAssignment();
              selectedName = instance.name();
              rebuildPending = true;
              rebuildPreview();
            }
          });
      listContent.add(entry).row();
    }
    propertyContent.clearChildren();
    selected()
        .ifPresent(
            instance -> {
              Prefab prefab = PrefabRegistry.require(instance.type());
              propertyContent
                  .add(
                      Scene2dElementFactory.createLabel(
                          prefab.displayName(), 18, ModeDetailsPanel.TEXT_COLOR))
                  .left()
                  .row();
              for (PrefabProperty<?> property : prefab.properties())
                addProperty(property, instance);
            });
  }

  private void addProperty(PrefabProperty<?> property, PrefabInstance source) {
    PrefabPropertyType type = property.type();
    switch (type) {
      case STRING -> {
        PrefabProperty<String> p = cast(property);
        propertyContent
            .add(
                new StringSetting(
                    p.displayName(), () -> p.get(current()), value -> setProperty(p, value)))
            .row();
      }
      case INTEGER -> {
        PrefabProperty<Integer> p = cast(property);
        int min = p.minimum().orElse(Integer.MIN_VALUE).intValue();
        int max = p.maximum().orElse(Integer.MAX_VALUE).intValue();
        propertyContent
            .add(
                new NumberSetting(
                    p.displayName(),
                    min,
                    max,
                    () -> p.get(current()),
                    value -> setProperty(p, value)))
            .row();
      }
      case FLOAT -> {
        PrefabProperty<Float> p = cast(property);
        float min = p.minimum().orElse(-Float.MAX_VALUE).floatValue();
        float max = p.maximum().orElse(Float.MAX_VALUE).floatValue();
        propertyContent
            .add(
                new FiniteFloatSetting(
                    p.displayName(),
                    min,
                    max,
                    () -> p.get(current()),
                    value -> setProperty(p, value)))
            .row();
      }
      case BOOLEAN -> {
        PrefabProperty<Boolean> p = cast(property);
        propertyContent
            .add(
                new BooleanSetting(
                    p.displayName(), () -> p.get(current()), value -> setProperty(p, value)))
            .row();
      }
      case ENUM -> {
        PrefabProperty<String> p = cast(property);
        String[] values = p.choices().toArray(String[]::new);
        propertyContent
            .add(
                new SelectSetting<>(
                    p.displayName(),
                    values,
                    () -> p.get(current()),
                    value -> setProperty(p, value),
                    value -> value))
            .row();
      }
      case POINT -> {
        PrefabProperty<Point> p = cast(property);
        propertyContent
            .add(
                new PointSetting(
                    p.displayName(),
                    () -> p.get(current()),
                    value -> setProperty(p, value),
                    callback -> pendingPointAssignment = callback))
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
                  getLevel().replacePrefab(index, replacement);
                  levelChanged();
                  rebuildPreview();
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
    getLevel().addPrefab(prefab.normalize(instance));
    selectedName = name;
    levelChanged();
    requestRebuild();
    rebuildPreview();
  }

  private void duplicateSelected() {
    clearPendingPointAssignment();
    selected()
        .ifPresent(
            source -> {
              String name = uniqueName(source.name());
              getLevel().addPrefab(source.withName(name));
              selectedName = name;
              levelChanged();
              requestRebuild();
              rebuildPreview();
            });
  }

  private void delete(String name) {
    clearPendingPointAssignment();
    if (getLevel().removePrefab(name)) {
      if (Objects.equals(selectedName, name))
        selectedName =
            getLevel().prefabs().stream().map(PrefabInstance::name).findFirst().orElse(null);
      levelChanged();
      requestRebuild();
      rebuildPreview();
    }
  }

  private void showRenameDialog(PrefabInstance source) {
    DialogFactory.showInputDialog(
        "",
        "Rename Prefab",
        source.name(),
        "Unique name",
        "Rename",
        "Cancel",
        false,
        payload -> {
          if (payload instanceof DialogResponseMessage.StringValue(String value))
            rename(source, value);
        },
        () -> {});
  }

  private void rename(PrefabInstance source, String name) {
    clearPendingPointAssignment();
    if (name == null
        || name.isBlank()
        || getLevel().prefabs().stream()
            .anyMatch(i -> !i.name().equals(source.name()) && i.name().equals(name))) {
      LevelEditorSystem.showFeedback("Prefab name must be non-empty and unique.", Color.YELLOW);
      return;
    }
    int index = getLevel().prefabs().indexOf(source);
    if (index >= 0) {
      getLevel().replacePrefab(index, source.withName(name));
      selectedName = name;
      levelChanged();
      requestRebuild();
      rebuildPreview();
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
              rebuildPreview();
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

  private void rebuildPreview() {
    removePreviews();
    selected()
        .ifPresent(
            source -> {
              List<Entity> created = null;
              try {
                Prefab prefab = PrefabRegistry.require(source.type());
                created =
                    prefab.createEditorPreview(
                        new PrefabCreationContext(getLevel(), PrefabSide.CLIENT),
                        prefab.normalize(source));
                List<Entity> added = new ArrayList<>();
                for (Entity entity : created) {
                  entity
                      .fetch(CollideComponent.class)
                      .ifPresent(collision -> collision.isSolid(false));
                  entity
                      .fetch(DrawComponent.class)
                      .ifPresent(draw -> draw.tintColor(Color.rgba8888(1f, 1f, 1f, 0.45f)));
                  Game.add(entity);
                  added.add(entity);
                }
                previewEntities.addAll(added);
              } catch (RuntimeException exception) {
                removePreviewEntities(created);
                LevelEditorSystem.showFeedback(
                    "Preview unavailable: " + exception.getMessage(), Color.YELLOW);
              }
            });
  }

  private void removePreviews() {
    removePreviewEntities(previewEntities);
    previewEntities.clear();
  }

  private void removePreviewEntities(Iterable<Entity> entities) {
    if (entities == null) return;
    for (Entity entity : entities) {
      if (entity == null) continue;
      Game.findEntityById(entity.id())
          .filter(existing -> existing == entity)
          .ifPresent(Game::remove);
    }
  }

  private void clearPendingPointAssignment() {
    pendingPointAssignment = null;
  }

  @SuppressWarnings("unchecked")
  private static <T> PrefabProperty<T> cast(PrefabProperty<?> property) {
    return (PrefabProperty<T>) property;
  }
}
