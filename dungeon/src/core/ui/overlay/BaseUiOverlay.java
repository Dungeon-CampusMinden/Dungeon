package core.ui.overlay;

import java.awt.Rectangle;

/** Base implementation for overlays with rectangular bounds and visibility. */
public abstract class BaseUiOverlay implements UiOverlay {

  protected int x;
  protected int y;
  protected int width;
  protected int height;
  protected boolean visible = true;
  private boolean autoCenter = true;

  protected BaseUiOverlay() {}

  protected BaseUiOverlay(int width, int height) {
    this.width = width;
    this.height = height;
  }

  protected BaseUiOverlay(int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.autoCenter = false;
  }

  @Override
  public int x() {
    return x;
  }

  @Override
  public void x(int x) {
    this.x = x;
    this.autoCenter = false;
  }

  @Override
  public int y() {
    return y;
  }

  @Override
  public void y(int y) {
    this.y = y;
    this.autoCenter = false;
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

  protected final Rectangle bounds() {
    return new Rectangle(x, y, width, height);
  }

  protected final void centerIn(int outerWidth, int outerHeight) {
    x = (outerWidth - width) / 2;
    y = (outerHeight - height) / 2;
  }

  protected final void centerInIfUnpositioned(int outerWidth, int outerHeight) {
    if (autoCenter) {
      centerIn(outerWidth, outerHeight);
    }
  }
}
