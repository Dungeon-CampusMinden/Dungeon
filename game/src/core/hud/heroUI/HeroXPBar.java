package core.hud.heroUI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import contrib.components.XPComponent;

import core.Game;

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
    protected void updateXPBar(XPComponent xc) {
        float xpPercentage = (float) xc.currentXP() / (xc.xpToNextLevel() + xc.currentXP()) * 100;
        this.setValue(xpPercentage);
    }

    /**
     * Creates a popup on the screen with how much xp the hero lost or gained
     *
     * @param xpChange the amount of XP to display
     * @param font the font to use
     */
    public void createXPPopup(long xpChange, BitmapFont font) {
        Color fontColor = xpChange > 0 ? Color.GREEN : Color.RED;
        Label xpPopup = new Label("%+d XP".formatted(xpChange), new Skin());
        xpPopup.addAction(Actions.sequence(Actions.moveBy(0, 50, 1), Actions.removeActor()));
        Game.stage().get().addActor(xpPopup);
    }
}
