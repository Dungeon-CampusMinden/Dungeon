package core.platform.litiengine.dialogs;

import contrib.hud.dialogs.DialogCallbackResolver;
import contrib.hud.dialogs.DialogContextKeys;
import core.Game;
import core.input.MouseButtons;
import core.platform.litiengine.ui.LitiengineUiOverlay;
import core.ui.StageHandle;
import core.utils.InputManager;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * A minimal real yes/no dialog for the LITIENGINE backend.
 *
 * <p>Rendered as a custom overlay via the LITIENGINE Graphics2D render bridge.
 */
final class LitiengineYesNoDialogOverlay implements LitiengineUiOverlay {

  private static final int DEFAULT_WIDTH = 500;
  private static final int DEFAULT_HEIGHT = 230;
  private static final int PADDING = 20;
  private static final int BUTTON_WIDTH = 120;
  private static final int BUTTON_HEIGHT = 34;
  private static final int BUTTON_GAP = 20;

  private static final String YES_LABEL = "Ja";
  private static final String NO_LABEL = "Nein";

  private final String title;
  private final String text;
  private final String dialogId;

  private int x;
  private int y;
  private int width = DEFAULT_WIDTH;
  private int height = DEFAULT_HEIGHT;
  private boolean visible = true;

  private boolean yesPressed = false;
  private boolean noPressed = false;

  LitiengineYesNoDialogOverlay(String title, String text, String dialogId) {
    this.title = title;
    this.text = text;
    this.dialogId = dialogId;
  }

  @Override
  public void render(Graphics2D g) {
    if (!visible) {
      return;
    }

    handleInput();

    var oldComposite = g.getComposite();
    Color oldColor = g.getColor();
    Font oldFont = g.getFont();

    try {
      int windowWidth = Game.windowWidth();
      int windowHeight = Game.windowHeight();

      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
      g.setColor(Color.BLACK);
      g.fillRect(0, 0, windowWidth, windowHeight);

      g.setComposite(AlphaComposite.SrcOver);
      g.setColor(new Color(32, 32, 40, 235));
      g.fillRoundRect(x, y, width, height, 14, 14);

      g.setColor(new Color(180, 180, 210));
      g.drawRoundRect(x, y, width, height, 14, 14);

      g.setColor(Color.WHITE);
      g.setFont(oldFont.deriveFont(Font.BOLD, 18f));
      g.drawString(title, x + PADDING, y + 32);

      g.setFont(oldFont.deriveFont(15f));
      FontMetrics fm = g.getFontMetrics();

      int textY = y + 62;
      for (String line : wrapText(text, fm, width - 2 * PADDING)) {
        g.drawString(line, x + PADDING, textY);
        textY += fm.getHeight();
      }

      Rectangle no = noBounds();
      Rectangle yes = yesBounds();

      drawButton(g, no, NO_LABEL, noPressed);
      drawButton(g, yes, YES_LABEL, yesPressed);
    } finally {
      g.setComposite(oldComposite);
      g.setColor(oldColor);
      g.setFont(oldFont);
    }
  }

  private void drawButton(Graphics2D g, Rectangle bounds, String label, boolean pressed) {
    g.setColor(pressed ? new Color(105, 135, 190) : new Color(75, 95, 140));
    g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);

    g.setColor(Color.WHITE);
    g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);

    FontMetrics fm = g.getFontMetrics();
    int tx = bounds.x + (bounds.width - fm.stringWidth(label)) / 2;
    int ty = bounds.y + ((bounds.height - fm.getHeight()) / 2) + fm.getAscent();
    g.drawString(label, tx, ty);
  }

  private void handleInput() {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      return;
    }

    int mouseX = stage.mouseX();
    int mouseY = stage.mouseY();

    Rectangle no = noBounds();
    Rectangle yes = yesBounds();

    if (InputManager.isButtonJustPressed(MouseButtons.LEFT)) {
      noPressed = no.contains(mouseX, mouseY);
      yesPressed = yes.contains(mouseX, mouseY);
    }

    if (InputManager.isButtonJustReleased(MouseButtons.LEFT)) {
      boolean releasedOnNo = noPressed && no.contains(mouseX, mouseY);
      boolean releasedOnYes = yesPressed && yes.contains(mouseX, mouseY);

      noPressed = false;
      yesPressed = false;

      if (releasedOnYes) {
        DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_YES)
          .accept(null);
      } else if (releasedOnNo) {
        DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_NO)
          .accept(null);
      }
    }
  }

  private Rectangle noBounds() {
    int totalWidth = 2 * BUTTON_WIDTH + BUTTON_GAP;
    int startX = x + (width - totalWidth) / 2;
    int by = y + height - BUTTON_HEIGHT - 18;
    return new Rectangle(startX, by, BUTTON_WIDTH, BUTTON_HEIGHT);
  }

  private Rectangle yesBounds() {
    Rectangle no = noBounds();
    return new Rectangle(no.x + BUTTON_WIDTH + BUTTON_GAP, no.y, BUTTON_WIDTH, BUTTON_HEIGHT);
  }

  private static List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
    List<String> lines = new ArrayList<>();
    if (text == null || text.isBlank()) {
      lines.add("");
      return lines;
    }

    for (String paragraph : text.split("\n")) {
      if (paragraph.isBlank()) {
        lines.add("");
        continue;
      }

      String[] words = paragraph.trim().split("\\s+");
      StringBuilder current = new StringBuilder();

      for (String word : words) {
        String candidate = current.isEmpty() ? word : current + " " + word;
        if (fm.stringWidth(candidate) <= maxWidth) {
          current.setLength(0);
          current.append(candidate);
        } else {
          if (!current.isEmpty()) {
            lines.add(current.toString());
          }
          current.setLength(0);
          current.append(word);
        }
      }

      if (!current.isEmpty()) {
        lines.add(current.toString());
      }
    }

    return lines;
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
