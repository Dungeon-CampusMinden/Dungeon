package contrib.hud.elements;

import core.ui.overlay.UiOverlay;
import core.ui.overlay.UiOverlayRegistry;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Simple LITIENGINE overlay for entity attribute bars.
 *
 * <p>This overlay doubles as the backend-agnostic {@link AttributeBarHandle}, so
 * AttributeBarSystem can continue to update it without knowing the concrete UI backend.
 */
public final class AttributeBarOverlay
  implements UiOverlay, AttributeBarHandle, AttributeBarHandleProvider {

  private static final int DEFAULT_WIDTH = 50;
  private static final int DEFAULT_HEIGHT = 10;

  private final String styleName;

  private int x;
  private int y;
  private int width = DEFAULT_WIDTH;
  private int height = DEFAULT_HEIGHT;
  private boolean visible = true;
  private float value = 1f;

  public AttributeBarOverlay(String styleName) {
    this.styleName = styleName == null ? "" : styleName;
  }

  @Override
  public void render(Graphics2D g) {
    if (!visible) {
      return;
    }

    int drawX = x - (width / 2);
    int drawY = y;

    // background
    g.setColor(new Color(20, 20, 26, 220));
    g.fillRoundRect(drawX, drawY, width, height, 8, 8);

    // fill
    int fillWidth = Math.round(width * clamp01(value));
    if (fillWidth > 0) {
      g.setColor(fillColor());
      g.fillRoundRect(drawX, drawY, fillWidth, height, 8, 8);
    }

    // border
    g.setColor(new Color(220, 220, 230));
    g.drawRoundRect(drawX, drawY, width, height, 8, 8);
  }

  private Color fillColor() {
    return switch (styleName) {
      case "healthbar" -> new Color(190, 55, 55);
      case "manabar" -> new Color(70, 110, 210);
      case "staminabar" -> new Color(70, 180, 90);
      default -> new Color(180, 180, 180);
    };
  }

  private static float clamp01(float v) {
    return Math.max(0f, Math.min(1f, v));
  }

  @Override
  public void remove() {
    visible = false;
    UiOverlayRegistry.remove(this);
  }

  @Override
  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  @Override
  public void setPosition(float x, float y) {
    this.x = Math.round(x);
    this.y = Math.round(y);
  }

  @Override
  public void setValue(float value) {
    this.value = clamp01(value);
  }

  @Override
  public AttributeBarHandle attributeBarHandle() {
    return this;
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
