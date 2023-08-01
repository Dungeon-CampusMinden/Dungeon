package contrib.hud.inventory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

import core.Game;

public class InventoryGUI extends Widget {

    private static final Texture texture;

    static {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.RED);
        pixmap.fill();
        texture = new Texture(pixmap);
    }

    public InventoryGUI() {
        super();
        this.setColor(Color.RED);
        this.setSize(25, 25);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // super.draw(batch, parentAlpha);
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public float getMinWidth() {
        return 50;
    }

    @Override
    public float getMinHeight() {
        return 50;
    }

    @Override
    public float getPrefWidth() {
        int nrOfChildren = this.getParent().getChildren().size;
        return Math.min(
                Game.stage().orElseThrow().getHeight() * 0.9f,
                Game.stage().orElseThrow().getWidth() / (float) nrOfChildren
                        - 30.0f); // -30.0f um etwas Platz um die Inventories zu lassen (Padding)
    }

    @Override
    public float getPrefHeight() {
        return this.getPrefWidth(); // Quadratisch
    }

    @Override
    public float getMaxWidth() {
        return Game.stage().orElseThrow().getHeight() * 0.9f;
    }

    @Override
    public float getMaxHeight() {
        return this.getMaxWidth();
    }

    @Override
    public void layout() {
        super.layout();
    }

    @Override
    public void validate() {
        super.validate();
    }
}
