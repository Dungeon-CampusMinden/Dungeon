package contrib.hud.dialogs.overlays;

import contrib.hud.dialogs.DialogCallbackResolver;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.elements.Button;
import contrib.hud.renderers.DialogFrameRenderer;
import core.Game;
import core.input.Keys;
import core.input.MouseButtons;
import core.ui.overlay.UiOverlay;
import core.ui.StageHandle;
import core.utils.InputManager;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * A UI overlay component that represents a text dialog with customizable buttons and callbacks.
 *
 * <p>The TextDialogOverlay class provides a dialog interface that can display a title, main text,
 * and multiple buttons for user interactions.
 *
 * <p>This overlay is rendered on top of the game scene and allows for custom positioning, sizing, and visibility control.
 *
 * <p>Key features of this class include:
 * <ul>
 *   <li>A structured frame with a title and main text area.</li>
 *   <li>Buttons for standard actions (confirm, cancel) and optional additional buttons.</li>
 *   <li>Support for triggering callback actions based on button interactions.</li>
 *   <li>Input handling for keyboard and mouse interactions.</li>
 * </ul>
 */
final class TextDialogOverlay implements UiOverlay {

  private static final int DEFAULT_WIDTH = 560;
  private static final int DEFAULT_HEIGHT = 260;
  private static final int BUTTON_GAP = 16;

  private final String title;
  private final String text;
  private final String confirmLabel;
  private final String cancelLabel;
  private final String[] additionalButtons;
  private final String dialogId;
  private final List<Button> actionButtons = new ArrayList<>();

  private int x;
  private int y;
  private int width = DEFAULT_WIDTH;
  private int height = DEFAULT_HEIGHT;
  private boolean visible = true;

  TextDialogOverlay(
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

    initButtons();
  }

  private void initButtons() {
    actionButtons.clear();

    for (String label : buttonLabels()) {
      Button button = new Button(0, 0, 1, 1);
      button.onClick(ignored -> triggerCallback(label));
      actionButtons.add(button);
    }
  }

  @Override
  public void render(Graphics2D g) {
    if (!visible) {
      return;
    }

    List<String> labels = buttonLabels();
    List<Rectangle> bounds = buttonBounds(labels.size());
    syncButtonBounds(bounds);

    handleInput();

    DialogFrameRenderer.RenderState state =
      DialogFrameRenderer.beginDialog(g);

    try {
      int textY = DialogFrameRenderer.drawFrameAndTitle(g, x, y, width, height, title);

      DialogFrameRenderer.drawWrappedText(
        g,
        text,
        x + DialogFrameRenderer.PADDING,
        textY,
        width - 2 * DialogFrameRenderer.PADDING);

      for (int i = 0; i < labels.size() && i < actionButtons.size(); i++) {
        Button button = actionButtons.get(i);
        Rectangle buttonBounds = new Rectangle(button.x(), button.y(), button.width(), button.height());

        DialogFrameRenderer.drawButton(g, buttonBounds, labels.get(i), false);
      }
    } finally {
      DialogFrameRenderer.finishDialog(g, state);
    }
  }

  private void handleInput() {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      actionButtons.forEach(Button::resetInteractionState);
      return;
    }

    if (InputManager.isKeyJustPressed(Keys.ENTER)) {
      onConfirm();
      return;
    }

    if (InputManager.isKeyJustPressed(Keys.ESCAPE)) {
      onCancel();
      return;
    }

    int mouseX = stage.mouseX();
    int mouseY = stage.mouseY();
    boolean leftButtonDown = InputManager.isButtonPressed(MouseButtons.LEFT);

    for (Button button : actionButtons) {
      button.update(mouseX, mouseY, leftButtonDown);
    }
  }

  private void syncButtonBounds(List<Rectangle> bounds) {
    for (int i = 0; i < bounds.size() && i < actionButtons.size(); i++) {
      Rectangle rect = bounds.get(i);
      Button button = actionButtons.get(i);
      button.x(rect.x);
      button.y(rect.y);
      button.width(rect.width);
      button.height(rect.height);
    }
  }

  private void triggerCallback(String label) {
    if (label.equals(confirmLabel)) {
      onConfirm();
      return;
    }

    if (label.equals(cancelLabel)) {
      onCancel();
      return;
    }

    DialogCallbackResolver.createButtonCallback(dialogId, "on" + label).accept(null);
  }

  private void onConfirm() {
    DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_CONFIRM).accept(null);
  }

  private void onCancel() {
    DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_CANCEL).accept(null);
  }

  private List<String> buttonLabels() {
    List<String> labels = new ArrayList<>();
    labels.add(confirmLabel);

    if (cancelLabel != null && !cancelLabel.isBlank()) {
      labels.add(cancelLabel);
    }

    for (String additionalButton : additionalButtons) {
      if (additionalButton != null && !additionalButton.isBlank()) {
        labels.add(additionalButton);
      }
    }

    return labels;
  }

  private List<Rectangle> buttonBounds(int buttonCount) {
    return DialogFrameRenderer.centeredButtonRow(
      x, y, width, height, buttonCount, BUTTON_GAP);
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
