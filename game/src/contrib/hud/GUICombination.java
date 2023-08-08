package contrib.hud;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Align;

import core.Game;

import java.util.Arrays;

public class GUICombination extends HorizontalGroup {

    private final DragAndDrop dragAndDrop;

    public GUICombination(GUI... guis) {
        this.dragAndDrop = new DragAndDrop();
        Arrays.stream(guis)
                .forEach(
                        a -> {
                            this.addActor(a);
                            a.dragAndDrop(this.dragAndDrop);
                        });
        this.space(10.0f);
        this.validate();
        this.center();
        this.pack();
        this.setPosition(
                Game.stage().orElseThrow().getWidth() / 2.0f,
                Game.stage().orElseThrow().getHeight() / 2.0f,
                Align.center);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        this.invalidateHierarchy();
        this.pack();
    }

    @Override
    public void validate() {
        super.validate();
        this.setPosition(
                Game.stage().orElseThrow().getWidth() / 2.0f,
                Game.stage().orElseThrow().getHeight() / 2.0f,
                Align.center);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }

    /**
     * Get the drag and drop object
     *
     * @return the drag and drop object
     */
    public DragAndDrop dragAndDrop() {
        return this.dragAndDrop;
    }
}
