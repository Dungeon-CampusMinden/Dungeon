package contrib.editor.level.mode.deco;

import contrib.components.CollideComponent;
import contrib.editor.level.LevelEditorSystem;
import contrib.entities.deco.DecoFactory;
import contrib.utils.components.collide.ColliderSync;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.utils.ClipboardUtil;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.logging.DungeonLogger;
import java.awt.Color;
import java.util.Optional;

/**
 * The DecoColliderController class provides functionality for managing and fine-tuning collider
 * properties of decorative game entities within the Level Editor system.
 *
 * <p>It allows toggling edit modes, adjusting collider sizes, offsets, and swapping decorative
 * entities while providing visual feedback and interaction with the system.
 *
 * <p>This class handles temporary "test" entities for collider manipulation, integration with the
 * clipboard for saving collider information, and supports contextual feedback to the user.
 */
final class DecoColliderController {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(DecoColliderController.class);

  private static final float STEP = 0.05f;
  private static final float MIN_SIZE = STEP;

  private final LevelEditorSystem system;
  private final DecoColliderState state = new DecoColliderState();

  private Entity testEntity;

  DecoColliderController(LevelEditorSystem system) {
    this.system = system;
  }

  DecoColliderState state() {
    return state;
  }

  boolean hasTestEntity() {
    return testEntity != null;
  }

  void onEnter(Point cursorWorld) {
    ensureTestEntity(cursorWorld);
    system.showModeFeedback("Entered deco collider tuning mode", Color.WHITE);
  }

  void onExit() {
    removeTestEntity();
  }

  void cycleEditMode() {
    state.cycleEditMode();
    system.showModeFeedback("Collider mode: " + state.editMode().displayName(), Color.WHITE);
  }

  void applyCurrentEdit(int change, Point cursorWorld) {
    switch (state.editMode()) {
      case CHANGE_DECO -> changeDeco(change, cursorWorld);
      case MODIFY_OFFSET_X -> modifyOffset(true, change, cursorWorld);
      case MODIFY_OFFSET_Y -> modifyOffset(false, change, cursorWorld);
      case MODIFY_WIDTH -> modifySize(true, change, cursorWorld);
      case MODIFY_HEIGHT -> modifySize(false, change, cursorWorld);
    }

    if (state.editMode().copiesColliderToClipboard()) {
      copyColliderInfoToClipboard();
    }
  }

  void moveTestEntity(Point cursorWorld) {
    ensureTestEntity(cursorWorld);
    testEntity.fetch(PositionComponent.class).ifPresent(pc -> pc.position(cursorWorld));
    ColliderSync.sync(testEntity);
  }

  Optional<String> colliderString() {
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

  private void changeDeco(int change, Point cursorWorld) {
    ensureTestEntity(cursorWorld);
    Point currentPosition =
        testEntity
            .fetch(PositionComponent.class)
            .map(PositionComponent::position)
            .orElse(cursorWorld);

    state.shiftSelectedDeco(change);

    Game.remove(testEntity);
    testEntity = DecoFactory.createDeco(currentPosition, state.selectedDeco());
    Game.add(testEntity);

    system.showModeFeedback("Selected deco: " + state.selectedDeco().name(), Color.WHITE);
  }

  private void modifyOffset(boolean xAxis, int change, Point cursorWorld) {
    Optional<CollideComponent> collideComponent = testCollider(cursorWorld);
    if (collideComponent.isEmpty()) {
      return;
    }

    Vector2 offset = collideComponent.get().collider().offset();
    float newX = offset.x();
    float newY = offset.y();

    if (xAxis) {
      newX += change * STEP;
    } else {
      newY += change * STEP;
    }

    collideComponent.get().collider().offset(Vector2.of(newX, newY));
    system.showModeFeedback(
        (xAxis ? "Collider offset X" : "Collider offset Y") + " updated", Color.WHITE);
  }

  private void modifySize(boolean widthAxis, int change, Point cursorWorld) {
    Optional<CollideComponent> collideComponent = testCollider(cursorWorld);
    if (collideComponent.isEmpty()) {
      return;
    }

    if (widthAxis) {
      float newWidth =
          Math.max(MIN_SIZE, collideComponent.get().collider().width() + change * STEP);
      collideComponent.get().collider().width(newWidth);
      system.showModeFeedback("Collider width updated", Color.WHITE);
    } else {
      float newHeight =
          Math.max(MIN_SIZE, collideComponent.get().collider().height() + change * STEP);
      collideComponent.get().collider().height(newHeight);
      system.showModeFeedback("Collider height updated", Color.WHITE);
    }
  }

  private Optional<CollideComponent> testCollider(Point cursorWorld) {
    ensureTestEntity(cursorWorld);

    Optional<CollideComponent> collideComponent = testEntity.fetch(CollideComponent.class);
    if (collideComponent.isEmpty()) {
      system.showModeFeedback("Selected deco has no collider", Color.YELLOW);
    }
    return collideComponent;
  }

  private void copyColliderInfoToClipboard() {
    Optional<String> colliderString = colliderString();
    if (colliderString.isEmpty()) {
      system.showModeFeedback("Nothing to copy: selected deco has no collider", Color.YELLOW);
      return;
    }

    ClipboardUtil.copyToClipboard(colliderString.get());
    LOGGER.info("Copied collider info to clipboard: " + colliderString.get());
    system.showModeFeedback("Copied collider Rectangle to clipboard", new Color(120, 220, 120));
  }

  private void ensureTestEntity(Point cursorWorld) {
    if (testEntity != null) {
      return;
    }

    testEntity = DecoFactory.createDeco(cursorWorld, state.selectedDeco());
    Game.add(testEntity);
  }

  private void removeTestEntity() {
    if (testEntity == null) {
      return;
    }

    Game.remove(testEntity);
    testEntity = null;
  }
}
