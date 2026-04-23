package contrib.modules.interaction.ui;

import contrib.modules.interaction.IInteractable;
import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionChoices;
import core.Game;
import core.input.MouseButtons;
import core.ui.overlay.OverlayManager;
import core.ui.StageHandle;
import core.ui.overlay.BaseUiOverlay;
import core.utils.InputManager;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents a UI overlay that displays a panel for selecting an interaction.
 * This overlay is rendered on top of the game screen, providing a list of available
 * interactions and a cancel option.
 *
 * <p>The selection is handled via mouse clicks,
 * and the panel can be closed by either selecting an interaction or clicking outside the panel.
 *
 * <p>The panel is centered within the game window and styled with specific visual
 * elements such as rounded corners and hover effects.
 *
 * <p>Key features:
 * <ul>
 *   <li>Dynamically adjusts its height based on the number of interactions.</li>
 *   <li>Handles user input (mouse clicks) to select an interaction or cancel the selection.</li>
 *   <li>Supports smooth integration with the game's UI system.</li>
 * </ul>
 *
 * <p>The overlay becomes inactive (hidden) after a selection is made or canceled.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Renders the interaction selection panel with styled buttons.</li>
 *   <li>Detects user input for interaction selection or cancellation.</li>
 *   <li>Notifies a consumer callback upon selection or cancellation.</li>
 *   <li>Manages panel visibility and position properties.</li>
 * </ul>
 */
final class InteractionSelectionOverlay extends BaseUiOverlay {

  private static final int PANEL_WIDTH = 320;
  private static final int PANEL_PADDING = 16;
  private static final int TITLE_HEIGHT = 24;
  private static final int BUTTON_HEIGHT = 30;
  private static final int BUTTON_GAP = 8;
  private static final String TITLE = "Choose interaction";
  private static final String CANCEL_LABEL = "Cancel";

  private final List<Interaction> interactions;
  private final Consumer<Interaction> onSelected;

  private boolean leftButtonDownLastFrame = false;

  InteractionSelectionOverlay(
    IInteractable interactable, Consumer<Interaction> onSelected) {
    super(PANEL_WIDTH, 220);
    this.interactions = InteractionChoices.from(Objects.requireNonNull(interactable));
    this.onSelected = Objects.requireNonNull(onSelected);
  }

  @Override
  public void render(Graphics2D g) {
    if (!visible) {
      return;
    }

    height =
      PANEL_PADDING * 3
        + TITLE_HEIGHT
        + (interactions.size() + 1) * BUTTON_HEIGHT
        + interactions.size() * BUTTON_GAP;

    centerInIfUnpositioned(Game.windowWidth(), Game.windowHeight());

    drawOverlay(g);
    handleInput();
  }

  private void drawOverlay(Graphics2D g) {
    Composite oldComposite = g.getComposite();
    Color oldColor = g.getColor();
    Font oldFont = g.getFont();

    try {
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
      g.setColor(Color.BLACK);
      g.fillRect(0, 0, Game.windowWidth(), Game.windowHeight());

      g.setComposite(oldComposite);
      g.setColor(new Color(24, 24, 30, 235));
      g.fillRoundRect(x, y, width, height, 16, 16);

      g.setColor(new Color(220, 220, 230));
      g.drawRoundRect(x, y, width, height, 16, 16);

      g.setFont(g.getFont().deriveFont(Font.BOLD, 15f));
      FontMetrics titleMetrics = g.getFontMetrics();
      g.drawString(TITLE, x + PANEL_PADDING, y + PANEL_PADDING + titleMetrics.getAscent());

      StageHandle stage = Game.stage().orElse(null);
      int mouseX = stage == null ? Integer.MIN_VALUE : stage.mouseX();
      int mouseY = stage == null ? Integer.MIN_VALUE : stage.mouseY();

      for (int i = 0; i < interactions.size(); i++) {
        drawButton(g, interactionBounds(i), interactions.get(i).displayName(), mouseX, mouseY);
      }

      drawButton(g, cancelBounds(), CANCEL_LABEL, mouseX, mouseY);
    } finally {
      g.setComposite(oldComposite);
      g.setColor(oldColor);
      g.setFont(oldFont);
    }
  }

  private void drawButton(Graphics2D g, Rectangle bounds, String label, int mouseX, int mouseY) {
    boolean hovered = bounds.contains(mouseX, mouseY);

    g.setColor(hovered ? new Color(85, 105, 150) : new Color(60, 70, 90));
    g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);

    g.setColor(new Color(220, 220, 230));
    g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);

    FontMetrics fm = g.getFontMetrics();
    int textX = bounds.x + 10;
    int textY = bounds.y + (bounds.height - fm.getHeight()) / 2 + fm.getAscent();

    g.drawString(label, textX, textY);
  }

  private void handleInput() {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      leftButtonDownLastFrame = false;
      return;
    }

    boolean leftButtonDown = InputManager.isButtonPressed(MouseButtons.LEFT);

    if (leftButtonDown && !leftButtonDownLastFrame) {
      int mouseX = stage.mouseX();
      int mouseY = stage.mouseY();

      for (int i = 0; i < interactions.size(); i++) {
        if (interactionBounds(i).contains(mouseX, mouseY)) {
          close(interactions.get(i));
          leftButtonDownLastFrame = true;
          return;
        }
      }

      if (cancelBounds().contains(mouseX, mouseY) || !panelBounds().contains(mouseX, mouseY)) {
        close(null);
        leftButtonDownLastFrame = true;
        return;
      }
    }

    leftButtonDownLastFrame = leftButtonDown;
  }

  private Rectangle panelBounds() {
    return bounds();
  }

  private Rectangle interactionBounds(int index) {
    int buttonY =
      y + PANEL_PADDING * 2 + TITLE_HEIGHT + index * (BUTTON_HEIGHT + BUTTON_GAP);

    return new Rectangle(
      x + PANEL_PADDING, buttonY, width - 2 * PANEL_PADDING, BUTTON_HEIGHT);
  }

  private Rectangle cancelBounds() {
    int buttonY = y + height - PANEL_PADDING - BUTTON_HEIGHT;
    return new Rectangle(
      x + PANEL_PADDING, buttonY, width - 2 * PANEL_PADDING, BUTTON_HEIGHT);
  }

  private void close(Interaction interaction) {
    visible = false;
    OverlayManager.remove(this);
    onSelected.accept(interaction);
  }
}
