package contrib.modules.interaction.ui;

import contrib.modules.interaction.IInteractable;
import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionChoices;
import core.Game;
import core.input.MouseButtons;
import core.ui.overlay.UiOverlay;
import core.ui.overlay.UiOverlayRegistry;
import core.ui.StageHandle;
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
 * Simple LITIENGINE overlay for selecting one interaction out of several options.
 *
 * <p>This intentionally mirrors the semantics of the old RingMenu, but uses the existing
 * LITIENGINE overlay infrastructure instead of scene2d.
 */
final class LitiengineInteractionSelectionOverlay implements UiOverlay {

  private static final int PANEL_WIDTH = 320;
  private static final int PANEL_PADDING = 16;
  private static final int TITLE_HEIGHT = 24;
  private static final int BUTTON_HEIGHT = 30;
  private static final int BUTTON_GAP = 8;
  private static final String TITLE = "Choose interaction";
  private static final String CANCEL_LABEL = "Cancel";

  private final List<Interaction> interactions;
  private final Consumer<Interaction> onSelected;

  private int x;
  private int y;
  private int width = PANEL_WIDTH;
  private int height = 220;
  private boolean visible = true;
  private boolean leftButtonDownLastFrame = false;

  LitiengineInteractionSelectionOverlay(
    IInteractable interactable, Consumer<Interaction> onSelected) {
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

    if (x == 0 && y == 0) {
      x = Math.round((Game.windowWidth() - width) / 2f);
      y = Math.round((Game.windowHeight() - height) / 2f);
    }

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
          leftButtonDownLastFrame = leftButtonDown;
          return;
        }
      }

      if (cancelBounds().contains(mouseX, mouseY) || !panelBounds().contains(mouseX, mouseY)) {
        close(null);
        leftButtonDownLastFrame = leftButtonDown;
        return;
      }
    }

    leftButtonDownLastFrame = leftButtonDown;
  }

  private Rectangle panelBounds() {
    return new Rectangle(x, y, width, height);
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
    UiOverlayRegistry.remove(this);
    onSelected.accept(interaction);
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
