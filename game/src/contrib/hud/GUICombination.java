package contrib.hud;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.utils.Align;

import core.Game;

import java.util.Arrays;

public class GUICombination extends HorizontalGroup {

    public GUICombination(Actor... guis) {
        Arrays.stream(guis).forEach(this::addActor);
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
}
