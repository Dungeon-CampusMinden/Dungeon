package contrib.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import contrib.components.CollideComponent;
import contrib.entities.deco.Deco;
import contrib.entities.deco.DecoFactory;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.utils.FontHelper;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.logging.DungeonLogger;

/**
 * A system to quickly test decorations, adjust colliders and save the resulting collider as
 * Rectangle definition to the clipboard.
 */
public class DecoTestSystem extends System {
  private static final DungeonLogger LOGGER =
      DungeonLogger.getLogger(DecoTestSystem.class.getName());
  private static final int CHANGE_MODE = Input.Keys.UP;

  private static final int MODE_MODIFY_PLUS = Input.Keys.RIGHT;
  private static final int MODE_MODIFY_MINUS = Input.Keys.LEFT;
  private static final int MOVE_DECO = Input.Keys.DOWN;
  private static final int RAPID_FIRE_THRESHOLD = 5;
  private static int rapidFireCounter = 0;

  private Entity testEntity;
  private Deco currentDeco;
  private Mode currentMode = Mode.ChangeDeco;
  private final BitmapFont font;

  /** Constructor for DecoTestSystem. */
  public DecoTestSystem() {
    font = FontHelper.getFont("fonts/Roboto-Bold.ttf", 16);
  }

  /** Executes the system. */
  @Override
  public void execute() {
    drawStatus();

    if (Gdx.input.isKeyJustPressed(CHANGE_MODE)) {
      currentMode = currentMode.next();
      return;
    }

    if (Gdx.input.isKeyJustPressed(MODE_MODIFY_PLUS)) {
      rapidFireCounter = RAPID_FIRE_THRESHOLD;
      executeMode(1);
    }
    if (Gdx.input.isKeyJustPressed(MODE_MODIFY_MINUS)) {
      rapidFireCounter = RAPID_FIRE_THRESHOLD;
      executeMode(-1);
    }

    if (Gdx.input.isKeyPressed(MODE_MODIFY_MINUS) || Gdx.input.isKeyPressed(MODE_MODIFY_PLUS)) {
      if (rapidFireCounter <= 0) {
        rapidFireCounter = RAPID_FIRE_THRESHOLD;
        executeMode(Gdx.input.isKeyPressed(MODE_MODIFY_PLUS) ? 1 : -1);
      }
      rapidFireCounter--;
    }

    if (Gdx.input.isKeyPressed(MOVE_DECO)) {
      if (testEntity == null) createTestEntity();
      testEntity.fetch(PositionComponent.class).ifPresent(pos -> pos.position(getMousePos()));
      PositionSync.syncPosition(testEntity);
    }
  }

  private void drawStatus() {
    String modeText = "Mode: " + currentMode.name();
    modeText += "\nControls: Change Mode (UP), Modify (+RIGHT/-LEFT), Move Deco (DOWN)";

    if (testEntity != null) {
      modeText += String.format("\nCurrent Deco: %s", currentDeco.name());

      if (testEntity.fetch(CollideComponent.class).isPresent()) {
        CollideComponent cc = testEntity.fetch(CollideComponent.class).get();
        modeText +=
            String.format(
                "\nCollider: Rectangle(%.2ff, %.2ff, %.2ff, %.2ff)",
                cc.collider().width(),
                cc.collider().height(),
                cc.collider().offset().x(),
                cc.collider().offset().y());
      }
    } else {
      modeText += "\nNo Deco selected. Change Deco to create one.";
    }

    float offset = 10;
    DebugDrawSystem.drawText(
        font, modeText, new Point(offset, Gdx.graphics.getHeight() - offset - 200));
  }

  /**
   * Change the current decoration by the given change value.
   *
   * @param change the change in index (positive or negative)
   */
  private void executeMode(int change) {
    switch (currentMode) {
      case ChangeDeco -> changeDeco(change);
      case ModifyOffsetX -> modifyOffset(true, change);
      case ModifyOffsetY -> modifyOffset(false, change);
      case ModifySizeWidth -> modifySize(true, change);
      case ModifySizeHeight -> modifySize(false, change);
    }
    if (currentMode != Mode.ChangeDeco) {
      copyColliderInfoToClipboard();
    }
  }

  private void changeDeco(int change) {
    if (testEntity == null) createTestEntity();
    Deco[] decos = Deco.values();
    int currentIndex = currentDeco.ordinal();
    int newIndex = (currentIndex + change + decos.length) % decos.length;
    currentDeco = decos[newIndex];

    Point oldPos =
        testEntity
            .fetch(PositionComponent.class)
            .map(PositionComponent::position)
            .orElse(new Point(0, 0));

    Game.remove(testEntity);
    testEntity = DecoFactory.createDeco(oldPos, currentDeco);
    Game.add(testEntity);
  }

  private void createTestEntity() {
    currentDeco = Deco.values()[0];
    testEntity = DecoFactory.createDeco(getMousePos(), currentDeco);
    Game.add(testEntity);
  }

  private void modifyOffset(boolean x, int change) {
    if (testEntity == null) return;
    testEntity
        .fetch(CollideComponent.class)
        .ifPresent(
            cc -> {
              Vector2 offset = cc.collider().offset();
              float newX = offset.x(), newY = offset.y();
              if (x) {
                newX += change * 0.05f;
              } else {
                newY += change * 0.05f;
              }
              cc.collider().offset(Vector2.of(newX, newY));
            });
  }

  private void modifySize(boolean width, int change) {
    if (testEntity == null) return;
    testEntity
        .fetch(CollideComponent.class)
        .ifPresent(
            cc -> {
              if (width) {
                cc.collider().width(cc.collider().width() + change * 0.05f);
              } else {
                cc.collider().height(cc.collider().height() + change * 0.05f);
              }
            });
  }

  private void copyColliderInfoToClipboard() {
    // Copy string in this format: new Rectangle(<width>, <height>, <xOffset>, <yOffset>)
    if (testEntity == null) return;
    testEntity
        .fetch(CollideComponent.class)
        .ifPresent(
            cc -> {
              String colliderString =
                  String.format(
                      "new Rectangle(%.2ff, %.2ff, %.2ff, %.2ff)",
                      cc.collider().width(),
                      cc.collider().height(),
                      cc.collider().offset().x(),
                      cc.collider().offset().y());
              Gdx.app.getClipboard().setContents(colliderString);
              LOGGER.info("Copied collider info to clipboard: " + colliderString);
            });
  }

  private Point getMousePos() {
    return SkillTools.cursorPositionAsPoint();
  }

  private enum Mode {
    ChangeDeco,
    ModifyOffsetX,
    ModifyOffsetY,
    ModifySizeWidth,
    ModifySizeHeight;

    /**
     * Get the next mode.
     *
     * @return The next mode
     */
    public Mode next() {
      return values()[(this.ordinal() + 1) % values().length];
    }
  }
}
