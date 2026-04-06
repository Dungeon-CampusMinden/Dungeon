package contrib.hud.elements;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import contrib.platform.gdx.hud.GdxGuiInteractionContext;
import contrib.platform.gdx.hud.GdxGuiRenderContext;
import core.Game;
import core.utils.Vector2;
import java.util.ArrayList;
import java.util.Arrays;

public class GUICombination extends Group {

  public static final int GAP = 10;

  private final DragAndDrop dragAndDrop;
  private final ArrayList<CombinableGUI> combinableGuis;
  private final int guisPerRow;

  public GUICombination(int guisPerRow, final CombinableGUI... combinableGuis) {
    this.guisPerRow = guisPerRow;
    this.setPosition(0, 0);
    this.combinableGuis = new ArrayList<>(Arrays.asList(combinableGuis));

    if (Game.isHeadless()) {
      this.dragAndDrop = null;
      this.combinableGuis.forEach(
        combinableGUI -> combinableGUI.interactionContext(new GuiInteractionContext() {}));
      return;
    }

    this.dragAndDrop = new DragAndDrop();
    this.setSize(Game.stage().orElseThrow().getWidth(), Game.stage().orElseThrow().getHeight());

    this.combinableGuis.forEach(
      combinableGUI -> {
        Actor anchor = new Actor();
        combinableGUI.interactionContext(new GdxGuiInteractionContext(anchor, this.dragAndDrop));
        this.addActor(anchor);
      });

    this.scalePositionChildren();
  }

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

      GuiAvailableSpace availableSpace =
        new GuiAvailableSpace(
          column * width + (column + 1) * GAP,
          row * height + (row + 1) * GAP,
          width,
          height);

      Vector2 size = combinableGUI.preferredSize(availableSpace);
      combinableGUI.width((int) size.x());
      combinableGUI.height((int) size.y());
      combinableGUI.x(availableSpace.x() + (availableSpace.width() - (int) size.x()) / 2);
      combinableGUI.y(availableSpace.y() + (availableSpace.height() - (int) size.y()) / 2);

      combinableGUI
        .interactionContext(GdxGuiInteractionContext.class)
        .flatMap(GdxGuiInteractionContext::actor)
        .ifPresent(
          actor ->
            actor.setBounds(
              combinableGUI.x(),
              combinableGUI.y(),
              combinableGUI.width(),
              combinableGUI.height()));
    }
  }

  @Override
  public void act(float delta) {
    int stageWidth = (int) Game.stage().orElseThrow().getWidth();
    int stageHeight = (int) Game.stage().orElseThrow().getHeight();
    if (stageWidth != this.getWidth() || stageHeight != this.getHeight()) {
      this.setSize(stageWidth, stageHeight);
      this.setPosition(0, 0);
      this.scalePositionChildren();
    }
  }

  @Override
  public void draw(final Batch batch, float parentAlpha) {
    GuiRenderContext renderContext = new GdxGuiRenderContext(batch);
    this.combinableGuis.forEach(combinableGUI -> combinableGUI.render(renderContext));
    this.combinableGuis.forEach(combinableGUI -> combinableGUI.renderTopLayer(renderContext));
  }

  @Override
  public void drawDebug(final ShapeRenderer shapes) {
    this.combinableGuis.forEach(CombinableGUI::drawDebug);
  }

  public ArrayList<CombinableGUI> combinableGuis() {
    return this.combinableGuis;
  }
}
