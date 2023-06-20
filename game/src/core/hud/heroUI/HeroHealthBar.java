package core.hud.heroUI;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import contrib.components.HealthComponent;

import core.Game;

/** This class represents the HealthBar of the Hero */
public class HeroHealthBar extends ProgressBar {

    /** Creates a new HealthBar for the Hero */
    public HeroHealthBar() {
        super(0f, 1f, 0.01f, false, new Skin());
    }

    /**
     * Updates the HealthBar of the Hero based on his current health percentage
     *
     * @param hc the HealthComponent of the Hero
     */
    protected void updateHealthBar(HealthComponent hc) {
        float healthPercentage = (float) hc.currentHealthpoints() / hc.maximalHealthpoints() * 100;

        this.setValue(healthPercentage);
    }

    /**
     * Creates a popup on the screen with how much hp the hero lost or gained
     *
     * @param hpChange the amount of HP to display
     */
    public void createHPPopup(int hpChange) {
        Label hpPopup = new Label("%+d HP".formatted(hpChange), new Skin());
        hpPopup.addAction(Actions.sequence(Actions.moveBy(0, 50, 1), Actions.removeActor()));
        Game.stage().get().addActor(hpPopup);
    }
}
