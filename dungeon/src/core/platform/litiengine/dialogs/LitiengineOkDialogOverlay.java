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
 * A minimal real OK dialog for the LITIENGINE backend.
 *
 * <p>Rendered as a custom overlay via the LITIENGINE Graphics2D render bridge.
 */
final class LitiengineOkDialogOverlay implements LitiengineUiOverlay {

  private static final int DEFAULT_WIDTH = 460;
  private static final int DEFAULT_HEIGHT = 220;
  private static final int PADDING = 20;
  private static final int BUTTON_WIDTH = 120;
  private static final int BUTTON_HEIGHT = 34;

  private final String title;
  private final String text;
  private final String dialogId;

  private int x;
  private int y;
  private int width = DEFAULT_WIDTH;
  private int height = DEFAULT_HEIGHT;
  private boolean visible = true;
  private boolean okPressed = false;

  LitiengineOkDialogOverlay(String title, String text, String dialogId) {
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

    AlphaComposite oldComposite = (AlphaComposite) g.getComposite();
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

      Rectangle ok = okBounds();
      g.setColor(okPressed ? new Color(105, 135, 190) : new Color(75, 95, 140));
      g.fillRoundRect(ok.x, ok.y, ok.width, ok.height, 10, 10);

      g.setColor(Color.WHITE);
      g.drawRoundRect(ok.x, ok.y, ok.width, ok.height, 10, 10);

      String label = "OK";
      FontMetrics buttonFm = g.getFontMetrics();
      int tx = ok.x + (ok.width - buttonFm.stringWidth(label)) / 2;
      int ty = ok.y + ((ok.height - buttonFm.getHeight()) / 2) + buttonFm.getAscent();
      g.drawString(label, tx, ty);
    } finally {
      g.setComposite(oldComposite);
      g.setColor(oldColor);
      g.setFont(oldFont);
    }
  }

  private void handleInput() {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      return;
    }

    int mouseX = stage.mouseX();
    int mouseY = stage.mouseY();
    Rectangle ok = okBounds();

    if (InputManager.isButtonJustPressed(MouseButtons.LEFT) && ok.contains(mouseX, mouseY)) {
      okPressed = true;
    }

    if (okPressed && InputManager.isButtonJustReleased(MouseButtons.LEFT)) {
      boolean releasedInside = ok.contains(mouseX, mouseY);
      okPressed = false;

      if (releasedInside) {
        DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_CONFIRM)
          .accept(null);
      }
    }
  }

  private Rectangle okBounds() {
    int bx = x + (width - BUTTON_WIDTH) / 2;
    int by = y + height - BUTTON_HEIGHT - 18;
    return new Rectangle(bx, by, BUTTON_WIDTH, BUTTON_HEIGHT);
  }

  private static List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
    List<String> lines = new ArrayList<>();
    if (text == null || text.isBlank()) {
      lines.add("");
      return lines;
    }

    for (String paragraph : text.split("\n")) {
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

      if (paragraph.isBlank()) {
        lines.add("");
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
