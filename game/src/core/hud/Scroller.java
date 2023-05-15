package core.hud;

import com.badlogic.gdx.scenes.scene2d.ui.*;

/** allows elements to be added to the scroller */
public class Scroller extends ScrollPane {

    public Scroller(Skin skin, Label labelContent) {
        super(labelContent, skin);
        this.setFadeScrollBars(false);
        this.setScrollbarsVisible(true);
    }

    public Scroller(Skin skin, Image imageContent) {
        super(imageContent, skin);
        this.setFadeScrollBars(false);
        this.setScrollbarsVisible(true);
    }

    public Scroller(Skin skin, VerticalGroup vertGroupContent) {
        super(vertGroupContent, skin);
        this.setFadeScrollBars(false);
        this.setScrollbarsVisible(true);
    }
}
