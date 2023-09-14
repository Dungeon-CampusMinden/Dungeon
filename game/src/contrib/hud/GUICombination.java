package contrib.hud;

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
 * <p>This class calculates the position and available space for each {@link CombinableGUI} and
 * calls the methods of the {@link CombinableGUI} to draw the elements and to calculate the
 * preferred size.
 *
 * <p>The class inherits from {@link Group} so it can be added to a {@link
 * com.badlogic.gdx.scenes.scene2d.Stage Stage} to be displayed. This should happen through the use
 * of a {@link UIComponent}.
 */
public class GUICombination extends Group {

    public static final int GAP = 10;

    public record AvailableSpace(int x, int y, int width, int height) {}

    private final DragAndDrop dragAndDrop;
    private final ArrayList<CombinableGUI> combinableGuis;
    private final int guisPerRow;

    public GUICombination(int guisPerRow, CombinableGUI... combinableGuis) {
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

    public GUICombination(CombinableGUI... combinableGuis) {
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
                            column * width + (column + 1) * GAP,
                            row * height + (row + 1) * GAP,
                            width,
                            height);
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
            this.setSize(stageWidth, stageHeight);
            this.setPosition(0, 0);
            this.scalePositionChildren();
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        this.combinableGuis.forEach(
                combinableGUI -> {
                    combinableGUI.draw(batch);
                });
        this.combinableGuis.forEach(
                combinableGUI -> {
                    combinableGUI.drawTopLayer(batch);
                });
    }

    @Override
    public void drawDebug(ShapeRenderer shapes) {
        this.combinableGuis.forEach(
                combinableGUI -> {
                    combinableGUI.drawDebug(shapes);
                });
    }
}
