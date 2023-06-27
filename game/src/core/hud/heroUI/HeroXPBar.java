package core.hud.heroUI;

import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import contrib.components.XPComponent;

/** This class represents the XPBar of the Hero */
public class HeroXPBar extends ProgressBar {

    public HeroXPBar() {
        super(0f, 1f, 0.01f, false, new Skin());
    }

    /**
     * Updates the XPBar of the Hero based on his current level progress
     *
     * @param xc the XPComponent of the Hero
     */
    protected void updateXPBar(float xpPercentage) {
        this.setValue(xpPercentage);
    }

}
