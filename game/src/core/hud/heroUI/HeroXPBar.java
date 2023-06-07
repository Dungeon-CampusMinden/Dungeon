package core.hud.heroUI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import contrib.components.XPComponent;

import core.hud.LabelStyleBuilder;
import core.hud.ScreenImage;
import core.hud.ScreenText;
import core.utils.Constants;
import core.utils.Point;

/** This class represents the XPBar of the Hero */
public class HeroXPBar extends ScreenImage {

    /**
     * Creates a new XPBar for the Hero
     *
     * @param texturePath the Path to the Texture
     * @param position the Position where the Image should be drawn
     * @param scale Determination of the scale
     */
    public HeroXPBar(String texturePath, Point position, float scale) {
        super(texturePath, position, scale);
    }

    /**
     * Updates the XPBar of the Hero based on his current level progress
     *
     * @param xc the XPComponent of the Hero
     */
    protected void updateXPBar(XPComponent xc) {
        float xpPercentage =
                (float) xc.getCurrentXP() / (xc.getXPToNextLevel() + xc.getCurrentXP()) * 100;
        if (xpPercentage <= 10) {
            this.setTexture("hud/xpBar/xpBar_7.png");
        } else if (xpPercentage <= 20) {
            this.setTexture("hud/xpBar/xpBar_6.png");
        } else if (xpPercentage <= 36) {
            this.setTexture("hud/xpBar/xpBar_5.png");
        } else if (xpPercentage <= 52) {
            this.setTexture("hud/xpBar/xpBar_4.png");
        } else if (xpPercentage <= 68) {
            this.setTexture("hud/xpBar/xpBar_3.png");
        } else if (xpPercentage <= 84) {
            this.setTexture("hud/xpBar/xpBar_2.png");
        } else if (xpPercentage <= 100) {
            this.setTexture("hud/xpBar/xpBar_1.png");
        }
    }

    /**
     * Creates a popup on the screen with how much xp the hero lost or gained
     *
     * @param xpChange the amount of XP to display
     * @param font the font to use
     */
    public void createXPPopup(long xpChange, BitmapFont font) {
        Color fontColor = xpChange > 0 ? Color.GREEN : Color.RED;
        ScreenText xpPopup =
                new ScreenText(
                        "%+d XP".formatted(xpChange),
                        new Point(Constants.WINDOW_WIDTH / 2f, Constants.WINDOW_HEIGHT / 2f),
                        1,
                        new LabelStyleBuilder(font).setFontcolor(fontColor).build());
        xpPopup.addAction(Actions.sequence(Actions.moveBy(0, 50, 1), Actions.removeActor()));
        HeroUI.getHeroUI().add(xpPopup);
    }
}
