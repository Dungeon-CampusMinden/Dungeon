package core.hud.heroUI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import contrib.components.HealthComponent;

import core.hud.LabelStyleBuilder;
import core.hud.ScreenImage;
import core.hud.ScreenText;
import core.utils.Constants;
import core.utils.Point;

/** This class represents the HealthBar of the Hero */
public class HeroHealthBar extends ScreenImage {

    /**
     * Creates a new HealthBar for the Hero
     *
     * @param texturePath the Path to the Texture
     * @param position the Position where the Image should be drawn
     * @param scale Determination of the scale
     */
    public HeroHealthBar(String texturePath, Point position, float scale) {
        super(texturePath, position, scale);
    }

    /**
     * Updates the HealthBar of the Hero based on his current health percentage
     *
     * @param hc the HealthComponent of the Hero
     */
    protected void updateHealthBar(HealthComponent hc) {
        float healthPercentage =
                (float) hc.getCurrentHealthpoints() / hc.getMaximalHealthpoints() * 100;
        if (healthPercentage <= 0) {
            this.setTexture("hud/healthBar/healthBar_7.png");
        } else if (healthPercentage <= 20) {
            this.setTexture("hud/healthBar/healthBar_6.png");
        } else if (healthPercentage <= 36) {
            this.setTexture("hud/healthBar/healthBar_5.png");
        } else if (healthPercentage <= 52) {
            this.setTexture("hud/healthBar/healthBar_4.png");
        } else if (healthPercentage <= 68) {
            this.setTexture("hud/healthBar/healthBar_3.png");
        } else if (healthPercentage <= 84) {
            this.setTexture("hud/healthBar/healthBar_2.png");
        } else if (healthPercentage <= 100) {
            this.setTexture("hud/healthBar/healthBar_1.png");
        }
    }

    /**
     * Creates a popup on the screen with how much hp the hero lost or gained
     *
     * @param hpChange the amount of HP to display
     * @param font the font to use
     */
    public void createHPPopup(int hpChange, BitmapFont font) {
        Color fontColor = hpChange > 0 ? Color.GREEN : Color.RED;
        ScreenText hpPopup =
                new ScreenText(
                        "%+d HP".formatted(hpChange),
                        new Point(Constants.WINDOW_WIDTH - 55, 30),
                        1,
                        new LabelStyleBuilder(font).setFontcolor(fontColor).build());
        hpPopup.addAction(Actions.sequence(Actions.moveBy(0, 50, 1), Actions.removeActor()));
        HeroUI.getHeroUI().add(hpPopup);
    }
}
