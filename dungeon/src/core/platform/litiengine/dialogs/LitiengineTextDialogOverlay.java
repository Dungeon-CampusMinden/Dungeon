package core.platform.litiengine.dialogs;

import contrib.hud.dialogs.DialogCallbackResolver;
import contrib.hud.dialogs.DialogContextKeys;
import core.Game;
import core.input.MouseButtons;
import core.platform.litiengine.ui.LitiengineUiOverlay;
import core.ui.StageHandle;
import core.utils.InputManager;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * A minimal real text dialog for the LITIENGINE backend.
 *
 * <p>Rendered as a custom overlay via the LITIENGINE Graphics2D render bridge.
 */
final class LitiengineTextDialogOverlay implements LitiengineUiOverlay {

  private static final int DEFAULT_WIDTH = 560;
  private static final int DEFAULT_HEIGHT = 260;
  private static final int BUTTON_GAP = 16;

  private final String title;
  private final String text;
  private final String confirmLabel;
  private final String cancelLabel;
  private final String[] additionalButtons;
  private final String dialogId;

  private int x;
  private int y;
  private int width = DEFAULT_WIDTH;
  private int height = DEFAULT_HEIGHT;
  private boolean visible = true;

  private int pressedButtonIndex = -1;
  private boolean leftButtonDownLastFrame = false;

  LitiengineTextDialogOverlay(
    String title,
    String text,
    String confirmLabel,
    String cancelLabel,
    String[] additionalButtons,
    String dialogId) {
    this.title = title;
    this.text = text;
    this.confirmLabel = confirmLabel;
    this.cancelLabel = cancelLabel;
    this.additionalButtons = additionalButtons != null ? additionalButtons : new String[] {};
    this.dialogId = dialogId;
  }

  @Override
  public void render(Graphics2D g) {
    if (!visible) {
      return;
    }

    handleInput();

    LitiengineDialogOverlaySupport.RenderState state =
      LitiengineDialogOverlaySupport.beginDialog(g);

    try {
      int textY = LitiengineDialogOverlaySupport.drawFrameAndTitle(g, x, y, width, height, title);

      LitiengineDialogOverlaySupport.drawWrappedText(
        g, text, x + LitiengineDialogOverlaySupport.PADDING, textY,
        width - 2 * LitiengineDialogOverlaySupport.PADDING);

      List<String> labels = buttonLabels();
      List<Rectangle> bounds = buttonBounds(labels.size());

      for (int i = 0; i < labels.size(); i++) {
        LitiengineDialogOverlaySupport.drawButton(
          g, bounds.get(i), labels.get(i), pressedButtonIndex == i);
      }
    } finally {
      LitiengineDialogOverlaySupport.finishDialog(g, state);
    }
  }

  private void handleInput() {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      pressedButtonIndex = -1;
      leftButtonDownLastFrame = false;
      return;
    }

    int mouseX = stage.mouseX();
    int mouseY = stage.mouseY();

    List<String> labels = buttonLabels();
    List<Rectangle> bounds = buttonBounds(labels.size());
    boolean leftButtonDown = InputManager.isButtonPressed(MouseButtons.LEFT);

    if (leftButtonDown && !leftButtonDownLastFrame) {
      pressedButtonIndex = buttonIndexAt(mouseX, mouseY, bounds);
    }

    if (!leftButtonDown && leftButtonDownLastFrame) {
      int releasedIndex = buttonIndexAt(mouseX, mouseY, bounds);
      int previouslyPressed = pressedButtonIndex;
      pressedButtonIndex = -1;

      if (previouslyPressed >= 0 && previouslyPressed == releasedIndex) {
        triggerCallback(labels.get(previouslyPressed));
      }
    }

    leftButtonDownLastFrame = leftButtonDown;
  }

  private int buttonIndexAt(int mouseX, int mouseY, List<Rectangle> bounds) {
    for (int i = 0; i < bounds.size(); i++) {
      if (bounds.get(i).contains(mouseX, mouseY)) {
        return i;
      }
    }
    return -1;
  }

  private void triggerCallback(String label) {
    if (label.equals(confirmLabel)) {
      DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_CONFIRM)
        .accept(null);
      return;
    }

    if (label.equals(cancelLabel)) {
      DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_CANCEL)
        .accept(null);
      return;
    }

    DialogCallbackResolver.createButtonCallback(dialogId, "on" + label).accept(null);
  }

  private List<String> buttonLabels() {
    List<String> labels = new ArrayList<>();
    labels.add(confirmLabel);
    if (cancelLabel != null) {
      labels.add(cancelLabel);
    }
    for (String extra : additionalButtons) {
      labels.add(extra);
    }
    return labels;
  }

  private List<Rectangle> buttonBounds(int count) {
    return LitiengineDialogOverlaySupport.centeredButtonRow(
      x, y, width, height, count, BUTTON_GAP);
  }

  @Override
  public int x() {
    return x;
  }

  @Override
  public void x(int x) {
    this.x = x;
  }

  @Override
  public int y() {
    return y;
  }

  @Override
  public void y(int y) {
    this.y = y;
  }

  @Override
  public int width() {
    return width;
  }

  @Override
  public void width(int width) {
    this.width = width;
  }

  @Override
  public int height() {
    return height;
  }

  @Override
  public void height(int height) {
    this.height = height;
  }

  @Override
  public boolean visible() {
    return visible;
  }

  @Override
  public void visible(boolean visible) {
    this.visible = visible;
  }
}
