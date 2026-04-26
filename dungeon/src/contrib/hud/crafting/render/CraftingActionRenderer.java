package contrib.hud.crafting.render;

import contrib.hud.crafting.CraftingDialogAction;
import contrib.hud.crafting.CraftingDialogController;
import contrib.hud.dialogs.DialogCallbackResolver;
import contrib.hud.elements.ImageButton;
import core.Game;
import core.game.render.image.ImageFrameResolver;
import core.input.MouseButtons;
import core.ui.StageHandle;
import core.utils.InputManager;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Renders and manages the crafting action buttons within the crafting dialog.
 *
 * <p>This class is responsible for:
 * <ul>
 *   <li>Managing crafting action buttons and their visual states</li>
 *   <li>Calculating button bounds based on panel dimensions</li>
 *   <li>Rendering buttons with appropriate visual feedback (hover, pressed states)</li>
 *   <li>Detecting clicks on action buttons and triggering corresponding callbacks</li>
 * </ul>
 *
 * @see CraftingDialogAction
 * @see CraftingDialogController
 */
public final class CraftingActionRenderer {

  private static final Color ACTION_BOX_FILL = new Color(210, 210, 210, 235);
  private static final Color ACTION_BOX_HOVER_FILL = new Color(232, 232, 232, 245);
  private static final Color ACTION_BOX_PRESSED_FILL = new Color(180, 180, 180, 245);
  private static final Color ACTION_BOX_BORDER = new Color(70, 70, 70, 220);
  private static final Color ACTION_BOX_HOVER_BORDER = new Color(20, 20, 20, 230);

  private static final int ACTION_BOX_ARC = 10;
  private static final int BUTTON_ICON_PADDING = 6;

  private final String dialogId;
  private final CraftingDialogController controller;
  private final Map<CraftingDialogAction, ImageButton> actionButtons =
      new EnumMap<>(CraftingDialogAction.class);

  /**
   * Constructs a new CraftingActionRenderer.
   *
   * @param dialogId the identifier of the crafting dialog
   * @param controller the controller managing the crafting dialog state and callbacks
   */
  public CraftingActionRenderer(String dialogId, CraftingDialogController controller) {
    this.dialogId = dialogId;
    this.controller = controller;

    for (CraftingDialogAction action : CraftingDialogAction.values()) {
      ImageButton button =
          new ImageButton(new Animation(new SimpleIPath(action.iconPath())), 0, 0, 1, 1);

      button.onClick(ignored -> trigger(action));

      actionButtons.put(action, button);
    }
  }

  /**
   * Calculates the bounds for each crafting action button based on the panel dimensions.
   *
   * <p>Uses relative positioning values from each CraftingDialogAction to determine button
   * positions and sizes within the panel.
   *
   * @param panelBounds the bounds of the parent panel
   * @return a map containing the calculated bounds for each crafting action
   */
  public Map<CraftingDialogAction, Rectangle> buttonBounds(Rectangle panelBounds) {
    Map<CraftingDialogAction, Rectangle> bounds = new EnumMap<>(CraftingDialogAction.class);

    for (CraftingDialogAction action : CraftingDialogAction.values()) {
      int buttonX = panelBounds.x + Math.round(panelBounds.width * action.relativeX());
      int bottomY = Math.round(panelBounds.height * action.relativeY());
      int buttonWidth = Math.round(panelBounds.width * action.relativeWidth());
      int buttonHeight = Math.round(panelBounds.height * action.relativeHeight());

      int buttonY = panelBounds.y + panelBounds.height - bottomY - buttonHeight;

      bounds.put(action, new Rectangle(buttonX, buttonY, buttonWidth, buttonHeight));
    }

    return bounds;
  }

  /**
   * Synchronizes the visual button components with the provided bounds.
   *
   * <p>Updates the position, width, and height of each action button based on the calculated
   * bounds. This should be called whenever the panel layout changes.
   *
   * @param buttonBounds a map containing the target bounds for each crafting action button
   */
  public void syncButtonBounds(Map<CraftingDialogAction, Rectangle> buttonBounds) {
    for (CraftingDialogAction action : CraftingDialogAction.values()) {
      ImageButton button = actionButtons.get(action);
      Rectangle bounds = buttonBounds.get(action);

      if (button == null || bounds == null) {
        continue;
      }

      button.x(bounds.x);
      button.y(bounds.y);
      button.width(bounds.width);
      button.height(bounds.height);
    }
  }

  /**
   * Renders all crafting action buttons on the provided graphics context.
   *
   * <p>Draws button backgrounds with appropriate colors based on hover and pressed states,
   * and renders the action icons centered within each button.
   *
   * @param g the Graphics2D object to render to
   */
  public void draw(Graphics2D g) {
    StageHandle stage = Game.stage().orElse(null);
    int mouseX = stage == null ? Integer.MIN_VALUE : stage.mouseX();
    int mouseY = stage == null ? Integer.MIN_VALUE : stage.mouseY();
    boolean leftPressed = InputManager.isButtonPressed(MouseButtons.LEFT);

    for (CraftingDialogAction action : CraftingDialogAction.values()) {
      ImageButton button = actionButtons.get(action);
      if (button == null) {
        continue;
      }

      Rectangle bounds = new Rectangle(button.x(), button.y(), button.width(), button.height());
      boolean hovered = bounds.contains(mouseX, mouseY);
      boolean pressed = hovered && leftPressed;

      drawActionBoxBackground(g, bounds, hovered, pressed);

      BufferedImage icon = ImageFrameResolver.toImage(button.animation().update());
      if (icon != null) {
        drawCenteredButtonIcon(g, icon, bounds);
      }
    }
  }

  /**
   * Finds the crafting action at the specified mouse coordinates.
   *
   * @param mouseX the x-coordinate of the mouse pointer
   * @param mouseY the y-coordinate of the mouse pointer
   * @return an Optional containing the CraftingDialogAction at the coordinates, or an empty
   *     Optional if no action button is at that location
   */
  public Optional<CraftingDialogAction> findActionAt(int mouseX, int mouseY) {
    for (Map.Entry<CraftingDialogAction, ImageButton> entry : actionButtons.entrySet()) {
      ImageButton button = entry.getValue();
      Rectangle bounds = new Rectangle(button.x(), button.y(), button.width(), button.height());
      if (bounds.contains(mouseX, mouseY)) {
        return Optional.of(entry.getKey());
      }
    }

    return Optional.empty();
  }

  /**
   * Triggers the callback for the specified crafting action.
   *
   * <p>Resolves and executes the callback associated with the given action. For the CRAFT
   * action, the crafting payload is passed to the callback.
   *
   * @param action the crafting action to trigger
   */
  public void trigger(CraftingDialogAction action) {
    if (action == null) {
      return;
    }

    DialogCallbackResolver.createButtonCallback(dialogId, action.callbackKey())
        .accept(action == CraftingDialogAction.CRAFT ? controller.craftingPayload() : null);
  }

  private void drawActionBoxBackground(
      Graphics2D g, Rectangle bounds, boolean hovered, boolean pressed) {
    Color fill =
        pressed ? ACTION_BOX_PRESSED_FILL : hovered ? ACTION_BOX_HOVER_FILL : ACTION_BOX_FILL;

    Color border = hovered ? ACTION_BOX_HOVER_BORDER : ACTION_BOX_BORDER;

    g.setColor(fill);
    g.fillRoundRect(
        bounds.x, bounds.y, bounds.width, bounds.height, ACTION_BOX_ARC, ACTION_BOX_ARC);

    g.setColor(border);
    g.drawRoundRect(
        bounds.x, bounds.y, bounds.width, bounds.height, ACTION_BOX_ARC, ACTION_BOX_ARC);
  }

  private void drawCenteredButtonIcon(Graphics2D g, BufferedImage icon, Rectangle bounds) {
    int maxWidth = bounds.width - 2 * BUTTON_ICON_PADDING;
    int maxHeight = bounds.height - 2 * BUTTON_ICON_PADDING;

    double scale =
        Math.min(
            maxWidth / (double) Math.max(1, icon.getWidth()),
            maxHeight / (double) Math.max(1, icon.getHeight()));

    int drawWidth = Math.max(1, (int) Math.round(icon.getWidth() * scale));
    int drawHeight = Math.max(1, (int) Math.round(icon.getHeight() * scale));
    int drawX = bounds.x + (bounds.width - drawWidth) / 2;
    int drawY = bounds.y + (bounds.height - drawHeight) / 2;

    g.drawImage(icon, drawX, drawY, drawWidth, drawHeight, null);
  }
}
