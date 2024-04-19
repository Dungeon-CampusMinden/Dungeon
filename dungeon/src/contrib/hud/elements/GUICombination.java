package contrib.hud.elements;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import contrib.components.UIComponent;
import core.Game;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * An object of this class represents a combination of multiple {@link CombinableGUI
 * CombinableGUIs}.
 *
 * <p>This class calculates the position and available space for each {@link CombinableGUI}, calling
 * the methods of the {@link CombinableGUI} to draw the elements and calculate the preferred size.
 *
 * <p>The class inherits from {@link Group}, allowing it to be added to a {@link
 * com.badlogic.gdx.scenes.scene2d.Stage Stage} for display. This addition should be facilitated
 * through the use of a {@link UIComponent}.
 */
public final class GUICombination extends Group {

  /** WTF? . */
  public static final int GAP = 10;

  private final DragAndDrop dragAndDrop;
  private final ArrayList<CombinableGUI> combinableGuis;
  private final int guisPerRow;
  private boolean isFullScreen = false;

  /**
   * Creates a GUICombination, a combination of multiple CombinableGUI elements.
   *
   * @param guisPerRow The number of CombinableGUI elements to display per row.
   * @param combinableGuis The CombinableGUI elements to be combined.
   */
  public GUICombination(int guisPerRow, final CombinableGUI... combinableGuis) {
    this.guisPerRow = guisPerRow;
    this.dragAndDrop = new DragAndDrop();
    this.setSize(Game.stage().orElseThrow().getWidth(), Game.stage().orElseThrow().getHeight());
    this.setPosition(0, 0);
    this.combinableGuis = new ArrayList<>(Arrays.asList(combinableGuis));
    this.combinableGuis.forEach(
        combinableGUI -> {
          combinableGUI.dragAndDrop(this.dragAndDrop);
          this.addActor(combinableGUI.actor());
        });
    this.scalePositionChildren();
  }

  /**
   * Creates a GUICombination, a combination of multiple CombinableGUI elements.
   *
   * @param combinableGuis The CombinableGUI elements to be combined.
   */
  public GUICombination(final CombinableGUI... combinableGuis) {
    this(2, combinableGuis);
  }

  private void scalePositionChildren() {
    int rows = (int) Math.ceil(this.combinableGuis.size() / (float) this.guisPerRow);
    int columns = Math.min(this.combinableGuis.size(), this.guisPerRow);
    int width = ((int) this.getWidth() - (GAP * (columns + 1))) / columns;
    int height = ((int) this.getHeight() - (GAP * (rows + 1))) / rows;

    for (int i = 0; i < this.combinableGuis.size(); i++) {
      CombinableGUI combinableGUI = this.combinableGuis.get(i);
      int row = i / columns;
      int column = i % columns;
      AvailableSpace avs =
          new AvailableSpace(
              column * width + (column + 1) * GAP, row * height + (row + 1) * GAP, width, height);
      Vector2 size = combinableGUI.preferredSize(avs);
      combinableGUI.width((int) size.x);
      combinableGUI.height((int) size.y);
      combinableGUI.x(avs.x + (avs.width - (int) size.x) / 2);
      combinableGUI.y(avs.y + (avs.height - (int) size.y) / 2);
      combinableGUI.boundsUpdate();
    }
  }

  @Override
  public void act(float delta) {
    int stageWidth = (int) Game.stage().orElseThrow().getWidth();
    int stageHeight = (int) Game.stage().orElseThrow().getHeight();
    if (stageWidth != this.getWidth() || stageHeight != this.getHeight()) {
      if (!this.fullScreen()) {
        this.setSize(stageWidth, stageHeight);
      }
      this.setPosition(0, 0);
      this.scalePositionChildren();
    }
  }

  public boolean fullScreen() {
    return this.isFullScreen;
  }

  public void fullScreen(boolean fullScreen) {
    this.isFullScreen = fullScreen;
  }

  @Override
  public void draw(final Batch batch, float parentAlpha) {
    this.combinableGuis.forEach(combinableGUI -> combinableGUI.draw(batch));
    this.combinableGuis.forEach(combinableGUI -> combinableGUI.drawTopLayer(batch));
  }

  @Override
  public void drawDebug(final ShapeRenderer shapes) {
    this.combinableGuis.forEach(CombinableGUI::drawDebug);
  }

  /**
   * Returns the list of CombinableGUI elements that are part of this GUICombination.
   *
   * @return An ArrayList of CombinableGUI elements.
   */
  public ArrayList<CombinableGUI> combinableGuis() {
    return this.combinableGuis;
  }

  /**
   * WTF? .
   *
   * @param x foo
   * @param y foo
   * @param width foo
   * @param height foo
   */
  public record AvailableSpace(int x, int y, int width, int height) {}
}
