package contrib.editor.level.mode;

import contrib.components.CollideComponent;
import contrib.editor.level.LevelEditorSystem;
import contrib.entities.deco.Deco;
import contrib.entities.deco.DecoFactory;
import contrib.utils.components.position.PositionSyncUtils;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.input.InputLabel.InputCode;
import core.input.Keys;
import core.platform.Platform;
import core.utils.ClipboardUtil;
import core.utils.InputManager;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.logging.DungeonLogger;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The DecoColliderMode class provides functionality for customizing and editing
 * the collider properties of decorative game elements within a level editor
 * environment.
 *
 * <p>It extends the LevelEditorMode class and offers a variety of
 * operations such as changing the decorative entity, modifying the size and
 * offset of a collider, and positioning the decorative entity in the game world.
 *
 * <p>This mode supports rapid value adjustments and clipboard functionality for
 * storing collider information.
 *
 * <p>Key features:
 * <ul>
 *   <li> Allows switching between multiple collider editing modes, such as modifying
 *   size and offset or changing the decorative entity. </li>
 *   <li> Provides real-time feedback about the current editing state and operations. </li>
 *   <li> Supports rapid-fire adjustment for continuous value changes. </li>
 *   <li> Automatically updates the position and collider properties of the
 *   selected decorative entity. </li>
 *   <li> Integrates with the clipboard utility to facilitate copying collider
 *   configurations for reuse. </li>
 * </ul>
 *
 * <p>Usage context:
 * Designed to be used in a level editor system for game development, where
 * precise and efficient customization of game objects' colliders is necessary.
 *
 * <p>Thread Safety:
 * This class is not thread-safe and is designed for single-threaded use
 * within a level editor environment.
 */
public final class DecoColliderMode extends LevelEditorMode {

  private static final DungeonLogger LOGGER =
    DungeonLogger.getLogger(DecoColliderMode.class);

  private static final int CHANGE_MODE = Keys.UP;
  private static final int MODE_MODIFY_PLUS = Keys.RIGHT;
  private static final int MODE_MODIFY_MINUS = Keys.LEFT;
  private static final int MOVE_DECO = Keys.DOWN;

  private static final int RAPID_FIRE_THRESHOLD = 5;
  private static final float STEP = 0.05f;
  private static final float MIN_SIZE = STEP;

  private Entity testEntity;
  private Deco currentDeco;
  private ColliderEditMode currentMode = ColliderEditMode.ChangeDeco;
  private int rapidFireCounter = 0;

  /**
   * Constructs a new instance of the DecoColliderMode class, an editing mode
   * for managing decoration colliders in the Level Editor system.
   *
   * @param system the LevelEditorSystem instance to which this mode belongs.
   */
  public DecoColliderMode(LevelEditorSystem system) {
    super(system, "Deco Collider Mode");
  }

  @Override
  protected void execute() {
    if (InputManager.isKeyJustPressed(CHANGE_MODE)) {
      currentMode = currentMode.next();
      system().showModeFeedback(
        "Collider mode: " + currentMode.displayName(),
        Color.WHITE);
      return;
    }

    if (InputManager.isKeyJustPressed(MODE_MODIFY_PLUS)) {
      rapidFireCounter = RAPID_FIRE_THRESHOLD;
      executeMode(1);
    }

    if (InputManager.isKeyJustPressed(MODE_MODIFY_MINUS)) {
      rapidFireCounter = RAPID_FIRE_THRESHOLD;
      executeMode(-1);
    }

    if (InputManager.isKeyPressed(MODE_MODIFY_MINUS)
      || InputManager.isKeyPressed(MODE_MODIFY_PLUS)) {
      if (rapidFireCounter <= 0) {
        rapidFireCounter = RAPID_FIRE_THRESHOLD;
        executeMode(InputManager.isKeyPressed(MODE_MODIFY_PLUS) ? 1 : -1);
      }
      rapidFireCounter--;
    }

    if (InputManager.isKeyPressed(MOVE_DECO)) {
      ensureTestEntity();
      testEntity
        .fetch(PositionComponent.class)
        .ifPresent(pc -> pc.position(cursorWorld()));
      PositionSyncUtils.syncPosition(testEntity);
    }
  }

  @Override
  public void onEnter() {
    ensureTestEntity();
    system().showModeFeedback(
      "Entered deco collider tuning mode",
      Color.WHITE);
  }

  @Override
  public void onExit() {
    removeTestEntity();
  }

  @Override
  protected List<String> getStatusLines() {
    List<String> lines = new ArrayList<>();

    lines.add("Edit mode: " + currentMode.displayName());

    Deco shownDeco = currentDeco != null ? currentDeco : Deco.values()[0];
    lines.add("Current deco: " + shownDeco.name());

    if (testEntity == null) {
      lines.add("Test entity: <none>");
      lines.add("Collider: <none>");
    } else {
      lines.add("Test entity: active");
      lines.add("Collider: " + colliderString().orElse("<missing collide component>"));
    }

    lines.add("Clipboard copy: automatic after collider edits");
    return lines;
  }

  @Override
  protected Map<InputCode, String> getControls() {
    Map<InputCode, String> controls = new LinkedHashMap<>();
    controls.put(key(CHANGE_MODE), "Next collider edit mode");
    controls.put(key(MODE_MODIFY_MINUS), "Modify current value -");
    controls.put(key(MODE_MODIFY_PLUS), "Modify current value +");
    controls.put(key(MOVE_DECO), "Move test deco to cursor");
    return controls;
  }

  private void executeMode(int change) {
    switch (currentMode) {
      case ChangeDeco -> changeDeco(change);
      case ModifyOffsetX -> modifyOffset(true, change);
      case ModifyOffsetY -> modifyOffset(false, change);
      case ModifySizeWidth -> modifySize(true, change);
      case ModifySizeHeight -> modifySize(false, change);
    }

    if (currentMode != ColliderEditMode.ChangeDeco) {
      copyColliderInfoToClipboard();
    }
  }

  private void changeDeco(int change) {
    ensureTestEntity();

    Deco[] values = Deco.values();
    int currentIndex = currentDeco == null ? 0 : currentDeco.ordinal();
    int newIndex = Math.floorMod(currentIndex + change, values.length);
    currentDeco = values[newIndex];

    Point oldPos =
      testEntity
        .fetch(PositionComponent.class)
        .map(PositionComponent::position)
        .orElse(cursorWorld());

    Game.remove(testEntity);
    testEntity = DecoFactory.createDeco(oldPos, currentDeco);
    Game.add(testEntity);

    system().showModeFeedback("Selected deco: " + currentDeco.name(), Color.WHITE);
  }

  private void modifyOffset(boolean xAxis, int change) {
    ensureTestEntity();

    Optional<CollideComponent> cc = testEntity.fetch(CollideComponent.class);
    if (cc.isEmpty()) {
      system().showModeFeedback("Selected deco has no collider", Color.YELLOW);
      return;
    }

    Vector2 offset = cc.get().collider().offset();
    float newX = offset.x();
    float newY = offset.y();

    if (xAxis) {
      newX += change * STEP;
    } else {
      newY += change * STEP;
    }

    cc.get().collider().offset(Vector2.of(newX, newY));
    system().showModeFeedback(
      (xAxis ? "Collider offset X" : "Collider offset Y") + " updated",
      Color.WHITE);
  }

  private void modifySize(boolean widthAxis, int change) {
    ensureTestEntity();

    Optional<CollideComponent> cc = testEntity.fetch(CollideComponent.class);
    if (cc.isEmpty()) {
      system().showModeFeedback("Selected deco has no collider", Color.YELLOW);
      return;
    }

    if (widthAxis) {
      float newWidth = Math.max(MIN_SIZE, cc.get().collider().width() + change * STEP);
      cc.get().collider().width(newWidth);
      system().showModeFeedback("Collider width updated", Color.WHITE);
    } else {
      float newHeight = Math.max(MIN_SIZE, cc.get().collider().height() + change * STEP);
      cc.get().collider().height(newHeight);
      system().showModeFeedback("Collider height updated", Color.WHITE);
    }
  }

  private void copyColliderInfoToClipboard() {
    Optional<String> colliderString = colliderString();
    if (colliderString.isEmpty()) {
      system().showModeFeedback("Nothing to copy: selected deco has no collider", Color.YELLOW);
      return;
    }

    ClipboardUtil.copyToClipboard(colliderString.get());
    LOGGER.info("Copied collider info to clipboard: " + colliderString.get());
    system().showModeFeedback("Copied collider Rectangle to clipboard", new Color(120, 220, 120));
  }

  private Optional<String> colliderString() {
    if (testEntity == null) {
      return Optional.empty();
    }

    return testEntity
      .fetch(CollideComponent.class)
      .map(
        cc ->
          String.format(
            "new Rectangle(%.2ff, %.2ff, %.2ff, %.2ff)",
            cc.collider().width(),
            cc.collider().height(),
            cc.collider().offset().x(),
            cc.collider().offset().y()));
  }

  private void ensureTestEntity() {
    if (testEntity != null) {
      return;
    }

    createTestEntity();
  }

  private void createTestEntity() {
    if (currentDeco == null) {
      currentDeco = Deco.values()[0];
    }

    testEntity = DecoFactory.createDeco(cursorWorld(), currentDeco);
    Game.add(testEntity);
  }

  private void removeTestEntity() {
    if (testEntity == null) {
      return;
    }

    Game.remove(testEntity);
    testEntity = null;
  }

  private Point cursorWorld() {
    return Platform.cursor().world();
  }

  private enum ColliderEditMode {
    ChangeDeco("Change deco"),
    ModifyOffsetX("Modify offset X"),
    ModifyOffsetY("Modify offset Y"),
    ModifySizeWidth("Modify width"),
    ModifySizeHeight("Modify height");

    private final String displayName;

    ColliderEditMode(String displayName) {
      this.displayName = displayName;
    }

    public String displayName() {
      return displayName;
    }

    public ColliderEditMode next() {
      return values()[(this.ordinal() + 1) % values().length];
    }
  }
}
